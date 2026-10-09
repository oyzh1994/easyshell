package cn.oyzh.easyshell.mariadb.index;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.fx.mariadb.table.ShellMariadbIndexFieldTextFiled;
import cn.oyzh.easyshell.fx.mariadb.table.ShellMariadbIndexMethodComboBox;
import cn.oyzh.easyshell.fx.mariadb.table.ShellMariadbIndexTypeComboBox;
import cn.oyzh.easyshell.mariadb.column.MariadbColumn;
import cn.oyzh.fx.db.util.DBUtil;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.i18n.I18nHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * MariaDB索引组件
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class MariadbIndexControl extends MariadbIndex {

    /**
     * 字段列表
     */
    private List<MariadbColumn> columnList;

    /**
     * 设置字段列表
     *
     * @param columnList 字段列表
     */
    public void setColumnList(List<MariadbColumn> columnList) {
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
    public ShellMariadbIndexFieldTextFiled getColumnControl() {
        //List<MariadbColumn> columnList = CacheHelper.get("mariadb:columnList");
        if (this.columnList == null) {
            this.columnList = new ArrayList<>();
        }
        ShellMariadbIndexFieldTextFiled textField = new ShellMariadbIndexFieldTextFiled(this, this.columnList, this.getColumns());
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
    public ShellMariadbIndexTypeComboBox getTypeControl() {
        ShellMariadbIndexTypeComboBox comboBox = new ShellMariadbIndexTypeComboBox();
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
    public ShellMariadbIndexMethodComboBox getMethodControl() {
        ShellMariadbIndexMethodComboBox comboBox = new ShellMariadbIndexMethodComboBox();
        comboBox.selectFirstIfNull(this.getMethod());
        comboBox.selectedItemChanged((observable, oldValue, newValue) -> this.setMethod(newValue));
        TableViewUtil.rowOnCtrlS(comboBox);
        TableViewUtil.selectRowOnMouseClicked(comboBox);
        return comboBox;
    }

    /**
     * 获取注释组件
     *
     * @return 注释组件
     */
    public ClearableTextField getCommentControl() {
        ClearableTextField textField = new ClearableTextField();
        textField.setFlexWidth("100% - 12");
        textField.setPromptText(I18nHelper.pleaseInputComment());
        textField.setText(this.getComment());
        textField.addTextChangeListener((observable, oldValue, newValue) -> this.setComment(newValue));
        TableViewUtil.rowOnCtrlS(textField);
        TableViewUtil.selectRowOnMouseClicked(textField);
        return textField;
    }

    /**
     * 根据索引构建组件
     *
     * @param index 索引
     * @return 组件
     */
    public static MariadbIndexControl of(MariadbIndex index) {
        MariadbIndexControl control = new MariadbIndexControl();
        control.copy(index);
        return control;
    }

    /**
     * 根据索引列表构建组件列表
     *
     * @param indices 索引列表
     * @return 组件列表
     */
    public static List<MariadbIndexControl> of(List<MariadbIndex> indices) {
        List<MariadbIndexControl> controls = new ArrayList<>();
        for (MariadbIndex index : indices) {
            controls.add(of(index));
        }
        return controls;
    }
}
