package cn.oyzh.easyshell.tabs.zk.node;

import cn.oyzh.common.system.OSUtil;
import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.easyshell.event.zk.ZKEventUtil;
import cn.oyzh.easyshell.event.zk.auth.ZKAuthAuthedEvent;
import cn.oyzh.easyshell.event.zk.node.ZKNodeAddedEvent;
import cn.oyzh.easyshell.event.zk.node.ZKNodeChangedEvent;
import cn.oyzh.easyshell.event.zk.node.ZKNodeCreatedEvent;
import cn.oyzh.easyshell.event.zk.node.ZKNodeRemovedEvent;
import cn.oyzh.easyshell.filter.zk.ZKNodeFilterTextField;
import cn.oyzh.easyshell.filter.zk.ZKNodeFilterTypeComboBox;
import cn.oyzh.easyshell.trees.zk.ZKNodeTreeItem;
import cn.oyzh.easyshell.trees.zk.ZKNodeTreeView;
import cn.oyzh.easyshell.util.zk.ZKI18nHelper;
import cn.oyzh.easyshell.util.zk.ZKViewFactory;
import cn.oyzh.easyshell.zk.ZKClient;
import cn.oyzh.event.EventSubscribe;
import cn.oyzh.fx.gui.svg.pane.CollectSVGPane;
import cn.oyzh.fx.gui.svg.pane.SortSVGPane;
import cn.oyzh.fx.gui.tabs.ParentTabController;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.keyboard.KeyHandler;
import cn.oyzh.fx.plus.keyboard.KeyListener;
import cn.oyzh.fx.plus.node.NodeWidthResizer;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.window.StageManager;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
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
public class ZKNodeTabController extends ParentTabController {

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
    private ZKNodeTreeView treeView;

    public ZKNodeTreeView getTreeView() {
        return treeView;
    }

    /**
     * 过滤类型
     */
    @FXML
    private ZKNodeFilterTypeComboBox filterType;

    /**
     * 过滤内容
     */
    @FXML
    private ZKNodeFilterTextField filterKW;

//    /**
//     * 右侧zk属性组件
//     */
//    @FXML
//    private FXVBox statBox;
//
//    /**
//     * zk属性视图切换按钮
//     */
//    @FXML
//    private FXToggleSwitch statViewSwitch;

//    /**
//     * 内容过滤组件
//     */
//    @FXML
//    private ClearableTextField dataSearch;

//    /**
//     * 分页信息
//     */
//    private Paging<ZKACL> aclPaging;

    // /**
    //  * zk连接节点
    //  */
    // private ZKConnectTreeItem treeItem;

    /**
     * 当前激活的节点
     */
    private transient ZKNodeTreeItem activeItem;

    // public ZKConnectTreeItem getTreeItem() {
    //     return treeItem;
    // }

    public ZKNodeTreeItem getActiveItem() {
        return activeItem;
    }

//    /**
//     * 右侧acl分页组件
//     */
//    @FXML
//    private PageBox<ZKACL> aclPage;

//    /**
//     * 右侧zk权限视图切换按钮
//     */
//    @FXML
//    private CharsetComboBox charset;

//    /**
//     * 右侧zk权限视图切换按钮
//     */
//    @FXML
//    private FXToggleSwitch aclViewSwitch;
//
//    /**
//     * acl表视图
//     */
//    @FXML
//    private ZKACLTableView aclTableView;

//    /**
//     * 数据大小
//     */
//    @FXML
//    private FXText dataSize;
//
//    /**
//     * 加载耗时
//     */
//    @FXML
//    private FXText loadTime;
//
//    /**
//     * zk数据保存
//     */
//    @FXML
//    private SVGGlyph dataSave;
//
//    /**
//     * zk数据撤销
//     */
//    @FXML
//    private SVGGlyph dataUndo;
//
//    /**
//     * zk数据重做
//     */
//    @FXML
//    private SVGGlyph dataRedo;
//
//    /**
//     * 右侧zk数据
//     */
//    @FXML
//    private RichDataTextAreaPane nodeData;
//
//    /**
//     * 格式
//     */
//    @FXML
//    protected RichDataTypeComboBox format;

//    /**
//     * 数据tab
//     */
//    @FXML
//    private FXTab dataTab;
//
//    /**
//     * 状态tab
//     */
//    @FXML
//    private FXTab statTab;

//    /**
//     * 权限tab
//     */
//    @FXML
//    private FXTab aclTab;

//    /**
//     * 配额tab
//     */
//    @FXML
//    protected FXTab quotaTab;

//    /**
//     * 子节点数量配额
//     */
//    @FXML
//    protected NumberTextField quotaCount;
//
//    /**
//     * 节点数据大小配额
//     */
//    @FXML
//    protected NumberTextField quotaBytes;

