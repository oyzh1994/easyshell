package cn.oyzh.easyshell.tabs.smb;

import cn.oyzh.common.object.ObjectWatcherManager;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.fx.ShellOsTypeComboBox;
import cn.oyzh.easyshell.smb.ShellSMBClient;
import cn.oyzh.easyshell.tabs.ShellConnectTab;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
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
        ObjectWatcherManager.watch(this);
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
    public ShellSMBTabController controller() {
        return (ShellSMBTabController) super.controller();
    }

    @Override
    public ShellSMBClient client() {
        return this.controller().client();
    }

    //    @Override
    //    protected void onTabClosed(Event event) {
    //        super.onTabClosed(event);
    //        this.destroy();
    //    }
}
