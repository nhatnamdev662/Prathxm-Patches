package app.prathxm.chess.extension.stockfish;

import android.content.Context;

public class I18n {

    public static String get(Context context, String key) {
        boolean isVi = "vi".equalsIgnoreCase(StockfishSettings.getLanguage(context));
        switch (key) {
            case "title":
                return isVi ? "Cài Đặt Engine" : "Engine Settings";
            case "engine":
                return isVi ? "Engine" : "Engine";
            case "enable_stockfish":
                return isVi ? "Bật Stockfish" : "Enable Stockfish";
            case "panic_hint":
                return isVi ? "Chạm đúp vào thanh trên để bật/tắt ngay lập tức (chế độ ẩn)." : "Double-tap the top bar to switch it off or on instantly (panic mode).";
            case "depth":
                return isVi ? "Độ sâu phân tích" : "Analysis depth";
            case "depth_hint":
                return isVi ? "Càng cao càng mạnh nhưng chậm hơn. Mức 18–22 phù hợp hầu hết điện thoại." : "Higher is stronger but slower. 18–22 suits most phones. Arrows update while the engine thinks.";
            case "on_board":
                return isVi ? "Hiển thị trên bàn cờ" : "On the board";
            case "best_move_arrows":
                return isVi ? "Mũi tên nước đi tốt nhất" : "Best-move arrows";
            case "eval_bar":
                return isVi ? "Thanh đánh giá thế cờ (Eval bar)" : "Evaluation bar";
            case "rate_moves":
                return isVi ? "Đánh giá từng nước đi (Best, Blunder…)" : "Rate each move (Best, Blunder…)";
            case "show_advanced":
                return isVi ? "Hiện cài đặt nâng cao ▾" : "Show advanced settings ▾";
            case "hide_advanced":
                return isVi ? "Ẩn cài đặt nâng cao ▴" : "Hide advanced settings ▴";
            case "arrows_overlays":
                return isVi ? "Mũi tên & Lớp phủ" : "Arrows & overlays";
            case "num_arrows":
                return isVi ? "Số lượng mũi tên" : "Number of arrows";
            case "arrows_hint":
                return isVi ? "Màu xanh lá là nước tối ưu, kế đến là xanh dương, cam và tím." : "Green is the best move, then blue, orange and purple. More arrows make each search a little slower.";
            case "my_turn_only":
                return isVi ? "Chỉ hiện mũi tên ở lượt của tôi" : "Arrows only on my turn";
            case "threat_arrow":
                return isVi ? "Mũi tên hiểm hoạ (đối thủ đáp trả)" : "Threat arrow (opponent's best reply)";
            case "wdl_bar":
                return isVi ? "Thanh Thắng / Hoà / Thua (WDL)" : "Win / Draw / Loss bar";
            case "depth_score_above":
                return isVi ? "Hiện độ sâu & điểm số phía trên bàn cờ" : "Depth & score above the board";
            case "forced_mates":
                return isVi ? "Thông báo nước chiếu hết bắt buộc" : "Announce forced mates";
            case "vibrate_blunder":
                return isVi ? "Rung khi mắc sai lầm & Blunder" : "Vibrate on mistakes & blunders";
            case "vibrate_hint":
                return isVi ? "Cần bật \"Đánh giá từng nước đi\"." : "Needs \"Rate each move\" to be on.";
            case "performance":
                return isVi ? "Hiệu năng" : "Performance";
            case "cpu_threads":
                return isVi ? "Số luồng CPU" : "CPU threads";
            case "cpu_hint":
                return isVi ? "Tất cả các nhân cho tốc độ phân tích nhanh nhất. Giảm xuống nếu máy bị nóng." : "All cores gives the fastest analysis. Lower it if your phone gets hot.";
            case "game_review":
                return isVi ? "Game Review" : "Game Review";
            case "extra_review_depth":
                return isVi ? "Độ sâu review bổ sung" : "Extra review depth";
            case "review_hint":
                return isVi ? "Cộng thêm vào mức thiết lập Game Review của Chess.com. Mỗi +1 sẽ chính xác hơn nhưng mất thời gian hơn." : "Added on top of the Fast / Standard / Deep / Maximum preset you choose in Game Review. Each +1 is more accurate and takes longer.";
            case "engine_strength":
                return isVi ? "Sức mạnh Engine" : "Engine strength";
            case "limit_strength":
                return isVi ? "Giới hạn sức mạnh theo Elo" : "Limit strength to an Elo";
            case "engine_elo":
                return isVi ? "Elo Engine" : "Engine Elo";
            case "elo_hint":
                return isVi ? "Chỉ áp dụng cho mũi tên trực tiếp. Game Review luôn phân tích với toàn bộ sức mạnh." : "Only changes the live arrows. Game Review always runs at full strength.";
            case "reset_defaults":
                return isVi ? "Đặt lại cài đặt engine về mặc định" : "Reset engine settings to defaults";
            case "reset_title":
                return isVi ? "Đặt lại cài đặt engine?" : "Reset engine settings?";
            case "reset_message":
                return isVi ? "Độ sâu, mũi tên, lớp phủ, luồng CPU, review và Elo sẽ trở về mặc định." : "Depth, arrows, overlays, threads, review depth and strength go back to their defaults.";
            case "reset_toast":
                return isVi ? "Đã đặt lại cài đặt engine" : "Engine settings reset";
            case "cancel":
                return isVi ? "Huỷ" : "Cancel";
            case "reset":
                return isVi ? "Đặt lại" : "Reset";
            case "save":
                return isVi ? "Lưu" : "Save";
            case "settings_saved":
                return isVi ? "Đã lưu cài đặt" : "Settings saved";
            case "engine_name":
                return isVi ? "Engine: Stockfish 19 NNUE · ngoại tuyến" : "Engine: Stockfish 19 NNUE · offline";
            case "dev_name":
                return "Nhat Nam";
            case "contact_telegram":
                return isVi ? "Liên hệ Telegram: @nncutett" : "Telegram: @nncutett";
            case "open_telegram":
                return isVi ? "Mở Telegram @nncutett" : "Open Telegram @nncutett";
            default:
                return key;
        }
    }
}
