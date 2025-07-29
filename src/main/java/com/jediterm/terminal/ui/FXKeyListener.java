package com.jediterm.terminal.ui;

import javafx.scene.input.KeyEvent;

/**
 * 按键事件
 *
 * @author oyzh
 * @since 2025-07-29
 */
public interface FXKeyListener {

    void keyTyped(KeyEvent e);

    void keyPressed(KeyEvent e);

    void keyReleased(KeyEvent e);
}
