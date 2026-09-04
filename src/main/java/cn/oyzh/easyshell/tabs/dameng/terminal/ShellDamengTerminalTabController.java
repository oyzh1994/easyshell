package cn.oyzh.easyshell.tabs.dameng.terminal;

import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.terminal.dameng.DamengTerminalPane;
import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
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
    private ShellDamengSchemaTreeItem dbItem;

    /**
     * 初始化
     *
     * @param dbItem db节点
     */
    public void init(ShellDamengSchemaTreeItem dbItem) {
        this.dbItem = dbItem;
        this.terminal.init(dbItem.client(), dbItem.schema());
    }

    public ShellDamengSchemaTreeItem getDbItem() {
        return dbItem;
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
