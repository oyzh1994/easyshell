package cn.oyzh.easyshell.tabs.dameng.query;

import cn.oyzh.easyshell.tabs.dameng.ShellDamengBaseTab;
import cn.oyzh.fx.db.query.DBQueryResults;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.FXConst;

/**
 * 达梦查询信息标签页，用于展示SQL执行结果信息
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellDamengQueryInfoTab extends RichTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "dameng/query/shellDamengQueryInfoTab.fxml";
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
    public ShellDamengQueryInfoTabController controller() {
        return (ShellDamengQueryInfoTabController) super.controller();
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
    public static ShellDamengQueryInfoTab of(DBQueryResults<?> results) {
        ShellDamengQueryInfoTab tab = new ShellDamengQueryInfoTab();
        tab.init(results);
        return tab;
    }
}
