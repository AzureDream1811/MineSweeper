package com.minesweeper.model;

/**
 * Đại diện cho một ô đơn lẻ trên bàn cờ Minesweeper.
 * <p>
 * Mỗi ô biết vị trí của mình, có phải mìn không,
 * số mìn xung quanh, và trạng thái hiển thị hiện tại.
 * </p>
 */
public class Cell {

    // ── Fields ────────────────────────────────────────────────

    /** Chỉ số hàng của ô này trên bàn cờ */
    private final int row;

    /** Chỉ số cột của ô này trên bàn cờ */
    private final int col;

    /** True nếu ô này là mìn */
    private boolean isMine;

    /**
     * Số lượng mìn trong 8 ô xung quanh.
     * Giá trị từ 0–8. Bằng 0 nghĩa là ô trống (blank).
     * Không có ý nghĩa nếu {@code isMine == true}.
     */
    private int adjacentMines;

    /** Trạng thái hiển thị hiện tại của ô — FR-02, FR-09 */
    private CellState state;

    // ── Constructor ───────────────────────────────────────────

    /**
     * Tạo ô mới tại vị trí (row, col).
     * Mặc định: không phải mìn, 0 mìn xung quanh, trạng thái HIDDEN.
     *
     * @param row chỉ số hàng
     * @param col chỉ số cột
     */
    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
        this.isMine = false;
        this.adjacentMines = 0;
        this.state = CellState.HIDDEN;
    }

    // ── Mutations ─────────────────────────────────────────────

    /**
     * Mở ô này.
     * Chỉ có tác dụng khi ô đang ở trạng thái HIDDEN.
     * UR-01, FR-08
     */
    public void reveal() {
        if (state == CellState.HIDDEN) {
            state = CellState.REVEALED;
        }
    }

    /**
     * Chuyển đổi cờ cho ô.
     * HIDDEN → FLAGGED, FLAGGED → HIDDEN.
     * Không làm gì nếu ô đã REVEALED.
     * UR-02, UR-03, FR-09
     */
    public void toggleFlag() {
        // UC-1 - 1.1.2: Nếu chưa cắm cờ (HIDDEN), tiến hành cắm cờ (chuyển sang FLAGGED).
        if (state == CellState.HIDDEN) {
            state = CellState.FLAGGED;
        // UC-2 - 2.1.3: Nếu đang cắm cờ (FLAGGED), gỡ cờ chuyển ô về trạng thái HIDDEN.
        } else if (state == CellState.FLAGGED) {
            state = CellState.HIDDEN;
        } // UC-1 - 1.2.3 / UC-2 - 2.2.3: Nếu đã mở (REVEALED), từ chối thao tác.
    }

    /**
     * Đặt ô này là mìn.
     * Gọi bởi Board khi khởi tạo mìn — FR-12.
     */
    public void setMine() {
        this.isMine = true;
    }

    /**
     * Cập nhật số mìn xung quanh ô.
     * Gọi bởi Board sau khi đặt xong mìn — FR-13.
     *
     * @param count số mìn từ 0 đến 8
     */
    public void setAdjacentMines(int count) {
        if (count < 0) count = 0;
        if (count > 8) count = 8;
        this.adjacentMines = count;
    }

    // ── Queries ───────────────────────────────────────────────

    /**
     * @return trạng thái hiển thị hiện tại của ô
     */
    public CellState getState() {
        return state;
    }

    /**
     * @return true nếu ô đang được cắm cờ — UR-04
     */
    public boolean isFlagged() {
        return state == CellState.FLAGGED;
    }

    /**
     * @return true nếu ô đã được mở
     */
    public boolean isRevealed() {
        return state == CellState.REVEALED;
    }

    /**
     * @return true nếu ô là mìn
     */
    public boolean isMine() {
        return isMine;
    }

    /**
     * @return true nếu ô là ô trống (không phải mìn, 0 mìn xung quanh) — FR-14
     */
    public boolean isBlank() {
        return !isMine && adjacentMines == 0;
    }

    /**
     * @return số mìn trong 8 ô xung quanh — FR-13
     */
    public int getAdjacentMines() {
        return adjacentMines;
    }

    /**
     * @return chỉ số hàng
     */
    public int getRow() {
        return row;
    }

    /**
     * @return chỉ số cột
     */
    public int getCol() {
        return col;
    }

    // ── Object overrides ──────────────────────────────────────

    @Override
    public String toString() {
        return String.format("Cell[%d][%d] mine=%b adj=%d state=%s",
                row, col, isMine, adjacentMines, state);
    }
}