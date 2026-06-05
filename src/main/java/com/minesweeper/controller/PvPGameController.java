package com.minesweeper.controller;

import com.minesweeper.model.Board;
import com.minesweeper.model.GameState;
import com.minesweeper.model.GameTimer;
import com.minesweeper.view.PvPBoardView;
import com.minesweeper.view.PvPSetupDialog;

import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Controller điều phối toàn bộ chế độ PvP cục bộ.
 *
 * Đảm nhận các bước:
 *   [UC5.4.4] Khởi tạo GameView PvP chia đôi màn hình
 *   Khởi tạo hai Board độc lập với bố trí mìn riêng biệt, safe-first-click cho mỗi người
 *   [UC5.4.6] Đăng ký Key Listeners điều khiển cursor P1 (WASD/Space/F) và P2 (Arrow/Enter/P)
 *   [UC5.4.7] Đặt trạng thái PVP_SPLIT_START, kích hoạt hai đồng hồ
 */
public class PvPGameController {

    // ── Model ─────────────────────────────────────────────────
    private Board boardP1;                  // Board độc lập Player 1
    private Board boardP2;                  // Board độc lập Player 2
    private GameState stateP1;             // Trạng thái game Player 1
    private GameState stateP2;             // Trạng thái game Player 2

    // ── Timer ─────────────────────────────────────────────────
    private final GameTimer timerP1 = new GameTimer(); // Đồng hồ riêng Player 1
    private final GameTimer timerP2 = new GameTimer(); // Đồng hồ riêng Player 2

    // ── Config từ dialog ──────────────────────────────────────
    private final Difficulty difficulty;
    private final String player1Name;
    private final String player2Name;

    // ── View ──────────────────────────────────────────────────
    private final PvPBoardView pvpView;

    // Callback để báo MainView khi trận kết thúc (tuỳ chọn mở rộng sau)
    private Runnable onMatchEnd;

    /**
     * [UC5.4.4] Khởi tạo controller với config từ dialog.
     * Tạo view, sinh board, đăng ký key, bắt đầu trận.
     */
    public PvPGameController(PvPSetupDialog.Config config) {
        this.difficulty  = config.difficulty;
        this.player1Name = config.player1Name;
        this.player2Name = config.player2Name;

        // Tạo view chia đôi màn hình
        this.pvpView = new PvPBoardView(player1Name, player2Name);

        initMatch(); // Khởi tạo trận đấu
    }

    // ── Khởi tạo trận ──────────────────────────────────────

    /**
     * Khởi tạo hai Board độc lập với bố trí mìn riêng biệt cho mỗi người chơi.
     * Mìn sẽ được đặt ngẫu nhiên khi người chơi click ô đầu tiên (safe-first-click),
     * giống chế độ chơi cơ bản — đảm bảo ô đầu tiên không bao giờ là mìn.
     * [UC5.4.7] Đặt trạng thái PVP_SPLIT_START, kích hoạt hai đồng hồ.
     */
    public void initMatch() {
        int rows  = difficulty.getRows();
        int cols  = difficulty.getCols();
        int mines = difficulty.getMines();

        // Tạo hai Board hoàn toàn độc lập, mỗi người có bố trí mìn riêng.
        // Mìn chưa được đặt — sẽ được đặt ngẫu nhiên khi mỗi người click lần đầu
        // (safe-first-click: ô đầu tiên và 8 ô xung quanh đảm bảo không có mìn).
        boardP1 = new Board(rows, cols, mines);
        boardP2 = new Board(rows, cols, mines);

        // Reset trạng thái mỗi người chơi về PVP_SPLIT_START
        stateP1 = GameState.PVP_SPLIT_START; // [UC5.4.7]
        stateP2 = GameState.PVP_SPLIT_START; // [UC5.4.7]

        // Reset đồng hồ trước khi dùng lại
        timerP1.reset();
        timerP2.reset();

        // [UC5.4.4] Xây lưới hiển thị theo kích thước board
        pvpView.buildBoards(rows, cols);

        // Bind đồng hồ lên nhãn hiển thị
        pvpView.bindTimerP1(timerP1.elapsedSecondsProperty());
        pvpView.bindTimerP2(timerP2.elapsedSecondsProperty());

        // Bind số mìn còn lại (totalMines - flagCount) cho mỗi bên
        IntegerProperty remainP1 = new SimpleIntegerProperty();
        remainP1.bind(Bindings.subtract(boardP1.getTotalMines(), boardP1.flagCountProperty()));
        pvpView.bindMinesP1(remainP1);

        IntegerProperty remainP2 = new SimpleIntegerProperty();
        remainP2.bind(Bindings.subtract(boardP2.getTotalMines(), boardP2.flagCountProperty()));
        pvpView.bindMinesP2(remainP2);

        // [UC5.4.7] Kích hoạt cả hai đồng hồ đồng thời ngay khi trận bắt đầu
        timerP1.start();
        timerP2.start();
    }

