package cn.oyzh.easyshell.tabs.dameng.query;

import cn.oyzh.easyshell.tabs.dameng.ShellDamengBaseTab;
import cn.oyzh.fx.db.query.DBQueryResults;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.FXConst;

/**
 * db查询信息tab
 *
 * @author oyzh
 * @since 2024/08/12
 */
public class ShellDamengQueryInfoTab extends RichTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "query/damengQueryInfoTab.fxml";
    }

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
}
