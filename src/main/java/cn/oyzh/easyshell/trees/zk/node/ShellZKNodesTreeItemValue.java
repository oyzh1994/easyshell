package cn.oyzh.easyshell.trees.zk.node;

import cn.oyzh.easyshell.fx.svg.glyph.zk.NodeSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;

/**
 * zk节点分类节点值
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellZKNodesTreeItemValue extends RichTreeItemValue {

    /**
     * 构造节点分类节点值
     *
     * @param item 节点分类节点
     */
    public ShellZKNodesTreeItemValue(ShellZKNodesTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    public ShellZKNodesTreeItem item() {
        return (ShellZKNodesTreeItem) super.item();
    }

    @Override
    public String name() {
        return I18nHelper.node();
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(new NodeSVGGlyph());
            super.graphic().disableTheme();
        }
        return super.graphic();
    }
}
