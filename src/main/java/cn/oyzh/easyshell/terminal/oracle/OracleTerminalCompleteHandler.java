package cn.oyzh.easyshell.terminal.oracle;

import cn.oyzh.easyshell.terminal.oracle.OracleTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.oracle.OracleTerminalPane;
import cn.oyzh.fx.db.util.DBUtil;
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
 * @since 2026-06-16
 */
public class OracleTerminalCompleteHandler extends BaseTerminalCompleteHandler<OracleTerminalPane> {

//    /**
//     * 创建指定名称的命令处理器
//     *
//     * @param name 命令名称
//     * @return 命令处理器
//     */
//    private OracleTerminalCommandHandler<TerminalCommand> newCommandHandler(String name) {
//        return new OracleTerminalCommandHandler<>() {
//
//            @Override
//            public TerminalExecuteResult execute(TerminalCommand command, OracleTerminalPane terminal) {
//                return terminal.eval(command.getCommand());
//            }
//
//            @Override
//            public String commandName() {
//                return name;
//            }
//        };
//    }
//
//    @Override
//    protected List<TerminalCommandHandler<?, ?>> findCommandHandlers(OracleTerminalPane terminal, String line) {
//        List<TerminalCommandHandler<?, ?>> list = new ArrayList<>();
//        if (line.isEmpty()) {
//            for (String keyword : DBUtil.SQL_KEYWORDS) {
//                list.add(this.newCommandHandler(keyword));
//            }
//        } else {
//            list = super.findCommandHandlers(terminal, line);
//            if (list.isEmpty()) {
//                String upperLine = line.toUpperCase();
//                for (String keyword : DBUtil.SQL_KEYWORDS) {
//                    if (keyword.startsWith(upperLine)) {
//                        list.add(this.newCommandHandler(keyword));
//                    }
//                }
//            }
//        }
//        return list;
//    }

//    @Override
//    public boolean completion(String line, OracleTerminalPane terminal) {
//        List<TerminalCommandHandler<?, ?>> handlers = this.findCommandHandlers(terminal, line);
//        if (handlers.isEmpty()) {
//            this.noMatch(line, terminal);
//        } else if (handlers.size() == 1) {
//            this.oneMatch(line, terminal, handlers.getFirst());
//        } else {
//            this.multiMatch(line, terminal, handlers);
//        }
//        return true;
//    }

    /**
     * 当前实例
     */
    public static final OracleTerminalCompleteHandler INSTANCE = new OracleTerminalCompleteHandler();

}
