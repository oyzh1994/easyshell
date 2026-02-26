package cn.oyzh.easyshell.test;

import animatefx.animation.Bounce;
import animatefx.animation.BounceIn;
import animatefx.animation.BounceInDown;
import animatefx.animation.BounceInLeft;
import animatefx.animation.BounceInRight;
import animatefx.animation.BounceInUp;
import animatefx.animation.BounceOut;
import animatefx.animation.BounceOutDown;
import animatefx.animation.BounceOutLeft;
import animatefx.animation.BounceOutRight;
import animatefx.animation.BounceOutUp;
import animatefx.animation.FadeIn;
import animatefx.animation.FadeInDown;
import animatefx.animation.FadeInDownBig;
import animatefx.animation.FadeInLeft;
import animatefx.animation.FadeInLeftBig;
import animatefx.animation.FadeInRight;
import animatefx.animation.FadeInRightBig;
import animatefx.animation.FadeInUp;
import animatefx.animation.FadeInUpBig;
import animatefx.animation.FadeOut;
import animatefx.animation.FadeOutDown;
import animatefx.animation.FadeOutDownBig;
import animatefx.animation.FadeOutLeft;
import animatefx.animation.FadeOutLeftBig;
import animatefx.animation.FadeOutRight;
import animatefx.animation.FadeOutRightBig;
import animatefx.animation.FadeOutUp;
import animatefx.animation.FadeOutUpBig;
import animatefx.animation.Flash;
import animatefx.animation.Flip;
import animatefx.animation.FlipInX;
import animatefx.animation.FlipInY;
import animatefx.animation.FlipOutX;
import animatefx.animation.FlipOutY;
import animatefx.animation.GlowBackground;
import animatefx.animation.Hinge;
import animatefx.animation.JackInTheBox;
import animatefx.animation.Jello;
import animatefx.animation.LightSpeedIn;
import animatefx.animation.LightSpeedOut;
import animatefx.animation.Pulse;
import animatefx.animation.RollIn;
import animatefx.animation.RollOut;
import animatefx.animation.RotateIn;
import animatefx.animation.RotateInDownLeft;
import animatefx.animation.RotateInDownRight;
import animatefx.animation.RotateInUpLeft;
import animatefx.animation.RotateInUpRight;
import animatefx.animation.RotateOut;
import animatefx.animation.RotateOutDownLeft;
import animatefx.animation.RotateOutDownRight;
import animatefx.animation.RotateOutUpLeft;
import animatefx.animation.RotateOutUpRight;
import animatefx.animation.RubberBand;
import animatefx.animation.Shake;
import animatefx.animation.SlideInDown;
import animatefx.animation.SlideInLeft;
import animatefx.animation.SlideInRight;
import animatefx.animation.SlideInUp;
import animatefx.animation.SlideOutDown;
import animatefx.animation.SlideOutLeft;
import animatefx.animation.SlideOutRight;
import animatefx.animation.SlideOutUp;
import animatefx.animation.Swing;
import animatefx.animation.Tada;
import animatefx.animation.Wobble;
import animatefx.animation.ZoomIn;
import animatefx.animation.ZoomInDown;
import animatefx.animation.ZoomInLeft;
import animatefx.animation.ZoomInRight;
import animatefx.animation.ZoomInUp;
import animatefx.animation.ZoomOut;
import animatefx.animation.ZoomOutDown;
import animatefx.animation.ZoomOutLeft;
import animatefx.animation.ZoomOutRight;
import animatefx.animation.ZoomOutUp;
import animatefx.util.ParallelAnimationFX;
import animatefx.util.SequentialAnimationFX;
import cn.oyzh.fx.gui.svg.glyph.TestSVGGlyph;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URISyntaxException;

/**
 *
 * @author oyzh
 * @since 2026-02-26
 */
public class AnimateFXTest extends Application {


    public static void main(String[] args) throws URISyntaxException {
        launch(AnimateFXTest.class, args);
    }

//    Label text = new Label("test");
    TestSVGGlyph text = new TestSVGGlyph();

