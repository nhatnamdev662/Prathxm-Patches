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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MoveClassifier {
    private static final String TAG = "MoveClassifier";

    private static final List<String> fenHistory = new ArrayList<>();
    private static final Map<String, Float> fenToEvalMap = new ConcurrentHashMap<>();
    private static final Map<String, List<String>> fenToBestMovesMap = new ConcurrentHashMap<>();
    /** Line evaluations (MultiPV) for calculating gap between top lines. */
    private static final Map<String, float[]> fenToLineScoresMap = new ConcurrentHashMap<>();
    /** Principal variation lines from this position. */
    private static final Map<String, List<String>> fenToPvMap = new ConcurrentHashMap<>();
    /** Whether this position has a known mate in N. */
    private static final Map<String, Boolean> fenToMateMap = new ConcurrentHashMap<>();
    /** Search depth behind each stored evaluation, so a shallower result never overwrites a deeper one. */
    private static final Map<String, Integer> fenToDepthMap = new ConcurrentHashMap<>();
    /** Moves ("prevKey|currentKey") that already produced a toast, so a move is rated only once. */
    private static final java.util.Set<String> classifiedMoves = ConcurrentHashMap.newKeySet();

    /** Move and loss tracking across plies for recapture and missed win / miss detection. */
    private static volatile String lastPlayedUci = null;
    private static volatile boolean lastWasCapture = false;
    private static volatile float lastOppLoss = -1f;

    /** Minimum depth for an interrupted / intermediate search to be trusted for a rating. */
    private static final int MIN_RATING_DEPTH = 8;

    public static void clearHistory() {
        synchronized (fenHistory) {
            fenHistory.clear();
            clearMaps();
        }
    }

    private static void clearMaps() {
        fenToEvalMap.clear();
        fenToBestMovesMap.clear();
        fenToDepthMap.clear();
        fenToLineScoresMap.clear();
        fenToPvMap.clear();
        fenToMateMap.clear();
        classifiedMoves.clear();
        lastPlayedUci = null;
        lastWasCapture = false;
        lastOppLoss = -1f;
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

    public static void updateHistory(String fen) {
        String key = getFenKey(fen);
        if (key == null) return;
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
                if (!fenHistory.isEmpty()) {
                    String lastKey = fenHistory.get(fenHistory.size() - 1);
                    String deduced = deduceUciMove(lastKey, key);
                    if (deduced == null) {
                        fenHistory.clear();
                        clearMaps();
                        StockfishExtension.isReviewMode = false;
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
            String currentKey = getFenKey(currentFen);
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

            boolean whiteMoved = prevKey.endsWith(" w");
            String uciMove = deduceUciMove(prevKey, currentKey);
            if (uciMove == null) {
                TorchEngine.log("[CLASSIFIER DEDUCE NULL] prev=" + prevKey + ", curr=" + currentKey);
                return;
            }

            final Activity currentAct = activity;
            TorchEngine.log("[CLASSIFIER TRIGGER] Move=" + uciMove + ", whiteMoved=" + whiteMoved + ", torchReady=" + TorchEngine.getInstance(context).isReady());

            // ── 1. 100% Real Torch WebAssembly Engine Execution ──
            TorchEngine torch = TorchEngine.getInstance(context);
            if (torch.isReady()) {
                List<String> moves = getPlayedMoves();
                if (moves.isEmpty()) moves.add(uciMove);
                String userColor = whiteMoved ? "white" : "black";
                final String finalUci = uciMove;
                final String transitionKey = transition;
                classifiedMoves.add(transitionKey);
                torch.analyze(moves, userColor, (classificationName, playedMoveLan, bestMoveLan, speechText, rawJson) -> {
                    TorchEngine.log("[CLASSIFIER CALLBACK] class=" + classificationName + ", act=" + (currentAct != null));
                    if (currentAct != null) {
                        currentAct.runOnUiThread(() -> {
                            displayTorchClassification(currentAct, classificationName,
                                    (playedMoveLan != null && !playedMoveLan.isEmpty()) ? playedMoveLan : finalUci,
                                    speechText);
                        });
                    }
                });
                return;
            }

            // ── 2. Immediate Java Fallback if Torch is not ready ──
            Float prevEvalVal = fenToEvalMap.get(prevKey);
            List<String> prevBestMoves = fenToBestMovesMap.get(prevKey);
            if (prevEvalVal == null || prevBestMoves == null || prevBestMoves.isEmpty()) return;

            float prevEval = prevEvalVal;
            float currentEval = currentResult.score;
            
            // Same expected-points model as the game review (win probability, mover POV).
            float winBefore = ReviewMath.win(prevEval, whiteMoved);
            float winAfter = ReviewMath.win(currentEval, whiteMoved);
            boolean isBest = !prevBestMoves.isEmpty() && uciMove.equals(prevBestMoves.get(0));
            boolean deliversMate = currentResult.terminal && currentResult.hasMate;
            float loss = (isBest || deliversMate) ? 0f : Math.max(0f, winBefore - winAfter);

            // 1. secondGap: win probability difference between line 1 and line 2 (for Great Move)
            float secondGap = -1f;
            float[] prevLineScores = fenToLineScoresMap.get(prevKey);
            if (prevLineScores != null && prevLineScores.length >= 2) {
                float winL1 = ReviewMath.win(prevLineScores[0], whiteMoved);
                float winL2 = ReviewMath.win(prevLineScores[1], whiteMoved);
                secondGap = Math.max(0f, winL1 - winL2);
            }

            // 2. Parse board before the move
            char[] beforeBoard = BoardUtil.parseBoard(prevKey);

            // 3. Recapture detection
            boolean recapture = false;
            if (lastPlayedUci != null && lastWasCapture) {
                recapture = BoardUtil.isRecapture(lastPlayedUci, true, uciMove);
            }

            // 4. Sacrifice detection (engine reply PV after played move)
            boolean sacrifice = false;
            List<String> replyPv = (currentResult.pv != null && !currentResult.pv.isEmpty())
                    ? currentResult.pv
                    : currentResult.moves;
            if (beforeBoard != null && replyPv != null && !replyPv.isEmpty()) {
                sacrifice = BoardUtil.isSacrifice(beforeBoard, whiteMoved, uciMove, replyPv, 4);
            }

            // 5. Missed mate detection
            Boolean prevHadMate = fenToMateMap.get(prevKey);
            boolean prevMoverHadWinningMate = (prevHadMate != null && prevHadMate)
                    && (whiteMoved ? prevEval > 50f : prevEval < -50f);
            boolean currentMoverHasWinningMate = currentResult.hasMate
                    && (whiteMoved ? currentResult.score > 50f : currentResult.score < -50f);
            boolean missedMate = prevMoverHadWinningMate && !currentMoverHasWinningMate;

            // 6. Forced move detection
            Integer prevDepth = fenToDepthMap.get(prevKey);
            boolean forced = false;
            if (prevBestMoves.size() == 1 && (prevLineScores == null || prevLineScores.length <= 1)
                    && prevDepth != null && prevDepth >= 10) {
                forced = true;
            }

            // 7. Opponent previous loss
            float oppPrevLoss = lastOppLoss;

            // Compute classification
            String c = ReviewMath.classify(isBest || deliversMate, forced, loss, winBefore, winAfter,
                    secondGap, sacrifice, recapture, oppPrevLoss, missedMate);

            // 8. Book move detection: first 16 plies (8 moves each) with theoretical play
            int ply = fenHistory.size();
            if (ply <= 16 && (isBest || loss < 0.02f) && ReviewMath.isBookEligible(c) && !sacrifice) {
                c = ReviewMath.BOOK;
            }

            // Update state tracking for the next move
            lastOppLoss = loss;
            lastPlayedUci = uciMove;
            if (beforeBoard != null && uciMove.length() >= 4) {
                int toSq = BoardUtil.square(uciMove, 2);
                lastWasCapture = (toSq >= 0 && toSq < 64 && beforeBoard[toSq] != '.');
            } else {
                lastWasCapture = false;
            }

            String classification;
            String emoji;
            boolean isBlunderOrMistake = false;
            boolean isVi = "vi".equalsIgnoreCase(StockfishSettings.getLanguage(activity != null ? activity : context));

            switch (c) {
                case ReviewMath.BRILLIANT:
                    classification = isVi ? "Nước cờ thiên tài (Brilliant)" : "Brilliant Move";
                    emoji = "!!";
                    break;
                case ReviewMath.GREAT:
                    classification = isVi ? "Nước cờ xuất sắc (Great)" : "Great Move";
                    emoji = "!";
                    break;
                case ReviewMath.BEST:
                    classification = isVi ? "Nước cờ tốt nhất (Best)" : "Best Move";
                    emoji = "★";
                    break;
                case ReviewMath.EXCELLENT:
                    classification = isVi ? "Nước cờ tuyệt vời (Excellent)" : "Excellent";
                    emoji = "👍";
                    break;
                case ReviewMath.GOOD:
                    classification = isVi ? "Nước cờ hay (Good)" : "Good Move";
                    emoji = "✓";
                    break;
                case ReviewMath.BOOK:
                    classification = isVi ? "Nước cờ khai cuộc (Book)" : "Book Move";
                    emoji = "📖";
                    break;
                case ReviewMath.INACCURACY:
                    classification = isVi ? "Thiếu chính xác (Inaccuracy)" : "Inaccuracy";
                    emoji = "?!";
                    break;
                case ReviewMath.MISTAKE:
                    classification = isVi ? "Sai lầm (Mistake)" : "Mistake";
                    emoji = "?";
                    isBlunderOrMistake = true;
                    break;
                case ReviewMath.BLUNDER:
                    classification = isVi ? "Sai lầm nghiêm trọng (Blunder)" : "Blunder";
                    emoji = "??";
                    isBlunderOrMistake = true;
                    break;
                case ReviewMath.MISS:
                    classification = isVi ? "Bỏ lỡ cơ hội (Miss)" : "Miss";
                    emoji = "✕";
                    isBlunderOrMistake = true;
                    break;
                case ReviewMath.MISSED_WIN:
                    classification = isVi ? "Bỏ lỡ cơ hội thắng (Missed Win)" : "Missed Win";
                    emoji = "✕";
                    isBlunderOrMistake = true;
                    break;
                case ReviewMath.FORCED:
                    classification = isVi ? "Nước bắt buộc (Forced)" : "Forced Move";
                    emoji = "➔";
                    break;
                default:
                    classification = isVi ? "Nước cờ hay (Good)" : "Good Move";
                    emoji = "✓";
                    break;
            }

            String lossText = (loss > 0.005f) ? String.format(java.util.Locale.US, " [-%.0f%%]", loss * 100f) : "";
            final String toastText = "[Torch] [" + emoji + "] " + classification + " (" + uciMove + ")" + lossText;
            final boolean triggerVibrate = isBlunderOrMistake;

            if (activity != null) {
                classifiedMoves.add(transition);
                activity.runOnUiThread(() -> {
                    Toast.makeText(activity, toastText, Toast.LENGTH_SHORT).show();
                    
                    if (triggerVibrate && StockfishSettings.isBlunderAlertsEnabled(activity)) {
                        Vibrator vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
                        if (vibrator != null && vibrator.hasVibrator()) {
                            if (Build.VERSION.SDK_INT >= 26) {
                                vibrator.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE));
                            } else {
                                vibrator.vibrate(150);
                            }
                        }
                    }
                });
            }
        } catch (Throwable t) {
            Log.e(TAG, "Error in classifyMoveIfPossible: " + t.getMessage());
        }
    }

    private static void displayTorchClassification(Activity activity, String torchName, String uciMove, String speechText) {
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
        final String toastText = "[Torch] [" + emoji + "] " + classification + " (" + uciMove + ")" + comment;
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
