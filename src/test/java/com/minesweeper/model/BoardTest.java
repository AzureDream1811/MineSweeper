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
        board = new Board(ROWS, COLS, MINES);
    }

    // ─── UC-09 - 9.1.4: First Click - Mine Placement ─────────────────

    @Test
    @DisplayName("[UC-09-9.1.4] First click should place mines and ensure clicked cell is safe")
    void testFirstClickPlacesMinesSafely() {
        int safeRow = 5;
        int safeCol = 5;

        boolean result = board.revealCell(safeRow, safeCol);

        assertTrue(result, "First click should be safe");
        assertTrue(board.getCell(safeRow, safeCol).isRevealed(),
            "Clicked cell should be revealed");
        assertFalse(board.getCell(safeRow, safeCol).isMine(),
            "Clicked cell should not contain mine");

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
        int safeRow = 5;
        int safeCol = 5;

        board.revealCell(safeRow, safeCol);

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
        board.revealCell(5, 5);
        Cell cell = board.getCell(5, 5);
        cell.reveal();

        boolean result = board.revealCell(5, 5);

        assertTrue(result, "Second click on revealed cell should return true");
        assertTrue(cell.isRevealed(), "Cell should remain revealed");
    }

    // ─── UC-09 - 9.3.1 / UC-09 - 9.2.1: Flagged Cell ───────────────────

    @Test
    @DisplayName("[UC-09-9.3.1] Clicking flagged cell should not reveal it")
    void testClickFlaggedCellDoesNotReveal() {
        Board customBoard = new Board(5, 5, 4);
        boolean[][] layout = new boolean[5][5];
        layout[1][1] = true;
        layout[1][2] = true;
        layout[1][3] = true;
        layout[2][1] = true;
        customBoard = new Board(5, 5, 4, layout);

        Cell cell = customBoard.getCell(2, 2);
        assertTrue(cell.getAdjacentMines() > 0, "Cell should have adjacent mines");

        cell.toggleFlag();
        assertTrue(cell.isFlagged(), "Cell should be flagged after toggle");

        boolean result = customBoard.revealCell(2, 2);

        assertTrue(result, "Should return true for flagged cell");
        assertTrue(cell.isFlagged(), "Cell should remain flagged");
        assertFalse(cell.isRevealed(), "Cell should not be revealed");
    }

    // ─── UC-09 - 9.2.1: Mine Hit ──────────────────────────────────────

    @Test
    @DisplayName("[UC-09-9.2.1] Clicking mine should return false")
    void testClickMineReturnsFalse() {
        boolean[][] mineLayout = new boolean[ROWS][COLS];
        mineLayout[5][5] = true;
        Board boardWithMine = new Board(ROWS, COLS, 1, mineLayout);

        boolean result = boardWithMine.revealCell(5, 5);

        assertFalse(result, "Clicking mine should return false");
        assertTrue(boardWithMine.getCell(5, 5).isRevealed(),
            "Mine cell should be revealed after click");
    }

    @Test
    @DisplayName("[UC-09-9.2.1] Clicked mine should be added to lastRevealedPositions")
    void testMineShouldBeInLastRevealedPositions() {
        boolean[][] mineLayout = new boolean[ROWS][COLS];
        mineLayout[7][7] = true;
        Board boardWithMine = new Board(ROWS, COLS, 1, mineLayout);

        boardWithMine.revealCell(7, 7);

        var lastRevealed = boardWithMine.getLastRevealedPositions();
        assertFalse(lastRevealed.isEmpty(), "lastRevealedPositions should not be empty");
        int[] minePos = lastRevealed.get(lastRevealed.size() - 1);
        assertEquals(7, minePos[0], "Mine position row should match");
        assertEquals(7, minePos[1], "Mine position col should match");
    }

    // ─── UC-09 - 9.1.6: Safe Cell ──────────────────────────────────────

    @Test
    @DisplayName("[UC-09-9.1.6] Clicking safe cell should reveal it and return true")
    void testClickSafeCellRevealsIt() {
        boolean[][] mineLayout = new boolean[ROWS][COLS];
        mineLayout[0][0] = true;
        Board boardWithMine = new Board(ROWS, COLS, 1, mineLayout);
        Cell safeCell = boardWithMine.getCell(0, 1);

        boolean result = boardWithMine.revealCell(0, 1);

        assertTrue(result, "Should return true for safe cell");
        assertTrue(safeCell.isRevealed(), "Safe cell should be revealed");
        assertFalse(safeCell.isMine(), "Safe cell should not be mine");
    }

    // ─── UC-09 - 9.1.8 & UC-15: Blank Cell & Flood Fill ────────────────

    @Test
    @DisplayName("[UC-09-9.1.8] Clicking blank cell should trigger flood fill")
    void testClickBlankCellTriggersFloodFill() {
        Board smallBoard = new Board(5, 5, 1);
        boolean[][] layout = new boolean[5][5];
        layout[4][4] = true;
        smallBoard = new Board(5, 5, 1, layout);

        smallBoard.revealCell(0, 0);

        Cell clickedCell = smallBoard.getCell(0, 0);
        assertTrue(clickedCell.isRevealed(), "Clicked cell should be revealed");
        assertTrue(clickedCell.isBlank(), "Clicked cell should be blank (0 adjacent mines)");

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
        Board smallBoard = new Board(5, 5, 1);
        boolean[][] layout = new boolean[5][5];
        layout[4][4] = true;
        smallBoard = new Board(5, 5, 1, layout);

        smallBoard.revealCell(0, 0);
        var lastRevealed = smallBoard.getLastRevealedPositions();

        assertFalse(lastRevealed.isEmpty(), "lastRevealedPositions should contain cells");
        assertTrue(lastRevealed.size() > 1,
            "Flood fill should result in multiple positions being tracked");
    }

    @Test
    @DisplayName("[UC-15-15.1.4] Flood fill should stop at numbered cells")
    void testFloodFillStopsAtNumberedCells() {
        Board smallBoard = new Board(5, 5, 0);
        smallBoard.revealCell(0, 0);

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
        board.revealCell(5, 5);

        boolean result = board.revealCell(-1, 5);

        assertTrue(result, "Out of bounds click should return true");
    }

    @Test
    @DisplayName("Out of bounds negative coordinates should be ignored")
    void testNegativeCoordinatesIgnored() {
        board.revealCell(5, 5);

        assertTrue(board.revealCell(-1, -1), "Negative row/col should return true");
        assertTrue(board.revealCell(5, -1), "Negative col should return true");
        assertTrue(board.revealCell(-1, 5), "Negative row should return true");
    }

    @Test
    @DisplayName("Out of bounds coordinates beyond max should be ignored")
    void testCoordinatesBeyondMaxIgnored() {
        board.revealCell(5, 5);

        assertTrue(board.revealCell(100, 100), "Oversized coordinates should return true");
        assertTrue(board.revealCell(ROWS, COLS), "row=ROWS, col=COLS should return true");
        assertTrue(board.revealCell(ROWS - 1, COLS), "row=valid, col=COLS should return true");
    }

    // ─── UC-09 - 9.1.10: Win Condition ────────────────────────────────

    @Test
    @DisplayName("[UC-09-9.1.10] Revealing all non-mine cells should trigger win condition")
    void testRevealAllNonMineCellsWin() {
        Board smallBoard = new Board(3, 3, 1);
        boolean[][] layout = new boolean[3][3];
        layout[2][2] = true;
        smallBoard = new Board(3, 3, 1, layout);

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (!(r == 2 && c == 2)) {
                    smallBoard.revealCell(r, c);
                }
            }
        }

        assertTrue(smallBoard.checkWin(), "Should win when all non-mine cells are revealed");
    }

    @Test
    @DisplayName("[UC-09-9.1.10] Unrevealed non-mine cell should prevent win")
    void testUnrevealedNonMinePreventsWin() {
        Board testBoard = new Board(10, 10, 18);
        boolean[][] layout = new boolean[10][10];
        for (int i = 0; i < 10; i++) {
            if (i < 9) {
                layout[i][9] = true;
                layout[9][i] = true;
            }
        }
        testBoard = new Board(10, 10, 18, layout);

        boolean result = testBoard.revealCell(8, 8);

        assertTrue(result, "Revealing a safe cell should return true");
        assertTrue(testBoard.getCell(8, 8).isRevealed(), "Clicked cell should be revealed");
        assertFalse(testBoard.checkWin(),
            "Should not win when there are still unrevealed non-mine cells");
    }

    // ─── Cell with Adjacent Mines ──────────────────────────────────────

    @Test
    @DisplayName("Clicking cell with adjacent mines should just reveal it")
    void testClickCellWithAdjacentMines() {
        Board customBoard = new Board(5, 5, 1);
        boolean[][] layout = new boolean[5][5];
        layout[4][4] = true;
        customBoard = new Board(5, 5, 1, layout);

        Cell cellWithMine = customBoard.getCell(3, 3);

        boolean result = customBoard.revealCell(3, 3);

        assertTrue(result, "Should return true for cell with adjacent mines");
        assertTrue(cellWithMine.isRevealed(), "Cell should be revealed");
        assertFalse(cellWithMine.isBlank(), "Cell should not be blank");
        assertTrue(cellWithMine.getAdjacentMines() > 0,
            "Cell should have adjacent mine count");
    }
}

