package cn.oyzh.easyshell.tabs.local;

import cn.oyzh.common.system.OSUtil;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.local.ShellLocalTermWidget;
import cn.oyzh.easyshell.local.ShellLocalTtyConnector;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.easyshell.util.ShellConnectUtil;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.jeditermfx.terminal.ui.FXHyperlinkFilter;
import com.jediterm.terminal.ui.FXTerminalPanel;
import javafx.event.Event;
import javafx.fxml.FXML;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * 本地终端tab内容组件
 *
 * @author oyzh
 * @since 2025/03/20
 */
public class ShellLocalTabController extends RichTabController {

    /**
     * 根节点
     */
    @FXML
    private ShellLocalTermWidget widget;

    /**
     * 当前连接
     */
    private ShellConnect shellConnect;

    /**
     * 设置
     */
    private final ShellSetting setting = ShellSettingStore.SETTING;

    private void initWidget() throws IOException {
        Charset charset = Charset.forName(this.shellConnect.getCharset());
        ShellLocalTtyConnector connector = this.widget.createTtyConnector(charset);
        this.widget.openSession(connector);
        this.widget.onTermination(exitCode -> this.widget.close());
        this.widget.addHyperlinkFilter(new FXHyperlinkFilter());
        // macos需要初始化部分参数
        if (OSUtil.isMacOS()) {
            if (this.shellConnect.getTermType() != null) {
                connector.write("export TERM=" + this.shellConnect.getTermType() + "\n");
            }
            if (this.shellConnect.getCharset() != null) {
                connector.write("export LANG=en_US." + this.shellConnect.getCharset() + "\n");
            }
        }
    }

    /**
     * 初始化背景
     */
    private void initBackground() {
        FXTerminalPanel terminalPanel = this.widget.getTerminalPanel();
        // 处理背景
        ShellConnectUtil.initBackground(this.shellConnect, terminalPanel);
    }

    public void init(ShellConnect shellConnect) throws IOException {
        this.shellConnect = shellConnect;
        // 收起左侧
        if (this.setting.isHiddenLeftAfterConnected()) {
            ShellEventUtil.layout1();
        }
        // 初始化组件
        this.initWidget();
        // 异步加载背景
        ThreadUtil.startVirtual(this::initBackground);
    }

    @Override
    public void onTabClosed(Event event) {
        super.onTabClosed(event);
        this.widget.close();
        // 展开左侧
        if (this.setting.isHiddenLeftAfterConnected()) {
            ShellEventUtil.layout2();
        }
    }

    public ShellConnect shellConnect() {
        return shellConnect;
    }
}
