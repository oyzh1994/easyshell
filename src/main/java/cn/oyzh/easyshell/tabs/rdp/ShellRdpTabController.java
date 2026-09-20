package cn.oyzh.easyshell.tabs.rdp;

import cn.oyzh.common.util.IOUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.internal.ShellConnState;
import cn.oyzh.easyshell.rdp.ShellRDPClient;
import cn.oyzh.easyshell.tabs.ShellBaseTabController;
import cn.oyzh.easyshell.util.ShellClientUtil;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.fx.rdp.RdpView;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.fxml.FXML;

/**
 * rdp组件
 *
 * @author oyzh
 * @since 2026/09/20
 */
public class ShellRdpTabController extends ShellBaseTabController {

    /**
     * rdp视图
     */
    @FXML
    private RdpView rdpView;

    /**
     * rdp客户端
     */
    private ShellRDPClient client;

    public ShellRDPClient client() {
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
                this.client.initRdpView(this.rdpView);
                if (!this.client.isConnected()) {
                    this.client.start();
                }
                if (!this.client.isConnected()) {
                    MessageBox.warn(I18nHelper.connectFail());
                    this.closeTab();
                    return;
                }
                this.rdpView.setScaleToFit(true);
                this.hideLeft();
            } catch (Throwable ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
                this.closeTab();
            }
        });
    }

    @Override
    public void onTabClosed(Event event) {
        super.onTabClosed(event);
        IOUtil.close(this.client);
    }
}
