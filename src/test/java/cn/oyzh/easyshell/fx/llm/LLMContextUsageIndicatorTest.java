package cn.oyzh.easyshell.fx.llm;

import cn.oyzh.easyshell.llm.LLMModelContextWindowUtil;
import cn.oyzh.easyshell.llm.LLMTokenUsageStats;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LLMContextUsageIndicatorTest {

    @Test
    public void showsCircularRatioAndActualContextDetails() {
        startToolkit();
        AtomicReference<LLMContextUsageIndicator> indicatorRef = new AtomicReference<>();
        runFxAndWait(() -> {
            LLMContextUsageIndicator indicator = new LLMContextUsageIndicator();
            indicator.update(new LLMTokenUsageStats(
                            12_800, 1_200, 14_000,
                            25_600, 2_400, 28_000,
                            true, 4_000),
                    new LLMModelContextWindowUtil.ContextWindow(128_000, false));
            indicatorRef.set(indicator);
        });
        runFxAndWait(() -> {
            LLMContextUsageIndicator indicator = indicatorRef.get();
            assertEquals(0.1, indicator.ratio(), 0.001);
            String tooltip = indicator.tooltipText();
            assertTrue(tooltip.contains("10%"));
            assertTrue(tooltip.contains("12.8K"));
            assertTrue(tooltip.contains("128.0K"));
            assertTrue(tooltip.contains("14.0K"));
            assertTrue(tooltip.contains("28.0K"));
            assertTrue(tooltip.contains("00:04"));
        });
    }

    @Test
    public void reportsUnknownInsteadOfFabricatingCapacity() {
        startToolkit();
        AtomicReference<LLMContextUsageIndicator> indicatorRef = new AtomicReference<>();
        runFxAndWait(() -> {
            LLMContextUsageIndicator indicator = new LLMContextUsageIndicator();
            indicator.update(new LLMTokenUsageStats(
                            8_000, 1_000, 9_000,
                            8_000, 1_000, 9_000,
                            true, 2_000),
                    LLMModelContextWindowUtil.ContextWindow.UNKNOWN);
            indicatorRef.set(indicator);
        });
        runFxAndWait(() -> {
            LLMContextUsageIndicator indicator = indicatorRef.get();
            assertEquals(-1, indicator.ratio(), 0.001);
            assertTrue(indicator.tooltipText().contains("8.0K"));
            assertFalse(indicator.tooltipText().contains("%"));
        });
    }

    @Test
    public void compactToolbarKeepsContextRingAtFixedSize() throws Exception {
        startToolkit();
        AtomicReference<Stage> stageRef = new AtomicReference<>();
        AtomicReference<LLMView> viewRef = new AtomicReference<>();
        AtomicReference<Double> sessionWidth = new AtomicReference<>();
        AtomicReference<Double> modelWidth = new AtomicReference<>();
        Path attachment = Files.createTempFile(
                "llm-context-attachment-", "-a-very-long-diagnostic-log-name.txt");
        Files.writeString(attachment, "context attachment");
        runFxAndWait(() -> {
            LLMView view = new LLMView();
            Stage stage = new Stage();
            stage.setScene(new Scene(view, 520, 560));
            stage.show();
            view.applyCss();
            view.layout();
            stageRef.set(stage);
            viewRef.set(view);
        });
        try {
            runFxAndWait(() -> {
                LLMView view = viewRef.get();
                LLMContextUsageIndicator indicator = view.getContextUsageIndicator();
                assertTrue(indicator.getWidth() >= 28);
                assertTrue(indicator.getParent().getLayoutBounds().getWidth() >= 36);
                assertEquals(LLMModelContextWindowUtil.DEFAULT_TOKENS,
                        view.getModelCapability().getContextWindowTokens());
                assertTrue(view.getSessionTabComboBox().getWidth() >= 159);
                assertTrue(view.getModelComboBox().getWidth() >= 159);
                sessionWidth.set(view.getSessionTabComboBox().getWidth());
                modelWidth.set(view.getModelComboBox().getWidth());
                view.setSelectedFile(attachment.toFile());
            });
            runFxAndWait(() -> {
                LLMView view = viewRef.get();
                view.applyCss();
                view.layout();
                LLMContextUsageIndicator indicator = view.getContextUsageIndicator();
                assertTrue(indicator.isVisible());
                assertTrue(indicator.getWidth() >= 28);
                assertTrue(indicator.getParent().getLayoutBounds().getWidth() >= 36);
                assertTrue(view.getSessionTabComboBox().getWidth()
                        >= sessionWidth.get() - 0.5);
                assertTrue(view.getModelComboBox().getWidth()
                        >= modelWidth.get() - 0.5);
            });
        } finally {
            runFxAndWait(stageRef.get()::close);
            Files.deleteIfExists(attachment);
        }
    }

    @Test
    public void zeroUsageDoesNotRenderAsCompressedDot() {
        startToolkit();
        AtomicReference<LLMContextUsageIndicator> indicatorRef = new AtomicReference<>();
        runFxAndWait(() -> {
            LLMContextUsageIndicator indicator = new LLMContextUsageIndicator();
            indicator.update(new LLMTokenUsageStats(
                            0, 0, 0,
                            0, 0, 0,
                            true, 1_000),
                    new LLMModelContextWindowUtil.ContextWindow(
                            LLMModelContextWindowUtil.DEFAULT_TOKENS, false));
            indicatorRef.set(indicator);
        });
        runFxAndWait(() -> {
            LLMContextUsageIndicator indicator = indicatorRef.get();
            assertEquals(0, indicator.ratio(), 0.001);
            assertTrue("progress arc must be hidden below 1%", !indicator.isProgressVisible());
        });
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
}
