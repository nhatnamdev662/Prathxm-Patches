# NHẬT KÝ DỰ ÁN & BỘ GHI NHỚ DẶN DÒ (PROJECT MEMORY)
*Dự án: Chess Mobile - ReVanced Extension Patches cho Chess.com Android*
*Phiên bản hiện tại: v2.0.38*
*Cập nhật lần cuối: 2026-10-08*

---

## 1. QUY TẮC LÀM VIỆC & DẶN DÒ CỐT LÕI
1. **QUY TẮC PHIÊN BẢN (BẮT BUỘC SỐNG CÒN)**:
   - **TUYỆT ĐỐI KHÔNG ĐƯỢC TRÙNG PHIÊN BẢN KHI ĐÃ VÁ**: Morphe sẽ bị dính cache cũ nếu dùng lại version đã từng phát hành.
   - Mỗi lần sửa lỗi hoặc build gói mới, **BẮT BUỘC TĂNG PHIÊN BẢN LÊN** (ví dụ: 2.0.14 -> 2.0.15 -> 2.0.16 -> 2.0.17 -> 2.0.18).
   - **KHÔNG PUSH FILE BUILD .PY HOẶC SCRIPT TẠM LÊN GITHUB**: Tất cả script build/test/python chỉ để chạy local, đã được đưa vào `.gitignore`. Chỉ commit mã nguồn chính thức (Java, Kotlin, metadata JSON, MD).
2. **Thái độ nghiêm túc tuyệt đối**:
   - Không được để xảy ra bất kỳ lỗi ngớ ngẩn nào (syntax error, escape error, crash, NPE, missing assets).
   - Kiểm tra kỹ lưỡng trước khi đóng gói và phát hành.
   - Không code bừa, không báo kết quả ảo, mọi tính năng đều phải chạy thực tế trên Android WebView và Runtime.
3. **Phong cách giao tiếp**:
   - Cực kỳ ngắn gọn, dứt khoát, telegraphic (ultra-terse).
   - Đi thẳng vào vấn đề: Nêu nguyên nhân, hành động đã thực hiện, kết quả, bước tiếp theo. Không văn hoa, không dài dòng.
4. **Hệ thống chẩn đoán lỗi toàn diện (Diagnostic Logging)**:
   - Tất cả các tính năng (Stockfish UCI, FEN events, MoveClassifier, Torch WASM Engine, Overlays) **BẮT BUỘC** phải ghi log chi tiết kèm timestamp mili-giây vào `TorchEngine.DIAGNOSTIC_LOGS`.
   - Nút **"📋 Xem & Copy Log"** trong Menu cài đặt là công cụ then chốt để người dùng copy log thực tế và phản hồi nhanh nhất.
   - Buffer log duy trì tối thiểu 300 dòng mới nhất để bao quát toàn bộ ván đấu.
5. **PHÂN HỆ COACH PHỤ THUỘC 100% VÀO TORCH - TUYỆT ĐỐI KHÔNG LIÊN QUAN ĐẾN STOCKFISH**:
   - Phân loại nước đi (Brilliant, Great, Best, Good, Book, Inaccuracy, Mistake, Blunder...) là nhiệm vụ độc quyền của **Torch CEE 26MB (Komodo WebAssembly)**.
   - **NẾU KHÔNG PHÂN LOẠI ĐƯỢC THÌ BÁO LỖI RÕ RÀNG**, tuyệt đối không tự ý chuyển sang Stockfish ReviewMath.
   - Stockfish chỉ làm nhiệm vụ tính toán nước đi tốt nhất (`go depth`), Best Moves, Arrows, Eval Bar trong tab ENGINE.
6. **CẤU TRÚC GIAO DIỆN BÁM SÁT 100% THEO EXTENSION**:
   - Mobile bám sát theo cấu trúc của Extension, chia thành đúng 5 Tab theo chuẩn Extension:
     `[ LIVE | ENGINE | COACH | VISUAL | ARROWS ]`.
   - Mọi thiết lập của Torch Coach (phân loại nước đi, giọng nói, cảnh báo rung, chẩn đoán log) phải nằm trọn trong tab **COACH**.

---

## 2. KIẾN TRÚC KỸ THUẬT DỰ ÁN

