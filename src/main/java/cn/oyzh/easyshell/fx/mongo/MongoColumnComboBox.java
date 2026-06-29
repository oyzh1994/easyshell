package cn.oyzh.easyshell.fx.mongo;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.mongo.MongoColumn;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.converter.SimpleStringConverter;

import java.util.List;

/**
 * db字段类型选择框
 *
 * @author oyzh
 * @since 2024/01/16
 */
public class MongoColumnComboBox extends FXComboBox<MongoColumn> {

    {
        this.setConverter(new SimpleStringConverter<>() {
            @Override
            public String toString(MongoColumn o) {
                if (o == null) {
                    return "";
                }
                return o.displayName();
            }
        });
    }

    public MongoColumnComboBox() {

    }

    public MongoColumnComboBox(List<MongoColumn> columns) {
        this.addItems(columns);
    }

    public void select(String colName) {
        for (MongoColumn object : this.getItems()) {
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
