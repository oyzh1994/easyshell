package cn.oyzh.easyshell.tabs.zk.terminal;

import cn.oyzh.easyshell.zk.ShellZKClient;
import cn.oyzh.fx.gui.svg.glyph.TerminalSVGGlyph;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.Cursor;

/**
 * zk终端标签页
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellZKTerminalTab extends RichTab {

    @Override
    protected String url() {
        return "/tabs/zk/terminal/shellZKTerminalTab.fxml";
    }

    @Override
    public String getTabTitle() {
        return I18nHelper.terminal();
    }

    @Override
    public void flushGraphic() {
        SVGGlyph graphic = (SVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = new TerminalSVGGlyph();
            graphic.setCursor(Cursor.DEFAULT);
            this.setGraphic(graphic);
        }
    }

    /**
     * 初始化
     *
     * @param client 客户端
     */
    public void init(ShellZKClient client) {
        this.controller().init(client);
        this.flush();
    }

    @Override
    public ShellZKTerminalTabController controller() {
        return (ShellZKTerminalTabController) super.controller();
    }
}
