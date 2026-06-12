package cn.oyzh.easyshell.query.redis;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.query.ShellQueryToken;
import redis.clients.jedis.Protocol;

import java.util.List;
import java.util.Optional;

/**
 * redis查询token
 *
 * @author oyzh
 * @since 2025/01/21
 */
public class ShellRedisQueryToken extends ShellQueryToken {

    /**
     * 输入
     */
    private String input;

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public boolean isPossibilityKeyword() {
        return this.getToken() == null || this.getToken() == ' ';
    }

    public boolean isPossibilityParam() {
        return this.getToken() != null && this.getToken() == ' ';
    }

    public boolean isPossibilityKey() {
        if (this.getToken() != null && this.getToken() == ' ') {
            if (StringUtil.count(this.input, " ") > 1) {
                return false;
            }
            List<Protocol.Command> commands = ShellRedisQueryUtil.keyCommands();
            //            for (Protocol.Command command : commands) {
            //                if (StringUtil.startWithIgnoreCase(this.input, command.toString())) {
            //                    return true;
            //                }
            //            }
            Optional<Protocol.Command> optional = commands.parallelStream()
                    .filter(f -> StringUtil.startWithIgnoreCase(this.input, f.toString()))
                    .findAny();
            return optional.isPresent();
        }
        return false;
    }
}
