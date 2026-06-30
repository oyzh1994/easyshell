package cn.oyzh.easyshell.data.db.ui;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/08/22
 */
public class DBDataDumpTypeComboBox extends FXComboBox<String> {

    {
        this.addItem(I18nHelper.dataAndStructure());
        this.addItem(I18nHelper.structure());
    }

    public boolean isFull() {
        return this.getSelectedIndex() == 0;
    }

}
