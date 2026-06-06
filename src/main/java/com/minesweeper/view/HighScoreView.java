package com.minesweeper.view;

import com.minesweeper.controller.Difficulty;
import com.minesweeper.model.ScoreRecord;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Dialog hiển thị bảng High Score (Best Time) theo từng cấp độ.
 * Thiết kế hài hòa với dark theme hiện tại của game.
 */
public class HighScoreView {

    private final ScoreRecord record;

    public HighScoreView(ScoreRecord record) {
        this.record = record;
    }

    /**
     * Mở dialog High Score modal trên owner window.
     * @param ownerStage cửa sổ cha (để căn giữa)
     * @param newRecordDifficulty nếu vừa lập kỷ lục mới, truyền difficulty đó để highlight; null nếu không
     */
    public void show(Stage ownerStage, Difficulty newRecordDifficulty) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(ownerStage);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.setTitle("High Score");

        // ── Root container ─────────────────────────────────────
        VBox root = new VBox(0);
        root.getStyleClass().add("hs-root");

        // ── Title bar ──────────────────────────────────────────
        HBox titleBar = new HBox();
        titleBar.getStyleClass().add("hs-title-bar");
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(12, 16, 12, 16));

        Label trophy = new Label("🏆");
        trophy.getStyleClass().add("hs-trophy");

        Label titleLabel = new Label("HIGH SCORE");
        titleLabel.getStyleClass().add("hs-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeBtn = new Button("✕");
        closeBtn.getStyleClass().add("hs-close-btn");
        closeBtn.setOnAction(e -> dialog.close());

        titleBar.getChildren().addAll(trophy, titleLabel, spacer, closeBtn);

        // ── Divider ─────────────────────────────────────────────
        Label divider = new Label();
        divider.getStyleClass().add("hs-divider");
        divider.setMaxWidth(Double.MAX_VALUE);

        // ── Score rows ──────────────────────────────────────────
        VBox scoreBox = new VBox(8);
        scoreBox.setPadding(new Insets(20, 24, 8, 24));

        for (Difficulty d : Difficulty.values()) {
            HBox row = buildScoreRow(d, newRecordDifficulty);
            scoreBox.getChildren().add(row);
        }

        // ── Hint ────────────────────────────────────────────────
        Label hint = new Label("Thời gian tốt nhất cho mỗi cấp độ");
        hint.getStyleClass().add("hs-hint");
        hint.setPadding(new Insets(4, 24, 4, 24));

        // ── Close button bar ────────────────────────────────────
        HBox btnBar = new HBox();
        btnBar.setAlignment(Pos.CENTER);
        btnBar.setPadding(new Insets(16, 24, 20, 24));

        Button okBtn = new Button("ĐÓNG");
        okBtn.getStyleClass().add("hs-ok-btn");
        okBtn.setOnAction(e -> dialog.close());
        btnBar.getChildren().add(okBtn);

        root.getChildren().addAll(titleBar, divider, scoreBox, hint, btnBar);

        // ── Scene ────────────────────────────────────────────────
        Scene scene = new Scene(root);
        scene.getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm()
        );
        dialog.setScene(scene);
        dialog.setResizable(false);
        dialog.showAndWait();
    }

    /** Tạo một hàng hiển thị kỷ lục cho một difficulty */
    private HBox buildScoreRow(Difficulty d, Difficulty highlight) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 14, 10, 14));
        row.getStyleClass().add("hs-row");

        if (d == highlight) {
            row.getStyleClass().add("hs-row-new");
        }

        // Emoji + tên cấp độ
        String emoji = switch (d) {
            case EASY   -> "🟢";
            case MEDIUM -> "🟡";
            case HARD   -> "🔴";
        };

        Label diffLabel = new Label(emoji + "  " + capitalize(d.name()));
        diffLabel.getStyleClass().add("hs-diff-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Thời gian
        int best = record.getBestTime(d);
        String timeText = (best == Integer.MAX_VALUE) ? "--" : best + " s";

        Label timeLabel = new Label(timeText);
        timeLabel.getStyleClass().add(best == Integer.MAX_VALUE ? "hs-time-empty" : "hs-time");

        // Badge "NEW!" nếu vừa lập kỷ lục
        if (d == highlight) {
            Label badge = new Label("NEW!");
            badge.getStyleClass().add("hs-new-badge");
            row.getChildren().addAll(diffLabel, spacer, timeLabel, badge);
        } else {
            row.getChildren().addAll(diffLabel, spacer, timeLabel);
        }

        return row;
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.charAt(0) + s.substring(1).toLowerCase();
    }
}
