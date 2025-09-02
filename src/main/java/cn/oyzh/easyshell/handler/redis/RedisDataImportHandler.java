package cn.oyzh.easyshell.handler.redis;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.ArrayUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.redis.RedisClient;
import cn.oyzh.easyshell.redis.RedisKeyType;
import cn.oyzh.easyshell.redis.key.RedisHashValue;
import cn.oyzh.easyshell.redis.key.RedisKey;
import cn.oyzh.easyshell.redis.key.RedisListValue;
import cn.oyzh.easyshell.redis.key.RedisSetValue;
import cn.oyzh.easyshell.redis.key.RedisStreamValue;
import cn.oyzh.easyshell.redis.key.RedisZSetValue;
import cn.oyzh.easyshell.redis.RedisKeyUtil;
import cn.oyzh.store.file.FileColumns;
import cn.oyzh.store.file.FileHelper;
import cn.oyzh.store.file.FileReadConfig;
import cn.oyzh.store.file.FileRecord;
import cn.oyzh.store.file.TypeFileReader;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author oyzh
 * @since 2024/11/26
 */
public class RedisDataImportHandler extends DataHandler {

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public RedisClient getClient() {
        return client;
    }

    public void setClient(RedisClient client) {
        this.client = client;
    }

    public boolean isRetainTTL() {
        return retainTTL;
    }

