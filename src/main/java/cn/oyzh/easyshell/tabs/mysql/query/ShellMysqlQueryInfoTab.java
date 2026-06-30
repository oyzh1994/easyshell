package cn.oyzh.easyshell.tabs.mysql.query;

import cn.oyzh.easyshell.query.mysql.ShellMysqlQueryResults;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.FXConst;

/**
 * db查询信息tab
 *
 * @author oyzh
 * @since 2024/08/12
 */
public class ShellMysqlQueryInfoTab extends RichTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "mysql/query/shellMysqlQueryInfoTab.fxml";
    }

    public void init(ShellMysqlQueryResults<?> results) {
        this.controller().init(results);
    }

    @Override
    public ShellMysqlQueryInfoTabController controller() {
        return (ShellMysqlQueryInfoTabController) super.controller();
    }

    @Override
    public void initNode() {
        this.setClosable(false);
        super.initNode();
    }
}
