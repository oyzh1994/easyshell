package cn.oyzh.easyshell.tabs.zk.query;

import cn.oyzh.easyshell.zk.ZKClient;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2025/01/20
 */
public class ZKQueryDataTab extends RichTab {

    public ZKQueryDataTab(String path, byte[] data, ZKClient zkClient) {
        super();
        super.flush();
        this.controller().init(path, data, zkClient);
    }

    @Override
    protected String url() {
        return "/tabs/query/zkQueryDataTab.fxml";
    }

    @Override
    protected ZKQueryDataTabController controller() {
        return (ZKQueryDataTabController) super.controller();
    }

    @Override
    public String getTabTitle() {
        return I18nHelper.data();
    }
}
