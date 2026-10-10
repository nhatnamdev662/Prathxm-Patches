package app.prathxm.chess.extension.stockfish;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.ServiceWorkerClient;
import android.webkit.ServiceWorkerController;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * WasmEngineManager – Manages Stockfish 18 WASM and Komodo Dragon 3.3 WASM
 * inside a headless background WebView running dedicated Web Workers.
 *
 * Architecture:
 *  - 2 Web Workers:
 *     1) 'playWorker': Runs the active play engine (Stockfish 18 or Komodo 3.3).
 *     2) 'classifyWorker': Dedicated Stockfish 18 engine for Eval Bar & Move Classification
 *        (Hash=64, Threads=1, MultiPV=3, Depth=10).
 *  - High-performance blocking UCI communication for StockfishProcess background worker threads.
 *  - Real-time UCI stream parsing from classifyWorker to drive EvalBarView directly.
 */
public class WasmEngineManager {

    private static final String TAG = "WasmEngineManager";
    private static final String HOST = "https://nnvc-engine.local/";

    private static volatile WasmEngineManager instance;

    private final Context context;
    private final Handler mainHandler;
    private WebView webView;

    private final Object playReadyLock = new Object();
    private final Object classifyReadyLock = new Object();

    private volatile boolean isReady = false;
    private volatile boolean isPlayReady = false;
    private volatile boolean isClassifyReady = false;
    private volatile String currentPlayEngineKey = StockfishSettings.ENGINE_KOMODO;

    // UCI Line queues for synchronous readSearchOutput
    private final BlockingQueue<String> playUciOutputQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<String> classifyUciOutputQueue = new LinkedBlockingQueue<>();

    // Listeners for async/intermediate monitoring
    public interface UciLineListener {
        void onLine(String line);
    }
    private volatile UciLineListener playLineListener;
    private volatile UciLineListener classifyLineListener;

    // Track active position for classifyWorker
    private volatile String currentClassifyFen = null;
    private volatile boolean currentClassifyWhiteToMove = true;

