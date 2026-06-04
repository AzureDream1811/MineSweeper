package com.minesweeper.view;

import com.minesweeper.model.Board;
import com.minesweeper.model.Cell;
import javafx.geometry.Pos;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;

import java.util.function.BiConsumer;

/**
 * Hiển thị toàn bộ lưới ô dưới dạng {@link GridPane}.
 * <p>
 * BoardView chứa mảng 2D các {@link CellView} và chịu trách nhiệm:
 * <ul>
 *   <li>Xây dựng lưới theo kích thước bàn cờ</li>
 *   <li>Cập nhật từng ô khi model thay đổi</li>
 *   <li>Chuyển sự kiện chuột ra ngoài cho GameController xử lý</li>
 * </ul>
 * BoardView <b>không biết logic game</b> — nó chỉ expose event handler.
 * </p>
 */
public class BoardView {

    // ── Fields ────────────────────────────────────────────────

    /**
     * GridPane chứa toàn bộ CellView
     */
    private final GridPane grid;

    /**
     * Mảng 2D các CellView — ánh xạ 1-1 với Board.cells[][]
     */
    private CellView[][] cellViews;

    /**
     * Handler cho click chuột trái (mở ô).
     * Nhận (row, col) của ô được click — FR-08.
     */
    private BiConsumer<Integer, Integer> onLeftClick;

    /**
     * Handler cho click chuột phải (cắm/bỏ cờ).
     * Nhận (row, col) của ô được click — FR-09.
     */
    private BiConsumer<Integer, Integer> onRightClick;

    /**
     * Handler cho double-click chuột trái (Chording).
     * Nhận (row, col) của ô được click — FR-11.
     */
    private BiConsumer<Integer, Integer> onChord;

    // ── Constructor ───────────────────────────────────────────

    /**
     * Tạo BoardView với GridPane rỗng.
     * Gọi {@link #build(int, int)} để khởi tạo lưới.
     */
    public BoardView() {
        grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        // TODO: grid.setHgap(2); grid.setVgap(2)
        // TODO: grid.getStyleClass().add("board-grid")
    }

    // ── Build ─────────────────────────────────────────────────

