package cn.oyzh.easyshell.controller.tool;

import cn.oyzh.common.thread.DownLatch;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.text.field.NumberTextField;
import cn.oyzh.fx.gui.text.field.PortTextField;
import cn.oyzh.fx.plus.controller.SubStageController;
import cn.oyzh.fx.plus.controls.text.area.FXTextArea;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import org.apache.commons.net.telnet.TelnetClient;

import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.util.concurrent.TimeUnit;


/**
 * shell工具箱 telnet业务
 *
 * @author oyzh
 * @since 2025/05/29
 */
public class ShellToolTelnetTabController extends SubStageController {

    /**
     * telnet地址
     */
    @FXML
    private ClearableTextField telnetHost;

    /**
     * telnet端口
     */
    @FXML
    private PortTextField telnetPort;

    /**
     * telnet超时
     */
    @FXML
    private NumberTextField telnetTimeout;

    /**
     * telnet文本域
     */
    @FXML
    private FXTextArea telnetArea;

    /**
     * 执行telnet
     */
    @FXML
    private void execTelnet() {
        if (!this.telnetHost.validate()) {
            return;
        }
        StageManager.showMask(() -> {
            try {
                // 清除记录
                this.telnetArea.clear();
                // 超时时间
                int timeout = this.telnetTimeout.getIntValue();
                // 创建客户端
                TelnetClient client = new TelnetClient();
                // 设置超时
                client.setConnectTimeout(timeout * 1000);
                // 执行连接
                client.connect(this.telnetHost.getTextTrim(), this.telnetPort.getIntValue());
                // 连接成功
                if (client.isConnected()) {
                    DownLatch latch = DownLatch.of();
                    Thread thread = ThreadUtil.start(() -> {
                        try {
                            // 读取数据
                            InputStreamReader reader = new InputStreamReader(client.getInputStream());
                            int len;
                            char[] buffer = new char[1024];
                            while (true) {
                                len = reader.read(buffer, 0, buffer.length);
                                if (len == -1) {
                                    break;
                                }
                                String str = new String(buffer, 0, len);
                                this.telnetArea.appendText(str);
                                ThreadUtil.sleep(5);
                            }
                        } catch (Exception ex) {
                            if (!(ex instanceof InterruptedIOException)) {
                                ex.printStackTrace();
                                this.telnetArea.appendLine(ex.getMessage());
                            }
                        } finally {
                            latch.countDown();
                        }
                    });
                    // 设置等待超时
                    if (!latch.await(timeout, TimeUnit.SECONDS)) {
                        thread.interrupt();
                    }
                    // 断开连接
                    client.disconnect();
                } else {// 连接失败
                    // 断开连接
                    client.disconnect();
                    MessageBox.warn(I18nHelper.connectFail());
                }
            } catch (Exception ex) {
                this.telnetArea.appendLine(ex.getMessage());
                MessageBox.warn(I18nHelper.connectFail());
            }
        });
    }
}
