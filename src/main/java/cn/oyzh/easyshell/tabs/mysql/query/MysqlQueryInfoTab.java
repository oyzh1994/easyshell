package cn.oyzh.easyshell.tabs.mysql.query;

import cn.oyzh.easyshell.mysql.query.MysqlQueryResults;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.FXConst;

/**
 * db查询信息tab
 *
 * @author oyzh
 * @since 2024/08/12
 */
public class MysqlQueryInfoTab extends RichTab {

    {
        this.setClosable(false);
    }

    @Override
    protected String url() {
        return FXConst.FXML_PATH + "mysql/query/shellMysqlQueryInfoTab.fxml";
    }

    public void init(MysqlQueryResults<?> results) {
        this.controller().init(results);
    }

    @Override
    public MysqlQueryInfoTabController controller() {
        return (MysqlQueryInfoTabController) super.controller();
    }
}
