package com.minesweeper.controller;

import com.minesweeper.model.Board;
import com.minesweeper.model.GameState;
import com.minesweeper.model.GameTimer;
import com.minesweeper.model.PvPRequestType;
import com.minesweeper.view.PvPBoardView;
import com.minesweeper.view.PvPSetupDialog;
import com.minesweeper.view.GameResultView;

import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Controller điều phối toàn bộ chế độ PvP cục bộ.
 * Đã sửa lỗi ẩn màn hình khi Pause: Khi đồng ý Pause, ẩn Overlay để lộ rõ bàn cờ và Header,
 * cho phép người chơi click nút Resume hoặc bấm phím tắt hệ thống để tiếp tục giải mìn.
 */
public class PvPGameController {

    // ── Model ─────────────────────────────────────────────────
    private Board boardP1;                  // Board độc lập Player 1
    private Board boardP2;                  // Board độc lập Player 2
    private GameState stateP1;              // Trạng thái game Player 1
    private GameState stateP2;              // Trạng thái game Player 2

    // Quản lý số lượng người chơi tích cực trên sân để điều phối luồng chơi đơn độc
    private int activePlayersCount = 2;

    private boolean p1Started;
    private boolean p2Started;

    // Request state (Giữ nguyên cấu trúc giao thức của bản chính)
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

    // Callback để báo MainView khi trận kết thúc
    private Runnable onMatchEnd;

    /**
     * [UC5.4.4] Khởi tạo controller với config từ dialog.
     */
    public PvPGameController(PvPSetupDialog.Config config) {
        this.difficulty = config.difficulty;
        this.player1Name = config.player1Name;
        this.player2Name = config.player2Name;

        this.pvpView = new PvPBoardView(player1Name, player2Name);

        initMatch(); // Khởi tạo trận đấu
    }

    // ── Khởi tạo trận ──────────────────────────────────────

    public void initMatch() {
        int rows = difficulty.getRows();
        int cols = difficulty.getCols();
        int mines = difficulty.getMines();

        boolean[][] sharedLayout = generateMineLayout(rows, cols, mines);

        boardP1 = new Board(rows, cols, mines, sharedLayout);
        boardP2 = new Board(rows, cols, mines, sharedLayout);

        stateP1 = GameState.PVP_SPLIT_START;
        stateP2 = GameState.PVP_SPLIT_START;

        p1Started = false;
        p2Started = false;
        activePlayersCount = 2;

        timerP1.reset();
        timerP2.reset();

        pvpView.buildBoards(rows, cols);

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

        IntegerProperty remainP1 = new SimpleIntegerProperty();
        remainP1.bind(Bindings.subtract(boardP1.getTotalMines(), boardP1.flagCountProperty()));
        pvpView.getHeaderView().bindMinesP1(remainP1);

        IntegerProperty remainP2 = new SimpleIntegerProperty();
        remainP2.bind(Bindings.subtract(boardP2.getTotalMines(), boardP2.flagCountProperty()));
        pvpView.getHeaderView().bindMinesP2(remainP2);

        timerP1.start();
        timerP2.start();
        pvpView.hideOverlay();
    }

    private boolean[][] generateMineLayout(int rows, int cols, int totalMines) {
        List<int[]> allCells = new ArrayList<>(rows * cols);
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                allCells.add(new int[]{r, c});

        Collections.shuffle(allCells, new Random());

        boolean[][] layout = new boolean[rows][cols];
        for (int i = 0; i < totalMines; i++) {
            int[] pos = allCells.get(i);
            layout[pos[0]][pos[1]] = true;
        }
        return layout;
    }

    // ── Xử lý gửi yêu cầu (Pause / Resume / Reset) ──

