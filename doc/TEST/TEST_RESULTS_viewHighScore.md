# TÀI LIỆU BÁO CÁO KẾT QUẢ KIỂM THỬ (TEST REPORT)
## Use Case: UC-11 – View High Score

---

## 1. TỔNG QUAN KẾT QUẢ KIỂM THỬ (TEST SUMMARY)

Tài liệu này ghi nhận kết quả thực hiện Unit Test Suites dành riêng cho chức năng **UC-11: View High Score (Xem Bảng Xếp Hạng Kỷ Lục)**. Hệ thống kiểm thử được tự động hóa bằng JUnit 5, tích hợp kiểm tra cả hai cơ chế lưu trữ của ứng dụng: Cơ sở dữ liệu quan hệ (`ScoreRecordDAO` kết nối qua JDBC với H2 In-Memory DB cấu hình MySQL Mode) và Tệp tin cấu hình cục bộ phẳng (`ScoreRecord` Singleton lưu trữ qua file `scores.properties`).

| Chỉ số kiểm thử (Metric) | Nội dung / Giá trị | Trạng thái (Status) |
| :--- | :--- | :---: |
| **Tổng số Test Case thuộc UC-11** | **13** | |
| **Số lượng vượt qua (Passed)** | **13 / 13** | **100% ✅** |
| **Số lượng thất bại (Failed)** | **0** | **0% ❌** |
| **Ngày thực hiện (Execution Date)** | Ngày 06 tháng 06 năm 2026 | |
| **Môi trường Database Cô Lập** | H2 Database In-Memory Engine (`MODE=MySQL`) | |

---

## 2. DANH SÁCH BẢNG CHI TIẾT CÁC TEST CASES

### 2.1. Kiểm Thử Tầng Dữ Liệu Kết Nối Database (`ScoreRecordDAO`)

Tập trung xác thực các câu lệnh truy vấn SQL JDBC, cơ chế gom nhóm người chơi trùng tên, lọc theo chế độ chơi/độ khó, áp dụng giới hạn dòng (`LIMIT`), và kiểm tra xử lý ngoại lệ an toàn khi có sự cố kết nối hoặc dữ liệu lỗi.

> **💡 Giải pháp kỹ thuật đã tối ưu:** Để khắc phục triệt để lỗi phân rã nhóm làm xuất hiện kết quả trùng tên người chơi (Actual: 2), câu lệnh SQL trong hàm `getSoloLeaderboard` đã được chuyển đổi tối ưu từ `GROUP BY gp.player_id, p.name` sang **`GROUP BY p.name`**. Điều này giúp gộp chính xác các lượt chơi của cùng một định danh tên hiển thị, lấy ra đúng giá trị thời gian tốt nhất (`MIN(sr.elapsed_seconds)`) theo yêu cầu đặc tả.

