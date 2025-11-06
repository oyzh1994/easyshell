package cn.oyzh.easyshell.fx.mysql.view;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * db视图算法下拉框
 *
 * @author oyzh
 * @since 2024/08/07
 */
public class MysqlViewAlgorithmComboBox extends FXComboBox<String> {

    {
        this.addItem("UNDEFINED");
        this.addItem("MERGE");
        this.addItem("TEMPTABLE");
    }

    @Override
    public void select(String obj) {
        if (obj != null) {
            super.select(obj.toUpperCase());
        }
    }
}
