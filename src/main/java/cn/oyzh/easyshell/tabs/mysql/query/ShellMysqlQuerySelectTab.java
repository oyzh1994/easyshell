package cn.oyzh.easyshell.tabs.mysql.query;

import cn.oyzh.easyshell.query.mysql.ShellMysqlExecuteResult;
import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.FXConst;

/**
 * MySQL 查询结果标签页
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellMysqlQuerySelectTab extends RichTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "mysql/query/shellMysqlQuerySelectTab.fxml";
    }

    /**
     * 初始化
     *
     * @param title 标题
     * @param result 结果
     * @param dbItem 数据库树节点
     */
    public void init(String title, ShellMysqlExecuteResult result, ShellMysqlDatabaseTreeItem dbItem) {
        this.setTitle(title);
        this.controller().init(result, dbItem);
    }

    @Override
    public ShellMysqlQuerySelectTabController controller() {
        return (ShellMysqlQuerySelectTabController) super.controller();
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
    public static ShellMysqlQuerySelectTab of(String title, ShellMysqlExecuteResult result, ShellMysqlDatabaseTreeItem dbItem) {
        ShellMysqlQuerySelectTab tab = new ShellMysqlQuerySelectTab();
        tab.init(title, result, dbItem);
        return tab;
    }
}
