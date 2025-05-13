package cn.oyzh.easyshell.tabs.sftp;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.fx.ShellOsTypeComboBox;
import cn.oyzh.easyshell.sftp.ShellSFTPClient;
import cn.oyzh.easyshell.trees.connect.ShellConnectTreeItem;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.Cursor;

/**
 * sftp tab
 *
 * @author oyzh
 * @since 2023/7/21
 */
public class ShellSFTPTab extends RichTab {

    public ShellSFTPTab(ShellConnectTreeItem item) {
        this.init(item);
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
     * @param treeItem shell信息
     */
    public void init(ShellConnectTreeItem treeItem) {
        try {
            // 初始化shell连接
            this.controller().init(treeItem.value());
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
    public ShellSFTPClient client() {
        return this.controller().client();
    }
}
