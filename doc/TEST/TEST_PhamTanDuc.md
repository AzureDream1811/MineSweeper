# Kế hoạch kiểm thử (Test Plan) - v2

**Phiên bản:** 2.0 | **Ngày:** 10/05/2026

Tài liệu này mô tả các kịch bản kiểm thử được thực hiện, bao gồm cả các test tự động (JUnit) và các test thủ công/quan
sát.

---

## I. KIỂM THỬ CHỨC NĂNG (FUNCTIONAL)

### A. Test Tự Động (Automated Tests - JUnit)

Các test case này được viết bằng JUnit và nằm trong thư mục `src/test/java`. Chúng kiểm tra logic cốt lõi của ứng dụng
một cách tự động.

| STT | UC        | Thuộc tính / Kịch bản cần kiểm tra             | Kết quả mong đợi (Tiêu chuẩn)                                                        | Phương thức Test                                                                                                                 |
|:----|:----------|:-----------------------------------------------|:-------------------------------------------------------------------------------------|:---------------------------------------------------------------------------------------------------------------------------------|
| 1   | **UC-01** | Mở một ô có chứa mìn.                          | Trò chơi kết thúc (thua), hàm `revealCell` trả về `false`.                           | `BoardTest.testRevealCellWithMine_ShouldReturnFalse`                                                                             |
| 2   | **UC-01** | Mở một ô trống (0 mìn xung quanh).             | Kích hoạt thuật toán "flood fill", mở ra nhiều hơn 1 ô.                              | `BoardTest.testFloodFill_WhenBlankCellIsRevealed`                                                                                |
| 3   | **UC-02** | Đánh dấu cờ (flag) trên một ô chưa mở.         | Ô chuyển sang trạng thái `FLAGGED`.                                                  | `BoardTest.testToggleFlag_ShouldChangeCellStateAndFlagCount`                                                                     |
| 4   | **UC-03** | Hủy đánh dấu cờ trên một ô đã flag.            | Ô trở về trạng thái `HIDDEN`.                                                        | `BoardTest.testToggleFlag_ShouldChangeCellStateAndFlagCount`                                                                     |
| 5   | **UC-04** | Cố gắng mở (click chuột trái) một ô đã cắm cờ. | Ô không bị mở, trạng thái vẫn là `FLAGGED`.                                          | `BoardTest.testRevealFlaggedCell_ShouldDoNothing`                                                                                |
| 6   | **UC-05** | Mở một ô và kiểm tra số mìn xung quanh.        | Ô hiển thị con số chính xác bằng số mìn được đặt thủ công xung quanh nó.             | `BoardTest.testAdjacentMinesCount_IsCorrect`                                                                                     |
| 7   | **UC-06** | Thay đổi số lượng cờ đã cắm.                   | Bộ đếm cờ của `Board` tăng/giảm chính xác.                                           | `BoardTest.testToggleFlag_ShouldChangeCellStateAndFlagCount`                                                                     |
| 8   | **UC-07** | Bắt đầu một game mới khi thay đổi độ khó.      | Phương thức `newGame()` trong controller được gọi.                                   | `GameControllerTest.testSetDifficulty_ShouldStartNewGame`                                                                        |
| 9   | **UC-08** | Tạm dừng game đang chơi.                       | View được vô hiệu hóa (`setDisabled(true)`).                                         | `GameControllerTest.testPauseAndResume_ShouldToggleDisabledState`                                                                |
| 10  | **UC-09** | Tiếp tục game đang tạm dừng.                   | View được kích hoạt lại (`setDisabled(false)`).                                      | `GameControllerTest.testPauseAndResume_ShouldToggleDisabledState`                                                                |
| 11  | **UC-11** | Thắng một ván game.                            | Phương thức `checkWin()` trả về `true`. View hiển thị kết quả thắng được gọi.        | `BoardTest.testCheckWin_WhenAllSafeCellsAreRevealed_ShouldReturnTrue`<br>`GameControllerTest.testHandleWin_ShouldCallShowResult` |
| 12  | **UC-11** | Thua một ván game.                             | View hiển thị kết quả thua được gọi.                                                 | `GameControllerTest.testHandleLose_ShouldCallShowResult`                                                                         |
| 13  | **UC-12** | Bắt đầu và kết thúc game.                      | `timer.start()` được gọi khi click lần đầu, `timer.pause()` được gọi khi thắng/thua. | `GameControllerTest.testTimer_ShouldStartAndStop`                                                                                |
| 14  | **UC-14** | Nhấn phím F2 để chơi lại.                      | Phương thức `reset()` trong controller được gọi.                                     | `GameControllerTest.testResetOnF2KeyPress_ShouldCallReset`                                                                       |
| 15  | **UC-15** | Click chuột vào ô đầu tiên.                    | Ô được click và 8 ô xung quanh nó không bao giờ chứa mìn.                            | `BoardTest.testFirstClickIsSafe`                                                                                                 |

