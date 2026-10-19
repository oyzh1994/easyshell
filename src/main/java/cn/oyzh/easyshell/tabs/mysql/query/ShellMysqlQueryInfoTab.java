package cn.oyzh.easyshell.tabs.mysql.query;

import cn.oyzh.fx.db.query.DBQueryResults;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.FXConst;

/**
 * MySQL 查询信息标签页
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellMysqlQueryInfoTab extends RichTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "mysql/query/shellMysqlQueryInfoTab.fxml";
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
    public ShellMysqlQueryInfoTabController controller() {
        return (ShellMysqlQueryInfoTabController) super.controller();
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
    public static ShellMysqlQueryInfoTab of(DBQueryResults<?> results) {
        ShellMysqlQueryInfoTab tab = new ShellMysqlQueryInfoTab();
        tab.init(results);
        return tab;
    }
}
