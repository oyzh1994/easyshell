package cn.oyzh.easyshell.fx.mysql.table;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.controls.list.FXListView;
import cn.oyzh.fx.plus.util.ListViewUtil;
import javafx.scene.control.CheckBox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * db字段选择框
 *
 * @author oyzh
 * @since 2024/01/24
 */
public class ShellMysqlColumnListView extends FXListView<FXCheckBox> {

    public ShellMysqlColumnListView() {

    }

    public ShellMysqlColumnListView(List<MysqlColumn> columns) {
        this.init(columns);
    }

    public void init(List<MysqlColumn> columns) {
        this.init(columns, null);
    }

    public void init(List<MysqlColumn> columns, List<String> selectedColumns) {
        this.clearItems();
        if (CollectionUtil.isNotEmpty(columns)) {
            for (MysqlColumn column : columns) {
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

    public List<MysqlColumn> getSelectedColumns() {
        List<FXCheckBox> checkBoxes = this.getItems().parallelStream().filter(CheckBox::isSelected).toList();
        List<MysqlColumn> columns = new ArrayList<>();
        for (FXCheckBox checkBox : checkBoxes) {
            columns.add(checkBox.getProp("column"));
        }
        return columns;
    }

    public Set<String> getSelectedColumnNames() {
        List<MysqlColumn> columns = this.getSelectedColumns();
        return columns.parallelStream().map(MysqlColumn::getName).collect(Collectors.toSet());
    }

    public void select(Collection<String> columns) {
        if (CollectionUtil.isNotEmpty(columns)) {
            for (FXCheckBox checkBox : this.getItems()) {
                MysqlColumn column = checkBox.getProp("column");
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
