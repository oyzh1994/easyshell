package cn.oyzh.easyshell.tabs.sqlite.terminal;

import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.easyshell.terminal.sqlite.ShellSqliteTerminalPane;
import cn.oyzh.fx.gui.tabs.RichTabController;
import javafx.fxml.FXML;

/**
 * SQLite 终端控制器。
 *
 * @since 2026-11-02
 */
public class ShellSqliteTerminalTabController extends RichTabController {

    @FXML
    private ShellSqliteTerminalPane terminal;

    public void init(ShellSqliteClient client) {
        this.terminal.init(client);
    }
}

