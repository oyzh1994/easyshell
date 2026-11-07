package cn.oyzh.easyshell.tabs.valkey.subscribe;

import cn.oyzh.easyshell.fx.svg.glyph.SubscribeSVGGlyph;
import cn.oyzh.easyshell.valkey.ShellValkeyClient;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;
import cn.oyzh.i18n.I18nResourceBundle;
import javafx.scene.Cursor;

/**
 * redis订阅/发布标签页
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellValkeySubscribeTab extends RichTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "valkey/subscribe/shellValkeySubscribeTab.fxml";
    }

    @Override
    public String getTabTitle() {
        return I18nHelper.subscribe() + "&" + I18nResourceBundle.i18nString("base.publish");
    }

    @Override
    public void flushGraphic() {
        SVGGlyph graphic = (SVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = new SubscribeSVGGlyph();
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
    public ShellValkeySubscribeTabController controller() {
        return (ShellValkeySubscribeTabController) super.controller();
    }
}
