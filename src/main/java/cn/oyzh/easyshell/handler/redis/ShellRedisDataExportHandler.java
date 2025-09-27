package cn.oyzh.easyshell.handler.redis;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.redis.ShellRedisClient;
import cn.oyzh.easyshell.redis.ShellRedisKeyUtil;
import cn.oyzh.easyshell.redis.key.ShellRedisKey;
import cn.oyzh.i18n.I18nHelper;
import cn.oyzh.store.file.FileColumns;
import cn.oyzh.store.file.FileHelper;
import cn.oyzh.store.file.FileRecord;
import cn.oyzh.store.file.FileWriteConfig;
import cn.oyzh.store.file.TypeFileWriter;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

/**
 * redis数据导出处理器
 *
 * @author oyzh
 * @since 2024/11/26
 */
public class ShellRedisDataExportHandler extends ShellRedisDataHandler {

    /**
     * 文件格式
     */
    private String fileType;

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public ShellRedisClient getClient() {
        return client;
    }

    public void setClient(ShellRedisClient client) {
        this.client = client;
    }

    public Integer getDatabase() {
        return database;
    }

    public void setDatabase(Integer database) {
        this.database = database;
    }

    // public List<RedisFilter> getFilters() {
    //     return filters;
    // }
    //
    // public void setFilters(List<RedisFilter> filters) {
    //     this.filters = filters;
    // }

    public List<String> getKeyTypes() {
        return keyTypes;
    }

    public void setKeyTypes(List<String> keyTypes) {
        this.keyTypes = keyTypes;
    }

    public boolean isRetainTTL() {
        return retainTTL;
    }

