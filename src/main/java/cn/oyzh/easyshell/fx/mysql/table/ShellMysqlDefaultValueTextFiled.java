package cn.oyzh.easyshell.fx.mysql.table;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.fx.gui.text.field.SelectTextFiled;

import java.util.List;
import java.util.Objects;

/**
 * MySQL字段默认值输入框
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellMysqlDefaultValueTextFiled extends SelectTextFiled<String> {

    /**
     * 是否可编辑标志
     */
    private boolean editableFlag;

    /**
     * 根据字段初始化
     *
     * @param column 字段信息
     */
    public void init(MysqlColumn column) {
        this.init(column, null);
    }

    /**
     * 根据字段与默认值初始化
     *
     * @param column       字段信息
     * @param defaultValue 默认值
     */
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

    @Override
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
