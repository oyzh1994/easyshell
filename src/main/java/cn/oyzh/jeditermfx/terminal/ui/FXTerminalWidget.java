package cn.oyzh.jeditermfx.terminal.ui;

import com.jediterm.terminal.TerminalDisplay;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.ui.FXJediTermWidget;
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
