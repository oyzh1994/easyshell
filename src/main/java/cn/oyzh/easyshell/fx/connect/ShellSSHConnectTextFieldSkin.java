package cn.oyzh.easyshell.fx.connect;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.fx.gui.skin.SelectTextFiledSkin;
import javafx.scene.control.TextField;

/**
 * 连接输入框皮肤
 *
 * @author oyzh
 * @since 2025/06/06
 */
public class ShellSSHConnectTextFieldSkin extends SelectTextFiledSkin<ShellConnect> {

    public ShellSSHConnectTextFieldSkin(TextField textField) {
        super(textField);
    }
}
