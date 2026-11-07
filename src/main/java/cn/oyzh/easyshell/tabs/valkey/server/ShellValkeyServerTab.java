package cn.oyzh.easyshell.tabs.valkey.server;

import cn.oyzh.easyshell.valkey.ShellValkeyClient;
import cn.oyzh.fx.gui.svg.glyph.ServerSVGGlyph;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.Cursor;

/**
 * redis服务器信息标签页
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellValkeyServerTab extends RichTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "valkey/server/shellValkeyServerTab.fxml";
    }

    @Override
    public String getTabTitle() {
        return I18nHelper.serverInfo();
    }

    @Override
    public void flushGraphic() {
        SVGGlyph graphic = (SVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = new ServerSVGGlyph();
            graphic.setCursor(Cursor.DEFAULT);
            this.setGraphic(graphic);
        }
    }

    /**
     * 初始化
     *
     * @param client redis客户端
     */
    public void init(ShellValkeyClient client) {
        this.controller().init(client);
        this.flush();
    }

    @Override
    public ShellValkeyServerTabController controller() {
        return (ShellValkeyServerTabController) super.controller();
    }
}
