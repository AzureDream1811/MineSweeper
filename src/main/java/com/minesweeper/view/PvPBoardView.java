package com.minesweeper.view;

import com.minesweeper.model.Board;
import com.minesweeper.model.Cell;
import javafx.beans.property.IntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * [UC5.4.4] Màn hình chơi PvP chia đôi gồm hai khu vực board độc lập.
 * Mỗi bên có: tên người chơi, đồng hồ, số mìn còn lại, và lưới bàn cờ.
 * Con trỏ bàn phím hiển thị bằng highlight ô đang chọn.
 */
public class PvPBoardView {

    // Layout gốc chứa cả hai nửa màn hình
    private final HBox root;

    // ── Bên trái (Player 1) ──────────────────────────────────
    private final Label lblP1Name;
    private final Label lblP1Timer;
    private final Label lblP1Mines;
    private final GridPane gridP1;      // Lưới bàn cờ Player 1
    private CellView[][] cellsP1;       // Mảng CellView Player 1

    // ── Bên phải (Player 2) ─────────────────────────────────
    private final Label lblP2Name;
    private final Label lblP2Timer;
    private final Label lblP2Mines;
    private final GridPane gridP2;      // Lưới bàn cờ Player 2
    private CellView[][] cellsP2;       // Mảng CellView Player 2

    // ── Vị trí con trỏ bàn phím của mỗi người chơi ─────────
    private int cursorP1Row = 0, cursorP1Col = 0; // Con trỏ Player 1
    private int cursorP2Row = 0, cursorP2Col = 0; // Con trỏ Player 2

    // Style highlight con trỏ (viền vàng nổi bật)
    private static final String CURSOR_STYLE =
        "-fx-border-color: #FFD700; -fx-border-width: 2; -fx-border-style: solid;";

