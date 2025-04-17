package cn.oyzh.jeditermfx.terminal.ui;

import com.jediterm.terminal.TerminalDisplay;
import com.jediterm.terminal.TtyConnector;
import javafx.geometry.Dimension2D;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

/**
 * @author traff
 */
public interface TerminalWidget {

    JediTermFxWidget createTerminalSession(TtyConnector ttyConnector);

    Pane getPane();

    Node getPreferredFocusableNode();

    boolean canOpenSession();

    Dimension2D getPreferredSize();

    TerminalDisplay getTerminalDisplay();

    void addListener(TerminalWidgetListener listener);

    void removeListener(TerminalWidgetListener listener);
}
