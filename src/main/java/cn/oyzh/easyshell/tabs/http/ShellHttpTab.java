package cn.oyzh.easyshell.tabs.http;

import cn.oyzh.common.util.UUIDUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.fx.ShellOsTypeComboBox;
import cn.oyzh.easyshell.http.ShellHttpClient;
import cn.oyzh.easyshell.tabs.ShellConnectTab;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.Cursor;

/**
 * HTTP 工作区标签页。
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellHttpTab extends ShellConnectTab {

    private final String tabId = UUIDUtil.uuidSimple();

    @Override
    protected String getTabTitle() {
        return this.shellConnect().getName() + "(" + this.shellConnect().getType().toUpperCase() + ")";
    }

    public String getTabName() {
        return this.getTabTitle();
    }

    public String getTabId() {
        return tabId;
    }

    @Override
    protected String url() {
        return "/tabs/http/shellHttpTab.fxml";
    }

    @Override
    public void flushGraphic() {
        SVGGlyph graphic = (SVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = ShellOsTypeComboBox.getGlyph(this.shellConnect().getOsType());
            graphic.setCursor(Cursor.DEFAULT);
            this.setGraphic(graphic);
        }
    }

    @Override
    public void init(ShellConnect connect) {
        try {
            this.controller().init(connect);
            super.init(connect);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected ShellHttpTabController controller() {
        return (ShellHttpTabController) super.controller();
    }

    @Override
    public ShellHttpClient client() {
        return this.controller().getClient();
    }

    public static ShellHttpTab of(ShellConnect connect) {
        ShellHttpTab tab = new ShellHttpTab();
        tab.init(connect);
        return tab;
    }
}
