package com.minesweeper.controller;

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

public class GameController {

    private Board board;
    private final GameTimer timer;
    private final ScoreRecord record;
    private GameState gameState;
    private Difficulty difficulty;
    private final MainView mainView;

    public GameController(MainView mainView) {
        this.mainView = mainView;
        this.timer = new GameTimer();
        this.record = ScoreRecord.getInstance();
        this.difficulty = Difficulty.EASY;
        this.gameState = GameState.IDLE;

        registerViewHandlers();
        registerMenuHandlers();
        registerResultHandlers();
        registerPvPHandler();
    }

    // ── Setup ─────────────────────────────────────────────────

    private void registerViewHandlers() {
        mainView.getBoardView().setOnLeftClick(this::onLeftClick);
        mainView.getBoardView().setOnRightClick(this::onRightClick);
        mainView.getBoardView().setOnChord(this::onChord);
        
        // UC-6 - 6.1.1: Người chơi nhấp nút Reset (biểu tượng mặt cười) trên Header.
        mainView.getHeaderView().setOnReset(this::reset);
        
        // UC-7 - 7.1.1 / UC-8 - 8.1.1: Người chơi nhấp vào nút Pause (hoặc Play/Resume) trên Header.
        mainView.getHeaderView().setOnPause(this::togglePause);
        
        // UC-5 - 5.1.1: Người chơi chọn mức độ khó trên menu chọn độ khó (Header).
        mainView.getHeaderView().setOnDifficultyChange(this::setDifficulty);
        mainView.getHeaderView().setOnHighScore(this::showHighScore);
    }

    private void registerMenuHandlers() {
        // UC-5 - 5.1.0: Người chơi đang ở màn hình Menu chính, chưa bắt đầu chơi game.
        // UC-5 - 5.1.1: Người chơi nhấp chọn mức độ khó trên màn hình chính.
        mainView.getMenuView().setOnDifficultySelected(difficulty -> {
            // UC-5 - 5.3.1 / 5.3.2 / 5.3.3 / 5.3.4 (Handling Game Already In Progress):
            // (Hệ thống tự động hủy ván cũ và khởi tạo ván mới luôn mà không qua hộp thoại xác nhận)
            
            // UC-5 - 5.1.2: Hệ thống tiếp nhận tham số tương ứng với cấp độ đã chọn (Board_Size, Mine_Count).
            this.difficulty = difficulty;
            
            // UC-5 - 5.1.3: Hệ thống đóng màn hình lựa chọn hiện tại (ẩn menu chính).
            mainView.showGame();
            
            newGame();
            mainView.getHeaderView().setDifficulty(difficulty);
        });
    }

