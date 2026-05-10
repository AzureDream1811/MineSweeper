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
 *
 * <pre>
 * // Cách sử dụng:
 * ScoreRecord record = ScoreRecord.getInstance();
 * int best = record.getBestTime(Difficulty.EASY);
 * record.update(Difficulty.EASY, 42);
 * </pre>
 */
@SuppressWarnings("unused")
public class ScoreRecord {

    // ── Singleton ─────────────────────────────────────────────

    /**
     * Instance duy nhất của ScoreRecord (gạch chân = static trong UML).
     * Khởi tạo lazy — chỉ tạo khi lần đầu gọi {@link #getInstance()}.
     */
    private static ScoreRecord instance;

    /**
     * Lấy instance duy nhất của ScoreRecord.
     * Thread-safe với synchronized — đảm bảo không tạo 2 instance trong môi trường đa luồng.
     *
     * @return instance duy nhất
     */
    public static synchronized ScoreRecord getInstance() {
        if (instance == null) {
            instance = new ScoreRecord();
        }
        return instance;
    }

    /**
     * Constructor private — ngăn bên ngoài gọi {@code new ScoreRecord()}.
     * Đây là ràng buộc cốt lõi của Singleton pattern.
     */
    private ScoreRecord() {
        bestTimes = new HashMap<>();
        // TODO: gọi load() để đọc kỷ lục từ file nếu có
    }

    // ── Fields ────────────────────────────────────────────────

    /**
     * Map lưu kỷ lục thời gian (giây) cho từng cấp độ khó.
     * Key: Difficulty, Value: thời gian tốt nhất (giây).
     * Nếu chưa có kỷ lục cho một cấp độ → không có entry trong map.
     */
    private final Map<Difficulty, Integer> bestTimes;

    /** Tên file dùng để lưu/đọc kỷ lục */
    private static final String SAVE_FILE = "scores.dat";

    // ── Public API ────────────────────────────────────────────

    /**
     * Lấy kỷ lục của một cấp độ khó.
     * FR-07, UR-13
     *
     * @param difficulty cấp độ khó cần truy vấn
     * @return thời gian tốt nhất (giây), hoặc {@code Integer.MAX_VALUE} nếu chưa có kỷ lục
     */
    public int getBestTime(Difficulty difficulty) {
        // TODO: return bestTimes.getOrDefault(difficulty, Integer.MAX_VALUE)
        return Integer.MAX_VALUE;
    }

    /**
     * Cập nhật kỷ lục nếu thời gian mới tốt hơn thời gian hiện tại.
     * Nếu được cập nhật → gọi {@link #save()} tự động.
     * FR-07
     *
     * @param difficulty cấp độ khó
     * @param timeSeconds thời gian vừa hoàn thành (giây)
     * @return true nếu đây là kỷ lục mới, false nếu không
     */
    public boolean update(Difficulty difficulty, int timeSeconds) {
        // TODO: so sánh với getBestTime(), nếu tốt hơn → put vào map và save()
        return false;
    }

    /**
     * Kiểm tra xem đã có kỷ lục cho cấp độ này chưa.
     *
     * @param difficulty cấp độ khó
     * @return true nếu đã có kỷ lục
     */
    public boolean hasRecord(Difficulty difficulty) {
        // TODO: return bestTimes.containsKey(difficulty)
        return false;
    }

    // ── Persistence ───────────────────────────────────────────

    /**
     * Đọc kỷ lục từ file {@value #SAVE_FILE} vào {@code bestTimes}.
     * Gọi trong constructor khi khởi tạo lần đầu.
     * FR-07
     */
    public void load() {
        // TODO: dùng ObjectInputStream hoặc Properties để đọc file
        //       nếu file không tồn tại thì bỏ qua (kỷ lục trống)
    }

    /**
     * Ghi kỷ lục hiện tại ra file {@value #SAVE_FILE}.
     * Gọi tự động sau mỗi lần {@link #update(Difficulty, int)} thành công.
     * FR-07
     */
    public void save() {
        // TODO: dùng ObjectOutputStream hoặc Properties để ghi file
    }
}
