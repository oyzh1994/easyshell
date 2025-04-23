package cn.oyzh.easyshell.tabs.connect;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.serial.SerialTtyConnector;
import cn.oyzh.easyshell.shell.ShellClient;
import cn.oyzh.easyshell.shell.ShellShell;
import cn.oyzh.easyshell.shell.ShellTermWidget;
import cn.oyzh.easyshell.shell.ShellTtyConnector;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.i18n.I18nHelper;
import cn.oyzh.jeditermfx.terminal.ui.FXHyperlinkFilter;
import com.jcraft.jsch.JSchException;
import com.jediterm.core.util.TermSize;
import com.jediterm.terminal.ui.FXFXTerminalPanel;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * shell终端tab内容组件
 *
 * @author oyzh
 * @since 2025/03/11
 */
public class ShellTermTabController extends SubTabController {

    /**
     * 终端组件
     */
    @FXML
    private ShellTermWidget widget;

    private void initWidget(ShellShell shell) throws IOException {
        Charset charset = this.client().getCharset();
//        ShellTtyConnector connector = (ShellTtyConnector) this.widget.createTtyConnector(charset);
//        connector.initShell(shell);
        SerialTtyConnector connector = (SerialTtyConnector) this.widget.createSerialTtyConnector(charset);
        connector.initSerial();
        this.widget.openSession(connector);
        this.widget.onTermination(exitCode -> this.widget.close());
        this.widget.addHyperlinkFilter(new FXHyperlinkFilter());
        connector.terminalSizeProperty().addListener((observable, oldValue, newValue) -> this.initShellSize());
    }

    private void initShellSize() {
        int sizeW = (int) this.widget.getWidth();
        int sizeH = (int) this.widget.getHeight();
        TermSize termSize = this.widget.getTermSize();
        ShellShell shell = this.client().openShell();
        shell.setPtySize(termSize.getColumns(), termSize.getRows(), sizeW, sizeH);
    }

    /**
     * 初始化背景
     */
    private void initBackground() {
        ShellConnect connect = this.client().getShellConnect();
        if (!connect.isEnableBackground()) {
            return;
        }
        // 背景失效
        if (connect.isBackgroundImageInvalid()) {
            MessageBox.warn(ShellI18nHelper.connectTip7());
            return;
        }
        // 处理背景
        FXFXTerminalPanel terminalPane = this.widget.getTerminalPanel();
        Node canvas = terminalPane.getFirstChild();
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
        terminalPane.setBackground(background);
    }

    public void init() throws IOException, JSchException {
        ShellClient client = this.client();
        ShellShell shell = client.openShell();
        this.initWidget(shell);
        shell.connect(client.connectTimeout());
        if (!shell.isConnected()) {
            MessageBox.warn(I18nHelper.connectFail());
            this.closeTab();
            return;
        }
        // 异步加载背景
        ThreadUtil.startVirtual(this::initBackground);
    }

    @Override
    public void onTabClosed(Event event) {
        super.onTabClosed(event);
        this.widget.close();
    }

    @Override
    public ShellConnectTabController parent() {
        return (ShellConnectTabController) super.parent();
    }

    public ShellClient client() {
        return this.parent().getClient();
    }
}
