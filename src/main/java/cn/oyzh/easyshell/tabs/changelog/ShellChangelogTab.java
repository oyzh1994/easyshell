package cn.oyzh.easyshell.tabs.changelog;

import cn.oyzh.fx.gui.svg.glyph.ChangelogSVGGlyph;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.Cursor;

/**
 * ssh更新日志表tab
 *
 * @author oyzh
 * @since 2024/04/07
 */
public class ShellChangelogTab extends RichTab {

    public ShellChangelogTab() {
        super();
        super.flush();
    }

    @Override
    public void flushGraphic() {
        ChangelogSVGGlyph glyph = (ChangelogSVGGlyph) this.getGraphic();
        if (glyph == null) {
            glyph = new ChangelogSVGGlyph("12");
            glyph.setCursor(Cursor.DEFAULT);
            this.graphic(glyph);
        }
    }

    @Override
    protected String url() {
        return "/tabs/changelog/sshChangelogTab.fxml";
    }

    @Override
    public String getTabTitle() {
        return I18nHelper.changelogTitle();
    }

}
