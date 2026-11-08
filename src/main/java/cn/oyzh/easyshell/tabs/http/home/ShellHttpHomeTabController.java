package cn.oyzh.easyshell.tabs.http.home;

import cn.oyzh.easyshell.domain.http.ShellHttpEnvironment;
import cn.oyzh.easyshell.http.ShellHttpWorkspace;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import javafx.fxml.FXML;

/**
 * HTTP 工作区主页控制器。
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellHttpHomeTabController extends RichTabController {

    /**
     * 超时时间
     */
    @FXML
    private FXLabel timeout;

    /**
     * 集合数量
     */
    @FXML
    private FXLabel collections;

    /**
     * 请求数量
     */
    @FXML
    private FXLabel requests;

    /**
     * 环境数量
     */
    @FXML
    private FXLabel environments;

    /**
     * 当前环境
     */
    @FXML
    private FXLabel activeEnvironment;

    /**
     * 工作区
     */
    private ShellHttpWorkspace workspace;

    /**
     * 初始化
     *
     * @param workspace 工作区
     */
    public void init(ShellHttpWorkspace workspace) {
        this.workspace = workspace;
        this.refresh(workspace);
    }

    @Override
    public void onTabInit(FXTab tab) {
        super.onTabInit(tab);
        if (this.workspace != null) {
            this.refresh(this.workspace);
        }
    }

    /**
     * 刷新信息
     *
     * @param workspace 工作区
     */
    public void refresh(ShellHttpWorkspace workspace) {
        if (workspace == null) {
            return;
        }
        this.workspace = workspace;
        this.timeout.text(workspace.getSettings().getTimeoutSeconds() + " "
                + ShellI18nHelper.httpSeconds());
        this.collections.text(String.valueOf(workspace.getCollections().size()));
        this.requests.text(String.valueOf(workspace.requestCount()));
        this.environments.text(String.valueOf(workspace.getEnvironments().size()));
        ShellHttpEnvironment active = workspace.activeEnvironment();
        this.activeEnvironment.text(active == null
                ? ShellI18nHelper.httpNoEnvironment() : active.getName());
    }
}
