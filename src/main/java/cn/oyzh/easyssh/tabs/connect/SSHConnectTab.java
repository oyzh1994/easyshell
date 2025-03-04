package cn.oyzh.easyssh.tabs.connect;

import cn.oyzh.easyssh.domain.SSHConnect;
import cn.oyzh.easyssh.fx.svg.glyph.LinuxSVGGlyph;
import cn.oyzh.easyssh.ssh.SSHClient;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.Cursor;

/**
 * ssh终端tab
 *
 * @author oyzh
 * @since 2023/7/21
 */
public class SSHConnectTab extends RichTab {

    public SSHConnectTab(SSHConnect sshConnect) {
        this.init(sshConnect);
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
     * @param sshConnect ssh信息
     */
    public void init(SSHConnect sshConnect) {
        try {
            // 初始化ssh连接
            this.controller().init(new SSHClient(sshConnect));
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
    public SSHConnectTabController controller() {
        return (SSHConnectTabController) super.controller();
    }

    /**
     * ssh信息
     *
     * @return 当前ssh信息
     */
    public SSHConnect sshConnect() {
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
