package cn.oyzh.easyshell.data.oracle.dto;


import cn.oyzh.easyshell.oracle.column.OracleColumn;

/**
 * Oracle数据导出字段
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellOracleDataExportColumn extends OracleColumn {

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
