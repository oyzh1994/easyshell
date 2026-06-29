package cn.oyzh.easyshell.fx.mongo;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * @author oyzh
 * @since 2024/1/26
 */
public class MongoJoinSymbolComboBox extends FXComboBox<String> {

    @Override
    public void initNode(){
        this.addItem("AND");
        this.addItem("OR");
    }
}
