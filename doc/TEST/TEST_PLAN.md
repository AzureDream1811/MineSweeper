# Test Plan — Sinh tự động từ thư mục `src/test/java`

Tài liệu này tóm tắt các ca kiểm thử hiện có trong thư mục `src/test/java` và mô tả mục đích, kịch bản và kết quả mong đợi của chúng. File này được tạo dựa trên nội dung của hai lớp kiểm thử:

- `com.minesweeper.model.BoardTest`
- `com.minesweeper.controller.GameControllerTest`

Môi trường thực thi: Java 21, Maven, JUnit 5, Mockito. Chạy toàn bộ kiểm thử:

```bash
mvn test
```

---

## 1. Mục tiêu chung

- Xác thực logic model (`Board`) — đặt mìn, mở ô, đánh dấu cờ, flood-fill, điều kiện thắng/thua.
- Xác thực hành vi điều khiển (`GameController`) khi tương tác với `MainView`/`BoardView`/`HeaderView` (sử dụng mocks). Kiểm tra: pause/resume, reset (F2), đổi độ khó, xử lý click trái/phải.

---

## 2. Tóm tắt ca kiểm thử (theo file)

### `BoardTest` (6 ca)

- testFirstClickIsSafe
  - Mục đích: Sau khi gọi `placeMines(safeRow,safeCol)` ô click đầu và 8 ô xung quanh không chứa mìn.
  - Kết quả mong đợi: Tất cả ô liên quan `isMine()` = false.

- testRevealCellWithMine_ShouldReturnFalse
  - Mục đích: Mở một ô có mìn trả về false và ô đó được đánh dấu là revealed.
  - Kết quả mong đợi: `revealCell(...)` trả về false; ô có `isRevealed()` = true.

- testToggleFlag_ShouldChangeCellStateAndFlagCount
  - Mục đích: Đánh dấu / gỡ cờ thay đổi trạng thái ô và bộ đếm cờ.
  - Kết quả mong đợi: `isFlagged()` cập nhật; `getFlagCount()` tăng/giảm tương ứng.

- testRevealFlaggedCell_ShouldDoNothing
  - Mục đích: Không thể mở ô đã cắm cờ.
  - Kết quả mong đợi: Ô vẫn flagged và không revealed.

- testCheckWin_WhenAllSafeCellsRevealed_ShouldReturnTrue
  - Mục đích: Khi tất cả ô an toàn được reveal, `checkWin()` trả về true.
  - Kết quả mong đợi: `checkWin()` = true.

- testFloodFill_WhenBlankCellRevealed_ShouldRevealMultipleCells
  - Mục đích: Kiểm tra behavior flood-fill khi reveal ô trống (blank).
  - Kết quả mong đợi: Số ô revealed > 1.

### `GameControllerTest` (10 ca)

- Các kiểm thử chính (mô tả ngắn):
  - `testPause_WhenPlaying_ShouldDisableView`: Khi đang chơi, pause sẽ disable view.
  - `testPause_WhenIdle_ShouldDoNothing`: Pause khi idle không gọi setDisabled(true).
  - `testResume_WhenPaused_ShouldEnableView`: Resume khi đang pause sẽ enable view.
  - `testResume_WhenNotPaused_ShouldDoNothing`: Resume khi không pause không gọi setDisabled(false).
  - `testOnKeyPressed_F2_ShouldCallReset`: Nhấn F2 sẽ gọi reset/newGame (BoardView.build được gọi).
  - `testOnKeyPressed_OtherKey_ShouldNotReset`: Phím khác không gọi reset.
  - `testSetDifficulty_Medium_ShouldStartNewGame`: Đổi độ khó (MEDIUM) gọi build cho board view.
  - `testSetDifficulty_Hard_ShouldStartNewGame`: Đổi độ khó (HARD) gọi build cho board view.
  - `testOnRightClick_WhenPlaying_ShouldUpdateCell`: Right-click khi đang chơi cập nhật ô.
  - `testOnRightClick_WhenIdle_ShouldDoNothing`: Right-click khi idle không cập nhật ô.

  - Các test này sử dụng `Mockito` để mock các view component; JavaFX toolkit được khởi động trong `@BeforeAll` và một `Scene` được cung cấp trong `@BeforeEach` để tránh NPE liên quan tới Stage/Scene.

---

## 3. Mapping test → yêu cầu chức năng (ngắn)

- UC-01 (Mở ô, flood-fill): Covered by `BoardTest.testRevealCellWithMine_*` và `testFloodFill_*`.
- UC-02 / UC-03 / UC-04 (Đánh dấu cờ / hủy cờ / cấm mở ô cắm cờ): Covered by `BoardTest.testToggleFlag_*` và `testRevealFlaggedCell_*`.
- UC-07 / UC-08 / UC-09 / UC-14 (New game, Pause, Resume, F2): Covered by `GameControllerTest` cases.
- UC-11 (Win condition): Covered by `BoardTest.testCheckWin_*`.

---

## 4. Hướng dẫn chạy và mở rộng

- Chạy toàn bộ test:

```bash
mvn test
```

- Chạy một lớp test riêng:

```bash
mvn -Dtest=com.minesweeper.controller.GameControllerTest test
```

- Ghi chú khi mở rộng test:
  - Nếu thêm test tương tác UI JavaFX, khởi động toolkit (Platform.startup) hoặc dùng TestFX; tránh tạo `Stage` ngoài FX thread.
  - Giữ các mock `lenient()` nếu stub dùng để tránh `UnnecessaryStubbingException` (hoặc cấu hình Mockito strictness).

---

## 5. Ghi chú nhỏ

- File này được sinh lại từ mã kiểm thử hiện tại; nếu bạn thêm/bớt test, hãy chạy lại `mvn test` và cập nhật kế hoạch nếu cần.
