package cn.oyzh.easyshell.command;

import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.json.JSONUtil;
import cn.oyzh.common.util.StringUtil;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * redis命令工具类
 *
 * @author oyzh
 * @since 2024/05/29
 */

public class RedisCommandUtil {

    /**
     * 版本缓存
     */
    private final static List<RedisCommand> COMMANDS = new ArrayList<>();

    static {
        try {
            URL url = RedisCommand.class.getResource("/redis_commands.json");
            String json = FileUtil.readString(url, StandardCharsets.UTF_8);
            if (StringUtil.isNotBlank(json)) {
                COMMANDS.addAll(JSONUtil.toBeanList(json, RedisCommand.class));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static List<RedisCommand> getCommands() {
        return COMMANDS;
    }

    /**
     * 检查指令是否支持
     *
     * @param command 指令
     */
    public static RedisCommand getCommand(String command) {
        if (command != null) {
            for (RedisCommand redisCommand : COMMANDS) {
                if (StringUtil.equalsIgnoreCase(redisCommand.getCommand(), command)) {
                    return redisCommand;
                }
            }
        }
        return null;
    }

    /**
     * 检查指令是否支持
     *
     * @param command 指令
     */
    public static String getCommandDesc(String command) {
        RedisCommand redisCommand = getCommand(command);
        return redisCommand == null || redisCommand.getDesc() == null ? "" : redisCommand.getDesc();
    }

    /**
     * 检查指令是否支持
     *
     * @param command 指令
     */
    public static String getCommandArgs(String command) {
        RedisCommand redisCommand = getCommand(command);
        return redisCommand == null || redisCommand.getArgs() == null ? "" : redisCommand.getArgs();
    }

    /**
     * 检查指令是否支持
     *
     * @param command 指令
     */
    public static String getCommandAvailable(String command) {
        RedisCommand redisCommand = getCommand(command);
        return redisCommand == null || redisCommand.getAvailable() == null ? "" : redisCommand.getAvailable();
    }

}
