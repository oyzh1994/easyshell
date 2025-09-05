package cn.oyzh.easyshell.tabs.zk.query;

import cn.oyzh.easyshell.query.zk.ShellZKQueryParam;
import cn.oyzh.easyshell.query.zk.ShellZKQueryResult;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2025/01/20
 */
public class ZKQueryMsgTab extends RichTab {

    public ZKQueryMsgTab(ShellZKQueryParam param, ShellZKQueryResult result) {
        super();
        super.flush();
        this.controller().init(param, result);
    }

    @Override
    protected String url() {
        return "/tabs/zk/query/zkQueryMsgTab.fxml";
    }

    @Override
    protected ZKQueryMsgTabController controller() {
        return (ZKQueryMsgTabController) super.controller();
    }

    @Override
    public String getTabTitle() {
        return I18nHelper.message();
    }


}
