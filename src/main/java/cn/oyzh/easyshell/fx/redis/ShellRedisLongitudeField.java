package cn.oyzh.easyshell.fx.redis;


import cn.oyzh.fx.gui.text.field.DecimalTextField;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2023/7/5
 */
public class ShellRedisLongitudeField extends DecimalTextField {

    {
        this.setMax(180D);
        this.setMin(-180D);
        this.setRequire(true);
        this.setTipText(I18nHelper.longitude());
    }
}
