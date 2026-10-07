package cn.oyzh.easyshell.tabs.zk.query;

import cn.oyzh.easyshell.zk.ShellZKClient;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.i18n.I18nHelper;

/**
 * zk查询数据标签页
 *
 * @author oyzh
 * @since 2025-09-04
 */
public class ShellZKQueryDataTab extends RichTab {

    //public ShellZKQueryDataTab(String path, byte[] data, ShellZKClient zkClient) {
    //    super();
    //    super.flush();
    //    this.controller().init(path, data, zkClient);
    //}

    /**
     * 初始化数据
     *
     * @param path     节点路径
     * @param data     节点数据
     * @param zkClient zk客户端
     */
    public void init(String path, byte[] data, ShellZKClient zkClient) {
        super.flush();
        this.controller().init(path, data, zkClient);
    }

    @Override
    protected String url() {
        return "/tabs/zk/query/shellZKQueryDataTab.fxml";
    }

    @Override
    protected ShellZKQueryDataTabController controller() {
        return (ShellZKQueryDataTabController) super.controller();
    }

    @Override
    public String getTabTitle() {
        return I18nHelper.data();
    }

    @Override
    public void initNode() {
        this.setClosable(false);
        super.initNode();
    }

    /**
     * 创建zk查询数据标签页
     *
     * @param path     节点路径
     * @param data     节点数据
     * @param zkClient zk客户端
     * @return zk查询数据标签页
     */
    public static ShellZKQueryDataTab of(String path, byte[] data, ShellZKClient zkClient) {
        ShellZKQueryDataTab tab = new ShellZKQueryDataTab();
        tab.init(path, data, zkClient);
        return tab;
    }
}
