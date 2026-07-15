package cn.oyzh.easyshell.tabs.vnc;

import cn.oyzh.common.object.ObjectWatcher;
import cn.oyzh.common.object.ObjectWatcherManager;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.fx.ShellOsTypeComboBox;
import cn.oyzh.easyshell.tabs.ShellConnectTab;
import cn.oyzh.easyshell.vnc.ShellVNCClient;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.Cursor;

/**
 * vnc tab
 *
 * @author oyzh
 * @since 2023/7/21
 */
public class ShellVNCTab extends ShellConnectTab {

    public ShellVNCTab(ShellConnect connect) {
        this.init(connect);
        ObjectWatcherManager.watch(this);
    }

    @Override
    protected String url() {
        return "/tabs/vnc/shellVNCTab.fxml";
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
    public ShellVNCTabController controller() {
        return (ShellVNCTabController) super.controller();
    }

    @Override
    public ShellVNCClient client() {
        return this.controller().client();
    }
}
