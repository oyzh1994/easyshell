package cn.oyzh.easyshell.data.db.ui;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * @author oyzh
 * @since 2024/1/26
 */
public class DBJoinSymbolComboBox extends FXComboBox<String> {

    @Override
    public void initNode(){
        this.addItem("AND");
        this.addItem("OR");
    }
}
