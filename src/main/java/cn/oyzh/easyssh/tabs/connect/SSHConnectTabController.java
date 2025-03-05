package cn.oyzh.easyssh.tabs.connect;

import cn.oyzh.easyssh.domain.SSHConnect;
import cn.oyzh.easyssh.fx.ssh.SSHConnectWidget;
import cn.oyzh.easyssh.fx.ssh.SSHTtyConnector;
import cn.oyzh.easyssh.ssh.SSHClient;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.i18n.I18nHelper;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSchException;
import com.techsenger.jeditermfx.core.TtyConnector;
import com.techsenger.jeditermfx.core.util.TermSize;
import com.techsenger.jeditermfx.ui.DefaultHyperlinkFilter;
import com.techsenger.jeditermfx.ui.settings.DefaultSettingsProvider;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
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
            if (!this.client.isConnected()) {
                this.client.start();
            }
            if (!this.client.isConnected()) {
                MessageBox.warn(I18nHelper.connectFail());
                return;
            }
            ChannelShell shell = this.client.openShell();
            this.initWidget(shell);
            shell.connect(this.client.connectTimeout());
            if (!shell.isConnected()) {
                MessageBox.warn(I18nHelper.connectFail());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    private void initWidget(ChannelShell shell) throws IOException {
        this.widget = new SSHConnectWidget(new DefaultSettingsProvider());
        SSHTtyConnector connector = (SSHTtyConnector) this.widget.createTtyConnector();
        connector.initShell(shell);
        this.widget.openSession(connector);
        this.widget.onTermination(exitCode -> this.widget.close());
        this.widget.addHyperlinkFilter(new DefaultHyperlinkFilter());
        Pane pane = this.widget.getPane();
        this.root.setChild(this.widget.getPane());
//        pane.widthProperty().addListener((observable, oldValue, newValue) -> this.initShellSize());
//        pane.heightProperty().addListener((observable, oldValue, newValue) -> this.initShellSize());
        connector.terminalSizeProperty().addListener((observable, oldValue, newValue) -> this.initShellSize());
    }

    private void initShellSize()   {
        int sizeW = (int) this.widget.getWidth();
        int sizeH = (int) this.widget.getHeight();
        TermSize termSize = this.widget.getTermSize();
//        System.out.println(termSize);
        ChannelShell shell = this.client.openShell();
        shell.setPtySize(termSize.getColumns(), termSize.getRows(), sizeW, sizeH);
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
