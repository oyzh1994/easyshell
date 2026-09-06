package cn.oyzh.easyshell.tabs.zk.query;

import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.i18n.I18nHelper;

import java.util.List;

/**
 * @author oyzh
 * @since 2025/01/20
 */
public class ShellZKQueryNodeTab extends RichTab {

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

    public static ShellZKQueryNodeTab of(String path, List<String> nodes) {
        ShellZKQueryNodeTab tab = new ShellZKQueryNodeTab();
        tab.init(path, nodes);
        return tab;
    }
}
