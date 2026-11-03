package cn.oyzh.easyshell.sqlite.foreignKey;

import cn.oyzh.easyshell.fx.sqlite.table.ShellSqliteIndexFieldTextFiled;
import cn.oyzh.easyshell.fx.sqlite.table.ShellSqliteTableComboBox;
import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.ComboBox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * SQLite 外键设计组件。
 *
 * @since 2026-11-02
 */
public class SQLiteForeignKeyControl extends SQLiteForeignKey {

    private static final List<String> POLICIES = Arrays.asList(
            "NO ACTION", "RESTRICT", "CASCADE", "SET NULL", "SET DEFAULT");

    private List<? extends SQLiteColumn> columnList;

    private List<? extends SQLiteColumn> primaryKeyColumnList;

    private ShellSqliteClient dbClient;

    private ShellSqliteIndexFieldTextFiled columnControl;

    private ShellSqliteTableComboBox referencedTableControl;

    private ShellSqliteIndexFieldTextFiled referencedColumnControl;

    public void setColumnList(List<? extends SQLiteColumn> columnList) {
        this.columnList = columnList;
        if (this.columnControl != null) {
            this.columnControl.setColumnList(columnList);
        }
    }

    public void setDbClient(ShellSqliteClient dbClient) {
        this.dbClient = dbClient;
        if (this.referencedTableControl != null) {
            this.referencedTableControl.init(this.getPrimaryKeyTable(), dbClient);
        }
        this.refreshReferencedColumns();
    }

    public ClearableTextField getNameControl() {
        ClearableTextField field = new ClearableTextField();
        field.setText(this.getName());
        field.setPromptText(I18nHelper.pleaseInputName());
        field.addTextChangeListener((observable, oldValue, newValue) -> {
            this.setName(newValue);
            this.setChanged(true);
        });
        TableViewUtil.rowOnCtrlS(field);
        TableViewUtil.selectRowOnMouseClicked(field);
        return field;
    }

    public ShellSqliteIndexFieldTextFiled getColumnControl() {
        if (this.columnList == null) {
            this.columnList = new ArrayList<>();
        }
        ShellSqliteIndexFieldTextFiled field = new ShellSqliteIndexFieldTextFiled(
                this.columnList, this.getColumns());
        this.columnControl = field;
        field.setFlexWidth("100% - 12");
        field.addTextChangeListener((observable, oldValue, newValue) -> {
            this.setColumns(field.getColumns());
            this.setChanged(true);
        });
        TableViewUtil.rowOnCtrlS(field);
        TableViewUtil.selectRowOnMouseClicked(field);
        return field;
    }

    public ShellSqliteTableComboBox getReferencedTableControl() {
        ShellSqliteTableComboBox comboBox = new ShellSqliteTableComboBox();
        this.referencedTableControl = comboBox;
        comboBox.init(this.getPrimaryKeyTable(), this.dbClient);
        comboBox.selectedItemChanged((observable, oldValue, newValue) -> {
            this.setPrimaryKeyTable(newValue);
            this.setChanged(true);
        });
        this.primaryKeyTableProperty().addListener((observable, oldValue, newValue) ->
                this.refreshReferencedColumns());
        TableViewUtil.rowOnCtrlS(comboBox);
        TableViewUtil.selectRowOnMouseClicked(comboBox);
        return comboBox;
    }

    public ShellSqliteIndexFieldTextFiled getReferencedColumnControl() {
        this.primaryKeyColumnList = this.loadPrimaryKeyColumns();
        ShellSqliteIndexFieldTextFiled field = new ShellSqliteIndexFieldTextFiled(
                this.primaryKeyColumnList, this.getPrimaryKeyColumns());
        this.referencedColumnControl = field;
        field.setFlexWidth("100% - 12");
        field.addTextChangeListener((observable, oldValue, newValue) -> {
            this.setPrimaryKeyColumns(field.getColumns());
            this.setChanged(true);
        });
        TableViewUtil.rowOnCtrlS(field);
        TableViewUtil.selectRowOnMouseClicked(field);
        return field;
    }

    private List<? extends SQLiteColumn> loadPrimaryKeyColumns() {
        if (this.dbClient == null || StringUtil.isBlank(this.getPrimaryKeyTable())) {
            return new ArrayList<>();
        }
        return this.dbClient.selectColumns(this.getPrimaryKeyTable());
    }

    private void refreshReferencedColumns() {
        this.primaryKeyColumnList = this.loadPrimaryKeyColumns();
        if (this.referencedColumnControl != null) {
            this.referencedColumnControl.setColumnList(this.primaryKeyColumnList);
            this.referencedColumnControl.setColumns(this.getPrimaryKeyColumns());
        }
    }

    public ComboBox<String> getDeletePolicyControl() {
        return this.policyControl(this.getDeletePolicy(), this::setDeletePolicy);
    }

    public ComboBox<String> getUpdatePolicyControl() {
        return this.policyControl(this.getUpdatePolicy(), this::setUpdatePolicy);
    }

    private ComboBox<String> policyControl(String value, java.util.function.Consumer<String> consumer) {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().setAll(POLICIES);
        comboBox.setValue(StringUtil.blankToDefault(value, "NO ACTION"));
        comboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            consumer.accept(newValue);
            this.setChanged(true);
        });
        TableViewUtil.selectRowOnMouseClicked(comboBox);
        return comboBox;
    }

    public static SQLiteForeignKeyControl of(SQLiteForeignKey foreignKey) {
        SQLiteForeignKeyControl control = new SQLiteForeignKeyControl();
        control.copy(foreignKey);
        return control;
    }

    public static List<SQLiteForeignKeyControl> of(List<SQLiteForeignKey> foreignKeys) {
        List<SQLiteForeignKeyControl> controls = new ArrayList<>();
        if (foreignKeys != null) {
            for (SQLiteForeignKey foreignKey : foreignKeys) {
                controls.add(of(foreignKey));
            }
        }
        return controls;
    }
}
