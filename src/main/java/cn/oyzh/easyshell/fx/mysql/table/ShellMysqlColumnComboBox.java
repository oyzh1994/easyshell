package cn.oyzh.easyshell.fx.mysql.table;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.converter.SimpleStringConverter;

import java.util.List;

/**
 * db字段类型选择框
 *
 * @author oyzh
 * @since 2024/01/16
 */
public class ShellMysqlColumnComboBox extends FXComboBox<MysqlColumn> {

    {
        this.setConverter(new SimpleStringConverter<>() {
            @Override
            public String toString(MysqlColumn o) {
                if (o == null) {
                    return "";
                }
                return o.getName();
            }
        });
    }

    public ShellMysqlColumnComboBox() {

    }

    public ShellMysqlColumnComboBox(List<MysqlColumn> columns) {
        this.addItems(columns);
    }

    public void select(String colName) {
        for (MysqlColumn object : this.getItems()) {
            if (StringUtil.equalsIgnoreCase(colName, object.getName())) {
                this.select(object);
                break;
            }
        }
    }

    public String getColumnName() {
        return this.getSelectedItem().getName();
    }
}