    private void registerPvPHandler() {
        mainView.setOnPvPLocalRequested(() -> {
            Stage ownerStage = (Stage) mainView.getScene().getWindow();
            PvPSetupDialog.Config config = PvPSetupDialog.show(ownerStage);

            if (config == null) return;

            PvPGameController pvpController = new PvPGameController(config);

            mainView.showPvP(pvpController.getPvPBoardView());

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

        // UC-4 - 4.1.2 / 4.1.3: Truy xuất totalMines, flagCountProperty và thiết lập công thức liên kết hiển thị số mìn (Remaining mines = totalMines - flagCount)
        IntegerProperty remaining = new SimpleIntegerProperty();
        remaining.bind(Bindings.subtract(board.getTotalMines(), board.flagCountProperty()));
        
        // UC-4 - 4.1.4: Controller bind dữ liệu số mìn còn lại tới HeaderView
        // UC-4 - 4.1.5: Tự động cập nhật số lượng mìn còn lại qua Observer Pattern khi cắm/gỡ cờ.
        // UC-4 - 4.3.1 / 4.3.2 (Exceeding Flag Limit): Hệ thống vẫn tiếp tục cập nhật hiển thị số mìn còn lại (chuyển âm) khi cắm cờ vượt quá giới hạn.
        mainView.getHeaderView().bindMineCount(remaining);
    }

    // ── Game Management ───────────────────────────────────────

    public void newGame() {
        // UC-5 - 5.1.4: Hệ thống khởi tạo cấu trúc dữ liệu cho Bàn cờ (Game Board) dựa trên tham số đã nhận.
        // UC-4 - 4.1.0 / 4.1.1: Khi người chơi bắt đầu ván mới, gọi BoardFactory tạo board mới theo độ khó
        board = BoardFactory.createBoard(difficulty);

        // UC-5 - 5.1.6 / UC-6 - 6.1.6: Hệ thống chuyển/ghi nhận trạng thái trò chơi là IDLE (chờ click đầu tiên).
        gameState = GameState.IDLE;
        
        // UC-6 - 6.1.2: Hệ thống xóa trạng thái bàn cờ hiện tại, dừng và đặt lại bộ đếm thời gian và mìn.
        timer.reset();

        // UC-5 - 5.1.5 / UC-6 - 6.1.4: Hệ thống cập nhật lại vùng hiển thị Board và Header trên màn hình.
        mainView.getBoardView().build(board.getRows(), board.getCols());
        bindProperties();

        // UC-5 - 5.1.7 / UC-6 - 6.1.7: Hệ thống hiển thị bàn cờ mới được làm mới hoàn toàn, sẵn sàng chơi.
        mainView.getHeaderView().setResetEmoji("🙂");
        mainView.getHeaderView().showBestTime(record.getBestTime(difficulty));
        
        // UC-6 - 6.1.5: Hệ thống đặt lại biểu tượng Pause về trạng thái Pause mặc định.
        mainView.getHeaderView().setPauseEmoji(false);
        mainView.setDisabled(false);
    }

    public void reset() {
        // UC-6 - 6.2.1 / 6.2.2 / 6.2.3 / 6.2.4 (Handling Reset During IDLE State):
        // Nếu nhấn Reset khi trò chơi đang ở trạng thái IDLE, hệ thống nhận biết và vẫn thực hiện reset bình thường.
        // UC-6 - 6.3.1 / 6.3.2 / 6.3.3 / 6.3.4 (Handling Reset During WIN or LOSE State):
        // Nếu ván chơi kết thúc (WIN/LOSE), hệ thống tiếp tục cho phép reset và bắt đầu từ bước 6.1.2.
        
        // UC-6 - 6.1.3: Hệ thống giữ nguyên các thông số cũ để khởi tạo lại bàn cờ mới.
        newGame();
    }

    public void pause() {
        // UC-7 - 7.1.2: Hệ thống kiểm tra và xác nhận trạng thái trò chơi hiện tại là PLAYING.
        if (gameState != GameState.PLAYING) return;
        
        // UC-7 - 7.1.3: Hệ thống dừng bộ đếm thời gian (Stopwatch/Timer).
        timer.pause();
        
        // UC-7 - 7.1.6: Hệ thống ghi nhận trạng thái trò chơi mới là PAUSED.
        gameState = GameState.PAUSED;
        
        // UC-7 - 7.1.4: Hệ thống vô hiệu hóa Grid bàn cờ, ngăn mọi thao tác mở ô hoặc cắm cờ.
        mainView.setDisabled(true);
        
        // UC-7 - 7.1.5: Hệ thống thay đổi biểu tượng nút Pause thành biểu tượng Resume/Play.
        mainView.getHeaderView().setPauseEmoji(true);
    }

    public void resume() {
        // UC-8 - 8.1.2: Hệ thống xác nhận trạng thái trò chơi hiện tại là PAUSED.
        // UC-8 - 8.2.1 / 8.2.2 / 8.2.3 (Handling Resume During PLAYING State): 
        // Nếu không phải PAUSED (đang PLAYING), bỏ qua thao tác và không có thay đổi nào xảy ra.
        // UC-8 - 8.3.1 / 8.3.2 / 8.3.3 (Resume Game from Main Menu): 
        // Nếu đang ở IDLE, WIN hoặc LOSE, bỏ qua thao tác vì không phải PAUSED.
        if (gameState != GameState.PAUSED) return;
        
        // UC-8 - 8.1.3: Hệ thống kích hoạt lại bộ đếm thời gian, chạy tiếp từ thời điểm đã ghi nhận trước đó.
        timer.resume();
        
        // UC-8 - 8.1.6: Hệ thống ghi nhận trạng thái trò chơi trở lại là PLAYING.
        gameState = GameState.PLAYING;
        
        // UC-8 - 8.1.4: Hệ thống mở lại (re-enable) tất cả các tương tác trên bàn cờ.
        mainView.setDisabled(false);
        
        // UC-8 - 8.1.5: Hệ thống chuyển biểu tượng nút bấm quay lại hình Pause.
        mainView.getHeaderView().setPauseEmoji(false);
    }

    public void setDifficulty(Difficulty d) {
        // UC-5 - 5.2.1 / 5.2.2 / 5.2.3 / 5.2.4 (Handling Invalid Difficulty Parameter):
        // Vì Difficulty là Enum và được chọn từ giao diện nên tham số đầu vào luôn hợp lệ.
        this.difficulty = d;
        // UC-4 - 4.2.1 / 4.2.2 (Changing Difficulty): Người chơi chọn cấp độ khó mới -> Hủy liên kết cũ (unbind) và khởi tạo lại dữ liệu mới
        newGame();
        Platform.runLater(() -> {
            Stage stage = (Stage) mainView.getScene().getWindow();
            if (stage != null) stage.sizeToScene();
        });
    }

    private void togglePause() {
        // UC-7 - 7.2.1 / 7.2.2 / 7.2.3 (Handling Pause During IDLE State):
        // Nếu game ở trạng thái IDLE, hệ thống nhận diện không phải PLAYING, bỏ qua thao tác.
        // UC-7 - 7.3.1 / 7.3.2 / 7.3.3 (Handling Pause During WIN or LOSE State):
        // Nếu game đã kết thúc (WIN/LOSE), hệ thống nhận diện game kết thúc, bỏ qua thao tác.
        if (gameState == GameState.PLAYING) {
            pause();
        } else if (gameState == GameState.PAUSED) {
            resume();
        }
    }

    private void showHighScore() {
        mainView.showHighScore(null);
    }

    // ── Board Interaction ─────────────────────────────────────

    public void onLeftClick(int row, int col) {
        if (gameState == GameState.PAUSED
                || gameState == GameState.WIN
                || gameState == GameState.LOSE) {
            return;
        }

        board.getLastRevealedPositions().clear();

        if (gameState == GameState.IDLE) {
            timer.start();
            gameState = GameState.PLAYING;
        }

        boolean safe = board.revealCell(row, col);

        updateChangedCells();

        for (int[] pos : board.getLastRevealedPositions()) {
            mainView.getBoardView().updateCell(
                    pos[0],
                    pos[1],
                    board.getCell(pos[0], pos[1])
            );
        }

        if (!safe) {
            handleLose(row, col);
        } else if (board.checkWin()) {
            handleWin();
        }
    }

    public void onRightClick(int row, int col) {
        // UC-1 - 1.3.1 / UC-2 - 2.3.1: Người chơi click chuột phải khi game ở trạng thái PAUSED, WIN hoặc LOSE.
        // UC-1 - 1.3.2 / UC-2 - 2.3.2: Hệ thống nhận diện trạng thái không cho phép tương tác.
        // UC-1 - 1.3.3 / UC-2 - 2.3.3: Hệ thống bỏ qua thao tác và không cập nhật trạng thái trò chơi.
        if (gameState != GameState.PLAYING) return;
        
        // UC-1 - 1.1.0 / UC-2 - 2.1.0: Người chơi click chuột phải vào ô.
        // UC-1 - 1.1.1 / UC-2 - 2.1.1: Hệ thống xác định tọa độ ô.
        board.toggleFlag(row, col);
        
        // UC-1 - 1.1.4 / UC-2 - 2.1.6: BoardView làm mới giao diện hiển thị của ô.
        mainView.getBoardView().updateCell(row, col, board.getCell(row, col));
    }

    public void onChord(int row, int col) {
        // UC-3 - Tiền điều kiện: Trò chơi đang hoạt động (GameState.PLAYING). Trận đấu chưa kết thúc.
        if (gameState != GameState.PLAYING) return;
        board.getLastRevealedPositions().clear();
        
        // UC-3 - 3.1.0: Người chơi nhấp đúp chuột vào một ô số trên bàn cờ.
        // UC-3 - 3.1.1: Hệ thống xác định tọa độ và kiểm tra điều kiện Chord.
        boolean safe = board.chord(row, col);
        
        // UC-3 - 3.1.3: Hệ thống cập nhật giao diện hiển thị cho các ô mới được mở.
        updateChangedCells();
        
        // UC-3 - 3.1.4: Hệ thống kiểm tra điều kiện thắng/thua.
        if (!safe) {
            // UC-3 - 3.3.3 (Incorrect Flag Placement): Hệ thống kích nổ mìn và kết thúc ván đấu (LOSE).
            handleLose(row, col);
        } else if (board.checkWin()) {
            handleWin();
        }
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
        timer.pause();
        int elapsed = timer.getElapsedSeconds();
        gameState = GameState.WIN;

        mainView.getHeaderView().setResetEmoji("😎");

        mainView.setDisabled(true);

        boolean isNewRecord = record.update(difficulty, elapsed);

        mainView.getHeaderView().showBestTime(record.getBestTime(difficulty));

        mainView.showResult(true, elapsed, isNewRecord);

        if (isNewRecord) {
            mainView.showHighScore(difficulty);
        }
    }

    private void handleLose(int explodedRow, int explodedCol) {
        gameState = GameState.LOSE;

        timer.pause();
        int elapsed = timer.getElapsedSeconds();

        board.revealAllMines();

        mainView.getBoardView().revealAllMines(board, explodedRow, explodedCol);

        mainView.setDisabled(true);

        mainView.getHeaderView().setResetEmoji("😵");

        mainView.showResult(false, elapsed, false);
    }

    private void updateChangedCells() {
        for (int[] pos : board.getLastRevealedPositions()) {
            mainView.getBoardView().updateCell(
                    pos[0],
                    pos[1],
                    board.getCell(pos[0], pos[1])
            );
        }
    }
}