package cn.oyzh.easyshell.fx.redis;


import cn.oyzh.fx.gui.text.field.DecimalTextField;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2023/7/5
 */
public class ShellRedisLatitudeField extends DecimalTextField {

    {
        this.setRequire(true);
        this.setMax(85.05112878);
        this.setMin(-85.05112878);
        this.setTipText(I18nHelper.latitude());
    }
}
