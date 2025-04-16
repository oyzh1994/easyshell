package cn.oyzh.easyshell.fx.process;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.i18n.I18nHelper;

/**
 * 进程类型选择框
 *
 * @author oyzh
 * @since 25/03/29
 */
public class ShellProcessTypeComboBox extends FXComboBox<String> {

    {
        this.addItem(I18nHelper.allUser());
        this.addItem(I18nHelper.currentUser());
    }
}
