package cn.oyzh.easyshell.tabs.mysql.query;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.mysql.query.MysqlExecuteResult;
import cn.oyzh.easyshell.mysql.query.MysqlExplainResult;
import cn.oyzh.easyshell.mysql.query.MysqlQueryResults;
import cn.oyzh.easyshell.query.mysql.MysqlQueryEditor;
import cn.oyzh.easyshell.query.mysql.MysqlQueryUtil;
import cn.oyzh.easyshell.store.ShellQueryStore;
import cn.oyzh.easyshell.trees.mysql.database.MysqlDatabaseTreeItem;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.keyboard.KeyboardUtil;
import cn.oyzh.fx.plus.node.NodeHeightResizer;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.input.KeyEvent;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * db查询内容组件
 *
 * @author oyzh
 * @since 2024/02/18
 */
public class MysqlQueryMainTabController extends RichTabController {

    /**
     * 查询对象
     */
    private ShellQuery query;

    /**
     * 未保存标志位
     */
    private boolean unsaved;

    public ShellQuery getQuery() {
        return query;
    }

    /**
     * db数据库树节点
     */
    private MysqlDatabaseTreeItem dbItem;

    public MysqlDatabaseTreeItem getDbItem() {
        return dbItem;
    }

    /**
     * 查询文本域
     */
    @FXML
    private MysqlQueryEditor queryArea;

    /**
     * 结果文本域
     */
    @FXML
    private FXTabPane resultTabPane;

    /**
     * 根节点
     */
    @FXML
    private FXVBox root;

    /**
     * 结果信息tab
     */
    @FXML
    private MysqlQueryInfoTab infoTab;

    /**
     * tab组件
     */
    private MysqlQueryMainTab tab;

    /**
     * 执行初始化
     *
     * @param query  查询对象
     * @param dbItem db库树节点
     */
    public void init(MysqlQueryMainTab tab, ShellQuery query, MysqlDatabaseTreeItem dbItem) {
        this.tab = tab;
        this.query = query;
        this.dbItem = dbItem;
        this.showNode(0);
        this.queryArea.setText(query.getContent());
        this.queryArea.forgetHistory();
        this.queryArea.setDialect(this.dbItem.dialect());
        this.queryArea.addTextChangeListener((observable, oldValue, newValue) -> {
            this.unsaved = true;
            this.flushTab();
            // this.tab.setContentChanged(true)
        });
        MysqlQueryUtil.updateIndex(dbItem.client());
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
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
    }

    @Override
    public void onTabInit(FXTab tab) {
        super.onTabInit(tab);
        // 初始化拉伸事件
        NodeHeightResizer.of(this.resultTabPane, this::onResultTabPaneResize, 150f, 650f);
    }

    /**
     * 结果组件拉伸事件
     *
     * @param newHeight 新高度
     */
    private void onResultTabPaneResize(double newHeight) {
        this.resultTabPane.setFlexHeight("");
        this.resultTabPane.setRealHeight(newHeight);
        this.resultTabPane.setFlexY("100% - " + newHeight);
        double newSize = 35 + newHeight;
        this.queryArea.setFlexHeight("100% - " + newSize);
    }

