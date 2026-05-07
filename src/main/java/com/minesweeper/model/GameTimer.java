package com.minesweeper.model;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.util.Duration;

/**
 * Đồng hồ đếm giây cho ván chơi Minesweeper.
 * <p>
 * Dùng JavaFX {@link Timeline} để đếm theo từng giây.
 * {@code elapsedSeconds} là {@link IntegerProperty} để HeaderView
 * có thể bind trực tiếp mà không cần Controller cập nhật thủ công
 * — Observer pattern.
 * </p>
 * <p>
 * Timer chỉ bắt đầu khi người chơi mở ô đầu tiên — FR-06, UR-12.
 * </p>
 */
public class GameTimer {

    // ── Fields ────────────────────────────────────────────────

    /**
     * Số giây đã trôi qua kể từ khi bắt đầu.
     * IntegerProperty để View bind — Observer pattern.
     */
    private final IntegerProperty elapsedSeconds = new SimpleIntegerProperty(0);

    /**
     * JavaFX Timeline chạy định kỳ mỗi 1 giây.
     */
    private Timeline timeline;

    // ── Constructor ───────────────────────────────────────────

    /**
     * Khởi tạo GameTimer.
     * Timeline được cấu hình nhưng chưa chạy cho đến khi gọi {@link #start()}.
     */
    public GameTimer() {
        timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                    if (elapsedSeconds.get() < 999) // giới hạn tối đa như Minesweeper gốc
                        elapsedSeconds.set(elapsedSeconds.get() + 1);
                })
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
    }

    public void start() {
        timeline.play();
    }

    public void pause() {
        timeline.pause();
    }

    public void resume() {
        timeline.play();
    }

    public void reset() {
        timeline.stop();
        elapsedSeconds.set(0);
    }
    public IntegerProperty elapsedSecondsProperty() {
        return elapsedSeconds;
    }

    /** @return số giây hiện tại */
    public int getElapsedSeconds() {
        return elapsedSeconds.get();
    }
}