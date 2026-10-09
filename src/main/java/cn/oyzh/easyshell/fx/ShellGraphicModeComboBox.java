package cn.oyzh.easyshell.fx;

import cn.oyzh.common.system.OSUtil;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.i18n.I18nHelper;

/**
 * 图形模式
 *
 * @author oyzh
 * @since 2026-10-10
 */
public class ShellGraphicModeComboBox extends FXComboBox<String> {

    @Override
    public void initNode() {
        this.addItem(I18nHelper.performance());
        this.addItem(I18nHelper.compatible());
        if (OSUtil.isMacOS()) {
            this.addItem(I18nHelper.balance());
        }
        super.initNode();
    }
}
