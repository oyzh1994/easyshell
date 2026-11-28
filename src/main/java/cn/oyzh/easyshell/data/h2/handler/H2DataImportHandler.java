package cn.oyzh.easyshell.data.h2.handler;

import cn.oyzh.easyshell.data.h2.H2DataImportHelper;
import cn.oyzh.easyshell.data.h2.dto.H2DataImportFile;
import cn.oyzh.easyshell.h2.column.H2Column;
import cn.oyzh.easyshell.h2.ShellH2Client;
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
 * H2 数据导入处理器。
 *
 * @since 2026-11-02
 */
public class H2DataImportHandler extends DBDataImportHandler<String> {

    private final ShellH2Client client;

    private final DBDataImportConfig config;

    private List<H2DataImportFile> files;

    public H2DataImportHandler(ShellH2Client client, String name) {
        super(name);
        this.client = client;
        this.config = new DBDataImportConfig();
        this.config.setDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    @Override
    protected void doActionInternal() throws Exception {
        this.message("Import Starting");
        if (this.files != null) {
            for (H2DataImportFile file : this.files) {
                this.checkInterrupt();
                try {
                    this.importFile(file);
                    this.processedIncr();
                } catch (Exception ex) {
                    this.handleFailure(ex, this.isContinueWithErrors());
                }
            }
        }
        this.doBatchInsert();
        this.message("Import Finished");
    }

    private void importFile(H2DataImportFile file) throws Exception {
        // 目标表名可能来自文件名或用户输入，归一化为数据库中的实际名称
        String tableName = this.client.canonicalObjectName(file.getTargetTableName());
        List<H2Column> columns = this.client.selectColumns(tableName);
        if (columns.isEmpty()) {
            throw new IllegalArgumentException("H2 target table has no columns: " + tableName);
        }
        if (this.config.isCopyMode()) {
            this.client.executeSqlSimple("DELETE FROM " + cn.oyzh.easyshell.h2.generator.H2GeneratorUtil.quote(tableName));
        }
        try (DBDataTypeFileReader reader = this.initReader(new File(file.getFilePath()))) {
            if (reader == null) {
                throw new IllegalArgumentException("Unsupported H2 import type: " + this.fileType);
            }
            while (true) {
                this.checkInterrupt();
                List<Map<String, Object>> records = reader.readRecords(this.readLimit);
                if (records.isEmpty()) {
                    break;
                }
                this.addInsert(H2DataImportHelper.toInsertSql(tableName, columns, records));
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
                    this.processedDecr();
                    this.exception(ex);
                }
            }
        } else {
            try {
                int count = this.client.insertBatch(this.name, list, false);
                this.processedIncr(count);
            } catch (Exception ex) {
                this.processedDecr(list.size());
                throw ex;
            }
        }
    }

    @Override
    public boolean enableParallel() {
        return false;
    }

    public void setFiles(List<H2DataImportFile> files) {
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