    private void requestAction(int player, PvPRequestType type) {
        // Nếu game đã kết thúc thắng thua toàn cục thì không nhận lệnh hệ thống nữa
        if (stateP1 == GameState.WIN || stateP1 == GameState.LOSE || stateP2 == GameState.WIN || stateP2 == GameState.LOSE)
            return;

        // UC-7 - 7.6.1: Một người chơi nhấn nút Pause trên Header hoặc nhấn phím tắt yêu cầu Pause (T / NUMPAD2).
        // UC-7 - 7.6.2: Hệ thống nhận diện trạng thái hiện tại của cả hai người chơi đã là PAUSED (stateP1 == GameState.PAUSED).
        // UC-7 - 7.6.3: Hệ thống bỏ qua thao tác, không hiển thị Overlay và không làm thay đổi trạng thái trận đấu.
        if (type == PvPRequestType.PAUSE && stateP1 == GameState.PAUSED) return;
        // UC-8 - 8.6.1: Một người chơi nhấn nút Resume trên Header hoặc phím tắt yêu cầu Resume (Y / NUMPAD3).
        // UC-8 - 8.6.2: Hệ thống kiểm tra thấy trạng thái hiện tại không phải là tạm dừng (stateP1 != GameState.PAUSED).
        // UC-8 - 8.6.3: Hệ thống bỏ qua thao tác, không hiển thị Overlay và giữ nguyên trạng thái chơi hiện tại.
        if (type == PvPRequestType.RESUME && stateP1 != GameState.PAUSED) return;

        // UC-6 - 6.6.1 / UC-7 - 7.7.1 / UC-8 - 8.7.1: Một người chơi gửi yêu cầu Reset / Pause / Resume.
        // UC-6 - 6.6.2 / UC-7 - 7.7.2 / UC-8 - 8.7.2: Hệ thống phát hiện waitingConfirmation == true.
        // UC-6 - 6.6.3 / UC-7 - 7.7.3 / UC-8 - 8.7.3: Hệ thống bỏ qua yêu cầu mới, duy trì Overlay và yêu cầu chờ xác nhận ban đầu.
        if (waitingConfirmation) return;

        requestingPlayer = player;
        pendingRequest = type;
        waitingConfirmation = true;

        // Hiển thị Overlay đồ họa để hỏi ý kiến đối thủ
        if (player == 1) {
            pvpView.showOverlay(
                    "Yêu cầu " + type,
                    "Player 1 yêu cầu " + type + ". Player 2 nhấn O (Đồng ý) hoặc N (Từ chối)."
            );
        } else {
            pvpView.showOverlay(
                    "Yêu cầu " + type,
                    "Player 2 yêu cầu " + type + ". Player 1 nhấn C (Đồng ý) hoặc X (Từ chối)."
            );
        }
        timerP1.pause();
        timerP2.pause();
    }

    // ── Xử lý Đồng ý (Confirm Request) ──

    private void confirmRequest(int player) {
        if (!waitingConfirmation) return;

        // LUỒNG XỬ LÝ 1: Phản hồi từ Overlay gối chồng khi một bên nổ mìn (Hỏi chơi tiếp bãi đơn độc)
        if (pendingRequest == PvPRequestType.PAUSE && activePlayersCount == 1) {
            pvpView.hideOverlay();
            waitingConfirmation = false;
            if (player == 1) {
                stateP1 = GameState.PLAYING;
                timerP1.resume();
                pvpView.showRequest(player1Name + " đang thực hiện lượt chơi tiếp...");
            } else {
                stateP2 = GameState.PLAYING;
                timerP2.resume();
                pvpView.showRequest(player2Name + " đang thực hiện lượt chơi tiếp...");
            }
            clearRequestVariables();
            return;
        }

        // LUỒNG XỬ LÝ 2: Phản hồi từ Overlay "Bạn đã dậm mìn" lúc chơi đơn độc kết thúc hẳn
        if (pendingRequest == PvPRequestType.RESET && activePlayersCount == 1) {
            pvpView.hideOverlay();
            clearRequestVariables();
            initMatch();
            return;
        }

        // LUỒNG XỬ LÝ 3: Các xử lý hệ thống khi đủ 2 người chơi bình thường
        // UC-6 - 6.5.1 / UC-7 - 7.5.1 / UC-8 - 8.5.1: Người gửi yêu cầu tự nhấn phím xác nhận (ví dụ: Player 1 nhấn C để xác nhận yêu cầu của chính mình).
        // UC-6 - 6.5.2 / UC-7 - 7.5.2 / UC-8 - 8.5.2: Hệ thống nhận diện người thực hiện thao tác trùng với người gửi yêu cầu ban đầu (confirmerPlayer == requesterPlayer).
        // UC-6 - 6.5.3 / UC-7 - 7.5.3 / UC-8 - 8.5.3: Hệ thống bỏ qua thao tác đó, không thay đổi trạng thái và tiếp tục hiển thị Overlay chờ xác nhận.
        if (player == requestingPlayer) return;

        waitingConfirmation = false;
        switch (pendingRequest) {
            case RESET -> initMatch();
            case PAUSE -> {
                // CHỈNH SỬA QUAN TRỌNG: Thiết lập trạng thái PAUSED ngầm cho Model và Controller
                stateP1 = GameState.PAUSED;
                stateP2 = GameState.PAUSED;
                timerP1.pause();
                timerP2.pause();

                // Thay đổi Icon Emoji mặt cười ở HeaderView sang trạng thái Pause tạm dừng
                pvpView.getHeaderView().setPauseEmoji(true);

                // ẨN OVERLAY ĐỂ KHÔNG CHE MẤT GAMEPLAY: Cho người chơi nhìn thấy bãi mìn và Header
                pvpView.hideOverlay();
                pvpView.showRequest("Trận đấu đã tạm dừng. Nhấn nút RESUME trên Header hoặc phím tắt để tiếp tục.");
            }
            case RESUME -> {
                stateP1 = GameState.PLAYING;
                stateP2 = GameState.PLAYING;
                pvpView.getHeaderView().setPauseEmoji(false);
                pvpView.hideOverlay();
                pvpView.showRequest("Tiếp tục trận đấu!");
                timerP1.resume();
                timerP2.resume();
            }
        }
        clearRequestVariables();
    }