### 2.1. Nền tảng mục tiêu
- Ứng dụng: **Chess.com Android APK**
- Phiên bản tương thích: `v4.9.0-free (versionCode: 409000000)`
- Công nghệ vá (Patching): ReVanced Patcher (`.mpp` format, ReVanced Patches v2/v3).
- Ngôn ngữ: Java 8 (Extension DEX) + Kotlin Bytecode (ReVanced Patch Bytecode).

### 2.2. Phân hệ Stockfish Engine Native
- Binary: `libstockfish.so` (biên dịch từ Stockfish C++ mã nguồn mở, tối ưu cho `arm64-v8a`, `armeabi-v7a`, `x86_64`).
- Giao thức: UCI qua `stdin`/`stdout`.
- Quản lý tiến trình: `StockfishProcess.java`, kết nối qua `StockfishBridge.java`.
- Tự động điều chỉnh Elo và Depth: Tính năng Auto-Depth theo Elo của người chơi, giới hạn thời gian suy nghĩ hợp lý để không gây nóng máy.

### 2.3. Phân hệ Torch Engine (CEE WebAssembly) - Đánh giá & Phân loại nước đi
- Engine: **Torch WebAssembly Engine (CEE 26MB)** nguyên bản từ Chess.com/NNVC Extension.
- Môi trường chạy: Headless Android `WebView` với **Web Worker** chạy nền.
- Cấu trúc file:
  - `torch.wasm`: File binary WebAssembly ~26MB.
  - `torch.js`: File glue code Emscripten ~375KB.
- Tự động tải: Nếu chưa có file cục bộ, tự động tải ngầm từ GitHub Release vào thư mục `files/torch/`.
- **Kỹ thuật Worker Bootstrap (v2.0.13+)**:
  - Main Document fetch `torch.js` và `torch.wasm` qua intercepted request (`https://torch-engine.local/`).
  - Toàn bộ mã nguồn Web Worker được đóng gói bằng **Base64 String** trong Java và giải mã bằng `atob()` trong WebView để triệt tiêu hoàn toàn 100% lỗi cú pháp và escape ký tự ngắt dòng (`\r\n`).
  - Worker thiết lập mock `XMLHttpRequest` (`WasmAwareXHR`) để cung cấp ngay buffer WASM cho Emscripten mà không cần tải lại mạng.
  - Sau khi khởi động, Worker kích hoạt các option: `UseDeclarativePositionCommand`, `HandleContinuations`, `HandleContinuationsDepth 4`, `ClassificationV3 true`, `ServeCommandV2 true`.
- Giao thức phân loại: Gửi chuỗi nước đi `moves <UCI_LIST>` và `fetch analysis`, nhận JSON kết quả chứa nhãn nước đi chuẩn Chess.com:
  - `Brilliant` (!!) - Thiên tài
  - `Great` (!) - Xuất sắc
  - `Best` (★) - Tốt nhất
  - `Excellent` (👍) - Tuyệt vời
  - `Good` (✓) - Nước cờ hay
  - `Book` (📖) - Nước khai cuộc
  - `Inaccuracy` (?!) - Thiếu chính xác
  - `Mistake` (?) - Sai lầm
  - `Blunder` (??) - Sai lầm nghiêm trọng
  - `Miss` / `Missed Win` (✕) - Bỏ lỡ cơ hội thắng
  - `Forced` (➔) - Bắt buộc

---

## 3. CÁC ĐIỂM CHẠM BYTECODE & QUY TRÌNH BUILD

### 3.1. Hook 5 & Hook 6 trong `StockfishPatchKt.class`
- **Hook 5 (Stockfish Init)**:
  - Chèn gọi phương thức khởi tạo `StockfishExtension.init(activity)`.
  - **LƯU Ý SỐNG CÒN**: Phương thức trong Kotlin bytecode trả về `Unit.INSTANCE` (`kotlin.Unit`). Không được để hổng stack hoặc return sai kiểu.
- **Hook 6 (UI Hooks & Navigation)**:
  - Bắt các sự kiện `onBoardChanged`, `onArrowsChanged`, menu settings.