### B. Test Thủ Công (Manual Tests)

Các kịch bản này cần sự tương tác trực tiếp của người dùng để kiểm tra giao diện và trải nghiệm người dùng.

| STT | UC        | Thuộc tính / Kịch bản cần kiểm tra              | Kết quả mong đợi (Tiêu chuẩn)                                                                          |
|:----|:----------|:------------------------------------------------|:-------------------------------------------------------------------------------------------------------|
| 1   | **UC-05** | Quan sát giao diện sau khi mở ô.                | Số hiển thị trên ô (1-8) phải có màu sắc riêng biệt, dễ đọc.                                           |
| 2   | **UC-06** | Quan sát bộ đếm mìn trên giao diện.             | Con số trên bộ đếm giảm khi cắm cờ và tăng khi hủy cờ.                                                 |
| 3   | **UC-07** | Nhấn nút "New Game" hoặc chọn độ khó từ menu.   | Bàn cờ mới được tạo ra, đồng hồ reset về 0.                                                            |
| 4   | **UC-08** | Nhấn nút "Pause".                               | Bàn cờ bị mờ đi hoặc bị vô hiệu hóa, đồng hồ dừng chạy.                                                |
| 5   | **UC-09** | Nhấn nút "Resume" (hoặc nút Pause một lần nữa). | Bàn cờ hoạt động trở lại, đồng hồ tiếp tục chạy.                                                       |
| 6   | **UC-11** | Thắng hoặc thua một ván.                        | Một cửa sổ, icon hoặc thông báo rõ ràng hiện ra (ví dụ: mặt cười/mếu, thông báo "You Win"/"You Lose"). |
| 7   | **UC-12** | Quan sát đồng hồ.                               | Đồng hồ đứng yên ở 000, bắt đầu chạy khi click ô đầu tiên, và dừng lại khi game kết thúc.              |

---

## II. KIỂM THỬ PHI CHỨC NĂNG (NON-FUNCTIONAL)

Các yêu cầu này được kiểm tra bằng cách quan sát và sử dụng thực tế.

| STT | NFR        | Thuộc tính / Kịch bản cần kiểm tra                  | Kết quả mong đợi (Tiêu chuẩn)                                    |
|:----|:-----------|:----------------------------------------------------|:-----------------------------------------------------------------|
| 1   | **NFR-01** | Click vào một ô bất kỳ.                             | Phản hồi (mở ô, nổ mìn) gần như tức thì (< 100ms).               |
| 2   | **NFR-02** | Click vào ô trống ở góc bàn cờ lớn (Hard).          | Hệ thống xử lý mượt mà, không bị treo, lag.                      |
| 3   | **NFR-03** | Chạy file thực thi của ứng dụng.                    | Ứng dụng khởi động và hiển thị giao diện chính < 5 giây.         |
| 4   | **NFR-04** | Chơi lại nhiều lần ở cùng một cấp độ.               | Vị trí các quả mìn ở mỗi ván mới phải khác nhau, không lặp lại.  |
| 5   | **NFR-05** | Dùng đồng hồ bấm giờ bên ngoài để so sánh.          | Đồng hồ của game chạy chính xác, sai số không đáng kể.           |
| 6   | **NFR-06** | Mở các ô có số từ 1 đến 8.                          | Mỗi số có một màu riêng biệt, dễ phân biệt.                      |
| 7   | **NFR-09** | Chạy game trên các màn hình độ phân giải khác nhau. | Giao diện không bị vỡ, cắt xén hay hiển thị sai lệch.            |
| 8   | **NFR-10** | Sao chép ứng dụng sang máy tính khác (đã có JRE).   | Ứng dụng chạy được ngay mà không cần cài đặt.                    |
| 9   | **NFR-11** | Tìm và mở file lưu kỷ lục.                          | Nội dung file không phải là dạng text rõ ràng, khó sửa thủ công. |

