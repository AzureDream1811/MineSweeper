package com.minesweeper.view;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.ParallelTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * Dialog hiển thị kết quả game (Win / Lose).
 * UC_14 — View Game Result (bao gồm thời gian, kỷ lục mới nếu có)
 * Thay thế Alert mặc định bằng popup đẹp hài hòa với dark theme.
 */
public class GameResultView {

    public enum Action { RESTART, QUIT_TO_MENU }

    private Action result = Action.QUIT_TO_MENU;

    /**
     * Hiển thị popup kết quả game.
     *
     * @param ownerStage       cửa sổ cha
     * @param win              true = thắng, false = thua
     * @param elapsedSeconds   thời gian đã trôi qua
     * @param isNewRecord      true nếu vừa lập kỷ lục mới (chỉ khi win)
     * @return Action mà người chơi chọn (RESTART hoặc QUIT_TO_MENU)
     */
    public Action show(Stage ownerStage, boolean win, int elapsedSeconds, boolean isNewRecord) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(ownerStage);
        dialog.initStyle(StageStyle.UNDECORATED);

        // ── Root ─────────────────────────────────────────────────
        VBox root = new VBox(0);
        root.getStyleClass().add(win ? "gr-root-win" : "gr-root-lose");
        root.setAlignment(Pos.CENTER);

        // ── Top accent bar ──────────────────────────────────────
        Label accentBar = new Label();
        accentBar.getStyleClass().add(win ? "gr-accent-bar-win" : "gr-accent-bar-lose");
        accentBar.setMaxWidth(Double.MAX_VALUE);

        // ── Emoji ────────────────────────────────────────────────
        Label emojiLabel = new Label(win ? "😎" : "😵");
        emojiLabel.getStyleClass().add("gr-emoji");
        emojiLabel.setPadding(new Insets(24, 0, 8, 0));

        // ── Title ────────────────────────────────────────────────
        Label titleLabel = new Label(win ? "BẠN THẮNG!" : "BẠN THUA!");
        titleLabel.getStyleClass().add(win ? "gr-title-win" : "gr-title-lose");

        // ── Subtitle ─────────────────────────────────────────────
        String subText = win
                ? "Chúc mừng! Bạn đã gỡ sạch mìn."
                : "Bạn đã mở trúng mìn. Thử lại nào!";
        Label subLabel = new Label(subText);
        subLabel.getStyleClass().add("gr-subtitle");
        subLabel.setPadding(new Insets(4, 24, 0, 24));

        // ── Time display ─────────────────────────────────────────
        HBox timeBox = new HBox(8);
        timeBox.setAlignment(Pos.CENTER);
        timeBox.setPadding(new Insets(16, 24, 0, 24));

        Label clockIcon = new Label("⏱");
        clockIcon.getStyleClass().add("gr-clock");

        Label timeLabel = new Label(elapsedSeconds + " giây");
        timeLabel.getStyleClass().add("gr-time");

        timeBox.getChildren().addAll(clockIcon, timeLabel);

        // ── New Record badge ─────────────────────────────────────
        VBox extras = new VBox(4);
        extras.setAlignment(Pos.CENTER);
        extras.setPadding(new Insets(8, 24, 0, 24));

        if (win && isNewRecord) {
            Label recordBadge = new Label("🏆  KỶ LỤC MỚI!");
            recordBadge.getStyleClass().add("gr-record-badge");
            extras.getChildren().add(recordBadge);
        }

        // ── Divider ──────────────────────────────────────────────
        Label divider = new Label();
        divider.getStyleClass().add("gr-divider");
        divider.setMaxWidth(Double.MAX_VALUE);
        divider.setPadding(new Insets(16, 24, 0, 24));

        // ── Action buttons ───────────────────────────────────────
        HBox btnBox = new HBox(12);
        btnBox.setAlignment(Pos.CENTER);
        btnBox.setPadding(new Insets(16, 24, 24, 24));

        Button menuBtn = new Button("MENU");
        menuBtn.getStyleClass().add("gr-btn-menu");
        menuBtn.setOnAction(e -> {
            result = Action.QUIT_TO_MENU;
            dialog.close();
        });

        Button restartBtn = new Button("CHƠI LẠI");
        restartBtn.getStyleClass().add(win ? "gr-btn-restart-win" : "gr-btn-restart-lose");
        restartBtn.setOnAction(e -> {
            result = Action.RESTART;
            dialog.close();
        });

        btnBox.getChildren().addAll(menuBtn, restartBtn);

        root.getChildren().addAll(
                accentBar, emojiLabel, titleLabel, subLabel,
                timeBox, extras, divider, btnBox
        );

        // ── Scene ─────────────────────────────────────────────────
        Scene scene = new Scene(root);
        scene.getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm()
        );
        dialog.setScene(scene);
        dialog.setResizable(false);

        // ── Entry animation ───────────────────────────────────────
        root.setOpacity(0);
        root.setScaleX(0.85);
        root.setScaleY(0.85);

        FadeTransition fade = new FadeTransition(Duration.millis(200), root);
        fade.setFromValue(0);
        fade.setToValue(1);

        ScaleTransition scale = new ScaleTransition(Duration.millis(220), root);
        scale.setFromX(0.85);
        scale.setFromY(0.85);
        scale.setToX(1.0);
        scale.setToY(1.0);

        ParallelTransition anim = new ParallelTransition(fade, scale);

        dialog.setOnShown(e -> anim.play());
        dialog.showAndWait();
        return result;
    }
}
