package cn.oyzh.easyshell.tabs.telnet;

import cn.oyzh.common.object.ObjectWatcherManager;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.fx.ShellOsTypeComboBox;
import cn.oyzh.easyshell.tabs.ShellTermTab;
import cn.oyzh.easyshell.telnet.ShellTelnetClient;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
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
        ObjectWatcherManager.watch(this);
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
            graphic.setCursor(Cursor.DEFAULT);
            this.setGraphic(graphic);
        }
    }

    @Override
    public void init(ShellConnect connect) {
        try {
            // 初始化shell连接
            this.controller().init(connect);
            // 刷新图标
            super.init(connect);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected String getTabTitle() {
        return this.shellConnect().getName() + "(" + this.shellConnect().getType().toUpperCase() + ")";
    }

    @Override
    public ShellTelnetTabController controller() {
        return (ShellTelnetTabController) super.controller();
    }

    @Override
    public ShellTelnetClient client() {
        return this.controller().getClient();
    }

    @Override
    public void runSnippet(String content) throws Exception {
        this.controller().runSnippet(content);
    }

//    @Override
//    protected void onTabClosed(Event event) {
//        super.onTabClosed(event);
//        this.destroy();
//    }
}
