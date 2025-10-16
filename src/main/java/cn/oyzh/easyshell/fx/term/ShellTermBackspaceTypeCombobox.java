package cn.oyzh.easyshell.fx.term;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
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

    public boolean isType1() {
        return this.getSelectedIndex() == 0;
    }

    public boolean isType2() {
        return this.getSelectedIndex() == 1;
    }

    public boolean isType3() {
        return this.getSelectedIndex() == 2;
    }

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
