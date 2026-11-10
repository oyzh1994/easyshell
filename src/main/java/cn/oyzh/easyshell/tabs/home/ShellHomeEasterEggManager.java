package cn.oyzh.easyshell.tabs.home;

import cn.oyzh.fx.plus.theme.ThemeManager;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.Effect;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Home page logo Easter eggs.
 *
 * @author oyzh
 * @since 2026-11-10
 */
public final class ShellHomeEasterEggManager {

    private static final long CLICK_WINDOW_MILLIS = 1_800;

    private static final int REQUIRED_CLICKS = 2;

    private static final int EGG_COUNT = 10;

    private static final Color[] CONFETTI_COLORS = {
            Color.web("#FF5A5F"),
            Color.web("#FFB400"),
            Color.web("#00C2A8"),
            Color.web("#4D96FF"),
            Color.web("#9B5DE5"),
            Color.web("#F15BB5")
    };

    private final ImageView logo;

    private final Pane layer;

    private final Random random = new Random();

    private int clickCount;

    private long lastClickTime;

    private boolean playing;

    private int lastEgg = -1;

    public ShellHomeEasterEggManager(ImageView logo, Pane layer) {
        this.logo = logo;
        this.layer = layer;
    }

    /**
     * Handles a logo click and triggers a random egg after enough clicks.
     */
    public void click(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - this.lastClickTime > CLICK_WINDOW_MILLIS) {
            this.clickCount = 0;
        }
        this.lastClickTime = now;
        this.clickCount++;
        if (this.clickCount < REQUIRED_CLICKS) {
            return;
        }
        this.clickCount = 0;
        if (this.playing) {
            return;
        }
        this.playing = true;
        int egg;
        do {
            egg = this.random.nextInt(EGG_COUNT);
        } while (egg == this.lastEgg);
        this.lastEgg = egg;
        switch (egg) {
            case 0 -> this.playBounce();
            case 1 -> this.playSpin();
            case 2 -> this.playRainbow();
            case 3 -> this.playConfetti();
            case 4 -> this.playOrbit();
            case 5 -> this.playPulse();
            case 6 -> this.playFlip();
            case 7 -> this.playGlitch();
            case 8 -> this.playStarRain();
            default -> this.playVortex();
        }
    }

    private void playBounce() {
        TranslateTransition jump = new TranslateTransition(Duration.millis(150), this.logo);
        jump.setByY(-30);
        jump.setAutoReverse(true);
        jump.setCycleCount(4);
        jump.setInterpolator(Interpolator.EASE_OUT);

        ScaleTransition squash = new ScaleTransition(Duration.millis(150), this.logo);
        squash.setToX(1.08);
        squash.setToY(0.92);
        squash.setAutoReverse(true);
        squash.setCycleCount(4);

        RotateTransition tilt = new RotateTransition(Duration.millis(300), this.logo);
        tilt.setFromAngle(0);
        tilt.setToAngle(8);
        tilt.setAutoReverse(true);
        tilt.setCycleCount(2);

        this.playAndReset(new ParallelTransition(jump, squash, tilt));
    }

    private void playSpin() {
        RotateTransition spin = new RotateTransition(Duration.millis(1_050), this.logo);
        spin.setFromAngle(0);
        spin.setToAngle(720);
        spin.setInterpolator(Interpolator.EASE_BOTH);

        ScaleTransition breathe = new ScaleTransition(Duration.millis(260), this.logo);
        breathe.setToX(1.12);
        breathe.setToY(1.12);
        breathe.setAutoReverse(true);
        breathe.setCycleCount(4);

        this.playAndReset(new ParallelTransition(spin, breathe));
    }

    private void playRainbow() {
        Effect previousEffect = this.logo.getEffect();
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setHue(0);
        this.logo.setEffect(colorAdjust);

        Timeline rainbow = new Timeline();
        for (int i = 0; i <= 14; i++) {
            double hue = -1 + (2D * i / 14D);
            rainbow.getKeyFrames().add(new javafx.animation.KeyFrame(
                    Duration.millis(i * 90),
                    new javafx.animation.KeyValue(colorAdjust.hueProperty(), hue, Interpolator.LINEAR)));
        }

        ScaleTransition pulse = new ScaleTransition(Duration.millis(220), this.logo);
        pulse.setToX(1.09);
        pulse.setToY(1.09);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(6);

        ParallelTransition transition = new ParallelTransition(rainbow, pulse);
        transition.setOnFinished(event -> {
            this.logo.setEffect(previousEffect);
            this.resetLogo();
            this.playing = false;
        });
        transition.play();
    }

    private void playConfetti() {
        Bounds bounds = this.logo.getBoundsInParent();
        double centerX = bounds.getMinX() + bounds.getWidth() / 2;
        double centerY = bounds.getMinY() + bounds.getHeight() / 2;
        List<Circle> confetti = new ArrayList<>();
        ParallelTransition burst = new ParallelTransition();

        for (int i = 0; i < 32; i++) {
            Circle piece = new Circle(centerX, centerY, 3 + this.random.nextDouble() * 4,
                    CONFETTI_COLORS[this.random.nextInt(CONFETTI_COLORS.length)]);
            piece.setManaged(false);
            piece.setMouseTransparent(true);
            this.layer.getChildren().add(piece);
            confetti.add(piece);

            double angle = this.random.nextDouble() * Math.PI * 2;
            double distance = 80 + this.random.nextDouble() * 130;
            TranslateTransition fly = new TranslateTransition(
                    Duration.millis(700 + this.random.nextInt(500)), piece);
            fly.setByX(Math.cos(angle) * distance);
            fly.setByY(Math.sin(angle) * distance + 80);
            fly.setInterpolator(Interpolator.EASE_OUT);

            FadeTransition fade = new FadeTransition(fly.getDuration(), piece);
            fade.setToValue(0);
            burst.getChildren().add(new ParallelTransition(fly, fade));
        }

        ScaleTransition pop = new ScaleTransition(Duration.millis(160), this.logo);
        pop.setToX(1.18);
        pop.setToY(1.18);
        pop.setAutoReverse(true);
        pop.setCycleCount(2);
        burst.getChildren().add(pop);

        burst.setOnFinished(event -> {
            this.layer.getChildren().removeAll(confetti);
            this.resetLogo();
            this.playing = false;
        });
        burst.play();
    }

    private void playOrbit() {
        Bounds bounds = this.logo.getBoundsInParent();
        double centerX = bounds.getMinX() + bounds.getWidth() / 2;
        double centerY = bounds.getMinY() + bounds.getHeight() / 2;
        Group orbit = new Group();
        orbit.setManaged(false);
        orbit.setMouseTransparent(true);
        orbit.setLayoutX(centerX);
        orbit.setLayoutY(centerY);
        String accent = this.toWebColor(ThemeManager.currentAccentColor());

        for (int i = 0; i < 10; i++) {
            double angle = Math.PI * 2 * i / 10;
            Label star = new Label("✦");
            star.setStyle("-fx-text-fill: " + accent + "; -fx-font-size: "
                    + (10 + this.random.nextInt(7)) + "px; -fx-font-weight: bold;");
            star.setLayoutX(Math.cos(angle) * 126);
            star.setLayoutY(Math.sin(angle) * 126);
            orbit.getChildren().add(star);
        }
        this.layer.getChildren().add(orbit);

        RotateTransition rotate = new RotateTransition(Duration.millis(1_350), orbit);
        rotate.setFromAngle(0);
        rotate.setToAngle(360);
        rotate.setInterpolator(Interpolator.LINEAR);

        FadeTransition fade = new FadeTransition(Duration.millis(1_350), orbit);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setAutoReverse(true);
        fade.setCycleCount(2);

        ScaleTransition logoPulse = new ScaleTransition(Duration.millis(220), this.logo);
        logoPulse.setToX(1.08);
        logoPulse.setToY(1.08);
        logoPulse.setAutoReverse(true);
        logoPulse.setCycleCount(6);

        ParallelTransition transition = new ParallelTransition(rotate, fade, logoPulse);
        transition.setOnFinished(event -> {
            this.layer.getChildren().remove(orbit);
            this.resetLogo();
            this.playing = false;
        });
        transition.play();
    }

    private void playPulse() {
        Bounds bounds = this.logo.getBoundsInParent();
        double centerX = bounds.getMinX() + bounds.getWidth() / 2;
        double centerY = bounds.getMinY() + bounds.getHeight() / 2;
        Color accent = ThemeManager.currentAccentColor();
        List<Circle> rings = new ArrayList<>();
        ParallelTransition pulse = new ParallelTransition();

        for (int i = 0; i < 3; i++) {
            Circle ring = new Circle(centerX, centerY, 46);
            ring.setManaged(false);
            ring.setMouseTransparent(true);
            ring.setFill(Color.TRANSPARENT);
            ring.setStroke(accent);
            ring.setStrokeWidth(2.5);
            ring.setOpacity(0.9);
            this.layer.getChildren().add(ring);
            rings.add(ring);

            ScaleTransition expand = new ScaleTransition(Duration.millis(850), ring);
            expand.setToX(2.8);
            expand.setToY(2.8);
            expand.setDelay(Duration.millis(i * 130));
            expand.setInterpolator(Interpolator.EASE_OUT);

            FadeTransition fade = new FadeTransition(expand.getDuration(), ring);
            fade.setToValue(0);
            fade.setDelay(expand.getDelay());
            pulse.getChildren().add(new ParallelTransition(expand, fade));
        }

        ScaleTransition logoPulse = new ScaleTransition(Duration.millis(180), this.logo);
        logoPulse.setToX(1.12);
        logoPulse.setToY(1.12);
        logoPulse.setAutoReverse(true);
        logoPulse.setCycleCount(4);
        pulse.getChildren().add(logoPulse);

        pulse.setOnFinished(event -> {
            this.layer.getChildren().removeAll(rings);
            this.resetLogo();
            this.playing = false;
        });
        pulse.play();
    }

    private void playFlip() {
        Point3D previousAxis = this.logo.getRotationAxis();
        this.logo.setRotationAxis(Rotate.Y_AXIS);

        RotateTransition flip = new RotateTransition(Duration.millis(900), this.logo);
        flip.setFromAngle(0);
        flip.setToAngle(360);
        flip.setInterpolator(Interpolator.EASE_BOTH);

        ScaleTransition lift = new ScaleTransition(Duration.millis(200), this.logo);
        lift.setToX(1.1);
        lift.setToY(1.1);
        lift.setAutoReverse(true);
        lift.setCycleCount(4);

        ParallelTransition transition = new ParallelTransition(flip, lift);
        transition.setOnFinished(event -> {
            this.logo.setRotationAxis(previousAxis);
            this.resetLogo();
            this.playing = false;
        });
        transition.play();
    }

    private void playGlitch() {
        ImageView cyan = this.createGhost(-0.45);
        ImageView magenta = this.createGhost(0.48);
        this.layer.getChildren().addAll(cyan, magenta);

        Timeline glitch = new Timeline();
        for (int i = 0; i <= 9; i++) {
            double offset = 3 + this.random.nextDouble() * 8;
            double vertical = this.random.nextDouble() * 4;
            double opacity = i % 2 == 0 ? 0.45 : 1;
            glitch.getKeyFrames().add(new javafx.animation.KeyFrame(
                    Duration.millis(i * 75),
                    new javafx.animation.KeyValue(cyan.translateXProperty(), offset),
                    new javafx.animation.KeyValue(cyan.translateYProperty(), vertical),
                    new javafx.animation.KeyValue(magenta.translateXProperty(), -offset),
                    new javafx.animation.KeyValue(magenta.translateYProperty(), -vertical),
                    new javafx.animation.KeyValue(this.logo.translateXProperty(), (this.random.nextDouble() - 0.5) * 4),
                    new javafx.animation.KeyValue(this.logo.opacityProperty(), opacity)));
        }
        glitch.setOnFinished(event -> {
            this.layer.getChildren().removeAll(cyan, magenta);
            this.resetLogo();
            this.playing = false;
        });
        glitch.play();
    }

    private ImageView createGhost(double hue) {
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setHue(hue);
        colorAdjust.setSaturation(1);
        ImageView ghost = new ImageView(this.logo.getImage());
        ghost.setManaged(false);
        ghost.setMouseTransparent(true);
        ghost.setFitWidth(this.logo.getFitWidth());
        ghost.setFitHeight(this.logo.getFitHeight());
        ghost.setPreserveRatio(this.logo.isPreserveRatio());
        ghost.setSmooth(this.logo.isSmooth());
        ghost.setLayoutX(this.logo.getLayoutX());
        ghost.setLayoutY(this.logo.getLayoutY());
        ghost.setOpacity(0.32);
        ghost.setEffect(colorAdjust);
        return ghost;
    }

    private void playStarRain() {
        Bounds bounds = this.logo.getBoundsInParent();
        String accent = this.toWebColor(ThemeManager.currentAccentColor());
        String foreground = this.toWebColor(ThemeManager.currentForegroundColor());
        String[] symbols = {"✦", "01", "+", "×", "•", "◇", "∞"};
        List<Label> stars = new ArrayList<>();
        ParallelTransition rain = new ParallelTransition();

        for (int i = 0; i < 28; i++) {
            Label star = new Label(symbols[this.random.nextInt(symbols.length)]);
            star.setManaged(false);
            star.setMouseTransparent(true);
            star.setOpacity(0.85);
            star.setStyle("-fx-text-fill: " + (i % 3 == 0 ? foreground : accent)
                    + "; -fx-font-size: " + (9 + this.random.nextInt(8))
                    + "px; -fx-font-weight: bold;");
            star.setLayoutX(bounds.getMinX() + this.random.nextDouble() * bounds.getWidth());
            star.setLayoutY(bounds.getMinY() + this.random.nextDouble() * bounds.getHeight() * 0.55);
            this.layer.getChildren().add(star);
            stars.add(star);

            TranslateTransition fall = new TranslateTransition(
                    Duration.millis(700 + this.random.nextInt(650)), star);
            fall.setByY(70 + this.random.nextDouble() * 110);
            fall.setByX((this.random.nextDouble() - 0.5) * 28);
            fall.setInterpolator(Interpolator.EASE_IN);

            FadeTransition fade = new FadeTransition(fall.getDuration(), star);
            fade.setToValue(0);
            rain.getChildren().add(new ParallelTransition(fall, fade));
        }

        ScaleTransition logoPulse = new ScaleTransition(Duration.millis(240), this.logo);
        logoPulse.setToX(1.07);
        logoPulse.setToY(1.07);
        logoPulse.setAutoReverse(true);
        logoPulse.setCycleCount(5);
        rain.getChildren().add(logoPulse);

        rain.setOnFinished(event -> {
            this.layer.getChildren().removeAll(stars);
            this.resetLogo();
            this.playing = false;
        });
        rain.play();
    }

    private void playVortex() {
        Bounds bounds = this.logo.getBoundsInParent();
        double centerX = bounds.getMinX() + bounds.getWidth() / 2;
        double centerY = bounds.getMinY() + bounds.getHeight() / 2;
        Color accent = ThemeManager.currentAccentColor();
        Group vortex = new Group();
        vortex.setManaged(false);
        vortex.setMouseTransparent(true);
        vortex.setLayoutX(centerX);
        vortex.setLayoutY(centerY);
        ParallelTransition pull = new ParallelTransition();

        for (int i = 0; i < 30; i++) {
            double angle = this.random.nextDouble() * Math.PI * 2;
            double radius = 150 + this.random.nextDouble() * 95;
            Circle particle = new Circle(Math.cos(angle) * radius, Math.sin(angle) * radius,
                    2 + this.random.nextDouble() * 4,
                    i % 4 == 0 ? CONFETTI_COLORS[this.random.nextInt(CONFETTI_COLORS.length)] : accent);
            particle.setOpacity(0.9);
            vortex.getChildren().add(particle);

            TranslateTransition inward = new TranslateTransition(
                    Duration.millis(750 + this.random.nextInt(450)), particle);
            inward.setByX(-Math.cos(angle) * radius * 0.88);
            inward.setByY(-Math.sin(angle) * radius * 0.88);
            inward.setInterpolator(Interpolator.EASE_IN);

            ScaleTransition shrink = new ScaleTransition(inward.getDuration(), particle);
            shrink.setToX(0.1);
            shrink.setToY(0.1);

            FadeTransition fade = new FadeTransition(inward.getDuration(), particle);
            fade.setToValue(0);
            pull.getChildren().add(new ParallelTransition(inward, shrink, fade));
        }
        this.layer.getChildren().add(vortex);

        RotateTransition swirl = new RotateTransition(Duration.millis(1_150), vortex);
        swirl.setFromAngle(0);
        swirl.setToAngle(260);
        swirl.setInterpolator(Interpolator.EASE_IN);

        RotateTransition logoSpin = new RotateTransition(Duration.millis(1_150), this.logo);
        logoSpin.setFromAngle(0);
        logoSpin.setToAngle(-24);
        logoSpin.setAutoReverse(true);
        logoSpin.setCycleCount(2);

        ScaleTransition logoGrow = new ScaleTransition(Duration.millis(260), this.logo);
        logoGrow.setToX(1.14);
        logoGrow.setToY(1.14);
        logoGrow.setAutoReverse(true);
        logoGrow.setCycleCount(4);

        pull.getChildren().addAll(swirl, logoSpin, logoGrow);
        pull.setOnFinished(event -> {
            this.layer.getChildren().remove(vortex);
            this.resetLogo();
            this.playing = false;
        });
        pull.play();
    }

    private void playAndReset(Animation animation) {
        animation.setOnFinished(event -> {
            this.resetLogo();
            this.playing = false;
        });
        animation.play();
    }

    private void resetLogo() {
        this.logo.setTranslateX(0);
        this.logo.setTranslateY(0);
        this.logo.setRotate(0);
        this.logo.setScaleX(1);
        this.logo.setScaleY(1);
        this.logo.setOpacity(1);
    }

    private String toWebColor(Color color) {
        Color value = color == null ? Color.web("#4D96FF") : color;
        return String.format("#%02X%02X%02X",
                Math.round(value.getRed() * 255),
                Math.round(value.getGreen() * 255),
                Math.round(value.getBlue() * 255));
    }
}
