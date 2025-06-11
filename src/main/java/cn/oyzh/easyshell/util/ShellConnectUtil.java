package cn.oyzh.easyshell.util;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.ftp.ShellFTPClient;
import cn.oyzh.easyshell.local.ShellLocalClient;
import cn.oyzh.easyshell.rlogin.ShellRLoginClient;
import cn.oyzh.easyshell.serial.ShellSerialClient;
import cn.oyzh.easyshell.sftp.ShellSFTPClient;
import cn.oyzh.easyshell.ssh.ShellSSHClient;
import cn.oyzh.easyshell.telnet.ShellTelnetClient;
import cn.oyzh.easyshell.vnc.ShellVNCClient;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import com.jediterm.terminal.ui.FXTerminalPanel;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;

/**
 * shell连接工具类
 *
 * @author oyzh
 * @since 2023/07/01
 */

public class ShellConnectUtil {

    /**
     * 关闭连接
     *
     * @param client shell客户端
     * @param async  是否异步
     */
    public static void close(ShellSSHClient client, boolean async) {
        try {
            if (client != null && client.isConnected()) {
                Runnable func = client::close;
                if (async) {
                    ThreadUtil.start(func);
                } else {
                    func.run();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * 测试连接
     *
     * @param adapter      页面
     * @param shellConnect 连接信息
     */
    public static void testConnect(StageAdapter adapter, ShellConnect shellConnect) {
        StageManager.showMask(adapter, () -> {
            try {
                if (shellConnect.isSSHType()) {
                    ShellSSHClient client = new ShellSSHClient(shellConnect);
                    // 开始连接
                    client.start(5_000);
                    if (client.isConnected()) {
                        client.close();
                        MessageBox.okToast(I18nHelper.connectSuccess());
                    } else {
                        MessageBox.warn(I18nHelper.connectFail());
                    }
                } else if (shellConnect.isTelnetType()) {
                    ShellTelnetClient client = new ShellTelnetClient(shellConnect);
                    // 开始连接
                    client.start(5_000);
                    if (client.isConnected()) {
                        client.close();
                        MessageBox.okToast(I18nHelper.connectSuccess());
                    } else {
                        MessageBox.warn(I18nHelper.connectFail());
                    }
                } else if (shellConnect.isSFTPType()) {
                    ShellSFTPClient client = new ShellSFTPClient(shellConnect);
                    // 开始连接
                    client.start(5_000);
                    if (client.isConnected()) {
                        client.close();
                        MessageBox.okToast(I18nHelper.connectSuccess());
                    } else {
                        MessageBox.warn(I18nHelper.connectFail());
                    }
                } else if (shellConnect.isFTPType()) {
                    ShellFTPClient client = new ShellFTPClient(shellConnect);
                    // 开始连接
                    client.start(5_000);
                    if (client.isConnected()) {
                        client.close();
                        MessageBox.okToast(I18nHelper.connectSuccess());
                    } else {
                        MessageBox.warn(I18nHelper.connectFail());
                    }
                } else if (shellConnect.isLocalType()) {
                    ShellLocalClient client = new ShellLocalClient(shellConnect);
                    // 开始连接
                    client.start(5_000);
                    if (client.isConnected()) {
                        client.close();
                        MessageBox.okToast(I18nHelper.connectSuccess());
                    } else {
                        MessageBox.warn(I18nHelper.connectFail());
                    }
                } else if (shellConnect.isVNCType()) {
                    ShellVNCClient client = new ShellVNCClient(shellConnect);
                    // 开始连接
                    client.start(5_000);
                    if (client.isConnected()) {
                        client.close();
                        MessageBox.okToast(I18nHelper.connectSuccess());
                    } else {
                        MessageBox.warn(I18nHelper.connectFail());
                    }
                } else if (shellConnect.isRloginType()) {
                    ShellRLoginClient client = new ShellRLoginClient(shellConnect);
                    // 开始连接
                    client.start(5_000);
                    if (client.isConnected()) {
                        client.close();
                        MessageBox.okToast(I18nHelper.connectSuccess());
                    } else {
                        MessageBox.warn(I18nHelper.connectFail());
                    }
                } else if (shellConnect.isSerialType()) {
                    ShellSerialClient client = new ShellSerialClient(shellConnect);
                    // 开始连接
                    client.start(5_000);
                    if (client.isConnected()) {
                        client.close();
                        MessageBox.okToast(I18nHelper.connectSuccess());
                    } else {
                        MessageBox.warn(I18nHelper.connectFail());
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    /**
     * 初始化背景
     */
    public static void initBackground(ShellConnect connect, FXTerminalPanel terminalPanel) {
        if (!connect.isEnableBackground()) {
            return;
        }
        // 背景失效
        if (connect.isBackgroundImageInvalid()) {
            MessageBox.warn(ShellI18nHelper.connectTip7());
            return;
        }
        // 处理背景
        Node canvas = terminalPanel.getFirstChild();
        // 对画板设置透明度
        canvas.setOpacity(0.7);
        // 背景图片
        String url = connect.getBackgroundImageUrl();
        Image backgroundImage = new Image(url);
//            int imgH = (int) backgroundImage.getHeight();
//            int imgW = (int) backgroundImage.getWidth();
//            PixelReader reader = backgroundImage.getPixelReader();
//            WritableImage image = new WritableImage(imgW, imgH);
//            PixelWriter writer = image.getPixelWriter();
//            for (int y = 0; y < imgH; y++) {
//                for (int x = 0; x < imgW; x++) {
//                    try {
//                        Color color1 = reader.getColor(x, y);
//                        Color color2 = new Color(color1.getRed(), color1.getGreen(), color1.getBlue(), 0.8);
//                        writer.setColor(x, y, color2);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
        // 生成背景
        BackgroundSize backgroundSize = new BackgroundSize(
                BackgroundSize.AUTO, BackgroundSize.AUTO, false, false,
                true, true);
        BackgroundImage backgroundImg = new BackgroundImage(backgroundImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT,
                backgroundSize);
        // 创建 Background 对象
        Background background = new Background(backgroundImg);
        terminalPanel.setBackground(background);
    }
}
