package cn.oyzh.easyshell.tabs.oracle.terminal;

import cn.oyzh.easyshell.oracle.ShellOracleClient;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.terminal.oracle.OracleTerminalPane;
import cn.oyzh.easyshell.trees.oracle.schema.ShellOracleSchemaTreeItem;
import cn.oyzh.fx.gui.tabs.RichTabController;
import javafx.event.Event;
import javafx.fxml.FXML;

/**
 * oracle命令行tab内容组件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellOracleTerminalTabController extends RichTabController {

    /**
     * oracle命令行文本域
     */
    @FXML
    private OracleTerminalPane terminal;

    /**
     * 模式
     */
    private ShellOracleSchemaTreeItem dbItem;

    /**
     * 初始化
     *
     * @param dbItem db节点
     */
    public void init(ShellOracleSchemaTreeItem dbItem) {
        this.dbItem = dbItem;
        this.terminal.init(dbItem.client(), dbItem.schema());
    }

    /**
     * 获取数据库树节点
     *
     * @return 数据库树节点
     */
    public ShellOracleSchemaTreeItem getDbItem() {
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

    /**
     * 获取客户端
     *
     * @return 客户端
     */
    public ShellOracleClient client() {
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
