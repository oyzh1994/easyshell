package cn.oyzh.easyshell.test.rdp;


import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.rdp.ShellRDPClient;
import cn.oyzh.fx.plus.controls.pane.FXScrollPane;
import cn.oyzh.fx.plus.ext.FXApplication;
import cn.oyzh.fx.rdp.RdpView;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URISyntaxException;

/**
 * RDP 远程桌面连接的测试
 *
 * @author oyzh
 * @since 2025-09-12
 */
public class RdpTest1 extends FXApplication {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        ShellConnect connect = new ShellConnect();
        connect.setUser("");
        connect.setPassword("");
        connect.setHost("192.168.2.31:3389");
        connect.setSSLMode(true);
        connect.putExtra("method", 0);
        connect.putExtra("remoteAudio", true);
        connect.putExtra("redirectClipboard", true);
        ShellRDPClient client = new ShellRDPClient(connect);
        RdpView rdpView = new RdpView();
        rdpView.setFlexWidth("100%");
        rdpView.setFlexHeight("100%");
        client.initRdpView(rdpView);
        FXScrollPane scrollPane = new FXScrollPane(rdpView);
        scrollPane.setCenterShape(false);
        scrollPane.setFlexWidth("100%");
        scrollPane.setFlexHeight("100%");
        Scene scene = new Scene(scrollPane);
        primaryStage.setWidth(1200);
        primaryStage.setHeight(800);
        primaryStage.setScene(scene);
        primaryStage.setTitle("RDP Demo");
        primaryStage.show();
        try {
            client.start();
            rdpView.setScaleToFit(true);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

    }

    public static class RdpTest1Starter {

        public static void main(String[] args) throws URISyntaxException {
            RdpTest1.main(args);
        }
    }
}
