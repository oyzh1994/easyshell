package cn.oyzh.easyshell.terminal;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.shortcut.ShellShortcutAction;
import cn.oyzh.easyshell.shortcut.ShellShortcutKeyCombination;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.easyshell.store.ShellShortcutStore;
import cn.oyzh.fx.plus.font.FontConfig;
import cn.oyzh.fx.plus.font.FontManager;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.tty.TtyColorPalette;
import cn.oyzh.fx.tty.TtyTermSettingsProvider;
import cn.oyzh.fx.tty.TtyTerminalUtil;
import cn.oyzh.i18n.I18nHelper;
import com.jediterm.core.Color;
import com.jediterm.terminal.HyperlinkStyle;
import com.jediterm.terminal.TerminalColor;
import com.jediterm.terminal.TextStyle;
import com.jediterm.terminal.emulator.ColorPalette;
import com.jediterm.terminal.ui.FXTerminalActionPresentation;
import com.jediterm.terminal.ui.TerminalActionPresentation;
import com.jediterm.terminal.ui.settings.FXDefaultSettingsProvider;
import javafx.scene.input.KeyCombination;
import javafx.scene.text.Font;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;


/**
 * shell终端设置提供者
 *
 * @author oyzh
 * @since 2025-03-25
 */
public class ShellSettingsProvider extends FXDefaultSettingsProvider implements TtyTermSettingsProvider {

    /**
     * 程序设置
     */
    private final ShellSetting setting = ShellSettingStore.SETTING;

    /**
     * 终端字体大小
     */
    private transient float terminalFontSize = this.setting.getTerminalFontSize();

    @Override
    public @NotNull FXTerminalActionPresentation getOpenUrlActionPresentation() {
        return new FXTerminalActionPresentation(I18nHelper.openAsUrl());
    }

    @Override
    public @NotNull FXTerminalActionPresentation getCopyActionPresentation() {
        return new FXTerminalActionPresentation(I18nHelper.copy(),
                this.keyCombinations(ShellShortcutAction.TERMINAL_COPY));
    }

    @Override
    public @NotNull FXTerminalActionPresentation getPasteActionPresentation() {
        return new FXTerminalActionPresentation(I18nHelper.paste(),
                this.keyCombinations(ShellShortcutAction.TERMINAL_PASTE));
    }

    @Override
    public @NotNull FXTerminalActionPresentation getClearBufferActionPresentation() {
        return new FXTerminalActionPresentation(I18nHelper.clearBuffer(),
                this.keyCombinations(ShellShortcutAction.TERMINAL_CLEAR));
    }

    @Override
    public @NotNull FXTerminalActionPresentation getPageUpActionPresentation() {
        return new FXTerminalActionPresentation(I18nHelper.pageUp(),
                this.keyCombinations(ShellShortcutAction.TERMINAL_PAGE_UP));
    }

    @Override
    public @NotNull FXTerminalActionPresentation getPageDownActionPresentation() {
        return new FXTerminalActionPresentation(I18nHelper.pageDown(),
                this.keyCombinations(ShellShortcutAction.TERMINAL_PAGE_DOWN));
    }

    @Override
    public @NotNull FXTerminalActionPresentation getLineUpActionPresentation() {
        return new FXTerminalActionPresentation(I18nHelper.lineUp(),
                this.keyCombinations(ShellShortcutAction.TERMINAL_LINE_UP));
    }

    @Override
    public @NotNull FXTerminalActionPresentation getLineDownActionPresentation() {
        return new FXTerminalActionPresentation(I18nHelper.lineDown(),
                this.keyCombinations(ShellShortcutAction.TERMINAL_LINE_DOWN));
    }

    @Override
    public @NotNull FXTerminalActionPresentation getFindActionPresentation() {
        return new FXTerminalActionPresentation(I18nHelper.find(),
                this.keyCombinations(ShellShortcutAction.TERMINAL_FIND));
    }

    @Override
    public @NotNull FXTerminalActionPresentation getSelectAllActionPresentation() {
        return new FXTerminalActionPresentation(I18nHelper.selectAll(),
                this.keyCombinations(ShellShortcutAction.TERMINAL_SELECT_ALL));
    }

    @Override
    public @NotNull TerminalActionPresentation getIncrTermSizePresentation() {
        return new FXTerminalActionPresentation(I18nHelper.incrFont(),
                this.keyCombinations(ShellShortcutAction.TERMINAL_FONT_INCREASE));
    }

