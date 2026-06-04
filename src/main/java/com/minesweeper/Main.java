package com.minesweeper;

import com.minesweeper.controller.GameController;
import com.minesweeper.model.ScoreRecord;
import com.minesweeper.view.BoardView;
import com.minesweeper.view.HeaderView;
import com.minesweeper.view.MainView;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        // 1. Dựng View từ dưới lên (ScoreRecord singleton đã load file khi getInstance)
        ScoreRecord record   = ScoreRecord.getInstance();
        HeaderView  headerView = new HeaderView();
        BoardView   boardView  = new BoardView();
        MainView    mainView   = new MainView(headerView, boardView, record);

        // 2. Tạo Controller
        GameController controller = new GameController(mainView);

        // 3. Stage
        stage.setTitle("Minesweeper");
        stage.setScene(mainView.getScene());
        stage.setResizable(true);
        stage.sizeToScene();
        stage.show();

        // 4. Phím tắt F2 -> reset
        mainView.getScene().setOnKeyPressed(controller::onKeyPressed);
    }

    public static void main(String[] args) {
        launch(args);
    }
}