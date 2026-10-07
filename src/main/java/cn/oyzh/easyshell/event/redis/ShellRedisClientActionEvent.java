package cn.oyzh.easyshell.event.redis;

import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import redis.clients.jedis.CommandArguments;
import redis.clients.jedis.args.Rawable;

/**
 * redis客户端操作事件
 *
 * @author oyzh
 * @since 2025-01-01
 */
public class ShellRedisClientActionEvent extends Event<String> implements EventFormatter {

    /**
     * 命令参数
     */
    private CommandArguments arguments;

    public CommandArguments getArguments() {
        return arguments;
    }

    public void setArguments(CommandArguments arguments) {
        this.arguments = arguments;
    }

    @Override
    public String eventFormat() {
        if (this.data() == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(this.data());
        sb.append(" >");
        for (Rawable argument : this.arguments) {
            sb.append(" ").append(new String(argument.getRaw()));
        }
        return sb.toString();
    }
}
