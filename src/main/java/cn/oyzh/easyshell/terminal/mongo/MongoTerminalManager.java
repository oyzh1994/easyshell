package cn.oyzh.easyshell.terminal.mongo;

import cn.oyzh.easyshell.terminal.mongo.basic.MongoShowCollectionsTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.mongo.basic.MongoShowDatabasesTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.mongo.basic.MongoShowDbsTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.mongo.basic.MongoShowTablesTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.mongo.basic.MongoShowUseTerminalCommandHandler;
import cn.oyzh.fx.terminal.standard.ClearTerminalCommandHandler;
import cn.oyzh.fx.terminal.standard.HelpTerminalCommandHandler;
import cn.oyzh.fx.terminal.util.TerminalManager;

/**
 * @author oyzh
 * @since 2024-12-30
 */
public class MongoTerminalManager {

    /**
     * 注册处理器
     */
    public static void registerHandlers() {
        // 标准命令
        TerminalManager.registerHandler(MongoTerminalPane.TERMINAL_NAME, HelpTerminalCommandHandler.class);
        TerminalManager.registerHandler(MongoTerminalPane.TERMINAL_NAME, ClearTerminalCommandHandler.class);

        // 基础命令
        TerminalManager.registerHandler(MongoTerminalPane.TERMINAL_NAME, MongoShowDbsTerminalCommandHandler.class);
        TerminalManager.registerHandler(MongoTerminalPane.TERMINAL_NAME, MongoShowUseTerminalCommandHandler.class);
        TerminalManager.registerHandler(MongoTerminalPane.TERMINAL_NAME, MongoShowTablesTerminalCommandHandler.class);
        TerminalManager.registerHandler(MongoTerminalPane.TERMINAL_NAME, MongoShowDatabasesTerminalCommandHandler.class);
        TerminalManager.registerHandler(MongoTerminalPane.TERMINAL_NAME, MongoShowCollectionsTerminalCommandHandler.class);
    }
}
