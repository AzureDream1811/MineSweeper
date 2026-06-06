package com.minesweeper.view;

import javafx.beans.property.IntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class PvPHeaderView {
    private final HBox root;

    private final Label p1NameLabel;
    private final Label p1TimerLabel;
    private final Label p1MineLabel;

    private final Button resetButton;
    private final Button pauseButton;

    private final Label p2TimerLabel;
    private final Label p2MineLabel;
    private final Label p2NameLabel;

    public PvPHeaderView(String p1Name, String p2Name) {
        p1NameLabel = new Label(p1Name);
        p1TimerLabel = new Label("000");
        p1MineLabel = new Label("000");

        resetButton = new Button("🙂");
        pauseButton = new Button("⏸");

        p2TimerLabel = new Label("000");
        p2MineLabel = new Label("000");
        p2NameLabel = new Label(p2Name);

        root = new HBox(12,
                p1NameLabel, p1TimerLabel, p1MineLabel,
                resetButton, pauseButton,
                p2TimerLabel, p2MineLabel, p2NameLabel
        );

        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(8, 12, 8, 12));
        root.getStyleClass().add("header");
    }

    public HBox getRoot() {
        return root;
    }

    public void bindTimerP1(IntegerProperty seconds) {
        p1TimerLabel.textProperty().unbind();
        p1TimerLabel.textProperty().bind(seconds.asString("%03d"));
    }

    public void bindTimerP2(IntegerProperty seconds) {
        p2TimerLabel.textProperty().unbind();
        p2TimerLabel.textProperty().bind(seconds.asString("%03d"));
    }

    public void bindMinesP1(IntegerProperty mines) {
        p1MineLabel.textProperty().unbind();
        p1MineLabel.textProperty().bind(mines.asString("%03d"));
    }

    public void bindMinesP2(IntegerProperty mines) {
        p2MineLabel.textProperty().unbind();
        p2MineLabel.textProperty().bind(mines.asString("%03d"));
    }

    public void setOnReset(Runnable handler) {
        resetButton.setOnAction(e -> handler.run());
    }

    public void setOnPause(Runnable handler) {
        pauseButton.setOnAction(e -> handler.run());
    }

    public void setPauseEmoji(boolean paused) {
        pauseButton.setText(paused ? "▶" : "⏸");
    }
}
