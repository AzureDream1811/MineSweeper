package com.minesweeper.controller;

import com.minesweeper.model.Board;
import com.minesweeper.model.GameState;
import com.minesweeper.model.PvPRequestType;
import com.minesweeper.view.PvPSetupDialog;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

class PvPGameControllerTest {

    @BeforeAll
    static void startJavaFx() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
            latch.await();
        } catch (IllegalStateException ignored) {
            // JavaFX đã được start
        }
    }

    @Test
    void resetRequest_shouldRequireOtherPlayerConfirmation_andResetMatch() throws Exception {
        PvPGameController controller = createController();

        Board oldBoardP1 = getField(controller, "boardP1");

        runFx(() -> invoke(controller, "requestAction",
                new Class[]{int.class, PvPRequestType.class},
                1, PvPRequestType.RESET));

        assertTrue(getBooleanField(controller, "waitingConfirmation"));

        // Người yêu cầu không được tự xác nhận
        runFx(() -> invoke(controller, "confirmRequest",
                new Class[]{int.class},
                1));

        assertTrue(getBooleanField(controller, "waitingConfirmation"));

        // Player 2 xác nhận
        runFx(() -> invoke(controller, "confirmRequest",
                new Class[]{int.class},
                2));

        Board newBoardP1 = getField(controller, "boardP1");

        assertNotSame(oldBoardP1, newBoardP1);
        assertEquals(GameState.PVP_SPLIT_START, getField(controller, "stateP1"));
        assertEquals(GameState.PVP_SPLIT_START, getField(controller, "stateP2"));
        assertFalse(getBooleanField(controller, "waitingConfirmation"));
        assertFalse(getBooleanField(controller, "p1Started"));
        assertFalse(getBooleanField(controller, "p2Started"));
    }

    @Test
    void pauseRequest_shouldPauseBothPlayers_afterOtherPlayerConfirms() throws Exception {
        PvPGameController controller = createController();

        runFx(() -> invoke(controller, "requestAction",
                new Class[]{int.class, PvPRequestType.class},
                1, PvPRequestType.PAUSE));

        assertTrue(getBooleanField(controller, "waitingConfirmation"));

        runFx(() -> invoke(controller, "confirmRequest",
                new Class[]{int.class},
                2));

        assertEquals(GameState.PAUSED, getField(controller, "stateP1"));
        assertEquals(GameState.PAUSED, getField(controller, "stateP2"));
        assertFalse(getBooleanField(controller, "waitingConfirmation"));
    }

    @Test
    void resumeRequest_shouldResumeBothPlayers_afterOtherPlayerConfirms() throws Exception {
        PvPGameController controller = createController();

        // Pause trước
        runFx(() -> invoke(controller, "requestAction",
                new Class[]{int.class, PvPRequestType.class},
                1, PvPRequestType.PAUSE));
        runFx(() -> invoke(controller, "confirmRequest",
                new Class[]{int.class},
                2));

        assertEquals(GameState.PAUSED, getField(controller, "stateP1"));

        // Resume
        runFx(() -> invoke(controller, "requestAction",
                new Class[]{int.class, PvPRequestType.class},
                2, PvPRequestType.RESUME));
        runFx(() -> invoke(controller, "confirmRequest",
                new Class[]{int.class},
                1));

        assertEquals(GameState.PVP_SPLIT_START, getField(controller, "stateP1"));
        assertEquals(GameState.PVP_SPLIT_START, getField(controller, "stateP2"));
        assertFalse(getBooleanField(controller, "waitingConfirmation"));
    }

    @Test
    void rejectedRequest_shouldNotChangeGameState() throws Exception {
        PvPGameController controller = createController();

        runFx(() -> invoke(controller, "requestAction",
                new Class[]{int.class, PvPRequestType.class},
                1, PvPRequestType.PAUSE));

        runFx(() -> invoke(controller, "rejectRequest",
                new Class[]{int.class},
                2));

        assertEquals(GameState.PVP_SPLIT_START, getField(controller, "stateP1"));
        assertEquals(GameState.PVP_SPLIT_START, getField(controller, "stateP2"));
        assertFalse(getBooleanField(controller, "waitingConfirmation"));
    }

    @Test
    void requestIgnored_whenInvalidOrAlreadyWaiting() throws Exception {
        PvPGameController controller = createController();

        // 1. Gửi RESUME khi đang không PAUSE -> không thay đổi trạng thái và không đợi confirmation
        runFx(() -> invoke(controller, "requestAction",
                new Class[]{int.class, PvPRequestType.class},
                1, PvPRequestType.RESUME));
        assertFalse(getBooleanField(controller, "waitingConfirmation"));

        // 2. Gửi PAUSE hợp lệ -> vào trạng thái đợi confirmation
        runFx(() -> invoke(controller, "requestAction",
                new Class[]{int.class, PvPRequestType.class},
                1, PvPRequestType.PAUSE));
        assertTrue(getBooleanField(controller, "waitingConfirmation"));

        // 3. Gửi thêm yêu cầu RESET trong khi đang đợi -> yêu cầu sau bị bỏ qua, pendingRequest vẫn là PAUSE
        runFx(() -> invoke(controller, "requestAction",
                new Class[]{int.class, PvPRequestType.class},
                2, PvPRequestType.RESET));
        assertEquals(PvPRequestType.PAUSE, getField(controller, "pendingRequest"));

        // Xác nhận PAUSE để game tạm dừng
        runFx(() -> invoke(controller, "confirmRequest",
                new Class[]{int.class},
                2));
        assertEquals(GameState.PAUSED, getField(controller, "stateP1"));

        // 4. Gửi PAUSE khi đang PAUSE -> không đổi gì
        runFx(() -> invoke(controller, "requestAction",
                new Class[]{int.class, PvPRequestType.class},
                1, PvPRequestType.PAUSE));
        assertFalse(getBooleanField(controller, "waitingConfirmation"));
    }

    private static PvPGameController createController() throws Exception {
        return runFx(() -> {
            Constructor<PvPSetupDialog.Config> constructor =
                    PvPSetupDialog.Config.class.getDeclaredConstructor(
                            Difficulty.class,
                            String.class,
                            String.class
                    );

            constructor.setAccessible(true);

            PvPSetupDialog.Config config =
                    constructor.newInstance(Difficulty.EASY, "Player 1", "Player 2");

            return new PvPGameController(config);
        });
    }

    private static Object invoke(Object target, String methodName, Class<?>[] paramTypes, Object... args) {
        try {
            Method method = target.getClass().getDeclaredMethod(methodName, paramTypes);
            method.setAccessible(true);
            return method.invoke(target, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T getField(Object target, String fieldName) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(target);
    }

    private static boolean getBooleanField(Object target, String fieldName) throws Exception {
        return getField(target, fieldName);
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
