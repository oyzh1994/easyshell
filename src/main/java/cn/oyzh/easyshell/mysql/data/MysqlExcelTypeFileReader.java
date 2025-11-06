package cn.oyzh.easyshell.mysql.data;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.xls.WorkbookHelper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author oyzh
 * @since 2024-09-03
 */
public class MysqlExcelTypeFileReader extends MysqlTypeFileReader {

    /**
     * xml读取器
     */
    private Workbook workbook;

    /**
     * 字段列表
     */
    private List<String> columns;

    /**
     * 导入配置
     */
    private MysqlDataImportConfig config;

    /**
     * 当前行索引
     */
    private Integer currentRowIndex;

    public MysqlExcelTypeFileReader( File file, MysqlDataImportConfig config) throws Exception {
        this.config = config;
        boolean isXlsx = StringUtil.endWithIgnoreCase(file.getName(), ".xlsx");
        this.workbook = WorkbookHelper.create(isXlsx, file);
        this.init();
    }

    @Override
    protected void init() throws Exception {
        this.columns = new ArrayList<>();
        Row row = this.workbook.getSheetAt(0).getRow(this.config.getColumnIndex());
        for (Cell cell : row) {
            this.columns.add(cell.getStringCellValue());
        }
        this.currentRowIndex = this.config.getDataStartIndex();
    }

    @Override
    public Map<String, Object> readObject() {
        Sheet sheet = this.workbook.getSheetAt(0);
        Row row = sheet.getRow(this.currentRowIndex++);
        if (row != null) {
            Map<String, Object> map = new HashMap<>();
            for (Cell cell : row) {
                Object val;
                CellType cellType = cell.getCellType();
                if (cellType == CellType.BOOLEAN) {
                    val = cell.getBooleanCellValue();
                } else if (cellType == CellType.NUMERIC) {
                    if (DateUtil.isCellDateFormatted(cell)) {
                        val = cell.getDateCellValue();
                    } else {
                        val = cell.getNumericCellValue();
                    }
                } else if (cellType == CellType.STRING) {
                    val = cell.getStringCellValue();
                } else {
                    val = cell.getStringCellValue();
                }
                String columnName = this.columns.get(cell.getColumnIndex());
                map.put(columnName, val);
            }
            return map;
        }
        return null;
    }

    @Override
    public void close() {
        try {
            this.workbook.close();
            this.workbook = null;
            this.config = null;
            this.columns = null;
            this.currentRowIndex = null;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
