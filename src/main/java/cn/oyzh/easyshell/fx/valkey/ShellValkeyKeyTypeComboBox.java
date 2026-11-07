package cn.oyzh.easyshell.fx.valkey;


import cn.oyzh.easyshell.valkey.ShellValkeyKeyType;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.i18n.I18nSelectAdapter;

import java.util.List;
import java.util.Locale;

/**
 * redis 键类型下拉框
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellValkeyKeyTypeComboBox extends FXComboBox<String> implements I18nSelectAdapter<String> {

    /**
     * 获取类型
     *
     * @return 键类型
     */
    public ShellValkeyKeyType getType() {
        String type = this.getValue();
        if (type != null) {
            return ShellValkeyKeyType.valueOf(type.substring(0, type.indexOf("(")));
        }
        return null;
    }

    /**
     * 选择类型
     *
     * @param type 类型
     */
    public void select(ShellValkeyKeyType type) {
        if (type != null) {
            this.select(type.ordinal());
        }
    }

    @Override
    public List<String> values(Locale locale) {
        this.clearItems();
        for (ShellValkeyKeyType value : ShellValkeyKeyType.values()) {
            this.getItems().add(value.name());
        }
        this.getItems().add("HYPERLOGLOG");
        this.getItems().add("COORDINATE");
        this.getItems().add("BITMAP");
        return this.getItems();
    }
}
