package cn.oyzh.easyshell.dameng.index;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.dameng.column.DamengColumn;
import cn.oyzh.easyshell.dameng.index.DamengIndex;
import cn.oyzh.easyshell.fx.dameng.table.DamengIndexFieldTextFiled;
import cn.oyzh.easyshell.fx.dameng.table.DamengIndexMethodComboBox;
import cn.oyzh.easyshell.fx.dameng.table.DamengIndexTypeComboBox;
import cn.oyzh.fx.db.util.DBUtil;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.i18n.I18nHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * 达梦索引组件
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class DamengIndexControl extends DamengIndex {

    /**
     * 字段列表
     */
    private List<DamengColumn> columnList;

    /**
     * 设置字段列表
     *
     * @param columnList 字段列表
     */
    public void setColumnList(List<DamengColumn> columnList) {
        this.columnList = columnList;
    }

    /**
     * 获取名称组件
     *
     * @return 名称组件
     */
    public ClearableTextField getNameControl() {
        ClearableTextField textField = new ClearableTextField();
        if (StringUtil.isEmpty(this.getName())) {
            this.setName(DBUtil.genIndexName());
        }
        textField.setText(this.getName());
        textField.setPromptText(I18nHelper.pleaseInputName());
        textField.addTextChangeListener((observable, oldValue, newValue) -> this.setName(newValue));
        TableViewUtil.rowOnCtrlS(textField);
        TableViewUtil.selectRowOnMouseClicked(textField);
        return textField;
    }

    /**
     * 获取字段组件
     *
     * @return 字段组件
     */
    public DamengIndexFieldTextFiled getColumnControl() {
        //List<DamengColumn> columnList = CacheHelper.get("dameng:columnList");
        if (this.columnList == null) {
            this.columnList = null;
        }
        DamengIndexFieldTextFiled textField = new DamengIndexFieldTextFiled(this, this.columnList, this.getColumns());
        textField.setFlexWidth("100% - 12");
        textField.addTextChangeListener((observable, oldValue, newValue) -> this.setColumns(textField.getColumns()));
        TableViewUtil.rowOnCtrlS(textField);
        TableViewUtil.selectRowOnMouseClicked(textField);
        return textField;
    }

    /**
     * 获取类型组件
     *
     * @return 类型组件
     */
    public DamengIndexTypeComboBox getTypeControl() {
        DamengIndexTypeComboBox comboBox = new DamengIndexTypeComboBox();
        comboBox.selectFirstIfNull(this.getType());
        comboBox.selectedItemChanged((observable, oldValue, newValue) -> this.setType(newValue));
        TableViewUtil.rowOnCtrlS(comboBox);
        TableViewUtil.selectRowOnMouseClicked(comboBox);
        // 初始化数据
        this.setType(comboBox.getValue());
        return comboBox;
    }

    /**
     * 获取方式组件
     *
     * @return 方式组件
     */
    public DamengIndexMethodComboBox getMethodControl() {
        DamengIndexMethodComboBox comboBox = new DamengIndexMethodComboBox();
        comboBox.selectFirstIfNull(this.getMethod());
        comboBox.selectedItemChanged((observable, oldValue, newValue) -> this.setMethod(newValue));
        TableViewUtil.rowOnCtrlS(comboBox);
        TableViewUtil.selectRowOnMouseClicked(comboBox);
        return comboBox;
    }

    /**
     * 根据索引构建组件
     *
     * @param index 索引
     * @return 组件
     */
    public static DamengIndexControl of(DamengIndex index) {
        DamengIndexControl control = new DamengIndexControl();
        control.copy(index);
        return control;
    }

    /**
     * 根据索引列表构建组件列表
     *
     * @param indices 索引列表
     * @return 组件列表
     */
    public static List<DamengIndexControl> of(List<DamengIndex> indices) {
        List<DamengIndexControl> controls = new ArrayList<>();
        for (DamengIndex index : indices) {
            controls.add(of(index));
        }
        return controls;
    }
}
