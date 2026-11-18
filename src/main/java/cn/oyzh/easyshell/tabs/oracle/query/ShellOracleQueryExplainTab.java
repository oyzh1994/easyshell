package cn.oyzh.easyshell.tabs.oracle.query;

import cn.oyzh.easyshell.query.oracle.OracleExplainResult;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.FXConst;

/**
 * OracleSQL解释结果标签页，用于展示执行计划与耗时等信息
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellOracleQueryExplainTab extends RichTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "oracle/query/shellOracleQueryExplainTab.fxml";
    }

    /**
     * 初始化
     *
     * @param title 标题
     * @param result 结果
     */
    public void init(String title, OracleExplainResult result) {
        this.setTitle(title);
        this.controller().init(result);
    }

    @Override
    public ShellOracleQueryExplainTabController controller() {
        return (ShellOracleQueryExplainTabController) super.controller();
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
    public static ShellOracleQueryExplainTab of(String title, OracleExplainResult result) {
        ShellOracleQueryExplainTab tab = new ShellOracleQueryExplainTab();
        tab.init(title, result);
        return tab;
    }
}
