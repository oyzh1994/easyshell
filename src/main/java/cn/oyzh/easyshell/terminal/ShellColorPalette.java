package cn.oyzh.easyshell.terminal;

import cn.oyzh.fx.plus.theme.ThemeManager;
import com.techsenger.jeditermfx.core.Color;
import com.techsenger.jeditermfx.core.emulator.ColorPalette;
import org.jetbrains.annotations.NotNull;

/**
 * @author oyzh
 * @since 2025-03-26
 */
public class ShellColorPalette extends ColorPalette {

    public static final ShellColorPalette INSTANCE = new ShellColorPalette();

    @Override
    protected @NotNull Color getForegroundByColorIndex(int colorIndex) {
        javafx.scene.paint.Color color = ThemeManager.currentForegroundColor();
        int red = (int) (color.getRed() * 255);
        int green = (int) (color.getGreen() * 255);
        int blue = (int) (color.getBlue() * 255);
        int opacity = (int) (color.getOpacity() * 255);
        return new Color(red, green, blue, opacity);
    }

    @Override
    protected @NotNull Color getBackgroundByColorIndex(int colorIndex) {
        javafx.scene.paint.Color color = ThemeManager.currentBackgroundColor();
        int red = (int) (color.getRed() * 255);
        int green = (int) (color.getGreen() * 255);
        int blue = (int) (color.getBlue() * 255);
        int opacity = (int) (color.getOpacity() * 255);
        return new Color(red, green, blue, opacity);
    }
}
