package app.prathxm.chess.extension.stockfish;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * StockfishProcess – Hybrid Chess Engine Manager.
 *
 * Supports both:
 *  1) Native Stockfish (libstockfish.so): Instant (<50ms), ultra-fast C++ binary, zero battery drain,
 *     100% offline, bundled in APK. Primary and bulletproof fallback.
 *  2) WebAssembly Engines (Stockfish 18 / Komodo Dragon 3.3): Runs in background headless WebView
 *     Web Workers via WasmEngineManager when WASM assets are present.
 *
 * If WASM files are missing or worker is not ready, automatically and seamlessly falls back
 * to Native Stockfish so the app NEVER freezes or fails.
 */
@SuppressWarnings("unused")
public class StockfishProcess {

    private static final String TAG = "StockfishProcess";

    public static final float MATE_SCORE = 99.0f;
    private static final int READY_TIMEOUT_MS = 10_000;
    private static final int BESTMOVE_GRACE_MS = 10_000;
    private static final int DEFAULT_SEARCH_TIMEOUT_MS = 60_000;
    private static final int PROGRESS_MIN_DEPTH = 8;
    private static final long PROGRESS_INTERVAL_MS = 250;

    // Active Engine Mode: "native" or "wasm"
    private volatile String activeEngineMode = "native";

    // Native Subprocess Fields
    private Process nativeProcess;
    private PrintWriter nativeStdin;
    private BufferedReader nativeStdout;
    private volatile boolean nativeReady = false;

    // WASM Engine Fields
    private WasmEngineManager wasmManager;

    // Option Caches
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

    public synchronized boolean start(Context context) {
        if (context == null) return false;
        NnvcLogger.init(context);

        String engineChoice = StockfishSettings.getEngineChoice(context);
        curEngineChoice = engineChoice;
        NnvcLogger.i(TAG, "Starting engine with requested choice: " + engineChoice);

        // 1. If user chose WASM (Stockfish 18 or Komodo 3.3)
        if (StockfishSettings.ENGINE_KOMODO.equals(engineChoice) ||
            StockfishSettings.ENGINE_STOCKFISH18.equals(engineChoice)) {

            wasmManager = WasmEngineManager.getInstance(context);
            if (wasmManager.hasWasmFiles(engineChoice)) {
                NnvcLogger.i(TAG, "WASM files found for " + engineChoice + ". Initializing WASM engine...");
                EngineStatusHUD.show(context, "📦 Đang nạp " + engineChoice + " WASM...", false, 3000);
                wasmManager.switchEngine(engineChoice);

                if (wasmManager.waitForPlayWorkerReady(5000)) {
                    sendUciCommand("uci");
                    waitForLine("uciok", 5000);
                    sendUciCommand("setoption name Threads value 1");
                    sendUciCommand("setoption name Hash value 16");
                    sendUciCommand("setoption name UCI_ShowWDL value true");
                    sendUciCommand("isready");
                    waitForLine("readyok", 5000);

                    activeEngineMode = "wasm";
                    NnvcLogger.i(TAG, "✓ WASM engine " + engineChoice + " started successfully!");
                    EngineStatusHUD.showReady(context, engineChoice.toUpperCase() + " WASM");
                    return true;
                } else {
                    NnvcLogger.w(TAG, "WASM engine failed to respond in time; falling back to Native Stockfish.");
                    EngineStatusHUD.show(context, "⚡ Chuyển sang Stockfish Native (WASM chưa sẵn sàng)", false, 3000);
                }
            } else {
                NnvcLogger.i(TAG, "WASM files for " + engineChoice + " not installed. Using Native Stockfish.");
                EngineStatusHUD.show(context, "⚡ Chạy Stockfish Native (WASM chưa cài đặt)", false, 3000);
            }
        }

        // 2. Start Native Stockfish Process (Primary & Bulletproof Fallback)
        boolean nativeOk = startNativeProcess(context);
        if (nativeOk) {
            activeEngineMode = "native";
            NnvcLogger.i(TAG, "✓ Native Stockfish engine ready!");
            EngineStatusHUD.showReady(context, "Stockfish Native");
            return true;
        }

        // 3. Last-ditch: If native failed, try WASM if available
        if (wasmManager != null && wasmManager.isReady()) {
            activeEngineMode = "wasm";
            NnvcLogger.i(TAG, "Falling back to WASM engine as backup.");
            return true;
        }

        NnvcLogger.e(TAG, "All engine initializations failed!");
        EngineStatusHUD.show(context, "⚠️ Lỗi khởi động Engine", true, 5000);
        return false;
    }

