package cn.oyzh.easyshell.fx.sqlite;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.i18n.I18nResourceBundle;
import javafx.util.StringConverter;

import java.util.List;

/**
 * SQLite 排序规则下拉框。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteCollationComboBox extends FXComboBox<String> {

    @Override
    public void initNode() {
        this.setConverter(new StringConverter<>() {
            @Override
            public String toString(String value) {
                return StringUtil.isBlank(value)
                        ? I18nResourceBundle.i18nString("shell.sqlite.defaultCollation") : value;
            }

            @Override
            public String fromString(String value) {
                return value;
            }
        });
        super.initNode();
    }

    /**
     * 初始化排序规则。
     *
     * @param client        SQLite客户端
     * @param currentValue  当前排序规则
     */
    public void init(ShellSqliteClient client, String currentValue) {
        this.clearItems();
        this.addItem("");
        List<String> collations = client == null ? List.of() : client.selectCollations();
        for (String collation : collations) {
            if (this.getItems().stream().noneMatch(item -> StringUtil.equalsIgnoreCase(item, collation))) {
                this.addItem(collation);
            }
        }
        if (StringUtil.isNotBlank(currentValue)
                && this.getItems().stream().noneMatch(item -> StringUtil.equalsIgnoreCase(item, currentValue))) {
            this.addItem(currentValue);
        }
        this.select(currentValue);
    }

    @Override
    public void select(String obj) {
        if (StringUtil.isBlank(obj)) {
            super.select("");
            return;
        }
        String value = this.getItems().stream()
                .filter(item -> StringUtil.equalsIgnoreCase(item, obj))
                .findFirst()
                .orElse(obj);
        super.select(value);
    }
}
