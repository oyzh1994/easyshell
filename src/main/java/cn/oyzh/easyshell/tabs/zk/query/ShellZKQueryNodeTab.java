package cn.oyzh.easyshell.tabs.zk.query;

import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.i18n.I18nHelper;

import java.util.List;

/**
 * zk查询节点标签页
 *
 * @author oyzh
 * @since 2025-09-04
 */
public class ShellZKQueryNodeTab extends RichTab {

    /**
     * 初始化节点数据
     *
     * @param path  父节点路径
     * @param nodes 子节点列表
     */
    public void init(String path, List<String> nodes) {
        super.flush();
        this.controller().init(path, nodes);
    }

    @Override
    protected String url() {
        return "/tabs/zk/query/shellZKQueryNodeTab.fxml";
    }

    @Override
    protected ShellZKQueryNodeTabController controller() {
        return (ShellZKQueryNodeTabController) super.controller();
    }

    @Override
    public String getTabTitle() {
        return I18nHelper.node();
    }

    @Override
    public void initNode() {
        this.setClosable(false);
        super.initNode();
    }

    /**
     * 创建zk查询节点标签页
     *
     * @param path  父节点路径
     * @param nodes 子节点列表
     * @return zk查询节点标签页
     */
    public static ShellZKQueryNodeTab of(String path, List<String> nodes) {
        ShellZKQueryNodeTab tab = new ShellZKQueryNodeTab();
        tab.init(path, nodes);
        return tab;
    }
}
