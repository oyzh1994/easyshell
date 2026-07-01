package cn.oyzh.easyshell.data.mongo.dto;

import cn.oyzh.easyshell.mongo.column.MongoColumn;

/**
 * @author oyzh
 * @since 2024/8/27
 */
public class ShellMongoDataExportColumn extends MongoColumn {

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
