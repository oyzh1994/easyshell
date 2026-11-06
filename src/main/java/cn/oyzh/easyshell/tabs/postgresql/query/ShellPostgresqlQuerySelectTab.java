package cn.oyzh.easyshell.tabs.postgresql.query;

import cn.oyzh.easyshell.query.postgresql.PostgresqlExecuteResult;
import cn.oyzh.easyshell.trees.postgresql.schema.ShellPostgresqlSchemaTreeItem;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.FXConst;

/**
 * PostgreSQL查询结果标签页，用于展示查询执行结果
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellPostgresqlQuerySelectTab extends RichTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "postgresql/query/shellPostgresqlQuerySelectTab.fxml";
    }

    /**
     * 初始化
     *
     * @param title 标题
     * @param result 结果
     * @param dbItem 数据库树节点
     */
    public void init(String title, PostgresqlExecuteResult result, ShellPostgresqlSchemaTreeItem dbItem) {
        this.setTitle(title);
        this.controller().init(result, dbItem);
    }

    @Override
    public ShellPostgresqlQuerySelectTabController controller() {
        return (ShellPostgresqlQuerySelectTabController) super.controller();
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
    public static ShellPostgresqlQuerySelectTab of(String title, PostgresqlExecuteResult result, ShellPostgresqlSchemaTreeItem dbItem) {
        ShellPostgresqlQuerySelectTab tab = new ShellPostgresqlQuerySelectTab();
        tab.init(title, result, dbItem);
        return tab;
    }
}
