package cn.oyzh.easyshell.tabs.http.home;

import cn.oyzh.easyshell.http.ShellHttpWorkspace;
import cn.oyzh.fx.gui.svg.glyph.protol.HttpSVGGlyph;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.Cursor;

/**
 * HTTP 工作区主页标签页。
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellHttpHomeTab extends RichTab {

    /**
     * 构造主页标签页
     */
    public ShellHttpHomeTab() {
        this.setClosable(false);
    }

    @Override
    protected String url() {
        return "/tabs/http/home/shellHttpHomeTab.fxml";
    }

    @Override
    public String getTabTitle() {
        return I18nHelper.info();
    }

    @Override
    public void flushGraphic() {
        SVGGlyph graphic = (SVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = new HttpSVGGlyph();
            graphic.setCursor(Cursor.DEFAULT);
            this.setGraphic(graphic);
        }
    }

    /**
     * 初始化
     *
     * @param workspace 工作区
     */
    public void init(ShellHttpWorkspace workspace) {
        this.controller().init(workspace);
        this.flush();
    }

    /**
     * 刷新信息
     *
     * @param workspace 工作区
     */
    public void refresh(ShellHttpWorkspace workspace) {
        this.controller().refresh(workspace);
    }

    @Override
    public ShellHttpHomeTabController controller() {
        return (ShellHttpHomeTabController) super.controller();
    }
}
