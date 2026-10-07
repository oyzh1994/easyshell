package cn.oyzh.easyshell.dameng.column;

import cn.oyzh.easyshell.dameng.column.DamengColumn;
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
 * 达梦数据库字段编辑控件
 *
 * @author oyzh
 * @since 2024/09/14
 */
public class DamengColumnControl extends DamengColumn {

    /**
     * 创建名称编辑控件
     *
     * @return 名称编辑控件
     */
    public ClearableTextField getNameControl() {
        ClearableTextField textField = new ClearableTextField();
        textField.setPromptText(I18nHelper.pleaseInputName());
        textField.setText(this.getName());
        textField.addTextChangeListener((observable, oldValue, newValue) -> this.setName(newValue));
        TableViewUtil.rowOnCtrlS(textField);
        TableViewUtil.selectRowOnMouseClicked(textField);
        return textField;
    }

    /**
     * 创建注释编辑控件
     *
     * @return 注释编辑控件
     */
    public ClearableTextField getCommentControl() {
        ClearableTextField textField = new ClearableTextField();
        textField.setPromptText(I18nHelper.pleaseInputComment());
        textField.setFlexWidth("100% - 12");
        textField.setText(this.getComment());
        textField.addTextChangeListener((observable, oldValue, newValue) -> this.setComment(newValue));
        TableViewUtil.rowOnCtrlS(textField);
        TableViewUtil.selectRowOnMouseClicked(textField);
        return textField;
    }

    /**
     * 创建长度编辑控件
     *
     * @return 长度编辑控件
     */
    public NumberTextField getSizeControl() {
        NumberTextField textField = new NumberTextField();
        textField.setFlexWidth("100% - 12");
        TableViewUtil.rowOnCtrlS(textField);
        if (this.getSize() != null) {
            textField.setValue(this.getSize());
        } else if (this.supportSize() && this.isCreated() && this.suggestSize() != null) {
            textField.setValue(this.getSize());
        }
        textField.addTextChangeListener((observable, oldValue, newValue) -> this.setSize(textField.getIntValue()));
        TableViewUtil.selectRowOnMouseClicked(textField);
        return textField;
    }

    /**
     * 创建小数位编辑控件
     *
     * @return 小数位编辑控件
     */
    public NumberTextField getDigitsControl() {
        NumberTextField textField = new NumberTextField();
        textField.setFlexWidth("100% - 12");
        textField.setValue(this.getDigits());
        textField.addTextChangeListener((observable, oldValue, newValue) -> this.setDigits(textField.getIntValue()));
        TableViewUtil.rowOnCtrlS(textField);
        TableViewUtil.selectRowOnMouseClicked(textField);
        return textField;
    }

    /**
     * 创建类型选择控件
     *
     * @return 类型选择控件
     */
    public DBFiledTypeComboBox getTypeControl() {
        DBFiledTypeComboBox comboBox = new DBFiledTypeComboBox();
        comboBox.setDialect(DBDialect.DAMENG);
        comboBox.selectedItemChanged((observable, oldValue, newValue) -> this.setType(newValue));
        comboBox.selectFirstIfNull(this.getType());
        TableViewUtil.rowOnCtrlS(comboBox);
        TableViewUtil.selectRowOnMouseClicked(comboBox);
        return comboBox;
    }

    /**
     * 创建是否可为空选择控件
     *
     * @return 是否可为空选择控件
     */
    public FXCheckBox getNullableControl() {
        FXCheckBox checkBox = new FXCheckBox();
        checkBox.setSelected(this.isNullable());
        checkBox.selectedChanged((observable, oldValue, newValue) -> this.setNullable(newValue));
        // 监听主键值变化
        this.primaryKeyProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                checkBox.setSelected(false);
            }
        });
        TableViewUtil.rowOnCtrlS(checkBox);
        TableViewUtil.selectRowOnMouseClicked(checkBox);
        return checkBox;
    }

    /**
     * 创建主键选择控件
     *
     * @return 主键选择控件
     */
    public FXCheckBox getPrimaryKeyControl() {
        FXCheckBox checkBox = new FXCheckBox();
        checkBox.setSelected(this.isPrimaryKey());
        checkBox.selectedChanged((observable, oldValue, newValue) -> {
            this.setPrimaryKey(newValue);
        });
        TableViewUtil.rowOnCtrlS(checkBox);
        TableViewUtil.selectRowOnMouseClicked(checkBox);
        return checkBox;
    }

    // public ConfigurationSVGGlyph getConfigControl() {
    //     ConfigurationSVGGlyph glyph = new ConfigurationSVGGlyph();
    //     glyph.setOnMousePrimaryClicked(event -> {
    //         PopupAdapter popup = PopupManager.parsePopup(DBColumnConfigPopupController.class);
    //         popup.setProp("dbColumn", this);
    //         popup.setProp("dbClient", CacheHelper.get("dbClient"));
    //         popup.showPopup(glyph);
    //     });
    //     TableViewUtil.selectRowOnMouseClicked(glyph);
    //     return glyph;
    // }

    /**
     * 将字段转换为字段编辑控件
     *
     * @param column 字段
     * @return 字段编辑控件
     */
    public static DamengColumnControl of(DamengColumn column) {
        DamengColumnControl control = new DamengColumnControl();
        control.copy(column);
        return control;
    }

    /**
     * 将字段列表转换为字段编辑控件列表
     *
     * @param columns 字段列表
     * @return 字段编辑控件列表
     */
    public static List<DamengColumnControl> of(List<DamengColumn> columns) {
        List<DamengColumnControl> controls = new ArrayList<>();
        for (DamengColumn column : columns) {
            controls.add(of(column));
        }
        return controls;
    }
}
