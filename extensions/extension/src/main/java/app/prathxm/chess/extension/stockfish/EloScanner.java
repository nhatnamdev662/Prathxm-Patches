/*
 * Copyright 2026 NNVC
 * https://github.com/nhatnamdev662/Prathxm-Patches
 */

package app.prathxm.chess.extension.stockfish;

import android.app.Activity;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * EloScanner – Quét và nhận diện chính xác chỉ số Elo/Rating của hai kỳ thủ (White & Black)
 * chạy 100% không làm nghẽn Main UI Thread (Triệt tiêu hiện tượng lag/khựng khi đi cờ).
 */
public class EloScanner {

    private static final String TAG = "EloScanner";

    public static class EloPair {
        public final int whiteElo;
        public final int blackElo;
        public final String source;
        public final boolean isAutoDetected;

        public EloPair(int whiteElo, int blackElo, String source, boolean isAutoDetected) {
            this.whiteElo = whiteElo;
            this.blackElo = blackElo;
            this.source = source;
            this.isAutoDetected = isAutoDetected;
        }

        @Override
        public String toString() {
            return "WhiteElo=" + whiteElo + ", BlackElo=" + blackElo + " (nguồn=" + source + ")";
        }
    }

    private static class ViewSnapshot {
        final String text;
        final int y;
        ViewSnapshot(String text, int y) {
            this.text = text;
            this.y = y;
        }
    }

    private static volatile EloPair lastDetectedPair = null;
    private static volatile long lastScanTimeMs = 0;
    private static final ExecutorService SCAN_EXECUTOR = Executors.newSingleThreadExecutor();
    private static final AtomicBoolean IS_SCANNING = new AtomicBoolean(false);

    public static void reset() {
        lastDetectedPair = null;
        lastScanTimeMs = 0;
    }

    /**
     * Quét và tự động đồng bộ hóa Elo vào Torch WebAssembly Engine.
     * Chạy hoàn toàn trên Background Worker, tuyệt đối không chặn Main Thread.
     */
    public static void scanAndApply(Activity activity, Object stateImplObject) {
        if (activity == null) return;

        long now = System.currentTimeMillis();
        // Tránh quét dồn dập: giãn cách tối thiểu 3 giây
        if (now - lastScanTimeMs < 3000) {
            return;
        }
        // Nếu đã nhận diện thành công cho ván đấu hiện tại, không quét lại để tiết kiệm CPU
        if (lastDetectedPair != null && lastDetectedPair.isAutoDetected) {
            return;
        }

        if (!IS_SCANNING.compareAndSet(false, true)) {
            return;
        }
        lastScanTimeMs = now;

        SCAN_EXECUTOR.submit(() -> {
            try {
                TorchEngine.log("[ELO SCAN] Bắt đầu quét Elo người chơi...");
                EloPair pair = detectEloPair(activity, stateImplObject);
                if (pair != null && pair.isAutoDetected) {
                    if (lastDetectedPair == null || lastDetectedPair.whiteElo != pair.whiteElo || lastDetectedPair.blackElo != pair.blackElo) {
                        lastDetectedPair = pair;
                        TorchEngine.log("[ELO SCANNER] ĐÃ PHÁT HIỆN: " + pair.toString());
                        TorchEngine.getInstance(activity.getApplicationContext()).updateRatings(pair.whiteElo, pair.blackElo);
                    }
                } else {
                    int def = StockfishSettings.getElo(activity);
                    TorchEngine.log("[ELO DEBUG] Không phát hiện tự động, dùng cài đặt mặc định: " + def);
                }
            } catch (Throwable t) {
                Log.e(TAG, "Lỗi khi quét Elo: " + t.getMessage(), t);
                TorchEngine.log("[ELO ERROR] " + t.getMessage());
            } finally {
                IS_SCANNING.set(false);
            }
        });
    }

    public static EloPair getLastDetectedPair() {
        return lastDetectedPair;
    }

    /**
     * Nhận diện cặp Elo theo thứ tự ưu tiên:
     * 1. Deep Reflection từ Model/State/Activity
     * 2. Quét View Hierarchy theo toạ độ bàn cờ
     * 3. Fallback: Cài đặt người dùng
     */
    public static EloPair detectEloPair(Activity activity, Object stateImplObject) {
        if (activity == null) return null;

        // ── 1. Thử qua Deep Reflection ──
        try {
            EloPair reflectPair = tryReflectionElo(activity, stateImplObject);
            if (reflectPair != null) {
                TorchEngine.log("[ELO DEBUG] Reflection thành công: " + reflectPair);
                return reflectPair;
            }
        } catch (Throwable t) {
            Log.e(TAG, "Lỗi tryReflectionElo: " + t.getMessage());
        }

        // ── 2. Quét View Hierarchy theo toạ độ bàn cờ ──
        try {
            EloPair viewPair = tryScanViewHierarchy(activity, stateImplObject);
            if (viewPair != null) {
                TorchEngine.log("[ELO DEBUG] Quét View thành công: " + viewPair);
                return viewPair;
            }
        } catch (Throwable t) {
            Log.e(TAG, "Lỗi tryScanViewHierarchy: " + t.getMessage());
        }

        // ── 3. Fallback an toàn: Dùng Elo cài đặt từ người dùng ──
        int defaultElo = StockfishSettings.getElo(activity);
        return new EloPair(defaultElo, defaultElo, "cài đặt mặc định", false);
    }

