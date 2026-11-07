package cn.oyzh.easyshell.fx.valkey;


import cn.oyzh.fx.gui.text.field.DecimalTextField;
import cn.oyzh.i18n.I18nHelper;

/**
 * Valkey纬度输入框
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellValkeyLatitudeField extends DecimalTextField {

    @Override
    public void initNode() {
        this.setRequire(true);
        this.setMax(85.05112878);
        this.setMin(-85.05112878);
        this.setTipText(I18nHelper.latitude());
        super.initNode();
    }
}
