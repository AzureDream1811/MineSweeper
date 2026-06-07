package com.minesweeper.model;

import com.minesweeper.controller.Difficulty;
import org.junit.jupiter.api.*;

import java.io.File;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class cho ScoreRecord (model singleton - lưu file .properties).
 * Vị trí: src/test/java/com/minesweeper/model/ScoreRecordTest.java
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ScoreRecordTest {

    @BeforeEach
    void resetSingleton() throws Exception {
        // Reset singleton trước mỗi test để tránh state dư từ test trước
        Field instance = ScoreRecord.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);

        // Xoá file scores.properties nếu tồn tại
        new File("scores.properties").delete();
    }

    @AfterEach
    void cleanup() {
        new File("scores.properties").delete();
    }

    /**
     *  11.1.0: Chưa có kỷ lục → trả về Integer.MAX_VALUE.
     */
    @Test
    @Order(1)
    @DisplayName("getBestTime - trả về MAX_VALUE khi chưa có kỷ lục")
    void getBestTime_shouldReturnMaxValue_whenNoRecord() {
        // Arrange
        ScoreRecord record = ScoreRecord.getInstance();

        // Act
        int result = record.getBestTime(Difficulty.EASY);

        // Assert
        assertEquals(Integer.MAX_VALUE, result,
                "Chưa có kỷ lục phải trả về Integer.MAX_VALUE");
    }

    /**
     *  11.1.3: update() lưu kỷ lục mới khi thời gian tốt hơn.
     *  11.3.1: Sau khi thắng, hệ thống lưu kỷ lục mới.
     */
    @Test
    @Order(2)
    @DisplayName("update - lưu kỷ lục mới và trả về true khi thời gian tốt hơn")
    void update_shouldSaveAndReturnTrue_whenBetterTime() {
        // Arrange
        ScoreRecord record = ScoreRecord.getInstance();

        // Act
        boolean isNew = record.update(Difficulty.EASY, 42);

        // Assert
        assertTrue(isNew, "Kỷ lục đầu tiên phải là kỷ lục mới");
        assertEquals(42, record.getBestTime(Difficulty.EASY),
                "Best time phải được cập nhật thành 42s");
    }

    /**
     *  11.1.3: update() không ghi đè khi thời gian kém hơn.
     */
    @Test
    @Order(3)
    @DisplayName("update - không cập nhật và trả về false khi thời gian kém hơn")
    void update_shouldNotSaveAndReturnFalse_whenWorseTime() {
        // Arrange
        ScoreRecord record = ScoreRecord.getInstance();
        record.update(Difficulty.EASY, 42); // kỷ lục hiện tại = 42s

        // Act
        boolean isNew = record.update(Difficulty.EASY, 99); // tệ hơn

        // Assert
        assertFalse(isNew, "Thời gian kém hơn không phải kỷ lục mới");
        assertEquals(42, record.getBestTime(Difficulty.EASY),
                "Best time không được thay đổi");
    }

    /**
     *  11.1.0: hasRecord() trả về đúng trạng thái.
     */
    @Test
    @Order(4)
    @DisplayName("hasRecord - trả về false trước khi có kỷ lục, true sau khi update")
    void hasRecord_shouldReflectRecordState() {
        // Arrange
        ScoreRecord record = ScoreRecord.getInstance();

        // Act & Assert — trước khi update
        assertFalse(record.hasRecord(Difficulty.MEDIUM),
                "Chưa có kỷ lục phải trả về false");

        // Act & Assert — sau khi update
        record.update(Difficulty.MEDIUM, 60);
        assertTrue(record.hasRecord(Difficulty.MEDIUM),
                "Sau khi lưu phải trả về true");
    }

    /**
     *  11.1.0: update() của một difficulty không ảnh hưởng difficulty khác.
     */
    @Test
    @Order(5)
    @DisplayName("update - các mức độ khó độc lập nhau")
    void update_shouldBeIndependentPerDifficulty() {
        // Arrange
        ScoreRecord record = ScoreRecord.getInstance();

        // Act
        record.update(Difficulty.EASY, 42);
        record.update(Difficulty.HARD, 120);

        // Assert
        assertEquals(42,              record.getBestTime(Difficulty.EASY));
        assertEquals(Integer.MAX_VALUE, record.getBestTime(Difficulty.MEDIUM),
                "MEDIUM chưa có kỷ lục phải vẫn là MAX_VALUE");
        assertEquals(120,             record.getBestTime(Difficulty.HARD));
    }

    /**
     *  11.1.0: Dữ liệu được persist và load lại đúng từ file.
     */
    @Test
    @Order(6)
    @DisplayName("save và load - kỷ lục được lưu và đọc lại đúng từ file")
    void saveAndLoad_shouldPersistAcrossInstances() throws Exception {
        // Arrange — lưu kỷ lục
        ScoreRecord record1 = ScoreRecord.getInstance();
        record1.update(Difficulty.EASY, 42);

        // Reset singleton để giả lập khởi động lại app
        Field instance = ScoreRecord.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);

        // Act — tạo instance mới, load từ file
        ScoreRecord record2 = ScoreRecord.getInstance();

        // Assert
        assertEquals(42, record2.getBestTime(Difficulty.EASY),
                "Kỷ lục phải được đọc lại đúng sau khi restart");
    }
}