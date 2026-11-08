package cn.oyzh.easyshell.trees.http.environment;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.http.ShellHttpEnvironment;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.gui.svg.glyph.ConfigurationSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.paint.Color;

/**
 * HTTP 环境节点值
 *
 * @author oyzh
 * @since 2026-11-08
 */
public class ShellHttpEnvironmentTreeItemValue extends RichTreeItemValue {

    /**
     * 构造环境节点值
     *
     * @param item 环境节点
     */
    public ShellHttpEnvironmentTreeItemValue(ShellHttpEnvironmentTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    public ShellHttpEnvironmentTreeItem item() {
        return (ShellHttpEnvironmentTreeItem) super.item();
    }

    @Override
    public String name() {
        ShellHttpEnvironment environment = this.item().environment();
        return StringUtil.blankToDefault(environment.getName(), ShellI18nHelper.httpEnvironment());
    }

    @Override
    public String extra() {
        return this.item().isActive() ? "[" + ShellI18nHelper.httpActive() + "]" : null;
    }

    @Override
    public Color extraColor() {
        return this.item().isActive() ? Color.FORESTGREEN : super.extraColor();
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
        return this.item().isActive() ? Color.FORESTGREEN : super.graphicColor();
    }
}
