package com.minesweeper.view;

import com.minesweeper.controller.Difficulty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Scene;

/**
 * [UC5.4.2] Pop-up thiết lập cấu hình trận đấu PvP cục bộ.
 * Cho phép người chơi:
 *   - Chọn cấp độ khó chung (Dễ / Trung bình / Khó)
 *   - Nhập tên Player 1 và Player 2
 * [UC5.4.3] Xác nhận bằng nút "Bắt đầu đấu".
 */
public class PvPSetupDialog {

    /** Kết quả người chơi điền vào dialog, null nếu huỷ */
    public static class Config {
        public final Difficulty difficulty;
        public final String player1Name;
        public final String player2Name;

        Config(Difficulty difficulty, String player1Name, String player2Name) {
            this.difficulty  = difficulty;
            this.player1Name = player1Name;
            this.player2Name = player2Name;
        }
    }

    /**
     * [UC5.4.2] Hiển thị dialog cấu hình, chặn luồng UI cho đến khi đóng.
     * @param ownerStage cửa sổ cha để modal bám vào
     * @return Config nếu xác nhận, null nếu người chơi huỷ
     */
    public static Config show(Stage ownerStage) {
        // --- Dữ liệu lựa chọn ---
        // Mặc định cấp độ là EASY
        final Difficulty[] chosen = { Difficulty.EASY };
        final Config[]     result = { null };

        // --- Xây dựng UI dialog ---
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL); // Chặn thao tác cửa sổ cha
        dialog.initOwner(ownerStage);
        dialog.setTitle("Thiết lập trận PvP Cục Bộ");
        dialog.setResizable(false);

        // [UC5.4.2] Nhóm chọn cấp độ khó bằng ToggleGroup
        Label lblDiff = new Label("Cấp độ khó:");
        ToggleGroup diffGroup = new ToggleGroup();
        RadioButton rbEasy   = new RadioButton("Dễ");
        RadioButton rbMedium = new RadioButton("Trung bình");
        RadioButton rbHard   = new RadioButton("Khó");
        rbEasy.setToggleGroup(diffGroup);
        rbMedium.setToggleGroup(diffGroup);
        rbHard.setToggleGroup(diffGroup);
        rbEasy.setSelected(true); // Mặc định chọn Dễ

        // Ánh xạ toggle → Difficulty khi người chơi thay đổi lựa chọn
        diffGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            if (newT == rbEasy)        chosen[0] = Difficulty.EASY;
            else if (newT == rbMedium) chosen[0] = Difficulty.MEDIUM;
            else if (newT == rbHard)   chosen[0] = Difficulty.HARD;
        });

        HBox diffBox = new HBox(15, rbEasy, rbMedium, rbHard);
        diffBox.setAlignment(Pos.CENTER_LEFT);

        // [UC5.4.2] Ô nhập tên Player 1 và Player 2
        Label  lblP1   = new Label("Tên Người chơi 1 (Player 1):");
        TextField tfP1 = new TextField();
        tfP1.setPromptText("Player 1"); // Placeholder hiển thị giá trị mặc định

        Label  lblP2   = new Label("Tên Người chơi 2 (Player 2):");
        TextField tfP2 = new TextField();
        tfP2.setPromptText("Player 2");

        // [UC5.4.3] Nút "Bắt đầu đấu" xác nhận cấu hình
        Button btnStart  = new Button("Bắt đầu đấu ▶");
        Button btnCancel = new Button("Huỷ");

        btnStart.setDefaultButton(true); // Enter = nhấn nút này
        btnStart.setOnAction(e -> {
            // Nếu tên trống → dùng tên mặc định theo UC5.4.2
            String name1 = tfP1.getText().isBlank() ? "Player 1" : tfP1.getText().trim();
            String name2 = tfP2.getText().isBlank() ? "Player 2" : tfP2.getText().trim();
            result[0] = new Config(chosen[0], name1, name2); // Lưu kết quả
            dialog.close();
        });

        btnCancel.setOnAction(e -> dialog.close()); // Huỷ → result giữ null

        HBox btnBox = new HBox(12, btnStart, btnCancel);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        // Ghép toàn bộ thành layout dọc
        VBox layout = new VBox(12,
            lblDiff, diffBox,
            new Separator(),
            lblP1, tfP1,
            lblP2, tfP2,
            new Separator(),
            btnBox
        );
        layout.setPadding(new Insets(20));
        layout.setPrefWidth(350);

        dialog.setScene(new Scene(layout));
        dialog.showAndWait(); // Chặn đến khi dialog đóng

        return result[0]; // null nếu người chơi huỷ
    }
}
