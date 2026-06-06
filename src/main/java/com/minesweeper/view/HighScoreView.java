package com.minesweeper.view;

import com.minesweeper.controller.Difficulty;
import com.minesweeper.data.dao.ScoreRecordDAO;
import com.minesweeper.data.dao.ScoreRecordDAO.SoloRankRow;
import com.minesweeper.data.dao.ScoreRecordDAO.PvpMatchRow;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * UC11 — Xem bảng High Score.
 * Dialog hiển thị bảng xếp hạng chế độ Solo và lịch sử PvP.
 * Đọc dữ liệu thẳng từ DB qua ScoreRecordDAO — không phụ thuộc file .properties.
 */
public class HighScoreView {

    private static final int MAX_SOLO_ROWS = 10;
    private static final int MAX_PVP_ROWS  = 20;
    private static final SimpleDateFormat DATE_FMT =
            new SimpleDateFormat("dd/MM/yyyy HH:mm");

    private final ScoreRecordDAO dao = new ScoreRecordDAO();

    private String  currentDifficulty = "EASY";
    private boolean showingPvp        = false;

    private VBox   contentArea;
    private Button btnEasy, btnMedium, btnHard;
    private Button btnTabSolo, btnTabPvp;
    private Stage  dialog;

    // ── Entry point ───────────────────────────────────────────

