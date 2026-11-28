package cn.oyzh.easyshell.data.h2.handler;

import cn.oyzh.easyshell.data.h2.dto.H2DataExportTable;
import cn.oyzh.easyshell.data.h2.file.H2DataFileWriter;
import cn.oyzh.easyshell.h2.column.H2Column;
import cn.oyzh.easyshell.h2.ShellH2Client;
import cn.oyzh.fx.db.data.dto.DBDataExportConfig;
import cn.oyzh.fx.db.data.handler.DBDataExportHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * H2 数据导出处理器。
 *
 * @since 2026-11-02
 */
public class H2DataExportHandler extends DBDataExportHandler {

    private final ShellH2Client client;

    private final String schema;

    private final DBDataExportConfig config;

    private List<H2DataExportTable> tables;

    public H2DataExportHandler(ShellH2Client client, String name) {
        super(name);
        this.client = client;
        this.schema = client.schema(name);
        this.config = new DBDataExportConfig();
        this.config.setDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    @Override
    protected void doActionInternal() throws Exception {
        this.message("Export Starting");
        if (this.tables != null) {
            for (H2DataExportTable table : this.tables) {
                this.checkInterrupt();
                try {
                    this.exportTable(table);
                    this.processedIncr();
                } catch (Exception ex) {
                    this.handleFailure(ex, this.isContinueWithErrors());
                }
            }
        }
        this.message("Export Finished");
    }

    private void exportTable(H2DataExportTable table) throws Exception {
        // 表名可能来自用户输入，归一化为数据库中的实际名称，保证生成的 INSERT 语句可用
        String objectName = this.client.canonicalObjectName(this.schema, table.getName());
        List<H2Column> columns = table.selectedColumns();
        if (columns.isEmpty()) {
            throw new IllegalArgumentException("Export table has no selected columns: " + objectName);
        }
        this.config.rootNodeName(objectName);
        this.config.itemNodeName("record");
        this.config.sheetName(objectName);
        try (H2DataFileWriter writer = new H2DataFileWriter(
                table.getFilePath(), this.fileType, objectName, this.config, columns)) {
            writer.writeHeader();
            if (table.getRecords() != null) {
                for (Map<String, Object> record : table.getRecords()) {
                    this.checkInterrupt();
                    try {
                        writer.writeRecord(record);
                        this.processedIncr();
                    } catch (Exception ex) {
                        this.handleFailure(ex, this.isContinueWithErrors());
                    }
                }
            } else {
                long offset = 0;
                while (true) {
                    this.checkInterrupt();
                    List<Map<String, Object>> records =
                            this.client.selectRecords(
                                    this.schema, objectName, columns, offset, this.queryLimit);
                    if (records.isEmpty()) {
                        break;
                    }
                    for (Map<String, Object> record : records) {
                        try {
                            writer.writeRecord(record);
                            this.processedIncr();
                        } catch (Exception ex) {
                            this.handleFailure(ex, this.isContinueWithErrors());
                        }
                    }
                    offset += records.size();
                    if (records.size() < this.queryLimit) {
                        break;
                    }
                }
            }
            writer.writeTrial();
        }
        this.message("Exported " + objectName + " -> " + table.getFilePath());
    }

    public void setTables(List<H2DataExportTable> tables) {
        this.tables = tables;
    }

    public DBDataExportConfig getConfig() {
        return this.config;
    }

    public ShellH2Client getClient() {
        return this.client;
    }

    public String getSchema() {
        return this.schema;
    }

    public void dateFormat(String value) {
        this.config.setDateFormat(value);
    }

    public void includeTitle(boolean value) {
        this.config.setIncludeTitle(value);
    }

    public void fieldToAttr(boolean value) {
        this.config.setFieldToAttr(value);
    }

    public void earlyVersion(boolean value) {
        this.config.setEarlyVersion(value);
    }

    public void fieldSeparator(String value) {
        this.config.setFieldSeparator(value);
    }

    public void recordSeparator(String value) {
        this.config.setRecordSeparator(value);
    }

    public void txtIdentifier(String value) {
        this.config.setTxtIdentifier(value);
    }

    public void charset(String value) {
        this.config.setCharset(value);
    }
}
