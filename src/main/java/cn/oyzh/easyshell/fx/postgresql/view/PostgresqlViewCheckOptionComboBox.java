package cn.oyzh.easyshell.fx.postgresql.view;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * PostgreSQL视图检查选项下拉框
 * <p>
 * 对应CREATE VIEW语句中的WITH [ CASCADED | LOCAL ] CHECK OPTION，NONE表示不添加该子句。
 *
 * @author oyzh
 * @since 2024/08/07
 */
public class PostgresqlViewCheckOptionComboBox extends FXComboBox<String> {

    {
        this.addItem("NONE");
        this.addItem("CASCADED");
        this.addItem("LOCAL");
    }

    @Override
    public void select(String obj) {
        if (obj != null) {
            super.select(obj.toUpperCase());
        } else {
            super.clearSelection();
        }
    }
}
