package cn.oyzh.easyshell.terminal.mongo.basic;

/**
 * @author oyzh
 * @since 2023/09/20
 */
public class MongoShowTablesTerminalCommandHandler extends MongoShowCollectionsTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return "tables";
    }

}
