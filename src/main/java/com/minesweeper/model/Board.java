package com.minesweeper.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import java.util.ArrayDeque;
import java.util.Deque;


public class Board {

    private Cell[][] cells;
    private int rows;
    private int cols;
    private int totalMines;
    private final IntegerProperty flagCount = new SimpleIntegerProperty(0);
    private boolean firstClick;
    // Danh sách các ô vừa được reveal trong lần gọi revealCell gần nhất
    private List<int[]> lastRevealedPositions = new ArrayList<>();

    /**
     * Tạo Board với kích thước và số mìn cho trước.
     * Chưa đặt mìn — đợi đến click đầu tiên.
     *
     * @param rows       số hàng
     * @param cols       số cột
     * @param totalMines tổng số mìn
     */
    public Board(int rows, int cols, int totalMines) {
        this.rows = rows;
        this.cols = cols;
        this.totalMines = totalMines;
        this.firstClick = true;
        this.cells = new Cell[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                this.cells[r][c] = new Cell(r, c);
            }
        }
    }

    /**
     * [UC5.4.5] Tạo Board với mine layout định sẵn (dùng chung cho PvP).
     * firstClick = false vì mìn đã được đặt sẵn, không cần an toàn click đầu.
     *
     * @param rows       số hàng
     * @param cols       số cột
     * @param totalMines tổng số mìn
     * @param mineLayout mảng 2D đánh dấu vị trí mìn (true = có mìn)
     */
    public Board(int rows, int cols, int totalMines, boolean[][] mineLayout) {
        this.rows = rows;
        this.cols = cols;
        this.totalMines = totalMines;
        this.firstClick = false; // Mìn đã được đặt sẵn, bỏ qua bước safe-first-click
        this.cells = new Cell[rows][cols];

        // Đặt tạo cells và áp dụng layout mìn
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                this.cells[r][c] = new Cell(r, c);
                if (mineLayout[r][c]) {
                    this.cells[r][c].setMine(); // Đặt mìn theo layout có sẵn
                }
            }
        }
        calculateAdjacentMines(); // Tính số mìn kế cận sau khi đặt xong
    }

    // ── Mine Placement ────────────────────────────────────────

    /**
     * Đặt mìn ngẫu nhiên trên bàn cờ, đảm bảo ô (safeRow, safeCol) không phải mìn.
     * Cập nhật adjacentMines cho tất cả ô sau khi đặt mìn xong.
     * <p>
     * Gọi lần đầu tiên khi người chơi click ô đầu tiên — FR-12.
     *
     * @param safeRow hàng của ô đầu tiên được click
     * @param safeCol cột của ô đầu tiên được clickf
     */
    public void placeMines(int safeRow, int safeCol) {

        List<int[]> candidates = new ArrayList<>();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                // Loại trừ ô an toàn và 8 ô xung quanh nó
                // VD: nếu safeRow=3, safeCol=3 → loại (2,2) đến (4,4)
                if (Math.abs(r - safeRow) <= 1 && Math.abs(c - safeCol) <= 1)
                    continue;
                candidates.add(new int[] { r, c });
            }
        }

        Collections.shuffle(candidates, new Random());

        int placed = 0;
        for (int i = 0; i < candidates.size() && placed < totalMines; i++) {
            int[] pos = candidates.get(i);
            int r = pos[0];
            int c = pos[1];
            Cell cell = cells[r][c];
            cell.setMine();
            placed++;
        }

        calculateAdjacentMines();
    }

    /**
     * Tính và gán số xung quanh cho từng ô không phải mìn.
     * Gọi một lần sau {@link #placeMines(int, int)} — FR-13.
     */
    private void calculateAdjacentMines() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = cells[r][c];
                if (cell.isMine())
                    continue;

                int count = 0;
                for (int dr = -1; dr <= 1; dr++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        if (dr == 0 && dc == 0)
                            continue;
                        int nr = r + dr;
                        int nc = c + dc;
                        if (inBounds(nr, nc) && cells[nr][nc].isMine())
                            count++;
                    }
                }
                cell.setAdjacentMines(count);
            }
        }
    }
    public boolean revealCell(int row, int col) {
        // Board.java - revealCell()
        if (firstClick) {
            placeMines(row, col);
            firstClick = false;
        }
        // Reset danh sách revealed cho lần thao tác này
        // lastRevealedPositions.clear();

        if (!inBounds(row, col)) return true;

        // Board.java - revealCell()
        Cell cell = cells[row][col];

        if (cell.isRevealed() || cell.isFlagged()) return true;

        // Nếu là mìn → reveal và trả về false (người chơi thua)
        if (cell.isMine()) {
            cell.reveal();
            lastRevealedPositions.add(new int[]{row, col});
            return false;
        }

        // Bình thường: mở ô
        cell.reveal();
        lastRevealedPositions.add(new int[]{row, col});

        // Board.java - revealCell()
        // Nếu ô trống (blank), mở rộng vùng bằng flood fill
        if (cell.isBlank()) {
            floodFill(row, col);
        }

        return true;
    }

    /**
     * Đệ quy mở tất cả ô trống liền kề cho đến khi gặp ô có số.
     * FR-14
     *
     * @param row hàng
     * @param col cột
     */
    // Board.java
    private void floodFill(int row, int col) {
        if (!inBounds(row, col)) return;

        Deque<int[]> toVisit = new ArrayDeque<>();

        // Reveal ô gốc trước rồi mới push
        cells[row][col].reveal();
        lastRevealedPositions.add(new int[]{row, col});
        toVisit.push(new int[]{row, col});

        while (!toVisit.isEmpty()) {
            int[] position = toVisit.pop();
            int currentRow = position[0];
            int currentCol = position[1];

            for (int deltaRow = -1; deltaRow <= 1; deltaRow++) {
                for (int deltaCol = -1; deltaCol <= 1; deltaCol++) {
                    if (deltaRow == 0 && deltaCol == 0) continue; // bỏ qua chính nó

                    int neighborRow = currentRow + deltaRow;
                    int neighborCol = currentCol + deltaCol;
                    if (!inBounds(neighborRow, neighborCol)) continue;

                    Cell neighbor = cells[neighborRow][neighborCol];
                    if (neighbor.isRevealed() || neighbor.isFlagged() || neighbor.isMine()) continue;

                    neighbor.reveal();
                    lastRevealedPositions.add(new int[]{neighborRow, neighborCol});

                    if (neighbor.isBlank()) {
                        toVisit.push(new int[]{neighborRow, neighborCol});
                    }
                }
            }
        }
    }

    /**
     * Xử lý Chording: double-click vào ô số đã có đủ cờ xung quanh
     * → tự động mở các ô HIDDEN còn lại xung quanh.
     * FR-11
     *
     * @param row hàng
     * @param col cột
     * @return false nếu chording gây nổ mìn, true nếu an toàn
     * Delta Row: Độ lệch hàng (chạy từ -1, 0, đến 1)
     * Delta Column: Độ lệch cột (chạy từ -1, 0, đến 1)
     * Neighbor Row: Chỉ số hàng thực tế của ô lân cận
     * Neighbor Column: Chỉ số cột thực tế của ô lân cận
     */
    public boolean chord(int row, int col) {
        // TODO: kiểm tra số cờ xung quanh == adjacentMines của ô đó
        // nếu đúng → mở toàn bộ ô HIDDEN xung quanh
        if (!inBounds(row, col)) return true;
        Cell cell = cells[row][col];
        if (!cell.isRevealed() || cell.getAdjacentMines() == 0) return true;
        int FlaggedAround = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int nr = row + i;
                int nc = col + j;
                if (inBounds(nr, nc)){
                    if (cells[nr][nc].isFlagged()) {
                        FlaggedAround++;
                    }
                }
            }
        }
        if (FlaggedAround == cell.getAdjacentMines()){
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    int nr = row + i;
                    int nc = col + j;
                    if (inBounds(nr, nc)){
                        Cell neighbor = cells[nr][nc];
                        if (neighbor.getState() == CellState.HIDDEN){
                            boolean safe = revealCell(nr, nc);
                            if (!safe) return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Chuyển đổi cờ cho ô tại (row, col).
     * Cập nhật flagCount — FR-09, FR-15, FR-16.
     *
     * @param row hàng
     * @param col cột
     */
    public void toggleFlag(int row, int col) {
        // TODO: gọi cell.toggleFlag(), cập nhật flagCount (+1 hoặc -1)
        if (!inBounds(row, col)) return;
        Cell cell = cells[row][col];
        if (!cell.isRevealed()){
            boolean wasFlagged = cell.isFlagged();
            cell.toggleFlag();
            if (!wasFlagged && cell.isFlagged()){
                flagCount.set(flagCount.get() + 1);
            }else if (wasFlagged && !cell.isFlagged()){
                flagCount.set(flagCount.get() - 1);
            }
        }
    }

    // ── Game Status ───────────────────────────────────────────

    /**
     * Kiểm tra điều kiện thắng.
     * Thắng khi tất cả ô không phải mìn đều đã được mở — FR-18.
     *
     * @return true nếu người chơi đã thắng
     */
    public boolean checkWin() {
        // TODO: đếm số ô chưa mở, so với totalMines
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Cell cell = cells[i][j];
                if (!cell.isMine() && !cell.isRevealed()) return false;
            }
        }
        return true;
    }

    /**
     * Lật ngửa toàn bộ mìn trên bàn cờ khi người chơi thua.
     * FR-17
     */
    public void revealAllMines() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = cells[r][c];
                if (cell.isMine() && !cell.isRevealed()) {
                    cell.reveal();
                }
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────

    /**
     * Kiểm tra (row, col) có nằm trong phạm vi bàn cờ không.
     *
     * @param row hàng cần kiểm tra
     * @param col cột cần kiểm tra
     * @return true nếu hợp lệ
     */
    private boolean inBounds(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }

    // ── Getters ───────────────────────────────────────────────

    /**
     * Lấy ô tại vị trí (row, col).
     *
     * @param row hàng
     * @param col cột
     * @return đối tượng {@link Cell}
     */
    public Cell getCell(int row, int col) {
        return cells[row][col];
    }

    /** @return số hàng của bàn cờ */
    public int getRows() {
        return rows;
    }

    /** @return số cột của bàn cờ */
    public int getCols() {
        return cols;
    }

    /** @return tổng số mìn */
    public int getTotalMines() {
        return totalMines;
    }

    /**
     * Trả về IntegerProperty của số cờ đã cắm.
     * HeaderView bind vào property này để tự cập nhật — Observer pattern, FR-15.
     *
     * @return flagCount property
     */
    public IntegerProperty flagCountProperty() {
        return flagCount;
    }

    /** @return giá trị số cờ hiện tại */
    public int getFlagCount() {
        return flagCount.get();
    }

    /**
     * Trả về danh sách các ô vừa được reveal trong lần gọi {@code revealCell} gần nhất.
     * Mỗi phần tử là int[2] = {row, col}.
     */
    public List<int[]> getLastRevealedPositions() {
        return lastRevealedPositions;
    }
    public void clearLastRevealed() {
        lastRevealedPositions.clear();
    }
}

