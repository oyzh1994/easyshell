package cn.oyzh.easyshell.tabs.sqlserver.query;

import cn.oyzh.easyshell.tabs.sqlserver.ShellSqlServerBaseTab;
import cn.oyzh.fx.db.query.DBQueryResults;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.FXConst;

/**
 * SqlServer查询信息标签页，用于展示SQL执行结果信息
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellSqlServerQueryInfoTab extends RichTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "sqlserver/query/shellSqlServerQueryInfoTab.fxml";
    }

    /**
     * 初始化
     *
     * @param results 结果集
     */
    public void init(DBQueryResults<?> results) {
        this.controller().init(results);
    }

    @Override
    public ShellSqlServerQueryInfoTabController controller() {
        return (ShellSqlServerQueryInfoTabController) super.controller();
    }

    @Override
    public void initNode() {
        this.setClosable(false);
        super.initNode();
    }

    /**
     * 创建实例
     *
     * @param results 结果集
     * @return 实例对象
     */
    public static ShellSqlServerQueryInfoTab of(DBQueryResults<?> results) {
        ShellSqlServerQueryInfoTab tab = new ShellSqlServerQueryInfoTab();
        tab.init(results);
        return tab;
    }
}
