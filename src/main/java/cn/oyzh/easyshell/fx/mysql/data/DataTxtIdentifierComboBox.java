package cn.oyzh.easyshell.fx.mysql.data;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * @author oyzh
 * @since 2024/09/04
 */
public class DataTxtIdentifierComboBox extends FXComboBox<String> {

    {
        this.addItem("\"");
        this.addItem("'");
    }
}
