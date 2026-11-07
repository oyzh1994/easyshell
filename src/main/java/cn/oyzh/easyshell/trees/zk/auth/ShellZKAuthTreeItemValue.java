package cn.oyzh.easyshell.trees.zk.auth;

import cn.oyzh.easyshell.fx.svg.glyph.zk.AuditSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;

/**
 * zk认证节点值
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellZKAuthTreeItemValue extends RichTreeItemValue {

    /**
     * 构造认证节点值
     *
     * @param item 认证节点
     */
    public ShellZKAuthTreeItemValue(ShellZKAuthTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    public ShellZKAuthTreeItem item() {
        return (ShellZKAuthTreeItem) super.item();
    }

    @Override
    public String name() {
        return I18nHelper.auth();
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(new AuditSVGGlyph());
            super.graphic().disableTheme();
        }
        return super.graphic();
    }
}
