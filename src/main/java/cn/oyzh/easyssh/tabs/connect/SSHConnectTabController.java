package cn.oyzh.easyssh.tabs.connect;

import cn.oyzh.easyssh.domain.SSHConnect;
import cn.oyzh.easyssh.fx.ssh.SSHConnectWidget;
import cn.oyzh.easyssh.ssh.SSHClient;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import com.techsenger.jeditermfx.ui.settings.DefaultSettingsProvider;
import javafx.fxml.FXML;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

/**
 * ssh命令行tab内容组件
 *
 * @author oyzh
 * @since 2023/07/21
 */
public class SSHConnectTabController extends RichTabController {

    /**
     * ssh客户端
     */
    @Getter
    @Accessors(chain = true, fluent = true)
    private SSHClient client;

    /**
     * ssh命令行文本域
     */
    @FXML
    private FXVBox root;

    /**
     * 设置ssh客户端
     *
     * @param client ssh客户端
     */
    public void init(@NonNull SSHClient client) {
        this.client = client;
        SSHConnectWidget widget = new SSHConnectWidget(new DefaultSettingsProvider());
        widget.openSession();
        widget.onTermination( exitCode -> widget.close());
        this.root.setChild(widget.getPane());
    }

    /**
     * ssh信息
     *
     * @return 当前ssh信息
     */
    protected SSHConnect sshConnect() {
        return this.client.sshConnect();
    }
}
