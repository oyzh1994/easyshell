package cn.oyzh.easyshell.data.sqlite.handler;

import cn.oyzh.common.util.StringUtil;
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
        this.message("Run SQL File Starting");
        try {
            String content = Files.readString(this.file.toPath(), StandardCharsets.UTF_8);
            List<String> sqlList = DBSqlParser.parseSql(content, DBDialect.SQLITE);
            for (String sql : sqlList) {
                this.checkInterrupt();
                if (StringUtil.isBlank(sql)) {
                    continue;
                }
                try {
                    // 新增记录用批量处理，避免逐条事务提交
                    if (StringUtil.startWithIgnoreCase(sql.stripLeading(), "INSERT INTO ")) {
                        this.addInsert(sql);
                    } else {
                        // 先执行已缓存的插入，保证 SQL 文件的执行顺序
                        this.doBatchInsert();
                        this.client.executeSqlSimple(sql);
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
            this.exception(ex);
        } finally {
            this.message("Run SQL File Finished");
        }
    }

    @Override
    public void doBatchInsert(List<String> list, boolean parallel) throws Exception {
        try {
            int result = this.client.insertBatch(this.dbName, list, parallel);
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
}
