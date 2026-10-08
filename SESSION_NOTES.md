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
11. **Phát Hành Bản v2.0.2 (Khắc Phục Crash Game Review, Nhận Diện Quân Trắng Nước 0 & Khôi Phục Đa Nhiệm)**:
   - **Khắc phục triệt để Crash Game Review**: Vô hiệu hoá toàn bộ 9 fingerprint Game Analysis & Game Review trong file `.mpp` (cho các matcher lambda trả về `false`), triệt tiêu việc inject mã smali gọi `getLocalAnalysisFlowForConfig` vào `GameAnalysisRepositoryImpl.a`. Chess.com dùng 100% Game Review gốc (real) từ máy chủ, không bao giờ bị lỗi `NoSuchMethodError`. Bổ sung stub methods tương thích trong `StockfishExtension.java`.
   - **Nhận diện quân Trắng & Tự động chạy ở Nước 0**: Bổ sung fallback kiểm tra góc nhìn bàn cờ `getFlipBoard()` từ `stateImplObject` trong `isUserWhite(...)` (bàn cờ không lật = quân Trắng `Boolean.TRUE`, bàn cờ lật = quân Đen `Boolean.FALSE`). Trong `onArrowsChanged`, tự động kích hoạt `triggerAnalysisForCurrentState()` ngay ở move 0 khi vừa vào ván cờ, hiển thị mũi tên và gợi ý tức thì mà không cần phải đi 1 nước trước.
    - **Khôi phục mũi tên khi đa nhiệm (thoát Home rồi vào lại)**: Trong `onActivityResumed`, reset toàn bộ signature cache (`lastArrowSignature = null`, `lastScheduledKey = null`), lên lịch chạy lại phân tích và ép vẽ lại bàn cờ sau khi Window/DecorView layout xong. Trong `OverlayManager.java`, bổ sung cơ chế `boardView.post(...)` nếu bàn cờ chưa kịp layout (`boardW <= 0`) khi resume, đảm bảo vẽ lại overlay ngay khi sẵn sàng.
    - Đã phát hành Release **`v2.0.2`** chứa file `patches-2.0.2.mpp` lên GitHub.
12. **Phát Hành Bản v2.0.3 (Sửa Dứt Điểm VerifyError Bằng Native DEX Patching)**:
    - **Nguyên nhân VerifyError trong v2.0.2**: Việc thay thế byte trực tiếp trong các file `.class` vô tình làm hỏng constructor `<init>()` của các class Fingerprint khiến Dalvik/ART Verifier ném `VerifyError: Constructor returning without calling superclass constructor`.
    - **Khắc phục triệt để 100% bằng Native DEX Patch**:
      - Giữ nguyên vẹn 100% tất cả file `.class` gốc trong bundle, loại trừ hoàn toàn mọi nguy cơ lỗi bytecode verifier.
      - Chèn trực tiếp lệnh DEX `return-void` (`0x0e 0x00`) vào method `stockfishPatch$lambda$0$0` trong file `classes.dex` của bundle ngay sau Hook 5 (`ensureEngineReady()`).
      - Cập nhật chuẩn xác Adler32 checksum và SHA-1 hash của `classes.dex`.
      - Kết quả: Khi Morphe Manager chạy đến Hook 5 thì kết thúc ngay lập tức, bỏ qua toàn bộ Game Analysis & Game Review bytecode patch. Không còn `VerifyError`, Chess.com dùng 100% Game Review gốc máy chủ không lỗi `NoSuchMethodError`.
    - Đã phát hành Release **`v2.0.3`** chứa file `patches-2.0.3.mpp` lên GitHub.

13. **Phát Hành Bản v2.0.30 - v2.0.31 (Khắc Phục Lỗi Lệch Bàn Cờ Trận Trực Tiếp Live Match)**:
    - **Căn chỉnh đúng ChessBoardView**: Không lấy nhầm container `ChessBoardLayout` (chứa cả player card và clock).
    - **Lật bàn cờ động**: Đọc `getFlipBoard()` trực tiếp từ view để đồng bộ ngay lập tức khi cầm quân Đen.
    - **Chuẩn hóa tọa độ**: Tính delta giữa window location của bàn cờ và decor view, triệt tiêu 100% hiện tượng lệch eval bar và mũi tên.
    - **Giới hạn biên huy hiệu**: Clamping đảm bảo huy hiệu nằm gọn trong ô cờ.
