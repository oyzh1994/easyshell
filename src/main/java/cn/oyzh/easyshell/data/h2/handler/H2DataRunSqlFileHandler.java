package cn.oyzh.easyshell.data.h2.handler;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.data.h2.util.H2SqlSchemaRewriter;
import cn.oyzh.easyshell.h2.generator.H2GeneratorUtil;
import cn.oyzh.easyshell.h2.ShellH2Client;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.data.handler.DBDataRunFileHandler;
import cn.oyzh.fx.db.sql.DBSqlParser;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * H2 SQL 文件执行处理器。
 *
 * @since 2026-11-02
 */
public class H2DataRunSqlFileHandler extends DBDataRunFileHandler<String> {

    private static final Pattern CREATE_TRIGGER = Pattern.compile(
            "(?is)^\\s*CREATE\\s+(?:FORCE\\s+)?TRIGGER\\s+"
                    + "(?:(?:\"((?:[^\"]|\"\")*)\"|([A-Za-z_][\\w$]*))\\s*\\.\\s*)?"
                    + "(?:\"((?:[^\"]|\"\")*)\"|([A-Za-z_][\\w$]*))");

    private final ShellH2Client client;

    private final String schema;

    public H2DataRunSqlFileHandler(ShellH2Client client, String dbName) {
        super(dbName);
        this.client = client;
        this.schema = client.schema(dbName);
        this.dialect = DBDialect.H2;
    }

    @Override
    protected void doActionInternal() throws Exception {
        this.message("Run SQL File Starting");
        try {
            String content = Files.readString(this.file.toPath(), StandardCharsets.UTF_8);
            String sourceSchema = H2SqlSchemaRewriter.sourceSchema(content);
            List<String> sqlList = DBSqlParser.parseSql(content, DBDialect.H2);
            for (String sql : sqlList) {
                this.checkInterrupt();
                if (StringUtil.isBlank(sql)) {
                    continue;
                }
                try {
                    sql = H2SqlSchemaRewriter.rewriteStatement(
                            sql, sourceSchema, this.schema);
                    // 新增记录用批量处理，避免逐条事务提交
                    if (StringUtil.startWithIgnoreCase(sql.stripLeading(), "INSERT INTO ")) {
                        this.addInsert(sql);
                    } else {
                        // 先执行已缓存的插入，保证 SQL 文件的执行顺序
                        this.doBatchInsert();
                        if (this.isSchemaStatement(sql)) {
                            // 目标 schema 尚不存在时不能先切换到该 schema
                            this.client.executeSqlSimple(sql);
                        } else {
                            String dropTrigger = this.dropTriggerSql(sql);
                            if (dropTrigger != null) {
                                this.client.executeSqlSimple(this.schema, dropTrigger);
                            }
                            this.client.executeSqlSimple(this.schema, sql);
                        }
                        this.processedIncr();
                    }
                } catch (Exception ex) {
                    this.processedDecr();
                    this.exception(ex);
                    if (!this.isContinueWithErrors()) {
                        break;
                    }
                    this.getInsertList().clear();
                }
            }
            // 收尾批量插入
            this.doBatchInsert();
        } catch (Exception ex) {
            this.handleFailure(ex, false);
        } finally {
            this.message("Run SQL File Finished");
        }
    }

    @Override
    public void doBatchInsert(List<String> list, boolean parallel) throws Exception {
        try {
            int result = this.client.insertBatch(this.schema, list, parallel);
            this.processedIncr(result);
        } catch (Exception ex) {
            this.processedDecr(list.size());
            throw ex;
        }
    }

    @Override
    public boolean enableParallel() {
        return false;
    }

    private boolean isSchemaStatement(String sql) {
        String value = sql.stripLeading().toUpperCase();
        return value.startsWith("CREATE SCHEMA ")
                || value.startsWith("ALTER SCHEMA ")
                || value.startsWith("DROP SCHEMA ")
                || value.startsWith("SET SCHEMA ");
    }

    private String dropTriggerSql(String sql) {
        Matcher matcher = CREATE_TRIGGER.matcher(sql);
        if (!matcher.find()) {
            return null;
        }
        String schema = StringUtil.blankToDefault(
                unquote(matcher.group(1)), unquote(matcher.group(2)));
        String trigger = StringUtil.blankToDefault(
                unquote(matcher.group(3)), unquote(matcher.group(4)));
        if (StringUtil.isBlank(trigger)) {
            return null;
        }
        if (StringUtil.isBlank(schema)) {
            schema = this.schema;
        }
        return "DROP TRIGGER IF EXISTS "
                + H2GeneratorUtil.qualified(schema, trigger);
    }

    private static String unquote(String value) {
        if (StringUtil.isBlank(value)) {
            return value;
        }
        return value.trim().replace("\"\"", "\"");
    }
}
