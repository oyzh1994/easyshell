package cn.oyzh.easyshell.trees.http.root;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.http.ShellHttpClient;
import cn.oyzh.fx.gui.svg.glyph.protol.HttpSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * HTTP 工作区树根节点值
 *
 * @author oyzh
 * @since 2026-11-08
 */
public class ShellHttpRootTreeItemValue extends RichTreeItemValue {

    /**
     * 构造 HTTP 工作区树根节点值
     *
     * @param item 根节点
     */
    public ShellHttpRootTreeItemValue(ShellHttpRootTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    public ShellHttpRootTreeItem item() {
        return (ShellHttpRootTreeItem) super.item();
    }

    @Override
    public String name() {
        try {
            ShellHttpClient client = this.item().client();
            if (client != null) {
                return StringUtil.blankToDefault(client.connectName(), "HTTP");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "HTTP";
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(new HttpSVGGlyph());
            super.graphic().disableTheme();
        }
        return super.graphic();
    }
}
