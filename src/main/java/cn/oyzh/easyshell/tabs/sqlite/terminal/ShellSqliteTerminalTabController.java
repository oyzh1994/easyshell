package cn.oyzh.easyshell.tabs.sqlite.terminal;

import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.easyshell.terminal.sqlite.ShellSqliteTerminalPane;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.trees.sqlite.root.ShellSqliteRootTreeItem;
import cn.oyzh.fx.gui.tabs.RichTabController;
import javafx.fxml.FXML;

/**
 * SQLite 命令行标签页控制器
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellSqliteTerminalTabController extends RichTabController {

    /**
     * SQLite命令行文本域
     */
    @FXML
    private ShellSqliteTerminalPane terminal;

    /**
     * 数据库树节点
     */
    private ShellSqliteRootTreeItem dbItem;

    /**
     * 初始化
     *
     * @param dbItem 数据库树节点
     */
    public void init(ShellSqliteRootTreeItem dbItem) {
        this.dbItem = dbItem;
        this.terminal.init(dbItem.client(), dbItem.dbName());
    }

    /**
     * 获取数据库树节点
     *
     * @return 数据库树节点
     */
    public ShellSqliteRootTreeItem getDbItem() {
        return this.dbItem;
    }

    /**
     * 获取数据库名称
     *
     * @return 数据库名称
     */
    public String getDbName() {
        return this.dbItem.dbName();
    }

    /**
     * 获取连接信息
     *
     * @return 连接信息
     */
    public ShellConnect shellConnect() {
        return this.terminal.shellConnect();
    }

    /**
     * 获取客户端
     *
     * @return 客户端
     */
    public ShellSqliteClient client() {
        return this.terminal.getClient();
    }

    @Override
    public void destroy() {
        if (this.terminal.isTemporary()) {
            this.client().close();
        }
        super.destroy();
    }
}