### 3.2. Quy trình biên dịch & Đóng gói (.mpp)
1. Thư mục mã nguồn Java: `extensions/extension/src/main/java/`
2. Biên dịch Java sang `.class` bằng `javac -source 8 -target 8`.
3. Biên dịch `.class` sang `.dex` bằng `d8.bat` (Android SDK Build Tools 34.0.0).
4. Đóng gói vào tệp zip không nén `dexes.zip` -> đổi tên thành `classes.dex` bên trong gói patch.
5. Tạo tệp `.mpp` (ReVanced Patches Package):
   - Đặt metadata version (ví dụ: `2.0.13`).
   - Cập nhật `patches-bundle.json` và `patches-list.json`.
6. Thực hiện tự động thông qua script `build_2_0_*.py`.

---

## 4. LỊCH SỬ PHÁT TRIỂN & CÁC BÀI HỌC KINH NGHIỆM
- **v2.0.9 - v2.0.11**:
  - Tích hợp Torch WebAssembly đầu tiên.
  - Nhận diện cần thiết kế bộ nạp file 26MB từ bộ nhớ máy hoặc GitHub Release.
- **v2.0.12**:
  - Bật mặc định tính năng phân loại nước đi (`show_move_classification = true`).
  - Tích hợp nút xem & sao chép log trực tiếp trong hộp thoại Cài đặt cờ vua Stockfish.
- **v2.0.13**:
  - **Sửa lỗi SyntaxError line 41**: Chuỗi escape `\r\n` trong Java string template literal bị parse thành ký tự ngắt dòng vật lý trong JS single quotes. Khắc phục dứt điểm bằng Base64 encode toàn bộ worker script.
  - **Nâng cấp Diagnostic Logger**: Tăng bộ đệm log lên 300 dòng; bổ sung log cho Stockfish UCI commands, Board FEN updates, và MoveClassifier events.
  - **Khởi tạo tài liệu dự án `PROJECT_MEMORY.md`**.
- **v2.0.14**:
  - **Tự động nhận diện Elo đối thủ và người chơi (`EloScanner.java`)**:
    - Quét tọa độ view người chơi phía trên (Top) và phía dưới (Bottom) bàn cờ ChessBoardView.
    - Áp dụng bộ lọc nghiêm ngặt (Strict Filter): Loại bỏ 100% đồng hồ thời gian (chứa dấu `:`, `|`), đơn vị thời gian (`min`, `sec`), điểm quân chênh lệch (`+1`, `-3`). Chỉ nhận đúng định dạng Elo số nguyên từ 100 đến 3800.
    - Tự động nhận diện hướng bàn cờ (Flipped board) để gán đúng `WhiteElo` và `BlackElo`.
    - Tự động đồng bộ vào Torch WebAssembly Engine qua `setoption name WhiteElo/BlackElo value ...`.
- **v2.0.16**:
  - **Khắc phục triệt để lỗi phân loại nước đi Torch CEE bằng chuỗi nước đi tích lũy (Cumulative Moves)**:
    - **Nguyên nhân cốt lõi**:
      1. Komodo/Torch CEE WebAssembly biên dịch với tùy chọn `UseDeclarativePositionCommand` chỉ chấp nhận cú pháp chuẩn của Chess.com Extension: `position startpos moves m1 m2 m3...`.
      2. Khi gửi `position fen <FEN> moves <move>`, parser của Torch CEE không hỗ trợ FEN đi kèm moves trực tiếp cho Declarative mode dẫn đến abort assertion `Aborted()`.
      3. Ngược lại, khi gửi danh sách nước đi tích lũy `position startpos moves d2d4 g8f6 c1f4...`, Torch chạy 100% mượt mà, trả về nhãn Book/Good/Best/Inaccuracy/Mistake/Blunder hoàn hảo.
    - **Giải pháp triệt để**:
      - Tự động duy trì và suy luận toàn bộ danh sách nước đi từ thế cờ ban đầu (`fenHistory` tự động chèn `startpos` nếu trận đấu bắt đầu từ nước 1).
      - Sử dụng chuỗi nước đi tích lũy: `position startpos moves <ALL_PLAYED_MOVES>`.
      - Bổ sung cơ chế Auto-Recover Worker: Tự động khởi động lại Worker sau 1.5s nếu gặp bất kỳ lỗi `RuntimeError: Aborted()` nào để khôi phục trạng thái sạch cho engine.
