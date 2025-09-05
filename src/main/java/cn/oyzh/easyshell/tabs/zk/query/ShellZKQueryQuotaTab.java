package cn.oyzh.easyshell.tabs.zk.query;

import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.i18n.I18nHelper;
import org.apache.zookeeper.StatsTrack;

/**
 * @author oyzh
 * @since 2025/01/20
 */
public class ShellZKQueryQuotaTab extends RichTab {

    public ShellZKQueryQuotaTab(StatsTrack track) {
        super();
        super.flush();
        this.controller().init(track);
    }

    @Override
    protected String url() {
        return "/tabs/zk/query/shellZKQueryQuotaTab.fxml";
    }

    @Override
    protected ShellZKQueryQuotaTabController controller() {
        return (ShellZKQueryQuotaTabController) super.controller();
    }

    @Override
    public String getTabTitle() {
        return I18nHelper.quota();
    }

}
