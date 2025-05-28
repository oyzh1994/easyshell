package com.jediterm.terminal.ui;

import com.jediterm.terminal.TerminalDisplay;
import com.jediterm.terminal.TtyConnector;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

/**
 * @author traff
 */
public interface FXTerminalWidget {

    FXJediTermWidget createTerminalSession(TtyConnector ttyConnector);

    Pane getComponent();

    Node getPreferredFocusableNode();

    boolean requestFocusInWindow();

    void requestFocus();

    boolean canOpenSession();

    TerminalDisplay getTerminalDisplay();

    void addListener(FXTerminalWidgetListener listener);

    void removeListener(FXTerminalWidgetListener listener);
}
