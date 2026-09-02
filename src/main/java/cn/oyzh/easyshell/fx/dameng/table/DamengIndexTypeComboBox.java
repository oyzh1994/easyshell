package cn.oyzh.easyshell.fx.dameng.table;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * db索引类型选择框
 *
 * @author oyzh
 * @since 2024/01/24
 */
public class DamengIndexTypeComboBox extends FXComboBox<String> {

    @Override
    public void initNode() {
        this.addItem("NORMAL");
        this.addItem("UNIQUE");
    }
}
