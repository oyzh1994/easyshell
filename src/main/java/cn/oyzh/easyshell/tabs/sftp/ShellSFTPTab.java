package cn.oyzh.easyshell.tabs.sftp;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.fx.ShellOsTypeComboBox;
import cn.oyzh.easyshell.sftp2.ShellSFTPClient;
import cn.oyzh.easyshell.tabs.ShellConnectTab;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.Cursor;

/**
 * sftp tab
 *
 * @author oyzh
 * @since 2025/04/25
 */
public class ShellSFTPTab extends ShellConnectTab {

    //public ShellSFTPTab(ShellConnect connect) {
    //    this.init(connect);
    //    ObjectWatcherManager.watch(this);
    //}

    @Override
    protected String url() {
        return "/tabs/sftp/shellSFTPTab.fxml";
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
    public ShellSFTPTabController controller() {
        return (ShellSFTPTabController) super.controller();
    }

    @Override
    public ShellSFTPClient client() {
        return this.controller().client();
    }

//    @Override
//    protected void onTabClosed(Event event) {
//        super.onTabClosed(event);
//        this.destroy();
//    }
}
