package cn.oyzh.easyshell.tabs.sqlserver.query;

import cn.oyzh.easyshell.query.sqlserver.SqlServerExplainResult;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.FXConst;

/**
 * SqlServerSQL解释结果标签页，用于展示执行计划与耗时等信息
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellSqlServerQueryExplainTab extends RichTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "sqlserver/query/shellSqlServerQueryExplainTab.fxml";
    }

    /**
     * 初始化
     *
     * @param title 标题
     * @param result 结果
     */
    public void init(String title, SqlServerExplainResult result) {
        this.setTitle(title);
        this.controller().init(result);
    }

    @Override
    public ShellSqlServerQueryExplainTabController controller() {
        return (ShellSqlServerQueryExplainTabController) super.controller();
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
    public static ShellSqlServerQueryExplainTab of(String title, SqlServerExplainResult result) {
        ShellSqlServerQueryExplainTab tab = new ShellSqlServerQueryExplainTab();
        tab.init(title, result);
        return tab;
    }
}
