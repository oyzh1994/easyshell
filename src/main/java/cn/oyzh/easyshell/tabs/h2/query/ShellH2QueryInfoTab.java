package cn.oyzh.easyshell.tabs.h2.query;

import cn.oyzh.fx.db.query.DBQueryResults;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.FXConst;

/**
 * H2 查询信息标签页
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellH2QueryInfoTab extends RichTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "h2/query/shellH2QueryInfoTab.fxml";
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
    public ShellH2QueryInfoTabController controller() {
        return (ShellH2QueryInfoTabController) super.controller();
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
    public static ShellH2QueryInfoTab of(DBQueryResults<?> results) {
        ShellH2QueryInfoTab tab = new ShellH2QueryInfoTab();
        tab.init(results);
        return tab;
    }
}
