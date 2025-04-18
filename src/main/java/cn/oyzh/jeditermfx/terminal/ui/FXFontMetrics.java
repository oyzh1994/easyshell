package cn.oyzh.jeditermfx.terminal.ui;

import cn.oyzh.common.log.JulLog;
import javafx.scene.text.Font;
import javafx.scene.text.Text;


public class FXFontMetrics {

    public static FXFontMetrics create(Font font, String str) {
        var text = new Text(str);
        text.setFont(font);
        text.applyCss();//TODO???
        var width = text.getLayoutBounds().getWidth();
        var height = text.getLayoutBounds().getHeight();
        var descent = text.getLayoutBounds().getHeight() - text.getBaselineOffset();
        var metrics = new FXFontMetrics(width, height, descent);
        JulLog.trace("Created metrics: {} for {}", metrics, font);
        return metrics;
    }

    private final double descent;

    private final double width;

    private final double height;

    private FXFontMetrics(double width, double height, double descent) {
        this.descent = descent;
        this.width = width;
        this.height = height;
    }

    public double getDescent() {
        return descent;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    @Override
    public String toString() {
        return "{" + "descent=" + descent + ", width=" + width + ", height=" + height + '}';
    }
}
