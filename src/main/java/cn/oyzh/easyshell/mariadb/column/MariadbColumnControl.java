package cn.oyzh.easyshell.mariadb.column;

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
 * MariaDB字段组件
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class MariadbColumnControl extends MariadbColumn {

    /**
     * 获取名称组件
     *
     * @return 名称组件
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
     * 获取注释组件
     *
     * @return 注释组件
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
     * 获取字段大小组件
     *
     * @return 字段大小组件
     */
    public NumberTextField getSizeControl() {
        NumberTextField textField = new NumberTextField();
        textField.setPromptText(I18nHelper.pleaseInputContent());
        textField.setFlexWidth("100% - 12");
        TableViewUtil.rowOnCtrlS(textField);
        if (this.getSize() != null) {
            textField.setValue(this.getSize());
        } else if (this.supportSize() && this.isCreated() && this.suggestSize() != null) {
            textField.setValue(this.suggestSize());
        }
        textField.addTextChangeListener((observable, oldValue, newValue) -> this.setSize(textField.getIntValue()));
        TableViewUtil.selectRowOnMouseClicked(textField);
        return textField;
    }

    /**
     * 获取小数位组件
     *
     * @return 小数位组件
     */
    public NumberTextField getDigitsControl() {
        NumberTextField textField = new NumberTextField();
        textField.setPromptText(I18nHelper.pleaseInputContent());
        textField.setFlexWidth("100% - 12");
        textField.setValue(this.getDigits());
        textField.addTextChangeListener((observable, oldValue, newValue) -> this.setDigits(textField.getIntValue()));
        TableViewUtil.rowOnCtrlS(textField);
        TableViewUtil.selectRowOnMouseClicked(textField);
        return textField;
    }

    /**
     * 获取类型组件
     *
     * @return 类型组件
     */
    public DBFiledTypeComboBox getTypeControl() {
        DBFiledTypeComboBox comboBox = new DBFiledTypeComboBox();
        comboBox.setDialect(DBDialect.MARIADB);
        comboBox.selectedItemChanged((observable, oldValue, newValue) -> this.setType(newValue));
        comboBox.selectFirstIfNull(this.getType());
        TableViewUtil.rowOnCtrlS(comboBox);
        TableViewUtil.selectRowOnMouseClicked(comboBox);
        return comboBox;
    }

    /**
     * 获取可为null组件
     *
     * @return 可为null组件
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
     * 获取主键组件
     *
     * @return 主键组件
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
     * 根据字段构建组件
     *
     * @param column 字段
     * @return 组件
     */
    public static MariadbColumnControl of(MariadbColumn column) {
        MariadbColumnControl control = new MariadbColumnControl();
        control.copy(column);
        return control;
    }

    /**
     * 根据字段列表构建组件列表
     *
     * @param columns 字段列表
     * @return 组件列表
     */
    public static List<MariadbColumnControl> of(List<MariadbColumn> columns) {
        List<MariadbColumnControl> controls = new ArrayList<>();
        for (MariadbColumn column : columns) {
            controls.add(of(column));
        }
        return controls;
    }
}
