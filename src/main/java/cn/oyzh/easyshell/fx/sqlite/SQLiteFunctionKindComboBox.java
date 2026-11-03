package cn.oyzh.easyshell.fx.sqlite;

import cn.oyzh.easyshell.sqlite.function.SQLiteFunction;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.i18n.I18nResourceBundle;
import javafx.util.StringConverter;

/**
 * SQLite 函数设计类型下拉框。
 *
 * @since 2026-11-02
 */
public class SQLiteFunctionKindComboBox extends FXComboBox<String> {

    {
        this.addItem(SQLiteFunction.KIND_SQL_EXPRESSION);
        this.addItem(SQLiteFunction.KIND_TABLE_VIEW);
        this.addItem(SQLiteFunction.KIND_GENERATED_COLUMN);
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
                    case SQLiteFunction.KIND_SQL_EXPRESSION ->
                            I18nResourceBundle.i18nString("shell.sqlite.function.kind.expression");
                    case SQLiteFunction.KIND_TABLE_VIEW ->
                            I18nResourceBundle.i18nString("shell.sqlite.function.kind.tableView");
                    case SQLiteFunction.KIND_GENERATED_COLUMN ->
                            I18nResourceBundle.i18nString("shell.sqlite.function.kind.generatedColumn");
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
