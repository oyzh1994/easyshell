package cn.oyzh.easyshell.dto.redis;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import com.alibaba.fastjson2.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

/**
 * redis信息属性
 *
 * @author oyzh
 * @since 2023/08/01
 */
public class RedisInfoProp {

    /**
     * 属性列表
     */
    private Map<String, JSONObject> props;

    public Map<String, JSONObject> getProps() {
        return props;
    }

    /**
     * 解析数据
     *
     * @param str 数据
     */
    public void parse(String str) {
        if (StringUtil.isNotBlank(str)) {
            this.props = new HashMap<>();
            Stream<String> lines = str.lines();
            AtomicReference<String> currGroup = new AtomicReference<>();
            lines.forEach(l -> {
                if (l.startsWith("#")) {
                    String group = l.replace("# ", "").toLowerCase();
                    props.computeIfAbsent(group, k -> new JSONObject());
                    currGroup.set(group);
                } else if (l.contains(":")) {
                    int index = l.indexOf(":");
                    String propName = l.substring(0, index);
                    String propValue = l.substring(index + 1);
                    props.get(currGroup.get()).put(propName, propValue);
                }
            });
        }
    }

    /**
     * 获取属性
     *
     * @param group 分组
     * @return 属性
     */
    public JSONObject getProps(String group) {
        if (group != null && this.props != null) {
            return this.props.get(group.toLowerCase());
        }
        return null;
    }

    /**
     * 是否存在属性
     *
     * @param group 分组
     * @return 结果
     */
    public boolean hasProps(String group) {
        if (group != null && this.props != null) {
            return this.props.containsKey(group.toLowerCase());
        }
        return false;
    }

    /**
     * 获取单个属性
     *
     * @param group    分组
     * @param propName 属性名
     * @return 单个属性
     */
    public String getProp(String group, String propName) {
        if (propName != null) {
            JSONObject object = this.getProps(group);
            if (CollectionUtil.isNotEmpty(object)) {
                return object.getString(propName);
            }
        }
        return null;
    }

    /**
     * 获取单个int属性
     *
     * @param group    分组
     * @param propName 属性名
     * @return 单个int属性
     */
    public int getIntProp(String group, String propName) {
        if (propName != null) {
            JSONObject object = this.getProps(group);
            if (CollectionUtil.isNotEmpty(object)) {
                return object.getIntValue(propName);
            }
        }
        return -1;
    }

    /**
     * 获取单个Integer属性
     *
     * @param group    分组
     * @param propName 属性名
     * @return 单个Integer属性
     */
    public Integer getIntegerProp(String group, String propName) {
        if (propName != null) {
            JSONObject object = this.getProps(group);
            if (CollectionUtil.isNotEmpty(object)) {
                return object.getIntValue(propName);
            }
        }
        return null;
    }

    /**
     * 获取单个Long属性
     *
     * @param group    分组
     * @param propName 属性名
     * @return 单个Integer属性
     */
    public long getLongProp(String group, String propName) {
        if (propName != null) {
            JSONObject object = this.getProps(group);
            if (CollectionUtil.isNotEmpty(object)) {
                return object.getLong(propName);
            }
        }
        return -1L;
    }

    /**
     * 获取单个Double属性
     *
     * @param group    分组
     * @param propName 属性名
     * @return 单个Double属性
     */
    public Double getDoubleProp(String group, String propName) {
        if (propName != null) {
            JSONObject object = this.getProps(group);
            if (CollectionUtil.isNotEmpty(object)) {
                return object.getDouble(propName);
            }
        }
        return -1d;
    }

    /**
     * 获取redis版本号
     *
     * @return redis版本号
     */
    public String getRedisVersion() {
        return this.getProp("server", "redis_version");
    }

    /**
     * 获取已使用内存
     *
     * @return 已使用内存
     */
    public long getUsedMemory() {
        return this.getLongProp("memory", "used_memory");
    }

