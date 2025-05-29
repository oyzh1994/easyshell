package cn.oyzh.easyshell.tabs.split;

import cn.oyzh.common.thread.ThreadLocalUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.fx.connect.ShellConnectTab;
import cn.oyzh.fx.gui.svg.glyph.SplitViewSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.node.NodeLifeCycle;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.Cursor;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * ssh-组件tab
 *
 * @author oyzh
 * @since 2023/7/21
 */
public class ShellSplitTab extends ShellConnectTab implements NodeLifeCycle {

    /**
     * 分屏索引
     */
    private static final AtomicInteger INDEX = new AtomicInteger(0);

    public ShellSplitTab() {
        super();
        this.flush();
    }

    @Override
    protected String url() {
        String type = ThreadLocalUtil.getVal("type");
        // 模式1
        if (StringUtil.isBlank(type) || type.equals("type1")) {
            return "/tabs/split/shellSplitTab1.fxml";
        }
        // 模式2
        if (type.equals("type2")) {
            return "/tabs/split/shellSplitTab2.fxml";
        }
        // 模式3
        if (type.equals("type3")) {
            return "/tabs/split/shellSplitTab3.fxml";
        }
        // 模式4
        if (type.equals("type4")) {
            return "/tabs/split/shellSplitTab4.fxml";
        }
        // 模式5
        if (type.equals("type5")) {
            return "/tabs/split/shellSplitTab5.fxml";
        }
        // 模式6
        if (type.equals("type6")) {
            return "/tabs/split/shellSplitTab6.fxml";
        }
        return "/tabs/split/shellSplitTab1.fxml";
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
}
