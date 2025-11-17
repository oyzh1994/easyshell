package cn.oyzh.easyshell.tabs.message;

import cn.oyzh.fx.gui.svg.glyph.MessageSVGGlyph;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.Cursor;

/**
 * shell消息tab
 *
 * @author oyzh
 * @since 2023/6/24
 */
public class ShellMessageTab extends RichTab {

    public ShellMessageTab() {
        super();
        super.flush();
    }

    @Override
    protected String url() {
        return "/tabs/message/shellMessageTab.fxml";
    }

    @Override
    public void flushGraphic() {
        MessageSVGGlyph glyph = (MessageSVGGlyph) this.getGraphic();
        if (glyph == null) {
            glyph = new MessageSVGGlyph("12");
            glyph.setCursor(Cursor.DEFAULT);
            this.graphic(glyph);
        }
    }

    @Override
    public String getTabTitle() {
        return I18nHelper.message();
    }

}
