package cn.oyzh.easyshell.fx.term;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * shell 退格类型选择框
 *
 * @author oyzh
 * @since 2025-07-03
 */
public class ShellTermBackspaceTypeCombobox extends FXComboBox<String> {

    {
        this.addItem("ASCII Delete(0x7F)");
        this.addItem("ASCII Delete(0x08)");
        this.addItem("VT220 Delete(ESC[3~)");
        this.select(1);
    }

    /**
     * 是否为类型一
     *
     * @return 是否为类型一
     */
    public boolean isType1() {
        return this.getSelectedIndex() == 0;
    }

    /**
     * 是否为类型二
     *
     * @return 是否为类型二
     */
    public boolean isType2() {
        return this.getSelectedIndex() == 1;
    }

    /**
     * 是否为类型三
     *
     * @return 是否为类型三
     */
    public boolean isType3() {
        return this.getSelectedIndex() == 2;
    }

    /**
     * 选择退格类型
     *
     * @param type 退格类型
     */
    public void selectType(Integer type) {
        if (type == null || type == 1) {
            super.select(1);
        } else if (type == 0) {
            super.selectFirst();
        } else if (type == 2) {
            super.select(2);
        }
    }
}
