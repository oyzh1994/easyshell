package cn.oyzh.easyshell.fx.llm;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LLMModelTextFieldTest {

    @Test
    public void fetchedModelListAndDefaultModelRemainSeparate() {
        startToolkit();
        AtomicReference<TestContext> contextRef = new AtomicReference<>();
        runFxAndWait(() -> {
            LLMModelTextField field = new LLMModelTextField();
            Stage stage = new Stage();
            stage.setScene(new Scene(field, 420, 60));
            stage.show();
            field.applyCss();
            field.requestFocus();
            field.setText("default-model");
            field.setModels(List.of("default-model", "model-b", "model-c"));
            contextRef.set(new TestContext(stage, field));
        });
        try {
            Thread.sleep(100);
            runFxAndWait(() -> {
                TestContext context = contextRef.get();
                assertEquals("default-model", context.field().getTextTrim());
                assertEquals(List.of("default-model", "model-b", "model-c"),
                        context.field().getModels());
                assertEquals("default-model", context.field().getEffectiveDefaultModel());
                assertEquals(3, context.field().getItemSize());

                context.field().setModels(context.field().getModels(), "model-b");
                assertEquals("model-b", context.field().getTextTrim());
                assertEquals("model-b", context.field().getEffectiveDefaultModel());

                context.field().setModels(context.field().getModels(), null);
                assertEquals("default-model", context.field().getTextTrim());
                assertEquals("default-model", context.field().getEffectiveDefaultModel());
            });
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new AssertionError(ex);
        } finally {
            TestContext context = contextRef.get();
            if (context != null) {
                runFxAndWait(context.stage()::close);
            }
        }
    }

    private static void startToolkit() {
        try {
            Platform.startup(() -> {
                Platform.setImplicitExit(false);
            });
        } catch (IllegalStateException ignored) {
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

    private record TestContext(Stage stage, LLMModelTextField field) {
    }
}
