package cn.oyzh.easyshell.terminal.mongo.basic;

/**
 * mongo显示表命令处理器（集合名别名）
 *
 * @author oyzh
 * @since 2023/09/20
 */
public class MongoShowTablesTerminalCommandHandler extends MongoShowCollectionsTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return "tables";
    }

}
