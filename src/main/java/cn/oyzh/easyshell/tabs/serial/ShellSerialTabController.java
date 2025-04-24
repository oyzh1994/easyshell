package cn.oyzh.easyshell.tabs.serial;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.serial.SerialClient;
import cn.oyzh.easyshell.serial.SerialTermWidget;
import cn.oyzh.easyshell.serial.SerialTtyConnector;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.easyshell.trees.connect.ShellConnectTreeItem;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import cn.oyzh.jeditermfx.terminal.ui.FXHyperlinkFilter;
import com.jediterm.terminal.ui.FXFXTerminalPanel;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * shell连接tab内容组件
 *
 * @author oyzh
 * @since 2025/04/16
 */
public class ShellSerialTabController extends RichTabController {

    /**
     * 终端组件
     */
    @FXML
    private SerialTermWidget widget;

    /**
     * serial客户端
     */
    private SerialClient client;

    public SerialClient getClient() {
        return client;
    }

    public ShellConnectTreeItem getTreeItem() {
        return treeItem;
    }

    private ShellConnectTreeItem treeItem;

    /**
     * 设置
     */
    private final ShellSetting setting = ShellSettingStore.SETTING;

    private void initWidget( ) throws IOException {
        Charset charset = this.client.getCharset();
        SerialTtyConnector connector = this.widget.createTtyConnector(charset);
        connector.initSerial(this.client);
        this.widget.openSession(connector);
        this.widget.onTermination(exitCode -> this.widget.close());
        this.widget.addHyperlinkFilter(new FXHyperlinkFilter());
    }

    /**
     * 初始化背景
     */
    private void initBackground() {
        ShellConnect connect = this.client.getShellConnect();
        if (!connect.isEnableBackground()) {
            return;
        }
        // 背景失效
        if (connect.isBackgroundImageInvalid()) {
            MessageBox.warn(ShellI18nHelper.connectTip7());
            return;
        }
        // 处理背景
        FXFXTerminalPanel terminalPane = this.widget.getTerminalPanel();
        Node canvas = terminalPane.getFirstChild();
        // 对画板设置透明度
        canvas.setOpacity(0.7);
        // 背景图片
        String url = connect.getBackgroundImageUrl();
        Image backgroundImage = new Image(url);
        // 生成背景
        BackgroundSize backgroundSize = new BackgroundSize(
                BackgroundSize.AUTO, BackgroundSize.AUTO, false, false,
                true, true);
        BackgroundImage backgroundImg = new BackgroundImage(backgroundImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT,
                backgroundSize);
        // 创建 Background 对象
        Background background = new Background(backgroundImg);
        terminalPane.setBackground(background);
    }

    /**
     * 设置shell客户端
     *
     * @param treeItem shell客户端
     */
    public void init(ShellConnectTreeItem treeItem) {
        this.treeItem = treeItem;
        this.client = new SerialClient(treeItem.value());
        StageManager.showMask(() -> {
            try {
                if (!this.client.isConnected()) {
                    this.client.start();
                }
                if (!this.client.isConnected()) {
                    MessageBox.warn(I18nHelper.connectFail());
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
        // 展开左侧
        if (this.setting.isHiddenLeftAfterConnected()) {
            ShellEventUtil.layout2();
        }
    }

    public ShellConnect shellConnect() {
        return this.client.getShellConnect();
    }
}
