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

                // UC-09 - 9.1.4:
                // Đảm bảo ô vừa được chọn không chứa mìn.
                // Code hiện tại còn loại trừ thêm 8 ô xung quanh để click đầu tiên an toàn hơn.
                if (Math.abs(r - safeRow) <= 1 && Math.abs(c - safeCol) <= 1) {
                    continue;
                }

                candidates.add(new int[]{r, c});
            }
        }

        // Random vị trí mìn.
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

        // Sau khi đặt mìn, tính số mìn xung quanh cho từng ô.
        // Phục vụ UC-09 - 9.1.7.
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
        // UC-09 - 9.1.4:
        // Nếu đây là lượt mở ô đầu tiên,
        // hệ thống khởi tạo vị trí mìn và đảm bảo ô vừa được chọn không chứa mìn.
        if (firstClick) {
            placeMines(row, col);
            firstClick = false;
        }

        // Kiểm tra tọa độ nằm trong bàn cờ.
        // Nếu ngoài phạm vi thì không làm thay đổi bàn chơi.
        if (!inBounds(row, col)) return true;

        Cell cell = cells[row][col];

        // UC-09 - 9.3.1:
        // Nếu ô đã được mở hoặc đang được đánh dấu cờ,
        // hệ thống không thực hiện thao tác mở ô.
        if (cell.isRevealed() || cell.isFlagged()) {
            return true;
        }

        // UC-09 - 9.1.5:
        // Hệ thống kiểm tra nội dung của ô được chọn.
        //
        // UC-09 - 9.2.1:
        // Nếu ô được chọn chứa mìn, hệ thống xác định người chơi thua ván.
        if (cell.isMine()) {
            cell.reveal();
            lastRevealedPositions.add(new int[]{row, col});

            // Trả về false để GameController gọi handleLose(row, col)
            // tương ứng với UC-17 - Trigger Explosion.
            return false;
        }

        // UC-09 - 9.1.6:
        // Nếu ô được chọn không chứa mìn,
        // hệ thống chuyển ô sang trạng thái đã mở.
        cell.reveal();
        lastRevealedPositions.add(new int[]{row, col});

        // UC-09 - 9.1.8:
        // Nếu ô được mở không có mìn xung quanh,
        // hệ thống tự động mở các ô trống liền kề và các ô biên có số liên quan.
        if (cell.isBlank()) {
            floodFill(row, col);
        }

        // Trả về true nghĩa là thao tác mở ô an toàn.
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
        // UC-15 - 15.5.1:
        // Nếu vị trí bắt đầu nằm ngoài phạm vi bàn chơi,
        // hệ thống bỏ qua vị trí đó.
        if (!inBounds(row, col)) return;

        Deque<int[]> toVisit = new ArrayDeque<>();

        // UC-15 - 15.1.2:
        // Ô gốc đã được xác định là ô trống trong revealCell().
        // Tại đây, hệ thống đưa ô đó vào danh sách duyệt để bắt đầu mở rộng.
        toVisit.push(new int[]{row, col});

        while (!toVisit.isEmpty()) {
            int[] position = toVisit.pop();
            int currentRow = position[0];
            int currentCol = position[1];

            // Duyệt 8 ô lân cận xung quanh ô hiện tại.
            for (int deltaRow = -1; deltaRow <= 1; deltaRow++) {
                for (int deltaCol = -1; deltaCol <= 1; deltaCol++) {
                    if (deltaRow == 0 && deltaCol == 0) continue;

                    int neighborRow = currentRow + deltaRow;
                    int neighborCol = currentCol + deltaCol;

                    // UC-15 - 15.5.1:
                    // Nếu ô lân cận nằm ngoài phạm vi bàn chơi,
                    // hệ thống bỏ qua vị trí đó.
                    if (!inBounds(neighborRow, neighborCol)) continue;

                    Cell neighbor = cells[neighborRow][neighborCol];

                    // UC-15 - 15.3.1:
                    // Nếu ô lân cận đã được mở, hệ thống bỏ qua ô đó.
                    //
                    // UC-15 - 15.2.1:
                    // Nếu ô lân cận đang được đánh dấu cờ, hệ thống bỏ qua ô đó.
                    //
                    // Post-condition UC-15:
                    // Các ô chứa mìn không bị thay đổi.
                    if (neighbor.isRevealed()
                            || neighbor.isFlagged()
                            || neighbor.isMine()) {
                        continue;
                    }

                    // UC-15 - 15.1.3:
                    // Với mỗi ô lân cận hợp lệ, chưa được mở,
                    // chưa bị đánh dấu cờ và không chứa mìn, hệ thống mở ô đó.
                    neighbor.reveal();
                    lastRevealedPositions.add(new int[]{neighborRow, neighborCol});

                    // UC-15 - 15.1.4:
                    // Nếu ô lân cận là ô có số, hệ thống mở ô đó
                    // nhưng không tiếp tục mở rộng từ ô đó.
                    //
                    // Nếu ô lân cận cũng là ô trống,
                    // hệ thống tiếp tục mở rộng từ ô đó.
                    if (neighbor.isBlank()) {
                        toVisit.push(new int[]{neighborRow, neighborCol});
                    }
                }
            }
        }

        // UC-15 - 15.2.2 / 15.3.2 / 15.5.2:
        // Sau khi bỏ qua ô không hợp lệ, ô đã mở hoặc ô cắm cờ,
        // hệ thống tiếp tục kiểm tra các ô lân cận hợp lệ khác.
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
                if (inBounds(nr, nc)) {
                    if (cells[nr][nc].isFlagged()) {
                        FlaggedAround++;
                    }
                }
            }
        }
        if (FlaggedAround == cell.getAdjacentMines()) {
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    int nr = row + i;
                    int nc = col + j;
                    if (inBounds(nr, nc)) {
                        Cell neighbor = cells[nr][nc];
                        if (neighbor.getState() == CellState.HIDDEN) {
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
        if (!cell.isRevealed()) {
            boolean wasFlagged = cell.isFlagged();
            cell.toggleFlag();
            if (!wasFlagged && cell.isFlagged()) {
                flagCount.set(flagCount.get() + 1);
            } else if (wasFlagged && !cell.isFlagged()) {
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
        // UC-09 - 9.1.10:
        // Kiểm tra điều kiện thắng:
        // nếu còn ô không phải mìn nhưng chưa mở thì chưa thắng.
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Cell cell = cells[i][j];

                if (!cell.isMine() && !cell.isRevealed()) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Lật ngửa toàn bộ mìn trên bàn cờ khi người chơi thua.
     * FR-17
     */
    public void revealAllMines() {
        // UC-17 - 17.1.5:
        // Hệ thống hiển thị các ô chứa mìn trên bàn chơi.
        //
        // Ở tầng Model, phương thức này duyệt toàn bộ bàn cờ,
        // tìm các ô chứa mìn và chuyển chúng sang trạng thái REVEALED.
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

    /**
     * @return số hàng của bàn cờ
     */
    public int getRows() {
        return rows;
    }

    /**
     * @return số cột của bàn cờ
     */
    public int getCols() {
        return cols;
    }

    /**
     * @return tổng số mìn
     */
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

    /**
     * @return giá trị số cờ hiện tại
     */
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

