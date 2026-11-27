package cn.oyzh.easyshell.data.redis.handler;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.ArrayUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.redis.ShellRedisClient;
import cn.oyzh.easyshell.redis.ShellRedisKeyType;
import cn.oyzh.easyshell.redis.ShellRedisKeyUtil;
import cn.oyzh.easyshell.redis.key.ShellRedisHashValue;
import cn.oyzh.easyshell.redis.key.ShellRedisKey;
import cn.oyzh.easyshell.redis.key.ShellRedisListValue;
import cn.oyzh.easyshell.redis.key.ShellRedisSetValue;
import cn.oyzh.easyshell.redis.key.ShellRedisStreamValue;
import cn.oyzh.easyshell.redis.key.ShellRedisZSetValue;
import cn.oyzh.fx.db.data.handler.DataImportHandler;
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
 * redis数据导入处理器
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellRedisDataImportHandler extends DataImportHandler {

    /**
     * 获取文件格式
     *
     * @return 文件格式
     */
    public String getFileType() {
        return fileType;
    }

    /**
     * 设置文件格式
     *
     * @param fileType 文件格式
     */
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    /**
     * 获取客户端
     *
     * @return 客户端
     */
    public ShellRedisClient getClient() {
        return client;
    }

    /**
     * 设置客户端
     *
     * @param client 客户端
     */
    public void setClient(ShellRedisClient client) {
        this.client = client;
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
     * 获取批量处理大小
     *
     * @return 批量处理大小
     */
    public int getBatchSize() {
        return batchSize;
    }

    /**
     * 设置批量处理大小
     *
     * @param batchSize 批量处理大小
     */
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    /**
     * 是否忽略已存在
     *
     * @return 结果
     */
    public boolean isIgnoreExist() {
        return ignoreExist;
    }

    /**
     * 设置是否忽略已存在
     *
     * @param ignoreExist 是否忽略已存在
     */
    public void setIgnoreExist(boolean ignoreExist) {
        this.ignoreExist = ignoreExist;
    }

    /**
     * 获取导入配置
     *
     * @return 导入配置
     */
    public FileReadConfig getConfig() {
        return config;
    }

    /**
     * 设置导入配置
     *
     * @param config 导入配置
     */
    public void setConfig(FileReadConfig config) {
        this.config = config;
    }

    /**
     * 文件格式
     */
    private String fileType;

    /**
     * 数据库索引
     */
    private Integer dbIndex;

    /**
     * 客户端
     */
    private ShellRedisClient client;

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

    @Override
    public void doImport() throws Exception {
        this.message("Import Starting");
        FileColumns columns = new FileColumns();
        columns.addColumn("key", 0);
        columns.addColumn("value", 1);
        columns.addColumn("dbIndex", 2);
        columns.addColumn("type", 3);
        columns.addColumn("ttl", 4);
        // 获取写入器
        TypeFileReader<FileRecord> reader = FileHelper.initReader(this.fileType, this.config, columns);
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
                        String backupKey = null;
                        Integer dbIndex = null;
                        try {
                            // 每条记录都检查中断，避免一批内继续导入剩余记录
                            this.checkInterrupt();
                            key = this.recordValue(record, 0);
                            if (StringUtil.isBlank(key)) {
                                this.message("key:" + key + " is invalid");
                                this.processedSkip();
                                continue;
                            }
                            if (this.dbIndex == null) {
                                dbIndex = this.parseInt(this.recordValue(record, 2));
                            } else {
                                dbIndex = this.dbIndex;
                            }
                            if (dbIndex == null) {
                                this.message("dbIndex of key: " + key + " is invalid");
                                this.processedSkip();
                                continue;
                            }
                            // 获取数据
                            String value = this.recordValue(record, 1);
                            String type = this.recordValue(record, 3);
                            Long ttl = this.parseLong(this.recordValue(record, 4));
                            ShellRedisKeyType keyType = ShellRedisKeyType.valueOfType(type);
                            if (keyType == null) {
                                this.message("type of key[" + key + "] is invalid: " + type);
                                this.processedSkip();
                                continue;
                            }
                            // 创建键
                            if (!this.client.exists(dbIndex, key)) {
                                this.createKey(key, dbIndex, keyType, value, ttl);
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
                            // 更新：用唯一备份名，避免覆盖用户已有的 <key>_backup；
                            // 重建失败时恢复原键，避免数据丢失。
                            backupKey = key + ".easyshell-backup-" + java.util.UUID.randomUUID();
                            this.client.rename(dbIndex, key, backupKey);
                            this.createKey(key, dbIndex, keyType, value, ttl);
                            this.client.del(dbIndex, backupKey);
                            backupKey = null;
                            this.processedIncr();
                            this.message("key[ " + key + "] is exists, update it");
                        } catch (Exception ex) {
                            // 中断需要向上传播，否则会遍历完整个文件
                            if (ex instanceof InterruptedException) {
                                if (backupKey != null) {
                                    this.restoreBackup(key, backupKey, dbIndex);
                                }
                                throw ex;
                            }
                            if (backupKey != null) {
                                this.restoreBackup(key, backupKey, dbIndex);
                            }
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

    /**
     * 设置字符集
     *
     * @param charset 字符集
     */
    public void charset(String charset) {
        if (StringUtil.isBlank(charset)) {
            this.config.charset(StandardCharsets.UTF_8.name());
        } else {
            this.config.charset(charset);
        }
    }

    /**
     * 设置文件路径
     *
     * @param filePath 文件路径
     */
    public void filePath(String filePath) {
        this.config.filePath(filePath);
    }

    /**
     * 设置文本标识符
     *
     * @param txtIdentifier 文本标识符
     */
    public void txtIdentifier(String txtIdentifier) {
        this.config.setTxtIdentifier(txtIdentifier);
    }

    /**
     * 设置数据起始行
     *
     * @param dataRowStarts 数据起始行
     */
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
    private void createKey(String key, int dbIndex, ShellRedisKeyType type, String value, Long ttl) throws Exception {
        ShellRedisKey redisKey = ShellRedisKeyUtil.deserializeNode(type, value);
        if (redisKey == null) {
            throw new IllegalStateException("无法反序列化 key[" + key + "] 的值");
        }
        if (redisKey.isStringKey()) {
            Object val = redisKey.asStringValue().getValue();
            if (val instanceof String s) {
                this.client.set(dbIndex, key, s);
            } else if (val instanceof byte[] bytes) {
                this.client.set(dbIndex, key.getBytes(StandardCharsets.UTF_8), bytes);
            } else {
                throw new IllegalStateException("key[" + key + "] 的值类型不支持: " + val);
            }
        } else if (redisKey.isJsonKey()) {
            this.client.jsonSet(dbIndex, key, redisKey.asJsonValue().getValue());
        } else if (redisKey.isListKey()) {
            List<ShellRedisListValue.RedisListRow> rows = redisKey.asListValue().getValue();
            String[] arr;
            if (CollectionUtil.isEmpty(rows)) {
                arr = new String[]{""};
            } else {
                List<String> strings = rows.parallelStream().map(ShellRedisListValue.RedisListRow::getValue).collect(Collectors.toList());
                arr = ArrayUtil.toArray(strings, String.class);
            }
            // 序列化列表是按 LRANGE 头到尾的顺序，重建必须用 rpush，lpush 会反转顺序
            this.client.rpush(dbIndex, key, arr);
        } else if (redisKey.isSetKey()) {
            List<ShellRedisSetValue.RedisSetRow> rows = redisKey.asSetValue().getValue();
            String[] arr;
            if (CollectionUtil.isEmpty(rows)) {
                arr = new String[]{""};
            } else {
                List<String> strings = rows.parallelStream().map(ShellRedisSetValue.RedisSetRow::getValue).collect(Collectors.toList());
                arr = ArrayUtil.toArray(strings, String.class);
            }
            this.client.sadd(dbIndex, key, arr);
        } else if (redisKey.isZSetKey()) {
            List<ShellRedisZSetValue.RedisZSetRow> rows = redisKey.asZSetValue().getValue();
            Map<String, Double> scoreMembers;
            if (CollectionUtil.isEmpty(rows)) {
                scoreMembers = new HashMap<>();
            } else {
                scoreMembers = new HashMap<>();
                for (ShellRedisZSetValue.RedisZSetRow row : rows) {
                    scoreMembers.put(row.getValue(), row.getScore());
                }
            }
            this.client.zadd(dbIndex, key, scoreMembers);
        } else if (redisKey.isHashKey()) {
            List<ShellRedisHashValue.RedisHashRow> rows = redisKey.asHashValue().getValue();
            Map<String, String> hash;
            if (CollectionUtil.isEmpty(rows)) {
                hash = new HashMap<>();
            } else {
                hash = new HashMap<>();
                for (ShellRedisHashValue.RedisHashRow row : rows) {
                    hash.put(row.getField(), row.getValue());
                }
            }
            this.client.hmset(dbIndex, key, hash);
        } else if (redisKey.isStreamKey()) {
            List<ShellRedisStreamValue.RedisStreamRow> rows = redisKey.asStreamValue().getValue();
            if (CollectionUtil.isNotEmpty(rows)) {
                for (ShellRedisStreamValue.RedisStreamRow row : rows) {
                    this.client.xadd(dbIndex, key, row.getStreamId(), row.getFields());
                }
            }
        } else {
            throw new IllegalStateException("不支持的键类型: " + type);
        }
        // 处理ttl。-1 表示持久化，正数才设置过期；-2/-0 不处理，避免误删键。
        if (ttl != null && this.retainTTL) {
            if (ttl == -1) {
                this.client.persist(dbIndex, key);
            } else if (ttl > 0) {
                this.client.expire(dbIndex, key, ttl, null);
            }
        }
    }

    /**
     * 读取记录字段并统一转为字符串。Excel 数字/日期单元格返回的不是 String，
     * 直接强转会 ClassCastException。
     *
     * @param record 记录
     * @param index  字段索引
     * @return 字段文本（null 按空串处理）
     */
    private String recordValue(FileRecord record, int index) {
        Object value = record == null ? null : record.get(index);
        if (value == null) {
            return "";
        }
        if (value instanceof Double d) {
            return java.math.BigDecimal.valueOf(d).stripTrailingZeros().toPlainString();
        }
        if (value instanceof java.util.Date date) {
            return String.valueOf(date.getTime());
        }
        return value.toString();
    }

    /**
     * 解析整数，空串或非法值返回 null。
     */
    private Integer parseInt(String value) {
        if (StringUtil.isBlank(value)) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * 解析长整数，空串或非法值返回 null。
     */
    private Long parseLong(String value) {
        if (StringUtil.isBlank(value)) {
            return null;
        }
        try {
            return Long.valueOf(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * 更新失败时把备份键恢复为原键。
     */
    private void restoreBackup(String key, String backupKey, Integer dbIndex) {
        try {
            int index = dbIndex == null ? 0 : dbIndex;
            if (this.client.exists(index, backupKey)) {
                if (this.client.exists(index, key)) {
                    this.client.del(index, key);
                }
                this.client.rename(index, backupKey, key);
            }
        } catch (Exception ex) {
            this.message("restore key[" + key + "] from backup failed: " + ex.getMessage());
        }
    }

    /**
     * 设置数据库索引
     *
     * @param dbIndex 数据库索引
     */
    public void dbIndex(Integer dbIndex) {
        this.dbIndex = dbIndex;
    }
}

