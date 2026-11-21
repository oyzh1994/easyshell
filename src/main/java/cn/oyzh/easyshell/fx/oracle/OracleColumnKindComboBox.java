package cn.oyzh.easyshell.fx.oracle;

import cn.oyzh.easyshell.oracle.column.OracleColumn;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.i18n.I18nResourceBundle;
import javafx.util.StringConverter;

/**
 * Oracle字段种类下拉框。
 */
public class OracleColumnKindComboBox extends FXComboBox<String> {

    {
        this.addItem(OracleColumn.KIND_NORMAL);
        this.addItem(OracleColumn.KIND_GENERATED);
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
                    case OracleColumn.KIND_NORMAL ->
                            I18nResourceBundle.i18nString("shell.sqlite.column.kind.normal");
                    case OracleColumn.KIND_GENERATED ->
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
