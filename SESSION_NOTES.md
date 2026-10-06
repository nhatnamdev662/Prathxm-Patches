# Tóm Tắt Phiên Làm Việc (Session Notes) - Cập Nhật Nhat Nam Patches

## 1. Thông Tin Tác Giả & Thiết Bị
- **Tác giả**: Nhat Nam (`@nncutett` trên Telegram)
- **Repository độc lập**: `https://github.com/nhatnamdev662/Prathxm-Patches`
- **Tài khoản Git/GitHub**: `nhatnamdev662` (`nhatnamdev662@users.noreply.github.com`)
- **ADB kết nối không dây**: `192.168.100.41:39215` (Device ID: `adb-10AC4N1L0H000JK-37ZsAy (2)._adb-tls-connect._tcp`)
- **Bản APK mục tiêu**: `Chess.com v4.10.20-googleplay` (versionCode: `280085`), lưu tại `E:\chess mobile\apk\chess_4.10.20.apk`
- **Morphe Manager trên điện thoại**: Đã cài đặt sẵn `app.morphe.manager`

---

## 2. Các Thay Đổi Mới Đã Hoàn Thành
1. **Hỗ trợ Đa Ngôn Ngữ (Song Ngữ Anh / Việt)**:
   - Thêm `I18n.java` hỗ trợ chuyển ngữ toàn bộ UI menu settings.
   - Thêm nút switch `[EN]` / `[VI]` trên header của `StockfishSettingsDialog.java`.
2. **Bỏ Chặn Giới Hạn Trận Live**:
   - `StockfishExtension.isLiveMatch(...)` luôn trả về `false`, cho phép hiện đầy đủ gợi ý và eval bar trong các trận đấu live.
3. **Thay Đổi Nhận Diện Tác Giả & Liên Hệ**:
   - Tên thương hiệu: **Nhat Nam Patches**.
   - Thêm nút liên hệ Telegram `@nncutett` với màu sắc và icon máy bay giấy (`✈`) chuẩn Telegram trên:
     - Menu Cài đặt Engine (`StockfishSettingsDialog.java`)
     - Màn hình Hướng dẫn Gesture (`StockfishTourOverlay.java`)
     - Màn hình Báo lỗi Crash (`CrashActivity.java`)
     - `README.md`, `patches-list.json`, `CustomTitlesPatch.kt`.
4. **Đẩy Lên Kho Lưu Trữ Độc Lập**:
   - Đã khởi tạo và push thành công toàn bộ source code độc lập lên `https://github.com/nhatnamdev662/Prathxm-Patches`.
   - Đã gửi lệnh tự động thêm nguồn patch vào **Morphe Manager** trên điện thoại qua ADB.
5. **Khắc Phục Phiên Bản Mục Tiêu 4.10.20 Trong Morphe Manager**:
   - Biên dịch lại toàn bộ Java Extension thành DEX hoàn chỉnh kèm `I18n` và liên hệ Telegram `@nncutett`.
   - Cập nhật bytecode `Constants.class` và `classes.dex` của gói patch `.mpp` chuyển mục tiêu hỗ trợ từ `4.10.0` sang `4.10.20` và `4.10.20-googleplay`.
   - Đã tải lại file `patches-1.15.0.mpp` lên Release `v1.15.0` trên GitHub cá nhân.
   - Dọn dẹp sạch sẽ toàn bộ thư mục tạm, chỉ giữ lại cấu trúc gọn gàng: `apk/`, `project/`, `SESSION_NOTES.md`.
6. **Khắc Phục Lỗi Fingerprint Trên Bản 4.10.20**:
   - Sửa `AdFreePatch`: Thay thế phương thức `ofCode` cũ bằng phương thức kế nhiệm chuẩn trong 4.10.20 là `ofCodeOrDefault(String, UserMembershipLevel)` thuộc `UserMembershipLevel$Companion`, giữ nguyên logic fallback về `BASIC` nếu gặp mã membership lạ.
   - Sửa `BotUnlockPatch`: Khớp chính xác method `f()` cho `getCanPlay` của bản 4.10.20 và bỏ qua `requiresActivation` đã gỡ bỏ.
   - Đã phát hành Release **`v1.16.1`** chứa file `patches-1.16.1.mpp` lên GitHub.
