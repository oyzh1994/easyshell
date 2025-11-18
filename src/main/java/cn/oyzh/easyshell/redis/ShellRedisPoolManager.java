package cn.oyzh.easyshell.redis;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadLocalUtil;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.CollectionUtil;
import redis.clients.jedis.ConnectionPool;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @author oyzh
 * @since 2025/01/01
 */
public class ShellRedisPoolManager {

    public String getConnectName() {
        return connectName;
    }

    public void setConnectName(String connectName) {
        this.connectName = connectName;
    }

    public JedisPool getJedisPool() {
        return jedisPool;
    }

    public void setJedisPool(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    public JedisCluster getCluster() {
        return cluster;
    }

    public void setCluster(JedisCluster cluster) {
        this.cluster = cluster;
    }

    public byte getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(byte maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public byte getInitPoolSize() {
        return initPoolSize;
    }

    public void setInitPoolSize(byte initPoolSize) {
        this.initPoolSize = initPoolSize;
    }

    public List<ConnectionPool> getClusterPools() {
        return clusterPools;
    }

    public void setClusterPools(List<ConnectionPool> clusterPools) {
        this.clusterPools = clusterPools;
    }

    /**
     * 连接名称
     */
    private String connectName;

    /**
     * 连接池
     */
    private JedisPool jedisPool;

    /**
     * redis集群操作对象
     */
    private JedisCluster cluster;

    /**
     * 最大池上限
     * 默认16
     */
    private byte maxPoolSize = 16;

    /**
     * 最大池上限
     * 默认16
     */
    private byte initPoolSize = 3;

    /**
     * 资源集合
     */
    private final List<ShellRedisConn> resources = new CopyOnWriteArrayList<>();

    /**
     * cluster集群的主节点连接
     */
    private List<ConnectionPool> clusterPools;

    /**
     * 初始化资源
     */
    public void initResource() {
        try {
            // 创建数个待备用的
            if (this.jedisPool != null) {
                synchronized (this.resources) {
                    for (int i = 0; i < this.initPoolSize; i++) {
                        Jedis jedis = this.jedisPool.getResource();
                        jedis.select(i);
                        this.resources.add(new ShellRedisConn(jedis));
                    }
                }
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 初始化集群连接
     *
     * @param poolMap 连接列表
     */
    public void initClusterPool(Map<String, ConnectionPool> poolMap) {
        if (CollectionUtil.isNotEmpty(poolMap) && (this.clusterPools == null || !poolMap.values().containsAll(this.clusterPools))) {
            this.clusterPools = new ArrayList<>(poolMap.size());
            this.clusterPools.addAll(poolMap.values());
        }
    }

    /**
     * 是否有集群连接
     *
     * @return 结果
     */
    public boolean hasClusterPool() {
        return CollectionUtil.isNotEmpty(this.clusterPools);
    }

    /**
     * 获取连接
     *
     * @return Jedis
     */
    public Jedis getResource(Integer dbIndex) {
        try {
            // 从已有连接里面找
            if (CollectionUtil.isNotEmpty(this.resources)) {
                List<ShellRedisConn> list;
                synchronized (this.resources) {
                    list = this.resources.parallelStream().filter(i -> !i.isUsing()).collect(Collectors.toList());
                }
                Collections.shuffle(list);
                for (ShellRedisConn resource : list) {
                    if (resource.isUsing()) {
                        continue;
                    }
                    // 随便返回一个
                    if (dbIndex == null) {
                        resource.setUsing(true);
                        return resource.getJedis();
                    }
                    // 寻找db一样的连接，没有找到就继续
                    if (resource.getDB() == dbIndex) {
                        resource.setUsing(true);
                        return resource.getJedis();
                    }
                }
            }
            // 创建一个新的
            if (this.jedisPool != null) {
                Jedis jedis = this.jedisPool.getResource();
                synchronized (this.resources) {
                    this.resources.add(new ShellRedisConn(jedis, true));
                }
                return jedis;
            }
        } finally {
            ThreadLocalUtil.setVal("connectName", this.connectName);
        }
        return null;
    }

    /**
     * 返还连接
     *
     * @param jedis 连接
     */
    public void returnResource(Jedis jedis) {
        if (jedis != null) {
            // 寻找连接，标记为未使用
            ShellRedisConn conn = null;
            for (ShellRedisConn redisConn : this.resources) {
                if (redisConn.getJedis() == jedis) {
                    redisConn.setUsing(false);
                    conn = redisConn;
                    break;
                }
            }
            // 如果没有超过限制，则不回收
            if (this.resources.size() <= this.maxPoolSize) {
                return;
            }
            if (conn != null) {
                // 从列表移除
                this.resources.remove(conn);
                // 执行资源返回
                ShellRedisConn finalConn = conn;
                ThreadUtil.start(() -> this.doReturnResource(finalConn.getJedis()));
            }
        }
    }

    /**
     * 执行返回连接
     *
     * @param jedis 连接
     */
    private void doReturnResource(Jedis jedis) {
        if (jedis != null && this.jedisPool != null) {
            try {
                this.jedisPool.returnResource(jedis);
            } catch (Exception ex) {
                ex.printStackTrace();
                JulLog.warn("doReturnResource error", ex);
            }
        }
    }

    /**
     * 销毁
     */
    public void destroy() {
        // 清理一般连接
        if (CollectionUtil.isNotEmpty(this.resources)) {
            for (ShellRedisConn value : this.resources) {
                this.doReturnResource(value.getJedis());
            }
            this.resources.clear();
        }
        // 关闭连接池
        if (this.jedisPool != null && !this.jedisPool.isClosed()) {
            this.jedisPool.close();
            this.jedisPool = null;
        }
        // 关闭集群连接
        if (this.cluster != null) {
            this.cluster.close();
            this.cluster = null;
        }
        // 清理哨兵连接
        if (CollectionUtil.isNotEmpty(this.clusterPools)) {
            for (ConnectionPool pool : this.clusterPools) {
                pool.close();
            }
            this.clusterPools.clear();
            this.clusterPools = null;
        }
    }
}
