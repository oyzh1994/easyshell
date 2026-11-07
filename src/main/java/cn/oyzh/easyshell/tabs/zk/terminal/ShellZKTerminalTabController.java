package cn.oyzh.easyshell.tabs.zk.terminal;

import cn.oyzh.common.util.IOUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.terminal.zk2.ZKTerminalPane;
import cn.oyzh.easyshell.zk.ShellZKClient;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.information.MessageBox;
import javafx.fxml.FXML;

/**
 * zk终端tab内容组件
 *
 * @author oyzh
 * @since 2025-09-04
 */
public class ShellZKTerminalTabController extends RichTabController {

    /**
     * 命令行文本域
     */
    @FXML
    private ZKTerminalPane terminal;

    /**
     * zk客户端
     */
    private ShellZKClient client;

    /**
     * 终端客户端
     */
    private ShellZKClient terminalClient;

    /**
     * 设置客户端
     *
     * @param client 客户端
     */
    public void init(ShellZKClient client) {
        this.client = client;
        // 如果tab已被选中，则直接初始化终端
        if (this.getTab() != null && this.getTab().isSelected()) {
            this.initTerminal();
        }
    }

    /**
     * 初始化终端
     */
    private void initTerminal() {
        if (this.terminalClient == null && this.client != null) {
            try {
                this.terminalClient = this.client.forkClient();
                this.terminal.init(this.terminalClient);
            } catch (Throwable ex) {
                ex.printStackTrace();
                MessageBox.exception(ex, this.getWindow());
            }
        }
    }

    /**
     * 获取zk客户端
     *
     * @return zk客户端
     */
    public ShellZKClient client() {
        return this.terminal.getClient();
    }

    /**
     * 获取zk信息
     *
     * @return zk信息
     */
    public ShellConnect shellConnect() {
        return this.client.getShellConnect();
    }

    @Override
    public void onTabInit(FXTab tab) {
        super.onTabInit(tab);
        tab.selectedProperty().subscribe((oldValue, newValue) -> {
            if (newValue) {
                this.initTerminal();
            }
        });
    }

    @Override
    public void destroy() {
        this.terminal.destroy();
        IOUtil.closeAsync(this.terminalClient);
        super.destroy();
    }
}