    /**
     * 获取已使用内存，可读模式
     *
     * @return 已使用内存，可读模式
     */
    public String getUsedMemoryHuman() {
        return this.getProp("memory", "used_memory_human");
    }

    /**
     * 获取客户端列表
     *
     * @return 客户端列表
     */
    public int getConnectedClients() {
        return this.getIntProp("clients", "connected_clients");
    }

    /**
     * 获取键命中数量
     *
     * @return 键命中数量
     */
    public long getKeyspaceHits() {
        return this.getLongProp("stats", "keyspace_hits");
    }

    /**
     * 获取键未命中数量
     *
     * @return 键未命中数量
     */
    public long getKeyspaceMisses() {
        return this.getLongProp("stats", "keyspace_misses");
    }

    /**
     * 获取运行时间天数
     *
     * @return 运行时间天数
     */
    public int getUptimeInDays() {
        return this.getIntProp("server", "uptime_in_days");
    }

    /**
     * 获取运行时间秒数
     *
     * @return 运行时间秒数
     */
    public long getUptimeInSeconds() {
        return this.getLongProp("server", "uptime_in_seconds");
    }

    /**
     * 获取命令处理数量
     *
     * @return 命令处理数量
     */
    public long getTotalCommandsProcessed() {
        return this.getLongProp("stats", "total_commands_processed");
    }

    /**
     * 获取总入网数据
     *
     * @return 总入网数据
     */
    public long getTotalNetInputBytes() {
        return this.getLongProp("stats", "total_net_input_bytes");
    }

    /**
     * 获取总出网数据
     *
     * @return 总出网数据
     */
    public long getTotalNetOutputBytes() {
        return this.getLongProp("stats", "total_net_output_bytes");
    }

    /**
     * 获取每秒入网数据
     *
     * @return 每秒入网数据
     */
    public Double getInstantaneousInputKbps() {
        return this.getDoubleProp("stats", "instantaneous_input_kbps");
    }

    /**
     * 获取每秒出网数据
     *
     * @return 每秒出网数据
     */
    public Double getInstantaneousOutputKbps() {
        return this.getDoubleProp("stats", "instantaneous_output_kbps");
    }

    /**
     * 获取每秒命令处理数据
     *
     * @return 每秒命令处理数据
     */
    public long getInstantaneousOpsPerSec() {
        return this.getLongProp("stats", "instantaneous_ops_per_sec");
    }

    /**
     * 获取当前副本角色
     *
     * @return 副本角色
     */
    public String getReplicationRole() {
        return this.getProp("replication", "role");
    }

    /**
     * 获取cluster开启状态
     *
     * @return cluster开启状态
     */
    public Integer getClusterEnabled() {
        return this.getIntegerProp("cluster", "cluster_enabled");
    }

    /**
     * 获取redis mode
     *
     * @return redis_mode
     */
    public String getRedisMode() {
        return this.getProp("server", "redis_mode");
    }

    /**
     * 获取键数量
     *
     * @return 键数量
     */
    public Long keyCount() {
        JSONObject object = this.getProps("keyspace");
        if (object == null) {
            return null;
        }
        long count = 0;
        for (Object value : object.values()) {
            String str = (String) value;
            String[] arr = str.split(",");
            for (String s : arr) {
                if (s.startsWith("keys=")) {
                    count += Long.parseLong(s.split("=")[1]);
                }
            }
        }
        return count;
    }

    /**
     * 获取当前master名称
     *
     * @return master名称
     */
    public String masterName() {
        String master0 = this.getProp("sentinel", "master0");
        if (master0 != null) {
            String[] arr = master0.split(",");
            for (String s : arr) {
                if (StringUtil.startWithIgnoreCase(s, "name")) {
                    return s.split("=")[1];
                }
            }
        }
        return null;
    }

    /**
     * 获取分组名称
     *
     * @return 分组名称
     */
    public Set<String> groups() {
        return this.props.keySet();
    }

    /**
     * 是否为空
     *
     * @return 结果
     */
    public boolean isEmpty() {
        return this.props == null || this.props.isEmpty();
    }
}
