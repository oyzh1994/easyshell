package cn.oyzh.easyshell.tabs.local;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.local.ShellLocalTermWidget;
import cn.oyzh.easyshell.local.ShellLocalTtyConnector;
import cn.oyzh.easyshell.tabs.ShellBaseTabController;
import cn.oyzh.easyshell.tabs.ShellSnippetAdapter;
import cn.oyzh.easyshell.util.ShellConnectUtil;
import com.jediterm.terminal.ui.FXTerminalPanel;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * 本地终端tab内容组件
 *
 * @author oyzh
 * @since 2025/03/20
 */
public class ShellLocalTabController extends ShellBaseTabController implements ShellSnippetAdapter {

    /**
     * 根节点
     */
    @FXML
    private ShellLocalTermWidget widget;

    /**
     * 当前连接
     */
    private ShellConnect shellConnect;

    // /**
    //  * 设置
    //  */
    // private final ShellSetting setting = ShellSettingStore.SETTING;

    private void initWidget() throws IOException {
        Charset charset = Charset.forName(this.shellConnect.getCharset());
        // 初始化部分参数
        if (this.shellConnect.getTermType() != null) {
            this.widget.putEnvironment("TERM", this.shellConnect.getTermType());
        }
        if (this.shellConnect.getCharset() != null) {
            this.widget.putEnvironment("LANG", "en_US." + charset);
        }
        ShellLocalTtyConnector connector = this.widget.createTtyConnector(charset);
        // 初始化退格码
        this.widget.initBackspaceCode(this.shellConnect().getBackspaceType());
        // 设置alt修饰
        this.widget.setAltSendsEscape(this.shellConnect().isAltSendsEscape());
        this.widget.openSession(connector);
        // this.widget.onTermination(exitCode -> this.widget.close());
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
        // if (this.setting.isHiddenLeftAfterConnected()) {
        //     ShellEventUtil.layout1();
        // }
        this.hideLeft();
        // 初始化组件
        this.initWidget();
        // 异步加载背景
        ThreadUtil.startVirtual(this::initBackground);
    }

    @Override
    public void onTabClosed(Event event) {
        super.onTabClosed(event);
        this.widget.close();
        // // 展开左侧
        // if (this.setting.isHiddenLeftAfterConnected()) {
        //     ShellEventUtil.layout2();
        // }
    }

    public ShellConnect shellConnect() {
        return shellConnect;
    }

    /**
     * 片段列表
     *
     * @param event 事件
     */
    @FXML
    private void snippet(MouseEvent event) {
        ShellSnippetAdapter.super.snippetList((Node) event.getSource());
    }

    @Override
    public void runSnippet(String content) throws IOException {
        this.widget.getTtyConnector().write(content);
    }
}
