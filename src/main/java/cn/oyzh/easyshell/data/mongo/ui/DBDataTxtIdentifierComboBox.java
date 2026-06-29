package cn.oyzh.easyshell.data.mongo.ui;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * @author oyzh
 * @since 2024/09/04
 */
public class DBDataTxtIdentifierComboBox extends FXComboBox<String> {

    {
        this.addItem("\"");
        this.addItem("'");
    }
}
