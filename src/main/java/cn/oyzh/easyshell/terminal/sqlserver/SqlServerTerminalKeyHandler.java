package cn.oyzh.easyshell.terminal.sqlserver;

import cn.oyzh.easyshell.terminal.sqlserver.SqlServerTerminalPane;
import cn.oyzh.fx.terminal.key.TerminalKeyHandler;

/**
 * SqlServer终端按键处理器
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class SqlServerTerminalKeyHandler implements TerminalKeyHandler<SqlServerTerminalPane> {

    /**
     * 当前实例
     */
    public static final SqlServerTerminalKeyHandler INSTANCE = new SqlServerTerminalKeyHandler();

}