7. **Khắc Phục Lỗi Fingerprint Lichess Puzzle Trên Bản 4.10.20**:
   - Trong `NewDailyPuzzleServiceImpl` của Chess.com 4.10.20:
     - Phương thức tải câu đố (`getDailyPuzzle`) đổi tên obfuscation từ `a` sang `b` nhận `(String, Continuation)`.
     - Phương thức gửi nước đi (`submitDailyPuzzleAction`) đổi từ `b` sang `a` nhận `(long, Action, HintState, Integer, Integer, Continuation)`.
   - Sửa `NewDailyPuzzleGetFingerprint`: Khớp chính xác method `b` (và `a`) trên `NewDailyPuzzleServiceImpl`.
   - Sửa `NewDailyPuzzleSubmitFingerprint`: Khớp chính xác method `a` trả về `Object`.
   - Thêm overload `submitDailyPuzzleAction(int, Object, Object, Object)` vào `LichessPuzzleExtension.java` để tương thích cả 2 chuẩn gọi.
   - Đã phát hành Release **`v1.16.2`** chứa file `patches-1.16.2.mpp` lên GitHub.
8. **Khắc Phục Nguồn Morphe 1.15.0 Và Lỗi BotPersonalityBot**:
   - File `patches-bundle.json` trên root repo trước đó chưa cập nhật phiên bản, khiến Morphe Manager vẫn nạp gói cũ `1.15.0`.
   - Trong bản 4.10.x, class `Bot$PersonalityBot` không còn chứa method `canPlay` (đã chuyển hoàn toàn sang Protobuf `Lchesscom/bots/v1/BotPersonality` và check UI `LockedBots`).
   - Đã bỏ qua lệnh gọi `BotPersonalityBotGetCanPlayFingerprint` để không gây lỗi `PatchException` khi patch bất kỳ bản 4.10.x nào.
   - Đã cập nhật `patches-bundle.json` lên `1.16.2` và tải lại file `patches-1.16.2.mpp` lên Release GitHub.
9. **Phát Hành Bản v2.0.0**:
   - Chuyển đổi định dạng `extension.mpe` sang raw DEX bytecode chuẩn (`dex\n035\0`), dọn dẹp các release và tag cũ.
10. **Phát Hành Bản v2.0.1 (Né Mũi Tên Trùng, Mũi Tên Hiểm Họa Gốc, Engine Sát Nhau & Bảng Màu Tùy Chỉnh)**:
   - **Né Mũi Tên (Lane Separation)**: Tính toán độ lệch trực giao `perpOffset` trong `ArrowOverlayView.java` theo chuẩn NNVC Extension, triệt tiêu trùng đè khi cùng hướng, ngược chiều, cùng đích hoặc cùng điểm xuất phát (hỗ trợ cả đường thẳng và đường chữ L của quân Mã).
   - **Mũi Tên Hiểm Họa Gốc Chess.com**: Tách riêng khỏi Canvas Overlay; gọi `ArrowInjector.injectThreatArrow(...)` để Chess.com tự hiển thị vector native `HintArrow` màu đỏ gốc.
   - **Engine Luôn Hoạt Động & Đặt Liền Kề**: Bỏ hoàn toàn nút switch `limit_strength`. Độ sâu (Depth) và ELO luôn chạy song song và nằm sát nhau trong Tab ENGINE.
   - **Bảng Màu Mũi Tên Tùy Chỉnh (Cyber Palette)**: Thêm giao diện chọn màu trực tiếp cho từng bậc gợi ý (Nước 1 đến Nước 5) với bảng màu Cyber Luxury trong Tab VISUAL, lưu cấu hình vào `SharedPreferences`.
   - Đã phát hành Release **`v2.0.1`** chứa file `patches-2.0.1.mpp` lên GitHub.
