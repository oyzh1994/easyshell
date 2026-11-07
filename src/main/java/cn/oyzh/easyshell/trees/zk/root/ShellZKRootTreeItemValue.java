package cn.oyzh.easyshell.trees.zk.root;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.fx.svg.glyph.zk.ZookeeperSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * zk根节点值
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellZKRootTreeItemValue extends RichTreeItemValue {

    /**
     * 构造zk根节点值
     *
     * @param item 根节点
     */
    public ShellZKRootTreeItemValue(ShellZKRootTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    public ShellZKRootTreeItem item() {
        return (ShellZKRootTreeItem) super.item();
    }

    @Override
    public String name() {
        try {
            ShellConnect connect = this.item().connect();
            if (connect != null) {
                return connect.getName();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(new ZookeeperSVGGlyph());
            super.graphic().disableTheme();
        }
        return super.graphic();
    }
}
