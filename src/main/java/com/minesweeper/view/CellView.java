package com.minesweeper.view;
import com.minesweeper.model.Cell;
import javafx.scene.control.Button;

import com.minesweeper.model.Cell;
import javafx.scene.control.Button;

public class CellView extends Button {

    public static final int CELL_SIZE = 30;

    private final int row, col;

    public CellView(int row, int col) {
        this.row = row;
        this.col = col;
        setPrefSize(CELL_SIZE, CELL_SIZE);
        setMinSize(CELL_SIZE, CELL_SIZE);
        setMaxSize(CELL_SIZE, CELL_SIZE);
        getStyleClass().add("cell-hidden");
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