package cn.oyzh.easyshell.tabs.sftp;

import cn.oyzh.common.object.ObjectWatcher;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.fx.ShellOsTypeComboBox;
import cn.oyzh.easyshell.sftp2.ShellSFTPClient;
import cn.oyzh.easyshell.tabs.ShellConnectTab;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.event.Event;
import javafx.scene.Cursor;

/**
 * sftp tab
 *
 * @author oyzh
 * @since 2025/04/25
 */
public class ShellSFTPTab extends ShellConnectTab {

    public ShellSFTPTab(ShellConnect connect) {
        this.init(connect);
        ObjectWatcher.watch(this);
    }

    @Override
    protected String url() {
        return "/tabs/sftp/shellSFTPTab.fxml";
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
    public ShellSFTPTabController controller() {
        return (ShellSFTPTabController) super.controller();
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
    public ShellSFTPClient client() {
        return this.controller().client();
    }

    @Override
    protected void onTabClosed(Event event) {
        super.onTabClosed(event);
        Object controller = this.controller();
        this.destroy();
        System.out.println(controller);

    }
}
