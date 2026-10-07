package cn.oyzh.easyshell.terminal.dameng.basic;

/**
 * 达梦显示数据库命令处理器
 *
 * @author oyzh
 * @since 2024-12-30
 */
public class DamengShowDbsTerminalCommandHandler extends DamengShowDatabasesTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return "dbs;";
    }
}
