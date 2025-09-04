package cn.oyzh.easyshell.tabs.zk.query;

import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.i18n.I18nHelper;
import org.apache.zookeeper.data.ClientInfo;

import java.util.List;

/**
 * @author oyzh
 * @since 2025/01/20
 */
public class ZKQueryWhoamiTab extends RichTab {

    public ZKQueryWhoamiTab(List<ClientInfo> clientInfos) {
        super();
        super.flush();
        this.controller().init(clientInfos);
    }

    @Override
    protected String url() {
        return "/tabs/zk/query/zkQueryWhoamiTab.fxml";
    }

    @Override
    protected ZKQueryWhoamiTabController controller() {
        return (ZKQueryWhoamiTabController) super.controller();
    }

    @Override
    public String getTabTitle() {
        return I18nHelper.authInfo();
    }

}
