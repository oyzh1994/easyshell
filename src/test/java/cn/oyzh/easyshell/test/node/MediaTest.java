package cn.oyzh.easyshell.test.node;

import cn.oyzh.common.object.ObjectWatcherManager;
import cn.oyzh.fx.plus.controls.media.FXMediaView;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.theme.Themes;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;


/**
 * @author oyzh
 * @since 2022/5/18
 */
public class MediaTest extends Application {

    public static void main(String[] args) {
        launch(MediaTest.class, args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        ThemeManager.apply(Themes.PRIMER_LIGHT);
        test1(stage);
        stage.getScene().getStylesheets().add("/fx-plus/css/fx-base.css");
        stage.setTitle("code area");
    }

    private void test1(Stage stage) {

        Button button = new Button("新页面");
        button.setOnAction(event -> {
            Stage stage1 = new Stage();
            FXMediaView mediaView = new FXMediaView();
            Scene scene1 = new Scene(new HBox(mediaView));
            stage1.setScene(scene1);
            stage1.setWidth(800);
            stage1.setHeight(600);
            stage1.show();

            stage1.setOnHidden(event1 -> {
//                if (mediaView.getMediaPlayer() != null) {
//                    mediaView.getMediaPlayer().dispose();
//                }
                System.out.println("close----");
            });
            ObjectWatcherManager.watch(stage1);
        });

        Scene scene = new Scene(button);
        stage.setScene(scene);
        stage.setWidth(300);
        stage.setHeight(200);
        stage.show();
    }

    public static class MediaTestStarter {

        public static void main(String[] args) {
            MediaTest.main(args);
        }

    }

}
