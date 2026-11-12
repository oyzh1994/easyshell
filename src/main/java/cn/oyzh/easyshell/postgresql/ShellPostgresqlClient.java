package cn.oyzh.easyshell.postgresql;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.postgresql.check.PostgresqlCheck;
import cn.oyzh.easyshell.postgresql.column.PostgresqlColumn;
import cn.oyzh.easyshell.postgresql.column.PostgresqlColumns;
import cn.oyzh.easyshell.postgresql.column.PostgresqlSelectColumnParam;
import cn.oyzh.easyshell.postgresql.condition.PostgresqlConditionUtil;
import cn.oyzh.easyshell.postgresql.foreignKey.PostgresqlForeignKey;
import cn.oyzh.easyshell.postgresql.function.PostgresqlAlertFunctionParam;
import cn.oyzh.easyshell.postgresql.function.PostgresqlCreateFunctionParam;
import cn.oyzh.easyshell.postgresql.function.PostgresqlFunction;
import cn.oyzh.easyshell.postgresql.function.PostgresqlSelectFunctionParam;
import cn.oyzh.easyshell.postgresql.generator.function.PostgresqlFunctionAlertSqlGenerator;
import cn.oyzh.easyshell.postgresql.generator.function.PostgresqlFunctionCreateSqlGenerator;
import cn.oyzh.easyshell.postgresql.generator.procedure.PostgresqlProcedureAlertSqlGenerator;
import cn.oyzh.easyshell.postgresql.generator.procedure.PostgresqlProcedureCreateSqlGenerator;
import cn.oyzh.easyshell.postgresql.generator.sequence.PostgresqlSequenceAlertSqlGenerator;
import cn.oyzh.easyshell.postgresql.generator.sequence.PostgresqlSequenceCreateSqlGenerator;
import cn.oyzh.easyshell.postgresql.generator.table.PostgresqlTableAlertSqlGenerator;
import cn.oyzh.easyshell.postgresql.generator.table.PostgresqlTableCreateSqlGenerator;
import cn.oyzh.easyshell.postgresql.generator.view.PostgresqlViewAlertSqlGenerator;
import cn.oyzh.easyshell.postgresql.generator.view.PostgresqlViewCreateSqlGenerator;
import cn.oyzh.easyshell.postgresql.index.PostgresqlIndex;
import cn.oyzh.easyshell.postgresql.procedure.PostgresqlAlertProcedureParam;
import cn.oyzh.easyshell.postgresql.procedure.PostgresqlCreateProcedureParam;
import cn.oyzh.easyshell.postgresql.procedure.PostgresqlProcedure;
import cn.oyzh.easyshell.postgresql.procedure.PostgresqlSelectProcedureParam;
import cn.oyzh.easyshell.postgresql.record.PostgresqlDeleteRecordParam;
import cn.oyzh.easyshell.postgresql.record.PostgresqlInsertRecordParam;
import cn.oyzh.easyshell.postgresql.record.PostgresqlRecord;
import cn.oyzh.easyshell.postgresql.record.PostgresqlRecordFilter;
import cn.oyzh.easyshell.postgresql.record.PostgresqlRecordPrimaryKey;
import cn.oyzh.easyshell.postgresql.record.PostgresqlSelectRecordParam;
import cn.oyzh.easyshell.postgresql.record.PostgresqlUpdateRecordParam;
import cn.oyzh.easyshell.postgresql.routine.PostgresqlRoutineParam;
import cn.oyzh.easyshell.postgresql.schema.PostgresqlSchema;
import cn.oyzh.easyshell.postgresql.sequence.PostgresqlAlertSequenceParam;
import cn.oyzh.easyshell.postgresql.sequence.PostgresqlCreateSequenceParam;
import cn.oyzh.easyshell.postgresql.sequence.PostgresqlSelectSequenceParam;
import cn.oyzh.easyshell.postgresql.sequence.PostgresqlSequence;
import cn.oyzh.easyshell.postgresql.table.PostgresqlAlertTableParam;
import cn.oyzh.easyshell.postgresql.table.PostgresqlCreateTableParam;
import cn.oyzh.easyshell.postgresql.table.PostgresqlSelectTableParam;
import cn.oyzh.easyshell.postgresql.table.PostgresqlTable;
import cn.oyzh.easyshell.postgresql.trigger.PostgresqlSelectTriggerParam;
import cn.oyzh.easyshell.postgresql.trigger.PostgresqlTrigger;
import cn.oyzh.easyshell.postgresql.view.PostgresqlAlertViewParam;
import cn.oyzh.easyshell.postgresql.view.PostgresqlCreateViewParam;
import cn.oyzh.easyshell.postgresql.view.PostgresqlSelectViewParam;
import cn.oyzh.easyshell.postgresql.view.PostgresqlView;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellJumpConfig;
import cn.oyzh.easyshell.domain.ShellProxyConfig;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.internal.ShellBaseClient;
import cn.oyzh.easyshell.internal.ShellClientChecker;
import cn.oyzh.easyshell.internal.ShellConnState;
import cn.oyzh.easyshell.query.postgresql.PostgresqlExecuteResult;
import cn.oyzh.easyshell.query.postgresql.PostgresqlExplainResult;
import cn.oyzh.fx.db.DBClient;
import cn.oyzh.fx.db.DBColumn;
import cn.oyzh.fx.db.DBConnConfig;
import cn.oyzh.fx.db.DBConnManager;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBFeature;
import cn.oyzh.fx.db.DBName;
import cn.oyzh.fx.db.DBObjects;
import cn.oyzh.fx.db.DBRawQueryResults;
import cn.oyzh.fx.db.DBRecordData;
import cn.oyzh.fx.db.query.DBQueryResults;
import cn.oyzh.fx.db.sql.DBSqlParser;
import cn.oyzh.fx.db.util.DBUtil;
import cn.oyzh.ssh.domain.SSHConnect;
import cn.oyzh.ssh.jump.SSHJumpForwarder;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PostgreSQL数据库客户端封装，提供数据库连接、表/视图/函数/过程/触发器等对象的CRUD操作，
 * 以及SQL执行、记录管理、索引/外键/检查约束管理等功能
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellPostgresqlClient implements ShellBaseClient, DBClient {

    /**
     * ssh端口转发器
     */
    private SSHJumpForwarder jumpForwarder;

    /**
     * db信息
     */
    protected ShellConnect shellConnect;

    /**
     * 数据库连接管理器
     */
    protected DBConnManager connManager;

    @Override
    public DBConnManager getConnManager() {
        if (this.connManager == null) {
            this.connManager = new ShellPostgresqlConnManager();
        }
        return this.connManager;
    }

    /**
     * 属性列表
     */
    private Map<String, Object> properties;

    @Override
    public Map<String, Object> getProperties() {
        if (this.properties == null) {
            this.properties = new HashMap<>();
        }
        return this.properties;
    }

    /**
     * 连接状态
     */
    private final SimpleObjectProperty<ShellConnState> state = new SimpleObjectProperty<>();

    /**
     * 当前状态监听器
     */
    private final ChangeListener<ShellConnState> stateListener = (state1, state2, state3) -> ShellBaseClient.super.onStateChanged(state3);

    @Override
    public ObjectProperty<ShellConnState> stateProperty() {
        return this.state;
    }

    /**
     * 构造PostgreSQL客户端
     *
     * @param shellConnect 连接信息
     */
    public ShellPostgresqlClient(ShellConnect shellConnect) {
        this.shellConnect = shellConnect;
        this.addStateListener(this.stateListener);
    }

    @Override
    public boolean isReadonly() {
        return this.shellConnect.isReadonly();
    }

    @Override
    public void start(int timeout) throws Throwable {
        if (this.isConnected() || this.isConnecting()) {
            return;
        }
        // 初始化客户端
        this.initClient();
        // 连接超时
        this.getConnManager().setConnectTimeout(timeout);
        try {
            // 开始连接时间
            final AtomicLong starTime = new AtomicLong();
            // 开始连接时间
            starTime.set(System.currentTimeMillis());
            // 更新连接状态
            this.state.set(ShellConnState.CONNECTING);
            // 连接成功前阻塞线程
            if (this.getConnManager().connection().isValid(timeout / 1000)) {
                // 更新连接状态
                this.state.set(ShellConnState.CONNECTED);
                // 添加到状态监听器队列
                ShellClientChecker.push(this);
            } else {// 连接未成功则关闭
                this.close();
                if (this.state.get() == ShellConnState.FAILED) {
                    this.state.set(null);
                } else {
                    this.state.set(ShellConnState.FAILED);
                }
            }
        } catch (Throwable ex) {
            this.state.set(ShellConnState.FAILED);
            if (ex.getCause() != null) {
                ex = ex.getCause();
            }
            JulLog.warn("Postgresql client start error", ex);
            throw new ShellException(ex);
        }
    }

    @Override
    public ShellConnect getShellConnect() {
        return this.shellConnect;
    }

    /**
     * 初始化连接主机地址
     *
     * @return 主机地址
     */
    private String initHost() {
        // 连接地址
        String host;
        // 初始化跳板转发
        if (this.shellConnect.isEnableJump()) {
            if (this.jumpForwarder == null) {
                this.jumpForwarder = new SSHJumpForwarder();
            }
            // 初始化跳板配置
            List<ShellJumpConfig> jumpConfigs = this.shellConnect.getEnableJumpConfigs();
            // 转换为目标连接
            SSHConnect target = new SSHConnect();
            target.setHost(this.shellConnect.hostIp());
            target.setPort(this.shellConnect.hostPort());
            // 执行连接
            int localPort = this.jumpForwarder.forward(jumpConfigs, target);
            // 连接信息
            host = "127.0.0.1:" + localPort;
        } else {// 直连
            if (this.jumpForwarder != null) {
                IOUtil.close(this.jumpForwarder);
                this.jumpForwarder = null;
            }
            // 连接信息
            host = this.shellConnect.hostIp() + ":" + this.shellConnect.hostPort();
        }
        return host;
    }

    /**
     * 初始化客户端
     */
    protected void initClient() {
        String host = this.initHost();
        // 连接地址
        String ip = host.split(":")[0];
        int port = Integer.parseInt(host.split(":")[1]);
        // 连接配置
        DBConnConfig connConfig = new DBConnConfig();
        connConfig.setHost(ip);
        connConfig.setPort(port);
        connConfig.setUser(this.shellConnect.getUser());
        connConfig.setPassword(this.shellConnect.getPassword());
        // 数据库名称
        connConfig.setDatabase(this.shellConnect.getDatabase());
        // SSL
        connConfig.setUseSSL(this.shellConnect.isSSLMode());
        // 环境参数
        connConfig.setEnv(this.shellConnect.getEnvironment());
        // 代理处理
        if (this.shellConnect.isEnableProxy()) {
            ShellProxyConfig proxyConfig = this.shellConnect.getProxyConfig();
            connConfig.setProxyHost(proxyConfig.getHost());
            connConfig.setProxyPort(proxyConfig.getPort());
            connConfig.setProxyType(proxyConfig.getProtocol());
            if (proxyConfig.isPasswordAuth()) {
                connConfig.setProxyUser(proxyConfig.getUser());
                connConfig.setProxyPassword(proxyConfig.getPassword());
            }
            connConfig.setSocketFactory(ShellPostgresqlProxySocketFactory.class.getName());
        }
        this.getConnManager().setConfig(connConfig);
    }

    @Override
    public void close() {
        try {
            IOUtil.close(this.connManager);
            IOUtil.close(this.jumpForwarder);
            this.state.set(ShellConnState.CLOSED);
            this.removeStateListener(this.stateListener);
            this.connManager = null;
            this.jumpForwarder = null;
        } catch (Exception ex) {
            ex.printStackTrace();
            JulLog.warn("Postgresql client close error.", ex);
        }
    }

    @Override
    public boolean isConnected() {
        try {
            if (this.connManager == null) {
                return false;
            }
            Connection connection = this.getConnManager().connection();
            if (connection == null) {
                return false;
            }
            return !connection.isClosed() && connection.isValid(1000);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    //    /**
    //     * db是否连接中
    //     *
    //     * @return 结果
    //     */
    //    public boolean isConnecting() {
    //        return this.getState() == ShellConnState.CONNECTING;
    //    }

    @Override
    public int tableSize(String schema) {
        int size = 0;
        try {
            Connection connection = this.getConnManager().connection();
            String sql = """
                    SELECT
                        COUNT(*)
                    FROM
                        PG_CATALOG.PG_CLASS C
                    JOIN
                        PG_CATALOG.PG_NAMESPACE N ON N.OID = C.RELNAMESPACE
                    WHERE
                        N.NSPNAME = ?
                    AND
                        C.RELKIND IN ('r', 'p')
                    """;
            this.printSql(sql);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, schema);
            ResultSet resultSet = statement.executeQuery();
            DBUtil.printMetaData(resultSet);
            if (resultSet.next()) {
                size = resultSet.getInt(1);
            }
            IOUtil.close(resultSet);
            IOUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
        return size;
    }

    @Override
    public int viewSize(String schema) {
        int size = 0;
        try {
            Connection connection = this.getConnManager().connection();
            String sql = """
                    SELECT
                        COUNT(*)
                    FROM
                        PG_CATALOG.PG_CLASS C
                    JOIN
                        PG_CATALOG.PG_NAMESPACE N ON N.OID = C.RELNAMESPACE
                    WHERE
                        N.NSPNAME = ?
                    AND
                        C.RELKIND = 'v'
                    """;
            this.printSql(sql);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, schema);
            ResultSet resultSet = statement.executeQuery();
            DBUtil.printMetaData(resultSet);
            if (resultSet.next()) {
                size = resultSet.getInt(1);
            }
            IOUtil.close(resultSet);
            IOUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
        return size;
    }

    /**
     * 执行SQL语句
     *
     * @param schema 模式名称
     * @param sql    SQL语句
     * @return 执行结果
     */
    public DBQueryResults<PostgresqlExecuteResult> executeSql(String schema, String sql) {
        DBQueryResults<PostgresqlExecuteResult> results = new DBQueryResults<>();
        Connection connection = null;
        boolean autoCommit = true;
        Statement statement = null;
        try {
            this.printSql(sql);
            DBSqlParser parser = DBSqlParser.getParser(sql, DBDialect.POSTGRESQL);
            List<String> list = parser.parseSql();
            connection = this.getConnManager().connection(schema);
            autoCommit = connection.getAutoCommit();
            // PostgreSQL 的 DDL/DML 都是事务性的。查询页按 psql 的自动提交语义逐条执行，
            // 避免关闭连接时回滚未提交的 DDL/DML；用户显式写 BEGIN/COMMIT/ROLLBACK 时仍按事务执行。
            connection.setAutoCommit(true);
            statement = connection.createStatement();
            for (String execSql : list) {
                PostgresqlExecuteResult result = new PostgresqlExecuteResult();
                result.setContent(execSql);
                try {
                    long startTime = System.nanoTime();
                    boolean isQuery = statement.execute(execSql);
                    if (isQuery) {
                        ResultSet resultSet = statement.getResultSet();
                        result.setFullColumn(parser.isFullColumn(sql));
                        result.parseResult(resultSet, connection, !parser.isSelect(sql));
                        result.fillSchema(schema);
                        IOUtil.close(resultSet);
                        result.setSuccess(true);
                    } else {
                        int updateCount = statement.getUpdateCount();
                        result.setUpdateCount(updateCount);
                        result.setSuccess(true);
                    }
                    long endTime = System.nanoTime();
                    result.setUsed(endTime - startTime);
                } catch (SQLException ex) {
                    result.setMsg(ex.toString());
                }
                results.addResult(result);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            DBUtil.rollback(connection);
            results.parseError(ex);
        } finally {
            IOUtil.close(statement);
            if (connection != null) {
                try {
                    connection.setAutoCommit(autoCommit);
                } catch (SQLException ex) {
                    JulLog.warn("restore autocommit error", ex);
                }
            }
        }
        return results;
    }

    /**
     * 执行SQL语句
     *
     * @param sql SQL语句
     * @return 执行结果
     */
    public DBQueryResults<PostgresqlExecuteResult> executeSql(String sql) {
        return this.executeSql(null, sql);
    }

    @Override
    public DBRawQueryResults executeSqlRaw(String name, String sql) throws Exception {
        DBRawQueryResults results = new DBRawQueryResults();
        Connection connection;
        try {
            this.printSql(sql);
            List<String> sqlList = DBSqlParser.getParser(sql, this.dialect()).parseSql();
            // PostgreSQL一个连接对应一个数据库，name仅作为schema提示，SQL中已限定schema
            connection = this.getConnManager().connection();
            // 原始结果集需要保持可读，且执行 DML 时不能被关闭连接回滚，因此使用自动提交
            connection.setAutoCommit(true);
            for (String execSql : sqlList) {
                Statement statement = connection.createStatement();
                try {
                    boolean hasResult = statement.execute(execSql);
                    while (true) {
                        if (hasResult) {
                            results.add(statement, statement.getResultSet());
                        }
                        if (!statement.getMoreResults(Statement.KEEP_CURRENT_RESULT) && statement.getUpdateCount() == -1) {
                            break;
                        }
                        hasResult = statement.getResultSet() != null;
                    }
                } catch (Exception ex) {
                    try {
                        statement.close();
                    } catch (Exception ignored) {
                    }
                    throw ex;
                }
            }
            return results;
        } catch (Exception ex) {
            results.close();
            throw ex;
        }
    }

    @Override
    public int insertBatch(String schema, List<String> sqlList) {
        return this.insertBatch(schema, sqlList, false);
    }

    @Override
    public int procedureSize(String schema) {
        int size = 0;
        try {
            Connection connection = this.getConnManager().connection();
            String sql = """
                    SELECT
                        COUNT(*)
                    FROM
                        PG_CATALOG.PG_PROC P
                    JOIN
                        PG_CATALOG.PG_NAMESPACE N ON N.OID = P.PRONAMESPACE
                    WHERE
                        N.NSPNAME = ?
                    AND
                        P.PROKIND = 'p'
                    """;
            this.printSql(sql);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, schema);
            ResultSet resultSet = statement.executeQuery();
            DBUtil.printMetaData(resultSet);
            if (resultSet.next()) {
                size = resultSet.getInt(1);
            }
            IOUtil.close(resultSet);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
        return size;
    }

    @Override
    public int functionSize(String schema) {
        int size = 0;
        try {
            Connection connection = this.getConnManager().connection();
            String sql = """
                    SELECT
                        COUNT(*)
                    FROM
                        PG_CATALOG.PG_PROC P
                    JOIN
                        PG_CATALOG.PG_NAMESPACE N ON N.OID = P.PRONAMESPACE
                    WHERE
                        N.NSPNAME = ?
                    AND
                        P.PROKIND = 'f'
                    AND
                        P.PRORETTYPE <> 'trigger'::REGTYPE
                    AND
                        NOT EXISTS (SELECT 1 FROM PG_CATALOG.PG_AGGREGATE A WHERE A.AGGFNOID = P.OID)
                    """;
            this.printSql(sql);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, schema);
            ResultSet resultSet = statement.executeQuery();
            DBUtil.printMetaData(resultSet);
            if (resultSet.next()) {
                size = resultSet.getInt(1);
            }
            IOUtil.close(resultSet);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
        return size;
    }

    /**
     * 修改函数
     *
     * @param param 参数
     */
    public void alertFunction(PostgresqlAlertFunctionParam param) {
        Connection connection = null;
        boolean autoCommit = true;
        Statement statement = null;
        try {
            String sql = PostgresqlFunctionAlertSqlGenerator.generateSqlSingle(param);
            this.printSql(sql);
            connection = this.getConnManager().connection();
            autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            statement = connection.createStatement();
            statement.executeUpdate(sql);
            connection.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
            DBUtil.rollback(connection);
            throw new ShellException(ex);
        } finally {
            IOUtil.close(statement);
            if (connection != null) {
                try {
                    connection.setAutoCommit(autoCommit);
                } catch (SQLException ex) {
                    JulLog.warn("restore autocommit error", ex);
                }
            }
        }
    }

    /**
     * 查询触发器列表
     *
     * @param schema 模式
     * @return 触发器列表
     */
    public List<PostgresqlTrigger> selectTriggers(String schema) {
        PostgresqlSelectTriggerParam param = new PostgresqlSelectTriggerParam();
        param.setFull(true);
        param.setSchema(schema);
        return this.selectTriggers(param);
    }

    /**
     * 查询触发器列表
     *
     * @param schema    模式
     * @param tableName 表
     * @return 触发器列表
     */
    public List<PostgresqlTrigger> selectTriggers(String schema, String tableName) {
        PostgresqlSelectTriggerParam param = new PostgresqlSelectTriggerParam();
        param.setFull(true);
        param.setSchema(schema);
        param.setTableName(tableName);
        return this.selectTriggers(param);
    }

    //    /**
    //     * 查询触发器列表
    //     *
    //     * @param param 参数
    //     * @return 触发器列表
    //     */
    //    public List<PostgresqlTrigger> selectTriggers(PostgresqlSelectTriggerParam param) {
    //        try {
    //            //            String sql = """
    //            //                        SELECT
    //            //                            a.TRIGGER_NAME, a.TRIGGERING_EVENT AS EVENT_MANIPULATION, a.TRIGGERING_TYPE AS TRIGGER_TYPE, a.TABLE_NAME AS EVENT_OBJECT_TABLE , dt.TRIGGER_BODY ACTION_STATEMENT
    //            //                        FROM
    //            //                            ALL_TRIGGERS a
    //            //                        LEFT JOIN
    //            //                            DBA_TRIGGERS dt
    //            //                        ON
    //            //                            dt.OWNER = a.OWNER
    //            //                        AND
    //            //                            dt.TABLE_NAME = a.TABLE_NAME
    //            //                        AND
    //            //                            dt.TRIGGER_NAME = a.TRIGGER_NAME
    //            //                        WHERE
    //            //                            A.OWNER = ?
    //            //                    """;
    //            String schema = param.getSchema();
    //            String sql = """
    //                        SELECT
    //                            A.TRIGGER_NAME,
    //                            A.TRIGGERING_TYPE AS TRIGGER_TYPE,
    //                            A.TABLE_NAME AS EVENT_OBJECT_TABLE,
    //                            A.TRIGGER_BODY AS ACTION_STATEMENT,
    //                            A.TRIGGERING_EVENT AS EVENT_MANIPULATION,
    //                            DBMS_METADATA.GET_DDL('TRIGGER', A.TRIGGER_NAME, A.OWNER) AS DLL
    //                        FROM
    //                            ALL_TRIGGERS A
    //                        WHERE
    //                            A.OWNER = ?
    //                    """;
    //            this.printSql(sql);
    //            PreparedStatement statement = this.getConnManager().connection(schema).prepareStatement(sql);
    //            statement.setString(1, schema);
    //            ResultSet resultSet = statement.executeQuery();
    //            List<PostgresqlTrigger> list = new ArrayList<>();
    //            while (resultSet.next()) {
    //                PostgresqlTrigger trigger = new PostgresqlTrigger();
    //                String name = resultSet.getString("TRIGGER_NAME");
    //                String type = resultSet.getString("TRIGGER_TYPE");
    //                String createDefinition = resultSet.getString("DLL");
    //                String definition = resultSet.getString("ACTION_STATEMENT");
    //                String tableName = resultSet.getString("EVENT_OBJECT_TABLE");
    //                String manipulation = resultSet.getString("EVENT_MANIPULATION");
    //                trigger.setName(name);
    //                trigger.setTableName(tableName);
    //                trigger.setCreateDefinition(createDefinition);
    //                trigger.setDefinition(ShellPostgresqlHelper.fixTiggerDefinition(definition));
    //                trigger.setPolicy(type.contains("BEFORE") ? "BEFORE" : "AFTER", manipulation);
    //                list.add(trigger);
    //            }
    //            IOUtil.close(resultSet);
    //            IOUtil.close(statement);
    //            return list;
    //        } catch (Exception ex) {
    //            throw new ShellException(ex);
    //        }
    //    }

    /**
     * 查询触发器列表
     *
     * @param param 查询参数
     * @return 触发器列表
     */
    public DBObjects<PostgresqlTrigger> selectTriggers(PostgresqlSelectTriggerParam param) {
        try {
            //            String sql = """
            //                        SELECT
            //                            a.TRIGGER_NAME, a.TRIGGERING_EVENT AS EVENT_MANIPULATION, a.TRIGGERING_TYPE AS TRIGGER_TYPE, a.TABLE_NAME AS EVENT_OBJECT_TABLE , dt.TRIGGER_BODY ACTION_STATEMENT
            //                        FROM
            //                            ALL_TRIGGERS a
            //                        LEFT JOIN
            //                            DBA_TRIGGERS dt
            //                        ON
            //                            dt.OWNER = a.OWNER
            //                        AND
            //                            dt.TABLE_NAME = a.TABLE_NAME
            //                        AND
            //                            dt.TRIGGER_NAME = a.TRIGGER_NAME
            //                        WHERE
            //                            a.OWNER = ?
            //                        AND
            //                            a.TABLE_NAME = ?
            //                    """;
            String schema = param.getSchema();
            PreparedStatement statement;
            Connection connection = this.getConnManager().connection();
            if (param.getTableName() == null) {
                String sql = """
                        SELECT
                            T.TGNAME AS "TRIGGER_NAME",
                            T.TGTYPE AS "TRIGGER_TYPE",
                            C.RELNAME AS "EVENT_OBJECT_TABLE",
                            PG_CATALOG.PG_GET_FUNCTIONDEF(T.TGFOID) AS "FUNCTION_DEFINITION",
                            PG_CATALOG.PG_GET_TRIGGERDEF(T.OID, TRUE) AS "DLL"
                        FROM
                            PG_CATALOG.PG_TRIGGER T
                        JOIN
                            PG_CATALOG.PG_CLASS C ON C.OID = T.TGRELID
                        JOIN
                            PG_CATALOG.PG_NAMESPACE N ON N.OID = C.RELNAMESPACE
                        WHERE
                            N.NSPNAME = ?
                        AND
                            NOT T.TGISINTERNAL
                        ORDER BY
                            T.TGNAME
                        """;
                this.printSql(sql);
                statement = connection.prepareStatement(sql);
                statement.setString(1, schema);
            } else {
                String sql = """
                        SELECT
                            T.TGNAME AS "TRIGGER_NAME",
                            T.TGTYPE AS "TRIGGER_TYPE",
                            C.RELNAME AS "EVENT_OBJECT_TABLE",
                            PG_CATALOG.PG_GET_FUNCTIONDEF(T.TGFOID) AS "FUNCTION_DEFINITION",
                            PG_CATALOG.PG_GET_TRIGGERDEF(T.OID, TRUE) AS "DLL"
                        FROM
                            PG_CATALOG.PG_TRIGGER T
                        JOIN
                            PG_CATALOG.PG_CLASS C ON C.OID = T.TGRELID
                        JOIN
                            PG_CATALOG.PG_NAMESPACE N ON N.OID = C.RELNAMESPACE
                        WHERE
                            N.NSPNAME = ?
                        AND
                            C.RELNAME = ?
                        AND
                            NOT T.TGISINTERNAL
                        ORDER BY
                            T.TGNAME
                        """;
                this.printSql(sql);
                statement = connection.prepareStatement(sql);
                statement.setString(1, schema);
                statement.setString(2, param.getTableName());
            }
            ResultSet resultSet = statement.executeQuery();
            DBObjects<PostgresqlTrigger> list = new DBObjects<>();
            while (resultSet.next()) {
                PostgresqlTrigger trigger = new PostgresqlTrigger();
                String name = resultSet.getString("TRIGGER_NAME");
                int type = resultSet.getInt("TRIGGER_TYPE");
                String tableName = resultSet.getString("EVENT_OBJECT_TABLE");
                String functionDefinition = resultSet.getString("FUNCTION_DEFINITION");
                String createDefinition = resultSet.getString("DLL");
                trigger.setName(name);
                trigger.setSchema(schema);
                trigger.setTableName(tableName);
                trigger.setCreateDefinition(createDefinition);
                trigger.setFunctionDefinition(functionDefinition);
                String policy = this.triggerTiming(type) + " " + this.triggerManipulation(type);
                trigger.setPolicy(policy);
                trigger.setDefinition(ShellPostgresqlHelper.normalizeTriggerBody(
                        ShellPostgresqlHelper.fixTiggerDefinition(ShellPostgresqlHelper.parseFunctionBody(functionDefinition)),
                        policy));
                list.add(trigger);
            }
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            DBName.sortItems(list);
            return list;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 解析触发器执行时机
     *
     * @param tgtype 触发器类型标识
     * @return 执行时机
     */
    private String triggerTiming(int tgtype) {
        // 2: BEFORE，64: INSTEAD OF
        if ((tgtype & 2) != 0) {
            return "BEFORE";
        }
        if ((tgtype & 64) != 0) {
            return "INSTEAD OF";
        }
        return "AFTER";
    }

    /**
     * 解析触发器事件
     *
     * @param tgtype 触发器类型标识
     * @return 触发器事件
     */
    private String triggerManipulation(int tgtype) {
        List<String> events = new ArrayList<>();
        // 4: INSERT，8: DELETE，16: UPDATE，32: TRUNCATE
        if ((tgtype & 4) != 0) {
            events.add("INSERT");
        }
        if ((tgtype & 16) != 0) {
            events.add("UPDATE");
        }
        if ((tgtype & 8) != 0) {
            events.add("DELETE");
        }
        if ((tgtype & 32) != 0) {
            events.add("TRUNCATE");
        }
        return String.join(" OR ", events);
    }

    public List<PostgresqlSequence> selectSequences(String schema) {
        PostgresqlSelectSequenceParam param = new PostgresqlSelectSequenceParam();
        param.setFull(true);
        param.setSchema(schema);
        return this.selectSequences(param);
    }

    /**
     * 查询序列列表
     *
     * @param param 查询参数
     * @return 触发器列表
     */
    public DBObjects<PostgresqlSequence> selectSequences(PostgresqlSelectSequenceParam param) {
        try {
            String schema = param.getSchema();
            PreparedStatement statement;
            Connection connection = this.getConnManager().connection();
            String sql = """
                    SELECT
                        S.SCHEMANAME AS "SEQUENCE_OWNER",
                        S.SEQUENCENAME AS "SEQUENCE_NAME",
                        S.MIN_VALUE,
                        S.MAX_VALUE,
                        S.CACHE_SIZE,
                        S.CYCLE AS "CYCLE_FLAG",
                        S.INCREMENT_BY,
                        S.LAST_VALUE AS "CURRENT_VALUE",
                        S.START_VALUE
                    FROM
                        PG_CATALOG.PG_SEQUENCES S
                    JOIN
                        PG_CATALOG.PG_CLASS C ON C.RELNAME = S.SEQUENCENAME AND C.RELKIND = 'S'
                    JOIN
                        PG_CATALOG.PG_NAMESPACE N ON N.OID = C.RELNAMESPACE AND N.NSPNAME = S.SCHEMANAME
                    WHERE
                        S.SCHEMANAME = ?
                    AND
                        NOT EXISTS (
                            SELECT 1
                            FROM PG_CATALOG.PG_DEPEND D
                            WHERE D.CLASSID = 'pg_class'::REGCLASS
                            AND D.OBJID = C.OID
                            AND D.REFCLASSID = 'pg_class'::REGCLASS
                            AND D.REFOBJSUBID > 0
                        )
                    ORDER BY
                        S.SEQUENCENAME
                    """;
            this.printSql(sql);
            statement = connection.prepareStatement(sql);
            statement.setString(1, schema);
            ResultSet resultSet = statement.executeQuery();
            DBObjects<PostgresqlSequence> list = new DBObjects<>();
            while (resultSet.next()) {
                PostgresqlSequence sequence = new PostgresqlSequence();
                long minValue = resultSet.getLong("MIN_VALUE");
                Long minVal = resultSet.wasNull() ? null : minValue;
                long maxValue = resultSet.getLong("MAX_VALUE");
                Long maxVal = resultSet.wasNull() ? null : maxValue;
                long cacheSize = resultSet.getLong("CACHE_SIZE");
                Long cache = resultSet.wasNull() ? null : cacheSize;
                String name = resultSet.getString("SEQUENCE_NAME");
                boolean cycle = resultSet.getBoolean("CYCLE_FLAG");
                long incrementBy = resultSet.getLong("INCREMENT_BY");
                Long increment = resultSet.wasNull() ? null : incrementBy;
                long currentValue = resultSet.getLong("CURRENT_VALUE");
                Long currentVal = resultSet.wasNull() ? null : currentValue;
                sequence.setName(name);
                sequence.setSchema(schema);
                sequence.setMinVal(minVal);
                sequence.setMaxVal(maxVal);
                sequence.setCacheSize(cache);
                sequence.setCurrentVal(currentVal);
                sequence.setIncrementBy(increment);
                sequence.setCycle(cycle);
                // PostgreSQL序列不支持ORDER
                sequence.setOrder(false);
                list.add(sequence);
            }
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            // 生成建序列语句
            for (PostgresqlSequence sequence : list) {
                sequence.setCreateDefinition(this.showCreateSequence(schema, sequence.getName()));
            }
            DBName.sortItems(list);
            return list;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    @Override
    public String selectVersion() {
        if (this.hasProperty("version")) {
            return this.getProperty("version");
        }
        String version = "";
        try {
            Statement stmt = this.getConnManager().connection().createStatement();
            ResultSet resultSet = stmt.executeQuery("SHOW server_version");
            if (resultSet.next()) {
                version = resultSet.getString(1);
            }
            IOUtil.close(resultSet);
            IOUtil.close(stmt);
        } catch (Exception e) {
            // 兼容SHOW不可用的场景
            try {
                Statement stmt = this.getConnManager().connection().createStatement();
                ResultSet resultSet = stmt.executeQuery("SELECT current_setting('server_version')");
                if (resultSet.next()) {
                    version = resultSet.getString(1);
                }
                IOUtil.close(resultSet);
                IOUtil.close(stmt);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        this.putProperty("version", version);
        return version;
    }

    @Override
    public String selectProduct() {
        if (this.hasProperty("product")) {
            return this.getProperty("product");
        }
        String product = "";
        try {
            Connection conn = this.getConnManager().connection();
            Statement stmt = conn.createStatement();
            ResultSet resultSet = stmt.executeQuery("SELECT version()");
            if (resultSet.next()) {
                product = resultSet.getString(1);
                if (product != null) {
                    product = product.replaceAll("[\\r\\n]+", " ").trim();
                }
            }
            IOUtil.close(resultSet);
            IOUtil.close(stmt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.putProperty("product", product);
        return product;
    }

    //public void dropEvent(String schema, PostgresqlEvent event) {
    //    try {
    //        String sql = "DROP EVENT " + DBUtil.wrap(event.getschema(), event.getName(), DBDialect.POSTGRESQL);
    //        this.printSql(sql);
    //        Statement statement = this.getConnManager().connection(schema).createStatement();
    //        statement.executeUpdate(sql);
    //        IOUtil.close(statement);
    //    } catch (Exception ex) {
    //        ex.printStackTrace();
    //        throw new ShellException(ex);
    //    }
    //}

    //    public void createEvent(String schema, PostgresqlEvent event) {
    //        try {
    //            String sql = PostgresqlEventCreateSqlGenerator.generateSql(event);
    //            this.printSql(sql);
    //            Statement statement = this.getConnManager().connection(schema).createStatement();
    //            statement.executeUpdate(sql);
    //            IOUtil.close(statement);
    //        } catch (Exception ex) {
    //            ex.printStackTrace();
    //            throw new ShellException(ex);
    //        }
    //    }
    //
    //    public void alertEvent(String schema, PostgresqlEvent event) {
    //        try {
    //            String sql = EventAlertSqlGenerator.generateSql(event);
    //            this.printSql(sql);
    //            Statement statement = this.getConnManager().connection(schema).createStatement();
    //            statement.executeUpdate(sql);
    //            IOUtil.close(statement);
    //        } catch (Exception ex) {
    //            ex.printStackTrace();
    //            throw new ShellException(ex);
    //        }
    //    }

    //    public PostgresqlEvent selectEvent(String schema, String eventName) {
    //        try {
    //            Connection connection = this.functionConnection(schema, null);
    //            String sql = """
    //                    SELECT
    //                        t1.ENABLED AS STATUS,
    //                        t1.JOB_ACTION AS ACTION,
    //                        t1.SCHEDULE_EXPR AS EXPR,
    //                    FROM
    //                        USER_SCHEDULER_JOBS t1
    //                    WHERE
    //                        t1.JOB_NAME = ?
    //                    """;
    //            this.printSql(sql);
    //            PreparedStatement statement = connection.prepareStatement(sql);
    //            statement.setString(1, eventName);
    //            ResultSet resultSet = statement.executeQuery();
    //            DBUtil.printMetaData(resultSet);
    //            PostgresqlEvent event = new PostgresqlEvent();
    //            while (resultSet.next()) {
    //                String expr = resultSet.getString("EXPR");
    //                String status = resultSet.getString("STATUS");
    //                String action = resultSet.getString("ACTION");
    //                event.setName(eventName);
    //                event.setStatus("TRUE".equalsIgnoreCase(status) ? "ENABLE" : "DISABLE");
    //                PostgresqlHelper.parseJobExpr(expr, event);
    //                PostgresqlHelper.parseJobAction(action, event);
    //            }
    //            IOUtil.close(resultSet);
    //            return event;
    //        } catch (Exception ex) {
    //            ex.printStackTrace();
    //            throw new ShellException(ex);
    //        }
    //    }

    //    public Integer eventSize(String schema) {
    //        int size = 0;
    //        try {
    //            Connection connection = this.functionConnection(schema, null);
    //            String sql = """
    //                    SELECT
    //                        COUNT(*)
    //                    FROM
    //                        USER_SCHEDULER_JOBS
    //                    """;
    //            this.printSql(sql);
    //            PreparedStatement statement = connection.prepareStatement(sql);
    //            ResultSet resultSet = statement.executeQuery();
    //            DBUtil.printMetaData(resultSet);
    //            if (resultSet.next()) {
    //                size = resultSet.getInt(1);
    //            }
    //            IOUtil.close(resultSet);
    //        } catch (Exception ex) {
    //            ex.printStackTrace();
    //            throw new ShellException(ex);
    //        }
    //        return size;
    //    }

    //    public List<PostgresqlEvent> events(String schema) {
    //        try {
    //            Connection connection = this.functionConnection(schema, null);
    //            String sql = """
    //                    SELECT
    //                        t1.JOB_NAME AS NAME,
    //                        t1.ENABLED AS STATUS,
    //                        t1.JOB_ACTION AS ACTION,
    //                        t1.REPEAT_INTERVAL AS EXPR
    //                    FROM
    //                        USER_SCHEDULER_JOBS t1
    //                    """;
    //            this.printSql(sql);
    //            PreparedStatement statement = connection.prepareStatement(sql);
    //            ResultSet resultSet = statement.executeQuery();
    //            DBUtil.printMetaData(resultSet);
    //            List<PostgresqlEvent> list = new ArrayList<>();
    //            while (resultSet.next()) {
    //                String name = resultSet.getString("NAME");
    //                String expr = resultSet.getString("EXPR");
    //                String status = resultSet.getString("STATUS");
    //                String action = resultSet.getString("ACTION");
    //                PostgresqlEvent event = new PostgresqlEvent();
    //                event.setName(name);
    //                event.setStatus("TRUE".equalsIgnoreCase(status) ? "ENABLE" : "DISABLE");
    //                PostgresqlHelper.parseJobExpr(expr, event);
    //                PostgresqlHelper.parseJobAction(action, event);
    //                list.add(event);
    //            }
    //            IOUtil.close(resultSet);
    //            return list;
    //        } catch (Exception ex) {
    //            ex.printStackTrace();
    //            throw new ShellException(ex);
    //        }
    //    }

    @Override
    public boolean isSupportFeature(DBFeature feature) {
        try {
            if (feature == DBFeature.EVENT) {
                return false;
            }
            // 检查约束
            if (feature == DBFeature.CHECK) {
                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * 是否支持检查约束特性
     *
     * @return 支持返回true，否则返回false
     */
    public boolean isSupportCheckFeature() {
        return this.isSupportFeature(DBFeature.CHECK);
    }

    @Override
    public Long getGeneratedKeys(Statement statement) throws Exception {
        Long newId = null;
        ResultSet rs = null;
        try {
            rs = statement.getGeneratedKeys();
            if (rs != null && rs.next()) {
                newId = rs.getLong(1);
                if (rs.wasNull()) {
                    newId = null;
                }
            }
        } catch (Exception ex) {
            // PostgreSQL部分场景不支持getGeneratedKeys，使用lastval()兜底
            JulLog.warn("getGeneratedKeys error", ex);
        } finally {
            IOUtil.close(rs);
        }
        if (newId == null) {
            Connection connection = statement.getConnection();
            IOUtil.close(statement);
            statement = connection.createStatement();
            try {
                ResultSet resultSet = statement.executeQuery("SELECT lastval()");
                if (resultSet.next()) {
                    newId = resultSet.getLong(1);
                }
                IOUtil.close(resultSet);
            } catch (Exception ex) {
                JulLog.warn("select lastval error", ex);
            }
        }
        IOUtil.close(statement);
        return newId;
    }

    /**
     * 获取序列当前值
     *
     * @param statement 语句块
     * @param column    字段
     * @return 结果
     * @throws Exception 异常
     */
    public Long getSequenceCurrVal(Statement statement, DBColumn column) throws Exception {
        if (column == null || !column.isDefaultValueSequence()) {
            return null;
        }
        String sequenceName = column.getSequenceName();
        if (StringUtil.isBlank(sequenceName)) {
            return null;
        }
        // 序列名称可能是schema.seq形式
        String schema = column.getSchema();
        String name = sequenceName;
        if (StringUtil.contains(sequenceName, ".")) {
            String[] arr = sequenceName.split("\\.", 2);
            schema = arr[0];
            name = arr[1];
        }
        // 去除引号
        if (schema != null) {
            schema = schema.replace("\"", "");
        }
        name = name.replace("\"", "");
        String sql;
        if (StringUtil.isBlank(schema)) {
            sql = "SELECT last_value FROM " + DBUtil.wrap(name, DBDialect.POSTGRESQL);
        } else {
            sql = "SELECT last_value FROM " + DBUtil.wrap(schema, name, DBDialect.POSTGRESQL);
        }
        this.printSql(sql);
        Connection connection = statement.getConnection();
        IOUtil.close(statement);
        statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        Long value = null;
        if (resultSet.next()) {
            value = resultSet.getLong(1);
        }
        IOUtil.close(resultSet);
        IOUtil.close(statement);
        return value;
    }

    //    public boolean isSupportEventFeature() {
    //        return this.isSupportFeature(DBFeature.EVENT);
    //    }

    /**
     * 查询表列表(完整信息)
     *
     * @param schema 模式名称
     * @return 表列表
     */
    public List<PostgresqlTable> selectTables(String schema) {
        PostgresqlSelectTableParam param = new PostgresqlSelectTableParam();
        param.setFull(true);
        param.setSchema(schema);
        return this.selectTables(param);
    }

    /**
     * 查询表列表(简要信息)
     *
     * @param schema 模式名称
     * @return 表列表
     */
    public List<PostgresqlTable> selectTablesSimple(String schema) {
        PostgresqlSelectTableParam param = new PostgresqlSelectTableParam();
        param.setFull(false);
        param.setSchema(schema);
        return this.selectTables(param);
    }

    /**
     * 查询表列表
     *
     * @param param 查询参数
     * @return 表列表
     */
    public List<PostgresqlTable> selectTables(PostgresqlSelectTableParam param) {
        try {
            String schema = param.getSchema();
            List<PostgresqlTable> tables = new ArrayList<>();
            Connection connection = this.getConnManager().connection();
            String sql = """
                    SELECT
                        C.RELNAME AS "TABLE_NAME",
                        PG_CATALOG.OBJ_DESCRIPTION(C.OID, 'pg_class') AS "TABLE_COMMENT",
                        COALESCE(TS.SPCNAME, DTS.SPCNAME) AS "TABLE_SPACE"
                    FROM
                        PG_CATALOG.PG_CLASS C
                    JOIN
                        PG_CATALOG.PG_NAMESPACE N ON N.OID = C.RELNAMESPACE
                    LEFT JOIN
                        PG_CATALOG.PG_TABLESPACE TS ON TS.OID = C.RELTABLESPACE
                    LEFT JOIN
                        PG_CATALOG.PG_DATABASE PD ON PD.DATNAME = CURRENT_DATABASE()
                    LEFT JOIN
                        PG_CATALOG.PG_TABLESPACE DTS ON DTS.OID = PD.DATTABLESPACE
                    WHERE
                        N.NSPNAME = ?
                    AND
                        C.RELKIND IN ('r', 'p')
                    ORDER BY
                        C.RELNAME
                    """;
            this.printSql(sql);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, schema);
            ResultSet resultSet = statement.executeQuery();
            DBUtil.printMetaData(resultSet);
            while (resultSet.next()) {
                PostgresqlTable table = new PostgresqlTable();
                String tableName = resultSet.getString("TABLE_NAME");
                String tableSpace = resultSet.getString("TABLE_SPACE");
                String tableComment = resultSet.getString("TABLE_COMMENT");
                table.setSchema(schema);
                table.setName(tableName);
                table.setComment(tableComment);
                table.setTableSpace(tableSpace);
                tables.add(table);
            }
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            // 完整信息才生成建表语句
            if (param.isFull()) {
                for (PostgresqlTable table : tables) {
                    try {
                        table.setCreateDefinition(this.showCreateTable(schema, table.getName()));
                    } catch (Exception ex) {
                        JulLog.warn("showCreateTable error, table: {}", table.getName(), ex);
                    }
                }
            }
            DBName.sortItems(tables);
            return tables;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    //    public List<PostgresqlTable> selectTablesSimple(String schema) {
    //        try {
    //            List<PostgresqlTable> tables = new ArrayList<>();
    //            Connection connection = this.getConnManager().connection(schema);
    //            String sql = """
    //                        SELECT
    //                            T.TABLE_NAME, T.TABLESPACE_NAME AS "TABLE_SPACE"
    //                        FROM
    //                            ALL_TABLES T
    //                        WHERE
    //                            T.OWNER = ?
    //                    """;
    //            this.printSql(sql);
    //            PreparedStatement statement = connection.prepareStatement(sql);
    //            statement.setString(1, schema);
    //            ResultSet resultSet = statement.executeQuery();
    //            DBUtil.printMetaData(resultSet);
    //            while (resultSet.next()) {
    //                PostgresqlTable table = new PostgresqlTable();
    //                String tableName = resultSet.getString("TABLE_NAME");
    //                String tableSpace = resultSet.getString("TABLE_SPACE");
    //                table.setschema(schema);
    //                table.setName(tableName);
    //                table.setTableSpace(tableSpace);
    //                tables.add(table);
    //            }
    //            IOUtil.close(resultSet);
    //            IOUtil.close(statement);
    //            return tables;
    //        } catch (Exception ex) {
    //            ex.printStackTrace();
    //            throw new ShellException(ex);
    //        }
    //    }

    /**
     * 查询字段列表
     *
     * @param param 查询参数
     * @return 字段列表
     */
    public List<PostgresqlColumn> selectColumns(PostgresqlSelectColumnParam param) {
        try {
            String schema = param.getSchema();
            String tableName = param.getTableName();
            PostgresqlColumns columns = new PostgresqlColumns();
            String sql;
            //            if (this.isDbaRole()) {
            //                sql = """
            //                        SELECT
            //                            C.COLUMN_ID AS "POSITION",
            //                            C.COLUMN_NAME AS "Field",
            //                            C.DATA_SCALE AS "DATA_SCALE",
            //                            C.DATA_LENGTH AS "DATA_LENGTH",
            //                            CASE WHEN C.NULLABLE='Y' THEN 'YES' ELSE 'NO' END AS "Null",
            //                            C.DATA_TYPE AS "Type",
            //                            CC.COMMENT$ AS "Comment",
            //                            C.DATA_DEFAULT AS "Default",
            //                            CASE WHEN CONS.COLUMN_NAME IS NOT NULL THEN 'PRI' ELSE '' END AS "Key"
            //                        FROM
            //                            ALL_TAB_COLUMNS C
            //                        LEFT JOIN
            //                            SYS.SYSCOLUMNCOMMENTS CC
            //                        ON
            //                            C.OWNER = CC.SCHNAME
            //                        AND
            //                            C.TABLE_NAME = CC.TVNAME
            //                        AND
            //                            C.COLUMN_NAME = CC.COLNAME
            //                        LEFT JOIN
            //                            ALL_CONS_COLUMNS CONS
            //                        ON
            //                            C.OWNER = CONS.OWNER
            //                        AND
            //                            C.TABLE_NAME = CONS.TABLE_NAME
            //                        AND
            //                            C.COLUMN_NAME = CONS.COLUMN_NAME
            //                        AND
            //                            EXISTS (SELECT 1 FROM ALL_CONSTRAINTS AC WHERE AC.OWNER=CONS.OWNER AND AC.CONSTRAINT_NAME=CONS.CONSTRAINT_NAME AND AC.CONSTRAINT_TYPE='P')
            //                        WHERE
            //                            C.OWNER = ?
            //                        AND
            //                            C.TABLE_NAME = ?
            //                        """;
            //            } else {
            sql = """
                    SELECT
                        A.ATTNAME AS "Field",
                        PG_CATALOG.FORMAT_TYPE(A.ATTTYPID, A.ATTTYPMOD) AS "Type",
                        PG_CATALOG.COL_DESCRIPTION(A.ATTRELID, A.ATTNUM) AS "Comment",
                        A.ATTNUM AS "POSITION",
                        PG_CATALOG.PG_GET_EXPR(AD.ADBIN, AD.ADRELID) AS "Default",
                        A.ATTNOTNULL AS "NotNull",
                        CASE WHEN EXISTS (
                            SELECT 1
                            FROM PG_CATALOG.PG_INDEX I
                            WHERE I.INDRELID = A.ATTRELID
                            AND I.INDISPRIMARY
                            AND A.ATTNUM = ANY(I.INDKEY::SMALLINT[])
                        ) THEN 'PRI' ELSE '' END AS "Key",
                        A.ATTIDENTITY AS "IDENTITY"
                    FROM
                        PG_CATALOG.PG_ATTRIBUTE A
                    JOIN
                        PG_CATALOG.PG_CLASS C ON C.OID = A.ATTRELID
                    JOIN
                        PG_CATALOG.PG_NAMESPACE N ON N.OID = C.RELNAMESPACE
                    LEFT JOIN
                        PG_CATALOG.PG_ATTRDEF AD ON AD.ADRELID = A.ATTRELID AND AD.ADNUM = A.ATTNUM
                    WHERE
                        N.NSPNAME = ?
                    AND
                        C.RELNAME = ?
                    AND
                        A.ATTNUM > 0
                    AND
                        NOT A.ATTISDROPPED
                    ORDER BY
                        A.ATTNUM
                    """;
            //            }
            Connection connection = this.getConnManager().connection();
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, schema);
            statement.setString(2, tableName);
            ResultSet resultSet = statement.executeQuery();
            //            DatabaseMetaData metaData = connection.getMetaData();
            DBUtil.printMetaData(resultSet);
            while (resultSet.next()) {
                String key = resultSet.getString("Key");
                String type = resultSet.getString("Type");
                String field = resultSet.getString("Field");
                Object def = resultSet.getObject("Default");
                int position = resultSet.getInt("POSITION");
                boolean notNull = resultSet.getBoolean("NotNull");
                String comment = resultSet.getString("Comment");
                String identity = resultSet.getString("IDENTITY");
                // 自动递增
                boolean autoIncrement = StringUtil.isNotBlank(identity)
                        || StringUtil.startWithIgnoreCase(def == null ? null : def.toString().trim(), "nextval(");
                PostgresqlColumn column = new PostgresqlColumn();
                column.parseKey(key);
                column.setName(field);
                // 解析类型中的长度和小数位
                this.parseColumnType(column, type);
                column.setSchema(schema);
                column.setComment(comment);
                column.setDefaultValue(def);
                column.setPosition(position);
                column.setTableName(tableName);
                column.setNullable(!notNull);
                column.setAutoIncrement(autoIncrement);
                if (StringUtil.equalsIgnoreCase(identity, "a")) {
                    column.setAutoIncrementType("GENERATED ALWAYS");
                } else if (StringUtil.isNotBlank(identity) || autoIncrement) {
                    column.setAutoIncrementType("GENERATED BY DEFAULT");
                }
                columns.add(column);
            }
            IOUtil.close(resultSet);
            // 初始化自动递增
            this.initAutoIncrement(connection, schema, tableName, columns, false);
            // 返回排序后的数据
            return new ArrayList<>(columns.sortOfPosition());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 解析字段类型，提取类型名称、长度和小数位
     *
     * @param column     字段
     * @param columnType 完整类型，如character varying(255)、numeric(10,2)
     */
    private void parseColumnType(PostgresqlColumn column, String columnType) {
        if (StringUtil.isBlank(columnType)) {
            column.setSize(null);
            column.setDigits(null);
            return;
        }
        String type = columnType.trim();
        Integer size = null;
        Integer digits = null;
        // 只匹配由数字组成的类型修饰符，避免误解析geometry(Point,4326)等类型
        Matcher matcher = Pattern.compile("\\((\\d+)(?:\\s*,\\s*(\\d+))?\\)").matcher(type);
        if (matcher.find()) {
            String typeName = (type.substring(0, matcher.start()) + type.substring(matcher.end()))
                    .replaceAll("\\s+", " ")
                    .trim();
            // timestamp/time 的精度修饰符位于 WITH/WITHOUT TIME ZONE 之前，不能作为长度拼接
            if (StringUtil.containsIgnoreCase(typeName, "TIME ZONE")) {
                column.setType(type.replaceAll("\\s+", " ").trim());
                column.setSize(null);
                column.setDigits(null);
                return;
            }
            column.setType(typeName);
            size = Integer.parseInt(matcher.group(1));
            if (matcher.group(2) != null) {
                digits = Integer.parseInt(matcher.group(2));
            }
        } else {
            column.setType(type);
        }
        // 始终初始化长度/小数位的原始值，便于后续变更检测
        column.setSize(size);
        column.setDigits(digits);
    }

    /**
     * 是否整型类型
     *
     * @param type 类型名称
     * @return 结果
     */
    private boolean isIntegerType(String type) {
        return StringUtil.equalsAnyIgnoreCase(type, "smallint", "integer", "int", "int2", "int4", "bigint", "int8");
    }

    /**
     * 默认值是否nextval序列
     *
     * @param defaultValue 默认值
     * @return 结果
     */
    private boolean isNextValDefault(String defaultValue) {
        return StringUtil.startWithIgnoreCase(defaultValue == null ? null : defaultValue.trim(), "nextval(");
    }

    /**
     * 初始化自动递增
     *
     * @param connection 连接
     * @param schema     模式
     * @param name       名称
     * @param columns    字段列表
     * @throws SQLException 异常
     */
    private void initAutoIncrement(Connection connection, String schema, String name, PostgresqlColumns columns, boolean isView) throws SQLException {
        if (isView) {
            this.initViewAutoIncrement(connection, schema, name, columns);
            return;
        }
        String sql = """
                SELECT
                    A.ATTNAME AS "COLUMN_NAME",
                    A.ATTIDENTITY AS "IDENTITY",
                    PG_CATALOG.PG_GET_EXPR(AD.ADBIN, AD.ADRELID) AS "DEFAULT_VALUE"
                FROM
                    PG_CATALOG.PG_ATTRIBUTE A
                JOIN
                    PG_CATALOG.PG_CLASS C ON C.OID = A.ATTRELID
                JOIN
                    PG_CATALOG.PG_NAMESPACE N ON N.OID = C.RELNAMESPACE
                LEFT JOIN
                    PG_CATALOG.PG_ATTRDEF AD ON AD.ADRELID = A.ATTRELID AND AD.ADNUM = A.ATTNUM
                WHERE
                    N.NSPNAME = ?
                AND
                    C.RELNAME = ?
                AND
                    A.ATTNUM > 0
                AND
                    NOT A.ATTISDROPPED
                """;
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, schema);
        statement.setString(2, name);
        ResultSet resultSet = statement.executeQuery();
        DBUtil.printMetaData(resultSet);
        while (resultSet.next()) {
            String columnName = resultSet.getString("COLUMN_NAME");
            String identity = resultSet.getString("IDENTITY");
            String defaultValue = resultSet.getString("DEFAULT_VALUE");
            boolean autoIncrement = StringUtil.isNotBlank(identity)
                    || StringUtil.startWithIgnoreCase(defaultValue == null ? null : defaultValue.trim(), "nextval(");
            PostgresqlColumn column = columns.column(columnName);
            if (column != null && autoIncrement) {
                column.setAutoIncrement(true);
                if (!isView) {
                    if (StringUtil.equalsIgnoreCase(identity, "a")) {
                        column.setAutoIncrementType("GENERATED ALWAYS");
                    } else {
                        column.setAutoIncrementType("GENERATED BY DEFAULT");
                    }
                }
            }
        }
        IOUtil.close(resultSet);
        IOUtil.close(statement);
    }

    /**
     * 初始化视图自动递增字段
     * 视图自身不保留基表IDENTITY信息，需要从视图依赖的基表列中查找
     *
     * @param connection 连接
     * @param schema     模式
     * @param viewName   视图名称
     * @param columns    视图字段列表
     * @throws SQLException 异常
     */
    private void initViewAutoIncrement(Connection connection, String schema, String viewName, PostgresqlColumns columns) throws SQLException {
        String sql = """
                SELECT DISTINCT
                    U.COLUMN_NAME AS "COLUMN_NAME"
                FROM
                    INFORMATION_SCHEMA.VIEW_COLUMN_USAGE U
                JOIN
                    PG_CATALOG.PG_CLASS C ON C.RELNAME = U.TABLE_NAME
                JOIN
                    PG_CATALOG.PG_NAMESPACE N ON N.OID = C.RELNAMESPACE AND N.NSPNAME = U.TABLE_SCHEMA
                JOIN
                    PG_CATALOG.PG_ATTRIBUTE A ON A.ATTRELID = C.OID AND A.ATTNAME = U.COLUMN_NAME
                LEFT JOIN
                    PG_CATALOG.PG_ATTRDEF AD ON AD.ADRELID = A.ATTRELID AND AD.ADNUM = A.ATTNUM
                WHERE
                    U.VIEW_SCHEMA = ?
                AND
                    U.VIEW_NAME = ?
                AND
                    (
                        A.ATTIDENTITY <> ''
                        OR
                        PG_CATALOG.PG_GET_EXPR(AD.ADBIN, AD.ADRELID) LIKE 'nextval(%'
                    )
                """;
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, schema);
        statement.setString(2, viewName);
        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
            PostgresqlColumn column = columns.column(resultSet.getString("COLUMN_NAME"));
            if (column != null) {
                column.setAutoIncrement(true);
            }
        }
        IOUtil.close(resultSet);
        IOUtil.close(statement);
    }

    /**
     * 查询记录列表
     *
     * @param param 查询参数
     * @return 记录列表
     */
    public List<PostgresqlRecord> selectRecords(PostgresqlSelectRecordParam param) {
        try {
            Connection connection = this.getConnManager().connection();
            StringBuilder builder = new StringBuilder("SELECT * FROM ");
            builder.append(DBUtil.wrap(param.getSchema(), param.getTableName(), DBDialect.POSTGRESQL));
            String filterCondition = DBUtil.buildCondition(param.getFilters());
            if (StringUtil.isNotBlank(filterCondition)) {
                builder.append(" WHERE ").append(filterCondition);
            }
            if (param.hasPageControl()) {
                builder.append(" LIMIT ")
                        .append(param.getLimit())
                        .append(" OFFSET ")
                        .append(param.getStart());
            }
            String sql = builder.toString();
            this.printSql(sql);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            DBUtil.printMetaData(resultSet);
            List<PostgresqlRecord> records = new ArrayList<>();
            List<PostgresqlColumn> columns;
            if (param.getColumns() != null) {
                columns = param.getColumns();
            } else {
                columns = ShellPostgresqlHelper.parseColumns(resultSet);
            }
            while (resultSet.next()) {
                PostgresqlRecord record = new PostgresqlRecord(columns, param.isReadonly());
                for (PostgresqlColumn column : columns) {
                    Object data = resultSet.getObject(column.getName());
                    record.putValue(column, data);
                }
                records.add(record);
            }
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            return records;
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    /**
     * 查询记录总数
     *
     * @param param 查询参数
     * @return 记录总数
     */
    public long selectRecordCount(PostgresqlSelectRecordParam param) {
        long count = 0;
        try {
            Connection connection = this.getConnManager().connection();
            StringBuilder builder = new StringBuilder("SELECT COUNT(*) FROM ");
            builder.append(DBUtil.wrap(param.getSchema(), param.getTableName(), DBDialect.POSTGRESQL));
            String filterCondition = DBUtil.buildCondition(param.getFilters());
            if (StringUtil.isNotBlank(filterCondition)) {
                builder.append(" WHERE ").append(filterCondition);
            }
            String sql = builder.toString();
            this.printSql(sql);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
                count = resultSet.getLong(1);
            }
            IOUtil.close(resultSet);
            IOUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
        return count;
    }

    /**
     * 新增记录
     *
     * @param param 新增参数
     * @return 受影响的行数
     */
    public int insertRecord(PostgresqlInsertRecordParam param) {
        if (param == null || param.getRecord() == null) {
            return 0;
        }
        try {
            DBRecordData recordData = param.getRecord();
            String tableName = DBUtil.wrap(param.getSchema(), param.getTableName(), DBDialect.POSTGRESQL);
            // 需要插入的字段
            List<String> insertColumns = new ArrayList<>();
            for (String column : recordData.columns()) {
                if (this.skipInsertColumn(recordData, column)) {
                    continue;
                }
                insertColumns.add(column);
            }
            StringBuilder builder = new StringBuilder("INSERT INTO ").append(tableName);
            if (insertColumns.isEmpty()) {
                builder.append(" DEFAULT VALUES");
            } else {
                builder.append("(");
                for (String column : insertColumns) {
                    builder.append(DBUtil.wrap(column, DBDialect.POSTGRESQL)).append(",");
                }
                StringUtil.deleteLast(builder);
                builder.append(")");
                // GENERATED ALWAYS AS IDENTITY列写入显式值时需要OVERRIDING SYSTEM VALUE
                if (this.hasAlwaysIdentityColumn(param.getSchema(), param.getTableName())) {
                    builder.append(" OVERRIDING SYSTEM VALUE");
                }
                builder.append(" VALUES(");
                for (String column : insertColumns) {
                    DBColumn dbColumn = recordData.column(column);
                    builder.append(this.valuePlaceholder(dbColumn)).append(",");
                }
                StringUtil.deleteLast(builder);
                builder.append(")");
            }
            PostgresqlRecordPrimaryKey primaryKey = param.getPrimaryKey();
            // PostgreSQL使用RETURNING返回自增主键
            boolean returning = primaryKey != null && primaryKey.shouldReturnData()
                    && StringUtil.isNotBlank(primaryKey.getColumnName());
            if (returning) {
                builder.append(" RETURNING ").append(DBUtil.wrap(primaryKey.getColumnName(), DBDialect.POSTGRESQL));
            }
            String sql = builder.toString();
            this.printSql(sql);
            Connection connection = this.getConnManager().connection();
            PreparedStatement statement = connection.prepareStatement(sql);
            int index = 1;
            for (String column : insertColumns) {
                DBUtil.setVal(statement, recordData.value(column), index++);
            }
            int count;
            Object newId = null;
            if (returning) {
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    newId = resultSet.getLong(1);
                    if (resultSet.wasNull()) {
                        newId = null;
                    }
                }
                IOUtil.close(resultSet);
                count = newId == null ? 0 : 1;
            } else {
                count = statement.executeUpdate();
            }
            // 处理自动递增值
            if (primaryKey != null) {
                // 正常主键
                if (primaryKey.shouldReturnData()) {
                    if (newId == null) {
                        newId = this.getGeneratedKeys(statement);
                    }
                } else if (primaryKey.getColumn() != null && primaryKey.getColumn().isDefaultValueSequence()) {
                    // 处理序列值
                    newId = this.getSequenceCurrVal(statement, primaryKey.getColumn());
                    primaryKey.setData(newId);
                } else {
                    // 显式指定了主键值，直接使用
                    newId = primaryKey.data();
                }
                primaryKey.setReturnData(newId);
            }
            IOUtil.close(statement);
            return count;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 插入记录时是否跳过字段
     * 序列字段、自增字段未指定值时交由数据库生成
     *
     * @param recordData 记录数据
     * @param columnName 字段名称
     * @return 结果
     */
    private boolean skipInsertColumn(DBRecordData recordData, String columnName) {
        if (recordData.isDefaultValueSequence(columnName)) {
            return true;
        }
        DBColumn column = recordData.column(columnName);
        if (column != null && column.isAutoIncrement()) {
            Object value = recordData.value(columnName);
            if (value == null) {
                return true;
            }
            Object defaultValue = column.getDefaultValue();
            if (defaultValue != null && StringUtil.equals(String.valueOf(value), String.valueOf(defaultValue))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 删除记录
     *
     * @param param 删除参数
     * @return 受影响的行数
     */
    public int deleteRecord(PostgresqlDeleteRecordParam param) {
        try {
            int updateCount;
            String schema = param.getSchema();
            String tableName = param.getTableName();
            Connection connection = this.getConnManager().connection();
            StringBuilder builder = new StringBuilder();
            builder.append("DELETE FROM ")
                    .append(DBUtil.wrap(schema, tableName, DBDialect.POSTGRESQL))
                    .append(" WHERE ");
            if (param.getPrimaryKey() == null) {
                DBRecordData recordData = param.getRecord();
                boolean first = true;
                for (String colName : recordData.columns()) {
                    if (first) {
                        first = false;
                    } else {
                        builder.append(" AND ");
                    }
                    if (recordData.hasValue(colName)) {
                        builder.append(DBUtil.wrap(colName, DBDialect.POSTGRESQL))
                                .append(" = ?");
                    } else {
                        builder.append(DBUtil.wrap(colName, DBDialect.POSTGRESQL))
                                .append(" IS NULL");
                    }
                }
                String sql = builder.toString();
                this.printSql(sql);
                PreparedStatement statement = connection.prepareStatement(sql);
                int index = 1;
                // 设置参数
                for (String colName : recordData.notNullColumns()) {
                    DBUtil.setVal(statement, recordData.value(colName), index++);
                }
                updateCount = DBUtil.executeUpdate(statement);
            } else {
                PostgresqlRecordPrimaryKey primaryKey = param.getPrimaryKey();
                builder.append(DBUtil.wrap(primaryKey.getColumnName(), DBDialect.POSTGRESQL))
                        .append(" = ?");
                String sql = builder.toString();
                this.printSql(sql);
                PreparedStatement statement = connection.prepareStatement(sql);
                DBUtil.setVal(statement, primaryKey.originalData(), 1);
                updateCount = DBUtil.executeUpdate(statement);
                IOUtil.close(statement);
            }
            return updateCount;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 修改记录
     *
     * @param param 修改参数
     * @return 受影响的行数
     */
    public int updateRecord(PostgresqlUpdateRecordParam param) {
        try {
            int updateCount;
            String schema = param.getSchema();
            String tableName = param.getTableName();
            DBRecordData recordData = param.getUpdateRecord();
            StringBuilder builder = new StringBuilder();
            builder.append("UPDATE ")
                    .append(DBUtil.wrap(schema, tableName, DBDialect.POSTGRESQL))
                    .append(" SET ");
            for (String column : recordData.columns()) {
                DBColumn dbColumn = recordData.column(column);
                builder.append(DBUtil.wrap(column, DBDialect.POSTGRESQL))
                        .append(" = ").append(this.valuePlaceholder(dbColumn)).append(",");
            }
            builder.deleteCharAt(builder.length() - 1);
            builder.append(" WHERE ");
            Connection connection = this.getConnManager().connection();
            if (param.getPrimaryKey() == null) {
                DBRecordData originalRecordData = param.getRecord();
                // 参数
                boolean first = true;
                for (String column : originalRecordData.columns()) {
                    if (first) {
                        first = false;
                    } else {
                        builder.append(" AND ");
                    }
                    Object value = originalRecordData.value(column);
                    if (value == null) {
                        builder.append(DBUtil.wrap(column, DBDialect.POSTGRESQL)).append(" IS NULL");
                    } else {
                        DBColumn dbColumn = originalRecordData.column(column);
                        builder.append(DBUtil.wrap(column, DBDialect.POSTGRESQL))
                                .append(" = ").append(this.valuePlaceholder(dbColumn));
                    }
                }
                int index = 1;
                String sql = builder.toString();
                this.printSql(sql);
                PreparedStatement statement = connection.prepareStatement(sql);
                // 设置值
                for (String colName : recordData.columns()) {
                    DBUtil.setVal(statement, recordData.value(colName), index++);
                }
                // 设置参数
                for (String colName : originalRecordData.columns()) {
                    Object value = originalRecordData.value(colName);
                    if (value != null) {
                        DBUtil.setVal(statement, value, index++);
                    }
                }
                updateCount = DBUtil.executeUpdate(statement);
                IOUtil.close(statement);
            } else {
                PostgresqlRecordPrimaryKey primaryKey = param.getPrimaryKey();
                builder.append(DBUtil.wrap(primaryKey.getColumnName(), DBDialect.POSTGRESQL)).append(" = ?");
                String sql = builder.toString();
                DBUtil.printInfo(sql, recordData);
                PreparedStatement statement = connection.prepareStatement(sql);
                int index = 1;
                // 设置值
                for (String colName : recordData.columns()) {
                    DBUtil.setVal(statement, recordData.value(colName), index++);
                }
                // 设置参数
                DBUtil.setVal(statement, primaryKey.originalData(), index);
                updateCount = DBUtil.executeUpdate(statement);
                IOUtil.close(statement);
            }
            return updateCount;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 获取参数占位符，PostgreSQL的部分类型需要显式类型转换
     *
     * @param column 字段
     * @return 占位符
     */
    private String valuePlaceholder(DBColumn column) {
        if (column == null) {
            return "?";
        }
        String type = column.getType();
        if (StringUtil.isBlank(type)) {
            return "?";
        }
        if (column.supportJson()
                || StringUtil.equalsAnyIgnoreCase(type, "UUID", "XML", "INET", "CIDR", "MACADDR",
                "INTERVAL", "TSVECTOR", "TSQUERY", "POINT", "LINE", "LSEG", "BOX", "PATH", "POLYGON", "CIRCLE")
                || type.endsWith("[]")) {
            return "?::" + type;
        }
        return "?";
    }

    /**
     * 查询建表语句
     *
     * @param schema    模式名称
     * @param tableName 表名称
     * @return 建表语句
     */
    public String showCreateTable(String schema, String tableName) {
        try {
            Connection connection = this.getConnManager().connection();
            String fullName = DBUtil.wrap(schema, tableName, DBDialect.POSTGRESQL);
            List<String> definitions = new ArrayList<>();
            List<String> comments = new ArrayList<>();
            // 字段定义
            String sql = """
                    SELECT
                        A.ATTNAME AS "Field",
                        PG_CATALOG.FORMAT_TYPE(A.ATTTYPID, A.ATTTYPMOD) AS "Type",
                        PG_CATALOG.PG_GET_EXPR(AD.ADBIN, AD.ADRELID) AS "Default",
                        A.ATTNOTNULL AS "NotNull",
                        A.ATTIDENTITY AS "IDENTITY",
                        PG_CATALOG.COL_DESCRIPTION(A.ATTRELID, A.ATTNUM) AS "Comment"
                    FROM
                        PG_CATALOG.PG_ATTRIBUTE A
                    JOIN
                        PG_CATALOG.PG_CLASS C ON C.OID = A.ATTRELID
                    JOIN
                        PG_CATALOG.PG_NAMESPACE N ON N.OID = C.RELNAMESPACE
                    LEFT JOIN
                        PG_CATALOG.PG_ATTRDEF AD ON AD.ADRELID = A.ATTRELID AND AD.ADNUM = A.ATTNUM
                    WHERE
                        N.NSPNAME = ?
                    AND
                        C.RELNAME = ?
                    AND
                        A.ATTNUM > 0
                    AND
                        NOT A.ATTISDROPPED
                    ORDER BY
                        A.ATTNUM
                    """;
            this.printSql(sql);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, schema);
            statement.setString(2, tableName);
            ResultSet resultSet = statement.executeQuery();
            DBUtil.printMetaData(resultSet);
            while (resultSet.next()) {
                String field = resultSet.getString("Field");
                String type = resultSet.getString("Type");
                String defaultValue = resultSet.getString("Default");
                boolean notNull = resultSet.getBoolean("NotNull");
                String identity = resultSet.getString("IDENTITY");
                String comment = resultSet.getString("Comment");
                // serial列（整型+nextval默认值）转换为IDENTITY，避免建表时依赖尚未创建的序列
                boolean serial = StringUtil.isBlank(identity)
                        && this.isIntegerType(type)
                        && this.isNextValDefault(defaultValue);
                StringBuilder builder = new StringBuilder("  ");
                builder.append(DBUtil.wrap(field, DBDialect.POSTGRESQL)).append(" ").append(type);
                if (StringUtil.isNotBlank(defaultValue) && !serial) {
                    builder.append(" DEFAULT ").append(defaultValue);
                }
                if (notNull) {
                    builder.append(" NOT NULL");
                }
                if (serial) {
                    builder.append(" GENERATED BY DEFAULT AS IDENTITY");
                } else if (StringUtil.equalsIgnoreCase(identity, "a")) {
                    builder.append(" GENERATED ALWAYS AS IDENTITY");
                } else if (StringUtil.equalsIgnoreCase(identity, "d")) {
                    builder.append(" GENERATED BY DEFAULT AS IDENTITY");
                }
                definitions.add(builder.toString());
                if (StringUtil.isNotBlank(comment)) {
                    comments.add("COMMENT ON COLUMN " + fullName + "."
                            + DBUtil.wrap(field, DBDialect.POSTGRESQL) + " IS " + this.quoteLiteral(comment) + ";");
                }
            }
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            // 主键定义
            String primaryKeySql = """
                    SELECT
                        CON.CONNAME AS "NAME",
                        A.ATTNAME AS "COLUMN_NAME"
                    FROM
                        PG_CATALOG.PG_CONSTRAINT CON
                    JOIN
                        PG_CATALOG.PG_CLASS C ON C.OID = CON.CONRELID
                    JOIN
                        PG_CATALOG.PG_NAMESPACE N ON N.OID = C.RELNAMESPACE
                    JOIN
                        PG_CATALOG.PG_ATTRIBUTE A ON A.ATTRELID = CON.CONRELID AND A.ATTNUM = ANY(CON.CONKEY)
                    WHERE
                        CON.CONTYPE = 'p'
                    AND
                        N.NSPNAME = ?
                    AND
                        C.RELNAME = ?
                    ORDER BY
                        ARRAY_POSITION(CON.CONKEY, A.ATTNUM)
                    """;
            this.printSql(primaryKeySql);
            statement = connection.prepareStatement(primaryKeySql);
            statement.setString(1, schema);
            statement.setString(2, tableName);
            resultSet = statement.executeQuery();
            String primaryKeyName = null;
            List<String> primaryKeys = new ArrayList<>();
            while (resultSet.next()) {
                primaryKeyName = resultSet.getString("NAME");
                primaryKeys.add(resultSet.getString("COLUMN_NAME"));
            }
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            if (!primaryKeys.isEmpty()) {
                StringBuilder builder = new StringBuilder("  CONSTRAINT ");
                builder.append(DBUtil.wrap(primaryKeyName, DBDialect.POSTGRESQL)).append(" PRIMARY KEY (");
                for (String primaryKey : primaryKeys) {
                    builder.append(DBUtil.wrap(primaryKey, DBDialect.POSTGRESQL)).append(",");
                }
                StringUtil.deleteLast(builder, ",");
                builder.append(")");
                definitions.add(builder.toString());
            }
            // 唯一约束定义
            String uniqueSql = """
                    SELECT
                        CON.CONNAME AS "NAME",
                        A.ATTNAME AS "COLUMN_NAME"
                    FROM
                        PG_CATALOG.PG_CONSTRAINT CON
                    JOIN
                        PG_CATALOG.PG_CLASS C ON C.OID = CON.CONRELID
                    JOIN
                        PG_CATALOG.PG_NAMESPACE N ON N.OID = C.RELNAMESPACE
                    JOIN
                        PG_CATALOG.PG_ATTRIBUTE A ON A.ATTRELID = CON.CONRELID AND A.ATTNUM = ANY(CON.CONKEY)
                    WHERE
                        CON.CONTYPE = 'u'
                    AND
                        N.NSPNAME = ?
                    AND
                        C.RELNAME = ?
                    ORDER BY
                        CON.CONNAME, ARRAY_POSITION(CON.CONKEY, A.ATTNUM)
                    """;
            this.printSql(uniqueSql);
            statement = connection.prepareStatement(uniqueSql);
            statement.setString(1, schema);
            statement.setString(2, tableName);
            resultSet = statement.executeQuery();
            Map<String, List<String>> uniqueConstraints = new LinkedHashMap<>();
            while (resultSet.next()) {
                String uniqueName = resultSet.getString("NAME");
                uniqueConstraints.computeIfAbsent(uniqueName, key -> new ArrayList<>())
                        .add(resultSet.getString("COLUMN_NAME"));
            }
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            for (Map.Entry<String, List<String>> entry : uniqueConstraints.entrySet()) {
                StringBuilder builder = new StringBuilder("  CONSTRAINT ");
                builder.append(DBUtil.wrap(entry.getKey(), DBDialect.POSTGRESQL)).append(" UNIQUE (");
                for (String column : entry.getValue()) {
                    builder.append(DBUtil.wrap(column, DBDialect.POSTGRESQL)).append(",");
                }
                StringUtil.deleteLast(builder, ",");
                builder.append(")");
                definitions.add(builder.toString());
            }
            // 外键定义
            List<PostgresqlForeignKey> foreignKeys = this.selectForeignKeys(schema, tableName);
            if (foreignKeys != null) {
                for (PostgresqlForeignKey foreignKey : foreignKeys) {
                    StringBuilder builder = new StringBuilder("  CONSTRAINT ");
                    builder.append(DBUtil.wrap(foreignKey.getName(), DBDialect.POSTGRESQL)).append(" FOREIGN KEY (");
                    for (String column : foreignKey.getColumns()) {
                        builder.append(DBUtil.wrap(column, DBDialect.POSTGRESQL)).append(",");
                    }
                    StringUtil.deleteLast(builder, ",");
                    builder.append(") REFERENCES ")
                            .append(DBUtil.wrap(foreignKey.getReferencedDatabase(), foreignKey.getReferencedTable(), DBDialect.POSTGRESQL))
                            .append(" (");
                    for (String column : foreignKey.getReferencedColumns()) {
                        builder.append(DBUtil.wrap(column, DBDialect.POSTGRESQL)).append(",");
                    }
                    StringUtil.deleteLast(builder, ",");
                    builder.append(")");
                    if (StringUtil.isNotBlank(foreignKey.getUpdatePolicy())) {
                        builder.append(" ON UPDATE ").append(foreignKey.getUpdatePolicy());
                    }
                    if (StringUtil.isNotBlank(foreignKey.getDeletePolicy())) {
                        builder.append(" ON DELETE ").append(foreignKey.getDeletePolicy());
                    }
                    definitions.add(builder.toString());
                }
            }
            // 检查约束定义
            List<PostgresqlCheck> checks = this.selectChecks(schema, tableName);
            if (checks != null) {
                for (PostgresqlCheck check : checks) {
                    if (StringUtil.isBlank(check.getClause())) {
                        continue;
                    }
                    definitions.add("  CONSTRAINT " + DBUtil.wrap(check.getName(), DBDialect.POSTGRESQL)
                            + " CHECK (" + check.getClause() + ")");
                }
            }
            // 表定义
            StringBuilder builder = new StringBuilder("CREATE TABLE ").append(fullName).append(" (\n");
            for (int i = 0; i < definitions.size(); i++) {
                builder.append(definitions.get(i));
                if (i < definitions.size() - 1) {
                    builder.append(",");
                }
                builder.append("\n");
            }
            builder.append(");");
            // 表注释
            String tableComment = this.selectTableComment(schema, tableName);
            if (StringUtil.isNotBlank(tableComment)) {
                builder.append("\n\nCOMMENT ON TABLE ").append(fullName)
                        .append(" IS ").append(this.quoteLiteral(tableComment)).append(";");
            }
            for (String comment : comments) {
                builder.append("\n").append(comment);
            }
            // 索引定义
            List<PostgresqlIndex> indexes = this.indexes(schema, tableName);
            if (indexes != null) {
                for (PostgresqlIndex index : indexes) {
                    if (index == null || index.getColumns() == null || index.getColumns().isEmpty()) {
                        continue;
                    }
                    builder.append("\n\nCREATE ");
                    if ("UNIQUE".equalsIgnoreCase(index.getType())) {
                        builder.append("UNIQUE ");
                    }
                    builder.append("INDEX ").append(DBUtil.wrap(index.getName(), DBDialect.POSTGRESQL))
                            .append(" ON ").append(fullName);
                    if (StringUtil.isNotBlank(index.getMethod())) {
                        builder.append(" USING ").append(index.getMethod());
                    }
                    builder.append(" (");
                    for (PostgresqlIndex.IndexColumn column : index.getColumns()) {
                        builder.append(DBUtil.wrap(column.getColumnName(), DBDialect.POSTGRESQL)).append(",");
                    }
                    StringUtil.deleteLast(builder, ",");
                    builder.append(");");
                }
            }
            return builder.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 查询表注释
     *
     * @param schema    模式名称
     * @param tableName 表名称
     * @return 表注释
     * @throws Exception 异常
     */
    private String selectTableComment(String schema, String tableName) throws Exception {
        Connection connection = this.getConnManager().connection();
        String sql = """
                SELECT
                    PG_CATALOG.OBJ_DESCRIPTION(C.OID, 'pg_class') AS "TABLE_COMMENT"
                FROM
                    PG_CATALOG.PG_CLASS C
                JOIN
                    PG_CATALOG.PG_NAMESPACE N ON N.OID = C.RELNAMESPACE
                WHERE
                    N.NSPNAME = ?
                AND
                    C.RELNAME = ?
                """;
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, schema);
        statement.setString(2, tableName);
        ResultSet resultSet = statement.executeQuery();
        String comment = null;
        if (resultSet.next()) {
            comment = resultSet.getString("TABLE_COMMENT");
        }
        IOUtil.close(resultSet);
        IOUtil.close(statement);
        return comment;
    }

    /**
     * 转换SQL字符串字面量
     *
     * @param value 值
     * @return 结果
     */
    private String quoteLiteral(String value) {
        return "'" + value.replace("'", "''") + "'";
    }

    /**
     * 查询建视图语句
     *
     * @param schema   模式名称
     * @param viewName 视图名称
     * @return 建视图语句
     */
    public String showCreateView(String schema, String viewName) {
        try {
            Connection connection = this.getConnManager().connection();
            String sql = """
                    SELECT
                        PG_CATALOG.PG_GET_VIEWDEF(C.OID, TRUE) AS "DEFINITION",
                        V.CHECK_OPTION AS "CHECK_OPTION",
                        (SELECT OPTION_VALUE FROM PG_CATALOG.PG_OPTIONS_TO_TABLE(C.RELOPTIONS) WHERE OPTION_NAME = 'security_invoker') AS "SECURITY_INVOKER"
                    FROM
                        PG_CATALOG.PG_CLASS C
                    JOIN
                        PG_CATALOG.PG_NAMESPACE N ON N.OID = C.RELNAMESPACE
                    LEFT JOIN
                        INFORMATION_SCHEMA.VIEWS V ON V.TABLE_SCHEMA = N.NSPNAME AND V.TABLE_NAME = C.RELNAME
                    WHERE
                        N.NSPNAME = ?
                    AND
                        C.RELNAME = ?
                    AND
                        C.RELKIND = 'v'
                    """;
            this.printSql(sql);
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, schema);
            stmt.setString(2, viewName);
            ResultSet resultSet = stmt.executeQuery();
            String definition = null;
            String checkOption = null;
            String securityInvoker = null;
            if (resultSet.next()) {
                definition = resultSet.getString("DEFINITION");
                checkOption = resultSet.getString("CHECK_OPTION");
                securityInvoker = resultSet.getString("SECURITY_INVOKER");
            }
            IOUtil.close(resultSet);
            IOUtil.close(stmt);
            if (StringUtil.isBlank(definition)) {
                return "";
            }
            StringBuilder builder = new StringBuilder("CREATE OR REPLACE VIEW ")
                    .append(DBUtil.wrap(schema, viewName, DBDialect.POSTGRESQL));
            if (StringUtil.isNotBlank(securityInvoker)) {
                builder.append(" WITH (security_invoker = ").append(securityInvoker).append(")");
            }
            builder.append(" AS\n").append(definition);
            // CHECK OPTION
            if (StringUtil.isNotBlank(checkOption) && !StringUtil.equalsIgnoreCase(checkOption, "NONE")) {
                builder.append("\n WITH ").append(checkOption.toUpperCase()).append(" CHECK OPTION");
            }
            return builder.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 查询建函数语句
     *
     * @param schema       模式名称
     * @param functionName 函数名称
     * @return 建函数语句
     */
    public String showCreateFunction(String schema, String functionName) {
        try {
            Connection connection = this.getConnManager().connection();
            String sql = """
                    SELECT
                        PG_CATALOG.PG_GET_FUNCTIONDEF(P.OID) AS "DEFINITION"
                    FROM
                        PG_CATALOG.PG_PROC P
                    JOIN
                        PG_CATALOG.PG_NAMESPACE N ON N.OID = P.PRONAMESPACE
                    WHERE
                        N.NSPNAME = ?
                    AND
                        P.PRONAME = ?
                    AND
                        P.PROKIND = 'f'
                    AND
                        NOT EXISTS (SELECT 1 FROM PG_CATALOG.PG_AGGREGATE A WHERE A.AGGFNOID = P.OID)
                    ORDER BY
                        P.OID
                    LIMIT 1
                    """;
            this.printSql(sql);
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, schema);
            stmt.setString(2, functionName);
            ResultSet resultSet = stmt.executeQuery();
            String createDefinition = "";
            if (resultSet.next()) {
                createDefinition = resultSet.getString("DEFINITION");
            }
            IOUtil.close(resultSet);
            IOUtil.close(stmt);
            return createDefinition;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 查询建序列语句
     *
     * @param schema       模式名称
     * @param sequenceName 序列名称
     * @return 建函数语句
     */
    public String showCreateSequence(String schema, String sequenceName) {
        try {
            Connection connection = this.getConnManager().connection();
            String sql = """
                    SELECT
                        START_VALUE,
                        MIN_VALUE,
                        MAX_VALUE,
                        INCREMENT_BY,
                        CACHE_SIZE,
                        CYCLE
                    FROM
                        PG_CATALOG.PG_SEQUENCES
                    WHERE
                        SCHEMANAME = ?
                    AND
                        SEQUENCENAME = ?
                    """;
            this.printSql(sql);
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, schema);
            stmt.setString(2, sequenceName);
            ResultSet resultSet = stmt.executeQuery();
            StringBuilder builder = new StringBuilder();
            if (resultSet.next()) {
                Long startValue = this.getLong(resultSet, "START_VALUE");
                Long minValue = this.getLong(resultSet, "MIN_VALUE");
                Long maxValue = this.getLong(resultSet, "MAX_VALUE");
                Long incrementBy = this.getLong(resultSet, "INCREMENT_BY");
                Long cacheSize = this.getLong(resultSet, "CACHE_SIZE");
                boolean cycle = resultSet.getBoolean("CYCLE");
                builder.append("CREATE SEQUENCE ")
                        .append(DBUtil.wrap(schema, sequenceName, DBDialect.POSTGRESQL));
                if (incrementBy != null) {
                    builder.append("\n    INCREMENT BY ").append(incrementBy);
                }
                if (minValue != null) {
                    builder.append("\n    MINVALUE ").append(minValue);
                }
                if (maxValue != null) {
                    builder.append("\n    MAXVALUE ").append(maxValue);
                }
                if (startValue != null) {
                    builder.append("\n    START WITH ").append(startValue);
                }
                if (cacheSize != null) {
                    builder.append("\n    CACHE ").append(cacheSize);
                }
                builder.append(cycle ? "\n    CYCLE;" : "\n    NO CYCLE;");
            }
            IOUtil.close(resultSet);
            IOUtil.close(stmt);
            return builder.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 获取可能为空的long值
     *
     * @param resultSet 结果集
     * @param column    字段名称
     * @return 结果
     * @throws SQLException 异常
     */
    private Long getLong(ResultSet resultSet, String column) throws SQLException {
        long value = resultSet.getLong(column);
        return resultSet.wasNull() ? null : value;
    }

    /**
     * 查询建过程语句
     *
     * @param schema        模式名称
     * @param procedureName 过程名称
     * @return 建过程语句
     */
    public String showCreateProcedure(String schema, String procedureName) {
        try {
            Connection connection = this.getConnManager().connection();
            String sql = """
                    SELECT
                        PG_CATALOG.PG_GET_FUNCTIONDEF(P.OID) AS "DEFINITION"
                    FROM
                        PG_CATALOG.PG_PROC P
                    JOIN
                        PG_CATALOG.PG_NAMESPACE N ON N.OID = P.PRONAMESPACE
                    WHERE
                        N.NSPNAME = ?
                    AND
                        P.PRONAME = ?
                    AND
                        P.PROKIND = 'p'
                    ORDER BY
                        P.OID
                    LIMIT 1
                    """;
            this.printSql(sql);
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, schema);
            stmt.setString(2, procedureName);
            ResultSet resultSet = stmt.executeQuery();
            String createDefinition = "";
            if (resultSet.next()) {
                createDefinition = resultSet.getString("DEFINITION");
            }
            IOUtil.close(resultSet);
            IOUtil.close(stmt);
            return createDefinition;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 查询建触发器语句
     *
     * @param schema      模式名称
     * @param triggerName 触发器名称
     * @return 建触发器语句
     */
    public String showCreateTrigger(String schema, String triggerName) {
        try {
            Connection connection = this.getConnManager().connection();
            String sql = """
                    SELECT
                        PG_CATALOG.PG_GET_TRIGGERDEF(T.OID, TRUE) AS "DEFINITION"
                    FROM
                        PG_CATALOG.PG_TRIGGER T
                    JOIN
                        PG_CATALOG.PG_CLASS C ON C.OID = T.TGRELID
                    JOIN
                        PG_CATALOG.PG_NAMESPACE N ON N.OID = C.RELNAMESPACE
                    WHERE
                        N.NSPNAME = ?
                    AND
                        T.TGNAME = ?
                    AND
                        NOT T.TGISINTERNAL
                    ORDER BY
                        T.OID
                    LIMIT 1
                    """;
            this.printSql(sql);
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, schema);
            stmt.setString(2, triggerName);
            ResultSet resultSet = stmt.executeQuery();
            String createDefinition = "";
            if (resultSet.next()) {
                createDefinition = resultSet.getString("DEFINITION");
            }
            IOUtil.close(resultSet);
            IOUtil.close(stmt);
            if (StringUtil.isNotBlank(createDefinition) && !createDefinition.trim().endsWith(";")) {
                createDefinition = createDefinition + ";";
            }
            return createDefinition;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 查询表空间列表
     *
     * @return 表空间列表
     */
    public List<String> tableSpaces() {
        try {
            List<String> engines = new ArrayList<>();
            String sql;
            //            if (this.isDbaRole()) {
            //                sql = "SELECT TABLESPACE_NAME FROM DBA_TABLESPACES WHERE STATUS = 0";
            //            } else {
            sql = "SELECT SPCNAME FROM PG_CATALOG.PG_TABLESPACE ORDER BY SPCNAME";
            //            }
            this.printSql(sql);
            Statement statement = this.getConnManager().connection().createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            DBUtil.printMetaData(resultSet);
            while (resultSet.next()) {
                engines.add(resultSet.getString(1));
            }
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            return engines;
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    /**
     * 查询模式列表
     *
     * @return 模式列表
     */
    public List<PostgresqlSchema> selectSchemas() {
        try {
            Statement statement = this.getConnManager().connection().createStatement();
            // 查询当前用户有权限访问的schema：优先显示有对象的schema，也包含当前用户自己的schema
            //            String sql = """
            //                    SELECT DISTINCT OWNER FROM ALL_OBJECTS
            //                    UNION
            //                    SELECT USERNAME FROM ALL_USERS
            //                    ORDER BY 1
            //                    """;

            List<PostgresqlSchema> list = new ArrayList<>();
            String sql;
            //            if (this.isDbaRole()) {
            //                sql = "SELECT * FROM SYSOBJECTS WHERE TYPE$ = 'SCH'";
            //            } else {
            sql = """
                    SELECT
                        NSPNAME
                    FROM
                        PG_CATALOG.PG_NAMESPACE
                    WHERE
                        NSPNAME NOT IN ('pg_catalog', 'information_schema')
                    AND
                        NSPNAME NOT LIKE 'pg_toast%'
                    AND
                        NSPNAME NOT LIKE 'pg_temp_%'
                    ORDER BY
                        NSPNAME
                    """;
            //                sql = "SELECT SF_GET_SCHEMA_NAME_BY_ID(CURRENT_SCHID);";
            //            }
            this.printSql(sql);
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                PostgresqlSchema schema = new PostgresqlSchema();
                String name = resultSet.getString(1);
                schema.setName(name);
                list.add(schema);
            }
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            DBName.sortItems(list);
            return list;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 查询模式信息
     *
     * @param schema 模式名称
     * @return 模式对象
     */
    public PostgresqlSchema schema(String schema) {
        try {
            PostgresqlSchema dbSchema = new PostgresqlSchema();
            dbSchema.setName(schema);
            return dbSchema;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 查询表信息(完整信息)
     *
     * @param schema    模式名称
     * @param tableName 表名称
     * @return 表对象
     */
    public PostgresqlTable selectTable(String schema, String tableName) {
        PostgresqlSelectTableParam param = new PostgresqlSelectTableParam();
        param.setFull(true);
        param.setSchema(schema);
        param.setTableName(tableName);
        return this.selectTable(param);
    }

    /**
     * 查询表信息(简要信息)
     *
     * @param schema    模式名称
     * @param tableName 表名称
     * @return 表对象
     */
    public PostgresqlTable selectTableSimple(String schema, String tableName) {
        PostgresqlSelectTableParam param = new PostgresqlSelectTableParam();
        param.setFull(false);
        param.setSchema(schema);
        param.setTableName(tableName);
        return this.selectTable(param);
    }

    /**
     * 查询表信息
     *
     * @param param 查询参数
     * @return 表对象
     */
    public PostgresqlTable selectTable(PostgresqlSelectTableParam param) {
        try {
            String schema = param.getSchema();
            String tableName = param.getTableName();
            PostgresqlTable table = new PostgresqlTable();
            table.setSchema(schema);
            table.setName(tableName);
            Connection connection = this.getConnManager().connection();
            String sql = """
                    SELECT
                        PG_CATALOG.OBJ_DESCRIPTION(C.OID, 'pg_class') AS "TABLE_COMMENT",
                        COALESCE(TS.SPCNAME, DTS.SPCNAME) AS "TABLE_SPACE"
                    FROM
                        PG_CATALOG.PG_CLASS C
                    JOIN
                        PG_CATALOG.PG_NAMESPACE N ON N.OID = C.RELNAMESPACE
                    LEFT JOIN
                        PG_CATALOG.PG_TABLESPACE TS ON TS.OID = C.RELTABLESPACE
                    LEFT JOIN
                        PG_CATALOG.PG_DATABASE PD ON PD.DATNAME = CURRENT_DATABASE()
                    LEFT JOIN
                        PG_CATALOG.PG_TABLESPACE DTS ON DTS.OID = PD.DATTABLESPACE
                    WHERE
                        N.NSPNAME = ?
                    AND
                        C.RELNAME = ?
                    AND
                        C.RELKIND IN ('r', 'p')
                    """;
            this.printSql(sql);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, schema);
            statement.setString(2, tableName);
            ResultSet resultSet = statement.executeQuery();
            DBUtil.printMetaData(resultSet);
            while (resultSet.next()) {
                String tableSpace = resultSet.getString("TABLE_SPACE");
                String tableComment = resultSet.getString("TABLE_COMMENT");
                table.setComment(tableComment);
                table.setTableSpace(tableSpace);
            }
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            // 完整信息才生成建表语句
            if (param.isFull()) {
                table.setCreateDefinition(this.showCreateTable(schema, tableName));
            }
            return table;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 查询视图信息(完整信息)
     *
     * @param schema   模式名称
     * @param viewName 视图名称
     * @return 视图对象
     */
    public PostgresqlView selectView(String schema, String viewName) {
        PostgresqlSelectViewParam param = new PostgresqlSelectViewParam();
        param.setFull(true);
        param.setSchema(schema);
        param.setViewName(viewName);
        return this.selectView(param);
    }

    /**
     * 查询视图信息(简要信息)
     *
     * @param schema   模式名称
     * @param viewName 视图名称
     * @return 视图对象
     */
    public PostgresqlView selectViewSimple(String schema, String viewName) {
        PostgresqlSelectViewParam param = new PostgresqlSelectViewParam();
        param.setFull(false);
        param.setSchema(schema);
        param.setViewName(viewName);
        return this.selectView(param);
    }

    /**
     * 查询视图信息
     *
     * @param param 查询参数
     * @return 视图对象
     */
    public PostgresqlView selectView(PostgresqlSelectViewParam param) {
        try {
            String schema = param.getSchema();
            String viewName = param.getViewName();
            String sql;
            sql = """
                    SELECT
                        C.RELNAME AS "VIEW_NAME",
                        CASE WHEN V.IS_UPDATABLE = 'YES' THEN 'N' ELSE 'Y' END AS "READ_ONLY",
                        PG_CATALOG.PG_GET_VIEWDEF(C.OID, TRUE) AS "DEFINITION",
                        (SELECT OPTION_VALUE FROM PG_CATALOG.PG_OPTIONS_TO_TABLE(C.RELOPTIONS) WHERE OPTION_NAME = 'security_invoker') AS "SECURITY_INVOKER",
                        PG_CATALOG.OBJ_DESCRIPTION(C.OID, 'pg_class') AS "TABLE_COMMENT"
                    FROM
                        PG_CATALOG.PG_CLASS C
                    JOIN
                        PG_CATALOG.PG_NAMESPACE N ON N.OID = C.RELNAMESPACE
                    LEFT JOIN
                        INFORMATION_SCHEMA.VIEWS V ON V.TABLE_SCHEMA = N.NSPNAME AND V.TABLE_NAME = C.RELNAME
                    WHERE
                        N.NSPNAME = ?
                    AND
                        C.RELNAME = ?
                    AND
                        C.RELKIND = 'v'
                    """;
            this.printSql(sql);
            Connection connection = this.getConnManager().connection();
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, schema);
            statement.setString(2, viewName);
            // 执行SQL查询并获取结果集
            ResultSet resultSet = statement.executeQuery();
            // 打印元数据
            DBUtil.printMetaData(resultSet);
            // 遍历结果集
            PostgresqlView view = new PostgresqlView();
            while (resultSet.next()) {
                String name = resultSet.getString("VIEW_NAME");
                String readOnly = resultSet.getString("READ_ONLY");
                String definition = resultSet.getString("DEFINITION");
                String securityInvoker = resultSet.getString("SECURITY_INVOKER");
                String tableComment = resultSet.getString("TABLE_COMMENT");
                if (readOnly == null) {
                    readOnly = ShellPostgresqlHelper.getViewReadOnly(connection, schema, viewName);
                }
                if (readOnly == null) {
                    // PostgreSQL视图默认不可更新
                    readOnly = "Y";
                }
                view.setName(name);
                view.setSchema(schema);
                view.setComment(tableComment);
                view.setDefinition(definition);
                view.setUpdatable(!StringUtil.equalsIgnoreCase("Y", readOnly));
                if (StringUtil.isBlank(securityInvoker)) {
                    view.setSecurityType("");
                } else {
                    view.setSecurityType(StringUtil.equalsIgnoreCase(securityInvoker, "true") ? "INVOKER" : "DEFINER");
                }
            }
            // 关闭连接和释放资源
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            // 完整信息才生成建视图语句
            if (param.isFull() && StringUtil.isNotBlank(view.getName())) {
                view.setCreateDefinition(this.showCreateView(schema, view.getName()));
            }
            return view;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 查询视图列表(完整信息)
     *
     * @param schema 模式名称
     * @return 视图列表
     */
    public List<PostgresqlView> selectViews(String schema) {
        PostgresqlSelectViewParam param = new PostgresqlSelectViewParam();
        param.setFull(true);
        param.setSchema(schema);
        return this.selectViews(param);
    }

    /**
     * 查询视图列表(简要信息)
     *
     * @param schema 模式名称
     * @return 视图列表
     */
    public List<PostgresqlView> selectViewsSimple(String schema) {
        PostgresqlSelectViewParam param = new PostgresqlSelectViewParam();
        param.setFull(false);
        param.setSchema(schema);
        return this.selectViews(param);
    }

    /**
     * 查询视图列表
     *
     * @param param 查询参数
     * @return 视图列表
     */
    public List<PostgresqlView> selectViews(PostgresqlSelectViewParam param) {
        try {
            String schema = param.getSchema();
            List<PostgresqlView> list = new ArrayList<>();
            String sql = """
                    SELECT
                        C.RELNAME AS "VIEW_NAME",
                        CASE WHEN V.IS_UPDATABLE = 'YES' THEN 'N' ELSE 'Y' END AS "READ_ONLY",
                        PG_CATALOG.PG_GET_VIEWDEF(C.OID, TRUE) AS "DEFINITION",
                        (SELECT OPTION_VALUE FROM PG_CATALOG.PG_OPTIONS_TO_TABLE(C.RELOPTIONS) WHERE OPTION_NAME = 'security_invoker') AS "SECURITY_INVOKER",
                        PG_CATALOG.OBJ_DESCRIPTION(C.OID, 'pg_class') AS "TABLE_COMMENT"
                    FROM
                        PG_CATALOG.PG_CLASS C
                    JOIN
                        PG_CATALOG.PG_NAMESPACE N ON N.OID = C.RELNAMESPACE
                    LEFT JOIN
                        INFORMATION_SCHEMA.VIEWS V ON V.TABLE_SCHEMA = N.NSPNAME AND V.TABLE_NAME = C.RELNAME
                    WHERE
                        N.NSPNAME = ?
                    AND
                        C.RELKIND = 'v'
                    ORDER BY
                        C.RELNAME
                    """;

            this.printSql(sql);
            Connection connection = this.getConnManager().connection();
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, schema);
            ResultSet resultSet = statement.executeQuery();
            // 打印元数据
            DBUtil.printMetaData(resultSet);
            // 遍历结果集
            while (resultSet.next()) {
                PostgresqlView view = new PostgresqlView();
                String name = resultSet.getString("VIEW_NAME");
                String readOnly = resultSet.getString("READ_ONLY");
                String definition = resultSet.getString("DEFINITION");
                String securityInvoker = resultSet.getString("SECURITY_INVOKER");
                String tableComment = resultSet.getString("TABLE_COMMENT");
                if (readOnly == null) {
                    readOnly = ShellPostgresqlHelper.getViewReadOnly(connection, schema, name);
                }
                if (readOnly == null) {
                    // PostgreSQL视图默认不可更新
                    readOnly = "Y";
                }
                view.setSchema(schema);
                view.setName(name);
                view.setComment(tableComment);
                view.setDefinition(definition);
                view.setUpdatable(!StringUtil.equalsIgnoreCase("Y", readOnly));
                if (StringUtil.isBlank(securityInvoker)) {
                    view.setSecurityType("");
                } else {
                    view.setSecurityType(StringUtil.equalsIgnoreCase(securityInvoker, "true") ? "INVOKER" : "DEFINER");
                }
                list.add(view);
            }
            // 关闭连接和释放资源
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            // 完整信息才生成建视图语句
            if (param.isFull()) {
                for (PostgresqlView view : list) {
                    try {
                        view.setCreateDefinition(this.showCreateView(schema, view.getName()));
                    } catch (Exception ex) {
                        JulLog.warn("showCreateView error, view: {}", view.getName(), ex);
                    }
                }
            }
            DBName.sortItems(list);
            return list;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 删除视图
     *
     * @param view 视图
     */
    public void dropView(PostgresqlView view) {
        if (view == null || StringUtil.isBlank(view.getSchema()) || StringUtil.isBlank(view.getName())) {
            return;
        }
        try {
            String sql = "DROP VIEW IF EXISTS " + DBUtil.wrap(view.getSchema(), view.getName(), DBDialect.POSTGRESQL) + " CASCADE";
            Statement statement = this.getConnManager().connection().createStatement();
            this.printSql(sql);
            statement.executeUpdate(sql);
            IOUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 判断视图是否存在
     *
     * @param schema   模式名称
     * @param viewName 视图名称
     * @return 存在返回true，否则返回false
     */
    public boolean existView(String schema, String viewName) {
        boolean result;
        try {
            String sql = """
                    SELECT
                        COUNT(*)
                    FROM
                        PG_CATALOG.PG_CLASS C
                    JOIN
                        PG_CATALOG.PG_NAMESPACE N ON N.OID = C.RELNAMESPACE
                    WHERE
                        N.NSPNAME = ?
                    AND
                        C.RELNAME = ?
                    AND
                        C.RELKIND = 'v'
                    """;
            PreparedStatement statement = this.getConnManager().connection().prepareStatement(sql);
            statement.setString(1, schema);
            statement.setString(2, viewName);
            ResultSet resultSet = statement.executeQuery();
            result = resultSet.next() && resultSet.getInt(1) > 0;
            IOUtil.close(resultSet);
            IOUtil.close(statement);
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
        return result;
    }

    /**
     * 创建视图
     *
     * @param param 参数
     */
    public void createView(PostgresqlCreateViewParam param) {
        Connection connection = null;
        boolean autoCommit = true;
        Statement statement = null;
        try {
            List<String> sqlList = PostgresqlViewCreateSqlGenerator.generateSql(param);
            // 无变化
            if (CollectionUtil.isEmpty(sqlList)) {
                return;
            }
            connection = this.getConnManager().connection();
            autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            statement = connection.createStatement();
            for (String sql : sqlList) {
                if (StringUtil.isBlank(sql)) {
                    continue;
                }
                this.printSql(sql);
                statement.executeUpdate(sql);
            }
            connection.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
            DBUtil.rollback(connection);
            throw new ShellException(ex);
        } finally {
            IOUtil.close(statement);
            if (connection != null) {
                try {
                    connection.setAutoCommit(autoCommit);
                } catch (SQLException ex) {
                    JulLog.warn("restore autocommit error", ex);
                }
            }
        }
    }

    /**
     * 修改视图
     *
     * @param param 参数
     */
    public void alertView(PostgresqlAlertViewParam param) {
        Connection connection = null;
        boolean autoCommit = true;
        Statement statement = null;
        try {
            List<String> sqlList = PostgresqlViewAlertSqlGenerator.generateSql(param);
            // 无变化
            if (CollectionUtil.isEmpty(sqlList)) {
                return;
            }
            connection = this.getConnManager().connection();
            autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            statement = connection.createStatement();
            for (String sql : sqlList) {
                if (StringUtil.isBlank(sql)) {
                    continue;
                }
                this.printSql(sql);
                statement.executeUpdate(sql);
            }
            connection.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
            DBUtil.rollback(connection);
            throw new ShellException(ex);
        } finally {
            IOUtil.close(statement);
            if (connection != null) {
                try {
                    connection.setAutoCommit(autoCommit);
                } catch (SQLException ex) {
                    JulLog.warn("restore autocommit error", ex);
                }
            }
        }
    }

    /**
     * 查询索引列表
     *
     * @param schema    模式名称
     * @param tableName 表名称
     * @return 索引列表
     */
    public List<PostgresqlIndex> indexes(String schema, String tableName) {
        try {
            Connection connection = this.getConnManager().connection();
            String sql = """
                    SELECT
                        IC.RELNAME AS "Key_name",
                        A.ATTNAME AS "Column_name",
                        K.ORD AS "Seq_in_index",
                        CASE WHEN IX.INDISUNIQUE THEN 0 ELSE 1 END AS "Non_unique",
                        AM.AMNAME AS "Index_type",
                        PG_CATALOG.PG_GET_INDEXDEF(IX.INDEXRELID) AS "Index_def"
                    FROM
                        PG_CATALOG.PG_INDEX IX
                    JOIN
                        PG_CATALOG.PG_CLASS TC ON TC.OID = IX.INDRELID
                    JOIN
                        PG_CATALOG.PG_NAMESPACE N ON N.OID = TC.RELNAMESPACE
                    JOIN
                        PG_CATALOG.PG_CLASS IC ON IC.OID = IX.INDEXRELID
                    JOIN
                        PG_CATALOG.PG_AM AM ON AM.OID = IC.RELAM
                    LEFT JOIN LATERAL
                        UNNEST(IX.INDKEY::SMALLINT[]) WITH ORDINALITY AS K(ATTNUM, ORD) ON TRUE
                    LEFT JOIN
                        PG_CATALOG.PG_ATTRIBUTE A ON A.ATTRELID = IX.INDRELID AND A.ATTNUM = K.ATTNUM
                    WHERE
                        N.NSPNAME = ?
                    AND
                        TC.RELNAME = ?
                    AND
                        NOT IX.INDISPRIMARY
                    AND
                        NOT EXISTS (
                            SELECT 1
                            FROM PG_CATALOG.PG_CONSTRAINT CON
                            WHERE CON.CONINDID = IX.INDEXRELID
                            AND CON.CONRELID = IX.INDRELID
                            AND CON.CONTYPE IN ('p', 'u', 'x')
                        )
                    ORDER BY
                        IC.RELNAME, K.ORD
                    """;
            this.printSql(sql);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, schema);
            statement.setString(2, tableName);
            ResultSet resultSet = statement.executeQuery();
            // 打印元数据
            DBUtil.printMetaData(resultSet);
            Map<String, PostgresqlIndex> indexMap = new HashMap<>();
            while (resultSet.next()) {
                String keyName = resultSet.getString("Key_name");
                PostgresqlIndex tableIndex = indexMap.get(keyName);
                String columnName = resultSet.getString("Column_name");
                if (tableIndex == null) {
                    int noneUnique = resultSet.getInt("Non_unique");
                    int seqInIndex = resultSet.getInt("Seq_in_index");
                    String indexType = resultSet.getString("Index_type");
                    tableIndex = new PostgresqlIndex();
                    tableIndex.setName(keyName);
                    tableIndex.setSeqIndex(seqInIndex);
                    tableIndex.setType(noneUnique == 0 ? "UNIQUE" : "NORMAL");
                    tableIndex.setMethod(indexType == null ? null : indexType.toUpperCase());
                    indexMap.put(keyName, tableIndex);
                }
                if (StringUtil.isNotBlank(columnName)) {
                    tableIndex.addColumn(columnName);
                }
            }
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            return new ArrayList<>(indexMap.values());
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    /**
     * 查询检查约束列表
     *
     * @param schema    模式名称
     * @param tableName 表名称
     * @return 检查约束列表
     */
    public List<PostgresqlCheck> selectChecks(String schema, String tableName) {
        if (!this.isSupportCheckFeature()) {
            return null;
        }
        try {
            String sql = """
                    SELECT
                        CON.CONNAME AS "NAME",
                        PG_CATALOG.PG_GET_CONSTRAINTDEF(CON.OID, TRUE) AS "CLAUSE"
                    FROM
                        PG_CATALOG.PG_CONSTRAINT CON
                    JOIN
                        PG_CATALOG.PG_CLASS C ON C.OID = CON.CONRELID
                    JOIN
                        PG_CATALOG.PG_NAMESPACE N ON N.OID = C.RELNAMESPACE
                    WHERE
                        CON.CONTYPE = 'c'
                    AND
                        CON.CONISLOCAL
                    AND
                        N.NSPNAME = ?
                    AND
                        C.RELNAME = ?
                    ORDER BY
                        CON.CONNAME
                    """;
            this.printSql(sql);
            PreparedStatement statement = this.getConnManager().connection().prepareStatement(sql);
            statement.setString(1, schema);
            statement.setString(2, tableName);
            ResultSet resultSet = statement.executeQuery();
            DBUtil.printMetaData(resultSet);
            List<PostgresqlCheck> checks = new ArrayList<>();
            while (resultSet.next()) {
                PostgresqlCheck check = new PostgresqlCheck();
                String name = resultSet.getString("NAME");
                String clause = resultSet.getString("CLAUSE");
                // 去掉pg_get_constraintdef返回的CHECK包装
                if (StringUtil.startWithIgnoreCase(clause, "CHECK ")) {
                    clause = clause.substring("CHECK ".length()).trim();
                }
                if (StringUtil.endsWithIgnoreCase(clause, " NOT VALID")) {
                    clause = clause.substring(0, clause.length() - " NOT VALID".length()).trim();
                }
                check.setName(name);
                check.setClause(clause);
                check.setSchema(schema);
                check.setTableName(tableName);
                checks.add(check);
            }
            IOUtil.close(resultSet);
            DBName.sortItems(checks);
            return checks;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 查询有外键的表
     *
     * @param schema 模式
     * @return 结果
     */
    public List<String> selectForeignKeyTables(String schema) {
        try {
            String sql = """
                    SELECT DISTINCT
                        TC.RELNAME AS "FKTABLE_NAME"
                    FROM
                        PG_CATALOG.PG_CONSTRAINT CON
                    JOIN
                        PG_CATALOG.PG_CLASS TC ON TC.OID = CON.CONRELID
                    JOIN
                        PG_CATALOG.PG_NAMESPACE N ON N.OID = TC.RELNAMESPACE
                    WHERE
                        CON.CONTYPE = 'f'
                    AND
                        N.NSPNAME = ?
                    ORDER BY
                        TC.RELNAME
                    """;
            this.printSql(sql);
            PreparedStatement statement = this.getConnManager().connection().prepareStatement(sql);
            statement.setString(1, schema);
            ResultSet resultSet = statement.executeQuery();
            DBUtil.printMetaData(resultSet);
            List<String> tables = new ArrayList<>();
            while (resultSet.next()) {
                String fkTableName = resultSet.getString("FKTABLE_NAME");
                tables.add(fkTableName);
            }
            IOUtil.close(resultSet);
            return tables;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 查询外键列表
     *
     * @param schema    模式名称
     * @param tableName 表名称
     * @return 外键列表
     */
    public List<PostgresqlForeignKey> selectForeignKeys(String schema, String tableName) {
        try {
            String sql = """
                    SELECT
                        CON.CONNAME AS "FK_NAME",
                        RT.RELNAME AS "PKTABLE_NAME",
                        RN.NSPNAME AS "PKTABLE_CAT",
                        A.ATTNAME AS "FKCOLUMN_NAME",
                        RA.ATTNAME AS "PKCOLUMN_NAME",
                        CON.CONFDELTYPE AS "DELETE_RULE",
                        CON.CONFUPDTYPE AS "UPDATE_RULE"
                    FROM
                        PG_CATALOG.PG_CONSTRAINT CON
                    JOIN
                        PG_CATALOG.PG_CLASS TC ON TC.OID = CON.CONRELID
                    JOIN
                        PG_CATALOG.PG_NAMESPACE TN ON TN.OID = TC.RELNAMESPACE
                    JOIN
                        PG_CATALOG.PG_CLASS RT ON RT.OID = CON.CONFRELID
                    JOIN
                        PG_CATALOG.PG_NAMESPACE RN ON RN.OID = RT.RELNAMESPACE
                    JOIN LATERAL
                        UNNEST(CON.CONKEY::SMALLINT[]) WITH ORDINALITY AS K(ATTNUM, ORD) ON TRUE
                    JOIN LATERAL
                        UNNEST(CON.CONFKEY::SMALLINT[]) WITH ORDINALITY AS RK(ATTNUM, ORD) ON RK.ORD = K.ORD
                    JOIN
                        PG_CATALOG.PG_ATTRIBUTE A ON A.ATTRELID = CON.CONRELID AND A.ATTNUM = K.ATTNUM
                    JOIN
                        PG_CATALOG.PG_ATTRIBUTE RA ON RA.ATTRELID = CON.CONFRELID AND RA.ATTNUM = RK.ATTNUM
                    WHERE
                        CON.CONTYPE = 'f'
                    AND
                        TN.NSPNAME = ?
                    AND
                        TC.RELNAME = ?
                    ORDER BY
                        CON.CONNAME, K.ORD
                    """;
            this.printSql(sql);
            PreparedStatement statement = this.getConnManager().connection().prepareStatement(sql);
            statement.setString(1, schema);
            statement.setString(2, tableName);
            ResultSet resultSet = statement.executeQuery();
            DBUtil.printMetaData(resultSet);
            Map<String, PostgresqlForeignKey> foreignKeyMap = new HashMap<>();
            while (resultSet.next()) {
                String fkName = resultSet.getString("FK_NAME");
                String fkColumnName = resultSet.getString("FKCOLUMN_NAME");
                String pkColumnName = resultSet.getString("PKCOLUMN_NAME");
                PostgresqlForeignKey foreignKey = foreignKeyMap.get(fkName);
                if (foreignKey == null) {
                    String pkTableName = resultSet.getString("PKTABLE_NAME");
                    String pkTableCat = resultSet.getString("PKTABLE_CAT");
                    String updateRule = this.foreignKeyPolicy(resultSet.getString("UPDATE_RULE"));
                    String deleteRule = this.foreignKeyPolicy(resultSet.getString("DELETE_RULE"));
                    foreignKey = new PostgresqlForeignKey();
                    foreignKey.setName(fkName);
                    foreignKey.setUpdatePolicy(updateRule);
                    foreignKey.setDeletePolicy(deleteRule);
                    foreignKey.setReferencedTable(pkTableName);
                    foreignKey.setReferencedDatabase(pkTableCat);
                    foreignKeyMap.put(fkName, foreignKey);
                }
                foreignKey.addColumn(fkColumnName);
                foreignKey.addReferencedColumn(pkColumnName);
            }
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            return new ArrayList<>(foreignKeyMap.values());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 解析外键策略
     *
     * @param code 策略标识
     * @return 策略
     */
    private String foreignKeyPolicy(String code) {
        if (code == null) {
            return null;
        }
        return switch (code.toLowerCase()) {
            case "a" -> "NO ACTION";
            case "r" -> "RESTRICT";
            case "c" -> "CASCADE";
            case "n" -> "SET NULL";
            case "d" -> "SET DEFAULT";
            default -> code.toUpperCase();
        };
    }

    /**
     * 查询视图字段列表
     *
     * @param schema   模式名称
     * @param viewName 视图名称
     * @return 字段列表
     */
    public List<PostgresqlColumn> viewColumns(String schema, String viewName) {
        try {
            PostgresqlColumns columns = new PostgresqlColumns();
            String sql = """
                    SELECT
                        A.ATTNAME AS "Field",
                        PG_CATALOG.FORMAT_TYPE(A.ATTTYPID, A.ATTTYPMOD) AS "Type",
                        PG_CATALOG.COL_DESCRIPTION(A.ATTRELID, A.ATTNUM) AS "Comment",
                        A.ATTNUM AS "POSITION",
                        PG_CATALOG.PG_GET_EXPR(AD.ADBIN, AD.ADRELID) AS "Default",
                        A.ATTNOTNULL AS "NotNull",
                        CASE WHEN EXISTS (
                            SELECT 1
                            FROM PG_CATALOG.PG_INDEX I
                            WHERE I.INDRELID = A.ATTRELID
                            AND I.INDISPRIMARY
                            AND A.ATTNUM = ANY(I.INDKEY::SMALLINT[])
                        ) THEN 'PRI' ELSE '' END AS "Key",
                        A.ATTIDENTITY AS "IDENTITY"
                    FROM
                        PG_CATALOG.PG_ATTRIBUTE A
                    JOIN
                        PG_CATALOG.PG_CLASS C ON C.OID = A.ATTRELID
                    JOIN
                        PG_CATALOG.PG_NAMESPACE N ON N.OID = C.RELNAMESPACE
                    LEFT JOIN
                        PG_CATALOG.PG_ATTRDEF AD ON AD.ADRELID = A.ATTRELID AND AD.ADNUM = A.ATTNUM
                    WHERE
                        N.NSPNAME = ?
                    AND
                        C.RELNAME = ?
                    AND
                        A.ATTNUM > 0
                    AND
                        NOT A.ATTISDROPPED
                    ORDER BY
                        A.ATTNUM
                    """;
            Connection connection = this.getConnManager().connection();
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, schema);
            statement.setString(2, viewName);
            ResultSet resultSet = statement.executeQuery();
            DBUtil.printMetaData(resultSet);
            while (resultSet.next()) {
                String key = resultSet.getString("Key");
                String type = resultSet.getString("Type");
                String field = resultSet.getString("Field");
                Object def = resultSet.getObject("Default");
                int position = resultSet.getInt("POSITION");
                boolean notNull = resultSet.getBoolean("NotNull");
                String comment = resultSet.getString("Comment");
                String identity = resultSet.getString("IDENTITY");
                boolean autoIncrement = StringUtil.isNotBlank(identity)
                        || StringUtil.startWithIgnoreCase(def == null ? null : def.toString().trim(), "nextval(");
                PostgresqlColumn column = new PostgresqlColumn();
                column.parseKey(key);
                column.setName(field);
                this.parseColumnType(column, type);
                column.setSchema(schema);
                column.setComment(comment);
                column.setDefaultValue(def);
                column.setPosition(position);
                column.setTableName(viewName);
                column.setNullable(!notNull);
                column.setAutoIncrement(autoIncrement);
                columns.add(column);
            }
            IOUtil.close(resultSet);
            // 初始化自动递增
            this.initAutoIncrement(connection, schema, viewName, columns, true);
            // 返回排序后的数据
            return new ArrayList<>(columns.sortOfPosition());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 查询视图记录列表
     *
     * @param schema   模式名称
     * @param viewName 视图名称
     * @param start    起始位置
     * @param limit    查询条数
     * @param filters  过滤条件
     * @return 记录列表
     */
    public List<PostgresqlRecord> viewRecords(String schema, String viewName, Long start, Long limit, List<PostgresqlRecordFilter> filters) {
        try {
            Connection connection = this.getConnManager().connection();
            StringBuilder builder = new StringBuilder("SELECT * FROM ");
            builder.append(DBUtil.wrap(schema, viewName, DBDialect.POSTGRESQL));
            String filterCondition = DBUtil.buildCondition(filters);
            if (StringUtil.isNotBlank(filterCondition)) {
                builder.append(" WHERE ").append(filterCondition);
            }
            if (start != null && limit != null) {
                builder.append(" LIMIT ").append(limit).append(" OFFSET ").append(start);
            }
            String sql = builder.toString();
            this.printSql(sql);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            DBUtil.printMetaData(resultSet);
            List<PostgresqlRecord> records = new ArrayList<>();
            boolean updatable = ShellPostgresqlHelper.isViewUpdatable(connection, schema, viewName);
            PostgresqlColumns columns = ShellPostgresqlHelper.parseColumns(resultSet);
            for (PostgresqlColumn column : columns) {
                column.setTableName(viewName);
            }
            while (resultSet.next()) {
                PostgresqlRecord record = new PostgresqlRecord(columns, !updatable);
                for (PostgresqlColumn column : columns) {
                    Object data = resultSet.getObject(column.getName());
                    //                    // 获取几何值
                    //                    if (column.supportGeometry()) {
                    //                        data = PostgresqlHelper.getGeometryString(connection, data);
                    //                    }
                    record.putValue(column, data);
                }
                records.add(record);
            }
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            return records;
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    /**
     * 创建表
     *
     * @param param 创建参数
     */
    public void createTable(PostgresqlCreateTableParam param) {
        Connection connection = null;
        boolean autoCommit = true;
        Statement statement = null;
        try {
            connection = this.getConnManager().connection();
            autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            statement = connection.createStatement();
            List<String> sqlList = PostgresqlTableCreateSqlGenerator.generateSql(param);
            for (String sqlStr : sqlList) {
                this.printSql(sqlStr);
                statement.executeUpdate(sqlStr);
            }
            connection.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
            DBUtil.rollback(connection);
            throw new ShellException(ex);
        } finally {
            IOUtil.close(statement);
            if (connection != null) {
                try {
                    connection.setAutoCommit(autoCommit);
                } catch (SQLException ex) {
                    JulLog.warn("restore autocommit error", ex);
                }
            }
        }
    }

    /**
     * 修改表
     *
     * @param param 修改参数
     */
    public void alertTable(PostgresqlAlertTableParam param) {
        Connection connection = null;
        boolean autoCommit = true;
        Statement statement = null;
        try {
            List<String> sqlList = PostgresqlTableAlertSqlGenerator.generateSql(param);
            // 无变化
            if (CollectionUtil.isEmpty(sqlList)) {
                return;
            }
            connection = this.getConnManager().connection();
            autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            statement = connection.createStatement();
            for (String sql : sqlList) {
                if (StringUtil.isBlank(sql)) {
                    continue;
                }
                this.printSql(sql);
                statement.executeUpdate(sql);
            }
            connection.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
            DBUtil.rollback(connection);
            throw new ShellException(ex);
        } finally {
            IOUtil.close(statement);
            if (connection != null) {
                try {
                    connection.setAutoCommit(autoCommit);
                } catch (SQLException ex) {
                    JulLog.warn("restore autocommit error", ex);
                }
            }
        }
    }

    //    @Deprecated
    //    public boolean existTable(String schema, String tableName) {
    //        boolean result;
    //        try {
    //            String sql = "SELECT COUNT(*) FROM ALL_TABLES WHERE OWNER = ? AND TABLE_NAME = ?";
    //            PreparedStatement statement = this.getConnManager().connection(schema).prepareStatement(sql);
    //            statement.setString(1, schema);
    //            statement.setString(2, tableName);
    //            ResultSet resultSet = statement.executeQuery();
    //            DBUtil.printMetaData(resultSet);
    //            result = resultSet.next() && resultSet.getInt(1) > 0;
    //            IOUtil.close(resultSet);
    //            IOUtil.close(statement);
    //        } catch (Exception ex) {
    //            throw new ShellException(ex);
    //        }
    //        return result;
    //    }

    /**
     * 重命名表
     *
     * @param schema       模式名称
     * @param oldTableName 原表名称
     * @param newTableName 新表名称
     */
    public void renameTable(String schema, String oldTableName, String newTableName) {
        try {
            String sql = "ALTER TABLE " + DBUtil.wrap(schema, oldTableName, DBDialect.POSTGRESQL)
                    + " RENAME TO " + DBUtil.wrap(newTableName, DBDialect.POSTGRESQL);
            Connection connection = this.getConnManager().connection();
            Statement statement = connection.createStatement();
            this.printSql(sql);
            statement.execute(sql);
            IOUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 重命名视图
     *
     * @param schema      模式名称
     * @param oldViewName 原视图名称
     * @param newViewName 新视图名称
     */
    public void renameView(String schema, String oldViewName, String newViewName) {
        try {
            String sql = "ALTER VIEW " + DBUtil.wrap(schema, oldViewName, DBDialect.POSTGRESQL)
                    + " RENAME TO " + DBUtil.wrap(newViewName, DBDialect.POSTGRESQL);
            Connection connection = this.getConnManager().connection();
            Statement statement = connection.createStatement();
            this.printSql(sql);
            statement.execute(sql);
            IOUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 重命名函数
     *
     * @param schema          库名称
     * @param oldFunctionName 函数名称
     * @param newFunctionName 新函数名称
     */
    public void renameFunction(String schema, String oldFunctionName, String newFunctionName) {
        try {
            String arguments = this.functionIdentityArguments(schema, oldFunctionName, "f");
            String sql = "ALTER FUNCTION " + DBUtil.wrap(schema, oldFunctionName, DBDialect.POSTGRESQL);
            if (arguments != null) {
                sql += "(" + arguments + ")";
            }
            sql += " RENAME TO " + DBUtil.wrap(newFunctionName, DBDialect.POSTGRESQL);
            Connection connection = this.getConnManager().connection();
            Statement statement = connection.createStatement();
            this.printSql(sql);
            statement.execute(sql);
            IOUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 查询函数/过程的参数标识
     *
     * @param schema 模式名称
     * @param name   名称
     * @param kind   f:函数，p:过程
     * @return 参数标识
     * @throws Exception 异常
     */
    private String functionIdentityArguments(String schema, String name, String kind) throws Exception {
        Connection connection = this.getConnManager().connection();
        String sql = """
                SELECT
                    PG_CATALOG.PG_GET_FUNCTION_IDENTITY_ARGUMENTS(P.OID) AS "ARGUMENTS"
                FROM
                    PG_CATALOG.PG_PROC P
                JOIN
                    PG_CATALOG.PG_NAMESPACE N ON N.OID = P.PRONAMESPACE
                WHERE
                    N.NSPNAME = ?
                AND
                    P.PRONAME = ?
                AND
                    P.PROKIND = ?
                ORDER BY
                    P.OID
                LIMIT 1
                """;
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, schema);
        statement.setString(2, name);
        statement.setString(3, kind);
        ResultSet resultSet = statement.executeQuery();
        String arguments = null;
        if (resultSet.next()) {
            arguments = resultSet.getString("ARGUMENTS");
        }
        IOUtil.close(resultSet);
        IOUtil.close(statement);
        return arguments;
    }

    /**
     * 重命名过程
     *
     * @param schema           库名称
     * @param oldProcedureName 过程名称
     * @param newProcedureName 新过程名称
     */
    public void renameProcedure(String schema, String oldProcedureName, String newProcedureName) {
        try {
            String arguments = this.functionIdentityArguments(schema, oldProcedureName, "p");
            String sql = "ALTER PROCEDURE " + DBUtil.wrap(schema, oldProcedureName, DBDialect.POSTGRESQL);
            if (arguments != null) {
                sql += "(" + arguments + ")";
            }
            sql += " RENAME TO " + DBUtil.wrap(newProcedureName, DBDialect.POSTGRESQL);
            Connection connection = this.getConnManager().connection();
            Statement statement = connection.createStatement();
            this.printSql(sql);
            statement.execute(sql);
            IOUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 清空表数据
     *
     * @param schema    模式名称
     * @param tableName 表名称
     */
    public void clearTable(String schema, String tableName) {
        try {
            Statement statement = this.getConnManager().connection().createStatement();
            String sql = "DELETE FROM " + DBUtil.wrap(schema, tableName, DBDialect.POSTGRESQL);
            this.printSql(sql);
            statement.executeUpdate(sql);
            IOUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 截断表
     *
     * @param schema    模式名称
     * @param tableName 表名称
     */
    public void truncateTable(String schema, String tableName) {
        try {
            Statement statement = this.getConnManager().connection().createStatement();
            String sql = "TRUNCATE TABLE " + DBUtil.wrap(schema, tableName, DBDialect.POSTGRESQL) + " RESTART IDENTITY";
            this.printSql(sql);
            statement.executeUpdate(sql);
            IOUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 删除表
     *
     * @param schema    模式名称
     * @param tableName 表名称
     */
    public void dropTable(String schema, String tableName) {
        try {
            Statement statement = this.getConnManager().connection().createStatement();
            String sql = "DROP TABLE IF EXISTS " + DBUtil.wrap(schema, tableName, DBDialect.POSTGRESQL) + " CASCADE";
            this.printSql(sql);
            statement.executeUpdate(sql);
            IOUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 判断模式是否存在
     *
     * @param schema 模式名称
     * @return 存在返回true，否则返回false
     */
    public boolean existSchema(String schema) {
        boolean result = false;
        try {
            String sql;
            //            if (this.isDbaRole()) {
            //                sql = "SELECT COUNT(*) FROM SYSOBJECTS WHERE TYPE$ = 'SCH' AND NAME = ?";
            //            } else {
            sql = """
                    SELECT
                        COUNT(*)
                    FROM
                        PG_CATALOG.PG_NAMESPACE
                    WHERE
                        NSPNAME = ?
                    """;
            //            }
            this.printSql(sql);
            PreparedStatement statement = this.getConnManager().connection().prepareStatement(sql);
            statement.setString(1, schema);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                result = resultSet.getInt(1) > 0;
            }
            IOUtil.close(resultSet);
            IOUtil.close(statement);
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
        return result;
    }

    /**
     * 创建模式
     *
     * @param schema 模式
     */
    public void createSchema(PostgresqlSchema schema) {
        try {
            StringBuilder builder = new StringBuilder("CREATE SCHEMA ");
            builder.append(DBUtil.wrap(schema.getName(), DBDialect.POSTGRESQL));
            String sql = builder.toString();
            this.printSql(sql);
            Statement statement = this.getConnManager().connection().createStatement();
            statement.execute(sql);
            IOUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 修改模式
     *
     * @param schema 模式
     * @return 修改成功返回true，否则返回false
     */
    public boolean alterSchema(PostgresqlSchema schema) {
        try {
            return true;
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    /**
     * 删除模式
     *
     * @param schema 模式名称
     * @return 删除成功返回true，否则返回false
     */
    public boolean dropSchema(String schema) {
        try {
            String sql = "DROP SCHEMA " + DBUtil.wrap(schema, DBDialect.POSTGRESQL) + " CASCADE";
            this.printSql(sql);
            Statement statement = this.getConnManager().connection().createStatement();
            statement.executeUpdate(sql);
            IOUtil.close(statement);
            return true;
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    /**
     * 解释SQL
     *
     * @param schema 模式名称
     * @param sql    SQL语句
     * @return 执行计划结果
     */
    public DBQueryResults<PostgresqlExplainResult> explainSql(String schema, String sql) {
        DBQueryResults<PostgresqlExplainResult> results = new DBQueryResults<>();
        Connection connection = null;
        try {
            this.printSql(sql);
            DBSqlParser parser = DBSqlParser.getParser(sql, DBDialect.POSTGRESQL);
            List<String> list = parser.parseSql();
            connection = this.getConnManager().connection(schema);
            Statement statement = connection.createStatement();
            for (String execSql : list) {
                PostgresqlExplainResult result = new PostgresqlExplainResult();
                try {
                    execSql = "EXPLAIN (FORMAT TEXT) " + execSql.stripLeading();
                    result.setContent(execSql);
                    long startTime = System.nanoTime();
                    ResultSet resultSet = statement.executeQuery(execSql);
                    result.parseResult(resultSet, connection);
                    result.fillSchema(schema);
                    result.setUsed(System.nanoTime() - startTime);
                    IOUtil.close(resultSet);
                    result.setSuccess(true);
                } catch (SQLException ex) {
                    result.setMsg(ex.toString());
                }
                results.addResult(result);
            }
            IOUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            DBUtil.rollback(connection);
            results.parseError(ex);
        }
        return results;
    }

    /**
     * 执行单条SQL
     *
     * @param schema 模式名称
     * @param sql    SQL语句
     * @return 执行结果
     */
    public PostgresqlExecuteResult executeSingleSql(String schema, String sql) {
        Connection connection = null;
        boolean autoCommit = true;
        Statement statement = null;
        PostgresqlExecuteResult result = new PostgresqlExecuteResult();
        result.setContent(sql);
        try {
            this.printSql(sql);
            DBSqlParser parser = DBSqlParser.getParser(sql, DBDialect.POSTGRESQL);
            String execSql = parser.parseSingleSql();
            connection = this.getConnManager().connection(schema);
            autoCommit = connection.getAutoCommit();
            // 单条语句按自动提交语义执行，兼容 CREATE DATABASE / VACUUM 等不能运行在事务块中的语句
            connection.setAutoCommit(true);
            statement = connection.createStatement();
            long startTime = System.nanoTime();
            boolean isQuery = statement.execute(execSql);
            if (isQuery) {
                ResultSet resultSet = statement.getResultSet();
                result.setFullColumn(parser.isFullColumn(sql));
                result.parseResult(resultSet, connection, !parser.isSelect(sql));
                result.fillSchema(schema);
                IOUtil.close(resultSet);
                result.setSuccess(true);
            } else {
                int updateCount = statement.getUpdateCount();
                result.setUpdateCount(updateCount);
                result.setSuccess(true);
            }
            long endTime = System.nanoTime();
            result.setUsed(endTime - startTime);
        } catch (Exception ex) {
            ex.printStackTrace();
            result.setMsg(ex.getMessage());
        } finally {
            IOUtil.close(statement);
            if (connection != null) {
                try {
                    connection.setAutoCommit(autoCommit);
                } catch (SQLException ex) {
                    JulLog.warn("restore autocommit error", ex);
                }
            }
        }
        return result;
    }

    /**
     * 简单执行SQL
     *
     * @param schema 模式名称
     * @param sql    SQL语句
     */
    public void executeSqlSimple(String schema, String sql) {
        Connection connection = null;
        boolean autoCommit = true;
        Statement statement = null;
        try {
            this.printSql(sql);
            connection = this.getConnManager().connection(schema);
            autoCommit = connection.getAutoCommit();
            // 自动提交，兼容 CREATE DATABASE / VACUUM 等不能运行在事务块中的语句
            connection.setAutoCommit(true);
            statement = connection.createStatement();
            statement.execute(sql);
        } catch (Exception ex) {
            JulLog.warn("sql:\n{}", sql);
            ex.printStackTrace();
            throw new ShellException(ex);
        } finally {
            IOUtil.close(statement);
            if (connection != null) {
                try {
                    connection.setAutoCommit(autoCommit);
                } catch (SQLException ex) {
                    JulLog.warn("restore autocommit error", ex);
                }
            }
        }
    }

    /**
     * 批量插入
     *
     * @param schema   模式名称
     * @param sqlList  SQL列表
     * @param parallel 是否使用独立连接并行执行
     * @return 受影响的行数
     */
    public int insertBatch(String schema, List<String> sqlList, boolean parallel) {
        Connection connection = null;
        boolean autoCommit = true;
        Statement statement = null;
        int result = 0;
        try {
            connection = parallel ? this.getConnManager().newConnection(null) : this.getConnManager().connection();
            autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            statement = connection.createStatement();
            for (String sql : sqlList) {
                this.printSql(sql);
                statement.addBatch(sql);
            }
            int[] results = statement.executeBatch();
            connection.commit();
            for (int i : results) {
                result += i;
            }
        } catch (Exception ex) {
            JulLog.warn("sqlList:{}", sqlList);
            ex.printStackTrace();
            DBUtil.rollback(connection);
            throw new ShellException(ex);
        } finally {
            IOUtil.close(statement);
            if (connection != null) {
                if (parallel) {
                    IOUtil.close(connection);
                } else {
                    try {
                        connection.setAutoCommit(autoCommit);
                    } catch (SQLException ex) {
                        JulLog.warn("restore autocommit error", ex);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public DBDialect dialect() {
        return DBDialect.POSTGRESQL;
    }

    /**
     * 查询函数列表
     *
     * @param schema 模式名称
     * @return 函数列表
     */
    public List<PostgresqlFunction> selectFunctions(String schema) {
        PostgresqlSelectFunctionParam param = new PostgresqlSelectFunctionParam();
        param.setFull(true);
        param.setSchema(schema);
        return this.selectFunctions(param);
    }

    /**
     * 查询函数列表(简要信息)
     *
     * @param schema 模式名称
     * @return 函数列表
     */
    public List<PostgresqlFunction> selectFunctionsSimple(String schema) {
        PostgresqlSelectFunctionParam param = new PostgresqlSelectFunctionParam();
        param.setFull(false);
        param.setSchema(schema);
        return this.selectFunctions(param);
    }

    /**
     * 查询函数列表
     *
     * @param param 查询参数
     * @return 函数列表
     */
    public List<PostgresqlFunction> selectFunctions(PostgresqlSelectFunctionParam param) {
        try {
            String schema = param.getSchema();
            List<PostgresqlFunction> list = new ArrayList<>();
            String sql = """
                    SELECT
                        P.OID AS "OID",
                        P.PRONAME AS "ROUTINE_NAME",
                        P.PROSECDef AS "SECURITY_DEFINER",
                        P.PROVOLATILE AS "VOLATILE",
                        P.PROISSTRICT AS "STRICT",
                        P.PROSRC AS "DEFINITION",
                        PG_CATALOG.PG_GET_FUNCTIONDEF(P.OID) AS "DLL"
                    FROM
                        PG_CATALOG.PG_PROC P
                    JOIN
                        PG_CATALOG.PG_NAMESPACE N ON N.OID = P.PRONAMESPACE
                    WHERE
                        N.NSPNAME = ?
                    AND
                        P.PROKIND = 'f'
                    AND
                        P.PRORETTYPE <> 'trigger'::REGTYPE
                    AND
                        NOT EXISTS (SELECT 1 FROM PG_CATALOG.PG_AGGREGATE A WHERE A.AGGFNOID = P.OID)
                    ORDER BY
                        P.PRONAME, P.OID
                    """;
            this.printSql(sql);
            PreparedStatement statement = this.getConnManager().connection().prepareStatement(sql);
            statement.setString(1, schema);
            // 执行SQL查询并获取结果集
            ResultSet resultSet = statement.executeQuery();
            // 打印元数据
            DBUtil.printMetaData(resultSet);
            // 遍历结果集
            while (resultSet.next()) {
                PostgresqlFunction function = new PostgresqlFunction();
                long oid = resultSet.getLong("OID");
                String name = resultSet.getString("ROUTINE_NAME");
                String createDefinition = resultSet.getString("DLL");
                String definition = resultSet.getString("DEFINITION");
                boolean securityDefiner = resultSet.getBoolean("SECURITY_DEFINER");
                boolean strict = resultSet.getBoolean("STRICT");
                String volatility = resultSet.getString("VOLATILE");
                String securityType = securityDefiner ? "DEFINER" : "INVOKER";
                StringBuilder characteristic = new StringBuilder();
                if (param.isFull()) {
                    List<PostgresqlRoutineParam> params = this.listFunctionParam(oid);
                    function.setParams(params);
                }
                if (StringUtil.isBlank(definition)) {
                    definition = ShellPostgresqlHelper.parseFunctionBody(createDefinition);
                }
                if (strict) {
                    characteristic.append(",STRICT");
                }
                if (StringUtil.equalsIgnoreCase("i", volatility)) {
                    characteristic.append(",IMMUTABLE");
                } else if (StringUtil.equalsIgnoreCase("s", volatility)) {
                    characteristic.append(",STABLE");
                }
                if (!characteristic.isEmpty()) {
                    function.setCharacteristic(characteristic.substring(1));
                }
                function.setName(name);
                function.setSchema(schema);
                function.setDefinition(definition);
                function.setSecurityType(securityType);
                function.setCreateDefinition(createDefinition);
                list.add(function);
            }
            // 关闭连接和释放资源
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            DBName.sortItems(list);
            return list;
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    /**
     * 查询存储过程列表
     *
     * @param schema 模式名称
     * @return 存储过程列表
     */
    public List<PostgresqlProcedure> selectProcedures(String schema) {
        PostgresqlSelectProcedureParam param = new PostgresqlSelectProcedureParam();
        param.setFull(true);
        param.setSchema(schema);
        return this.selectProcedures(param);
    }

    /**
     * 查询存储过程列表(简要信息)
     *
     * @param schema 模式名称
     * @return 存储过程列表
     */
    public List<PostgresqlProcedure> selectProceduresSimple(String schema) {
        PostgresqlSelectProcedureParam param = new PostgresqlSelectProcedureParam();
        param.setFull(false);
        param.setSchema(schema);
        return this.selectProcedures(param);
    }

    /**
     * 查询存储过程列表
     *
     * @param param 查询参数
     * @return 存储过程列表
     */
    public List<PostgresqlProcedure> selectProcedures(PostgresqlSelectProcedureParam param) {
        try {
            String schema = param.getSchema();
            List<PostgresqlProcedure> list = new ArrayList<>();
            String sql = """
                    SELECT
                        P.OID AS "OID",
                        P.PRONAME AS "ROUTINE_NAME",
                        P.PROSECDef AS "SECURITY_DEFINER",
                        P.PROVOLATILE AS "VOLATILE",
                        P.PROISSTRICT AS "STRICT",
                        P.PROSRC AS "DEFINITION",
                        PG_CATALOG.PG_GET_FUNCTIONDEF(P.OID) AS "DLL"
                    FROM
                        PG_CATALOG.PG_PROC P
                    JOIN
                        PG_CATALOG.PG_NAMESPACE N ON N.OID = P.PRONAMESPACE
                    WHERE
                        N.NSPNAME = ?
                    AND
                        P.PROKIND = 'p'
                    ORDER BY
                        P.PRONAME, P.OID
                    """;
            this.printSql(sql);
            PreparedStatement statement = this.getConnManager().connection().prepareStatement(sql);
            statement.setString(1, schema);
            // 执行SQL查询并获取结果集
            ResultSet resultSet = statement.executeQuery();
            // 打印元数据
            DBUtil.printMetaData(resultSet);
            // 遍历结果集
            while (resultSet.next()) {
                PostgresqlProcedure procedure = new PostgresqlProcedure();
                long oid = resultSet.getLong("OID");
                String name = resultSet.getString("ROUTINE_NAME");
                String createDefinition = resultSet.getString("DLL");
                String definition = resultSet.getString("DEFINITION");
                boolean securityDefiner = resultSet.getBoolean("SECURITY_DEFINER");
                boolean strict = resultSet.getBoolean("STRICT");
                String volatility = resultSet.getString("VOLATILE");
                String securityType = securityDefiner ? "DEFINER" : "INVOKER";
                StringBuilder characteristic = new StringBuilder();
                if (param.isFull()) {
                    List<PostgresqlRoutineParam> params = this.listProcedureParam(oid);
                    procedure.setParams(params);
                }
                if (StringUtil.isBlank(definition)) {
                    definition = ShellPostgresqlHelper.parseFunctionBody(createDefinition);
                }
                if (strict) {
                    characteristic.append(",STRICT");
                }
                if (StringUtil.equalsIgnoreCase("i", volatility)) {
                    characteristic.append(",IMMUTABLE");
                } else if (StringUtil.equalsIgnoreCase("s", volatility)) {
                    characteristic.append(",STABLE");
                }
                if (!characteristic.isEmpty()) {
                    procedure.setCharacteristic(characteristic.substring(1));
                }
                procedure.setName(name);
                procedure.setSchema(schema);
                procedure.setDefinition(definition);
                procedure.setSecurityType(securityType);
                procedure.setCreateDefinition(createDefinition);
                list.add(procedure);
            }
            // 关闭连接和释放资源
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            DBName.sortItems(list);
            return list;
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    /**
     * 查询存储过程信息(完整信息)
     *
     * @param schema        模式名称
     * @param procedureName 过程名称
     * @return 存储过程对象
     */
    public PostgresqlProcedure selectProcedure(String schema, String procedureName) {
        PostgresqlSelectProcedureParam param = new PostgresqlSelectProcedureParam();
        param.setFull(true);
        param.setSchema(schema);
        param.setProcedureName(procedureName);
        return this.selectProcedure(param);
    }

    /**
     * 查询存储过程信息(简要信息)
     *
     * @param schema        模式名称
     * @param procedureName 过程名称
     * @return 存储过程对象
     */
    public PostgresqlProcedure selectProcedureSimple(String schema, String procedureName) {
        PostgresqlSelectProcedureParam param = new PostgresqlSelectProcedureParam();
        param.setFull(false);
        param.setSchema(schema);
        param.setProcedureName(procedureName);
        return this.selectProcedure(param);
    }

    /**
     * 查询存储过程信息
     *
     * @param param 查询参数
     * @return 存储过程对象
     */
    public PostgresqlProcedure selectProcedure(PostgresqlSelectProcedureParam param) {
        try {
            String schema = param.getSchema();
            String produceName = param.getProcedureName();
            String sql = """
                    SELECT
                        P.OID AS "OID",
                        P.PROSECDef AS "SECURITY_DEFINER",
                        P.PROVOLATILE AS "VOLATILE",
                        P.PROISSTRICT AS "STRICT",
                        P.PROSRC AS "DEFINITION",
                        PG_CATALOG.PG_GET_FUNCTIONDEF(P.OID) AS "DLL"
                    FROM
                        PG_CATALOG.PG_PROC P
                    JOIN
                        PG_CATALOG.PG_NAMESPACE N ON N.OID = P.PRONAMESPACE
                    WHERE
                        N.NSPNAME = ?
                    AND
                        P.PRONAME = ?
                    AND
                        P.PROKIND = 'p'
                    ORDER BY
                        P.OID
                    LIMIT 1
                    """;
            this.printSql(sql);
            PreparedStatement statement = this.getConnManager().connection().prepareStatement(sql);
            statement.setString(1, schema);
            statement.setString(2, produceName);
            // 执行SQL查询并获取结果集
            ResultSet resultSet = statement.executeQuery();
            // 打印元数据
            DBUtil.printMetaData(resultSet);
            PostgresqlProcedure procedure = new PostgresqlProcedure();
            procedure.setSchema(schema);
            procedure.setName(produceName);
            // 遍历结果集
            while (resultSet.next()) {
                long oid = resultSet.getLong("OID");
                String createDefinition = resultSet.getString("DLL");
                String definition = resultSet.getString("DEFINITION");
                boolean securityDefiner = resultSet.getBoolean("SECURITY_DEFINER");
                boolean strict = resultSet.getBoolean("STRICT");
                String volatility = resultSet.getString("VOLATILE");
                String securityType = securityDefiner ? "DEFINER" : "INVOKER";
                StringBuilder characteristic = new StringBuilder();
                if (param.isFull()) {
                    List<PostgresqlRoutineParam> params = this.listProcedureParam(oid);
                    procedure.setParams(params);
                }
                if (StringUtil.isBlank(definition)) {
                    definition = ShellPostgresqlHelper.parseFunctionBody(createDefinition);
                }
                if (strict) {
                    characteristic.append(",STRICT");
                }
                if (StringUtil.equalsIgnoreCase("i", volatility)) {
                    characteristic.append(",IMMUTABLE");
                } else if (StringUtil.equalsIgnoreCase("s", volatility)) {
                    characteristic.append(",STABLE");
                }
                if (!characteristic.isEmpty()) {
                    procedure.setCharacteristic(characteristic.substring(1));
                }
                procedure.setSchema(schema);
                procedure.setDefinition(definition);
                procedure.setSecurityType(securityType);
                procedure.setCreateDefinition(createDefinition);
            }
            // 关闭连接和释放资源
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            return procedure;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 删除存储过程
     *
     * @param routine 存储过程
     */
    public void dropProcedure(PostgresqlProcedure routine) {
        if (routine == null || StringUtil.isBlank(routine.getSchema()) || StringUtil.isBlank(routine.getName())) {
            return;
        }
        try {
            String sql = "DROP PROCEDURE IF EXISTS " + DBUtil.wrap(routine.getSchema(), routine.getName(), DBDialect.POSTGRESQL);
            this.printSql(sql);
            Statement statement = this.getConnManager().connection().createStatement();
            statement.executeUpdate(sql);
            IOUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 创建过程
     *
     * @param param 参数
     */
    public void createProcedure(PostgresqlCreateProcedureParam param) {
        try {
            String sql = PostgresqlProcedureCreateSqlGenerator.generateSqlSingle(param);
            this.printSql(sql);
            Statement statement = this.getConnManager().connection().createStatement();
            statement.executeUpdate(sql);
            IOUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 修改过程
     *
     * @param param 参数
     */
    public void alertProcedure(PostgresqlAlertProcedureParam param) {
        try {
            String sql = PostgresqlProcedureAlertSqlGenerator.generateSqlSingle(param);
            this.printSql(sql);
            Statement statement = this.getConnManager().connection().createStatement();
            statement.executeUpdate(sql);
            IOUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 删除函数
     *
     * @param function 函数
     */
    public void dropFunction(PostgresqlFunction function) {
        if (function == null || StringUtil.isBlank(function.getSchema()) || StringUtil.isBlank(function.getName())) {
            return;
        }
        try {
            String sql = "DROP FUNCTION IF EXISTS " + DBUtil.wrap(function.getSchema(), function.getName(), DBDialect.POSTGRESQL);
            this.printSql(sql);
            Statement statement = this.getConnManager().connection().createStatement();
            statement.executeUpdate(sql);
            IOUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 查询函数信息(完整信息)
     *
     * @param schema       模式名称
     * @param functionName 函数名称
     * @return 函数对象
     */
    public PostgresqlFunction selectFunction(String schema, String functionName) {
        PostgresqlSelectFunctionParam param = new PostgresqlSelectFunctionParam();
        param.setFull(true);
        param.setSchema(schema);
        param.setFunctionName(functionName);
        return this.selectFunction(param);
    }

    /**
     * 查询函数信息(简要信息)
     *
     * @param schema       模式名称
     * @param functionName 函数名称
     * @return 函数对象
     */
    public PostgresqlFunction selectFunctionSimple(String schema, String functionName) {
        PostgresqlSelectFunctionParam param = new PostgresqlSelectFunctionParam();
        param.setFull(false);
        param.setSchema(schema);
        param.setFunctionName(functionName);
        return this.selectFunction(param);
    }

    /**
     * 查询函数
     *
     * @param param 参数
     * @return 结果
     */
    public PostgresqlFunction selectFunction(PostgresqlSelectFunctionParam param) {
        try {
            String schema = param.getSchema();
            String functionName = param.getFunctionName();
            String sql = """
                    SELECT
                        P.OID AS "OID",
                        P.PROSECDef AS "SECURITY_DEFINER",
                        P.PROVOLATILE AS "VOLATILE",
                        P.PROISSTRICT AS "STRICT",
                        P.PROSRC AS "DEFINITION",
                        PG_CATALOG.PG_GET_FUNCTIONDEF(P.OID) AS "DLL"
                    FROM
                        PG_CATALOG.PG_PROC P
                    JOIN
                        PG_CATALOG.PG_NAMESPACE N ON N.OID = P.PRONAMESPACE
                    WHERE
                        N.NSPNAME = ?
                    AND
                        P.PRONAME = ?
                    AND
                        P.PROKIND = 'f'
                    ORDER BY
                        P.OID
                    LIMIT 1
                    """;
            this.printSql(sql);
            PreparedStatement statement = this.getConnManager().connection().prepareStatement(sql);
            statement.setString(1, schema);
            statement.setString(2, functionName);
            // 执行SQL查询并获取结果集
            ResultSet resultSet = statement.executeQuery();
            // 打印元数据
            DBUtil.printMetaData(resultSet);
            PostgresqlFunction function = new PostgresqlFunction();
            function.setSchema(schema);
            function.setName(functionName);
            // 遍历结果集
            while (resultSet.next()) {
                long oid = resultSet.getLong("OID");
                String createDefinition = resultSet.getString("DLL");
                String definition = resultSet.getString("DEFINITION");
                boolean securityDefiner = resultSet.getBoolean("SECURITY_DEFINER");
                boolean strict = resultSet.getBoolean("STRICT");
                String volatility = resultSet.getString("VOLATILE");
                String securityType = securityDefiner ? "DEFINER" : "INVOKER";
                StringBuilder characteristic = new StringBuilder();
                if (param.isFull()) {
                    List<PostgresqlRoutineParam> params = this.listFunctionParam(oid);
                    function.setParams(params);
                }
                if (StringUtil.isBlank(definition)) {
                    definition = ShellPostgresqlHelper.parseFunctionBody(createDefinition);
                }
                if (strict) {
                    characteristic.append(",STRICT");
                }
                if (StringUtil.equalsIgnoreCase("i", volatility)) {
                    characteristic.append(",IMMUTABLE");
                } else if (StringUtil.equalsIgnoreCase("s", volatility)) {
                    characteristic.append(",STABLE");
                }
                if (!characteristic.isEmpty()) {
                    function.setCharacteristic(characteristic.substring(1));
                }
                function.setSchema(schema);
                function.setDefinition(definition);
                function.setSecurityType(securityType);
                function.setCreateDefinition(createDefinition);
            }
            // 关闭连接和释放资源
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            return function;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 创建函数
     *
     * @param param 参数
     */
    public void createFunction(PostgresqlCreateFunctionParam param) {
        try {
            String sql = PostgresqlFunctionCreateSqlGenerator.generateSqlSingle(param);
            this.printSql(sql);
            Statement statement = this.getConnManager().connection().createStatement();
            statement.executeUpdate(sql);
            IOUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 查询单条记录
     *
     * @param param 查询参数
     * @return 记录对象
     */
    public PostgresqlRecord selectRecord(PostgresqlSelectRecordParam param) {
        try {
            String schema = param.getSchema();
            String tableName = param.getTableName();
            Connection connection = this.getConnManager().connection();
            PostgresqlRecordPrimaryKey primaryKey = param.getPrimaryKey();
            StringBuilder builder = new StringBuilder("SELECT * FROM ");
            builder.append(DBUtil.wrap(schema, tableName, DBDialect.POSTGRESQL))
                    .append(" WHERE ")
                    .append(DBUtil.wrap(primaryKey.getColumnName(), DBDialect.POSTGRESQL))
                    .append(" = ?");
            String sql = builder.toString();
            this.printSql(sql);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setObject(1, primaryKey.data());
            ResultSet resultSet = statement.executeQuery();
            DBUtil.printMetaData(resultSet);
            PostgresqlColumns columns = ShellPostgresqlHelper.parseColumns(resultSet);
            PostgresqlRecord record = new PostgresqlRecord(columns);
            while (resultSet.next()) {
                for (PostgresqlColumn column : columns) {
                    Object data = resultSet.getObject(column.getName());
                    record.putValue(column, data);
                }
            }
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            return record;
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    /**
     * 查询客户端字符集
     *
     * @return 字符集
     */
    public String selectClientCharacter() {
        // PostgreSQL客户端字符集，返回默认值
        return "UTF-8";
    }

    /**
     * 判断表是否存在自增列
     *
     * @param schema    模式名称
     * @param tableName 表名称
     * @return 存在返回true，否则返回false
     */
    public boolean existAutoIncrement(String schema, String tableName) {
        try {
            Connection connection = this.getConnManager().connection();
            boolean exist = false;
            //            if (this.isDbaRole()) {
            //                // String sql = "SELECT COUNT(*) FROM ALL_CONSTRAINTS WHERE OWNER = ? AND TABLE_NAME = ? AND CONSTRAINT_TYPE = 'P'";
            //                String sql = """
            //                        SELECT
            //                            COUNT(*)
            //                        FROM
            //                            SYSCOLUMNS C, SYSOBJECTS O
            //                        WHERE
            //                            C.ID = O.ID
            //                        AND
            //                            O.NAME = ?
            //                        AND
            //                            O.SCHID = (SELECT ID FROM SYSOBJECTS WHERE NAME = ? AND TYPE$ = 'SCH')
            //                        AND
            //                            C.INFO2 = 1;
            //                        """;
            //                this.printSql(sql);
            //                PreparedStatement stmt = connection.prepareStatement(sql);
            //                stmt.setString(1, tableName);
            //                stmt.setString(2, schema);
            //                ResultSet resultSet = stmt.executeQuery();
            //                DBUtil.printMetaData(resultSet);
            //                exist = resultSet.next() && resultSet.getInt(1) > 0;
            //                IOUtil.close(resultSet);
            //                IOUtil.close(stmt);
            //            } else {
            String sql = """
                    SELECT
                        COUNT(*)
                    FROM
                        PG_CATALOG.PG_ATTRIBUTE A
                    JOIN
                        PG_CATALOG.PG_CLASS C ON C.OID = A.ATTRELID
                    JOIN
                        PG_CATALOG.PG_NAMESPACE N ON N.OID = C.RELNAMESPACE
                    LEFT JOIN
                        PG_CATALOG.PG_ATTRDEF AD ON AD.ADRELID = A.ATTRELID AND AD.ADNUM = A.ATTNUM
                    WHERE
                        N.NSPNAME = ?
                    AND
                        C.RELNAME = ?
                    AND
                        A.ATTNUM > 0
                    AND
                        NOT A.ATTISDROPPED
                    AND
                        (A.ATTIDENTITY <> '' OR PG_CATALOG.PG_GET_EXPR(AD.ADBIN, AD.ADRELID) LIKE 'nextval(%')
                    """;
            this.printSql(sql);
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, schema);
            stmt.setString(2, tableName);
            ResultSet resultSet = stmt.executeQuery();
            DBUtil.printMetaData(resultSet);
            if (resultSet.next()) {
                exist = resultSet.getInt(1) > 0;
            }
            IOUtil.close(resultSet);
            IOUtil.close(stmt);
            //            }
            return exist;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 查询主键约束列表
     *
     * @param schema    模式名称
     * @param tableName 表名称
     * @return 主键约束名称列表
     */
    public List<String> selectePrimaryKeys(String schema, String tableName) {
        try {
            Connection connection = this.getConnManager().connection();
            String sql = """
                    SELECT
                        CON.CONNAME AS "CONSTRAINT_NAME"
                    FROM
                        PG_CATALOG.PG_CONSTRAINT CON
                    JOIN
                        PG_CATALOG.PG_CLASS C ON C.OID = CON.CONRELID
                    JOIN
                        PG_CATALOG.PG_NAMESPACE N ON N.OID = C.RELNAMESPACE
                    WHERE
                        CON.CONTYPE = 'p'
                    AND
                        N.NSPNAME = ?
                    AND
                        C.RELNAME = ?
                    """;
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, schema);
            stmt.setString(2, tableName);
            ResultSet resultSet = stmt.executeQuery();
            DBUtil.printMetaData(resultSet);
            List<String> primaryKeys = new ArrayList<>();
            while (resultSet.next()) {
                primaryKeys.add(resultSet.getString("CONSTRAINT_NAME"));
            }
            IOUtil.close(resultSet);
            IOUtil.close(stmt);
            return primaryKeys;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 克隆表
     *
     * @param schema        数据库
     * @param tableName     表名称
     * @param newTableName  新表名称
     * @param includeRecord 是否包含数据
     * @return 克隆表名称
     */
    public String cloneTable(String schema, String tableName, String newTableName, boolean includeRecord) {
        try {
            Connection connection = this.getConnManager().connection();
            String sourceTable = DBUtil.wrap(schema, tableName, DBDialect.POSTGRESQL);
            String newTable = DBUtil.wrap(schema, newTableName, DBDialect.POSTGRESQL);
            // 克隆表结构（包含默认值、约束、索引、注释、IDENTITY等）
            String sql = "CREATE TABLE " + newTable + " (LIKE " + sourceTable + " INCLUDING ALL)";
            this.printSql(sql);
            Statement statement = connection.createStatement();
            statement.execute(sql);
            // 克隆数据
            if (includeRecord) {
                String insertSql = "INSERT INTO " + newTable;
                // GENERATED ALWAYS AS IDENTITY列需要OVERRIDING SYSTEM VALUE才能写入
                if (this.hasAlwaysIdentityColumn(schema, newTableName)) {
                    insertSql += " OVERRIDING SYSTEM VALUE";
                }
                insertSql += " SELECT * FROM " + sourceTable;
                this.printSql(insertSql);
                statement.executeUpdate(insertSql);
            }
            IOUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
        return newTableName;
    }

    /**
     * 判断表是否存在GENERATED ALWAYS AS IDENTITY字段
     *
     * @param schema    模式名称
     * @param tableName 表名称
     * @return 结果
     * @throws Exception 异常
     */
    private boolean hasAlwaysIdentityColumn(String schema, String tableName) throws Exception {
        Connection connection = this.getConnManager().connection();
        String sql = """
                SELECT
                    COUNT(*)
                FROM
                    PG_CATALOG.PG_ATTRIBUTE A
                JOIN
                    PG_CATALOG.PG_CLASS C ON C.OID = A.ATTRELID
                JOIN
                    PG_CATALOG.PG_NAMESPACE N ON N.OID = C.RELNAMESPACE
                WHERE
                    N.NSPNAME = ?
                AND
                    C.RELNAME = ?
                AND
                    A.ATTIDENTITY = 'a'
                """;
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, schema);
        statement.setString(2, tableName);
        ResultSet resultSet = statement.executeQuery();
        boolean result = resultSet.next() && resultSet.getInt(1) > 0;
        IOUtil.close(resultSet);
        IOUtil.close(statement);
        return result;
    }

    /**
     * 克隆视图
     *
     * @param schema      数据库
     * @param viewName    视图名称
     * @param newViewName 新视图名称
     */
    public void cloneView(String schema, String viewName, String newViewName) {
        String sql = this.showCreateView(schema, viewName);
        try {
            sql = sql.replace("VIEW " + DBUtil.wrap(viewName, DBDialect.POSTGRESQL), "VIEW " + DBUtil.wrap(newViewName, DBDialect.POSTGRESQL));
            sql = sql.replace("VIEW " + DBUtil.wrap(schema, viewName, DBDialect.POSTGRESQL), "VIEW " + DBUtil.wrap(schema, newViewName, DBDialect.POSTGRESQL));
            this.printSql(sql);
            Connection connection = this.getConnManager().connection();
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.execute();
            IOUtil.close(stmt);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 克隆函数
     *
     * @param schema          数据库
     * @param functionName    函数名称
     * @param newFunctionName 新函数名称
     */
    public void cloneFunction(String schema, String functionName, String newFunctionName) {
        String sql = this.showCreateFunction(schema, functionName);
        try {
            sql = this.replaceRoutineName(sql, "FUNCTION", schema, newFunctionName);
            this.printSql(sql);
            Connection connection = this.getConnManager().connection();
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.execute();
            IOUtil.close(stmt);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 克隆过程
     *
     * @param schema           数据库
     * @param procedureName    过程名称
     * @param newProcedureName 新过程名称
     */
    public void cloneProcedure(String schema, String procedureName, String newProcedureName) {
        String sql = this.showCreateProcedure(schema, procedureName);
        try {
            sql = this.replaceRoutineName(sql, "PROCEDURE", schema, newProcedureName);
            this.printSql(sql);
            Connection connection = this.getConnManager().connection();
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.execute();
            IOUtil.close(stmt);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 替换函数/过程定义中的名称
     *
     * @param definition 定义
     * @param keyword    FUNCTION或PROCEDURE
     * @param schema     模式名称
     * @param newName    新名称
     * @return 结果
     */
    private String replaceRoutineName(String definition, String keyword, String schema, String newName) {
        if (StringUtil.isBlank(definition)) {
            return definition;
        }
        int keywordIndex = definition.toUpperCase().indexOf(keyword + " ");
        if (keywordIndex < 0) {
            return definition;
        }
        int start = keywordIndex + keyword.length() + 1;
        int end = definition.indexOf('(', start);
        if (end < 0) {
            return definition;
        }
        String replacement = DBUtil.wrap(schema, newName, DBDialect.POSTGRESQL);
        return definition.substring(0, start) + replacement + definition.substring(end);
    }

    /**
     * 查询例程参数列表
     *
     * @param schema      模式名称
     * @param routineName 例程名称
     * @param routineType 例程类型
     * @return 例程参数列表
     * @throws Exception 异常
     */
    public List<PostgresqlRoutineParam> listRoutineParam(String schema, String routineName, String routineType) throws Exception {
        try {
            Connection connection = this.getConnManager().connection();
            String prokind = StringUtil.equalsIgnoreCase(routineType, "PROCEDURE") ? "p" : "f";
            // 查询函数/过程oid及返回类型
            String procSql = """
                    SELECT
                        P.OID AS "OID",
                        PG_CATALOG.FORMAT_TYPE(P.PRORETTYPE, NULL) AS "RETURN_TYPE"
                    FROM
                        PG_CATALOG.PG_PROC P
                    JOIN
                        PG_CATALOG.PG_NAMESPACE N ON N.OID = P.PRONAMESPACE
                    WHERE
                        N.NSPNAME = ?
                    AND
                        P.PRONAME = ?
                    AND
                        P.PROKIND = ?
                    ORDER BY
                        P.OID
                    LIMIT 1
                    """;
            this.printSql(procSql);
            PreparedStatement procStatement = connection.prepareStatement(procSql);
            procStatement.setString(1, schema);
            procStatement.setString(2, routineName);
            procStatement.setString(3, prokind);
            ResultSet procResultSet = procStatement.executeQuery();
            long oid = 0;
            String returnType = null;
            if (procResultSet.next()) {
                oid = procResultSet.getLong("OID");
                returnType = procResultSet.getString("RETURN_TYPE");
            }
            IOUtil.close(procResultSet);
            IOUtil.close(procStatement);
            if (oid == 0) {
                return Collections.emptyList();
            }
            // 查询参数
            String sql = """
                    SELECT
                        K.ORD AS "POSITION",
                        PG_CATALOG.FORMAT_TYPE(T.OID, NULL) AS "DATA_TYPE",
                        P.PROARGNAMES[(K.ORD)::INT] AS "PARAMETER_NAME",
                        COALESCE(P.PROARGMODES[(K.ORD)::INT]::TEXT, 'i') AS "PARAMETER_MODE"
                    FROM
                        PG_CATALOG.PG_PROC P
                    JOIN LATERAL
                        UNNEST(COALESCE(P.PROALLARGTYPES, P.PROARGTYPES)::OID[]) WITH ORDINALITY AS K(TYPOID, ORD) ON TRUE
                    JOIN
                        PG_CATALOG.PG_TYPE T ON T.OID = K.TYPOID
                    WHERE
                        P.OID = ?
                    ORDER BY
                        K.ORD
                    """;
            this.printSql(sql);
            List<PostgresqlRoutineParam> params = new ArrayList<>();
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, oid);
            // 执行SQL查询并获取结果集
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                PostgresqlRoutineParam param = new PostgresqlRoutineParam();
                param.setPosition(resultSet.getInt("POSITION"));
                param.setType(resultSet.getString("DATA_TYPE"));
                param.setName(resultSet.getString("PARAMETER_NAME"));
                param.setMode(this.routineParamMode(resultSet.getString("PARAMETER_MODE")));
                param.setCharset(null);
                params.add(param);
            }
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            // 函数返回参数
            if (!StringUtil.equalsIgnoreCase(prokind, "p") && StringUtil.isNotBlank(returnType)) {
                PostgresqlRoutineParam returnParam = new PostgresqlRoutineParam();
                returnParam.setName("V_RET");
                returnParam.setMode("OUT");
                returnParam.setPosition(0);
                returnParam.setType(returnType);
                params.add(returnParam);
            }
            // 参数顺序必须保持系统目录中的顺序，不能按名称排序
            return params;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Collections.emptyList();
    }

    /**
     * 按函数/过程OID查询参数，确保重载函数读取到各自正确的参数签名。
     *
     * @param oid         函数/过程OID
     * @param routineType 类型
     * @return 参数列表
     * @throws Exception 异常
     */
    public List<PostgresqlRoutineParam> listRoutineParamByOid(long oid, String routineType) throws Exception {
        try {
            Connection connection = this.getConnManager().connection();
            String prokind = StringUtil.equalsIgnoreCase(routineType, "PROCEDURE") ? "p" : "f";
            String returnSql = """
                    SELECT
                        PG_CATALOG.FORMAT_TYPE(P.PRORETTYPE, NULL) AS "RETURN_TYPE"
                    FROM
                        PG_CATALOG.PG_PROC P
                    WHERE
                        P.OID = ?
                    """;
            PreparedStatement returnStatement = connection.prepareStatement(returnSql);
            returnStatement.setLong(1, oid);
            ResultSet returnResult = returnStatement.executeQuery();
            String returnType = null;
            if (returnResult.next()) {
                returnType = returnResult.getString("RETURN_TYPE");
            }
            IOUtil.close(returnResult);
            IOUtil.close(returnStatement);
            if (returnType == null) {
                return Collections.emptyList();
            }
            String sql = """
                    SELECT
                        K.ORD AS "POSITION",
                        PG_CATALOG.FORMAT_TYPE(T.OID, NULL) AS "DATA_TYPE",
                        P.PROARGNAMES[(K.ORD)::INT] AS "PARAMETER_NAME",
                        COALESCE(P.PROARGMODES[(K.ORD)::INT]::TEXT, 'i') AS "PARAMETER_MODE"
                    FROM
                        PG_CATALOG.PG_PROC P
                    JOIN LATERAL
                        UNNEST(COALESCE(P.PROALLARGTYPES, P.PROARGTYPES)::OID[]) WITH ORDINALITY AS K(TYPOID, ORD) ON TRUE
                    JOIN
                        PG_CATALOG.PG_TYPE T ON T.OID = K.TYPOID
                    WHERE
                        P.OID = ?
                    ORDER BY
                        K.ORD
                    """;
            this.printSql(sql);
            List<PostgresqlRoutineParam> params = new ArrayList<>();
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, oid);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                PostgresqlRoutineParam param = new PostgresqlRoutineParam();
                param.setPosition(resultSet.getInt("POSITION"));
                param.setType(resultSet.getString("DATA_TYPE"));
                param.setName(resultSet.getString("PARAMETER_NAME"));
                param.setMode(this.routineParamMode(resultSet.getString("PARAMETER_MODE")));
                param.setCharset(null);
                params.add(param);
            }
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            if (!StringUtil.equalsIgnoreCase(prokind, "p") && StringUtil.isNotBlank(returnType)) {
                PostgresqlRoutineParam returnParam = new PostgresqlRoutineParam();
                returnParam.setName("V_RET");
                returnParam.setMode("OUT");
                returnParam.setPosition(0);
                returnParam.setType(returnType);
                params.add(returnParam);
            }
            return params;
        } catch (Exception ex) {
            ex.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * 解析例程参数模式
     *
     * @param mode 模式标识
     * @return 参数模式
     */
    private String routineParamMode(String mode) {
        if (mode == null) {
            return "IN";
        }
        return switch (mode.toLowerCase()) {
            case "o" -> "OUT";
            case "b" -> "INOUT";
            case "v" -> "VARIADIC";
            case "t" -> "TABLE";
            default -> "IN";
        };
    }

    /**
     * 查询函数参数列表
     *
     * @param schema       模式名称
     * @param functionName 函数名称
     * @return 参数列表
     * @throws Exception 异常
     */
    public List<PostgresqlRoutineParam> listFunctionParam(String schema, String functionName) throws Exception {
        return listRoutineParam(schema, functionName, "FUNCTION");
    }

    /**
     * 按函数OID查询参数
     *
     * @param oid 函数OID
     * @return 参数列表
     * @throws Exception 异常
     */
    public List<PostgresqlRoutineParam> listFunctionParam(long oid) throws Exception {
        return this.listRoutineParamByOid(oid, "FUNCTION");
    }

    /**
     * 查询过程参数列表
     *
     * @param schema        模式名称
     * @param procedureName 过程名称
     * @return 参数列表
     * @throws Exception 异常
     */
    public List<PostgresqlRoutineParam> listProcedureParam(String schema, String procedureName) throws Exception {
        return listRoutineParam(schema, procedureName, "PROCEDURE");
    }

    /**
     * 按过程OID查询参数
     *
     * @param oid 过程OID
     * @return 参数列表
     * @throws Exception 异常
     */
    public List<PostgresqlRoutineParam> listProcedureParam(long oid) throws Exception {
        return this.listRoutineParamByOid(oid, "PROCEDURE");
    }

    //    private Boolean dbaRole;
    //
    //    private final Object dbaRoleLock = new Object();
    //
    //    private boolean isDbaRole() {
    //        if (this.dbaRole == null) {
    //            synchronized (this.dbaRoleLock) {
    //                ResultSet resultSet = null;
    //                try {
    //                    Statement statement = this.getConnManager().connection().createStatement();
    //                    String sql = "SELECT COUNT(*) FROM SYSOBJECTS";
    //                    this.printSql(sql);
    //                    resultSet = statement.executeQuery(sql);
    //                    this.dbaRole = true;
    //                } catch (Exception ex) {
    //                    this.dbaRole = false;
    //                } finally {
    //                    IOUtil.close(resultSet);
    //                }
    //            }
    //        }
    //        return this.dbaRole;
    //    }

    /**
     * 查询父表
     *
     * @param schema 模式
     * @param table  表名称
     */
    public List<String> selectParentTables(String schema, String table) {
        List<String> parents = new ArrayList<>();
        List<PostgresqlForeignKey> foreignKeys = this.selectForeignKeys(schema, table);
        for (PostgresqlForeignKey foreignKey : foreignKeys) {
            String parentTable = foreignKey.getReferencedTable();
            parents.add(parentTable);
        }
        return parents;
    }

    /**
     * 创建序列
     *
     * @param param 参数
     */
    public void createSequence(PostgresqlCreateSequenceParam param) {
        try {
            String sql = PostgresqlSequenceCreateSqlGenerator.generateSqlSingle(param);
            this.printSql(sql);
            Statement statement = this.getConnManager().connection().createStatement();
            statement.executeUpdate(sql);
            IOUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public int sequenceSize(String schema) {
        int size = 0;
        try {
            Connection connection = this.getConnManager().connection();
            String sql = """
                    SELECT
                        COUNT(*)
                    FROM
                        PG_CATALOG.PG_CLASS C
                    JOIN
                        PG_CATALOG.PG_NAMESPACE N ON N.OID = C.RELNAMESPACE
                    WHERE
                        N.NSPNAME = ?
                    AND
                        C.RELKIND = 'S'
                    AND
                        NOT EXISTS (
                            SELECT 1
                            FROM PG_CATALOG.PG_DEPEND D
                            WHERE D.CLASSID = 'pg_class'::REGCLASS
                            AND D.OBJID = C.OID
                            AND D.REFCLASSID = 'pg_class'::REGCLASS
                            AND D.REFOBJSUBID > 0
                        )
                    """;
            this.printSql(sql);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, schema);
            ResultSet resultSet = statement.executeQuery();
            DBUtil.printMetaData(resultSet);
            if (resultSet.next()) {
                size = resultSet.getInt(1);
            }
            IOUtil.close(resultSet);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
        return size;
    }

    public List<PostgresqlSequence> selectSequencesSimple(String schema) {
        PostgresqlSelectSequenceParam param = new PostgresqlSelectSequenceParam();
        param.setFull(false);
        param.setSchema(schema);
        return this.selectSequences(param);
    }

    public void cloneSequence(String schema, String sequenceName, String newSequenceName) {
        String sql = this.showCreateSequence(schema, sequenceName);
        try {
            sql = sql.replace("SEQUENCE " + DBUtil.wrap(sequenceName, DBDialect.POSTGRESQL), "SEQUENCE " + DBUtil.wrap(newSequenceName, DBDialect.POSTGRESQL));
            sql = sql.replace("SEQUENCE " + DBUtil.wrap(schema, sequenceName, DBDialect.POSTGRESQL), "SEQUENCE " + DBUtil.wrap(schema, newSequenceName, DBDialect.POSTGRESQL));
            this.printSql(sql);
            Connection connection = this.getConnManager().connection();
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.execute();
            IOUtil.close(stmt);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public PostgresqlSequence selectSequence(String schema, String sequenceName) {
        try {
            PreparedStatement statement;
            Connection connection = this.getConnManager().connection();
            String sql = """
                    SELECT
                        MIN_VALUE,
                        MAX_VALUE,
                        CACHE_SIZE,
                        CYCLE,
                        INCREMENT_BY,
                        LAST_VALUE AS CURRENT_VALUE,
                        START_VALUE
                    FROM
                        PG_CATALOG.PG_SEQUENCES
                    WHERE
                        SCHEMANAME = ?
                    AND
                        SEQUENCENAME = ?
                    """;
            this.printSql(sql);
            statement = connection.prepareStatement(sql);
            statement.setString(1, schema);
            statement.setString(2, sequenceName);
            ResultSet resultSet = statement.executeQuery();
            PostgresqlSequence sequence = new PostgresqlSequence();
            sequence.setName(sequenceName);
            while (resultSet.next()) {
                Long minValue = this.getLong(resultSet, "MIN_VALUE");
                Long maxValue = this.getLong(resultSet, "MAX_VALUE");
                Long cacheSize = this.getLong(resultSet, "CACHE_SIZE");
                boolean cycle = resultSet.getBoolean("CYCLE");
                Long incrementBy = this.getLong(resultSet, "INCREMENT_BY");
                Long currentValue = this.getLong(resultSet, "CURRENT_VALUE");
                sequence.setSchema(schema);
                sequence.setMinVal(minValue);
                sequence.setMaxVal(maxValue);
                sequence.setCacheSize(cacheSize);
                sequence.setCurrentVal(currentValue);
                sequence.setIncrementBy(incrementBy);
                sequence.setCycle(cycle);
                // PostgreSQL序列不支持ORDER
                sequence.setOrder(false);
            }
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            sequence.setCreateDefinition(this.showCreateSequence(schema, sequenceName));
            return sequence;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public void dropSequence(PostgresqlSequence sequence) {
        if (sequence == null || StringUtil.isBlank(sequence.getSchema()) || StringUtil.isBlank(sequence.getName())) {
            return;
        }
        try {
            String sql = "DROP SEQUENCE IF EXISTS " + DBUtil.wrap(sequence.getSchema(), sequence.getName(), DBDialect.POSTGRESQL);
            this.printSql(sql);
            Statement statement = this.getConnManager().connection().createStatement();
            statement.executeUpdate(sql);
            IOUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public void renameSequence(String schema, String oldSequenceName, String newSequenceName) {
        try {
            String sql = "ALTER SEQUENCE " + DBUtil.wrap(schema, oldSequenceName, DBDialect.POSTGRESQL)
                    + " RENAME TO " + DBUtil.wrap(newSequenceName, DBDialect.POSTGRESQL);
            this.printSql(sql);
            Statement statement = this.getConnManager().connection().createStatement();
            statement.execute(sql);
            IOUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public void alertSequence(PostgresqlAlertSequenceParam param) {
        Connection connection = null;
        boolean autoCommit = true;
        Statement statement = null;
        try {
            String sql = PostgresqlSequenceAlertSqlGenerator.generateSqlSingle(param);
            // 无变化
            if (StringUtil.isBlank(sql)) {
                return;
            }
            connection = this.getConnManager().connection();
            autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            statement = connection.createStatement();
            this.printSql(sql);
            statement.executeUpdate(sql);
            connection.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
            DBUtil.rollback(connection);
            throw new ShellException(ex);
        } finally {
            IOUtil.close(statement);
            if (connection != null) {
                try {
                    connection.setAutoCommit(autoCommit);
                } catch (SQLException ex) {
                    JulLog.warn("restore autocommit error", ex);
                }
            }
        }
    }

    @Override
    public void printSql(String sql) {
        DBUtil.printSql(sql);
        String compressedSql = DBSqlParser.compressSql(sql, this.dialect());
        ShellEventUtil.printSql(compressedSql, this.shellConnect);
    }
}
