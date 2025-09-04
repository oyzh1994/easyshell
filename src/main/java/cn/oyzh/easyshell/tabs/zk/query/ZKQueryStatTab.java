package cn.oyzh.easyshell.tabs.zk.query;

import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.i18n.I18nHelper;
import org.apache.zookeeper.data.Stat;

/**
 * @author oyzh
 * @since 2025/01/20
 */
public class ZKQueryStatTab extends RichTab {

    public ZKQueryStatTab(Stat stat) {
        super();
        super.flush();
        this.controller().init(stat);
    }

    @Override
    protected String url() {
        return "/tabs/zk/query/zkQueryStatTab.fxml";
    }

    @Override
    protected ZKQueryStatTabController controller() {
        return (ZKQueryStatTabController) super.controller();
    }

    @Override
    public String getTabTitle() {
        return I18nHelper.stat();
    }

}
