package cn.oyzh.easyshell.tabs.telnet;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.fx.ShellOsTypeComboBox;
import cn.oyzh.easyshell.telnet.ShellTelnetClient;
import cn.oyzh.easyshell.trees.connect.ShellConnectTreeItem;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.Cursor;

/**
 * telnet tab
 *
 * @author oyzh
 * @since 2025/04/24
 */
public class ShellTelnetTab extends RichTab {

    public ShellTelnetTab(ShellConnectTreeItem item) {
        this.init(item);
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
     * @param treeItem shell信息
     */
    public void init(ShellConnectTreeItem treeItem) {
        try {
            // 初始化shell连接
            this.controller().init(treeItem);
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

    /**
     * shell信息
     *
     * @return 当前shell信息
     */
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
}
