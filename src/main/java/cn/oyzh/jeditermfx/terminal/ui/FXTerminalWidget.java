package cn.oyzh.jeditermfx.terminal.ui;

import com.jediterm.terminal.TerminalDisplay;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.ui.JediTermFxWidget;
import javafx.geometry.Dimension2D;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

/**
 * @author traff
 */
public interface FXTerminalWidget {

    JediTermFxWidget createTerminalSession(TtyConnector ttyConnector);

    Pane getPane();

    Node getPreferredFocusableNode();

    boolean canOpenSession();

    Dimension2D getPreferredSize();

    TerminalDisplay getTerminalDisplay();

    void addListener(cn.oyzh.jeditermfx.terminal.ui.TerminalWidgetListener listener);

    void removeListener(TerminalWidgetListener listener);
}
