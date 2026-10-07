package cn.oyzh.easyshell.tabs.changelog;

import cn.oyzh.common.object.ObjectWatcherManager;
import cn.oyzh.fx.gui.svg.glyph.ChangelogSVGGlyph;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.Cursor;

/**
 * shell更新日志标签页
 *
 * @author oyzh
 * @since 2024/04/07
 */
public class ShellChangelogTab extends RichTab {

    /**
     * 构造更新日志标签页
     */
    public ShellChangelogTab() {
        super();
        super.flush();
        ObjectWatcherManager.watch(this);
    }

    @Override
    public void flushGraphic() {
        ChangelogSVGGlyph glyph = (ChangelogSVGGlyph) this.getGraphic();
        if (glyph == null) {
            glyph = new ChangelogSVGGlyph();
            glyph.setCursor(Cursor.DEFAULT);
            this.graphic(glyph);
        }
    }

    @Override
    protected String url() {
        return "/tabs/changelog/shellChangelogTab.fxml";
    }

    @Override
    public String getTabTitle() {
        return I18nHelper.changelogTitle();
    }

}
