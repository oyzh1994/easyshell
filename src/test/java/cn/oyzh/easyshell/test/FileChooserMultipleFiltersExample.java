package cn.oyzh.easyshell.test;

import javafx.application.Application;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.util.List;

public class FileChooserMultipleFiltersExample extends Application {

    @Override
    public void start(Stage primaryStage) {
        // 创建一个FileChooser对象
        FileChooser fileChooser = new FileChooser();

        // 设置文件选择对话框的标题
        fileChooser.setTitle("选择图片文件");


        // 创建一个PNG文件过滤器
        FileChooser.ExtensionFilter pngFilter = new FileChooser.ExtensionFilter("PNG图片", "*.jpeg",",*.png");
//        // 创建一个JPEG文件过滤器，注意这里构造函数的参数顺序，先描述后扩展名列表
        FileChooser.ExtensionFilter jpegFilter = new FileChooser.ExtensionFilter("JPEG图片", "*.jpg", "*.jpeg");

        // 将文件过滤器添加到FileChooser中
        fileChooser.getExtensionFilters().add(pngFilter );
//        fileChooser.getExtensionFilters().add(jpegFilter );
//        fileChooser.setSelectedExtensionFilter(jpegFilter);

        // 显示文件选择对话框并获取用户选择的文件
        List<File> selectedFile = fileChooser.showOpenMultipleDialog(primaryStage);

        if (selectedFile != null) {
            // 处理用户选择的文件
            System.out.println("选择的文件: " + selectedFile.getFirst().getAbsolutePath());
        } else {
            System.out.println("未选择文件");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static class FileChooserMultipleFiltersExampleApp{

        public static void main(String[] args) {
            System.setProperty("javafx.embed.singleThread", "true");

            FileChooserMultipleFiltersExample.main(args);
        }
    }
}