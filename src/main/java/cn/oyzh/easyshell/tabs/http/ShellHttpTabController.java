package cn.oyzh.easyshell.tabs.http;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.http.ShellHttpClient;
import cn.oyzh.easyshell.http.ShellHttpWorkspace;
import cn.oyzh.easyshell.store.http.ShellHttpWorkspaceStore;
import cn.oyzh.easyshell.tabs.ShellBaseTabController;
import cn.oyzh.easyshell.trees.http.ShellHttpTreeItem;
import cn.oyzh.easyshell.trees.http.ShellHttpTreeView;
import cn.oyzh.easyshell.trees.http.collection.ShellHttpCollectionTreeItem;
import cn.oyzh.easyshell.trees.http.collection.ShellHttpCollectionsTreeItem;
import cn.oyzh.easyshell.trees.http.environment.ShellHttpEnvironmentTreeItem;
import cn.oyzh.easyshell.trees.http.environment.ShellHttpEnvironmentsTreeItem;
import cn.oyzh.easyshell.trees.http.request.ShellHttpRequestTreeItem;
import cn.oyzh.easyshell.trees.http.root.ShellHttpRootTreeItem;
import cn.oyzh.easyshell.trees.http.settings.ShellHttpSettingsTreeItem;
import cn.oyzh.fx.gui.text.field.FilterTextField;
import cn.oyzh.fx.plus.controls.pane.FXSplitPane;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.keyboard.KeyHandler;
import cn.oyzh.fx.plus.keyboard.KeyListener;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.window.StageManager;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * HTTP 工作区控制器，负责左侧树与右侧标签页的协同。
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellHttpTabController extends ShellBaseTabController {

    /**
     * 根节点
     */
    @FXML
    private FXSplitPane root;

    /**
     * 工作区树
     */
    @FXML
    private ShellHttpTreeView treeView;

    /**
     * 过滤框
     */
    @FXML
    private FilterTextField filterKW;

    /**
     * 标签页面板
     */
    @FXML
    private ShellHttpTabPane tabPane;

    /**
     * 连接
     */
    private ShellConnect shellConnect;

    /**
     * 客户端
     */
    private ShellHttpClient client;

    /**
     * 工作区
     */
    private ShellHttpWorkspace workspace;

    /**
     * 初始化工作区。
     *
     * @param connect 连接
     */
    public void init(ShellConnect connect) {
        this.shellConnect = connect;
        this.client = new ShellHttpClient(connect);
        try {
            StageManager.showMask(this.getWindow(), () -> FXUtil.runLater(this::initWorkspace));
        } catch (Throwable ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, this.getWindow());
            this.closeTab();
        }
    }

    /**
     * 初始化工作区界面，必须在 JavaFX 线程执行。
     */
    private void initWorkspace() {
        try {
            this.client.start();
            int timeout = this.shellConnect.getConnectTimeOut() == null ? 30 : this.shellConnect.getConnectTimeOut();
            this.workspace = ShellHttpWorkspaceStore.INSTANCE.load(this.shellConnect.getId(), timeout);
            this.tabPane.setClient(this.client);
            this.tabPane.setWorkspace(this.workspace);
            this.tabPane.setTabController(this);
            this.treeView.init(this.workspace, this.client, this.tabPane);
            this.tabPane.initHomeTab();
            this.initFilter();
            this.hideLeft();
        } catch (Throwable ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, this.getWindow());
            this.closeTab();
        }
    }

    /**
     * 获取客户端
     *
     * @return 客户端
     */
    public ShellHttpClient getClient() {
        return client;
    }

    /**
     * 获取工作区。
     *
     * @return 工作区
     */
    public ShellHttpWorkspace getWorkspace() {
        return workspace;
    }

    /**
     * 初始化过滤
     */
    private void initFilter() {
        this.filterKW.textProperty().addListener((observable, oldValue, newValue) -> this.doFilter());
        this.filterKW.matchCasePropery().addListener((observable, oldValue, newValue) -> this.doFilter());
        this.filterKW.wholeWordPropery().addListener((observable, oldValue, newValue) -> this.doFilter());
    }

    /**
     * 执行过滤
     */
    private void doFilter() {
        String keyword = this.filterKW.getTextTrim();
        boolean matchCase = this.filterKW.isMatchCase();
        boolean wholeWord = this.filterKW.isWholeWord();
        this.treeView.setHighlight(keyword);
        this.treeView.setHighlightMatchCase(matchCase);
        this.treeView.getItemFilter().setKw(keyword);
        this.treeView.getItemFilter().setMatchCase(matchCase);
        this.treeView.getItemFilter().setWholeWord(wholeWord);
        ThreadUtil.start(() -> this.treeView.filter());
    }

    /**
     * 绑定快捷键
     */
    @Override
    protected void bindListeners() {
        super.bindListeners();
        KeyHandler searchKeyHandler = new KeyHandler();
        searchKeyHandler.setHandler(e -> this.filterKW.requestFocus());
        searchKeyHandler.setKeyCode(KeyCode.F);
        searchKeyHandler.setMainModifierDown(true);
        searchKeyHandler.setKeyType(KeyEvent.KEY_RELEASED);
        KeyListener.addHandler(this.root, searchKeyHandler);
    }

    /**
     * 刷新环境相关的树与主页显示。
     */
    public void refreshEnvironmentState() {
        if (this.treeView != null) {
            this.treeView.reload();
        }
        if (this.tabPane != null) {
            this.tabPane.refreshHomeTab();
        }
    }

    /**
     * 处理 LLM 工具对工作区的变更。
     *
     * @param type 变更类型
     * @param id   变更对象标识
     */
    public void onWorkspaceChanged(String type, String id) {
        if (this.tabPane != null) {
            switch (StringUtil.blankToDefault(type, "")) {
                case "request-updated", "request-deleted" -> this.tabPane.closeRequestTabSilently(id);
                case "collection-deleted" -> this.tabPane.closeCollectionTabsSilently(id);
                case "environment-updated", "environment-deleted" -> this.tabPane.closeEnvironmentTabSilently(id);
                case "settings-updated" -> this.tabPane.closeSettingsTabSilently();
                default -> {
                }
            }
        }
        this.refreshWorkspaceState();
    }

    /**
     * 刷新工作区树、首页及环境下拉框。
     */
    public void refreshWorkspaceState() {
        if (this.treeView != null) {
            this.treeView.reload();
        }
        if (this.tabPane != null) {
            this.tabPane.refreshHomeTab();
            this.tabPane.refreshEnvironmentCombo();
        }
    }

    /**
     * 新增节点
     */
    @FXML
    private void addNode() {
        ShellHttpTreeItem<?> item = (ShellHttpTreeItem<?>) this.treeView.getSelectedItem();
        if (item == null || item instanceof ShellHttpRootTreeItem) {
            this.treeView.collectionsItem().addCollection();
        } else if (item instanceof ShellHttpCollectionsTreeItem collectionsItem) {
            collectionsItem.addCollection();
        } else if (item instanceof ShellHttpCollectionTreeItem collectionItem) {
            collectionItem.addRequest();
        } else if (item instanceof ShellHttpRequestTreeItem requestItem
                && ((TreeItem<?>) requestItem.getParent()) instanceof ShellHttpCollectionTreeItem collectionItem) {
            collectionItem.addRequest();
        } else if (item instanceof ShellHttpEnvironmentsTreeItem environmentsItem) {
            environmentsItem.addEnvironment();
        } else if (item instanceof ShellHttpEnvironmentTreeItem) {
            this.treeView.environmentsItem().addEnvironment();
        } else if (item instanceof ShellHttpSettingsTreeItem settingsItem) {
            settingsItem.open();
        }
    }

    /**
     * 定位当前节点
     */
    @FXML
    private void positionNode() {
        this.treeView.positionItem();
    }

    @Override
    public void onTabClosed(Event event) {
        if (this.treeView != null) {
            this.treeView.saveExpansionState();
        }
        if (this.tabPane != null) {
            this.tabPane.closeAllTabs();
        }
        super.onTabClosed(event);
        if (this.client != null) {
            this.client.close();
        }
    }
}
