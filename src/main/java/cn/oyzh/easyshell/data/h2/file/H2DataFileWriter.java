package cn.oyzh.easyshell.data.h2.file;

import com.alibaba.fastjson2.JSON;
import cn.oyzh.common.date.DateUtil;
import cn.oyzh.common.file.LineFileWriter;
import cn.oyzh.easyshell.h2.column.H2Column;
import cn.oyzh.easyshell.h2.generator.record.H2RecordSqlGenerator;
import cn.oyzh.easyshell.util.h2.ShellH2ColumnUtil;
import cn.oyzh.easyshell.util.h2.ShellH2DataUtil;
import cn.oyzh.fx.db.data.dto.DBDataExportConfig;
import cn.oyzh.store.file.CsvFileWriter;
import cn.oyzh.store.file.ExcelTypeFileWriter;
import cn.oyzh.store.file.FileColumn;
import cn.oyzh.store.file.FileColumns;
import cn.oyzh.store.file.FileRecord;
import cn.oyzh.store.file.HtmlTypeFileWriter;
import cn.oyzh.store.file.JsonTypeFileWriter;
import cn.oyzh.store.file.TypeFileWriter;
import cn.oyzh.store.file.XmlTypeFileWriter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * H2 数据类型文件写入器。
 *
 * <p>SQL 使用 H2 字面量；CSV、TXT、JSON、XML、HTML、XLS/XLSX 复用
 * base-store 的通用文件写入器。</p>
 *
 * @since 2026-11-02
 */
public class H2DataFileWriter implements AutoCloseable {

    private final String fileType;

    private final String objectName;

    private final List<H2Column> columns;

    private final DBDataExportConfig config;

    private final TypeFileWriter<FileRecord> delegate;

    private final CsvFileWriter csvWriter;

    private final LineFileWriter sqlWriter;

    private boolean firstSqlRecord = true;

    public H2DataFileWriter(String filePath, String fileType, String objectName,
                                DBDataExportConfig config, List<H2Column> columns) throws Exception {
        this.fileType = fileType.toLowerCase(Locale.ROOT);
        this.objectName = objectName;
        this.columns = List.copyOf(columns);
        this.config = config;
        this.config.setFilePath(filePath);
        FileColumns fileColumns = this.fileColumns();
        switch (this.fileType) {
            case "sql" -> {
                this.delegate = null;
                this.csvWriter = null;
                this.sqlWriter = LineFileWriter.create(filePath, config.getCharset());
            }
            case "csv", "txt" -> {
                this.delegate = null;
                this.sqlWriter = null;
                this.csvWriter = new CsvFileWriter(filePath, config, config.getFieldSeparator());
            }
            case "json" -> {
                this.delegate = new JsonTypeFileWriter(config, fileColumns);
                this.csvWriter = null;
                this.sqlWriter = null;
            }
            case "xml" -> {
                this.delegate = new XmlTypeFileWriter(config, fileColumns);
                this.csvWriter = null;
                this.sqlWriter = null;
            }
            case "html" -> {
                this.delegate = new HtmlTypeFileWriter(config, fileColumns);
                this.csvWriter = null;
                this.sqlWriter = null;
            }
            case "xls", "xlsx" -> {
                this.delegate = new ExcelTypeFileWriter(config, fileColumns);
                this.csvWriter = null;
                this.sqlWriter = null;
            }
            default -> throw new IllegalArgumentException("Unsupported H2 export type: " + fileType);
        }
    }

    public void writeHeader() throws Exception {
        if (this.delegate != null) {
            this.delegate.writeHeader();
        } else if (this.csvWriter != null && config.isIncludeTitle()) {
            this.csvWriter.write(this.columns.stream().map(H2Column::getName).toList());
        }
    }

    public void writeRecord(Map<String, Object> record) throws Exception {
        if (this.sqlWriter != null) {
            if (!this.firstSqlRecord) {
                this.sqlWriter.writeLine("");
            }
            Map<String, Object> normalized = ShellH2DataUtil.normalize(this.columns, record);
            List<H2Column> insertColumns = this.columns.stream()
                    .filter(column -> !column.isGenerated())
                    .toList();
            this.sqlWriter.writeLine(H2RecordSqlGenerator.insertWithTypes(
                    this.tableName(), insertColumns, normalized));
            this.firstSqlRecord = false;
            return;
        }
        FileRecord fileRecord = new FileRecord();
        for (int index = 0; index < this.columns.size(); index++) {
            H2Column column = this.columns.get(index);
            Object value = normalize(column, record.get(column.getName()));
            if ("xml".equals(this.fileType) || "html".equals(this.fileType)) {
                value = escapeXml(value);
            }
            fileRecord.put(index, value);
        }
        if (this.delegate != null) {
            this.delegate.writeRecord(fileRecord);
        } else {
            this.csvWriter.write(fileRecord.values());
        }
    }

    public void writeTrial() throws Exception {
        if (this.delegate != null) {
            this.delegate.writeTrial();
        }
    }

    private String tableName() {
        return this.objectName;
    }

    private FileColumns fileColumns() {
        FileColumns fileColumns = new FileColumns();
        for (int index = 0; index < this.columns.size(); index++) {
            H2Column column = this.columns.get(index);
            fileColumns.addColumn(column.getName(), index);
            FileColumn fileColumn = fileColumns.column(column.getName());
            fileColumn.setDesc(column.getName());
        }
        return fileColumns;
    }

    private Object normalize(H2Column column, Object value) {
        value = ShellH2DataUtil.valueStandardization(column, value);
        if (value == null) {
            return null;
        }
        if (ShellH2ColumnUtil.isJsonType(column.getType()) && value instanceof CharSequence text) {
            return unwrapJsonString(text.toString());
        }
        if (value instanceof byte[] bytes) {
            StringBuilder builder = new StringBuilder("X'");
            for (byte item : bytes) {
                builder.append(String.format("%02X", item));
            }
            return builder.append("'").toString();
        }
        if (ShellH2ColumnUtil.isDateType(column.getType())
                || ShellH2ColumnUtil.isTimeType(column.getType())) {
            return ShellH2DataUtil.normalizeTemporalValue(column, value);
        }
        if (value instanceof Date date && config.getDateFormat() != null) {
            return DateUtil.format(date, config.getDateFormat());
        }
        if (value instanceof LocalDateTime date && config.getDateFormat() != null) {
            return DateUtil.format(date, config.getDateFormat());
        }
        if (value instanceof OffsetDateTime date && config.getDateFormat() != null) {
            return DateTimeFormatter.ofPattern(config.getDateFormat() + "XXX")
                    .format(date);
        }
        if (value instanceof Temporal temporal && config.getDateFormat() != null) {
            return DateTimeFormatter.ofPattern(config.getDateFormat()).format(temporal);
        }
        return value;
    }

    private Object unwrapJsonString(String value) {
        if (!value.startsWith("\"") || !value.endsWith("\"")) {
            return value;
        }
        try {
            Object parsed = JSON.parse(value);
            return parsed instanceof String ? parsed : value;
        } catch (Exception ignore) {
            return value;
        }
    }

    private Object escapeXml(Object value) {
        if (value == null) {
            return null;
        }
        return String.valueOf(value)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    @Override
    public void close() throws IOException {
        try {
            if (this.delegate != null) {
                this.delegate.close();
            }
            if (this.csvWriter != null) {
                this.csvWriter.close();
            }
            if (this.sqlWriter != null) {
                this.sqlWriter.close();
            }
        } finally {
            this.config.setFilePath(null);
        }
    }
}
