package cn.oyzh.easyshell.ui.ssh;

import cn.oyzh.easyshell.fx.ssh.ShellServerMonitorPane;
import cn.oyzh.easyshell.ssh.server.ShellServerMonitor;
import cn.oyzh.easyshell.tabs.ssh.ShellSSHEffTabController;
import cn.oyzh.fx.plus.ext.FXMLResult;
import cn.oyzh.fx.plus.ext.FXMLLoaderExt;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.font.FontUtil;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * SSH 效率模式页面的 FXML 装载回归测试。
 *
 * @author oyzh
 * @since 2026-11-13
 */
public class ShellSSHEffTabFxmlTest {

    @BeforeClass
    public static void startJavaFx() throws Exception {
        CountDownLatch startup = new CountDownLatch(1);
        try {
            Platform.startup(startup::countDown);
        } catch (IllegalStateException ex) {
            startup.countDown();
        }
        assertTrue("JavaFX toolkit startup timed out", startup.await(20, TimeUnit.SECONDS));
    }

    @Test
    public void monitorPaneLoadsAndInjectsController() throws Throwable {
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                FXMLResult result = FXMLLoaderExt.loadFromUrl("/tabs/ssh/shellSSHEffTab.fxml");
                assertNotNull(result.getNode());
                ShellSSHEffTabController controller =
                        (ShellSSHEffTabController) result.getController();
                assertNotNull(controller);
                Field field = ShellSSHEffTabController.class.getDeclaredField("serverMonitorPane");
                field.setAccessible(true);
                assertTrue(field.get(controller) instanceof ShellServerMonitorPane);
            } catch (Throwable ex) {
                error.set(ex);
            } finally {
                done.countDown();
            }
        });
        assertTrue("SSH efficiency FXML load timed out", done.await(20, TimeUnit.SECONDS));
        if (error.get() != null) {
            throw new AssertionError("SSH efficiency FXML load failed", error.get());
        }
    }

    @Test
    public void monitorCardsStayEqualWhenWidthChanges() throws Throwable {
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                ShellServerMonitorPane pane = new ShellServerMonitorPane();
                pane.showMetrics();
                assertEqualCardWidths(pane, 800);
                assertEqualCardWidths(pane, 500);
            } catch (Throwable ex) {
                error.set(ex);
            } finally {
                done.countDown();
            }
        });
        assertTrue("Monitor card resize test timed out", done.await(20, TimeUnit.SECONDS));
        if (error.get() != null) {
            throw new AssertionError("Monitor card resize test failed", error.get());
        }
    }

    @Test
    public void monitorPaneFillsRemainingRowWidth() throws Throwable {
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                ShellServerMonitorPane pane = new ShellServerMonitorPane();
                pane.showMetrics();
                HBox row = new HBox(10, new Label("monitor"), pane);
                row.resize(800, 30);
                row.applyCss();
                row.layout();
                assertTrue("monitor pane did not fill remaining row width: "
                        + pane.getLayoutBounds().getWidth(), pane.getLayoutBounds().getWidth() > 700);
                assertEqualCards(pane, 800);
            } catch (Throwable ex) {
                error.set(ex);
            } finally {
                done.countDown();
            }
        });
        assertTrue("Monitor pane parent layout test timed out", done.await(20, TimeUnit.SECONDS));
        if (error.get() != null) {
            throw new AssertionError("Monitor pane parent layout test failed", error.get());
        }
    }

    @Test
    public void metricValuesFitAcrossFontFamilies() throws Throwable {
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                List<String> families = List.of("Arial", "Verdana", "Menlo", "PingFang SC");
                for (String family : families) {
                    for (int size = 10; size <= 16; size += 3) {
                        assertMetricValuesFit(family, size);
                    }
                }
            } catch (Throwable ex) {
                error.set(ex);
            } finally {
                done.countDown();
            }
        });
        assertTrue("Monitor font layout test timed out", done.await(20, TimeUnit.SECONDS));
        if (error.get() != null) {
            throw new AssertionError("Monitor font layout test failed", error.get());
        }
    }

    @Test
    public void gaugeRowsStayAligned() throws Throwable {
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                ShellServerMonitorPane pane = new ShellServerMonitorPane();
                pane.showMetrics();
                Scene scene = new Scene(pane, 1200, 30);
                scene.getStylesheets().add(
                        ShellSSHEffTabFxmlTest.class.getResource("/fx-plus/css/theme/intellij-light.css").toExternalForm());
                scene.getStylesheets().add(
                        ShellSSHEffTabFxmlTest.class.getResource("/fx-plus/css/fx-base.css").toExternalForm());
                pane.applyCss();
                pane.layout();

                List<Node> gaugeRows = nodesWithStyle(pane, "server-metric-gauge-row");
                assertTrue(gaugeRows.size() == 4);
                double expected = gaugeRows.getFirst().getLayoutBounds().getWidth();
                assertTrue(expected > 0);
                for (Node gaugeRow : gaugeRows) {
                    assertTrue("gauge row widths differ: "
                                    + gaugeRows.stream().map(node -> node.getLayoutBounds().getWidth()).toList(),
                            Math.abs(expected - gaugeRow.getLayoutBounds().getWidth()) <= 1.01);
                }
            } catch (Throwable ex) {
                error.set(ex);
            } finally {
                done.countDown();
            }
        });
        assertTrue("Monitor gauge alignment test timed out", done.await(20, TimeUnit.SECONDS));
        if (error.get() != null) {
            throw new AssertionError("Monitor gauge alignment test failed", error.get());
        }
    }

    private static void assertMetricValuesFit(String family, int size) {
        ShellServerMonitorPane pane = new ShellServerMonitorPane();
        pane.showMetrics();
        ShellServerMonitor monitor = new ShellServerMonitor();
        monitor.setCpuUsage(0);
        monitor.setMemoryUsage(84.99);
        monitor.setNetworkReceiveSpeed(999.99);
        monitor.setNetworkSendSpeed(99.99);
        monitor.setDiskReadSpeed(12.34);
        monitor.setDiskWriteSpeed(56.78);
        pane.update(monitor);
        Scene scene = new Scene(pane, 1200, 30);
        scene.getStylesheets().add(
                ShellSSHEffTabFxmlTest.class.getResource("/fx-plus/css/theme/intellij-light.css").toExternalForm());
        scene.getStylesheets().add(
                ShellSSHEffTabFxmlTest.class.getResource("/fx-plus/css/fx-base.css").toExternalForm());
        applyFont(pane, Font.font(family, size));
        pane.applyCss();
        pane.layout();
        for (Label label : labels(pane)) {
            if (!label.getStyleClass().contains("server-metric-value")) {
                continue;
            }
            double available = label.getWidth() - label.getInsets().getLeft() - label.getInsets().getRight();
            double textWidth = FontUtil.textWidth(label.getText(), label.getFont());
            assertTrue("metric value clipped with font " + family + " " + size + "px: text="
                            + textWidth + ", available=" + available + ", value=" + label.getText(),
                    textWidth <= available + 0.5);
        }
    }

    private static void applyFont(Node node, Font font) {
        if (node instanceof FontAdapter adapter) {
            adapter.changeFont(font);
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                applyFont(child, font);
            }
        }
    }

    private static List<Label> labels(Node node) {
        List<Label> labels = new java.util.ArrayList<>();
        collectLabels(node, labels);
        return labels;
    }

    private static List<Node> nodesWithStyle(Node node, String styleClass) {
        List<Node> nodes = new java.util.ArrayList<>();
        collectNodesWithStyle(node, styleClass, nodes);
        return nodes;
    }

    private static void collectNodesWithStyle(Node node, String styleClass, List<Node> nodes) {
        if (node.getStyleClass().contains(styleClass)) {
            nodes.add(node);
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                collectNodesWithStyle(child, styleClass, nodes);
            }
        }
    }

    private static void collectLabels(Node node, List<Label> labels) {
        if (node instanceof Label label) {
            labels.add(label);
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                collectLabels(child, labels);
            }
        }
    }

    private static void assertEqualCardWidths(ShellServerMonitorPane pane, double width) {
        pane.resize(width, 30);
        pane.applyCss();
        pane.layout();
        assertEqualCards(pane, width);
    }

    private static void assertEqualCards(ShellServerMonitorPane pane, double width) {
        List<Node> cards = pane.getChildrenUnmodifiable().stream().toList();
        assertTrue(cards.size() == 4);
        double expected = cards.getFirst().getLayoutBounds().getWidth();
        assertTrue(expected > 0);
        for (Node card : cards) {
            double actual = card.getLayoutBounds().getWidth();
            assertTrue("card widths differ at pane width " + width + ": "
                            + cards.stream().map(node -> node.getLayoutBounds().getWidth()).toList(),
                    Math.abs(expected - actual) <= 1.01);
        }
    }
}
