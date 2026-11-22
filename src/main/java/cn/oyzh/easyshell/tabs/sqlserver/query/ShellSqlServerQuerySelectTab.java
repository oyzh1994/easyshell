package cn.oyzh.easyshell.tabs.sqlserver.query;

import cn.oyzh.easyshell.query.sqlserver.SqlServerExecuteResult;
import cn.oyzh.easyshell.trees.sqlserver.schema.ShellSqlServerSchemaTreeItem;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.FXConst;

/**
 * SqlServer查询结果标签页，用于展示查询执行结果
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellSqlServerQuerySelectTab extends RichTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "sqlserver/query/shellSqlServerQuerySelectTab.fxml";
    }

    /**
     * 初始化
     *
     * @param title 标题
     * @param result 结果
     * @param dbItem 数据库树节点
     */
    public void init(String title, SqlServerExecuteResult result, ShellSqlServerSchemaTreeItem dbItem) {
        this.setTitle(title);
        this.controller().init(result, dbItem);
    }

    @Override
    public ShellSqlServerQuerySelectTabController controller() {
        return (ShellSqlServerQuerySelectTabController) super.controller();
    }

    @Override
    public void initNode() {
        this.setClosable(false);
        super.initNode();
    }

    /**
     * 创建实例
     *
     * @param title 标题
     * @param result 结果
     * @param dbItem 数据库树节点
     * @return 实例对象
     */
    public static ShellSqlServerQuerySelectTab of(String title, SqlServerExecuteResult result, ShellSqlServerSchemaTreeItem dbItem) {
        ShellSqlServerQuerySelectTab tab = new ShellSqlServerQuerySelectTab();
        tab.init(title, result, dbItem);
        return tab;
    }
}
