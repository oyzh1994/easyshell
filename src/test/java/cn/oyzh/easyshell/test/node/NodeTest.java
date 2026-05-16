package cn.oyzh.easyshell.test.node;

import cn.oyzh.common.object.ObjectWatcherManager;
import cn.oyzh.fx.gui.font.FontFamilyTextField;
import cn.oyzh.fx.plus.font.FontSizeComboBox;
import cn.oyzh.fx.plus.font.FontWeightComboBox;
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
public class NodeTest extends Application {

    public static void main(String[] args) {
        launch(NodeTest.class, args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        ThemeManager.apply(Themes.PRIMER_LIGHT);
        test1(stage);
        stage.getScene().getStylesheets().add("/fx-plus/css/fx-base.css");
        stage.setTitle("node test");
    }

    private void test1(Stage stage) {

        Button button = new Button("新页面");
        button.setOnAction(event -> {
            Stage stage1 = new Stage();
            FontFamilyTextField node1 = new FontFamilyTextField();
            FontFamilyTextField node2 = new FontFamilyTextField();
            FontSizeComboBox node3 = new FontSizeComboBox();
            FontSizeComboBox node4 = new FontSizeComboBox();
            FontWeightComboBox node5 = new FontWeightComboBox();
            FontWeightComboBox node6 = new FontWeightComboBox();
            //Scene scene1 = new Scene(new HBox(node1, node2, node3, node4));
            Scene scene1 = new Scene(new HBox(node1, node2, node3, node4, node5, node6));
            stage1.setScene(scene1);
            stage1.setWidth(800);
            stage1.setHeight(600);
            stage1.show();

            stage1.setOnHidden(event1 -> {
                //node1.destroy();
                //node2.destroy();
                //node3.destroy();
                //node4.destroy();
                //node5.destroy();
                //node6.destroy();
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

    public static class NodeTestStarter {

        public static void main(String[] args) {
            NodeTest.main(args);
        }

    }

}
