package cn.oyzh.easyshell.tabs.vnc;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.easyshell.vnc.ShellVNCClient;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import org.jfxvnc.ui.control.VncImageView;
import org.jfxvnc.ui.service.VncRenderService;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * vnc组件
 *
 * @author oyzh
 * @since 2025/05/23
 */
public class ShellVNCTabController extends RichTabController   {

    @FXML
    private ScrollPane root;

    /**
     * 上传/下载管理
     */
    @FXML
    private VncImageView vncView;

   private VncRenderService renderService;


    /**
     * ftp客户端
     */
    private ShellVNCClient client;

    /**
     * 设置
     */
    private final ShellSetting setting = ShellSettingStore.SETTING;

    /**
     * 设置储存
     */
    private final ShellSettingStore settingStore = ShellSettingStore.INSTANCE;


    public ShellVNCClient client() {
        return this.client;
    }

    public ShellConnect shellConnect() {
        return this.client.getShellConnect();
    }

    /**
     * 初始化
     */
    public void init(ShellConnect shellConnect) {
        this.client = new ShellVNCClient(shellConnect);
        this.vncView.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> {

        });
        this.initRenderService();
        this.client.setRenderProtocol(this.renderService);
        StageManager.showMask(() -> {
            try {
                if (!this.client.isConnected()) {
                    this.client.start();
                }
                if (!this.client.isConnected()) {
                    MessageBox.warn(I18nHelper.connectFail());
                    this.closeTab();
                    return;
                }
                // 收起左侧
                if (this.setting.isHiddenLeftAfterConnected()) {
                    ShellEventUtil.layout1();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
                this.closeTab();
            }
        });
    }

    private void initRenderService() {
        this.renderService = new VncRenderService();

        this.renderService.setEventConsumer(this.vncView);
        this.renderService.serverCutTextProperty().addListener((l, old, text) -> this.vncView.addClipboardText(text));

        this.renderService.onlineProperty().addListener((l, old, online) -> Platform.runLater(() -> {
            this.vncView.setDisable(!online);
        }));

        this.renderService.inputEventListenerProperty().addListener(l -> this.vncView.registerInputEventListener(this.renderService.inputEventListenerProperty().get()));
        this.renderService.getConfiguration().clientCursorProperty().addListener((l, a, b) -> this.vncView.setUseClientCursor(b));
        this.vncView.setOnZoom(e -> this.renderService.zoomLevelProperty().set(e.getTotalZoomFactor()));

        this.renderService.zoomLevelProperty().addListener((l, old, zoom) ->  this.vncView.zoomLevelProperty().set(zoom.doubleValue()));

        this.root.setOnScroll(e -> this.renderService.zoomLevelProperty().set(this.renderService.zoomLevelProperty().get() + (e.getDeltaY() > 0.0 ? 0.01 : -0.01)));
    }

    @Override
    public void onTabClosed(Event event) {
        super.onTabClosed(event);
        this.client.close();
        // 展开左侧
        if (this.setting.isHiddenLeftAfterConnected()) {
            ShellEventUtil.layout2();
        }
    }

//    @Override
//    public void render(ImageRect imageRect, RenderCallback renderCallback) {
//        this.vncView.accept(null, imageRect);
//        renderCallback.renderComplete();
//    }
//
//    @Override
//    public void eventReceived(ServerDecoderEvent event) {
//        this.vncView.accept(event, null);
////        if (event instanceof ConnectInfoEvent) {
////            this.vncView.accept(event, null);
////            return;
////        }
//        if (event instanceof ServerCutTextEvent cutTextEvent) {
//            this.vncView.addClipboardText(cutTextEvent.getText());
//            return;
//        }
//    }
//
//    @Override
//    public void exceptionCaught(Throwable throwable) {
//
//    }
//
//    @Override
//    public void stateChanged(ProtocolState protocolState) {
//
//    }
//
//    @Override
//    public void registerInputEventListener(InputEventListener inputEventListener) {
//
//
//    }

    @Override
    public void initialize(URL location, ResourceBundle resourceBundle) {
        super.initialize(location, resourceBundle);
    }
}