- **v2.0.18**:
  - **Tách phân hệ COACH 100% độc lập cho Torch & Cấu trúc 5 Tab chuẩn Extension**:
    - **100% Phụ thuộc vào Torch Coach Engine**:
      - Loại bỏ hoàn toàn sự can thiệp của Stockfish vào việc phân loại nước đi (`MoveClassifier`).
      - Phân loại nước cờ là nhiệm vụ độc quyền của **Torch CEE 26MB (Komodo WebAssembly)**.
      - Khi Torch gặp lỗi hoặc thiếu lịch sử `startpos`, hệ thống **ghi log chẩn đoán lỗi và báo lỗi trung thực**, tuyệt đối không tự động chuyển sang Stockfish ReviewMath.
    - **Tổ chức 5 Tab chuẩn Extension**:
      - Đồng bộ cấu trúc Settings Dialog với Extension: `[ LIVE | ENGINE | COACH | VISUAL | ARROWS ]`.
      - Toàn bộ cài đặt phân loại nước đi, cảnh báo rung, chẩn đoán log WebAssembly được chuyển trọn vẹn sang tab **COACH**.
      - Tab **ENGINE** chỉ còn thuần túy phục vụ cấu hình động cơ tính toán nước đi (Komodo/Stockfish, Depth, MultiPV, Elo).
- **v2.0.19**:
  - **Khắc phục triệt để lỗi RuntimeError Aborted WebAssembly khi vào giữa ván**:
    - **Nguyên nhân gốc rễ**: Khi vào lại ván ở giữa chừng (nước 4-5), `fenHistory` bị thiếu các nước mở màn từ `startpos`. Chuỗi nước đi bắt đầu bằng nước đi không thể thực hiện từ bàn cờ ban đầu (ví dụ `e1g1` - nhập thành khi các quân chưa mở đường), khiến WebAssembly dính assertion fail `RuntimeError: Aborted()` và rơi vào vòng lặp restart worker.
    - **Giải pháp**:
      - Bổ sung bộ lọc an toàn `STARTPOS_LEGAL_FIRST_MOVES` (20 nước đi mở màn hợp lệ của Trắng từ `startpos`).
      - Nếu chuỗi nước đi không bắt đầu từ một nước đi hợp lệ của `startpos`, lập tức chặn không gửi cho Torch WebAssembly và hiển thị hướng dẫn người dùng: `⚠️ [Torch Coach] Cần lịch sử nước đi từ đầu ván`.
      - Bảo vệ tuyệt đối WebAssembly Worker không bao giờ bị abort hay restart lặp đi lặp lại.
- **v2.0.20**:
  - **Hook Move History trực tiếp từ PositionObject của Chess.com App**:
    - **Vấn đề**: Khi người dùng vào lại ván cờ giữa chừng, `fenHistory` không có các nước đi trước đó, dẫn đến bộ lọc chặn lệnh phân loại và hiển thị cảnh báo thiếu lịch sử.
    - **Giải pháp triệt để**:
      - Hook trực tiếp vào `positionObject` (`StandardPosition`) được truyền vào `onBoardChanged(stateImplObject, positionObject)`.
      - Thông qua reflection gọi `positionObject.h()` để lấy toàn bộ danh sách `PositionAndMove` (`com.chess.chessboard.history.i`) từ Move 1 đến hiện tại.
      - Chuyển đổi từng phần tử sang chuỗi UCI move bằng `com.chess.chessboard.compengine.MoveConverterKt.c(item)` (hoặc `b(move)`).
      - Cung cấp chuỗi nước đi đầy đủ 100% từ `startpos` cho `torch.analyze(moves, userColor, ...)`.
      - Giải quyết dứt điểm vấn đề vào giữa ván: Torch CEE phân loại chính xác mọi nước đi mà không bao giờ bị thiếu lịch sử hay crash WebAssembly.
