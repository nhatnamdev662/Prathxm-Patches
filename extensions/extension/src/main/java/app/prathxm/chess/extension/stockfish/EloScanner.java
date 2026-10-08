/*
 * Copyright 2026 NNVC
 * https://github.com/nhatnamdev662/Prathxm-Patches
 */

package app.prathxm.chess.extension.stockfish;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * EloScanner – Quét và nhận diện chính xác chỉ số Elo/Rating của hai kỳ thủ (White & Black)
 * tương tự như cơ chế của NNVC Extension trên PC.
 * 
 * Áp dụng bộ lọc nghiêm ngặt (Strict Filter) để loại trừ 100% các giá trị nhầm lẫn:
 * đồng hồ bấm giờ, thời lượng ván đấu, điểm số chênh lệch quân, số thứ tự nước đi.
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

    private static volatile EloPair lastDetectedPair = null;
    private static volatile long lastScanTimeMs = 0;

    /**
     * Quét và tự động đồng bộ hóa Elo vào Torch WebAssembly Engine.
     */
    public static void scanAndApply(Activity activity, Object stateImplObject) {
        if (activity == null) return;

        // Tránh quét liên tục làm tốn CPU, chỉ quét cách nhau tối thiểu 2 giây
        long now = System.currentTimeMillis();
        if (now - lastScanTimeMs < 2000 && lastDetectedPair != null && lastDetectedPair.isAutoDetected) {
            return;
        }
        lastScanTimeMs = now;

        activity.runOnUiThread(() -> {
            try {
                EloPair pair = detectEloPair(activity, stateImplObject);
                if (pair != null && pair.isAutoDetected) {
                    if (lastDetectedPair == null || lastDetectedPair.whiteElo != pair.whiteElo || lastDetectedPair.blackElo != pair.blackElo) {
                        lastDetectedPair = pair;
                        TorchEngine.log("[ELO SCANNER] ĐÃ PHÁT HIỆN: " + pair.toString());
                        TorchEngine.getInstance(activity.getApplicationContext()).updateRatings(pair.whiteElo, pair.blackElo);
                    }
                }
            } catch (Throwable t) {
                Log.e(TAG, "Lỗi khi quét Elo: " + t.getMessage(), t);
            }
        });
    }

    public static EloPair getLastDetectedPair() {
        return lastDetectedPair;
    }

    /**
     * Nhận diện cặp Elo với độ chính xác cao:
     * 1. Deep Reflection từ Model/State/Activity (RcnGameState, UserInfo, LiveUserInfo)
     * 2. Quét View Hierarchy theo toạ độ bàn cờ (Top/Bottom Player Zones)
     * 3. Fallback: Cài đặt người dùng
     */
    public static EloPair detectEloPair(Activity activity, Object stateImplObject) {
        if (activity == null) return null;

        // ── 1. Thử qua Deep Reflection từ State / ViewModel / Activity ──
        EloPair reflectPair = tryReflectionElo(activity, stateImplObject);
        if (reflectPair != null) {
            return reflectPair;
        }

        // ── 2. Quét View Hierarchy theo toạ độ bàn cờ (Top/Bottom Player Zones) ──
        EloPair viewPair = tryScanViewHierarchy(activity, stateImplObject);
        if (viewPair != null) {
            return viewPair;
        }

        // ── 3. Fallback an toàn: Dùng Elo cài đặt từ người dùng ──
        int defaultElo = StockfishSettings.getElo(activity);
        return new EloPair(defaultElo, defaultElo, "cài đặt mặc định", false);
    }

    private static EloPair tryReflectionElo(Activity activity, Object stateImpl) {
        // 1. Thử từ stateImplObject
        if (stateImpl != null) {
            EloPair pair = inspectObjectForElo(stateImpl, 0);
            if (pair != null) return pair;
        }

        // 2. Thử từ Activity fields (ViewModel, Presenter, Game Controller)
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

        // A. Kiểm tra direct getters: getWhiteRating / getBlackRating
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

        // B. Kiểm tra nếu có UserInfo / LiveUserInfo
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

        // C. Kiểm tra các fields con đến độ sâu 2
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
        try {
            if (activity.getWindow() == null || activity.getWindow().getDecorView() == null) return null;
            View decorView = activity.getWindow().getDecorView();

            View boardView = OverlayManager.findChessBoardView(decorView);
            if (boardView == null || boardView.getWidth() <= 0 || boardView.getHeight() <= 0) return null;

            int[] boardLoc = new int[2];
            boardView.getLocationOnScreen(boardLoc);
            int boardY = boardLoc[1];
            int boardH = boardView.getHeight();

            float density = activity.getResources().getDisplayMetrics().density;
            int maxZoneDist = (int) (280 * density); // Giới hạn vùng player chỉ trong 280dp quanh bàn cờ

            List<TextView> allTextViews = new ArrayList<>();
            collectTextViews(decorView, allTextViews);

            Integer topZoneElo = null;
            Integer bottomZoneElo = null;
            int closestTopDist = Integer.MAX_VALUE;
            int closestBottomDist = Integer.MAX_VALUE;

            for (TextView tv : allTextViews) {
                if (tv.getVisibility() != View.VISIBLE) continue;
                CharSequence cs = tv.getText();
                if (cs == null) continue;

                Integer parsed = parseStrictElo(cs.toString());
                if (parsed == null) continue;

                int[] tvLoc = new int[2];
                tv.getLocationOnScreen(tvLoc);
                int tvY = tvLoc[1];

                // Kiểm tra Top Zone (phía trên bàn cờ)
                if (tvY < boardY && tvY >= boardY - maxZoneDist) {
                    int dist = boardY - tvY;
                    if (dist < closestTopDist) {
                        closestTopDist = dist;
                        topZoneElo = parsed;
                    }
                }
                // Kiểm tra Bottom Zone (phía dưới bàn cờ)
                else if (tvY >= boardY + boardH && tvY <= boardY + boardH + maxZoneDist) {
                    int dist = tvY - (boardY + boardH);
                    if (dist < closestBottomDist) {
                        closestBottomDist = dist;
                        bottomZoneElo = parsed;
                    }
                }
            }

            if (topZoneElo != null || bottomZoneElo != null) {
                boolean flipped = OverlayManager.isBoardFlipped(stateImpl);
                int finalWhite;
                int finalBlack;

                int def = StockfishSettings.getElo(activity);
                int top = (topZoneElo != null) ? topZoneElo : def;
                int bot = (bottomZoneElo != null) ? bottomZoneElo : def;

                if (!flipped) {
                    // Không lật bàn: Người chơi (Bottom) là Trắng, Đối thủ (Top) là Đen
                    finalWhite = bot;
                    finalBlack = top;
                } else {
                    // Lật bàn: Người chơi (Bottom) là Đen, Đối thủ (Top) là Trắng
                    finalWhite = top;
                    finalBlack = bot;
                }

                return new EloPair(finalWhite, finalBlack, "view_hierarchy_zones", true);
            }

        } catch (Throwable t) {
            Log.e(TAG, "Lỗi tryScanViewHierarchy: " + t.getMessage());
        }
        return null;
    }

    /**
     * Bộ lọc nghiêm ngặt (Strict Filter):
     * Chỉ chấp nhận chuỗi là số Elo chuẩn (100 - 3800).
     * Hỗ trợ:
     * - Số trong ngoặc đơn dạng "(2850)" hoặc "Magnus (2850)"
     * - Dạng tiền tố / hậu tố "1500 Rapid", "Blitz • 1820", "Rating: 1500"
     * - Số đứng một mình "1500", "2070"
     * Loại bỏ triệt để:
     * - Đồng hồ thời gian ("10:00", "03:15", "0:45")
     * - Thời lượng ván ("10 min", "3|2", "5+3")
     * - Chênh lệch quân ("+1", "+3", "-2")
     */
    public static Integer parseStrictElo(String rawText) {
        if (rawText == null) return null;
        String s = rawText.trim();
        if (s.isEmpty()) return null;

        // 1. Loại bỏ các ký tự đồng hồ và phép chia thời gian
        if (s.contains(":") || s.contains("|") || s.contains("/") || s.contains("\\")) {
            return null;
        }

        // 2. Loại bỏ các chuỗi chứa đơn vị thời gian
        String lower = s.toLowerCase(java.util.Locale.US);
        if (lower.contains("min") || lower.contains("sec") || lower.contains("phút") || lower.contains("giây") || lower.contains("ms")) {
            return null;
        }

        // 3. Loại bỏ ký hiệu chênh lệch điểm quân cờ
        if (s.startsWith("+") || s.startsWith("-")) {
            return null;
        }

        // 4. Ưu tiên số trong ngoặc đơn dạng "(1500)" hoặc "Tên (1500)"
        Matcher mBracket = Pattern.compile(".*?\\((\\d{2,4})\\??\\).*?").matcher(s);
        if (mBracket.matches()) {
            try {
                int val = Integer.parseInt(mBracket.group(1));
                if (isValidElo(val)) return val;
            } catch (Throwable ignored) {}
        }

        // 5. Khớp trực tiếp dạng "1500" hoặc "1500?"
        Matcher mDirect = Pattern.compile("^\\(?(\\d{2,4})\\??\\)?$").matcher(s);
        if (mDirect.matches()) {
            try {
                int val = Integer.parseInt(mDirect.group(1));
                if (isValidElo(val)) return val;
            } catch (Throwable ignored) {}
        }

        // 6. Khớp dạng có chữ: "Rating: 1500", "Elo: 1500", "1500 Rapid", "Blitz • 1500"
        Matcher mWord1 = Pattern.compile(".*?(?:rating|elo|rapid|blitz|bullet|daily)\\s*[:•\\s]\\s*\\(?(\\d{2,4})\\??\\)?.*?", Pattern.CASE_INSENSITIVE).matcher(s);
        if (mWord1.matches()) {
            try {
                int val = Integer.parseInt(mWord1.group(1));
                if (isValidElo(val)) return val;
            } catch (Throwable ignored) {}
        }

        Matcher mWord2 = Pattern.compile(".*?\\(?(\\d{2,4})\\??\\)?\\s*[:•\\s]\\s*(?:rating|elo|rapid|blitz|bullet|daily).*?", Pattern.CASE_INSENSITIVE).matcher(s);
        if (mWord2.matches()) {
            try {
                int val = Integer.parseInt(mWord2.group(1));
                if (isValidElo(val)) return val;
            } catch (Throwable ignored) {}
        }

        return null;
    }

    private static boolean isValidElo(int val) {
        return val >= 100 && val <= 3800;
    }

    private static void collectTextViews(View root, List<TextView> out) {
        if (root == null) return;
        if (root instanceof TextView) {
            out.add((TextView) root);
        }
        if (root instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) root;
            for (int i = 0; i < g.getChildCount(); i++) {
                collectTextViews(g.getChildAt(i), out);
            }
        }
    }
}
