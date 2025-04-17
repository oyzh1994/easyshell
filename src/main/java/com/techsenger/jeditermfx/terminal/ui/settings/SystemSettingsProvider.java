package com.techsenger.jeditermfx.terminal.ui.settings;

import com.techsenger.jeditermfx.terminal.ui.TerminalActionPresentation;
import org.jetbrains.annotations.NotNull;

public interface SystemSettingsProvider {

    @NotNull
    TerminalActionPresentation getOpenUrlActionPresentation();

    @NotNull
    TerminalActionPresentation getCopyActionPresentation();

    @NotNull
    TerminalActionPresentation getPasteActionPresentation();

    @NotNull
    TerminalActionPresentation getClearBufferActionPresentation();

    @NotNull
    TerminalActionPresentation getPageUpActionPresentation();

    @NotNull
    TerminalActionPresentation getPageDownActionPresentation();

    @NotNull
    TerminalActionPresentation getLineUpActionPresentation();

    @NotNull
    TerminalActionPresentation getLineDownActionPresentation();

    @NotNull
    TerminalActionPresentation getFindActionPresentation();

    @NotNull
    TerminalActionPresentation getSelectAllActionPresentation();
}
