package cn.oyzh.easyssh.tabs.connect;

import cn.oyzh.easyssh.ssh.SSHClient;
import cn.oyzh.easyssh.ssh.SSHSftp;
import cn.oyzh.easyssh.ssh.SSHTermWidget;
import cn.oyzh.easyssh.ssh.SSHTtyConnector;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.i18n.I18nHelper;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSchException;
import com.techsenger.jeditermfx.core.util.TermSize;
import com.techsenger.jeditermfx.ui.DefaultHyperlinkFilter;
import com.techsenger.jeditermfx.ui.settings.DefaultSettingsProvider;
import javafx.event.Event;
import javafx.fxml.FXML;

import java.io.IOException;

/**
 * ssh命令行tab内容组件
 *
 * @author oyzh
 * @since 2023/07/21
 */
public class SSHSftpTabController extends SubTabController {

    /**
     * ssh命令行文本域
     */
    @FXML
    private FXTab root;



    public void init() throws IOException, JSchException {
        SSHClient client = this.client();
        SSHSftp sftp = client.openSftp();
        sftp.connect(client.connectTimeout());
        if (!sftp.isConnected()) {
            MessageBox.warn(I18nHelper.connectFail());
        }
    }

    @Override
    public void onTabClosed(Event event) {
        super.onTabClosed(event);
        this.client().close();
    }

    @Override
    public SSHConnectTabController parent() {
        return (SSHConnectTabController) super.parent();
    }

    public SSHClient client() {
        return this.parent().client();
    }
}
