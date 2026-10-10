package app.prathxm.chess.extension.stockfish;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * StockfishProcess – manages the chess engine via WebAssembly (Stockfish 18 / Komodo 3.3)
 * through WasmEngineManager, communicating via the UCI protocol over Web Worker messages.
 *
 * Fully replaces native subprocesses (libstockfish.so) with 100% authentic WebAssembly
 * identical to the NNVC Extension.
 */
@SuppressWarnings("unused")
public class StockfishProcess {

    private static final String TAG = "StockfishProcess";

    /** Ready timeout for WebAssembly engine bootstrap. */
    private static final int READY_TIMEOUT_MS = 30_000;
    /** Extra grace time on top of any movetime cap before we force a "stop". */
    private static final int BESTMOVE_GRACE_MS = 10_000;
    /** Hard ceiling for a depth-only search before we force a "stop". */
    private static final int DEFAULT_SEARCH_TIMEOUT_MS = 60_000;

    /** Scores are clamped to +/-MATE_SCORE (pawns) to keep mate evaluations ordered. */
    public static final float MATE_SCORE = 99.0f;

    private WasmEngineManager wasmManager;

    // Cached option state so we only send setoption when something changes.
    private int curThreads = -1;
    private int curHash = -1;
    private int curMultiPV = -1;
    private Boolean curLimitStrength = null;
    private int curElo = -1;
    private int curSkillLevel = -1;
    private int curSkillErr = -1;
    private int curSkillProb = -1;
    private String curEngineChoice = null;

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    public boolean start(Context context) {
        try {
            wasmManager = WasmEngineManager.getInstance(context);
            resetOptionCache();

            String engineChoice = StockfishSettings.getEngineChoice(context);
            curEngineChoice = engineChoice;
            wasmManager.switchEngine(engineChoice);

            if (!wasmManager.waitForPlayWorkerReady(15_000)) {
                Log.w(TAG, "Play worker not ready within 15s; attempting UCI handshake anyway.");
            }

            send("uci");
            if (!waitForLine("uciok", READY_TIMEOUT_MS)) {
                Log.w(TAG, "Engine did not respond with 'uciok' within timeout; proceeding with async ready check.");
            }

            // Standard UCI options
            send("setoption name Threads value 1");
            send("setoption name Hash value 16");
            send("setoption name UCI_ShowWDL value true");

            send("isready");
            if (!waitForLine("readyok", READY_TIMEOUT_MS)) {
                Log.w(TAG, "Engine did not respond with 'readyok' within timeout.");
            }

            TorchEngine.log("[WASM ENGINE READY] Active play engine: " + engineChoice);
            return true;
        } catch (Throwable e) {
            Log.e(TAG, "Failed to start WASM engine: " + e.getMessage());
            return false;
        }
    }

    public boolean isReady() {
        return wasmManager != null && wasmManager.isReady();
    }

    // ── Results ───────────────────────────────────────────────────────────────

    public static class AnalysisResult {
        /** First move of each principal variation, best first (MultiPV order). */
        public final List<String> moves;
        /** Score of the best line in pawns from WHITE's point of view (mates mapped to +/-(99-n)). */
        public final float score;
        public final boolean hasMate;
        /** Mate distance from WHITE's point of view (positive = white mates). */
        public final int mateIn;
        /** Win/Draw/Loss per mille from WHITE's point of view. */
        public final int wdlWin;
        public final int wdlDraw;
        public final int wdlLoss;
        public final String ponder;
        /** Full principal variation of the best line (starts with moves.get(0)). */
        public final List<String> pv;
        /** Score (white POV, pawns) of every MultiPV line, aligned with {@link #moves}. */
        public final float[] lineScores;
        /** Depth actually reached by the search. */
        public final int depth;
        /** True if the position has no legal moves (checkmate or stalemate). */
        public final boolean terminal;

        public AnalysisResult(List<String> moves, float score, boolean hasMate, int mateIn,
                              int wdlWin, int wdlDraw, int wdlLoss, String ponder) {
            this(moves, score, hasMate, mateIn, wdlWin, wdlDraw, wdlLoss, ponder,
                    moves != null && !moves.isEmpty() ? Collections.singletonList(moves.get(0)) : new ArrayList<>(),
                    new float[]{score}, 0, false);
        }

