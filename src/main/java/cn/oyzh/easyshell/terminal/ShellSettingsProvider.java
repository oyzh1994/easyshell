package cn.oyzh.easyshell.terminal;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.system.OSUtil;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.fx.plus.font.FontManager;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.i18n.I18nHelper;
import cn.oyzh.jeditermfx.terminal.ui.TerminalActionPresentation;
import cn.oyzh.jeditermfx.terminal.ui.settings.DefaultSettingsProvider;
import com.jediterm.terminal.emulator.ColorPalette;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.text.Font;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;


/**
 * @author oyzh
 * @since 2025-03-08
 */
public class ShellSettingsProvider extends DefaultSettingsProvider {

    /**
     * 程序设置
     */
    private final ShellSetting setting = ShellSettingStore.SETTING;

    @Override
    public @NotNull TerminalActionPresentation getOpenUrlActionPresentation() {
        return new TerminalActionPresentation(I18nHelper.openAsUrl(), Collections.emptyList());
    }

    @Override
    public @NotNull TerminalActionPresentation getCopyActionPresentation() {
        KeyCombination keyCombination = OSUtil.isMacOS()
                ? new KeyCodeCombination(KeyCode.C, KeyCombination.META_DOWN)
                // CTRL + C is used for signal; use CTRL + SHIFT + C instead
                : new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
        return new TerminalActionPresentation(I18nHelper.copy(), keyCombination);
    }

    @Override
    public @NotNull TerminalActionPresentation getPasteActionPresentation() {
        KeyCombination keyCombination = OSUtil.isMacOS()
                ? new KeyCodeCombination(KeyCode.V, KeyCombination.META_DOWN)
                // CTRL + V is used for signal; use CTRL + SHIFT + V instead
                : new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
        return new TerminalActionPresentation(I18nHelper.paste(), keyCombination);
    }

    @Override
    public @NotNull TerminalActionPresentation getClearBufferActionPresentation() {
        return new TerminalActionPresentation(I18nHelper.clearBuffer(), OSUtil.isMacOS()
                ? new KeyCodeCombination(KeyCode.L, KeyCombination.META_DOWN)
                : new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN));
    }

    @Override
    public @NotNull TerminalActionPresentation getPageUpActionPresentation() {
        return new TerminalActionPresentation(I18nHelper.pageUp(),
                new KeyCodeCombination(KeyCode.PAGE_UP, KeyCombination.SHIFT_DOWN));
    }

    @Override
    public @NotNull TerminalActionPresentation getPageDownActionPresentation() {
        return new TerminalActionPresentation(I18nHelper.pageDown(),
                new KeyCodeCombination(KeyCode.PAGE_DOWN, KeyCombination.SHIFT_DOWN));
    }

    @Override
    public @NotNull TerminalActionPresentation getLineUpActionPresentation() {
        return new TerminalActionPresentation(I18nHelper.lineUp(), OSUtil.isMacOS()
                ? new KeyCodeCombination(KeyCode.UP, KeyCombination.META_DOWN)
                : new KeyCodeCombination(KeyCode.UP, KeyCombination.CONTROL_DOWN));
    }

    @Override
    public @NotNull TerminalActionPresentation getLineDownActionPresentation() {
        return new TerminalActionPresentation(I18nHelper.lineDown(), OSUtil.isMacOS()
                ? new KeyCodeCombination(KeyCode.DOWN, KeyCombination.META_DOWN)
                : new KeyCodeCombination(KeyCode.DOWN, KeyCombination.CONTROL_DOWN));
    }

    @Override
    public @NotNull TerminalActionPresentation getFindActionPresentation() {
        return new TerminalActionPresentation(I18nHelper.find(), OSUtil.isMacOS()
                ? new KeyCodeCombination(KeyCode.F, KeyCombination.META_DOWN)
                : new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN));
    }

    @Override
    public @NotNull TerminalActionPresentation getSelectAllActionPresentation() {
        return new TerminalActionPresentation(I18nHelper.selectAll(), Collections.emptyList());
    }

    @Override
    public ColorPalette getTerminalColorPalette() {
        return ShellColorPalette.INSTANCE;
    }

    @Override
    public Font getTerminalFont() {
        Font font = FontManager.toFont(this.setting.terminalFontConfig());
        JulLog.debug("Terminal font: {}", font);
        return font;
    }

    @Override
    public float getTerminalFontSize() {
        return this.setting.getFontSize();
    }

    @Override
    public int maxRefreshRate() {
        if (this.setting.getTermRefreshRate() == -1) {
            return FXUtil.screenRefreshRate();
        }
        return this.setting.getTermRefreshRate();
    }

    @Override
    public int caretBlinkingMs() {
        return this.setting.getTermCursorBlinks();
    }

    @Override
    public boolean forceActionOnMouseReporting() {
        return true;
    }

    @Override
    public int getBufferMaxLinesCount() {
        return this.setting.getTermMaxLineCount();
    }

    @Override
    public boolean audibleBell() {
        return this.setting.isTermBeep();
    }

    @Override
    public boolean copyOnSelect() {
        return this.setting.isTermCopyOnSelected();
    }

    //    @Override
