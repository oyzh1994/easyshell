package cn.oyzh.easyshell.fx.llm;

import cn.oyzh.easyshell.llm.LLMMessage;
import cn.oyzh.easyshell.util.ShellI18nHelper;
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

            LLMMessageView content = findStyledView(
                    context.listView(), 0, "llm-bubble-text-assistant");
            assertTrue(content.isManaged());
            assertTrue(content.isVisible());
            assertTrue(content.getText().contains("step thinking"));
            assertTrue(content.getText().contains("step result"));
            assertTrue(content.getText().contains("final answer"));
            assertFalse("agent trace must not use an expandable button",
                    context.listView().lookupAll(".llm-agent-trace-button").iterator().hasNext());

            VBox bubble = (VBox) content.getParent();
            assertSame("the message content must use one editor", content, bubble.getChildren().getFirst());
            assertFalse(context.listView().lookupAll(".llm-agent-trace-text").iterator().hasNext());

            runFxAndWait(() -> context.listView().setThinkingVisible(false));
            waitForPulse();
            LLMMessageView hiddenContent = findStyledView(
                    context.listView(), 0, "llm-bubble-text-assistant");
            assertFalse(hiddenContent.getText().contains("step thinking"));
            assertTrue(hiddenContent.getText().contains("step result"));
            assertFalse(hiddenContent.getText().contains("aggregate thinking"));

            runFxAndWait(() -> context.listView().setThinkingVisible(true));
            waitForPulse();
            assertTrue(findStyledView(context.listView(), 0, "llm-bubble-text-assistant")
                    .getText().contains("step thinking"));

            runFxAndWait(() -> message.setAgentTrace(message.getAgentTrace()
                    + "\n\n### streamed step\n\nstreamed result"));
            waitForPulse();
            assertTrue(findStyledView(context.listView(), 0, "llm-bubble-text-assistant")
                    .getText().contains("streamed result"));
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

    @Test
    public void longAgentTraceAndAnswerShareOneEditor() {
        startToolkit();
        AtomicReference<TestContext> contextRef = new AtomicReference<>();
        runFxAndWait(() -> {
            LLMListView listView = new LLMListView();
            LLMMessage message = LLMMessage.llm("final answer");
            message.setAgentTrace(longTrace());
            listView.addMessage(message);
            Stage stage = new Stage();
            stage.setScene(new Scene(new StackPane(listView), 560, 420));
            stage.show();
            contextRef.set(new TestContext(stage, null, listView));
        });
        try {
            for (int i = 0; i < 4; i++) {
                waitForPulse();
            }
            TestContext context = contextRef.get();
            LLMMessageView content = findStyledView(
                    context.listView(), 0, "llm-bubble-text-assistant");
            assertTrue(content.getText().contains("执行 Agent 40"));
            assertTrue(content.getText().contains("final answer"));
            VBox bubble = (VBox) content.getParent();
            assertSame(content, bubble.getChildren().getFirst());
            assertFalse(context.listView().lookupAll(".llm-agent-trace-text").iterator().hasNext());
            LLMMessage message = context.listView().getItems().getFirst();
            for (int i = 0; i < 12; i++) {
                int step = i;
                runFxAndWait(() -> {
                    message.setAgentTrace(longTrace() + "\n\n### 流式步骤 "
                            + step + "\n\n新的结果内容");
                    message.setMessage(message.getMessage() + "\n\n流式正文 " + step);
                });
                waitForPulse();
                LLMMessageView updated = findStyledView(
                        context.listView(), 0, "llm-bubble-text-assistant");
                assertTrue(updated.getText().contains("流式正文 " + step));
                assertTrue(updated.getText().contains("**" + ShellI18nHelper.answerLabel() + "**"));
            }
        } finally {
            TestContext context = contextRef.get();
            if (context != null) {
                runFxAndWait(context.stage()::close);
            }
        }
    }

    private static String longTrace() {
        StringBuilder trace = new StringBuilder();
        for (int i = 0; i < 40; i++) {
            trace.append("### 执行 Agent ").append(i + 1).append(" · 已完成\n\n")
                    .append("**思考过程**\n\n")
                    .append("分析第 ").append(i + 1)
                    .append(" 步，并核对当前环境、输入与预期输出。\n\n")
                    .append("**结果**\n\n")
                    .append("第 ").append(i + 1)
                    .append(" 步已完成，继续处理后续内容。\n\n");
        }
        return trace.toString();
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
