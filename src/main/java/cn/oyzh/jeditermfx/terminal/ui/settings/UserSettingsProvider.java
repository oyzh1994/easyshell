package cn.oyzh.jeditermfx.terminal.ui.settings;

import cn.oyzh.jeditermfx.terminal.HyperlinkStyle;
import cn.oyzh.jeditermfx.terminal.TerminalColor;
import cn.oyzh.jeditermfx.terminal.TextStyle;
import cn.oyzh.jeditermfx.terminal.emulator.ColorPalette;
import cn.oyzh.jeditermfx.terminal.model.TerminalTypeAheadSettings;
import javafx.scene.text.Font;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public interface UserSettingsProvider {

    ColorPalette getTerminalColorPalette();

    Font getTerminalFont();

    float getTerminalFontSize();

    /**
     * @return vertical scaling factor
     */
    default float getLineSpacing() {
        return 1.0f;
    }

    default boolean shouldDisableLineSpacingForAlternateScreenBuffer() {
        return false;
    }

    default boolean shouldFillCharacterBackgroundIncludingLineSpacing() {
        return true;
    }

    default @NotNull TerminalColor getDefaultForeground() {
        return Objects.requireNonNull(getDefaultStyle().getForeground());
    }

    default @NotNull TerminalColor getDefaultBackground() {
        return Objects.requireNonNull(getDefaultStyle().getBackground());
    }

    /**
     * @deprecated override {@link UserSettingsProvider#getDefaultForeground()} and
     * {@link UserSettingsProvider#getDefaultBackground()} instead
     */
    @Deprecated
    default @NotNull TextStyle getDefaultStyle() {
        return new TextStyle(TerminalColor.BLACK, TerminalColor.WHITE);
    }

    @NotNull
    TextStyle getSelectionColor();

    @NotNull
    TextStyle getFoundPatternColor();

    TextStyle getHyperlinkColor();

    HyperlinkStyle.HighlightMode getHyperlinkHighlightingMode();

    default boolean enableTextBlinking() {
        return false;
    }

    default int slowTextBlinkMs() {
        return 1000;
    }

    default int rapidTextBlinkMs() {
        return 500;
    }

    boolean useInverseSelectionColor();

    boolean copyOnSelect();

    boolean pasteOnMiddleMouseClick();

    boolean emulateX11CopyPaste();

    boolean useAntialiasing();

    int maxRefreshRate();

    boolean audibleBell();

    boolean enableMouseReporting();

    int caretBlinkingMs();

    boolean scrollToBottomOnTyping();

    boolean DECCompatibilityMode();

    boolean forceActionOnMouseReporting();

    int getBufferMaxLinesCount();

    boolean altSendsEscape();

    boolean ambiguousCharsAreDoubleWidth();

    @NotNull
    TerminalTypeAheadSettings getTypeAheadSettings();

    boolean sendArrowKeysInAlternativeMode();
}
