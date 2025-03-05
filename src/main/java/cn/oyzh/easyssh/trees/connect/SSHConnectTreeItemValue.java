package cn.oyzh.easyssh.trees.connect;

import cn.oyzh.easyssh.fx.svg.glyph.LinuxSVGGlyph;
import cn.oyzh.easyssh.fx.svg.glyph.SSHSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.paint.Color;
import lombok.NonNull;
import lombok.experimental.Accessors;

/**
 * ssh树节点值
 *
 * @author oyzh
 * @since 2023/4/7
 */
@Accessors(chain = true, fluent = true)
public class SSHConnectTreeItemValue extends RichTreeItemValue {

    public SSHConnectTreeItemValue(@NonNull SSHConnectTreeItem item) {
        super(item);
    }

    @Override
    protected SSHConnectTreeItem item() {
        return (SSHConnectTreeItem) super.item();
    }

    @Override
    public String name() {
        return this.item().value().getName();
    }

    @Override
    public SVGGlyph graphic() {
        if (this.graphic == null) {
            this.graphic = new LinuxSVGGlyph("12");
        }
        return super.graphic();
    }

    @Override
    public Color graphicColor() {
        if (this.item().isConnected() || this.item().isConnecting()) {
            return Color.GREEN;
        }
        return super.graphicColor();
    }
}
