package cn.oyzh.easyshell.dto.redis;

import cn.oyzh.common.Const;
import cn.oyzh.common.util.StringUtil;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.resps.Slowlog;

/**
 * redis慢查日志项目
 *
 * @author oyzh
 * @since 2023/8/1
 */
public class ShellRedisSlowlogItem {

    /** 获取日志id */
    public long getLogId() {
        return logId;
    }

    /** 设置日志id */
    public void setLogId(long logId) {
        this.logId = logId;
    }

    /** 获取指令 */
    public String getCommand() {
        return command;
    }

    /** 设置指令 */
    public void setCommand(String command) {
        this.command = command;
    }

    /** 获取发生时间 */
    public String getTimeStamp() {
        return timeStamp;
    }

    /** 设置发生时间 */
    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    /** 获取客户端地址 */
    public String getClientHost() {
        return clientHost;
    }

    /** 获取客户端名称 */
    public String getClientName() {
        return clientName;
    }

    /** 设置客户端名称 */
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    /** 获取耗时 */
    public long getExecutionTime() {
        return executionTime;
    }

    /** 设置耗时 */
    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }

    /**
     * id
     */
    private long logId;

    /**
     * 指令
     */
    private String command;

    /**
     * 发生时间
     */
    private String timeStamp;

    /**
     * 客户端地址
     */
    private String clientHost;

    /**
     * 客户端名称
     */
    private String clientName;

    /**
     * 耗时
     */
    private long executionTime;

    /** 设置客户端地址 */
    public void setClientHost(String clientHost) {
        this.clientHost = clientHost;
    }

    /** 设置客户端地址（从地址端口对象） */
    public void setClientHost(HostAndPort hostAndPort) {
        this.clientHost = hostAndPort == null ? "未知" : hostAndPort.toString();
    }

    /**
     * 从慢查日志生成
     *
     * @param slowlog 慢查日志
     * @return 慢查日志键
     */
    public static ShellRedisSlowlogItem from(Slowlog slowlog) {
        ShellRedisSlowlogItem item = new ShellRedisSlowlogItem();
        item.setLogId(slowlog.getId());
        item.setClientName(slowlog.getClientName());
        item.setExecutionTime(slowlog.getExecutionTime());
        item.setClientHost(slowlog.getClientIpPort());
        item.setCommand(StringUtil.join(" ", slowlog.getArgs()));
        item.setTimeStamp(Const.DATE_FORMAT.format(slowlog.getTimeStamp() * 1000));
        return item;
    }
}
