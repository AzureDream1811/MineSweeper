package com.minesweeper.controller;

import com.minesweeper.model.Board;
import com.minesweeper.model.GameState;
import com.minesweeper.model.GameTimer;
import com.minesweeper.model.PvPRequestType;
import com.minesweeper.view.PvPBoardView;
import com.minesweeper.view.PvPSetupDialog;

import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Controller điều phối toàn bộ chế độ PvP cục bộ.
 *
 * Đảm nhận các bước:
 *   - Khởi tạo GameView PvP chia đôi màn hình
 *   - Sinh mine layout ngẫu nhiên dùng chung cho hai board
 *   - Đăng ký Key Listeners điều khiển cursor P1 (WASD/Space/F) và P2 (Arrow/Enter/P)
 *   - Đặt trạng thái PVP_SPLIT_START, kích hoạt hai đồng hồ
 */
public class PvPGameController {

    // ── Model ─────────────────────────────────────────────────
    private Board boardP1;                  // Board độc lập Player 1
    private Board boardP2;                  // Board độc lập Player 2
    private GameState stateP1;             // Trạng thái game Player 1
    private GameState stateP2;             // Trạng thái game Player 2

    private boolean p1Started;
    private boolean p2Started;
    
    // Request state
    private boolean waitingConfirmation = false;
    private int requestingPlayer = 0;
    private PvPRequestType pendingRequest = null;

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
     * Sinh mine layout ngẫu nhiên và khởi tạo hai Board độc lập
     * dùng chung layout đó để đảm bảo công bằng tuyệt đối.
     * Đặt trạng thái PVP_SPLIT_START, kích hoạt hai đồng hồ.
     */
    public void initMatch() {
        int rows  = difficulty.getRows();
        int cols  = difficulty.getCols();
        int mines = difficulty.getMines();

        // Sinh mine layout ngẫu nhiên chung cho cả hai board
        boolean[][] sharedLayout = generateMineLayout(rows, cols, mines);

        // Tạo hai Board độc lập nhưng dùng cùng layout mìn
        boardP1 = new Board(rows, cols, mines, sharedLayout);
        boardP2 = new Board(rows, cols, mines, sharedLayout);

        // Reset trạng thái mỗi người chơi về PVP_SPLIT_START
        stateP1 = GameState.PVP_SPLIT_START;
        stateP2 = GameState.PVP_SPLIT_START;

        p1Started = false;
        p2Started = false;

        // Reset đồng hồ trước khi dùng lại
        timerP1.reset();
        timerP2.reset();

        // Xây lưới hiển thị theo kích thước board
        pvpView.buildBoards(rows, cols);

        // Bind đồng hồ lên nhãn hiển thị
        pvpView.getHeaderView().bindTimerP1(timerP1.elapsedSecondsProperty());
        pvpView.getHeaderView().bindTimerP2(timerP2.elapsedSecondsProperty());

        pvpView.getHeaderView().setOnReset(() -> requestAction(1, PvPRequestType.RESET));
        pvpView.getHeaderView().setOnPause(() -> {
            if (stateP1 == GameState.PAUSED) {
                requestAction(1, PvPRequestType.RESUME);
            } else {
                requestAction(1, PvPRequestType.PAUSE);
            }
        });

        // Bind số mìn còn lại (totalMines - flagCount) cho mỗi bên
        IntegerProperty remainP1 = new SimpleIntegerProperty();
        remainP1.bind(Bindings.subtract(boardP1.getTotalMines(), boardP1.flagCountProperty()));
        pvpView.getHeaderView().bindMinesP1(remainP1);

        IntegerProperty remainP2 = new SimpleIntegerProperty();
        remainP2.bind(Bindings.subtract(boardP2.getTotalMines(), boardP2.flagCountProperty()));
        pvpView.getHeaderView().bindMinesP2(remainP2);

        // Kích hoạt cả hai đồng hồ đồng thời ngay khi trận bắt đầu
        timerP1.start();
        timerP2.start();
    }