        public AnalysisResult(List<String> moves, float score, boolean hasMate, int mateIn,
                              int wdlWin, int wdlDraw, int wdlLoss, String ponder,
                              List<String> pv, float[] lineScores, int depth, boolean terminal) {
            this.moves = moves != null ? moves : new ArrayList<>();
            this.score = score;
            this.hasMate = hasMate;
            this.mateIn = mateIn;
            this.wdlWin = wdlWin;
            this.wdlDraw = wdlDraw;
            this.wdlLoss = wdlLoss;
            this.ponder = ponder;
            this.pv = pv != null ? pv : new ArrayList<>();
            this.lineScores = lineScores != null ? lineScores : new float[0];
            this.depth = depth;
            this.terminal = terminal;
        }

        public static AnalysisResult empty() {
            return new AnalysisResult(new ArrayList<>(), 0f, false, 0, 0, 0, 0, null,
                    new ArrayList<>(), new float[0], 0, false);
        }

        /** A usable result has either a best move or is a genuine terminal position. */
        public boolean isValid() {
            return !moves.isEmpty() || terminal;
        }
    }

    public interface ProgressListener {
        void onProgress(AnalysisResult partial);
    }

    private static final int PROGRESS_MIN_DEPTH = 10;
    private static final long PROGRESS_INTERVAL_MS = 300;

    // ── Analysis API ──────────────────────────────────────────────────────────

    public List<String> bestMoves(Context context, String fen, int depth, int multiPV) {
        return analyze(context, fen, depth, multiPV).moves;
    }

    public AnalysisResult analyze(Context context, String fen, int depth, int multiPV) {
        return analyze(context, fen, null, depth, multiPV, 0, true);
    }

    public AnalysisResult analyze(Context context, String fen, List<String> uciMoves, int depth,
                                  int multiPV, int movetimeMs, boolean honorLimit) {
        return analyze(context, fen, uciMoves, depth, multiPV, movetimeMs, honorLimit, null);
    }

    public AnalysisResult analyze(Context context, String fen, List<String> uciMoves, int depth,
                                  int multiPV, int movetimeMs, boolean honorLimit,
                                  ProgressListener progress) {
        if (fen == null) return AnalysisResult.empty();
        if (wasmManager == null) wasmManager = WasmEngineManager.getInstance(context);

        final boolean whiteToMove = isWhiteToMove(fen, uciMoves);
        final int elo = StockfishSettings.getElo(context);

        // Auto depth according to Elo (exact Extension NNVC logic)
        boolean isAutoDepth = StockfishSettings.isAutoDepthEnabled(context);
        int effectiveDepth;
        if (isAutoDepth && honorLimit) {
            effectiveDepth = StockfishSettings.autoDepthForElo(elo);
        } else {
            effectiveDepth = Math.max(1, depth > 0 ? depth : StockfishSettings.getDepth(context));
        }

        try {
            drainReady();

            applyPlayEngineOptions(context, elo, multiPV, honorLimit);

            StringBuilder pos = new StringBuilder(fen.length() + 8 + (uciMoves != null ? uciMoves.size() * 6 : 0));
            pos.append("position fen ").append(fen);
            if (uciMoves != null && !uciMoves.isEmpty()) {
                pos.append(" moves");
                for (String m : uciMoves) pos.append(' ').append(m);
            }
            send(pos.toString());

            String goCmd;
            if (isAutoDepth && honorLimit) {
                int softMs = movetimeMs > 0 ? movetimeMs : Math.max(700, Math.min(2400, effectiveDepth * 200));
                goCmd = "go depth " + effectiveDepth + " movetime " + softMs;
            } else {
                goCmd = movetimeMs > 0 ? ("go depth " + effectiveDepth + " movetime " + movetimeMs) : ("go depth " + effectiveDepth);
            }
            send(goCmd);
            TorchEngine.log("[WASM ENGINE GO] " + goCmd + " (Elo=" + elo + ")");

            return readSearchOutput(Math.max(3, multiPV), whiteToMove,
                    movetimeMs > 0 ? movetimeMs + BESTMOVE_GRACE_MS : DEFAULT_SEARCH_TIMEOUT_MS,
                    effectiveDepth, progress);
        } catch (Throwable e) {
            Log.e(TAG, "analyze error: " + e.getMessage());
            return AnalysisResult.empty();
        }
    }

