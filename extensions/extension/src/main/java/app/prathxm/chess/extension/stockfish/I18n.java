package app.prathxm.chess.extension.stockfish;

import android.content.Context;

public class I18n {

    public static String get(Context context, String key) {
        boolean isVi = "vi".equalsIgnoreCase(StockfishSettings.getLanguage(context));
        switch (key) {
            case "title":
                return isVi ? "CÀI ĐẶT NNVC" : "NNVC SETTINGS";
            case "ttl_sub":
                return isVi ? "AI ENGINE · CHESS.COM" : "AI ENGINE · CHESS.COM";
            case "tab_live":
                return "LIVE";
            case "tab_engine":
                return "ENGINE";
            case "tab_arrows":
                return "ARROWS";
            case "tab_visual":
                return "VISUAL";
            case "engine":
                return isVi ? "Engine" : "Engine";
            case "card_control":
                return isVi ? "Điều Khiển" : "Controls";
            case "enable_stockfish":
                return isVi ? "Bật Engine" : "Enable Engine";
            case "panic_hint":
                return isVi ? "Chạm đúp vào thanh trên để bật/tắt ngay lập tức (chế độ ẩn)." : "Double-tap the top bar to switch it off or on instantly (panic mode).";
            case "card_elo":
                return isVi ? "Engine Strength" : "Engine Strength";
            case "row_elo":
                return isVi ? "Elo" : "Elo";
            case "elo_hint":
                return isVi ? "Điều chỉnh trình độ gợi ý của Engine theo mức Elo." : "Adjust the engine Elo.";
            case "depth":
                return isVi ? "Độ Sâu (Depth)" : "Depth";
            case "depth_hint":
                return isVi ? "Độ sâu tìm kiếm nước đi tối ưu của Engine." : "Search depth for optimal moves.";
            case "card_arrow":
                return isVi ? "Thiết Lập Mũi Tên" : "Arrow Settings";
            case "best_move_arrows":
                return isVi ? "Bật / Tắt Mũi Tên" : "Toggle Arrows";
            case "best_move_arrows_hint":
                return isVi ? "Bật hoặc tắt toàn bộ mũi tên gợi ý trên bàn cờ." : "Toggle all suggestion arrows on board.";
            case "num_arrows":
                return isVi ? "Số Lượng Mũi Tên" : "Arrow Limit";
            case "arrows_hint":
                return isVi ? "Mũi tên 1 luôn bật tối ưu. Tăng số lượng để xem thêm các nước ứng viên." : "Move 1 is always optimal. Increase limit to view more candidate moves.";
            case "opponent_arrows":
                return isVi ? "Mũi Tên Đối Thủ" : "Opponent Arrows";
            case "opponent_arrows_hint":
                return isVi ? "Hiện mũi tên gợi ý khi đến lượt đối thủ." : "Show suggestion arrows when it is opponent's turn.";
            case "threat_arrow":
                return isVi ? "Mũi Tên Đe Dọa" : "Threat Arrow";
            case "threat_arrow_hint":
                return isVi ? "Nước đi đáp trả nguy hiểm nhất của đối thủ (đỏ crimson)." : "Opponent's most dangerous reply (crimson).";
            case "card_cfg":
                return isVi ? "Giao Diện" : "Visual Interface";
            case "eval_bar":
                return isVi ? "Thanh Eval Bar" : "Eval Bar";
            case "eval_bar_hint":
                return isVi ? "Hiện thanh đánh giá cạnh bàn cờ." : "Show evaluation bar next to the board.";
            case "forced_mates":
                return isVi ? "Thông Báo Chiếu Hết" : "Mate Announcement";
            case "forced_mates_hint":
                return isVi ? "Thông báo nước chiếu hết bắt buộc khi Engine phát hiện đòn Mate." : "Announce forced checkmate when detected.";
            case "rate_moves":
                return isVi ? "Đánh Giá Nước Đi" : "Rate Moves";
            case "vibrate_blunder":
                return isVi ? "Rung Khi Mắc Sai Lầm" : "Vibrate on Mistakes";
            case "vibrate_hint":
                return isVi ? "Rung cảnh báo khi vừa đi nước đi không tối ưu hoặc Blunder." : "Vibrate warning on sub-optimal or blunder moves.";
            case "reset_defaults":
                return isVi ? "Đặt Lại Cài Đặt Mặc Định" : "Reset to Defaults";
            case "reset_title":
                return isVi ? "Đặt lại cài đặt engine?" : "Reset engine settings?";
            case "reset_message":
                return isVi ? "Độ sâu, mũi tên, lớp phủ và Elo sẽ trở về mặc định." : "Depth, arrows, overlays and strength go back to their defaults.";
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
                return "NNVC";
            case "contact_telegram":
                return isVi ? "Liên hệ Telegram: @nncutett" : "Telegram: @nncutett";
            case "open_telegram":
                return isVi ? "Mở Telegram @nncutett" : "Open Telegram @nncutett";
            case "arrow_palette_title":
                return isVi ? "Bảng màu mũi tên gợi ý" : "Arrow Color Palette";
            case "arrow_palette_hint":
                return isVi ? "Chạm vào ô màu để đổi màu cho từng thứ hạng gợi ý." : "Tap color box to customize colors for each candidate tier.";
            case "tier_1":
                return isVi ? "Nước 1 (Tối ưu)" : "Move 1 (Best)";
            case "tier_2":
                return isVi ? "Nước 2" : "Move 2";
            case "tier_3":
                return isVi ? "Nước 3" : "Move 3";
            case "tier_4":
                return isVi ? "Nước 4" : "Move 4";
            case "tier_5":
                return isVi ? "Nước 5" : "Move 5";
            case "choose_color":
                return isVi ? "Chọn màu" : "Pick Color";
            case "engine_choice":
                return isVi ? "Chọn Engine" : "Engine";
            case "engine_choice_hint":
                return isVi ? "Chọn động cơ phân tích (Komodo 3.3 hoặc Stockfish 18)." : "Select engine (Komodo 3.3 or Stockfish 18).";
            case "engine_komodo":
                return "Komodo 3.3";
            case "engine_stockfish":
                return "Stockfish 18";
            case "auto_depth":
                return isVi ? "Tự Động Độ Sâu Theo Elo" : "Auto Depth by Elo";
            case "auto_depth_hint":
                return isVi ? "Tự động gán độ sâu phù hợp nhất theo mức Elo (chuẩn Extension)." : "Automatically assign optimal depth based on Elo.";
            case "komodo_style":
                return isVi ? "Lối Chơi Komodo" : "Komodo Style";
            case "style_default":
                return isVi ? "Mặc định" : "Default";
            case "style_aggressive":
                return isVi ? "Tấn công" : "Aggressive";
            case "style_defensive":
                return isVi ? "Phòng thủ" : "Defensive";
            default:
                return key;
        }
    }
}
