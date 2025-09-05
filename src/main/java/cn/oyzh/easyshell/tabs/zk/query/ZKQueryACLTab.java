package cn.oyzh.easyshell.tabs.zk.query;

import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.i18n.I18nHelper;
import org.apache.zookeeper.data.ACL;

import java.util.List;

/**
 * @author oyzh
 * @since 2025/01/21
 */
public class ZKQueryACLTab extends RichTab {

    public ZKQueryACLTab(List<ACL> aclList) {
        super();
        super.flush();
        this.controller().init(aclList);
    }

    @Override
    protected String url() {
        return "/tabs/zk/query/shellZKQueryACLTab.fxml";
    }

    @Override
    protected ZKQueryACLTabController controller() {
        return (ZKQueryACLTabController) super.controller();
    }

    @Override
    public String getTabTitle() {
        return I18nHelper.acl();
    }

}
