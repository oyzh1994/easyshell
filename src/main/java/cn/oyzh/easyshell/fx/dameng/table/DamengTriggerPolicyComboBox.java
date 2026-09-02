package cn.oyzh.easyshell.fx.dameng.table;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * @author oyzh
 * @since 2024/7/9
 */
public class DamengTriggerPolicyComboBox extends FXComboBox<String> {

    {
        this.addItem("BEFORE INSERT");
        this.addItem("BEFORE UPDATE");
        this.addItem("BEFORE DELETE");
        this.addItem("AFTER INSERT");
        this.addItem("AFTER UPDATE");
        this.addItem("AFTER DELETE");
    }
}