    // ── Xử lý Từ chối (Reject Request) ──

    private void rejectRequest(int player) {
        if (!waitingConfirmation) return;

        // LUỒNG XỬ LÝ 1: Ở thông báo 1 bên nổ mìn, người thắng từ chối chơi tiếp -> [CHƠI LẠI] trận mới
        if (pendingRequest == PvPRequestType.PAUSE && activePlayersCount == 1) {
            pvpView.hideOverlay();
            clearRequestVariables();
            initMatch();
            return;
        }

        // LUỒNG XỬ LÝ 2: Ở thông báo "Bạn đã dậm mìn" lúc chơi đơn, bấm Từ chối -> [QUAY LẠI MENU]
        if (pendingRequest == PvPRequestType.RESET && activePlayersCount == 1) {
            pvpView.hideOverlay();
            clearRequestVariables();
            if (onMatchEnd != null) onMatchEnd.run();
            return;
        }

        // LUỒNG XỬ LÝ 3: Từ chối các lệnh Pause/Reset thông thường khi game đang chạy
        // UC-6 - 6.5.1 / UC-7 - 7.5.1 / UC-8 - 8.5.1: Người gửi yêu cầu tự nhấn phím từ chối (ví dụ: Player 1 nhấn X để từ chối yêu cầu của chính mình).
        // UC-6 - 6.5.2 / UC-7 - 7.5.2 / UC-8 - 8.5.2: Hệ thống nhận diện trùng lặp người thực hiện.
        // UC-6 - 6.5.3 / UC-7 - 7.5.3 / UC-8 - 8.5.3: Hệ thống bỏ qua thao tác, tiếp tục hiển thị Overlay chờ đối thủ xác nhận.
        if (player == requestingPlayer) return;

        // UC-6 - 6.4.1 / UC-7 - 7.4.1 / UC-8 - 8.4.1: Người chơi còn lại nhấn phím từ chối (Player 1 nhấn X để từ chối Player 2).
        // UC-6 - 6.4.2 / UC-7 - 7.4.2 / UC-8 - 8.4.2: Hệ thống kiểm tra thấy người thực hiện thao tác hợp lệ (rejecterPlayer != requesterPlayer).
        waitingConfirmation = false;

        // UC-6 - 6.4.3 / UC-7 - 7.4.3 / UC-8 - 8.4.3: Hệ thống ẩn màn hình Overlay yêu cầu xác nhận.
        pvpView.hideOverlay();

        // UC-6 - 6.4.4 / UC-7 - 7.4.4 / UC-8 - 8.4.4: Hệ thống hiển thị thông báo: "Yêu cầu bị từ chối".
        pvpView.showRequest("Yêu cầu bị từ chối!");

        // Nếu từ chối lệnh Tạm dừng/Reset, game phải chạy tiếp tục ngay lập tức
        if (stateP1 != GameState.PAUSED) {
            timerP1.resume();
            timerP2.resume();
        }
        // UC-6 - 6.4.5 / UC-7 - 7.4.5 / UC-8 - 8.4.5: Hệ thống đặt lại trạng thái chờ xác nhận. Trận đấu tiếp tục giữ nguyên trạng thái tạm dừng (PAUSED).
        clearRequestVariables();
    }

