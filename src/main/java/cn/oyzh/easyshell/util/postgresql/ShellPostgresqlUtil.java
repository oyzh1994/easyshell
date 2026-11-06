package cn.oyzh.easyshell.util.postgresql;

import cn.oyzh.common.function.ExceptionConsumer;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.postgresql.ShellPostgresqlClient;
import cn.oyzh.easyshell.postgresql.column.PostgresqlColumn;
import cn.oyzh.easyshell.postgresql.column.PostgresqlColumns;
import cn.oyzh.easyshell.postgresql.record.PostgresqlRecord;
import cn.oyzh.easyshell.postgresql.routine.PostgresqlRoutineParam;
import cn.oyzh.easyshell.postgresql.record.PostgresqlRecordPrimaryKey;
import cn.oyzh.easyshell.postgresql.table.PostgresqlTable;
import cn.oyzh.fx.db.DBDialect;
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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * PostgreSQL工具类
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellPostgresqlUtil {

    public static boolean ENABLE_PRINT_METADATA = true;

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
        // 模板库
        if (StringUtil.equalsAnyIgnoreCase(dbName, "POSTGRES", "TEMPLATE0", "TEMPLATE1")) {
            return true;
        }
        // 系统模式
        if (StringUtil.equalsAnyIgnoreCase(dbName, "PG_CATALOG", "INFORMATION_SCHEMA", "PG_TOAST")) {
            return true;
        }
        // 临时模式，如pg_temp_1、pg_toast_temp_1
        return StringUtil.startWithIgnoreCase(dbName, "PG_TEMP_")
                || StringUtil.startWithIgnoreCase(dbName, "PG_TOAST_TEMP_");
    }

    /**
     * 打印元数据
     *
     * @param resultSet 结果集
     * @throws SQLException 异常
     */
    public static void printMetaData(ResultSet resultSet) throws SQLException {
        if (ENABLE_PRINT_METADATA) {
            // 获取结果集元数据
            ResultSetMetaData metaData = resultSet.getMetaData();
            // 获取列数
            int columnCount = metaData.getColumnCount();
            // 遍历结果集并输出列名
            for (int i = 1; i <= columnCount; i++) {
                // 获取列名
                String columnName = metaData.getColumnName(i);
                JulLog.info("Column Name: {}", columnName);
            }
            JulLog.info("printMetaData======================>");
        }
    }

    /**
     * 包装名称，PostgreSQL使用双引号包裹标识符
     *
     * @param name    名称
     * @param dialect 方言
     * @return 结果
     */
    public static String wrap(String name, DBDialect dialect) {
        if (name == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        if (dialect == DBDialect.POSTGRESQL) {
            if (!name.startsWith("\"")) {
                builder.append("\"");
            }
            builder.append(name);
            if (!name.endsWith("\"")) {
                builder.append("\"");
            }
        }
        return builder.toString();
    }

    /**
     * 生成删除函数/过程的SQL，必须带输入参数签名，避免重载时名称不唯一。
     *
     * @param schema    模式
     * @param name      名称
     * @param params    参数列表
     * @param procedure 是否为过程
     * @return SQL
     */
    public static String dropRoutineSql(String schema, String name, List<PostgresqlRoutineParam> params, boolean procedure) {
        List<String> args = new ArrayList<>();
        if (params != null) {
            for (PostgresqlRoutineParam param : params) {
                String mode = param.getMode();
                if (StringUtil.containsIgnoreCase(mode, "TABLE")) {
                    continue;
                }
                boolean variadic = StringUtil.containsIgnoreCase(mode, "VARIADIC");
                boolean input = StringUtil.isBlank(mode)
                        || StringUtil.containsIgnoreCase(mode, "IN")
                        || variadic;
                if (!input) {
                    continue;
                }
                String type = param.getType();
                if (StringUtil.isBlank(type)) {
                    continue;
                }
                args.add(variadic ? "VARIADIC " + type : type);
            }
        }
        return "DROP " + (procedure ? "PROCEDURE" : "FUNCTION") + " IF EXISTS "
                + DBUtil.wrap(schema, name, DBDialect.POSTGRESQL)
                + "(" + String.join(", ", args) + ");";
    }

    /**
     * 初始化主键
     *
     * @param columns 字段列表
     * @param record  记录
     * @return 主键
     */
    public static PostgresqlRecordPrimaryKey initPrimaryKey(PostgresqlColumns columns, PostgresqlRecord record) {
        if (columns == null || columns.isEmpty()) {
            return null;
        }
        PostgresqlColumn column = CollectionUtil.getFirst(columns.primaryKeys());
        if (column == null) {
            return null;
        }
        PostgresqlRecordPrimaryKey pk = new PostgresqlRecordPrimaryKey();
        pk.init(column, record);
        return pk;
    }

    /**
     * 对表进行排序，被外键引用的父表排在前面，子表排在后面
     *
     * @param tables   表列表
     * @param dbClient PostgreSQL客户端
     * @param handler  数据处理器
     * @throws InterruptedException 中断异常
     */
    public static void sortTablesInHandler(List<PostgresqlTable> tables, ShellPostgresqlClient dbClient, DataHandler handler) throws InterruptedException {
        if (tables == null || tables.isEmpty()) {
            return;
        }
        // 表名 -> 表对象映射
        Map<String, PostgresqlTable> tableMap = tables.stream()
                .collect(Collectors.toMap(PostgresqlTable::getName, t -> t, (a, b) -> a));
        Set<String> tableNames = tableMap.keySet();
        Map<String, Integer> tableIndex = new HashMap<>();
        for (int i = 0; i < tables.size(); i++) {
            tableIndex.putIfAbsent(tables.get(i).getName(), i);
        }

        // 邻接表: 父表 -> 依赖它的子表集合
        Map<String, Set<String>> dependents = new HashMap<>();
        // 每个表的入度（被引用次数）
        Map<String, Integer> inDegree = new HashMap<>();
        for (PostgresqlTable table : tables) {
            inDegree.putIfAbsent(table.getName(), 0);
            dependents.putIfAbsent(table.getName(), new LinkedHashSet<>());
        }

        // 查询外键，构建依赖图：子表 -> 父表（子表依赖父表）
        // 查询有外键的表名称
        List<String> fTables = dbClient.selectForeignKeyTables(tables.getFirst().getSchema());
        for (PostgresqlTable table : tables) {
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
        for (PostgresqlTable table : tables) {
            if (inDegree.get(table.getName()) == 0 && queued.add(table.getName())) {
                queue.add(table.getName());
            }
        }

        List<PostgresqlTable> sorted = new ArrayList<>(tables.size());
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
            Set<String> sortedNames = sorted.stream().map(PostgresqlTable::getName).collect(Collectors.toSet());
            for (PostgresqlTable table : tables) {
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
    public static void sortTablesByCallback(ShellPostgresqlClient dbClient, List<PostgresqlTable> tables, ExceptionConsumer<PostgresqlTable> callback) throws Exception {
        if (CollectionUtil.isEmpty(tables)) {
            return;
        }
        String schema = tables.getFirst().getSchema();
        // 有外键的表名称
        Set<String> fTables = new HashSet<>(dbClient.selectForeignKeyTables(schema));
        // 表名 -> 表对象映射
        Map<String, PostgresqlTable> tableMap = tables.stream()
                .collect(Collectors.toMap(PostgresqlTable::getName, t -> t, (a, b) -> a));
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
    private static void doSortTablesByCallback(ShellPostgresqlClient dbClient, String schema, Set<String> fTables, Map<String, PostgresqlTable> tableMap, List<PostgresqlTable> tables, Set<String> handles, ExceptionConsumer<PostgresqlTable> callback) throws Exception {
        for (PostgresqlTable table : tables) {
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
                List<PostgresqlTable> parents = new ArrayList<>();
                for (String parent : dbClient.selectParentTables(schema, tName)) {
                    PostgresqlTable parentTable = tableMap.get(parent);
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
     * 只处理字符串、注释、双引号标识符之外的模式限定，避免改坏数据值。
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
            } else if (c == '"') {
                int end = i + 1;
                while (end < len) {
                    if (sql.charAt(end) == '"') {
                        if (end + 1 < len && sql.charAt(end + 1) == '"') {
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
                    result.append('"').append(target).append('"');
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
                    result.append('"').append(target).append('"');
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
     * 带双引号的形式严格按大小写匹配；不带引号的形式按 PostgreSQL 的标识符折叠规则忽略大小写匹配。
     * 例如（schema = "a"）：
     *   CREATE TABLE "a"."t" (...)      -> CREATE TABLE "t" (...)
     *   ALTER TABLE "a"."c" ... REFERENCES "a"."p" -> ... REFERENCES "p"
     *   CREATE TABLE a.t (...)          -> CREATE TABLE t (...)   （无引号形式，大小写不敏感）
     * 而 "A"."t"、a1.t、x.a.t、列名 "a" 均不会被误伤。
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

        // 1) 带双引号："schema".
        String quoted = "\"" + q + "\"\\.";

        // 2) 不带引号：schema.（PostgreSQL会把未加引号的标识符折叠为小写，因此忽略大小写匹配）
        //    - 前一个字符不能是标识符字符 / $ / 双引号 / 点（Unicode 感知，兼容中文标识符）
        //    - 后一个字符不能是标识符字符 / $（防止 a1. / ab. 误伤）
        String unquoted = "(?<![\\p{L}\\p{N}_$.])" + q + "(?![\\p{L}\\p{N}_$])\\.";

        String result = sql.replaceAll(quoted, "");
        result = Pattern.compile(unquoted, Pattern.CASE_INSENSITIVE).matcher(result).replaceAll("");
        return result;
    }
}
