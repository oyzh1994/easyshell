package cn.oyzh.easyshell.fx.dameng.table;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * 达梦索引方法下拉选择框
 *
 * @author oyzh
 * @since 2024/01/24
 */
public class DamengIndexMethodComboBox extends FXComboBox<String> {

    {
        this.addItem("");
        this.addItem("BTREE");
        this.addItem("HASH");
    }
}
