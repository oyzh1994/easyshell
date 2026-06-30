package cn.oyzh.easyshell.mongo.tabs;

import cn.oyzh.common.object.ObjectWatcherManager;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.fx.ShellOsTypeComboBox;
import cn.oyzh.easyshell.tabs.ShellConnectTab;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.Cursor;

/**
 * @author oyzh
 * @since 2024-09-12
 */
public class ShellMongoTab extends ShellConnectTab {

    public ShellMongoTab(ShellConnect connect) {
        super();
        this.init(connect);
        super.flush();
        ObjectWatcherManager.watch(this);
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
            graphic.setCursor(Cursor.DEFAULT);
            this.setGraphic(graphic);
        }
    }

    @Override
    protected String url() {
        return "/tabs/mongo/shellMongoTab.fxml";
    }

    @Override
    protected ShellMongoTabController controller() {
        return (ShellMongoTabController) super.controller();
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
}
