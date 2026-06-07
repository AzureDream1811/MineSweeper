package com.minesweeper.model;

import com.minesweeper.controller.Difficulty;
import java.util.HashMap;
import java.util.Map;

/**
 * Lưu trữ và quản lý kỷ lục thời gian (Best Time) cho từng cấp độ khó.
 * <p>
 * <b>Singleton pattern:</b> Toàn bộ ứng dụng chỉ có đúng một instance
 * của class này. Dùng {@link #getInstance()} để lấy instance.
 * Constructor là {@code private} — không thể gọi {@code new ScoreRecord()}.
 * </p>
 * <p>
 * Kỷ lục được lưu vào file cục bộ và tải lại mỗi khi ứng dụng khởi động.
 * FR-07
 * </p>
 */
@SuppressWarnings("unused")
public class ScoreRecord {

    // ── Singleton ─────────────────────────────────────────────

    private static ScoreRecord instance;

    public static synchronized ScoreRecord getInstance() {
        if (instance == null) {
            instance = new ScoreRecord();
        }
        return instance;
    }

    private ScoreRecord() {
        bestTimes = new HashMap<>();
        load(); // Tải kỷ lục từ file khi khởi tạo
    }

    // ── Fields ────────────────────────────────────────────────

    /** Map lưu kỷ lục (giây) cho từng difficulty */
    private final Map<Difficulty, Integer> bestTimes;

    /** File lưu kỷ lục */
    private static final String SAVE_FILE = "scores.properties";

    // ── Public API ────────────────────────────────────────────

    /**
     * Lấy kỷ lục của một cấp độ khó. FR-07
     * @return thời gian tốt nhất (giây), hoặc {@code Integer.MAX_VALUE} nếu chưa có
     */
    public int getBestTime(Difficulty difficulty) {
        return bestTimes.getOrDefault(difficulty, Integer.MAX_VALUE);
    }

    /**
     * Cập nhật kỷ lục nếu thời gian mới tốt hơn. FR-07
     * @return true nếu đây là kỷ lục mới
     */
    public boolean update(Difficulty difficulty, int timeSeconds) {
        int current = getBestTime(difficulty);
        if (timeSeconds < current) {
            bestTimes.put(difficulty, timeSeconds);
            save();
            return true;
        }
        return false;
    }

    /** Kiểm tra đã có kỷ lục cho cấp độ này chưa */
    public boolean hasRecord(Difficulty difficulty) {
        return bestTimes.containsKey(difficulty);
    }

    // ── Persistence ───────────────────────────────────────────

    /**
     * Đọc kỷ lục từ file scores.properties vào map. FR-07
     */
    public void load() {
        java.util.Properties props = new java.util.Properties();
        java.io.File file = new java.io.File(SAVE_FILE);
        if (!file.exists()) return;
        try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
            props.load(fis);
            for (Difficulty d : Difficulty.values()) {
                String val = props.getProperty(d.name());
                if (val != null) {
                    try {
                        int time = Integer.parseInt(val.trim());
                        if (time > 0) bestTimes.put(d, time);
                    } catch (NumberFormatException ignored) {
                        // Dữ liệu lỗi → bỏ qua (NFR-11)
                    }
                }
            }
        } catch (java.io.IOException ignored) {
            // File không đọc được → kỷ lục trống
        }
    }

    /**
     * Ghi kỷ lục ra file scores.properties. FR-07
     */
    public void save() {
        java.util.Properties props = new java.util.Properties();
        for (Map.Entry<Difficulty, Integer> entry : bestTimes.entrySet()) {
            props.setProperty(entry.getKey().name(), String.valueOf(entry.getValue()));
        }
        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(SAVE_FILE)) {
            props.store(fos, "Minesweeper High Scores");
        } catch (java.io.IOException ignored) {
            // Không ghi được → bỏ qua, không crash game
        }
    }
}
