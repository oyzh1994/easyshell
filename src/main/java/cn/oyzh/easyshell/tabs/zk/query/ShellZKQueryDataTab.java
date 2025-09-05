package cn.oyzh.easyshell.tabs.zk.query;

import cn.oyzh.easyshell.zk.ShellZKClient;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2025/01/20
 */
public class ShellZKQueryDataTab extends RichTab {

    public ShellZKQueryDataTab(String path, byte[] data, ShellZKClient zkClient) {
        super();
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
}
