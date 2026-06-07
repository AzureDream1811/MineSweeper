package com.minesweeper.controller;

import com.minesweeper.model.Board;
import com.minesweeper.model.GameState;
import com.minesweeper.model.PvPRequestType;
import com.minesweeper.view.GameResultView;
import com.minesweeper.view.PvPSetupDialog;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class PvPGameControllerTest2 {
    @BeforeAll
    static void startJavaFx() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        try {
            Platform.startup(latch::countDown);
            latch.await();
        } catch (IllegalStateException ignored) {
            // JavaFX đã được khởi động
        }
    }

    // =========================================================
    // checkMatchResult() - 2 tests
    // =========================================================

    @Test
    void checkMatchResult_p1HitsMine_shouldSetP1LoseAndAskP2Continue() throws Exception {
        TestablePvPGameController controller =
                createController(GameResultView.Action.QUIT_TO_MENU);

        runFxVoid(() -> controller.checkMatchResult(true, false, 0, 0));

        assertEquals(GameState.LOSE, getGameStateField(controller, "stateP1"));
        assertEquals(GameState.PLAYING, getGameStateField(controller, "stateP2"));
        assertEquals(1, getIntField(controller, "activePlayersCount"));

        assertTrue(getBooleanField(controller, "waitingConfirmation"));
        assertEquals(PvPRequestType.PAUSE, getRequestTypeField(controller, "pendingRequest"));
        assertEquals(1, getIntField(controller, "requestingPlayer"));
    }

    @Test
    void checkMatchResult_p2HitsMine_shouldSetP2LoseAndAskP1Continue() throws Exception {
        TestablePvPGameController controller =
                createController(GameResultView.Action.QUIT_TO_MENU);

        runFxVoid(() -> controller.checkMatchResult(false, false, 0, 0));

        assertEquals(GameState.PLAYING, getGameStateField(controller, "stateP1"));
        assertEquals(GameState.LOSE, getGameStateField(controller, "stateP2"));
        assertEquals(1, getIntField(controller, "activePlayersCount"));

        assertTrue(getBooleanField(controller, "waitingConfirmation"));
        assertEquals(PvPRequestType.PAUSE, getRequestTypeField(controller, "pendingRequest"));
        assertEquals(2, getIntField(controller, "requestingPlayer"));
    }

    // =========================================================
    // showIntermediateOverlay() - 2 tests
    // =========================================================

    @Test
    void showIntermediateOverlay_winnerIsP1_shouldSetPauseRequestAndRequestingPlayerIsP2() throws Exception {
        TestablePvPGameController controller =
                createController(GameResultView.Action.QUIT_TO_MENU);

        runFxVoid(() -> controller.showIntermediateOverlay(1));

        assertTrue(getBooleanField(controller, "waitingConfirmation"));
        assertEquals(PvPRequestType.PAUSE, getRequestTypeField(controller, "pendingRequest"));
        assertEquals(2, getIntField(controller, "requestingPlayer"));
    }

    @Test
    void showIntermediateOverlay_winnerIsP2_shouldSetPauseRequestAndRequestingPlayerIsP1() throws Exception {
        TestablePvPGameController controller =
                createController(GameResultView.Action.QUIT_TO_MENU);

        runFxVoid(() -> controller.showIntermediateOverlay(2));

        assertTrue(getBooleanField(controller, "waitingConfirmation"));
        assertEquals(PvPRequestType.PAUSE, getRequestTypeField(controller, "pendingRequest"));
        assertEquals(1, getIntField(controller, "requestingPlayer"));
    }

    // =========================================================
    // showSinglePlayerLoseOverlay() - 2 tests
    // =========================================================

    @Test
    void showSinglePlayerLoseOverlay_player1Lose_shouldSetResetRequestAndRequestingPlayerIsP1() throws Exception {
        TestablePvPGameController controller =
                createController(GameResultView.Action.QUIT_TO_MENU);

        runFxVoid(() -> controller.showSinglePlayerLoseOverlay(1));

        assertTrue(getBooleanField(controller, "waitingConfirmation"));
        assertEquals(PvPRequestType.RESET, getRequestTypeField(controller, "pendingRequest"));
        assertEquals(1, getIntField(controller, "requestingPlayer"));
    }

    @Test
    void showSinglePlayerLoseOverlay_player2Lose_shouldSetResetRequestAndRequestingPlayerIsP2() throws Exception {
        TestablePvPGameController controller =
                createController(GameResultView.Action.QUIT_TO_MENU);

        runFxVoid(() -> controller.showSinglePlayerLoseOverlay(2));

        assertTrue(getBooleanField(controller, "waitingConfirmation"));
        assertEquals(PvPRequestType.RESET, getRequestTypeField(controller, "pendingRequest"));
        assertEquals(2, getIntField(controller, "requestingPlayer"));
    }

    // =========================================================
    // finishMatch() - 2 tests
    // =========================================================

    @Test
    void finishMatch_whenUserChoosesRestart_shouldInitNewMatch() throws Exception {
        TestablePvPGameController controller =
                createController(GameResultView.Action.RESTART);

        Board oldBoardP1 = getBoardField(controller, "boardP1");

        setField(controller, "activePlayersCount", 1);
        setField(controller, "stateP1", GameState.WIN);
        setField(controller, "stateP2", GameState.LOSE);
        setField(controller, "p1Started", true);
        setField(controller, "p2Started", true);

        runFxVoid(() -> controller.finishMatch(1));

        Board newBoardP1 = getBoardField(controller, "boardP1");

        assertNotSame(oldBoardP1, newBoardP1);
        assertEquals(2, getIntField(controller, "activePlayersCount"));
        assertEquals(GameState.PVP_SPLIT_START, getGameStateField(controller, "stateP1"));
        assertEquals(GameState.PVP_SPLIT_START, getGameStateField(controller, "stateP2"));
        assertFalse(getBooleanField(controller, "p1Started"));
        assertFalse(getBooleanField(controller, "p2Started"));
    }

    @Test
    void finishMatch_whenUserChoosesQuitToMenu_shouldCallOnMatchEnd() throws Exception {
        TestablePvPGameController controller =
                createController(GameResultView.Action.QUIT_TO_MENU);

        AtomicBoolean callbackCalled = new AtomicBoolean(false);
        controller.setOnMatchEnd(() -> callbackCalled.set(true));

        runFxVoid(() -> controller.finishMatch(2));

        assertTrue(callbackCalled.get());
    }

    // =========================================================
    // Testable subclass
    // =========================================================

    private static class TestablePvPGameController extends PvPGameController {

        private final GameResultView.Action action;

        TestablePvPGameController(PvPSetupDialog.Config config,
                                  GameResultView.Action action) {
            super(config);
            this.action = action;
        }

        @Override
        protected GameResultView createGameResultView() {
            return new FakeGameResultView(action);
        }
    }

    private static class FakeGameResultView extends GameResultView {

        private final Action action;

        FakeGameResultView(Action action) {
            this.action = action;
        }

        @Override
        public Action showPvP(Stage ownerStage,
                              int winner,
                              String p1Name,
                              String p2Name,
                              int timeP1,
                              int timeP2,
                              int flagsP1,
                              int flagsP2) {
            return action;
        }
    }

    // =========================================================
    // Helpers
    // =========================================================

    private static TestablePvPGameController createController(
            GameResultView.Action action
    ) throws Exception {
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

            TestablePvPGameController controller =
                    new TestablePvPGameController(config, action);

            new Scene(controller.getPvPBoardView().getRoot());

            return controller;
        });
    }

    private static Field getDeclaredField(String fieldName) throws Exception {
        Field field = PvPGameController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field;
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = getDeclaredField(fieldName);
        field.set(target, value);
    }

    private static int getIntField(Object target, String fieldName) throws Exception {
        Field field = getDeclaredField(fieldName);
        return field.getInt(target);
    }

    private static boolean getBooleanField(Object target, String fieldName) throws Exception {
        Field field = getDeclaredField(fieldName);
        return field.getBoolean(target);
    }

    private static GameState getGameStateField(Object target, String fieldName) throws Exception {
        Field field = getDeclaredField(fieldName);
        return (GameState) field.get(target);
    }

    private static PvPRequestType getRequestTypeField(Object target, String fieldName) throws Exception {
        Field field = getDeclaredField(fieldName);
        return (PvPRequestType) field.get(target);
    }

    private static Board getBoardField(Object target, String fieldName) throws Exception {
        Field field = getDeclaredField(fieldName);
        return (Board) field.get(target);
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

    private static void runFxVoid(FxRunnable runnable) throws Exception {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
            return;
        }

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                runnable.run();
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
    }

    @FunctionalInterface
    private interface FxCallable<T> {
        T call() throws Exception;
    }

    @FunctionalInterface
    private interface FxRunnable {
        void run() throws Exception;
    }
}
