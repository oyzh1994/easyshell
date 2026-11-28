package cn.oyzh.easyshell.h2.column;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.util.h2.ShellH2ColumnUtil;
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
 * H2 表设计字段控制组件。
 *
 * @since 2026-11-02
 */
public class H2ColumnControl extends H2Column {

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
        comboBox.setDialect(DBDialect.H2);
        comboBox.selectedItemChanged((observable, oldValue, newValue) -> {
            this.setType(newValue);
            this.setChanged(true);
        });
        String type = StringUtil.blankToDefault(this.getType(), "CHARACTER VARYING").toUpperCase();
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

    /**
     * 创建注释编辑控件。
     *
     * @return 注释编辑控件
     */
    public ClearableTextField getCommentControl() {
        ClearableTextField field = new ClearableTextField();
        field.setPromptText(I18nHelper.pleaseInputComment());
        field.setFlexWidth("100% - 12");
        field.setText(this.getComment());
        field.addTextChangeListener((observable, oldValue, newValue) -> {
            this.setComment(newValue);
            this.setChanged(true);
        });
        TableViewUtil.rowOnCtrlS(field);
        TableViewUtil.selectRowOnMouseClicked(field);
        return field;
    }

    /**
     * 创建小数位编辑控件。
     *
     * @return 小数位编辑控件
     */
    public NumberTextField getDigitsControl() {
        NumberTextField field = new NumberTextField();
        field.setFlexWidth("100% - 12");
        if (this.getDigits() != null) {
            field.setValue(this.getDigits());
        }
        field.addTextChangeListener((observable, oldValue, newValue) -> {
            this.setDigits(field.getIntValue());
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

    public static H2ColumnControl of(H2Column column) {
        H2ColumnControl control = new H2ColumnControl();
        control.copy(column);
        return control;
    }

    public static List<H2ColumnControl> of(List<H2Column> columns) {
        List<H2ColumnControl> controls = new ArrayList<>();
        if (columns != null) {
            for (H2Column column : columns) {
                controls.add(of(column));
            }
        }
        return controls;
    }

    public boolean isInvalid() {
        return StringUtil.isBlank(this.getName()) || StringUtil.isBlank(this.getType());
    }
}
