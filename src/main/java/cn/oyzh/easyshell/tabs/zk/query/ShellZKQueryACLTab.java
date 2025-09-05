package cn.oyzh.easyshell.tabs.zk.query;

import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.i18n.I18nHelper;
import org.apache.zookeeper.data.ACL;

import java.util.List;

/**
 * @author oyzh
 * @since 2025/01/21
 */
public class ShellZKQueryACLTab extends RichTab {

    public ShellZKQueryACLTab(List<ACL> aclList) {
        super();
        super.flush();
        this.controller().init(aclList);
    }

    @Override
    protected String url() {
        return "/tabs/zk/query/shellZKQueryACLTab.fxml";
    }

    @Override
    protected ShellZKQueryACLTabController controller() {
        return (ShellZKQueryACLTabController) super.controller();
    }

    @Override
    public String getTabTitle() {
        return I18nHelper.acl();
    }

}
