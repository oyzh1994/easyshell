package cn.oyzh.easyshell.fx.llm;

import cn.oyzh.easyshell.llm.LLMMessage;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Headful JavaFX regression for inline agent-trace rendering.
 */
public class LLMListViewAgentTraceInlineTest {

    @Test
    public void agentTraceStreamsInsideTheMessageBubble() {
        startToolkit();
        AtomicReference<TestContext> contextRef = new AtomicReference<>();
        runFxAndWait(() -> contextRef.set(createContext()));
        try {
            waitForPulse();
            TestContext context = contextRef.get();
            LLMMessage message = context.listView().getItems().getFirst();

            LLMMessageView trace = findTraceView(context.listView(), 0);
            assertTrue(trace.isManaged());
            assertTrue(trace.isVisible());
            assertTrue(trace.getText().contains("step thinking"));
            assertTrue(trace.getText().contains("step result"));
            assertFalse("agent trace must not use an expandable button",
                    context.listView().lookupAll(".llm-agent-trace-button").iterator().hasNext());

            VBox bubble = (VBox) trace.getParent();
            assertSame("agent trace must be rendered before the answer", trace, bubble.getChildren().getFirst());
            LLMMessageView answer = findStyledView(context.listView(), 0, "llm-bubble-text-assistant");
            assertFalse("agent thinking must not be duplicated in the answer",
                    answer.getText().contains("aggregate thinking"));

            runFxAndWait(() -> context.listView().setThinkingVisible(false));
            waitForPulse();
            LLMMessageView hiddenTrace = findTraceView(context.listView(), 0);
            assertFalse(hiddenTrace.getText().contains("step thinking"));
            assertTrue(hiddenTrace.getText().contains("step result"));
            LLMMessageView hiddenAnswer = findStyledView(
                    context.listView(), 0, "llm-bubble-text-assistant");
            assertFalse(hiddenAnswer.getText().contains("aggregate thinking"));

            runFxAndWait(() -> context.listView().setThinkingVisible(true));
            waitForPulse();
            assertTrue(findTraceView(context.listView(), 0).getText().contains("step thinking"));

            runFxAndWait(() -> message.setAgentTrace(message.getAgentTrace()
                    + "\n\n### streamed step\n\nstreamed result"));
            waitForPulse();
            assertTrue(findTraceView(context.listView(), 0).getText().contains("streamed result"));
        } finally {
            TestContext context = contextRef.get();
            if (context != null) {
                runFxAndWait(context.stage()::close);
            }
        }
    }

    @Test
    public void normalThinkingCanBeHiddenWithoutHidingAnswer() {
        startToolkit();
        AtomicReference<TestContext> contextRef = new AtomicReference<>();
        runFxAndWait(() -> {
            LLMListView listView = new LLMListView();
            LLMMessage message = LLMMessage.llm("visible answer");
            message.setThinking("private reasoning");
            listView.addMessage(message);
            Stage stage = new Stage();
            stage.setScene(new Scene(new StackPane(listView), 560, 320));
            stage.show();
            contextRef.set(new TestContext(stage, null, listView));
        });
        try {
            waitForPulse();
            TestContext context = contextRef.get();
            LLMMessageView answer = findStyledView(
                    context.listView(), 0, "llm-bubble-text-assistant");
            assertTrue(answer.getText().contains("private reasoning"));
            assertTrue(answer.getText().contains("visible answer"));

            runFxAndWait(() -> context.listView().setThinkingVisible(false));
            waitForPulse();
            LLMMessageView hidden = findStyledView(
                    context.listView(), 0, "llm-bubble-text-assistant");
            assertFalse(hidden.getText().contains("private reasoning"));
            assertTrue(hidden.getText().contains("visible answer"));

            runFxAndWait(() -> context.listView().setThinkingVisible(true));
            waitForPulse();
            assertTrue(findStyledView(context.listView(), 0, "llm-bubble-text-assistant")
                    .getText().contains("private reasoning"));
        } finally {
            TestContext context = contextRef.get();
            if (context != null) {
                runFxAndWait(context.stage()::close);
            }
        }
    }

    private static TestContext createContext() {
        LLMListView listView = new LLMListView();
        LLMMessage message = LLMMessage.llm("final answer");
        message.setThinking("aggregate thinking");
        message.setAgentTrace("""
                ### 执行 Agent 1 · 已完成

                **思考过程**

                step thinking

                **结果**

                step result
                """);
        listView.addMessage(message);
        StackPane root = new StackPane(listView);
        Stage stage = new Stage();
        stage.setScene(new Scene(root, 560, 420));
        stage.show();
        root.applyCss();
        root.layout();
        return new TestContext(stage, root, listView);
    }

    private static LLMMessageView findTraceView(LLMListView listView, int itemIndex) {
        return findStyledView(listView, itemIndex, "llm-agent-trace-text");
    }

    private static LLMMessageView findStyledView(LLMListView listView, int itemIndex, String styleClass) {
        return runFx(() -> {
            for (Node node : listView.lookupAll("." + styleClass)) {
                if (!(node instanceof LLMMessageView view)) {
                    continue;
                }
                Node parent = node;
                while (parent != null && !(parent instanceof ListCell<?>)) {
                    parent = parent.getParent();
                }
                if (parent instanceof ListCell<?> cell && cell.getIndex() == itemIndex) {
                    return view;
                }
            }
            throw new AssertionError(styleClass + " not rendered for item " + itemIndex);
        });
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

    private record TestContext(Stage stage, StackPane root, LLMListView listView) {
    }

    @FunctionalInterface
    private interface FxCallable<T> {

        T call() throws Exception;
    }
}
