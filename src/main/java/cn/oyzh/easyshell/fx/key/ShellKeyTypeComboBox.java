package cn.oyzh.easyshell.fx.key;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * shell密钥类型选择框
 *
 * @author oyzh
 * @since 25/04/03
 */
public class ShellKeyTypeComboBox extends FXComboBox<String> {

    {
        this.addItem("RSA");
        this.addItem("ED25519");
        this.addItem("ECDSA");
    }

    public boolean isRsaType() {
        return this.getSelectedIndex() == 0;
    }

    public boolean isEd25519Type() {
        return this.getSelectedIndex() == 1;
    }

    public boolean isEcdsaType() {
        return this.getSelectedIndex() == 2;
    }
}
