package cn.oyzh.easyshell.test;

import cn.oyzh.easyshell.fx.svg.glyph.redis.StringSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.FXSVGPath;
import cn.oyzh.fx.plus.controls.svg.SVGManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

import java.net.URISyntaxException;

/**
 *
 * @author oyzh
 * @since 2026-02-26
 */
public class SvgTest extends Application {


    public static void main(String[] args) throws URISyntaxException {
        launch(SvgTest.class, args);
    }


    @Override
    public void start(Stage primaryStage) throws Exception {

        VBox vBox = new VBox();
        // 直接使用 path 的 d 字符串，注意不要包含 <svg> 标签
        SVGPath stringIcon = new SVGPath();
        stringIcon.setContent("M4 7V5h16v2M12 5v14M9 19h6");

        // 可选：设置缩放（因为原始坐标是 24x24，可以根据需要放大）
        stringIcon.setScaleX(4);
        stringIcon.setScaleY(4);

        // 设置颜色和描边
        stringIcon.setStroke(Color.web("#cdd6f4"));
        stringIcon.setStrokeWidth(1.8);
        stringIcon.setFill(Color.TRANSPARENT);

        StackPane stackPane = new StackPane();
        stackPane.getChildren().setAll(new StringSVGGlyph());

        StackPane stackPane2 = new StackPane();
        stackPane2.getChildren().setAll(stringIcon);

        StackPane stackPane3 = new StackPane();
        FXSVGPath svgPath = (FXSVGPath) new StringSVGGlyph().getFirstChild();
        stackPane3.getChildren().setAll(svgPath);

        StackPane stackPane4 = new StackPane();
        SVGPath svgPath4 = new SVGPath();
        svgPath4.setContent(stringIcon.getContent());
        svgPath4.setScaleX(2);
        svgPath4.setScaleY(2);
        svgPath4.setStroke(Color.web("#cdd6f4"));
        svgPath4.setStrokeWidth(1.8);
        svgPath4.setFill(Color.TRANSPARENT);
        stackPane4.getChildren().setAll(svgPath4);

        StackPane stackPane5= new StackPane();
        SVGPath stringIcon1 = SVGManager.load("/font/redis/string.svg");
        //stringIcon1.setStroke(ThemeManager.currentForegroundColor());
        stringIcon1.setStrokeWidth(1.8);
        stringIcon1.setFill(Color.TRANSPARENT);
        stackPane5.getChildren().setAll(stringIcon1);

        vBox.getChildren().add(new StringSVGGlyph());
        //vBox.getChildren().add(stackPane);
        //vBox.getChildren().add(stackPane2);
        //vBox.getChildren().add(stackPane3);
        //vBox.getChildren().add(stackPane4);
        //vBox.getChildren().add(stackPane5);
        Scene scene = new Scene(vBox, 200, 200);
        primaryStage.setTitle("Redis String Icon");
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    public static class SvgTestStart {

        public static void main(String[] args) throws URISyntaxException {
            SvgTest.main(args);
        }

    }
}
