package cn.oyzh.easyshell.fx.valkey;


import cn.oyzh.fx.gui.text.field.DecimalTextField;
import cn.oyzh.i18n.I18nHelper;

/**
 * Valkey经度输入框
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellValkeyLongitudeField extends DecimalTextField {

    @Override
    public void initNode() {
        this.setMax(180D);
        this.setMin(-180D);
        this.setRequire(true);
        this.setTipText(I18nHelper.longitude());
        super.initNode();
    }
}