//    public boolean ambiguousCharsAreDoubleWidth() {
//        return true;
//    }

//    /**
//     * 寻找最接近的配色
//     *
//     * @return 配色
//     */
//    protected TerminalColor[] findClosestColor() {
//        Color color1 = FXColorUtil.findClosestColor(ThemeManager.currentAccentColor(), RGB_COLORS);
//        Color color2 = FXColorUtil.findClosestColor(ThemeManager.currentBackgroundColor(), RGB_COLORS);
//        if (color1 != null && color2 != null) {
//            int index1 = RGB_COLORS.indexOf(color1);
//            int index2 = RGB_COLORS.indexOf(color2);
//            return new TerminalColor[]{TerminalColor.index(index1), TerminalColor.index(index2)};
//        }
//        if (ThemeManager.isDarkMode()) {
//            return new TerminalColor[]{TerminalColor.WHITE, TerminalColor.BLACK};
//        }
//        return new TerminalColor[]{TerminalColor.BLACK, TerminalColor.WHITE};
//    }
//
//    @Override
//    public @NotNull TerminalColor getDefaultForeground() {
//        return this.findClosestColor()[0];
//    }
//
//    @Override
//    public @NotNull TerminalColor getDefaultBackground() {
//        return this.findClosestColor()[1];
//    }
//
//    @Override
//    public TextStyle getDefaultStyle() {
//        TerminalColor[] color = this.findClosestColor();
//        return new TextStyle(color[0], color[1]);
////        Color color1 = ThemeManager.currentAccentColor();
////        Color color2 = ThemeManager.currentBackgroundColor();
////        com.techsenger.jeditermfx.core.Color color3=new com.techsenger.jeditermfx.core.Color((int) color1.getRed() * 255, (int) color1.getGreen() * 255, (int) color1.getBlue() * 255, 255);
////        com.techsenger.jeditermfx.core.Color color4=new com.techsenger.jeditermfx.core.Color((int) color2.getRed() * 255, (int) color2.getGreen() * 255, (int) color2.getBlue() * 255, 255);
////        TerminalColor terminalColor1 = TerminalColor.fromColor(color3);
////        TerminalColor terminalColor2 = TerminalColor.fromColor(color4);
//////        TerminalColor terminalColor1 = TerminalColor.rgb((int) color1.getRed() * 255, (int) color1.getGreen() * 255, (int) color1.getBlue() * 255,color1.getOpacity());
//////        TerminalColor terminalColor2 = TerminalColor.rgb((int) color2.getRed() * 255, (int) color2.getGreen() * 255, (int) color2.getBlue() * 255);
////        return new TextStyle(terminalColor1, terminalColor2);
//    }

//    /**
//     * 终端支持的rgb颜色列表
//     */
//    public static final List<Color> RGB_COLORS = new ArrayList<>();
//
//    static {
//        for (int i = 0; i < 16; i++) {
//            if (i == 0) {
//                RGB_COLORS.add(Color.rgb(0, 0, 0));
//            } else if (i == 1) {
//                RGB_COLORS.add(Color.rgb(170, 0, 0));
//            } else if (i == 2) {
//                RGB_COLORS.add(Color.rgb(0, 170, 0));
//            } else if (i == 3) {
//                RGB_COLORS.add(Color.rgb(170, 85, 0));
//            } else if (i == 4) {
//                RGB_COLORS.add(Color.rgb(0, 0, 170));
//            } else if (i == 5) {
//                RGB_COLORS.add(Color.rgb(170, 0, 170));
//            } else if (i == 6) {
//                RGB_COLORS.add(Color.rgb(0, 170, 170));
//            } else if (i == 7) {
//                RGB_COLORS.add(Color.rgb(170, 170, 170));
//            } else if (i == 8) {
//                RGB_COLORS.add(Color.rgb(85, 85, 85));
//            } else if (i == 9) {
//                RGB_COLORS.add(Color.rgb(255, 85, 85));
//            } else if (i == 10) {
//                RGB_COLORS.add(Color.rgb(85, 255, 85));
//            } else if (i == 11) {
//                RGB_COLORS.add(Color.rgb(255, 255, 85));
//            } else if (i == 12) {
//                RGB_COLORS.add(Color.rgb(85, 85, 255));
//            } else if (i == 13) {
//                RGB_COLORS.add(Color.rgb(255, 85, 255));
//            } else if (i == 14) {
//                RGB_COLORS.add(Color.rgb(85, 255, 255));
//            } else {
//                RGB_COLORS.add(Color.rgb(255, 255, 255));
//            }
//        }
//    }
}
