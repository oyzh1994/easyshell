package cn.oyzh.easyshell.tabs.zk.query;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.query.zk.ShellZKQueryEditor;
import cn.oyzh.easyshell.query.zk.ShellZKQueryParam;
import cn.oyzh.easyshell.query.zk.ShellZKQueryResult;
import cn.oyzh.easyshell.store.ShellQueryStore;
import cn.oyzh.easyshell.trees.query.ShellQueryTreeItem;
import cn.oyzh.easyshell.trees.query.ShellQueryTreeView;
import cn.oyzh.easyshell.zk.ShellZKClient;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.keyboard.KeyboardUtil;
import cn.oyzh.fx.plus.node.NodeWidthResizer;
import cn.oyzh.fx.plus.window.StageManager;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.TreeItem;
import javafx.scene.input.KeyEvent;

/**
 * @author oyzh
 * @since 2025/01/20
 */
public class ShellZKQueryTabController extends RichTabController {

    /**
     * 查询对象
     */
    private ShellQuery query;

    /// **
    // * 未保存标志位
    // */
    // private boolean unsaved;

    // public boolean isUnsaved() {
    //    return unsaved;
    //}
    public ShellQuery getQuery() {
        return query;
    }

    /**
     * zk客户端
     */
    private ShellZKClient zkClient;

    /**
     * 当前内容
     */
    @FXML
    private ShellZKQueryEditor content;

    /**
     * 结果面板
     */
    @FXML
    private FXTabPane resultTabPane;

    /**
     * 查询列表
     */
    @FXML
    private ShellQueryTreeView queryTreeView;

    /**
     * 右边组件
     */
    @FXML
    private FXVBox rightBox;

    /**
     * 查询存储
     */
    private final ShellQueryStore queryStore = ShellQueryStore.INSTANCE;

    public ShellConnect shellConnect() {
        return this.zkClient.getShellConnect();
    }

    /**
     * 初始化
     *
     * @param client 客户端
     */
    public void init(ShellZKClient client) {
        this.zkClient = client;
        this.content.setClient(client);
        // 初始化查询数据
        this.queryTreeView.setIid(client.iid());
    }

