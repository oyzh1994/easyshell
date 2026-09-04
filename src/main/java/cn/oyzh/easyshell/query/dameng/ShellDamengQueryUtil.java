package cn.oyzh.easyshell.query.dameng;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.easyshell.dameng.column.DamengColumn;
import cn.oyzh.easyshell.dameng.function.DamengFunction;
import cn.oyzh.easyshell.dameng.procedure.DamengProcedure;
import cn.oyzh.easyshell.dameng.schema.DamengSchema;
import cn.oyzh.easyshell.dameng.table.DamengTable;
import cn.oyzh.easyshell.dameng.view.DamengView;
import cn.oyzh.easyshell.util.dameng.ShellDamengUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author oyzh
 * @since 2024/2/21
 */
public class ShellDamengQueryUtil {

    /**
     * 0未初始化
     * 1 初始化中
     * 2 已初始化
     */
    private static int indexStatus = 0;

    /**
     * 关键字
     */
    private static final List<String> DB_KEYWORDS = new ArrayList<>();

    /**
     * 数据库
     */
    private static final List<DamengSchema> DB_SCHEMAS = new ArrayList<>();

    /**
     * 表
     */
    private static final List<DamengTable> DB_TABLES = new CopyOnWriteArrayList<>();

    /**
     * 视图
     */
    private static final List<DamengView> DB_VIEWS = new CopyOnWriteArrayList<>();

    /**
     * 函数
     */
    private static final List<DamengFunction> DB_FUNCTIONS = new CopyOnWriteArrayList<>();

    /**
     * 过程
     */
    private static final List<DamengProcedure> DB_PROCEDURES = new CopyOnWriteArrayList<>();

    /**
     * 字段
     */
    private static final List<DamengColumn> DB_COLUMNS = new CopyOnWriteArrayList<>();

    static {
        // dml
        DB_KEYWORDS.add("SELECT");
        DB_KEYWORDS.add("UPDATE");
        DB_KEYWORDS.add("DELETE");
        DB_KEYWORDS.add("FROM");
        DB_KEYWORDS.add("WHERE");
        DB_KEYWORDS.add("WHEN");
        DB_KEYWORDS.add("INSERT INTO");

        // ddl
        DB_KEYWORDS.add("DATABASE");
        DB_KEYWORDS.add("DATABASES");
        DB_KEYWORDS.add("SHOW");
        DB_KEYWORDS.add("ALTER TABLE");
        DB_KEYWORDS.add("CREATE TABLE");
        DB_KEYWORDS.add("DROP TABLE");
        DB_KEYWORDS.add("CHANGE");
        DB_KEYWORDS.add("COLUMN");
        DB_KEYWORDS.add("CHARACTER SET");
        DB_KEYWORDS.add("COMMENT");
        DB_KEYWORDS.add("FIRST");
        DB_KEYWORDS.add("COLLATE");

        // query
        DB_KEYWORDS.add("AS");
        DB_KEYWORDS.add("LIKE");
        DB_KEYWORDS.add("IN");
        DB_KEYWORDS.add("BETWEEN");
        DB_KEYWORDS.add("AND");
        DB_KEYWORDS.add("OR");
        DB_KEYWORDS.add("NOT");
        DB_KEYWORDS.add("NULL");
        DB_KEYWORDS.add("IS");
        DB_KEYWORDS.add("CASE");
        DB_KEYWORDS.add("THEN");
        DB_KEYWORDS.add("ELSE");
        DB_KEYWORDS.add("END");
        DB_KEYWORDS.add("GROUP BY");
        DB_KEYWORDS.add("ORDER BY");
        DB_KEYWORDS.add("LIMIT");
        DB_KEYWORDS.add("HAVING");
        DB_KEYWORDS.add("ON");
        DB_KEYWORDS.add("JOIN");
        DB_KEYWORDS.add("LEFT JOIN");
        DB_KEYWORDS.add("RIGHT JOIN");
        DB_KEYWORDS.add("CROSS JOIN");
        DB_KEYWORDS.add("FULL JOIN");
        DB_KEYWORDS.add("INNER JOIN");
        DB_KEYWORDS.add("INTERSECT");
        DB_KEYWORDS.add("UNION");
        DB_KEYWORDS.add("UNION ALL");
        DB_KEYWORDS.add("EXCEPT");
        DB_KEYWORDS.add("COUNT");
        DB_KEYWORDS.add("SUM");
        DB_KEYWORDS.add("MAX");
        DB_KEYWORDS.add("MIN");
        DB_KEYWORDS.add("AVG");
        DB_KEYWORDS.add("DISTINCT");
        DB_KEYWORDS.add("ASC");
        DB_KEYWORDS.add("DESC");
        DB_KEYWORDS.add("EXISTS");
        DB_KEYWORDS.add("ANY");
        DB_KEYWORDS.add("ALL");

        // 函数
        DB_KEYWORDS.add("CONCAT");
        DB_KEYWORDS.add("LENGTH");
        DB_KEYWORDS.add("SUBSTRING");
        DB_KEYWORDS.add("UPPER");
        DB_KEYWORDS.add("LOWER");
        DB_KEYWORDS.add("TRIM");
        DB_KEYWORDS.add("ROUND");
    }

