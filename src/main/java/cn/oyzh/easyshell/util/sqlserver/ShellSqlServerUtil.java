package cn.oyzh.easyshell.util.sqlserver;

import cn.oyzh.common.function.ExceptionConsumer;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sqlserver.ShellSqlServerClient;
import cn.oyzh.easyshell.sqlserver.column.SqlServerColumn;
import cn.oyzh.easyshell.sqlserver.column.SqlServerColumns;
import cn.oyzh.easyshell.sqlserver.record.SqlServerRecord;
import cn.oyzh.easyshell.sqlserver.routine.SqlServerRoutineParam;
import cn.oyzh.easyshell.sqlserver.record.SqlServerRecordPrimaryKey;
import cn.oyzh.easyshell.sqlserver.table.SqlServerTable;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.sql.DBSqlParser;
import cn.oyzh.fx.db.util.DBUtil;
import cn.oyzh.fx.db.data.handler.DataHandler;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * SqlServer工具类
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellSqlServerUtil {

//    public static boolean ENABLE_PRINT_METADATA = true;

    /**
     * INSERT目标表
     */
    private static final Pattern INSERT_TARGET_PATTERN = Pattern.compile(
            "(?is)^\\s*INSERT\\s+INTO\\s+"
                    + "(\\[(?:\\]\\]|[^\\]])+\\]|[A-Za-z_][A-Za-z0-9_$]*)"
                    + "(?:\\s*\\.\\s*(\\[(?:\\]\\]|[^\\]])+\\]|[A-Za-z_][A-Za-z0-9_$]*))?");

    /**
     * 解析INSERT目标表
     *
     * @param sql SQL
     * @return 索引0为模式，索引1为表名；无模式时索引0为null
     */
    public static String[] parseInsertTarget(String sql) {
        if (StringUtil.isBlank(sql)) {
            return null;
        }
        Matcher matcher = INSERT_TARGET_PATTERN.matcher(sql);
        if (!matcher.find()) {
            return null;
        }
        String first = unquoteIdentifier(matcher.group(1));
        String second = matcher.group(2);
        return second == null
                ? new String[]{null, first}
                : new String[]{first, unquoteIdentifier(second)};
    }

    /**
     * 去除标识符引号
     *
     * @param identifier 标识符
     * @return 结果
     */
    private static String unquoteIdentifier(String identifier) {
        if (identifier != null && identifier.startsWith("[") && identifier.endsWith("]")) {
            return identifier.substring(1, identifier.length() - 1).replace("]]", "]");
        }
        return identifier;
    }

    /**
     * 是否内部库
     *
     * @param dbName 数据库名称
     * @return 结果
     */
    public static boolean isInternalDatabase(String dbName) {
        if (StringUtil.isBlank(dbName)) {
            return false;
        }
        return StringUtil.equalsAnyIgnoreCase(dbName,
                "MASTER", "MODEL", "MSDB", "TEMPDB",
                "SYS", "INFORMATION_SCHEMA", "GUEST");
    }

//    /**
//     * 打印元数据
//     *
//     * @param resultSet 结果集
//     * @throws SQLException 异常
//     */
//    public static void printMetaData(ResultSet resultSet) throws SQLException {
//        if (ENABLE_PRINT_METADATA) {
//            // 获取结果集元数据
//            ResultSetMetaData metaData = resultSet.getMetaData();
//            // 获取列数
//            int columnCount = metaData.getColumnCount();
//            // 遍历结果集并输出列名
//            for (int i = 1; i <= columnCount; i++) {
//                // 获取列名
//                String columnName = metaData.getColumnName(i);
//                JulLog.info("Column Name: {}", columnName);
//            }
//            JulLog.info("printMetaData======================>");
//        }
//    }

//    /**
//     * 包装名称，SqlServer使用双引号包裹标识符
//     *
//     * @param name    名称
//     * @param dialect 方言
//     * @return 结果
//     */
//    public static String wrap(String name, DBDialect dialect) {
//        if (name == null) {
//            return "";
//        }
//        StringBuilder builder = new StringBuilder();
//        if (dialect == DBDialect.SQLSERVER) {
//            if (!name.startsWith("\"")) {
//                builder.append("\"");
//            }
//            builder.append(name);
//            if (!name.endsWith("\"")) {
//                builder.append("\"");
//            }
//        }
//        return builder.toString();
//    }

    /**
     * 生成删除函数/过程的SQL，必须带输入参数签名，避免重载时名称不唯一。
     *
     * @param schema    模式
     * @param name      名称
     * @param params    参数列表
     * @param procedure 是否为过程
     * @return SQL
     */
    public static String dropRoutineSql(String schema, String name, List<SqlServerRoutineParam> params, boolean procedure) {
        String objectName = DBUtil.wrap(schema, name, DBDialect.SQLSERVER);
        return "IF OBJECT_ID(N'" + objectName.replace("'", "''") + "', N'"
                + (procedure ? "P" : "FN") + "') IS NOT NULL DROP "
                + (procedure ? "PROCEDURE " : "FUNCTION ") + objectName + ";";
    }

    /**
     * 初始化主键
     *
     * @param columns 字段列表
     * @param record  记录
     * @return 主键
     */
    public static SqlServerRecordPrimaryKey initPrimaryKey(SqlServerColumns columns, SqlServerRecord record) {
        if (columns == null || columns.isEmpty()) {
            return null;
        }
        SqlServerColumn column = CollectionUtil.getFirst(columns.primaryKeys());
        if (column == null) {
            return null;
        }
        SqlServerRecordPrimaryKey pk = new SqlServerRecordPrimaryKey();
        pk.init(column, record);
        return pk;
    }

    /**
     * 对表进行排序，被外键引用的父表排在前面，子表排在后面
     *
     * @param tables   表列表
     * @param dbClient SqlServer客户端
     * @param handler  数据处理器
     * @throws InterruptedException 中断异常
     */
    public static void sortTablesInHandler(List<SqlServerTable> tables, ShellSqlServerClient dbClient, DataHandler handler) throws InterruptedException {
        if (tables == null || tables.isEmpty()) {
            return;
        }
        // 表名 -> 表对象映射
        Map<String, SqlServerTable> tableMap = tables.stream()
                .collect(Collectors.toMap(SqlServerTable::getName, t -> t, (a, b) -> a));
        Set<String> tableNames = tableMap.keySet();
        Map<String, Integer> tableIndex = new HashMap<>();
        for (int i = 0; i < tables.size(); i++) {
            tableIndex.putIfAbsent(tables.get(i).getName(), i);
        }

        // 邻接表: 父表 -> 依赖它的子表集合
        Map<String, Set<String>> dependents = new HashMap<>();
        // 每个表的入度（被引用次数）
        Map<String, Integer> inDegree = new HashMap<>();
        for (SqlServerTable table : tables) {
            inDegree.putIfAbsent(table.getName(), 0);
            dependents.putIfAbsent(table.getName(), new LinkedHashSet<>());
        }

        // 查询外键，构建依赖图：子表 -> 父表（子表依赖父表）
        // 查询有外键的表名称
        List<String> fTables = dbClient.selectForeignKeyTables(tables.getFirst().getSchema());
        for (SqlServerTable table : tables) {
            if (!fTables.contains(table.getName())) {
                continue;
            }
            List<String> parentTables = dbClient.selectParentTables(table.getSchema(), table.getName());
            for (String parentTable : parentTables) {
                handler.checkInterrupt();
                // 仅处理属于当前表列表的引用
                if (parentTable != null && tableNames.contains(parentTable) && !parentTable.equals(table.getName())) {
                    Set<String> children = dependents.computeIfAbsent(parentTable, k -> new LinkedHashSet<>());
                    if (children.add(table.getName())) {
                        inDegree.merge(table.getName(), 1, Integer::sum);
                    }
                }
                // 更新状态
                handler.processed(0);
            }
        }

        // 拓扑排序（BFS / Kahn算法）
        Queue<String> queue = new PriorityQueue<>(Comparator.comparingInt(tableIndex::get));
        Set<String> queued = new LinkedHashSet<>();
        for (SqlServerTable table : tables) {
            if (inDegree.get(table.getName()) == 0 && queued.add(table.getName())) {
                queue.add(table.getName());
            }
        }

        List<SqlServerTable> sorted = new ArrayList<>(tables.size());
        while (!queue.isEmpty()) {
            String tableName = queue.poll();
            sorted.add(tableMap.get(tableName));
            for (String child : dependents.getOrDefault(tableName, Collections.emptySet())) {
                int newDegree = inDegree.merge(child, -1, Integer::sum);
                if (newDegree == 0 && queued.add(child)) {
                    queue.add(child);
                }
            }
        }

        // 存在循环依赖时，将剩余表追加到末尾
        if (sorted.size() < tables.size()) {
            Set<String> sortedNames = sorted.stream().map(SqlServerTable::getName).collect(Collectors.toSet());
            for (SqlServerTable table : tables) {
                if (!sortedNames.contains(table.getName())) {
                    sorted.add(table);
                }
            }
        }

        // 替换原列表内容
        tables.clear();
        tables.addAll(sorted);
    }

    /**
     * 执行表排序并回调，被外键引用的父表先回调，子表后回调
     *
     * @param dbClient 客户端
     * @param tables   传输表
     * @param callback 回调
     */
    public static void sortTablesByCallback(ShellSqlServerClient dbClient, List<SqlServerTable> tables, ExceptionConsumer<SqlServerTable> callback) throws Exception {
        if (CollectionUtil.isEmpty(tables)) {
            return;
        }
        String schema = tables.getFirst().getSchema();
        // 有外键的表名称
        Set<String> fTables = new HashSet<>(dbClient.selectForeignKeyTables(schema));
        // 表名 -> 表对象映射
        Map<String, SqlServerTable> tableMap = tables.stream()
                .collect(Collectors.toMap(SqlServerTable::getName, t -> t, (a, b) -> a));
        // 已处理表名称
        Set<String> handles = new HashSet<>();
        doSortTablesByCallback(dbClient, schema, fTables, tableMap, tables, handles, callback);
    }

    /**
     * 执行表排序并回调
     *
     * @param dbClient 客户端
     * @param schema   模式
     * @param fTables  外键表
     * @param tableMap 表名 -> 表对象映射
     * @param tables   本次处理的表
     * @param handles  已处理表
     * @param callback 回调
     */
    private static void doSortTablesByCallback(ShellSqlServerClient dbClient, String schema, Set<String> fTables, Map<String, SqlServerTable> tableMap, List<SqlServerTable> tables, Set<String> handles, ExceptionConsumer<SqlServerTable> callback) throws Exception {
        for (SqlServerTable table : tables) {
            String tName = table.getName();
            // 已处理或正在处理的表，跳过
            if (handles.contains(tName)) {
                continue;
            }
            // 必须在处理父表之前标记，否则自引用、循环外键会无限递归
            handles.add(tName);
            // 判断是否在外键表
            if (fTables.contains(tName)) {
                // 父表列表
                List<SqlServerTable> parents = new ArrayList<>();
                for (String parent : dbClient.selectParentTables(schema, tName)) {
                    SqlServerTable parentTable = tableMap.get(parent);
                    if (parentTable != null && !StringUtil.equals(parent, tName)) {
                        parents.add(parentTable);
                    }
                }
                // 先处理父表
                doSortTablesByCallback(dbClient, schema, fTables, tableMap, parents, handles, callback);
            }
            // 再处理自身
            if (callback != null) {
                callback.accept(table);
            }
        }
    }

    /**
     * 将SQL中的源模式限定安全地替换为目标模式。
     * 只处理字符串、注释、方括号标识符之外的模式限定，避免改坏数据值。
     *
     * @param sql     SQL
     * @param source  源模式
     * @param target  目标模式
     * @return 替换后的SQL
     */
    public static String replaceSchema(String sql, String source, String target) {
        if (StringUtil.isBlank(sql) || StringUtil.isBlank(source) || StringUtil.isBlank(target)
                || StringUtil.equalsIgnoreCase(source, target)) {
            return sql;
        }
        StringBuilder result = new StringBuilder(sql.length());
        int len = sql.length();
        int i = 0;
        while (i < len) {
            char c = sql.charAt(i);
            if (c == '\'') {
                int end = i + 1;
                while (end < len) {
                    if (sql.charAt(end) == '\'') {
                        if (end + 1 < len && sql.charAt(end + 1) == '\'') {
                            end += 2;
                            continue;
                        }
                        end++;
                        break;
                    }
                    end++;
                }
                result.append(sql, i, Math.min(end, len));
                i = Math.min(end, len);
            } else if (c == '-' && i + 1 < len && sql.charAt(i + 1) == '-') {
                int end = sql.indexOf('\n', i);
                if (end < 0) {
                    end = len;
                }
                result.append(sql, i, end);
                i = end;
            } else if (c == '/' && i + 1 < len && sql.charAt(i + 1) == '*') {
                int end = sql.indexOf("*/", i + 2);
                end = end < 0 ? len : end + 2;
                result.append(sql, i, end);
                i = end;
            } else if (c == '[') {
                int end = i + 1;
                while (end < len) {
                    if (sql.charAt(end) == ']') {
                        if (end + 1 < len && sql.charAt(end + 1) == ']') {
                            end += 2;
                            continue;
                        }
                        end++;
                        break;
                    }
                    end++;
                }
                String identifier = sql.substring(i + 1, Math.max(i + 1, Math.min(end - 1, len)));
                if (StringUtil.equals(identifier, source) && followedByDot(sql, end)) {
                    result.append('[').append(target).append(']');
                } else {
                    result.append(sql, i, Math.min(end, len));
                }
                i = Math.min(end, len);
            } else if (Character.isLetter(c) || c == '_') {
                int end = i + 1;
                while (end < len) {
                    char next = sql.charAt(end);
                    if (!Character.isLetterOrDigit(next) && next != '_' && next != '$') {
                        break;
                    }
                    end++;
                }
                String identifier = sql.substring(i, end);
                if (StringUtil.equalsIgnoreCase(identifier, source) && followedByDot(sql, end)) {
                    result.append('[').append(target).append(']');
                } else {
                    result.append(identifier);
                }
                i = end;
            } else {
                result.append(c);
                i++;
            }
        }
        return result.toString();
    }

    /**
     * DDL迁移时替换Schema。除普通限定名外，还会处理扩展属性参数和
     * DBCC CHECKIDENT中的对象名字符串。
     *
     * @param sql    SQL
     * @param source 源Schema
     * @param target 目标Schema
     * @return 重写后的SQL
     */
    public static String replaceSchemaForDdl(String sql, String source, String target) {
        String result = replaceSchema(sql, source, target);
        if (StringUtil.isBlank(result) || StringUtil.isBlank(source) || StringUtil.isBlank(target)
                || StringUtil.equalsIgnoreCase(source, target)) {
            return result;
        }
        String quotedSource = Pattern.quote(source);
        String quotedTarget = java.util.regex.Matcher.quoteReplacement(target);
        result = Pattern.compile("(?i)(@level0name\\s*=\\s*N?')" + quotedSource + "(')")
                .matcher(result)
                .replaceAll("$1" + quotedTarget + "$2");
        result = Pattern.compile("(?i)(DBCC\\s+CHECKIDENT\\s*\\(\\s*N?'\\[)"
                        + quotedSource + "(\\]\\.)")
                .matcher(result)
                .replaceAll("$1" + quotedTarget + "$2");
        result = Pattern.compile("(?i)(OBJECT_ID\\s*\\(\\s*N?'\\[)"
                        + quotedSource + "(\\]\\.)")
                .matcher(result)
                .replaceAll("$1" + quotedTarget + "$2");
        return result;
    }

    /**
     * 判断指定位置之后是否是模式限定点（允许空白）
     *
     * @param sql   SQL
     * @param index 位置
     * @return 结果
     */
    private static boolean followedByDot(String sql, int index) {
        int i = index;
        while (i < sql.length() && Character.isWhitespace(sql.charAt(i))) {
            i++;
        }
        return i < sql.length() && sql.charAt(i) == '.';
    }

    /**
     * 移除 SQL 中指定模式名前缀，保留对象名。
     * 带方括号的形式严格按大小写匹配；不带引号的形式按 SqlServer 的标识符折叠规则忽略大小写匹配。
     * 例如（schema = "a"）：
     *   CREATE TABLE [a].[t] (...)      -> CREATE TABLE [t] (...)
     *   ALTER TABLE [a].[c] ... REFERENCES [a].[p] -> ... REFERENCES [p]
     *   CREATE TABLE a.t (...)          -> CREATE TABLE t (...)   （无引号形式，大小写不敏感）
     * 而 [A].[t]、a1.t、x.a.t、列名 [a] 均不会被误伤。
     *
     * @param sql    原始 SQL
     * @param schema 模式名（不带引号，带引号的形式需与 DDL 中大小写完全一致，例如 a）
     * @return 处理后的 SQL
     */
    public static String removeSchema(String sql, String schema) {
        if (sql == null || sql.isEmpty() || schema == null || schema.isEmpty()) {
            return sql;
        }

        String q = Pattern.quote(schema);

        // 1) 带方括号：[schema].
        String quoted = "\\[" + q + "\\]\\.";

        // 2) 不带引号：schema.（SqlServer会把未加引号的标识符折叠为小写，因此忽略大小写匹配）
        //    - 前一个字符不能是标识符字符 / $ / 方括号 / 点（Unicode 感知，兼容中文标识符）
        //    - 后一个字符不能是标识符字符 / $（防止 a1. / ab. 误伤）
        String unquoted = "(?<![\\p{L}\\p{N}_$.])" + q + "(?![\\p{L}\\p{N}_$])\\.";

        String result = sql.replaceAll(quoted, "");
        result = Pattern.compile(unquoted, Pattern.CASE_INSENSITIVE).matcher(result).replaceAll("");
        return result;
    }

    /**
     * 按SQL Server的GO分隔符拆分为可执行语句。可编程对象保持在同一个批次中，
     * 普通SQL继续按顶层分号拆分。
     *
     * @param sql SQL内容
     * @return 可逐条执行的语句
     */
    public static List<String> splitSqlServerBatches(String sql) {
        List<String> result = new ArrayList<>();
        if (StringUtil.isBlank(sql)) {
            return result;
        }
        StringBuilder batch = new StringBuilder();
        for (String line : sql.split("\\R", -1)) {
            if (line.matches("(?i)^\\s*GO(?:\\s+\\d+)?\\s*(?:--.*)?$")) {
                appendSqlServerBatch(result, batch.toString());
                batch.setLength(0);
            } else {
                if (!batch.isEmpty()) {
                    batch.append('\n');
                }
                batch.append(line);
            }
        }
        appendSqlServerBatch(result, batch.toString());
        return result;
    }

    private static void appendSqlServerBatch(List<String> result, String batch) {
        String sql = batch == null ? "" : batch.trim();
        if (sql.isEmpty()) {
            return;
        }
        if (isModuleDefinition(sql) || containsBatchVariable(sql)) {
            result.add(sql);
        } else {
            result.addAll(DBSqlParser.getParser(sql, DBDialect.SQLSERVER).parseSql());
        }
    }

    private static boolean isModuleDefinition(String sql) {
        String normalized = sql.replaceAll("(?m)^\\s*--.*$", " ")
                .replaceAll("(?s)/\\*.*?\\*/", " ")
                .trim();
        return Pattern.compile(
                        "(?is)^\\s*(?:CREATE\\s+(?:OR\\s+ALTER\\s+)?|ALTER\\s+)"
                                + "(?:PROCEDURE|PROC|FUNCTION|VIEW|TRIGGER)\\b")
                .matcher(normalized)
                .find();
    }

    /**
     * 包含T-SQL局部变量的批次必须整体执行，变量作用域不能跨批次。
     */
    private static boolean containsBatchVariable(String sql) {
        return Pattern.compile("(?i)\\b(?:DECLARE|SET|SELECT)\\s+@[A-Za-z_][A-Za-z0-9_]*")
                .matcher(sql)
                .find();
    }

    /**
     * 去除SQL开头的空行、行注释和块注释。
     *
     * @param sql SQL
     * @return 去除前导注释后的SQL
     */
    public static String stripLeadingSqlComments(String sql) {
        if (sql == null) {
            return null;
        }
        String value = sql;
        boolean changed;
        do {
            changed = false;
            String previous = value;
            value = value.replaceFirst("^\\s+", "");
            value = value.replaceFirst("(?s)^/\\*.*?\\*/\\s*", "");
            value = value.replaceFirst("(?m)^--[^\\r\\n]*(?:\\R|$)", "");
            changed = !previous.equals(value);
        } while (changed);
        return value.trim();
    }
}
