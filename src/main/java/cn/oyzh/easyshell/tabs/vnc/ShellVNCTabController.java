package cn.oyzh.easyshell.tabs.vnc;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.easyshell.vnc.ShellVNCClient;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import org.jfxvnc.ui.control.VncImageView;
import org.jfxvnc.ui.service.VncRenderService;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * vnc组件
 *
 * @author oyzh
 * @since 2025/05/23
 */
public class ShellVNCTabController extends RichTabController {

    /**
     * 根节点
     */
    @FXML
    private ScrollPane root;

    /**
     * vnc视图
     */
    @FXML
    private VncImageView vncView;

    /**
     * vnc渲染组件
     */
    private VncRenderService renderService;

    /**
     * vnc客户端
     */
    private ShellVNCClient client;

    /**
     * 设置
     */
    private final ShellSetting setting = ShellSettingStore.SETTING;

    public ShellVNCClient client() {
        return this.client;
    }

    public ShellConnect shellConnect() {
        return this.client.getShellConnect();
    }

    /**
     * 初始化
     */
    public void init(ShellConnect shellConnect) {
        this.client = new ShellVNCClient(shellConnect);
        // 初始化组件
        this.initRenderService();
        // 设置渲染组件
        this.client.setRenderProtocol(this.renderService);
        // 执行连接
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
                // 初始化缩放
                this.initScale();
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
                this.closeTab();
            }
        });
    }

    /**
     * 初始化渲染组件
     */
    private void initRenderService() {
        this.renderService = new VncRenderService();
        this.renderService.setEventConsumer(this.vncView);
        this.renderService.serverCutTextProperty().addListener((l, old, text) -> this.vncView.addClipboardText(text));
        this.renderService.getConfiguration().clientCursorProperty().addListener((l, a, b) -> this.vncView.setUseClientCursor(b));
        this.renderService.inputEventListenerProperty().addListener(l -> this.vncView.registerInputEventListener(this.renderService.inputEventListenerProperty().get()));
        this.renderService.zoomLevelProperty().addListener((observable, oldValue, newValue) -> this.vncView.setZoomLevel(newValue.doubleValue()));
        this.vncView.setOnZoom(e -> this.renderService.setZoomLevel(e.getTotalZoomFactor()));
    }

    @Override
    public void onTabClosed(Event event) {
        super.onTabClosed(event);
        this.client.close();
        // 展开左侧
        if (this.setting.isHiddenLeftAfterConnected()) {
            ShellEventUtil.layout2();
        }
    }

    @Override
    public void onTabCloseRequest(Event event) {
        super.onTabCloseRequest(event);
        this.vncView.unregisterInputEventListener();
    }

    /**
     * 初始化缩放
     */
    private void initScale() {
        ThreadUtil.start(() -> {
            Integer frameWidth = this.renderService.frameWidth();
            Integer frameHeight = this.renderService.frameHeight();
            if (frameWidth != null && frameHeight != null) {
                double width = this.root.getWidth() - 4;
                double scale1 = width / frameWidth;
                double height = this.root.getHeight() - 4;
                double scale2 = height / frameHeight;
                this.renderService.setZoomLevel(Math.min(scale1, scale2));
            }
        }, 200);
    }

    @Override
    public void initialize(URL location, ResourceBundle resourceBundle) {
        super.initialize(location, resourceBundle);
        this.root.widthProperty().addListener((observable, oldValue, newValue) -> this.initScale());
        this.root.heightProperty().addListener((observable, oldValue, newValue) -> this.initScale());
    }
}
