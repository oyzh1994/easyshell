package cn.oyzh.easyshell.tabs.zk.server;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ExecutorUtil;
import cn.oyzh.easyshell.dto.zk.ShellZKEnvNode;
import cn.oyzh.easyshell.dto.zk.ShellZKServerInfo;
import cn.oyzh.easyshell.zk.ShellZKClient;
import cn.oyzh.fx.gui.tabs.ParentTabController;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.controls.table.FXTableColumn;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.fxml.FXML;

import java.util.List;
import java.util.concurrent.Future;


/**
 * zk服务信息业务
 *
 * @author oyzh
 * @since 2022/08/25
 */
public class ShellZKServerTabController extends ParentTabController {

    /**
     * 根节点
     */
    @FXML
    private FXTab root;

    public ShellZKClient getClient() {
        return client;
    }

    public void setClient(ShellZKClient client) {
        this.client = client;
    }

    /**
     * zk客户端
     */
    private ShellZKClient client;

    /**
     * 服务信息
     */
    @FXML
    private FXTableView<ShellZKServerInfo> serverTable;

    /**
     * 延迟信息
     */
    @FXML
    private FXTableColumn<ShellZKServerInfo, String> latency;

    /**
     * 命令信息
     */
    @FXML
    private FXTableColumn<ShellZKServerInfo, String> command;

    /**
     * 汇总信息
     */
    @FXML
    private ShellZKAggregationTabController aggregationController;

    /**
     * 服务信息
     */
    @FXML
    private ShellZKSrvrTabController srvrController;

    /**
     * 状态信息
     */
    @FXML
    private ShellZKStatTabController statController;

    /**
     * 本地信息
     */
    @FXML
    private ShellZKLocalTabController localController;

    /**
     * 配置信息
     */
    @FXML
    private ShellZKConfTabController confController;

    /**
     * 环境信息
     */
    @FXML
    private ShellZKEnviTabController enviController;

    /**
     * 集群信息
     */
    @FXML
    private ShellZKClusterTabController clusterController;

    /**
     * 刷新任务
     */
    private Future<?> refreshTask;

    /**
     * 设置zk客户端
     *
     * @param client zk客户端
     */
    public void init(ShellZKClient client) {
        this.client = client;
        // 设置信息
        String command = this.command.getText() + "(" + I18nHelper.received() + "/" + I18nHelper.sent() + "/" + I18nHelper.outstanding() + ")";
        this.command.setTextExt(command);
        String latency = this.latency.getText() + "(" + I18nHelper.min() + "/" + I18nHelper.avg() + "/" + I18nHelper.max() + ")" + I18nHelper.millisecond();
        this.latency.setTextExt(latency);
    }

    /**
     * 初始化自动刷新任务
     */
    private void initRefreshTask() {
        try {
            this.refreshTask = ExecutorUtil.start(this::renderPane, 0, 3_000);
            JulLog.debug("RefreshTask started.");
        } catch (Exception ex) {
            ex.printStackTrace();
            JulLog.error("initRefreshTask error", ex);
        }
    }

    /**
     * 关闭自动刷新任务
     */
    public void closeRefreshTask() {
        try {
            ExecutorUtil.cancel(this.refreshTask);
            JulLog.debug("RefreshTask closed.");
        } catch (Exception ex) {
            ex.printStackTrace();
            JulLog.error("closeRefreshTask error", ex);
        }
    }

    /**
     * 渲染主面板
     */
    private void renderPane() {
        try {
            JulLog.info("renderPane started.");
            if (this.client != null) {
                // 服务信息
                ShellZKServerInfo serverInfo;
                // 初始化
                if (this.serverTable.isItemEmpty()) {
                    serverInfo = new ShellZKServerInfo();
                    this.serverTable.setItem(serverInfo);
                } else {// 获取
                    serverInfo = (ShellZKServerInfo) this.serverTable.getItem(0);
                }
                // 获取信息
                List<ShellZKEnvNode> envNodes = this.client.srvrNodes();
                // 更新信息
                serverInfo.update(envNodes);
                // 初始化图表
                this.aggregationController.init(serverInfo);
            }
            JulLog.info("renderPane finished.");
        } catch (Exception ex) {
            ex.printStackTrace();
            JulLog.error("renderPane error", ex);
        }
    }

    @Override
    public void onTabInit(FXTab tab) {
        super.onTabInit(tab);
        // // 初始化刷新任务
        // this.initRefreshTask();
        this.root.selectedProperty().subscribe((oldValue, newValue) -> {
            if (newValue) {
                this.initRefreshTask();
            } else {
                this.closeRefreshTask();
            }
        });
    }

    @Override
    public void onTabClosed(Event event) {
        super.onTabClosed(event);
        this.closeRefreshTask();
    }

    @Override
    public List<? extends RichTabController> getSubControllers() {
        return List.of(this.aggregationController,
                this.localController,
                this.srvrController,
                this.statController,
                this.clusterController,
                this.confController,
                this.enviController);
    }

    @Override
    public void destroy() {
        this.serverTable.destroy();
        this.aggregationController.destroy();
        this.localController.destroy();
        this.srvrController.destroy();
        this.statController.destroy();
        this.clusterController.destroy();
        this.confController.destroy();
        this.enviController.destroy();
        this.closeRefreshTask();
        super.destroy();
    }
}