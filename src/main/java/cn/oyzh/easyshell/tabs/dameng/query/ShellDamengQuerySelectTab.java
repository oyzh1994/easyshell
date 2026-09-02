package cn.oyzh.easyshell.tabs.dameng.query;

import cn.oyzh.easyshell.dameng.query.DamengExecuteResult;
import cn.oyzh.easyshell.tabs.dameng.ShellDamengBaseTab;
import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.FXConst;

/**
 * db查询tab
 *
 * @author oyzh
 * @since 2024/08/12
 */
public class ShellDamengQuerySelectTab extends RichTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "dameng/query/shellDamengQuerySelectTab.fxml";
    }

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
}
