package cn.oyzh.easyshell.tabs.home;

import cn.oyzh.fx.gui.svg.glyph.HomeSVGGlyph;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.Cursor;

/**
 * ssh主页tab
 *
 * @author oyzh
 * @since 2023/6/24
 */
public class SSHHomeTab extends RichTab {

    public SSHHomeTab() {
        super();
        super.flush();
    }

    @Override
    protected String url() {
        return "/tabs/home/sshHomeTab.fxml";
    }

    @Override
    public void flushGraphic() {
        HomeSVGGlyph glyph = (HomeSVGGlyph) this.getGraphic();
        if (glyph == null) {
            glyph = new HomeSVGGlyph("12");
            glyph.setCursor(Cursor.DEFAULT);
            this.graphic(glyph);
        }
    }

    @Override
    public String getTabTitle() {
        return I18nHelper.homeTitle();
    }

}
