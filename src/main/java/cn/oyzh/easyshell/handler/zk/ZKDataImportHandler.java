package cn.oyzh.easyshell.handler.zk;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.util.TextUtil;
import cn.oyzh.easyshell.util.zk.ZKACLUtil;
import cn.oyzh.easyshell.zk.ZKClient;
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
 * zk数据导入业务
 *
 * @author oyzh
 * @since 2024/11/26
 */
public class ZKDataImportHandler extends DataHandler {

    /**
     * 文件格式
     */
    private String fileType;

    /**
     * 客户端
     */
    private ZKClient client;

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
     * 执行导出
     *
     * @throws Exception 异常
     */
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
                                        aclList = ZKACLUtil.parseAcl(acl);
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
}