- **v2.0.21**:
  - **Deep Reflection & Enhanced Scanner tự động nhận diện Elo cho Torch Coach**:
    - **Đục sâu Reflection từ Game Models**: Quét đa tầng qua `stateImplObject` và `Activity` (ViewModel, Game Controller) để tìm trực tiếp `RcnGameState` (`getWhiteRating()`, `getBlackRating()`) và `UserInfo` / `LiveUserInfo` (`getRating()`, `getColor()`) của cả 2 bên.
    - **Nâng cấp Enhanced Regex Scanner**: Hỗ trợ nhận diện số Elo kèm tên hoặc thể loại cờ (ví dụ `Magnus (2850)`, `Bot Martin (250)`, `1500 Rapid`), tự động phát hiện Elo người chơi (Bottom) và đối thủ (Top) dựa theo chiều bàn cờ (Flipped).
    - **Tự động đồng bộ Elo vào Torch CEE**: Gửi ngay lệnh `setoption name WhiteElo value X` và `setoption name BlackElo value Y` vào WebAssembly Engine để tính toán phân loại chuẩn xác theo trình độ thực tế của ván đấu.
- **v2.0.22**:
  - **Triệt tiêu 100% hiện tượng khựng/lag khi đi cờ**:
    - Chuyển toàn bộ tác vụ quét Elo (Reflection và View Hierarchy) sang một background single-thread worker (`SCAN_EXECUTOR`).
    - Trên UI Thread chỉ thực hiện snapshot cực nhẹ các text view (< 1ms, không reflection, không regex).
    - Loại bỏ lệnh gọi `EloScanner.scanAndApply` dư thừa khỏi `onArrowsChanged`.
    - Thêm throttle 3000ms và không quét lại khi đã phát hiện Elo cho ván hiện tại.
  - **Phân biệt rành mạch [Bạn] vs [Đối thủ]**:
    - Xác định chính xác quân của người chơi (`isUserWhite`), gắn tag `[Bạn]` hoặc `[Đối thủ]` trên Toast và Log.
    - Truyền đúng `userColor` là màu quân của người chơi thực tế vào Torch CEE.
  - **Sửa lỗi map nhãn Great Move**:
    - Ánh xạ chính xác `greatfind`, `great_find` sang `Nước cờ xuất sắc (Great)` (`!`) thay vì rơi vào default `Good`.
    - Bổ sung alias `missedwin` sang `Bỏ lỡ cơ hội thắng (Missed Win)` (`✕`).
  - **Hỗ trợ v1 và v2 ChessBoardView**:
    - Mở rộng `findChessBoardView` nhận diện cả `com.chess.chessboard.v2.ChessBoardView`.
    - Bổ sung log chẩn đoán `[ELO DEBUG]` chi tiết từng bước.
- **v2.0.23**:
  - **Triệt tiêu 100% Toast rác chạy ngầm / hệ thống**:
    - Xóa bỏ toàn bộ các Toast thông báo trạng thái nội bộ (`[Torch] Engine WASM sẵn sàng (100% Real)`, `Đang tải engine Torch`, `Tải hoàn tất`, cảnh báo startpos, v.v.).
    - Đảm bảo khi người dùng đóng app hoặc ra ngoài màn hình không bao giờ bị popup làm phiền.
    - Thêm kiểm tra vòng đời `isFinishing()` và `isDestroyed()` để bảo vệ toast phân loại nước đi.
  - **Đại tu giao diện Nhật ký chẩn đoán (Log Console)**:
    - **Tự động cuộn xuống đáy (Auto-scroll to bottom)**: Khi mở màn hình Log, tự động cuộn xuống dưới cùng để người dùng thấy ngay các log mới nhất.
    - **Tô màu cú pháp thông minh (Syntax Highlighting)**: Dùng `SpannableStringBuilder` phân biệt rực rỡ các tag `[ERROR]`, `[WARN]`, `[BESTMOVE]`, `[CLASSIFIED]`, `[TORCH]`, `[STOCKFISH]`, `[ELO]`, `[BOARD]` và timestamp.
    - **Thanh lọc nhanh (Filter Chips)**: Thêm 5 chip lựa chọn: `Tất cả`, `Torch / Phân loại`, `Stockfish`, `Bàn cờ & Elo`, `Lỗi / Cảnh báo`.
