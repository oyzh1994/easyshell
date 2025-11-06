package cn.oyzh.easyshell.mysql.data;// package cn.oyzh.easyshell.mysql.data;
//
// import cn.oyzh.common.date.DateUtil;
// import cn.oyzh.common.util.HexUtil;
// import cn.oyzh.common.util.TextUtil;
// import cn.oyzh.easyshell.mysql.DBDialect;
// import cn.oyzh.easyshell.mysql.column.MysqlColumn;
// import cn.oyzh.easyshell.mysql.column.MysqlColumns;
// import cn.oyzh.easyshell.mysql.record.MysqlRecord;
// import cn.oyzh.easyshell.util.mysql.DBDataUtil;
// import cn.oyzh.easyshell.util.mysql.DBUtil;
//
// import java.time.LocalDateTime;
// import java.util.ArrayList;
// import java.util.Date;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;
//
// /**
//  * @author oyzh
//  * @since 2024/09/02
//  */
// public class MysqlDataExportHelper {
//
//     /**
//      * 参数化，json
//      *
//      * @param column 字段
//      * @param value  值
//      * @return 参数化后的值
//      */
//     public static Object parameterizedForJson(MysqlColumn column, Object value, MysqlDataExportConfig config) {
//         if (value == null) {
//             return null;
//         }
//         if (column.supportGeometry()) {
//             return "ST_GeomFromText('" + value + "')";
//         }
//         if (column.isDateType() || column.supportTimestamp()) {
//             if (value instanceof LocalDateTime date) {
//                 return DateUtil.format(date, config.getDateFormat());
//             }
//             if (value instanceof Date date) {
//                 return DateUtil.format(date, config.getDateFormat());
//             }
//         }
//         if (column.supportJson()) {
//             return value.toString();
//         }
//         if (column.supportBinary()) {
//             byte[] bytes = (byte[]) value;
//             if (bytes.length == 0) {
//                 return "";
//             }
//             return "0x" + HexUtil.encodeHexStr(bytes, false);
//         }
//         if (column.supportBit()) {
//             byte[] bytes = (byte[]) value;
//             if (bytes.length == 0) {
//                 return "";
//             }
//             return "b'" + TextUtil.byteToBitStr(bytes) + "'";
//         }
//         if (column.supportEnum()) {
//             return value.toString();
//         }
//         if (column.supportString()) {
//             return DBDataUtil.escapeQuotes((String) value);
//         }
//         if (column.supportInteger() || column.supportDigits()) {
//             return value;
//         }
//         return value.toString();
//     }
//
//     /**
//      * 参数化，xml
//      *
//      * @param column 字段
//      * @param value  值
//      * @return 参数化后的值
//      */
//     public static Object parameterizedForXml(MysqlColumn column, Object value, MysqlDataExportConfig config) {
//         if (value == null) {
//             return null;
//         }
//         if (column.supportGeometry()) {
//             return "ST_GeomFromText('" + value + "')";
//         }
//         if (column.isDateType() || column.supportTimestamp()) {
//             if (value instanceof LocalDateTime date) {
//                 return DateUtil.format(date, config.getDateFormat());
//             }
//             if (value instanceof Date date) {
//                 return DateUtil.format(date, config.getDateFormat());
//             }
//         }
//         if (column.supportJson()) {
//             return value.toString();
//         }
//         if (column.supportBinary()) {
//             byte[] bytes = (byte[]) value;
//             if (bytes.length == 0) {
//                 return "";
//             }
//             return "0x" + HexUtil.encodeHexStr(bytes, false);
//         }
//         if (column.supportBit()) {
//             byte[] bytes = (byte[]) value;
//             if (bytes.length == 0) {
//                 return "";
//             }
//             return "b'" + TextUtil.byteToBitStr(bytes) + "'";
//         }
//         if (column.supportEnum()) {
//             return value.toString();
//         }
//         if (column.supportString()) {
//             return DBDataUtil.escapeQuotes((String) value);
//         }
//         if (column.supportInteger() || column.supportDigits()) {
//             return value;
//         }
//         return value.toString();
//     }
//
//     /**
//      * 参数化，csv
//      *
//      * @param column 字段
//      * @param value  值
//      * @return 参数化后的值
//      */
//     public static Object parameterizedForCsv(MysqlColumn column, Object value, MysqlDataExportConfig config) {
//         if (value == null) {
//             return "";
//         }
//         if (column.supportGeometry()) {
//             return "\"ST_GeomFromText('" + value + "')\"";
//         }
//         if (column.isDateType() || column.supportTimestamp()) {
//             if (value instanceof LocalDateTime date) {
//                 return "\"" + DateUtil.format(date, config.getDateFormat()) + "\"";
//             }
//             if (value instanceof Date date) {
//                 return "\"" + DateUtil.format(date, config.getDateFormat()) + "\"";
//             }
//         }
//         if (column.supportBinary()) {
//             byte[] bytes = (byte[]) value;
//             if (bytes.length == 0) {
//                 return "";
//             }
//             return "0x" + HexUtil.encodeHexStr(bytes, false);
//         }
//         if (column.supportBit()) {
//             byte[] bytes = (byte[]) value;
//             if (bytes.length == 0) {
//                 return "";
//             }
//             return "\"b'" + TextUtil.byteToBitStr(bytes) + "'\"";
//         }
//         if (column.supportString()) {
//             return "\"" + DBDataUtil.escapeQuotes((String) value) + "\"";
//         }
//         return "\"" + value + "\"";
//     }
//
//     /**
//      * 参数化，sql
//      *
//      * @param column 字段
//      * @param value  值
//      * @return 参数化后的值
//      */
//     public static Object parameterizedForSql(MysqlColumn column, Object value, MysqlDataExportConfig config) {
//         if (value == null) {
//             return "NULL";
//         }
//         if (column.supportGeometry()) {
//             return "ST_GeomFromText('" + value + "')";
//         }
//         if (column.isDateType() || column.supportTimestamp()) {
//             if (value instanceof LocalDateTime date) {
//                 return "'" + DateUtil.format(date, config.getDateFormat()) + "'";
//             }
//             if (value instanceof Date date) {
//                 return "'" + DateUtil.format(date, config.getDateFormat()) + "'";
//             }
//         }
//         if (column.supportJson()) {
//             return "'" + value + "'";
//         }
//         if (column.supportBinary()) {
//             byte[] bytes = (byte[]) value;
//             if (bytes.length == 0) {
//                 return "NULL";
//             }
//             return "0x" + HexUtil.encodeHexStr(bytes, false);
//         }
//         if (column.supportBit()) {
//             byte[] bytes = (byte[]) value;
//             if (bytes.length == 0) {
//                 return "NULL";
//             }
//             return "b'" + TextUtil.byteToBitStr(bytes) + "'";
//         }
//         if (column.supportEnum()) {
//             return "'" + value + "'";
//         }
//         if (column.supportString()) {
//             String str = DBDataUtil.escapeQuotes((String) value);
//             return "'" + str + "'";
//         }
//         return value;
//     }
//
//     /**
//      * 参数化，html
//      *
//      * @param column 字段
//      * @param value  值
//      * @return 参数化后的值
//      */
//     public static Object parameterizedForHtml(MysqlColumn column, Object value, MysqlDataExportConfig config) {
//         if (value == null) {
//             return "";
//         }
//         if (column.supportGeometry()) {
//             return "ST_GeomFromText('" + value + "')";
//         }
//         if (column.isDateType() || column.supportTimestamp()) {
//             if (value instanceof LocalDateTime date) {
//                 return DateUtil.format(date, config.getDateFormat());
//             }
//             if (value instanceof Date date) {
//                 return DateUtil.format(date, config.getDateFormat());
//             }
//         }
//         if (column.supportJson()) {
//             return value.toString();
//         }
//         if (column.supportBinary()) {
//             byte[] bytes = (byte[]) value;
//             if (bytes.length == 0) {
//                 return "";
//             }
//             return "0x" + HexUtil.encodeHexStr(bytes, false);
//         }
//         if (column.supportBit()) {
//             byte[] bytes = (byte[]) value;
//             if (bytes.length == 0) {
//                 return "";
//             }
//             return "b'" + TextUtil.byteToBitStr(bytes) + "'";
//         }
//         if (column.supportEnum()) {
//             return value.toString();
//         }
//         if (column.supportString()) {
//             return DBDataUtil.escapeQuotes((String) value);
//         }
//         if (column.supportInteger() || column.supportDigits()) {
//             return value;
//         }
//         return value.toString();
//     }
//
//     /**
//      * 参数化，xls
//      *
//      * @param column 字段
//      * @param value  值
//      * @return 参数化后的值
//      */
//     public static Object parameterizedForXls(MysqlColumn column, Object value, MysqlDataExportConfig config) {
//         if (value == null) {
//             return null;
//         }
//         if (column.supportGeometry()) {
//             return "ST_GeomFromText('" + value + "')";
//         }
//         if (column.isDateType() || column.supportTimestamp()) {
//             if (value instanceof LocalDateTime date) {
//                 return DateUtil.format(date, config.getDateFormat());
//             }
//             if (value instanceof Date date) {
//                 return DateUtil.format(date, config.getDateFormat());
//             }
//         }
//         if (column.supportJson()) {
//             return value.toString();
//         }
//         if (column.supportBinary()) {
//             byte[] bytes = (byte[]) value;
//             if (bytes.length == 0) {
//                 return "";
//             }
//             return "0x" + HexUtil.encodeHexStr(bytes, false);
//         }
//         if (column.supportBit()) {
//             byte[] bytes = (byte[]) value;
//             if (bytes.length == 0) {
//                 return "";
//             }
//             return "b'" + TextUtil.byteToBitStr(bytes) + "'";
//         }
//         if (column.supportEnum()) {
//             return value.toString();
//         }
//         if (column.supportString()) {
//             return DBDataUtil.escapeQuotes((String) value);
//         }
//         return value;
//     }
//
//     /**
//      * 转换为导出sql
//      *
//      * @param columns 字段列表
//      * @param records 记录
//      * @param config  配置
//      * @return 插入sql
//      */
//     public static List<String> toExportSql(MysqlColumns columns, List<MysqlRecord> records, MysqlDataExportConfig config) {
//         List<String> list = new ArrayList<>();
//         String tableName = columns.tableName();
//         List<MysqlColumn> columnList = columns.sortOfPosition();
//         final String sqlBase = "INSERT INTO " + DBUtil.wrap(tableName, DBDialect.MYSQL);
//         for (MysqlRecord record : records) {
//             StringBuilder sql = new StringBuilder(sqlBase);
//             if (config.isIncludeFields()) {
//                 sql.append("(");
//                 for (MysqlColumn dbColumn : columnList) {
//                     sql.append(DBUtil.wrap(dbColumn.getName(), DBDialect.MYSQL)).append(", ");
//                 }
//                 if (sql.toString().endsWith(", ")) {
//                     sql.delete(sql.length() - 2, sql.length());
//                 }
//                 sql.append(")");
//             }
//             sql.append(" VALUES (");
//             for (MysqlColumn dbColumn : columnList) {
//                 Object value = record.getValue(dbColumn.getName());
//                 value = parameterizedForSql(dbColumn, value, config);
//                 sql.append(value).append(", ");
//             }
//             if (sql.toString().endsWith(", ")) {
//                 sql.delete(sql.length() - 2, sql.length());
//             }
//             sql.append(");");
//             list.add(sql.toString());
//         }
//         return list;
//     }
//
//     /**
//      * 转换为插入json
//      *
//      * @param columns 字段列表
//      * @param records 记录
//      * @return 插入json
//      */
//     public static List<Map<String, Object>> toExportJson(MysqlColumns columns, List<MysqlRecord> records, MysqlDataExportConfig config) {
//         List<Map<String, Object>> list = new ArrayList<>();
//         List<MysqlColumn> columnList = columns.sortOfPosition();
//         for (MysqlRecord record : records) {
//             Map<String, Object> object = new HashMap<>();
//             for (MysqlColumn dbColumn : columnList) {
//                 Object value = record.getValue(dbColumn.getName());
//                 value = parameterizedForJson(dbColumn, value, config);
//                 object.put(dbColumn.getName(), value);
//             }
//             list.add(object);
//         }
//         return list;
//     }
//
//     /**
//      * 转换为插入xml
//      *
//      * @param columns 字段列表
//      * @param records 记录
//      * @return 插入xml
//      */
//     public static List<Map<String, Object>> toExportXml(MysqlColumns columns, List<MysqlRecord> records, MysqlDataExportConfig config) {
//         List<Map<String, Object>> list = new ArrayList<>();
//         List<MysqlColumn> columnList = columns.sortOfPosition();
//         for (MysqlRecord record : records) {
//             Map<String, Object> object = new HashMap<>();
//             for (MysqlColumn dbColumn : columnList) {
//                 Object value = record.getValue(dbColumn.getName());
//                 value = parameterizedForXml(dbColumn, value, config);
//                 object.put(dbColumn.getName(), value);
//             }
//             list.add(object);
//         }
//         return list;
//     }
//
//     /**
//      * 转换为插入csv
//      *
//      * @param columns 字段列表
//      * @param records 记录
//      * @return 插入csv
//      */
//     public static List<List<Object>> toExportCsv(MysqlColumns columns, List<MysqlRecord> records, MysqlDataExportConfig config) {
//         List<List<Object>> list = new ArrayList<>();
//         List<MysqlColumn> columnList = columns.sortOfPosition();
//         for (MysqlRecord record : records) {
//             List<Object> object = new ArrayList<>();
//             for (MysqlColumn dbColumn : columnList) {
//                 Object value = record.getValue(dbColumn.getName());
//                 value = parameterizedForCsv(dbColumn, value, config);
//                 object.add(value);
//             }
//             list.add(object);
//         }
//         return list;
//     }
//
//     /**
//      * 转换为插入html
//      *
//      * @param columns 字段列表
//      * @param records 记录
//      * @return 插入html
//      */
//     public static List<List<Object>> toExportHtml(MysqlColumns columns, List<MysqlRecord> records, MysqlDataExportConfig config) {
//         List<List<Object>> list = new ArrayList<>();
//         List<MysqlColumn> columnList = columns.sortOfPosition();
//         for (MysqlRecord record : records) {
//             List<Object> object = new ArrayList<>();
//             for (MysqlColumn dbColumn : columnList) {
//                 Object value = record.getValue(dbColumn.getName());
//                 value = parameterizedForHtml(dbColumn, value, config);
//                 object.add(value);
//             }
//             list.add(object);
//         }
//         return list;
//     }
//
//     /**
//      * 转换为插入xls
//      *
//      * @param columns 字段列表
//      * @param records 记录
//      * @return 插入xls
//      */
//     public static List<List<Object>> toExportXls(MysqlColumns columns, List<MysqlRecord> records, MysqlDataExportConfig config) {
//         List<List<Object>> list = new ArrayList<>();
//         List<MysqlColumn> columnList = columns.sortOfPosition();
//         for (MysqlRecord record : records) {
//             List<Object> object = new ArrayList<>();
//             for (MysqlColumn dbColumn : columnList) {
//                 Object value = record.getValue(dbColumn.getName());
//                 value = parameterizedForXls(dbColumn, value, config);
//                 object.add(value);
//             }
//             list.add(object);
//         }
//         return list;
//     }
//
// }
