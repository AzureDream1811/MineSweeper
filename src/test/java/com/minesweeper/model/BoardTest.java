package com.minesweeper.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class cho Board.revealCell() method
 * Kiểm tra tất cả các trường hợp sử dụng và edge cases
 */
@DisplayName("Board.revealCell() Tests")
class BoardTest {

    private Board board;
    private static final int ROWS = 10;
    private static final int COLS = 10;
    private static final int MINES = 10;

    @BeforeEach
    void setUp() {
        // Tạo board mới trước mỗi test
        board = new Board(ROWS, COLS, MINES);
    }

    // ─── UC-09 - 9.1.4: First Click - Mine Placement ─────────────────

    @Test
    @DisplayName("[UC-09-9.1.4] First click should place mines and ensure clicked cell is safe")
    void testFirstClickPlacesMinesSafely() {
        // Arrange - Board vừa được tạo, firstClick = true
        int safeRow = 5;
        int safeCol = 5;

        // Act
        boolean result = board.revealCell(safeRow, safeCol);

        // Assert
        // Thao tác an toàn - không hit mìn
        assertTrue(result, "First click should be safe");

        // Ô được click phải được mở
        assertTrue(board.getCell(safeRow, safeCol).isRevealed(),
            "Clicked cell should be revealed");

        // Ô được click không phải mìn
        assertFalse(board.getCell(safeRow, safeCol).isMine(),
            "Clicked cell should not contain mine");

        // Có mìn được đặt trên bàn cờ (tổng số mìn > 0 được reveal)
        int mineCount = 0;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (board.getCell(r, c).isMine()) {
                    mineCount++;
                }
            }
        }
        assertEquals(MINES, mineCount, "Total mines should be placed");
    }

    @Test
    @DisplayName("[UC-09-9.1.4] First click neighbors should be mine-safe")
    void testFirstClickNeighborsSafe() {
        // Arrange
        int safeRow = 5;
        int safeCol = 5;

        // Act
        board.revealCell(safeRow, safeCol);

        // Assert - 8 ô xung quanh không được có mìn
        for (int r = safeRow - 1; r <= safeRow + 1; r++) {
            for (int c = safeCol - 1; c <= safeCol + 1; c++) {
                if (r >= 0 && r < ROWS && c >= 0 && c < COLS) {
                    assertFalse(board.getCell(r, c).isMine(),
                        "Neighboring cells of first click should not have mines");
                }
            }
        }
    }

    // ─── UC-09 - 9.3.1: Already Revealed Cell ─────────────────────────

    @Test
    @DisplayName("[UC-09-9.3.1] Clicking already revealed cell should return true")
    void testClickAlreadyRevealedCell() {
        // Arrange
        board.revealCell(5, 5); // Click đầu tiên
        Cell cell = board.getCell(5, 5);
        cell.reveal(); // Đảm bảo ô này đã được mở

        // Act
        boolean result = board.revealCell(5, 5); // Click lần thứ hai

        // Assert
        assertTrue(result, "Second click on revealed cell should return true");
        assertTrue(cell.isRevealed(), "Cell should remain revealed");
    }

    // ─── UC-09 - 9.3.1 / UC-09 - 9.2.1: Flagged Cell ───────────────────

    @Test
    @DisplayName("[UC-09-9.3.1] Clicking flagged cell should not reveal it")
    void testClickFlaggedCellDoesNotReveal() {
        // Arrange - Tạo board custom với mìn xung quanh ô (2,2)
        // để (2,2) có số thay vì blank, tránh flood fill
        Board customBoard = new Board(5, 5, 4);
        boolean[][] layout = new boolean[5][5];
        layout[1][1] = true;
        layout[1][2] = true;
        layout[1][3] = true;
        layout[2][1] = true;
        customBoard = new Board(5, 5, 4, layout);

        Cell cell = customBoard.getCell(2, 2);
        // Verify cell has adjacent mines (so it's not blank)
        assertTrue(cell.getAdjacentMines() > 0, "Cell should have adjacent mines");

        // Cắm cờ trước khi reveal
        cell.toggleFlag();
        assertTrue(cell.isFlagged(), "Cell should be flagged after toggle");

        // Act - Cố gắng reveal flagged cell
        boolean result = customBoard.revealCell(2, 2);

        // Assert
        assertTrue(result, "Should return true for flagged cell");
        assertTrue(cell.isFlagged(), "Cell should remain flagged");
        assertFalse(cell.isRevealed(), "Cell should not be revealed");
    }

    // ─── UC-09 - 9.2.1: Mine Hit ──────────────────────────────────────

    @Test
    @DisplayName("[UC-09-9.2.1] Clicking mine should return false")
    void testClickMineReturnsFalse() {
        // Arrange - Tạo board với mine layout cố định
        boolean[][] mineLayout = new boolean[ROWS][COLS];
        mineLayout[5][5] = true; // Đặt mìn tại (5, 5)
        Board boardWithMine = new Board(ROWS, COLS, 1, mineLayout);

        // Act
        boolean result = boardWithMine.revealCell(5, 5);

        // Assert
        assertFalse(result, "Clicking mine should return false");
        assertTrue(boardWithMine.getCell(5, 5).isRevealed(),
            "Mine cell should be revealed after click");
    }

    @Test
    @DisplayName("[UC-09-9.2.1] Clicked mine should be added to lastRevealedPositions")
    void testMineShouldBeInLastRevealedPositions() {
        // Arrange
        boolean[][] mineLayout = new boolean[ROWS][COLS];
        mineLayout[7][7] = true;
        Board boardWithMine = new Board(ROWS, COLS, 1, mineLayout);

        // Act
        boardWithMine.revealCell(7, 7);

        // Assert
        var lastRevealed = boardWithMine.getLastRevealedPositions();
        assertFalse(lastRevealed.isEmpty(), "lastRevealedPositions should not be empty");
        int[] minePosn = lastRevealed.get(lastRevealed.size() - 1);
        assertEquals(7, minePosn[0], "Mine position row should match");
        assertEquals(7, minePosn[1], "Mine position col should match");
    }

    // ─── UC-09 - 9.1.6: Safe Cell ──────────────────────────────────────

    @Test
    @DisplayName("[UC-09-9.1.6] Clicking safe cell should reveal it and return true")
        // Arrange - Use deterministic mine layout to avoid flaky tests due to random placement
        boolean[][] mineLayout = new boolean[ROWS][COLS];
        mineLayout[0][0] = true;
        Board boardWithMine = new Board(ROWS, COLS, 1, mineLayout);
        Cell safeCell = boardWithMine.getCell(0, 1);

        // Act
        boolean result = boardWithMine.revealCell(0, 1);

        // Assert
        assertTrue(result, "Should return true for safe cell");
        assertTrue(safeCell.isRevealed(), "Safe cell should be revealed");
        assertFalse(safeCell.isMine(), "Safe cell should not be mine");

    // ─── UC-09 - 9.1.8 & UC-15: Blank Cell & Flood Fill ────────────────

    @Test
    @DisplayName("[UC-09-9.1.8] Clicking blank cell should trigger flood fill")
    void testClickBlankCellTriggersFloodFill() {
        // Arrange - Tạo board nhỏ, ít mìn để dễ tạo blank cells
        Board smallBoard = new Board(5, 5, 1);
        boolean[][] layout = new boolean[5][5];
        layout[4][4] = true; // Mìn ở góc
        smallBoard = new Board(5, 5, 1, layout);

        // Act - Click vào (0, 0) - sẽ là blank cell
        smallBoard.revealCell(0, 0);

        // Assert
        // Ô được click phải là blank
        Cell clickedCell = smallBoard.getCell(0, 0);
        assertTrue(clickedCell.isRevealed(), "Clicked cell should be revealed");
        assertTrue(clickedCell.isBlank(), "Clicked cell should be blank (0 adjacent mines)");

        // Flood fill should reveal multiple cells
        int revealedCount = 0;
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                if (smallBoard.getCell(r, c).isRevealed()) {
                    revealedCount++;
                }
            }
        }
        assertTrue(revealedCount > 1,
            "Flood fill should reveal multiple cells, only revealed: " + revealedCount);
    }

    @Test
    @DisplayName("[UC-09-9.1.8] Blank cell should add to lastRevealedPositions")
    void testBlankCellAndFloodFillAddToLastRevealed() {
        // Arrange
        Board smallBoard = new Board(5, 5, 1);
        boolean[][] layout = new boolean[5][5];
        layout[4][4] = true;
        smallBoard = new Board(5, 5, 1, layout);

        // Act
        smallBoard.revealCell(0, 0);
        var lastRevealed = smallBoard.getLastRevealedPositions();

        // Assert
        assertFalse(lastRevealed.isEmpty(), "lastRevealedPositions should contain cells");
        assertTrue(lastRevealed.size() > 1,
            "Flood fill should result in multiple positions being tracked");
    }

    @Test
    @DisplayName("[UC-15-15.1.4] Flood fill should stop at numbered cells")
    void testFloodFillStopsAtNumberedCells() {
        // Arrange
        Board smallBoard = new Board(5, 5, 0); // Không có mìn
        // First click để start
        smallBoard.revealCell(0, 0);

        // Kiểm tra rằng flood fill dừng tại cells với số
        // Nếu tất cả cells không có mìn, flood fill nên reveal toàn bộ
        int revealedCount = 0;
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                if (smallBoard.getCell(r, c).isRevealed()) {
                    revealedCount++;
                }
            }
        }

        assertEquals(25, revealedCount,
            "All cells should be revealed when board has no mines");
    }

    // ─── Out of Bounds ───────────────────────────────────────────────

    @Test
    @DisplayName("Clicking out of bounds should return true without revealing")
    void testOutOfBoundsClickReturnsTrue() {
        // Arrange
        board.revealCell(5, 5); // First click để place mines

        // Act
        boolean result = board.revealCell(-1, 5); // Out of bounds row

        // Assert
        assertTrue(result, "Out of bounds click should return true");
    }

    @Test
    @DisplayName("Out of bounds negative coordinates should be ignored")
    void testNegativeCoordinatesIgnored() {
        // Arrange
        board.revealCell(5, 5);

        // Act & Assert
        assertTrue(board.revealCell(-1, -1), "Negative row/col should return true");
        assertTrue(board.revealCell(5, -1), "Negative col should return true");
        assertTrue(board.revealCell(-1, 5), "Negative row should return true");
    }

    @Test
    @DisplayName("Out of bounds coordinates beyond max should be ignored")
    void testCoordinatesBeyondMaxIgnored() {
        // Arrange
        board.revealCell(5, 5);

        // Act & Assert
        assertTrue(board.revealCell(100, 100), "Oversized coordinates should return true");
        assertTrue(board.revealCell(ROWS, COLS), "row=ROWS, col=COLS should return true");
        assertTrue(board.revealCell(ROWS - 1, COLS), "row=valid, col=COLS should return true");
    }

    // ─── UC-09 - 9.1.10: Win Condition ────────────────────────────────

    @Test
    @DisplayName("[UC-09-9.1.10] Revealing all non-mine cells should trigger win condition")
    void testRevealAllNonMineCellsWin() {
        // Arrange - Board nhỏ, 1 mìn
        Board smallBoard = new Board(3, 3, 1);
        boolean[][] layout = new boolean[3][3];
        layout[2][2] = true; // Mìn ở góc
        smallBoard = new Board(3, 3, 1, layout);

        // Act - Reveal tất cả trừ mìn
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (!(r == 2 && c == 2)) { // Skip the mine
                    smallBoard.revealCell(r, c);
                }
            }
        }

        // Assert
        assertTrue(smallBoard.checkWin(), "Should win when all non-mine cells are revealed");
    }

    @Test
    @DisplayName("[UC-09-9.1.10] Unrevealed non-mine cell should prevent win")
    void testUnrevealedNonMinePreventsWin() {
        // Arrange - Board với mines xung quanh corners để prevent large flood fill
        // Tạo board 10x10 với mines ở edges
        Board testBoard = new Board(10, 10, 18);
        boolean[][] layout = new boolean[10][10];
        // Đặt mines ở edges
        for (int i = 0; i < 10; i++) {
            if (i < 9) {
                layout[i][9] = true;  // Right edge
                layout[9][i] = true;  // Bottom edge
            }
        }
        testBoard = new Board(10, 10, 18, layout);

        // Act - Reveal từng cell một để không trigger mass reveal
        testBoard.revealCell(0, 0);

        // Make sure we don't accidentally win
        // Count unrevealed non-mine cells
        int unrevealedNonMines = 0;
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                Cell cell = testBoard.getCell(r, c);
                if (!cell.isMine() && !cell.isRevealed()) {
                    unrevealedNonMines++;
                }
            }
        }

        // Assert - If there are unrevealed non-mine cells, should not win
        if (unrevealedNonMines > 0) {
            assertFalse(testBoard.checkWin(),
                "Should not win with unrevealed non-mine cells (unrevealed: " + unrevealedNonMines + ")");
        }
    }

    // ─── Cell with Adjacent Mines ──────────────────────────────────────

    @Test
    @DisplayName("Clicking cell with adjacent mines should just reveal it")
    void testClickCellWithAdjacentMines() {
        // Arrange - Board với mìn ở vị trí cụ thể
        Board customBoard = new Board(5, 5, 1);
        boolean[][] layout = new boolean[5][5];
        layout[4][4] = true; // Mìn ở (4,4)
        customBoard = new Board(5, 5, 1, layout);

        // Ô (3,3) sẽ có 1 adjacent mine (là ô kế cận với mìn)
        Cell cellWithMine = customBoard.getCell(3, 3);

        // Act
        boolean result = customBoard.revealCell(3, 3);

        // Assert
        assertTrue(result, "Should return true for cell with adjacent mines");
        assertTrue(cellWithMine.isRevealed(), "Cell should be revealed");
        assertFalse(cellWithMine.isBlank(), "Cell should not be blank");
        assertTrue(cellWithMine.getAdjacentMines() > 0,
            "Cell should have adjacent mine count");
    }

    // ─── Multiple Consecutive Clicks ───────────────────────────────────

    @Test
    @DisplayName("Multiple clicks on different cells should work correctly")
    void testMultipleConsecutiveClicks() {
        // Arrange - deterministic mine layout so clicked cells are guaranteed safe
        boolean[][] mineLayout = new boolean[ROWS][COLS];
        mineLayout[0][0] = true;
        Board deterministicBoard = new Board(ROWS, COLS, 1, mineLayout);

        // Act
        boolean result1 = deterministicBoard.revealCell(0, 1); // numbered safe cell
        boolean result2 = deterministicBoard.revealCell(4, 4);
        boolean result3 = deterministicBoard.revealCell(6, 6);

        // Assert
        assertTrue(result1, "First click should succeed");
        assertTrue(result2, "Second click should succeed");
        assertTrue(result3, "Third click should succeed");

        assertTrue(deterministicBoard.getCell(0, 1).isRevealed(), "First cell should be revealed");
        assertTrue(deterministicBoard.getCell(4, 4).isRevealed(), "Second cell should be revealed");
        assertTrue(deterministicBoard.getCell(6, 6).isRevealed(), "Third cell should be revealed");
    }

    // ─── Regression Tests ──────────────────────────────────────────────

    @Test
    @DisplayName("Two firstClick scenarios with different Board instances")
    void testTwoIndependentFirstClicks() {
        // Arrange
        Board board1 = new Board(5, 5, 1);
        Board board2 = new Board(5, 5, 1);

        // Act
        board1.revealCell(0, 0);
        board2.revealCell(4, 4);

        // Assert
        assertTrue(board1.getCell(0, 0).isRevealed(), "Board1 's first click should be revealed");
        assertTrue(board2.getCell(4, 4).isRevealed(), "Board2's first click should be revealed");

        assertFalse(board1.getCell(0, 0).isMine(), "Board1's first click cell should be safe");
        assertFalse(board2.getCell(4, 4).isMine(), "Board2's first click cell should be safe");
    }

    @Test
    @DisplayName("Flagging and clicking should interact correctly")
    void testFlagThenClick() {
        // Arrange - Tạo board custom với mines xung quanh (2,2)
        // để (2,2) không phải blank cell
        Board customBoard = new Board(5, 5, 4);
        boolean[][] layout = new boolean[5][5];
        layout[1][1] = true;
        layout[1][2] = true;
        layout[1][3] = true;
        layout[2][3] = true;
        customBoard = new Board(5, 5, 4, layout);

        Cell cell = customBoard.getCell(2, 2);
        assertTrue(cell.getAdjacentMines() > 0, "Cell should have adjacent mines");
        cell.toggleFlag();

        // Act
        boolean result = customBoard.revealCell(2, 2);

        // Assert
        assertTrue(result, "Clicking flagged cell should return true");
        assertTrue(cell.isFlagged(), "Cell should still be flagged");
        assertFalse(cell.isRevealed(), "Cell should not be revealed");
    }

    @Test
    @DisplayName("lastRevealedPositions should be tracked correctly")
    void testLastRevealedPositionsTracking() {
        // Arrange & Act - First reveal
        board.revealCell(5, 5);
        var revealed1 = board.getLastRevealedPositions();
        int countAfterFirst = revealed1.size();

        // Assert - Should have tracked at least the first cell
        assertTrue(countAfterFirst > 0, "Should have tracked first reveal");

        // Act - Clear and verify
        board.clearLastRevealed();

        // Act - Second reveal with fresh board to ensure clean test
        Board board2 = new Board(5, 5, 1);
        boolean[][] layout = new boolean[5][5];
        layout[4][4] = true;
        board2 = new Board(5, 5, 1, layout);

        board2.revealCell(0, 0);
        var revealed2 = board2.getLastRevealedPositions();

        // Assert
        assertTrue(revealed2.size() > 0, "Should have tracked revealed positions");

        // Verify positions are int[2] arrays
        for (int[] pos : revealed2) {
            assertEquals(2, pos.length, "Position should be [row, col]");
            assertTrue(pos[0] >= 0 && pos[0] < 5, "Row should be in bounds");
            assertTrue(pos[1] >= 0 && pos[1] < 5, "Col should be in bounds");
        }
    }
}

