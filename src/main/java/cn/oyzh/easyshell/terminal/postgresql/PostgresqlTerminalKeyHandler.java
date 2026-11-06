package cn.oyzh.easyshell.terminal.postgresql;

import cn.oyzh.easyshell.terminal.postgresql.PostgresqlTerminalPane;
import cn.oyzh.fx.terminal.key.TerminalKeyHandler;

/**
 * PostgreSQL终端按键处理器
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class PostgresqlTerminalKeyHandler implements TerminalKeyHandler<PostgresqlTerminalPane> {

    /**
     * 当前实例
     */
    public static final PostgresqlTerminalKeyHandler INSTANCE = new PostgresqlTerminalKeyHandler();

}
