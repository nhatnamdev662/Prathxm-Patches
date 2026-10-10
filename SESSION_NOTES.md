# Tóm Tắt Phiên Làm Việc (Session Notes) - Cập Nhật Nhat Nam Patches

## 1. Thông Tin Tác Giả & Thiết Bị
- **Tác giả**: Nhat Nam (`@nncutett` trên Telegram)
- **Repository độc lập**: `https://github.com/nhatnamdev662/Prathxm-Patches`
- **Tài khoản Git/GitHub**: `nhatnamdev662` (`nhatnamdev662@users.noreply.github.com`)

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

18. **Phát Hành Bản v2.0.36 (Sửa Lỗi Tối Màn Hình & Ghim Chặt Layout Thẻ Accuracy)**:
    - **Vô hiệu hóa triệt để StockfishTourOverlay**: Xóa bỏ hoàn toàn việc hiển thị tự động overlay hướng dẫn từ `GestureInterceptor` và `StockfishTourOverlay`, triệt tiêu dứt điểm bóng elip tối mờ 80% màn hình khi vào trận.
    - **Ghim Layout Thẻ Bằng Translation**: Sửa `OverlayManager` dùng `FrameLayout.LayoutParams(pillW, pillH)` kết hợp `setTranslationX/Y()` ghim cứng thẻ trên và dưới bàn cờ, không bị trôi dạt.
    - **Reset MaskFilter & Mutate Drawable**: Đảm bảo vẽ shadow và resource icon trong sạch 100%, không bị đè shader hay lem màu giữa các lượt đi.

19. **Phát Hành Bản v2.0.37 (Cách C: Ghim Khít Bàn Cờ, Đồng Bộ 100% Song Ngữ & Gỡ Thông Báo Chiếu Hết)**:
    - **Triển khai Cách C cho Accuracy & Elo Pills**: Ghim khít 0 margin vào mép trên và mép dưới bàn cờ, tự động đo chiều rộng tối thiểu 232dp, triệt tiêu hoàn toàn lỗi cắt chữ `ELO ĐÁNH GIÁ`.
    - **Đồng bộ hóa 100% Song Ngữ Anh / Việt**: Cập nhật toàn bộ thẻ Accuracy/Elo (`[BẠN]/[ĐỐI THỦ]`, `CHÍNH XÁC`, `ELO ĐÁNH GIÁ` <-> `[YOU]/[OPPONENT]`, `ACCURACY`, `EST. ELO`), menu cài đặt 5 tab, hộp thoại nhật ký hệ thống, phân loại nước đi đồng bộ qua `I18n.java`.
    - **Xóa bỏ hoàn toàn thông báo chiếu hết**: Gỡ vĩnh viễn banner `MATE IN X!` và switch cài đặt.
    - **Quy chuẩn hóa và dọn dẹp**: Bổ sung `rule.md`, dọn sạch repository đưa scripts cũ vào `scripts_archive/` và `mpp_archive/`.

22. **Auto-apply Real-time, Nút Đóng Extension, Tải Ngôn Ngữ In-place (v2.0.38 - v2.0.39)**:
    - Lưu và áp dụng tức thì mọi cài đặt thời gian thực lên bàn cờ.
    - Menu đóng duy nhất bằng nút `✕` chuẩn phong cách Extension ở header, loại bỏ 100% nút ở footer.
    - Đổi ngôn ngữ in-place tại chỗ không bị dismiss dialog hay chớp màn hình.

23. **Đồng Bộ 100% Giao Diện & Bảng Màu Phân Loại Nước Đi Chuẩn Extension (v2.0.40)**:
    - **Bảng màu phân loại chuẩn Extension**: Sửa triệt để các mã màu phân loại bị lệch (Great `#749BBF`, Missed Win `#F7C631`, Mistake `#FFA459`, Miss `#FF7769`, Forced `#999999`).
    - **Highlight ô cờ 50% Opacity**: Cả ô xuất phát (`from`) và ô đích (`to`) đều phủ cùng màu phân loại với độ mờ chuẩn 50% (alpha 128) giống hệt Extension; nước cờ Forced không phủ màu.
    - **Huy hiệu góc ô cờ & Fallback**: Chuẩn hóa kích thước `0.35f sqSize`, bóng đổ mờ 30% (`0x4D000000`), và đồng bộ trọn bộ ký hiệu fallback glyph / emoji theo Extension.

24. **Thử Nghiệm Chuyển Threat Arrow Sang Canvas Overlay (v2.0.41)**:
    - Thử nghiệm đưa Threat Arrow sang Canvas `ArrowOverlayView`.

