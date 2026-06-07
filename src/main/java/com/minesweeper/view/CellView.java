package com.minesweeper.view;
import com.minesweeper.model.Cell;
import javafx.scene.control.Button;

public class CellView extends Button {

    public static final int DEFAULT_CELL_SIZE = 30;

    private final int row, col;
    private final int size;

    public CellView(int row, int col, int size) {
        this.row = row;
        this.col = col;
        this.size = size;
        setPrefSize(size, size);
        setMinSize(size, size);
        setMaxSize(size, size);
        getStyleClass().add("cell-hidden");
        
        // Điều chỉnh cỡ chữ dựa trên kích thước ô bằng Java API để tránh lỗi CSS hover
        setFont(javafx.scene.text.Font.font("Share Tech Mono", javafx.scene.text.FontWeight.BOLD, size * 0.7));
        setPadding(javafx.geometry.Insets.EMPTY);
        setAlignment(javafx.geometry.Pos.CENTER);
    }

    public CellView(int row, int col) {
        this(row, col, DEFAULT_CELL_SIZE);
    }

    public void render(Cell cell) {
        switch (cell.getState()) {
            case HIDDEN -> reset();
            case FLAGGED -> showFlag();
            case REVEALED -> {
                if (cell.isMine()) showMine(false);
                else showNumber(cell.getAdjacentMines());
            }
        }
    }

    public void showMine(boolean exploded) {
        setText("💣");
        getStyleClass().clear();
        getStyleClass().add(exploded ? "cell-exploded" : "cell-mine");
    }

    public void showNumber(int n) {
        setText(n == 0 ? "" : String.valueOf(n));
        getStyleClass().clear();
        getStyleClass().add("cell-revealed");
        if (n > 0) getStyleClass().add("cell-num-" + n);
    }

    public void showFlag() {
        setText("🚩");
        getStyleClass().clear();
        getStyleClass().add("cell-flagged");
    }

    public void reset() {
        setText("");
        getStyleClass().clear();
        getStyleClass().add("cell-hidden");
        setDisable(false);
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }
}