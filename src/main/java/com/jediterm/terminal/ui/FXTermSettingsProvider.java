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
     * 获取恢复终端字体大小操作
     *
     * @return 操作
     */
    @NotNull TerminalActionPresentation getResetTermSizePresentation();

    /**
     * 设置终端字体大小
     *
     * @param terminalFontSize 终端字体大小
     */
    void setTerminalFontSize(float terminalFontSize);

    /**
     * 获取退格编码
     *
     * @return 退格编码
     */
    default Object getBackspaceCode() {
        // return new byte[]{0x7F};
        // return new byte[]{0x08};
        // return "ESC[3~";
        return new byte[]{0x08};
    }

    /**
     * 设置退格码
     *
     * @param backspaceCode 退格码
     */
    default void setBackspaceCode(Object backspaceCode) {

    }
}
