package cn.oyzh.easyshell.tabs.http;

import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.http.ShellHttpClient;
import cn.oyzh.easyshell.domain.http.ShellHttpCollection;
import cn.oyzh.easyshell.domain.http.ShellHttpEnvironment;
import cn.oyzh.easyshell.domain.http.ShellHttpRequest;
import cn.oyzh.easyshell.http.ShellHttpWorkspace;
import cn.oyzh.easyshell.tabs.http.environment.ShellHttpEnvironmentTab;
import cn.oyzh.easyshell.tabs.http.home.ShellHttpHomeTab;
import cn.oyzh.easyshell.tabs.http.request.ShellHttpRequestTab;
import cn.oyzh.easyshell.tabs.http.settings.ShellHttpSettingsTab;
import cn.oyzh.easyshell.trees.http.environment.ShellHttpEnvironmentTreeItem;
import cn.oyzh.easyshell.trees.http.request.ShellHttpRequestTreeItem;
import cn.oyzh.fx.gui.tabs.RichTabPane;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Tab;

import java.util.ArrayList;
import java.util.List;

/**
 * HTTP 工作区标签页面板，负责请求、环境和设置标签页的打开与去重。
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellHttpTabPane extends RichTabPane {

    /**
     * 客户端属性
     */
    private SimpleObjectProperty<ShellHttpClient> clientProperty;

    /**
     * 工作区
     */
    private ShellHttpWorkspace workspace;

    /**
     * 父控制器
     */
    private ShellHttpTabController tabController;

    /**
     * 是否正在关闭
     */
    private boolean closing;

    @Override
    public void initNode() {
        super.initNode();
        this.getTabs().addListener((ListChangeListener<? super Tab>) change -> {
            while (change.next()) {
                if (change.wasAdded() || change.wasRemoved()) {
                    TaskManager.startDelay(() -> FXUtil.runLater(this::flushHomeTab), 100);
                }
            }
        });
    }

    /**
     * 设置客户端
     *
     * @param client 客户端
     */
    public void setClient(ShellHttpClient client) {
        this.clientProperty().set(client);
    }

    /**
     * 获取客户端
     *
     * @return 客户端
     */
    public ShellHttpClient getClient() {
        return this.clientProperty == null ? null : this.clientProperty.get();
    }

    /**
     * 客户端属性
     *
     * @return 结果
     */
    public SimpleObjectProperty<ShellHttpClient> clientProperty() {
        if (this.clientProperty == null) {
            this.clientProperty = new SimpleObjectProperty<>();
        }
        return this.clientProperty;
    }

    /**
     * 设置工作区
     *
     * @param workspace 工作区
     */
    public void setWorkspace(ShellHttpWorkspace workspace) {
        this.workspace = workspace;
    }

    /**
     * 获取工作区
     *
     * @return 工作区
     */
    public ShellHttpWorkspace getWorkspace() {
        return workspace;
    }

    /**
     * 设置父控制器
     *
     * @param parent 父控制器
     */
    public void setTabController(ShellHttpTabController tabController) {
        this.tabController = tabController;
    }

    /**
     * 获取父控制器
     *
     * @return 父控制器
     */
    public ShellHttpTabController tabController() {
        return tabController;
    }

    /**
     * 刷新主页标签
     */
    private void flushHomeTab() {
        if (this.closing) {
            return;
        }
        if (this.getTabs().isEmpty()) {
            this.initHomeTab();
        } else if (this.getTabs().size() > 1) {
            this.closeHomeTab();
        }
    }

    /**
     * 获取主页标签
     *
     * @return 主页标签
     */
    public ShellHttpHomeTab getHomeTab() {
        return super.getTab(ShellHttpHomeTab.class);
    }

    /**
     * 初始化主页标签
     */
    public void initHomeTab() {
        if (this.getHomeTab() == null && this.workspace != null) {
            ShellHttpHomeTab tab = new ShellHttpHomeTab();
            this.addTab(tab);
            tab.init(this.workspace);
        }
    }

    /**
     * 关闭主页标签
     */
    public void closeHomeTab() {
        ShellHttpHomeTab tab = this.getHomeTab();
        if (tab != null) {
            tab.setClosable(true);
            tab.closeTab();
            tab.setClosable(false);
        }
    }

    /**
     * 刷新主页信息
     */
    public void refreshHomeTab() {
        ShellHttpHomeTab tab = this.getHomeTab();
        if (tab != null) {
            tab.refresh(this.workspace);
        }
    }

    /**
     * 关闭全部标签页
     */
    public void closeAllTabs() {
        this.closing = true;
        List<Tab> tabs = new ArrayList<>(this.getTabs());
        for (Tab tab : tabs) {
            if (tab instanceof FXTab fxTab) {
                fxTab.closeTab();
            } else {
                this.removeTab(tab);
            }
        }
        this.closing = false;
    }

    /**
     * 打开请求标签页
     *
     * @param item 请求节点
     */
    public void openRequest(ShellHttpRequestTreeItem item) {
        ShellHttpRequestTab tab = this.getRequestTab(item.request().getId());
        if (tab == null) {
            tab = new ShellHttpRequestTab();
            this.addTab(tab);
            tab.init(item, this);
        }
        this.select(tab);
    }

    /**
     * 获取请求标签页
     *
     * @param requestId 请求标识
     * @return 请求标签页
     */
    private ShellHttpRequestTab getRequestTab(String requestId) {
        if (StringUtil.isBlank(requestId)) {
            return null;
        }
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellHttpRequestTab requestTab
                    && requestId.equals(requestTab.requestId())) {
                return requestTab;
            }
        }
        return null;
    }

    /**
     * 重命名请求标签页
     *
     * @param requestId 请求标识
     * @param name      名称
     */
    public void renameRequestTab(String requestId, String name) {
        ShellHttpRequestTab tab = this.getRequestTab(requestId);
        if (tab != null) {
            tab.rename(name);
        }
    }

    /**
     * 关闭请求标签页
     *
     * @param requestId 请求标识
     */
    public void closeRequestTab(String requestId) {
        ShellHttpRequestTab tab = this.getRequestTab(requestId);
        if (tab != null) {
            tab.closeTab();
        }
    }

    /**
     * 关闭集合下的全部请求标签页
     *
     * @param collection 集合
     */
    public void closeCollectionTabs(ShellHttpCollection collection) {
        if (collection == null) {
            return;
        }
        for (ShellHttpRequest request : collection.getRequests()) {
            this.closeRequestTab(request.getId());
        }
    }


    /**
     * 打开环境标签页
     *
     * @param item 环境节点
     */
    public void openEnvironment(ShellHttpEnvironmentTreeItem item) {
        ShellHttpEnvironment environment = item.environment();
        ShellHttpEnvironmentTab tab = this.getEnvironmentTab(environment.getId());
        if (tab == null) {
            tab = new ShellHttpEnvironmentTab();
            this.addTab(tab);
            tab.init(item, this);
        }
        this.select(tab);
    }

    /**
     * 获取环境标签页
     *
     * @param environmentId 环境标识
     * @return 环境标签页
     */
    private ShellHttpEnvironmentTab getEnvironmentTab(String environmentId) {
        if (StringUtil.isBlank(environmentId)) {
            return null;
        }
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellHttpEnvironmentTab environmentTab
                    && environmentId.equals(environmentTab.environmentId())) {
                return environmentTab;
            }
        }
        return null;
    }

    /**
     * 重命名环境标签页
     *
     * @param environmentId 环境标识
     * @param name          名称
     */
    public void renameEnvironmentTab(String environmentId, String name) {
        ShellHttpEnvironmentTab tab = this.getEnvironmentTab(environmentId);
        if (tab != null) {
            tab.rename(name);
        }
    }

    /**
     * 关闭环境标签页
     *
     * @param environmentId 环境标识
     */
    public void closeEnvironmentTab(String environmentId) {
        ShellHttpEnvironmentTab tab = this.getEnvironmentTab(environmentId);
        if (tab != null) {
            tab.closeTab();
        }
    }

    /**
     * 打开设置标签页
     */
    public void openSettings() {
        ShellHttpSettingsTab tab = this.getTab(ShellHttpSettingsTab.class);
        if (tab == null) {
            tab = new ShellHttpSettingsTab();
            this.addTab(tab);
            tab.init(this.workspace, this);
        }
        this.select(tab);
    }

    /**
     * 刷新设置页的环境下拉框
     */
    public void refreshEnvironmentCombo() {
        ShellHttpSettingsTab tab = this.getTab(ShellHttpSettingsTab.class);
        if (tab != null && tab.controller() != null) {
            tab.controller().refreshEnvironments();
        }
    }
}
