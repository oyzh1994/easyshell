package com.jediterm.terminal.ui;

import org.jetbrains.annotations.NotNull;

/**
 * 终端设置提供者
 *
 * @author oyzh
 * @since 2025-06-28
 */
public interface FXTermSettingsProvider {

    /**
     * 获取增加终端字体大小操作
     *
     * @return 操作
     */
    @NotNull TerminalActionPresentation getIncrTermSizePresentation();

    /**
     * 获取减少终端字体大小操作
     *
     * @return 操作
     */
    @NotNull TerminalActionPresentation getDecrTermSizePresentation();

    /**
     * 设置终端字体大小
     *
     * @param terminalFontSize 终端字体大小
     */
    void setTerminalFontSize(float terminalFontSize);

}
