package cn.oyzh.easyshell.fx.postgresql.view;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * PostgreSQL视图算法下拉框
 * <p>
 * PostgreSQL的CREATE VIEW语法不支持ALGORITHM子句，该类保留下来用于兼容通用的视图设计界面，
 * 取值不参与SQL生成。
 *
 * @author oyzh
 * @since 2024/08/07
 */
public class PostgresqlViewAlgorithmComboBox extends FXComboBox<String> {

    {
        this.addItem("UNDEFINED");
        this.addItem("MERGE");
        this.addItem("TEMPTABLE");
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