    /**
     * [UC5.4.5] Sinh ngẫu nhiên mine layout dạng boolean[][].
     * Không áp dụng safe-first-click (fair layout trước khi chơi).
     */
    private boolean[][] generateMineLayout(int rows, int cols, int totalMines) {
        // Tạo danh sách tất cả ô, xáo trộn, lấy `totalMines` ô đầu
        List<int[]> allCells = new ArrayList<>(rows * cols);
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                allCells.add(new int[]{ r, c });

        Collections.shuffle(allCells, new Random()); // Xáo trộn ngẫu nhiên

        boolean[][] layout = new boolean[rows][cols];
        for (int i = 0; i < totalMines; i++) {
            int[] pos = allCells.get(i);
            layout[pos[0]][pos[1]] = true; // Đánh dấu ô có mìn
        }
        return layout;
    }

    private void requestAction(int player, PvPRequestType type) {
        if (stateP1 == GameState.WIN || stateP1 == GameState.LOSE) return;
        
        requestingPlayer = player;
        pendingRequest = type;
        waitingConfirmation = true;
        
        String action = switch (type) {
            case RESET -> "Reset";
            case PAUSE -> "Pause";
            case RESUME -> "Resume";
        };
        pvpView.showRequest("Player " + player + " muon " + action + ". P" + (player == 1 ? 2 : 1) + " (C/O=Dong y, X/N=Tu choi)");
        timerP1.pause();
        timerP2.pause();
    }

    private void confirmRequest(int player) {
        if (!waitingConfirmation || player == requestingPlayer) return; 
        
        waitingConfirmation = false;
        switch (pendingRequest) {
            case RESET -> initMatch();
            case PAUSE -> {
                stateP1 = GameState.PAUSED;
                stateP2 = GameState.PAUSED;
                pvpView.showRequest("Game Paused");
                timerP1.pause();
                timerP2.pause();
            }
            case RESUME -> {
                stateP1 = GameState.PLAYING;
                stateP2 = GameState.PLAYING;
                pvpView.showRequest(""); // clear request text
                timerP1.resume();
                timerP2.resume();
            }
        }
    }

    private void rejectRequest(int player) {
        if (!waitingConfirmation || player == requestingPlayer) return;
        waitingConfirmation = false;
        pvpView.showRequest("Yeu cau bi tu choi!");
        if (stateP1 != GameState.PAUSED) {
            timerP1.resume();
            timerP2.resume();
        }
    }

    // ── Key Listener ────────────────────────────────────────

    /**
     * Đăng ký Key Listener lên Scene để nhận phím từ cả hai người chơi.
     * Gọi sau khi Scene đã được tạo và PvPBoardView đã được thêm vào.
     * Dùng addEventFilter để chặn trước khi các Button tiêu thụ phím Space/Enter.
     */
    public void registerKeyListeners(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
    }

    /**
     * Phân loại và xử lý phím bấm của từng người chơi:
     *   Player 1: W/A/S/D = di chuyển, Space = mở ô, F = cắm cờ
     *   Player 2: Arrow Keys = di chuyển, Enter = mở ô, P = cắm cờ
     */
    private void handleKeyPressed(KeyEvent e) {
        KeyCode key = e.getCode();
        if (waitingConfirmation) {
            switch (key) {
                case C -> confirmRequest(1);
                case X -> rejectRequest(1);
                case O -> confirmRequest(2);
                case N -> rejectRequest(2);
                default -> {}
            }
            e.consume();
            return;
        }
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
            case NUMPAD1 -> requestAction(2, PvPRequestType.RESET);
            case NUMPAD2 -> requestAction(2, PvPRequestType.PAUSE);
            case NUMPAD3 -> requestAction(2, PvPRequestType.RESUME);

            case C -> confirmRequest(1); // Player 1 xác nhận
            case X -> rejectRequest(1); // Player 1 từ chối
            case O -> confirmRequest(2); // Player 2 xác nhận
            case N -> rejectRequest(2); // Player 2 từ chối
            default -> handled = false; // Phím khác → bỏ qua
        }