    private static EloPair tryReflectionElo(Activity activity, Object stateImpl) {
        if (stateImpl != null) {
            EloPair pair = inspectObjectForElo(stateImpl, 0);
            if (pair != null) return pair;
        }

        if (activity != null) {
            try {
                for (Field f : activity.getClass().getDeclaredFields()) {
                    try {
                        f.setAccessible(true);
                        Object val = f.get(activity);
                        if (val != null && !isFrameworkClass(val.getClass().getName())) {
                            EloPair pair = inspectObjectForElo(val, 0);
                            if (pair != null) return pair;
                        }
                    } catch (Throwable ignored) {}
                }
            } catch (Throwable ignored) {}
        }

        return null;
    }

    private static boolean isFrameworkClass(String name) {
        return name.startsWith("android.") || name.startsWith("java.") || name.startsWith("androidx.") || name.startsWith("kotlin.");
    }

    private static EloPair inspectObjectForElo(Object obj, int depth) {
        if (obj == null || depth > 2) return null;
        Class<?> clazz = obj.getClass();
        if (isFrameworkClass(clazz.getName())) return null;

        // A. Direct getters: getWhiteRating / getBlackRating
        try {
            Method wM = findMethod(clazz, "getWhiteRating", "getWhiteElo");
            Method bM = findMethod(clazz, "getBlackRating", "getBlackElo");
            if (wM != null && bM != null) {
                Object w = wM.invoke(obj);
                Object b = bM.invoke(obj);
                if (w instanceof Number && b instanceof Number) {
                    int wVal = ((Number) w).intValue();
                    int bVal = ((Number) b).intValue();
                    if (isValidElo(wVal) && isValidElo(bVal)) {
                        return new EloPair(wVal, bVal, "reflection_" + clazz.getSimpleName(), true);
                    }
                }
            }
        } catch (Throwable ignored) {}

        // B. UserInfo / LiveUserInfo
        try {
            Integer whiteElo = null;
            Integer blackElo = null;
            for (Method m : clazz.getMethods()) {
                if (m.getParameterCount() == 0) {
                    String retName = m.getReturnType().getName();
                    if (retName.endsWith("UserInfo") || retName.endsWith("LiveUserInfo") || retName.endsWith("DailyUserInfo")) {
                        Object uInfo = m.invoke(obj);
                        if (uInfo != null) {
                            int r = extractRatingFromUserInfo(uInfo);
                            Boolean isWhite = extractColorFromUserInfo(uInfo);
                            if (isValidElo(r) && isWhite != null) {
                                if (isWhite) whiteElo = r;
                                else blackElo = r;
                            }
                        }
                    }
                }
            }
            if (whiteElo != null && blackElo != null) {
                return new EloPair(whiteElo, blackElo, "reflection_userinfo_" + clazz.getSimpleName(), true);
            }
        } catch (Throwable ignored) {}

        // C. Recursive fields search
        if (depth < 2) {
            try {
                for (Field f : clazz.getDeclaredFields()) {
                    try {
                        f.setAccessible(true);
                        Object child = f.get(obj);
                        if (child != null && !isFrameworkClass(child.getClass().getName())) {
                            EloPair childPair = inspectObjectForElo(child, depth + 1);
                            if (childPair != null) return childPair;
                        }
                    } catch (Throwable ignored) {}
                }
            } catch (Throwable ignored) {}
        }

        return null;
    }

    private static Method findMethod(Class<?> clazz, String... names) {
        for (String n : names) {
            try {
                Method m = clazz.getMethod(n);
                if (m.getParameterCount() == 0) return m;
            } catch (Throwable ignored) {}
        }
        return null;
    }

    private static int extractRatingFromUserInfo(Object userInfo) {
        if (userInfo == null) return -1;
        try {
            Method m = userInfo.getClass().getMethod("getRating");
            Object r = m.invoke(userInfo);
            if (r instanceof Number) return ((Number) r).intValue();
        } catch (Throwable ignored) {}
        return -1;
    }

