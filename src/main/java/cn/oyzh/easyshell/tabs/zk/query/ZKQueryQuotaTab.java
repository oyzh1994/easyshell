package cn.oyzh.easyshell.tabs.zk.query;

import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.i18n.I18nHelper;
import org.apache.zookeeper.StatsTrack;

/**
 * @author oyzh
 * @since 2025/01/20
 */
public class ZKQueryQuotaTab extends RichTab {

    public ZKQueryQuotaTab(StatsTrack track) {
        super();
        super.flush();
        this.controller().init(track);
    }

    @Override
    protected String url() {
        return "/tabs/zk/query/zkQueryQuotaTab.fxml";
    }

    @Override
    protected ZKQueryQuotaTabController controller() {
        return (ZKQueryQuotaTabController) super.controller();
    }

    @Override
    public String getTabTitle() {
        return I18nHelper.quota();
    }

}
