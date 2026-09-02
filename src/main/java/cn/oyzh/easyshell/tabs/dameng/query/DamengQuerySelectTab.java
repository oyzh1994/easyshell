package cn.oyzh.easyshell.tabs.dameng.query;

import cn.oyzh.easyshell.dameng.query.DamengExecuteResult;
import cn.oyzh.easyshell.tabs.dameng.DamengTab;
import cn.oyzh.easyshell.tabs.dameng.query.DamengQuerySelectTabController;
import cn.oyzh.easyshell.trees.dameng.schema.DamengSchemaTreeItem;
import cn.oyzh.fx.gui.tabs.RichTab;

/**
 * db查询tab
 *
 * @author oyzh
 * @since 2024/08/12
 */
public class DamengQuerySelectTab extends RichTab {

    @Override
    protected String url() {
        return DamengTab.BASE_PATH + "query/damengQuerySelectTab.fxml";
    }

    public void init(String title, DamengExecuteResult result, DamengSchemaTreeItem dbItem) {
        this.setTitle(title);
        this.controller().init(result, dbItem);
    }

    @Override
    public DamengQuerySelectTabController controller() {
        return (DamengQuerySelectTabController) super.controller();
    }

    @Override
    public void initNode() {
        this.setClosable(false);
        super.initNode();
    }
}
