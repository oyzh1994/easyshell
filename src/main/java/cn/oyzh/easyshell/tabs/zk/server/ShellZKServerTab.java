package cn.oyzh.easyshell.tabs.zk.server;

import cn.oyzh.easyshell.zk.ShellZKClient;
import cn.oyzh.fx.gui.svg.glyph.ServerSVGGlyph;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.Cursor;

/**
 * zk服务标签页
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellZKServerTab extends RichTab {

    @Override
    protected String url() {
        return "/tabs/zk/server/shellZKServerTab.fxml";
    }

    @Override
    public String getTabTitle() {
        return I18nHelper.server();
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
     * @param client 客户端
     */
    public void init(ShellZKClient client) {
        this.controller().init(client);
        this.flush();
    }

    @Override
    public ShellZKServerTabController controller() {
        return (ShellZKServerTabController) super.controller();
    }
}
