package cn.oyzh.easyshell.fx.mysql.table;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.fx.gui.text.field.SelectTextFiled;

import java.util.List;
import java.util.Objects;

/**
 * @author oyzh
 * @since 2024/7/12
 */
public class ShellMysqlDefaultValueTextFiled extends SelectTextFiled<String> {

    private boolean editableFlag;

    {
        this.selectedItemChanged((newValue) -> {
            if (this.editableFlag) {
                this.setEditable(Objects.equals(newValue, CollectionUtil.getFirst(this.getItemList())));
            }
        });
    }

    public void init(MysqlColumn column) {
        this.init(column, null);
    }

    public void init(MysqlColumn column, String defaultValue) {
        this.clear();
        this.clearItemList();
        if (column.supportEnum()) {
            this.editableFlag = false;
            this.setEditable(false);
            this.setItemList(column.getValueList());
            this.addItem("NULL");
            if (defaultValue != null) {
                this.selectItem(defaultValue);
            } else {
                this.selectIndex(this.getItemSize());
            }
            // 监听值变化，刷新列表
            column.valueProperty().addListener((observableValue) -> {
                List<String> vals = column.getValueList();
                String item = this.getSelectedItem();
                this.setItemList(column.getValueList());
                this.addItem("NULL");
                if (item != null && vals.contains(item)) {
                    this.selectItem(item);
                } else {
                    this.clear();
                }
            });
        } else {
            this.editableFlag = true;
            this.addItem("");
            this.addItem("EMPTY STRING");
            this.addItem("NULL");
            if (defaultValue != null) {
                this.setEditable(true);
                this.setText(defaultValue);
            } else {
                this.setEditable(false);
                this.selectIndex(2);
            }
        }
    }

    public String getValue() {
        String text = super.getTextTrim();
        if (this.isEditable()) {
            text = super.getTextTrim();
        } else if ("NULL".equalsIgnoreCase(text)) {
            text = null;
        } else if ("EMPTY STRING".equalsIgnoreCase(text)) {
            text = "";
        }
        return text;
    }
}
