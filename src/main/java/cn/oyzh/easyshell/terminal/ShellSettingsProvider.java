package cn.oyzh.easyshell.terminal;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.system.OSUtil;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.fx.plus.font.FontConfig;
import cn.oyzh.fx.plus.font.FontManager;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.i18n.I18nHelper;
import com.jediterm.terminal.emulator.ColorPalette;
import com.jediterm.terminal.ui.FXTermSettingsProvider;
import com.jediterm.terminal.ui.FXTerminalActionPresentation;
import com.jediterm.terminal.ui.TerminalActionPresentation;
import com.jediterm.terminal.ui.settings.FXDefaultSettingsProvider;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.text.Font;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;


/**
 * @author oyzh
 * @since 2025-03-08
 */
public class ShellSettingsProvider extends FXDefaultSettingsProvider implements FXTermSettingsProvider {


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
        return new FXTerminalActionPresentation(I18nHelper.openAsUrl(), Collections.emptyList());
    }

    @Override
    public @NotNull FXTerminalActionPresentation getCopyActionPresentation() {
        KeyCombination keyCombination = OSUtil.isMacOS()
                ? new KeyCodeCombination(KeyCode.C, KeyCombination.META_DOWN)
                // CTRL + C is used for signal; use CTRL + SHIFT + C instead
                : new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
        return new FXTerminalActionPresentation(I18nHelper.copy(), keyCombination);
    }

    @Override
    public @NotNull FXTerminalActionPresentation getPasteActionPresentation() {
        KeyCombination keyCombination = OSUtil.isMacOS()
                ? new KeyCodeCombination(KeyCode.V, KeyCombination.META_DOWN)
                // CTRL + V is used for signal; use CTRL + SHIFT + V instead
                : new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
        return new FXTerminalActionPresentation(I18nHelper.paste(), keyCombination);
    }

    @Override
    public @NotNull FXTerminalActionPresentation getClearBufferActionPresentation() {
        return new FXTerminalActionPresentation(I18nHelper.clearBuffer(), OSUtil.isMacOS()
                ? new KeyCodeCombination(KeyCode.L, KeyCombination.META_DOWN)
                : new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN));
    }

    @Override
    public @NotNull FXTerminalActionPresentation getPageUpActionPresentation() {
        return new FXTerminalActionPresentation(I18nHelper.pageUp(),
                new KeyCodeCombination(KeyCode.PAGE_UP, KeyCombination.SHIFT_DOWN));
    }

    @Override
    public @NotNull FXTerminalActionPresentation getPageDownActionPresentation() {
        return new FXTerminalActionPresentation(I18nHelper.pageDown(),
                new KeyCodeCombination(KeyCode.PAGE_DOWN, KeyCombination.SHIFT_DOWN));
    }

    @Override
    public @NotNull FXTerminalActionPresentation getLineUpActionPresentation() {
        return new FXTerminalActionPresentation(I18nHelper.lineUp(), OSUtil.isMacOS()
                ? new KeyCodeCombination(KeyCode.UP, KeyCombination.META_DOWN)
                : new KeyCodeCombination(KeyCode.UP, KeyCombination.CONTROL_DOWN));
    }

    @Override
    public @NotNull FXTerminalActionPresentation getLineDownActionPresentation() {
        return new FXTerminalActionPresentation(I18nHelper.lineDown(), OSUtil.isMacOS()
                ? new KeyCodeCombination(KeyCode.DOWN, KeyCombination.META_DOWN)
                : new KeyCodeCombination(KeyCode.DOWN, KeyCombination.CONTROL_DOWN));
    }

    @Override
    public @NotNull FXTerminalActionPresentation getFindActionPresentation() {
        return new FXTerminalActionPresentation(I18nHelper.find(), OSUtil.isMacOS()
                ? new KeyCodeCombination(KeyCode.F, KeyCombination.META_DOWN)
                : new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN));
    }

    @Override
    public @NotNull FXTerminalActionPresentation getSelectAllActionPresentation() {
        KeyCombination keyCombination = OSUtil.isMacOS()
                ? new KeyCodeCombination(KeyCode.A, KeyCombination.META_DOWN)
                // CTRL + A is used for signal; use CTRL + SHIFT + A instead
                : new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
        return new FXTerminalActionPresentation(I18nHelper.selectAll(), keyCombination);
    }

    @Override
    public @NotNull TerminalActionPresentation getIncrTermSizePresentation() {
        KeyCombination keyCombination = OSUtil.isMacOS()
                ? new KeyCodeCombination(KeyCode.PLUS, KeyCombination.META_DOWN)
                : new KeyCodeCombination(KeyCode.PLUS, KeyCombination.CONTROL_DOWN);
        KeyCombination keyCombination1 = OSUtil.isMacOS()
                ? new KeyCodeCombination(KeyCode.ADD, KeyCombination.META_DOWN)
                : new KeyCodeCombination(KeyCode.ADD, KeyCombination.CONTROL_DOWN);
        return new FXTerminalActionPresentation(I18nHelper.incrFont(), List.of(keyCombination, keyCombination1));
    }

    @Override
    public @NotNull TerminalActionPresentation getDecrTermSizePresentation() {
        KeyCombination keyCombination = OSUtil.isMacOS()
                ? new KeyCodeCombination(KeyCode.SUBTRACT, KeyCombination.META_DOWN)
                : new KeyCodeCombination(KeyCode.SUBTRACT, KeyCombination.CONTROL_DOWN);
        KeyCombination keyCombination1 = OSUtil.isMacOS()
                ? new KeyCodeCombination(KeyCode.MINUS, KeyCombination.META_DOWN)
                : new KeyCodeCombination(KeyCode.MINUS, KeyCombination.CONTROL_DOWN);
        return new FXTerminalActionPresentation(I18nHelper.decrFont(), List.of(keyCombination, keyCombination1));
    }

    @Override
    public java.awt.Font getTerminalFont() {
        return super.getTerminalFont();
    }

    @Override
    public ColorPalette getTerminalColorPalette() {
        return ShellColorPalette.INSTANCE;
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
