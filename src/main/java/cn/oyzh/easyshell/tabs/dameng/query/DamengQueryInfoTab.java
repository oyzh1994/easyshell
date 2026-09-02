package cn.oyzh.easyshell.tabs.dameng.query;

import cn.oyzh.easyshell.tabs.dameng.DamengTab;
import cn.oyzh.easyshell.tabs.dameng.query.DamengQueryInfoTabController;
import cn.oyzh.fx.db.query.DBQueryResults;
import cn.oyzh.fx.gui.tabs.RichTab;

/**
 * db查询信息tab
 *
 * @author oyzh
 * @since 2024/08/12
 */
public class DamengQueryInfoTab extends RichTab {

    @Override
    protected String url() {
        return DamengTab.BASE_PATH + "query/damengQueryInfoTab.fxml";
    }

    public void init(DBQueryResults<?> results) {
        this.controller().init(results);
    }

    @Override
    public DamengQueryInfoTabController controller() {
        return (DamengQueryInfoTabController) super.controller();
    }

    @Override
    public void initNode() {
        this.setClosable(false);
        super.initNode();
    }
}
