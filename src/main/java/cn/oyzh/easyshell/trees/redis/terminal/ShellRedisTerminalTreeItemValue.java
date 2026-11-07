package cn.oyzh.easyshell.trees.redis.terminal;

import cn.oyzh.fx.gui.svg.glyph.TerminalSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;

/**
 * redis终端节点值
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellRedisTerminalTreeItemValue extends RichTreeItemValue {

    /**
     * 构造终端节点值
     *
     * @param item 终端节点
     */
    public ShellRedisTerminalTreeItemValue(ShellRedisTerminalTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    public ShellRedisTerminalTreeItem item() {
        return (ShellRedisTerminalTreeItem) super.item();
    }

    @Override
    public String name() {
        return I18nHelper.terminal();
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(new TerminalSVGGlyph());
            super.graphic().disableTheme();
        }
        return super.graphic();
    }
}
