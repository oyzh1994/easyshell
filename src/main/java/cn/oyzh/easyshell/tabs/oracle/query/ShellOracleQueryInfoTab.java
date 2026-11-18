package cn.oyzh.easyshell.tabs.oracle.query;

import cn.oyzh.easyshell.tabs.oracle.ShellOracleBaseTab;
import cn.oyzh.fx.db.query.DBQueryResults;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.FXConst;

/**
 * Oracle查询信息标签页，用于展示SQL执行结果信息
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellOracleQueryInfoTab extends RichTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "oracle/query/shellOracleQueryInfoTab.fxml";
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
    public ShellOracleQueryInfoTabController controller() {
        return (ShellOracleQueryInfoTabController) super.controller();
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
    public static ShellOracleQueryInfoTab of(DBQueryResults<?> results) {
        ShellOracleQueryInfoTab tab = new ShellOracleQueryInfoTab();
        tab.init(results);
        return tab;
    }
}
