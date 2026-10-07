package cn.oyzh.easyshell.data.mongo.dto;

import cn.oyzh.easyshell.mongo.column.MongoColumn;

/**
 * Mongo数据导出字段
 *
 * @author oyzh
 * @since 2024/8/27
 */
public class ShellMongoDataExportColumn extends MongoColumn {

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
