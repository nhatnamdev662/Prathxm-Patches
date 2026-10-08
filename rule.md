# QUY TẮC PHÁT TRIỂN & YÊU CẦU DỰ ÁN (RULE.MD)
*Dự án: Chess Mobile (NNVC Patches cho Chess.com Android)*
*Tác giả: NNVC (@nncutett)*
*Cập nhật lần cuối: 2026-10-08*

---

## 1. QUY TẮC PHIÊN BẢN & REPOSITORY (BẮT BUỘC SỐNG CÒN)
1. **Tuyệt đối không trùng phiên bản**:
   - Mỗi lần sửa lỗi hoặc build gói mới, **BẮT BUỘC TĂNG PHIÊN BẢN LÊN** (ví dụ: `2.0.35` -> `2.0.36` -> `2.0.37`...).
   - Morphe Manager sẽ bị dính cache cũ nếu dùng lại version đã từng phát hành.
   - Cập nhật đồng bộ: `patches-bundle.json`, `patches-list.json`, `PROJECT_MEMORY.md`, `SESSION_NOTES.md`, và GitHub Release tag.
2. **Repository sạch sẽ & Không commit file rác**:
   - Tuyệt đối không commit file `.mpp`, `.py` build script, file `.html`, hay file tạm lên Git.
   - Các file phân tích, build cũ phải chuyển vào `scripts_archive/` và `mpp_archive/` (đã có trong `.gitignore`).
   - Thư mục gốc project luôn giữ gọn gàng, ngăn nắp.

---

## 2. QUY TẮC ĐỒNG BỘ NGÔN NGỮ (I18N) 100%
1. **Tất cả chức năng phải đồng bộ ngôn ngữ**:
   - Mọi chuỗi văn bản trên giao diện **BẮT BUỘC** gọi qua `I18n.get(context, key)` và đọc `StockfishSettings.getLanguage(context)`.
   - Hỗ trợ đầy đủ song ngữ Anh (`en`) và Việt (`vi`).
2. **Các thành phần bắt buộc chuyển ngữ tức thì**:
   - **Menu Cài đặt (5 Tabs)**: Tab headers, Card titles, Switch labels, Subtitles, Tooltips, Seekbars, Dialogs, Toasts.
   - **Thẻ Accuracy & Elo (PlayerAccuracyPillView)**:
     - `vi`: `[BẠN]` / `[ĐỐI THỦ]`, `CHÍNH XÁC`, `ELO ĐÁNH GIÁ`.
     - `en`: `[YOU]` / `[OPPONENT]`, `ACCURACY`, `EST. ELO`.
   - **Phân loại nước đi (MoveClassifier)**:
     - `vi`: `Nước cờ thiên tài (Brilliant)`, `Nước cờ xuất sắc (Great)`, `Nước cờ tốt nhất (Best)`...
     - `en`: `Brilliant Move`, `Great Move`, `Best Move`...
   - **Hộp thoại Nhật ký (Diagnostic Log Dialog)**: Header, Status badge, Subtitle counter, Nút hành động (`⬆ Top`, `⬇ Bottom`, `🗑️ Clear`, `📋 Copy`, `Close`), Toasts.

---

## 3. KIẾN TRÚC HUẤN LUYỆN VIÊN (COACH) & ENGINE
1. **Coach phụ thuộc 100% vào Torch WASM (Komodo CEE 26MB)**:
   - Phân loại nước đi (Brilliant, Great, Best, Good, Book, Inaccuracy, Mistake, Blunder...) là nhiệm vụ độc quyền của Torch CEE WebAssembly.
   - Tuyệt đối không tự ý fallback sang Stockfish ReviewMath.
   - Độ sâu Torch mặc định = 2 (tối ưu tốc độ, khớp Extension).
2. **Cơ chế Triệt Tiêu Hàng Chờ Dồn Lệnh (Supersede Cancellation Token)**:
   - Dùng `currentRequestId` (AtomicInteger). Khi có nước cờ mới dồn dập, tự động hủy bỏ request cũ và chỉ xử lý nước cờ mới nhất.
