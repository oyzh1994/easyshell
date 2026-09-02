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
 * db字段选择框
 *
 * @author oyzh
 * @since 2024/01/24
 */
public class DamengColumnListView extends FXListView<FXCheckBox> {

    public DamengColumnListView() {

    }

    public DamengColumnListView(List<DamengColumn> columns) {
        this.init(columns);
    }

    public void init(List<DamengColumn> columns) {
        this.init(columns, null);
    }

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

    public List<DamengColumn> getSelectedColumns() {
        List<FXCheckBox> checkBoxes = this.getItems().parallelStream().filter(CheckBox::isSelected).toList();
        List<DamengColumn> columns = new ArrayList<>();
        for (FXCheckBox checkBox : checkBoxes) {
            columns.add(checkBox.getProp("column"));
        }
        return columns;
    }

    public List<String> getSelectedColumnNames() {
        List<DamengColumn> columns = this.getSelectedColumns();
        return columns.parallelStream().map(DamengColumn::getName).collect(Collectors.toList());
    }

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
