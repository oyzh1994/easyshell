package cn.oyzh.easyshell.fx.dameng.table;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.dameng.column.DamengColumn;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.controls.list.FXListView;
import cn.oyzh.fx.plus.util.ListViewUtil;
import javafx.scene.control.CheckBox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 达梦字段多选列表
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class DamengColumnListView extends FXListView<FXCheckBox> {

    /**
     * 构造达梦字段多选列表
     */
    public DamengColumnListView() {

    }

    /**
     * 构造并初始化达梦字段多选列表
     *
     * @param columns 字段列表
     */
    public DamengColumnListView(List<DamengColumn> columns) {
        this.init(columns);
    }

    /**
     * 初始化字段多选列表
     *
     * @param columns 字段列表
     */
    public void init(List<DamengColumn> columns) {
        this.init(columns, null);
    }

    /**
     * 初始化字段多选列表并勾选指定字段
     *
     * @param columns         字段列表
     * @param selectedColumns 已勾选的字段名称列表
     */
    public void init(List<DamengColumn> columns, List<String> selectedColumns) {
        this.clearItems();
        if (CollectionUtil.isNotEmpty(columns)) {
            for (DamengColumn column : columns) {
                boolean selected = CollectionUtil.contains(selectedColumns, column.getName());
                FXCheckBox checkBox = new FXCheckBox();
                checkBox.setSelected(selected);
                checkBox.setText(column.getName());
                checkBox.setProp("column", column);
                ListViewUtil.selectRowOnMouseClicked(checkBox);
                this.addItem(checkBox);
            }
        }
    }

    /**
     * 获取已勾选的字段列表
     *
     * @return 已勾选的字段列表
     */
    public List<DamengColumn> getSelectedColumns() {
        List<FXCheckBox> checkBoxes = this.getItems().parallelStream().filter(CheckBox::isSelected).toList();
        List<DamengColumn> columns = new ArrayList<>();
        for (FXCheckBox checkBox : checkBoxes) {
            columns.add(checkBox.getProp("column"));
        }
        return columns;
    }

    /**
     * 获取已勾选的字段名称列表
     *
     * @return 已勾选的字段名称列表
     */
    public List<String> getSelectedColumnNames() {
        List<DamengColumn> columns = this.getSelectedColumns();
        return columns.parallelStream().map(DamengColumn::getName).collect(Collectors.toList());
    }

    /**
     * 勾选指定名称的字段
     *
     * @param columns 字段名称集合
     */
    public void select(Collection<String> columns) {
        if (CollectionUtil.isNotEmpty(columns)) {
            for (FXCheckBox checkBox : this.getItems()) {
                DamengColumn column = checkBox.getProp("column");
                for (String s : columns) {
                    if (StringUtil.equalsIgnoreCase(s.trim(), column.getName())) {
                        checkBox.setSelected(true);
                        break;
                    }
                }
            }
        }
    }
}
