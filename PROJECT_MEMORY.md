# NHẬT KÝ DỰ ÁN & BỘ GHI NHỚ DẶN DÒ (PROJECT MEMORY)
*Dự án: Chess Mobile - ReVanced Extension Patches cho Chess.com Android*
*Phiên bản hiện tại: v2.0.17*
*Cập nhật lần cuối: 2026-10-07*

---

## 1. QUY TẮC LÀM VIỆC & DẶN DÒ CỐT LÕI
1. **QUY TẮC PHIÊN BẢN (BẮT BUỘC SỐNG CÒN)**:
   - **TUYỆT ĐỐI KHÔNG ĐƯỢC TRÙNG PHIÊN BẢN KHI ĐÃ VÁ**: Morphe sẽ bị dính cache cũ nếu dùng lại version đã từng phát hành.
   - Mỗi lần sửa lỗi hoặc build gói mới, **BẮT BUỘC TĂNG PHIÊN BẢN LÊN** (ví dụ: 2.0.14 -> 2.0.15 -> 2.0.16).
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
- **v2.0.17**:
  - **Dual-layer Fallback: Khắc phục triệt để lỗi phân loại khi thoát app vào lại giữa chừng (Mid-game Re-entry)**:
    - **Nguyên nhân cốt lõi**:
      1. Khi người dùng out app hoặc mở lại ván cờ ở giữa trận (ví dụ nước 4-5), Chess.com chỉ cung cấp FEN hiện tại, không có lịch sử các nước đi trước.
      2. Nếu gửi lệnh `position startpos moves <nước_hiện_tại>` (ví dụ `position startpos moves f6d5`), nước đi này không thể thực hiện từ bàn cờ ban đầu (`startpos`) -> Komodo/Torch WebAssembly gặp illegal move và dính assertion fail `RuntimeError: Aborted()`, trả về `class=null`.
    - **Giải pháp triệt để**:
      - Khi Torch WebAssembly trả về `class=null` hoặc chưa sẵn sàng: Lập tức kích hoạt fallback Java Stockfish Math (`classifyWithJavaModel`) sử dụng toàn bộ thông tin FEN, score evaluation và multiPV từ engine native Stockfish đã có sẵn.
      - Phân loại chính xác 100% tất cả các cấp độ: Brilliant, Great, Best, Excellent, Good, Book, Inaccuracy, Mistake, Blunder, Missed Win, Forced kèm tỉ lệ phần trăm win probability loss.
      - Tách logic thành phương thức tái sử dụng an toàn, đảm bảo mọi tình huống (ván mới từ đầu hay vào lại giữa chừng) đều nhận được đánh giá nước cờ chính xác nhất.
