# NHẬT KÝ DỰ ÁN & BỘ GHI NHỚ DẶN DÒ (PROJECT MEMORY)
*Dự án: Chess Mobile - ReVanced Extension Patches cho Chess.com Android*
*Phiên bản hiện tại: v2.0.13*
*Cập nhật lần cuối: 2026-10-07*

---

## 1. QUY TẮC LÀM VIỆC & DẶN DÒ CỐT LÕI
1. **Thái độ nghiêm túc tuyệt đối**:
   - Không được để xảy ra bất kỳ lỗi ngớ ngẩn nào (syntax error, escape error, crash, NPE, missing assets).
   - Kiểm tra kỹ lưỡng trước khi đóng gói và phát hành.
   - Không code bừa, không báo kết quả ảo, mọi tính năng đều phải chạy thực tế trên Android WebView và Runtime.
2. **Phong cách giao tiếp**:
   - Cực kỳ ngắn gọn, dứt khoát, telegraphic (ultra-terse).
   - Đi thẳng vào vấn đề: Nêu nguyên nhân, hành động đã thực hiện, kết quả, bước tiếp theo. Không văn hoa, không dài dòng.
3. **Hệ thống chẩn đoán lỗi toàn diện (Diagnostic Logging)**:
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