    private boolean startNativeProcess(Context context) {
        stopNativeProcess();
        try {
            File engineBin = extractBinary(context);
            if (engineBin == null) {
                NnvcLogger.e(TAG, "Could not find native libstockfish.so");
                return false;
            }

            ProcessBuilder pb = new ProcessBuilder(engineBin.getAbsolutePath());
            pb.redirectErrorStream(true);
            nativeProcess = pb.start();

            nativeStdin = new PrintWriter(new OutputStreamWriter(nativeProcess.getOutputStream()), true);
            nativeStdout = new BufferedReader(new InputStreamReader(nativeProcess.getInputStream()), 1 << 16);

            resetOptionCache();

            sendNative("uci");
            if (!waitForNativeLine("uciok", READY_TIMEOUT_MS)) {
                NnvcLogger.e(TAG, "Native engine did not respond with 'uciok'");
                stopNativeProcess();
                return false;
            }

            applyThreads(StockfishSettings.getThreads(context));
            applyHash(computeHashMb(context));
            sendNative("setoption name UCI_ShowWDL value true");

            sendNative("isready");
            if (!waitForNativeLine("readyok", READY_TIMEOUT_MS)) {
                NnvcLogger.e(TAG, "Native engine did not respond with 'readyok'");
                stopNativeProcess();
                return false;
            }

            nativeReady = true;
            NnvcLogger.i(TAG, "Native Stockfish ready on " + android.os.Build.CPU_ABI
                    + " (threads=" + curThreads + ", hash=" + curHash + "MB)");
            return true;

        } catch (Throwable e) {
            NnvcLogger.e(TAG, "Failed to start native Stockfish: " + e.getMessage(), e);
            stopNativeProcess();
            return false;
        }
    }

    public boolean isReady() {
        if ("wasm".equals(activeEngineMode)) {
            return wasmManager != null && wasmManager.isReady();
        }
        if (!nativeReady || nativeProcess == null) return false;
        try {
            nativeProcess.exitValue();
            nativeReady = false;
            return false;
        } catch (IllegalThreadStateException e) {
            return true;
        }
    }

    public String getActiveEngineMode() {
        return activeEngineMode;
    }

    // ── Results ───────────────────────────────────────────────────────────────

    public static class AnalysisResult {
        public final List<String> moves;
        public final float score;
        public final boolean hasMate;
        public final int mateIn;
        public final int wdlWin;
        public final int wdlDraw;
        public final int wdlLoss;
        public final String ponder;
        public final List<String> pv;
        public final float[] lineScores;
        public final int depth;
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

        public boolean isValid() {
            return !moves.isEmpty() || terminal;
        }
    }

    public interface ProgressListener {
        void onProgress(AnalysisResult partial);
    }

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
        if (!isReady() && !start(context)) {
            NnvcLogger.w(TAG, "analyze called but engine is not ready and failed to start.");
            return AnalysisResult.empty();
        }
        if (fen == null) return AnalysisResult.empty();

        final boolean whiteToMove = isWhiteToMove(fen, uciMoves);
        final int elo = StockfishSettings.getElo(context);

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
            sendUciCommand(pos.toString());

            String goCmd;
            if (isAutoDepth && honorLimit) {
                int softMs = movetimeMs > 0 ? movetimeMs : Math.max(700, Math.min(2400, effectiveDepth * 200));
                goCmd = "go depth " + effectiveDepth + " movetime " + softMs;
            } else {
                goCmd = movetimeMs > 0 ? ("go depth " + effectiveDepth + " movetime " + movetimeMs) : ("go depth " + effectiveDepth);
            }
            sendUciCommand(goCmd);
            NnvcLogger.d(TAG, "Search started [" + activeEngineMode + "]: " + goCmd + " (Elo=" + elo + ")");

