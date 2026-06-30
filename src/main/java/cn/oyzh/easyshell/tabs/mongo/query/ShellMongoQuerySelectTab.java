package cn.oyzh.easyshell.tabs.mongo.query;

import cn.oyzh.easyshell.query.mongo.ShellMongoExecuteResult;
import cn.oyzh.easyshell.trees.mongo.database.ShellMongoDatabaseTreeItem;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.FXConst;

/**
 * mongodb查询tab
 *
 * @author oyzh
 * @since 2024/08/12
 */
public class ShellMongoQuerySelectTab extends RichTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "mongo/query/shellMongoQuerySelectTab.fxml";
    }

    public void init(String title, ShellMongoExecuteResult result, ShellMongoDatabaseTreeItem dbItem) {
        this.setTitle(title);
        this.controller().init(result, dbItem);
    }

    @Override
    public ShellMongoQuerySelectTabController controller() {
        return (ShellMongoQuerySelectTabController) super.controller();
    }

    @Override
    public void initNode() {
        this.setClosable(false);
        super.initNode();
    }
}
