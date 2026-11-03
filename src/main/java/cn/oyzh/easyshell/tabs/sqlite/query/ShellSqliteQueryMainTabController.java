package cn.oyzh.easyshell.tabs.sqlite.query;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.data.sqlite.dto.SQLiteDataExportTable;
import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.event.sqlite.ShellSqliteEventUtil;
import cn.oyzh.easyshell.fx.ShellDataEditor;
import cn.oyzh.easyshell.fx.sqlite.SQLiteResultTableView;
import cn.oyzh.easyshell.query.sqlite.SQLiteExecuteResult;
import cn.oyzh.easyshell.query.sqlite.ShellSqliteQueryEditor;
import cn.oyzh.easyshell.query.sqlite.ShellSqliteQueryUtil;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;
import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.easyshell.sqlite.ShellSqliteHelper;
import cn.oyzh.easyshell.store.ShellQueryStore;
import cn.oyzh.easyshell.util.sqlite.ShellSqliteViewFactory;
import cn.oyzh.fx.db.query.DBQueryResults;
import cn.oyzh.fx.gui.svg.glyph.ExportSVGGlyph;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.controls.pane.FXSplitPane;
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.keyboard.KeyboardUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Tab;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite 查询控制器。
 *
 * @since 2026-11-02
 */
public class ShellSqliteQueryMainTabController extends RichTabController {

    private ShellQuery query;

    private ShellSqliteClient client;

    private boolean unsaved;

    @FXML
    private ShellSqliteQueryEditor queryArea;

    @FXML
    private FXSplitPane splitPane;

    @FXML
    private FXTabPane resultTabPane;

    @FXML
    private Tab infoTab;

    @FXML
    private Tab resultTab;

    @FXML
    private SQLiteResultTableView resultTable;

    private SQLiteExecuteResult currentResult;

    @FXML
    private ShellDataEditor resultInfo;

    public ShellQuery getQuery() {
        return this.query;
    }

    public boolean isUnsaved() {
        return this.unsaved;
    }

    public void init(ShellQuery query, ShellSqliteClient client) {
        this.query = query;
        this.client = client;
        this.queryArea.setText(query.getContent());
        this.queryArea.forgetHistory();
        this.queryArea.addTextChangeListener((observable, oldValue, newValue) -> {
            this.unsaved = true;
            this.flushTab();
        });
        this.showNode(0);
    }

    @Override
    protected void bindListeners() {
        this.queryArea.setRunCallback(this::run);
        super.bindListeners();
    }

    @FXML
    private void pretty() {
        this.queryArea.pretty();
    }

    @FXML
    private void run() {
        String sql = this.queryArea.isSelectedText() ? this.queryArea.getSelectedText() : this.queryArea.getTextTrim();
        StageManager.showMask(this.getWindow(), () -> this.doRun(sql));
    }

    private void doRun(String sql) {
        try {
            this.resultTabPane.disable();
            DBQueryResults<SQLiteExecuteResult> results = this.client.executeSql(sql);
            this.resultInfo.setText(ShellSqliteHelper.formatResults(results));
            this.showResults(results, I18nHelper.result());
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        } finally {
            this.resultTabPane.enable();
        }
    }

    @FXML
    private void explain() {
        String sql = this.queryArea.isSelectedText() ? this.queryArea.getSelectedText() : this.queryArea.getTextTrim();
        StageManager.showMask(this.getWindow(), () -> this.doExplain(sql));
    }

    private void doExplain(String sql) {
        try {
            this.resultTabPane.disable();
            DBQueryResults<SQLiteExecuteResult> results = this.client.explainSql(sql);
            this.resultInfo.setText(ShellSqliteHelper.formatResults(results));
            this.showResults(results, I18nHelper.explain());
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        } finally {
            this.resultTabPane.enable();
        }
    }

