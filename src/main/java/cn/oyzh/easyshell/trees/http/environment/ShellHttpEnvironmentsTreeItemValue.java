package cn.oyzh.easyshell.trees.http.environment;

import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.gui.svg.glyph.ConfigurationSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.paint.Color;

/**
 * HTTP 环境分类节点值
 *
 * @author oyzh
 * @since 2026-11-08
 */
public class ShellHttpEnvironmentsTreeItemValue extends RichTreeItemValue {

    /**
     * 构造环境分类节点值
     *
     * @param item 环境分类节点
     */
    public ShellHttpEnvironmentsTreeItemValue(ShellHttpEnvironmentsTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    public ShellHttpEnvironmentsTreeItem item() {
        return (ShellHttpEnvironmentsTreeItem) super.item();
    }

    @Override
    public String name() {
        return ShellI18nHelper.httpEnvironments();
    }

    @Override
    public String extra() {
        return "(" + this.item().workspace().getEnvironments().size() + ")";
    }

    @Override
    public Color extraColor() {
        return Color.FORESTGREEN;
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(new ConfigurationSVGGlyph());
            super.graphic().disableTheme();
        }
        return super.graphic();
    }

    @Override
    public Color graphicColor() {
        if (!this.item().workspace().getEnvironments().isEmpty()) {
            return Color.FORESTGREEN;
        }
        return super.graphicColor();
    }
}
