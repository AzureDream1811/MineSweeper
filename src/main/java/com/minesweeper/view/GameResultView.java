package com.minesweeper.view;

import javafx.stage.Stage;

/** Minimal stub for Game Result dialog used by MainView. */
public class GameResultView {

    public enum Action {
        RESTART,
        QUIT_TO_MENU,
        CLOSE
    }

    public GameResultView() {
    }

    public Action show(Stage owner, boolean win, int elapsedSeconds, boolean isNewRecord) {
        // Placeholder: real implementation should show a dialog and return the user's choice.
        return Action.CLOSE;
    }
}
