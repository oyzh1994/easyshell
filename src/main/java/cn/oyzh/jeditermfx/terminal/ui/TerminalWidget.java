package cn.oyzh.jeditermfx.terminal.ui;

import cn.oyzh.jeditermfx.terminal.TerminalDisplay;
import cn.oyzh.jeditermfx.terminal.TtyConnector;
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
