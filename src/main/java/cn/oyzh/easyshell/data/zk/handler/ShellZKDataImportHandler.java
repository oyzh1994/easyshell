package cn.oyzh.easyshell.data.zk.handler;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.util.TextUtil;
import cn.oyzh.easyshell.util.zk.ShellZKACLUtil;
import cn.oyzh.easyshell.zk.ShellZKClient;
import cn.oyzh.fx.db.data.handler.DataImportHandler;
import cn.oyzh.store.file.FileColumns;
import cn.oyzh.store.file.FileHelper;
import cn.oyzh.store.file.FileReadConfig;
import cn.oyzh.store.file.FileRecord;
import cn.oyzh.store.file.TypeFileReader;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * zk数据导入处理器
 *
 * @author oyzh
 * @since 2025-09-04
 */
public class ShellZKDataImportHandler extends DataImportHandler {

    /**
     * 文件格式
     */
    private String fileType;

    /**
     * 客户端
     */
    private ShellZKClient client;

    /**
     * 批量处理大小
     */
    private int batchSize = 50;

    /**
     * 包含acl
     */
    private boolean includeACL;

    /**
     * 存在时忽略
     */
    private boolean ignoreExist;

    /**
     * 导出配置
     */
    private FileReadConfig config = new FileReadConfig();

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

    @Override
    public void doImport() throws Exception {
        this.message("Import Starting");
        FileColumns columns = new FileColumns();
        columns.addColumn("path", 0);
        columns.addColumn("data", 1);
        columns.addColumn("acl", 2);
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
                        String path = "";
                        try {
                            path = (String) record.get(0);
                            if (StringUtil.isBlank(path)) {
                                this.message("node[" + path + "] is invalid");
                                this.processedSkip();
                                continue;
                            }
                            // 节点状态
                            boolean exists = this.client.exists(path);
                            // 跳过
                            if (this.ignoreExist && exists) {
                                this.message("node[" + path + "] is exists, skip it");
                                this.processedSkip();
                                continue;
                            }
                            String data = (String) record.get(1);
                            String dataStr = TextUtil.changeCharset(data, StandardCharsets.UTF_8.name(), this.config.charset());
                            // 更新
                            if (exists) {
                                this.client.setData(path, dataStr);
                                this.message("update node[" + path + "] success");
                            } else {// 创建
                                List<ACL> aclList = ZooDefs.Ids.OPEN_ACL_UNSAFE;
                                if (this.includeACL) {
                                    String acl = (String) record.get(2);
                                    if (StringUtil.isNotBlank(acl)) {
                                        aclList = ShellZKACLUtil.parseAcl(acl);
                                    }
                                }
                                this.client.create(path, dataStr, aclList, CreateMode.PERSISTENT, true);
                                this.message("create node[" + path + "] success");
                            }
                            this.processedIncr();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            this.processedDecr();
                            this.message("create node[" + path + "] failed");
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
    public void txtIdentifier(Character txtIdentifier) {
        this.config.txtIdentifier(txtIdentifier);
    }

    /**
     * 设置数据起始行
     *
     * @param dataRowStarts 数据起始行
     */
    public void dataRowStarts(Integer dataRowStarts) {
        this.config.dataRowStarts(dataRowStarts);
    }
}

