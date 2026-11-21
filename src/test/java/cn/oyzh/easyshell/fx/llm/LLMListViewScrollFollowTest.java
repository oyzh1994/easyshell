package cn.oyzh.easyshell.fx.llm;

import cn.oyzh.fx.plus.util.FXUtil;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Headful JavaFX regression for manual chat scrolling.
 */
public class LLMListViewScrollFollowTest {

    @Test
    public void upwardScrollStaysLatchedWhenContentHeightChanges() {
        startToolkit();
        AtomicReference<TestContext> contextRef = new AtomicReference<>();
        runFxAndWait(() -> {
            LLMListView listView = new LLMListView();
            for (int i = 0; i < 80; i++) {
                listView.addUserMessage("message " + i + "\nline 2\nline 3\nline 4\nline 5");
            }
            StackPane root = new StackPane(listView);
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 560, 360));
            stage.show();
            contextRef.set(new TestContext(stage, root, listView));
        });
        try {
            for (int i = 0; i < 3; i++) {
                waitForPulse();
            }
            runFxAndWait(() -> {
                TestContext context = contextRef.get();
                context.root().applyCss();
                context.root().layout();
                context.listView().applyCss();
                context.listView().layout();
                ScrollBar scrollBar = verticalScrollBar(context.listView());
                assertNotNull(scrollBar);
                assertTrue("chat content is not scrollable: max=" + scrollBar.getMax()
                                + ", visible=" + scrollBar.getVisibleAmount(),
                        scrollBar.getMax() > scrollBar.getVisibleAmount());
                scrollBar.setValue(scrollBar.getMax());

                Event.fireEvent(context.listView(), upwardScrollEvent());
                // 模拟流式内容增长期间滚动条短暂回到最大值。
                // 用户已经明确向上滚动，本次高度变化不能重新开启自动跟随。
                scrollBar.setValue(scrollBar.getMax());
            });
            waitForPulse();
            runFxAndWait(() -> assertFalse(
                    "height change re-enabled auto-follow after the user scrolled upward",
                    contextRef.get().listView().isAutoScrollEnabled()));

            runFxAndWait(() -> contextRef.get().listView()
                    .addAssistantMessage("new content\nline 2\nline 3\nline 4\nline 5"));
            waitForPulse();
            waitForPulse();

            runFxAndWait(() -> {
                assertFalse("chat unexpectedly followed the new content to the bottom",
                        contextRef.get().listView().isAutoScrollEnabled());
            });
        } finally {
            TestContext context = contextRef.get();
            if (context != null) {
                runFxAndWait(context.stage()::close);
            }
        }
    }

    @Test
    public void manualSendRestoresAutoFollowAfterUpwardScroll() {
        startToolkit();
        AtomicReference<ViewTestContext> contextRef = new AtomicReference<>();
        AtomicInteger sendCalls = new AtomicInteger();
        runFxAndWait(() -> {
            LLMView view = new LLMView();
            for (int i = 0; i < 80; i++) {
                view.addUserMessage("message " + i + "\nline 2\nline 3\nline 4\nline 5");
            }
            view.setSendHandler(message -> sendCalls.incrementAndGet());
            StackPane root = new StackPane(view);
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 720, 600));
            stage.show();
            contextRef.set(new ViewTestContext(stage, root, view));
        });
        try {
            for (int i = 0; i < 3; i++) {
                waitForPulse();
            }
            runFxAndWait(() -> {
                ViewTestContext context = contextRef.get();
                context.root().applyCss();
                context.root().layout();
                context.view().applyCss();
                context.view().layout();
                ScrollBar scrollBar = verticalScrollBar(context.view().getMessageListView());
                assertNotNull(scrollBar);
                scrollBar.setValue(scrollBar.getMax());
                Event.fireEvent(context.view().getMessageListView(), upwardScrollEvent());
                scrollBar.setValue(scrollBar.getMax());
            });
            waitForPulse();
            runFxAndWait(() -> {
                assertFalse(contextRef.get().view().getMessageListView()
                        .isAutoScrollEnabled());
                contextRef.get().view().getInputArea().setText("next message");
                contextRef.get().view().sendMessage();
                assertTrue("manual send did not restore auto-follow",
                        contextRef.get().view().getMessageListView()
                                .isAutoScrollEnabled());
            });
            waitForPulse();
            waitForPulse();
            runFxAndWait(() -> {
                assertEquals(1, sendCalls.get());
                assertTrue(contextRef.get().view().getMessageListView()
                        .isAutoScrollEnabled());
                ScrollBar scrollBar = verticalScrollBar(
                        contextRef.get().view().getMessageListView());
                assertNotNull(scrollBar);
                assertTrue("chat did not return to the bottom",
                        scrollBar.getValue() >= scrollBar.getMax() - 1);
            });
        } finally {
            ViewTestContext context = contextRef.get();
            if (context != null) {
                runFxAndWait(context.stage()::close);
            }
        }
    }

    private static ScrollBar verticalScrollBar(LLMListView listView) {
        ScrollBar scrollBar = null;
        if (listView.getSkin() instanceof LLMListViewSkin skin) {
            scrollBar = skin.getVerticalScrollBar();
        }
        if (scrollBar == null) {
            scrollBar = FXUtil.getVScrollBar(listView);
        }
        return scrollBar;
    }

    private static ScrollEvent upwardScrollEvent() {
        return new ScrollEvent(ScrollEvent.SCROLL, 0, 0, 0, 0,
                false, false, false, false, true, false,
                0, 40, 0, 40,
                ScrollEvent.HorizontalTextScrollUnits.NONE, 0,
                ScrollEvent.VerticalTextScrollUnits.NONE, 0, 1, null);
    }

    private static void startToolkit() {
        try {
            Platform.startup(() -> {
                Platform.setImplicitExit(false);
            });
        } catch (IllegalStateException ignored) {
            // toolkit is already running in the test JVM
        }
    }

    private static void runFxAndWait(Runnable task) {
        if (Platform.isFxApplicationThread()) {
            task.run();
            return;
        }
        AtomicReference<Throwable> failure = new AtomicReference<>();
        CountDownLatch completed = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                task.run();
            } catch (Throwable ex) {
                failure.set(ex);
            } finally {
                completed.countDown();
            }
        });
        try {
            assertTrue("JavaFX task timed out", completed.await(10, TimeUnit.SECONDS));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new AssertionError(ex);
        }
        if (failure.get() != null) {
            throw new RuntimeException(failure.get());
        }
    }

    private static void waitForPulse() {
        runFxAndWait(() -> {
        });
        try {
            Thread.sleep(50);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new AssertionError(ex);
        }
        runFxAndWait(() -> {
        });
    }

    private record TestContext(Stage stage, StackPane root, LLMListView listView) {
    }

    private record ViewTestContext(Stage stage, StackPane root, LLMView view) {
    }
}
