package cn.oyzh.easyshell.tabs.zk.query;

import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.i18n.I18nHelper;
import org.apache.zookeeper.data.ACL;

import java.util.List;

/**
 * zk查询acl标签页
 *
 * @author oyzh
 * @since 2025/01/21
 */
public class ShellZKQueryACLTab extends RichTab {

    //public ShellZKQueryACLTab(List<ACL> aclList) {
    //    super();
    //    super.flush();
    //    this.controller().init(aclList);
    //}

    /**
     * 初始化acl数据
     *
     * @param aclList acl列表
     */
    public void init(List<ACL> aclList) {
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

    @Override
    public void initNode() {
        this.setClosable(false);
        super.initNode();
    }

    /**
     * 创建zk查询acl标签页
     *
     * @param aclList acl列表
     * @return zk查询acl标签页
     */
    public static ShellZKQueryACLTab of(List<ACL> aclList) {
        ShellZKQueryACLTab tab = new ShellZKQueryACLTab();
        tab.init(aclList);
        return tab;
    }
}
