package cn.oyzh.easyshell.tabs.zk.query;

import cn.oyzh.easyshell.dto.zk.ShellZKEnvNode;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.i18n.I18nHelper;

import java.util.List;

/**
 * @author oyzh
 * @since 2025/01/20
 */
public class ShellZKQueryEnvTab extends RichTab {

    public ShellZKQueryEnvTab(List<ShellZKEnvNode> envNodes) {
        super();
        super.flush();
        this.controller().init(envNodes);
    }

    @Override
    protected String url() {
        return "/tabs/zk/query/shellZKQueryEnvTab.fxml";
    }

    @Override
    protected ShellZKQueryEnvTabController controller() {
        return (ShellZKQueryEnvTabController) super.controller();
    }

    @Override
    public String getTabTitle() {
        return I18nHelper.env();
    }

}
