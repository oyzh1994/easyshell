package cn.oyzh.easyshell.fx.sqlserver.table;

import cn.oyzh.easyshell.sqlserver.column.SqlServerColumn;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.i18n.I18nResourceBundle;
import javafx.util.StringConverter;

/**
 * SQL Server字段种类下拉框。
 */
public class SqlServerColumnKindComboBox extends FXComboBox<String> {

    {
        this.addItem(SqlServerColumn.KIND_NORMAL);
        this.addItem(SqlServerColumn.KIND_GENERATED);
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
                    case SqlServerColumn.KIND_NORMAL ->
                            I18nResourceBundle.i18nString("shell.sqlite.column.kind.normal");
                    case SqlServerColumn.KIND_GENERATED ->
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
