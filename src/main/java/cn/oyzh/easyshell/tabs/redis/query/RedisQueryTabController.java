package cn.oyzh.easyshell.tabs.redis.query;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.redis.RedisQuery;
import cn.oyzh.easyshell.fx.redis.RedisDatabaseComboBox;
import cn.oyzh.easyshell.query.redis.RedisQueryEditor;
import cn.oyzh.easyshell.query.redis.RedisQueryParam;
import cn.oyzh.easyshell.query.redis.RedisQueryResult;
import cn.oyzh.easyshell.redis.RedisClient;
import cn.oyzh.easyshell.store.redis.RedisQueryStore;
import cn.oyzh.easyshell.trees.redis.query.ShellRedisQueryTreeItem;
import cn.oyzh.easyshell.trees.redis.query.ShellRedisQueryTreeView;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.keyboard.KeyboardUtil;
import cn.oyzh.i18n.I18nHelper;
import javafx.beans.value.ChangeListener;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.input.KeyEvent;

/**
 * @author oyzh
 * @since 2025/02/06
 */
public class RedisQueryTabController extends RichTabController {

    /**
     * 查询对象
     */
    private RedisQuery query;

    /**
     * 未保存标志位
     */
    private boolean unsaved;

    public boolean isUnsaved() {
        return unsaved;
    }

    public RedisQuery getQuery() {
        return query;
    }

    /**
     * zk客户端
     */
    private RedisClient redisClient;

    /**
     * 当前内容
     */
    @FXML
    private RedisQueryEditor content;

    /**
     * 数据库
     */
    @FXML
    private RedisDatabaseComboBox database;

    /**
     * 结果面板
     */
    @FXML
    private FXTabPane resultTabPane;

    /**
     * 片段列表
     */
    @FXML
    private ShellRedisQueryTreeView queryTreeView;

    /**
     * 查询存储
     */
    private final RedisQueryStore queryStore = RedisQueryStore.INSTANCE;

    public ShellConnect shellConnect() {
        return this.redisClient.shellConnect();
    }

    public void init(RedisClient client) {
        this.redisClient = client;
        this.content.setClient(client);
        // 初始化数据库
        this.database.setDbCount(client.databases());
        this.database.selectFirst();
        // this.database.setInitIndex(query.getDbIndex());
        // 监听数据库变化
        this.database.selectedIndexChanged((observable, oldValue, newValue) -> {
            this.unsaved = true;
            this.flushTab();
            this.content.setDbIndex(newValue.intValue());
        });
        // 监听内容变化
        this.content.addTextChangeListener((observable, oldValue, newValue) -> {
            this.unsaved = true;
            this.flushTab();
        });
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
            this.unsaved = false;
            this.flushTab();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 运行
     */
    @FXML
    private void run() {
        try {
            this.disableTab();
            RedisQueryParam param = new RedisQueryParam();
            param.setContent(this.content.getText());
            param.setDbIndex(this.database.getSelectedIndex());
            RedisQueryResult result = this.redisClient.query(param);
            this.content.flexHeight("30% - 60");
            this.resultTabPane.setVisible(true);
            this.resultTabPane.clearChild();
            this.resultTabPane.addTab(new RedisQueryMsgTab(param, result));
            if (result.hasData()) {
                this.resultTabPane.addTab(new RedisQueryDataTab(result.getResult()));
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
     * 内容键入事件
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

    @Override
    public void onTabCloseRequest(Event event) {
        if (this.unsaved && !MessageBox.confirm(I18nHelper.unsavedAndContinue())) {
            event.consume();
        } else {
            super.onTabCloseRequest(event);
        }
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        // 片段选择事件
        this.queryTreeView.selectedItemChanged((ChangeListener<TreeItem<?>>) (observableValue, snippet, t1) -> {
            if (t1 instanceof ShellRedisQueryTreeItem item) {
                this.doEdit(item.value());
            } else {
                this.doEdit(null);
            }
        });
        // 片段新增回调
        this.queryTreeView.setAddCallback(this::doEdit);
        // 片段编辑回调
        this.queryTreeView.setEditCallback(this::doEdit);
        // 片段删除回调
        this.queryTreeView.setDeleteCallback(this::doDelete);
    }

    /**
     * 编辑片段
     *
     * @param query 片段
     */
    private void doEdit(RedisQuery query) {
        this.query = query;
        if (query == null) {
            this.content.clear();
        } else {
            this.content.setText(query.getContent());
        }
    }

    /**
     * 删除片段
     *
     * @param query 片段
     */
    private void doDelete(RedisQuery query) {
        if (query == this.query) {
            this.query = null;
            this.content.clear();
        }
    }
}