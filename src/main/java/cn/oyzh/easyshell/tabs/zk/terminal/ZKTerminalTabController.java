package cn.oyzh.easyshell.tabs.zk.terminal;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.terminal.zk.ZKTerminalPane;
import cn.oyzh.easyshell.zk.ZKClient;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import javafx.fxml.FXML;

/**
 * zk终端tab内容组件
 *
 * @author oyzh
 * @since 2023/07/21
 */
public class ZKTerminalTabController extends RichTabController {

    /**
     * 根节点
     */
    @FXML
    private FXTab root;

    /**
     * 命令行文本域
     */
    @FXML
    private ZKTerminalPane terminal;

    /**
     * 客户端
     */
    private ZKClient client;

    /**
     * 设置客户端
     *
     * @param client 客户端
     */
    public void init(ZKClient client) {
        this.client = client;
        // this.terminal.init(client);
    }

    /**
     * 获取zk客户端
     *
     * @return zk客户端
     */
    public ZKClient client() {
        return this.terminal.getClient();
    }

    /**
     * 获取zk信息
     *
     * @return zk信息
     */
    public ShellConnect zkConnect() {
        return this.terminal.zkConnect();
    }

    // @Override
    // public void onTabClosed(Event event) {
    //     if (this.terminal.isTemporary()) {
    //         ShellZKConnectUtil.close(this.client(), true, true);
    //     }
    //     super.onTabClosed(event);
    // }

    /**
     * 初始化标志位
     */
    private boolean initFlag = false;

    @Override
    public void onTabInit(FXTab tab) {
        super.onTabInit(tab);
        this.root.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue && !this.initFlag) {
                this.initFlag = true;
                this.terminal.init(this.client);
            }
        });
    }
}
