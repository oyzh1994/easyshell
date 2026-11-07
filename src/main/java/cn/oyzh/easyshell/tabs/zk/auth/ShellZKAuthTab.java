package cn.oyzh.easyshell.tabs.zk.auth;

import cn.oyzh.easyshell.fx.svg.glyph.zk.AuditSVGGlyph;
import cn.oyzh.easyshell.zk.ShellZKClient;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.Cursor;

/**
 * zk认证标签页
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellZKAuthTab extends RichTab {

    @Override
    protected String url() {
        return "/tabs/zk/auth/shellZKAuthTab.fxml";
    }

    @Override
    public String getTabTitle() {
        return I18nHelper.auth();
    }

    @Override
    public void flushGraphic() {
        SVGGlyph graphic = (SVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = new AuditSVGGlyph();
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
    public ShellZKAuthTabController controller() {
        return (ShellZKAuthTabController) super.controller();
    }
}
