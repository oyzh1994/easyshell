package cn.oyzh.easyshell.terminal.dameng;

import cn.oyzh.easyshell.terminal.dameng.DamengTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.dameng.DamengTerminalPane;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.command.TerminalCommandHandler;
import cn.oyzh.fx.terminal.complete.BaseTerminalCompleteHandler;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;

import java.util.ArrayList;
import java.util.List;

/**
 * 终端提示器
 *
 * @author oyzh
 * @since 2023/7/24
 */
public class DamengTerminalCompleteHandler extends BaseTerminalCompleteHandler<DamengTerminalPane> {

    private static final String[] SQL_KEYWORDS = {
            "SELECT", "FROM", "WHERE", "INSERT", "INTO", "VALUES", "UPDATE", "SET",
            "DELETE", "CREATE", "TABLE", "ALTER", "DROP", "INDEX", "VIEW",
            "DATABASE", "SHOW", "DATABASES", "TABLES", "COLUMNS", "USE",
            "JOIN", "LEFT", "RIGHT", "INNER", "OUTER", "ON", "AND", "OR", "NOT",
            "NULL", "IS", "LIKE", "IN", "BETWEEN", "ORDER", "BY", "GROUP",
            "HAVING", "LIMIT", "OFFSET", "AS", "DISTINCT", "COUNT", "SUM",
            "AVG", "MAX", "MIN", "DESC", "ASC", "PRIMARY", "KEY", "FOREIGN",
            "REFERENCES", "CASCADE", "DEFAULT", "UNIQUE", "CHECK", "NULL AS AUTO_INCREMENT",
            "VARCHAR", "INT", "BIGINT", "TEXT", "DATE", "DATETIME", "TIMESTAMP",
            "BOOLEAN", "FLOAT", "DOUBLE", "DECIMAL", "CHAR", "ENUM"
    };

    private DamengTerminalCommandHandler<TerminalCommand> newCommandHandler(String name) {
        return new DamengTerminalCommandHandler<>() {

            @Override
            public TerminalExecuteResult execute(TerminalCommand command, DamengTerminalPane terminal) {
                return terminal.eval(command.getCommand());
            }

            @Override
            public String commandName() {
                return name;
            }
        };
    }

    @Override
    protected List<TerminalCommandHandler<?, ?>> findCommandHandlers(DamengTerminalPane terminal, String line) {
        List<TerminalCommandHandler<?, ?>> list = new ArrayList<>();
        if (line.isEmpty()) {
            for (String keyword : SQL_KEYWORDS) {
                list.add(this.newCommandHandler(keyword));
            }
        } else {
            list = super.findCommandHandlers(terminal, line);
            if (list.isEmpty()) {
                String upperLine = line.toUpperCase();
                for (String keyword : SQL_KEYWORDS) {
                    if (keyword.startsWith(upperLine)) {
                        list.add(this.newCommandHandler(keyword));
                    }
                }
            }
        }
        return list;
    }

    @Override
    public boolean completion(String line, DamengTerminalPane terminal) {
        List<TerminalCommandHandler<?, ?>> handlers = this.findCommandHandlers(terminal, line);
        if (handlers.isEmpty()) {
            this.noMatch(line, terminal);
        } else if (handlers.size() == 1) {
            this.oneMatch(line, terminal, handlers.getFirst());
        } else {
            this.multiMatch(line, terminal, handlers);
        }
        return true;
    }

    /**
     * 当前实例
     */
    public static final DamengTerminalCompleteHandler INSTANCE = new DamengTerminalCompleteHandler();

}
