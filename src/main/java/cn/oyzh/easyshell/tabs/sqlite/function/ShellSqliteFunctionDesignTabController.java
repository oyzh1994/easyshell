package cn.oyzh.easyshell.tabs.sqlite.function;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.event.sqlite.ShellSqliteEventUtil;
import cn.oyzh.easyshell.fx.ShellDataEditor;
import cn.oyzh.easyshell.fx.sqlite.SQLiteFunctionKindComboBox;
import cn.oyzh.easyshell.fx.sqlite.SQLiteTableSelectTextField;
import cn.oyzh.easyshell.query.sqlite.ShellSqliteQueryEditor;
import cn.oyzh.easyshell.sqlite.function.SQLiteFunction;
import cn.oyzh.easyshell.sqlite.generator.function.SQLiteFunctionCreateSqlGenerator;
import cn.oyzh.easyshell.sqlite.schema.SQLiteSchemaManager;
import cn.oyzh.easyshell.sqlite.table.SQLiteTable;
import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.ui.DBFiledTypeComboBox;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.gui.tabs.RichTabPane;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.controls.tab.FixedTab;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.i18n.I18nHelper;
import cn.oyzh.i18n.I18nResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;

/**
 * SQLite 函数设计器控制器。
 *
 * <p>SQLite 不支持持久化存储函数，设计动作分别生成 SQL 表达式、表函数视图或生成列。</p>
 *
 * @since 2026-11-02
 */
public class ShellSqliteFunctionDesignTabController extends RichTabController {

    @FXML
    private SQLiteFunctionKindComboBox kind;

    @FXML
    private FXVBox expressionBox;

    @FXML
    private FXVBox viewDefinitionBox;

    @FXML
    private FXVBox generatedColumnBox;

    @FXML
    private SQLiteTableSelectTextField targetTable;

    @FXML
    private DBFiledTypeComboBox returnType;

    @FXML
    private FXComboBox<String> generatedStorage;

    @FXML
    private ShellSqliteQueryEditor expression;

    @FXML
    private ShellSqliteQueryEditor definition;

    @FXML
    private ShellDataEditor preview;

    @FXML
    private RichTabPane tabPane;

    @FXML
    private FixedTab previewTab;

    private ShellSqliteClient client;

    private SQLiteFunction function;

    private String originalName;

    private String functionName;

    private boolean unsaved;

    public void init(ShellSqliteClient client, SQLiteFunction function) {
        this.client = client;
        this.function = function == null ? new SQLiteFunction() : function;
        this.originalName = function == null ? null : function.getName();
        this.functionName = this.originalName;
        String functionKind = StringUtil.blankToDefault(this.function.getKind(), SQLiteFunction.KIND_SQL_EXPRESSION);
        this.kind.select(this.kind.getItems().contains(functionKind)
                ? functionKind : SQLiteFunction.KIND_SQL_EXPRESSION);
        this.targetTable.setTables(this.client.selectTables()
                .stream()
                .map(SQLiteTable::getName)
                .toList());
        this.returnType.setDialect(DBDialect.SQLITE);
        this.generatedStorage.getItems().setAll("VIRTUAL", "STORED");
        this.generatedStorage.setValue(StringUtil.blankToDefault(this.function.getGeneratedStorage(), "VIRTUAL"));
        this.targetTable.text(StringUtil.blankToDefault(this.function.getTargetTable(), ""));
        String returnType = StringUtil.blankToDefault(this.function.getReturnType(), "TEXT");
        if (!this.returnType.getItems().contains(returnType)) {
            this.returnType.addItem(returnType);
        }
        this.returnType.select(returnType);
        this.expression.text(StringUtil.blankToDefault(this.function.getExpression(), ""));
        this.definition.text(StringUtil.blankToDefault(this.function.getDefinition(), ""));
        this.expression.forgetHistory();
        this.definition.forgetHistory();
        this.unsaved = function == null;
        this.updateKindLayout();
        this.kind.valueProperty().addListener((observable, oldValue, newValue) -> {
            this.updateKindLayout();
            this.markUnsaved();
        });
        this.targetTable.textProperty().addListener((observable, oldValue, newValue) -> this.markUnsaved());
        this.returnType.selectedItemProperty().addListener((observable, oldValue, newValue) -> this.markUnsaved());
        this.generatedStorage.valueProperty().addListener((observable, oldValue, newValue) -> this.markUnsaved());
        this.expression.textProperty().addListener((observable, oldValue, newValue) -> this.markUnsaved());
        this.definition.textProperty().addListener((observable, oldValue, newValue) -> this.markUnsaved());
    }

