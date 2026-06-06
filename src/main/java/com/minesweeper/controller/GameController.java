package com.minesweeper.controller;

import com.minesweeper.data.dao.GameParticipantDAO;
import com.minesweeper.data.dao.GameSessionDAO;
import com.minesweeper.data.dao.PlayerDAO;
import com.minesweeper.data.dao.ScoreRecordDAO;
import com.minesweeper.data.model.GameParticipant;
import com.minesweeper.data.model.GameSession;
import com.minesweeper.data.model.Player;
import com.minesweeper.model.Board;
import com.minesweeper.model.GameState;
import com.minesweeper.model.GameTimer;
import com.minesweeper.model.ScoreRecord;
import com.minesweeper.view.MainView;
import com.minesweeper.view.PvPSetupDialog;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.util.UUID;

public class GameController {

    private Board board;
    private final GameTimer timer;
    private final ScoreRecord record;
    private GameState gameState;
    private Difficulty difficulty;
    private final MainView mainView;
    private final PlayerDAO playerDAO = new PlayerDAO();
    private final GameSessionDAO gameSessionDAO = new GameSessionDAO();
    private final GameParticipantDAO gameParticipantDAO = new GameParticipantDAO();
    private final ScoreRecordDAO  scoreRecordDAO = new ScoreRecordDAO();
    //   11.1.3. Hệ thống truy vấn cơ sở dữ liệu lấy danh sách thời gian tốt, sắp xếp theo thời gian. Hệ thống hiển thị bảng xếp hạng: Lưu participant hiện tại để dùng khi insert score_record.
    private String currentParticipantId;
    private String currentSessionId;

    public GameController(MainView mainView) {
        this.mainView = mainView;
        this.timer = new GameTimer();
        this.record = ScoreRecord.getInstance();
        this.difficulty = Difficulty.EASY;
        this.gameState = GameState.IDLE;

        registerViewHandlers();
        registerMenuHandlers();
        registerResultHandlers();
        registerPvPHandler(); // [UC5.4.1] Đăng ký handler cho nút PvP Cục Bộ
        refreshMenuBestTimes();
    }

    // ── Setup ─────────────────────────────────────────────────

    private void registerViewHandlers() {
        mainView.getBoardView().setOnLeftClick(this::onLeftClick);
        mainView.getBoardView().setOnRightClick(this::onRightClick);
        mainView.getBoardView().setOnChord(this::onChord);
        mainView.getHeaderView().setOnReset(this::reset);
        mainView.getHeaderView().setOnPause(this::togglePause);
        mainView.getHeaderView().setOnDifficultyChange(this::setDifficulty);

        // 11.1.1: Người chơi nhấn nút "High Score" trên Header.
        mainView.getHeaderView().setOnHighScore(this::showHighScore);
    }

    private void registerMenuHandlers() {
        mainView.getMenuView().setOnDifficultySelected(difficulty -> {
            this.difficulty = difficulty;
            mainView.showGame();
            newGame();
            mainView.getHeaderView().setDifficulty(difficulty);
        });
    }

    /**
     * [UC5.4.1] Đăng ký handler khi người chơi nhấn "Chơi PvP Cục Bộ".
     * Mở dialog cấu hình (UC5.4.2), sau đó khởi tạo trận PvP (UC5.4.4–5.4.7).
     */
    private void registerPvPHandler() {
        mainView.setOnPvPLocalRequested(() -> {
            // [UC5.4.2] Hiển thị dialog thiết lập trận đấu
            Stage ownerStage = (Stage) mainView.getScene().getWindow();
            PvPSetupDialog.Config config = PvPSetupDialog.show(ownerStage);

            // Người chơi đã huỷ dialog → dừng lại, giữ nguyên menu
            if (config == null) return;

            // [UC5.4.4] Tạo PvPGameController và lấy view
            PvPGameController pvpController = new PvPGameController(config);

            // [UC5.4.4] Hiển thị GameView PvP chia đôi, ẩn menu và chế độ đơn
            mainView.showPvP(pvpController.getPvPBoardView());

            // [UC5.4.6] Đăng ký Key Listeners sau khi view đã hiển thị
            pvpController.registerKeyListeners(mainView.getScene());
        });
    }

