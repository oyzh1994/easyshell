package cn.oyzh.easyshell.tabs.zk.node;

import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.event.zk.ShellZKHistoryRestoreEvent;
import cn.oyzh.easyshell.filter.zk.ShellZKNodeFilterTypeComboBox;
import cn.oyzh.easyshell.trees.zk.node.ShellZKNodeTreeItem;
import cn.oyzh.easyshell.trees.zk.ShellZKTreeView;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.easyshell.util.zk.ShellZKViewFactory;
import cn.oyzh.easyshell.zk.ShellZKClient;
import cn.oyzh.event.EventSubscribe;
import cn.oyzh.fx.gui.tabs.ParentTabController;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.gui.text.field.FilterTextField;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.keyboard.KeyHandler;
import cn.oyzh.fx.plus.keyboard.KeyListener;
import cn.oyzh.fx.plus.node.NodeWidthResizer;
import cn.oyzh.fx.plus.util.ClipboardUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.window.StageManager;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Window;

import java.util.List;

/**
 * zk节点tab内容组件
 *
 * @author oyzh
 * @since 2023/05/21
 */
public class ShellZKNodeTabController extends ParentTabController {

    /**
     * 根节点
     */
    @FXML
    private FXHBox root;

    /**
     * 左侧节点
     */
    @FXML
    private FXVBox leftBox;

    /**
     * tab节点
     */
    @FXML
    private FXTabPane tabPane;

    /**
     * 节点树
     */
    @FXML
    private ShellZKTreeView treeView;

    /**
     * 过滤类型
     */
    @FXML
    private ShellZKNodeFilterTypeComboBox filterType;

    /**
     * 过滤内容
     */
    @FXML
    private FilterTextField filterKW;

    /**
     * 节点路径
     */
    @FXML
    private FXLabel nodePath;

    /**
     * 当前激活的节点
     */
    private transient ShellZKNodeTreeItem activeItem;

    public ShellZKNodeTreeItem getActiveItem() {
        return activeItem;
    }

    /**
     * zk客户端
     */
    private ShellZKClient client;

    /**
     * 初始化
     *
     * @param client 树节点
     */
    public void init(ShellZKClient client) {
        this.client = client;
        this.treeView.client(this.client);
        // 加载根节点
        StageManager.showMask(() -> {
            try {
                this.treeView.loadRoot();
            } catch (Exception ex) {
                this.closeTab();
                MessageBox.exception(ex);
            }
        });
        // 状态无效，则关闭，延迟3秒检查
        TaskManager.startDelay(() -> {
            if (this.client.isInvalid()) {
                this.closeTab();
            }
        }, 3000);
    }

