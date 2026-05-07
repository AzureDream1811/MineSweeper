package com.minesweeper.view;
import com.minesweeper.controller.Difficulty;
import javafx.beans.property.IntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.util.function.Consumer;

public class HeaderView {

    private final HBox root;
    private final Label mineCountLabel;
    private final Label timerLabel;
    private final Button resetButton;
    private final Button pauseButton;
    private final MenuButton difficultyMenu;

    private Consumer<Difficulty> onDifficultyChange;
    private Runnable onPause;

    public HeaderView() {
        mineCountLabel = new Label("000");
        mineCountLabel.getStyleClass().add("header-counter");

        timerLabel = new Label("000");
        timerLabel.getStyleClass().add("header-counter");

        resetButton = new Button("Reset");
        resetButton.setPrefSize(36, 36);
        resetButton.setMinSize(36, 36);
        resetButton.setMaxSize(36, 36);
        resetButton.getStyleClass().add("reset-button");

        pauseButton = new Button("⏸");
        pauseButton.getStyleClass().add("pause-button");
        pauseButton.setPrefSize(36, 36);
        pauseButton.setMinSize(36, 36);
        pauseButton.setMaxSize(36, 36);
        pauseButton.setOnAction(e -> {
            if (onPause != null) onPause.run();
        });

        difficultyMenu = new MenuButton("Easy");
        difficultyMenu.getStyleClass().add("difficulty-menu");
        initDifficultyMenu();
        Region s1 = new Region();
        Region s2 = new Region();
        Region s3 = new Region();
        Region s4 = new Region();
        HBox.setHgrow(s1, Priority.ALWAYS);
        HBox.setHgrow(s2, Priority.ALWAYS);
        HBox.setHgrow(s3, Priority.ALWAYS);
        HBox.setHgrow(s4, Priority.ALWAYS);
        root = new HBox();
        root.setAlignment(Pos.CENTER_LEFT);
        root.setPadding(new Insets(8, 12, 8, 12));
        root.getStyleClass().add("header");
        root.getChildren().addAll(
                timerLabel,
                s1,
                mineCountLabel,
                s2,
                resetButton,
                s3,
                pauseButton,
                s4,
                difficultyMenu
        );
    }


    private void initDifficultyMenu() {
        for (Difficulty d : Difficulty.values()) {
            MenuItem item = new MenuItem(d.name().charAt(0) + d.name().substring(1).toLowerCase());
            item.setOnAction(e -> {
                difficultyMenu.setText(item.getText());
                if (onDifficultyChange != null) onDifficultyChange.accept(d);
            });
            difficultyMenu.getItems().add(item);
        }
    }

    // ── Binding ───────────────────────────────────────────────

    public void bindTimer(IntegerProperty secondsProp) {
        timerLabel.textProperty().bind(secondsProp.asString("%03d"));
    }

    public void bindMineCount(IntegerProperty remainingMinesProp) {
        mineCountLabel.textProperty().bind(remainingMinesProp.asString("%03d"));
    }

    // ── Event setters ─────────────────────────────────────────

    public void setOnReset(Runnable handler) {
        resetButton.setOnAction(e -> handler.run());
    }

    public void setOnPause(Runnable handler) {
        this.onPause = handler;
    }

    public void setOnDifficultyChange(Consumer<Difficulty> handler) {
        this.onDifficultyChange = handler;
    }
    public void setDifficulty(Difficulty d) {
        String name = d.name().charAt(0) + d.name().substring(1).toLowerCase();
        difficultyMenu.setText(name);
    }
    // ── UI updates ────────────────────────────────────────────

    public void setResetEmoji(String emoji) {
        resetButton.setText(emoji);
    }
    public void setPauseEmoji(boolean isPaused) {
        pauseButton.setText(isPaused ? "▶" : "⏸");
    }
    public void showBestTime(int seconds) {
        String text = (seconds == Integer.MAX_VALUE) ? "Best: --" : "Best: " + seconds + "s";
        difficultyMenu.setTooltip(new Tooltip(text));
    }

    public HBox getRoot() { return root; }
}