    @Override
    public @NotNull TerminalActionPresentation getDecrTermSizePresentation() {
        return new FXTerminalActionPresentation(I18nHelper.decrFont(),
                this.keyCombinations(ShellShortcutAction.TERMINAL_FONT_DECREASE));
    }

    @Override
    public @NotNull TerminalActionPresentation getResetTermSizePresentation() {
        return new FXTerminalActionPresentation(I18nHelper.resetFont(),
                this.keyCombinations(ShellShortcutAction.TERMINAL_FONT_RESET));
    }

    /**
     * Resolves the active key-map binding for a terminal action.
     */
    private List<KeyCombination> keyCombinations(String actionId) {
        if (!ShellShortcutStore.SHORTCUT.isEnabled()) {
            return List.of();
        }
        return List.of(new ShellShortcutKeyCombination(ShellShortcutStore.SHORTCUT, actionId));
    }

    // @Override
    // public java.awt.Font getTerminalFont() {
    //     return super.getTerminalFont();
    // }

    @Override
    public ColorPalette getTerminalColorPalette() {
        return TtyColorPalette.INSTANCE;
    }

    @Override
    public Font getFXTerminalFont() {
        FontConfig config = this.setting.terminalFontConfig();
        config.setSize((int) this.terminalFontSize);
        Font font = FontManager.toFont(config);
        if (JulLog.isDebugEnabled()) {
            JulLog.debug("Terminal font: {}", font);
        }
        return font;
    }

    @Override
    public float getTerminalFontSize() {
        return this.terminalFontSize;
    }

    @Override
    public void setTerminalFontSize(float terminalFontSize) {
        this.terminalFontSize = terminalFontSize;
        this.setting.setTerminalFontSize((byte) terminalFontSize);
        ShellSettingStore.INSTANCE.update(this.setting);
    }

    @Override
    public boolean useAntialiasing() {
        return this.setting.isTermUseAntialiasing();
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
    public boolean altSendsEscape() {
        return this.altSendsEscape;
    }

    @Override
    public boolean audibleBell() {
        return this.setting.isTermBeep();
    }

    @Override
    public boolean copyOnSelect() {
        return this.setting.isTermCopyOnSelected();
    }

    //@Override
    //public boolean pasteOnMiddleMouseClick() {
    //    return this.setting.isTermPasteByMiddle();
    //}

    /**
     * 退格码
     */
    private Object backspaceCode;

    @Override
    public Object getBackspaceCode() {
        if (this.backspaceCode == null) {
            return TtyTermSettingsProvider.super.getBackspaceCode();
        }
        return this.backspaceCode;
    }

    @Override
    public void setBackspaceCode(Object backspaceCode) {
        if (backspaceCode instanceof byte[] bytes) {
            JulLog.info("backspaceCode:{}", Arrays.toString(bytes));
        } else {
            JulLog.info("backspaceCode:{}", backspaceCode);
        }
        this.backspaceCode = backspaceCode;
    }

    /**
     * alt修饰符
     */
    private boolean altSendsEscape;

    @Override
    public void setAltSendsEscape(boolean altSendsEscape) {
        JulLog.info("altSendsEscape:{}", altSendsEscape);
        this.altSendsEscape = altSendsEscape;
    }

    @Override
    public @NotNull TextStyle getFoundPatternColor() {
        Color color;
        if (ThemeManager.isDarkMode()) {
            color = TtyTerminalUtil.fromFXColor(javafx.scene.paint.Color.valueOf("#AC7920"));
        } else {
            color = TtyTerminalUtil.fromFXColor(javafx.scene.paint.Color.valueOf("#E2A53A"));
        }
        Color color1 = TtyTerminalUtil.fromFXColor(ThemeManager.currentForegroundColor());
        return new TextStyle(TerminalColor.color(color1), TerminalColor.color(color));
    }

    @Override
    public TextStyle getHyperlinkColor() {
        Color color = TtyTerminalUtil.fromFXColor(ThemeManager.currentBackgroundColor());
        Color color1;
        if (ThemeManager.isDarkMode()) {
            color1 = TtyTerminalUtil.fromFXColor(javafx.scene.paint.Color.valueOf("#4B73A7"));
        } else {
            color1 = TtyTerminalUtil.fromFXColor(javafx.scene.paint.Color.valueOf("#236DB2"));
        }
        return new TextStyle(TerminalColor.color(color1), TerminalColor.color(color));
    }

    @Override
    public HyperlinkStyle.HighlightMode getHyperlinkHighlightingMode() {
        return HyperlinkStyle.HighlightMode.ALWAYS;
    }
}
