package cn.oyzh.easyshell.data.mariadb.dto;

import cn.oyzh.easyshell.mariadb.column.MariadbColumn;

/**
 * Mariadb数据导出字段
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbDataExportColumn extends MariadbColumn {

    /**
     * 是否选中
     */
    private boolean selected = true;

    /**
     * 是否选中
     *
     * @return 结果
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * 设置是否选中
     *
     * @param selected 是否选中
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
