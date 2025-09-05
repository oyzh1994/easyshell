package cn.oyzh.easyshell.handler.zk;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.zk.ZKFilter;
import cn.oyzh.easyshell.util.zk.ZKACLUtil;
import cn.oyzh.easyshell.util.zk.ZKNodeUtil;
import cn.oyzh.easyshell.zk.ZKClient;
import cn.oyzh.easyshell.zk.ZKNode;
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
import java.util.function.Predicate;

/**
 * zk数据导出业务
 *
 * @author oyzh
 * @since 2024/11/26
 */
public class ShellZKDataExportHandler extends ShellZKDataHandler {

    /**
     * 文件格式
     */
    private String fileType;

    /**
     * 客户端
     */
    private ZKClient client;

    /**
     * 节点路径
     */
    private String nodePath;

    /**
     * 过滤内容列表
     */
    private List<ZKFilter> filters;

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

    /**
     * 执行导出
     *
     * @throws Exception 异常
     */
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
                // 节点过滤
                Predicate<String> filter = path -> {
                    if (ZKNodeUtil.isFiltered(path, this.filters)) {
                        this.message("node[" + path + "] is filtered, skip it");
                        this.processedSkip();
                        return false;
                    }
                    return true;
                };

                // 获取节点成功
                Consumer<ZKNode> success = (node) -> {
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
                        record.put(2, ZKACLUtil.toAclStr(node.acl()));
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
                ZKNodeUtil.loopNode(this.client, this.nodePath, filter, success, error, this.includeACL);
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

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public ZKClient getClient() {
        return client;
    }

    public void setClient(ZKClient client) {
        this.client = client;
    }

    public String getNodePath() {
        return nodePath;
    }

    public void setNodePath(String nodePath) {
        this.nodePath = nodePath;
    }

    public List<ZKFilter> getFilters() {
        return filters;
    }

    public void setFilters(List<ZKFilter> filters) {
        this.filters = filters;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public boolean isIncludeACL() {
        return includeACL;
    }

    public void setIncludeACL(boolean includeACL) {
        this.includeACL = includeACL;
    }

    public FileWriteConfig getConfig() {
        return config;
    }

    public void setConfig(FileWriteConfig config) {
        this.config = config;
    }
}

