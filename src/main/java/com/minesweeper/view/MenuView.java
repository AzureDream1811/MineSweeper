package com.minesweeper.view;

import com.minesweeper.controller.Difficulty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import java.util.function.Consumer;

public class MenuView {
    private final VBox root;
    private Consumer<Difficulty> onDifficultySelected;
    // [UC5.4.1] Callback khi người chơi nhấn nút "Chơi PvP Cục Bộ"
    private Runnable onPvPLocalRequested;

    public MenuView() {
        root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 20;");

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