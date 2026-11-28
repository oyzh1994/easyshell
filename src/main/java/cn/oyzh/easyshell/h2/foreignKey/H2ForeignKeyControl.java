package cn.oyzh.easyshell.h2.foreignKey;

import cn.oyzh.easyshell.fx.h2.table.ShellH2IndexFieldTextFiled;
import cn.oyzh.easyshell.fx.h2.table.ShellH2TableComboBox;
import cn.oyzh.easyshell.h2.ShellH2Client;
import cn.oyzh.easyshell.h2.column.H2Column;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.ComboBox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * H2 外键设计组件。
 *
 * @since 2026-11-02
 */
public class H2ForeignKeyControl extends H2ForeignKey {

    private static final List<String> POLICIES = Arrays.asList(
            "NO ACTION", "RESTRICT", "CASCADE", "SET NULL", "SET DEFAULT");

    private List<? extends H2Column> columnList;

    private List<? extends H2Column> referencedColumnList;

    private ShellH2Client dbClient;

    private ShellH2IndexFieldTextFiled columnControl;

    private ShellH2TableComboBox referencedTableControl;

    private ShellH2IndexFieldTextFiled referencedColumnControl;

    public void setColumnList(List<? extends H2Column> columnList) {
        this.columnList = columnList;
        if (this.columnControl != null) {
            this.columnControl.setColumnList(columnList);
        }
    }

    public void setDbClient(ShellH2Client dbClient) {
        this.dbClient = dbClient;
        if (this.referencedTableControl != null) {
            this.referencedTableControl.init(this.getReferencedTable(), dbClient);
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

    public ShellH2IndexFieldTextFiled getColumnControl() {
        if (this.columnList == null) {
            this.columnList = new ArrayList<>();
        }
        ShellH2IndexFieldTextFiled field = new ShellH2IndexFieldTextFiled(
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

    public ShellH2TableComboBox getReferencedTableControl() {
        ShellH2TableComboBox comboBox = new ShellH2TableComboBox();
        this.referencedTableControl = comboBox;
        comboBox.init(this.getReferencedTable(), this.dbClient);
        comboBox.selectedItemChanged((observable, oldValue, newValue) -> {
            this.setReferencedTable(newValue);
            this.setChanged(true);
        });
        this.referencedTableProperty().addListener((observable, oldValue, newValue) ->
                this.refreshReferencedColumns());
        TableViewUtil.rowOnCtrlS(comboBox);
        TableViewUtil.selectRowOnMouseClicked(comboBox);
        return comboBox;
    }

    public ShellH2IndexFieldTextFiled getReferencedColumnControl() {
        this.referencedColumnList = this.loadReferencedColumns();
        ShellH2IndexFieldTextFiled field = new ShellH2IndexFieldTextFiled(
                this.referencedColumnList, this.getReferencedColumns());
        this.referencedColumnControl = field;
        field.setFlexWidth("100% - 12");
        field.addTextChangeListener((observable, oldValue, newValue) -> {
            this.setReferencedColumns(field.getColumns());
            this.setChanged(true);
        });
        TableViewUtil.rowOnCtrlS(field);
        TableViewUtil.selectRowOnMouseClicked(field);
        return field;
    }

    private List<? extends H2Column> loadReferencedColumns() {
        if (this.dbClient == null || StringUtil.isBlank(this.getReferencedTable())) {
            return new ArrayList<>();
        }
        return this.dbClient.selectColumns(this.getReferencedTable());
    }

    private void refreshReferencedColumns() {
        this.referencedColumnList = this.loadReferencedColumns();
        if (this.referencedColumnControl != null) {
            this.referencedColumnControl.setColumnList(this.referencedColumnList);
            this.referencedColumnControl.setColumns(this.getReferencedColumns());
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

    public static H2ForeignKeyControl of(H2ForeignKey foreignKey) {
        H2ForeignKeyControl control = new H2ForeignKeyControl();
        control.copy(foreignKey);
        return control;
    }

    public static List<H2ForeignKeyControl> of(List<H2ForeignKey> foreignKeys) {
        List<H2ForeignKeyControl> controls = new ArrayList<>();
        if (foreignKeys != null) {
            for (H2ForeignKey foreignKey : foreignKeys) {
                controls.add(of(foreignKey));
            }
        }
        return controls;
    }
}
