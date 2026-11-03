package cn.oyzh.easyshell.data.sqlite.handler;

import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.data.handler.DBDataRunFileHandler;
import cn.oyzh.fx.db.sql.DBSqlParser;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

/**
 * SQLite SQL 文件执行处理器。
 *
 * @since 2026-11-02
 */
public class SQLiteDataRunSqlFileHandler extends DBDataRunFileHandler<String> {

    private final ShellSqliteClient client;

    public SQLiteDataRunSqlFileHandler(ShellSqliteClient client, String dbName) {
        super(dbName);
        this.client = client;
        this.dialect = DBDialect.SQLITE;
    }

    @Override
    public void runFile() throws Exception {
        String content = Files.readString(this.file.toPath(), StandardCharsets.UTF_8);
        List<String> sqlList = DBSqlParser.parseSql(content, DBDialect.SQLITE);
        this.message("Run SQL File Starting");
        for (String sql : sqlList) {
            this.checkInterrupt();
            try {
                this.client.executeSqlSimple(sql);
                this.processedIncr();
                this.message("Executed SQL: " + sql);
            } catch (Exception ex) {
                if (!this.isContinueWithErrors()) {
                    throw ex;
                }
                this.exception(ex);
            }
        }
        this.message("Run SQL File Finished");
    }

    @Override
    public void doBatchInsert(List<String> list, boolean parallel) {
        this.client.insertBatch(this.dbName, list, false);
    }

    @Override
    public boolean enableParallel() {
        return false;
    }
}