    /**
     * 清理tab组件
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
            MessageBox.exception(ex);
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
        StageManager.showMask(() -> this.doRun(sql));
    }

    /**
     * 执行运行
     *
     * @param sql sql
     */
    private void doRun(String sql) {
        try {
            this.resultTabPane.disable();
            MysqlQueryResults<MysqlExecuteResult> results = this.dbItem.executeSql(sql);
            this.clearTabs();
            int showType = 1;
            this.initInfoTab(results);
            if (!results.isEmpty()) {
                int index = 1;
                this.initInfoTab(results);
                for (MysqlExecuteResult result : results.getResults()) {
                    if (result.isSuccess()) {
                        FXTab fxTab = this.initSelectTab(result, I18nHelper.result() + index++);
                        showType = 2;
                        this.resultTabPane.addTab(fxTab);
                    }
                }
                if (showType == 2) {
                    this.resultTabPane.select(1);
                } else {
                    this.resultTabPane.selectFirst();
                }
            }
            this.showNode(showType);
        } catch (Exception ex) {
            MessageBox.exception(ex);
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
            MysqlQueryResults<MysqlExplainResult> results = this.dbItem.explainSql(sql);
            this.clearTabs();
            int showType = 1;
            this.initInfoTab(results);
            if (!results.isEmpty()) {
                int index = 1;
                this.initInfoTab(results);
                for (MysqlExplainResult result : results.getResults()) {
                    if (result.isSuccess()) {
                        FXTab fxTab = this.initExplainTab(result, I18nHelper.explain() + index++);
                        showType = 2;
                        this.resultTabPane.addTab(fxTab);
                    }
                }
                if (showType == 2) {
                    this.resultTabPane.select(1);
                } else {
                    this.resultTabPane.selectFirst();
                }
            }
            this.showNode(showType);
        } catch (Exception ex) {
            MessageBox.exception(ex);
        } finally {
            this.resultTabPane.enable();
        }
    }

    /**
     * 初始化信息tab组件
     *
     * @param results 结果
     */
    private void initInfoTab(MysqlQueryResults<?> results) {
        this.infoTab.init(results);
    }

    /**
     * 初始化查询tab组件
     *
     * @param result 结果
     * @param title  标题
     * @return tab组件
     */
    private MysqlQuerySelectTab initSelectTab(MysqlExecuteResult result, String title) {
        MysqlQuerySelectTab selectTab = new MysqlQuerySelectTab();
        selectTab.init(title, result, this.dbItem);
        selectTab.setId("resultTab");
        selectTab.setProp("result", result);
        return selectTab;
    }

    /**
     * 初始化解释tab组件
     *
     * @param result 结果
     * @param title  标题
     * @return tab组件
     */
    private MysqlQueryExplainTab initExplainTab(MysqlExplainResult result, String title) {
        MysqlQueryExplainTab selectTab = new MysqlQueryExplainTab();
        selectTab.init(title, result);
        selectTab.setId("explainTab");
        selectTab.setProp("result", result);
        return selectTab;
    }

    /**
     * 保存查询
     */
    @FXML
    private void save() {
        try {
            String name = this.query.getName();
            if (name == null) {
                name = MessageBox.prompt(I18nHelper.pleaseInputName());
                if (name == null) {
                    return;
                }
                this.query.setName(name);
            }
            String sql = this.queryArea.getTextTrim();
            this.query.setContent(sql);
            this.query.setDbName(this.dbItem.dbName());
            this.query.setIid(this.dbItem.info().getId());
            boolean result;
            // 新增查询
            if (StringUtil.isBlank(this.query.getName())) {
                result = ShellQueryStore.INSTANCE.insert(this.query);
                if (result) {
                    // MysqlEventUtil.queryAdded(this.query, this.dbItem);
                    this.dbItem.getQueryTypeChild().addQuery(this.query);
                }
            } else {// 修改查询
                result = ShellQueryStore.INSTANCE.update(this.query);
            }
            if (!result) {
                MessageBox.warn(I18nHelper.operationFail());
            } else {
                // this.tab.setContentChanged(false);
                this.unsaved = false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        } finally {
            this.flushTab();
        }
    }

    /**
     * 查询组件按钮点击事件
     *
     * @param e 事件
     */
    @FXML
    private void queryKeyPressed(KeyEvent e) {
        if (KeyboardUtil.isCtrlS(e)) {
            this.save();
        } else if (KeyboardUtil.isCtrlR(e)) {
            this.run();
        }
    }

    /**
     * 显示组件
     *
     * @param type 类型
     */
    private void showNode(int type) {
        // 信息
        if (type == 0) {
            this.queryArea.setFlexHeight("100% - 35");
            this.resultTabPane.disappear();
        } else if (type == 1 || type == 2) {
            this.queryArea.setFlexHeight("30% - 35");
            this.resultTabPane.setFlexHeight("70%");
            this.resultTabPane.display();
        }
        this.root.autosize();
    }

    public boolean isUnsaved() {
        return unsaved;
    }
}
