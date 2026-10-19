package cn.oyzh.easyshell.tabs.mariadb.query;

import cn.oyzh.easyshell.query.mariadb.ShellMariadbExecuteResult;
import cn.oyzh.easyshell.trees.mariadb.database.ShellMariadbDatabaseTreeItem;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.FXConst;

/**
 * MariaDB 查询结果标签页
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbQuerySelectTab extends RichTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "mariadb/query/shellMariadbQuerySelectTab.fxml";
    }

    /**
     * 初始化
     *
     * @param title 标题
     * @param result 结果
     * @param dbItem 数据库树节点
     */
    public void init(String title, ShellMariadbExecuteResult result, ShellMariadbDatabaseTreeItem dbItem) {
        this.setTitle(title);
        this.controller().init(result, dbItem);
    }

    @Override
    public ShellMariadbQuerySelectTabController controller() {
        return (ShellMariadbQuerySelectTabController) super.controller();
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
    public static ShellMariadbQuerySelectTab of(String title, ShellMariadbExecuteResult result, ShellMariadbDatabaseTreeItem dbItem) {
        ShellMariadbQuerySelectTab tab = new ShellMariadbQuerySelectTab();
        tab.init(title, result, dbItem);
        return tab;
    }
}
