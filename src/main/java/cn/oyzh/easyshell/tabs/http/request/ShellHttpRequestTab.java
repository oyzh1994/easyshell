package cn.oyzh.easyshell.tabs.http.request;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.http.ShellHttpRequest;
import cn.oyzh.easyshell.tabs.http.ShellHttpTabPane;
import cn.oyzh.easyshell.trees.http.request.ShellHttpRequestTreeItem;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.gui.svg.glyph.protol.HttpSVGGlyph;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.event.Event;
import javafx.scene.Cursor;

/**
 * HTTP 请求标签页。
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellHttpRequestTab extends RichTab {

    /**
     * 构造请求标签页
     */
    public ShellHttpRequestTab() {
        super.setClosable(true);
    }

    @Override
    protected String url() {
        return "/tabs/http/request/shellHttpRequestTab.fxml";
    }

    @Override
    public String getTabTitle() {
        ShellHttpRequest request = this.request();
        if (request == null) {
            return ShellI18nHelper.httpUntitledRequest();
        }
        return StringUtil.blankToDefault(request.getName(), ShellI18nHelper.httpUntitledRequest());
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
     * @param item    请求节点
     * @param tabPane 标签页面板
     */
    public void init(ShellHttpRequestTreeItem item, ShellHttpTabPane tabPane) {
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
     * 获取请求
     *
     * @return 请求
     */
    public ShellHttpRequest request() {
        return this.controller() == null ? null : this.controller().request();
    }

    /**
     * 获取请求标识
     *
     * @return 请求标识
     */
    public String requestId() {
        ShellHttpRequest request = this.request();
        return request == null ? null : request.getId();
    }

    @Override
    public ShellHttpRequestTabController controller() {
        return (ShellHttpRequestTabController) super.controller();
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
