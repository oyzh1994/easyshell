package cn.oyzh.easyshell.fx.sqlserver.table;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * SqlServer索引方法下拉选择框
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellSqlServerIndexMethodComboBox extends FXComboBox<String> {

    {
        this.addItem("");
        this.addItem("CLUSTERED");
        this.addItem("NONCLUSTERED");
        this.addItem("COLUMNSTORE");
        this.addItem("CLUSTERED COLUMNSTORE");
        this.addItem("NONCLUSTERED COLUMNSTORE");
    }
}
