package cn.oyzh.easyshell.terminal.mysql.basic;

/**
 * mysql显示数据库命令处理器（简化名别名）
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class MysqlShowDbsTerminalCommandHandler extends MysqlShowDatabasesTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return "dbs;";
    }
}
