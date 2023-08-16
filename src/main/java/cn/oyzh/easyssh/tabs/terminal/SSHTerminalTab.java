package cn.oyzh.easyssh.tabs.terminal;

import cn.oyzh.easyfx.event.EventUtil;
import cn.oyzh.easyfx.svg.SVGGlyph;
import cn.oyzh.easyfx.view.FXMLLoaderExt;
import cn.oyzh.easyssh.domain.SSHInfo;
import cn.oyzh.easyssh.ssh.SSHClient;
import cn.oyzh.easyssh.ssh.SSHEvents;
import cn.oyzh.easyssh.tabs.SSHBaseTab;
import javafx.scene.CacheHint;
import javafx.scene.Cursor;
import javafx.scene.Node;

/**
 * ssh终端tab
 *
 * @author oyzh
 * @since 2023/7/21
 */
public class SSHTerminalTab extends SSHBaseTab {

    {
        this.setClosable(true);
        this.setOnCloseRequest(event -> EventUtil.fire(SSHEvents.SSH_CLOSE_CONNECT, this.info()));
        this.loadContent();
    }

    /**
     * 内容controller
     */
    private SSHTerminalTabContentController contentController;

    @Override
    protected void loadContent() {
        FXMLLoaderExt loaderExt = new FXMLLoaderExt();
        Node content = loaderExt.load("/tabs/terminal/sshTerminalTabContent.fxml");
        content.setCache(true);
        content.setCacheHint(CacheHint.QUALITY);
        this.contentController = loaderExt.getController();
        this.setContent(content);
    }

    @Override
    public void flushGraphic() {
        SVGGlyph graphic = (SVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = new SVGGlyph("/font/linux.svg", "13");
            graphic.setCursor(Cursor.DEFAULT);
            this.setGraphic(graphic);
        }
    }

    /**
     * 初始化
     *
     * @param info ssh信息
     */
    public void init(SSHInfo info) {
        try {
            if (info == null) {
                info = new SSHInfo();
                info.setName("未命名连接");
            }
            // 设置文本
            this.setText(info.getName());
            // 刷新图标
            this.flushGraphic();
            // 初始化ssh连接
            this.contentController.client(new SSHClient(info));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * ssh信息
     *
     * @return 当前ssh信息
     */
    public SSHInfo info() {
        return this.contentController.info();
    }

    /**
     * 获取ssh客户端
     *
     * @return ssh客户端
     */
    public SSHClient client() {
        return this.contentController.client();
    }
}