    @Override
    public void start(Stage primaryStage) throws Exception {

        VBox vBox = new VBox();


        vBox.getChildren().add(text);
        HBox hBox = new HBox();
        Button btn1 = new Button("Bounce");
        btn1.setOnAction(this::handleClick);
        hBox.getChildren().add(btn1);

        Button btn2 = new Button("Flash");
        btn2.setOnAction(this::handleClick);
        hBox.getChildren().add(btn2);

        Button btn3 = new Button("Pulse");
        btn3.setOnAction(this::handleClick);
        hBox.getChildren().add(btn3);

        Button btn4 = new Button("RubberBand");
        btn4.setOnAction(this::handleClick);
        hBox.getChildren().add(btn4);

        Button btn5 = new Button("Shake");
        btn5.setOnAction(this::handleClick);
        hBox.getChildren().add(btn5);

        Button btn6 = new Button("Swing");
        btn6.setOnAction(this::handleClick);
        hBox.getChildren().add(btn6);

        Button btn7 = new Button("Tada");
        btn7.setOnAction(this::handleClick);
        hBox.getChildren().add(btn7);

        Button btn8 = new Button("BounceOut");
        btn8.setOnAction(this::handleClick);
        hBox.getChildren().add(btn8);

        vBox.getChildren().add(hBox);

        primaryStage.setScene(new Scene(vBox));

        primaryStage.setTitle("AnimateFX Test");

        primaryStage.setWidth(600);
        primaryStage.setHeight(400);
        primaryStage.show();
    }

