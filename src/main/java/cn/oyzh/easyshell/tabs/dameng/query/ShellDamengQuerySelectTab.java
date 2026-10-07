package cn.oyzh.easyshell.tabs.dameng.query;

import cn.oyzh.easyshell.query.dameng.DamengExecuteResult;
import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.FXConst;

/**
 * 达梦查询结果标签页，用于展示查询执行结果
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellDamengQuerySelectTab extends RichTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "dameng/query/shellDamengQuerySelectTab.fxml";
    }

    /**
     * 初始化
     *
     * @param title 标题
     * @param result 结果
     * @param dbItem 数据库树节点
     */
    public void init(String title, DamengExecuteResult result, ShellDamengSchemaTreeItem dbItem) {
        this.setTitle(title);
        this.controller().init(result, dbItem);
    }

    @Override
    public ShellDamengQuerySelectTabController controller() {
        return (ShellDamengQuerySelectTabController) super.controller();
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
     * @param dbItem 数据库树节点
     * @return 实例对象
     */
    public static ShellDamengQuerySelectTab of(String title, DamengExecuteResult result, ShellDamengSchemaTreeItem dbItem) {
        ShellDamengQuerySelectTab tab = new ShellDamengQuerySelectTab();
        tab.init(title, result, dbItem);
        return tab;
    }
}