25. **Khôi Phục Mũi Tên Hiểm Họa Gốc Native Chess.com (v2.0.42)**:
    - **Yêu cầu người dùng**: Để mũi tên hiểm họa như cũ.
    - **Thực hiện**:
      - Khôi phục nguyên bản [ArrowInjector.java](file:///e:/chess%20mobile/project/extensions/extension/src/main/java/app/prathxm/chess/extension/stockfish/ArrowInjector.java), [OverlayManager.java](file:///e:/chess%20mobile/project/extensions/extension/src/main/java/app/prathxm/chess/extension/stockfish/OverlayManager.java), [ArrowOverlayView.java](file:///e:/chess%20mobile/project/extensions/extension/src/main/java/app/prathxm/chess/extension/stockfish/ArrowOverlayView.java) và [StockfishExtension.java](file:///e:/chess%20mobile/project/extensions/extension/src/main/java/app/prathxm/chess/extension/stockfish/StockfishExtension.java).
      - Mũi tên hiểm họa (Threat Arrow) tiếp tục sử dụng vector `HintArrow` màu đỏ gốc của Chess.com tiêm qua `ArrowInjector.injectThreatArrow`.
      - Mũi tên gợi ý Stockfish (Tier 1..5) và huy hiệu phân loại tiếp tục hiển thị trên `ArrowOverlayView` bằng Canvas cyberpunk như cũ.

26. **Tối Giản Menu Cài Đặt (v2.0.43)**:
    - **Yêu cầu người dùng**:
      - Xóa nút đỏ "Đặt lại cài đặt mặc định" trong menu (không cần thiết).
      - Xóa nút xem nhật ký chẩn đoán Torch WASM trong Tab COACH.
    - **Thực hiện**:
      - Gỡ bỏ hoàn toàn nút đỏ "Đặt lại cài đặt mặc định" ở cuối Tab ENGINE trong `StockfishSettingsDialog.java`.
      - Gỡ bỏ hoàn toàn nút `coachLogBtn` ("📋 Xem & Copy Log") ở Tab COACH trong `StockfishSettingsDialog.java`.
      - Đóng gói bản vá sạch `v2.0.43` (`patches-2.0.43.mpp`).

27. **Khắc Phục Triệt Để Âm Thanh Lạ / Illegal Move Sound (v2.0.44)**:
    - **Yêu cầu người dùng**:
      - "và lỗi âm thanh vẫn còn fix triệt để coi" (âm thanh nghe giống nước đi không hợp lệ kêu khi đi nước cờ, thỉnh thoảng lại kêu 1 nước).
    - **Nguyên nhân cốt lõi**:
      - `ArrowInjector` tiêm phản xạ `setMoveArrows` vào `CBViewModelStateImpl` và gọi `invalidateAllBoards()`. Khi người dùng thực hiện nước đi hoặc kéo thả quân cờ, việc sửa đổi trạng thái bàn cờ xung đột với luồng xử lý nước đi của Chess.com, khiến Chess.com phát âm thanh nước đi không hợp lệ (`illegal.mp3`).
    - **Thực hiện**:
      - Loại bỏ 100% các lệnh gọi `ArrowInjector.clearEngineArrows` và `ArrowInjector.injectThreatArrow` khỏi `StockfishExtension` và `StockfishSettingsDialog`.
      - Vô hiệu hóa việc can thiệp `setMoveArrows` trong `onArrowsChanged`.
      - Chuyển Threat Arrow sang render 100% bằng Canvas trên `ArrowOverlayView` với màu đỏ neon `#EF4444`.
      - Đóng gói bản vá sạch `v2.0.44` (`patches-2.0.44.mpp`).
  18. **Bản Vá v2.0.45 — Triệt Tiêu 100% Âm Thanh & Rung Động Trong MoveClassifier**:
      - Gỡ bỏ hoàn toàn lệnh gọi `playBrilliantSound(activity)` và hàm phát file `sounds/brilliant.mp3` qua `MediaPlayer` trong `MoveClassifier.java`.
      - Loại bỏ rung phản hồi cảnh báo blunder (`vibrator.vibrate`) trong `MoveClassifier.java` để ngăn chặn tiếng rè cơ học.
      - Biên dịch sạch 39 file Java và đóng gói thành công `patches-2.0.45.mpp`.

  19. **Bản Vá v2.0.46 - v2.0.50 — Xử Lý Âm Thanh Nước Đi Khai Cuộc & Z-Order Phân Loại**:
      - Chặn triệt để âm thanh không hợp lệ phát ra trong giai đoạn khai cuộc khi Chess.com tải nước đi.
      - Khôi phục âm thanh nước đi chuẩn thực tế của bàn cờ.
      - Điều chỉnh thứ tự z-order và ưu tiên của giao diện phân loại nước đi (Move Classifier) để không đè lên mũi tên và thanh eval bar.

  20. **Bản Vá v2.0.51 - v2.0.52 — Style Mũi Tên Đe Dọa Độc Lập**:
      - Tạo style mũi tên đe dọa (Threat Arrow) độc lập với màu sắc và hoa văn riêng biệt, giúp người chơi dễ dàng nhận biết nguy cơ từ đối thủ mà không bị nhầm lẫn với các mũi tên gợi ý tốt nhất.

  21. **Bản Vá v2.0.53 — Sửa Triệt Để Lỗi Lệch Toạ Độ Lượt Đi & Lật Bàn Cờ**:
      - Sửa dứt điểm việc nhầm lẫn giữa `sideToPlaySelfEffects` (lượt đi hiện tại) và màu cờ của người chơi `isUserWhite`.
      - Hook trực tiếp vào `getFlipBoard()` và trường `t` của `ChessBoardView` cũng như `CBViewModelStateImpl` để lấy trạng thái lật bàn cờ thời gian thực chính xác 100%.

  22. **Bản Vá v2.0.54 — Tự Động Né Nhãn Điểm Eval Khỏi Thanh Eval Bar**:
      - Tính toán độ chiếm chỗ của thanh Eval Bar ở cạnh trái bàn cờ trong `ArrowOverlayView.java`.
      - Đăng ký vùng cấm đè vào thuật toán va chạm và kẹp toạ độ an toàn `minSafeX`, tự động né nhãn điểm sang các góc bên phải ô cờ đích.

  23. **Bản Vá v2.0.55 — Tích Hợp Trực Tiếp Thanh Eval Bar Gốc Chess.com (Native EvaluationBarView)**:
      - Sử dụng trực tiếp class `com.chess.internal.views.EvaluationBarView` từ mã nguồn ứng dụng Chess.com.
      - Tạo các đối tượng điểm số `com.chess.entities.Score$Centipawns` và `com.chess.entities.Score$MateIn` nguyên bản, cập nhật điểm và trạng thái lật cờ thời gian thực.
      - Mang lại trải nghiệm thanh Eval Bar chuẩn 100% giống hệt chế độ Analysis của Chess.com.

  24. **Bản Vá v2.0.56 — Chuẩn Hóa Hiển Thị Thanh Eval Bar Dọc (Native Vertical Orientation)**:
      - Phân tích bytecode DEX `classes8.dex`: `EvaluationBarView` mặc định khởi tạo hướng là `HORIZONTAL` khi không có xml styleable.
      - Can thiệp trực tiếp bằng Reflection gán trường enum `EvaluationBarView$Orientation` thành `Orientation.VERTICAL`.
      - Đồng bộ TextPaint sang `Paint.Align.CENTER` để căn chỉnh nhãn điểm eval chuẩn tâm theo chiều dọc.
      - Bật sẵn thanh Eval Bar mặc định (`isEvalBarEnabled = true`) trong SharedPreferences.
      - Đảm bảo hiển thị dọc chuẩn 100% lên/xuống dọc theo cạnh bàn cờ.

  25. **Bản Vá v2.0.57 — Tối Ưu Bố Cục Bàn Cờ & Thanh Eval Bar Không Đè Lên Ô Cờ**:
      - Khắc phục triệt để lỗi Eval Bar nằm đè lên viền trái các ô cờ (cột a / cột h) khi màn hình chiều ngang vừa khít bàn cờ (`boardX = 0`).
      - Tự động áp dụng `translationX = barWidth` và scale tỷ lệ bàn cờ `(boardW - barWidth) / boardW` khi Eval Bar hiển thị để chừa rãnh độc lập, thanh thoát 100% ngoài mép bàn cờ.
      - Khi ẩn hoặc tắt Eval Bar, tự động khôi phục hoàn toàn bàn cờ về vị trí gốc (`translationX = 0`, `scale = 1.0`).
      - Cung cấp module `BoardMetrics` tính toán toạ độ thực tế, đồng bộ hoá 1:1 cho toàn bộ các lớp phủ: `ArrowOverlayView`, `MoveClassifier` badges, `WdlBarView`, `EngineInfo` và `PlayerAccuracyPillView`.

  26. **Bản Vá v2.0.58 — Sửa Triệt Để Lỗi Lệch Toạ Độ & Chuẩn Hoá Chiều Cao Thanh Eval Bar 100%**:
      - **Nguyên nhân cốt lõi trong v2.0.57**: Việc can thiệp `setScaleX`, `setScaleY` và `setTranslationX` trực tiếp vào `boardView` (ChessBoardView) làm lệch toạ độ `getLocationInWindow` giữa các frame, khiến bàn cờ bị co giật, mất cân đối với container, và làm chiều dài thanh Eval Bar lệch/không đều (dư/hụt so với chiều cao bàn cờ). Đồng thời, component `EvaluationBarView` native vẽ text ngang ở tâm `width / 2` khiến điểm số bị cắt/chìm ra ngoài mép màn hình khi đặt ở toạ độ x = 0.
      - **Khắc phục triệt để 100%**:
        1. **Giữ nguyên trạng 100% bàn cờ gốc & Chuẩn hoá hình học 1:1**: Tuyệt đối không can thiệp scale hay translate vào `ChessBoardView`. `BoardMetrics` tự động reset các biến dạng tồn đọng trước khi đo toạ độ và ép chuẩn hình học 1:1 (`Math.min(rawW, rawH)`), đảm bảo thanh Eval Bar luôn khớp 100% với chiều cao bàn cờ thực tế từ đỉnh đến đáy, không một pixel lệch lạc.
        2. **Căn chỉnh nhãn điểm số tại đầu sân vững chãi (Home-end Anchoring)**: Nhãn điểm số (ví dụ `0.2`, `1.5`, `#`, `M3`) được ghim cố định ở đầu sân của bên dẫn điểm (đáy với Trắng, đỉnh với Đen khi bình thường, tự đảo khi lật bàn cờ) thay vì nhảy cóc ở tâm khối chữ nhật, loại bỏ hoàn toàn hiện tượng va chạm với vạch mốc 0.00 ở giữa thanh hay dịch chuyển đột ngột.
        3. **Hiển thị điểm số xoay dọc chuẩn phong cách Extension (`EvalBarView`)**: Điểm đánh giá được render xoay dọc -90 độ, căn giữa trục ngang thanh dựa trên `FontMetrics` chính xác từng pixel, loại trừ 100% hiện tượng bị cắt chữ, tràn viền màn hình hay đè lên quân cờ.
        4. **Xuyên thấu cảm ứng toàn diện**: Cả `dispatchTouchEvent` và `onTouchEvent` đều trả về `false`, cho phép mọi thao tác chạm/kéo thả quân cờ ở cột a xuyên qua thanh Eval Bar xuống bàn cờ tự nhiên 100%.
        5. **Đồng bộ toạ độ toàn bộ Overlays**: `updateEvalBar` hợp nhất sử dụng trực tiếp `BoardMetrics`, đồng bộ 100% với `ArrowOverlayView`, `MoveClassifier`, `WdlBarView` và thẻ Elo/Accuracy.

---

## 3. Trạng Thái Hiện Tại & Checklist Kiểm Thử
- **Phiên bản mới nhất trên GitHub**: `v2.0.58` (tag `v2.0.58`).
- **File tải bundle**: `https://github.com/nhatnamdev662/Prathxm-Patches/releases/download/v2.0.58/patches-2.0.58.mpp`
- **Checklist Kiểm Thử**:
  - [x] Đã biên dịch sạch 39 file Java và đóng gói thành công `patches-2.0.58.mpp` (189,667,067 bytes).
  - [x] Chiều cao thanh Eval Bar khớp 100% tuyệt đối với chiều cao bàn cờ (`boardH = Math.min(rawW, rawH)`), đều cả đỉnh và đáy.
  - [x] Triệt tiêu hoàn toàn biến dạng toạ độ, không can thiệp scale/translation vào `ChessBoardView`.
  - [x] Nhãn điểm số Eval hiển thị dọc thanh thoát, ghim vững chãi tại home end, không bao giờ bị cắt chữ hay tràn viền ở cạnh màn hình.
  - [x] Cảm ứng chạm kéo thả quân cờ ở cột a mượt mà qua cả `dispatchTouchEvent` và `onTouchEvent`.
  - [x] Đã cập nhật metadata `patches-bundle.json` và `patches-list.json`.
  - [x] Đã upload bản bundle hoàn thiện lên GitHub Release `v2.0.58`.


