package cn.oyzh.easyshell.tabs.mongo.home;

import cn.oyzh.fx.gui.svg.glyph.HomeSVGGlyph;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;
import javafx.scene.Cursor;

/**
 * redis主页tab
 *
 * @author oyzh
 * @since 2023/6/24
 */
public class MongoHomeTab extends RichTab {

    public MongoHomeTab() {
        super();
        super.flush();
    }

    @Override
    protected String url() {
        return "/tabs/mongo/home/mongoHomeTab.fxml";
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
