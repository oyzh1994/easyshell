package cn.oyzh.easyshell.tabs.dameng.home;

import cn.oyzh.fx.gui.svg.glyph.database.MysqlSVGGlyph;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.Cursor;

/**
 * mysql主页tab
 *
 * @author oyzh
 * @since 2025/11/10
 */
public class ShellDamengHomeTab extends RichTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "dameng/home/shellDamengHomeTab.fxml";
    }

    @Override
    public String getTabTitle() {
        return I18nHelper.info();
    }

    @Override
    public void flushGraphic() {
        MysqlSVGGlyph graphic = (MysqlSVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = new MysqlSVGGlyph();
            graphic.setCursor(Cursor.DEFAULT);
            this.setGraphic(graphic);
        }
    }

    @Override
    public ShellDamengHomeTabController controller() {
        return (ShellDamengHomeTabController) super.controller();
    }
}