    public void handleClick(ActionEvent actionEvent) {

        Button button = (Button) actionEvent.getSource();
        String s = button.getText();
        switch (s) {
            /*Attention seekers**/
            case "Bounce":
                new Bounce(text).play();
                break;
            case "Flash":
                new Flash(text).play();

                break;
            case "Pulse":
                new Pulse(text).play();

                break;
            case "RubberBand":
                new RubberBand(text).play();

                break;
            case "Shake":
                new Shake(text).play();

                break;
            case "Swing":
                new Swing(text).play();

                break;
            case "Tada":
                new Tada(text).play();

                break;
            case "Wobble":
                new Wobble(text).play();

                break;
            case "Jello":
                new Jello(text).play();

                /*Bounce Entrances**/
                break;
            case "BounceIn":
                new BounceIn(text).play();

                break;
            case "BounceInDown":
                new BounceInDown(text).play();

                break;
            case "BounceInLeft":
                new BounceInLeft(text).play();

                break;
            case "BounceInRight":
                new BounceInRight(text).play();

                break;
            case "BounceInUp":
                new BounceInUp(text).play();

                /*Bouncing exits**/
                break;
            case "BounceOut":
                new BounceOut(text).setResetOnFinished(true).play();

                break;
            case "BounceOutDown":
                new BounceOutDown(text).setResetOnFinished(true).play();

                break;
            case "BounceOutLeft":
                new BounceOutLeft(text).setResetOnFinished(true).play();

                break;
            case "BounceOutRight":
                new BounceOutRight(text).setResetOnFinished(true).play();

                break;
            case "BounceOutUp":
                new BounceOutUp(text).setResetOnFinished(true).play();

                /*Fading entrances**/
                break;
            case "FadeIn":
                new FadeIn(text).play();

                break;
            case "FadeInDown":
                new FadeInDown(text).play();

                break;
            case "FadeInDownBig":
                new FadeInDownBig(text).play();

                break;
            case "FadeInLeft":
                new FadeInLeft(text).play();

                break;
            case "FadeInLeftBig":
                new FadeInLeftBig(text).play();

                break;
            case "FadeInRight":
                new FadeInRight(text).play();

                break;
            case "FadeInRightBig":
                new FadeInRightBig(text).play();

                break;
            case "FadeInUp":
                new FadeInUp(text).play();

                break;
            case "FadeInUpBig":
                new FadeInUpBig(text).play();


                /*Fading exits**/
                break;
            case "FadeOut":
                new FadeOut(text).setResetOnFinished(true).play();

                break;
            case "FadeOutDown":
                new FadeOutDown(text).setResetOnFinished(true).play();

                break;
            case "FadeOutDownBig":
                new FadeOutDownBig(text).setResetOnFinished(true).play();

                break;
            case "FadeOutLeft":
                new FadeOutLeft(text).setResetOnFinished(true).play();

                break;
            case "FadeOutLeftBig":
                new FadeOutLeftBig(text).setResetOnFinished(true).play();

                break;
            case "FadeOutRight":
                new FadeOutRight(text).setResetOnFinished(true).play();

                break;
            case "FadeOutRightBig":
                new FadeOutRightBig(text).setResetOnFinished(true).play();

                break;
            case "FadeOutUp":
                new FadeOutUp(text).setResetOnFinished(true).play();

                break;
            case "FadeOutUpBig":
                new FadeOutUpBig(text).setResetOnFinished(true).play();

                /*Flipeprs**/
                break;
            case "Flip":
                new Flip(text).play();

                break;
            case "FlipInX":
                new FlipInX(text).play();

                break;
            case "FlipInY":
                new FlipInY(text).play();

                break;
            case "FlipOutX":
                new FlipOutX(text).setResetOnFinished(true).play();

                break;
            case "FlipOutY":
                new FlipOutY(text).setResetOnFinished(true).play();


                /*LightSpeed**/
                break;
            case "LightSpeedIn":
                new LightSpeedIn(text).play();

                break;
            case "LightSpeedOut":
                new LightSpeedOut(text).setResetOnFinished(true).play();

                /*Rotating entrances**/
                break;
            case "RotateIn":
                new RotateIn(text).play();

                break;
            case "RotateInDownLeft":
                new RotateInDownLeft(text).play();

                break;
            case "RotateInDownRight":
                new RotateInDownRight(text).play();

                break;
            case "RotateInUpLeft":
                new RotateInUpLeft(text).play();

                break;
            case "RotateInUpRight":
                new RotateInUpRight(text).play();


                /*Rotating exits**/
                break;
            case "RotateOut":
                new RotateOut(text).setResetOnFinished(true).play();

                break;
            case "RotateOutDownLeft":
                new RotateOutDownLeft(text).setResetOnFinished(true).play();

                break;
            case "RotateOutDownRight":
                new RotateOutDownRight(text).setResetOnFinished(true).play();

                break;
            case "RotateOutUpLeft":
                new RotateOutUpLeft(text).setResetOnFinished(true).play();

                break;
            case "RotateOutUpRight":
                new RotateOutUpRight(text).setResetOnFinished(true).play();

                /*Sliding entrances**/
                break;
            case "SlideInUp":
                new SlideInUp(text).play();

                break;
            case "SlideInDown":
                new SlideInDown(text).play();

                break;
            case "SlideInLeft":
                new SlideInLeft(text).play();

                break;
            case "SlideInRight":
                new SlideInRight(text).play();


                /*Sliding exits**/
                break;
            case "SlideOutUp":
                new SlideOutUp(text).setResetOnFinished(true).play();

                break;
            case "SlideOutDown":
                new SlideOutDown(text).setResetOnFinished(true).play();

                break;
            case "SlideOutLeft":
                new SlideOutLeft(text).setResetOnFinished(true).play();

                break;
            case "SlideOutRight":
                new SlideOutRight(text).setResetOnFinished(true).play();


                /*Zoom entrances**/
                break;
            case "ZoomIn":
                new ZoomIn(text).play();

                break;
            case "ZoomInDown":
                new ZoomInDown(text).play();

                break;
            case "ZoomInLeft":
                new ZoomInLeft(text).play();

                break;
            case "ZoomInRight":
                new ZoomInRight(text).play();

                break;
            case "ZoomInUp":
                new ZoomInUp(text).play();


                /*Zoom exits**/
                break;
            case "ZoomOut":
                new ZoomOut(text).setResetOnFinished(true).play();

                break;
            case "ZoomOutDown":
                new ZoomOutDown(text).setResetOnFinished(true).play();

                break;
            case "ZoomOutLeft":
                new ZoomOutLeft(text).setResetOnFinished(true).play();

                break;
            case "ZoomOutRight":
                new ZoomOutRight(text).setResetOnFinished(true).play();

                break;
            case "ZoomOutUp":
                new ZoomOutUp(text).setResetOnFinished(true).play();


                /*Specials*/
                break;
            case "Hinge":
                new Hinge(text).setResetOnFinished(true).play();

                break;
            case "JackInTheBox":
                new JackInTheBox(text).play();

                break;
            case "RollIn":
                new RollIn(text).play();

                break;
            case "RollOut":
                new RollOut(text).setResetOnFinished(true).play();

                /* Colors */
                break;
//            case "TextGlow":
//                new GlowText(text, Color.ORANGE, Color.ORANGERED)
//                        .setCycleCount(3)
//                        .setSpeed(0.5)
//                        .setResetOnFinished(true)
//                        .play();
//                break;
            case "BackgroundGlow":

                new GlowBackground(text, Color.WHITE, Color.YELLOW, 20)
                        .setDelay(Duration.millis(500))
                        .setCycleCount(3)
                        .setResetOnFinished(true)
                        .play();

                break;
            case "SequentialAnimation":
                SequentialAnimationFX sequentialAnimationFX = new SequentialAnimationFX(text, new BounceIn(), new Flash());
                sequentialAnimationFX.setResetOnFinished(true);
                sequentialAnimationFX.play();
                break;
            case "ParallelAnimation":
                ParallelAnimationFX parallelAnimationFX = new ParallelAnimationFX(text, new BounceIn(), new Flash());
                parallelAnimationFX.play();
                break;
            default:
                System.err.println("No animation binded to this button");
                break;
        }

    }

    public static class AnimateFXTestSatrt {

        public static void main(String[] args) throws URISyntaxException {
            AnimateFXTest.main(args);
        }

    }
}
