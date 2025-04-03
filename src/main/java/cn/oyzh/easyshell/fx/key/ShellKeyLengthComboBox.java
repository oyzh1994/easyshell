package cn.oyzh.easyshell.fx.key;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * shell密钥长度选择框
 *
 * @author oyzh
 * @since 23/03/09
 */
public class ShellKeyLengthComboBox extends FXComboBox<Integer> {

    {
        this.init("RSA");
    }

    public void init(String keyType) {
        this.clearItems();
        switch (keyType.toUpperCase()) {
            case "RSA":
                this.addItem(1024);
                this.addItem(2048);
                this.addItem(3072);
                this.addItem(4096);
                this.addItem(8192);
                this.select(1);
                break;
            case "ED25519":
                this.addItem(256);
                this.select(0);
                break;
        }
    }
}
