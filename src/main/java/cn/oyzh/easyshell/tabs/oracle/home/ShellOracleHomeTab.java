package cn.oyzh.easyshell.tabs.oracle.home;

import cn.oyzh.fx.gui.svg.glyph.database.OracleSVGGlyph;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.Cursor;

/**
 * oracle主页tab
 *
 * @author oyzh
 * @since 2025-11-10
 */
public class ShellOracleHomeTab extends RichTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "oracle/home/shellOracleHomeTab.fxml";
    }

    @Override
    public String getTabTitle() {
        return I18nHelper.info();
    }

    @Override
    public void flushGraphic() {
        OracleSVGGlyph graphic = (OracleSVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = new OracleSVGGlyph();
            graphic.setCursor(Cursor.DEFAULT);
            this.setGraphic(graphic);
        }
    }

    @Override
    public ShellOracleHomeTabController controller() {
        return (ShellOracleHomeTabController) super.controller();
    }
}
