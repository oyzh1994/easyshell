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

    /**
     * 初始化
     *
     * @param title 标题
     * @param result 结果
     * @param dbItem 数据库树节点
     */
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

    /**
     * 创建实例
     *
     * @param title 标题
     * @param result 结果
     * @param dbItem 数据库树节点
     * @return 实例对象
     */
    public static ShellMongoQuerySelectTab of(String title, ShellMongoExecuteResult result, ShellMongoDatabaseTreeItem dbItem) {
        ShellMongoQuerySelectTab tab = new ShellMongoQuerySelectTab();
        tab.init(title, result, dbItem);
        return tab;
    }
}
