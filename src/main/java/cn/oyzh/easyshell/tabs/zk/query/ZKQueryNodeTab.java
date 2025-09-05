package cn.oyzh.easyshell.tabs.zk.query;

import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.i18n.I18nHelper;

import java.util.List;

/**
 * @author oyzh
 * @since 2025/01/20
 */
public class ZKQueryNodeTab extends RichTab {

    public ZKQueryNodeTab(String path, List<String> nodes) {
        super();
        super.flush();
        this.controller().init(path, nodes);
    }

    @Override
    protected String url() {
        return "/tabs/zk/query/shellZKQueryNodeTab.fxml";
    }

    @Override
    protected ZKQueryNodeTabController controller() {
        return (ZKQueryNodeTabController) super.controller();
    }

    @Override
    public String getTabTitle() {
        return I18nHelper.node();
    }

}
