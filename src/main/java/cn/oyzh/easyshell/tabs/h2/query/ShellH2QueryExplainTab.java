package cn.oyzh.easyshell.tabs.h2.query;

import cn.oyzh.easyshell.query.h2.H2ExecuteResult;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.FXConst;

/**
 * H2 查询执行计划标签页
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellH2QueryExplainTab extends RichTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "h2/query/shellH2QueryExplainTab.fxml";
    }

    /**
     * 初始化
     *
     * @param title  标题
     * @param result 执行结果
     */
    public void init(String title, H2ExecuteResult result) {
        this.setTitle(title);
        this.controller().init(result);
    }

    @Override
    public ShellH2QueryExplainTabController controller() {
        return (ShellH2QueryExplainTabController) super.controller();
    }

    @Override
    public void initNode() {
        this.setClosable(false);
        super.initNode();
    }

    /**
     * 创建实例
     *
     * @param title  标题
     * @param result 执行结果
     * @return 实例对象
     */
    public static ShellH2QueryExplainTab of(String title, H2ExecuteResult result) {
        ShellH2QueryExplainTab tab = new ShellH2QueryExplainTab();
        tab.init(title, result);
        return tab;
    }
}
