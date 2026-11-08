package cn.oyzh.easyshell.trees.http.request;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.http.ShellHttpRequest;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.gui.svg.glyph.protol.HttpSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.paint.Color;

/**
 * HTTP 请求节点值
 *
 * @author oyzh
 * @since 2026-11-08
 */
public class ShellHttpRequestTreeItemValue extends RichTreeItemValue {

    /**
     * 构造请求节点值
     *
     * @param item 请求节点
     */
    public ShellHttpRequestTreeItemValue(ShellHttpRequestTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    public ShellHttpRequestTreeItem item() {
        return (ShellHttpRequestTreeItem) super.item();
    }

    @Override
    public String name() {
        ShellHttpRequest request = this.item().request();
        return StringUtil.blankToDefault(request.getName(), ShellI18nHelper.httpUntitledRequest());
    }

    @Override
    public String extra() {
        return "[" + this.item().request().normalizedMethod() + "]";
    }

    @Override
    public Color extraColor() {
        return this.methodColor();
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(new HttpSVGGlyph());
            super.graphic().disableTheme();
        }
        return super.graphic();
    }

    @Override
    public Color graphicColor() {
        return this.methodColor();
    }

    /**
     * 请求方法颜色
     *
     * @return 颜色
     */
    private Color methodColor() {
        return switch (this.item().request().normalizedMethod()) {
            case ShellHttpRequest.METHOD_GET -> Color.FORESTGREEN;
            case ShellHttpRequest.METHOD_POST -> Color.DARKORANGE;
            case ShellHttpRequest.METHOD_PUT -> Color.DODGERBLUE;
            case ShellHttpRequest.METHOD_PATCH -> Color.MEDIUMPURPLE;
            case ShellHttpRequest.METHOD_DELETE -> Color.CRIMSON;
            default -> Color.GRAY;
        };
    }
}
