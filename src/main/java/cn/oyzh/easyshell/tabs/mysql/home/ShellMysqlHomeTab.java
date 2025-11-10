package cn.oyzh.easyshell.tabs.mysql.home;

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
public class ShellMysqlHomeTab extends RichTab {

    {
        this.setClosable(true);
    }

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "mysql/home/shellMysqlHomeTab.fxml";
    }

    @Override
    public String getTabTitle() {
        return I18nHelper.info();
    }

    @Override
    public void flushGraphic() {
        MysqlSVGGlyph graphic = (MysqlSVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = new MysqlSVGGlyph("12");
            graphic.setCursor(Cursor.DEFAULT);
            this.setGraphic(graphic);
        }
    }

    @Override
    public ShellMysqlHomeController controller() {
        return (ShellMysqlHomeController) super.controller();
    }
}
