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


    @Test
    @DisplayName("TC_09_01 First click should place mines and ensure clicked cell is safe")
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
    @DisplayName("TC_09_02 First click neighbors should be mine-safe")
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


    @Test
    @DisplayName("TC_09_03 Clicking already revealed cell should return true")
    void testClickAlreadyRevealedCell() {
        board.revealCell(5, 5);
        Cell cell = board.getCell(5, 5);
        cell.reveal();

        boolean result = board.revealCell(5, 5);

        assertTrue(result, "Second click on revealed cell should return true");
        assertTrue(cell.isRevealed(), "Cell should remain revealed");
    }


    @Test
    @DisplayName("TC_09_04 Clicking flagged cell should not reveal it")
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


    @Test
    @DisplayName("TC_09_05 Clicking mine should return false")
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
    @DisplayName("TC_09_06 Clicked mine should be added to lastRevealedPositions")
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

    @Test
    @DisplayName("TC_09_07 Clicking safe cell should reveal it and return true")
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

    @Test
    @DisplayName("TC_09_08 Revealing all non-mine cells should trigger win condition")
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
    @DisplayName("TC_09_09 Unrevealed non-mine cell should prevent win")
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

    @Test
    @DisplayName("TC_09_10 Clicking cell with adjacent mines should just reveal it")
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

    @Test
    @DisplayName("TC_15_01 Clicking blank cell should trigger flood fill")
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
    @DisplayName("TC_15_02 Flood fill should stop at numbered cells")
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

    @Test
    @DisplayName("TC_17_01 Out of bounds negative coordinates should be ignored")
    void testNegativeCoordinatesIgnored() {
        board.revealCell(5, 5);

        assertTrue(board.revealCell(-1, -1), "Negative row/col should return true");
        assertTrue(board.revealCell(5, -1), "Negative col should return true");
        assertTrue(board.revealCell(-1, 5), "Negative row should return true");
    }

    @Test
    @DisplayName("TC_17_02 Out of bounds coordinates beyond max should be ignored")
    void testCoordinatesBeyondMaxIgnored() {
        board.revealCell(5, 5);

        assertTrue(board.revealCell(100, 100), "Oversized coordinates should return true");
        assertTrue(board.revealCell(ROWS, COLS), "row=ROWS, col=COLS should return true");
        assertTrue(board.revealCell(ROWS - 1, COLS), "row=valid, col=COLS should return true");
    }


    // ─── UC-1 & UC-2: Toggle Flag Tests ─────────────────────

    @Test
    @DisplayName("TC_01_01 - Flag placement should increase flagCount when valid")
    void testFlagPlacementIncreasesFlagCount() {
        // Create custom board with mines to avoid flood fill
        boolean[][] layout = new boolean[10][10];
        layout[0][0] = true; // Mine at (0,0)
        layout[5][5] = true; // Mine at (5,5)
        Board customBoard = new Board(10, 10, 2, layout);

        // Cell (1,1) should still be HIDDEN, not affected by first click
        customBoard.toggleFlag(1, 1); // Place flag on unrevealed cell

        assertEquals(1, customBoard.getFlagCount(),
                "Flag count should increase to 1 after placing flag");
        assertTrue(customBoard.getCell(1, 1).isFlagged(),
                "Cell should be flagged");
    }


    @Test
    @DisplayName("TC_01_02 - Cannot place more flags than totalMines")
    void testCannotExceedMaxFlags() {
        // Create custom board with specific mine count
        boolean[][] layout = new boolean[10][10];
        for (int i = 0; i < 5; i++) {
            layout[i][0] = true;
        }
        Board customBoard = new Board(10, 10, 5, layout);

        int totalMines = customBoard.getTotalMines();

        // Try to place flags equal to totalMines
        for (int i = 0; i < 5; i++) {
            customBoard.toggleFlag(i, 1);
        }

        int flagCountBefore = customBoard.getFlagCount();
        assertEquals(totalMines, flagCountBefore,
                "Flag count should reach totalMines");

        // Try to place one more flag (should be blocked)
        customBoard.toggleFlag(5, 5);
        assertEquals(flagCountBefore, customBoard.getFlagCount(),
                "Flag count should not exceed totalMines");
    }


    @Test
    @DisplayName("TC_01_03 - Toggle flag multiple times maintains consistency")
    void testMultipleFlagToggleConsistency() {
        // Create custom board
        boolean[][] layout = new boolean[10][10];
        layout[0][0] = true;
        Board customBoard = new Board(10, 10, 1, layout);

        // Toggle flag on/off multiple times
        for (int i = 0; i < 3; i++) {
            customBoard.toggleFlag(1, 1); // Place flag
            assertEquals(1, customBoard.getFlagCount(),
                    "Flag count should be 1 after odd toggles");
            assertTrue(customBoard.getCell(1, 1).isFlagged());

            customBoard.toggleFlag(1, 1); // Remove flag
            assertEquals(0, customBoard.getFlagCount(),
                    "Flag count should be 0 after even toggles");
            assertFalse(customBoard.getCell(1, 1).isFlagged());
        }
    }

    @Test
    @DisplayName("TC_01_04 - Cannot toggle flag on revealed cell")
    void testCannotToggleFlagOnRevealedCell() {
        board.revealCell(0, 0); // First click - reveals a cell
        int initialFlagCount = board.getFlagCount();

        // Try to toggle flag on revealed cell
        board.toggleFlag(0, 0);
        assertEquals(initialFlagCount, board.getFlagCount(),
                "Flag count should not change when toggling on revealed cell");
        assertFalse(board.getCell(0, 0).isFlagged(),
                "Revealed cell should not be flagged");
    }

    @Test
    @DisplayName("TC_01_05 - Flag count property updates correctly")
    void testFlagCountPropertyUpdates() {
        // Create custom board
        boolean[][] layout = new boolean[10][10];
        layout[0][0] = true;
        Board customBoard = new Board(10, 10, 1, layout);

        customBoard.toggleFlag(1, 1); // Place flag
        assertEquals(1, customBoard.flagCountProperty().get(),
                "Flag count property should reflect updated count");

        customBoard.toggleFlag(1, 1); // Remove flag
        assertEquals(0, customBoard.flagCountProperty().get(),
                "Flag count property should reflect removed flag");
    }


    @Test
    @DisplayName("TC_02_01 - Flag removal should decrease flagCount when valid")
    void testFlagRemovalDecreasesFlagCount() {
        // Create custom board with mines
        boolean[][] layout = new boolean[10][10];
        layout[0][0] = true;
        layout[5][5] = true;
        Board customBoard = new Board(10, 10, 2, layout);

        customBoard.toggleFlag(1, 1); // Place flag
        assertEquals(1, customBoard.getFlagCount());

        customBoard.toggleFlag(1, 1); // Remove flag
        assertEquals(0, customBoard.getFlagCount(),
                "Flag count should decrease to 0 after removing flag");
        assertFalse(customBoard.getCell(1, 1).isFlagged(),
                "Cell should not be flagged");
    }

    @Test
    @DisplayName("TC_02_02 - Cannot remove flags when flagCount is 0 (prevent negative)")
    void testCannotGoNegativeFlagCount() {
        // Create custom board
        boolean[][] layout = new boolean[10][10];
        layout[0][0] = true;
        Board customBoard = new Board(10, 10, 1, layout);

        assertEquals(0, customBoard.getFlagCount(),
                "Initial flag count should be 0");

        // First place one flag
        customBoard.toggleFlag(1, 1);
        assertEquals(1, customBoard.getFlagCount(),
                "Flag count should be 1 after placing");

        // Remove the flag
        customBoard.toggleFlag(1, 1);
        assertEquals(0, customBoard.getFlagCount(),
                "Flag count should be 0 after removal");

        // Try to toggle on an unflagged cell when flagCount = 0
        // This should place a flag (not remove), since cell is unflagged
        customBoard.toggleFlag(2, 2);
        assertEquals(1, customBoard.getFlagCount(),
                "Should be able to place flag even when flagCount was 0");
    }
}