- **v2.0.24**:
  - **Trích xuất & Hiển thị Huy hiệu Phân loại chính hãng Chess.com trên Ô cờ (Square Classification Badge)**:
    - Nạp trực tiếp các Vector Drawable gốc từ resource APK Chess.com (`move_classification_classification_brilliant`, `great_find`, `best`, `excellent`, `good`, `book`, `inaccuracy`, `mistake`, `blunder`, `miss`, `missed_win`, `forced`).
    - Vẽ huy hiệu tròn chính thức ngay góc trên ô cờ vừa đi với bóng đổ mượt mà, biến mất khi bắt đầu ván mới.
    - Có cơ chế Fallback vẽ huy hiệu màu sắc chuẩn Chess.com nếu không nạp được drawable.
  - **Tích hợp Âm thanh Nước cờ Thiên tài (Brilliant Sound)**:
    - Tự động phát âm thanh chính thức `sounds/brilliant.mp3` của Chess.com khi phát hiện nước cờ Thiên tài.
  - **Loại bỏ Toast che màn hình**:
    - Chuyển hoàn toàn từ Toast hệ thống sang hiển thị trực quan trực tiếp trên bàn cờ.
- **v2.0.27 - v2.0.29**:
  - **Chuẩn hóa hiển thị đúng 1 phân loại duy nhất (v2.0.27)**: Khi đối thủ đi cờ, huy hiệu và hiệu ứng ô cờ của người chơi được thay thế ngay lập tức bằng nước đi mới của đối thủ.
  - **Bảng màu nguyên bản 100% từ APK (v2.0.28)**: Trích xuất trực tiếp giá trị màu nhị phân từ resource `color_classification_*.xml` trong APK Chess.com gốc.
  - **Gỡ bỏ cấm chụp màn hình (v2.0.29)**: Can thiệp Window và xóa cờ `FLAG_SECURE` trên toàn bộ Activity để người dùng tự do quay/chụp màn hình.
- **v2.0.30 - v2.0.31 (Sửa Lỗi Lệch Bàn Cờ Trận Trực Tiếp Live Match)**:
  - **Nhận diện chính xác ChessBoardView**: Không bị nhận nhầm container ngoài `ChessBoardLayout` (vốn bao gồm cả player bar và clock làm đẩy overlay lên trên).
  - **Nhận diện Flipped động**: Gọi trực tiếp `getFlipBoard()` từ `ChessBoardView` phản hồi ngay lập tức chiều bàn cờ.
  - **Chuẩn hóa tọa độ DecorView**: Tính toán delta giữa window location của bàn cờ và decor view cùng padding hệ thống, đảm bảo tuyệt đối khớp 1:1.
  - **Giới hạn biên huy hiệu (Clamping)**: Huy hiệu phân loại luôn nằm an toàn 100% trong ô cờ, không tràn viền.
- **v2.0.32 (Đột Phá Quét Elo Trong Trận Trực Tiếp RealGameActivity)**:
  - **Đục sâu RealGameViewModel qua Reflection**: Mở `Lazy` delegate, truy xuất `RcnPlayGameDelegateImpl` (`field m` -> `b()` -> `RcnGameState.getWhiteRating()` / `getBlackRating()`), `GameViewModelPlayersImpl` (`field d`), và danh sách `UserInfo` (`field e`).
  - **Bóc tách Jetpack Compose Semantics Tree**: Duyệt sâu `AndroidComposeView` / `ComposeView` qua `SemanticsOwner` (`field z` / `getSemanticsOwner()`) và config maps để lấy Elo và tên người chơi hiển thị trên giao diện Live.
  - **Regex Nâng Cấp**: Sử dụng `find()` thay cho `matches()` với mẫu `\\((\\d{3,4})\\??\\)` chấp nhận tên có emoji, cờ quốc gia, ký tự đặc biệt.
- **v2.0.35 (Thẻ Độ Chính Xác & Estimated Elo Gắn Trực Tiếp Vào Bàn Cờ Chess.com)**:
  - **Tạo widget PlayerAccuracyPillView**: Gắn 2 thẻ Cyber Glass trực tiếp vào mép trên và mép dưới bàn cờ hiển thị Accuracy (%) và Estimated Elo thời gian thực trích xuất từ `CAPS` và `reportCard` của Torch CEE WebAssembly.
  - **Nhận diện đúng màu quân người chơi**: Người chơi cầm Trắng thì thẻ phía bạn là Trắng, cầm Đen thì thẻ phía bạn là Đen kèm viền Cyber Blue phát sáng và nhãn `[BẠN]`, phía đối thủ mang nhãn `[ĐỐI THỦ]`.
  - **Bật/tắt linh hoạt**: Nút switch `Độ Chính Xác & Elo Trực Tiếp` trong Tab COACH menu cài đặt.
  - **Tự động dọn dẹp**: Tự ẩn khi hết ván hoặc tắt engine.
