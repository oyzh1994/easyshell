package cn.oyzh.easyshell.fx.h2;

import cn.oyzh.easyshell.h2.column.H2Column;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.i18n.I18nResourceBundle;
import javafx.util.StringConverter;

/**
 * H2 字段类型下拉框。
 *
 * @since 2026-11-28
 */
public class H2ColumnKindComboBox extends FXComboBox<String> {

    {
        this.addItem(H2Column.KIND_NORMAL);
        this.addItem(H2Column.KIND_GENERATED);
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
                    case H2Column.KIND_NORMAL ->
                            I18nResourceBundle.i18nString("shell.h2.normal");
                    case H2Column.KIND_GENERATED ->
                            I18nResourceBundle.i18nString("shell.h2.generated");
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
