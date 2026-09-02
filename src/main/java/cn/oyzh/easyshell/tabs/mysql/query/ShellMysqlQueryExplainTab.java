package cn.oyzh.easyshell.tabs.mysql.query;

import cn.oyzh.easyshell.mysql.query.ShellMysqlExplainResult;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.FXConst;

/**
 * db解释tab
 *
 * @author oyzh
 * @since 2024/08/16
 */
public class ShellMysqlQueryExplainTab extends RichTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "mysql/query/shellMysqlQueryExplainTab.fxml";
    }

    public void init(String title, ShellMysqlExplainResult result) {
        this.setTitle(title);
        this.controller().init(result);
    }

    @Override
    public ShellMysqlQueryExplainTabController controller() {
        return (ShellMysqlQueryExplainTabController) super.controller();
    }

    @Override
    public void initNode() {
        this.setClosable(false);
        super.initNode();
    }
}
