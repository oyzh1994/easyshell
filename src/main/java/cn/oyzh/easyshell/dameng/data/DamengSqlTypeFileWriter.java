package cn.oyzh.easyshell.dameng.data;

import cn.oyzh.common.date.DateUtil;
import cn.oyzh.common.file.LineFileWriter;
import cn.oyzh.common.util.HexUtil;
import cn.oyzh.common.util.TextUtil;
import cn.oyzh.easyshell.dameng.column.DamengColumn;
import cn.oyzh.easyshell.dameng.column.DamengColumns;
import cn.oyzh.easyshell.dameng.data.DamengTypeFileWriter;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.data.dto.DBDataExportConfig;
import cn.oyzh.fx.db.util.DBUtil;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author oyzh
 * @since 2024-09-04
 */
public class DamengSqlTypeFileWriter extends DamengTypeFileWriter {

    /**
     * 字段列表
     */
    private DamengColumns columns;

    /**
     * 导出配置
     */
    private DBDataExportConfig config;

    /**
     * 文件写入器
     */
    private final LineFileWriter writer;

    public DamengSqlTypeFileWriter(String filePath, DBDataExportConfig config, DamengColumns columns) throws FileNotFoundException {
        this.columns = columns;
        this.config = config;
        this.writer = LineFileWriter.create(filePath, config.getCharset());
    }

    @Override
    public void writeObject(Map<String, Object> object) throws Exception {
        String tableName = this.columns.tableName();
        List<DamengColumn> columnList = this.columns.sortOfPosition();
        final String sqlBase = "INSERT INTO " + DBUtil.wrap(tableName, DBDialect.DAMENG);
        StringBuilder sql = new StringBuilder(sqlBase);
        if (this.config.isIncludeFields()) {
            sql.append("(");
            for (DamengColumn dbColumn : columnList) {
                sql.append(DBUtil.wrap(dbColumn.getName(), DBDialect.DAMENG)).append(", ");
            }
            if (sql.toString().endsWith(", ")) {
                sql.delete(sql.length() - 2, sql.length());
            }
            sql.append(")");
        }
        sql.append(" VALUES (");
        for (DamengColumn dbColumn : columnList) {
            Object val = object.get(dbColumn.getName());
            val = this.parameterized(dbColumn, val, this.config);
            sql.append(val).append(", ");
        }
        if (sql.toString().endsWith(", ")) {
            sql.delete(sql.length() - 2, sql.length());
        }
        sql.append(");");
        this.writer.writeLine(sql.toString());
    }

    @Override
    public void close() throws IOException {
        this.writer.close();
        this.config = null;
        this.columns = null;
    }

    @Override
    public Object parameterized(DamengColumn column, Object value, DBDataExportConfig config) {
        if (value == null) {
            return "NULL";
        }
//        if (column.supportGeometry()) {
//            return "ST_GeomFromText('" + value + "')";
//        }
        if (column.isDateType() || column.supportTimestamp()) {
            if (value instanceof LocalDateTime date) {
                return "'" + DateUtil.format(date, config.getDateFormat()) + "'";
            }
            if (value instanceof Date date) {
                return "'" + DateUtil.format(date, config.getDateFormat()) + "'";
            }
        }
        if (column.supportJson()) {
            return "'" + value + "'";
        }
        if (column.supportBinary()) {
            byte[] bytes = (byte[]) value;
            if (bytes.length == 0) {
                return "NULL";
            }
            return "0x" + HexUtil.encodeHexStr(bytes, false);
        }
        if (column.supportBoolean()) {
            if (value instanceof Boolean b) {
                return b ? "1" : "0";
            }
        }
        if (column.supportBit()) {
            byte[] bytes = (byte[]) value;
            if (bytes.length == 0) {
                return "NULL";
            }
            return "b'" + TextUtil.byteToBitStr(bytes) + "'";
        }
//        if (column.supportEnum()) {
//            return "'" + value + "'";
//        }
        if (column.supportString()) {
            String str = TextUtil.escape((String) value);
            return "'" + str + "'";
        }
        return value;
    }
}
