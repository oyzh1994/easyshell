package cn.oyzh.easyshell.tabs.sqlite.query;

import cn.oyzh.fx.db.query.DBQueryResults;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.FXConst;

/**
 * SQLite 查询信息标签页
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellSqliteQueryInfoTab extends RichTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "sqlite/query/shellSqliteQueryInfoTab.fxml";
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
    public ShellSqliteQueryInfoTabController controller() {
        return (ShellSqliteQueryInfoTabController) super.controller();
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
    public static ShellSqliteQueryInfoTab of(DBQueryResults<?> results) {
        ShellSqliteQueryInfoTab tab = new ShellSqliteQueryInfoTab();
        tab.init(results);
        return tab;
    }
}
