package com.minesweeper.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BoardTest {

    private Board board;
    private final int rows = 9;
    private final int cols = 9;
    private final int totalMines = 10;

    @BeforeEach
    public void setUp() {
        board = new Board(rows, cols, totalMines);
    }

    /**
     * UC-15 & FR-12: Ô đầu tiên click không bao giờ là mìn.
     */
    @Test
    public void testFirstClickIsSafe() {
        int safeRow = 3, safeCol = 3;
        board.placeMines(safeRow, safeCol);

        for (int r = safeRow - 1; r <= safeRow + 1; r++) {
            for (int c = safeCol - 1; c <= safeCol + 1; c++) {
                if (r >= 0 && r < rows && c >= 0 && c < cols) {
                    assertFalse(board.getCell(r, c).isMine(),
                            "Ô (" + r + "," + c + ") xung quanh click đầu tiên không được là mìn");
                }
            }
        }
    }

    /**
     * UC-01: Mở ô có mìn → trả về false.
     */
    @Test
    public void testRevealCellWithMine_ShouldReturnFalse() {
        board.placeMines(0, 0);

        int mineRow = -1, mineCol = -1;
        outer:
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (board.getCell(r, c).isMine()) {
                    mineRow = r;
                    mineCol = c;
                    break outer;
                }
            }
        }

        assertNotEquals(-1, mineRow, "Phải tìm thấy ít nhất một ô mìn");

        boolean result = board.revealCell(mineRow, mineCol);

        assertFalse(result, "Mở ô mìn phải trả về false");
        assertTrue(board.getCell(mineRow, mineCol).isRevealed(), "Ô mìn phải chuyển sang REVEALED");
    }

    /**
     * UC-02 & UC-03: Đánh dấu và hủy cờ.
     */
    @Test
    public void testToggleFlag_ShouldChangeCellStateAndFlagCount() {
        int row = 2, col = 2;

        assertEquals(0, board.getFlagCount(), "Ban đầu số cờ phải bằng 0");

        board.toggleFlag(row, col);
        assertTrue(board.getCell(row, col).isFlagged(), "Ô phải được đánh dấu cờ");
        assertEquals(1, board.getFlagCount(), "Số cờ phải tăng lên 1");

        board.toggleFlag(row, col);
        assertFalse(board.getCell(row, col).isFlagged(), "Cờ phải được gỡ bỏ");
        assertEquals(0, board.getFlagCount(), "Số cờ phải giảm về 0");
    }

    /**
     * UC-04: Không thể mở ô đã cắm cờ.
     */
    @Test
    public void testRevealFlaggedCell_ShouldDoNothing() {
        int row = 4, col = 4;
        board.placeMines(0, 0);

        board.toggleFlag(row, col);
        assertTrue(board.getCell(row, col).isFlagged());

        board.revealCell(row, col);

        assertTrue(board.getCell(row, col).isFlagged(), "Ô đã cắm cờ không thể bị mở");
        assertFalse(board.getCell(row, col).isRevealed(), "Ô đã cắm cờ không được REVEALED");
    }

    /**
     * UC-11: Thắng khi tất cả ô an toàn đã được mở.
     */
    @Test
    public void testCheckWin_WhenAllSafeCellsRevealed_ShouldReturnTrue() {
        board.placeMines(rows - 1, cols - 1);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = board.getCell(r, c);
                if (!cell.isMine()) cell.reveal();
            }
        }

        assertTrue(board.checkWin(), "Phải thắng khi tất cả ô an toàn đã được mở");
    }

    /**
     * UC-01 & FR-14: Flood-fill mở nhiều ô khi click ô trống.
     */
    @Test
    public void testFloodFill_WhenBlankCellRevealed_ShouldRevealMultipleCells() {
        Board blankBoard = new Board(9, 9, 1);
        blankBoard.placeMines(0, 0);

        int blankRow = -1, blankCol = -1;
        outer:
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (blankBoard.getCell(r, c).isBlank()) {
                    blankRow = r;
                    blankCol = c;
                    break outer;
                }
            }
        }

        assertNotEquals(-1, blankRow, "Phải tìm thấy ô trống để test flood-fill");

        blankBoard.revealCell(blankRow, blankCol);

        long revealedCount = 0;
        for (int r = 0; r < 9; r++)
            for (int c = 0; c < 9; c++)
                if (blankBoard.getCell(r, c).isRevealed()) revealedCount++;

        assertTrue(revealedCount > 1, "Flood-fill phải mở nhiều hơn 1 ô, thực tế: " + revealedCount);
    }
}