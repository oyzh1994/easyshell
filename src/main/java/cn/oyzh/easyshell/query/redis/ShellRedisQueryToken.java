package cn.oyzh.easyshell.query.redis;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.db.query.DBQueryToken;
import redis.clients.jedis.Protocol;

import java.util.List;
import java.util.Optional;

/**
 * redis查询token
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellRedisQueryToken extends DBQueryToken {

    /**
     * 输入
     */
    private String input;

    /**
     * 获取输入
     *
     * @return 输入
     */
    public String getInput() {
        return input;
    }

    /**
     * 设置输入
     *
     * @param input 输入
     */
    public void setInput(String input) {
        this.input = input;
    }

    /**
     * 是否可能是关键字
     *
     * @return 结果
     */
    public boolean isPossibilityKeyword() {
        return this.getToken() == null || this.getToken() == ' ';
    }

    /**
     * 是否可能是参数
     *
     * @return 结果
     */
    public boolean isPossibilityParam() {
        return this.getToken() != null && this.getToken() == ' ';
    }

    /**
     * 是否可能是键
     *
     * @return 结果
     */
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
