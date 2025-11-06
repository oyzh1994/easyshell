package cn.oyzh.easyshell.tabs.mysql.query;

import cn.oyzh.easyshell.mysql.query.MysqlExplainResult;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.FXConst;

/**
 * db解释tab
 *
 * @author oyzh
 * @since 2024/08/16
 */
public class MysqlQueryExplainTab extends RichTab {

    {
        this.setClosable(false);
    }

    @Override
    protected String url() {
        return FXConst.FXML_PATH + "mysql/query/shellMysqlQueryExplainTab.fxml";
    }

    public void init(String title, MysqlExplainResult result) {
        this.setTitle(title);
        this.controller().init(result);
    }

    @Override
    public MysqlQueryExplainTabController controller() {
        return (MysqlQueryExplainTabController) super.controller();
    }

}