    public void newGame() {
        try {
            send("ucinewgame");
            send("isready");
            waitForLine("readyok", READY_TIMEOUT_MS);
        } catch (Throwable ignored) {}
    }

    public void stopSearch() {
        send("stop");
    }

    public void stop() {
        try { send("quit"); } catch (Exception ignored) {}
    }

    // ── Output parsing ────────────────────────────────────────────────────────

    private AnalysisResult readSearchOutput(int multiPV, boolean whiteToMove, long timeoutMs,
                                            int targetDepth, ProgressListener progress) throws IOException {
        String[] firstMoves = new String[multiPV];
        float[] scores = new float[multiPV];
        boolean[] haveExact = new boolean[multiPV];
        boolean[] haveAny = new boolean[multiPV];
        List<String> bestPv = null;

        boolean hasMate = false;
        int mateIn = 0;
        int wdlW = 0, wdlD = 0, wdlL = 0;
        int reachedDepth = 0;
        boolean terminal = false;
        String bestmove = null;
        String ponder = null;

        long deadline = System.currentTimeMillis() + timeoutMs;
        boolean stopSent = false;
        int reportedDepth = 0;
        long lastReport = 0;

        String line;
        while (true) {
            long remaining = deadline - System.currentTimeMillis();
            if (!stopSent && remaining <= 0) {
                send("stop");
                stopSent = true;
                deadline = System.currentTimeMillis() + READY_TIMEOUT_MS;
                remaining = READY_TIMEOUT_MS;
            } else if (stopSent && remaining <= 0) {
                Log.e(TAG, "Engine unresponsive after stop; exiting search.");
                break;
            }

            line = (wasmManager != null) ? wasmManager.readPlayLine(Math.max(50, remaining)) : null;
            if (line == null) {
                if (stopSent) break;
                continue;
            }

            if (line.startsWith("bestmove")) {
                String[] parts = line.split(" ");
                if (parts.length > 1 && !"(none)".equals(parts[1])) bestmove = parts[1];
                if (parts.length > 3 && "ponder".equals(parts[2])) ponder = parts[3];
                if (bestmove == null) terminal = true;
                TorchEngine.log("[WASM BESTMOVE] best=" + bestmove + ", eval=" + scores[0] + ", depth=" + reachedDepth);
                break;
            }

            if (!line.startsWith("info ")) continue;
            if (line.startsWith("info string")) {
                if (line.contains("CRITICAL")) Log.e(TAG, line);
                continue;
            }
            if (line.indexOf(" score ") < 0) continue;

            String[] t = line.split(" ");
            int mpv = 1;
            int depth = 0;
            boolean bound = false;
            boolean isMate = false;
            int scoreVal = 0;
            boolean haveScore = false;
            int w = -1, d = -1, l = -1;
            int pvStart = -1;

            for (int i = 1; i < t.length; i++) {
                String k = t[i];
                switch (k) {
                    case "depth":
                        if (i + 1 < t.length) depth = parseIntSafe(t[++i], 0);
                        break;
                    case "multipv":
                        if (i + 1 < t.length) mpv = parseIntSafe(t[++i], 1);
                        break;
                    case "score":
                        if (i + 2 < t.length) {
                            isMate = "mate".equals(t[i + 1]);
                            scoreVal = parseIntSafe(t[i + 2], 0);
                            haveScore = true;
                            i += 2;
                        }
                        break;
                    case "lowerbound":
                    case "upperbound":
                        bound = true;
                        break;
                    case "wdl":
                        if (i + 3 < t.length) {
                            w = parseIntSafe(t[i + 1], -1);
                            d = parseIntSafe(t[i + 2], -1);
                            l = parseIntSafe(t[i + 3], -1);
                            i += 3;
                        }
                        break;
                    case "pv":
                        pvStart = i + 1;
                        i = t.length; // pv is always last
                        break;
                    default:
                        break;
                }
            }

            if (!haveScore || mpv < 1 || mpv > multiPV) continue;
            int idx = mpv - 1;

            if (bound && haveExact[idx]) continue;

            if (depth == 0 && pvStart < 0) {
                terminal = true;
            }

            float whiteScore;
            int whiteMate = 0;
            if (isMate) {
                whiteMate = whiteToMove ? scoreVal : -scoreVal;
                if (scoreVal == 0) {
                    whiteScore = whiteToMove ? -MATE_SCORE : MATE_SCORE;
                    whiteMate = 0;
                } else {
                    whiteScore = whiteMate > 0 ? (MATE_SCORE - whiteMate) : (-MATE_SCORE - whiteMate);
                }
            } else {
                float pawns = scoreVal / 100.0f;
                whiteScore = whiteToMove ? pawns : -pawns;
            }

            scores[idx] = whiteScore;
            haveAny[idx] = true;
            if (!bound) haveExact[idx] = true;

            if (pvStart > 0 && pvStart < t.length) {
                firstMoves[idx] = t[pvStart];
            }

            if (idx == 0) {
                if (depth > reachedDepth) reachedDepth = depth;
                hasMate = isMate;
                mateIn = (isMate && scoreVal != 0) ? whiteMate : 0;
                if (w >= 0 && d >= 0 && l >= 0) {
                    if (whiteToMove) { wdlW = w; wdlD = d; wdlL = l; }
                    else { wdlW = l; wdlD = d; wdlL = w; }
                }
                if (pvStart > 0 && pvStart < t.length) {
                    ArrayList<String> pv = new ArrayList<>(t.length - pvStart);
                    for (int j = pvStart; j < t.length; j++) pv.add(t[j]);
                    bestPv = pv;
                }
            }

            if (progress != null && idx == 0 && !bound && !stopSent
                    && depth >= PROGRESS_MIN_DEPTH && depth > reportedDepth && depth < targetDepth
                    && firstMoves[0] != null) {
                long now = System.currentTimeMillis();
                if (now - lastReport >= PROGRESS_INTERVAL_MS) {
                    reportedDepth = depth;
                    lastReport = now;
                    try {
                        progress.onProgress(buildResult(multiPV, firstMoves, scores, haveAny, hasMate,
                                mateIn, wdlW, wdlD, wdlL,
                                bestPv != null && bestPv.size() > 1 ? bestPv.get(1) : null,
                                bestPv, depth, false, null));
                    } catch (Throwable ignored) {}
                }
            }
        }

        return buildResult(multiPV, firstMoves, scores, haveAny, hasMate, mateIn, wdlW, wdlD, wdlL,
                ponder, bestPv, reachedDepth, terminal, bestmove);
    }

