package com.minesweeper.view;

import com.minesweeper.controller.Difficulty;
import com.minesweeper.model.ScoreRecord;
import com.minesweeper.view.PvPBoardView;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainView {

    private final StackPane root;
    private final VBox gameLayer;
    private final MenuView menuView;
    private final HeaderView headerView;
    private final BoardView  boardView;
    private final Scene scene;

    // Views cho High Score và Game Result
    private final HighScoreView highScoreView;
    private final GameResultView gameResultView;

    // Callback sau khi dialog kết quả đóng lại
    private Runnable onRestartRequested;
    private Runnable onMenuRequested;
    // [UC5.4.1] Callback khi người chơi nhấn nút "Chơi PvP Cục Bộ"
    private Runnable onPvPLocalRequested;

    public MainView(HeaderView headerView, BoardView boardView, ScoreRecord record) {
        this.headerView = headerView;
        this.boardView  = boardView;
        menuView = new MenuView();
        highScoreView = new HighScoreView(record);
        gameResultView = new GameResultView();

        gameLayer = new VBox(8);
        gameLayer.getChildren().addAll(headerView.getRoot(), boardView.getGrid());
        gameLayer.setVisible(false);

        root = new StackPane(menuView.getRoot(), gameLayer);
        root.setPadding(new Insets(12));
        root.getStyleClass().add("main-root");

        scene = new Scene(root);
        scene.getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm()
        );
    }

    /**
     * Hiển thị kết quả game theo UC_14 — View Game Result.
     * Dừng clock, đổi emoji, khóa board, kiểm tra high score, hiện popup.
     *
     * @param win              true = thắng
     * @param elapsedSeconds   thời gian đã trôi qua
     * @param isNewRecord      true nếu vừa lập kỷ lục mới
     */
    public void showResult(boolean win, int elapsedSeconds, boolean isNewRecord) {
        Stage ownerStage = (Stage) scene.getWindow();
        GameResultView.Action action = gameResultView.show(ownerStage, win, elapsedSeconds, isNewRecord);
        switch (action) {
            case RESTART -> { if (onRestartRequested != null) onRestartRequested.run(); }
            case QUIT_TO_MENU -> { if (onMenuRequested != null) onMenuRequested.run(); }
        }
    }

    /**
     * Mở dialog High Score theo UC_11 — View High Score.
     * @param newRecordDifficulty difficulty vừa lập kỷ lục (null nếu không)
     */
    public void showHighScore(Difficulty newRecordDifficulty) {
        Stage ownerStage = (Stage) scene.getWindow();
        highScoreView.show(ownerStage, newRecordDifficulty);
    }

    /**
     * [UC5.4.4] Hiển thị màn hình PvP chia đôi.
     * Ẩn menu, ẩn gameLayer đơn, thêm pvpView vào root.
     */
    public void showPvP(PvPBoardView pvpView) {
        menuView.getRoot().setVisible(false); // Ẩn menu chính (UC5.4.4)
        gameLayer.setVisible(false);          // Ẩn chế độ đơn (UC5.4.4)
        if (!root.getChildren().contains(pvpView.getRoot()))
            root.getChildren().add(pvpView.getRoot()); // Thêm view PvP vào StackPane
        pvpView.getRoot().setVisible(true);
        Platform.runLater(() -> {
            Stage stage = (Stage) scene.getWindow();
            if (stage != null) stage.sizeToScene();
        });
    }

    /**
     * Xoá màn hình PvP, quay về menu.
     */
    public void hidePvP(PvPBoardView pvpView) {
        root.getChildren().remove(pvpView.getRoot()); // Gỡ view PvP
        showMenu();
    }

    public void showGame() {
        menuView.getRoot().setVisible(false);
        gameLayer.setVisible(true);
        Platform.runLater(() -> {
            Stage stage = (Stage) scene.getWindow();
            if (stage != null) {
                stage.sizeToScene();
            }
        });
    }

    public void showMenu() {
        menuView.getRoot().setVisible(true);
        gameLayer.setVisible(false);
    }

    public void setDisabled(boolean disabled) {
        boardView.setDisabled(disabled);
    }

    // ── Callback setters ──────────────────────────────────────

    public void setOnRestartRequested(Runnable handler) {
        this.onRestartRequested = handler;
    }

    public void setOnMenuRequested(Runnable handler) {
        this.onMenuRequested = handler;
    }

    // [UC5.4.1] Setter để GameController đăng ký xử lý yêu cầu PvP
    public void setOnPvPLocalRequested(Runnable handler) {
        this.onPvPLocalRequested = handler;
        menuView.setOnPvPLocalRequested(handler); // Chuyển tiếp xuống MenuView
    }

    // ── Getters ───────────────────────────────────────────────

    public Scene      getScene()      { return scene; }
    public HeaderView getHeaderView() { return headerView; }
    public BoardView  getBoardView()  { return boardView; }
    public MenuView   getMenuView()   { return menuView; }
}