    // ── Key Listener ────────────────────────────────────────

    /**
     * [UC5.4.6] Đăng ký Key Listener lên Scene để nhận phím từ cả hai người chơi.
     * Gọi sau khi Scene đã được tạo và PvPBoardView đã được thêm vào.
     * Dùng addEventFilter để chặn trước khi các Button tiêu thụ phím Space/Enter.
     */
    public void registerKeyListeners(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
    }

    /**
     * [UC5.4.6] Phân loại và xử lý phím bấm của từng người chơi:
     *   Player 1: W/A/S/D = di chuyển, Space = mở ô, F = cắm cờ
     *   Player 2: Arrow Keys = di chuyển, Enter = mở ô, P = cắm cờ
     */
    private void handleKeyPressed(KeyEvent e) {
        KeyCode key = e.getCode();
        boolean handled = true;

        // ── Player 1 Controls (WASD / Space / F) ────────────
        switch (key) {
            case W     -> moveCursorP1(-1,  0); // Lên
            case S     -> moveCursorP1( 1,  0); // Xuống
            case A     -> moveCursorP1( 0, -1); // Trái
            case D     -> moveCursorP1( 0,  1); // Phải
            case SPACE -> revealP1();            // Mở ô tại cursor P1
            case F     -> flagP1();              // Cắm/bỏ cờ tại cursor P1

            // ── Player 2 Controls (Arrow / Enter / P) ───────
            case UP    -> moveCursorP2(-1,  0); // Lên
            case DOWN  -> moveCursorP2( 1,  0); // Xuống
            case LEFT  -> moveCursorP2( 0, -1); // Trái
            case RIGHT -> moveCursorP2( 0,  1); // Phải
            case ENTER -> revealP2();            // Mở ô tại cursor P2
            case P     -> flagP2();              // Cắm/bỏ cờ tại cursor P2

            default -> handled = false; // Phím khác → bỏ qua
        }

        if (handled) {
            e.consume(); // Ngăn sự kiện lan tiếp (tránh xung đột)
        }
    }

    // ── Di chuyển cursor ────────────────────────────────────

    /** [UC5.4.6] Di chuyển con trỏ Player 1 */
    private void moveCursorP1(int dr, int dc) {
        pvpView.moveP1Cursor(dr, dc);
    }

    /** [UC5.4.6] Di chuyển con trỏ Player 2 */
    private void moveCursorP2(int dr, int dc) {
        pvpView.moveP2Cursor(dr, dc);
    }

    // ── Mở ô ────────────────────────────────────────────────

