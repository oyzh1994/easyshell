package cn.oyzh.easyshell.test;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.FileInputStream;

public class BackgroundImgTest extends Application {

    static void main(String[] args) {
        launch(BackgroundImgTest.class, args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        VBox vBox = new VBox();
        FileInputStream fis = new FileInputStream("C:/Users/Administrator/Downloads/ssh1.png");
        Image image = new Image(fis);
        BackgroundImage backgroundImage = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
        Background background = new Background(backgroundImage);
        vBox.setBackground(background);
        Tab tab1=  new Tab("测试1");
        Tab tab2=  new Tab("测试2");

        tab1.setContent(new Label("内容"));
        tab2.setContent(new Label("内容"));

        TabPane tabPane = new TabPane();
        tabPane.getTabs().add(tab1);
        tabPane.getTabs().add(tab2);
        vBox.getChildren().add(tabPane);

        Scene scene = new Scene(vBox);
        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.setWidth(800);
        primaryStage.setHeight(600);
    }

    public static class BackgroundImgTestStarter {

        static void main(String[] args) {
            BackgroundImgTest.main(args);
        }
    }
}