    private static AnalysisResult buildResult(int multiPV, String[] firstMovesIn, float[] scoresIn,
                                              boolean[] haveAny, boolean hasMate, int mateIn,
                                              int wdlW, int wdlD, int wdlL, String ponder,
                                              List<String> bestPv, int reachedDepth,
                                              boolean terminal, String bestmove) {
        String[] firstMoves = firstMovesIn.clone();
        float[] scores = scoresIn.clone();

        List<String> moves = new ArrayList<>(multiPV);
        List<Float> lineScoreList = new ArrayList<>(multiPV);
        if (bestmove != null) {
            firstMoves[0] = bestmove;
            if (bestPv == null || bestPv.isEmpty() || !bestmove.equals(bestPv.get(0))) {
                ArrayList<String> pv = new ArrayList<>(2);
                pv.add(bestmove);
                if (ponder != null) pv.add(ponder);
                bestPv = pv;
            }
        }
        for (int i = 0; i < multiPV; i++) {
            if (firstMoves[i] == null) continue;
            if (moves.contains(firstMoves[i])) continue;
            moves.add(firstMoves[i]);
            lineScoreList.add(haveAny[i] ? scores[i] : scores[0]);
        }
        float[] lineScores = new float[lineScoreList.size()];
        for (int i = 0; i < lineScores.length; i++) lineScores[i] = lineScoreList.get(i);

        boolean isTerminal = terminal && moves.isEmpty();
        if (isTerminal && !hasMate) {
            scores[0] = 0f;
        }

        return new AnalysisResult(moves, scores[0], hasMate, mateIn, wdlW, wdlD, wdlL, ponder,
                bestPv != null ? bestPv : new ArrayList<>(), lineScores, reachedDepth, isTerminal);
    }

