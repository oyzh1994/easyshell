package cn.oyzh.easyshell.fx.zk;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.i18n.I18nSelectAdapter;

import java.util.List;
import java.util.Locale;

/**
 * @author oyzh
 * @since 2024/4/19
 */
public class ShellZKACLTypeComboBox extends FXComboBox<String> implements I18nSelectAdapter<String> {

    @Override
    public List<String> values(Locale locale) {
        this.clearItems();
        if (locale == Locale.SIMPLIFIED_CHINESE || locale == Locale.PRC) {
            this.addItem("开放认证(WORLD)");
            this.addItem("摘要认证(DIGEST)-用户密码明文");
            this.addItem("摘要认证(DIGEST)-用户密码摘要");
            this.addItem("摘要认证(DIGEST)-已有认证信息");
            this.addItem("IP认证-单IP");
            this.addItem("IP认证-多IP");
        } else if (locale == Locale.TRADITIONAL_CHINESE || locale == Locale.TAIWAN) {
            this.addItem("開放認證(WORLD)");
            this.addItem("摘要認證(DIGEST)-用戶密碼明文");
            this.addItem("摘要認證(DIGEST)-用戶密碼摘要");
            this.addItem("摘要認證(DIGEST)-已有認證信息");
            this.addItem("IP認證-單IP");
            this.addItem("IP認證-多IP");
        } else {
            this.addItem("WORLD");
            this.addItem("DIGEST-User password by plain text");
            this.addItem("DIGEST-User password by digest text");
            this.addItem("DIGEST-Existing authentication information");
            this.addItem("IP-Single");
            this.addItem("IP-Multiple");
        }
        return this.getItems();
    }
}