    private void clearRequestVariables() {
        waitingConfirmation = false;
        pendingRequest = null;
        requestingPlayer = 0;
    }

    // ── Key Listener ────────────────────────────────────────

    public void registerKeyListeners(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
    }

    private void handleKeyPressed(KeyEvent e) {
        KeyCode key = e.getCode();
        if (waitingConfirmation) {
            switch (key) {
                case C -> confirmRequest(1);
                case X -> rejectRequest(1);
                case O -> confirmRequest(2);
                case N -> rejectRequest(2);
                default -> {
                }
            }
            e.consume();
            return;
        }
        boolean handled = true;

        switch (key) {
            // Player 1 Controls
            case W -> moveCursorP1(-1, 0);
            case S -> moveCursorP1(1, 0);
            case A -> moveCursorP1(0, -1);
            case D -> moveCursorP1(0, 1);
            case SPACE -> revealP1();
            case F -> flagP1();
            case R -> requestAction(1, PvPRequestType.RESET);
            case T -> requestAction(1, PvPRequestType.PAUSE);
            case Y -> requestAction(1, PvPRequestType.RESUME);

            // Player 2 Controls
            case UP -> moveCursorP2(-1, 0);
            case DOWN -> moveCursorP2(1, 0);
            case LEFT -> moveCursorP2(0, -1);
            case RIGHT -> moveCursorP2(0, 1);
            case ENTER -> revealP2();
            case P -> flagP2();
            case NUMPAD1 -> requestAction(2, PvPRequestType.RESET);
            case NUMPAD2 -> requestAction(2, PvPRequestType.PAUSE);
            case NUMPAD3 -> requestAction(2, PvPRequestType.RESUME);
            default -> handled = false;
        }

        if (handled) {
            e.consume();
        }
    }

    private void moveCursorP1(int dr, int dc) {
        pvpView.moveP1Cursor(dr, dc);
    }

    private void moveCursorP2(int dr, int dc) {
        pvpView.moveP2Cursor(dr, dc);
    }

    // ── Mở ô ────────────────────────────────────────────────

    private void revealP1() {
        if (stateP1 != GameState.PVP_SPLIT_START && stateP1 != GameState.PLAYING) return;

        p1Started = true;
        int[] cursor = pvpView.getCursorP1();
        int row = cursor[0], col = cursor[1];

        boardP1.getLastRevealedPositions().clear();
        boolean safe = boardP1.revealCell(row, col);

        for (int[] pos : boardP1.getLastRevealedPositions())
            pvpView.updateCellP1(pos[0], pos[1], boardP1.getCell(pos[0], pos[1]));
        pvpView.updateCellP1(row, col, boardP1.getCell(row, col));

        checkMatchResult(true, safe, row, col);
    }

    private void revealP2() {
        if (stateP2 != GameState.PVP_SPLIT_START && stateP2 != GameState.PLAYING) return;

        p2Started = true;
        int[] cursor = pvpView.getCursorP2();
        int row = cursor[0], col = cursor[1];

        boardP2.getLastRevealedPositions().clear();
        boolean safe = boardP2.revealCell(row, col);

        for (int[] pos : boardP2.getLastRevealedPositions())
            pvpView.updateCellP2(pos[0], pos[1], boardP2.getCell(pos[0], pos[1]));
        pvpView.updateCellP2(row, col, boardP2.getCell(row, col));

        checkMatchResult(false, safe, row, col);
    }

    // ── Cắm cờ ──────────────────────────────────────────────

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

    // ── Logic Kiểm tra kết quả Trận đấu ──