    private void markUnsaved() {
        this.unsaved = true;
        this.flushTab();
    }

    private void updateKindLayout() {
        String kind = this.kind.getValue();
        boolean tableView = SQLiteFunction.KIND_TABLE_VIEW.equals(kind);
        boolean generatedColumn = SQLiteFunction.KIND_GENERATED_COLUMN.equals(kind);
        if (tableView) {
            NodeUtil.disappear(this.expressionBox);
            NodeUtil.display(this.viewDefinitionBox);
        } else {
            NodeUtil.display(this.expressionBox);
            NodeUtil.disappear(this.viewDefinitionBox);
        }
        if (generatedColumn) {
            NodeUtil.display(this.generatedColumnBox);
        } else {
            NodeUtil.disappear(this.generatedColumnBox);
        }
        String hintKey;
        if (tableView) {
            hintKey = "shell.sqlite.function.hint.tableView";
        } else if (generatedColumn) {
            hintKey = "shell.sqlite.function.hint.generatedColumn";
        } else {
            hintKey = "shell.sqlite.function.hint.expression";
        }
        this.kind.setTipText(I18nResourceBundle.i18nString(hintKey));
    }

    private SQLiteFunction buildFunction(String name) {
        SQLiteFunction result = new SQLiteFunction();
        result.setKind(this.kind.getValue());
        result.setName(name);
        if (SQLiteFunction.KIND_GENERATED_COLUMN.equals(this.kind.getValue())) {
            result.setTargetTable(this.targetTable.getText().trim());
            result.setReturnType(StringUtil.blankToDefault(this.returnType.getSelectedItem(), "").trim());
            result.setGeneratedStorage(this.generatedStorage.getValue());
            result.setExpression(this.expression.getText().trim());
        } else if (SQLiteFunction.KIND_TABLE_VIEW.equals(this.kind.getValue())) {
            result.setDefinition(this.definition.getText().trim());
        } else {
            result.setExpression(this.expression.getText().trim());
        }
        return result;
    }

    @FXML
    private void createPreview() {
        this.initPreview();
        this.previewTab.selectTab();
    }

    private void initPreview() {
        try {
            String name = StringUtil.blankToDefault(this.functionName, "Unnamed_Function");
            this.preview.setText(SQLiteFunctionCreateSqlGenerator.createFunction(this.buildFunction(name)));
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    @FXML
    private void save() {
        try {
            String targetName = this.originalName;
            if (StringUtil.isBlank(targetName)) {
                targetName = MessageBox.prompt(I18nHelper.pleaseInputFunctionName(),
                        this.functionName, this.getWindow());
                if (targetName == null) {
                    return;
                }
                targetName = targetName.trim();
                if (StringUtil.isBlank(targetName)) {
                    MessageBox.warn(I18nHelper.pleaseInputFunctionName(), this.getWindow());
                    return;
                }
            }
            SQLiteFunction target = this.buildFunction(targetName);
            String previousName = this.originalName;
            new SQLiteSchemaManager(this.client).saveFunction(previousName, target);
            this.function = target;
            this.originalName = targetName;
            this.functionName = targetName;
            this.unsaved = false;
            ShellSqliteEventUtil.functionChanged(this.client, previousName, target);
            this.initPreview();
            this.flushTab();
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    public String functionName() {
        return this.functionName;
    }

    public boolean isUnsaved() {
        return this.unsaved;
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        NodeUtil.nodeOnCtrlS(this.getTab(), this::save);
        NodeUtil.nodeOnCtrlS(this.expression, this::save);
        NodeUtil.nodeOnCtrlS(this.definition, this::save);
        NodeUtil.nodeOnCtrlS(this.targetTable, this::save);
        NodeUtil.nodeOnCtrlS(this.returnType, this::save);
        NodeUtil.nodeOnCtrlS(this.generatedStorage, this::save);
        NodeUtil.nodeOnCtrlS(this.preview, this::save);
        this.tabPane.selectedItemChanged((observable, oldTab, newTab) -> {
            if (newTab == this.previewTab) {
                this.initPreview();
            }
        });
    }
}
