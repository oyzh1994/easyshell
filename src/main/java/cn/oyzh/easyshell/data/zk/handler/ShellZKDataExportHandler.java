package cn.oyzh.easyshell.data.zk.handler;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.util.TextUtil;
import cn.oyzh.common.xml.XMLUtil;
import cn.oyzh.easyshell.util.zk.ShellZKACLUtil;
import cn.oyzh.easyshell.util.zk.ShellZKNodeUtil;
import cn.oyzh.easyshell.zk.ShellZKClient;
import cn.oyzh.easyshell.zk.ShellZKNode;
import cn.oyzh.fx.db.data.handler.DataExportHandler;
import cn.oyzh.i18n.I18nHelper;
import cn.oyzh.store.file.FileColumns;
import cn.oyzh.store.file.FileHelper;
import cn.oyzh.store.file.FileRecord;
import cn.oyzh.store.file.FileWriteConfig;
import cn.oyzh.store.file.TypeFileWriter;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * zk数据导出处理器
 *
 * @author oyzh
 * @since 2025-09-04
 */
public class ShellZKDataExportHandler extends DataExportHandler {

    /**
     * 文件格式
     */
    private String fileType;

    /**
     * 客户端
     */
    private ShellZKClient client;

    /**
     * 节点路径
     */
    private String nodePath;

    // /**
    //  * 过滤内容列表
    //  */
    // private List<ZKFilter> filters;

    /**
     * 批量处理大小
     */
    private int batchSize = 10;

    /**
     * 包含acl
     */
    private boolean includeACL;

    /**
     * 导出配置
     */
    private FileWriteConfig config = new FileWriteConfig();

    @Override
    public void doExport() throws Exception {
        this.message("Export Starting");
        FileColumns columns = new FileColumns();
        columns.addColumn("path", I18nHelper.path());
        columns.addColumn("data", I18nHelper.data());
        if (this.includeACL) {
            columns.addColumn("acl", I18nHelper.acl());
        }
        // 获取写入器
        TypeFileWriter<FileRecord> writer = FileHelper.initWriter(this.fileType, this.config, columns);
        if (writer != null) {
            // 批量记录
            List<FileRecord> batchList = new ArrayList<>(this.batchSize);
            // 批量写入函数
            Runnable writeBatch = () -> {
                if (batchList.isEmpty()) {
                    return;
                }
                try {
                    writer.writeRecords(batchList);
                    for (FileRecord record : batchList) {
                        this.message("export node[" + record.get(0) + "] success");
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
                // // 节点过滤
                // Predicate<String> filter = path -> {
                //     if (ShellZKNodeUtil.isFiltered(path, this.filters)) {
                //         this.message("node[" + path + "] is filtered, skip it");
                //         this.processedSkip();
                //         return false;
                //     }
                //     return true;
                // };

                // 读取器/写入器均按 config.charset() 编解码文件，
                // zk 节点数据同样按该字符集解码，保证字节级往返一致。
                Charset charset = TextUtil.getCharset(this.config.charset());
                // 获取节点成功
                Consumer<ShellZKNode> success = (node) -> {
                    try {
                        this.checkInterrupt();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    // 记录。base-store 的 XmlTypeFileWriter 直接拼接字段值，不做 XML 转义，
                    // 这里先转义，读取器（StAX）会自动还原，避免 & < > 等字符破坏 XML 文档。
                    FileRecord record = new FileRecord();
                    record.put(0, this.escaped(node.nodePath()));
                    record.put(1, this.escaped(new String(node.getNodeData(), charset)));
                    if (this.includeACL) {
                        record.put(2, this.escaped(ShellZKACLUtil.toAclStr(node.acl())));
                    }
                    // 添加到集合
                    batchList.add(record);

                    // 批量写入
                    if (batchList.size() >= this.batchSize) {
                        writeBatch.run();
                    }
                };
                // 获取节点失败。中断必须向外传播，否则 loopNode 会继续遍历整棵树。
                BiConsumer<String, Exception> error = (path, ex) -> {
                    Throwable cause = ex;
                    while (cause instanceof RuntimeException && cause.getCause() != null) {
                        cause = cause.getCause();
                    }
                    if (cause instanceof InterruptedException) {
                        throw new RuntimeException(cause);
                    }
                    ex.printStackTrace();
                    this.message("export node[" + path + "] failed");
                    this.processedDecr();
                };
                // 递归获取节点
                ShellZKNodeUtil.loopNode(this.client, this.nodePath, null, success, error, this.includeACL);
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
     * XML 格式需要对字段值转义，其它格式原样返回。
     * 字段顺序：先转义 &，再转义 < >；回车用字符引用保留，避免 XML 解析时被规范化为换行。
     *
     * @param value 原始值
     * @return 写入文件的值
     */
    private String escaped(String value) {
        if (value == null || !"xml".equalsIgnoreCase(this.fileType)) {
            return value;
        }
        return XMLUtil.escape(value);
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
            this.config.prefix(prefix);
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
    public ShellZKClient getClient() {
        return client;
    }

    /**
     * 设置客户端
     *
     * @param client 客户端
     */
    public void setClient(ShellZKClient client) {
        this.client = client;
    }

    /**
     * 获取节点路径
     *
     * @return 节点路径
     */
    public String getNodePath() {
        return nodePath;
    }

    /**
     * 设置节点路径
     *
     * @param nodePath 节点路径
     */
    public void setNodePath(String nodePath) {
        this.nodePath = nodePath;
    }

    // public List<ZKFilter> getFilters() {
    //     return filters;
    // }
    //
    // public void setFilters(List<ZKFilter> filters) {
    //     this.filters = filters;
    // }

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
     * 是否包含acl
     *
     * @return 结果
     */
    public boolean isIncludeACL() {
        return includeACL;
    }

    /**
     * 设置是否包含acl
     *
     * @param includeACL 是否包含acl
     */
    public void setIncludeACL(boolean includeACL) {
        this.includeACL = includeACL;
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
}

