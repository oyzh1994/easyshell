package cn.oyzh.easyshell.tabs.key;

import cn.oyzh.common.object.ObjectWatcher;
import cn.oyzh.fx.gui.svg.glyph.key.KeySVGGlyph;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
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
        ObjectWatcher.watch(this);
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

    @Override
    protected void onTabClosed(Event event) {
        super.onTabClosed(event);
        this.destroy();
    }

}