    private static Boolean extractColorFromUserInfo(Object userInfo) {
        if (userInfo == null) return null;
        try {
            Method m = userInfo.getClass().getMethod("getColor");
            Object c = m.invoke(userInfo);
            if (c != null) {
                String s = c.toString().toUpperCase(java.util.Locale.US);
                if (s.contains("WHITE")) return Boolean.TRUE;
                if (s.contains("BLACK")) return Boolean.FALSE;
            }
        } catch (Throwable ignored) {}
        return null;
    }

    private static EloPair tryScanViewHierarchy(Activity activity, Object stateImpl) {
        final List<ViewSnapshot> snapshots = new ArrayList<>();
        final int[] boardData = new int[3]; // [boardY, boardH, screenH]
        final CountDownLatch latch = new CountDownLatch(1);

        // Snapshot cực nhanh trên UI Thread (< 1ms, không reflection, không regex)
        activity.runOnUiThread(() -> {
            try {
                if (activity.getWindow() != null && activity.getWindow().getDecorView() != null) {
                    View decorView = activity.getWindow().getDecorView();
                    boardData[2] = decorView.getHeight();

                    View boardView = OverlayManager.findChessBoardView(decorView);
                    if (boardView != null && boardView.getWidth() > 0 && boardView.getHeight() > 0) {
                        int[] loc = new int[2];
                        boardView.getLocationOnScreen(loc);
                        boardData[0] = loc[1];
                        boardData[1] = boardView.getHeight();
                    }
                    collectSnapshots(decorView, snapshots);
                }
            } catch (Throwable ignored) {
            } finally {
                latch.countDown();
            }
        });

        try {
            latch.await(300, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignored) {}

        if (snapshots.isEmpty()) return null;

        // Xử lý và tính toán vị trí trên Background Thread
        int boardY = boardData[0];
        int boardH = boardData[1];
        int screenH = boardData[2];

        int boardCenterY;
        if (boardH > 0) {
            boardCenterY = boardY + (boardH / 2);
        } else {
            boardCenterY = screenH > 0 ? (screenH / 2) : 1000;
        }

        Integer topZoneElo = null;
        Integer bottomZoneElo = null;
        int closestTopDist = Integer.MAX_VALUE;
        int closestBottomDist = Integer.MAX_VALUE;

        for (ViewSnapshot snap : snapshots) {
            Integer parsed = parseStrictElo(snap.text);
            if (parsed == null) continue;

            int tvY = snap.y;
            if (tvY < boardCenterY) {
                int dist = boardCenterY - tvY;
                if (dist < closestTopDist) {
                    closestTopDist = dist;
                    topZoneElo = parsed;
                }
            } else {
                int dist = tvY - boardCenterY;
                if (dist < closestBottomDist) {
                    closestBottomDist = dist;
                    bottomZoneElo = parsed;
                }
            }
        }

        TorchEngine.log("[ELO DEBUG] Views count=" + snapshots.size() + ", TopZoneElo=" + topZoneElo + ", BottomZoneElo=" + bottomZoneElo);

        if (topZoneElo != null || bottomZoneElo != null) {
            boolean flipped = OverlayManager.isBoardFlipped(stateImpl);
            int def = StockfishSettings.getElo(activity);
            int top = (topZoneElo != null) ? topZoneElo : def;
            int bot = (bottomZoneElo != null) ? bottomZoneElo : def;

            int finalWhite;
            int finalBlack;
            if (!flipped) {
                finalWhite = bot;
                finalBlack = top;
            } else {
                finalWhite = top;
                finalBlack = bot;
            }

            return new EloPair(finalWhite, finalBlack, "view_hierarchy_zones", true);
        }

        return null;
    }

    private static void collectSnapshots(View root, List<ViewSnapshot> out) {
        if (root == null || root.getVisibility() != View.VISIBLE) return;

        // 1. Quét sâu qua AccessibilityNodeInfo (Đặc trị 100% Jetpack Compose / ComposeView)
        try {
            AccessibilityNodeInfo rootNode = root.createAccessibilityNodeInfo();
            if (rootNode != null) {
                collectAccessibilitySnapshots(rootNode, out);
                try {
                    rootNode.recycle();
                } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}

        // 2. Dự phòng: Quét qua cây View/TextView Android truyền thống nếu Accessibility chưa bắt hết
        collectViewSnapshots(root, out);
    }

    private static void collectAccessibilitySnapshots(AccessibilityNodeInfo node, List<ViewSnapshot> out) {
        if (node == null || !node.isVisibleToUser()) return;

        CharSequence text = node.getText();
        if (text != null && text.length() > 0) {
            Rect bounds = new Rect();
            node.getBoundsInScreen(bounds);
            out.add(new ViewSnapshot(text.toString(), bounds.top));
        } else {
            CharSequence desc = node.getContentDescription();
            if (desc != null && desc.length() > 0) {
                Rect bounds = new Rect();
                node.getBoundsInScreen(bounds);
                out.add(new ViewSnapshot(desc.toString(), bounds.top));
            }
        }

        int count = node.getChildCount();
        for (int i = 0; i < count; i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                collectAccessibilitySnapshots(child, out);
                try {
                    child.recycle();
                } catch (Throwable ignored) {}
            }
        }
    }

    private static void collectViewSnapshots(View root, List<ViewSnapshot> out) {
        if (root == null || root.getVisibility() != View.VISIBLE) return;
        if (root instanceof TextView) {
            CharSequence cs = ((TextView) root).getText();
            if (cs != null && cs.length() > 0) {
                int[] loc = new int[2];
                root.getLocationOnScreen(loc);
                out.add(new ViewSnapshot(cs.toString(), loc[1]));
            }
        } else if (root instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) root;
            int count = g.getChildCount();
            for (int i = 0; i < count; i++) {
                collectViewSnapshots(g.getChildAt(i), out);
            }
        }
    }

