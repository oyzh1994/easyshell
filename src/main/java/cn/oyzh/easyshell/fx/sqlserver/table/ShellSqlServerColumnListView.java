package cn.oyzh.easyshell.fx.sqlserver.table;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sqlserver.column.SqlServerColumn;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.controls.list.FXListView;
import cn.oyzh.fx.plus.util.ListViewUtil;
import javafx.scene.control.CheckBox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * SqlServer字段多选列表
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellSqlServerColumnListView extends FXListView<FXCheckBox> {

    /**
     * 构造SqlServer字段多选列表
     */
    public ShellSqlServerColumnListView() {

    }

    /**
     * 构造并初始化SqlServer字段多选列表
     *
     * @param columns 字段列表
     */
    public ShellSqlServerColumnListView(List<SqlServerColumn> columns) {
        this.init(columns);
    }

    /**
     * 初始化字段多选列表
     *
     * @param columns 字段列表
     */
    public void init(List<SqlServerColumn> columns) {
        this.init(columns, null);
    }

    /**
     * 初始化字段多选列表并勾选指定字段
     *
     * @param columns         字段列表
     * @param selectedColumns 已勾选的字段名称列表
     */
    public void init(List<SqlServerColumn> columns, List<String> selectedColumns) {
        this.clearItems();
        if (CollectionUtil.isNotEmpty(columns)) {
            for (SqlServerColumn column : columns) {
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
    public List<SqlServerColumn> getSelectedColumns() {
        List<FXCheckBox> checkBoxes = this.getItems().parallelStream().filter(CheckBox::isSelected).toList();
        List<SqlServerColumn> columns = new ArrayList<>();
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
        List<SqlServerColumn> columns = this.getSelectedColumns();
        return columns.parallelStream().map(SqlServerColumn::getName).collect(Collectors.toList());
    }

    /**
     * 勾选指定名称的字段
     *
     * @param columns 字段名称集合
     */
    public void select(Collection<String> columns) {
        if (CollectionUtil.isNotEmpty(columns)) {
            for (FXCheckBox checkBox : this.getItems()) {
                SqlServerColumn column = checkBox.getProp("column");
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