    public void setRetainTTL(boolean retainTTL) {
        this.retainTTL = retainTTL;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public boolean isIgnoreExist() {
        return ignoreExist;
    }

    public void setIgnoreExist(boolean ignoreExist) {
        this.ignoreExist = ignoreExist;
    }

    public FileReadConfig getConfig() {
        return config;
    }

    public void setConfig(FileReadConfig config) {
        this.config = config;
    }

    /**
     * 文件格式
     */
    private String fileType;

    /**
     * 客户端
     */
    private RedisClient client;

    /**
     * 保留ttl
     */
    private boolean retainTTL;

    /**
     * 批量处理大小
     */
    private int batchSize = 50;

    /**
     * 存在时忽略
     */
    private boolean ignoreExist;

    /**
     * 导出配置
     */
    private FileReadConfig config = new FileReadConfig();

    /**
     * 执行导出
     *
     * @throws Exception 异常
     */
    public void doImport() throws Exception {
        this.message("Import Starting");
        FileColumns columns = new FileColumns();
        columns.addColumn("key", 0);
        columns.addColumn("value", 1);
        columns.addColumn("dbIndex", 2);
        columns.addColumn("type", 3);
        columns.addColumn("ttl", 4);
        // 获取写入器
        TypeFileReader reader = FileHelper.initReader(this.fileType, this.config, columns);
        if (reader != null) {
            try {
                while (true) {
                    this.checkInterrupt();
                    List<FileRecord> records = reader.readRecords(this.batchSize);
                    if (CollectionUtil.isEmpty(records)) {
                        break;
                    }
                    for (FileRecord record : records) {
                        String key = "";
                        try {
                            key = (String) record.get(0);
                            if (StringUtil.isBlank(key)) {
                                this.message("key:" + key + " is invalid");
                                this.processedSkip();
                                continue;
                            }
                            Number dbIndex = (Number) record.getValue(2, Integer.class);
                            if (dbIndex == null) {
                                this.message("dbIndex of key: " + key + " is invalid");
                                this.processedSkip();
                                continue;
                            }
                            // 获取数据
                            String value = (String) record.get(1);
                            String type = (String) record.get(3);
                            Number ttl = (Number) record.getValue(4, Long.class);
                            RedisKeyType keyType = RedisKeyType.valueOfType(type);
                            // 创建键
                            if (!this.client.exists(dbIndex.intValue(), key)) {
                                this.createKey(key, dbIndex.intValue(), keyType, value, ttl.longValue());
                                this.processedIncr();
                                this.message("key[ " + key + "] is not exists, create it");
                                continue;
                            }
                            // 跳过
                            if (this.ignoreExist) {
                                this.processedSkip();
                                this.message("key[ " + key + "] is exists, skip it");
                                continue;
                            }
                            // 更新
                            this.client.rename(dbIndex.intValue(), key, key + "_backup");
                            this.createKey(key, dbIndex.intValue(), keyType, value, ttl.longValue());
                            this.client.del(dbIndex.intValue(), key + "_backup");
                            this.processedIncr();
                            this.message("key[ " + key + "] is exists, update it");
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            this.processedDecr();
                            this.message("create key[" + key + "] failed");
                        }
                    }
                }
            } finally {
                reader.close();
                this.message("Imported From -> " + this.config.filePath());
            }
        } else {
            JulLog.error("未找到可用的读取器，文件类型:{}", this.fileType);
        }
        this.message("Import Finished");
    }

    public void charset(String charset) {
        if (StringUtil.isBlank(charset)) {
            this.config.charset(StandardCharsets.UTF_8.name());
        } else {
            this.config.charset(charset);
        }
    }

    public void filePath(String filePath) {
        this.config.filePath(filePath);
    }

    public void txtIdentifier(Character txtIdentifier) {
        this.config.txtIdentifier(txtIdentifier);
    }

    public void dataRowStarts(Integer dataRowStarts) {
        this.config.dataRowStarts(dataRowStarts);
    }

    /**
     * 创建键
     *
     * @param key     键
     * @param dbIndex db索引
     * @param type    类型
     * @param value   值
     * @param ttl     到期时间
     */
    private void createKey(String key, int dbIndex, RedisKeyType type, String value, Long ttl) {
        RedisKey redisKey = RedisKeyUtil.deserializeNode(type, value);
        if (redisKey == null) {
            JulLog.warn("redisKey is null");
            return;
        }
        if (redisKey.isStringKey()) {
            this.client.set(dbIndex, key, (String) redisKey.asStringValue().getValue());
        } else if (redisKey.isListKey()) {
            List<RedisListValue.RedisListRow> rows = redisKey.asListValue().getValue();
            String[] arr;
            if (CollectionUtil.isEmpty(rows)) {
                arr = new String[]{""};
            } else {
                List<String> strings = rows.parallelStream().map(RedisListValue.RedisListRow::getValue).collect(Collectors.toList());
                arr = ArrayUtil.toArray(strings, String.class);
            }
            this.client.lpush(dbIndex, key, arr);
        } else if (redisKey.isSetKey()) {
            List<RedisSetValue.RedisSetRow> rows = redisKey.asSetValue().getValue();
            String[] arr;
            if (CollectionUtil.isEmpty(rows)) {
                arr = new String[]{""};
            } else {
                List<String> strings = rows.parallelStream().map(RedisSetValue.RedisSetRow::getValue).collect(Collectors.toList());
                arr = ArrayUtil.toArray(strings, String.class);
            }
            this.client.sadd(dbIndex, key, arr);
        } else if (redisKey.isZSetKey()) {
            List<RedisZSetValue.RedisZSetRow> rows = redisKey.asZSetValue().getValue();
            Map<String, Double> scoreMembers;
            if (CollectionUtil.isEmpty(rows)) {
                scoreMembers = new HashMap<>();
            } else {
                scoreMembers = new HashMap<>();
                for (RedisZSetValue.RedisZSetRow row : rows) {
                    scoreMembers.put(row.getValue(), row.getScore());
                }
            }
            this.client.zadd(dbIndex, key, scoreMembers);
        } else if (redisKey.isHashKey()) {
            List<RedisHashValue.RedisHashRow> rows = redisKey.asHashValue().getValue();
            Map<String, String> hash;
            if (CollectionUtil.isEmpty(rows)) {
                hash = new HashMap<>();
            } else {
                hash = new HashMap<>();
                for (RedisHashValue.RedisHashRow row : rows) {
                    hash.put(row.getField(), row.getValue());
                }
            }
            this.client.hmset(dbIndex, key, hash);
        } else if (redisKey.isStreamKey()) {
            List<RedisStreamValue.RedisStreamRow> rows = redisKey.asStreamValue().getValue();
            if (CollectionUtil.isNotEmpty(rows)) {
                for (RedisStreamValue.RedisStreamRow row : rows) {
                    this.client.xadd(dbIndex, key, row.getStreamId(), row.getFields());
                }
            }
        }
        // 处理ttl
        if (ttl != null && this.retainTTL) {
            // 持久化
            if (ttl == -1) {
                this.client.persist(dbIndex, key);
            } else {// 设置ttl
                this.client.expire(dbIndex, key, ttl, null);
            }
        }
    }
}

