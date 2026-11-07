package cn.oyzh.easyshell.tabs.redis.server;

import cn.oyzh.easyshell.redis.ShellRedisClient;
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
public class ShellRedisServerTab extends RichTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "redis/server/shellRedisServerTab.fxml";
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
    public void init(ShellRedisClient client) {
        this.controller().init(client);
        this.flush();
    }

    @Override
    public ShellRedisServerTabController controller() {
        return (ShellRedisServerTabController) super.controller();
    }
}