    public static List<String> getKeywords() {
        return DB_KEYWORDS;
    }

    public static List<DamengSchema> getSchemas() {
        return DB_SCHEMAS;
    }

    public static List<DamengTable> getTables() {
        return DB_TABLES;
    }

    public static List<DamengView> getViews() {
        return DB_VIEWS;
    }

    public static List<DamengFunction> getFunctions() {
        return DB_FUNCTIONS;
    }

    public static List<DamengProcedure> getProcedures() {
        return DB_PROCEDURES;
    }

    public static List<DamengColumn> getColumns() {
        return DB_COLUMNS;
    }

    /**
     * 更新索引
     *
     * @param client 客户端
     */
    public static void updateIndex(ShellDamengClient client) {
        Runnable task = () -> {
            if (indexStatus == 0) {
                try {
                    indexStatus = 1;
                    DB_VIEWS.clear();
                    DB_TABLES.clear();
                    DB_COLUMNS.clear();
                    DB_FUNCTIONS.clear();
                    DB_PROCEDURES.clear();
                    DB_SCHEMAS.clear();
                    // 更新库索引
                    List<DamengSchema> databases = client.selectSchemas();
                    DB_SCHEMAS.addAll(databases);
                    List<Runnable> tasks = new ArrayList<>();
                    // 更新表索引
                    for (DamengSchema schema : DB_SCHEMAS) {
                        if (!ShellDamengUtil.isInternalDatabase(schema.getName())) {
                            tasks.add(() -> {
                                List<DamengTable> tables = client.selectTablesSimple(schema.getName());
                                DB_TABLES.addAll(tables);
                            });
                        }
                    }
                    // 更新视图索引
                    for (DamengSchema schema : DB_SCHEMAS) {
                        if (!ShellDamengUtil.isInternalDatabase(schema.getName())) {
                            tasks.add(() -> {
                                List<DamengView> views = client.selectViewsSimple(schema.getName());
                                DB_VIEWS.addAll(views);
                            });
                        }
                    }
                    // 更新函数索引
                    for (DamengSchema schema : DB_SCHEMAS) {
                        if (!ShellDamengUtil.isInternalDatabase(schema.getName())) {
                            tasks.add(() -> {
                                List<DamengFunction> functions = client.selectFunctionsSimple(schema.getName());
                                DB_FUNCTIONS.addAll(functions);
                            });
                        }
                    }
                    // 更新过程索引
                    for (DamengSchema schema : DB_SCHEMAS) {
                        if (!ShellDamengUtil.isInternalDatabase(schema.getName())) {
                            tasks.add(() -> {
                                List<DamengProcedure> procedures = client.selectProceduresSimple(schema.getName());
                                DB_PROCEDURES.addAll(procedures);
                            });
                        }
                    }
                    // 异步执行
                    ThreadUtil.submit(tasks);
                    indexStatus = 2;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    indexStatus = 0;
                }
            }
        };
        ThreadUtil.start(task);
    }
}
