package cn.oyzh.easyshell.fx.llm;

import cn.oyzh.easyshell.llm.LLMMessage;
import cn.oyzh.easyshell.llm.agent.LLMAgentOrchestrationMode;
import cn.oyzh.easyshell.llm.agent.LLMAgentTrace;
import cn.oyzh.easyshell.llm.agent.LLMAgentTraceFormatter;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.i18n.I18nResourceBundle;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
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
    public void finishedThinkingCollapsesAndCanBeExpandedInline() {
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
            LLMListView listView = contextRef.get().listView();
            LLMMessage message = listView.getItems().getFirst();
            runFxAndWait(message::finishThinking);
            waitForPulse();

            LLMMessageView collapsed = findStyledView(
                    listView, 0, "llm-bubble-text-assistant");
            assertFalse(collapsed.getText().contains("private reasoning"));
            assertTrue(collapsed.getText().contains("visible answer"));
            assertTrue(collapsed.getFolds().getFirst().collapsed());
            double collapsedHeight = collapsed.getHeight();
            HBox header = runFx(() -> (HBox) collapsed.lookup(".editor-fold-header"));
            assertTrue("fold header not found, folds=" + collapsed.getFolds()
                    + ", text=" + collapsed.getText()
                    + ", content=" + runFx(() -> ((cn.oyzh.fx.editor.incubator.EditorSkin)
                    collapsed.getSkin()).getVFlow().getCell(0).getContent().getClass()), header != null);
            Label title = (Label) header.lookup(".editor-fold-title");
            assertTrue(title != null);
            assertTrue(ShellI18nHelper.thinkingLabel().equals(title.getText()));

            runFxAndWait(() -> Event.fireEvent(header, new MouseEvent(
                    MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0,
                    MouseButton.PRIMARY, 1,
                    false, false, false, false, true, false, false,
                    true, false, true, null)));
            assertFalse("fold header handler did not toggle the fold",
                    collapsed.getFolds().getFirst().collapsed());
            for (int i = 0; i < 4; i++) {
                waitForPulse();
            }

            LLMMessageView expanded = findStyledView(
                    listView, 0, "llm-bubble-text-assistant");
            assertSame("fold toggle recreated the message editor", collapsed, expanded);
            assertTrue("expanded fold did not render thinking, folds=" + expanded.getFolds()
                    + ", text=" + expanded.getText(),
                    expanded.getText().contains("private reasoning"));
            assertTrue(expanded.getText().contains("visible answer"));
            assertFalse(expanded.getText().contains("**回答**"));
            assertFalse(expanded.getFolds().getFirst().collapsed());
            assertTrue("editor height did not expand: collapsed=" + collapsedHeight
                            + ", expanded=" + expanded.getHeight()
                            + ", pref=" + expanded.prefHeight(expanded.getWidth()),
                    expanded.getHeight() > collapsedHeight);
        } finally {
            TestContext context = contextRef.get();
            if (context != null) {
                runFxAndWait(context.stage()::close);
            }
        }
    }

    @Test
    public void finishedAgentThinkingCollapsesWhileResultsRemainVisible() {
        startToolkit();
        AtomicReference<TestContext> contextRef = new AtomicReference<>();
        runFxAndWait(() -> contextRef.set(createContext()));
        try {
            waitForPulse();
            TestContext context = contextRef.get();
            LLMMessage message = context.listView().getItems().getFirst();
            runFxAndWait(message::finishThinking);
            waitForPulse();

            LLMMessageView content = findStyledView(
                    context.listView(), 0, "llm-bubble-text-assistant");
            assertFalse(content.getText().contains("step thinking"));
            assertTrue(content.getText().contains("step result"));
            assertTrue(content.getText().contains("final answer"));
            assertFalse(content.getText().contains("**结果**"));
            assertTrue(content.getFolds().getFirst().collapsed());
            assertTrue("fold header not found, text=" + content.getText(),
                    runFx(() -> content.lookup(".editor-fold-header") != null));
            assertTrue(runFx(() -> content.lookupAll(".editor-block-heading").stream()
                    .anyMatch(node -> node instanceof Label label
                            && I18nResourceBundle.i18nString("shell.llm.agent.trace.result")
                            .equals(label.getText()))));
        } finally {
            TestContext context = contextRef.get();
            if (context != null) {
                runFxAndWait(context.stage()::close);
            }
        }
    }

    @Test
    public void localCommandTraceIsRenderedAsCollapsedSection() {
        startToolkit();
        AtomicReference<TestContext> contextRef = new AtomicReference<>();
        runFxAndWait(() -> {
            LLMListView listView = new LLMListView();
            LLMMessage message = LLMMessage.llm("command answer");
            long now = System.currentTimeMillis();
            LLMAgentTrace command = new LLMAgentTrace(
                    "tool-1",
                    LLMAgentTrace.Type.TOOL,
                    0, 0,
                    "local_execute_command_direct",
                    "mvn -q -DskipTests compile",
                    "/tmp/workspace",
                    "exitCode: 0\nSTDOUT:\ncompiled",
                    "",
                    LLMAgentTrace.Status.COMPLETED,
                    now, now + 1000,
                    LLMAgentOrchestrationMode.SEQUENTIAL);
            message.setAgentTrace(LLMAgentTraceFormatter.format(java.util.List.of(command)));
            message.finishThinking();
            listView.addMessage(message);
            Stage stage = new Stage();
            stage.setScene(new Scene(new StackPane(listView), 560, 320));
            stage.show();
            contextRef.set(new TestContext(stage, null, listView));
        });
        try {
            waitForPulse();
            LLMMessage message = contextRef.get().listView().getItems().getFirst();
            LLMMessageView content = findStyledView(
                    contextRef.get().listView(), 0, "llm-bubble-text-assistant");

            assertTrue(message.getAgentTrace().contains("mvn -q -DskipTests compile"));
            assertTrue(content.getText().contains("command answer"));
            assertFalse(content.getFolds().isEmpty());
            assertTrue(content.getFolds().getFirst().collapsed());
            assertTrue(content.getFolds().getFirst().title()
                    .contains(I18nResourceBundle.i18nString("shell.llm.agent.trace.command")));
            assertTrue(runFx(() -> content.lookup(".editor-fold-header") != null));
            Circle indicator = runFx(() -> (Circle) content.lookup(".editor-fold-indicator"));
            assertNotNull("command status indicator was not rendered", indicator);
            assertNotNull(indicator.getFill());
            HBox header = runFx(() -> (HBox) content.lookup(".editor-fold-header"));
            assertSame("status indicator must be the last header node",
                    indicator, header.getChildren().getLast());
        } finally {
            TestContext context = contextRef.get();
            if (context != null) {
                runFxAndWait(context.stage()::close);
            }
        }
    }

    @Test
    public void partiallySuccessfulCommandGroupUsesYellowIndicator() {
        startToolkit();
        AtomicReference<TestContext> contextRef = new AtomicReference<>();
        runFxAndWait(() -> {
            LLMListView listView = new LLMListView();
            LLMMessage message = LLMMessage.llm("partial answer");
            long now = System.currentTimeMillis();
            LLMAgentTrace success = new LLMAgentTrace(
                    "tool-1", LLMAgentTrace.Type.TOOL, 0, 0,
                    "local_execute_command_direct", "mvn -q -DskipTests compile",
                    "", "", "", LLMAgentTrace.Status.COMPLETED,
                    now, now + 1000, LLMAgentOrchestrationMode.SEQUENTIAL);
            LLMAgentTrace failure = new LLMAgentTrace(
                    "tool-2", LLMAgentTrace.Type.TOOL, 0, 0,
                    "local_execute_command_direct", "git diff --check",
                    "", "", "", LLMAgentTrace.Status.FAILED,
                    now + 1000, now + 1500, LLMAgentOrchestrationMode.SEQUENTIAL);
            message.setAgentTrace(LLMAgentTraceFormatter.format(
                    java.util.List.of(success, failure)));
            message.finishThinking();
            listView.addMessage(message);
            Stage stage = new Stage();
            stage.setScene(new Scene(new StackPane(listView), 560, 320));
            stage.show();
            contextRef.set(new TestContext(stage, null, listView));
        });
        try {
            waitForPulse();
            LLMMessageView content = findStyledView(
                    contextRef.get().listView(), 0, "llm-bubble-text-assistant");
            Circle indicator = runFx(() -> (Circle) content.lookup(".editor-fold-indicator"));
            assertNotNull(indicator);
            Color fill = (Color) indicator.getFill();
            assertTrue(fill.equals(Color.web("#9A6700")) || fill.equals(Color.web("#D29922")));
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
                Platform.setImplicitExit(false);
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
