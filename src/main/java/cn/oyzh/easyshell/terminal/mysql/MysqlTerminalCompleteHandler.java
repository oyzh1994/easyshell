package cn.oyzh.easyshell.terminal.mysql;

import cn.oyzh.fx.db.util.DBUtil;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.command.TerminalCommandHandler;
import cn.oyzh.fx.terminal.complete.BaseTerminalCompleteHandler;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;

import java.util.ArrayList;
import java.util.List;

/**
 * mysql终端提示器
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class MysqlTerminalCompleteHandler extends BaseTerminalCompleteHandler<MysqlTerminalPane> {

    /**
     * 创建命令处理器
     *
     * @param name 命令名称
     * @return 命令处理器
     */
    private MysqlTerminalCommandHandler<TerminalCommand> newCommandHandler(String name) {
        return new MysqlTerminalCommandHandler<>() {

            @Override
            public TerminalExecuteResult execute(TerminalCommand command, MysqlTerminalPane terminal) {
                return terminal.eval(command.getCommand());
            }

            @Override
            public String commandName() {
                return name;
            }
        };
    }

    @Override
    protected List<TerminalCommandHandler<?, ?>> findCommandHandlers(MysqlTerminalPane terminal, String line) {
        List<TerminalCommandHandler<?, ?>> list = new ArrayList<>();
        if (line.isEmpty()) {
            for (String keyword : DBUtil.SQL_KEYWORDS) {
                list.add(this.newCommandHandler(keyword));
            }
        } else {
            list = super.findCommandHandlers(terminal, line);
            if (list.isEmpty()) {
                String upperLine = line.toUpperCase();
                for (String keyword : DBUtil.SQL_KEYWORDS) {
                    if (keyword.startsWith(upperLine)) {
                        list.add(this.newCommandHandler(keyword));
                    }
                }
            }
        }
        return list;
    }

    @Override
    public boolean completion(String line, MysqlTerminalPane terminal) {
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
    public static final MysqlTerminalCompleteHandler INSTANCE = new MysqlTerminalCompleteHandler();

}
