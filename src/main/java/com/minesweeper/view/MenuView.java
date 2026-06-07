package com.minesweeper.view;


import com.minesweeper.controller.Difficulty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;


public class MenuView {
    private final VBox root;
    private Consumer<Difficulty> onDifficultySelected;
    // [UC5.4.1] Callback khi người chơi nhấn nút "Chơi PvP Cục Bộ"
    private Runnable onPvPLocalRequested;
    private final Map<Difficulty, Label> bestTimeLabels = new EnumMap<>(Difficulty.class);
    public MenuView() {
        root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 20;");
        //  11.1.0: Màn hình chính hiển thị sẵn kỷ lục tốt nhất của từng mức độ khó.
        VBox bestTimeSection = buildBestTimeSection();

        // Tạo các nút chọn level cho chế độ đơn
        Button btnEasy   = createButton("Dễ (Easy)",           Difficulty.EASY);
        Button btnMedium = createButton("Trung bình (Medium)", Difficulty.MEDIUM);
        Button btnHard   = createButton("Khó (Hard)",          Difficulty.HARD);

        // [UC5.4.1] Nút mở chế độ PvP cục bộ
        Button btnPvP = new Button("⚔ Chơi PvP Cục Bộ");
        btnPvP.setPrefWidth(200);
        btnPvP.setOnAction(e -> {
            if (onPvPLocalRequested != null) onPvPLocalRequested.run();
        });

        // Phân tách chế độ đơn và PvP bằng đường kẻ
        root.getChildren().addAll(btnEasy, btnMedium, btnHard, new Separator(), btnPvP);
    }

    /**
     *  11.1.0: Màn hình chính hiển thị sẵn kỷ lục tốt nhất của từng mức độ khó.
     * Tạo section hiển thị kỷ lục ở phía trên các nút chọn độ khó.
     * Layout:
     *   🏆 Kỷ lục tốt nhất
     *   🟢 Dễ      42 s
     *   🟡 Vừa     --
     *   🔴 Khó     --
     */
    private VBox buildBestTimeSection() {
        VBox section = new VBox(6);
        section.setAlignment(Pos.CENTER);
        section.setPadding(new Insets(8, 16, 8, 16));
        section.setStyle(
                "-fx-background-color: #e8e8e8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-color: #d0d0d0;" +
                        "-fx-border-radius: 8;" +
                        "-fx-border-width: 1;"
        );
        section.setPrefWidth(200);
        section.setMaxWidth(200);

        Label title = new Label("🏆  Kỷ lục tốt nhất");
        title.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #555;");
        section.getChildren().add(title);

        String[][] configs = {
                {"🟢", "Dễ",   "EASY"},
                {"🟡", "Vừa",  "MEDIUM"},
                {"🔴", "Khó",  "HARD"}
        };

        for (String[] cfg : configs) {
            Difficulty d = Difficulty.valueOf(cfg[2]);

            Label nameLabel = new Label(cfg[0] + "  " + cfg[1]);
            nameLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");
            nameLabel.setPrefWidth(80);

            //  11.1.0: Hiển thị "--" mặc định nếu chưa có kỷ lục.
            Label timeLabel = new Label("--");
            timeLabel.setStyle(
                    "-fx-font-size: 12px; -fx-font-weight: bold;" +
                            "-fx-text-fill: #aaa; -fx-font-family: 'Consolas', monospace;"
            );
            timeLabel.setPrefWidth(60);
            timeLabel.setAlignment(Pos.CENTER_RIGHT);

            bestTimeLabels.put(d, timeLabel);

            HBox row = new HBox();
            row.setAlignment(Pos.CENTER_LEFT);
            row.getChildren().addAll(nameLabel, timeLabel);
            section.getChildren().add(row);
        }

        return section;
    }

    /**
     *  11.1.0: Màn hình chính hiển thị sẵn kỷ lục tốt nhất của từng mức độ khó.
     *  11.2.3 Hệ thống hiển thị thông báo "Chưa có kỷ lục nào cho mức độ này" thay cho bảng dữ liệu.: Hiển thị "--" nếu chưa có kỷ lục cho mức độ này.
     * Được gọi bởi GameController khi khởi động và sau khi lập kỷ lục mới.
     */
    public void updateBestTime(Difficulty difficulty, int seconds) {
        Label lbl = bestTimeLabels.get(difficulty);
        if (lbl == null) return;

        if (seconds == Integer.MAX_VALUE) {
            //  11.2.3 Hệ thống hiển thị thông báo "Chưa có kỷ lục nào cho mức độ này" thay cho bảng dữ liệu.: Hệ thống hiển thị "--" khi chưa có kỷ lục cho mức độ này.
            lbl.setText("--");
            lbl.setStyle(
                    "-fx-font-size: 12px; -fx-font-weight: bold;" +
                            "-fx-text-fill: #aaa; -fx-font-family: 'Consolas', monospace;"
            );
        } else {
            lbl.setText(formatTime(seconds));
            lbl.setStyle(
                    "-fx-font-size: 12px; -fx-font-weight: bold;" +
                            "-fx-text-fill: #337ab7; -fx-font-family: 'Consolas', monospace;"
            );
        }
    }

    private String formatTime(int seconds) {
        if (seconds < 60) return seconds + " s";
        return (seconds / 60) + "m " + (seconds % 60) + "s";
    }

    private Button createButton(String text, Difficulty difficulty) {
        Button btn = new Button(text);
        btn.setPrefWidth(200);
        btn.setOnAction(e -> {
            if (onDifficultySelected != null) onDifficultySelected.accept(difficulty);
        });
        return btn;
    }

    // Setter để Controller đăng ký hành động chọn difficulty (chế độ đơn)
    public void setOnDifficultySelected(Consumer<Difficulty> handler) {
        this.onDifficultySelected = handler;
    }

    // [UC5.4.1] Setter để GameController đăng ký xử lý yêu cầu PvP
    public void setOnPvPLocalRequested(Runnable handler) {
        this.onPvPLocalRequested = handler;
    }

    public VBox getRoot() { return root; }
}
