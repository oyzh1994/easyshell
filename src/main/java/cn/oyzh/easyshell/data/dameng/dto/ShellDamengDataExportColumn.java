package cn.oyzh.easyshell.data.dameng.dto;


import cn.oyzh.easyshell.dameng.column.DamengColumn;

/**
 * @author oyzh
 * @since 2024/8/27
 */
public class ShellDamengDataExportColumn extends DamengColumn {

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