    private void showResults(DBQueryResults<SQLiteExecuteResult> results, String title) {
        this.clearDynamicResultTabs();
        List<SQLiteExecuteResult> dataResults = new ArrayList<>();
        if (results.getResults() != null) {
            for (SQLiteExecuteResult result : results.getResults()) {
                if (result.isSuccess() && !result.getColumns().isEmpty()) {
                    dataResults.add(result);
                }
            }
        }
        this.resultTable.setResult(dataResults.isEmpty() ? null : dataResults.getFirst());
        this.currentResult = dataResults.isEmpty() ? null : dataResults.getFirst();
        this.resultTab.setText(title + " 1");
        for (int index = 1; index < dataResults.size(); index++) {
            FXTab tab = new FXTab(title + " " + (index + 1),
                    this.resultContent(dataResults.get(index)));
            tab.setClosable(false);
            this.resultTabPane.addTab(tab);
        }
        boolean hasResult = !dataResults.isEmpty();
        FXUtil.runLater(() -> {
            this.showNode(hasResult ? 2 : 1);
            if (hasResult) {
                this.resultTabPane.select(1);
            } else {
                this.resultTabPane.select(0);
            }
        });
    }

    private void clearDynamicResultTabs() {
        List<Tab> tabs = new ArrayList<>();
        for (Tab tab : this.resultTabPane.getTabs()) {
            if (tab != this.resultTab && tab != this.infoTab) {
                tabs.add(tab);
            }
        }
        this.resultTabPane.getTabs().removeAll(tabs);
    }

    @FXML
    private void exportResult() {
        this.exportResult(this.currentResult);
    }

    private void exportResult(SQLiteExecuteResult result) {
        if (result == null || result.getColumns().isEmpty()) {
            MessageBox.warn(I18nHelper.pleaseSelectTable(), this.getWindow());
            return;
        }
        try {
            SQLiteDataExportTable exportTable = new SQLiteDataExportTable();
            String name = this.query == null || StringUtil.isBlank(this.query.getName())
                    ? "query_result" : this.query.getName();
            exportTable.setSelected(true);
            exportTable.setName(name);
            exportTable.columns(result.getColumns());
            exportTable.setRecords(result.getRecords().stream().map(record -> {
                java.util.Map<String, Object> values = new java.util.LinkedHashMap<>();
                for (SQLiteColumn column : result.getColumns()) {
                    values.put(column.getName(), record.get(column));
                }
                return values;
            }).toList());
            ShellSqliteViewFactory.exportData(this.client, null, 1, exportTable);
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    private FXVBox resultContent(SQLiteExecuteResult result) {
        SQLiteResultTableView tableView = new SQLiteResultTableView();
        tableView.setResult(result);
        ExportSVGGlyph export = new ExportSVGGlyph("16");
        export.setOnMousePrimaryClicked(event -> this.exportResult(result));
        FXHBox toolbar = new FXHBox(export);
        toolbar.setPrefHeight(30);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(5, 0, 0, 10));
        FXVBox content = new FXVBox(toolbar, tableView);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        return content;
    }

    private void showNode(int type) {
        if (type == 0) {
            this.resultTabPane.disappear();
            this.splitPane.setShowDivider(false);
            this.splitPane.setDividerPositions(1, 0);
        } else {
            this.resultTabPane.display();
            this.splitPane.setShowDivider(true);
            this.splitPane.setDividerPositions(0.45, 0.55);
        }
    }

    @FXML
    private void save() {
        try {
            boolean newQuery = this.query.isNew();
            if (newQuery && StringUtil.isBlank(this.query.getName())) {
                String name = MessageBox.prompt(I18nHelper.pleaseInputName(), this.getWindow());
                if (name == null) {
                    return;
                }
                this.query.setName(name);
            }
            if (newQuery) {
                this.query.setIid(this.client.iid());
                this.query.setDbName("main");
            }
            this.query.setContent(this.queryArea.getText());
            boolean success = newQuery
                    ? ShellQueryStore.INSTANCE.insert(this.query)
                    : ShellQueryStore.INSTANCE.update(this.query);
            if (success) {
                this.unsaved = false;
                ShellSqliteEventUtil.queryChanged(this.client, this.query);
                this.flushTab();
            } else {
                MessageBox.warn(I18nHelper.operationFail(), this.getWindow());
            }
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    @FXML
    private void queryKeyPressed(KeyEvent event) {
        if (KeyboardUtil.isCtrlS(event)) {
            this.save();
        } else if (KeyboardUtil.isCtrlR(event)) {
            this.run();
        }
    }
}
