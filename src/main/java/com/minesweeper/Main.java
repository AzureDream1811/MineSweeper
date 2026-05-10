package com.minesweeper;

import com.minesweeper.controller.GameController;
import com.minesweeper.view.BoardView;
import com.minesweeper.view.HeaderView;
import com.minesweeper.view.MainView;
import javafx.application.Application;
import javafx.stage.Stage;

public class  Main extends Application {

    @Override
    public void start(Stage stage) {
        // 1. Dựng View từ dưới lên
        HeaderView headerView = new HeaderView();
        BoardView  boardView  = new BoardView();
        MainView   mainView   = new MainView(headerView, boardView);



        // 2. Tao Controller
        GameController controller = new GameController(mainView);

        // 3. Stage
        stage.setTitle("Minesweeper");
        stage.setScene(mainView.getScene());
        stage.setResizable(false);
        stage.sizeToScene();
        stage.show();

        // 4. Phim tat F2 -> reset
        mainView.getScene().setOnKeyPressed(controller::onKeyPressed);
    }

    public static void main(String[] args) {
        launch(args);
    }
}