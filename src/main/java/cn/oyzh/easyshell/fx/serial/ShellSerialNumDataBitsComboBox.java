package cn.oyzh.easyshell.fx.serial;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * @author oyzh
 * @since 2025-04-24
 */
public class ShellSerialNumDataBitsComboBox extends FXComboBox<String> {

    {
        super.addItem("5");
        super.addItem("6");
        super.addItem("7");
        super.addItem("8");
        this.select(3);
    }

    public int getNumDataBits() {
        return Integer.parseInt(this.getSelectedItem());
    }

    public void init(int val) {
        if (5 == val) {
            this.select(0);
        } else if (6 == val) {
            this.select(1);
        } else if (7 == val) {
            this.select(2);
        } else if (8 == val) {
            this.select(3);
        } else {
            this.selectFirst();
        }
    }
}
