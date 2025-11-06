package cn.oyzh.easyshell.mysql.data;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.xls.WorkbookHelper;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.mysql.column.MysqlColumns;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author oyzh
 * @since 2024-09-04
 */
public class MysqlExcelTypeFileWriter extends MysqlTypeFileWriter {

    /**
     * 字段列表
     */
    private MysqlColumns columns;

    /**
     * 导出配置
     */
    private MysqlDataExportConfig config;

    /**
     * xls工作薄
     */
    private Workbook workbook;

    /**
     * xls行记录
     */
    private int xlsRowIndex = 1;

    private String filePath;

    public MysqlExcelTypeFileWriter(String filePath, MysqlDataExportConfig config, MysqlColumns columns) throws IOException {
        this.columns = columns;
        this.config = config;
        this.filePath = filePath;
        boolean isXlsx = StringUtil.endWithIgnoreCase(filePath, ".xlsx");
        this.workbook = WorkbookHelper.create(isXlsx);
    }

    @Override
    public void writeHeader() throws Exception {
        // 重置行索引
        this.xlsRowIndex = 1;
        // 创建一个新的工作表sheet
        Sheet sheet = this.workbook.createSheet(columns.tableName());
        // 创建列名行
        Row headerRow = sheet.createRow(0);
        // 写入列名
        List<MysqlColumn> columnList = columns.sortOfPosition();
        for (int i = 0; i < columnList.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnList.get(i).getName());
        }
        // 写入数据
        WorkbookHelper.write(this.workbook, this.filePath);
    }

    private void writeObject(Map<String, Object> object, boolean flush) throws Exception {
        // 处理数据
        Object[] values = new Object[object.size()];
        for (Map.Entry<String, Object> entry : object.entrySet()) {
            int index = this.columns.index(entry.getKey());
            MysqlColumn column = this.columns.column(entry.getKey());
            Object val = this.parameterized(column, entry.getValue(), this.config);
            values[index] = val;
        }
        // 获取当前页
        Sheet sheet = WorkbookHelper.getActiveSheet(this.workbook);
        // 创建数据行
        Row row = sheet.createRow(this.xlsRowIndex++);
        // 填充数据列
        for (int i = 0; i < values.length; i++) {
            Cell cell = row.createCell(i);
            Object val = values[i];
            switch (val) {
                case null -> {
                }
                case Date v -> cell.setCellValue(v);
                case Double v -> cell.setCellValue(v);
                case String v -> cell.setCellValue(v);
                case Boolean v -> cell.setCellValue(v);
                case Calendar v -> cell.setCellValue(v);
                case LocalDate v -> cell.setCellValue(v);
                case LocalDateTime v -> cell.setCellValue(v);
                case Number v -> cell.setCellValue(v.doubleValue());
                default -> cell.setCellValue(val.toString());
            }
        }
        // 写入数据
        if (flush) {
            WorkbookHelper.write(this.workbook, this.filePath);
        }
    }

    @Override
    public void writeObject(Map<String, Object> object) throws Exception {
        this.writeObject(object, true);
    }

    @Override
    public void writeObjects(List<Map<String, Object>> objects) throws Exception {
        for (Map<String, Object> object : objects) {
            this.writeObject(object, false);
        }
        // 写入数据
        WorkbookHelper.write(this.workbook, this.filePath);
    }

    @Override
    public void close() throws IOException {
        this.workbook.close();
        this.workbook = null;
        this.config = null;
        this.columns = null;
        this.filePath = null;
    }

    @Override
    public Object parameterized(MysqlColumn column, Object value, MysqlDataExportConfig config) {
        if (value == null) {
            return null;
        }
        return super.parameterized(column, value, config);
    }
}