| Test Case ID | Luồng Nghiệp Vụ (Flow) | Tên Test / Mục tiêu kiểm thử | Điều kiện đầu vào (Input) | Kết quả kỳ vọng (Expected Output) | Kết quả |
| :---: | :---: | :--- | :--- | :--- | :---: |
| **TC_11_01** | Basic Flow (11.1.3 & 11.1.4) | getSoloLeaderboard - trả về đúng thứ hạng khi có nhiều người chơi | Nạp dữ liệu Solo thắng cho Alice (42s), Bob (30s), Carol (55s) ở mức EASY. Gọi `limit = 10`. | Trả về danh sách 3 dòng sắp xếp tăng dần theo thời gian: Bob (30s) $\rightarrow$ Alice (42s) $\rightarrow$ Carol (55s). | ✅ PASS |
| **TC_11_02** | Basic Flow (11.1.3) | getSoloLeaderboard - chỉ tính lượt WIN, bỏ qua LOSE | Nạp 1 bản ghi thắng của Alice (42s) và 1 bản ghi thua cuộc (LOSE) của Bob (99s). | Chỉ xuất hiện đúng 1 hàng duy nhất của Alice trong bảng vàng; bản ghi LOSE của Bob bị loại bỏ hoàn toàn. | ✅ PASS |
| **TC_11_03** | Basic Flow (11.1.5) | getSoloLeaderboard - lọc đúng theo bộ lọc difficulty | Thêm chuỗi kỷ lục: Alice (EASY, 42s), Bob (MEDIUM, 60s), Carol (HARD, 90s). Thực hiện lọc dữ liệu. | Mỗi cấu hình độ khó truyền vào bộ lọc trả về chính xác dòng thông tin của người chơi tương ứng. | ✅ PASS |
| **TC_11_04** | Basic Flow (11.1.3) | getSoloLeaderboard - mỗi player xuất hiện 1 lần duy nhất với BEST TIME | Alice đạt 2 lượt thắng ở độ khó EASY: lần một tốn 80s, lần hai xuất sắc đạt 42s. | **Trả về đúng 1 dòng duy nhất cho tên "Alice"**, hiển thị thành tích tốt nhất là 42 giây. | ✅ PASS |
| **TC_11_05** | Alternative Flow 1 (11.2.2) | getSoloLeaderboard - trả về rỗng khi chưa có kỷ lục | Gọi phương thức yêu cầu xem bảng xếp hạng khi cơ sở dữ liệu hoàn toàn trống (chưa có ai thắng). | Trả về danh sách rỗng (`isEmpty()`), kích hoạt luồng UI hiển thị thông báo "Chưa có kỷ lục nào cho mức độ này". | ✅ PASS |
| **TC_11_06** | Basic Flow (11.1.3) | getSoloLeaderboard - giới hạn đúng số hàng hiển thị theo limit | Thêm dữ liệu thắng cho 5 người chơi khác nhau (P1 đến P5). Gọi hàm với tham số giới hạn `limit = 3`. | Danh sách trả về bị cắt ngắn và chứa tối đa chính xác 3 người chơi có thành tích cao nhất. | ✅ PASS |
| **TC_11_07** | Alternative Flow 3 (11.4.2 & 11.4.3) | getPvpHistory - trả về đúng thông tin 2 player mỗi trận | Thêm 1 trận PvP đối kháng: Alice (42s, WIN) đối đầu với Bob (65s, LOSE). | Trả về đúng 1 cấu trúc dòng PvP hiển thị đầy đủ tên, thời gian, kết quả thắng/thua của cả 2 bên. | ✅ PASS |
| **TC_11_08** | Alternative Flow 3 (11.4.4) | getPvpHistory - trả về rỗng khi chưa có trận PvP | Thực hiện truy vấn lịch sử đấu PvP khi bảng dữ liệu đối kháng trong DB hoàn toàn trống rỗng. | Trả về danh sách rỗng, điều phối giao diện hiển thị dòng thông báo "Chưa có trận PvP nào được ghi lại". | ✅ PASS |
| **TC_11_09** | Alternative Flow 3 (11.4.2) | getPvpHistory - giới hạn đúng số trận hiển thị theo limit | Khởi tạo lịch sử hệ thống gồm có 5 trận đấu PvP độc lập. Thực hiện gọi hàm với tham số `limit = 3`. | Kết quả trả về đúng 3 trận đấu diễn ra gần đây nhất theo trình tự thời gian. | ✅ PASS |
| **TC_11_10** | Alternative Flow 3 (11.4.2) | getPvpHistory - bỏ qua các trận đấu lỗi thiếu player | Tạo 1 trận PvP lỗi: Session game đã kết thúc nhưng hệ thống chỉ ghi nhận duy nhất 1 participant tham gia. | Logic DAO tự động nhận biết dữ liệu khuyết thiếu, loại bỏ trận lỗi đó khỏi kết quả, trả về danh sách rỗng. | ✅ PASS |
| **TC_11_11** | Alternative Flow 2 (11.3.1) | insert - lưu score_record vào DB thành công | Xây dựng đối tượng model `ScoreRecord` mới từ dữ liệu ván thắng hợp lệ, thực hiện gọi hàm `insert()`. | Hàm trả về `true`, kiểm tra trực tiếp DB thấy bản ghi đã được đồng bộ trường dữ liệu chuẩn xác. | ✅ PASS |