    // ── Options ───────────────────────────────────────────────────────────────

    private void resetOptionCache() {
        curThreads = -1;
        curHash = -1;
        curMultiPV = -1;
        curLimitStrength = null;
        curElo = -1;
        curSkillLevel = -1;
        curSkillErr = -1;
        curSkillProb = -1;
        curEngineChoice = null;
    }

    /**
     * Exact UCI options logic from Extension NNVC (_applyPlayEngineOptions).
     */
    private void applyPlayEngineOptions(Context context, int elo, int multiPV, boolean honorLimit) {
        String engineChoice = StockfishSettings.getEngineChoice(context);
        boolean isKomodo = StockfishSettings.ENGINE_KOMODO.equals(engineChoice);

        if (!engineChoice.equals(curEngineChoice)) {
            if (wasmManager != null) wasmManager.switchEngine(engineChoice);
            curEngineChoice = engineChoice;
            resetOptionCache();
        }

        int neededMpv = Math.max(3, multiPV);
        int finalMpv = Math.max(1, Math.min(8, neededMpv));
        if (finalMpv != curMultiPV) {
            send("setoption name MultiPV value " + finalMpv);
            curMultiPV = finalMpv;
        }

        if (curHash != 16) {
            send("setoption name Hash value 16");
            curHash = 16;
        }

        if (curThreads != 1) {
            send("setoption name Threads value 1");
            curThreads = 1;
        }

        send("setoption name Ponder value false");
        send("setoption name Slow Mover value 100");

        int skillFromElo = Math.max(0, Math.min(20, Math.round((elo - 600f) / 130f)));

        if (isKomodo) {
            if (curSkillLevel != skillFromElo) {
                send("setoption name Skill Level value " + skillFromElo);
                curSkillLevel = skillFromElo;
            }
            if (elo < 2000) {
                int skillErr = Math.round((2000f - elo) / 70f) + 2;
                int skillProb = Math.round((2000f - elo) / 50f) + 1;
                if (curSkillErr != skillErr) {
                    send("setoption name Skill Level Maximum Error value " + skillErr);
                    curSkillErr = skillErr;
                }
                if (curSkillProb != skillProb) {
                    send("setoption name Skill Level Probability value " + skillProb);
                    curSkillProb = skillProb;
                }
            } else {
                if (curSkillErr != 0) {
                    send("setoption name Skill Level Maximum Error value 0");
                    curSkillErr = 0;
                }
                if (curSkillProb != 0) {
                    send("setoption name Skill Level Probability value 0");
                    curSkillProb = 0;
                }
            }
        } else {
            if (elo >= 1320) {
                if (curLimitStrength == null || !curLimitStrength) {
                    send("setoption name UCI_LimitStrength value true");
                    curLimitStrength = true;
                }
                if (curElo != elo) {
                    send("setoption name UCI_Elo value " + elo);
                    curElo = elo;
                }
            } else {
                if (curLimitStrength == null || curLimitStrength) {
                    send("setoption name UCI_LimitStrength value false");
                    curLimitStrength = false;
                }
                if (curSkillLevel != skillFromElo) {
                    send("setoption name Skill Level value " + skillFromElo);
                    curSkillLevel = skillFromElo;
                }
            }
        }
    }

    private static boolean isWhiteToMove(String fen, List<String> moves) {
        boolean white = true;
        int sp = fen.indexOf(' ');
        if (sp >= 0 && sp + 1 < fen.length()) white = fen.charAt(sp + 1) != 'b';
        if (moves != null && (moves.size() & 1) == 1) white = !white;
        return white;
    }

    private static int parseIntSafe(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private void send(String cmd) {
        if (wasmManager != null) {
            wasmManager.sendPlayCommand(cmd);
        }
    }

    private boolean waitForLine(String token, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            String line = (wasmManager != null) ? wasmManager.readPlayLine(500) : null;
            if (line != null && line.startsWith(token)) return true;
        }
        return false;
    }

    private void drainReady() {
        if (wasmManager != null) {
            wasmManager.clearPlayOutputQueue();
        }
    }
}
