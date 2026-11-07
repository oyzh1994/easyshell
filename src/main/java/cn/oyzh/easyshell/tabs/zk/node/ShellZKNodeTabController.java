package cn.oyzh.easyshell.tabs.zk.node;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.event.zk.ShellZKHistoryRestoreEvent;
import cn.oyzh.easyshell.trees.zk.node.ShellZKNodeTreeItem;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.easyshell.zk.ShellZKClient;
import cn.oyzh.event.EventSubscribe;
import cn.oyzh.fx.gui.tabs.ParentTabController;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.window.StageManager;
import javafx.fxml.FXML;

import java.util.List;

/**
 * zk节点tab内容组件
 *
 * @author oyzh
 * @since 2025-09-04
 */
public class ShellZKNodeTabController extends ParentTabController {

    /**
     * tab节点
     */
    @FXML
    private FXTabPane tabPane;

    /**
     * 当前激活的节点
     */
    private transient ShellZKNodeTreeItem activeItem;

    /**
     * zk客户端
     */
    private ShellZKClient client;

    /**
     * 获取当前激活的节点
     *
     * @return 当前激活的节点
     */
    public ShellZKNodeTreeItem getActiveItem() {
        return activeItem;
    }

    /**
     * 获取zk客户端
     *
     * @return zk客户端
     */
    public ShellZKClient getClient() {
        return client;
    }

    /**
     * 初始化
     *
     * @param item 节点
     */
    public void init(ShellZKNodeTreeItem item) {
        this.activeItem = item;
        this.client = item.client();
        StageManager.showMask(this.getWindow(), () -> {
            try {
                this.initNode();
            } catch (Exception ex) {
                MessageBox.exception(ex, this.getWindow());
            }
        });
        // 检查状态
        FXUtil.runLater(this::checkStatus, 100);
    }

    /**
     * 初始化节点
     *
     * @throws Exception 异常
     */
    private void initNode() throws Exception {
        if (this.activeItem == null || this.tabPane == null) {
            return;
        }
        String id = this.tabPane.getSelectTabId();
        if (id == null || "dataTab".equals(id)) {
            // 初始化数据
            this.dataTabController.initData();
        } else if ("hexTab".equals(id)) {
            // 初始化hex
            this.hexTabController.initHex();
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
        StageManager.showMask(this.getWindow(), () -> {
            try {
                // 刷新节点
                this.activeItem.refreshNode();
                // 初始化节点
                this.initNode();
                // 刷新tab
                this.flushTab();
            } catch (Exception ex) {
                MessageBox.exception(ex, this.getWindow());
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
            if (MessageBox.confirm("[" + nodePath + "] " + ShellI18nHelper.zkNodeTip6(), this.getWindow())) {
                this.activeItem.authNode();
            }
        }
    }

    /**
     * 是否有未保存数据
     *
     * @return 结果
     */
    public boolean hasUnsavedData() {
        return this.activeItem != null && this.activeItem.isDataUnsaved();
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        // tab组件切换事件
        this.tabPane.selectedItemChanged((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // 初始化节点
                StageManager.showMask(this.getWindow(), () -> {
                    try {
                        this.initNode();
                    } catch (Exception ex) {
                        MessageBox.exception(ex, this.getWindow());
                    }
                });
            }
        });
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
     * hex
     */
    @FXML
    private ShellZKNodeHexTabController hexTabController;

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
        return List.of(
                this.dataTabController,
                this.hexTabController,
                this.statTabController,
                this.aclTabController,
                this.quotaTabController
        );
    }
}