    /**
     * Đăng ký callback khi người chơi chọn Restart hoặc Quay về Menu từ dialog kết quả
     */
    private void registerResultHandlers() {
        mainView.setOnRestartRequested(this::newGame);
        mainView.setOnMenuRequested(() -> {
            timer.reset();
            gameState = GameState.IDLE;
            mainView.showMenu();
            Platform.runLater(() -> {
                Stage stage = (Stage) mainView.getScene().getWindow();
                if (stage != null) stage.sizeToScene();
            });
        });
    }

    private void bindProperties() {
        // Bind timer
        mainView.getHeaderView().bindTimer(timer.elapsedSecondsProperty());

        // Remaining mines = totalMines - flagCount
        IntegerProperty remaining = new SimpleIntegerProperty();
        remaining.bind(Bindings.subtract(board.getTotalMines(), board.flagCountProperty()));
        mainView.getHeaderView().bindMineCount(remaining);
    }

    // ── Game Management ───────────────────────────────────────

    public void newGame() {
        // 1. Tạo board model trước
        board = BoardFactory.createBoard(difficulty);

        // 2. Reset state
        gameState = GameState.IDLE;
        timer.reset();
        // 11.1.3. Hệ thống truy vấn cơ sở dữ liệu lấy danh sách thời gian tốt, sắp xếp theo thời gian. Hệ thống hiển thị bảng xếp hạng: Reset participant/session — sẽ tạo mới khi ván bắt đầu.
        currentParticipantId = null;
        currentSessionId     = null;
        mainView.getBoardView().build(board.getRows(), board.getCols());
        bindProperties();

        mainView.getHeaderView().setResetEmoji("🙂");
        mainView.getHeaderView().showBestTime(record.getBestTime(difficulty));
        mainView.setDisabled(false);
    }

    public void reset() {
        newGame();
    }

    public void pause() {
        if (gameState != GameState.PLAYING) return;
        timer.pause();
        gameState = GameState.PAUSED;
        mainView.setDisabled(true);
    }

    public void resume() {
        if (gameState != GameState.PAUSED) return;
        timer.resume();
        gameState = GameState.PLAYING;
        mainView.setDisabled(false);
    }

    public void setDifficulty(Difficulty d) {
        this.difficulty = d;
        newGame();
        Platform.runLater(() -> {
            Stage stage = (Stage) mainView.getScene().getWindow();
            if (stage != null) stage.sizeToScene();
        });
    }

    private void togglePause() {
        if (gameState == GameState.PLAYING) {
            gameState = GameState.PAUSED;
            timer.pause();
            mainView.setDisabled(true);
            mainView.getHeaderView().setPauseEmoji(true);
        } else if (gameState == GameState.PAUSED) {
            gameState = GameState.PLAYING;
            timer.resume();
            mainView.setDisabled(false);
            mainView.getHeaderView().setPauseEmoji(false);
        }
    }

    //11.1.2: Hệ thống mở dialog High Score dạng modal, mặc định tab Solo, mức Dễ.
    private void showHighScore() {
        mainView.showHighScore(null);
    }

    // ── Board Interaction ─────────────────────────────────────

    /**
     * UC_10/UC_16 — Start Timer: khi click mở ô đầu tiên, chuyển IDLE -> PLAYING và start timer.
     */
    public void onLeftClick(int row, int col) {
        // UC-09 - 9.1.2:
        // Hệ thống kiểm tra trạng thái hiện tại của ván chơi.
        //
        // UC-09 - 9.4.1:
        // Nếu ván chơi đang tạm dừng hoặc đã kết thúc,
        // hệ thống không thực hiện thao tác mở ô.
        if (gameState == GameState.PAUSED
                || gameState == GameState.WIN
                || gameState == GameState.LOSE) {
            return;
        }

        // Chuẩn bị danh sách các ô vừa được mở trong lần thao tác này.
        // Danh sách này dùng cho bước 9.1.9 để cập nhật giao diện.
        board.getLastRevealedPositions().clear();

        // UC-09 - 9.1.3:
        // Nếu ván chơi đang ở trạng thái IDLE,
        // hệ thống chuyển ván chơi sang PLAYING và bắt đầu bộ đếm thời gian.
        if (gameState == GameState.IDLE) {
            timer.start();
            gameState = GameState.PLAYING;
            //   11.1.3. Hệ thống truy vấn cơ sở dữ liệu lấy danh sách thời gian tốt, sắp xếp theo thời gian. Hệ thống hiển thị bảng xếp hạng: Tạo session + participant khi ván chơi bắt đầu.
            initSoloSession();
        }

        // UC-09 - 9.1.4, 9.1.5, 9.1.6, 9.1.8:
        // Gọi Board xử lý logic mở ô:
        // - nếu là lượt đầu thì đặt mìn
        // - kiểm tra nội dung ô
        // - mở ô an toàn
        // - tự động mở rộng nếu là ô trống
        boolean safe = board.revealCell(row, col);

        // UC-09 - 9.1.9:
        // Hệ thống cập nhật giao diện bàn chơi.
        // Cập nhật các ô vừa được reveal, bao gồm cả các ô do flood-fill mở ra.
        updateChangedCells();

        // Cập nhật lại các ô trong danh sách lastRevealedPositions.
        for (int[] pos : board.getLastRevealedPositions()) {
            mainView.getBoardView().updateCell(
                    pos[0],
                    pos[1],
                    board.getCell(pos[0], pos[1])
            );
        }

        if (!safe) {
            // UC-09 - 9.2.2:
            // Hệ thống chuyển sang UC-17 – Trigger Explosion.
            handleLose(row, col);

        } else if (board.checkWin()) {
            // UC-09 - 9.1.10:
            // Nếu tất cả các ô không chứa mìn đã được mở,
            // hệ thống chuyển sang xử lý thắng.
            handleWin();
        }

        // UC-09 - 9.1.11:
        // Use case kết thúc.
    }

