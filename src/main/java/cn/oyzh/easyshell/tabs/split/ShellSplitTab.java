package cn.oyzh.easyshell.tabs.split;

import cn.oyzh.common.thread.ThreadLocalUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.tabs.ShellTermTab;
import cn.oyzh.fx.gui.svg.glyph.SplitViewSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.Cursor;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * shell-终端分屏组件tab
 *
 * @author oyzh
 * @since 2025/05/29
 */
public class ShellSplitTab extends ShellTermTab {

    /**
     * 分屏索引
     */
    private static final AtomicInteger INDEX = new AtomicInteger(0);

    public ShellSplitTab(List<ShellConnect> connects) {
        super();
        this.flush();
        this.controller().init(connects);
    }

    @Override
    protected String url() {
        String type = ThreadLocalUtil.getVal("type");
        // 默认
        if (StringUtil.isBlank(type)) {
            return "/tabs/split/shellSplitTab1.fxml";
        }
        return switch (type) {
            // 模式2
            case "type2" -> "/tabs/split/shellSplitTab2.fxml";
            // 模式3
            case "type3" -> "/tabs/split/shellSplitTab3.fxml";
            // 模式4
            case "type4" -> "/tabs/split/shellSplitTab4.fxml";
            // 模式5
            case "type5" -> "/tabs/split/shellSplitTab5.fxml";
            // 模式6
            case "type6" -> "/tabs/split/shellSplitTab6.fxml";
            // 模式7
            case "type7" -> "/tabs/split/shellSplitTab7.fxml";
            // 模式8
            case "type8" -> "/tabs/split/shellSplitTab8.fxml";
            // 模式1或者默认
            default -> "/tabs/split/shellSplitTab1.fxml";
        };
    }

    @Override
    public void flushGraphic() {
        SVGGlyph graphic = (SVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = new SplitViewSVGGlyph("12");
            graphic.setSizeStr("13");
            graphic.setCursor(Cursor.DEFAULT);
            this.setGraphic(graphic);
        }
    }

    @Override
    protected String getTabTitle() {
        return I18nHelper.termSplitView() + INDEX.incrementAndGet();
    }

    @Override
    public ShellSplitTabController controller() {
        return (ShellSplitTabController) super.controller();
    }

    @Override
    public void runSnippet(String content) throws Exception {
        super.runSnippet(content);
        this.controller().runSnippet(content);
    }

    @Override
    public ShellConnect shellConnect() {
        return null;
    }
}
