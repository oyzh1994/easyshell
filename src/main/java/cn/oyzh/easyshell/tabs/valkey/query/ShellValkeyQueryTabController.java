package cn.oyzh.easyshell.tabs.valkey.query;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.query.valkey.ShellValkeyQueryEditor;
import cn.oyzh.easyshell.query.valkey.ShellValkeyQueryParam;
import cn.oyzh.easyshell.query.valkey.ShellValkeyQueryResult;
import cn.oyzh.easyshell.valkey.ShellValkeyClient;
import cn.oyzh.easyshell.store.ShellQueryStore;
import cn.oyzh.easyshell.trees.valkey.database.ShellValkeyDatabaseTreeItem;
import cn.oyzh.easyshell.trees.valkey.query.ShellValkeyQueriesTreeItem;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.pane.FXSplitPane;
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.keyboard.KeyboardUtil;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.scene.input.KeyEvent;

/**
 * redis查询tab内容组件
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellValkeyQueryTabController extends RichTabController {

    /**
     * 查询对象
     */
    private ShellQuery query;

    /**
     * 数据库节点
     */
    private ShellValkeyDatabaseTreeItem dbItem;

    /**
     * redis客户端
     */
    private ShellValkeyClient client;

    /**
     * 未保存标志位
     */
    private boolean unsaved;

    /**
     * 当前内容
     */
    @FXML
    private ShellValkeyQueryEditor content;

    /**
     * 分割面板
     */
    @FXML
    private FXSplitPane splitPane;

    /**
     * 结果面板
     */
    @FXML
    private FXTabPane resultTabPane;

    /**
     * 查询存储
     */
    private final ShellQueryStore queryStore = ShellQueryStore.INSTANCE;

    /**
     * 获取查询对象
     *
     * @return 查询对象
     */
    public ShellQuery getQuery() {
        return query;
    }

    /**
     * 获取数据库节点
     *
     * @return 数据库节点
     */
    public ShellValkeyDatabaseTreeItem dbItem() {
        return dbItem;
    }

    /**
     * 获取数据库索引
     *
     * @return 数据库索引
     */
    public int dbIndex() {
        return this.dbItem.dbIndex();
    }

    /**
     * 是否未保存
     *
     * @return 结果
     */
    public boolean isUnsaved() {
        return unsaved;
    }

    /**
     * 获取shell连接
     *
     * @return shell连接
     */
    public ShellConnect shellConnect() {
        return this.client.shellConnect();
    }

    /**
     * 初始化
     *
     * @param query  查询对象
     * @param dbItem 数据库节点
     */
    public void init(ShellQuery query, ShellValkeyDatabaseTreeItem dbItem) {
        this.query = query;
        this.dbItem = dbItem;
        this.client = dbItem.client();
        this.content.setClient(this.client);
        this.content.setDbIndex(this.dbIndex());
        this.content.setText(query.getContent() == null ? "" : query.getContent());
        this.content.forgetHistory();
        // 监听内容变化
        this.content.addTextChangeListener((observable, oldValue, newValue) -> {
            if (StringUtil.notEquals(newValue, this.query.getContent())) {
                this.unsaved = true;
                this.flushTab();
            }
        });
        this.flushTab();
    }

    /**
     * 重命名查询
     *
     * @param name 查询名称
     */
    public void renameQuery(String name) {
        if (this.query != null) {
            this.query.setName(name);
            this.flushTab();
        }
    }

    /**
     * 保存
     */
    @FXML
    private void save() {
        try {
            String name = this.query.getName();
            if (StringUtil.isBlank(name)) {
                name = MessageBox.prompt(I18nHelper.pleaseInputName(), this.getWindow());
                if (StringUtil.isBlank(name)) {
                    return;
                }
                this.query.setName(name);
            }
            this.query.setContent(this.content.getText());
            this.query.setDbIndex(this.dbIndex());
            this.query.setIid(this.client.iid());
            boolean result;
            if (this.query.isNew()) {// 新增查询
                result = this.queryStore.insert(this.query);
                if (result) {
                    ShellValkeyQueriesTreeItem queriesItem = this.dbItem.getQueryChild();
                    if (queriesItem != null) {
                        queriesItem.addQuery(this.query);
                    }
                }
            } else {// 修改查询
                result = this.queryStore.update(this.query);
            }
            if (result) {
                this.unsaved = false;
                this.flushTab();
            } else {
                MessageBox.warn(I18nHelper.operationFail(), this.getWindow());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, this.getWindow());
        }
    }

    /**
     * 运行查询
     */
    @FXML
    private void run() {
        try {
            this.disableTab();
            ShellValkeyQueryParam param = new ShellValkeyQueryParam();
            param.setContent(this.content.getText());
            param.setDbIndex(this.dbIndex());
            ShellValkeyQueryResult result = this.client.query(param);
            if (!this.splitPane.isShowDivider()) {
                this.splitPane.setShowDivider(true);
                this.splitPane.setDividerPositions(0.3, 0.7);
            }
            this.resultTabPane.setVisible(true);
            this.resultTabPane.clearChild();
            this.resultTabPane.addTab(ShellValkeyQueryMsgTab.of(param, result));
            if (result.hasData()) {
                this.resultTabPane.addTab(ShellValkeyQueryDataTab.of(result.getResult()));
                this.resultTabPane.select(1);
            }
            this.content.parentAutosize();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, this.getWindow());
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
}
