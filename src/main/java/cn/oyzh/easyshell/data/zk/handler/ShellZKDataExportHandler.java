package cn.oyzh.easyshell.data.zk.handler;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.StringUtil;
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
        TypeFileWriter writer = FileHelper.initWriter(this.fileType, this.config, columns);
        if (writer != null) {
            // 批量记录
            List<FileRecord> batchList = new ArrayList<>(this.batchSize);
            // 批量写入函数
            Runnable writeBatch = () -> {
                try {
                    writer.writeRecords(batchList);
                    for (FileRecord record : batchList) {
                        this.message("export node[" + record.get(0) + "] success");
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
                // // 节点过滤
                // Predicate<String> filter = path -> {
                //     if (ShellZKNodeUtil.isFiltered(path, this.filters)) {
                //         this.message("node[" + path + "] is filtered, skip it");
                //         this.processedSkip();
                //         return false;
                //     }
                //     return true;
                // };

                // 获取节点成功
                Consumer<ShellZKNode> success = (node) -> {
                    try {
                        this.checkInterrupt();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    // 记录
                    FileRecord record = new FileRecord();
                    record.put(0, node.nodePath());
                    record.put(1, new String(node.getNodeData(), StandardCharsets.UTF_8));
                    if (this.includeACL) {
                        record.put(2, ShellZKACLUtil.toAclStr(node.acl()));
                    }
                    // 添加到集合
                    batchList.add(record);

                    // 批量写入
                    if (batchList.size() >= this.batchSize) {
                        writeBatch.run();
                    }
                };
                // 获取节点失败
                BiConsumer<String, Exception> error = (path, ex) -> {
                    if (ex instanceof RuntimeException) {
                        ex = (Exception) ex.getCause();
                    } else {
                        ex.printStackTrace();
                    }
                    // 针对中断异常不处理
                    if (ex instanceof InterruptedException) {
                        return;
                    }
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
    public void txtIdentifier(Character txtIdentifier) {
        this.config.txtIdentifier(txtIdentifier);
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