    public void show(Stage ownerStage, Difficulty highlightDifficulty) {
        //  11.3.2: Tự động chọn mức độ vừa lập kỷ lục khi mở dialog.
        if (highlightDifficulty != null) {
            currentDifficulty = highlightDifficulty.name();
        }

        dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(ownerStage);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.setTitle("High Score");

        //  11.1.2  Hệ thống mở dialog High Score dạng modal.
        VBox root = new VBox(0);
        root.getStyleClass().add("hs-root");
        root.setPrefWidth(480);

        root.getChildren().addAll(
                buildTitleBar(),
                buildTabBar(),
                buildDifficultyBar(),
                buildDivider(),
                buildContentArea(),
                buildFooter()
        );

        //   11.1.3. Hệ thống truy vấn cơ sở dữ liệu lấy danh sách thời gian tốt, sắp xếp theo thời gian. Hệ thống hiển thị bảng xếp hạng Hệ thống truy vấn cơ sở dữ liệu lấy danh sách thời gian tốt, sắp xếp theo thời gian
        refreshContent();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm()
        );
        dialog.setScene(scene);
        dialog.setResizable(false);
        dialog.showAndWait();
    }

    // ── Title bar ─────────────────────────────────────────────

    /**
     *  11.1.2: Hệ thống mở dialog High Score dạng modal.
     *  11.1.6 Người chơi nhấn nút "Đóng" hoặc nút X để đóng dialog và quay về màn hình trước đó.: Người chơi nhấn ✕ để đóng dialog và quay về màn hình trước đó.
     */
    private HBox buildTitleBar() {
        HBox bar = new HBox(8);
        bar.getStyleClass().add("hs-title-bar");
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(12, 16, 12, 16));

        Label trophy = new Label("🏆");
        trophy.getStyleClass().add("hs-trophy");

        Label title = new Label("HIGH SCORE");
        title.getStyleClass().add("hs-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        //  11.1.6 Người chơi nhấn nút "Đóng" hoặc nút X để đóng dialog và quay về màn hình trước đó.: Người chơi nhấn nút "Đóng" hoặc ✕ để đóng dialog.
        Button closeBtn = new Button("✕");
        closeBtn.getStyleClass().add("hs-close-btn");
        closeBtn.setOnAction(e -> dialog.close());

        bar.getChildren().addAll(trophy, title, spacer, closeBtn);
        return bar;
    }

    // ── Tab bar ───────────────────────────────────────────────

    /**
     *  11.4.1: Người chơi nhấn tab "PvP" để xem lịch sử trận PvP.
     *  11.4.5: Người chơi nhấn tab "Solo" để quay lại luồng chính.
     */
    private HBox buildTabBar() {
        HBox bar = new HBox(0);
        bar.getStyleClass().add("hs-tab-bar");

        btnTabSolo = new Button("🎮  Solo");
        btnTabSolo.getStyleClass().addAll("hs-tab", "hs-tab-active");
        btnTabSolo.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnTabSolo, Priority.ALWAYS);

        btnTabPvp = new Button("⚔️  PvP");
        btnTabPvp.getStyleClass().add("hs-tab");
        btnTabPvp.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnTabPvp, Priority.ALWAYS);

        //  11.4.5: Người chơi nhấn tab "Solo" để quay lại bảng xếp hạng Solo.
        btnTabSolo.setOnAction(e -> {
            showingPvp = false;
            btnTabSolo.getStyleClass().add("hs-tab-active");
            btnTabPvp.getStyleClass().remove("hs-tab-active");
            //   11.1.3. Hệ thống truy vấn cơ sở dữ liệu lấy danh sách thời gian tốt, sắp xếp theo thời gian. Hệ thống hiển thị bảng xếp hạng: Hệ thống truy vấn lại DB khi chuyển tab.
            refreshContent();
        });

        //  11.4.1: Người chơi nhấn tab "PvP" để xem lịch sử trận PvP.
        btnTabPvp.setOnAction(e -> {
            showingPvp = true;
            btnTabPvp.getStyleClass().add("hs-tab-active");
            btnTabSolo.getStyleClass().remove("hs-tab-active");
            //  11.4.2: Hệ thống ẩn bộ lọc độ khó và truy vấn lịch sử PvP.
            refreshContent();
        });

        bar.getChildren().addAll(btnTabSolo, btnTabPvp);
        return bar;
    }

    // ── Difficulty bar ────────────────────────────────────────

    /**
     *  11.1.5 Hệ thống truy vấn lại dữ liệu và cập nhật bảng tương ứng theo bộ lọc người dùng.: Người chơi nhấn các nút chuyển độ khó (Dễ / Vừa / Khó).
     *                Hệ thống truy vấn lại dữ liệu và cập nhật bảng tương ứng.
     */
    private HBox buildDifficultyBar() {
        HBox bar = new HBox(8);
        bar.getStyleClass().add("hs-diff-bar");
        bar.setAlignment(Pos.CENTER);
        bar.setPadding(new Insets(10, 16, 10, 16));

        btnEasy   = diffBtn("🟢 Dễ",  "EASY");
        btnMedium = diffBtn("🟡 Vừa", "MEDIUM");
        btnHard   = diffBtn("🔴 Khó", "HARD");

        updateDiffButtonStyles();
        bar.getChildren().addAll(btnEasy, btnMedium, btnHard);
        return bar;
    }

    private Button diffBtn(String label, String diff) {
        Button btn = new Button(label);
        btn.getStyleClass().add("hs-diff-btn");
        //  11.1.5 Hệ thống truy vấn lại dữ liệu và cập nhật bảng tương ứng theo bộ lọc người dùng.: Người chơi nhấn nút chuyển độ khó, hệ thống truy vấn lại.
        btn.setOnAction(e -> {
            currentDifficulty = diff;
            updateDiffButtonStyles();
            //   11.1.3. Hệ thống truy vấn cơ sở dữ liệu lấy danh sách thời gian tốt, sắp xếp theo thời gian. Hệ thống hiển thị bảng xếp hạng: Hệ thống truy vấn DB theo mức độ mới được chọn.
            refreshContent();
        });
        return btn;
    }

    private void updateDiffButtonStyles() {
        for (Button b : new Button[]{btnEasy, btnMedium, btnHard}) {
            b.getStyleClass().remove("hs-diff-btn-active");
        }
        Button active = switch (currentDifficulty) {
            case "MEDIUM" -> btnMedium;
            case "HARD"   -> btnHard;
            default       -> btnEasy;
        };
        active.getStyleClass().add("hs-diff-btn-active");
    }

    private Label buildDivider() {
        Label div = new Label();
        div.getStyleClass().add("hs-divider");
        div.setMaxWidth(Double.MAX_VALUE);
        return div;
    }

    private VBox buildContentArea() {
        contentArea = new VBox(0);
        contentArea.setPrefHeight(320);
        return contentArea;
    }

    // ── Footer ────────────────────────────────────────────────

    /**
     *  11.1.6 Người chơi nhấn nút "Đóng" hoặc nút X để đóng dialog và quay về màn hình trước đó.: Người chơi nhấn nút "Đóng" để đóng dialog và quay về màn hình trước đó.
     */
    private HBox buildFooter() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER);
        bar.setPadding(new Insets(14, 24, 18, 24));

        Button okBtn = new Button("ĐÓNG");
        okBtn.getStyleClass().add("hs-ok-btn");
        //  11.1.6 Người chơi nhấn nút "Đóng" hoặc nút X để đóng dialog và quay về màn hình trước đó.: Người chơi nhấn nút "Đóng" để đóng dialog.
        okBtn.setOnAction(e -> dialog.close());
        bar.getChildren().add(okBtn);
        return bar;
    }

    // ── Refresh content ───────────────────────────────────────

    /**
     *   11.1.3. Hệ thống truy vấn cơ sở dữ liệu lấy danh sách thời gian tốt, sắp xếp theo thời gian. Hệ thống hiển thị bảng xếp hạng: Hệ thống truy vấn DB lấy danh sách kỷ lục theo mức độ và tab hiện tại.
     *  11.4.2: Hệ thống ẩn bộ lọc độ khó khi đang ở tab PvP.
     */
    private void refreshContent() {
        contentArea.getChildren().clear();

        //  11.4.2: Ẩn difficulty bar khi ở tab PvP.
        boolean isSolo = !showingPvp;
        Node diffBar = contentArea.getParent() instanceof VBox vbox
                ? vbox.getChildren().get(2)
                : null;
        if (diffBar != null) {
            diffBar.setVisible(isSolo);
            diffBar.setManaged(isSolo);
        }

        if (showingPvp) {
            //  11.4.2: Hệ thống truy vấn lịch sử các trận PvP gần nhất từ DB.
            buildPvpContent();
        } else {
            //   11.1.3. Hệ thống truy vấn cơ sở dữ liệu lấy danh sách thời gian tốt, sắp xếp theo thời gian. Hệ thống hiển thị bảng xếp hạng: Hệ thống truy vấn DB lấy danh sách kỷ lục Solo.
            buildSoloContent();
        }
    }

    // ── Solo content ──────────────────────────────────────────

    /**
     *   11.1.3. Hệ thống truy vấn cơ sở dữ liệu lấy danh sách thời gian tốt, sắp xếp theo thời gian. Hệ thống hiển thị bảng xếp hạng: Hệ thống truy vấn cơ sở dữ liệu lấy danh sách thời gian
     *                tốt nhất của từng người chơi ở chế độ Solo, chỉ tính lượt
     *                thắng (result = 'WIN'), sắp xếp tăng dần theo thời gian.
     *  11.1.4. Hệ thống hiển thị bảng xếp hạng : Hệ thống hiển thị bảng xếp hạng gồm Thứ hạng, Tên, Thời gian.
     *                Top 3 được gắn huy chương 🥇🥈🥉.
     */
    private void buildSoloContent() {
        contentArea.getChildren().add(buildSoloHeaderRow());

        //   11.1.3. Hệ thống truy vấn cơ sở dữ liệu lấy danh sách thời gian tốt, sắp xếp theo thời gian. Hệ thống hiển thị bảng xếp hạng: Truy vấn DB.
        List<SoloRankRow> rows = dao.getSoloLeaderboard(currentDifficulty, MAX_SOLO_ROWS);

        if (rows.isEmpty()) {
            //  11.2.3 Hệ thống hiển thị thông báo "Chưa có kỷ lục nào cho mức độ này" thay cho bảng dữ liệu.: Hệ thống hiển thị thông báo khi chưa có kỷ lục.
            contentArea.getChildren().add(
                    buildEmptyLabel("Chưa có kỷ lục nào cho mức độ này.")
            );
            return;
        }

        //  11.1.4. Hệ thống hiển thị bảng xếp hạng : Hệ thống hiển thị bảng xếp hạng.
        for (SoloRankRow row : rows) {
            contentArea.getChildren().add(buildSoloDataRow(row));
        }
    }

    private HBox buildSoloHeaderRow() {
        HBox row = new HBox();
        row.getStyleClass().addAll("hs-row", "hs-row-header");
        row.setPadding(new Insets(8, 16, 8, 16));
        row.setAlignment(Pos.CENTER_LEFT);
        row.getChildren().addAll(
                colLabel("#",         60,  "hs-col-rank"),
                colLabel("Tên",       220, "hs-col-name"),
                colLabel("Thời gian", 100, "hs-col-time")
        );
        return row;
    }

    /**
     *  11.1.4. Hệ thống hiển thị bảng xếp hạng : Hiển thị từng hàng dữ liệu trong bảng xếp hạng.
     *                Top 3 được gắn huy chương 🥇🥈🥉.
     *  11.3.3: Hàng kỷ lục mới được làm nổi bật và gắn nhãn "NEW!".
     */
    private HBox buildSoloDataRow(SoloRankRow data) {
        HBox row = new HBox();
        row.getStyleClass().add("hs-row");
        row.setPadding(new Insets(9, 16, 9, 16));
        row.setAlignment(Pos.CENTER_LEFT);

        //  11.1.4. Hệ thống hiển thị bảng xếp hạng : Top 3 gắn huy chương.
        String rankText = switch (data.rank) {
            case 1 -> "🥇";
            case 2 -> "🥈";
            case 3 -> "🥉";
            default -> String.valueOf(data.rank);
        };
        if (data.rank <= 3) row.getStyleClass().add("hs-row-top");

        row.getChildren().addAll(
                colLabel(rankText,                   60,  "hs-col-rank"),
                colLabel(data.playerName,            220, "hs-col-name"),
                colLabel(formatTime(data.bestSeconds), 100, "hs-col-time")
        );
        return row;
    }

    // ── PvP content ───────────────────────────────────────────

    /**
     *  11.4.2: Hệ thống truy vấn lịch sử các trận PvP gần nhất từ DB.
     *  11.4.3: Hệ thống hiển thị danh sách trận đấu.
     *  11.4.4: Nếu chưa có trận PvP, hiển thị thông báo.
     */
    private void buildPvpContent() {
        //  11.4.2: Truy vấn lịch sử PvP từ DB.
        List<PvpMatchRow> matches = dao.getPvpHistory(MAX_PVP_ROWS);

        if (matches.isEmpty()) {
            //  11.4.4: Chưa có trận PvP nào được ghi lại.
            contentArea.getChildren().add(
                    buildEmptyLabel("Chưa có trận PvP nào được ghi lại.")
            );
            return;
        }

        //  11.4.3: Hệ thống hiển thị danh sách trận đấu.
        VBox list = new VBox(0);
        for (PvpMatchRow match : matches) {
            list.getChildren().add(buildPvpMatchRow(match));
        }

        ScrollPane scroll = new ScrollPane(list);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("hs-scroll");
        scroll.setPrefHeight(320);
        contentArea.getChildren().add(scroll);
    }

    /**
     *  11.4.3: Mỗi trận gồm:
     *   thời gian dò mìn P1 — tên P1 — tỉ số — tên P2 — thời gian dò mìn P2.
     *   Người thắng được làm nổi bật.
     */
    private VBox buildPvpMatchRow(PvpMatchRow m) {
        VBox wrapper = new VBox(4);
        wrapper.getStyleClass().add("hs-pvp-match");
        wrapper.setPadding(new Insets(10, 16, 10, 16));

        boolean p1Win = "WIN".equalsIgnoreCase(m.player1Status);
        boolean p2Win = "WIN".equalsIgnoreCase(m.player2Status);
        int score1 = p1Win ? 1 : 0;
        int score2 = p2Win ? 1 : 0;

        //  11.4.3: Thời gian dò mìn của Player 1 (bên trái).
        Label t1 = new Label(formatTime(m.player1Seconds));
        t1.getStyleClass().addAll("hs-pvp-time",
                p1Win ? "hs-pvp-winner-time" : "hs-pvp-loser-time");
        t1.setPrefWidth(80);
        t1.setAlignment(Pos.CENTER_RIGHT);

        //  11.4.3: Tên Player 1, làm nổi bật nếu thắng.
        Label n1 = new Label(m.player1Name != null ? m.player1Name : "?");
        n1.getStyleClass().addAll("hs-pvp-name", p1Win ? "hs-pvp-winner" : "");
        n1.setPrefWidth(100);
        n1.setAlignment(Pos.CENTER_RIGHT);

        //  11.4.3: Tỉ số ở giữa.
        Label vs = new Label(score1 + " : " + score2);
        vs.getStyleClass().add("hs-pvp-score");
        vs.setPrefWidth(80);
        vs.setAlignment(Pos.CENTER);

        //  11.4.3: Tên Player 2, làm nổi bật nếu thắng.
        Label n2 = new Label(m.player2Name != null ? m.player2Name : "?");
        n2.getStyleClass().addAll("hs-pvp-name", p2Win ? "hs-pvp-winner" : "");
        n2.setPrefWidth(100);
        n2.setAlignment(Pos.CENTER_LEFT);

        //  11.4.3: Thời gian dò mìn của Player 2 (bên phải).
        Label t2 = new Label(formatTime(m.player2Seconds));
        t2.getStyleClass().addAll("hs-pvp-time",
                p2Win ? "hs-pvp-winner-time" : "hs-pvp-loser-time");
        t2.setPrefWidth(80);
        t2.setAlignment(Pos.CENTER_LEFT);

        HBox main = new HBox(0);
        main.setAlignment(Pos.CENTER);
        main.getChildren().addAll(t1, n1, vs, n2, t2);

        HBox sub = new HBox();
        sub.setAlignment(Pos.CENTER);
        Label dateLabel = new Label(m.sessionDate != null ? DATE_FMT.format(m.sessionDate) : "");
        dateLabel.getStyleClass().add("hs-pvp-date");
        sub.getChildren().add(dateLabel);

        wrapper.getChildren().addAll(main, sub);
        return wrapper;
    }

    // ── Helpers ───────────────────────────────────────────────

    /**
     *  11.2.3 Hệ thống hiển thị thông báo "Chưa có kỷ lục nào cho mức độ này" thay cho bảng dữ liệu.: Hiển thị thông báo khi không có dữ liệu.
     *  11.4.4: Hiển thị thông báo khi chưa có trận PvP.
     */
    private Label buildEmptyLabel(String text) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add("hs-empty");
        lbl.setMaxWidth(Double.MAX_VALUE);
        lbl.setAlignment(Pos.CENTER);
        lbl.setPadding(new Insets(40, 16, 40, 16));
        return lbl;
    }

    private Label colLabel(String text, double width, String styleClass) {
        Label lbl = new Label(text);
        lbl.setPrefWidth(width);
        lbl.getStyleClass().add(styleClass);
        return lbl;
    }

    private String formatTime(int seconds) {
        if (seconds <= 0) return "--";
        if (seconds < 60) return seconds + " s";
        return (seconds / 60) + "m " + (seconds % 60) + "s";
    }
}