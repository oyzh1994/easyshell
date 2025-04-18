package cn.oyzh.jeditermfx.terminal.ui.settings;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.system.OSUtil;
import cn.oyzh.jeditermfx.terminal.ui.TerminalActionPresentation;
import com.jediterm.terminal.HyperlinkStyle;
import com.jediterm.terminal.TerminalColor;
import com.jediterm.terminal.TextStyle;
import com.jediterm.terminal.emulator.ColorPalette;
import com.jediterm.terminal.emulator.ColorPaletteImpl;
import com.jediterm.terminal.model.LinesBuffer;
import com.jediterm.terminal.model.TerminalTypeAheadSettings;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.text.Font;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

import static cn.oyzh.jeditermfx.terminal.ui.FXTransformers.fromFxToTerminalColor;

public class DefaultSettingsProvider implements SettingsProvider {

    @Override
    public @NotNull TerminalActionPresentation getOpenUrlActionPresentation() {
        return new TerminalActionPresentation("Open as URL", Collections.emptyList());
    }

    @Override
    public @NotNull TerminalActionPresentation getCopyActionPresentation() {
        KeyCombination keyCombination = OSUtil.isMacOS()
                ? new KeyCodeCombination(KeyCode.C, KeyCombination.META_DOWN)
                // CTRL + C is used for signal; use CTRL + SHIFT + C instead
                : new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
        return new TerminalActionPresentation("Copy", keyCombination);
    }

    @Override
    public @NotNull TerminalActionPresentation getPasteActionPresentation() {
        KeyCombination keyCombination = OSUtil.isMacOS()
                ? new KeyCodeCombination(KeyCode.V, KeyCombination.META_DOWN)
                // CTRL + V is used for signal; use CTRL + SHIFT + V instead
                : new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
        return new TerminalActionPresentation("Paste", keyCombination);
    }

    @Override
    public @NotNull TerminalActionPresentation getClearBufferActionPresentation() {
        return new TerminalActionPresentation("Clear Buffer", OSUtil.isMacOS()
                ? new KeyCodeCombination(KeyCode.K, KeyCombination.META_DOWN)
                : new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN));
    }

    @Override
    public @NotNull TerminalActionPresentation getPageUpActionPresentation() {
        return new TerminalActionPresentation("Page Up",
                new KeyCodeCombination(KeyCode.PAGE_UP, KeyCombination.SHIFT_DOWN));
    }

    @Override
    public @NotNull TerminalActionPresentation getPageDownActionPresentation() {
        return new TerminalActionPresentation("Page Down",
                new KeyCodeCombination(KeyCode.PAGE_DOWN, KeyCombination.SHIFT_DOWN));
    }

    @Override
    public @NotNull TerminalActionPresentation getLineUpActionPresentation() {
        return new TerminalActionPresentation("Line Up", OSUtil.isMacOS()
                ? new KeyCodeCombination(KeyCode.UP, KeyCombination.META_DOWN)
                : new KeyCodeCombination(KeyCode.UP, KeyCombination.CONTROL_DOWN));
    }

    @Override
    public @NotNull TerminalActionPresentation getLineDownActionPresentation() {
        return new TerminalActionPresentation("Line Down", OSUtil.isMacOS()
                ? new KeyCodeCombination(KeyCode.DOWN, KeyCombination.META_DOWN)
                : new KeyCodeCombination(KeyCode.DOWN, KeyCombination.CONTROL_DOWN));
    }

    @Override
    public @NotNull TerminalActionPresentation getFindActionPresentation() {
        return new TerminalActionPresentation("Find", OSUtil.isMacOS()
                ? new KeyCodeCombination(KeyCode.F, KeyCombination.META_DOWN)
                : new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN));
    }

    @Override
    public @NotNull TerminalActionPresentation getSelectAllActionPresentation() {
        return new TerminalActionPresentation("Select All", Collections.emptyList());
    }

    @Override
    public ColorPalette getTerminalColorPalette() {
        return OSUtil.isWindows() ? ColorPaletteImpl.WINDOWS_PALETTE : ColorPaletteImpl.XTERM_PALETTE;
    }

    @Override
    public Font getTerminalFont() {
        String fontName;
        if (OSUtil.isWindows()) {
            fontName = "Consolas";
        } else if (OSUtil.isMacOS()) {
            fontName = "Menlo";
        } else {
            fontName = "Monospaced";
        }
        var font = Font.font(fontName, getTerminalFontSize());
        JulLog.debug("Terminal font: {}", font);
        return font;
    }

    @Override
    public float getTerminalFontSize() {
        return 14;
    }

    @Override
    public @NotNull TextStyle getSelectionColor() {
        return new TextStyle(TerminalColor.WHITE, TerminalColor.rgb(82, 109, 165));
    }

    @Override
    public @NotNull TextStyle getFoundPatternColor() {
        return new TextStyle(TerminalColor.BLACK, TerminalColor.rgb(255, 255, 0));
    }

    @Override
    public TextStyle getHyperlinkColor() {
        return new TextStyle(fromFxToTerminalColor(javafx.scene.paint.Color.BLUE), TerminalColor.WHITE);
    }

    @Override
    public HyperlinkStyle.HighlightMode getHyperlinkHighlightingMode() {
        return HyperlinkStyle.HighlightMode.HOVER;
//        return HyperlinkStyle.HighlightMode.HOVER_WITH_BOTH_COLORS;
    }

    @Override
    public boolean useInverseSelectionColor() {
        return true;
    }

    @Override
    public boolean copyOnSelect() {
        return emulateX11CopyPaste();
    }

    @Override
    public boolean pasteOnMiddleMouseClick() {
        return emulateX11CopyPaste();
    }

    @Override
    public boolean emulateX11CopyPaste() {
        return false;
    }

    @Override
    public boolean useAntialiasing() {
        return true;
    }

    @Override
    public int maxRefreshRate() {
        return 50;
    }

    @Override
    public boolean audibleBell() {
        return true;
    }

    @Override
    public boolean enableMouseReporting() {
        return true;
    }

    @Override
    public int caretBlinkingMs() {
        return 505;
    }

    @Override
    public boolean scrollToBottomOnTyping() {
        return true;
    }

    @Override
    public boolean DECCompatibilityMode() {
        return true;
    }

    @Override
    public boolean forceActionOnMouseReporting() {
        return false;
    }

    @Override
    public int getBufferMaxLinesCount() {
        return LinesBuffer.DEFAULT_MAX_LINES_COUNT;
    }

    @Override
    public boolean altSendsEscape() {
        return true;
    }

    @Override
    public boolean ambiguousCharsAreDoubleWidth() {
        return false;
    }

    @Override
    public @NotNull TerminalTypeAheadSettings getTypeAheadSettings() {
        return TerminalTypeAheadSettings.DEFAULT;
    }

    @Override
    public boolean sendArrowKeysInAlternativeMode() {
        return true;
    }
}
