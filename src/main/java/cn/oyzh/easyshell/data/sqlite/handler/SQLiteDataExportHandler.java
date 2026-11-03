package cn.oyzh.easyshell.data.sqlite.handler;

import cn.oyzh.easyshell.data.sqlite.dto.SQLiteDataExportTable;
import cn.oyzh.easyshell.data.sqlite.file.SQLiteDataFileWriter;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;
import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.fx.db.data.dto.DBDataExportConfig;
import cn.oyzh.fx.db.data.handler.DBDataExportHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * SQLite 数据导出处理器。
 *
 * @since 2026-11-02
 */
public class SQLiteDataExportHandler extends DBDataExportHandler {

    private final ShellSqliteClient client;

    private final DBDataExportConfig config;

    private List<SQLiteDataExportTable> tables;

    public SQLiteDataExportHandler(ShellSqliteClient client, String name) {
        super(name);
        this.client = client;
        this.config = new DBDataExportConfig();
        this.config.setDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    @Override
    public void doExport() throws Exception {
        this.message("Export Starting");
        if (this.tables != null) {
            for (SQLiteDataExportTable table : this.tables) {
                this.checkInterrupt();
                try {
                    this.exportTable(table);
                    this.processedIncr();
                } catch (Exception ex) {
                    if (!this.isContinueWithErrors()) {
                        throw ex;
                    }
                    this.exception(ex);
                }
            }
        }
        this.message("Export Finished");
    }

    private void exportTable(SQLiteDataExportTable table) throws Exception {
        List<SQLiteColumn> columns = table.selectedColumns();
        if (columns.isEmpty()) {
            throw new IllegalArgumentException("Export table has no selected columns: " + table.getName());
        }
        this.config.rootNodeName(table.getName());
        this.config.itemNodeName("record");
        this.config.sheetName(table.getName());
        try (SQLiteDataFileWriter writer = new SQLiteDataFileWriter(
                table.getFilePath(), this.fileType, table.getName(), this.config, columns)) {
            writer.writeHeader();
            if (table.getRecords() != null) {
                for (Map<String, Object> record : table.getRecords()) {
                    this.checkInterrupt();
                    try {
                        writer.writeRecord(record);
                        this.processedIncr();
                    } catch (Exception ex) {
                        if (!this.isContinueWithErrors()) {
                            throw ex;
                        }
                        this.exception(ex);
                    }
                }
            } else {
                long offset = 0;
                while (true) {
                    this.checkInterrupt();
                    List<Map<String, Object>> records =
                            this.client.selectRecords(table.getName(), columns, offset, this.queryLimit);
                    if (records.isEmpty()) {
                        break;
                    }
                    for (Map<String, Object> record : records) {
                        try {
                            writer.writeRecord(record);
                            this.processedIncr();
                        } catch (Exception ex) {
                            if (!this.isContinueWithErrors()) {
                                throw ex;
                            }
                            this.exception(ex);
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
        this.message("Exported " + table.getName() + " -> " + table.getFilePath());
    }

    public void setTables(List<SQLiteDataExportTable> tables) {
        this.tables = tables;
    }

    public DBDataExportConfig getConfig() {
        return this.config;
    }

    public ShellSqliteClient getClient() {
        return this.client;
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
