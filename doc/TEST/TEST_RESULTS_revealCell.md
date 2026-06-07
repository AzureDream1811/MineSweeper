# Test Results - Board.revealCell() Method

## Test Summary

| Trường             | Nội dung     |
|--------------------|--------------|
| **Total Tests**    | 16           |
| **Passed**         | 16 ✅         |
| **Failed**         | 0            |
| **Execution Date** | June 6, 2026 |

---

## Test Cases

| Test Case ID | Use Case | Tên Test                                                       | Input                                                 | Expected Output                                            | Kết quả |
|:------------:|:--------:|:---------------------------------------------------------------|:------------------------------------------------------|:-----------------------------------------------------------|:-------:|
|   TC_09_01   |  UC-09   | First click should place mines and ensure clicked cell is safe | `revealCell(5, 5)` on new board                       | Mines placed, clicked cell safe, returns true              | ✅ PASS  |
|   TC_09_02   |  UC-09   | First click neighbors should be mine-safe                      | `placeMines()` called for first click at (5,5)        | All 8 neighboring cells are mine-free                      | ✅ PASS  |
|   TC_09_03   |  UC-09   | Clicking already revealed cell should return true              | Click cell twice at same position                     | Returns true on second click, cell remains revealed        | ✅ PASS  |
|   TC_09_04   |  UC-09   | Clicking flagged cell should not reveal it                     | `revealCell(2,2)` on flagged cell with adjacent mines | Returns true, cell stays flagged and hidden                | ✅ PASS  |
|   TC_09_05   |  UC-09   | Clicking mine should return false                              | `revealCell(5,5)` where mine exists                   | Returns false, mine cell is revealed                       | ✅ PASS  |
|   TC_09_06   |  UC-09   | Clicked mine should be in lastRevealedPositions                | Hit mine at (7,7)                                     | Mine position added to `lastRevealedPositions`             | ✅ PASS  |
|   TC_09_07   |  UC-09   | Clicking safe cell should reveal it and return true            | `revealCell(0,1)` on empty safe cell                  | Returns true, cell revealed, not a mine                    | ✅ PASS  |
|   TC_09_08   |  UC-09   | Revealing all non-mine cells should trigger win condition      | Reveal all non-mine cells in 3x3 board                | `checkWin()` returns true                                  | ✅ PASS  |
|   TC_09_09   |  UC-09   | Unrevealed non-mine cell should prevent win                    | Partial reveal on 10x10 board                         | `checkWin()` returns false with unrevealed cells           | ✅ PASS  |
|   TC_09_10   |  UC-09   | Clicking cell with adjacent mines should just reveal it        | Click cell with 1+ adjacent mines                     | Returns true, cell revealed, not blank (no flood fill)     | ✅ PASS  |
|   TC_15_01   |  UC-15   | Clicking blank cell should trigger flood fill                  | `revealCell(0,0)` on blank cell (0 adjacent mines)    | Multiple cells revealed through flood fill                 | ✅ PASS  |
|   TC_15_02   |  UC-15   | Blank cell flood fill should add to lastRevealedPositions      | Flood fill triggered from (0,0)                       | Multiple cell positions tracked in `lastRevealedPositions` | ✅ PASS  |
|   TC_15_03   |  UC-15   | Flood fill should stop at numbered cells                       | Click (0,0) on board with no mines                    | All 25 cells revealed (5x5 board, no mines)                | ✅ PASS  |
|   TC_17_01   |  UC-17   | Out of bounds click (negative row) should return true          | `revealCell(-1, 5)`                                   | Returns true without revealing                             | ✅ PASS  |
|   TC_17_02   |  UC-17   | Out of bounds negative coordinates should be ignored           | `revealCell(-1, -1)` and variations                   | All return true, no cells modified                         | ✅ PASS  |
|   TC_17_03   |  UC-17   | Out of bounds coordinates beyond max should be ignored         | `revealCell(100, 100)`, `revealCell(ROWS, COLS)`      | All return true without error                              | ✅ PASS  |

---

## Test Coverage Summary

| Use Case                  | Mô tả                                                             |   Số test   |
|---------------------------|-------------------------------------------------------------------|:-----------:|
| UC-09 – Mở Ô              | First click safety, mine placement, win condition, state handling |     10      |
| UC-15 – Flood Fill        | Blank cell detection, flood fill operation, boundary termination  |      3      |
| UC-17 – Bounds/Edge Cases | Out of bounds handling, coordinate validation                     |      3      |
| **TOTAL**                 |                                                                   | **16/16 ✅** |