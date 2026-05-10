package com.minesweeper.controller;

import com.minesweeper.view.BoardView;
import com.minesweeper.view.HeaderView;
import com.minesweeper.view.MainView;
import com.minesweeper.view.MenuView;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GameControllerTest {

    @Mock private MainView   mockMainView;
    @Mock private HeaderView mockHeaderView;
    @Mock private MenuView   mockMenuView;
    @Mock private BoardView  mockBoardView;

    private GameController gameController;

    @BeforeAll
    public static void setupJavaFX() {
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "true");
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.text", "t2k");
        System.setProperty("java.awt.headless", "true");
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // toolkit already started
        }
    }

    @BeforeEach
    public void setUp() throws InterruptedException {
        when(mockMainView.getHeaderView()).thenReturn(mockHeaderView);
        when(mockMainView.getMenuView()).thenReturn(mockMenuView);
        when(mockMainView.getBoardView()).thenReturn(mockBoardView);

        // Create Scene/Stage and initialize controller on the JavaFX thread to avoid FX thread errors
        runOnFxThread(() -> {
            javafx.scene.Group root = new javafx.scene.Group();
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setScene(scene);
            lenient().when(mockMainView.getScene()).thenReturn(scene);

            gameController = new GameController(mockMainView);
            // Ensure a board exists before tests interact with controller
            gameController.newGame();
        });
    }

    private void runOnFxThread(Runnable action) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                action.run();
            } finally {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    // ── UC-08: Tạm dừng game ─────────────────────────────────

    @Test
    public void testPause_WhenPlaying_ShouldDisableView() throws InterruptedException {
        runOnFxThread(() -> {
            gameController.onLeftClick(0, 0);
            clearInvocations(mockMainView);

            gameController.pause();

            verify(mockMainView).setDisabled(true);
        });
    }

    @Test
    public void testPause_WhenIdle_ShouldDoNothing() throws InterruptedException {
        runOnFxThread(() -> {
            gameController.pause();

            verify(mockMainView, never()).setDisabled(true);
        });
    }

    // ── UC-09: Tiếp tục game ─────────────────────────────────

    @Test
    public void testResume_WhenPaused_ShouldEnableView() throws InterruptedException {
        runOnFxThread(() -> {
            gameController.onLeftClick(0, 0);
            gameController.pause();
            clearInvocations(mockMainView);

            gameController.resume();

            verify(mockMainView).setDisabled(false);
        });
    }

    @Test
    public void testResume_WhenNotPaused_ShouldDoNothing() throws InterruptedException {
        runOnFxThread(() -> {
            clearInvocations(mockMainView);
            gameController.resume();

            verify(mockMainView, never()).setDisabled(false);
        });
    }

    // ── UC-14: Reset bằng phím F2 ────────────────────────────

    @Test
    public void testOnKeyPressed_F2_ShouldCallReset() throws InterruptedException {
        runOnFxThread(() -> {
            clearInvocations(mockMainView, mockHeaderView, mockBoardView);

            KeyEvent f2 = new KeyEvent(
                    KeyEvent.KEY_PRESSED, "", "", KeyCode.F2,
                    false, false, false, false
            );
            gameController.onKeyPressed(f2);

            verify(mockBoardView, atLeastOnce()).build(anyInt(), anyInt());
        });
    }

    @Test
    public void testOnKeyPressed_OtherKey_ShouldNotReset() throws InterruptedException {
        runOnFxThread(() -> {
            clearInvocations(mockBoardView);

            KeyEvent spaceKey = new KeyEvent(
                    KeyEvent.KEY_PRESSED, "", "", KeyCode.SPACE,
                    false, false, false, false
            );
            gameController.onKeyPressed(spaceKey);

            verify(mockBoardView, never()).build(anyInt(), anyInt());
        });
    }

    // ── UC-07: Đổi độ khó ────────────────────────────────────

    @Test
    public void testSetDifficulty_Medium_ShouldStartNewGame() throws InterruptedException {
        runOnFxThread(() -> {
            clearInvocations(mockBoardView);

            gameController.setDifficulty(Difficulty.MEDIUM);

            verify(mockBoardView, atLeastOnce()).build(anyInt(), anyInt());
        });
    }

    @Test
    public void testSetDifficulty_Hard_ShouldStartNewGame() throws InterruptedException {
        runOnFxThread(() -> {
            clearInvocations(mockBoardView);

            gameController.setDifficulty(Difficulty.HARD);

            verify(mockBoardView, atLeastOnce()).build(anyInt(), anyInt());
        });
    }

    // ── UC-10: Right click đặt cờ ────────────────────────────

    @Test
    public void testOnRightClick_WhenPlaying_ShouldUpdateCell() throws InterruptedException {
        runOnFxThread(() -> {
            gameController.onLeftClick(0, 0);
            clearInvocations(mockBoardView);

            gameController.onRightClick(1, 1);

            verify(mockBoardView).updateCell(eq(1), eq(1), any());
        });
    }

    @Test
    public void testOnRightClick_WhenIdle_ShouldDoNothing() throws InterruptedException {
        runOnFxThread(() -> {
            clearInvocations(mockBoardView);

            gameController.onRightClick(1, 1);

            verify(mockBoardView, never()).updateCell(anyInt(), anyInt(), any());
        });
    }
}