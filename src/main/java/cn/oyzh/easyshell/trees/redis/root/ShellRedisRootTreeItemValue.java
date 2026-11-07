package cn.oyzh.easyshell.trees.redis.root;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.fx.gui.svg.glyph.database.DatabaseSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;

/**
 * redis根节点值
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellRedisRootTreeItemValue extends RichTreeItemValue {

    /**
     * 构造redis根节点值
     *
     * @param item 根节点
     */
    public ShellRedisRootTreeItemValue(ShellRedisRootTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    public ShellRedisRootTreeItem item() {
        return (ShellRedisRootTreeItem) super.item();
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() != null && super.graphic().isWaiting()) {
            return super.graphic();
        }
        if (super.graphic() == null) {
            super.graphic(new DatabaseSVGGlyph());
            super.graphic().disableTheme();
        }
        return super.graphic();
    }

    @Override
    public String name() {
        try {
            ShellConnect connect = this.item().shellConnect();
            if (connect != null) {
                return connect.getName();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return I18nHelper.database();
    }
}
