package cn.oyzh.easyshell.tabs.redis.query;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.fx.redis.ShellRedisDatabaseComboBox;
import cn.oyzh.easyshell.query.redis.ShellRedisQueryEditor;
import cn.oyzh.easyshell.query.redis.ShellRedisQueryParam;
import cn.oyzh.easyshell.query.redis.ShellRedisQueryResult;
import cn.oyzh.easyshell.redis.ShellRedisClient;
import cn.oyzh.easyshell.store.ShellQueryStore;
import cn.oyzh.easyshell.trees.query.ShellQueryTreeItem;
import cn.oyzh.easyshell.trees.query.ShellQueryTreeView;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.keyboard.KeyboardUtil;
import cn.oyzh.fx.plus.node.NodeWidthResizer;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.TreeItem;
import javafx.scene.input.KeyEvent;

/**
 * @author oyzh
 * @since 2025/02/06
 */
public class ShellRedisQueryTabController extends SubTabController {

    /**
     * 查询对象
     */
    private ShellQuery query;

    /// **
    // * 未保存标志位
    // */
    //private boolean unsaved;
    //
    //public boolean isUnsaved() {
    //    return unsaved;
    //}
    public ShellQuery getQuery() {
        return query;
    }

    /**
     * zk客户端
     */
    private ShellRedisClient client;

    /**
     * 当前内容
     */
    @FXML
    private ShellRedisQueryEditor content;

    /**
     * 数据库
     */
    @FXML
    private ShellRedisDatabaseComboBox database;

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
        return this.client.shellConnect();
    }

    /**
     * 初始化
     *
     * @param client 客户端
     */
    public void init(ShellRedisClient client) {
        this.client = client;
        this.content.setClient(client);
        // 初始化数据库
        this.database.setDbCount(client.databases());
        this.database.selectFirst();
        // 初始化查询数据
        this.queryTreeView.setIid(client.iid());
    }

    /**
     * 保存
     */
    @FXML
    private void save() {
        try {
            this.query.setContent(this.content.getText());
            this.query.setDbIndex(this.database.getSelectedIndex());
            this.queryStore.update(this.query);
            this.setUnsaved(false);
            //this.unsaved = false;
            //this.flushTab();
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
        try {
            this.disableTab();
            ShellRedisQueryParam param = new ShellRedisQueryParam();
            param.setContent(this.content.getText());
            param.setDbIndex(this.database.getSelectedIndex());
            ShellRedisQueryResult result = this.client.query(param);
            this.content.flexHeight("30% - 60");
            this.resultTabPane.setVisible(true);
            this.resultTabPane.clearChild();
            this.resultTabPane.addTab(new ShellRedisQueryMsgTab(param, result));
            if (result.hasData()) {
                this.resultTabPane.addTab(new ShellRedisQueryDataTab(result.getResult()));
                this.resultTabPane.select(1);
            }
            this.content.parentAutosize();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        } finally {
            this.enableTab();
        }
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
    //public void onTabCloseRequest(Event event) {
    //    if (this.unsaved && !MessageBox.confirm(I18nHelper.unsavedAndContinue())) {
    //        event.consume();
    //    } else {
    //        super.onTabCloseRequest(event);
    //    }
    //}

    @Override
    protected void bindListeners() {
        super.bindListeners();
        // 监听数据库变化
        this.database.selectedIndexChanged((observable, oldValue, newValue) -> {
            //this.unsaved = true;
            //this.flushTab();
            this.setUnsaved(true);
            this.content.setDbIndex(newValue.intValue());
        });
        // 监听内容变化
        this.content.addTextChangeListener((observable, oldValue, newValue) -> {
            if (this.query != null && !StringUtil.equals(newValue, this.query.getContent())) {
                this.setUnsaved(true);
            }
            //this.unsaved = true;
            //this.flushTab();
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
            //this.rightBox.setLayoutX(newWidth);
            this.rightBox.setFlexWidth("100% - " + newWidth);
            //this.queryTreeView.parentAutosize();
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
}