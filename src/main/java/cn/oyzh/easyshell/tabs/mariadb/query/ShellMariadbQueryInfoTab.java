package cn.oyzh.easyshell.tabs.mariadb.query;

import cn.oyzh.fx.db.query.DBQueryResults;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.FXConst;

/**
 * MariaDB 查询信息标签页
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbQueryInfoTab extends RichTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "mariadb/query/shellMariadbQueryInfoTab.fxml";
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
    public ShellMariadbQueryInfoTabController controller() {
        return (ShellMariadbQueryInfoTabController) super.controller();
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
    public static ShellMariadbQueryInfoTab of(DBQueryResults<?> results) {
        ShellMariadbQueryInfoTab tab = new ShellMariadbQueryInfoTab();
        tab.init(results);
        return tab;
    }
}
