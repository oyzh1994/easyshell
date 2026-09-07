package cn.oyzh.easyshell.test.projectcover;

import cn.oyzh.fx.plus.util.FXCoverChecker;
import cn.oyzh.fx.plus.window.StageManager;
import javafx.application.Application;
import javafx.stage.Stage;

import java.net.URISyntaxException;

public class EasyShellCoverStarter extends Application {

    public static void main(String[] args) throws URISyntaxException {
        launch(EasyShellCoverStarter.class, args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        StageManager.setPrimaryStage(primaryStage);
        FXCoverChecker coverChecker = new FXCoverChecker();
        coverChecker.setProjectPath(EasyShellCoverStarter.class.getResource("").toExternalForm());
        coverChecker.tabCheck();
        coverChecker.viewCheck();
        coverChecker.popupCheck();
        StageManager.exit();
    }
}
