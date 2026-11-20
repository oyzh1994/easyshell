package cn.oyzh.easyshell.fx.llm;

import cn.oyzh.easyshell.llm.LLMMessage;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Headful JavaFX regression for the anchored agent-trace popup.
 */
public class LLMListViewAgentTraceAnchorTest {

    @Test
    public void lastTracePopupDoesNotMoveTheMessageList() throws Exception {
        assertPopupAnchor(29, 373);
    }

    @Test
    public void secondLastTracePopupDoesNotMoveTheMessageList() throws Exception {
        assertPopupAnchor(28, 160);
    }

    private static void assertPopupAnchor(int itemIndex, double desiredY) {
        startToolkit();
        AtomicReference<TestContext> contextRef = new AtomicReference<>();
        runFxAndWait(() -> contextRef.set(createContext()));
        try {
            waitForPulse();
            waitForPulse();
            TestContext context = contextRef.get();
            int scrollIndex = itemIndex == context.listView().getItems().size() - 1
                    ? itemIndex
                    : itemIndex - 1;
            runFxAndWait(() -> context.listView().scrollTo(scrollIndex));
            waitForPulse();
            waitForPulse();

            Button button = findTraceButton(context.listView(), itemIndex);
            double currentY = runFx(() -> button.localToScene(0, 0).getY());
            if (desiredY >= 0) {
                runFxAndWait(() -> {
                    Node current = button;
                    while (current != null && !(current.getParent() instanceof ListCell<?>)) {
                        current = current.getParent();
                    }
                    if (current != null && current.getParent() instanceof ListCell<?> cell) {
                        context.listView().scrollTo(itemIndex);
                    }
                });
            }
            currentY = runFx(() -> button.localToScene(0, 0).getY());
            double beforeY = currentY;
            double beforeCellHeight = cellHeight(button);

            runFxAndWait(button::fire);
            waitForPulse();
            waitForPulse();
            double shownY = runFx(() -> button.localToScene(0, 0).getY());
            double shownCellHeight = cellHeight(button);

            runFxAndWait(button::fire);
            waitForPulse();
            waitForPulse();
            double hiddenY = runFx(() -> button.localToScene(0, 0).getY());
            double hiddenCellHeight = cellHeight(button);

            System.out.printf("agent trace popup item=%d: before=%.2f shown=%.2f hidden=%.2f "
                            + "cellHeight=%.2f/%.2f/%.2f%n",
                    itemIndex, beforeY, shownY, hiddenY,
                    beforeCellHeight, shownCellHeight, hiddenCellHeight);
            assertEquals("message moved when popup opened", beforeY, shownY, 1.0);
            assertEquals("message moved when popup closed", shownY, hiddenY, 1.0);
            assertEquals("message cell changed height", beforeCellHeight, shownCellHeight, 1.0);
            assertEquals("message cell changed height after closing",
                    shownCellHeight, hiddenCellHeight, 1.0);
        } finally {
            TestContext context = contextRef.get();
            if (context != null) {
                runFxAndWait(context.stage()::close);
            }
        }
    }

    private static double cellHeight(Button button) {
        return runFx(() -> {
            Node current = button;
            while (current != null && !(current instanceof ListCell<?>)) {
                current = current.getParent();
            }
            return current == null ? -1 : ((ListCell<?>) current).getHeight();
        });
    }

    private static TestContext createContext() {
        LLMListView listView = new LLMListView();
        for (int index = 0; index < 30; index++) {
            LLMMessage message = LLMMessage.llm("message " + index);
            message.setAgentTrace(("thinking " + index + "\n").repeat(20)
                    + "\nanswer " + index);
            listView.addAssistantMessage(("message " + index + "\n").repeat(20));
            listView.getItems().getLast().setAgentTrace(message.getAgentTrace());
        }
        StackPane root = new StackPane(listView);
        Stage stage = new Stage();
        stage.setScene(new Scene(root, 560, 420));
        stage.show();
        root.applyCss();
        root.layout();
        return new TestContext(stage, root, listView);
    }

    private static void startToolkit() {
        try {
            Platform.startup(() -> {
            });
        } catch (IllegalStateException ignored) {
            // toolkit is already running in the test JVM
        }
    }

    private static void runFxAndWait(Runnable task) {
        runFx(() -> {
            task.run();
            return null;
        });
    }

    private static <T> T runFx(FxCallable<T> task) {
        if (Platform.isFxApplicationThread()) {
            try {
                return task.call();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        AtomicReference<T> result = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        CountDownLatch completed = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                result.set(task.call());
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
        return result.get();
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

    private static Button findTraceButton(LLMListView listView, int itemIndex) {
        return runFx(() -> {
            for (Node node : listView.lookupAll(".llm-agent-trace-button")) {
                if (!(node instanceof Button button)) {
                    continue;
                }
                Node parent = node;
                while (parent != null && !(parent instanceof ListCell<?>)) {
                    parent = parent.getParent();
                }
                if (parent instanceof ListCell<?> cell && cell.getIndex() == itemIndex) {
                    return button;
                }
            }
            throw new AssertionError("agent trace button not rendered for item " + itemIndex);
        });
    }

    private record TestContext(Stage stage, StackPane root, LLMListView listView) {
    }

    @FunctionalInterface
    private interface FxCallable<T> {

        T call() throws Exception;
    }
}
