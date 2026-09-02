package cn.oyzh.easyshell.tabs.dameng.query;

import cn.oyzh.easyshell.dameng.query.DamengExplainResult;
import cn.oyzh.easyshell.tabs.dameng.ShellDamengBaseTab;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.FXConst;

/**
 * db解释tab
 *
 * @author oyzh
 * @since 2024/08/16
 */
public class ShellDamengQueryExplainTab extends RichTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "query/damengQueryExplainTab.fxml";
    }

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
}
