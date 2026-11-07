package cn.oyzh.easyshell.util.valkey;

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
 * @since 2025-09-01
 */
public class ShellValkeyCommandUtil {

    /**
     * 版本缓存
     */
    private final static List<ShellValkeyCommand> COMMANDS = new ArrayList<>();

    static {
        try {
            URL url = ShellValkeyCommand.class.getResource("/valkey_commands.json");
            String json = FileUtil.readString(url, StandardCharsets.UTF_8);
            if (StringUtil.isNotBlank(json)) {
                COMMANDS.addAll(JSONUtil.toList(json, ShellValkeyCommand.class));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static List<ShellValkeyCommand> getCommands() {
        return COMMANDS;
    }

    /**
     * 检查指令是否支持
     *
     * @param command 指令
     */
    public static ShellValkeyCommand getCommand(String command) {
        if (command != null) {
            for (ShellValkeyCommand redisCommand : COMMANDS) {
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
        ShellValkeyCommand redisCommand = getCommand(command);
        return redisCommand == null || redisCommand.getDesc() == null ? "" : redisCommand.getDesc();
    }

    /**
     * 检查指令是否支持
     *
     * @param command 指令
     */
    public static String getCommandArgs(String command) {
        ShellValkeyCommand redisCommand = getCommand(command);
        return redisCommand == null || redisCommand.getArgs() == null ? "" : redisCommand.getArgs();
    }

    /**
     * 检查指令是否支持
     *
     * @param command 指令
     */
    public static String getCommandAvailable(String command) {
        ShellValkeyCommand redisCommand = getCommand(command);
        return redisCommand == null || redisCommand.getAvailable() == null ? "" : redisCommand.getAvailable();
    }

}
