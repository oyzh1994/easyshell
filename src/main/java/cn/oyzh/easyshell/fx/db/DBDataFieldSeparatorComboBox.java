package cn.oyzh.easyshell.fx.db;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/09/04
 */
public class DBDataFieldSeparatorComboBox extends FXComboBox<String> {

    {
        this.addItem(I18nHelper.semicolon() + "(;)");
        this.addItem(I18nHelper.comma() + "(,)");
        this.addItem(I18nHelper.space() + "( )");
    }

    public String value() {
        int itemIndex = this.getSelectedIndex();
        if (itemIndex == 0) {
            return ";";
        }
        if (itemIndex == 1) {
            return ",";
        }
        return " ";
    }
}
