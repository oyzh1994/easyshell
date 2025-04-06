package cn.oyzh.easyshell.terminal;

import cn.oyzh.common.system.OSUtil;
import cn.oyzh.common.util.ReflectUtil;
import cn.oyzh.fx.plus.theme.ThemeManager;
import com.techsenger.jeditermfx.core.Color;
import com.techsenger.jeditermfx.core.TerminalColor;
import com.techsenger.jeditermfx.core.emulator.ColorPalette;
import com.techsenger.jeditermfx.core.emulator.ColorPaletteImpl;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

/**
 * @author oyzh
 * @since 2025-03-26
 */
public class ShellColorPalette extends ColorPalette {

    public static final ShellColorPalette INSTANCE = new ShellColorPalette();

//    @Override
//    public @NotNull Color getForeground(@NotNull TerminalColor color) {
//        return this.getForegroundByColorIndex(-1);
//    }
//
//    @Override
//    protected @NotNull Color getForegroundByColorIndex(int colorIndex) {
//        javafx.scene.paint.Color color = ThemeManager.currentForegroundColor();
//        int red = (int) (color.getRed() * 255);
//        int green = (int) (color.getGreen() * 255);
//        int blue = (int) (color.getBlue() * 255);
//        int opacity = (int) (color.getOpacity() * 255);
//        return new Color(red, green, blue, opacity);
//    }

    @Override
    protected @NotNull Color getForegroundByColorIndex(int colorIndex) {
        Color color;
        //TODO: 如果是暗黑模式，需要修正部分颜色
        if (ThemeManager.isDarkMode()) {
            if (colorIndex == 0) {
                colorIndex = 7;
            } else if (colorIndex == 8) {
                colorIndex = 15;
            } else if (colorIndex == 7) {
                colorIndex = 0;
            } else if (colorIndex == 15) {
                colorIndex = 8;
            } else if (colorIndex == 4) {
                colorIndex = 2;
            } else if (colorIndex == 12) {
                colorIndex = 10;
            }
        }
        Method method = ReflectUtil.getMethod(ColorPalette.class, "getForegroundByColorIndex", int.class);
        if (OSUtil.isWindows()) {
            color = (Color) ReflectUtil.invoke(ColorPaletteImpl.WINDOWS_PALETTE, method, colorIndex);
        } else {
            color = (Color) ReflectUtil.invoke(ColorPaletteImpl.XTERM_PALETTE, method, colorIndex);
        }
        return color;
    }

    @Override
    public @NotNull Color getBackground(@NotNull TerminalColor color) {
        return this.getBackgroundByColorIndex(-1);
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
