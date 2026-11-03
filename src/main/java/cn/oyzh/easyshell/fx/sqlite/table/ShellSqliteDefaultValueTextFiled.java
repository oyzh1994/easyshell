package cn.oyzh.easyshell.fx.sqlite.table;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;
import cn.oyzh.fx.gui.text.field.SelectTextFiled;

import java.util.Objects;

/**
 * SQLite 字段默认值输入框。
 *
 * @since 2026-11-03
 */
public class ShellSqliteDefaultValueTextFiled extends SelectTextFiled<String> {

    /**
     * 是否可编辑标志
     */
    private boolean editableFlag;

    /**
     * 根据字段初始化
     *
     * @param column 字段信息
     */
    public void init(SQLiteColumn column) {
        this.init(column, null);
    }

    /**
     * 根据字段与默认值初始化
     *
     * @param column       字段信息
     * @param defaultValue 默认值
     */
    public void init(SQLiteColumn column, String defaultValue) {
        this.clear();
        this.clearItemList();
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

    @Override
    public String getValue() {
        if (this.isEditable()) {
            return super.getTextTrim();
        }
        String text = super.getTextTrim();
        if ("NULL".equalsIgnoreCase(text)) {
            return null;
        }
        if ("EMPTY STRING".equalsIgnoreCase(text)) {
            return "''";
        }
        return text;
    }

    @Override
    public void initNode() {
        this.selectedItemChanged((newValue) -> {
            if (this.editableFlag) {
                this.setEditable(Objects.equals(newValue, CollectionUtil.getFirst(this.getItemList())));
            }
        });
        super.initNode();
    }
}
