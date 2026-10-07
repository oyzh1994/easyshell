package cn.oyzh.easyshell.fx.dameng.table;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.dameng.column.DamengColumn;
import cn.oyzh.fx.gui.text.field.SelectTextFiled;

import java.util.Objects;

/**
 * 达梦字段默认值输入框
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class DamengDefaultValueTextFiled extends SelectTextFiled<String> {

    /**
     * 是否允许编辑
     */
    private boolean editableFlag;

    {
        this.selectedItemChanged(( newValue) -> {
            if (this.editableFlag) {
                this.setEditable(Objects.equals(newValue, CollectionUtil.getFirst(this.getItemList())));
            }
        });
    }

    /**
     * 初始化默认值输入框
     *
     * @param column 字段
     */
    public void init(DamengColumn column) {
        this.init(column, null);
    }

    /**
     * 初始化默认值输入框并设置默认值
     *
     * @param column       字段
     * @param defaultValue 默认值
     */
    public void init(DamengColumn column, String defaultValue) {
        this.clear();
        this.clearItemList();
//        if (column.supportEnum()) {
//            this.editableFlag = false;
//            this.setEditable(false);
//            this.setItemList(column.getValueList());
//            this.addItem("NULL");
//            if (defaultValue != null) {
//                this.selectItem(defaultValue);
//            } else {
//                this.selectIndex(this.getItemSize());
//            }
//        } else {
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
//        }
    }

    /**
     * 获取默认值
     *
     * @return 默认值
     */
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