        if (handled) {
            e.consume(); // Ngăn sự kiện lan tiếp (tránh xung đột)
        }
    }

    // ── Di chuyển cursor ────────────────────────────────────

    /** Di chuyển con trỏ Player 1 */
    private void moveCursorP1(int dr, int dc) {
        pvpView.moveP1Cursor(dr, dc);
    }

    /** Di chuyển con trỏ Player 2 */
    private void moveCursorP2(int dr, int dc) {
        pvpView.moveP2Cursor(dr, dc);
    }

    // ── Mở ô ────────────────────────────────────────────────

    /** Player 1 nhấn Space → mở ô tại cursor */
    private void revealP1() {
        if (stateP1 != GameState.PVP_SPLIT_START && stateP1 != GameState.PLAYING) return; // Không nhận thêm input khi đã kết thúc

        p1Started = true;

        int[] cursor = pvpView.getCursorP1();
        int row = cursor[0], col = cursor[1];

        boardP1.getLastRevealedPositions().clear();
        boolean safe = boardP1.revealCell(row, col); // Mở ô trên model P1

        // Cập nhật tất cả ô vừa được reveal (bao gồm flood-fill)
        for (int[] pos : boardP1.getLastRevealedPositions())
            pvpView.updateCellP1(pos[0], pos[1], boardP1.getCell(pos[0], pos[1]));
        pvpView.updateCellP1(row, col, boardP1.getCell(row, col));

        // [13.1.1] Hệ thống tiếp nhận trạng thái sau khi một người chơi vừa thực hiện mở ô (revealCell).
        checkMatchResult(true, safe, row, col);
    }

    /** [UC5.4.6] Player 2 nhấn Enter → mở ô tại cursor */
    private void revealP2() {
        if (stateP2 != GameState.PVP_SPLIT_START && stateP2 != GameState.PLAYING) return;

        p2Started = true;

        int[] cursor = pvpView.getCursorP2();
        int row = cursor[0], col = cursor[1];

        boardP2.getLastRevealedPositions().clear();
        boolean safe = boardP2.revealCell(row, col); // Mở ô trên model P2

        for (int[] pos : boardP2.getLastRevealedPositions())
            pvpView.updateCellP2(pos[0], pos[1], boardP2.getCell(pos[0], pos[1]));
        pvpView.updateCellP2(row, col, boardP2.getCell(row, col));

        // [13.1.1] Hệ thống tiếp nhận trạng thái sau khi một người chơi vừa thực hiện mở ô (revealCell).
        checkMatchResult(false, safe, row, col);
    }

    // ── Cắm cờ ──────────────────────────────────────────────

    /** [UC5.4.6] Player 1 nhấn F → cắm/bỏ cờ tại cursor */
    private void flagP1() {
        if (stateP1 != GameState.PVP_SPLIT_START && stateP1 != GameState.PLAYING) return;
        if (!p1Started) {
            pvpView.showRequest("Player 1 phải mở ô đầu tiên trước khi cắm cờ");
            return;
        }

        int[] cursor = pvpView.getCursorP1();
        boardP1.toggleFlag(cursor[0], cursor[1]);
        pvpView.updateCellP1(cursor[0], cursor[1], boardP1.getCell(cursor[0], cursor[1]));
    }

    /** [UC5.4.6] Player 2 nhấn P → cắm/bỏ cờ tại cursor */
    private void flagP2() {
        if (stateP2 != GameState.PVP_SPLIT_START && stateP2 != GameState.PLAYING) return;
        if (!p2Started) {
            pvpView.showRequest("Player 2 phải mở ô đầu tiên trước khi cắm cờ");
            return;
        }
        int[] cursor = pvpView.getCursorP2();
        boardP2.toggleFlag(cursor[0], cursor[1]);
        pvpView.updateCellP2(cursor[0], cursor[1], boardP2.getCell(cursor[0], cursor[1]));
    }

    // ── Xử lý kết quả trận ─────────────────────────────────

    /**
     * [UC-13] Check Game Result PvP (Kiểm tra, đối chiếu song song kết quả trận đấu giữa 2 người chơi).
     * @param isP1   Thao tác vừa rồi là của Player 1 (true) hay Player 2 (false)
     * @param safe   Trạng thái an toàn sau khi reveal
     * @param row    Hàng mở cuối cùng (dùng khi nổ mìn)
     * @param col    Cột mở cuối cùng (dùng khi nổ mìn)
     */
    private void checkMatchResult(boolean isP1, boolean safe, int row, int col) {
        if (stateP1 != GameState.LOSE && stateP1 != GameState.WIN) stateP1 = GameState.PLAYING;
        if (stateP2 != GameState.LOSE && stateP2 != GameState.WIN) stateP2 = GameState.PLAYING;

        if (isP1) {
            if (!safe) {
                // 13.1.3: Nếu người vừa thao tác kích nổ mìn, hệ thống chuyển trạng thái sang LOSE nhưng không dừng game ngay; tiến hành đối chiếu.
                stateP1 = GameState.LOSE;
                boardP1.revealAllMines();
                pvpView.revealAllMinesP1(boardP1, row, col);

                if (stateP2 == GameState.PVP_SPLIT_START || stateP2 == GameState.PLAYING) {
                    // 13.1.4: Nếu đối thủ vẫn đang trong trạng thái PLAYING, hệ thống xác định đối thủ là người chiến thắng do sống sót lâu hơn.
                    stateP2 = GameState.WIN;
                    finishMatch();
                } else if (stateP2 == GameState.LOSE) {
                    // 13.2.1: Alternative Flow - Nếu cả hai người chơi cùng kích nổ mìn, hệ thống ghi nhận kết quả là Hòa (DRAW).
                    finishMatch();
                }
            } else if (boardP1.checkWin()) {
                // 13.1.2: Hệ thống kiểm tra điều kiện dọn sạch mìn. Nếu true, hệ thống lập tức xác định người này thắng tuyệt đối (Win).
                stateP1 = GameState.WIN;
                stateP2 = GameState.LOSE;
                finishMatch();
            }
        } else {
            if (!safe) {
                // 13.1.3: Nếu người vừa thao tác kích nổ mìn...
                stateP2 = GameState.LOSE;
                boardP2.revealAllMines();
                pvpView.revealAllMinesP2(boardP2, row, col);

                if (stateP1 == GameState.PVP_SPLIT_START || stateP1 == GameState.PLAYING) {
                    // 13.1.4: Xác định đối thủ là người chiến thắng do sống sót lâu hơn.
                    stateP1 = GameState.WIN;
                    finishMatch();
                } else if (stateP1 == GameState.LOSE) {
                    // 13.2.1: Alternative Flow - Hòa.
                    finishMatch();
                }
            } else if (boardP2.checkWin()) {
                // 13.1.2: Hệ thống lập tức xác định người này thắng tuyệt đối do hoàn thành trước.
                stateP2 = GameState.WIN;
                stateP1 = GameState.LOSE;
                finishMatch();
            }
        }
    }

    private void finishMatch() {
        // 13.1.5: Hệ thống đóng băng cả 2 bàn cờ, dừng toàn bộ timer và chuyển giao kết quả sang UC-14.
        timerP1.pause();
        timerP2.pause();

        int winner = 0; // 0 = Hoà
        if (stateP1 == GameState.WIN && stateP2 == GameState.LOSE) winner = 1;
        if (stateP2 == GameState.WIN && stateP1 == GameState.LOSE) winner = 2;

        com.minesweeper.view.GameResultView resultView = new com.minesweeper.view.GameResultView();
        com.minesweeper.view.GameResultView.Action action = resultView.showPvP(
                (javafx.stage.Stage) pvpView.getRoot().getScene().getWindow(),
                winner,
                player1Name, player2Name,
                timerP1.getElapsedSeconds(), timerP2.getElapsedSeconds(),
                boardP1.flagCountProperty().get(), boardP2.flagCountProperty().get()
        );

        if (action == com.minesweeper.view.GameResultView.Action.RESTART) {
            initMatch();
        } else {
            if (onMatchEnd != null) onMatchEnd.run();
        }
    }

    // ── Getter ──────────────────────────────────────────────

    /** Trả về PvPBoardView để MainView nhúng vào scene */
    public PvPBoardView getPvPBoardView() { return pvpView; }

    /** Đăng ký callback khi trận kết thúc (mở rộng UC sau) */
    public void setOnMatchEnd(Runnable handler) { this.onMatchEnd = handler; }
}
