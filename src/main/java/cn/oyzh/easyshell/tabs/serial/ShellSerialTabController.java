package cn.oyzh.easyshell.tabs.serial;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.serial.ShellSerialClient;
import cn.oyzh.easyshell.serial.ShellSerialTermWidget;
import cn.oyzh.easyshell.serial.ShellSerialTtyConnector;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.easyshell.trees.connect.ShellConnectTreeItem;
import cn.oyzh.easyshell.util.ShellConnectUtil;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import com.jediterm.terminal.ui.FXTerminalPanel;
import javafx.event.Event;
import javafx.fxml.FXML;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * shell连接串口内容组件
 *
 * @author oyzh
 * @since 2025/04/24
 */
public class ShellSerialTabController extends RichTabController {

    /**
     * 终端组件
     */
    @FXML
    private ShellSerialTermWidget widget;

    /**
     * serial客户端
     */
    private ShellSerialClient client;

    /**
     * 设置
     */
    private final ShellSetting setting = ShellSettingStore.SETTING;

    public ShellSerialClient getClient() {
        return client;
    }

    private ShellConnectTreeItem treeItem;

    public ShellConnectTreeItem getTreeItem() {
        return treeItem;
    }

    /**
     * 初始化组件
     *
     * @throws IOException io异常
     */
    private void initWidget() throws IOException {
        Charset charset = this.client.getCharset();
        ShellSerialTtyConnector connector = this.widget.createTtyConnector(charset);
        connector.initSerial(this.client);
        this.widget.openSession(connector);
        this.widget.onTermination(exitCode -> this.widget.close());
        // this.widget.addHyperlinkFilter(new FXHyperlinkFilter());
    }

    /**
     * 初始化背景
     */
    private void initBackground() {
        ShellConnect connect = this.client.getShellConnect();
        FXTerminalPanel terminalPanel = this.widget.getTerminalPanel();
        // 处理背景
        ShellConnectUtil.initBackground(connect, terminalPanel);
    }

    /**
     * 初始化
     *
     * @param treeItem shell连接节点
     */
    public void init(ShellConnectTreeItem treeItem) {
        this.treeItem = treeItem;
        this.client = new ShellSerialClient(treeItem.value());
        StageManager.showMask(() -> {
            try {
                if (!this.client.isConnected()) {
                    this.client.start();
                }
                if (!this.client.isConnected()) {
                    MessageBox.warn(I18nHelper.connectFail()
                            + " errCode: " + this.client.getLastErrorCode()
                            + " errLocation: " + this.client.getLastErrorLocation());
                    this.closeTab();
                    return;
                }
                // 收起左侧
                if (this.setting.isHiddenLeftAfterConnected()) {
                    ShellEventUtil.layout1();
                }
                // 初始化组件
                this.initWidget();
                // 异步加载背景
                ThreadUtil.startVirtual(this::initBackground);
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
                this.closeTab();
            }
        });
    }

    @Override
    public void onTabClosed(Event event) {
        super.onTabClosed(event);
        this.getClient().close();
        this.widget.close();
        // 展开左侧
        if (this.setting.isHiddenLeftAfterConnected()) {
            ShellEventUtil.layout2();
        }
    }

    public ShellConnect shellConnect() {
        return this.client.getShellConnect();
    }

    /**
     * 运行片段
     *
     * @param content 内容
     */
    public void runSnippet(String content) throws IOException {
        this.widget.getTtyConnector().write(content);
    }
}
