package cn.oyzh.easyshell.data.redis.handler;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.xml.XMLUtil;
import cn.oyzh.easyshell.redis.ShellRedisClient;
import cn.oyzh.easyshell.redis.ShellRedisKeyUtil;
import cn.oyzh.easyshell.redis.key.ShellRedisKey;
import cn.oyzh.easyshell.util.redis.ShellRedisUtil;
import cn.oyzh.fx.db.data.handler.DataExportHandler;
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
 * @since 2025-09-01
 */
public class ShellRedisDataExportHandler extends DataExportHandler {

    /**
     * 文件格式
     */
    private String fileType;

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
     * 获取数据库
     *
     * @return 数据库
     */
    public Integer getDatabase() {
        return database;
    }

    /**
     * 设置数据库
     *
     * @param database 数据库
     */
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
     * 获取导出配置
     *
     * @return 导出配置
     */
    public FileWriteConfig getConfig() {
        return config;
    }

    /**
     * 设置导出配置
     *
     * @param config 导出配置
     */
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

    @Override
    protected void doActionInternal() throws Exception {
        this.message("Export Starting");
        FileColumns columns = new FileColumns();
        columns.addColumn("key", I18nHelper.key());
        columns.addColumn("value", I18nHelper.value());
        columns.addColumn("dbIndex", I18nHelper.database());
        columns.addColumn("type", I18nHelper.type());
        columns.addColumn("ttl", I18nHelper.ttl());
        // 获取写入器
        TypeFileWriter<FileRecord> writer = FileHelper.initWriter(this.fileType, this.config, columns);
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
                    this.processedDecr(batchList.size());
                }
            };
            try {
                // 写入头
                writer.writeHeader();
                // 节点过滤
                BiPredicate<String, ShellRedisKey> filter = (key, redisKey) -> {
                    if (redisKey == null) {
                        this.message("key[" + key + "] don't exist");
                        // 此处尚未计入处理数，不能用 processedDecr，否则计数变负
                        this.processedSkip();
                        return false;
                    }
                    if (ShellRedisUtil.isExclude(this.keyTypes, redisKey)) {
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
                    // 记录。base-store 的 XmlTypeFileWriter 不转义字段值，
                    // 这里先转义，StAX 读取器会自动还原。
                    FileRecord record = new FileRecord();
                    // 序列化键值
                    String value = ShellRedisKeyUtil.serializeNode(redisKey);
                    record.put(0, this.escaped(key));
                    record.put(1, this.escaped(value));
                    record.put(2, redisKey.getDbIndex());
                    record.put(3, this.escaped(redisKey.typeName()));
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
                    Throwable cause = ex;
                    while (cause instanceof RuntimeException && cause.getCause() != null) {
                        cause = cause.getCause();
                    }
                    // 中断必须向外传播，否则会继续遍历剩余所有键
                    if (cause instanceof InterruptedException) {
                        throw new RuntimeException(cause);
                    }
                    ex.printStackTrace();
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
                        // 查询对象编码
                        if (redisKey.isStringKey()) {
                            redisKey.setObjectedEncoding(this.client.objectEncoding(dbIndex, key));
                        }
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
     * XML 格式需要对字段值转义，其它格式原样返回。
     * 先转义 &，再转义 < >；回车用字符引用保留。
     *
     * @param value 原始值
     * @return 写入文件的值
     */
    private String escaped(String value) {
        if (value == null || !"xml".equalsIgnoreCase(this.fileType)) {
            return value;
        }
        // 最后防线：XML 1.0 禁止 U+0000-U+0008/0x0B/0x0C/0x0E-0x1F 等控制字符，
        // 连字符引用都不允许。正常二进制值已在序列化层转为 0x'hex'，这里只处理
        // 其它路径可能带入的非法字符（如 key 名），剔除以保证文件可解析。
        StringBuilder safe = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == 0x9 || c == 0xA || c == 0xD) {
                safe.append(c);
            } else if (c >= 0x20 && (c < 0xD800 || c > 0xDFFF) && c != 0xFFFE && c != 0xFFFF) {
                safe.append(c);
            }
            // 其余非法字符丢弃，避免生成无法解析的 XML
        }
        return XMLUtil.escape(safe.toString());
    }

    /**
     * 设置前缀
     *
     * @param prefix 前缀
     */
    public void prefix(String prefix) {
        if (prefix.isBlank()) {
            this.config.prefix(null);
        } else {
            this.config.prefix(prefix + " ");
        }
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
     * 设置是否包含标题
     *
     * @param includeTitle 是否包含标题
     */
    public void includeTitle(boolean includeTitle) {
        this.config.includeTitle(includeTitle);
    }

    /**
     * 设置是否压缩
     *
     * @param compress 是否压缩
     */
    public void compress(boolean compress) {
        this.config.compress(compress);
    }
}

