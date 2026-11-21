package cn.oyzh.easyshell.fx.mariadb;

import cn.oyzh.easyshell.mariadb.column.MariadbColumn;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.i18n.I18nResourceBundle;
import javafx.util.StringConverter;

/**
 * MariaDB 字段种类下拉框。
 */
public class MariadbColumnKindComboBox extends FXComboBox<String> {

    {
        this.addItem(MariadbColumn.KIND_NORMAL);
        this.addItem(MariadbColumn.KIND_GENERATED);
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
                    case MariadbColumn.KIND_NORMAL ->
                            I18nResourceBundle.i18nString("shell.sqlite.column.kind.normal");
                    case MariadbColumn.KIND_GENERATED ->
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
