package cn.oyzh.easyshell.fx.dameng.table;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * 达梦触发器策略下拉选择框
 *
 * @author oyzh
 * @since 2025-11-06
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
