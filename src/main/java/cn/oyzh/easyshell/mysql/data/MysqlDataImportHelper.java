package cn.oyzh.easyshell.mysql.data;

import cn.oyzh.common.date.DateUtil;
import cn.oyzh.easyshell.db.DBDialect;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.mysql.column.MysqlColumns;
import cn.oyzh.easyshell.mysql.record.MysqlRecord;
import cn.oyzh.easyshell.util.mysql.DBDataUtil;
import cn.oyzh.easyshell.util.mysql.DBUtil;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author oyzh
 * @since 2024/09/02
 */
public class MysqlDataImportHelper {

    /**
     * 参数化
     *
     * @param column 字段
     * @param value  值
     * @return 参数化后的值
     */
    public static Object parameterized(MysqlColumn column, Object value, MysqlDataImportConfig config) throws ParseException {
        if (value == null) {
            return null;
        }
        if (value.toString().isEmpty()) {
            return null;
        }
        if (column.isDateType()) {
            if (value instanceof CharSequence date) {
                Date date1 = DateUtil.parse(date, config.getDateFormat());
                return DateUtil.format(date1, "yyyy-MM-dd HH:mm:ss");
            }
        }
        if (column.supportTimestamp()) {
            if (value instanceof CharSequence date) {
                LocalDateTime date1 = DateUtil.parseLocalDateTime(date, config.getDateFormat());
                return DateUtil.format(date1, "yyyy-MM-dd HH:mm:ss");
            }
            if (value instanceof Date date) {
                return DateUtil.format(date, "yyyy-MM-dd HH:mm:ss");
            }
        }
        if (column.supportString()) {
            return DBDataUtil.escapeQuotes(value.toString());
        }
        return value;
    }

    /**
     * 转换为插入sql
     *
     * @param columns 字段列表
     * @param records 记录
     * @param config  配置
     * @return 插入sql
     */
    public static List<String> toInsertSql(MysqlColumns columns, List<MysqlRecord> records, MysqlDataImportConfig config) throws Exception {
        List<String> insertSql = new ArrayList<>();
        for (MysqlRecord record : records) {
            StringBuilder sql = new StringBuilder("INSERT INTO ");
            sql.append(DBUtil.wrap(columns.tableName(), DBDialect.MYSQL));
            sql.append("(");
            for (MysqlColumn column : columns) {
                sql.append(DBUtil.wrap(column.getName(), DBDialect.MYSQL)).append(", ");
            }
            sql.deleteCharAt(sql.length() - 2);
            sql.append(") VALUES (");
            for (MysqlColumn column : columns) {
                Object val = record.getValue(column.getName());
                val = DBUtil.unwrapData(val);
                val = parameterized(column, val, config);
                sql.append(DBUtil.wrapData(val)).append(", ");
            }
            sql.deleteCharAt(sql.length() - 2);
            sql.append(")");
            insertSql.add(sql.toString());
            System.out.println(sql);
        }
        return insertSql;
    }
}
