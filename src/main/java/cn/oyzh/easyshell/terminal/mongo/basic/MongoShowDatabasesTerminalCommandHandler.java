package cn.oyzh.easyshell.terminal.mongo.basic;

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
