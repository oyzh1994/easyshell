package cn.oyzh.easyshell.tabs.vnc1;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.internal.ShellConnState;
import cn.oyzh.easyshell.tabs.ShellBaseTabController;
import cn.oyzh.easyshell.util.ShellClientUtil;
import cn.oyzh.easyshell.vnc1.ShellVNCClient;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.fx.vnc.VncFramebufferView;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;

/**
 * vnc组件
 *
 * @author oyzh
 * @since 2025/05/23
 */
public class ShellVNCTabController extends ShellBaseTabController {

    /**
     * 根节点
     */
    @FXML
    private ScrollPane root;

    /**
     * vnc视图
     */
    @FXML
    private VncFramebufferView vncView;

    /**
     * vnc客户端
     */
    private ShellVNCClient client;

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
        this.client = ShellClientUtil.newClient(shellConnect);
        // 监听连接状态
        this.client.addStateListener((observableValue, shellConnState, t1) -> {
            if (t1 == ShellConnState.INTERRUPTED) {
                MessageBox.warn("[" + this.client.connectName() + "] " + I18nHelper.connectSuspended());
            }
        });
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
                // 初始化组件
                this.initVncView();
                this.hideLeft();
            } catch (Throwable ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
                this.closeTab();
            }
        });
    }

    /**
     * 初始化渲染组件
     */
    private void initVncView() {
        // Create framebuffer view (must happen before startNormalHandling)
        this.client.initVncView(this.vncView);
        ThreadUtil.sleep(20);
        this.initScale();
    }

    /**
     * 初始化缩放
     */
    private void initScale() {
        double width = this.root.getWidth() - 4;
        double height = this.root.getHeight() - 4;
        this.client.zoomToFit((int) width, (int) height, this.vncView.getFbWidth(), this.vncView.getFbHeight());
    }

    @Override
    public void onTabClosed(Event event) {
        super.onTabClosed(event);
        IOUtil.close(this.client);
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        this.root.widthProperty().addListener((observable, oldValue, newValue) -> this.initScale());
        this.root.heightProperty().addListener((observable, oldValue, newValue) -> this.initScale());

    }
}
