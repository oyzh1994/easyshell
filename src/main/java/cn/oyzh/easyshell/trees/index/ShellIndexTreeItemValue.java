package cn.oyzh.easyshell.trees.index;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.gui.svg.glyph.database.IndexSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 数据库树索引节点值
 *
 * @author oyzh
 * @since 2026-11-28
 */
public class ShellIndexTreeItemValue extends RichTreeItemValue {

    /**
     * 构造数据库树索引节点值
     *
     * @param item 索引节点
     */
    public ShellIndexTreeItemValue(ShellIndexTreeItem item) {
        super(item);
    }

    @Override
    public ShellIndexTreeItem item() {
        return (ShellIndexTreeItem) super.item();
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(new IndexSVGGlyph());
        }
        return super.graphic();
    }

    @Override
    public String name() {
        String tableName = this.item().tableName();
        String indexName = this.item().indexName();
        // 索引隶属于表/视图，展示为“表名.索引名”
        if (StringUtil.isNotBlank(tableName)) {
            return tableName + "." + indexName;
        }
        return indexName;
    }
}
