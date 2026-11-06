package cn.oyzh.easyshell.tabs.postgresql.terminal;

import cn.oyzh.easyshell.postgresql.ShellPostgresqlClient;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.terminal.postgresql.PostgresqlTerminalPane;
import cn.oyzh.easyshell.trees.postgresql.schema.ShellPostgresqlSchemaTreeItem;
import cn.oyzh.fx.gui.tabs.RichTabController;
import javafx.event.Event;
import javafx.fxml.FXML;

/**
 * postgresql命令行tab内容组件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellPostgresqlTerminalTabController extends RichTabController {

    /**
     * postgresql命令行文本域
     */
    @FXML
    private PostgresqlTerminalPane terminal;

    /**
     * 模式
     */
    private ShellPostgresqlSchemaTreeItem dbItem;

    /**
     * 初始化
     *
     * @param dbItem db节点
     */
    public void init(ShellPostgresqlSchemaTreeItem dbItem) {
        this.dbItem = dbItem;
        this.terminal.init(dbItem.client(), dbItem.schema());
    }

    /**
     * 获取数据库树节点
     *
     * @return 数据库树节点
     */
    public ShellPostgresqlSchemaTreeItem getDbItem() {
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
    public ShellPostgresqlClient client() {
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
