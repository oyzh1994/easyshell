package cn.oyzh.easyshell.tabs.connect;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.fx.svg.glyph.LinuxSVGGlyph;
import cn.oyzh.easyshell.ssh.SSHClient;
import cn.oyzh.easyshell.trees.connect.ShellConnectTreeItem;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.Cursor;

/**
 * ssh终端tab
 *
 * @author oyzh
 * @since 2023/7/21
 */
public class ShellConnectTab extends RichTab {

    public ShellConnectTab(ShellConnectTreeItem item) {
        this.init(item);
    }

    public ShellConnectTab( ) {

    }

    @Override
    protected String url() {
        return "/tabs/connect/sshConnectTab.fxml";
    }

    @Override
    public void flushGraphic() {
        SVGGlyph graphic = (SVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = new LinuxSVGGlyph("13");
            graphic.setCursor(Cursor.DEFAULT);
            this.setGraphic(graphic);
        }
    }

    /**
     * 初始化
     *
     * @param treeItem ssh信息
     */
    public void init(ShellConnectTreeItem treeItem) {
        try {
            // 初始化ssh连接
            this.controller().init(treeItem);
            // 刷新图标
            this.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected String getTabTitle() {
        return this.controller().sshConnect().getName();
    }

    @Override
    public ShellConnectTabController controller() {
        return (ShellConnectTabController) super.controller();
    }

    /**
     * ssh信息
     *
     * @return 当前ssh信息
     */
    public ShellConnect sshConnect() {
        return this.controller().sshConnect();
    }

    /**
     * 获取ssh客户端
     *
     * @return ssh客户端
     */
    public SSHClient client() {
        return this.controller().client();
    }
}