14. **Phát Hành Bản v2.0.32 (Đột Phá Nhận Diện Elo Trận Trực Tiếp RealGameActivity)**:
    - **Deep Reflection RealGameViewModel**: Mở `Lazy` delegate, quét `RcnPlayGameDelegateImpl` (`field m` -> `b()` -> `RcnGameState.getWhiteRating()` / `getBlackRating()`), `GameViewModelPlayersImpl` (`field d`), và `UserInfo` (`field e`).
    - **Jetpack Compose Semantics Tree**: Duyệt cấu trúc Compose qua `SemanticsOwner` / `SemanticsNode` trích xuất Elo trên giao diện Live.
    - **Regex Nâng Cấp**: Dùng `find()` với mẫu `\\((\\d{3,4})\\??\\)` chấp nhận tên chứa emoji, cờ quốc gia, ký tự đặc biệt.

15. **Phát Hành Bản v2.0.33 (Tùy Chọn Torch Depth, Supersede Cancellation & Xử Lý Kết Thúc Ván)**:
    - **Thanh trượt Độ Sâu Phân Loại (Torch Depth)**: Đưa thanh trượt `Độ Sâu Phân Loại (Torch Depth)` (từ 1 đến 10, mặc định = 2) vào tab COACH trong menu cài đặt, đồng bộ `StockfishSettings.getCoachDepth()`.
    - **Cơ chế Triệt Tiêu Hàng Chờ Dồn Lệnh (Supersede Cancellation Token)**: Bổ sung `currentRequestId` dạng Atomic. Khi có nước đi mới hoặc nước đi diễn ra dồn dập, Web Worker tự động bỏ qua toàn bộ kết quả phân loại của `reqId` cũ và hủy lệnh đang tính (`cancelTorchAnalysis`), chỉ xử lý nước cờ mới nhất (100% giống Extension).
    - **Xử lý Kết Thúc Ván Đấu & Reset Ván Mới Chuẩn Xác**: Khi Stockfish báo `terminal = true` (chiếu hết / hòa cờ) hoặc bàn cờ reset về `startpos`, lập tức dọn dẹp mũi tên trên bàn cờ (`clearEngineArrows`), ẩn overlay (`hideArrowOverlay`), và hủy sạch hàng chờ tính toán Torch (`cancelPendingRequests`).

17. **Phát Hành Bản v2.0.35 (Thẻ Độ Chính Xác & Estimated Elo Gắn Trực Tiếp Vào Bàn Cờ Chess.com)**:
    - **Tạo widget PlayerAccuracyPillView**: Gắn 2 thẻ Cyber Glass trực tiếp vào mép trên và mép dưới bàn cờ hiển thị Accuracy (%) và Estimated Elo thời gian thực trích xuất từ `CAPS` và `reportCard` của Torch CEE WebAssembly.
    - **Nhận diện đúng màu quân người chơi**: Người chơi cầm Trắng thì thẻ phía bạn là Trắng, cầm Đen thì thẻ phía bạn là Đen kèm viền Cyber Blue phát sáng và nhãn `[BẠN]`, phía đối thủ mang nhãn `[ĐỐI THỦ]`.
    - **Bật/tắt linh hoạt**: Nút switch `Độ Chính Xác & Elo Trực Tiếp` trong Tab COACH menu cài đặt.
    - **Tự động dọn dẹp**: Tự ẩn khi hết ván hoặc tắt engine.

---

## 3. Trạng Thái Hiện Tại & Checklist Kiểm Thử
- **Phiên bản mới nhất trên GitHub**: `v2.0.35` (tag `v2.0.35`).
- **File tải bundle**: `https://github.com/nhatnamdev662/Prathxm-Patches/releases/download/v2.0.35/patches-2.0.35.mpp`
- **Checklist Kiểm Thử**:
  - [x] Đã vá và nạp thành công bản `2.0.35` trên Morphe Manager.
  - [ ] Test hiển thị thẻ Accuracy & Estimated Elo pills: Khi đi cờ, kiểm tra 2 thẻ phía trên và phía dưới bàn cờ cập nhật % chính xác và Elo tương ứng với từng bên.