    void checkMatchResult(boolean isP1, boolean safe, int row, int col) {
        if (stateP1 == GameState.PVP_SPLIT_START) stateP1 = GameState.PLAYING;
        if (stateP2 == GameState.PVP_SPLIT_START) stateP2 = GameState.PLAYING;

        if (activePlayersCount == 2) {
            if (isP1) {
                if (!safe) {
                    stateP1 = GameState.LOSE;
                    timerP1.pause();
                    boardP1.revealAllMines();
                    pvpView.revealAllMinesP1(boardP1, row, col);

                    if (stateP2 == GameState.LOSE) {
                        finishMatch(0); // DRAW
                    } else {
                        activePlayersCount = 1;
                        showIntermediateOverlay(2);
                    }
                } else if (boardP1.checkWin()) {
                    stateP1 = GameState.WIN;
                    stateP2 = GameState.LOSE;
                    finishMatch(1);
                }
            } else {
                if (!safe) {
                    stateP2 = GameState.LOSE;
                    timerP2.pause();
                    boardP2.revealAllMines();
                    pvpView.revealAllMinesP2(boardP2, row, col);

                    if (stateP1 == GameState.LOSE) {
                        finishMatch(0);
                    } else {
                        activePlayersCount = 1;
                        showIntermediateOverlay(1);
                    }
                } else if (boardP2.checkWin()) {
                    stateP2 = GameState.WIN;
                    stateP1 = GameState.LOSE;
                    finishMatch(2);
                }
            }
        } else if (activePlayersCount == 1) {
            if (isP1 && stateP1 == GameState.PLAYING) {
                if (!safe) {
                    stateP1 = GameState.LOSE;
                    timerP1.pause();
                    boardP1.revealAllMines();
                    pvpView.revealAllMinesP1(boardP1, row, col);
                    showSinglePlayerLoseOverlay(1);
                } else if (boardP1.checkWin()) {
                    stateP1 = GameState.WIN;
                    finishMatch(1);
                }
            } else if (!isP1 && stateP2 == GameState.PLAYING) {
                if (!safe) {
                    stateP2 = GameState.LOSE;
                    timerP2.pause();
                    boardP2.revealAllMines();
                    pvpView.revealAllMinesP2(boardP2, row, col);
                    showSinglePlayerLoseOverlay(2);
                } else if (boardP2.checkWin()) {
                    stateP2 = GameState.WIN;
                    finishMatch(2);
                }
            }
        }
    }

    void showIntermediateOverlay(int winnerPlayer) {
        timerP1.pause();
        timerP2.pause();

        String winnerName = (winnerPlayer == 1) ? player1Name : player2Name;
        String loserName = (winnerPlayer == 1) ? player2Name : player1Name;

        pvpView.showOverlay(
                loserName + " đã nổ mìn! " + winnerName + " CHIẾN THẮNG!",
                winnerName + " ơi! Bạn có muốn làm gì tiếp theo?\n" +
                        "Nhấn [C] hoặc [O] để TIẾP TỤC giải nốt bãi mìn của mình.\n" +
                        "Nhấn [X] hoặc [N] để CHƠI LẠI ván đấu mới ngay."
        );

        waitingConfirmation = true;
        pendingRequest = PvPRequestType.PAUSE;
        requestingPlayer = (winnerPlayer == 1) ? 2 : 1;
    }

    void showSinglePlayerLoseOverlay(int player) {
        String name = (player == 1) ? player1Name : player2Name;
        pvpView.showOverlay(
                name + " ĐÃ DẬM MÌN VÀ THUA CUỘC!",
                "Hệ thống bãi mìn đối kháng chính thức đóng lại.\n" +
                        "Nhấn [C] hoặc [O] để khởi tạo CHƠI LẠI một trận mới.\n" +
                        "Nhấn [X] hoặc [N] để THOÁT QUAY LẠI MENU."
        );
        waitingConfirmation = true;
        pendingRequest = PvPRequestType.RESET;
        requestingPlayer = player;
    }

    protected GameResultView createGameResultView() {
        return new GameResultView();
    }

    void finishMatch(int winner) {
        timerP1.pause();
        timerP2.pause();

        GameResultView resultView = createGameResultView();
        GameResultView.Action action = resultView.showPvP(
                (Stage) pvpView.getRoot().getScene().getWindow(),
                winner,
                player1Name, player2Name,
                timerP1.getElapsedSeconds(), timerP2.getElapsedSeconds(),
                boardP1.getFlagCount(), boardP2.getFlagCount()
        );

        if (action == GameResultView.Action.RESTART) {
            initMatch();
        } else {
            if (onMatchEnd != null) onMatchEnd.run();
        }
    }

    // ── Getter ──────────────────────────────────────────────

    public PvPBoardView getPvPBoardView() {
        return pvpView;
    }

    public void setOnMatchEnd(Runnable handler) {
        this.onMatchEnd = handler;
    }
}