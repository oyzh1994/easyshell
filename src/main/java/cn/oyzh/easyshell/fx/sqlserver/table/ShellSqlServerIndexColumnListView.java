package cn.oyzh.easyshell.fx.sqlserver.table;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sqlserver.column.SqlServerColumn;
import cn.oyzh.easyshell.sqlserver.index.SqlServerIndex;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.controls.list.FXListView;
import cn.oyzh.fx.plus.util.ListViewUtil;
import javafx.scene.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SqlServer索引字段选择列表
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellSqlServerIndexColumnListView extends FXListView<FXHBox> {

    /**
     * 字段名称列表
     */
    private List<String> columnNames;

    /**
     * 构造SqlServer索引字段选择列表
     */
    public ShellSqlServerIndexColumnListView() {

    }

    /**
     * 初始化索引字段选择列表
     *
     * @param dbIndex    索引
     * @param columnList 字段列表
     */
    public void init(SqlServerIndex dbIndex, List<SqlServerColumn> columnList) {
        this.clearItems();
        this.columnNames = columnList.parallelStream().map(SqlServerColumn::getName).collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(dbIndex.getColumns())) {
            for (SqlServerIndex.IndexColumn column : dbIndex.getColumns()) {
                this.addColumn(column);
            }
        }
    }

    /**
     * 添加一个索引字段
     *
     * @param column 索引字段
     */
    public void addColumn(SqlServerIndex.IndexColumn column) {
        FXComboBox<String> comboBox = new FXComboBox<>();
        comboBox.setRealWidth(300);
        comboBox.setRealHeight(25);
        comboBox.setItem(this.columnNames);
        comboBox.addClass("popover-item");
        if (StringUtil.isNotBlank(column.getColumnName())) {
            comboBox.select(column.getColumnName());
        } else {
            comboBox.selectFirst();
        }
        ListViewUtil.selectRowOnMouseClicked(comboBox);
        this.addItem(comboBox);
    }

    /**
     * 获取已选中的索引字段列表
     *
     * @return 已选中的索引字段列表
     */
    public List<SqlServerIndex.IndexColumn> getColumns() {
        List<SqlServerIndex.IndexColumn> list = new ArrayList<>();
        for (Node item : this.getItems()) {
            FXComboBox<String> comboBox = (FXComboBox) item;
//            FXComboBox<String> comboBox = (FXComboBox) item.getFirstChild();
//            NumberTextField textField = (NumberTextField) item.getChild(1);
//            SqlServerIndex.IndexColumn indexColumn = new SqlServerIndex.IndexColumn(comboBox.getValue(), textField.getIntValue());
            SqlServerIndex.IndexColumn indexColumn = new SqlServerIndex.IndexColumn(comboBox.getValue());
            list.add(indexColumn);
        }
        return list;
    }
}
