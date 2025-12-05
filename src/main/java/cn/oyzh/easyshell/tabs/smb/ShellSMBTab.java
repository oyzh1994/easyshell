package cn.oyzh.easyshell.tabs.smb;

import cn.oyzh.common.object.ObjectWatcher;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.fx.ShellOsTypeComboBox;
import cn.oyzh.easyshell.smb.ShellSMBClient;
import cn.oyzh.easyshell.tabs.ShellConnectTab;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.event.Event;
import javafx.scene.Cursor;

/**
 * smb tab
 *
 * @author oyzh
 * @since 2025/7/23
 */
public class ShellSMBTab extends ShellConnectTab {

    public ShellSMBTab(ShellConnect connect) {
        this.init(connect);
        ObjectWatcher.watch(this);
    }

    @Override
    protected String url() {
        return "/tabs/smb/shellSMBTab.fxml";
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
    public ShellSMBTabController controller() {
        return (ShellSMBTabController) super.controller();
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
    public ShellSMBClient client() {
        return this.controller().client();
    }

    @Override
    protected void onTabClosed(Event event) {
        super.onTabClosed(event);
        this.destroy();
    }
}
