package cn.oyzh.easyshell.mongo.tabs.query;

import cn.oyzh.easyshell.mongo.query.ShellMongoExecuteResult;
import cn.oyzh.easyshell.mongo.trees.database.MongoDatabaseTreeItem;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.FXConst;

/**
 * db查询tab
 *
 * @author oyzh
 * @since 2024/08/12
 */
public class MongoQuerySelectTab extends RichTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "mongo/query/mongoQuerySelectTab.fxml";
    }

    public void init(String title, ShellMongoExecuteResult result, MongoDatabaseTreeItem dbItem) {
        this.setTitle(title);
        this.controller().init(result, dbItem);
    }

    @Override
    public MongoQuerySelectTabController controller() {
        return (MongoQuerySelectTabController) super.controller();
    }

    @Override
    public void initNode() {
        this.setClosable(false);
        super.initNode();
    }
}
