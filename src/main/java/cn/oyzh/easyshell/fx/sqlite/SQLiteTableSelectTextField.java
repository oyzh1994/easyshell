package cn.oyzh.easyshell.fx.sqlite;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.gui.text.field.SelectTextFiled;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * SQLite 表名可搜索选择框。
 *
 * @since 2026-11-02
 */
public class SQLiteTableSelectTextField extends SelectTextFiled<String> {

    private List<String> tables = List.of();

    public void setTables(Collection<String> tables) {
        this.tables = tables == null ? List.of() : List.copyOf(tables);
        this.setItemList(this.tables);
    }

    public static List<String> filterTables(Collection<String> tables, String keyword) {
        if (tables == null || tables.isEmpty()) {
            return List.of();
        }
        String search = StringUtil.blankToDefault(keyword, "").trim().toLowerCase(Locale.ROOT);
        if (search.isEmpty()) {
            return List.copyOf(tables);
        }
        return tables.stream()
                .filter(item -> item.toLowerCase(Locale.ROOT).contains(search))
                .toList();
    }

    @Override
    protected boolean onTextChanged(String newValue) {
        boolean changed = super.onTextChanged(newValue);
        if (!changed) {
            return false;
        }
        List<String> filtered = filterTables(this.tables, newValue);
        this.setItemList(filtered);
        if (this.isFocused()) {
            if (filtered.isEmpty()) {
                this.skin().hidePopup();
            } else {
                this.skin().showPopup();
            }
        }
        return true;
    }
}
