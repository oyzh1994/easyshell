package cn.oyzh.easyshell.fx.oracle.table;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.oracle.column.OracleColumn;
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
 * Oracle字段多选列表
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellOracleColumnListView extends FXListView<FXCheckBox> {

    /**
     * 构造Oracle字段多选列表
     */
    public ShellOracleColumnListView() {

    }

    /**
     * 构造并初始化Oracle字段多选列表
     *
     * @param columns 字段列表
     */
    public ShellOracleColumnListView(List<OracleColumn> columns) {
        this.init(columns);
    }

    /**
     * 初始化字段多选列表
     *
     * @param columns 字段列表
     */
    public void init(List<OracleColumn> columns) {
        this.init(columns, null);
    }

    /**
     * 初始化字段多选列表并勾选指定字段
     *
     * @param columns         字段列表
     * @param selectedColumns 已勾选的字段名称列表
     */
    public void init(List<OracleColumn> columns, List<String> selectedColumns) {
        this.clearItems();
        if (CollectionUtil.isNotEmpty(columns)) {
            for (OracleColumn column : columns) {
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
    public List<OracleColumn> getSelectedColumns() {
        List<FXCheckBox> checkBoxes = this.getItems().parallelStream().filter(CheckBox::isSelected).toList();
        List<OracleColumn> columns = new ArrayList<>();
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
        List<OracleColumn> columns = this.getSelectedColumns();
        return columns.parallelStream().map(OracleColumn::getName).collect(Collectors.toList());
    }

    /**
     * 勾选指定名称的字段
     *
     * @param columns 字段名称集合
     */
    public void select(Collection<String> columns) {
        if (CollectionUtil.isNotEmpty(columns)) {
            for (FXCheckBox checkBox : this.getItems()) {
                OracleColumn column = checkBox.getProp("column");
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
