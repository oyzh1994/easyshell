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
 * @author oyzh
 * @since 2024/09/14
 */
public class DamengIndexControl extends DamengIndex {

    private List<DamengColumn> columnList;

    public void setColumnList(List<DamengColumn> columnList) {
        this.columnList = columnList;
    }

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

    public DamengIndexMethodComboBox getMethodControl() {
        DamengIndexMethodComboBox comboBox = new DamengIndexMethodComboBox();
        comboBox.selectFirstIfNull(this.getMethod());
        comboBox.selectedItemChanged((observable, oldValue, newValue) -> this.setMethod(newValue));
        TableViewUtil.rowOnCtrlS(comboBox);
        TableViewUtil.selectRowOnMouseClicked(comboBox);
        return comboBox;
    }

    public static DamengIndexControl of(DamengIndex index) {
        DamengIndexControl control = new DamengIndexControl();
        control.copy(index);
        return control;
    }

    public static List<DamengIndexControl> of(List<DamengIndex> indices) {
        List<DamengIndexControl> controls = new ArrayList<>();
        for (DamengIndex index : indices) {
            controls.add(of(index));
        }
        return controls;
    }
}
