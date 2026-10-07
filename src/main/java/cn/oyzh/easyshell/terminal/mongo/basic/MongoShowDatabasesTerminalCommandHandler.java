package cn.oyzh.easyshell.terminal.mongo.basic;

/**
 * mongo显示数据库命令处理器（数据库名别名）
 *
 * @author oyzh
 * @since 2023/09/20
 */
public class MongoShowDatabasesTerminalCommandHandler extends MongoShowDbsTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return "databases";
    }

}
