package cn.oyzh.easyshell.terminal.oracle.basic;

/**
 * Oracle显示数据库命令处理器
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class OracleShowDbsTerminalCommandHandler extends OracleShowDatabasesTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return "dbs;";
    }
}
