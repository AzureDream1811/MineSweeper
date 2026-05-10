package com.minesweeper.view;

import com.minesweeper.controller.Difficulty;
import com.minesweeper.model.ScoreRecord;
import javafx.stage.Stage;

/** Minimal stub for High Score dialog used by MainView. */
public class HighScoreView {

    private final ScoreRecord record;

    public HighScoreView(ScoreRecord record) {
        this.record = record;
    }

    public void show(Stage owner, Difficulty newRecordDifficulty) {
        // Intentionally minimal: placeholder for the real high-score dialog.
    }
}