    public PvPBoardView(String player1Name, String player2Name) {
        // ── Header Player 1 ──────────────────────────────────
        lblP1Name  = new Label(player1Name);
        lblP1Name.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        lblP1Timer = new Label("⏱ 000");
        lblP1Mines = new Label("💣 000");
        gridP1     = new GridPane();
        gridP1.setAlignment(Pos.CENTER);

        VBox panelP1 = buildPlayerPanel(lblP1Name, lblP1Timer, lblP1Mines, gridP1,
                                        "W A S D  |  Space=Mở  F=Cờ");

        // ── Header Player 2 ──────────────────────────────────
        lblP2Name  = new Label(player2Name);
        lblP2Name.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        lblP2Timer = new Label("⏱ 000");
        lblP2Mines = new Label("💣 000");
        gridP2     = new GridPane();
        gridP2.setAlignment(Pos.CENTER);

        VBox panelP2 = buildPlayerPanel(lblP2Name, lblP2Timer, lblP2Mines, gridP2,
                                        "↑ ↓ ← →  |  Enter=Mở  P=Cờ");

        // Đường kẻ dọc phân tách hai bên
        Rectangle divider = new Rectangle(3, 1);
        divider.setFill(Color.GRAY);
        divider.heightProperty().bind(panelP1.heightProperty());

        // [UC5.4.4] Bố cục chia đôi màn hình
        root = new HBox(10, panelP1, divider, panelP2);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(10));
    }

    /** Tạo VBox chứa toàn bộ UI cho một bên người chơi */
    private VBox buildPlayerPanel(Label name, Label timer, Label mines,
                                   GridPane grid, String keysHint) {
        Label hintLabel = new Label(keysHint);
        hintLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #888;");

        HBox header = new HBox(12, name, timer, mines);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(4, 8, 4, 8));
        header.setStyle("-fx-background-color: #ddd; -fx-background-radius: 4;");

        VBox panel = new VBox(6, header, hintLabel, grid);
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setPadding(new Insets(8));
        panel.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 6;");
        HBox.setHgrow(panel, Priority.ALWAYS); // Cho phép Panel kéo giãn
        return panel;
    }

    // ── Build lưới bàn cờ ────────────────────────────────────

    /**
     * [UC5.4.4] Khởi tạo lưới CellView cho cả hai bên từ cùng một kích thước Board.
     * Không gán mouse handler vì PvP điều khiển bằng bàn phím.
     */
    public void buildBoards(int rows, int cols) {
        // Thu nhỏ ô nếu bàn cờ quá lớn
        int cellSize = CellView.DEFAULT_CELL_SIZE;
        if (rows >= 24 || cols >= 16) cellSize = 18;

        cellsP1 = buildGrid(gridP1, rows, cols, cellSize); // Xây lưới Player 1
        cellsP2 = buildGrid(gridP2, rows, cols, cellSize); // Xây lưới Player 2

        // Đặt con trỏ về góc trên trái
        cursorP1Row = 0; cursorP1Col = 0;
        cursorP2Row = 0; cursorP2Col = 0;

        // Highlight vị trí ban đầu của cursor
        highlightCursor(cellsP1, cursorP1Row, cursorP1Col);
        highlightCursor(cellsP2, cursorP2Row, cursorP2Col);
    }

    /** Xây lưới CellView vào GridPane, trả về mảng CellView */
    private CellView[][] buildGrid(GridPane grid, int rows, int cols, int cellSize) {
        grid.getChildren().clear();
        CellView[][] cells = new CellView[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                CellView cv = new CellView(r, c, cellSize);
                cells[r][c] = cv;
                grid.add(cv, c, r);
            }
        }
        return cells;
    }

    // ── Cập nhật ô theo model ────────────────────────────────

    /** Cập nhật một ô trên board Player 1 */
    public void updateCellP1(int row, int col, Cell cell) {
        cellsP1[row][col].render(cell);
        // Re-apply cursor highlight nếu ô này đang là cursor (render có thể xóa style)
        if (row == cursorP1Row && col == cursorP1Col)
            highlightCursor(cellsP1, row, col);
    }

    /** Cập nhật một ô trên board Player 2 */
    public void updateCellP2(int row, int col, Cell cell) {
        cellsP2[row][col].render(cell);
        if (row == cursorP2Row && col == cursorP2Col)
            highlightCursor(cellsP2, row, col);
    }

    /** Lật ngửa toàn bộ mìn của Player 1 khi thua */
    public void revealAllMinesP1(Board board, int explodedRow, int explodedCol) {
        revealAllMines(board, cellsP1, explodedRow, explodedCol);
    }

    /** Lật ngửa toàn bộ mìn của Player 2 khi thua */
    public void revealAllMinesP2(Board board, int explodedRow, int explodedCol) {
        revealAllMines(board, cellsP2, explodedRow, explodedCol);
    }

    private void revealAllMines(Board board, CellView[][] cells, int exR, int exC) {
        for (int r = 0; r < cells.length; r++) {
            for (int c = 0; c < cells[0].length; c++) {
                Cell cell = board.getCell(r, c);
                if (cell.isMine()) {
                    cells[r][c].render(cell);
                    if (r == exR && c == exC) cells[r][c].showMine(true); // Mìn nổ = đỏ
                }
            }
        }
    }

    // ── Di chuyển con trỏ bàn phím ─────────────────────────

    /**
     * [UC5.4.6] Di chuyển con trỏ Player 1 (W/A/S/D).
     * @return int[]{row, col} vị trí con trỏ mới
     */
    public int[] moveP1Cursor(int dRow, int dCol) {
        int rows = cellsP1.length, cols = cellsP1[0].length;
        // Xoá highlight vị trí cũ
        cellsP1[cursorP1Row][cursorP1Col].setStyle("");
        // Tính vị trí mới, giới hạn trong phạm vi board
        cursorP1Row = Math.max(0, Math.min(rows - 1, cursorP1Row + dRow));
        cursorP1Col = Math.max(0, Math.min(cols - 1, cursorP1Col + dCol));
        highlightCursor(cellsP1, cursorP1Row, cursorP1Col); // Highlight vị trí mới
        return new int[]{ cursorP1Row, cursorP1Col };
    }

    /**
     * [UC5.4.6] Di chuyển con trỏ Player 2 (phím mũi tên).
     * @return int[]{row, col} vị trí con trỏ mới
     */
    public int[] moveP2Cursor(int dRow, int dCol) {
        int rows = cellsP2.length, cols = cellsP2[0].length;
        cellsP2[cursorP2Row][cursorP2Col].setStyle("");
        cursorP2Row = Math.max(0, Math.min(rows - 1, cursorP2Row + dRow));
        cursorP2Col = Math.max(0, Math.min(cols - 1, cursorP2Col + dCol));
        highlightCursor(cellsP2, cursorP2Row, cursorP2Col);
        return new int[]{ cursorP2Row, cursorP2Col };
    }



    /** Lấy vị trí cursor hiện tại của Player 1 */
    public int[] getCursorP1() { return new int[]{ cursorP1Row, cursorP1Col }; }

    /** Lấy vị trí cursor hiện tại của Player 2 */
    public int[] getCursorP2() { return new int[]{ cursorP2Row, cursorP2Col }; }

    /** Tô màu viền vàng cho ô đang được con trỏ trỏ tới (thêm vào styleClass để không bị clear) */
    private void highlightCursor(CellView[][] cells, int row, int col) {
        // Dùng getStyleClass().add() thay vì setStyle() để tránh bị xóa bởi render()
        // render() dùng getStyleClass().clear() nên ta dùng setStyle() cho border riêng
        cells[row][col].setStyle(CURSOR_STYLE);
    }

    // ── Binding đồng hồ và mìn ──────────────────────────────

    /** Bind nhãn timer Player 1 với property giây đã trôi qua */
    public void bindTimerP1(IntegerProperty seconds) {
        lblP1Timer.textProperty().bind(seconds.asString("⏱ %03d"));
    }

    /** Bind nhãn timer Player 2 với property giây đã trôi qua */
    public void bindTimerP2(IntegerProperty seconds) {
        lblP2Timer.textProperty().bind(seconds.asString("⏱ %03d"));
    }

    /** Bind nhãn mìn còn lại Player 1 */
    public void bindMinesP1(IntegerProperty remaining) {
        lblP1Mines.textProperty().bind(remaining.asString("💣 %03d"));
    }

    /** Bind nhãn mìn còn lại Player 2 */
    public void bindMinesP2(IntegerProperty remaining) {
        lblP2Mines.textProperty().bind(remaining.asString("💣 %03d"));
    }

    // ── Getter root ─────────────────────────────────────────

    /** Trả về node gốc để MainView nhúng vào scene */
    public HBox getRoot() { return root; }
}
