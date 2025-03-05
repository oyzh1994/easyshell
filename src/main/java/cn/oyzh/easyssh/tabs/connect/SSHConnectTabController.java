package cn.oyzh.easyssh.tabs.connect;

import cn.oyzh.easyssh.domain.SSHConnect;
import cn.oyzh.easyssh.fx.ssh.SSHConnectWidget;
import cn.oyzh.easyssh.fx.ssh.SSHLoggingConnector;
import cn.oyzh.easyssh.ssh.SSHClient;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSchException;
import com.techsenger.jeditermfx.ui.DefaultHyperlinkFilter;
import com.techsenger.jeditermfx.ui.settings.DefaultSettingsProvider;
import javafx.fxml.FXML;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.io.IOException;

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

    private SSHConnectWidget widget;

    /**
     * 设置ssh客户端
     *
     * @param client ssh客户端
     */
    public void init(@NonNull SSHClient client) {
        try {
            this.client = client;
            this.client.start();
            this.widget = new SSHConnectWidget(new DefaultSettingsProvider());
            this.widget.openSession();
            this.widget.onTermination(exitCode -> this.widget.close());
            this.widget.addHyperlinkFilter(new DefaultHyperlinkFilter());
            this.root.setChild(this.widget.getPane());
//        this.waitReady();
            this.connect1();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void connect1() throws JSchException, IOException {
        SSHLoggingConnector connector = (SSHLoggingConnector) this.widget.getTtyConnector();
        ChannelShell channel = (ChannelShell) this.client.getSession().openChannel("shell");
        channel.setInputStream(System.in);
        channel.setOutputStream(System.out);
        connector.setSshInput(channel.getInputStream());
        connector.setSshOutput(channel.getOutputStream());
        channel.setPtyType("xterm");
        channel.connect();
    }

//    private void waitReady() {
////        DownLatch latch = new DownLatch(1);
//        ThreadUtil.start(() -> {
//            try {
//                do {
//                    System.out.println(this.widget.getTtyConnector().isConnected());
//                    if (this.widget.getTtyConnector().isConnected()) {
//                        this.onTtyReady();
//                        System.err.println("-----1");
//                        break;
//                    }
//                    ThreadUtil.sleep(1);
//                } while (true);
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            } finally {
////                latch.countDown();
//            }
//        });
////        latch.await();
//    }
//
//    private void onTtyReady() throws Exception {
//        SSHConnect connect = this.client.sshConnect();
//        StringBuilder builder = new StringBuilder("ssh ");
//        builder.append(connect.getUser())
//                .append("@")
//                .append(connect.hostIp())
//                .append(" -p ")
//                .append(connect.hostPort());
//        this.widget.getTtyConnector().write(builder.toString());
////        this.client.bindStream(this.widget.getProcess());
////        this.client.bindStream1(this.widget.getTtyConnector());
//    }

    /**
     * ssh信息
     *
     * @return 当前ssh信息
     */
    protected SSHConnect sshConnect() {
        return this.client.sshConnect();
    }
}
