package cn.oyzh.easyshell.tabs.sqlite.query;

import cn.oyzh.easyshell.query.sqlite.SQLiteExecuteResult;
import cn.oyzh.easyshell.trees.sqlite.root.ShellSqliteRootTreeItem;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.FXConst;

/**
 * SQLite 查询结果标签页
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellSqliteQuerySelectTab extends RichTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "sqlite/query/shellSqliteQuerySelectTab.fxml";
    }

    /**
     * 初始化
     *
     * @param title  标题
     * @param result 执行结果
     * @param dbItem 数据库树节点
     */
    public void init(String title, SQLiteExecuteResult result, ShellSqliteRootTreeItem dbItem) {
        this.setTitle(title);
        this.controller().init(result, dbItem);
    }

    @Override
    public ShellSqliteQuerySelectTabController controller() {
        return (ShellSqliteQuerySelectTabController) super.controller();
    }

    @Override
    public void initNode() {
        this.setClosable(false);
        super.initNode();
    }

    /**
     * 创建实例
     *
     * @param title  标题
     * @param result 执行结果
     * @param dbItem 数据库树节点
     * @return 实例对象
     */
    public static ShellSqliteQuerySelectTab of(String title, SQLiteExecuteResult result, ShellSqliteRootTreeItem dbItem) {
        ShellSqliteQuerySelectTab tab = new ShellSqliteQuerySelectTab();
        tab.init(title, result, dbItem);
        return tab;
    }
}