    public static WasmEngineManager getInstance(Context context) {
        if (instance == null) {
            synchronized (WasmEngineManager.class) {
                if (instance == null) {
                    instance = new WasmEngineManager(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    private WasmEngineManager(Context context) {
        this.context = context;
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.currentPlayEngineKey = StockfishSettings.getEngineChoice(context);
        mainHandler.post(this::initWebView);
    }

    public boolean isReady() {
        return isReady && isPlayReady;
    }

    public boolean isClassifyReady() {
        return isClassifyReady;
    }

    public boolean hasWasmFiles(String engineKey) {
        if ("komodo".equals(engineKey)) {
            File wasm = getKomodoWasm();
            File js = getKomodoJs();
            return wasm != null && wasm.exists() && js != null && js.exists();
        } else if ("stockfish18".equals(engineKey)) {
            File wasm = getStockfishWasm();
            File js = getStockfishJs();
            return wasm != null && wasm.exists() && js != null && js.exists();
        }
        return false;
    }

    public boolean waitForPlayWorkerReady(long timeoutMs) {
        if (!hasWasmFiles(currentPlayEngineKey)) {
            NnvcLogger.d(TAG, "No WASM files for " + currentPlayEngineKey + "; skipping wait.");
            return false;
        }
        long deadline = System.currentTimeMillis() + timeoutMs;
        synchronized (playReadyLock) {
            while (!isPlayReady && System.currentTimeMillis() < deadline) {
                try {
                    long wait = deadline - System.currentTimeMillis();
                    if (wait > 0) playReadyLock.wait(wait);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
            return isPlayReady;
        }
    }

    public boolean waitForClassifyWorkerReady(long timeoutMs) {
        if (!hasWasmFiles("stockfish18")) {
            return false;
        }
        long deadline = System.currentTimeMillis() + timeoutMs;
        synchronized (classifyReadyLock) {
            while (!isClassifyReady && System.currentTimeMillis() < deadline) {
                try {
                    long wait = deadline - System.currentTimeMillis();
                    if (wait > 0) classifyReadyLock.wait(wait);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
            return isClassifyReady;
        }
    }

    // ── Asset Resolution ───────────────────────────────────────────────────────

    private File getWasmDir() {
        File dir = new File(context.getFilesDir(), "wasm_engines");
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    public File getAssetOrCopy(String assetSubpath, String targetFileName) {
        File targetFile = new File(getWasmDir(), targetFileName);
        if (targetFile.exists() && targetFile.length() > 0) {
            return targetFile;
        }

        // 1. Try extracting from app Assets
        String[] candidatePaths = {
                assetSubpath,
                "assets/" + assetSubpath
        };
        for (String p : candidatePaths) {
            try (InputStream is = context.getAssets().open(p)) {
                copyStream(is, targetFile);
                if (targetFile.exists() && targetFile.length() > 0) {
                    Log.i(TAG, "Extracted from assets: " + p + " -> " + targetFile.getAbsolutePath() + " (" + targetFile.length() + " bytes)");
                    return targetFile;
                }
            } catch (Throwable ignored) {}
        }

        // 2. Try extracting directly from APK Zip (context.getPackageResourcePath())
        try {
            String apkPath = context.getPackageResourcePath();
            if (apkPath != null) {
                try (ZipFile zip = new ZipFile(apkPath)) {
                    for (String p : candidatePaths) {
                        ZipEntry entry = zip.getEntry(p);
                        if (entry != null) {
                            try (InputStream is = zip.getInputStream(entry)) {
                                copyStream(is, targetFile);
                                if (targetFile.exists() && targetFile.length() > 0) {
                                    Log.i(TAG, "Extracted from APK zip: " + p + " -> " + targetFile.getAbsolutePath() + " (" + targetFile.length() + " bytes)");
                                    return targetFile;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Throwable ignored) {}

        // 3. Try checking Downloads folder
        try {
            File dl = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), targetFileName);
            if (dl.exists() && dl.length() > 0) {
                try (InputStream is = new FileInputStream(dl)) {
                    copyStream(is, targetFile);
                    if (targetFile.exists() && targetFile.length() > 0) {
                        Log.i(TAG, "Copied from Downloads: " + dl.getAbsolutePath() + " -> " + targetFile.getAbsolutePath());
                        return targetFile;
                    }
                }
            }
        } catch (Throwable ignored) {}

        return targetFile.exists() && targetFile.length() > 0 ? targetFile : null;
    }

    public File getStockfishWasm() {
        return getAssetOrCopy("stockfish18/stockfish-18-lite-single.wasm", "stockfish-18-lite-single.wasm");
    }

    public File getStockfishJs() {
        return getAssetOrCopy("stockfish18/stockfish-18-lite-single.js", "stockfish-18-lite-single.js");
    }

    public File getKomodoWasm() {
        return getAssetOrCopy("komodo/dragon3.3.wasm", "dragon3.3.wasm");
    }

    public File getKomodoJs() {
        return getAssetOrCopy("komodo/komodo.js", "komodo.js");
    }

    public File getKomodoBook() {
        return getAssetOrCopy("komodo/book.bin", "book.bin");
    }

    private String readStreamToString(InputStream is) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            char[] buf = new char[8192];
            int read;
            while ((read = reader.read(buf)) != -1) {
                sb.append(buf, 0, read);
            }
        }
        return sb.toString();
    }

    private static void copyStream(InputStream in, File dst) throws Exception {
        try (FileOutputStream out = new FileOutputStream(dst)) {
            byte[] buf = new byte[65536];
            int read;
            while ((read = in.read(buf)) != -1) {
                out.write(buf, 0, read);
            }
        }
    }

    // ── Headless WebView Initialization ───────────────────────────────────────

    private WebResourceResponse handleIntercept(String url) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Access-Control-Allow-Methods", "GET, HEAD, OPTIONS");
        headers.put("Access-Control-Allow-Headers", "*");

        if (url.contains("stockfish-18-lite-single.wasm")) {
            try {
                File f = getStockfishWasm();
                if (f != null && f.exists()) {
                    return new WebResourceResponse("application/wasm", "binary", 200, "OK", headers, new FileInputStream(f));
                }
            } catch (Throwable t) {
                Log.e(TAG, "Error streaming stockfish wasm: " + t.getMessage());
            }
        } else if (url.contains("stockfish-18-lite-single.js")) {
            try {
                File f = getStockfishJs();
                if (f != null && f.exists()) {
                    String source = readStreamToString(new FileInputStream(f));
                    // Replace stockfish wasm resolution with absolute URL
                    source = source.replace("location.origin+location.pathname.replace(/\\.js$/i,\".wasm\")",
                            "\"" + HOST + "stockfish-18-lite-single.wasm\"");
                    byte[] bytes = source.getBytes(StandardCharsets.UTF_8);
                    return new WebResourceResponse("application/javascript", "utf-8", 200, "OK", headers, new ByteArrayInputStream(bytes));
                }
            } catch (Throwable t) {
                Log.e(TAG, "Error streaming stockfish js: " + t.getMessage());
            }
        } else if (url.contains("dragon3.3.wasm")) {
            try {
                File f = getKomodoWasm();
                if (f != null && f.exists()) {
                    return new WebResourceResponse("application/wasm", "binary", 200, "OK", headers, new FileInputStream(f));
                }
            } catch (Throwable t) {
                Log.e(TAG, "Error streaming komodo wasm: " + t.getMessage());
            }
        } else if (url.contains("komodo.js")) {
            try {
                File f = getKomodoJs();
                if (f != null && f.exists()) {
                    String source = readStreamToString(new FileInputStream(f));
                    // NNVC Extension Komodo replacements
                    source = source.replace("__NNCV_KOMODO_WASM_URL__", HOST + "dragon3.3.wasm")
                                   .replace("__NNCV_KOMODO_BOOK_URL__", HOST + "book.bin");
                    byte[] bytes = source.getBytes(StandardCharsets.UTF_8);
                    return new WebResourceResponse("application/javascript", "utf-8", 200, "OK", headers, new ByteArrayInputStream(bytes));
                }
            } catch (Throwable t) {
                Log.e(TAG, "Error streaming komodo js: " + t.getMessage());
            }
        } else if (url.contains("book.bin")) {
            try {
                File f = getKomodoBook();
                if (f != null && f.exists()) {
                    return new WebResourceResponse("application/octet-stream", "binary", 200, "OK", headers, new FileInputStream(f));
                }
            } catch (Throwable t) {
                Log.e(TAG, "Error streaming komodo book: " + t.getMessage());
            }
        }
        return null;
    }

    private void initWebView() {
        try {
            File sfWasm = getStockfishWasm();
            File sfJs = getStockfishJs();
            File kmWasm = getKomodoWasm();
            File kmJs = getKomodoJs();
            File kmBook = getKomodoBook();

            if (sfWasm == null && kmWasm == null) {
                NnvcLogger.i(TAG, "No WASM engine binaries found yet (SF=null, KM=null). WebView bootstrap deferred.");
                return;
            }

            NnvcLogger.i(TAG, "Starting WebView... (SF=" + (sfWasm != null) + ", KM=" + (kmWasm != null) + ")");

            webView = new WebView(context);
            WebSettings settings = webView.getSettings();
            settings.setJavaScriptEnabled(true);
            settings.setAllowFileAccess(true);
            settings.setAllowContentAccess(true);
            settings.setDomStorageEnabled(true);
            settings.setMediaPlaybackRequiresUserGesture(true);

            webView.setWebChromeClient(new android.webkit.WebChromeClient() {
                @Override
                public boolean onConsoleMessage(android.webkit.ConsoleMessage cm) {
                    TorchEngine.log("[WASM Console " + cm.messageLevel() + "] " + cm.message());
                    return true;
                }
            });

            webView.addJavascriptInterface(new WasmEngineJsBridge(), "WasmEngineBridge");

            webView.setWebViewClient(new WebViewClient() {
                @Override
                public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Access-Control-Allow-Origin", "*");
                        headers.put("Access-Control-Allow-Methods", "GET, HEAD, OPTIONS");
                        headers.put("Access-Control-Allow-Headers", "*");
                        return new WebResourceResponse("text/plain", "utf-8", 200, "OK", headers, new ByteArrayInputStream(new byte[0]));
                    }
                    WebResourceResponse resp = handleIntercept(request.getUrl().toString());
                    if (resp != null) return resp;
                    return super.shouldInterceptRequest(view, request);
                }
            });

            // Register ServiceWorker interception on API 24+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                try {
                    ServiceWorkerController swc = ServiceWorkerController.getInstance();
                    swc.setServiceWorkerClient(new ServiceWorkerClient() {
                        @Override
                        public WebResourceResponse shouldInterceptRequest(WebResourceRequest request) {
                            WebResourceResponse resp = handleIntercept(request.getUrl().toString());
                            if (resp != null) return resp;
                            return super.shouldInterceptRequest(request);
                        }
                    });
                } catch (Throwable ignored) {}
            }

            String html = "<!DOCTYPE html><html><head><meta charset='utf-8'></head><body><script>\n" +
                    "(function() {\n" +
                    "    window.AudioContext = undefined; window.webkitAudioContext = undefined; window.Audio = undefined;\n" +
                    "    console.log('[WasmManager] Bootstrap starting...');\n" +
                    "    window._playWorker = null;\n" +
                    "    window._classifyWorker = null;\n" +
                    "    window._playWorkerReady = false;\n" +
                    "    window._classifyWorkerReady = false;\n" +
                    "    window._playPendingCmds = [];\n" +
                    "    window._classifyPendingCmds = [];\n" +
                    "    window._playEngineKey = '" + currentPlayEngineKey + "';\n" +
                    "\n" +
                    "    function initWorker(kind, engineKey) {\n" +
                    "        const jsUrl = (engineKey === 'komodo') ? '" + HOST + "komodo.js' : '" + HOST + "stockfish-18-lite-single.js';\n" +
                    "        fetch(jsUrl).then(r => r.text()).then(sourceText => {\n" +
                    "            let code = sourceText;\n" +
                    "            if (engineKey === 'stockfish18') {\n" +
                    "                code = code.split('location.origin+location.pathname.replace(/\\\\.js$/i,\".wasm\")').join(JSON.stringify('" + HOST + "stockfish-18-lite-single.wasm'));\n" +
                    "            }\n" +
                    "            const blob = new Blob([code], { type: 'application/javascript' });\n" +
                    "            const hashTarget = (engineKey === 'komodo') ? '" + HOST + "dragon3.3.wasm' : '" + HOST + "stockfish-18-lite-single.wasm';\n" +
                    "            const blobUrl = URL.createObjectURL(blob) + '#' + encodeURIComponent(hashTarget);\n" +
                    "            const worker = new Worker(blobUrl);\n" +
                    "            worker.onmessage = function(e) {\n" +
                    "                const line = (typeof e.data === 'string') ? e.data : String(e.data || '');\n" +
                    "                if (line === 'uciok' || line === 'readyok') {\n" +
                    "                    if (kind === 'play') window._playWorkerReady = true;\n" +
                    "                    else if (kind === 'classify') window._classifyWorkerReady = true;\n" +
                    "                }\n" +
                    "                if (window.WasmEngineBridge) window.WasmEngineBridge.onEngineOutput(kind, line);\n" +
                    "            };\n" +
                    "            worker.onerror = function(err) {\n" +
                    "                console.error('[Worker Error ' + kind + '] ' + (err.message || err));\n" +
                    "                if (window.WasmEngineBridge) window.WasmEngineBridge.onEngineError(kind, String(err.message || err));\n" +
                    "            };\n" +
                    "            if (kind === 'play') {\n" +
                    "                if (window._playWorker) { try { window._playWorker.terminate(); } catch(e){} }\n" +
                    "                window._playWorker = worker;\n" +
                    "                window._playEngineKey = engineKey;\n" +
                    "                window._playWorkerReady = true;\n" +
                    "                while (window._playPendingCmds.length > 0) {\n" +
                    "                    worker.postMessage(window._playPendingCmds.shift());\n" +
                    "                }\n" +
                    "            } else {\n" +
                    "                if (window._classifyWorker) { try { window._classifyWorker.terminate(); } catch(e){} }\n" +
                    "                window._classifyWorker = worker;\n" +
                    "                window._classifyWorkerReady = true;\n" +
                    "                // Exact NNVC Extension options for classify engine\n" +
                    "                worker.postMessage('uci');\n" +
                    "                worker.postMessage('setoption name Hash value 64');\n" +
                    "                worker.postMessage('setoption name Threads value 1');\n" +
                    "                worker.postMessage('setoption name MultiPV value 3');\n" +
                    "                worker.postMessage('setoption name Ponder value false');\n" +
                    "                worker.postMessage('isready');\n" +
                    "                while (window._classifyPendingCmds.length > 0) {\n" +
                    "                    worker.postMessage(window._classifyPendingCmds.shift());\n" +
                    "                }\n" +
                    "            }\n" +
                    "            if (window.WasmEngineBridge) window.WasmEngineBridge.onEngineWorkerReady(kind, engineKey);\n" +
                    "        }).catch(err => {\n" +
                    "            console.error('[Worker Load Failed ' + kind + '] ' + err);\n" +
                    "            if (window.WasmEngineBridge) window.WasmEngineBridge.onEngineError(kind, String(err));\n" +
                    "        });\n" +
                    "    }\n" +
                    "\n" +
                    "    window.startEngines = function(playKey) {\n" +
                    "        initWorker('play', playKey || 'komodo');\n" +
                    "        initWorker('classify', 'stockfish18');\n" +
                    "    };\n" +
                    "    window.switchPlayEngine = function(playKey) {\n" +
                    "        window._playWorkerReady = false;\n" +
                    "        initWorker('play', playKey);\n" +
                    "    };\n" +
                    "    window.sendPlayCmd = function(cmd) {\n" +
                    "        if (window._playWorker && window._playWorkerReady) {\n" +
                    "            window._playWorker.postMessage(cmd);\n" +
                    "        } else {\n" +
                    "            window._playPendingCmds.push(cmd);\n" +
                    "        }\n" +
                    "    };\n" +
                    "    window.sendClassifyCmd = function(cmd) {\n" +
                    "        if (window._classifyWorker && window._classifyWorkerReady) {\n" +
                    "            window._classifyWorker.postMessage(cmd);\n" +
                    "        } else {\n" +
                    "            window._classifyPendingCmds.push(cmd);\n" +
                    "        }\n" +
                    "    };\n" +
                    "    window.startEngines('" + currentPlayEngineKey + "');\n" +
                    "})();\n" +
                    "</script></body></html>";

            webView.loadDataWithBaseURL(HOST, html, "text/html", "UTF-8", null);

        } catch (Throwable t) {
            TorchEngine.log("[WASM ENGINE ERROR] " + t.getMessage());
            Log.e(TAG, "Failed to initialize WasmEngineManager: " + t.getMessage(), t);
        }
    }

    // ── Command Dispatch & Synchronous Output Stream ──────────────────────────

    public void sendPlayCommand(String cmd) {
        mainHandler.post(() -> {
            try {
                if (webView != null) {
                    String escaped = cmd.replace("\\", "\\\\").replace("'", "\\'");
                    webView.evaluateJavascript("if (window.sendPlayCmd) window.sendPlayCmd('" + escaped + "');", null);
                }
            } catch (Throwable ignored) {}
        });
    }

    public void sendClassifyCommand(String cmd) {
        mainHandler.post(() -> {
            try {
                if (webView != null) {
                    String escaped = cmd.replace("\\", "\\\\").replace("'", "\\'");
                    webView.evaluateJavascript("if (window.sendClassifyCmd) window.sendClassifyCmd('" + escaped + "');", null);
                }
            } catch (Throwable ignored) {}
        });
    }

    /**
     * Dedicated Classify Engine evaluation (Stockfish 18 at locked Depth = 10, MultiPV = 3).
     * Drives the Eval Bar independently from the play engine.
     */
    public void evaluateClassifyPosition(String fen, List<String> moves) {
        if (fen == null) return;
        currentClassifyFen = fen;
        currentClassifyWhiteToMove = isWhiteTurnFromFen(fen);

        sendClassifyCommand("stop");
        StringBuilder pos = new StringBuilder(fen.length() + 8 + (moves != null ? moves.size() * 6 : 0));
        pos.append("position fen ").append(fen);
        if (moves != null && !moves.isEmpty()) {
            pos.append(" moves");
            for (String m : moves) pos.append(' ').append(m);
        }
        sendClassifyCommand(pos.toString());
        sendClassifyCommand("go depth 10");
        TorchEngine.log("[WASM CLASSIFY GO] depth 10 on FEN: " + fen);
    }

    private static boolean isWhiteTurnFromFen(String fen) {
        if (fen == null) return true;
        String[] parts = fen.split("\\s+");
        return parts.length < 2 || !"b".equalsIgnoreCase(parts[1]);
    }

    public void switchEngine(String engineKey) {
        if (engineKey == null || engineKey.equals(currentPlayEngineKey)) return;
        currentPlayEngineKey = engineKey;
        synchronized (playReadyLock) {
            isPlayReady = false;
        }
        playUciOutputQueue.clear();
        mainHandler.post(() -> {
            try {
                if (webView != null) {
                    webView.evaluateJavascript("if (window.switchPlayEngine) window.switchPlayEngine('" + engineKey + "');", null);
                }
            } catch (Throwable ignored) {}
        });
    }

    public void clearPlayOutputQueue() {
        playUciOutputQueue.clear();
    }

    public void clearClassifyOutputQueue() {
        classifyUciOutputQueue.clear();
    }

    public String readPlayLine(long timeoutMs) {
        try {
            return playUciOutputQueue.poll(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public String readClassifyLine(long timeoutMs) {
        try {
            return classifyUciOutputQueue.poll(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public void setPlayLineListener(UciLineListener listener) {
        this.playLineListener = listener;
    }

    public void setClassifyLineListener(UciLineListener listener) {
        this.classifyLineListener = listener;
    }

    // ── Javascript Bridge ─────────────────────────────────────────────────────

    private class WasmEngineJsBridge {
        @JavascriptInterface
        public void onEngineWorkerReady(String kind, String engineKey) {
            TorchEngine.log("[WASM BRIDGE] Worker READY: kind=" + kind + ", engine=" + engineKey);
            if ("play".equals(kind)) {
                synchronized (playReadyLock) {
                    isPlayReady = true;
                    isReady = true;
                    playReadyLock.notifyAll();
                }
            } else if ("classify".equals(kind)) {
                synchronized (classifyReadyLock) {
                    isClassifyReady = true;
                    classifyReadyLock.notifyAll();
                }
            }
        }

        @JavascriptInterface
        public void onEngineError(String kind, String error) {
            TorchEngine.log("[WASM BRIDGE ERROR] kind=" + kind + ", error=" + error);
            Log.e(TAG, "WASM Engine Error [" + kind + "]: " + error);
        }

        @JavascriptInterface
        public void onEngineOutput(String kind, String line) {
            if ("play".equals(kind)) {
                playUciOutputQueue.offer(line);
                UciLineListener l = playLineListener;
                if (l != null) l.onLine(line);
            } else if ("classify".equals(kind)) {
                classifyUciOutputQueue.offer(line);
                UciLineListener l = classifyLineListener;
                if (l != null) l.onLine(line);

                // Parse UCI output for real-time Eval Bar updates
                handleClassifyUciLine(line);
            }
        }
    }

    private void handleClassifyUciLine(String line) {
        if (line == null || !line.startsWith("info ") || line.indexOf(" score ") < 0) return;
        if (line.indexOf("multipv 1 ") < 0 && !line.contains("multipv 1\t")) return;

        try {
            String[] tokens = line.split("\\s+");
            boolean isMate = false;
            int scoreVal = 0;
            boolean foundScore = false;

            for (int i = 0; i < tokens.length - 2; i++) {
                if ("score".equals(tokens[i])) {
                    String type = tokens[i + 1];
                    if ("cp".equals(type)) {
                        scoreVal = Integer.parseInt(tokens[i + 2]);
                        isMate = false;
                        foundScore = true;
                        break;
                    } else if ("mate".equals(type)) {
                        scoreVal = Integer.parseInt(tokens[i + 2]);
                        isMate = true;
                        foundScore = true;
                        break;
                    }
                }
            }

            if (!foundScore) return;

            boolean whiteToMove = currentClassifyWhiteToMove;
            float whiteScore;
            int whiteMate = 0;
            if (isMate) {
                whiteMate = whiteToMove ? scoreVal : -scoreVal;
                whiteScore = whiteMate > 0 ? (StockfishProcess.MATE_SCORE - whiteMate) : (-StockfishProcess.MATE_SCORE - whiteMate);
            } else {
                float pawns = scoreVal / 100.0f;
                whiteScore = whiteToMove ? pawns : -pawns;
            }

            final float finalScore = whiteScore;
            final boolean finalHasMate = isMate;
            final int finalMateIn = whiteMate;

            mainHandler.post(() -> {
                try {
                    if (StockfishSettings.isEvalBarEnabled(context)) {
                        OverlayManager.updateEvalBar(finalScore, finalHasMate, finalMateIn, StockfishExtension.getStateImpl());
                    }
                } catch (Throwable ignored) {}
            });
        } catch (Throwable ignored) {}
    }
}
