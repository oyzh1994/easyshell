package cn.oyzh.easyshell.tabs.mongo.query;

import cn.oyzh.fx.db.query.DBQueryResults;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.FXConst;

/**
 * mongodb查询信息tab
 *
 * @author oyzh
 * @since 2026-06-29
 */
public class ShellMongoQueryInfoTab extends RichTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "mongo/query/shellMongoQueryInfoTab.fxml";
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
    public ShellMongoQueryInfoTabController controller() {
        return (ShellMongoQueryInfoTabController) super.controller();
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
    public static ShellMongoQueryInfoTab of(DBQueryResults<?> results) {
        ShellMongoQueryInfoTab tab = new ShellMongoQueryInfoTab();
        tab.init(results);
        return tab;
    }
}
