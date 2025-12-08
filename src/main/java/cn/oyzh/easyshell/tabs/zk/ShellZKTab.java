package cn.oyzh.easyshell.tabs.zk;

import cn.oyzh.common.object.ObjectWatcher;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.fx.ShellOsTypeComboBox;
import cn.oyzh.easyshell.tabs.ShellConnectTab;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.event.Event;
import javafx.scene.Cursor;

/**
 * @author oyzh
 * @since 2024-12-03
 */
public class ShellZKTab extends ShellConnectTab {

    public ShellZKTab(ShellConnect connect) {
        super();
        this.init(connect);
        super.flush();
        ObjectWatcher.watch(this);
    }

    @Override
    public String getTabTitle() {
        return this.connect.getName() + "(" + this.connect.getType().toUpperCase() + ")";
    }

    @Override
    public void flushGraphic() {
        SVGGlyph graphic = (SVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = ShellOsTypeComboBox.getGlyph(this.connect.getOsType());
            graphic.setSizeStr("13");
            graphic.setCursor(Cursor.DEFAULT);
            this.setGraphic(graphic);
        }
    }

    @Override
    protected String url() {
        return "/tabs/zk/shellZKTab.fxml";
    }

    @Override
    protected ShellZKTabController controller() {
        return (ShellZKTabController) super.controller();
    }

    /**
     * 连接
     */
    private ShellConnect connect;

    public void init(ShellConnect connect) {
        this.connect = connect;
        this.controller().init(connect);
    }

    @Override
    public ShellConnect shellConnect() {
        return this.connect;
    }

    @Override
    protected void onTabClosed(Event event) {
        super.onTabClosed(event);
        this.destroy();
    }
}
