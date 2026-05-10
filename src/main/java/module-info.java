module com.minesweeper {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;

    opens com.minesweeper.view to javafx.fxml;
    opens com.minesweeper.controller to javafx.fxml;

    exports com.minesweeper;
    exports com.minesweeper.model;
    exports com.minesweeper.view;
    exports com.minesweeper.controller;
}
