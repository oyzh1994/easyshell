package cn.oyzh.easyshell.ssh;

import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.util.FXColorUtil;
import com.techsenger.jeditermfx.core.TerminalColor;
import com.techsenger.jeditermfx.core.TextStyle;
import com.techsenger.jeditermfx.ui.settings.DefaultSettingsProvider;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2025-03-08
 */
public class ShellSettingsProvider extends DefaultSettingsProvider {

    /**
     * 寻找最接近的配色
     *
     * @return 配色
     */
    protected TerminalColor[] findClosestColor() {
        Color color1 = FXColorUtil.findClosestColor(ThemeManager.currentAccentColor(), RGB_COLORS);
        Color color2 = FXColorUtil.findClosestColor(ThemeManager.currentBackgroundColor(), RGB_COLORS);
        if (color1 != null && color2 != null) {
            int index1 = RGB_COLORS.indexOf(color1);
            int index2 = RGB_COLORS.indexOf(color2);
            return new TerminalColor[]{TerminalColor.index(index1), TerminalColor.index(index2)};
        }
        if (ThemeManager.isDarkMode()) {
            return new TerminalColor[]{TerminalColor.WHITE, TerminalColor.BLACK};
        }
        return new TerminalColor[]{TerminalColor.BLACK, TerminalColor.WHITE};
    }

    @Override
    public TextStyle getDefaultStyle() {
        TerminalColor[] color = this.findClosestColor();
        return new TextStyle(color[0], color[1]);
//        Color color1 = ThemeManager.currentAccentColor();
//        Color color2 = ThemeManager.currentBackgroundColor();
//        com.techsenger.jeditermfx.core.Color color3=new com.techsenger.jeditermfx.core.Color((int) color1.getRed() * 255, (int) color1.getGreen() * 255, (int) color1.getBlue() * 255, 255);
//        com.techsenger.jeditermfx.core.Color color4=new com.techsenger.jeditermfx.core.Color((int) color2.getRed() * 255, (int) color2.getGreen() * 255, (int) color2.getBlue() * 255, 255);
//        TerminalColor terminalColor1 = TerminalColor.fromColor(color3);
//        TerminalColor terminalColor2 = TerminalColor.fromColor(color4);
////        TerminalColor terminalColor1 = TerminalColor.rgb((int) color1.getRed() * 255, (int) color1.getGreen() * 255, (int) color1.getBlue() * 255,color1.getOpacity());
////        TerminalColor terminalColor2 = TerminalColor.rgb((int) color2.getRed() * 255, (int) color2.getGreen() * 255, (int) color2.getBlue() * 255);
//        return new TextStyle(terminalColor1, terminalColor2);
    }

    /**
     * 终端支持的rgb颜色列表
     */
    public static final List<Color> RGB_COLORS = new ArrayList<>();

    static {
        for (int i = 0; i < 16; i++) {
            if (i == 0) {
                RGB_COLORS.add(Color.rgb(0, 0, 0));
            } else if (i == 1) {
                RGB_COLORS.add(Color.rgb(170, 0, 0));
            } else if (i == 2) {
                RGB_COLORS.add(Color.rgb(0, 170, 0));
            } else if (i == 3) {
                RGB_COLORS.add(Color.rgb(170, 85, 0));
            } else if (i == 4) {
                RGB_COLORS.add(Color.rgb(0, 0, 170));
            } else if (i == 5) {
                RGB_COLORS.add(Color.rgb(170, 0, 170));
            } else if (i == 6) {
                RGB_COLORS.add(Color.rgb(0, 170, 170));
            } else if (i == 7) {
                RGB_COLORS.add(Color.rgb(170, 170, 170));
            } else if (i == 8) {
                RGB_COLORS.add(Color.rgb(85, 85, 85));
            } else if (i == 9) {
                RGB_COLORS.add(Color.rgb(255, 85, 85));
            } else if (i == 10) {
                RGB_COLORS.add(Color.rgb(85, 255, 85));
            } else if (i == 11) {
                RGB_COLORS.add(Color.rgb(255, 255, 85));
            } else if (i == 12) {
                RGB_COLORS.add(Color.rgb(85, 85, 255));
            } else if (i == 13) {
                RGB_COLORS.add(Color.rgb(255, 85, 255));
            } else if (i == 14) {
                RGB_COLORS.add(Color.rgb(85, 255, 255));
            } else {
                RGB_COLORS.add(Color.rgb(255, 255, 255));
            }
        }
    }
}
