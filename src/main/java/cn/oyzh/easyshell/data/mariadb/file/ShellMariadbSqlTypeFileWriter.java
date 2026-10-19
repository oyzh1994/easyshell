package cn.oyzh.easyshell.data.mariadb.file;

import cn.oyzh.common.date.DateUtil;
import cn.oyzh.common.file.LineFileWriter;
import cn.oyzh.common.util.HexUtil;
import cn.oyzh.common.util.TextUtil;
import cn.oyzh.easyshell.mariadb.column.MariadbColumn;
import cn.oyzh.easyshell.mariadb.column.MariadbColumns;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.data.dto.DBDataExportConfig;
import cn.oyzh.fx.db.util.DBDataUtil;
import cn.oyzh.fx.db.util.DBUtil;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Mariadb Sql类型文件写入器
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbSqlTypeFileWriter extends ShellMariadbTypeFileWriter {

    /**
     * 字段列表
     */
    private MariadbColumns columns;

    /**
     * 导出配置
     */
    private DBDataExportConfig config;

    /**
     * 文件写入器
     */
    private final LineFileWriter writer;

    /**
     * 构造 Mariadb Sql类型文件写入器
     *
     * @param filePath 文件路径
     * @param config   导出配置
     * @param columns  字段列表
     * @throws FileNotFoundException 文件未找到异常
     */
    public ShellMariadbSqlTypeFileWriter(String filePath, DBDataExportConfig config, MariadbColumns columns) throws FileNotFoundException {
        this.columns = columns;
        this.config = config;
        this.writer = LineFileWriter.create(filePath, config.getCharset());
    }

    @Override
    public void writeObject(Map<String, Object> object) throws Exception {
        String tableName = this.columns.tableName();
        List<MariadbColumn> columnList = this.columns.sortOfPosition();
        final String sqlBase = "INSERT INTO " + DBUtil.wrap(tableName, DBDialect.MARIADB);
        StringBuilder sql = new StringBuilder(sqlBase);
        if (this.config.isIncludeFields()) {
            sql.append("(");
            for (MariadbColumn dbColumn : columnList) {
                sql.append(DBUtil.wrap(dbColumn.getName(), DBDialect.MARIADB)).append(", ");
            }
            if (sql.toString().endsWith(", ")) {
                sql.delete(sql.length() - 2, sql.length());
            }
            sql.append(")");
        }
        sql.append(" VALUES (");
        for (MariadbColumn dbColumn : columnList) {
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
        if (this.writer != null) {
            this.writer.close();
            this.config = null;
            this.columns = null;
        }
    }

    @Override
    public Object parameterized(MariadbColumn column, Object value, DBDataExportConfig config) {
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
        //        if (column.supportJson()) {
        //            return "'" + value + "'";
        //        }
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
        //        if (column.supportEnum()) {
        //            return "'" + value + "'";
        //        }
//        if (column.supportString()) {
//            value = DBDataUtil.escapeQuotes((String) value, DBDialect.MARIADB);
            //            return "'" + str + "'";
//        }
        return DBUtil.wrapData(value, DBDialect.MARIADB);
    }
}