            return readSearchOutput(Math.max(3, multiPV), whiteToMove,
                    movetimeMs > 0 ? movetimeMs + BESTMOVE_GRACE_MS : DEFAULT_SEARCH_TIMEOUT_MS,
                    effectiveDepth, progress);
        } catch (Throwable e) {
            NnvcLogger.e(TAG, "Search error in mode " + activeEngineMode + ": " + e.getMessage(), e);
            if ("wasm".equals(activeEngineMode)) {
                NnvcLogger.w(TAG, "WASM search failed; automatically switching to Native Stockfish.");
                activeEngineMode = "native";
                startNativeProcess(context);
            } else {
                nativeReady = false;
            }
            return AnalysisResult.empty();
        }
    }

    public void newGame() {
        if (!isReady()) return;
        try {
            sendUciCommand("ucinewgame");
            sendUciCommand("isready");
            waitForLine("readyok", READY_TIMEOUT_MS);
        } catch (Throwable ignored) {}
    }

    public void stopSearch() {
        sendUciCommand("stop");
    }

    public void stop() {
        stopNativeProcess();
        if (wasmManager != null) {
            wasmManager.sendPlayCommand("quit");
        }
    }

    private void stopNativeProcess() {
        nativeReady = false;
        try { if (nativeStdin != null) nativeStdin.println("quit"); } catch (Throwable ignored) {}
        try { if (nativeProcess != null) nativeProcess.destroy(); } catch (Throwable ignored) {}
        nativeStdin = null;
        nativeStdout = null;
        nativeProcess = null;
    }

    // ── Output Parsing ────────────────────────────────────────────────────────

    private AnalysisResult readSearchOutput(int multiPV, boolean whiteToMove, long timeoutMs,
                                            int targetDepth, ProgressListener progress) throws Exception {
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
            if (!stopSent && System.currentTimeMillis() > deadline) {
                sendUciCommand("stop");
                stopSent = true;
                deadline = System.currentTimeMillis() + READY_TIMEOUT_MS;
            } else if (stopSent && System.currentTimeMillis() > deadline) {
                NnvcLogger.e(TAG, "Engine unresponsive after stop; restarting.");
                stop();
                break;
            }

            line = readLineFromActiveEngine();
            if (line == null) {
                NnvcLogger.w(TAG, "Engine output stream closed or timed out");
                break;
            }

            if (line.startsWith("bestmove")) {
                String[] parts = line.split("\\s+");
                if (parts.length > 1 && !"(none)".equals(parts[1])) bestmove = parts[1];
                if (parts.length > 3 && "ponder".equals(parts[2])) ponder = parts[3];
                if (bestmove == null) terminal = true;
                NnvcLogger.d(TAG, "BestMove: " + bestmove + ", eval=" + scores[0] + ", depth=" + reachedDepth);
                break;
            }

            if (!line.startsWith("info ")) continue;
            if (line.indexOf(" score ") < 0) continue;

            String[] t = line.split("\\s+");
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
                        i = t.length;
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
            haveExact[idx] = !bound;
            haveAny[idx] = true;

            if (idx == 0) {
                hasMate = isMate;
                mateIn = whiteMate;
                if (w >= 0) {
                    wdlW = whiteToMove ? w : l;
                    wdlD = d;
                    wdlL = whiteToMove ? l : w;
                }
                reachedDepth = depth;
            }

            if (pvStart >= 0 && pvStart < t.length) {
                firstMoves[idx] = t[pvStart];
                if (idx == 0) {
                    bestPv = new ArrayList<>(t.length - pvStart);
                    for (int j = pvStart; j < t.length; j++) bestPv.add(t[j]);
                }
            }

            // Stream intermediate updates to listener & Eval Bar
            if (progress != null && idx == 0 && !bound && !stopSent
                    && depth >= PROGRESS_MIN_DEPTH && depth > reportedDepth && depth < targetDepth
                    && firstMoves[0] != null) {
                long now = System.currentTimeMillis();
                if (now - lastReport >= PROGRESS_INTERVAL_MS) {
                    lastReport = now;
                    reportedDepth = depth;
                    try {
                        progress.onProgress(new AnalysisResult(
                                Collections.singletonList(firstMoves[0]),
                                whiteScore, hasMate,
                                mateIn, wdlW, wdlD, wdlL,
                                bestPv != null && bestPv.size() > 1 ? bestPv.get(1) : null,
                                bestPv, new float[]{whiteScore}, depth, false));
                    } catch (Throwable ignored) {}
                }
            }
        }

        return buildResult(firstMoves, scores, haveAny, multiPV, hasMate, mateIn,
                wdlW, wdlD, wdlL, ponder, bestPv, reachedDepth, terminal, bestmove);
    }

    private AnalysisResult buildResult(String[] firstMovesIn, float[] scoresIn,
                                       boolean[] haveAny, int multiPV, boolean hasMate, int mateIn,
                                       int wdlW, int wdlD, int wdlL, String ponder,
                                       List<String> bestPv, int reachedDepth,
                                       boolean terminal, String bestmove) {
        String[] firstMoves = firstMovesIn.clone();
        float[] scores = scoresIn.clone();

        List<String> moves = new ArrayList<>(multiPV);
        List<Float> lineScoreList = new ArrayList<>(multiPV);
        if (bestmove != null && firstMoves[0] == null) {
            firstMoves[0] = bestmove;
            haveAny[0] = true;
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

    // ── Communication Helpers ──────────────────────────────────────────────────

    private void sendUciCommand(String cmd) {
        if ("wasm".equals(activeEngineMode)) {
            if (wasmManager != null) wasmManager.sendPlayCommand(cmd);
        } else {
            sendNative(cmd);
        }
    }

    private void sendNative(String cmd) {
        if (nativeStdin != null) {
            nativeStdin.println(cmd);
        }
    }

    private String readLineFromActiveEngine() throws IOException {
        if ("wasm".equals(activeEngineMode)) {
            return wasmManager != null ? wasmManager.readPlayLine(500) : null;
        } else {
            return nativeStdout != null ? nativeStdout.readLine() : null;
        }
    }

    private boolean waitForLine(String token, long timeoutMs) {
        if ("wasm".equals(activeEngineMode)) {
            long deadline = System.currentTimeMillis() + timeoutMs;
            while (System.currentTimeMillis() < deadline) {
                String line = wasmManager != null ? wasmManager.readPlayLine(500) : null;
                if (line != null && line.startsWith(token)) return true;
            }
            return false;
        } else {
            return waitForNativeLine(token, timeoutMs);
        }
    }

    private boolean waitForNativeLine(String token, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            try {
                if (nativeStdout == null) return false;
                String line = nativeStdout.readLine();
                if (line == null) break;
                if (line.startsWith(token)) return true;
            } catch (Throwable ignored) {
                break;
            }
        }
        return false;
    }

    private void drainReady() {
        if ("wasm".equals(activeEngineMode)) {
            if (wasmManager != null) wasmManager.clearPlayOutputQueue();
        } else {
            try {
                while (nativeStdout != null && nativeStdout.ready()) {
                    if (nativeStdout.readLine() == null) break;
                }
            } catch (Throwable ignored) {}
        }
    }

    private File extractBinary(Context context) {
        String nativeLibDir = context.getApplicationInfo().nativeLibraryDir;
        File engineBin = new File(nativeLibDir, "libstockfish.so");

        if (!engineBin.exists()) {
            NnvcLogger.e(TAG, "Stockfish binary not found at: " + engineBin.getAbsolutePath());
            return null;
        }
        if (!engineBin.canExecute()) {
            engineBin.setExecutable(true);
            if (!engineBin.canExecute()) {
                NnvcLogger.e(TAG, "Stockfish binary is not executable: " + engineBin.getAbsolutePath());
                return null;
            }
        }
        return engineBin;
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
    }

    private void applyPlayEngineOptions(Context context, int elo, int multiPV, boolean honorLimit) {
        int neededMpv = Math.max(3, multiPV);
        int finalMpv = Math.max(1, Math.min(8, neededMpv));
        if (finalMpv != curMultiPV) {
            sendUciCommand("setoption name MultiPV value " + finalMpv);
            curMultiPV = finalMpv;
        }

        if ("wasm".equals(activeEngineMode)) {
            if (curHash != 16) {
                sendUciCommand("setoption name Hash value 16");
                curHash = 16;
            }
            if (curThreads != 1) {
                sendUciCommand("setoption name Threads value 1");
                curThreads = 1;
            }
        } else {
            applyThreads(StockfishSettings.getThreads(context));
            applyHash(computeHashMb(context));
        }

        sendUciCommand("setoption name Ponder value false");

        if (elo >= 1320) {
            if (curLimitStrength == null || !curLimitStrength) {
                sendUciCommand("setoption name UCI_LimitStrength value true");
                curLimitStrength = true;
            }
            if (curElo != elo) {
                sendUciCommand("setoption name UCI_Elo value " + elo);
                curElo = elo;
            }
        } else {
            if (curLimitStrength == null || curLimitStrength) {
                sendUciCommand("setoption name UCI_LimitStrength value false");
                curLimitStrength = false;
            }
            int skillFromElo = Math.max(0, Math.min(20, Math.round((elo - 600f) / 130f)));
            if (curSkillLevel != skillFromElo) {
                sendUciCommand("setoption name Skill Level value " + skillFromElo);
                curSkillLevel = skillFromElo;
            }
        }
    }

    private void applyThreads(int threads) {
        threads = Math.max(1, threads);
        if (threads != curThreads) {
            sendUciCommand("setoption name Threads value " + threads);
            curThreads = threads;
        }
    }

    private void applyHash(int mb) {
        if (mb != curHash) {
            sendUciCommand("setoption name Hash value " + mb);
            curHash = mb;
        }
    }

    static int computeHashMb(Context context) {
        long totalMb = 0;
        try {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (am != null) {
                ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
                am.getMemoryInfo(mi);
                totalMb = mi.totalMem / (1024L * 1024L);
            }
        } catch (Throwable ignored) {}
        if (totalMb >= 11_000) return 768;
        if (totalMb >= 7_000) return 512;
        if (totalMb >= 5_000) return 256;
        if (totalMb >= 3_000) return 128;
        return 32;
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
}
