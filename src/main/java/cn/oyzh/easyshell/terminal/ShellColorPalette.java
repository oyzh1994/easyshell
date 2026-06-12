package cn.oyzh.easyshell.terminal;

import cn.oyzh.common.system.OSUtil;
import cn.oyzh.common.util.ReflectUtil;
import cn.oyzh.fx.plus.theme.ThemeManager;
import com.jediterm.core.Color;
import com.jediterm.terminal.emulator.ColorPalette;
import com.jediterm.terminal.emulator.ColorPaletteImpl;
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

    @Override
    protected @NotNull Color getForegroundByColorIndex(int colorIndex) {
//        javafx.scene.paint.Color color = ThemeManager.currentForegroundColor();
//        int red = (int) (color.getRed() * 255);
//        int green = (int) (color.getGreen() * 255);
//        int blue = (int) (color.getBlue() * 255);
//        int opacity = (int) (color.getOpacity() * 255);
//        return new Color(red, green, blue, opacity);
        return this.getPaletteColor(true, colorIndex);
    }

    //    @Override
//    public @NotNull Color getBackground(@NotNull TerminalColor color) {
//        return this.getBackgroundByColorIndex(-1);
//    }
//
    @Override
    protected @NotNull Color getBackgroundByColorIndex(int colorIndex) {
//        javafx.scene.paint.Color color = ThemeManager.currentBackgroundColor();
//        int red = (int) (color.getRed() * 255);
//        int green = (int) (color.getGreen() * 255);
//        int blue = (int) (color.getBlue() * 255);
//        int opacity = (int) (color.getOpacity() * 255);
//        return new Color(red, green, blue, opacity);
        return this.getPaletteColor(false, colorIndex);
    }

    /**
     * 缓存的前景色
     */
    private Color lastFGColor = null;

    /**
     * 缓存的背景色
     */
    private Color lastBGColor = null;

    /**
     * 缓存的前景色方法
     */
    private Method lastFGMethod = null;

    /**
     * 缓存的前景色方法
     */
    private Method lastBGMethod = null;

    /**
     * 缓存的主题前景色
     */
    private javafx.scene.paint.Color lastThemeFGColor = null;

    /**
     * 缓存的主题背景色
     */
    private javafx.scene.paint.Color lastThemeBGColor = null;

    /**
     * 获取主题颜色
     *
     * @param foreground 是否前景色
     * @param colorIndex 颜色索引
     * @return 结果
     */
    private Color getPaletteColor(boolean foreground, int colorIndex) {
        Color color;
        // 基础的前景或者背景色则从主题生成
        if (colorIndex == 0 || colorIndex == 7 || colorIndex == 8 || colorIndex == 15) {
            javafx.scene.paint.Color color1 = foreground ? ThemeManager.currentForegroundColor() : ThemeManager.currentBackgroundColor();
            if (foreground) {
                if (this.lastFGColor == null || this.lastThemeFGColor != color1) {
                    color = ShelTerminalUtil.fromFXColor(color1);
                    this.lastFGColor = color;
                    this.lastThemeFGColor = color1;
                } else {
                    color = this.lastFGColor;
                }
            } else {
                if (this.lastBGColor == null || this.lastThemeBGColor != color1) {
                    color = ShelTerminalUtil.fromFXColor(color1);
                    this.lastBGColor = color;
                    this.lastThemeBGColor = color1;
                } else {
                    color = this.lastBGColor;
                }
            }
        } else {
            ////TODO: 如果是暗黑模式，需要修正部分颜色
            //if (ThemeManager.isDarkMode()) {
            //    if (colorIndex == 0) {
            //        colorIndex = 7;
            //    } else if (colorIndex == 8) {
            //        colorIndex = 15;
            //    } else if (colorIndex == 7) {
            //        colorIndex = 0;
            //    } else if (colorIndex == 15) {
            //        colorIndex = 8;
            //    } else if (colorIndex == 4) {
            //        colorIndex = 2;
            //    } else if (colorIndex == 12) {
            //        colorIndex = 10;
            //    }
            //}
            Method method;
            if (foreground) {
                if (this.lastFGMethod == null) {
                    method = ReflectUtil.getMethod(ColorPalette.class, "getForegroundByColorIndex", int.class);
                    method.setAccessible(true);
                    this.lastFGMethod = method;
                } else {
                    method = this.lastFGMethod;
                }
            } else {
                if (this.lastBGMethod == null) {
                    method = ReflectUtil.getMethod(ColorPalette.class, "getBackgroundByColorIndex", int.class);
                    method.setAccessible(true);
                    this.lastBGMethod = method;
                } else {
                    method = this.lastBGMethod;
                }
            }
            if (OSUtil.isWindows()) {
                color = (Color) ReflectUtil.invokeOnly(ColorPaletteImpl.WINDOWS_PALETTE, method, colorIndex);
            } else {
                color = (Color) ReflectUtil.invokeOnly(ColorPaletteImpl.XTERM_PALETTE, method, colorIndex);
            }
        }
        return color;
    }
}
