package cn.oyzh.easyshell.fx.mysql.table;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.fx.gui.text.field.SelectTextFiled;

import java.util.Objects;

/**
 * @author oyzh
 * @since 2024/7/12
 */
public class MysqlDefaultValueTextFiled extends SelectTextFiled<String> {

    private boolean editableFlag;

    {
        this.selectedItemChanged(( newValue) -> {
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
        this.clearItem();
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
        if (this.isEditable()) {
            return super.getTextTrim();
        }
        String text = super.getTextTrim();
        if ("NULL".equalsIgnoreCase(text)) {
            return null;
        }
        if ("EMPTY STRING".equalsIgnoreCase(text)) {
            return "";
        }
        return text;
    }
}
