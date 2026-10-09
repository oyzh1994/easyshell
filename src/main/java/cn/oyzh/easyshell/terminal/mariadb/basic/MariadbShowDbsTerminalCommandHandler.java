package cn.oyzh.easyshell.terminal.mariadb.basic;

/**
 * MariaDB显示数据库命令处理器（简化名别名）
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class MariadbShowDbsTerminalCommandHandler extends MariadbShowDatabasesTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return "dbs;";
    }
}
