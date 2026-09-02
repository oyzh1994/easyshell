package cn.oyzh.easyshell.tabs.dameng.terminal;

import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.terminal.dameng.DamengTerminalPane;
import cn.oyzh.fx.gui.tabs.RichTabController;
import javafx.event.Event;
import javafx.fxml.FXML;

/**
 * dameng命令行tab内容组件
 *
 * @author oyzh
 * @since 2023/07/21
 */
public class ShellDamengTerminalTabController extends RichTabController {

    /**
     * dameng命令行文本域
     */
    @FXML
    private DamengTerminalPane terminal;

    /**
     * 模式
     */
    private String schema;

    /**
     * 初始化
     *
     * @param client dameng客户端
     * @param schema 模式
     */
    public void init(ShellDamengClient client, String schema) {
        this.terminal.init(client, schema);
        this.schema = schema;
    }

    public String getSchema() {
        return schema;
    }

    /**
     * db信息
     *
     * @return 当前db信息
     */
    protected ShellConnect shellConnect() {
        return this.terminal.shellConnect();
    }

    public ShellDamengClient client() {
        return this.terminal.getClient();
    }

    @Override
    public void onTabClosed(Event event) {
        if (this.terminal.isTemporary()) {
            this.client().close();
        }
        super.onTabClosed(event);
    }
}
