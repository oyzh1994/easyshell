package cn.oyzh.easyshell.fx.dameng.table;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.dameng.column.DamengColumn;
import cn.oyzh.easyshell.dameng.index.DamengIndex;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.controls.list.FXListView;
import javafx.scene.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * db索引字段选择框
 *
 * @author oyzh
 * @since 2024/07/16
 */
public class DamengIndexColumnListView extends FXListView<FXHBox> {

    /**
     * 字段名称列表
     */
    private List<String> columnNames;

    public DamengIndexColumnListView() {

    }

    public void init(DamengIndex dbIndex, List<DamengColumn> columnList) {
        this.clearItems();
        this.columnNames = columnList.parallelStream().map(DamengColumn::getName).collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(dbIndex.getColumns())) {
            for (DamengIndex.IndexColumn column : dbIndex.getColumns()) {
                this.addColumn(column);
            }
        }
    }

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
//        NumberTextField textField = new NumberTextField();
//        textField.setMinVal(0);
//        textField.setRealHeight(25);
//        textField.setRealWidth(145);
//        textField.addClass("popover-item");
//        if (column.getSubPart() != null && column.getSubPart() > 0) {
//            textField.setValue(column.getSubPart());
//        }
//        FXHBox hBox = new FXHBox(comboBox);
//        HBox.setMargin(textField, new Insets(0, 0, 0, 5));
//        ListViewUtil.selectRowOnMouseClicked(comboBox, hBox);
//        ListViewUtil.selectRowOnMouseClicked(textField, hBox);
        this.addItem(comboBox);
    }

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
