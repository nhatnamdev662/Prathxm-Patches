/*
 * Copyright 2026 PrathxmOp
 * https://github.com/PrathxmOp/Prathxm-Patches
 */

package app.prathxm.chess.extension.stockfish;

import android.app.Activity;
import android.content.Context;
import android.os.Vibrator;
import android.os.VibrationEffect;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MoveClassifier {
    private static final String TAG = "MoveClassifier";

    private static final List<String> fenHistory = new ArrayList<>();
    private static final Map<String, Float> fenToEvalMap = new ConcurrentHashMap<>();
    private static final Map<String, List<String>> fenToBestMovesMap = new ConcurrentHashMap<>();
    private static final Map<String, String> keyToFullFenMap = new ConcurrentHashMap<>();
    /** Line evaluations (MultiPV) for calculating gap between top lines. */
    private static final Map<String, float[]> fenToLineScoresMap = new ConcurrentHashMap<>();
    /** Principal variation lines from this position. */
    private static final Map<String, List<String>> fenToPvMap = new ConcurrentHashMap<>();
    /** Whether this position has a known mate in N. */
    private static final Map<String, Boolean> fenToMateMap = new ConcurrentHashMap<>();
    /** Search depth behind each stored evaluation, so a shallower result never overwrites a deeper one. */
    private static final Map<String, Integer> fenToDepthMap = new ConcurrentHashMap<>();
    /** Moves ("prevKey|currentKey" or "seqLen:uci") that already produced a toast, so a move is rated only once. */
    private static final java.util.Set<String> classifiedMoves = ConcurrentHashMap.newKeySet();

    /** Move and loss tracking across plies for recapture and missed win / miss detection. */
    private static volatile String lastPlayedUci = null;
    private static volatile boolean lastWasCapture = false;
    private static volatile float lastOppLoss = -1f;

    /** Hooked moves from Chess.com positionObject (StandardPosition) */
    private static volatile Object lastPositionObject = null;
    private static volatile List<String> currentPositionMoves = null;

    private static volatile Class<?> cachedMoveConverterClass = null;
    private static volatile Method cachedConvertHistoryMethod = null;
    private static volatile Method cachedConvertMoveMethod = null;
    private static volatile boolean converterResolved = false;

    /** Minimum depth for an interrupted / intermediate search to be trusted for a rating. */
    private static final int MIN_RATING_DEPTH = 8;

    /** 20 legal first moves from startpos for White. Used to verify move list validity for Torch WASM. */
    private static final java.util.Set<String> STARTPOS_LEGAL_FIRST_MOVES = new java.util.HashSet<>(java.util.Arrays.asList(
        "a2a3", "a2a4", "b2b3", "b2b4", "c2c3", "c2c4", "d2d3", "d2d4",
        "e2e3", "e2e4", "f2f3", "f2f4", "g2g3", "g2g4", "h2h3", "h2h4",
        "b1a3", "b1c3", "g1f3", "g1h3"
    ));

    public static void clearHistory() {
        synchronized (fenHistory) {
            fenHistory.clear();
            clearMaps();
        }
        currentPositionMoves = null;
        lastPositionObject = null;
    }

    private static void resolveConverter(ClassLoader cl) {
        if (converterResolved) return;
        try {
            Class<?> clazz = cl.loadClass("com.chess.chessboard.compengine.MoveConverterKt");
            cachedMoveConverterClass = clazz;
            for (Method m : clazz.getMethods()) {
                if (m.getParameterCount() == 1 && m.getReturnType() == String.class) {
                    if ("c".equals(m.getName())) {
                        cachedConvertHistoryMethod = m;
                    } else if ("b".equals(m.getName())) {
                        cachedConvertMoveMethod = m;
                    }
                }
            }
        } catch (Throwable t) {
            TorchEngine.log("[EXTRACT MOVES] Không load được MoveConverterKt: " + t.getMessage());
        } finally {
            converterResolved = true;
        }
    }

    public static List<String> extractMovesFromPosition(Object positionObject) {
        if (positionObject == null) return null;
        try {
            Method hMethod = null;
            try {
                hMethod = positionObject.getClass().getMethod("h");
            } catch (NoSuchMethodException e) {
                for (Method m : positionObject.getClass().getMethods()) {
                    if (m.getName().equals("h") && m.getParameterCount() == 0 && List.class.isAssignableFrom(m.getReturnType())) {
                        hMethod = m;
                        break;
                    }
                }
            }
            if (hMethod == null) return null;

            Object listObj = hMethod.invoke(positionObject);
            if (!(listObj instanceof List)) return null;

            List<?> historyList = (List<?>) listObj;
            if (historyList.isEmpty()) {
                return new ArrayList<>();
            }

            resolveConverter(positionObject.getClass().getClassLoader());

            List<String> moves = new ArrayList<>();
            for (Object item : historyList) {
                if (item == null) continue;
                String uci = null;
                if (cachedConvertHistoryMethod != null) {
                    try {
                        Object res = cachedConvertHistoryMethod.invoke(null, item);
                        if (res instanceof String) {
                            uci = (String) res;
                        }
                    } catch (Throwable ignored) {}
                }
                if (uci == null && cachedConvertMoveMethod != null) {
                    try {
                        Method bMethod = item.getClass().getMethod("b");
                        Object moveObj = bMethod.invoke(item);
                        if (moveObj != null) {
                            Object res = cachedConvertMoveMethod.invoke(null, moveObj);
                            if (res instanceof String) {
                                uci = (String) res;
                            }
                        }
                    } catch (Throwable ignored) {}
                }
                if (uci != null && !uci.trim().isEmpty()) {
                    moves.add(uci.trim().toLowerCase(java.util.Locale.US));
                }
            }
            return moves;
        } catch (Throwable t) {
            TorchEngine.log("[EXTRACT MOVES ERROR] " + t.getMessage());
            return null;
        }
    }

    private static void clearMaps() {
        fenToEvalMap.clear();
        fenToBestMovesMap.clear();
        keyToFullFenMap.clear();
        fenToDepthMap.clear();
        fenToLineScoresMap.clear();
        fenToPvMap.clear();
        fenToMateMap.clear();
        classifiedMoves.clear();
        lastPlayedUci = null;
        lastWasCapture = false;
        lastOppLoss = -1f;
        EloScanner.reset();
    }

    /**
     * Stores the evaluation of a position (final, intermediate or interrupted search). Keeps the
     * deepest one. Recording intermediate results is what makes the move toasts reliable: the
     * opponent (e.g. a bot) often replies before the search after our move has reached full
     * depth, and that search is then cancelled. Previously its result was discarded, so neither
     * our move nor the reply could be rated and no toast appeared.
     */
    public static void recordResult(String fen, StockfishProcess.AnalysisResult r) {
        if (r == null || r.moves.isEmpty()) return;
        String key = getFenKey(fen);
        if (key == null) return;
        Integer stored = fenToDepthMap.get(key);
        if (stored != null && stored > r.depth) return;
        fenToEvalMap.put(key, r.score);
        fenToBestMovesMap.put(key, new ArrayList<>(r.moves));
        fenToDepthMap.put(key, r.depth);
        if (r.lineScores != null && r.lineScores.length > 0) {
            fenToLineScoresMap.put(key, r.lineScores.clone());
        }
        if (r.pv != null && !r.pv.isEmpty()) {
            fenToPvMap.put(key, new ArrayList<>(r.pv));
        }
        fenToMateMap.put(key, r.hasMate && r.mateIn != 0);
    }

    /** True if an interrupted/intermediate result is deep enough to rate a move with. */
    public static boolean isUsableForRating(StockfishProcess.AnalysisResult r) {
        if (r == null) return false;
        if (r.terminal) return true;
        return !r.moves.isEmpty() && r.depth >= MIN_RATING_DEPTH;
    }

    public static List<String> getFenHistory() {
        return fenHistory;
    }

    public static List<String> getPlayedMoves() {
        List<String> list = new ArrayList<>();
        synchronized (fenHistory) {
            for (int i = 0; i < fenHistory.size() - 1; i++) {
                String uci = deduceUciMove(fenHistory.get(i), fenHistory.get(i + 1));
                if (uci != null) list.add(uci);
            }
        }
        return list;
    }

    public static Map<String, Float> getFenToEvalMap() {
        return fenToEvalMap;
    }

    public static Map<String, List<String>> getFenToBestMovesMap() {
        return fenToBestMovesMap;
    }

    public static void updateHistory(String fen, Object positionObject) {
        lastPositionObject = positionObject;
        if (positionObject != null) {
            List<String> moves = extractMovesFromPosition(positionObject);
            if (moves != null && !moves.isEmpty()) {
                currentPositionMoves = moves;
                TorchEngine.log("[POSITION HOOK] Đã trích xuất " + moves.size() + " nước từ positionObject: " + moves);
            }
        }
        updateHistory(fen);
    }

    public static void updateHistory(String fen) {
        String key = getFenKey(fen);
        if (key == null) return;
        if (fen != null) keyToFullFenMap.put(key, fen);
        synchronized (fenHistory) {
            int idx = fenHistory.indexOf(key);
            if (idx >= 0) {
                boolean truncated = false;
                while (fenHistory.size() > idx + 1) {
                    fenHistory.remove(fenHistory.size() - 1);
                    truncated = true;
                }
                // Stepped back (take-back / navigation): allow the next move to be rated again.
                if (truncated) classifiedMoves.clear();
            } else {
                if (fenHistory.isEmpty()) {
                    String startPosKey = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w";
                    if (!startPosKey.equals(key)) {
                        String fromStart = deduceUciMove(startPosKey, key);
                        if (fromStart != null) {
                            fenHistory.add(startPosKey);
                        }
                    }
                } else {
                    String lastKey = fenHistory.get(fenHistory.size() - 1);
                    String deduced = deduceUciMove(lastKey, key);
                    if (deduced == null) {
                        fenHistory.clear();
                        clearMaps();
                        StockfishExtension.isReviewMode = false;
                        String startPosKey = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w";
                        if (!startPosKey.equals(key)) {
                            String fromStart = deduceUciMove(startPosKey, key);
                            if (fromStart != null) {
                                fenHistory.add(startPosKey);
                            }
                        }
                    }
                }
                fenHistory.add(key);
            }
        }
    }

    public static String getFenKey(String fen) {
        if (fen == null) return null;
        String[] parts = fen.split("\\s+");
        if (parts.length >= 2) {
            return parts[0] + " " + parts[1];
        }
        return fen;
    }

    private static String expandFenBoard(String fenBoard) {
        StringBuilder sb = new StringBuilder();
        for (char c : fenBoard.toCharArray()) {
            if (c == '/') continue;
            if (Character.isDigit(c)) {
                int emptySquares = c - '0';
                for (int i = 0; i < emptySquares; i++) {
                    sb.append('.');
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String getSquareName(int index) {
        char file = (char) ('a' + (index % 8));
        int rank = 8 - (index / 8);
        return "" + file + rank;
    }

    /** UCI promotion letter ("q", "n", ...) when a pawn turned into another piece, else "". */
    private static String promotionSuffix(char before, char after) {
        if (Character.toLowerCase(before) != 'p') return "";
        char a = Character.toLowerCase(after);
        return (a == 'q' || a == 'r' || a == 'b' || a == 'n') ? String.valueOf(a) : "";
    }

    public static String deduceUciMove(String prevFen, String currFen) {
        try {
            String[] prevParts = prevFen.split("\\s+");
            String[] currParts = currFen.split("\\s+");
            if (prevParts.length < 2 || currParts.length < 2) return null;

            String prevBoard = expandFenBoard(prevParts[0]);
            String currBoard = expandFenBoard(currParts[0]);
            if (prevBoard.length() != 64 || currBoard.length() != 64) return null;

            boolean whiteMoved = prevParts[1].equals("w");

            List<Integer> fromCandidates = new ArrayList<>();
            List<Integer> toCandidates = new ArrayList<>();

            for (int i = 0; i < 64; i++) {
                char p = prevBoard.charAt(i);
                char c = currBoard.charAt(i);
                if (p != c) {
                    if (p != '.') {
                        boolean isWhitePiece = Character.isUpperCase(p);
                        if (isWhitePiece == whiteMoved) {
                            fromCandidates.add(i);
                        }
                    }
                    if (c != '.') {
                        boolean isWhitePiece = Character.isUpperCase(c);
                        if (isWhitePiece == whiteMoved) {
                            toCandidates.add(i);
                        }
                    }
                }
            }

            if (fromCandidates.size() == 1 && toCandidates.size() == 1) {
                int f = fromCandidates.get(0), t = toCandidates.get(0);
                return getSquareName(f) + getSquareName(t) + promotionSuffix(prevBoard.charAt(f), currBoard.charAt(t));
            }

            if (fromCandidates.size() >= 1 && toCandidates.size() >= 1) {
                char kingChar = whiteMoved ? 'K' : 'k';
                int kingFrom = -1;
                int kingTo = -1;
                for (int f : fromCandidates) {
                    if (prevBoard.charAt(f) == kingChar) {
                        kingFrom = f;
                        break;
                    }
                }
                for (int t : toCandidates) {
                    if (currBoard.charAt(t) == kingChar) {
                        kingTo = t;
                        break;
                    }
                }
                if (kingFrom != -1 && kingTo != -1) {
                    return getSquareName(kingFrom) + getSquareName(kingTo);
                }

                if (toCandidates.size() == 1) {
                    int toIdx = toCandidates.get(0);
                    char movedPiece = currBoard.charAt(toIdx);
                    for (int f : fromCandidates) {
                        char prevPiece = prevBoard.charAt(f);
                        if (Character.toLowerCase(prevPiece) == Character.toLowerCase(movedPiece) ||
                            (Character.toLowerCase(prevPiece) == 'p' && (movedPiece == 'Q' || movedPiece == 'q' || movedPiece == 'R' || movedPiece == 'r' || movedPiece == 'B' || movedPiece == 'b' || movedPiece == 'N' || movedPiece == 'n'))) {
                            return getSquareName(f) + getSquareName(toIdx) + promotionSuffix(prevPiece, movedPiece);
                        }
                    }
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }

    @android.annotation.SuppressLint("MissingPermission")
    public static void classifyMoveIfPossible(Context context, String currentFen, StockfishProcess.AnalysisResult currentResult) {
        if (context == null) return;
        
        Activity activity = StockfishExtension.getCurrentActivity();
        if (activity != null && StockfishExtension.isLiveMatch(activity) && !StockfishExtension.isReviewMode) {
            return;
        }

        if (!StockfishSettings.isMoveClassificationEnabled(context)) return;

        try {
            List<String> hookMoves = currentPositionMoves;
            String currentKey = getFenKey(currentFen);
            if (currentKey == null && (hookMoves == null || hookMoves.isEmpty())) return;
            if (currentFen != null && currentKey != null) {
                keyToFullFenMap.put(currentKey, currentFen);
            }

            String uciMove = null;
            boolean whiteMoved = false;
            String moveIdentifier = null;
            List<String> moves = null;

            if (hookMoves != null && !hookMoves.isEmpty()) {
                // Ưu tiên 100%: Dùng danh sách nước đi đầy đủ được hook trực tiếp từ positionObject
                moves = new ArrayList<>(hookMoves);
                uciMove = moves.get(moves.size() - 1);
                whiteMoved = (moves.size() % 2 == 1);
                moveIdentifier = moves.size() + ":" + uciMove;
                if (classifiedMoves.contains(moveIdentifier)) return;
            } else {
                // Fallback: dựa vào FEN transition nếu không hook được positionObject
                if (currentKey == null) return;
                String prevKey = null;
                synchronized (fenHistory) {
                    int idx = fenHistory.indexOf(currentKey);
                    if (idx >= 1) {
                        prevKey = fenHistory.get(idx - 1);
                    }
                }
                if (prevKey == null) return;
                final String transition = prevKey + "|" + currentKey;
                if (classifiedMoves.contains(transition)) return;

                whiteMoved = prevKey.endsWith(" w");
                uciMove = deduceUciMove(prevKey, currentKey);
                if (uciMove == null) {
                    TorchEngine.log("[CLASSIFIER DEDUCE NULL] prev=" + prevKey + ", curr=" + currentKey);
                    return;
                }
                moveIdentifier = transition;
                moves = getPlayedMoves();
                if (moves.isEmpty()) {
                    moves.add(uciMove);
                } else if (!moves.get(moves.size() - 1).equals(uciMove)) {
                    moves.add(uciMove);
                }
            }

            final Activity currentAct = activity;
            TorchEngine.log("[CLASSIFIER TRIGGER] Move=" + uciMove + ", whiteMoved=" + whiteMoved + ", totalMoves=" + (moves != null ? moves.size() : 0) + ", torchReady=" + TorchEngine.getInstance(context).isReady());

            // ── 1. 100% Real Torch WebAssembly Engine Execution ──
            TorchEngine torch = TorchEngine.getInstance(context);
            if (torch.isReady()) {
                Boolean userIsWhite = StockfishExtension.isUserWhite(StockfishExtension.getStateImpl());
                final boolean isMyMove;
                if (userIsWhite != null) {
                    isMyMove = (whiteMoved == userIsWhite.booleanValue());
                } else {
                    isMyMove = (whiteMoved == !OverlayManager.isBoardFlipped(StockfishExtension.getStateImpl()));
                }

                String userColor = (userIsWhite != null && !userIsWhite) ? "black" : "white";
                final String finalUci = uciMove;
                classifiedMoves.add(moveIdentifier);

                // Kiểm tra tính hợp lệ của chuỗi nước đi tính từ bàn cờ ban đầu (startpos)
                String firstMove = moves.get(0);
                if (!STARTPOS_LEGAL_FIRST_MOVES.contains(firstMove)) {
                    TorchEngine.log("[TORCH CLASSIFIER BLOCKED] Nước đầu tiên '" + firstMove + "' không thể đi từ startpos (Vào lại giữa ván và không có lịch sử). Chặn gửi lệnh để bảo vệ WebAssembly khỏi Abort.");
                    if (currentAct != null) {
                        boolean isVi = "vi".equalsIgnoreCase(StockfishSettings.getLanguage(currentAct));
                        final String blockedText = isVi ? "⚠️ [Torch Coach] Cần lịch sử nước đi từ đầu ván" : "⚠️ [Torch Coach] Missing moves from startpos";
                        currentAct.runOnUiThread(() -> Toast.makeText(currentAct, blockedText, Toast.LENGTH_SHORT).show());
                    }
                    return;
                }

                torch.analyze(moves, userColor, (classificationName, playedMoveLan, bestMoveLan, speechText, rawJson) -> {
                    TorchEngine.log("[CLASSIFIER CALLBACK] class=" + classificationName + ", act=" + (currentAct != null) + ", isMyMove=" + isMyMove);
                    if (classificationName != null && !classificationName.isEmpty() && !"null".equalsIgnoreCase(classificationName)) {
                        if (currentAct != null) {
                            currentAct.runOnUiThread(() -> {
                                displayTorchClassification(currentAct, classificationName,
                                        (playedMoveLan != null && !playedMoveLan.isEmpty()) ? playedMoveLan : finalUci,
                                        speechText,
                                        isMyMove);
                            });
                        }
                    } else {
                        // 100% phụ thuộc vào Torch: Không chuyển sang Stockfish, báo lỗi rõ ràng nếu thiếu lịch sử startpos
                        TorchEngine.log("[TORCH CLASSIFIER ERROR] Không thể phân loại: Torch CEE trả về null (Thiếu chuỗi startpos hoặc lỗi WebAssembly)");
                        if (currentAct != null) {
                            boolean isVi = "vi".equalsIgnoreCase(StockfishSettings.getLanguage(currentAct));
                            final String errText = isVi ? "⚠️ [Torch Coach] Không thể phân loại (Thiếu lịch sử nước đi)" : "⚠️ [Torch Coach] Classification failed (Missing move history)";
                            currentAct.runOnUiThread(() -> {
                                Toast.makeText(currentAct, errText, Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                });
                return;
            }

            // Torch Engine chưa sẵn sàng: Báo lỗi và ghi log, tuyệt đối không gọi SF
            TorchEngine.log("[TORCH CLASSIFIER ERROR] Torch Engine WASM chưa sẵn sàng!");
            if (activity != null) {
                boolean isVi = "vi".equalsIgnoreCase(StockfishSettings.getLanguage(activity));
                final String notReadyText = isVi ? "⚠️ [Torch Coach] Engine đang khởi động..." : "⚠️ [Torch Coach] Engine initializing...";
                activity.runOnUiThread(() -> {
                    Toast.makeText(activity, notReadyText, Toast.LENGTH_SHORT).show();
                });
            }
        } catch (Throwable t) {
            Log.e(TAG, "Error in classifyMoveIfPossible: " + t.getMessage());
        }
    }

    private static void displayTorchClassification(Activity activity, String torchName, String uciMove, String speechText, boolean isMyMove) {
        if (activity == null || torchName == null) return;
        boolean isVi = "vi".equalsIgnoreCase(StockfishSettings.getLanguage(activity));
        String classification;
        String emoji;
        boolean isBlunderOrMistake = false;

        String lower = torchName.toLowerCase(java.util.Locale.US).replace(" ", "_");
        switch (lower) {
            case "brilliant":
                classification = isVi ? "Nước cờ thiên tài (Brilliant)" : "Brilliant Move";
                emoji = "!!";
                break;
            case "great":
            case "greatfind":
            case "great_find":
                classification = isVi ? "Nước cờ xuất sắc (Great)" : "Great Move";
                emoji = "!";
                break;
            case "best":
                classification = isVi ? "Nước cờ tốt nhất (Best)" : "Best Move";
                emoji = "★";
                break;
            case "excellent":
                classification = isVi ? "Nước cờ tuyệt vời (Excellent)" : "Excellent";
                emoji = "👍";
                break;
            case "good":
                classification = isVi ? "Nước cờ hay (Good)" : "Good Move";
                emoji = "✓";
                break;
            case "book":
                classification = isVi ? "Nước cờ khai cuộc (Book)" : "Book Move";
                emoji = "📖";
                break;
            case "inaccuracy":
                classification = isVi ? "Thiếu chính xác (Inaccuracy)" : "Inaccuracy";
                emoji = "?!";
                break;
            case "mistake":
                classification = isVi ? "Sai lầm (Mistake)" : "Mistake";
                emoji = "?";
                isBlunderOrMistake = true;
                break;
            case "blunder":
                classification = isVi ? "Sai lầm nghiêm trọng (Blunder)" : "Blunder";
                emoji = "??";
                isBlunderOrMistake = true;
                break;
            case "miss":
                classification = isVi ? "Bỏ lỡ cơ hội (Miss)" : "Miss";
                emoji = "✕";
                isBlunderOrMistake = true;
                break;
            case "missed":
            case "missed_win":
            case "missedwin":
                classification = isVi ? "Bỏ lỡ cơ hội thắng (Missed Win)" : "Missed Win";
                emoji = "✕";
                isBlunderOrMistake = true;
                break;
            case "forced":
                classification = isVi ? "Nước bắt buộc (Forced)" : "Forced Move";
                emoji = "➔";
                break;
            default:
                classification = isVi ? "Nước cờ hay (Good)" : "Good Move";
                emoji = "✓";
                break;
        }

        String comment = (speechText != null && !speechText.trim().isEmpty()) ? "\n\"" + speechText.trim() + "\"" : "";
        String playerPrefix = isMyMove ? (isVi ? "[Bạn]" : "[You]") : (isVi ? "[Đối thủ]" : "[Opponent]");
        final String toastText = "[Torch] " + playerPrefix + " [" + emoji + "] " + classification + " (" + uciMove + ")" + comment;
        TorchEngine.log("[CLASSIFIED TOAST] " + playerPrefix + " " + emoji + " " + classification + " (" + uciMove + ")");
        Toast.makeText(activity, toastText, Toast.LENGTH_SHORT).show();

        if (isBlunderOrMistake && StockfishSettings.isBlunderAlertsEnabled(activity)) {
            Vibrator vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= 26) {
                    vibrator.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(150);
                }
            }
        }
    }
}