    /**
     * zk客户端
     */
    private ZKClient client;

    /**
     * 收藏面板
     */
    @FXML
    private CollectSVGPane collectPane;

    /**
     * 排序面板
     */
    @FXML
    private SortSVGPane sortPane;

    /**
     * 初始化
     *
     * @param client 树节点
     */
    public void init(ZKClient client) {
        this.client = client;
        // this.client = item.getClient();
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
        if (treeItem instanceof ZKNodeTreeItem) {
            this.activeItem = (ZKNodeTreeItem) treeItem;
        } else {
            this.activeItem = null;
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
                // 设置是否收藏
                this.collectPane.setCollect(this.activeItem.isCollect());
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
            // 触发事件
            ZKEventUtil.nodeSelected(this.activeItem);
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
        if (this.activeItem.isBeDeleted()) {
            if (!this.activeItem.isIgnoreDeleted()) {
                if (MessageBox.confirm("[" + nodePath + "] " + ZKI18nHelper.nodeTip2())) {
                    this.activeItem.remove();
                } else {
                    this.activeItem.doIgnoreDeleted();
                }
            }
        } else if (this.activeItem.isBeChanged()) { // 节点被更新
            if (!this.activeItem.isIgnoreChanged()) {
                if (MessageBox.confirm("[" + nodePath + "] " + ZKI18nHelper.nodeTip1())) {
                    this.refreshItem();
                } else {
                    this.activeItem.doIgnoreChanged();
                }
            }
        } else if (this.activeItem.isBeChildChanged()) { // 子节点被更新
            if (!this.activeItem.isIgnoreChildChanged()) {
                if (MessageBox.confirm("[" + nodePath + "] " + ZKI18nHelper.nodeTip5())) {
                    this.activeItem.reloadChild();
                    this.activeItem.clearBeChildChanged();
                } else {
                    this.activeItem.doIgnoreChildChanged();
                }
            }
        } else if (this.activeItem.isNeedAuth()) { // 需要认证
            if (MessageBox.confirm("[" + nodePath + "] " + ZKI18nHelper.nodeTip6())) {
                this.activeItem.authNode();
            }
        }
    }

//    /**
//     * 重新载入权限
//     */
//    @FXML
//    public void reloadACL() {
//        try {
//            this.activeItem.refreshACL();
//            this.initACL();
//            MessageBox.okToast(I18nHelper.operationSuccess());
//        } catch (Exception ex) {
//            MessageBox.exception(ex);
//        }
//    }
//
//    /**
//     * 添加权限
//     */
//    @FXML
//    private void addACL() {
////        StageAdapter adapter = StageManager.parseStage(ZKACLAddController.class, this.window());
////        adapter.setProp("zkItem", this.activeItem);
////        adapter.setProp("zkClient", this.activeItem.client());
////        adapter.display();
//        ZKEventUtil.showAddACL(this.activeItem, this.activeItem.client());
//    }

//    /**
//     * 重载配额
//     */
//    @FXML
//    private void reloadQuota() {
//        try {
//            this.activeItem.refreshQuota();
//            this.initQuota();
//            MessageBox.okToast(I18nHelper.operationSuccess());
//        } catch (Exception ex) {
//            MessageBox.exception(ex);
//        }
//    }

//    /**
//     * 复制配额
//     */
//    @FXML
//    private void copyQuota() {
//        try {
//            if (this.activeItem == null) {
//                return;
//            }
//            StatsTrack quota = this.activeItem.quota();
//            String builder;
//            if (quota == null) {
//                builder = I18nHelper.count() + " -1" + System.lineSeparator() + I18nHelper.bytes() + " -1";
//            } else {
//                builder = I18nHelper.count() + " " + quota.getCount() + System.lineSeparator() + I18nHelper.bytes() + " " + quota.getBytes();
//            }
//            ClipboardUtil.setStringAndTip(builder);
//            MessageBox.okToast(I18nHelper.operationSuccess());
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            MessageBox.exception(ex);
//        }
//    }

//    /**
//     * 复制访问控制
//     */
//    @FXML
//    private void copyACL() {
//        ZKACL acl = this.aclTableView.getSelectedItem();
//        if (acl == null) {
//            MessageBox.warn(I18nHelper.acl() + " " + I18nHelper.isEmpty());
//            return;
//        }
//        try {
//            String builder = acl.idFriend().getName(this.aclViewSwitch.isSelected()) + " " + acl.idFriend().getValue(this.aclViewSwitch.isSelected()) + System.lineSeparator() + acl.schemeFriend().getName(this.aclViewSwitch.isSelected()) + " " + acl.schemeFriend().getValue(this.aclViewSwitch.isSelected()) + System.lineSeparator() + acl.permsFriend().getName(this.aclViewSwitch.isSelected()) + " " + acl.permsFriend().getValue(this.aclViewSwitch.isSelected());
//            ClipboardUtil.setStringAndTip(builder);
//            MessageBox.okToast(I18nHelper.operationSuccess());
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            MessageBox.exception(ex);
//        }
//    }

//    /**
//     * 修改权限
//     */
//    @FXML
//    private void updateACL() {
//        ZKACL acl = this.aclTableView.getSelectedItem();
//        if (acl == null) {
//            return;
//        }
////        try {
////            StageAdapter adapter = StageManager.parseStage(ZKACLUpdateController.class, this.window());
////            adapter.setProp("acl", acl);
////            adapter.setProp("zkItem", this.activeItem);
////            adapter.setProp("zkClient", this.treeItem.client());
////            adapter.display();
////        } catch (Exception ex) {
////            ex.printStackTrace();
////            MessageBox.exception(ex, I18nHelper.operationException());
////        }
//        ZKEventUtil.showUpdateACL(this.activeItem, this.treeItem.getClient(), acl);
//    }
//
//    /**
//     * 删除权限
//     */
//    @FXML
//    private void deleteACL() {
//        ZKACL acl = this.aclTableView.getSelectedItem();
//        if (acl == null) {
//            return;
//        }
//        if (this.activeItem.acl().size() == 1) {
//            MessageBox.warn(this.i18nString("zk.aclTip1"));
//            return;
//        }
//        if (!MessageBox.confirm(I18nHelper.deleteACL() + " " + acl.idVal() + " ?")) {
//            return;
//        }
//        try {
//            Stat stat = this.activeItem.deleteACL(acl);
//            if (stat != null) {
//                this.aclTableView.removeItem(acl);
//                // 重载权限和页面
//                if (this.aclTableView.isItemEmpty()) {
//                    this.reloadACL();
//                } else {// 仅重载权限
//                    this.activeItem.refreshACL();
//                }
//            } else {
//                MessageBox.warn(I18nHelper.operationFail());
//            }
//        } catch (Exception ex) {
//            MessageBox.exception(ex);
//        }
//    }

//    /**
//     * 权限列表，上一页
//     */
//    @FXML
//    private void aclPrevPage() {
//        if (this.aclPaging != null) {
//            this.renderACLView(this.aclPaging.prevPage());
//        }
//    }
//
//    /**
//     * 权限列表，下一页
//     */
//    @FXML
//    private void aclNextPage() {
//        if (this.aclPaging != null) {
//            this.renderACLView(this.aclPaging.nextPage());
//        }
//    }
//
//    /**
//     * 渲染权限控件
//     *
//     * @param pageNo 页码
//     */
//    private void renderACLView(long pageNo) {
//        // 获取zk权限分页数据
//        List<ZKACL> aclList = this.aclPaging.page(pageNo);
//        // 设置分页信息
//        this.aclPage.setPaging(this.aclPaging);
//        List<ZKACLControl> list = new ArrayList<>();
//        for (ZKACL zkacl : aclList) {
//            ZKACLControl control = new ZKACLControl();
//            control.setId(zkacl.getId());
//            control.setPerms(zkacl.getPerms());
//            control.setFriendly(this.aclViewSwitch.isSelected());
//            control.setAuthed(this.client.isDigestAuthed(zkacl.idVal()));
//            list.add(control);
//        }
//        this.aclTableView.setItem(list);
//    }

//    /**
//     * 复制zk状态
//     */
//    @FXML
//    private void copyStat() {
//        List<FriendlyInfo<Stat>> statInfos = this.activeItem.statInfos();
//        StringBuilder builder = new StringBuilder();
//        for (int i = 0; i < statInfos.size(); i++) {
//            FriendlyInfo<Stat> statInfo = statInfos.get(i);
//            builder.append(statInfo.getName(this.statViewSwitch.isSelected())).append(" : ").append(statInfo.getValue(this.statViewSwitch.isSelected()));
//            if (statInfo != CollectionUtil.getLast(statInfos)) {
//                builder.append(System.lineSeparator());
//            }
//        }
//        ClipboardUtil.setStringAndTip(builder.toString());
//    }

//    /**
//     * 刷新zk状态
//     */
//    @FXML
//    private void reloadStat() {
//        try {
//            this.activeItem.refreshStat();
//            this.initStat();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            MessageBox.exception(ex);
//        }
//    }

//    /**
//     * 复制节点路径及数据
//     */
//    @FXML
//    private void copyNode() {
//        try {
//            byte[] bytes = this.activeItem.getData();
//            String data = this.activeItem.decodeNodePath() + " " + new String(bytes);
//            ClipboardUtil.setStringAndTip(data);
//        } catch (Exception ex) {
//            MessageBox.exception(ex);
//        }
//    }
//
//    /**
//     * 复制节点路径
//     */
//    @FXML
//    private void copyNodePath() {
//        ClipboardUtil.setStringAndTip(this.activeItem.decodeNodePath());
//    }
//
//    /**
//     * 保存为二进制文件
//     */
//    @FXML
//    private void saveBinaryFile() {
//        try {
//            File file = FileChooserHelper.save(I18nHelper.saveFile(), "", FXChooser.allExtensionFilter());
//            if (file != null) {
//                FileUtil.writeBytes(this.activeItem.getNodeData(), file);
//                MessageBox.info(I18nHelper.operationSuccess());
//            }
//        } catch (Exception ex) {
//            MessageBox.exception(ex);
//        }
//    }

//    /**
//     * 刷新zk节点数据
//     */
//    @FXML
//    private void reloadData() {
//        // 放弃保存
//        if (this.activeItem.isDataUnsaved() && !MessageBox.confirm(I18nHelper.unsavedAndContinue())) {
//            return;
//        }
//        // 刷新数据
//        try {
//            this.activeItem.refreshData();
//            // 数据变更
//            this.showData();
//            // 刷新tab颜色
//            this.flushTabGraphicColor();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            MessageBox.exception(ex);
//        }
//    }
//
//    /**
//     * 保存节点数据
//     */
//    @FXML
//    private void saveNodeData() {
//        if (this.activeItem.isDataTooBig()) {
//            MessageBox.warn(I18nHelper.dataTooLarge());
//            return;
//        }
//        // 保存数据
//        if (!this.activeItem.isDataUnsaved()) {
//            return;
//        }
//        // 保存数据
//        RenderService.submit(() -> {
//            // 保存数据
//            if (this.activeItem.saveData()) {
//                // 禁用图标
//                this.dataSave.disable();
//                // 刷新数据大小
//                this.flushDataSize();
//                // 刷新tab颜色
//                this.flushTabGraphicColor();
//            }
//        });
//    }
//
//    /**
//     * 数据撤销
//     */
//    @FXML
//    private void dataUndo() {
//        this.nodeData.undo();
//        this.nodeData.requestFocus();
//    }
//
//    /**
//     * 数据重做
//     */
//    @FXML
//    private void dataRedo() {
//        this.nodeData.redo();
//        this.nodeData.requestFocus();
//    }
//
//    /**
//     * 粘贴数据
//     */
//    @FXML
//    private void pasteData() {
//        this.nodeData.paste();
//        this.nodeData.requestFocus();
//    }
//
//    /**
//     * 清空数据
//     */
//    @FXML
//    private void clearData() {
//        this.nodeData.clear();
//        this.nodeData.requestFocus();
//    }

//    /**
//     * zk节点转二维码
//     */
//    @FXML
//    private void node2QRCode(MouseEvent event) {
//        try {
////            StageAdapter adapter = StageManager.parseStage(ZKQRCodeNodeController.class, this.window());
////            adapter.setProp("zkNode", this.activeItem.value());
////            adapter.setProp("nodeData", this.nodeData.getTextTrim());
////            adapter.display();
//            PopupAdapter adapter = PopupManager.parsePopup(ZKNodeQRCodePopupController.class);
//            adapter.setProp("zkNode", this.activeItem.value());
//            adapter.setProp("nodeData", this.nodeData.getTextTrim());
//            adapter.showPopup((Node) event.getSource());
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            MessageBox.exception(ex);
//        }
////        ZKEventUtil.showQRCodeNode(this.activeItem.value(), this.nodeData.getTextTrim());
//    }

//    /**
//     * 认证节点
//     */
//    @FXML
//    private void authNode() {
//        this.activeItem.authNode();
//    }
//
//    /**
//     * zk数据控件按键事件
//     *
//     * @param e 事件
//     */
//    @FXML
//    private void onNodeDataKeyPressed(KeyEvent e) {
//        // 保存节点数据
//        if (KeyboardUtil.isCtrlS(e)) {
//            this.saveNodeData();
//            e.consume();
//        }
//    }

    /**
     * 显示历史
     */
    @FXML
    private void showHistory() {
        if (this.activeItem != null) {
            ZKEventUtil.historyShow(this.activeItem);
        }
    }
//
//    /**
//     * 显示数据
//     */
//    protected void showData() {
//        // 检测数据是否太大
//        if (this.activeItem.isDataTooBig()) {
//            this.nodeData.clear();
//            this.nodeData.disable();
//            NodeGroupUtil.disable(this.dataTab, "dataToBig");
//            // 异步处理，避免阻塞主程序
//            TaskManager.startDelay(() -> {
//                if (MessageBox.confirm(ZKI18nHelper.nodeTip7())) {
//                    this.saveBinaryFile();
//                }
//            }, 10);
//            return;
//        }
//        NodeGroupUtil.enable(this.dataTab, "dataToBig");
//        byte[] bytes = this.activeItem.getData();
//        // 转换编码
//        bytes = TextUtil.changeCharset(bytes, Charset.defaultCharset(), this.charset.getCharset());
//        // 显示检测后的数据
//        RichDataType dataType = this.nodeData.showDetectData(new String(bytes, this.charset.getCharset()));
//        // 选中格式
//        this.format.selectObj(dataType);
//    }
//
//    /**
//     * 显示数据
//     *
//     * @param dataType 数据类型
//     */
//    protected void showData(RichDataType dataType) {
//        byte[] bytes = this.activeItem.getData();
//        bytes = TextUtil.changeCharset(bytes, Charset.defaultCharset(), this.charset.getCharset());
//        this.nodeData.showData(dataType, bytes);
//    }
//
//    /**
//     * 初始化数据
//     */
//    public void initData() {
//        // 显示数据
//        this.showData();
//        // 刷新数据大小
//        this.flushDataSize();
//        // 遗忘历史
//        this.nodeData.forgetHistory();
//        // 按钮处理
//        this.dataUndo.disable();
//        this.dataRedo.disable();
//        this.dataSave.setDisable(!this.activeItem.isDataUnsaved());
//        // 加载耗时处理
//        this.loadTime.text(I18nHelper.cost() + " : " + this.activeItem.loadTime() + "ms");
//    }
//
//    /**
//     * 刷新数据大小
//     */
//    private void flushDataSize() {
//        // 数据大小处理
//        this.dataSize.text(I18nHelper.size() + " : " + this.activeItem.dataSizeInfo());
//    }

//    /**
//     * 初始化状态
//     */
//    public void initStat() {
//        if (this.activeItem == null) {
//            return;
//        }
//        List<FriendlyInfo<Stat>> statInfos = this.activeItem.statInfos();
//        // 有可能为空
//        if (CollectionUtil.isNotEmpty(statInfos)) {
//            Set<Node> statItems = this.statBox.lookupAll(".statItem");
//            // 遍历节点
//            int index = 0;
//            for (Node statItem : statItems) {
//                FXHBox box = (FXHBox) statItem;
//                FriendlyInfo<Stat> statInfo = statInfos.get(index++);
//                Label label = (Label) box.getChildren().get(0);
//                Label data = (Label) box.getChildren().get(1);
//                data.setFocusTraversable(true);
//                // 设置属性值及属性值
//                FXUtil.runLater(() -> {
//                    label.setText(statInfo.getName(this.statViewSwitch.isSelected()));
//                    data.setText(statInfo.getValue(this.statViewSwitch.isSelected()).toString());
//                });
//            }
//        }
//    }

//    /**
//     * 初始化权限
//     */
//    public void initACL() {
//        if (this.treeItem == null || this.activeItem == null) {
//            return;
//        }
//        if (this.activeItem.aclEmpty()) {
//            this.aclPaging = null;
//            this.aclViewSwitch.disable();
//            this.aclTableView.clearItems();
//        } else {
//            this.aclViewSwitch.enable();
//            List<ZKACL> aclList = this.activeItem.acl();
//            // 获取分页控件
//            this.aclPaging = new Paging<>(aclList, 10);
//            // 渲染首页数据
//            this.renderACLView(0);
//        }
//    }

//    /**
//     * 初始化配额
//     */
//    public void initQuota() throws Exception {
//        if (this.treeItem == null) {
//            return;
//        }
//        if (this.activeItem.isRootNode()) {
//            this.quotaTab.getContent().setDisable(true);
//        } else {
//            this.quotaTab.getContent().setDisable(false);
//            StatsTrack quota = this.activeItem.quota();
//            if (quota != null) {
//                this.quotaCount.setValue(quota.getCount());
//                this.quotaBytes.setValue(quota.getBytes());
//            } else {
//                this.quotaCount.setValue(-1);
//                this.quotaBytes.setValue(-1);
//            }
//        }
//    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        // 监听选中变化
        this.treeView.selectItemChanged(this::initItem);
        // 过滤处理
        this.filterType.selectedIndexChanged((observable, oldValue, newValue) -> this.doFilter());
//        // undo监听
//        this.nodeData.undoableProperty().addListener((observableValue, aBoolean, t1) -> this.dataUndo.setDisable(!t1));
//        // redo监听
//        this.nodeData.redoableProperty().addListener((observableValue, aBoolean, t1) -> this.dataRedo.setDisable(!t1));
//        // 字符集选择事件
//        this.charset.selectedItemChanged((t3, t2, t1) -> this.showData());
//        // 切换显示监听
//        this.aclViewSwitch.selectedChanged((t3, t2, t1) -> this.initACL());
//        // 切换显示监听
//        this.statViewSwitch.selectedChanged((t3, t2, t1) -> this.initStat());
//        // 节点内容过滤
//        this.dataSearch.addTextChangeListener((observable, oldValue, newValue) -> this.nodeData.setHighlightText(newValue));
//        // 格式监听
//        this.format.selectedItemChanged((t1, t2, t3) -> {
//            if (this.format.isStringFormat()) {
//                this.showData(RichDataType.STRING);
//                this.nodeData.setEditable(true);
//            } else if (this.format.isJsonFormat()) {
//                this.showData(RichDataType.JSON);
//                this.nodeData.setEditable(true);
//            } else if (this.format.isXmlFormat()) {
//                this.showData(RichDataType.XML);
//                this.nodeData.setEditable(true);
//            } else if (this.format.isHtmlFormat()) {
//                this.showData(RichDataType.HTML);
//                this.nodeData.setEditable(true);
//            } else if (this.format.isBinaryFormat()) {
//                this.showData(RichDataType.BINARY);
//                this.nodeData.setEditable(false);
//            } else if (this.format.isHexFormat()) {
//                this.showData(RichDataType.HEX);
//                this.nodeData.setEditable(false);
//            } else if (this.format.isRawFormat()) {
//                this.showData(RichDataType.RAW);
//                this.nodeData.setEditable(this.nodeData.getRealType() == RichDataType.STRING);
//            }
//        });
//        // 节点内容变更
//        this.nodeData.addTextChangeListener((observable, oldValue, newValue) -> {
//            this.dataSave.enable();
//            if (this.activeItem != null) {
//                byte[] bytes = newValue == null ? new byte[]{} : newValue.getBytes(this.charset.getCharset());
//                this.activeItem.nodeData(bytes);
//                this.flushTabGraphicColor();
//            }
//        });
        // tab组件切换事件
        this.tabPane.selectedTabChanged((observable, oldValue, newValue) -> {
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
        NodeWidthResizer resizer = new NodeWidthResizer(this.leftBox, Cursor.DEFAULT, this::resizeLeft);
        resizer.widthLimit(240f, 750f);
        resizer.initResizeEvent();
        // 过滤
        KeyHandler searchKeyHandler = new KeyHandler();
        searchKeyHandler.setHandler(e -> this.filterKW.requestFocus());
        searchKeyHandler.setKeyCode(KeyCode.F);
        if (OSUtil.isMacOS()) {
            searchKeyHandler.setMetaDown(true);
        } else {
            searchKeyHandler.setControlDown(true);
        }
        searchKeyHandler.setKeyType(KeyEvent.KEY_RELEASED);
        KeyListener.addHandler(this.root, searchKeyHandler);
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
            this.tabPane.setLayoutX(newWidth);
            this.tabPane.setFlexWidth("100% - " + newWidth);
            this.leftBox.parentAutosize();
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

    /**
     * 恢复数据
     *
     * @param data 数据
     */
    public void restoreData(byte[] data) {
        // 保存数据历史
        this.activeItem.nodeData(data);
        this.dataTabController.showData();
    }

    /**
     * 执行过滤
     */
    @FXML
    private void doFilter() {
        String kw = this.filterKW.getTextTrim();
        // 过滤模式
        byte mode = this.filterKW.filterMode();
        // 过滤范围
        byte scope = this.filterKW.filterScope();
        // 过滤类型
        int type = this.filterType.getSelectedIndex();
        // 设置高亮是否匹配大小写
        this.treeView.setHighlightMatchCase(mode == 3 || mode == 1);
        // 仅在过滤路径的情况下设置节点高亮
        if (scope == 2 || scope == 0) {
            this.treeView.setHighlightText(kw);
        } else {
            this.treeView.setHighlightText(null);
        }
        // 仅在过滤数据的情况下设置内容高亮
        if (scope == 2 || scope == 1) {
            // this.nodeData.setHighlightText(kw);
            this.dataTabController.setDataHighlight(kw);
        // } else {
            // this.nodeData.setHighlightText(this.dataSearch.getTextTrim());
        }
        this.treeView.getItemFilter().setKw(kw);
        this.treeView.getItemFilter().setScope(scope);
        this.treeView.getItemFilter().setMatchMode(mode);
        this.treeView.getItemFilter().setType((byte) type);
        this.treeView.filter();
        // StageManager.showMask(this.treeView::filter);
    }

    /**
     * 定位节点
     */
    @FXML
    private void positionNode() {
        this.treeView.positionItem();
    }

    /**
     * 节点添加事件
     *
     * @param event 事件
     */
    @EventSubscribe
    public void onNodeAdded(ZKNodeAddedEvent event) {
        if (event.getZkConnect() == this.client.zkConnect()) {
            this.treeView.onNodeAdded(event.data());
        }
    }

    /**
     * 节点已添加事件
     *
     * @param event 事件
     */
    @EventSubscribe
    public void onNodeCreated(ZKNodeCreatedEvent event) {
        if (event.connect() == this.client.zkConnect()) {
            this.treeView.onNodeCreated(event.data());
        }
    }

    /**
     * 节点已删除事件
     *
     * @param event 事件
     */
    @EventSubscribe
    public void onNodeRemoved(ZKNodeRemovedEvent event) {
        if (event.connect() == this.client.zkConnect()) {
            this.treeView.onNodeRemoved(event.data());
        }
    }

    /**
     * 节点已变更事件
     *
     * @param event 事件
     */
    @EventSubscribe
    public void onNodeChanged(ZKNodeChangedEvent event) {
        if (event.connect() == this.client.zkConnect()) {
            this.treeView.onNodeChanged(event.data());
        }
    }

//    /**
//     * 节点访问控制已新增事件
//     *
//     * @param event 事件
//     */
//    @EventSubscribe
//    public void onNodeACLAdded(ZKNodeACLAddedEvent event) {
//        if (event.data() == this.client.zkConnect()) {
//            this.reloadACL();
//            if (this.aclPaging != null) {
//                this.renderACLView(this.aclPaging.lastPage());
//            }
//        }
//    }
//
//    /**
//     * 节点访问控制已变更事件
//     *
//     * @param event 事件
//     */
//    @EventSubscribe
//    public void onNodeACLUpdated(ZKNodeACLUpdatedEvent event) {
//        if (event.data() == this.client.zkConnect()) {
//            Long curPage = this.aclPaging == null ? null : this.aclPaging.currentPage();
//            this.reloadACL();
//            if (curPage != null) {
//                this.renderACLView(curPage);
//            }
//        }
//    }

    /**
     * 认证已执行事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void authAuthed(ZKAuthAuthedEvent event) {
        try {
            if (event.isSuccess() && event.client() == this.client) {
                this.treeView.authChanged(event.auth());
                this.flushTab();
            }
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    @FXML
    private void addNode() {
//        StageAdapter adapter = StageManager.parseStage(ZKNodeAddController.class);
//        adapter.setProp("dbItem", this.treeItem);
//        adapter.display();
        ZKViewFactory.addNode(null, this.client);
    }

    @FXML
    private void deleteNode() {
        if (this.activeItem != null) {
            this.activeItem.delete();
        }
    }

    @FXML
    private void collectNode() {
        if (this.activeItem != null) {
            if (this.collectPane.isCollect()) {
                this.activeItem.unCollect();
                this.collectPane.unCollect();
            } else {
                this.activeItem.collect();
                this.collectPane.collect();
            }
        }
    }

    @FXML
    private void refreshNode() {
        // 加载根节点
        StageManager.showMask(() -> {
            try {
                this.treeView.loadRoot();
            } catch (Exception ex) {
                this.closeTab();
                MessageBox.exception(ex);
            }
        });
    }

    @FXML
    private void sortTree() {
        if (this.sortPane.isAsc()) {
            this.treeView.sortAsc();
            this.sortPane.desc();
        } else {
            this.treeView.sortDesc();
            this.sortPane.asc();
        }
    }

    /**
     * 导入数据
     */
    @FXML
    private void importData() {
        ZKViewFactory.importData(this.client.zkConnect());
    }

    public ZKClient getClient() {
        return client;
    }

//    @FXML
//    public void doSearch() {
//        StageAdapter adapter = StageManager.getStage(ZKNodeSearchController.class);
//        if (adapter != null && adapter.getProp("zkConnect") == this.client.zkConnect()) {
//            adapter.toFront();
//        } else {
//            if (adapter != null) {
//                adapter.disappear();
//            }
//            adapter = StageManager.parseStage(ZKNodeSearchController.class);
//            adapter.setProp("zkConnect", this.client.zkConnect());
//            adapter.display();
//        }
//    }

//    /**
//     * 搜索触发事件
//     *
//     * @param event 事件
//     */
//    @EventSubscribe
//    private void onSearchTrigger(ZKSearchTriggerEvent event) {
//        if (event.data() == this.client.zkConnect()) {
//            ZKSearchParam param = event.param();
//            boolean found = this.treeView.onSearchTrigger(param);
//            // 设置搜索文本
//            if (found) {
//                this.nodeData.setSearchText(param.getKeyword());
//            }
//        }
//    }
//
//    /**
//     * 搜索结束事件
//     *
//     * @param event 事件
//     */
//    @EventSubscribe
//    private void onSearchFinish(ZKSearchFinishEvent event) {
//        if (event.data() == this.client.zkConnect()) {
//            this.treeView.onSearchFinish();
//            // 设置搜索文本
//            this.nodeData.setSearchText(this.dataSearch.getText());
//        }
//    }

    /**
     * 数据
     */
    @FXML
    private ZKNodeDataTabController dataTabController;

    /**
     * 状态
     */
    @FXML
    private ZKNodeStatTabController statTabController;

    /**
     * 权限
     */
    @FXML
    private ZKNodeACLTabController aclTabController;

    /**
     * 配额
     */
    @FXML
    private ZKNodeQuotaTabController quotaTabController;

    @Override
    public List<? extends RichTabController> getSubControllers() {
        return List.of(this.dataTabController, this.statTabController, this.aclTabController, this.quotaTabController);
    }
}