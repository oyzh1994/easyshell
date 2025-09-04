package cn.oyzh.easyshell.fx.zk;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.i18n.I18nSelectAdapter;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.i18n.I18nHelper;

import java.util.List;
import java.util.Locale;

/**
 * @author oyzh
 * @since 2024/4/26
 */
public class ZKACLType2ComboBox extends FXComboBox<String> implements I18nSelectAdapter<String> {

    {
        NodeManager.init(this);
    }

    @Override
    public List<String> values(Locale locale) {
        this.clearItems();
        if (locale == Locale.SIMPLIFIED_CHINESE || locale == Locale.PRC) {
            this.addItem("开放认证(WORLD)");
            this.addItem(I18nHelper.digestAuth() + "(DIGEST)");
            this.addItem("IP");
        } else if (locale == Locale.TRADITIONAL_CHINESE || locale == Locale.TAIWAN) {
            this.addItem("開放認證(WORLD)");
            this.addItem(I18nHelper.digestAuth() + "(DIGEST)");
            this.addItem("IP");
        } else {
            this.addItem("WORLD");
            this.addItem("DIGEST");
            this.addItem("IP");
        }
        return this.getItems();
    }
}
