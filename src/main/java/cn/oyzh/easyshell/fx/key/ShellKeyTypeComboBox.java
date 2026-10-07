package cn.oyzh.easyshell.fx.key;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import org.apache.sshd.common.keyprovider.KeyPairProvider;

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
        this.addItem("DSA");
    }

    /**
     * 是否为RSA类型
     *
     * @return 是否为RSA类型
     */
    public boolean isRsaType() {
        return this.getSelectedIndex() == 0;
    }

    /**
     * 是否为ED25519类型
     *
     * @return 是否为ED25519类型
     */
    public boolean isEd25519Type() {
        return this.getSelectedIndex() == 1;
    }

    /**
     * 是否为ECDSA类型
     *
     * @return 是否为ECDSA类型
     */
    public boolean isEcdsaType() {
        return this.getSelectedIndex() == 2;
    }

    /**
     * 是否为DSA类型
     *
     * @return 是否为DSA类型
     */
    public boolean isDsaType() {
        return this.getSelectedIndex() == 3;
    }

    /**
     * 获取密钥类型名称
     *
     * @param type 密钥类型
     * @return 密钥类型名称
     */
    public static String getTypeName(String type) {
        if (type.equalsIgnoreCase(KeyPairProvider.SSH_RSA)) {
            return "RSA";
        }
        if (type.equalsIgnoreCase(KeyPairProvider.SSH_DSS)) {
            return "DSA";
        }
        if (type.equalsIgnoreCase(KeyPairProvider.SSH_ED25519)) {
            return "ED25519";
        }
        if (StringUtil.equalsAnyIgnoreCase(type, KeyPairProvider.ECDSA_SHA2_NISTP256, KeyPairProvider.ECDSA_SHA2_NISTP384, KeyPairProvider.ECDSA_SHA2_NISTP521)) {
            return "ECDSA";
        }
        throw new IllegalStateException("Unexpected value: " + type);
    }
}
