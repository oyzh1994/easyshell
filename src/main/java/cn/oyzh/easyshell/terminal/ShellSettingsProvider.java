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
    public boolean audibleBell() {
        return this.setting.isTermBeep();
    }

    @Override
    public boolean copyOnSelect() {
        return this.setting.isTermCopyOnSelected();
    }
}
