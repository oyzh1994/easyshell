package cn.oyzh.easyshell.tabs.h2.terminal;

import cn.oyzh.easyshell.h2.ShellH2Client;
import cn.oyzh.easyshell.terminal.h2.ShellH2TerminalPane;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.trees.h2.root.ShellH2RootTreeItem;
import cn.oyzh.fx.gui.tabs.RichTabController;
import javafx.fxml.FXML;

/**
 * H2 命令行标签页控制器
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellH2TerminalTabController extends RichTabController {

    /**
     * H2命令行文本域
     */
    @FXML
    private ShellH2TerminalPane terminal;

    /**
     * 数据库树节点
     */
    private ShellH2RootTreeItem dbItem;

    /**
     * 当前模式
     */
    private String schema;

    /**
     * 初始化
     *
     * @param dbItem 数据库树节点
     */
    public void init(ShellH2RootTreeItem dbItem) {
        this.init(dbItem, dbItem.dbName());
    }

    /**
     * 初始化指定模式终端。
     *
     * @param dbItem 数据库树节点
     * @param schema 模式名称
     */
    public void init(ShellH2RootTreeItem dbItem, String schema) {
        this.dbItem = dbItem;
        this.schema = schema;
        this.terminal.init(dbItem.client(), schema);
    }

    /**
     * 获取数据库树节点
     *
     * @return 数据库树节点
     */
    public ShellH2RootTreeItem getDbItem() {
        return this.dbItem;
    }

    /**
     * 获取数据库名称
     *
     * @return 数据库名称
     */
    public String getDbName() {
        return this.schema;
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
    public ShellH2Client client() {
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
