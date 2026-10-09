package cn.oyzh.easyshell.fx.mariadb.table;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.mariadb.column.MariadbColumn;
import cn.oyzh.easyshell.mariadb.index.MariadbIndex;
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
 * @since 2026-10-09
 */
public class ShellMariadbIndexColumnListView extends FXListView<FXHBox> {

    /**
     * 字段名称列表
     */
    private List<String> columnNames;

    /**
     * 构造索引字段选择框
     */
    public ShellMariadbIndexColumnListView() {

    }

    /**
     * 初始化索引字段选择框
     *
     * @param dbIndex    索引信息
     * @param columnList 可选字段列表
     */
    public void init(MariadbIndex dbIndex, List<MariadbColumn> columnList) {
        this.clearItems();
        this.columnNames = columnList.parallelStream().map(MariadbColumn::getName).collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(dbIndex.getColumns())) {
            for (MariadbIndex.IndexColumn column : dbIndex.getColumns()) {
                this.addColumn(column);
            }
        }
    }

    /**
     * 新增索引字段
     *
     * @param column 索引字段
     */
    public void addColumn(MariadbIndex.IndexColumn column) {
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

    /**
     * 获取索引字段列表
     *
     * @return 索引字段列表
     */
    public List<MariadbIndex.IndexColumn> getColumns() {
        List<MariadbIndex.IndexColumn> list = new ArrayList<>();
        for (FXHBox item : this.getItems()) {
            FXComboBox<String> comboBox = (FXComboBox) item.getChild(0);
            NumberTextField textField = (NumberTextField) item.getChild(1);
            MariadbIndex.IndexColumn indexColumn = new MariadbIndex.IndexColumn(comboBox.getValue(), textField.getIntValue());
            list.add(indexColumn);
        }
        return list;
    }
}
