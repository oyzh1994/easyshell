package cn.oyzh.easyshell.tabs.zk.query;

import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.i18n.I18nHelper;
import org.apache.zookeeper.data.ClientInfo;

import java.util.List;

/**
 * @author oyzh
 * @since 2025/01/20
 */
public class ShellZKQueryWhoamiTab extends RichTab {

    public void init(List<ClientInfo> clientInfos) {
        super.flush();
        this.controller().init(clientInfos);
    }

    @Override
    protected String url() {
        return "/tabs/zk/query/shellZKQueryWhoamiTab.fxml";
    }

    @Override
    protected ShellZKQueryWhoamiTabController controller() {
        return (ShellZKQueryWhoamiTabController) super.controller();
    }

    @Override
    public String getTabTitle() {
        return I18nHelper.authInfo();
    }

    @Override
    public void initNode() {
        this.setClosable(false);
        super.initNode();
    }

    public static ShellZKQueryWhoamiTab of(List<ClientInfo> clientInfos) {
        ShellZKQueryWhoamiTab tab = new ShellZKQueryWhoamiTab();
        tab.init(clientInfos);
        return tab;
    }
}
