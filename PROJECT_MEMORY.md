# NHẬT KÝ DỰ ÁN & BỘ GHI NHỚ DẶN DÒ (PROJECT MEMORY)
*Dự án: Chess Mobile - ReVanced Extension Patches cho Chess.com Android*
*Phiên bản hiện tại: v2.0.28*
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

---

## 3. TRẠNG THÁI HIỆN TẠI & KẾ HOẠCH TIẾP THEO

### 3.1. Trạng thái hiện tại
- **Phiên bản mới nhất**: **v2.0.24** (`patches-2.0.24.mpp`).
- **Kho lưu trữ GitHub**: Đã sẵn sàng phát hành Release v2.0.24.
- **Trạng thái Repo**: 100% sạch, không commit file `.py`.
- **Tiến trình kỹ thuật**:
  1. Hiển thị huy hiệu phân loại chính hãng Chess.com trực tiếp trên ô cờ.
  2. Âm thanh Brilliant sound chính hãng.
  3. Giao diện Log Console hiện đại, tô màu cú pháp, lọc theo danh mục và tự cuộn xuống đáy.
  4. Đã loại bỏ hoàn toàn các Toast rác và Toast che màn hình.
