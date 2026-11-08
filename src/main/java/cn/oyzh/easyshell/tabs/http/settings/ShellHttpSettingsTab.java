package cn.oyzh.easyshell.tabs.http.settings;

import cn.oyzh.easyshell.http.ShellHttpWorkspace;
import cn.oyzh.easyshell.tabs.http.ShellHttpTabPane;
import cn.oyzh.fx.gui.svg.glyph.SettingSVGGlyph;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.scene.Cursor;

/**
 * HTTP 设置标签页。
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellHttpSettingsTab extends RichTab {

    @Override
    protected String url() {
        return "/tabs/http/settings/shellHttpSettingsTab.fxml";
    }

    @Override
    public String getTabTitle() {
        return I18nHelper.setting();
    }

    @Override
    public void flushGraphic() {
        SVGGlyph graphic = (SVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = new SettingSVGGlyph();
            graphic.setCursor(Cursor.DEFAULT);
            this.setGraphic(graphic);
        }
    }

    /**
     * 初始化
     *
     * @param workspace 工作区
     * @param tabPane   标签页面板
     */
    public void init(ShellHttpWorkspace workspace, ShellHttpTabPane tabPane) {
        this.controller().init(workspace, tabPane);
        this.flush();
    }

    @Override
    public ShellHttpSettingsTabController controller() {
        return (ShellHttpSettingsTabController) super.controller();
    }

    @Override
    protected void onTabCloseRequest(Event event) {
        if (this.controller() != null) {
            this.controller().flush();
        }
        this.closeTab();
    }

    @Override
    protected void onTabClosed(Event event) {
        if (this.controller() != null) {
            this.controller().flush();
        }
        super.onTabClosed(event);
    }
}
