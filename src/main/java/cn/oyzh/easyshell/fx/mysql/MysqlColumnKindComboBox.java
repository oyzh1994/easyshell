package cn.oyzh.easyshell.fx.mysql;

import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.i18n.I18nResourceBundle;
import javafx.util.StringConverter;

/**
 * MySQL 字段种类下拉框。
 */
public class MysqlColumnKindComboBox extends FXComboBox<String> {

    {
        this.addItem(MysqlColumn.KIND_NORMAL);
        this.addItem(MysqlColumn.KIND_GENERATED);
    }

    @Override
    public void initNode() {
        this.setConverter(new StringConverter<>() {
            @Override
            public String toString(String value) {
                if (value == null) {
                    return "";
                }
                return switch (value) {
                    case MysqlColumn.KIND_NORMAL ->
                            I18nResourceBundle.i18nString("shell.sqlite.column.kind.normal");
                    case MysqlColumn.KIND_GENERATED ->
                            I18nResourceBundle.i18nString("shell.sqlite.column.kind.generated");
                    default -> value;
                };
            }

            @Override
            public String fromString(String value) {
                return value;
            }
        });
        super.initNode();
    }
}
