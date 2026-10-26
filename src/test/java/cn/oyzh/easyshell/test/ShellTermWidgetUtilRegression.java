package cn.oyzh.easyshell.test;

import cn.oyzh.easyshell.terminal.ShellTermWidgetUtil;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * 验证终端组件可以在 JavaFX 容器中原位替换且保留流式尺寸。
 */
public class ShellTermWidgetUtilRegression extends Application {

    @Override
    public void start(Stage stage) {
        try {
            VBox root = new VBox();
            ShellTestTermWidget oldWidget = new ShellTestTermWidget();
            oldWidget.setFlexWidth("100%");
            oldWidget.setFlexHeight("100% - 30");
            root.getChildren().add(oldWidget);

            stage.setScene(new Scene(root, 640, 480));
            stage.show();

            ShellTestTermWidget newWidget = new ShellTestTermWidget();
            ShellTermWidgetUtil.replace(oldWidget, newWidget);
            require(root.getChildren().size() == 1, "old widget was not removed");
            require(root.getChildren().get(0) == newWidget, "new widget was not inserted at the original index");
            require(oldWidget.getParent() == null, "old widget is still attached");
            require("100%".equals(newWidget.getFlexWidth()), "flex width was not preserved");
            require("100% - 30".equals(newWidget.getFlexHeight()), "flex height was not preserved");

            ShellTermWidgetUtil.close(newWidget);
            System.out.println("terminal widget replacement regression passed");
            stage.close();
            System.exit(0);
        } catch (Throwable ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