    public void setRetainTTL(boolean retainTTL) {
        this.retainTTL = retainTTL;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public FileWriteConfig getConfig() {
        return config;
    }

    public void setConfig(FileWriteConfig config) {
        this.config = config;
    }

    /**
     * 客户端
     */
    private ShellRedisClient client;

    /**
     * 数据库
     * null 全部
     * 其他 指定库
     */
    private Integer database;

    // /**
    //  * 过滤内容列表
    //  */
    // private List<RedisFilter> filters;

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

    /**
     * 批量处理大小
     */
    private int batchSize = 10;

    /**
     * 导出配置
     */
    private FileWriteConfig config = new FileWriteConfig();

    /**
     * 执行导出
     *
     * @throws Exception 异常
     */
    public void doExport() throws Exception {
        this.message("Export Starting");
        FileColumns columns = new FileColumns();
        columns.addColumn("key", I18nHelper.key());
        columns.addColumn("value", I18nHelper.value());
        columns.addColumn("dbIndex", I18nHelper.database());
        columns.addColumn("type", I18nHelper.type());
        columns.addColumn("ttl", I18nHelper.ttl());
        // 获取写入器
        TypeFileWriter writer = FileHelper.initWriter(this.fileType, this.config, columns);
        if (writer != null) {
            // 批量记录
            List<FileRecord> batchList = new ArrayList<>(this.batchSize);
            // 批量写入函数
            Runnable writeBatch = () -> {
                try {
                    writer.writeRecords(batchList);
                    for (FileRecord record : batchList) {
                        this.message("export key[" + record.get(0) + "] success");
                    }
                    this.processedIncr(batchList.size());
                    batchList.clear();
                } catch (Exception ex) {
                    this.message("write data failed");
                    this.processedDecr();
                }
            };
            try {
                // 写入头
                writer.writeHeader();
                // 节点过滤
                BiPredicate<String, ShellRedisKey> filter = (key, redisKey) -> {
                    if (redisKey == null) {
                        this.message("key[" + key + "] don't exist");
                        this.processedDecr();
                        return false;
                    }
                    if (this.isExclude(redisKey)) {
                        this.message("key[" + key + "] is exclude, skip it");
                        this.processedSkip();
                        return false;
                    }
                    // if (ShellRedisKeyUtil.isFiltered(key, this.filters)) {
                    //     this.message("key[" + key + "] is filtered, skip it");
                    //     this.processedSkip();
                    //     return false;
                    // }
                    return true;
                };
                // 获取节点成功
                Consumer<ShellRedisKey> success = redisKey -> {
                    try {
                        this.checkInterrupt();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    String key = redisKey.getKey();
                    // 记录
                    FileRecord record = new FileRecord();
                    // 序列化键值
                    String value = ShellRedisKeyUtil.serializeNode(redisKey);
                    record.put(0, key);
                    record.put(1, value);
                    record.put(2, redisKey.getDbIndex());
                    record.put(3, redisKey.typeName());
                    if (redisKey.getTtl() != null) {
                        record.put(4, redisKey.getTtl());
                    }
                    // 添加到集合
                    batchList.add(record);

                    // 批量写入
                    if (batchList.size() >= this.batchSize) {
                        writeBatch.run();
                    }
                };
                // 获取节点失败
                BiConsumer<String, Exception> error = (key, ex) -> {
                    if (ex instanceof RuntimeException) {
                        ex = (Exception) ex.getCause();
                    } else {
                        ex.printStackTrace();
                    }
                    // 针对中断异常不处理
                    if (ex instanceof InterruptedException) {
                        return;
                    }
                    this.message("export key[" + key + "] failed");
                    this.processedDecr();
                };
                this.doExport(success, error, filter);
            } finally {
                // 写入尾
                writeBatch.run();
                writer.writeTrial();
                writer.close();
                this.message("Exported To -> " + this.config.filePath());
            }
        } else {
            JulLog.error("未找到可用的写入器，文件类型:{}", this.fileType);
        }
        this.message("Export Finished");
    }

    /**
     * 执行导出
     *
     * @param success 成功操作
     * @param error   异常操作
     * @param filter  过滤操作
     */
    private void doExport(Consumer<ShellRedisKey> success, BiConsumer<String, Exception> error, BiPredicate<String, ShellRedisKey> filter) {
        // 导出业务处理
        BiConsumer<Integer, Set<String>> export = (dbIndex, keys) -> {
            for (String key : keys) {
                try {
                    // 获取键
                    ShellRedisKey redisKey = ShellRedisKeyUtil.getKey(dbIndex, key, this.retainTTL, true, this.client);
                    // 执行过滤
                    if (filter.test(key, redisKey)) {
                        // // 查询对象编码
                        // if (redisKey.isStringKey()) {
                        //     redisKey.objectedEncoding(this.client.objectEncoding(dbIndex, key));
                        // }
                        success.accept(redisKey);
                    }
                } catch (Exception ex) {
                    error.accept(key, ex);
                }
            }
        };
        // 所有库
        if (this.database == null) {
            int dbCount = this.client.databases();
            for (int i = 0; i < dbCount; i++) {
                Set<String> keys = this.client.allKeys(i, this.pattern);
                export.accept(i, keys);
            }
        } else {// 指定库
            Set<String> keys = this.client.allKeys(this.database, this.pattern);
            export.accept(this.database, keys);
        }
    }

    /**
     * 是否被排除
     *
     * @param node 键
     * @return 结果
     */
    private boolean isExclude(ShellRedisKey node) {
        if (CollectionUtil.isEmpty(this.keyTypes)) {
            return true;
        }
        if (!this.keyTypes.contains("list") && node.isListKey()) {
            return true;
        }
        if (!this.keyTypes.contains("set") && node.isSetKey()) {
            return true;
        }
        if (!this.keyTypes.contains("zset") && node.isZSetKey()) {
            return true;
        }
        if (!this.keyTypes.contains("hash") && node.isHashKey()) {
            return true;
        }
        if (!this.keyTypes.contains("stream") && node.isStreamKey()) {
            return true;
        }
        return !this.keyTypes.contains("string") && node.isStringKey();
    }

    public void prefix(String prefix) {
        if (prefix.isBlank()) {
            this.config.prefix(null);
        } else {
            this.config.prefix(prefix + " ");
        }
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

    public void includeTitle(boolean includeTitle) {
        this.config.includeTitle(includeTitle);
    }

    public void compress(boolean compress) {
        this.config.compress(compress);
    }
}

