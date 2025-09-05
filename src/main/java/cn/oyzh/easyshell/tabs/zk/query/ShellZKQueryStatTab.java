package cn.oyzh.easyshell.tabs.zk.query;

import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.i18n.I18nHelper;
import org.apache.zookeeper.data.Stat;

/**
 * @author oyzh
 * @since 2025/01/20
 */
public class ShellZKQueryStatTab extends RichTab {

    public ShellZKQueryStatTab(Stat stat) {
        super();
        super.flush();
        this.controller().init(stat);
    }

    @Override
    protected String url() {
        return "/tabs/zk/query/shellZKQueryStatTab.fxml";
    }

    @Override
    protected ShellZKQueryStatTabController controller() {
        return (ShellZKQueryStatTabController) super.controller();
    }

    @Override
    public String getTabTitle() {
        return I18nHelper.stat();
    }

}