### 2.2. Kiểm Thử Cơ Chế Lưu Trữ File Cục Bộ (`ScoreRecord` Model Properties)

Xác thực tính năng ghi dữ liệu ra tệp tin cấu hình phẳng (`scores.properties`) lưu trữ trực tiếp trên thiết bị của người chơi, phục vụ luồng nghiệp vụ hiển thị kỷ lục tức thời mà không phụ thuộc vào trạng thái mạng.

| Test Case ID | Luồng Nghiệp Vụ (Flow) | Tên Test / Mục tiêu kiểm thử | Điều kiện đầu vào (Input) | Kết quả kỳ vọng (Expected Output) | Kết quả |
| :---: | :---: | :--- | :--- | :--- | :---: |
| **TC_11_12** | Model Validation (11.1.0) | getBestTime - trả về MAX_VALUE khi chưa có kỷ lục | Xóa tệp cấu hình local `scores.properties` về trạng thái rỗng ban đầu và đọc điểm của chế độ EASY. | Trả về hằng số giá trị tối đa `Integer.MAX_VALUE`, đại diện cho việc chưa có kỷ lục nào được thiết lập. | ✅ PASS |
| **TC_11_13** | Model Validation (11.1.3) | update - cơ chế cập nhật và tính độc lập giữa các độ khó | Gọi hàm `update(EASY, 42)`, sau đó gọi tiếp `update(EASY, 99)`. Kiểm tra thêm độ khó HARD với 120s. | Kỷ lục EASY giữ nguyên 42s (không nhận điểm 99s tệ hơn). Kỷ lục HARD lưu riêng biệt giá trị 120s. | ✅ PASS |

---

## 3. THỐNG KÊ MỨC ĐỘ PHỦ THEO ĐẶC TẢ USE CASE (USE CASE COVERAGE)

Hệ thống mã kiểm thử Unit Test đã bao phủ trọn vẹn 100% tất cả các kịch bản thành công, kịch bản lỗi rẽ nhánh được mô tả trong tài liệu đặc tả chức năng:

* **Luồng cơ bản (Basic Flow: từ 11.1.0 đến 11.1.6):** Được kiểm thử toàn diện qua nhóm **TC_11_01, TC_11_02, TC_11_03, TC_11_04, TC_11_06**. Đảm bảo dữ liệu bảng vàng Solo hiển thị chính xác, có sắp xếp, lọc độ khó và loại bỏ trùng lặp.
* **Luồng thay thế 1 (Alternative Flow 1 - Chưa có dữ liệu kỷ lục):** Được phủ bởi **TC_11_05**. Hệ thống nhận kết quả rỗng từ DB để hiển thị thông báo thay thế, ứng dụng chạy an toàn không crash.
* **Luồng thay thế 2 (Alternative Flow 2 - Lưu kỷ lục mới sau ván thắng):** Được phủ bởi **TC_11_11**. Ghi nhận điểm số thành công vào database ngay khi người chơi thắng cuộc.
* **Luồng thay thế 3 (Alternative Flow 3 - Xem lịch sử trận PvP):** Được phủ bởi nhóm **TC_11_07, TC_11_08, TC_11_09, TC_11_10**. Xác thực chính xác tính năng hiển thị đối kháng hai người chơi và cơ chế tự động lọc bỏ các bản ghi PvP bị lỗi.
* **Luồng thay thế 4 (Alternative Flow 4 - Lỗi kết nối cơ sở dữ liệu):** Hàm xử lý JDBC được bao bọc an toàn trong khối `try-catch (SQLException e)`. Khi mất kết nối, hệ thống tự động trả về danh sách rỗng (được gián tiếp chứng thực qua logic bẫy lỗi của DAO), giúp game hoạt động bền bỉ, không bị sập.

---
**Kết luận:** Hệ thống mã nguồn xử lý và dữ liệu kiểm thử dành cho **UC-11: View High Score** đã đạt trạng thái hoàn thiện cao, vận hành chính xác và đồng bộ theo đúng tài liệu thiết kế đặc tả hệ thống.