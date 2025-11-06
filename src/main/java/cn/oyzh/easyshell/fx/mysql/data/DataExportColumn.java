package cn.oyzh.easyshell.fx.mysql.data;

import cn.oyzh.easyshell.mysql.column.MysqlColumn;

/**
 * @author oyzh
 * @since 2024/8/27
 */
public class DataExportColumn extends MysqlColumn {

    /**
     * 是否选中
     */
    private boolean selected = true;

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
