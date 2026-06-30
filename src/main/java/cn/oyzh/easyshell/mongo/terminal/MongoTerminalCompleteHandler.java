package cn.oyzh.easyshell.mongo.terminal;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.util.TextUtil;
import cn.oyzh.easyshell.mongo.script.MongoScriptUtil;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.command.TerminalCommandHandler;
import cn.oyzh.fx.terminal.complete.BaseTerminalCompleteHandler;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 终端提示器
 *
 * @author oyzh
 * @since 2023/7/24
 */
public class MongoTerminalCompleteHandler extends BaseTerminalCompleteHandler<MongoTerminalPane> {

    private MongoTerminalCommandHandler<TerminalCommand> newCommandHandler(String name) {
        return new MongoTerminalCommandHandler<>() {

            @Override
            public TerminalExecuteResult execute(TerminalCommand command, MongoTerminalPane terminal) {
                String input = command.getCommand();
                return terminal.eval(input);
            }

            @Override
            public String commandName() {
                return name;
            }
        };
    }

    private static Pattern collectionPattern;

    private static Pattern collectionPattern() {
        if (collectionPattern == null) {
            String regex = "^db\\.getCollection\\(\"[^\"]*\"\\).*";
            collectionPattern = Pattern.compile(regex);
        }
        return collectionPattern;
    }

    @Override
    protected List<TerminalCommandHandler<?, ?>> findCommandHandlers(MongoTerminalPane terminal, String line) {
        List<TerminalCommandHandler<?, ?>> list = new ArrayList<>();
        if (line.isEmpty()) {
            list.add(this.newCommandHandler("db.getCollection(\"\")"));
        } else if (collectionPattern().matcher(line).matches()) {
            long count = StringUtil.count(line, ".");
            Set<String> set = MongoScriptUtil.collectionFuncions();
            if (count == 1) {
                for (String s : set) {
                    list.add(this.newCommandHandler(line + "." + s + "()"));
                }
            } else if (count == 2) {
                String str = line.substring(line.lastIndexOf(".") + 1);
                String cmd = line.substring(0, line.lastIndexOf(".") + 1);
                for (String s : set) {
                    if (StringUtil.containsIgnoreCase(s, str) && TextUtil.clacCorr(s, str) > 0.3) {
                        list.add(this.newCommandHandler(cmd + s + "()"));
                    }
                }
            }
        } else if (line.startsWith("db")) {
            long count = StringUtil.count(line, ".");
            Set<String> set = MongoScriptUtil.databaseFuncions();
            if (count == 0) {
                for (String s : set) {
                    list.add(this.newCommandHandler("db." + s + "()"));
                }
            } else if (count == 1) {
                String str = line.substring(line.indexOf(".") + 1);
                for (String s : set) {
                    if (StringUtil.containsIgnoreCase(s, str) && TextUtil.clacCorr(s, str) > 0.3) {
                        list.add(this.newCommandHandler("db." + s + "()"));
                    }
                }
            }
        } else {
            list = super.findCommandHandlers(terminal, line);
        }
        return list;
    }

    @Override
    public boolean completion(String line, MongoTerminalPane terminal) {
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
    public static final MongoTerminalCompleteHandler INSTANCE = new MongoTerminalCompleteHandler();

}
