package cn.oyzh.easyshell.terminal.mongo.basic;

import cn.oyzh.easyshell.terminal.mongo.MongoTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.mongo.MongoTerminalPane;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;

/**
 * @author oyzh
 * @since 2023/09/20
 */
public class MongoShowDatabasesTerminalCommandHandler extends MongoShowDbsTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return "databases";
    }

}
