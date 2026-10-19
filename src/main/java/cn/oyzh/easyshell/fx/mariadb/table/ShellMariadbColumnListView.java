package cn.oyzh.easyshell.fx.mariadb.table;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.mariadb.column.MariadbColumn;
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
 * MariaDB字段选择列表
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbColumnListView extends FXListView<FXCheckBox> {

    /**
     * 构造字段选择列表
     */
    public ShellMariadbColumnListView() {

    }

    /**
     * 构造字段选择列表并初始化字段
     *
     * @param columns 字段列表
     */
    public ShellMariadbColumnListView(List<MariadbColumn> columns) {
        this.init(columns);
    }

    /**
     * 初始化字段列表
     *
     * @param columns 字段列表
     */
    public void init(List<MariadbColumn> columns) {
        this.init(columns, null);
    }

    /**
     * 初始化字段列表并勾选指定字段
     *
     * @param columns         字段列表
     * @param selectedColumns 已选中的字段名称
     */
    public void init(List<MariadbColumn> columns, List<String> selectedColumns) {
        this.clearItems();
        if (CollectionUtil.isNotEmpty(columns)) {
            for (MariadbColumn column : columns) {
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
     * 获取已选中的字段列表
     *
     * @return 已选中的字段列表
     */
    public List<MariadbColumn> getSelectedColumns() {
        List<FXCheckBox> checkBoxes = this.getItems().parallelStream().filter(CheckBox::isSelected).toList();
        List<MariadbColumn> columns = new ArrayList<>();
        for (FXCheckBox checkBox : checkBoxes) {
            columns.add(checkBox.getProp("column"));
        }
        return columns;
    }

    /**
     * 获取已选中的字段名称集合
     *
     * @return 已选中的字段名称集合
     */
    public Set<String> getSelectedColumnNames() {
        List<MariadbColumn> columns = this.getSelectedColumns();
        return columns.parallelStream().map(MariadbColumn::getName).collect(Collectors.toSet());
    }

    /**
     * 勾选指定字段
     *
     * @param columns 字段名称集合
     */
    public void select(Collection<String> columns) {
        if (CollectionUtil.isNotEmpty(columns)) {
            for (FXCheckBox checkBox : this.getItems()) {
                MariadbColumn column = checkBox.getProp("column");
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
