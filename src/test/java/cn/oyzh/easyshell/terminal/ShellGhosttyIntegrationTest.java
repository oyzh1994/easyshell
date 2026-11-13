package cn.oyzh.easyshell.terminal;

import cn.oyzh.common.system.OSUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.local.ShellLocalClient;
import cn.oyzh.easyshell.local.ShellLocalTermWidget;
import cn.oyzh.easyshell.fx.term.ShellTermEngineComboBox;
import cn.oyzh.fx.tty.TtyTermWidget;
import cn.oyzh.fx.tty.TtyTerminalEngine;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.ui.settings.FXDefaultSettingsProvider;
import javafx.application.Platform;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * easyshell 本地终端接入 Ghostty 引擎的集成测试。
 *
 * <p>验证既有 {@code ShellProcessTermWidget} + pty4j 连接器无需改动即可被 Ghostty 使用，
 * 并验证 PTY 输出能够经过 {@code TtyConnectorTerminal} 进入 libghostty-vt。</p>
 */
public class ShellGhosttyIntegrationTest {

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
    public void terminalEngineFollowsGlobalDefault() {
        var setting = new ShellSetting();
        assertEquals(TtyTerminalEngine.getDefaultEngine().id(), setting.getTerminalEngine());
        setting.setTerminalEngine("jediterm");
        assertEquals(TtyTerminalEngine.JEDITERM, TtyTerminalEngine.of(setting.getTerminalEngine()));
        setting.setTerminalEngine("ghostty");
        assertEquals(TtyTerminalEngine.GHOSTTY, TtyTerminalEngine.of(setting.getTerminalEngine()));
    }

    @Test
    public void engineComboMapsBothEngines() throws Exception {
        var combo = callOnFxThread(ShellTermEngineComboBox::new);
        assertEquals(TtyTerminalEngine.JEDITERM, callOnFxThread(combo::getEngine));
        callOnFxThread(() -> {
            combo.selectEngine(TtyTerminalEngine.GHOSTTY);
            return null;
        });
        assertEquals(TtyTerminalEngine.GHOSTTY, callOnFxThread(combo::getEngine));
    }

    @Test
    public void settingsSelectEngineForNewWidgets() throws Exception {
        TtyTerminalEngine previous = TtyTermWidget.getDefaultEngine();
        var widget = callOnFxThread(TestWidget::new);
        try {
            var setting = new ShellSetting();
            setting.setTerminalEngine("ghostty");
            callOnFxThread(() -> {
                ShellTerminalUtil.applySetting(widget, setting);
                return null;
            });
            assertEquals(TtyTerminalEngine.GHOSTTY, widget.getEngine());
            assertEquals(TtyTerminalEngine.GHOSTTY, TtyTermWidget.getDefaultEngine());
        } finally {
            TtyTermWidget.setDefaultEngine(previous);
        }
    }

    @Test
    public void localShellRunsOnGhostty() throws Exception {
        ShellProcessTermWidget widget = callOnFxThread(ShellProcessTermWidget::new);
        try {
            var connector = callOnFxThread(() -> {
                widget.setEngine(TtyTerminalEngine.GHOSTTY);
                var created = widget.createTtyConnector();
                widget.openSession(created);
                return created;
            });
            assertTrue("Ghostty engine should be active", callOnFxThread(widget::isGhosttyActive));
            assertNotNull(callOnFxThread(widget::getGhosttyView));
            assertEquals(connector, callOnFxThread(widget::getTtyConnector));
            assertNotNull(callOnFxThread(widget::getTermSize));

            if (!OSUtil.isWindows()) {
                // 让 shell 输出 OSC 2 标题，验证 PTY -> TtyConnector -> Ghostty 的完整链路
                callOnFxThread(() -> {
                    widget.getTtyConnector().write("printf '\\033]2;easyshell-ghostty\\007'\r");
                    return null;
                });
                long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
                String title = null;
                while (System.nanoTime() < deadline) {
                    title = callOnFxThread(() -> widget.getGhosttyView().getTitle());
                    if ("easyshell-ghostty".equals(title)) {
                        break;
                    }
                    Thread.sleep(100);
                }
                assertEquals("easyshell-ghostty", title);

                // 验证 Ghostty 键盘输入 -> TtyConnector 的写入链路
                callOnFxThread(() -> {
                    widget.getGhosttyView().sendText("printf '\\033]2;typed-ghostty\\007'\r");
                    return null;
                });
                deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
                title = null;
                while (System.nanoTime() < deadline) {
                    title = callOnFxThread(() -> widget.getGhosttyView().getTitle());
                    if ("typed-ghostty".equals(title)) {
                        break;
                    }
                    Thread.sleep(100);
                }
                assertEquals("typed-ghostty", title);
            }
        } finally {
            callOnFxThread(() -> {
                widget.close();
                return null;
            });
        }
    }

    @Test
    public void localConnectionPassesTerminalTypeBeforeProcessStart() throws Exception {
        var connect = new ShellConnect();
        connect.setCharset(StandardCharsets.UTF_8.name());
        connect.setTermType("linux");
        var widget = callOnFxThread(ShellLocalTermWidget::new);
        try {
            var connector = callOnFxThread(() -> {
                widget.setEngine(TtyTerminalEngine.GHOSTTY);
                var created = widget.createTtyConnector(new ShellLocalClient(connect));
                widget.openSession(created);
                return created;
            });
            assertNotNull(connector);
            callOnFxThread(() -> {
                widget.getGhosttyView().sendText("printf '\\033]2;term-%s\\007' \"$TERM\"\r");
                return null;
            });

            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
            String title = null;
            while (System.nanoTime() < deadline) {
                title = callOnFxThread(() -> widget.getGhosttyView().getTitle());
                if ("term-linux".equals(title)) {
                    break;
                }
                Thread.sleep(100);
            }
            assertEquals("term-linux", title);
        } finally {
            callOnFxThread(() -> {
                widget.close();
                return null;
            });
        }
    }

    private static final class TestWidget extends TtyTermWidget {

        private TestWidget() {
            super(new FXDefaultSettingsProvider());
        }

        @Override
        public TtyConnector createTtyConnector() {
            throw new UnsupportedOperationException();
        }
    }

    private static <T> T callOnFxThread(Callable<T> action) throws Exception {
        var future = new FutureTask<>(action);
        Platform.runLater(future);
        return future.get(30, TimeUnit.SECONDS);
    }
}
