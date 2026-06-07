# Test Results: Board.toggleFlag() Method

**Date:** June 7, 2026  
**Test Suite:** com.minesweeper.model.BoardTest  
**Total Tests:** 7  
**Status:** ✅ **ALL PASS (7/7)**  
**Build Status:** ✅ BUILD SUCCESS

---

## Test Case Summary

| Test Case ID | Tên Test                              | Input                                                                  | Expected Output                                        | Kết quả |
|:-------------|:--------------------------------------|:-----------------------------------------------------------------------|:-------------------------------------------------------|:-------:|
| **TC_01_01** | Flag placement increases flagCount    | Place flag on HIDDEN cell when `flagCount = 0`                         | `flagCount = 1`, cell flagged                          | ✅ PASS  |
| **TC_01_02** | Flag removal decreases flagCount      | Remove flag from already flagged cell                                  | `flagCount = 0`, cell unflagged                        | ✅ PASS  |
| **TC_01_03** | Cannot exceed max flags               | Place flags until `flagCount = totalMines`, then try to place one more | `flagCount` stays at `totalMines`                      | ✅ PASS  |
| **TC_01_04** | Prevent negative flagCount            | Place flag, remove all flags, verify consistency                       | `flagCount` can go 0→1 normally, never negative        | ✅ PASS  |
| **TC_01_05** | Multiple toggles maintain consistency | Toggle flag on/off 3 times                                             | Each toggle cycle: `flagCount` goes 0→1→0 consistently | ✅ PASS  |
| **TC_02_01** | Cannot toggle flag on revealed cell   | Try to flag an already revealed cell                                   | `flagCount` unchanged, cell not flagged                | ✅ PASS  |
| **TC_03_02** | Flag count property updates           | Place/remove flag and check property binding                           | Property reflects exact `flagCount` changes            | ✅ PASS  |

---

## Use Cases & Requirements Coverage

### UC-1 (Place Flag) ✅

- **1.1.2:** Flag toggle on unrevealed cell
- **1.1.3:** Flag count increment (+1)
- **Limit:** Cannot place more flags than `totalMines`

### UC-2 (Remove Flag) ✅

- **2.1.2:** Check cell flagged status
- **2.1.3:** Toggle flag off
- **2.1.4:** Flag count decrement (-1)
- **Limit:** Cannot cause negative flag count

### FR-09: Right-click handling ✅

- Flag/unflag on unrevealed cells

### FR-15 & FR-16: Flag display ✅

- Flag count updates in real-time
- Observer pattern with IntegerProperty

---

## Bug Fixes Applied

### ❌ **Original Bug**

When `flagCount = 0` and user tries to remove a flag from an unflagged cell, or when `flagCount = totalMines` and user
tries to place another flag, the system didn't validate.

### ✅ **Fix Implemented**

Added validation checks in `Board.toggleFlag()`:

```java
// Prevent placing more flags than totalMines
if(!wasFlagged &&flagCount.

get() >=totalMines){
        return;  // Block flag placement
        }

// Prevent removing flags when count would go negative
        if(wasFlagged &&flagCount.

get() <=0){
        return;  // Block flag removal (safety check)
        }
```

---

## Edge Cases Tested

1. ✅ **Boundary:** `flagCount = totalMines`, try to place one more
2. ✅ **Boundary:** `flagCount = 0`, place flag successfully
3. ✅ **Multiple toggles:** Alternating flag/unflag maintains correct state
4. ✅ **Revealed cells:** Cannot flag already revealed cells
5. ✅ **Property binding:** Changes propagate through IntegerProperty

---

