package cn.oyzh.easyshell.data.redis.handler;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.redis.ShellRedisClient;
import cn.oyzh.easyshell.redis.ShellRedisKeyUtil;
import cn.oyzh.easyshell.redis.key.ShellRedisKey;
import cn.oyzh.easyshell.util.redis.ShellRedisUtil;
import cn.oyzh.fx.db.data.handler.DataTransportHandler;

import java.util.List;
import java.util.Set;

/**
 * redis数据传输处理器
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellRedisDataTransportHandler extends DataTransportHandler {

    /**
     * 获取来源客户端
     *
     * @return 来源客户端
     */
    public ShellRedisClient getSourceClient() {
        return sourceClient;
    }

    /**
     * 设置来源客户端
     *
     * @param sourceClient 来源客户端
     */
    public void setSourceClient(ShellRedisClient sourceClient) {
        this.sourceClient = sourceClient;
    }

    /**
     * 获取目标客户端
     *
     * @return 目标客户端
     */
    public ShellRedisClient getTargetClient() {
        return targetClient;
    }

    /**
     * 设置目标客户端
     *
     * @param targetClient 目标客户端
     */
    public void setTargetClient(ShellRedisClient targetClient) {
        this.targetClient = targetClient;
    }

    /**
     * 获取存在时处理策略
     *
     * @return 存在时处理策略
     */
    public String getExistsPolicy() {
        return existsPolicy;
    }

    /**
     * 设置存在时处理策略
     *
     * @param existsPolicy 存在时处理策略
     */
    public void setExistsPolicy(String existsPolicy) {
        this.existsPolicy = existsPolicy;
    }

    /**
     * 获取来源数据库索引
     *
     * @return 来源数据库索引
     */
    public int getSourceDatabase() {
        return sourceDatabase;
    }

    /**
     * 设置来源数据库索引
     *
     * @param sourceDatabase 来源数据库索引
     */
    public void setSourceDatabase(int sourceDatabase) {
        this.sourceDatabase = sourceDatabase;
    }

    /**
     * 获取目标数据库索引
     *
     * @return 目标数据库索引
     */
    public int getTargetDatabase() {
        return targetDatabase;
    }

    /**
     * 设置目标数据库索引
     *
     * @param targetDatabase 目标数据库索引
     */
    public void setTargetDatabase(int targetDatabase) {
        this.targetDatabase = targetDatabase;
    }

    /**
     * 获取键类型
     *
     * @return 键类型
     */
    public List<String> getKeyTypes() {
        return keyTypes;
    }

    /**
     * 设置键类型
     *
     * @param keyTypes 键类型
     */
    public void setKeyTypes(List<String> keyTypes) {
        this.keyTypes = keyTypes;
    }

    /**
     * 是否保留ttl
     *
     * @return 结果
     */
    public boolean isRetainTTL() {
        return retainTTL;
    }

    /**
     * 设置是否保留ttl
     *
     * @param retainTTL 是否保留ttl
     */
    public void setRetainTTL(boolean retainTTL) {
        this.retainTTL = retainTTL;
    }

    /**
     * 获取查询模式
     *
     * @return 查询模式
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * 设置查询模式
     *
     * @param pattern 查询模式
     */
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    /**
     * 来源客户端
     */
    protected ShellRedisClient sourceClient;

    /**
     * 目标客户端
     */
    protected ShellRedisClient targetClient;

    /**
     * 节点存在时处理策略
     * 0 跳过
     * 1 更新
     */
    private String existsPolicy;

    /**
     * 来源数据库
     */
    private int sourceDatabase;

    /**
     * 目标数据库
     */
    private int targetDatabase;

    /**
     * 键类型
     */
    private List<String> keyTypes;

    /**
     * 保留ttl
     */
    private boolean retainTTL;

    /**
     * 查询模式
     */
    private String pattern = "*";

    @Override
    public void doTransport() throws Exception {
        this.message("Transport Starting");
        Set<String> allKeys = this.sourceClient.allKeys(this.sourceDatabase, this.pattern);
        this.doTransport(this.sourceDatabase, this.targetDatabase, allKeys);
        this.message("Transport Finished");
    }

    /**
     * 执行传输
     *
     * @param fromDBIndex   来源数据库索引
     * @param targetDBIndex 目标数据库索引
     * @param keys          键列表
     * @throws InterruptedException 中断异常
     */
    private void doTransport(int fromDBIndex, int targetDBIndex, Set<String> keys) throws InterruptedException {
        for (String key : keys) {
            // 检查操作
            this.checkInterrupt();
            // // 被过滤
            // if (ShellRedisKeyUtil.isFiltered(key, this.filters)) {
            //     this.message("key[ " + key + "] is filtered, skip it");
            //     this.processedSkip();
            //     continue;
            // }
            // 获取键
            ShellRedisKey redisKey = ShellRedisKeyUtil.getKey(fromDBIndex, key, this.retainTTL, true, this.sourceClient);
            // 获取键失败
            if (redisKey == null) {
                this.message("key[ " + key + "] does not exist");
                this.processedIncr();
                continue;
            }
            // 键被排除
            if (ShellRedisUtil.isExclude(this.keyTypes, redisKey)) {
                this.message("key[ " + key + "] is exclude, skip it");
                this.processedSkip();
                continue;
            }
            // 键不存在，创建
            if (!this.targetClient.exists(targetDBIndex, key)) {
                this.createKey(redisKey, targetDBIndex);
                this.processedIncr();
                this.message("key[ " + key + "] is not exists, create it");
                continue;
            }
            // 键存在，跳过
            if (StringUtil.equals(this.existsPolicy, "0")) {
                this.processedSkip();
                this.message("key[ " + key + "] is exists, skip it");
                continue;
            }
            // 键存在，更新
            this.targetClient.del(targetDBIndex, key);
            this.createKey(redisKey, targetDBIndex);
            this.processedIncr();
            this.message("key[ " + key + "] is exists, update it");
        }
    }

    /**
     * 创建键
     *
     * @param redisKey      redis键
     * @param targetDBIndex 目标数据库索引
     */
    private void createKey(ShellRedisKey redisKey, int targetDBIndex) {
        if (redisKey != null) {
            ShellRedisKeyUtil.createKey(redisKey, targetDBIndex, this.targetClient);
            String key = redisKey.getKey();
            Long ttl = redisKey.getTtl();
            if (ttl != null && this.retainTTL) {
                if (ttl >= 0) {
                    this.targetClient.expire(targetDBIndex, key, ttl, null);
                } else if (ttl == -1) {
                    this.targetClient.persist(targetDBIndex, key);
                }
            }
        }
    }
}