    /** [UC5.4.6] Player 1 nhấn Space → mở ô tại cursor */
    private void revealP1() {
        if (stateP1 != GameState.PVP_SPLIT_START) return; // Không nhận thêm input khi đã kết thúc

        int[] cursor = pvpView.getCursorP1();
        int row = cursor[0], col = cursor[1];

        boardP1.getLastRevealedPositions().clear();
        boolean safe = boardP1.revealCell(row, col); // Mở ô trên model P1

        // Cập nhật tất cả ô vừa được reveal (bao gồm flood-fill)
        for (int[] pos : boardP1.getLastRevealedPositions())
            pvpView.updateCellP1(pos[0], pos[1], boardP1.getCell(pos[0], pos[1]));
        pvpView.updateCellP1(row, col, boardP1.getCell(row, col));

        if (!safe) {
            handleLoseP1(row, col); // Người chơi 1 thua
        } else if (boardP1.checkWin()) {
            handleWinP1(); // Người chơi 1 thắng
        }
    }

    /** [UC5.4.6] Player 2 nhấn Enter → mở ô tại cursor */
    private void revealP2() {
        if (stateP2 != GameState.PVP_SPLIT_START) return;

        int[] cursor = pvpView.getCursorP2();
        int row = cursor[0], col = cursor[1];

        boardP2.getLastRevealedPositions().clear();
        boolean safe = boardP2.revealCell(row, col); // Mở ô trên model P2

        for (int[] pos : boardP2.getLastRevealedPositions())
            pvpView.updateCellP2(pos[0], pos[1], boardP2.getCell(pos[0], pos[1]));
        pvpView.updateCellP2(row, col, boardP2.getCell(row, col));

        if (!safe) {
            handleLoseP2(row, col);
        } else if (boardP2.checkWin()) {
            handleWinP2();
        }
    }

    // ── Cắm cờ ──────────────────────────────────────────────

    /** [UC5.4.6] Player 1 nhấn F → cắm/bỏ cờ tại cursor */
    private void flagP1() {
        if (stateP1 != GameState.PVP_SPLIT_START) return;
        int[] cursor = pvpView.getCursorP1();
        boardP1.toggleFlag(cursor[0], cursor[1]);
        pvpView.updateCellP1(cursor[0], cursor[1], boardP1.getCell(cursor[0], cursor[1]));
    }

    /** [UC5.4.6] Player 2 nhấn P → cắm/bỏ cờ tại cursor */
    private void flagP2() {
        if (stateP2 != GameState.PVP_SPLIT_START) return;
        int[] cursor = pvpView.getCursorP2();
        boardP2.toggleFlag(cursor[0], cursor[1]);
        pvpView.updateCellP2(cursor[0], cursor[1], boardP2.getCell(cursor[0], cursor[1]));
    }

    // ── Xử lý kết quả trận ─────────────────────────────────

    /** Player 1 thắng */
    private void handleWinP1() {
        stateP1 = GameState.WIN;
        timerP1.pause(); // Dừng đồng hồ P1
    }

    /** Player 2 thắng */
    private void handleWinP2() {
        stateP2 = GameState.WIN;
        timerP2.pause();
    }

    /** Player 1 thua (mở trúng mìn) */
    private void handleLoseP1(int explodedRow, int explodedCol) {
        stateP1 = GameState.LOSE;
        timerP1.pause(); // Dừng đồng hồ P1
        boardP1.revealAllMines();
        pvpView.revealAllMinesP1(boardP1, explodedRow, explodedCol); // Lật toàn bộ mìn P1
    }

    /** Player 2 thua (mở trúng mìn) */
    private void handleLoseP2(int explodedRow, int explodedCol) {
        stateP2 = GameState.LOSE;
        timerP2.pause();
        boardP2.revealAllMines();
        pvpView.revealAllMinesP2(boardP2, explodedRow, explodedCol);
    }

    // ── Getter ──────────────────────────────────────────────

    /** Trả về PvPBoardView để MainView nhúng vào scene */
    public PvPBoardView getPvPBoardView() { return pvpView; }

    /** Đăng ký callback khi trận kết thúc (mở rộng UC sau) */
    public void setOnMatchEnd(Runnable handler) { this.onMatchEnd = handler; }
}