    /**
     * Xây dựng lưới CellView với kích thước (rows × cols).
     * Xoá toàn bộ lưới cũ nếu có trước khi dựng lại.
     * Gọi khi bắt đầu ván mới hoặc đổi độ khó — FR-01, FR-05.
     *
     * @param rows số hàng
     * @param cols số cột
     */
    public void build(int rows, int cols) {
        // 1. Xoá hết mọi thứ cũ trong grid
        grid.getChildren().clear();

        // 2. Khởi tạo mảng lưu trữ
        cellViews = new CellView[rows][cols];

        // Tính toán kích thước ô tuỳ theo độ khó
        int cellSize = CellView.DEFAULT_CELL_SIZE;
        if (rows >= 24 || cols >= 24) cellSize = 18;
        else if (rows >= 16 || cols >= 16) cellSize = 24;

        // 3. Vòng lặp tạo từng ô
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                CellView cv = new CellView(row, col, cellSize); // Truyền cellSize

                // Gán sự kiện chuột
                attachMouseHandlers(cv);

                // Lưu vào mảng để truy xuất sau này
                cellViews[row][col] = cv;

                // Thêm vào GridPane
                grid.add(cv, col, row);
            }
        }
    }

    /**
     * Gán sự kiện chuột cho một CellView.
     * Tách ra method riêng để {@link #build(int, int)} gọn hơn.
     *
     * @param cv CellView cần gán event
     */
    private void attachMouseHandlers(CellView cv) {
        cv.setOnMouseClicked(e -> {
            MouseButton button = e.getButton();

            // Lấy vị trí ô được click để truyền sang Controller
            int row = cv.getRow();
            int col = cv.getCol();

            if (button == MouseButton.PRIMARY && e.getClickCount() == 2) {
                // Không thuộc UC-09, đây là UC-03 Chording
                if (onChord != null) onChord.accept(row, col);

            } else if (button == MouseButton.PRIMARY) {
                // UC-09 - 9.1.1:
                // Người chơi nhấp chuột trái vào một ô chưa được mở trên bàn chơi.
                // BoardView không xử lý logic game, chỉ chuyển tọa độ ô sang GameController.
                if (onLeftClick != null) onLeftClick.accept(row, col);

            } else if (button == MouseButton.SECONDARY) {
                // Không thuộc UC-09, đây là chức năng đặt/hủy cờ.
                if (onRightClick != null) onRightClick.accept(row, col);
            }
        });
    }

    // ── Update ────────────────────────────────────────────────

    /**
     * Cập nhật hiển thị một ô sau khi model thay đổi.
     * GameController gọi method này sau mỗi thao tác.
     *
     * @param row  hàng
     * @param col  cột
     * @param cell trạng thái model mới của ô
     */
    public void updateCell(int row, int col, Cell cell) {
        // UC-09 - 9.1.9:
        // Nhận trạng thái mới của Cell từ Model và render lại ô tương ứng trên giao diện.
        cellViews[row][col].render(cell);
    }

    /**
     * Lật ngửa toàn bộ mìn — gọi khi người chơi thua.
     * Phân biệt ô mìn bình thường và ô mìn vừa nổ (exploded).
     * FR-17
     *
     * @param explodedRow hàng của ô mìn vừa nổ (-1 nếu không có)
     * @param explodedCol cột của ô mìn vừa nổ (-1 nếu không có)
     */
    public void revealAllMines(Board board, int explodedRow, int explodedCol) {
        // UC-17 - 17.1.5:
        // Hệ thống hiển thị các ô chứa mìn trên bàn chơi.
        //
        // View duyệt qua toàn bộ CellView, lấy Cell tương ứng từ Board
        // và render các ô có chứa mìn.
        for (int r = 0; r < cellViews.length; r++) {
            for (int c = 0; c < cellViews[0].length; c++) {
                Cell cell = board.getCell(r, c);

                if (cell.isMine()) {
                    boolean exploded = (r == explodedRow && c == explodedCol);

                    // Render ô mìn bình thường.
                    cellViews[r][c].render(cell);

                    // UC-17 - 17.1.6:
                    // Hệ thống làm nổi bật ô mìn mà người chơi vừa mở.
                    //
                    // Nếu đây là ô gây nổ, ép hiển thị mine với trạng thái exploded.
                    if (exploded) {
                        cellViews[r][c].showMine(true);
                    }
                }
            }
        }
    }

    /**
     * Đặt lại toàn bộ ô về trạng thái HIDDEN.
     * FR-02
     */
    public void resetAll() {
        // TODO: duyệt cellViews → cv.reset()
    }

    /**
     * Vô hiệu hóa hoặc kích hoạt lại toàn bộ bàn cờ.
     * Gọi khi game kết thúc (FR-19) hoặc tạm dừng (FR-03).
     *
     * @param disabled true để vô hiệu hóa
     */
    public void setDisabled(boolean disabled) {
        // TODO: grid.setDisable(disabled)
    }

    // ── Event Handler Setters ─────────────────────────────────

    /**
     * Đăng ký handler cho click chuột trái (mở ô).
     * GameController truyền vào lambda của mình — FR-08.
     *
     * @param handler nhận (row, col)
     */
    public void setOnLeftClick(BiConsumer<Integer, Integer> handler) {
        this.onLeftClick = handler;

    }

    /**
     * Đăng ký handler cho click chuột phải (cắm/bỏ cờ).
     * FR-09
     *
     * @param handler nhận (row, col)
     */
    public void setOnRightClick(BiConsumer<Integer, Integer> handler) {
        this.onRightClick = handler;
    }

    /**
     * Đăng ký handler cho double-click (Chording).
     * FR-11
     *
     * @param handler nhận (row, col)
     */
    public void setOnChord(BiConsumer<Integer, Integer> handler) {
        this.onChord = handler;
    }

    // ── Getter ────────────────────────────────────────────────

    /**
     * Trả về GridPane để MainView đặt vào layout.
     *
     * @return GridPane của bàn cờ
     */
    public GridPane getGrid() {
        return grid;
    }
}
