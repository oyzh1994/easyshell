package cn.oyzh.easyshell.tabs.mongo.query;

import cn.oyzh.easyshell.query.mongo.ShellMongoQueryResults;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.FXConst;

/**
 * db查询信息tab
 *
 * @author oyzh
 * @since 2024/08/12
 */
public class MongoQueryInfoTab extends RichTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "mongo/query/mongoQueryInfoTab.fxml";
    }

    public void init(ShellMongoQueryResults<?> results) {
        this.controller().init(results);
    }

    @Override
    public MongoQueryInfoTabController controller() {
        return (MongoQueryInfoTabController) super.controller();
    }

    @Override
    public void initNode() {
        this.setClosable(false);
        super.initNode();
    }
}
