package cn.oyzh.easyshell.tabs.terminal;

import cn.oyzh.fx.gui.svg.glyph.TerminalSVGGlyph;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.Cursor;

/**
 * shell终端tab
 *
 * @author oyzh
 * @since 2025/03/20
 */
public class ShellTerminalTab extends RichTab {

    public ShellTerminalTab() {
        super();
        super.flush();
    }

    @Override
    protected String url() {
        return "/tabs/terminal/shellTerminalTab.fxml";
    }

    @Override
    public void flushGraphic() {
        TerminalSVGGlyph glyph = (TerminalSVGGlyph) this.getGraphic();
        if (glyph == null) {
            glyph = new TerminalSVGGlyph("12");
            glyph.setCursor(Cursor.DEFAULT);
            this.graphic(glyph);
        }
    }

    @Override
    public String getTabTitle() {
        return I18nHelper.localTerminal();
    }

}
