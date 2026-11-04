package cn.oyzh.easyshell.tabs.sqlite.query;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.event.sqlite.ShellSqliteEventUtil;
import cn.oyzh.easyshell.query.sqlite.SQLiteExecuteResult;
import cn.oyzh.easyshell.query.sqlite.ShellSqliteQueryEditor;
import cn.oyzh.easyshell.store.ShellQueryStore;
import cn.oyzh.easyshell.trees.sqlite.root.ShellSqliteRootTreeItem;
import cn.oyzh.fx.db.query.DBQueryResults;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.pane.FXSplitPane;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.keyboard.KeyboardUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.input.KeyEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite 查询标签页控制器
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellSqliteQueryMainTabController extends RichTabController {

    /**
     * 查询对象
     */
    private ShellQuery query;

    /**
     * 未保存标志位
     */
    private boolean unsaved;

    /**
     * 数据库树节点
     */
    private ShellSqliteRootTreeItem dbItem;

    /**
     * 查询文本域
     */
    @FXML
    private ShellSqliteQueryEditor queryArea;

    /**
     * 结果标签页组件
     */
    @FXML
    private FXTabPane resultTabPane;

    /**
     * 分割面板
     */
    @FXML
    private FXSplitPane splitPane;

    /**
     * 结果信息标签页
     */
    @FXML
    private ShellSqliteQueryInfoTab infoTab;

    /**
     * 获取查询对象
     *
     * @return 查询对象
     */
    public ShellQuery getQuery() {
        return this.query;
    }

    /**
     * 获取数据库树节点
     *
     * @return 数据库树节点
     */
    public ShellSqliteRootTreeItem getDbItem() {
        return this.dbItem;
    }

    /**
     * 初始化
     *
     * @param query  查询对象
     * @param dbItem 数据库树节点
     */
    public void init(ShellQuery query, ShellSqliteRootTreeItem dbItem) {
        this.resultTabPane.setProp("query", query);
        this.query = query;
        this.dbItem = dbItem;
        this.showNode(0);
        this.queryArea.setText(query.getContent());
        this.queryArea.forgetHistory();
        this.queryArea.setDialect(this.dbItem.dialect());
        this.queryArea.addTextChangeListener((observable, oldValue, newValue) -> {
            this.unsaved = true;
            this.flushTab();
        });
    }

    @Override
    protected void bindListeners() {
        this.resultTabPane.setupRefreshListener();
        this.resultTabPane.selectedItemChanged((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (StringUtil.equals(newValue.getId(), "infoTab")) {
                    this.showNode(1);
                } else if (StringUtil.equals(newValue.getId(), "resultTab")) {
                    this.showNode(2);
                }
            }
        });
        this.queryArea.setRunCallback(this::run);
        super.bindListeners();
    }

    /**
     * 清理标签页组件
     */
    private void clearTabs() {
        List<Tab> removes = new ArrayList<>();
        for (Tab tab : this.resultTabPane.getTabs()) {
            if (!tab.equals(this.infoTab)) {
                removes.add(tab);
            }
        }
        this.resultTabPane.removeTab(removes);
    }

    /**
     * 美化
     */
    @FXML
    private void pretty() {
        try {
            this.queryArea.pretty();
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    /**
     * 运行
     */
    @FXML
    private void run() {
        String sql;
        if (this.queryArea.isSelectedText()) {
            sql = this.queryArea.getSelectedText();
        } else {
            sql = this.queryArea.getTextTrim();
        }
        StageManager.showMask(this.getWindow(), () -> this.doRun(sql));
    }

    /**
     * 执行运行
     *
     * @param sql SQL
     */
    private void doRun(String sql) {
        try {
            this.resultTabPane.disable();
            DBQueryResults<SQLiteExecuteResult> results = this.dbItem.executeSql(sql);
            this.clearTabs();
            int showType = 1;
            this.initInfoTab(results);
            if (!results.isEmpty()) {
                int index = 1;
                for (SQLiteExecuteResult result : results.getResults()) {
                    if (result.isSuccess()) {
                        FXTab fxTab = this.initSelectTab(result, ShellI18nHelper.numberedLabel(I18nHelper.result(), index++));
                        this.resultTabPane.addTab(fxTab);
                        if (result.hasResult()) {
                            showType = 2;
                        }
                    }
                }
            }
            if (showType == 2) {
                FXUtil.runLater(() -> this.resultTabPane.select(1));
            } else {
                FXUtil.runLater(() -> this.resultTabPane.selectFirst());
            }
            this.showNode(showType);
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        } finally {
            this.resultTabPane.enable();
        }
    }

    /**
     * 解释
     */
    @FXML
    private void explain() {
        try {
            String sql = this.queryArea.getTextTrim();
            this.resultTabPane.disable();
            DBQueryResults<SQLiteExecuteResult> results = this.dbItem.explainSql(sql);
            this.clearTabs();
            int showType = 1;
            this.initInfoTab(results);
            if (!results.isEmpty()) {
                int index = 1;
                for (SQLiteExecuteResult result : results.getResults()) {
                    if (result.isSuccess()) {
                        FXTab fxTab = this.initExplainTab(result, ShellI18nHelper.numberedLabel(I18nHelper.explain(), index++));
                        this.resultTabPane.addTab(fxTab);
                        if (result.hasResult()) {
                            showType = 2;
                        }
                    }
                }
            }
            if (showType == 2) {
                FXUtil.runLater(() -> this.resultTabPane.select(1));
            } else {
                FXUtil.runLater(() -> this.resultTabPane.selectFirst());
            }
            this.showNode(showType);
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        } finally {
            this.resultTabPane.enable();
        }
    }

    /**
     * 初始化信息标签页组件
     *
     * @param results 结果
     */
    private void initInfoTab(DBQueryResults<?> results) {
        this.infoTab.init(results);
    }

    /**
     * 初始化查询标签页组件
     *
     * @param result 结果
     * @param title  标题
     * @return 标签页组件
     */
    private ShellSqliteQuerySelectTab initSelectTab(SQLiteExecuteResult result, String title) {
        ShellSqliteQuerySelectTab selectTab = new ShellSqliteQuerySelectTab();
        selectTab.init(title, result, this.dbItem);
        selectTab.setId("resultTab");
        selectTab.setProp("result", result);
        return selectTab;
    }

    /**
     * 初始化解释标签页组件
     *
     * @param result 结果
     * @param title  标题
     * @return 标签页组件
     */
    private ShellSqliteQueryExplainTab initExplainTab(SQLiteExecuteResult result, String title) {
        ShellSqliteQueryExplainTab explainTab = new ShellSqliteQueryExplainTab();
        explainTab.init(title, result);
        explainTab.setId("explainTab");
        explainTab.setProp("result", result);
        return explainTab;
    }

    /**
     * 保存查询
     */
    @FXML
    private void save() {
        try {
            String name = this.query.getName();
            if (name == null) {
                name = MessageBox.prompt(I18nHelper.pleaseInputName(), this.getWindow());
                if (name == null) {
                    return;
                }
                this.query.setName(name);
            }
            this.query.setContent(this.queryArea.getTextTrim());
            this.query.setDbName(this.dbItem.dbName());
            this.query.setIid(this.dbItem.info().getId());
            boolean result;
            if (this.query.isNew()) {
                result = ShellQueryStore.INSTANCE.insert(this.query);
                if (result) {
                    this.dbItem.upsertQuery(this.query);
                }
            } else {
                result = ShellQueryStore.INSTANCE.update(this.query);
            }
            if (!result) {
                MessageBox.warn(I18nHelper.operationFail(), this.getWindow());
            } else {
                this.unsaved = false;
                ShellSqliteEventUtil.queryChanged(this.dbItem.client(), this.query);
            }
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        } finally {
            this.flushTab();
        }
    }

    /**
     * 查询组件按钮点击事件
     *
     * @param event 事件
     */
    @FXML
    private void queryKeyPressed(KeyEvent event) {
        if (KeyboardUtil.isCtrlS(event)) {
            this.save();
        } else if (KeyboardUtil.isCtrlR(event)) {
            this.run();
        }
    }

    /**
     * 显示组件
     *
     * @param type 类型
     */
    private void showNode(int type) {
        if (type == 0) {
            this.resultTabPane.disappear();
            if (this.splitPane.isShowDivider()) {
                this.splitPane.setShowDivider(false);
                this.splitPane.setDividerPositions(1, 0);
            }
        } else {
            this.resultTabPane.display();
            if (!this.splitPane.isShowDivider()) {
                this.splitPane.setShowDivider(true);
                this.splitPane.setDividerPositions(0.3, 0.7);
            }
        }
    }

    /**
     * 是否未保存
     *
     * @return 是否未保存
     */
    public boolean isUnsaved() {
        return this.unsaved;
    }
}
