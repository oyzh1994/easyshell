package cn.oyzh.easyshell.tabs.zk.query;

import cn.oyzh.easyshell.dto.zk.ZKEnvNode;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.i18n.I18nHelper;

import java.util.List;

/**
 * @author oyzh
 * @since 2025/01/20
 */
public class ZKQueryEnvTab extends RichTab {

    public ZKQueryEnvTab(List<ZKEnvNode> envNodes) {
        super();
        super.flush();
        this.controller().init(envNodes);
    }

    @Override
    protected String url() {
        return "/tabs/zk/query/shellZKQueryEnvTab.fxml";
    }

    @Override
    protected ZKQueryEnvTabController controller() {
        return (ZKQueryEnvTabController) super.controller();
    }

    @Override
    public String getTabTitle() {
        return I18nHelper.env();
    }

}
