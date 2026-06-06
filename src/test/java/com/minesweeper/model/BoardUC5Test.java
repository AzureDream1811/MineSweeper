package com.minesweeper.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class cho Use Case 5: Khởi tạo bàn cờ PvP (UC-05 - 5.2.x, trước đây là 5.4.x)
 */
@DisplayName("Board Tests for UC-05 (PvP Initialization)")
class BoardUC5Test {

    // ─── UC-05 - 5.2.5 (was 5.4.5): Tạo Board với mine layout định sẵn cho PvP ─────────

    @Test
    @DisplayName("[UC-05-5.2.5] Tạo Board PvP với mine layout hợp lệ")
    void testCreatePvPBoardValidLayout() {
        int rows = 5;
        int cols = 5;
        int totalMines = 3;
        boolean[][] layout = new boolean[rows][cols];
        // Đặt mìn tại (0,0), (2,2) và (4,4)
        layout[0][0] = true;
        layout[2][2] = true;
        layout[4][4] = true;

        Board board = new Board(rows, cols, totalMines, layout);

        assertEquals(rows, board.getRows(), "Số hàng của bàn cờ PvP phải khớp với cấu hình");
        assertEquals(cols, board.getCols(), "Số cột của bàn cờ PvP phải khớp với cấu hình");
        assertEquals(totalMines, board.getTotalMines(), "Tổng số mìn của bàn cờ PvP phải khớp với cấu hình");

        // Kiểm tra xem mìn có được đặt đúng theo layout cấu hình sẵn không
        assertTrue(board.getCell(0, 0).isMine(), "Ô (0,0) phải được đặt mìn dựa theo layout");
        assertTrue(board.getCell(2, 2).isMine(), "Ô (2,2) phải được đặt mìn dựa theo layout");
        assertTrue(board.getCell(4, 4).isMine(), "Ô (4,4) phải được đặt mìn dựa theo layout");
        
        // Kiểm tra một ô ngẫu nhiên không có trong layout
        assertFalse(board.getCell(0, 1).isMine(), "Ô (0,1) không được có mìn");
        assertFalse(board.getCell(1, 1).isMine(), "Ô (1,1) không được có mìn");
    }

    @Test
    @DisplayName("[UC-05-5.2.5] Tạo Board PvP với layout mìn không chứa đủ mìn (Edge Case)")
    void testCreatePvPBoardMissingMinesInLayout() {
        int rows = 5;
        int cols = 5;
        int totalMines = 5; // Yêu cầu 5 mìn theo luật của cấp độ
        boolean[][] layout = new boolean[rows][cols];
        layout[1][1] = true; // Nhưng layout truyền vào chỉ định sẵn 1 mìn

        Board board = new Board(rows, cols, totalMines, layout);

        assertTrue(board.getCell(1, 1).isMine(), "Mìn phải được giữ lại từ layout");
        assertEquals(totalMines, board.getTotalMines(), "Thuộc tính totalMines vẫn phải giữ nguyên");
    }
}
