package cn.oyzh.easyshell.data.oracle.handler;

import cn.oyzh.common.db.SqlDatabase;
import cn.oyzh.common.db.SqlUtil;
import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.oracle.ShellOracleClient;
import cn.oyzh.easyshell.oracle.ShellOracleHelper;
import cn.oyzh.fx.db.data.handler.DBDataRunFileHandler;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Oracle执行SQL文件处理器
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellOracleDataRunSqlFileHandler extends DBDataRunFileHandler<String> {

    private static final Pattern CREATE_TYPE_PATTERN = Pattern.compile(
            "(?is)\\bCREATE\\s+(?:OR\\s+REPLACE\\s+)?(?:NON)?EDITIONABLE\\s+TYPE(?:\\s+BODY)?\\b");

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
    protected void doActionInternal() throws Exception {
        this.message("Run Sql File Starting");
        try {
            String content = FileUtil.readUtf8String(this.file);
            List<String> statements = splitOracleSql(content);
            if (!this.validateTargetSchema(content)) {
                return;
            }
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
            this.handleFailure(ex, true);
        } finally {
            this.message("Run Sql File Finished");
        }
    }

    private boolean validateTargetSchema(String content) {
        try {
            boolean exists;
            try {
                exists = this.dbClient.existSchema(this.dbName);
            } catch (Exception ex) {
                this.dbClient.resetServerConnection();
                exists = this.dbClient.existSchema(this.dbName);
            }
            if (!exists) {
                String container = this.dbClient.selectCurrentContainer();
                String message = "Target schema \"" + this.dbName + "\" does not exist in the current connection"
                        + (StringUtil.isBlank(container) ? "" : " (container: " + container + ")")
                        + ". Please check the connection Service Name (PDB) and reconnect before importing.";
                this.message(message);
                return false;
            }
        } catch (Exception ex) {
            this.message("Failed to validate target schema \"" + this.dbName + "\": " + ex.getMessage());
            return false;
        }
        // 目标用户可能已被删除重建，丢弃缓存会话，避免 ORA-23362/ORA-01129
        this.dbClient.resetSchemaConnection(this.dbName);
        if (content != null && CREATE_TYPE_PATTERN.matcher(content).find()
                && !this.dbClient.hasCreateTypePrivilege()) {
            this.message("Target schema \"" + this.dbName + "\" needs the CREATE TYPE privilege before importing"
                    + " user-defined types. Please ask a DBA to run:"
                    + " GRANT CREATE TYPE TO \"" + this.dbName + "\";");
            return false;
        }
        return true;
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

