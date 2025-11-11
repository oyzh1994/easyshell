package cn.oyzh.easyshell.fx.mysql.table;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.mysql.index.MysqlIndex;
import cn.oyzh.fx.gui.text.field.NumberTextField;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.controls.list.FXListView;
import cn.oyzh.fx.plus.util.ListViewUtil;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * db索引字段选择框
 *
 * @author oyzh
 * @since 2024/07/16
 */
public class ShellMysqlIndexColumnListView extends FXListView<FXHBox> {

    /**
     * 字段名称列表
     */
    private List<String> columnNames;

    public ShellMysqlIndexColumnListView() {

    }

    public void init(MysqlIndex dbIndex, List<MysqlColumn> columnList) {
        this.clearItems();
        this.columnNames = columnList.parallelStream().map(MysqlColumn::getName).collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(dbIndex.getColumns())) {
            for (MysqlIndex.IndexColumn column : dbIndex.getColumns()) {
                this.addColumn(column);
            }
        }
    }

    public void addColumn(MysqlIndex.IndexColumn column) {
        FXComboBox<String> comboBox = new FXComboBox<>();
        comboBox.setRealWidth(150);
        comboBox.setRealHeight(25);
        comboBox.setItem(this.columnNames);
        comboBox.addClass("popover-item");
        if (StringUtil.isNotBlank(column.getColumnName())) {
            comboBox.select(column.getColumnName());
        } else {
            comboBox.selectFirst();
        }

        NumberTextField textField = new NumberTextField();
        textField.setMinVal(0);
        textField.setRealHeight(25);
        textField.setRealWidth(145);
        textField.addClass("popover-item");
        if (column.getSubPart() != null && column.getSubPart() > 0) {
            textField.setValue(column.getSubPart());
        }
        FXHBox hBox = new FXHBox(comboBox, textField);
        HBox.setMargin(textField, new Insets(0, 0, 0, 5));
        ListViewUtil.selectRowOnMouseClicked(comboBox, hBox);
        ListViewUtil.selectRowOnMouseClicked(textField, hBox);
        this.addItem(hBox);
    }

    public List<MysqlIndex.IndexColumn> getColumns() {
        List<MysqlIndex.IndexColumn> list = new ArrayList<>();
        for (FXHBox item : this.getItems()) {
            FXComboBox<String> comboBox = (FXComboBox) item.getChild(0);
            NumberTextField textField = (NumberTextField) item.getChild(1);
            MysqlIndex.IndexColumn indexColumn = new MysqlIndex.IndexColumn(comboBox.getValue(), textField.getIntValue());
            list.add(indexColumn);
        }
        return list;
    }
}
