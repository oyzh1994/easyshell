package cn.oyzh.easyshell.tabs.redis.server;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ExecutorUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.dto.redis.RedisInfoProp;
import cn.oyzh.easyshell.dto.redis.RedisServerItem;
import cn.oyzh.easyshell.redis.ShellRedisClient;
import cn.oyzh.fx.gui.tabs.ParentTabController;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import javafx.event.Event;
import javafx.fxml.FXML;

import java.util.List;
import java.util.concurrent.Future;

/**
 * redis终端tab内容组件
 *
 * @author oyzh
 * @since 2023/08/01
 */
public class ShellRedisServerTabController extends ParentTabController {

    /**
     * 根节点
     */
    @FXML
    private FXTab root;

    /**
     * redis客户端
     */
    private ShellRedisClient client;

    public ShellRedisClient getClient() {
        return client;
    }

    /**
     * 发布及订阅tab
     */
    @FXML
    private FXTab pubsub;

    /**
     * 慢查日志tab
     */
    @FXML
    private FXTab slowlog;

    /**
     * 客户端信息tab
     */
    @FXML
    private FXTab clientInfo;

    /**
     * tab面板
     */
    @FXML
    private FXTabPane tabPane;

    /**
     * 订阅组件
     */
    @FXML
    private ShellRedisPubsubTabController pubsubController;

    /**
     * 慢查日志组件
     */
    @FXML
    private ShellRedisSlowlogTabController slowlogController;

    /**
     * 服务信息组件
     */
    @FXML
    private ShellRedisServerInfoTabController serverInfoController;

    /**
     * 客户端信息组件
     */
    @FXML
    private ShellRedisClientInfoTabController clientInfoController;

    /**
     * 汇总组件
     */
    @FXML
    private ShellRedisAggregationTabController aggregationController;

    /**
     * 属性表格
     */
    @FXML
    private FXTableView<RedisServerItem> propTable;

    /**
     * 刷新任务
     */
    private Future<?> refreshTask;

    /**
     * 设置redis客户端
     *
     * @param client redis客户端
     */
    public void init(ShellRedisClient client) {
        this.client = client;
        if (!client.isSentinelMode()) {
            this.pubsubController.init(client);
            this.slowlogController.init(client);
            this.clientInfoController.init(client);
        } else {
            this.tabPane.removeTab(this.pubsub);
            this.tabPane.removeTab(this.slowlog);
            this.tabPane.removeTab(this.clientInfo);
        }
//        this.renderPane();
//        this.initRefreshTask();
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
    private synchronized void renderPane() {
        try {
            JulLog.info("renderPane started.");
            if (this.client != null) {
                RedisInfoProp infoProp = new RedisInfoProp();
                infoProp.parse(this.client.info(null));
                RedisServerItem serverItem;
                if (this.propTable.isItemEmpty()) {
                    serverItem = new RedisServerItem();
                    serverItem.setServerVersion(infoProp.getRedisVersion());
                    try {
                        serverItem.setRole((String) CollectionUtil.getFirst(this.client.role()));
                    } catch (Exception ignored) {
                    }
                    this.propTable.addItem(serverItem);
                } else {
                    serverItem = (RedisServerItem) this.propTable.getItem(0);
                }
                serverItem.init(infoProp);
                this.serverInfoController.init(infoProp);
                this.aggregationController.init(infoProp);
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
        this.root.selectedProperty().addListener((observable, oldValue, newValue) -> {
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
                this.pubsubController,
                this.slowlogController,
                this.serverInfoController,
                this.clientInfoController);
    }
}