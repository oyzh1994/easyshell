package cn.oyzh.easyshell.fx.dameng.table;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.dameng.column.DamengColumn;
import cn.oyzh.easyshell.dameng.index.DamengIndex;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.controls.list.FXListView;
import cn.oyzh.fx.plus.util.ListViewUtil;
import javafx.scene.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 达梦索引字段选择列表
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellDamengIndexColumnListView extends FXListView<FXHBox> {

    /**
     * 字段名称列表
     */
    private List<String> columnNames;

    /**
     * 构造达梦索引字段选择列表
     */
    public ShellDamengIndexColumnListView() {

    }

    /**
     * 初始化索引字段选择列表
     *
     * @param dbIndex    索引
     * @param columnList 字段列表
     */
    public void init(DamengIndex dbIndex, List<DamengColumn> columnList) {
        this.clearItems();
        this.columnNames = columnList.parallelStream().map(DamengColumn::getName).collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(dbIndex.getColumns())) {
            for (DamengIndex.IndexColumn column : dbIndex.getColumns()) {
                this.addColumn(column);
            }
        }
    }

    /**
     * 添加一个索引字段
     *
     * @param column 索引字段
     */
    public void addColumn(DamengIndex.IndexColumn column) {
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
    public List<DamengIndex.IndexColumn> getColumns() {
        List<DamengIndex.IndexColumn> list = new ArrayList<>();
        for (Node item : this.getItems()) {
            FXComboBox<String> comboBox = (FXComboBox) item;
//            FXComboBox<String> comboBox = (FXComboBox) item.getFirstChild();
//            NumberTextField textField = (NumberTextField) item.getChild(1);
//            DamengIndex.IndexColumn indexColumn = new DamengIndex.IndexColumn(comboBox.getValue(), textField.getIntValue());
            DamengIndex.IndexColumn indexColumn = new DamengIndex.IndexColumn(comboBox.getValue());
            list.add(indexColumn);
        }
        return list;
    }
}
