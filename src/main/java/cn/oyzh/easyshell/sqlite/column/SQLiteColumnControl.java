package cn.oyzh.easyshell.sqlite.column;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.util.sqlite.ShellSqliteColumnUtil;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.ui.DBFiledTypeComboBox;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.text.field.NumberTextField;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.i18n.I18nHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite 表设计字段控制组件。
 *
 * @since 2026-11-02
 */
public class SQLiteColumnControl extends SQLiteColumn {

    public ClearableTextField getNameControl() {
        ClearableTextField field = new ClearableTextField();
        field.setPromptText(I18nHelper.pleaseInputName());
        field.setText(this.getName());
        field.addTextChangeListener((observable, oldValue, newValue) -> {
            this.setName(newValue);
            this.setChanged(true);
        });
        TableViewUtil.rowOnCtrlS(field);
        TableViewUtil.selectRowOnMouseClicked(field);
        return field;
    }

    public DBFiledTypeComboBox getTypeControl() {
        DBFiledTypeComboBox comboBox = new DBFiledTypeComboBox();
        comboBox.setDialect(DBDialect.SQLITE);
        comboBox.selectedItemChanged((observable, oldValue, newValue) -> {
            this.setType(newValue);
            this.setChanged(true);
        });
        String type = StringUtil.blankToDefault(this.getType(), "TEXT").toUpperCase();
        comboBox.setIgnoreChanged(true);
        try {
            if (StringUtil.isBlank(this.getType())) {
                this.setType(type);
            }
            if (comboBox.getItems().stream().noneMatch(item -> StringUtil.equalsIgnoreCase(item, type))) {
                comboBox.getItems().add(0, type);
            }
            comboBox.selectFirstIfNull(type);
        } finally {
            comboBox.setIgnoreChanged(false);
        }
        TableViewUtil.rowOnCtrlS(comboBox);
        TableViewUtil.selectRowOnMouseClicked(comboBox);
        return comboBox;
    }

    public NumberTextField getSizeControl() {
        NumberTextField field = new NumberTextField();
        field.setPromptText(I18nHelper.pleaseInputContent());
        field.setFlexWidth("100% - 12");
        if (this.getSize() != null) {
            field.setValue(this.getSize());
        }
        field.addTextChangeListener((observable, oldValue, newValue) -> {
            this.setSize(field.getIntValue());
            this.setChanged(true);
        });
        TableViewUtil.rowOnCtrlS(field);
        TableViewUtil.selectRowOnMouseClicked(field);
        return field;
    }

    public FXCheckBox getPrimaryKeyControl() {
        FXCheckBox checkBox = new FXCheckBox();
        checkBox.setSelected(this.isPrimaryKey());
        checkBox.selectedChanged((observable, oldValue, newValue) -> {
            this.setPrimaryKey(newValue);
            this.setChanged(true);
        });
        TableViewUtil.rowOnCtrlS(checkBox);
        TableViewUtil.selectRowOnMouseClicked(checkBox);
        return checkBox;
    }

    public FXCheckBox getNullableControl() {
        FXCheckBox checkBox = new FXCheckBox();
        checkBox.setSelected(this.isNullable());
        checkBox.selectedChanged((observable, oldValue, newValue) -> {
            this.setNullable(newValue);
            this.setChanged(true);
        });
        this.primaryKeyProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                checkBox.setSelected(false);
            }
        });
        TableViewUtil.rowOnCtrlS(checkBox);
        TableViewUtil.selectRowOnMouseClicked(checkBox);
        return checkBox;
    }

    public FXCheckBox getUniqueControl() {
        FXCheckBox checkBox = new FXCheckBox();
        checkBox.setSelected(this.isUnique());
        checkBox.selectedChanged((observable, oldValue, newValue) -> {
            this.setUnique(newValue);
            this.setChanged(true);
        });
        TableViewUtil.rowOnCtrlS(checkBox);
        TableViewUtil.selectRowOnMouseClicked(checkBox);
        return checkBox;
    }

    public static SQLiteColumnControl of(SQLiteColumn column) {
        SQLiteColumnControl control = new SQLiteColumnControl();
        control.copy(column);
        return control;
    }

    public static List<SQLiteColumnControl> of(List<SQLiteColumn> columns) {
        List<SQLiteColumnControl> controls = new ArrayList<>();
        if (columns != null) {
            for (SQLiteColumn column : columns) {
                controls.add(of(column));
            }
        }
        return controls;
    }

    public boolean isInvalid() {
        return StringUtil.isBlank(this.getName()) || StringUtil.isBlank(this.getType());
    }
}