    /**
     * Bộ lọc nghiêm ngặt (Strict Filter):
     * Nhận diện chính xác số Elo từ 100 đến 3800.
     * Hỗ trợ text nhiều dòng, tên kèm rating trong ngoặc, từ khoá Elo/Rapid/Blitz.
     * Loại bỏ triệt để: đồng hồ đếm ngược (10:00, 3:15), phép chia thời gian, chênh lệch quân.
     */
    public static Integer parseStrictElo(String rawText) {
        if (rawText == null) return null;
        String s = rawText.trim();
        if (s.isEmpty()) return null;

        String[] lines = s.split("\\r?\\n");
        for (String line : lines) {
            Integer res = parseStrictEloLine(line.trim());
            if (res != null) return res;
        }
        return null;
    }

    private static Integer parseStrictEloLine(String s) {
        if (s == null || s.isEmpty()) return null;

        // 1. Loại bỏ định dạng đồng hồ đếm ngược (e.g. 10:00, 3:15, 0:45)
        if (Pattern.compile("\\b\\d{1,2}:\\d{2}\\b").matcher(s).find()) {
            return null;
        }

        // 2. Loại bỏ đơn vị thời gian (min, sec, phút, giây) và phép chia thời lượng (3|2, 5+3)
        String lower = s.toLowerCase(java.util.Locale.US);
        if (lower.contains("min") || lower.contains("sec") || lower.contains("phút") || lower.contains("giây")) {
            return null;
        }
        if (Pattern.compile("\\b\\d+\\s*[|+x]\\s*\\d+\\b").matcher(s).find()) {
            return null;
        }

        // 3. Loại bỏ điểm chênh lệch quân cờ (+1, -3)
        if (Pattern.compile("^[+-]\\d+$").matcher(s).matches()) {
            return null;
        }

        // 4. Ưu tiên số trong ngoặc đơn: "Magnus (2850)", "Bot Martin (250)", "(1500)"
        Matcher mBracket = Pattern.compile(".*?\\((\\d{2,4})\\??\\).*?").matcher(s);
        if (mBracket.matches()) {
            try {
                int val = Integer.parseInt(mBracket.group(1));
                if (isValidElo(val)) return val;
            } catch (Throwable ignored) {}
        }

        // 5. Dạng kèm từ khoá: "Rating: 1500", "Elo: 1500", "1500 Rapid", "Blitz • 1820"
        Matcher mWord = Pattern.compile(".*?(?:rating|elo|rapid|blitz|bullet|daily)\\s*[:•\\-\\s]\\s*\\(?(\\d{2,4})\\??\\)?.*?", Pattern.CASE_INSENSITIVE).matcher(s);
        if (mWord.matches()) {
            try {
                int val = Integer.parseInt(mWord.group(1));
                if (isValidElo(val)) return val;
            } catch (Throwable ignored) {}
        }

        Matcher mWordRev = Pattern.compile(".*?\\(?(\\d{2,4})\\??\\)?\\s*[:•\\-\\s]\\s*(?:rating|elo|rapid|blitz|bullet|daily).*?", Pattern.CASE_INSENSITIVE).matcher(s);
        if (mWordRev.matches()) {
            try {
                int val = Integer.parseInt(mWordRev.group(1));
                if (isValidElo(val)) return val;
            } catch (Throwable ignored) {}
        }

        // 6. Số đứng độc lập hoặc trong ngoặc: "1500", "(1500)"
        Matcher mDirect = Pattern.compile("^\\(?(\\d{2,4})\\??\\)?$").matcher(s);
        if (mDirect.matches()) {
            try {
                int val = Integer.parseInt(mDirect.group(1));
                if (isValidElo(val)) return val;
            } catch (Throwable ignored) {}
        }

        return null;
    }

    private static boolean isValidElo(int val) {
        return val >= 100 && val <= 3800;
    }
}