- **v2.0.36 (Sửa Lỗi Tối Màn Hình & Ghim Chặt Layout Thẻ Accuracy)**:
  - **Vô hiệu hóa triệt để StockfishTourOverlay**: Loại bỏ hoàn toàn overlay hướng dẫn tự động kích hoạt gây bóng elip tối mờ 80% màn hình trong trận đấu.
  - **Ghim Layout Thẻ Bằng Translation**: Sửa `OverlayManager` dùng `FrameLayout.LayoutParams` và `setTranslationX/Y()` để ghim chặt thẻ phía trên và dưới bàn cờ, không bị trôi hay co giật.
  - **Reset MaskFilter & Mutate Drawable**: Đảm bảo vẽ shadow và nạp tài nguyên icon không bị lem màu hoặc đè shader giữa các nước đi.
- **v2.0.37 (Cách C: Ghim Khít Bàn Cờ, Đồng Bộ 100% Song Ngữ & Gỡ Thông Báo Chiếu Hết)**:
  - **Triển khai Cách C cho Accuracy & Elo Pills**: Ghim khít 0 margin vào mép trên và mép dưới bàn cờ, tự động đo chiều rộng tối thiểu 232dp, triệt tiêu hoàn toàn lỗi cắt chữ `ELO ĐÁNH GIÁ`.
  - **Đồng bộ hóa 100% Song Ngữ Anh / Việt**: Cập nhật toàn bộ thẻ Accuracy/Elo, menu cài đặt 5 tab, hộp thoại nhật ký hệ thống, phân loại nước đi đồng bộ qua `I18n.java`.
  - **Xóa bỏ hoàn toàn thông báo chiếu hết**: Gỡ vĩnh viễn banner `MATE IN X!` và switch cài đặt.
  - **Quy chuẩn hóa và dọn dẹp**: Bổ sung `rule.md`, dọn sạch repository đưa scripts cũ vào `scripts_archive/` và `mpp_archive/`.
- **v2.0.38 (Áp Dụng Cài Đặt Real-time & Nút Tắt Menu Chuẩn Extension)**:
  - **Tự động áp dụng & lưu cấu hình thời gian thực (Auto-apply Real-time)**: Mọi thao tác switch, seekbar, chọn engine, chọn phong cách Komodo, đổi bảng màu đều tự động lưu vào SharedPreferences và áp dụng tức thì lên bàn cờ, không cần bấm nút lưu.
  - **Nút đóng Menu dấu ✕ phong cách Extension**: Trang bị nút `✕` tinh tế ở góc phải header; footer chuyển thành nút `Xong` (hoặc đóng ngay bằng `✕`).
  - **Đồng bộ hóa ngôn ngữ thời gian thực**: Nút chuyển đổi [VI]/[EN] gọi `OverlayManager.refreshOverlaysLanguage()` cập nhật ngay lập tức các lớp phủ bàn cờ và menu.

---

## 3. TRẠNG THÁI HIỆN TẠI & KẾ HOẠCH TIẾP THEO

### 3.1. Trạng thái hiện tại
- **Phiên bản mới nhất**: **v2.0.38** (`patches-2.0.38.mpp`).
- **Kho lưu trữ GitHub**: Đã phát hành Release `v2.0.38`.
- **Trạng thái Repo**: 100% sạch, không commit file `.py`, root ngăn nắp.
- **Tiến trình kỹ thuật**:
  1. Đã đưa Torch Depth về mặc định = 2.
  2. Đã giải quyết triệt để vấn đề "hàng chờ dồn lệnh" nhờ cơ chế `reqId` token cancellation.
  3. Đã xử lý xóa nhãn phân loại ngay lập tức khi vừa có nước cờ mới.
  4. Đã hoàn thiện Cách C: ghim khít mép bàn cờ, tự đo kích thước, 100% song ngữ.
  5. Đã xóa vĩnh viễn thông báo chiếu hết và tour overlay tối màn hình.
  6. Đã bổ sung `rule.md` và dọn dẹp sạch sẽ repository.
  7. Đã hoàn thiện Auto-apply Real-time và nút đóng `✕` Extension v2.0.38.
