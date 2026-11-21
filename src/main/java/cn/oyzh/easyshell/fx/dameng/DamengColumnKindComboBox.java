package cn.oyzh.easyshell.fx.dameng;

import cn.oyzh.easyshell.dameng.column.DamengColumn;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.i18n.I18nResourceBundle;
import javafx.util.StringConverter;

/**
 * 达梦字段种类下拉框。
 */
public class DamengColumnKindComboBox extends FXComboBox<String> {

    {
        this.addItem(DamengColumn.KIND_NORMAL);
        this.addItem(DamengColumn.KIND_GENERATED);
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
                    case DamengColumn.KIND_NORMAL ->
                            I18nResourceBundle.i18nString("shell.sqlite.column.kind.normal");
                    case DamengColumn.KIND_GENERATED ->
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