3. **Engine Phân Tích (Stockfish UCI)**:
   - Phục vụ mũi tên gợi ý Best Moves, Threat Arrows, Eval Bar và WDL Bar.

---

## 4. QUY CÁCH WIDGET ACCURACY & ESTIMATED ELO (PLAYERACCURACYPILLVIEW)
1. **Thiết kế & Vị trí (Cách C)**:
   - Ghim khít mép trên và mép dưới bàn cờ (`topY = boardY - pillH`, `botY = boardY + boardH`), 0 margin, ăn liền vào viền bàn cờ.
   - Không để khoảng hở lơ lửng tạo cảm giác dán đè.
2. **Kích thước tự động (Không cắt chữ)**:
   - Sử dụng `calculateDesiredWidth()` đo lường chính xác pixel của văn bản.
   - Chiều rộng tối thiểu `232dp`, căn giữa theo chiều ngang bàn cờ (`pillX = boardX + (boardW - pillW) / 2`).
   - Cấm tuyệt đối hiện tượng cắt chữ như `ELO ĐÁNH G`.
3. **Nhận diện đúng màu quân**:
   - Phe người chơi hiển thị nhãn `[BẠN]` (hoặc `[YOU]`) kèm viền Cyber Blue phát sáng.
   - Phe đối thủ hiển thị nhãn `[ĐỐI THỦ]` (hoặc `[OPPONENT]`) viền xám mờ.
   - Tự động dọn dẹp khi hết ván hoặc tắt engine.

---

## 5. CÁC TÍNH NĂNG ĐÃ GỠ BỎ / CẤM TỰ ĐỘNG BẬT
1. **Thông báo chiếu hết (Mate Announcement)**:
   - ĐÃ XÓA VĨNH VIỄN banner thông báo chiếu hết (`MATE IN X!`).
   - `showMateAnnouncement()` luôn gọi `hideMateAnnouncement()`.
   - Đã gỡ bỏ tùy chọn switch tương ứng trong menu cài đặt.
2. **Overlay hướng dẫn (StockfishTourOverlay)**:
   - VÔ HIỆU HÓA TRIỆT ĐỂ, không bao giờ tự động hiện popup làm tối màn hình trận đấu.

---

## 6. QUY TẮC CÀI ĐẶT REAL-TIME & GIAO DIỆN EXTENSION
1. **Tự động áp dụng & Lưu tức thì (Auto-apply Real-time)**:
   - Thay đổi bất kỳ toggle, seekbar, engine choice, style, palette màu trong menu cài đặt -> Tự động lưu SharedPreferences và cập nhật ngay lập tức bàn cờ trực tiếp, không bắt người dùng phải bấm nút Lưu.
   - Khi đổi ngôn ngữ [VI]/[EN], menu và các overlay trên bàn cờ (Pills, Arrows, Eval Bar) lập tức chuyển đổi song ngữ thời gian thực.
2. **Nút đóng Menu phong cách Extension**:
   - Sử dụng nút đóng biểu tượng dấu `✕` kiểu Extension ở góc trên bên phải header menu.
   - Bỏ hoàn toàn nút `Hủy` (Cancel) thừa thãi ở footer.

---

## 7. PHONG CÁCH LÀM VIỆC & GIAO TIẾP
1. **Phong cách giao tiếp**:
   - Cực kỳ ngắn gọn, telegraphic (ultra-terse).
   - Đi thẳng vào kết quả: Nêu nguyên nhân, việc đã làm, bước tiếp theo. Không dài dòng, không văn hoa.
   - Trả lời bằng tiếng Việt.
2. **Thái độ nghiêm túc tuyệt đối**:
   - Kiểm tra kỹ cú pháp, biên dịch thực tế trước khi phát hành gói.
   - Không code ẩu, không báo cáo kết quả ảo.

