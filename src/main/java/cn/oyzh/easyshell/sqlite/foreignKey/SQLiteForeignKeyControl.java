package cn.oyzh.easyshell.sqlite.foreignKey;

import cn.oyzh.easyshell.sqlite.generator.SQLiteGeneratorUtil;

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

    public ClearableTextField getColumnControl() {
        ClearableTextField field = new ClearableTextField();
        field.setText(this.columnsText());
        field.setPromptText(I18nHelper.pleaseInputContent());
        field.addTextChangeListener((observable, oldValue, newValue) -> {
            this.setColumns(this.split(newValue));
            this.setChanged(true);
        });
        TableViewUtil.rowOnCtrlS(field);
        TableViewUtil.selectRowOnMouseClicked(field);
        return field;
    }

    public ClearableTextField getReferencedTableControl() {
        ClearableTextField field = new ClearableTextField();
        field.setText(this.getReferencedTable());
        field.setPromptText(I18nHelper.pleaseInputContent());
        field.addTextChangeListener((observable, oldValue, newValue) -> {
            this.setReferencedTable(newValue);
            this.setChanged(true);
        });
        TableViewUtil.rowOnCtrlS(field);
        TableViewUtil.selectRowOnMouseClicked(field);
        return field;
    }

    public ClearableTextField getReferencedColumnControl() {
        ClearableTextField field = new ClearableTextField();
        field.setText(this.referencedColumnsText());
        field.setPromptText(I18nHelper.pleaseInputContent());
        field.addTextChangeListener((observable, oldValue, newValue) -> {
            this.setReferencedColumns(this.split(newValue));
            this.setChanged(true);
        });
        TableViewUtil.rowOnCtrlS(field);
        TableViewUtil.selectRowOnMouseClicked(field);
        return field;
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

    private List<String> split(String value) {
        List<String> list = new ArrayList<>();
        if (StringUtil.isNotBlank(value)) {
            for (String item : value.split("\\s*,\\s*")) {
                if (StringUtil.isNotBlank(item)) {
                    list.add(SQLiteGeneratorUtil.unquote(item.trim()));
                }
            }
        }
        return list;
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
