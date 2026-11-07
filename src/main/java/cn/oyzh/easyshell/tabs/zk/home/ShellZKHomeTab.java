package cn.oyzh.easyshell.tabs.zk.home;

import cn.oyzh.easyshell.fx.svg.glyph.zk.ZookeeperSVGGlyph;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.Cursor;

/**
 * zk主页标签页
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellZKHomeTab extends RichTab {

    @Override
    protected String url() {
        return "/tabs/zk/home/shellZKHomeTab.fxml";
    }

    @Override
    public String getTabTitle() {
        return I18nHelper.info();
    }

    @Override
    public void flushGraphic() {
        SVGGlyph graphic = (SVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = new ZookeeperSVGGlyph();
            graphic.setCursor(Cursor.DEFAULT);
            this.setGraphic(graphic);
        }
    }

    @Override
    public ShellZKHomeTabController controller() {
        return (ShellZKHomeTabController) super.controller();
    }
}