    public void onRightClick(int row, int col) {
        if (gameState != GameState.PLAYING) return;
        board.toggleFlag(row, col);
        mainView.getBoardView().updateCell(row, col, board.getCell(row, col));
    }

    public void onChord(int row, int col) {
        if (gameState != GameState.PLAYING) return;
        board.getLastRevealedPositions().clear();
        // GameController.java
        boolean safe = board.chord(row, col);
        updateChangedCells();
        // GameController.java
        if (!safe) handleLose(row, col);
        else if (board.checkWin()) handleWin();
    }

    public void onKeyPressed(KeyEvent e) {
        if (e.getCode() == KeyCode.F2) {
            reset();
        } else if (e.getCode() == KeyCode.F11) {
            Stage stage = (Stage) mainView.getScene().getWindow();
            if (stage != null) stage.setFullScreen(!stage.isFullScreen());
        }
    }

    // ── Game Status ───────────────────────────────────────────

    private void handleWin() {
        // UC_14 step 1: dừng đồng hồ
        timer.pause();
        int elapsed = timer.getElapsedSeconds();
        gameState = GameState.WIN;

        // UC_14 step 2: đổi emoji → 😎
        mainView.getHeaderView().setResetEmoji("😎");

        // UC_14 step 3: khóa board
        mainView.setDisabled(true);

        // UC_14 step 4: kiểm tra và lưu high score
        boolean isNewRecord = record.update(difficulty, elapsed);
        //   11.1.3. Hệ thống truy vấn cơ sở dữ liệu lấy danh sách thời gian tốt, sắp xếp theo thời gian. Hệ thống hiển thị bảng xếp hạng: Lưu kết quả thắng vào DB score_record.
        saveScoreRecord(elapsed, "WIN");   // ← THIẾU DÒNG NÀY
        //  11.1.0: Cập nhật lại kỷ lục trên màn hình chính sau khi lập kỷ lục mới.
        refreshMenuBestTimes();
        // Cập nhật tooltip Best Time trên header
        mainView.getHeaderView().showBestTime(record.getBestTime(difficulty));

        // UC_14 step 5: hiện dialog kết quả (có badge kỷ lục mới nếu isNewRecord)
        mainView.showResult(true, elapsed, isNewRecord);

        //  11.3.1: Sau khi thắng, hệ thống lưu kỷ lục mới vào DB.
        //  11.3.2: Hệ thống tự động mở dialog High Score, chọn sẵn mức độ vừa chơi.
        if (isNewRecord) {
            mainView.showHighScore(difficulty);
        }
    }