    /**
     * 初始化节点
     *
     * @param treeItem 当前节点
     */
    private void initItem(TreeItem<?> treeItem) {
        if (treeItem instanceof ShellZKNodeTreeItem) {
            this.activeItem = (ShellZKNodeTreeItem) treeItem;
            this.nodePath.text(this.activeItem.nodePath());
        } else {
            this.activeItem = null;
            if (this.treeView.root() != null) {
                this.nodePath.text(this.treeView.root().nodePath());
            }
        }
        try {
            if (this.activeItem != null) {
                // 初始化节点
                StageManager.showMask(() -> {
                    try {
                        this.initNode();
                    } catch (Exception ex) {
                        MessageBox.exception(ex);
                    }
                });
                // 启用组件
                this.tabPane.enable();
                // 检查状态
                FXUtil.runLater(this::checkStatus, 100);
            } else {
                // 禁用组件
                this.tabPane.disable();
            }
            // 刷新树
            this.treeView.refresh();
            // 刷新tab
            this.flushTab();
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    /**
     * 初始化节点
     */
    private void initNode() throws Exception {
        if (this.activeItem == null) {
            return;
        }
        String id = this.tabPane.getSelectTabId();
        if ("dataTab".equals(id)) {
            // 初始化数据
            this.dataTabController.initData();
        } else if ("statTab".equals(id)) {
            // 初始化状态
            this.statTabController.initStat();
        } else if ("aclTab".equals(id)) {
            // 初始化acl
            this.aclTabController.initACL();
        } else if ("quotaTab".equals(id)) {
            // 初始化配额
            this.quotaTabController.initQuota();
        }
    }

    /**
     * 刷新节点
     */
    private void refreshItem() {
        StageManager.showMask(() -> {
            try {
                // 刷新节点
                this.activeItem.refreshNode();
                // 初始化节点
                this.initNode();
                // 刷新tab
                this.flushTab();
            } catch (Exception ex) {
                MessageBox.exception(ex);
            }
        });
    }

    /**
     * 检查节点状态
     */
    private void checkStatus() {
        if (this.activeItem == null) {
            return;
        }
        // 节点被移除
        String nodePath = this.activeItem.nodePath();
        if (this.activeItem.isNeedAuth()) { // 需要认证
            if (MessageBox.confirm("[" + nodePath + "] " + ShellI18nHelper.zkNodeTip6())) {
                this.activeItem.authNode();
            }
        }
    }

    private NodeWidthResizer widthResizer;

    @Override
    protected void bindListeners() {
        super.bindListeners();
        // 监听选中变化
        this.treeView.selectItemChanged(this::initItem);
        // 过滤处理
        this.filterType.selectedIndexChanged((observable, oldValue, newValue) -> this.doFilter());
        // tab组件切换事件
        this.tabPane.selectedItemChanged((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // 初始化节点
                StageManager.showMask(() -> {
                    try {
                        this.initNode();
                    } catch (Exception ex) {
                        MessageBox.exception(ex);
                    }
                });
            }
        });
        // 拉伸辅助
        this.widthResizer = NodeWidthResizer.of(this.leftBox, this::resizeLeft, 240, 750);
        // 过滤
        KeyHandler searchKeyHandler = new KeyHandler();
        searchKeyHandler.setHandler(e -> this.filterKW.requestFocus());
        searchKeyHandler.setKeyCode(KeyCode.F);
        searchKeyHandler.setMainModifierDown(true);
        searchKeyHandler.setKeyType(KeyEvent.KEY_RELEASED);
        KeyListener.addHandler(this.root, searchKeyHandler);
        // 拉伸辅助
        this.widthResizer = NodeWidthResizer.of(this.leftBox, this::resizeLeft, 240, 750);
        // 内容过滤
        this.filterKW.textProperty().addListener((observable, oldValue, newValue) -> {
            this.doFilter();
        });
        this.filterKW.wholeWordPropery().addListener((observable, oldValue, newValue) -> {
            this.doFilter();
        });
        this.filterKW.matchCasePropery().addListener((observable, oldValue, newValue) -> {
            this.doFilter();
        });
    }

    /**
     * 左侧组件重新布局
     *
     * @param newWidth 新宽度
     */
    private void resizeLeft(Float newWidth) {
        if (newWidth != null && !Float.isNaN(newWidth)) {
            // 设置组件宽
            this.leftBox.setRealWidth(newWidth);
            // this.tabPane.setLayoutX(newWidth);
            this.tabPane.setFlexWidth("100% - " + newWidth);
            // this.leftBox.parentAutosize();
        }
    }

    /**
     * 当前窗口
     *
     * @return 窗口
     */
    protected Window window() {
        return this.getActiveItem().window();
        // return this.treeItem == null ? null : this.treeItem.window();
    }

    //    /**
    //     * 保存配额
    //     */
    //    @FXML
    //    private void saveQuota() {
    //        try {
    //            this.activeItem.saveQuota(this.quotaBytes.getLongValue(), this.quotaCount.getIntValue());
    //            MessageBox.okToast(I18nHelper.operationSuccess());
    //        } catch (Exception ex) {
    //            MessageBox.exception(ex);
    //        }
    //    }

    //    /**
    //     * 清除子节点数量配额
    //     */
    //    @FXML
    //    private void clearQuotaCount() {
    //        this.quotaCount.setValue(-1);
    //    }
    //
    //    /**
    //     * 清除数据大小配额
    //     */
    //    @FXML
    //    private void clearQuotaBytes() {
    //        this.quotaBytes.setValue(-1);
    //    }

    // /**
    //  * 恢复数据
    //  *
    //  * @param data 数据
    //  */
    // public void restoreData(byte[] data) {
    //     // 保存数据历史
    //     this.activeItem.nodeData(data);
    //     this.dataTabController.showData();
    // }

    /**
     * 执行过滤
     */
    private void doFilter() {
        String kw = this.filterKW.getTextTrim();
        // 匹配大小写
        boolean matchCase = this.filterKW.isMatchCase();
        // 全字模式
        boolean wholeWord = this.filterKW.isWholeWord();
        // 过滤类型
        int type = this.filterType.getSelectedIndex();
        // 设置高亮是否匹配大小写
        this.treeView.setHighlightMatchCase(matchCase);
        //        // 仅在过滤路径的情况下设置节点高亮
        //        if (scope == 2 || scope == 0) {
        this.treeView.setHighlight(kw);
        //        } else {
        //            this.treeView.setHighlight(null);
        //        }
        //        // 仅在过滤数据的情况下设置内容高亮
        //        if (scope == 2 || scope == 1) {
        //            // this.nodeData.setHighlightText(kw);
        //            this.dataTabController.setDataHighlight(kw);
        //            // } else {
        //            // this.nodeData.setHighlightText(this.dataSearch.getTextTrim());
        //        }
        this.treeView.getItemFilter().setKw(kw);
        this.treeView.getItemFilter().setType((byte) type);
        this.treeView.getItemFilter().setMatchCase(matchCase);
        this.treeView.getItemFilter().setWholeWord(wholeWord);
        ThreadUtil.start(this.treeView::filter);
    }

    /**
     * 定位节点
     */
    @FXML
    private void positionNode() {
        this.treeView.positionItem();
    }

    /**
     * 导入数据
     */
    @FXML
    private void importData() {
        ShellZKViewFactory.zkImportData(this.client.getShellConnect());
    }

    /**
     * 导出数据
     */
    @FXML
    private void exportData() {
        ShellZKViewFactory.zkExportData(this.client.getShellConnect(), null);
    }

    /**
     * 传输数据
     */
    @FXML
    private void transportData() {
        ShellZKViewFactory.zkTransportData(this.client.getShellConnect());
    }

    public ShellZKClient getClient() {
        return client;
    }

    /**
     * 历史恢复事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onHistoryRestore(ShellZKHistoryRestoreEvent event) {
        if (event.data() == this.client && this.activeItem != null && StringUtil.equals(this.activeItem.nodePath(), event.getNodePath())) {
            this.refreshItem();
        }
    }

    /**
     * 数据
     */
    @FXML
    private ShellZKNodeDataTabController dataTabController;

    /**
     * 状态
     */
    @FXML
    private ShellZKNodeStatTabController statTabController;

    /**
     * 权限
     */
    @FXML
    private ShellZKNodeACLTabController aclTabController;

    /**
     * 配额
     */
    @FXML
    private ShellZKNodeQuotaTabController quotaTabController;

    @Override
    public List<? extends RichTabController> getSubControllers() {
        return List.of(this.dataTabController,
                this.statTabController,
                this.aclTabController,
                this.quotaTabController);
    }

    @Override
    public void destroy() {
        this.tabPane.destroy();
        this.treeView.destroy();
        this.widthResizer.destroy();
        this.dataTabController.destroy();
        this.statTabController.destroy();
        this.aclTabController.destroy();
        this.quotaTabController.destroy();
        super.destroy();
    }

    /**
     * 复制节点路径
     */
    @FXML
    private void copyNodePath() {
        if (this.activeItem != null) {
            ClipboardUtil.setStringAndTip(this.activeItem.decodeNodePath());
        }
    }
}
