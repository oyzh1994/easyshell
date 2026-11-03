package cn.oyzh.easyshell.data.sqlite.dto;

import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;

/**
 * SQLite 数据导出字段。
 *
 * @since 2026-11-02
 */
public class SQLiteDataExportColumn extends SQLiteColumn {

    private boolean selected = true;

    public boolean isSelected() {
        return this.selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
