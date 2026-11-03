package cn.oyzh.easyshell.data.sqlite.handler;

import cn.oyzh.easyshell.data.sqlite.SQLiteDataImportHelper;
import cn.oyzh.easyshell.data.sqlite.dto.SQLiteDataImportFile;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;
import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.fx.db.data.dto.DBDataImportConfig;
import cn.oyzh.fx.db.data.file.DBDataCsvTypeFileReader;
import cn.oyzh.fx.db.data.file.DBDataExcelTypeFileReader;
import cn.oyzh.fx.db.data.file.DBDataJsonTypeFileReader;
import cn.oyzh.fx.db.data.file.DBDataTypeFileReader;
import cn.oyzh.fx.db.data.file.DBDataTxtTypeFileReader;
import cn.oyzh.fx.db.data.file.DBDataXmlTypeFileReader;
import cn.oyzh.fx.db.data.handler.DBDataImportHandler;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * SQLite 数据导入处理器。
 *
 * @since 2026-11-02
 */
public class SQLiteDataImportHandler extends DBDataImportHandler<String> {

    private final ShellSqliteClient client;

    private final DBDataImportConfig config;

    private List<SQLiteDataImportFile> files;

    public SQLiteDataImportHandler(ShellSqliteClient client, String name) {
        super(name);
        this.client = client;
        this.config = new DBDataImportConfig();
        this.config.setDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    @Override
    public void doImport() throws Exception {
        this.message("Import Starting");
        if (this.files != null) {
            for (SQLiteDataImportFile file : this.files) {
                this.checkInterrupt();
                try {
                    this.importFile(file);
                    this.processedIncr();
                } catch (Exception ex) {
                    if (!this.isContinueWithErrors()) {
                        throw ex;
                    }
                    this.exception(ex);
                }
            }
        }
        this.doBatchInsert();
        this.message("Import Finished");
    }

    private void importFile(SQLiteDataImportFile file) throws Exception {
        String tableName = file.getTargetTableName();
        List<SQLiteColumn> columns = this.client.selectColumns(tableName);
        if (columns.isEmpty()) {
            throw new IllegalArgumentException("SQLite target table has no columns: " + tableName);
        }
        if (this.config.isCopyMode()) {
            this.client.executeSqlSimple("DELETE FROM " + cn.oyzh.easyshell.sqlite.generator.SQLiteGeneratorUtil.quote(tableName));
        }
        try (DBDataTypeFileReader reader = this.initReader(new File(file.getFilePath()))) {
            if (reader == null) {
                throw new IllegalArgumentException("Unsupported SQLite import type: " + this.fileType);
            }
            while (true) {
                this.checkInterrupt();
                List<Map<String, Object>> records = reader.readRecords(this.readLimit);
                if (records.isEmpty()) {
                    break;
                }
                this.addInsert(SQLiteDataImportHelper.toInsertSql(tableName, columns, records));
            }
        }
    }

    private DBDataTypeFileReader initReader(File file) throws Exception {
        if (this.isCsvType()) {
            return new DBDataCsvTypeFileReader(file, this.config);
        }
        if (this.isJsonType()) {
            return new DBDataJsonTypeFileReader(file, this.config);
        }
        if (this.isXmlType()) {
            return new DBDataXmlTypeFileReader(file, this.config);
        }
        if (this.isExcelType() || "xls".equalsIgnoreCase(this.fileType) || "xlsx".equalsIgnoreCase(this.fileType)) {
            return new DBDataExcelTypeFileReader(file, this.config);
        }
        if (this.isTxtType()) {
            return new DBDataTxtTypeFileReader(file, this.config);
        }
        return null;
    }

    @Override
    public void doBatchInsert(List<String> list, boolean parallel) throws Exception {
        if (this.isContinueWithErrors()) {
            for (String sql : list) {
                try {
                    int count = this.client.insertBatch(this.name, List.of(sql), false);
                    this.processedIncr(count);
                } catch (Exception ex) {
                    this.exception(ex);
                }
            }
        } else {
            int count = this.client.insertBatch(this.name, list, false);
            this.processedIncr(count);
        }
    }

    @Override
    public boolean enableParallel() {
        return false;
    }

    public void setFiles(List<SQLiteDataImportFile> files) {
        this.files = files;
    }

    public DBDataImportConfig getConfig() {
        return this.config;
    }

    public void importMode(String value) {
        this.config.setImportMode(value);
    }

    public void columnIndex(int value) {
        this.config.setColumnIndex(value);
    }

    public void dataRowStarts(int value) {
        this.config.setDataRowStarts(value);
    }

    public void recordLabel(String value) {
        this.config.setRecordLabel(value);
    }

    public void attrToColumn(boolean value) {
        this.config.setAttrToColumn(value);
    }

    public void recordSeparator(String value) {
        this.config.setRecordSeparator(value);
    }

    public void txtIdentifier(String value) {
        this.config.setTxtIdentifier(value);
    }

    public void fieldSeparator(String value) {
        this.config.setFieldSeparator(value);
    }

    public void dateFormat(String value) {
        this.config.setDateFormat(value);
    }
}
