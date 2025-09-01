package cn.oyzh.easyshell.terminal.redis;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.util.TextUtil;
import cn.oyzh.easyshell.redis.RedisKeyType;
import cn.oyzh.easyshell.terminal.redis.RedisTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.redis.RedisTerminalPane;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.util.TerminalUtil;

import java.util.Set;

/**
 * @author oyzh
 * @since 2023/7/31
 */
public abstract class RedisNKeysTerminalCommandHandler<C extends TerminalCommand> extends RedisTerminalCommandHandler<C> {

    @Override
    public boolean completion(String line, RedisTerminalPane terminal) {
        String[] words = TerminalUtil.split(line);
        if (words.length >= 1) {
            String key = line.substring(this.commandFullName().length());
            String pattern = StringUtil.isBlank(key) ? "*" : key + "*";
            Set<String> keys = terminal.getClient().keys(null, pattern, this.getKeyType());
            if (CollectionUtil.isEmpty(keys)) {
                return false;
            }
            if (keys.size() == 1) {
                terminal.coverInput(words[0] + " " + CollectionUtil.getFirst(keys));
            } else {
                String textFormat = TextUtil.beautifyFormat(keys, 3, 0);
                terminal.outputByPrompt(textFormat);
                terminal.outputPrompt();
                terminal.output(line);
            }
            terminal.moveCaretEnd();
            return true;
        }
        return false;
    }

    protected RedisKeyType getKeyType() {
        return null;
    }
}
