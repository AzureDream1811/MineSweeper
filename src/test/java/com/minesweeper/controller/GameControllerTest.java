package com.minesweeper.controller;

import com.minesweeper.model.Board;
import com.minesweeper.model.GameState;
import com.minesweeper.model.GameTimer;
import com.minesweeper.model.ScoreRecord;
import com.minesweeper.view.BoardView;
import com.minesweeper.view.HeaderView;
import com.minesweeper.view.MainView;
import javafx.application.Platform;
import javafx.scene.control.Button;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

public class GameControllerTest {

    @BeforeAll
    static void startJavaFx() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
            latch.await();
        } catch (IllegalStateException ignored) {
            // JavaFX has already been started
        }
    }

    // ─────────────────────────────────────────────────────────
    // UC 6 - Reset Game Test Cases
    // ─────────────────────────────────────────────────────────

    @Test
    void reset_shouldRecreateBoardAndSetStateToIdle_whenGameIsPlaying() throws Exception {
        GameController controller = createController();

        // Giả lập trạng thái PLAYING và timer đang chạy
        runFx(() -> {
            setField(controller, "gameState", GameState.PLAYING);
            GameTimer timer = getField(controller, "timer");
            timer.elapsedSecondsProperty().set(25);
            return null;
        });

        Board oldBoard = getField(controller, "board");
        assertNotNull(oldBoard);

        // Gọi reset
        runFx(() -> {
            controller.reset();
            return null;
        });

        Board newBoard = getField(controller, "board");
        assertNotNull(newBoard);
        assertNotSame(oldBoard, newBoard);
        assertEquals(GameState.IDLE, getField(controller, "gameState"));

        GameTimer timer = getField(controller, "timer");
        assertEquals(0, timer.getElapsedSeconds());
    }

    @Test
    void reset_shouldRecreateBoardAndMaintainIdleState_whenGameIsIdle() throws Exception {
        GameController controller = createController();

        assertEquals(GameState.IDLE, getField(controller, "gameState"));
        Board oldBoard = getField(controller, "board");

        // Gọi reset khi đang IDLE
        runFx(() -> {
            controller.reset();
            return null;
        });

        Board newBoard = getField(controller, "board");
        assertNotSame(oldBoard, newBoard);
        assertEquals(GameState.IDLE, getField(controller, "gameState"));
    }

    @Test
    void reset_shouldHideResultOverlayAndResetToIdle_whenGameIsOver() throws Exception {
        GameController controller = createController();

        // Thiết lập trạng thái kết thúc (WIN hoặc LOSE)
        runFx(() -> {
            setField(controller, "gameState", GameState.WIN);
            return null;
        });

        Board oldBoard = getField(controller, "board");

        // Gọi reset khi đang WIN
        runFx(() -> {
            controller.reset();
            return null;
        });

        Board newBoard = getField(controller, "board");
        assertNotSame(oldBoard, newBoard);
        assertEquals(GameState.IDLE, getField(controller, "gameState"));
        GameTimer timer = getField(controller, "timer");
        assertEquals(0, timer.getElapsedSeconds());
    }

    // ─────────────────────────────────────────────────────────
    // UC 7 - Pause Game Test Cases
    // ─────────────────────────────────────────────────────────

    @Test
    void pause_shouldPauseTimerAndDisableBoard_whenGameIsPlaying() throws Exception {
        GameController controller = createController();
        MainView mainView = getField(controller, "mainView");

        // Thiết lập game ở trạng thái PLAYING
        runFx(() -> {
            setField(controller, "gameState", GameState.PLAYING);
            return null;
        });

        // Gọi pause
        runFx(() -> {
            controller.pause();
            return null;
        });

        assertEquals(GameState.PAUSED, getField(controller, "gameState"));
        assertTrue(mainView.getBoardView().getGrid().isDisable());

        Button pauseButton = getField(mainView.getHeaderView(), "pauseButton");
        assertEquals("▶", pauseButton.getText());
    }

    @Test
    void pause_shouldBeIgnored_whenGameIsIdle() throws Exception {
        GameController controller = createController();
        MainView mainView = getField(controller, "mainView");

        assertEquals(GameState.IDLE, getField(controller, "gameState"));

        // Gọi pause khi IDLE
        runFx(() -> {
            controller.pause();
            return null;
        });

        // Trạng thái vẫn là IDLE, bàn cờ không bị khóa, nút pause không đổi icon
        assertEquals(GameState.IDLE, getField(controller, "gameState"));
        assertFalse(mainView.getBoardView().getGrid().isDisable());
        Button pauseButton = getField(mainView.getHeaderView(), "pauseButton");
        assertEquals("⏸", pauseButton.getText());
    }

    @Test
    void pause_shouldBeIgnored_whenGameIsAlreadyOver() throws Exception {
        GameController controller = createController();

        // Thiết lập trạng thái LOSE
        runFx(() -> {
            setField(controller, "gameState", GameState.LOSE);
            return null;
        });

        // Gọi pause
        runFx(() -> {
            controller.pause();
            return null;
        });

        // Vẫn giữ nguyên trạng thái LOSE, không bị đổi thành PAUSED
        assertEquals(GameState.LOSE, getField(controller, "gameState"));
    }

    // ─────────────────────────────────────────────────────────
    // UC 8 - Resume Game Test Cases
    // ─────────────────────────────────────────────────────────

    @Test
    void resume_shouldResumeTimerAndEnableBoard_whenGameIsPaused() throws Exception {
        GameController controller = createController();
        MainView mainView = getField(controller, "mainView");

        // Tạm dừng game trước
        runFx(() -> {
            setField(controller, "gameState", GameState.PLAYING);
            controller.pause();
            return null;
        });

        assertEquals(GameState.PAUSED, getField(controller, "gameState"));
        assertTrue(mainView.getBoardView().getGrid().isDisable());

        // Gọi resume
        runFx(() -> {
            controller.resume();
            return null;
        });

        assertEquals(GameState.PLAYING, getField(controller, "gameState"));
        assertFalse(mainView.getBoardView().getGrid().isDisable());
        Button pauseButton = getField(mainView.getHeaderView(), "pauseButton");
        assertEquals("⏸", pauseButton.getText());
    }

    @Test
    void resume_shouldBeIgnored_whenGameIsPlaying() throws Exception {
        GameController controller = createController();

        runFx(() -> {
            setField(controller, "gameState", GameState.PLAYING);
            return null;
        });

        // Gọi resume khi đang PLAYING
        runFx(() -> {
            controller.resume();
            return null;
        });

        assertEquals(GameState.PLAYING, getField(controller, "gameState"));
    }

    @Test
    void resume_shouldBeIgnored_whenGameIsIdleOrOver() throws Exception {
        GameController controller = createController();

        // 1. Kiểm tra khi IDLE
        assertEquals(GameState.IDLE, getField(controller, "gameState"));
        runFx(() -> {
            controller.resume();
            return null;
        });
        assertEquals(GameState.IDLE, getField(controller, "gameState"));

        // 2. Kiểm tra khi LOSE
        runFx(() -> {
            setField(controller, "gameState", GameState.LOSE);
            return null;
        });
        runFx(() -> {
            controller.resume();
            return null;
        });
        assertEquals(GameState.LOSE, getField(controller, "gameState"));
    }

    // ─────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────

    private static GameController createController() throws Exception {
        return runFx(() -> {
            HeaderView headerView = new HeaderView();
            BoardView boardView = new BoardView();
            ScoreRecord record = ScoreRecord.getInstance();
            MainView mainView = new MainView(headerView, boardView, record);
            GameController controller = new GameController(mainView);
            controller.newGame(); // Khởi chạy ván đầu tiên để khởi tạo board
            return controller;
        });
    }

    @SuppressWarnings("unchecked")
    private static <T> T getField(Object target, String fieldName) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(target);
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static <T> T runFx(FxCallable<T> callable) throws Exception {
        if (Platform.isFxApplicationThread()) {
            return callable.call();
        }

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<T> result = new AtomicReference<>();
        AtomicReference<Throwable> error = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                result.set(callable.call());
            } catch (Throwable e) {
                error.set(e);
            } finally {
                latch.countDown();
            }
        });

        latch.await();

        if (error.get() != null) {
            throw new RuntimeException(error.get());
        }

        return result.get();
    }

    @FunctionalInterface
    private interface FxCallable<T> {
        T call() throws Exception;
    }
}
