package cn.oyzh.easyshell.terminal.h2.basic;

/**
 * H2显示数据库命令处理器（简化名别名）
 *
 * @author oyzh
 * @since 2026-11-04
 */
public class H2ShowDbsTerminalCommandHandler extends H2ShowDatabasesTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return "dbs;";
    }
}
