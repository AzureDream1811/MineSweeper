package com.minesweeper.view;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
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

    public MainView(HeaderView headerView, BoardView boardView) {
        this.headerView = headerView;
        this.boardView  = boardView;
        menuView = new MenuView();

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

    public void showResult(boolean win) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(win ? "Bạn thắng! 🎉" : "Bạn thua! 💥");
        alert.setHeaderText(null);
        alert.setContentText(win ? "Chúc mừng! Bạn đã tìm được tất cả mìn!" : "Bạn đã mở trúng mìn. Chúc may mắn lần sau!");
        alert.showAndWait();
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
    public Scene      getScene()      { return scene; }
    public HeaderView getHeaderView() { return headerView; }
    public BoardView  getBoardView()  { return boardView; }
    public MenuView   getMenuView()   { return menuView; }
}