package cn.oyzh.easyshell.tabs.key;

import cn.oyzh.common.object.ObjectWatcherManager;
import cn.oyzh.fx.gui.svg.glyph.key.KeySVGGlyph;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.Cursor;

/**
 * shell密钥管理标签页
 *
 * @author oyzh
 * @since 2025/03/20
 */
public class ShellKeyTab extends RichTab {

    /**
     * 构造密钥管理标签页
     */
    public ShellKeyTab() {
        super();
        super.flush();
        ObjectWatcherManager.watch(this);
    }

    @Override
    protected String url() {
        return "/tabs/key/shellKeyTab.fxml";
    }

    @Override
    public void flushGraphic() {
        KeySVGGlyph glyph = (KeySVGGlyph) this.getGraphic();
        if (glyph == null) {
            glyph = new KeySVGGlyph();
            glyph.setCursor(Cursor.DEFAULT);
            this.graphic(glyph);
        }
    }

    @Override
    public String getTabTitle() {
        return I18nHelper.key1Manager();
    }

//    @Override
//    protected void onTabClosed(Event event) {
//        super.onTabClosed(event);
//        this.destroy();
//    }

}
