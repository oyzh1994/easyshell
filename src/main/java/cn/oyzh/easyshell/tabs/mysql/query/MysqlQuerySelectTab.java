package cn.oyzh.easyshell.tabs.mysql.query;

import cn.oyzh.easyshell.mysql.query.MysqlExecuteResult;
import cn.oyzh.easyshell.trees.mysql.database.MysqlDatabaseTreeItem;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.FXConst;

/**
 * db查询tab
 *
 * @author oyzh
 * @since 2024/08/12
 */
public class MysqlQuerySelectTab extends RichTab {

    {
        this.setClosable(false);
    }

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "mysql/query/shellMysqlQuerySelectTab.fxml";
    }

    public void init(String title, MysqlExecuteResult result, MysqlDatabaseTreeItem dbItem) {
        this.setTitle(title);
        this.controller().init(result, dbItem);
    }

    @Override
    public MysqlQuerySelectTabController controller() {
        return (MysqlQuerySelectTabController) super.controller();
    }
}
