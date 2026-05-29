module com.minesweeper {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires java.sql;

    opens com.minesweeper            to javafx.graphics;
    opens com.minesweeper.controller to javafx.graphics;
    opens com.minesweeper.model      to javafx.graphics;
    opens com.minesweeper.view       to javafx.graphics;
}
