package cn.oyzh.easyshell.fx.zk;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.i18n.I18nSelectAdapter;
import cn.oyzh.fx.plus.node.NodeManager;

import java.util.List;
import java.util.Locale;

/**
 * @author oyzh
 * @since 2024/4/23
 */
public class ShellZKAuthTypeComboBox extends FXComboBox<String> implements I18nSelectAdapter<String> {

    {
        NodeManager.init(this);
    }

    @Override
    public List<String> values(Locale locale) {
        this.clearItems();
        if (locale == Locale.TRADITIONAL_CHINESE || locale == Locale.TAIWAN) {
            this.addItem("用戶密碼明文");
            this.addItem("已有認證信息");
        } else if (locale == Locale.SIMPLIFIED_CHINESE || locale == Locale.PRC) {
            this.addItem("用户密码明文");
            this.addItem("已有认证信息");
        } else {
            this.addItem("User password in plaintext");
            this.addItem("Existing authentication information");
        }
        return this.getItems();
    }
}
