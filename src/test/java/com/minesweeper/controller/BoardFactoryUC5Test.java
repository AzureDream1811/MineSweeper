package com.minesweeper.controller;

import com.minesweeper.model.Board;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class cho Use Case 5: Khởi tạo bàn cờ mới (UC-05 - 5.1.x)
 */
@DisplayName("BoardFactory Tests for UC-05")
class BoardFactoryUC5Test {

    // ─── UC-05 - 5.1.4: Hệ thống khởi tạo cấu trúc dữ liệu cho Bàn cờ ─────────

    @Test
    @DisplayName("[UC-05-5.1.4] Khởi tạo bàn cờ với cấp độ DỄ (EASY)")
    void testCreateBoardEasy() {
        Board board = BoardFactory.createBoard(Difficulty.EASY);
        
        assertEquals(Difficulty.EASY.getRows(), board.getRows(), "Số hàng cấp EASY không đúng");
        assertEquals(Difficulty.EASY.getCols(), board.getCols(), "Số cột cấp EASY không đúng");
        assertEquals(Difficulty.EASY.getMines(), board.getTotalMines(), "Số mìn cấp EASY không đúng");
    }

    @Test
    @DisplayName("[UC-05-5.1.4] Khởi tạo bàn cờ với cấp độ TRUNG BÌNH (MEDIUM)")
    void testCreateBoardMedium() {
        Board board = BoardFactory.createBoard(Difficulty.MEDIUM);
        
        assertEquals(Difficulty.MEDIUM.getRows(), board.getRows(), "Số hàng cấp MEDIUM không đúng");
        assertEquals(Difficulty.MEDIUM.getCols(), board.getCols(), "Số cột cấp MEDIUM không đúng");
        assertEquals(Difficulty.MEDIUM.getMines(), board.getTotalMines(), "Số mìn cấp MEDIUM không đúng");
    }

    @Test
    @DisplayName("[UC-05-5.1.4] Khởi tạo bàn cờ với cấp độ KHÓ (HARD)")
    void testCreateBoardHard() {
        Board board = BoardFactory.createBoard(Difficulty.HARD);
        
        assertEquals(Difficulty.HARD.getRows(), board.getRows(), "Số hàng cấp HARD không đúng");
        assertEquals(Difficulty.HARD.getCols(), board.getCols(), "Số cột cấp HARD không đúng");
        assertEquals(Difficulty.HARD.getMines(), board.getTotalMines(), "Số mìn cấp HARD không đúng");
    }
}
