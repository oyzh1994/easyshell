package cn.oyzh.easyshell.tabs.telnet;

import cn.oyzh.common.object.ObjectWatcher;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.fx.ShellOsTypeComboBox;
import cn.oyzh.easyshell.tabs.ShellTermTab;
import cn.oyzh.easyshell.telnet.ShellTelnetClient;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.event.Event;
import javafx.scene.Cursor;

/**
 * telnet tab
 *
 * @author oyzh
 * @since 2025/04/24
 */
public class ShellTelnetTab extends ShellTermTab {

    public ShellTelnetTab(ShellConnect connect) {
        this.init(connect);
        ObjectWatcher.watch(this);
    }

    @Override
    protected String url() {
        return "/tabs/telnet/shellTelnetTab.fxml";
    }

    @Override
    public void flushGraphic() {
        SVGGlyph graphic = (SVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = ShellOsTypeComboBox.getGlyph(this.shellConnect().getOsType());
            graphic.setSizeStr("13");
            graphic.setCursor(Cursor.DEFAULT);
            this.setGraphic(graphic);
        }
    }

    /**
     * 初始化
     *
     * @param connect 连接
     */
    public void init(ShellConnect connect) {
        try {
            // 初始化shell连接
            this.controller().init(connect);
            // 刷新图标
            this.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected String getTabTitle() {
        return this.controller().shellConnect().getName() + "(" + this.shellConnect().getType().toUpperCase() + ")";
    }

    @Override
    public ShellTelnetTabController controller() {
        return (ShellTelnetTabController) super.controller();
    }

   @Override
    public ShellConnect shellConnect() {
        return this.controller().shellConnect();
    }

    /**
     * 获取shell客户端
     *
     * @return shell客户端
     */
    public ShellTelnetClient client() {
        return this.controller().getClient();
    }

    @Override
    protected void onTabClosed(Event event) {
        super.onTabClosed(event);
        this.destroy();
    }
}
