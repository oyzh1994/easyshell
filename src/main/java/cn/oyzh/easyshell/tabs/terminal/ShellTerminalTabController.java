package cn.oyzh.easyshell.tabs.terminal;

import cn.oyzh.common.system.OSUtil;
import cn.oyzh.easyshell.terminal.ShellDefaultTermWidget;
import cn.oyzh.easyshell.terminal.ShellDefaultTtyConnector;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.jeditermfx.terminal.ui.FXHyperlinkFilter;
import javafx.event.Event;
import javafx.fxml.FXML;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * shell终端tab内容组件
 *
 * @author oyzh
 * @since 2025/03/20
 */
public class ShellTerminalTabController extends RichTabController {

    /**
     * 根节点
     */
    @FXML
    private ShellDefaultTermWidget widget;

    private void initWidget() throws IOException {
        ShellDefaultTtyConnector connector = (ShellDefaultTtyConnector) this.widget.createTtyConnector();
        this.widget.openSession(connector);
        this.widget.onTermination(exitCode -> this.widget.close());
        this.widget.addHyperlinkFilter(new FXHyperlinkFilter());
        // macos需要初始化终端类型
        if (OSUtil.isMacOS()) {
            connector.write("export TERM=xterm-256color\n");
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resource) {
        super.initialize(url, resource);
        try {
            this.initWidget();
        } catch (IOException ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @Override
    public void onTabClosed(Event event) {
        super.onTabClosed(event);
        this.widget.close();
    }
}
