package cn.oyzh.easyshell.tabs.key;

import cn.oyzh.fx.gui.svg.glyph.key.KeySVGGlyph;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.Cursor;

/**
 * shell终端tab
 *
 * @author oyzh
 * @since 2025/03/20
 */
public class ShellKeyTab extends RichTab {

    public ShellKeyTab() {
        super();
        super.flush();
    }

    @Override
    protected String url() {
        return "/tabs/key/shellKeyTab.fxml";
    }

    @Override
    public void flushGraphic() {
        KeySVGGlyph glyph = (KeySVGGlyph) this.getGraphic();
        if (glyph == null) {
            glyph = new KeySVGGlyph("12");
            glyph.setCursor(Cursor.DEFAULT);
            this.graphic(glyph);
        }
    }

    @Override
    public String getTabTitle() {
        return I18nHelper.key1Manager();
    }

}
