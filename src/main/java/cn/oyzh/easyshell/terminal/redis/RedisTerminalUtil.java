package cn.oyzh.easyshell.terminal.redis;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import redis.clients.jedis.BuilderFactory;
import redis.clients.jedis.CommandArguments;
import redis.clients.jedis.CommandObject;
import redis.clients.jedis.GeoCoordinate;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.util.SafeEncoder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * redis终端工具
 *
 * @author oyzh
 * @since 2023/7/26
 */

public class RedisTerminalUtil {

    /**
     * 格式化输出
     *
     * @param value 值
     * @return 结果
     */
    public static String formatOut(Object value) {
        if (value == null) {
            return "";
        }
        // if (value instanceof Collection) {
        //     return formatOut((Collection<?>) value);
        // }
        // if (value instanceof Boolean b) {
        //     return "\"" + (b ? "1" : "0") + "\"";
        // }
        // return "\"" + value + "\"";
        Object o = SafeEncoder.encodeObject(value);
        if (o == null) {
            return "";
        }
        if (o instanceof Collection<?> collection) {
            return formatOut(collection);
        }
        return o.toString();
    }

    /**
     * 格式化输出
     *
     * @param values 值
     * @return 结果
     */
    public static String formatOut(Collection<?> values) {
        if (CollectionUtil.isEmpty(values)) {
            return "";
        }
        int index = 1;
        StringBuilder builder = new StringBuilder();
        for (Object value : values) {
            builder.append(index++).append(") ").append("\"").append(value).append("\"").append("\n");
        }
        return builder.toString();
    }

    /**
     * 格式化输出
     *
     * @param values 值
     * @return 结果
     */
    public static String formatOut(Map<?, ?> values) {
        if (CollectionUtil.isEmpty(values)) {
            return "";
        }
        List<Object> list = new ArrayList<>(values.size());
        for (Map.Entry<?, ?> entry : values.entrySet()) {
            list.add(entry.getKey());
            list.add(entry.getValue());
        }
        return formatOut(list);
    }
//
//    /**
//     * 格式化输出
//     *
//     * @param values 值
//     * @return 结果
//     */
//    public static String formatOutStream(List<StreamEntry> values) {
//        if (CollectionUtil.isEmpty(values)) {
//            return "";
//        }
//        int index = 1;
//        StringBuilder builder = new StringBuilder();
//        for (Object value : values) {
//            builder.append(index++).append(") ").append("\"").append(value).append("\"").append("\n");
//        }
//        return builder.toString();
//    }

    /**
     * 格式化输出
     *
     * @param coordinates 坐标值
     * @return 结果
     */
    public static String formatOut(List<GeoCoordinate> coordinates) {
        if (CollectionUtil.isEmpty(coordinates)) {
            return "";
        }
        int index = 1;
        StringBuilder builder = new StringBuilder();
        for (GeoCoordinate value : coordinates) {
            builder.append(index++).append(") 1)")
                    .append("\"").append(value.getLongitude()).append("\"").append("\n")
                    .append(" ".repeat(index / 10)).append("   2)")
                    .append("\"").append(value.getLatitude()).append("\"").append("\n");
        }
        return builder.toString();
    }

    /**
     * 获取命令
     *
     * @param command         命令
     * @param terminalCommand 终端命令
     * @return 命令对象
     */
    public static CommandObject<Object> getCommand(Protocol.Command command, TerminalCommand terminalCommand) {
        CommandArguments arguments = new CommandArguments(command);
        arguments.addObjects(terminalCommand.argsList());
        return new CommandObject<>(arguments, BuilderFactory.RAW_OBJECT);
    }

    /**
     * 获取命令
     *
     * @param command 命令
     * @param args    参数
     * @return 命令对象
     */
    public static CommandObject<Object> getCommand(Protocol.Command command, String[] args) {
        CommandArguments arguments = new CommandArguments(command);
        if (args != null) {
            for (String arg : args) {
                arguments.add(arg);
            }
        }
        return new CommandObject<>(arguments, BuilderFactory.RAW_OBJECT);
    }

    /**
     * 获取命令
     *
     * @param command 命令
     * @param arg    参数
     * @return 命令对象
     */
    public static CommandObject<Object> getCommand(Protocol.Command command, String arg) {
        CommandArguments arguments = new CommandArguments(command);
        if (arg != null) {
            arguments.add(arg);
        }
        return new CommandObject<>(arguments, BuilderFactory.RAW_OBJECT);
    }
}
