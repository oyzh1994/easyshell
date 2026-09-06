package cn.oyzh.easyshell.tabs.ftp;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.ftp.ShellFTPClient;
import cn.oyzh.easyshell.fx.ShellOsTypeComboBox;
import cn.oyzh.easyshell.tabs.ShellConnectTab;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.node.NodeLifeCycle;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;
import javafx.scene.Cursor;

/**
 * ftp tab
 *
 * @author oyzh
 * @since 2023/7/21
 */
public class ShellFTPTab extends ShellConnectTab implements NodeLifeCycle {

    //public ShellFTPTab(ShellConnect connect) {
    //    this.init(connect);
    //    ObjectWatcherManager.watch(this);
    //}

    @Override
    protected String url() {
        return "/tabs/ftp/shellFTPTab.fxml";
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
    public ShellFTPTabController controller() {
        return (ShellFTPTabController) super.controller();
    }

    @Override
    public ShellFTPClient client() {
        return this.controller().client();
    }

     @Override
     public void onNodeInitialize() {
         NodeLifeCycle.super.onNodeInitialize();
         // 更新一下内容
         StageAdapter adapter = StageManager.getAdapter(this.window());
         if (adapter != null) {
             adapter.updateContentLater();
         }
     }

//    @Override
//    protected void onTabClosed(Event event) {
//        super.onTabClosed(event);
//        this.destroy();
//    }
}
