package cn.oyzh.easyshell.terminal.postgresql;

import cn.oyzh.easyshell.terminal.postgresql.PostgresqlTerminalPane;
import cn.oyzh.fx.terminal.mouse.TerminalMouseHandler;

/**
 * PostgreSQL终端鼠标处理器
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class PostgresqlTerminalMouseHandler implements TerminalMouseHandler<PostgresqlTerminalPane> {

    /**
     * 当前实例
     */
    public static final PostgresqlTerminalMouseHandler INSTANCE = new PostgresqlTerminalMouseHandler();

}
