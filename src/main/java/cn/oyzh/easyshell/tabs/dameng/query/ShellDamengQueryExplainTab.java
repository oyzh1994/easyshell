package cn.oyzh.easyshell.tabs.dameng.query;

import cn.oyzh.easyshell.query.dameng.DamengExplainResult;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.FXConst;

/**
 * 达梦SQL解释结果标签页，用于展示执行计划与耗时等信息
 *
 * @author oyzh
 * @since 2024/08/16
 */
public class ShellDamengQueryExplainTab extends RichTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "dameng/query/shellDamengQueryExplainTab.fxml";
    }

    /**
     * 初始化
     *
     * @param title 标题
     * @param result 结果
     */
    public void init(String title, DamengExplainResult result) {
        this.setTitle(title);
        this.controller().init(result);
    }

    @Override
    public ShellDamengQueryExplainTabController controller() {
        return (ShellDamengQueryExplainTabController) super.controller();
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
    public static ShellDamengQueryExplainTab of(String title, DamengExplainResult result) {
        ShellDamengQueryExplainTab tab = new ShellDamengQueryExplainTab();
        tab.init(title, result);
        return tab;
    }
}
