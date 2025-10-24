package cn.oyzh.easyshell.terminal.redis;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.util.redis.ShellRedisCommandUtil;
import cn.oyzh.fx.terminal.command.BaseTerminalCommandHandler;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;
import redis.clients.jedis.CommandObject;
import redis.clients.jedis.commands.ProtocolCommand;
import redis.clients.jedis.json.JsonProtocol;
import redis.clients.jedis.util.SafeEncoder;

/**
 * @author oyzh
 * @since 2023/7/31
 */
public abstract class RedisTerminalCommandHandler<C extends TerminalCommand> extends BaseTerminalCommandHandler<C, RedisTerminalPane> {

    @Override
    public TerminalExecuteResult execute(C command, RedisTerminalPane terminal) {
        TerminalExecuteResult result = new TerminalExecuteResult();
        try {
            CommandObject<Object> object = RedisTerminalUtil.getCommand(this.getCommandType(), command);
            Object obj = terminal.getClient().execCommand(terminal.getDbIndex(), object);
            result.setResult(RedisTerminalUtil.formatOut(obj));
        } catch (Exception ex) {
            ex.printStackTrace();
            result.setException(ex);
        }
        return result;
    }

    @Override
    public String commandName() {
        // if (this.getCommandType() == null) {
        //     return "";
        // }
        // if(this.getCommandType() instanceof Protocol.Command command){
        //     return command.name();
        // }
        if(this.getCommandType() instanceof JsonProtocol.JsonCommand command){
            return SafeEncoder.encode(command.getRaw());
        }
        if (this.getCommandType() instanceof Enum<?> myEnum) {
            return myEnum.name();
        }
        return "";
    }

    @Override
    public String commandHelp(RedisTerminalPane terminal) {
        StringBuilder builder = new StringBuilder();
        if (StringUtil.isNotBlank(this.commandArg())) {
            builder.append(" ").append(this.commandArg());
        }
        return builder.isEmpty() ? "" : builder.substring(1);
    }

    @Override
    public String commandArg() {
        return ShellRedisCommandUtil.getCommandArgs(this.commandFullName());
    }

    @Override
    public String commandDesc() {
        return ShellRedisCommandUtil.getCommandDesc(this.commandFullName());
    }

    @Override
    public String commandSupportedVersion() {
        return ShellRedisCommandUtil.getCommandAvailable(this.commandFullName());
    }

    public abstract ProtocolCommand getCommandType();

}
