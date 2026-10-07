package cn.oyzh.easyshell.tabs.mongo.home;

import cn.oyzh.fx.gui.svg.glyph.HomeSVGGlyph;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;
import javafx.scene.Cursor;

/**
 * mongodb主页tab
 *
 * @author oyzh
 * @since 2026-06-29
 */
public class ShellMongoHomeTab extends RichTab {

    /**
     * 构造 mongodb 主页标签页
     */
    public ShellMongoHomeTab() {
        super();
        super.flush();
    }

    @Override
    protected String url() {
        return "/tabs/mongo/home/shellMongoHomeTab.fxml";
    }

    @Override
    public void flushGraphic() {
        SVGGlyph graphic = (SVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = new HomeSVGGlyph();
            graphic.setCursor(Cursor.DEFAULT);
            this.setGraphic(graphic);
        }
    }

    @Override
    protected String getTabTitle() {
        return I18nResourceBundle.i18nString("base.title.home");
    }

    @Override
    public void initNode() {
        this.setClosable(false);
        super.initNode();
    }
}