    private void handleLose(int explodedRow, int explodedCol) {
        // UC-17 - 17.1.3:
        // Hệ thống chuyển ván chơi sang trạng thái thua.
        gameState = GameState.LOSE;

        // UC-17 - 17.1.4:
        // Hệ thống dừng bộ đếm thời gian.
        timer.pause();
        int elapsed = timer.getElapsedSeconds();

        // UC-17 - 17.1.5:
        // Hệ thống hiển thị các ô chứa mìn trên bàn chơi.
        board.revealAllMines();

        // UC-17 - 17.1.5 + 17.1.6:
        // BoardView hiển thị toàn bộ mìn và làm nổi bật ô mìn mà người chơi vừa mở.
        mainView.getBoardView().revealAllMines(board, explodedRow, explodedCol);

        // UC-17 - 17.1.7:
        // Hệ thống khóa bàn chơi, không cho phép người chơi tiếp tục thao tác.
        mainView.setDisabled(true);

        // UC-17 - 17.1.8:
        // Hệ thống cập nhật biểu tượng trạng thái thua trên giao diện.
        mainView.getHeaderView().setResetEmoji("😵");
        //  11.1.3. Hệ thống truy vấn cơ sở dữ liệu lấy danh sách thời gian tốt, sắp xếp theo thời gian. Hệ thống hiển thị bảng xếp hạng: Lưu kết quả thua vào DB score_record.
        saveScoreRecord(elapsed, "LOSE");
        // UC-17 - 17.1.9:
        // Hệ thống hiển thị kết quả thua.
        mainView.showResult(false, elapsed, false);

        // UC-17 - 17.1.10:
        // Use case kết thúc.
    }

    private void updateChangedCells() {
        // UC-09 - 9.1.9:
        // Hệ thống cập nhật giao diện bàn chơi.
        //
        // Chỉ cập nhật những ô vừa được mở trong lần thao tác hiện tại,
        // thay vì render lại toàn bộ bàn cờ.
        for (int[] pos : board.getLastRevealedPositions()) {
            mainView.getBoardView().updateCell(
                    pos[0],
                    pos[1],
                    board.getCell(pos[0], pos[1])
            );
        }
    }
    // 11.1.0: Màn hình chính hiển thị sẵn kỷ lục tốt nhất của từng mức độ khó.
    private void refreshMenuBestTimes() {
        for (Difficulty d : Difficulty.values()) {
            mainView.getMenuView().updateBestTime(d, record.getBestTime(d));
        }
    }

    private void initSoloSession() {
        new Thread(() -> {
            try {
                // Bước 1: Tạo Player mặc định (chưa có hệ thống login)
                Player player = new Player(UUID.randomUUID().toString(), "Người chơi");
                playerDAO.insert(player);

                // Bước 2: Tạo GameSession
                GameSession session = new GameSession(
                        UUID.randomUUID().toString(),
                        "SOLO",
                        difficulty.name(),
                        board.getRows(),
                        board.getCols(),
                        board.getTotalMines()
                );
                session.setStatus("playing");
                gameSessionDAO.insert(session);

                // Bước 3: Tạo GameParticipant
                GameParticipant participant = new GameParticipant(
                        UUID.randomUUID().toString(),
                        session.getId(),
                        player.getId(),
                        1
                );
                participant.setStatus("playing");
                gameParticipantDAO.insert(participant);

                // Lưu lại ID để dùng trong handleWin / handleLose
                currentSessionId     = session.getId();
                currentParticipantId = participant.getId();

            } catch (Exception e) {
                //  11.5.1: Lỗi DB → bỏ qua, game vẫn chạy bình thường.
                e.printStackTrace();
            }
        }, "solo-session-init").start();
    }
    // ── DB: Score record insert ───────────────────────────────

    /**
     *   11.1.3. Hệ thống truy vấn cơ sở dữ liệu lấy danh sách thời gian tốt, sắp xếp theo thời gian. Hệ thống hiển thị bảng xếp hạng: Insert score_record vào DB sau khi ván kết thúc.
     * Chạy trên background thread để không block UI.
     *
     * @param elapsedSeconds thời gian thực hiện
     * @param result         "WIN" hoặc "LOSE"
     */
    private void saveScoreRecord(int elapsedSeconds, String result) {
        if (currentParticipantId == null || currentSessionId == null) {
            // Session chưa kịp khởi tạo (ván quá nhanh) → bỏ qua
            return;
        }
        new Thread(() -> {
            try {
                com.minesweeper.data.model.ScoreRecord sr =
                        new com.minesweeper.data.model.ScoreRecord(
                                UUID.randomUUID().toString(),
                                currentParticipantId,
                                currentSessionId,
                                "SOLO",
                                difficulty.name(),
                                elapsedSeconds,
                                result
                        );
                scoreRecordDAO.insert(sr);
            } catch (Exception e) {
                //  11.5.1: Lỗi DB → bỏ qua, game vẫn chạy bình thường.
                e.printStackTrace();
            }
        }, "score-record-save").start();
    }
}