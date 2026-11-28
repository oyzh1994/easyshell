package cn.oyzh.easyshell.data.h2.dto;

import cn.oyzh.easyshell.h2.column.H2Column;

/**
 * H2 数据导出字段。
 *
 * @since 2026-11-02
 */
public class H2DataExportColumn extends H2Column {

    private boolean selected = true;

    public boolean isSelected() {
        return this.selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
