package cn.oyzh.easyshell.tabs.postgresql.query;

import cn.oyzh.easyshell.query.postgresql.PostgresqlExplainResult;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.FXConst;

/**
 * PostgreSQLSQL解释结果标签页，用于展示执行计划与耗时等信息
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellPostgresqlQueryExplainTab extends RichTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "postgresql/query/shellPostgresqlQueryExplainTab.fxml";
    }

    /**
     * 初始化
     *
     * @param title 标题
     * @param result 结果
     */
    public void init(String title, PostgresqlExplainResult result) {
        this.setTitle(title);
        this.controller().init(result);
    }

    @Override
    public ShellPostgresqlQueryExplainTabController controller() {
        return (ShellPostgresqlQueryExplainTabController) super.controller();
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
     * @return 实例对象
     */
    public static ShellPostgresqlQueryExplainTab of(String title, PostgresqlExplainResult result) {
        ShellPostgresqlQueryExplainTab tab = new ShellPostgresqlQueryExplainTab();
        tab.init(title, result);
        return tab;
    }
}
