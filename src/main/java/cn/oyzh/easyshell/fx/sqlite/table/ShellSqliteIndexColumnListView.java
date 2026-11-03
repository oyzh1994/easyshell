package cn.oyzh.easyshell.fx.sqlite.table;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.controls.list.FXListView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * SQLite索引字段选择列表。
 *
 * @author oyzh
 * @since 2026-11-03
 */
public class ShellSqliteIndexColumnListView extends FXListView<FXComboBox<String>> {

    private List<String> columnNames;

    public ShellSqliteIndexColumnListView() {
    }

    /**
     * 初始化索引字段选择列表。
     *
     * @param columnList 可选字段列表
     * @param columns    已选字段列表
     */
    public void init(List<? extends SQLiteColumn> columnList, List<String> columns) {
        this.clearItems();
        this.columnNames = columnList == null ? new ArrayList<>()
                : columnList.parallelStream().map(SQLiteColumn::getName).collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(columns)) {
            for (String column : columns) {
                this.addColumn(column);
            }
        }
    }

    /**
     * 新增索引字段。
     *
     * @param column 字段名
     */
    public void addColumn(String column) {
        FXComboBox<String> comboBox = new FXComboBox<>();
        comboBox.setRealWidth(300);
        comboBox.setRealHeight(25);
        comboBox.setItem(this.columnNames);
        comboBox.addClass("popover-item");
        if (StringUtil.isNotBlank(column)) {
            if (!this.columnNames.contains(column)) {
                comboBox.addItem(column);
            }
            comboBox.select(column);
        } else if (CollectionUtil.isNotEmpty(this.columnNames)) {
            comboBox.selectFirst();
        }
        this.addItem(comboBox);
    }

    /**
     * 获取已选字段列表。
     *
     * @return 字段列表
     */
    public Set<String> getColumns() {
        Set<String> list = new HashSet<>();
        for (FXComboBox<String> item : this.getItems()) {
            if (StringUtil.isNotBlank(item.getValue())) {
                list.add(item.getValue());
            }
        }
        return list;
    }
}
