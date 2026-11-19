package cn.oyzh.easyshell.data.oracle.handler;

import cn.oyzh.common.db.SqlDatabase;
import cn.oyzh.common.db.SqlUtil;
import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.oracle.ShellOracleClient;
import cn.oyzh.easyshell.oracle.ShellOracleHelper;
import cn.oyzh.fx.db.data.handler.DBDataRunFileHandler;

import java.util.List;

/**
 * Oracle执行SQL文件处理器
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellOracleDataRunSqlFileHandler extends DBDataRunFileHandler<String> {

    /**
     * 数据库客户端
     */
    private final ShellOracleClient dbClient;

    /**
     * 构造 Oracle执行SQL文件处理器
     *
     * @param dbClient 数据库客户端
     * @param dbName   数据库名称
     */
    public ShellOracleDataRunSqlFileHandler(ShellOracleClient dbClient, String dbName) {
        super(dbName);
        this.dbClient = dbClient;
    }

    @Override
    public void runFile() throws Exception {
        this.message("Run Sql File Starting");
        try {
            List<String> statements = splitOracleSql(FileUtil.readUtf8String(this.file));
            for (String statement : statements) {
                try {
                    this.checkInterrupt();
                    if (StringUtil.startWithIgnoreCase(ShellOracleHelper.stripLeadingComments(statement), "INSERT ")) {
                        this.addInsert(statement);
                    } else {
                        this.doBatchInsert();
                        this.dbClient.executeSqlSimple(this.dbName, statement);
                        this.processedIncr();
                    }
                } catch (Exception ex) {
                    if (ShellOracleHelper.isTablespaceUnavailable(ex)) {
                        this.getInsertList().clear();
                        String tablespaceInfo = this.dbClient.describeUserTablespaces(this.dbName);
                        this.message("Run Sql File stopped: Oracle user default or temporary tablespace is unavailable"
                                + (StringUtil.isBlank(tablespaceInfo) ? "" : ": " + tablespaceInfo));
                        break;
                    }
                    if (ShellOracleHelper.isIgnorableDropError(statement, ex)) {
                        this.processedIncr();
                        continue;
                    }
                    this.exception(ex);
                    this.processedDecr();
                    this.getInsertList().clear();
                    if (!this.continueWithErrors) {
                        break;
                    }
                }
            }
            this.doBatchInsert();
        } catch (Exception ex) {
            this.exception(ex);
        } finally {
            this.message("Run Sql File Finished");
        }
    }

    /**
     * 按Oracle SQL*Plus规则拆分脚本。普通SQL以分号结束，PL/SQL块以独立的/结束。
     */
    public static List<String> splitOracleSql(String script) {
        return SqlUtil.split(script, SqlDatabase.ORACLE);
    }

    @Override
    public void doBatchInsert(List<String> list, boolean parallel) throws Exception {
        try {
            int result = this.dbClient.insertBatch(this.dbName, list, parallel);
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

