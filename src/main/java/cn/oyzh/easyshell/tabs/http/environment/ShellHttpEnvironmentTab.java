package cn.oyzh.easyshell.tabs.http.environment;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.http.ShellHttpEnvironment;
import cn.oyzh.easyshell.tabs.http.ShellHttpTabPane;
import cn.oyzh.easyshell.trees.http.environment.ShellHttpEnvironmentTreeItem;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.gui.svg.glyph.protol.HttpSVGGlyph;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.event.Event;
import javafx.scene.Cursor;

/**
 * HTTP 环境标签页。
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellHttpEnvironmentTab extends RichTab {

    @Override
    protected String url() {
        return "/tabs/http/environment/shellHttpEnvironmentTab.fxml";
    }

    @Override
    public String getTabTitle() {
        ShellHttpEnvironment environment = this.environment();
        if (environment == null) {
            return ShellI18nHelper.httpEnvironment();
        }
        return StringUtil.blankToDefault(environment.getName(), ShellI18nHelper.httpEnvironment());
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
     * @param item    环境节点
     * @param tabPane 标签页面板
     */
    public void init(ShellHttpEnvironmentTreeItem item, ShellHttpTabPane tabPane) {
        this.controller().init(item, tabPane);
        this.flush();
    }

    /**
     * 重命名
     *
     * @param name 名称
     */
    public void rename(String name) {
        if (this.controller() != null) {
            this.controller().rename(name);
        }
        this.setText(name);
        this.setTipText(name);
    }

    /**
     * 获取环境
     *
     * @return 环境
     */
    public ShellHttpEnvironment environment() {
        return this.controller() == null ? null : this.controller().environment();
    }

    /**
     * 获取环境标识
     *
     * @return 环境标识
     */
    public String environmentId() {
        ShellHttpEnvironment environment = this.environment();
        return environment == null ? null : environment.getId();
    }

    @Override
    public ShellHttpEnvironmentTabController controller() {
        return (ShellHttpEnvironmentTabController) super.controller();
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
