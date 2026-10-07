package app.prathxm.chess.extension.stockfish;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TorchEngine – runs the official 26MB WebAssembly Torch Engine (CEE)
 * inside a headless background WebView Web Worker.
 * Communicates via UCI declarative position commands and JSON output,
 * producing 100% authentic move classifications identical to the NNVC Extension.
 */
public class TorchEngine {

    private static final String TAG = "TorchEngine";
    private static final String TORCH_WASM_URL = "https://github.com/nhatnamdev662/Prathxm-Patches/releases/download/v2.0.9/torch.wasm";
    private static final String TORCH_JS_URL = "https://github.com/nhatnamdev662/Prathxm-Patches/releases/download/v2.0.9/torch.js";

    private static volatile TorchEngine instance;
    private final Context context;
    private final Handler mainHandler;
    private WebView webView;

    private volatile boolean isReady = false;
    private final AtomicBoolean isDownloading = new AtomicBoolean(false);

    public static final java.util.List<String> DIAGNOSTIC_LOGS = new java.util.concurrent.CopyOnWriteArrayList<>();

    public static void log(String msg) {
        String entry = new java.text.SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.US).format(new java.util.Date()) + " " + msg;
        DIAGNOSTIC_LOGS.add(entry);
        while (DIAGNOSTIC_LOGS.size() > 100) {
            DIAGNOSTIC_LOGS.remove(0);
        }
    }

    public static String getFormattedLogs() {
        if (DIAGNOSTIC_LOGS.isEmpty()) return "Chưa có log nào được ghi nhận.";
        StringBuilder sb = new StringBuilder();
        for (String l : DIAGNOSTIC_LOGS) {
            sb.append(l).append("\n");
        }
        return sb.toString();
    }

    public interface TorchClassificationCallback {
        void onClassification(String classificationName, String playedMoveLan, String bestMoveLan, String speechText, String rawJson);
    }

    private TorchClassificationCallback activeCallback;

    public static TorchEngine getInstance(Context context) {
        if (instance == null) {
            synchronized (TorchEngine.class) {
                if (instance == null) {
                    instance = new TorchEngine(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    private TorchEngine(Context context) {
        this.context = context;
        this.mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(this::initWebView);
    }

    public boolean isReady() {
        return isReady;
    }

    private File getTorchDir() {
        File dir = new File(context.getFilesDir(), "torch");
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    public File getWasmFile() {
        // 1. App internal files
        File f = new File(getTorchDir(), "torch.wasm");
        if (f.exists() && f.length() > 10_000_000) return f;

        // 2. Download folder
        try {
            File dl = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "torch.wasm");
            if (dl.exists() && dl.length() > 10_000_000) {
                copyFile(dl, f);
                return f;
            }
        } catch (Throwable ignored) {}

        // 3. Asset copy
        try {
            InputStream is = context.getAssets().open("torch/torch.wasm");
            copyStream(is, f);
            if (f.exists() && f.length() > 10_000_000) return f;
        } catch (Throwable ignored) {}

        return null;
    }

    public File getJsFile() {
        File f = new File(getTorchDir(), "torch.js");
        if (f.exists() && f.length() > 50_000) return f;

        try {
            InputStream is = context.getAssets().open("torch/torch.js");
            copyStream(is, f);
            if (f.exists() && f.length() > 50_000) return f;
        } catch (Throwable ignored) {}

        return null;
    }

    private void initWebView() {
        try {
            File wasm = getWasmFile();
            File js = getJsFile();

            if (wasm == null || js == null) {
                Log.w(TAG, "Torch binary files not found yet. Triggering auto-download...");
                downloadTorchFilesAsync();
                return;
            }

            Log.i(TAG, "Initializing headless WebView with Torch engine files...");
            log("[INIT] Khởi động headless WebView với torch files (wasm=" + (wasm != null ? wasm.length() : 0) + ", js=" + (js != null ? js.length() : 0) + ")");
            webView = new WebView(context);
            WebSettings settings = webView.getSettings();
            settings.setJavaScriptEnabled(true);
            settings.setAllowFileAccess(true);
            settings.setAllowContentAccess(true);
            settings.setDomStorageEnabled(true);

            webView.setWebChromeClient(new android.webkit.WebChromeClient() {
                @Override
                public boolean onConsoleMessage(android.webkit.ConsoleMessage cm) {
                    log("[JS Console " + cm.messageLevel() + "] " + cm.message() + " (line " + cm.lineNumber() + ")");
                    return true;
                }
            });

            webView.addJavascriptInterface(new TorchJsBridge(), "TorchBridge");

            webView.setWebViewClient(new WebViewClient() {
                @Override
                public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                    String url = request.getUrl().toString();
                    if (url.contains("torch.wasm")) {
                        try {
                            File w = getWasmFile();
                            if (w != null && w.exists()) {
                                Map<String, String> headers = new HashMap<>();
                                headers.put("Access-Control-Allow-Origin", "*");
                                log("[HTTP] Đã stream torch.wasm (" + w.length() + " bytes)");
                                return new WebResourceResponse("application/wasm", "binary", 200, "OK", headers, new FileInputStream(w));
                            }
                        } catch (Throwable t) {
                            log("[HTTP ERROR] Lỗi stream torch.wasm: " + t.getMessage());
                            Log.e(TAG, "Error streaming torch.wasm: " + t.getMessage());
                        }
                    } else if (url.contains("torch.js")) {
                        try {
                            File j = getJsFile();
                            if (j != null && j.exists()) {
                                Map<String, String> headers = new HashMap<>();
                                headers.put("Access-Control-Allow-Origin", "*");
                                log("[HTTP] Đã stream torch.js (" + j.length() + " bytes)");
                                return new WebResourceResponse("application/javascript", "utf-8", 200, "OK", headers, new FileInputStream(j));
                            }
                        } catch (Throwable t) {
                            log("[HTTP ERROR] Lỗi stream torch.js: " + t.getMessage());
                            Log.e(TAG, "Error streaming torch.js: " + t.getMessage());
                        }
                    }
                    return super.shouldInterceptRequest(view, request);
                }
            });

            String html = "<!DOCTYPE html><html><head><meta charset='utf-8'></head><body><script>\n" +
                    "(function() {\n" +
                    "    console.log('[Torch] Starting in-memory worker bootstrap...');\n" +
                    "    Promise.all([\n" +
                    "        fetch('https://torch-engine.local/torch.js').then(r => r.text()),\n" +
                    "        fetch('https://torch-engine.local/torch.wasm').then(r => r.arrayBuffer())\n" +
                    "    ]).then(([jsText, wasmBuffer]) => {\n" +
                    "        console.log('[Torch] JS and WASM fetched in main doc. Size: ' + wasmBuffer.byteLength);\n" +
                    "        const workerScript = `\n" +
                    "            let torchHandler = null;\n" +
                    "            let ready = false;\n" +
                    "            const queued = [];\n" +
                    "            function bufferToBinaryString(buffer) {\n" +
                    "                const bytes = new Uint8Array(buffer);\n" +
                    "                let result = '';\n" +
                    "                const chunkSize = 32768;\n" +
                    "                for (let i = 0; i < bytes.length; i += chunkSize) result += String.fromCharCode.apply(null, bytes.subarray(i, i + chunkSize));\n" +
                    "                return result;\n" +
                    "            }\n" +
                    "            function installWasmXhr(wasmBuffer) {\n" +
                    "                const NativeXHR = self.XMLHttpRequest;\n" +
                    "                const isWasmUrl = (url) => String(url || '').includes('torch.wasm');\n" +
                    "                class WasmAwareXHR {\n" +
                    "                    constructor() {\n" +
                    "                        this.readyState = 0; this.response = null; this.responseText = ''; this.responseType = '';\n" +
                    "                        this.status = 0; this.statusText = ''; this._async = true; this._headers = {};\n" +
                    "                        this._native = NativeXHR ? new NativeXHR() : null; this._isWasm = false;\n" +
                    "                    }\n" +
                    "                    open(method, url, async = true) {\n" +
                    "                        this._url = String(url || ''); this._async = async !== false;\n" +
                    "                        this._isWasm = isWasmUrl(this._url);\n" +
                    "                        if (this._isWasm) { this.readyState = 1; return; }\n" +
                    "                        if (this._native) this._native.open(method, url, async);\n" +
                    "                    }\n" +
                    "                    send(body) {\n" +
                    "                        if (!this._isWasm && this._native) return this._native.send(body);\n" +
                    "                        const finish = () => {\n" +
                    "                            this.status = 200; this.statusText = 'OK'; this.readyState = 4;\n" +
                    "                            if (this.responseType === 'arraybuffer') this.response = wasmBuffer.slice(0);\n" +
                    "                            else if (this.responseType === 'blob' && typeof Blob === 'function') this.response = new Blob([wasmBuffer], { type: 'application/wasm' });\n" +
                    "                            else { this.responseText = bufferToBinaryString(wasmBuffer); this.response = this.responseText; }\n" +
                    "                            if (typeof this.onreadystatechange === 'function') this.onreadystatechange();\n" +
                    "                            if (typeof this.onload === 'function') this.onload();\n" +
                    "                        };\n" +
                    "                        if (this._async) setTimeout(finish, 0); else finish();\n" +
                    "                    }\n" +
                    "                    setRequestHeader(h, v) { if (!this._isWasm && this._native) this._native.setRequestHeader(h, v); }\n" +
                    "                    getResponseHeader(name) { return String(name).toLowerCase() === 'content-type' ? 'application/wasm' : null; }\n" +
                    "                    getAllResponseHeaders() { return 'content-type: application/wasm\\r\\n'; }\n" +
                    "                }\n" +
                    "                self.XMLHttpRequest = WasmAwareXHR;\n" +
                    "            }\n" +
                    "            self.onmessage = function(e) {\n" +
                    "                if (e.data && e.data.__init_torch__) {\n" +
                    "                    try {\n" +
                    "                        installWasmXhr(e.data.wasm);\n" +
                    "                        self.Module = self.Module || {};\n" +
                    "                        self.Module.wasmBinary = e.data.wasm;\n" +
                    "                        self.Module.onAbort = function(what) { self.postMessage('__TORCH_ERROR__:' + (what || 'abort')); };\n" +
                    "                        (new Function(e.data.js))();\n" +
                    "                        torchHandler = self.onmessage;\n" +
                    "                        ready = true;\n" +
                    "                        while (queued.length && typeof torchHandler === 'function') {\n" +
                    "                            torchHandler.call(self, { data: queued.shift() });\n" +
                    "                        }\n" +
                    "                        self.postMessage('__TORCH_READY__');\n" +
                    "                    } catch (err) {\n" +
                    "                        self.postMessage('__TORCH_ERROR__:' + String(err));\n" +
                    "                    }\n" +
                    "                } else if (ready && typeof torchHandler === 'function') {\n" +
                    "                    torchHandler.call(self, e);\n" +
                    "                } else {\n" +
                    "                    queued.push(e.data);\n" +
                    "                }\n" +
                    "            };\n" +
                    "        `;\n" +
                    "        const blob = new Blob([workerScript], { type: 'text/javascript' });\n" +
                    "        const worker = new Worker(URL.createObjectURL(blob));\n" +
                    "        worker.onmessage = function(e) {\n" +
                    "            const data = e.data;\n" +
                    "            if (data === '__TORCH_READY__') {\n" +
                    "                if (window.TorchBridge) window.TorchBridge.onTorchReady();\n" +
                    "                worker.postMessage('setoption name UseDeclarativePositionCommand value true');\n" +
                    "                worker.postMessage('setoption name BlackElo value 3200');\n" +
                    "                worker.postMessage('setoption name WhiteElo value 3200');\n" +
                    "                worker.postMessage('setoption name HandleContinuations value true');\n" +
                    "                worker.postMessage('setoption name HandleContinuationsDepth value 4');\n" +
                    "                worker.postMessage('setoption name UserColor value white');\n" +
                    "                worker.postMessage('setoption name BotChatPrioritizePlayerMove value true');\n" +
                    "                worker.postMessage('setoption name AllowBoardEventsWithoutSpeech value true');\n" +
                    "                worker.postMessage('setoption name ServeCommandV2 value true');\n" +
                    "                worker.postMessage('setoption name SpeechV3 value true');\n" +
                    "                worker.postMessage('setoption name ClassificationV3 value true');\n" +
                    "                worker.postMessage('setoption name UCI_Chess960 value false');\n" +
                    "                worker.postMessage('setoption name UseRatingRanges value true');\n" +
                    "            } else if (typeof data === 'string' && data.startsWith('__TORCH_ERROR__:')) {\n" +
                    "                if (window.TorchBridge) window.TorchBridge.onTorchError(data.substring(16));\n" +
                    "            } else if (typeof data === 'string' && data.startsWith('json ')) {\n" +
                    "                if (window.TorchBridge) window.TorchBridge.onTorchResult(data.substring(5).trim());\n" +
                    "            }\n" +
                    "        };\n" +
                    "        window.sendTorchMove = function(movesStr, userColor, depth) {\n" +
                    "            if (!worker) return;\n" +
                    "            if (userColor) worker.postMessage('setoption name UserColor value ' + userColor);\n" +
                    "            const d = depth || 4;\n" +
                    "            worker.postMessage('setoption name HandleContinuationsDepth value ' + d);\n" +
                    "            worker.postMessage('position startpos moves ' + movesStr);\n" +
                    "            worker.postMessage('fetch analysis');\n" +
                    "        };\n" +
                    "        worker.postMessage({ __init_torch__: true, js: jsText, wasm: wasmBuffer }, [wasmBuffer]);\n" +
                    "    }).catch(err => {\n" +
                    "        if (window.TorchBridge) window.TorchBridge.onTorchError(String(err));\n" +
                    "    });\n" +
                    "})();\n" +
                    "</script></body></html>";

            webView.loadDataWithBaseURL("https://torch-engine.local/", html, "text/html", "UTF-8", null);

        } catch (Throwable t) {
            log("[INIT ERROR] Lỗi khởi tạo WebView: " + t.getMessage());
            Log.e(TAG, "Failed to initialize Torch WebView: " + t.getMessage(), t);
        }
    }

    public void analyze(List<String> moves, String userColor, TorchClassificationCallback callback) {
        analyze(moves, userColor, 4, callback);
    }

    public void analyze(List<String> moves, String userColor, int depth, TorchClassificationCallback callback) {
        if (!isReady || webView == null || moves == null || moves.isEmpty()) {
            log("[ANALYZE SKIP] isReady=" + isReady + ", webView=" + (webView != null) + ", moves=" + (moves != null ? moves.size() : 0));
            return;
        }
        this.activeCallback = callback;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < moves.size(); i++) {
            if (i > 0) sb.append(' ');
            sb.append(moves.get(i));
        }
        final String movesStr = sb.toString();
        final String color = (userColor != null) ? userColor : "white";
        final int targetDepth = Math.max(2, Math.min(10, depth > 0 ? depth : 4));
        log("[ANALYZE SEND] Moves=" + movesStr + ", Color=" + color + ", Depth=" + targetDepth);

        mainHandler.post(() -> {
            try {
                String js = "window.sendTorchMove('" + movesStr + "', '" + color + "', " + targetDepth + ");";
                webView.evaluateJavascript(js, null);
            } catch (Throwable t) {
                log("[EVAL JS ERROR] " + t.getMessage());
                Log.e(TAG, "evaluateJavascript failed: " + t.getMessage());
            }
        });
    }

    private class TorchJsBridge {
        @JavascriptInterface
        public void onTorchReady() {
            isReady = true;
            log("[BRIDGE] onTorchReady() -> Engine WASM đã SẴN SÀNG!");
            Log.i(TAG, "Torch WebAssembly engine is READY (100% authentic CEE)");
            mainHandler.post(() -> Toast.makeText(context, "[Torch] Engine WASM sẵn sàng (100% Real)", Toast.LENGTH_SHORT).show());
        }

        @JavascriptInterface
        public void onTorchError(String error) {
            log("[BRIDGE ERROR] onTorchError: " + error);
            Log.e(TAG, "Torch Engine runtime error: " + error);
        }

        @JavascriptInterface
        public void onTorchResult(String jsonStr) {
            log("[BRIDGE RESULT] Nhận json độ dài: " + (jsonStr != null ? jsonStr.length() : 0));
            try {
                JSONObject root = new JSONObject(jsonStr);
                JSONArray positions = root.optJSONArray("positions");
                if (positions == null || positions.length() == 0) {
                    log("[BRIDGE RESULT WARN] positions array rỗng");
                    return;
                }

                JSONObject lastPos = positions.getJSONObject(positions.length() - 1);
                String classificationName = lastPos.optString("classificationName", "");
                JSONObject playedMove = lastPos.optJSONObject("playedMove");
                String playedMoveLan = playedMove != null ? playedMove.optString("moveLan", "") : "";

                JSONObject bestMove = lastPos.optJSONObject("bestMove");
                String bestMoveLan = bestMove != null ? bestMove.optString("moveLan", "") : "";

                String speechText = "";
                if (playedMove != null) {
                    JSONArray speechArr = playedMove.optJSONArray("speech");
                    if (speechArr != null && speechArr.length() > 0) {
                        JSONObject spObj = speechArr.getJSONObject(0);
                        speechText = spObj.optString("sentence", spObj.optString("text", ""));
                    }
                }
                log("[BRIDGE PARSED] class=" + classificationName + ", played=" + playedMoveLan + ", best=" + bestMoveLan + ", speech=" + speechText);

                if (activeCallback != null) {
                    activeCallback.onClassification(classificationName, playedMoveLan, bestMoveLan, speechText, jsonStr);
                }

            } catch (Throwable t) {
                log("[BRIDGE PARSE ERROR] " + t.getMessage());
                Log.e(TAG, "Failed to parse Torch JSON: " + t.getMessage());
            }
        }
    }

    private void downloadTorchFilesAsync() {
        if (!isDownloading.compareAndSet(false, true)) return;

        new Thread(() -> {
            try {
                mainHandler.post(() -> Toast.makeText(context, "[Torch] Đang tải engine Torch (26MB) lần đầu...", Toast.LENGTH_LONG).show());

                File dir = getTorchDir();
                File wasmFile = new File(dir, "torch.wasm");
                File jsFile = new File(dir, "torch.js");

                if (!jsFile.exists() || jsFile.length() < 50_000) {
                    downloadToFile(TORCH_JS_URL, jsFile);
                }
                if (!wasmFile.exists() || wasmFile.length() < 10_000_000) {
                    downloadToFile(TORCH_WASM_URL, wasmFile);
                }

                isDownloading.set(false);
                mainHandler.post(() -> {
                    Toast.makeText(context, "[Torch] Tải hoàn tất! Đang khởi động...", Toast.LENGTH_SHORT).show();
                    initWebView();
                });

            } catch (Throwable t) {
                isDownloading.set(false);
                Log.e(TAG, "Download torch files failed: " + t.getMessage(), t);
                mainHandler.post(() -> Toast.makeText(context, "[Torch] Tải engine thất bại: " + t.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private static void downloadToFile(String urlStr, File dest) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(20_000);
        conn.setReadTimeout(60_000);
        conn.setInstanceFollowRedirects(true);
        int code = conn.getResponseCode();
        if (code >= 300 && code < 400) {
            String redirectUrl = conn.getHeaderField("Location");
            if (redirectUrl != null) {
                conn.disconnect();
                url = new URL(redirectUrl);
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(20_000);
                conn.setReadTimeout(60_000);
            }
        }
        try (InputStream in = conn.getInputStream(); FileOutputStream out = new FileOutputStream(dest)) {
            byte[] buf = new byte[65536];
            int read;
            while ((read = in.read(buf)) != -1) {
                out.write(buf, 0, read);
            }
        } finally {
            conn.disconnect();
        }
    }

    private static void copyFile(File src, File dst) {
        try (FileInputStream in = new FileInputStream(src); FileOutputStream out = new FileOutputStream(dst)) {
            copyStream(in, out);
        } catch (Throwable ignored) {}
    }

    private static void copyStream(InputStream in, File dst) throws Exception {
        try (FileOutputStream out = new FileOutputStream(dst)) {
            copyStream(in, out);
        } finally {
            in.close();
        }
    }

    private static void copyStream(InputStream in, FileOutputStream out) throws Exception {
        byte[] buf = new byte[65536];
        int read;
        while ((read = in.read(buf)) != -1) {
            out.write(buf, 0, read);
        }
    }
}
