package cn.oyzh.easyshell.fx.zk;

import cn.oyzh.easyshell.domain.zk.ShellZKAuth;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.converter.SimpleStringConverter;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/23
 */
public class ShellZKAuthComboBox extends FXComboBox<ShellZKAuth> {

    {
        NodeManager.init(this);
        this.setConverter(new SimpleStringConverter<ShellZKAuth>() {
            @Override
            public String toString(ShellZKAuth auth) {
                String text = "";
                if (auth != null) {
                    text = I18nHelper.userName() + ":" + auth.getUser() +
                            " " + I18nHelper.password() + ":" + auth.getPassword();
                }
                return text;
            }
        });
    }

}
