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

    /**
     * [UC-14] View Game Result PvP (Hiển thị popup kết quả đối kháng và bảng thống kê so sánh thông số).
     * 14.1.1: Hệ thống khởi tạo một Dialog popup kết quả dạng không viền (StageStyle.UNDECORATED) và thiết lập hiệu ứng mở rộng (ScaleTransition).
     */
    public Action showPvP(Stage ownerStage, int winner, 
                          String p1Name, String p2Name, 
                          int timeP1, int timeP2, 
                          int flagsP1, int flagsP2) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(ownerStage);
        dialog.initStyle(StageStyle.UNDECORATED); // 14.1.1

        VBox root = new VBox(0);
        root.setAlignment(Pos.CENTER);
        
        // 14.1.2: Hệ thống kiểm tra kết quả nhận được từ UC-13 để cập nhật tiêu đề chiến thắng tương ứng.
        String titleText;
        String bannerStyle;
        if (winner == 1) {
            titleText = p1Name.toUpperCase() + " CHIẾN THẮNG!";
            bannerStyle = "gr-accent-bar-win"; 
        } else if (winner == 2) {
            titleText = p2Name.toUpperCase() + " CHIẾN THẮNG!";
            bannerStyle = "gr-accent-bar-lose"; // Sử dụng tone đỏ
        } else {
            titleText = "KẾT QUẢ: HÒA NHAU!";
            bannerStyle = "gr-accent-bar-win"; // Cột mốc hoành tráng
        }

        Label accentBar = new Label();
        accentBar.getStyleClass().add(bannerStyle);
        accentBar.setMaxWidth(Double.MAX_VALUE);

        Label titleLabel = new Label(titleText);
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-padding: 20px;");
        if (winner == 1) titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-padding: 20px; -fx-text-fill: #4CAF50;");
        else if (winner == 2) titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-padding: 20px; -fx-text-fill: #F44336;");
        else titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-padding: 20px; -fx-text-fill: #FFC107;");

        // 14.1.3: Hệ thống dựng một bảng số liệu Grid hiển thị song song các thông số hiệu năng của cả 2 người chơi (Thời gian sống sót/hoàn thành và Số cờ đã cắm) để đối chiếu trực quan.
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(40);
        grid.setVgap(15);
        grid.setPadding(new Insets(10, 20, 20, 20));

        Label lblP1 = new Label(p1Name);
        lblP1.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #ddd;");
        Label lblP2 = new Label(p2Name);
        lblP2.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #ddd;");
        grid.add(lblP1, 1, 0);
        grid.add(lblP2, 2, 0);

        Label lblTime = new Label("⏱ Thời gian (giây):");
        lblTime.setStyle("-fx-font-weight: bold; -fx-text-fill: #aaa;");
        grid.add(lblTime, 0, 1);
        
        Label lblTime1 = new Label(String.valueOf(timeP1));
        lblTime1.setStyle("-fx-font-size: 16px; -fx-text-fill: #fff;");
        grid.add(lblTime1, 1, 1);
        
        Label lblTime2 = new Label(String.valueOf(timeP2));
        lblTime2.setStyle("-fx-font-size: 16px; -fx-text-fill: #fff;");
        grid.add(lblTime2, 2, 1);

        Label lblFlags = new Label("🚩 Số cờ:");
        lblFlags.setStyle("-fx-font-weight: bold; -fx-text-fill: #aaa;");
        grid.add(lblFlags, 0, 2);
        
        Label lblFlags1 = new Label(String.valueOf(flagsP1));
        lblFlags1.setStyle("-fx-font-size: 16px; -fx-text-fill: #fff;");
        grid.add(lblFlags1, 1, 2);
        
        Label lblFlags2 = new Label(String.valueOf(flagsP2));
        lblFlags2.setStyle("-fx-font-size: 16px; -fx-text-fill: #fff;");
        grid.add(lblFlags2, 2, 2);

        // 14.1.4: Hệ thống hiển thị hai nút chức năng điều hướng: "THI ĐẤU LẠI" (Restart) và "QUAY LẠI MENU" (Quit to Menu).
        HBox btnBox = new HBox(15);
        btnBox.setAlignment(Pos.CENTER);
        btnBox.setPadding(new Insets(20, 20, 20, 20));

        Button restartBtn = new Button("THI ĐẤU LẠI");
        restartBtn.getStyleClass().add("gr-btn-restart-win");
        restartBtn.setOnAction(e -> {
            result = Action.RESTART;
            dialog.close();
        });

        Button menuBtn = new Button("QUAY LẠI MENU");
        menuBtn.getStyleClass().add("gr-btn-menu");
        menuBtn.setOnAction(e -> {
            // 14.1.5: Khi người chơi lựa chọn, hệ thống đóng Dialog và trả về Action tương ứng cho GameController để xử lý vòng đời tiếp theo.
            result = Action.QUIT_TO_MENU;
            dialog.close();
        });

        btnBox.getChildren().addAll(menuBtn, restartBtn);

        root.getChildren().addAll(accentBar, titleLabel, grid, btnBox);
        root.setStyle("-fx-background-color: #2b2b2b; -fx-border-color: #444; -fx-border-width: 2px; -fx-border-radius: 8px; -fx-background-radius: 8px;");

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        dialog.setScene(scene);

        // Hiệu ứng entry
        root.setOpacity(0);
        root.setScaleX(0.85);
        root.setScaleY(0.85);

        FadeTransition fade = new FadeTransition(Duration.millis(200), root);
        fade.setFromValue(0); fade.setToValue(1);

        ScaleTransition scale = new ScaleTransition(Duration.millis(220), root);
        scale.setFromX(0.85); scale.setFromY(0.85); scale.setToX(1.0); scale.setToY(1.0);

        ParallelTransition anim = new ParallelTransition(fade, scale);
        dialog.setOnShown(e -> anim.play());
        
        dialog.showAndWait();
        return result;
    }
}
