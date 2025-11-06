package cn.oyzh.easyshell.mysql.data;

import cn.oyzh.common.date.DateUtil;
import cn.oyzh.common.file.LineFileWriter;
import cn.oyzh.common.util.HexUtil;
import cn.oyzh.common.util.TextUtil;
import cn.oyzh.easyshell.mysql.DBDialect;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.mysql.column.MysqlColumns;
import cn.oyzh.easyshell.mysql.data.MysqlDataExportConfig;
import cn.oyzh.easyshell.util.mysql.DBDataUtil;
import cn.oyzh.easyshell.util.mysql.DBUtil;

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
public class MysqlSqlTypeFileWriter extends MysqlTypeFileWriter {

    /**
     * 字段列表
     */
    private MysqlColumns columns;

    /**
     * 导出配置
     */
    private MysqlDataExportConfig config;

    /**
     * 文件写入器
     */
    private final LineFileWriter writer;

    public MysqlSqlTypeFileWriter(String filePath, MysqlDataExportConfig config, MysqlColumns columns) throws FileNotFoundException {
        this.columns = columns;
        this.config = config;
        this.writer = LineFileWriter.create(filePath, config.getCharset());
    }

    @Override
    public void writeObject(Map<String, Object> object) throws Exception {
        String tableName = this.columns.tableName();
        List<MysqlColumn> columnList = this.columns.sortOfPosition();
        final String sqlBase = "INSERT INTO " + DBUtil.wrap(tableName, DBDialect.MYSQL);
        StringBuilder sql = new StringBuilder(sqlBase);
        if (this.config.isIncludeFields()) {
            sql.append("(");
            for (MysqlColumn dbColumn : columnList) {
                sql.append(DBUtil.wrap(dbColumn.getName(), DBDialect.MYSQL)).append(", ");
            }
            if (sql.toString().endsWith(", ")) {
                sql.delete(sql.length() - 2, sql.length());
            }
            sql.append(")");
        }
        sql.append(" VALUES (");
        for (MysqlColumn dbColumn : columnList) {
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
    public Object parameterized(MysqlColumn column, Object value, MysqlDataExportConfig config) {
        if (value == null) {
            return "NULL";
        }
        if (column.supportGeometry()) {
            return "ST_GeomFromText('" + value + "')";
        }
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
        if (column.supportBit()) {
            byte[] bytes = (byte[]) value;
            if (bytes.length == 0) {
                return "NULL";
            }
            return "b'" + TextUtil.byteToBitStr(bytes) + "'";
        }
        if (column.supportEnum()) {
            return "'" + value + "'";
        }
        if (column.supportString()) {
            String str = DBDataUtil.escapeQuotes((String) value);
            return "'" + str + "'";
        }
        return value;
    }
}
