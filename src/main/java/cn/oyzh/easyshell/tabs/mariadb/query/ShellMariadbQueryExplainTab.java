package cn.oyzh.easyshell.tabs.mariadb.query;

import cn.oyzh.easyshell.query.mariadb.ShellMariadbExplainResult;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.FXConst;

/**
 * MariaDB 查询执行计划标签页
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbQueryExplainTab extends RichTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "mariadb/query/shellMariadbQueryExplainTab.fxml";
    }

    /**
     * 初始化
     *
     * @param title 标题
     * @param result 结果
     */
    public void init(String title, ShellMariadbExplainResult result) {
        this.setTitle(title);
        this.controller().init(result);
    }

    @Override
    public ShellMariadbQueryExplainTabController controller() {
        return (ShellMariadbQueryExplainTabController) super.controller();
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
    public static ShellMariadbQueryExplainTab of(String title, ShellMariadbExplainResult result) {
        ShellMariadbQueryExplainTab tab = new ShellMariadbQueryExplainTab();
        tab.init(title, result);
        return tab;
    }
}