    @FXML
    private void save() {
        try {
            this.query.setContent(this.content.getText());
            this.queryStore.update(this.query);
            // this.unsaved = false;
            this.setUnsaved(false);
            // this.flushTab();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 设置未保存状态
     *
     * @param unsaved 未保存状态
     */
    private void setUnsaved(boolean unsaved) {
        TreeItem<?> item = this.queryTreeView.getSelectedItem();
        if (item instanceof ShellQueryTreeItem queryTreeItem) {
            queryTreeItem.setUnsaved(unsaved);
            queryTreeItem.refresh();
        }
    }

    /**
     * 运行查询
     */
    @FXML
    private void run() {
        StageManager.showMask(() -> {
            try {
                ShellZKQueryParam param = new ShellZKQueryParam();
                param.setContent(this.content.getText());
                ShellZKQueryResult result = this.zkClient.query(param);
                this.content.flexHeight("30% - 40");
                this.resultTabPane.setVisible(true);
                this.resultTabPane.clearChild();
                this.resultTabPane.addTab(new ShellZKQueryMsgTab(param, result));
                if (param.isGet()) {
                    if (result.isSuccess()) {
                        this.resultTabPane.addTab(new ShellZKQueryDataTab(param.getPath(), result.asData(), this.zkClient));
                        if (param.hasParamStat()) {
                            this.resultTabPane.addTab(new ShellZKQueryStatTab(result.getStat()));
                        }
                        this.resultTabPane.select(1);
                    } else {
                        this.resultTabPane.select(0);
                    }
                } else if (param.isLs() || param.isLs2()) {
                    if (result.isSuccess()) {
                        this.resultTabPane.addTab(new ShellZKQueryNodeTab(param.getPath(), result.asNode()));
                        if (param.hasParamStat()) {
                            this.resultTabPane.addTab(new ShellZKQueryStatTab(result.getStat()));
                        }
                        this.resultTabPane.select(1);
                    } else {
                        this.resultTabPane.select(0);
                    }
                } else if (param.isGetEphemerals()) {
                    if (result.isSuccess()) {
                        String path = param.getPath() == null ? "/" : param.getPath();
                        this.resultTabPane.addTab(new ShellZKQueryNodeTab(path, result.asNode()));
                        this.resultTabPane.select(1);
                    } else {
                        this.resultTabPane.select(0);
                    }
//            } else if (param.isGetAllChildrenNumber()) {
//                if (result.isSuccess()) {
//                    this.resultTabPane.addTab(new ZKQueryCountTab(result.asCount()));
//                    this.resultTabPane.select(1);
//                } else {
//                    this.resultTabPane.select(0);
//                }
                } else if (param.isWhoami()) {
                    if (result.isSuccess()) {
                        this.resultTabPane.addTab(new ShellZKQueryWhoamiTab(result.asClientInfo()));
                        this.resultTabPane.select(1);
                    } else {
                        this.resultTabPane.select(0);
                    }
                } else if (param.isSrvr() || param.isEnvi() || param.isMntr() || param.isConf() || param.isStat4()) {
                    if (result.isSuccess()) {
                        this.resultTabPane.addTab(new ShellZKQueryEnvTab(result.asEnvInfo()));
                        this.resultTabPane.select(1);
                    } else {
                        this.resultTabPane.select(0);
                    }
                } else if (param.isSet() || param.isSetACL()) {
                    if (result.isSuccess() && param.hasParamStat()) {
                        this.resultTabPane.addTab(new ShellZKQueryStatTab(result.getStat()));
                    }
                    this.resultTabPane.select(0);
                } else if (param.isGetACL()) {
                    if (result.isSuccess()) {
                        this.resultTabPane.addTab(new ShellZKQueryACLTab(result.asACL()));
                        if (param.hasParamStat()) {
                            this.resultTabPane.addTab(new ShellZKQueryStatTab(result.getStat()));
                        }
                        this.resultTabPane.select(1);
                    } else {
                        this.resultTabPane.select(0);
                    }
                } else if (param.isCreate() || param.isSync() || param.isSetQuota() || param.isRmr() || param.isDeleteall()
                        || param.isDelete()) {
                    this.resultTabPane.select(0);
                } else if (param.isStat()) {
                    if (result.isSuccess()) {
                        this.resultTabPane.addTab(new ShellZKQueryStatTab(result.getStat()));
                        this.resultTabPane.select(1);
                    } else {
                        this.resultTabPane.select(0);
                    }
                } else if (param.isListquota()) {
                    if (result.isSuccess()) {
                        this.resultTabPane.addTab(new ShellZKQueryQuotaTab(result.asQuota()));
                        this.resultTabPane.select(1);
                    } else {
                        this.resultTabPane.select(0);
                    }
                }
                this.content.parentAutosize();
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
//            } finally {
//                this.enableTab();
            }
        });
    }

    /**
     * 内容按键事件
     *
     * @param event 事件
     */
    @FXML
    private void onContentKeyPressed(KeyEvent event) {
        if (KeyboardUtil.isCtrlS(event)) {
            this.save();
        } else if (KeyboardUtil.isCtrlR(event)) {
            this.run();
        }
    }

    //@Override
    // public void onTabCloseRequest(Event event) {
    //    if (this.unsaved && !MessageBox.confirm(I18nHelper.unsavedAndContinue())) {
    //        event.consume();
    //    } else {
    //        super.onTabCloseRequest(event);
    //    }
    //}

    @Override
    protected void bindListeners() {
        super.bindListeners();
        // 监听内容变化
        this.content.addTextChangeListener((observable, oldValue, newValue) -> {
            // this.unsaved = true;
            // this.flushTab();
            if (this.query != null && !StringUtil.equals(newValue, this.query.getContent())) {
                this.setUnsaved(true);
            }
        });
        // 查询选择事件
        this.queryTreeView.selectedItemChanged((ChangeListener<TreeItem<?>>) (observableValue, snippet, t1) -> {
            if (t1 instanceof ShellQueryTreeItem item) {
                this.doEdit(item.value());
            } else {
                this.doEdit(null);
            }
        });
        // 查询新增回调
        this.queryTreeView.setAddCallback(this::doEdit);
        // 查询编辑回调
        this.queryTreeView.setEditCallback(this::doEdit);
        // 查询删除回调
        this.queryTreeView.setDeleteCallback(this::doDelete);
        // 拉伸辅助
        NodeWidthResizer resizer = new NodeWidthResizer(this.queryTreeView, Cursor.DEFAULT, this::resizeLeft);
        resizer.widthLimit(240f, 750f);
        resizer.initResizeEvent();
    }

    /**
     * 左侧组件重新布局
     *
     * @param newWidth 新宽度
     */
    private void resizeLeft(Float newWidth) {
        if (newWidth != null && !Float.isNaN(newWidth)) {
            // 设置组件宽
            this.queryTreeView.setRealWidth(newWidth);
            // this.rightBox.setLayoutX(newWidth);
            this.rightBox.setFlexWidth("100% - " + newWidth);
            // this.queryTreeView.parentAutosize();
        }
    }

    /**
     * 编辑查询
     *
     * @param query 查询
     */
    private void doEdit(ShellQuery query) {
        this.query = query;
        if (query == null) {
            this.content.clear();
        } else {
            this.content.setText(query.getContent());
        }
    }

    /**
     * 删除查询
     *
     * @param query 查询
     */
    private void doDelete(ShellQuery query) {
        if (query == this.query) {
            this.query = null;
            this.content.clear();
        }
    }

    @Override
    public void destroy() {
        this.content.destroy();
        this.resultTabPane.destroy();
        this.queryTreeView.destroy();
        super.destroy();
    }
}