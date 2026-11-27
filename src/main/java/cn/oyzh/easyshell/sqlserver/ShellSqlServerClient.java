package cn.oyzh.easyshell.sqlserver;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sqlserver.check.SqlServerCheck;
import cn.oyzh.easyshell.sqlserver.column.SqlServerColumn;
import cn.oyzh.easyshell.sqlserver.column.SqlServerColumns;
import cn.oyzh.easyshell.sqlserver.column.SqlServerSelectColumnParam;
import cn.oyzh.easyshell.sqlserver.foreignKey.SqlServerForeignKey;
import cn.oyzh.easyshell.sqlserver.function.SqlServerAlertFunctionParam;
import cn.oyzh.easyshell.sqlserver.function.SqlServerCreateFunctionParam;
import cn.oyzh.easyshell.sqlserver.function.SqlServerFunction;
import cn.oyzh.easyshell.sqlserver.function.SqlServerSelectFunctionParam;
import cn.oyzh.easyshell.sqlserver.generator.function.SqlServerFunctionAlertSqlGenerator;
import cn.oyzh.easyshell.sqlserver.generator.function.SqlServerFunctionCreateSqlGenerator;
import cn.oyzh.easyshell.sqlserver.generator.procedure.SqlServerProcedureAlertSqlGenerator;
import cn.oyzh.easyshell.sqlserver.generator.procedure.SqlServerProcedureCreateSqlGenerator;
import cn.oyzh.easyshell.sqlserver.generator.sequence.SqlServerSequenceAlertSqlGenerator;
import cn.oyzh.easyshell.sqlserver.generator.sequence.SqlServerSequenceCreateSqlGenerator;
import cn.oyzh.easyshell.sqlserver.generator.table.SqlServerTableAlertSqlGenerator;
import cn.oyzh.easyshell.sqlserver.generator.table.SqlServerTableCreateSqlGenerator;
import cn.oyzh.easyshell.sqlserver.generator.view.SqlServerViewAlertSqlGenerator;
import cn.oyzh.easyshell.sqlserver.generator.view.SqlServerViewCreateSqlGenerator;
import cn.oyzh.easyshell.sqlserver.index.SqlServerIndex;
import cn.oyzh.easyshell.sqlserver.procedure.SqlServerAlertProcedureParam;
import cn.oyzh.easyshell.sqlserver.procedure.SqlServerCreateProcedureParam;
import cn.oyzh.easyshell.sqlserver.procedure.SqlServerProcedure;
import cn.oyzh.easyshell.sqlserver.procedure.SqlServerSelectProcedureParam;
import cn.oyzh.easyshell.sqlserver.record.SqlServerDeleteRecordParam;
import cn.oyzh.easyshell.sqlserver.record.SqlServerInsertRecordParam;
import cn.oyzh.easyshell.sqlserver.record.SqlServerRecord;
import cn.oyzh.easyshell.sqlserver.record.SqlServerRecordFilter;
import cn.oyzh.easyshell.sqlserver.record.SqlServerRecordPrimaryKey;
import cn.oyzh.easyshell.sqlserver.record.SqlServerSelectRecordParam;
import cn.oyzh.easyshell.sqlserver.record.SqlServerUpdateRecordParam;
import cn.oyzh.easyshell.sqlserver.routine.SqlServerRoutineParam;
import cn.oyzh.easyshell.sqlserver.schema.SqlServerSchema;
import cn.oyzh.easyshell.sqlserver.sequence.SqlServerAlertSequenceParam;
import cn.oyzh.easyshell.sqlserver.sequence.SqlServerCreateSequenceParam;
import cn.oyzh.easyshell.sqlserver.sequence.SqlServerSelectSequenceParam;
import cn.oyzh.easyshell.sqlserver.sequence.SqlServerSequence;
import cn.oyzh.easyshell.sqlserver.table.SqlServerAlertTableParam;
import cn.oyzh.easyshell.sqlserver.table.SqlServerCreateTableParam;
import cn.oyzh.easyshell.sqlserver.table.SqlServerSelectTableParam;
import cn.oyzh.easyshell.sqlserver.table.SqlServerTable;
import cn.oyzh.easyshell.sqlserver.trigger.SqlServerSelectTriggerParam;
import cn.oyzh.easyshell.sqlserver.trigger.SqlServerTrigger;
import cn.oyzh.easyshell.sqlserver.type.SqlServerTypeDefinition;
import cn.oyzh.easyshell.sqlserver.view.SqlServerAlertViewParam;
import cn.oyzh.easyshell.sqlserver.view.SqlServerCreateViewParam;
import cn.oyzh.easyshell.sqlserver.view.SqlServerSelectViewParam;
import cn.oyzh.easyshell.sqlserver.view.SqlServerView;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.db.index.ShellIndexHelper;
import cn.oyzh.easyshell.db.index.ShellSelectIndexParam;
import cn.oyzh.easyshell.db.trigger.ShellSelectTriggerParam;
import cn.oyzh.easyshell.db.type.ShellSelectTypeParam;
import cn.oyzh.easyshell.db.type.ShellTypeHelper;
import cn.oyzh.easyshell.db.trigger.ShellTriggerHelper;
import cn.oyzh.easyshell.domain.ShellJumpConfig;
import cn.oyzh.easyshell.domain.ShellProxyConfig;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.internal.ShellBaseClient;
import cn.oyzh.easyshell.internal.ShellClientChecker;
import cn.oyzh.easyshell.internal.ShellConnState;
import cn.oyzh.easyshell.query.sqlserver.SqlServerExecuteResult;
import cn.oyzh.easyshell.query.sqlserver.SqlServerExplainResult;
import cn.oyzh.easyshell.query.sqlserver.SqlServerQueryObject;
import cn.oyzh.easyshell.util.db.ShellSequenceUtil;
import cn.oyzh.easyshell.util.sqlserver.ShellSqlServerColumnUtil;
import cn.oyzh.easyshell.util.sqlserver.ShellSqlServerDataUtil;
import cn.oyzh.easyshell.util.sqlserver.ShellSqlServerUtil;
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

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SqlServer数据库客户端封装，提供数据库连接、表/视图/函数/过程/触发器等对象的CRUD操作，
 * 以及SQL执行、记录管理、索引/外键/检查约束管理等功能
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellSqlServerClient implements ShellBaseClient, DBClient {

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
            this.connManager = new ShellSqlServerConnManager();
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
     * 构造SqlServer客户端
     *
     * @param shellConnect 连接信息
     */
    public ShellSqlServerClient(ShellConnect shellConnect) {
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
        this.getConnManager().setConnectTimeout(Math.max(1, timeout / 1000));
        try {
            // 开始连接时间
            final AtomicLong starTime = new AtomicLong();
            // 开始连接时间
            starTime.set(System.currentTimeMillis());
            // 更新连接状态
            this.state.set(ShellConnState.CONNECTING);
            // 连接成功前阻塞线程
            if (this.getConnManager().connection().isValid(Math.max(1, timeout / 1000))) {
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
            JulLog.warn("SqlServer client start error", ex);
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
            connConfig.setSocketFactory(ShellSqlServerProxySocketFactory.class.getName());
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
            JulLog.warn("SqlServer client close error.", ex);
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
            return !connection.isClosed() && connection.isValid(Math.max(1, this.getConnManager().getConnectTimeout()));
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
                    SELECT COUNT(*)
                    FROM SYS.TABLES T
                    JOIN SYS.SCHEMAS S ON S.SCHEMA_ID = T.SCHEMA_ID
                    WHERE S.NAME = ?
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
                    SELECT COUNT(*)
                    FROM SYS.VIEWS V
                    JOIN SYS.SCHEMAS S ON S.SCHEMA_ID = V.SCHEMA_ID
                    WHERE S.NAME = ?
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
    public DBQueryResults<SqlServerExecuteResult> executeSql(String schema, String sql) {
        DBQueryResults<SqlServerExecuteResult> results = new DBQueryResults<>();
        Connection connection = null;
        boolean autoCommit = true;
        Statement statement = null;
        try {
            this.printSql(sql);
            DBSqlParser parser = DBSqlParser.getParser(sql, DBDialect.SQLSERVER);
            List<String> list = ShellSqlServerUtil.splitSqlServerBatches(sql);
            connection = this.getConnManager().connection(schema);
            autoCommit = connection.getAutoCommit();
            // SqlServer 的 DDL/DML 都是事务性的。查询页按 psql 的自动提交语义逐条执行，
            // 避免关闭连接时回滚未提交的 DDL/DML；用户显式写 BEGIN/COMMIT/ROLLBACK 时仍按事务执行。
            connection.setAutoCommit(true);
            statement = connection.createStatement();
            for (String execSql : list) {
                SqlServerExecuteResult result = new SqlServerExecuteResult();
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
    public DBQueryResults<SqlServerExecuteResult> executeSql(String sql) {
        return this.executeSql(null, sql);
    }

    @Override
    public DBRawQueryResults executeSqlRaw(String name, String sql) throws Exception {
        DBRawQueryResults results = new DBRawQueryResults();
        Connection connection;
        try {
            this.printSql(sql);
            List<String> sqlList = ShellSqlServerUtil.splitSqlServerBatches(sql);
            // SqlServer一个连接对应一个数据库，name仅作为schema提示，SQL中已限定schema
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
                    SELECT COUNT(*)
                    FROM SYS.PROCEDURES P
                    JOIN SYS.SCHEMAS S ON S.SCHEMA_ID = P.SCHEMA_ID
                    WHERE S.NAME = ?
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
                    SELECT COUNT(*)
                    FROM SYS.OBJECTS O
                    JOIN SYS.SCHEMAS S ON S.SCHEMA_ID = O.SCHEMA_ID
                    WHERE S.NAME = ?
                    AND O.TYPE IN ('FN', 'IF', 'TF', 'FS', 'FT', 'AF')
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
    public void alertFunction(SqlServerAlertFunctionParam param) {
        Connection connection = null;
        boolean autoCommit = true;
        Statement statement = null;
        try {
            String sql = SqlServerFunctionAlertSqlGenerator.generateSqlSingle(param);
            this.printSql(sql);
            connection = this.getConnManager().connection();
            autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            statement = connection.createStatement();
            for (String statementSql : ShellSqlServerUtil.splitSqlServerBatches(sql)) {
                statement.execute(statementSql);
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
     * 查询触发器列表
     *
     * @param schema 模式
     * @return 触发器列表
     */
    public List<SqlServerTrigger> selectTriggers(String schema) {
        SqlServerSelectTriggerParam param = new SqlServerSelectTriggerParam();
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
    public List<SqlServerTrigger> selectTriggers(String schema, String tableName) {
        SqlServerSelectTriggerParam param = new SqlServerSelectTriggerParam();
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
    //    public List<SqlServerTrigger> selectTriggers(SqlServerSelectTriggerParam param) {
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
    //            List<SqlServerTrigger> list = new ArrayList<>();
    //            while (resultSet.next()) {
    //                SqlServerTrigger trigger = new SqlServerTrigger();
    //                String name = resultSet.getString("TRIGGER_NAME");
    //                String type = resultSet.getString("TRIGGER_TYPE");
    //                String createDefinition = resultSet.getString("DLL");
    //                String definition = resultSet.getString("ACTION_STATEMENT");
    //                String tableName = resultSet.getString("EVENT_OBJECT_TABLE");
    //                String manipulation = resultSet.getString("EVENT_MANIPULATION");
    //                trigger.setName(name);
    //                trigger.setTableName(tableName);
    //                trigger.setCreateDefinition(createDefinition);
    //                trigger.setDefinition(ShellSqlServerHelper.fixTiggerDefinition(definition));
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
    public DBObjects<SqlServerTrigger> selectTriggers(SqlServerSelectTriggerParam param) {
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
                            TR.NAME AS "TRIGGER_NAME",
                            TR.IS_INSTEAD_OF_TRIGGER AS "INSTEAD_OF",
                            TB.NAME AS "EVENT_OBJECT_TABLE",
                            M.DEFINITION AS "FUNCTION_DEFINITION",
                            M.DEFINITION AS "DLL",
                            STUFF((SELECT ',' + TE.TYPE_DESC
                                FROM SYS.TRIGGER_EVENTS TE
                                WHERE TE.OBJECT_ID = TR.OBJECT_ID
                                ORDER BY TE.TYPE_DESC FOR XML PATH('')), 1, 1, '') AS "EVENTS"
                        FROM SYS.TRIGGERS TR
                        JOIN SYS.TABLES TB ON TB.OBJECT_ID = TR.PARENT_ID
                        JOIN SYS.SCHEMAS S ON S.SCHEMA_ID = TB.SCHEMA_ID
                        JOIN SYS.SQL_MODULES M ON M.OBJECT_ID = TR.OBJECT_ID
                        WHERE S.NAME = ?
                        ORDER BY TR.NAME
                        """;
                this.printSql(sql);
                statement = connection.prepareStatement(sql);
                statement.setString(1, schema);
            } else {
                String sql = """
                        SELECT
                            TR.NAME AS "TRIGGER_NAME",
                            TR.IS_INSTEAD_OF_TRIGGER AS "INSTEAD_OF",
                            TB.NAME AS "EVENT_OBJECT_TABLE",
                            M.DEFINITION AS "FUNCTION_DEFINITION",
                            M.DEFINITION AS "DLL",
                            STUFF((SELECT ',' + TE.TYPE_DESC
                                FROM SYS.TRIGGER_EVENTS TE
                                WHERE TE.OBJECT_ID = TR.OBJECT_ID
                                ORDER BY TE.TYPE_DESC FOR XML PATH('')), 1, 1, '') AS "EVENTS"
                        FROM SYS.TRIGGERS TR
                        JOIN SYS.TABLES TB ON TB.OBJECT_ID = TR.PARENT_ID
                        JOIN SYS.SCHEMAS S ON S.SCHEMA_ID = TB.SCHEMA_ID
                        JOIN SYS.SQL_MODULES M ON M.OBJECT_ID = TR.OBJECT_ID
                        WHERE S.NAME = ? AND TB.NAME = ?
                        ORDER BY TR.NAME
                        """;
                this.printSql(sql);
                statement = connection.prepareStatement(sql);
                statement.setString(1, schema);
                statement.setString(2, param.getTableName());
            }
            ResultSet resultSet = statement.executeQuery();
            DBObjects<SqlServerTrigger> list = new DBObjects<>();
            while (resultSet.next()) {
                SqlServerTrigger trigger = new SqlServerTrigger();
                String name = resultSet.getString("TRIGGER_NAME");
                boolean insteadOf = resultSet.getBoolean("INSTEAD_OF");
                String tableName = resultSet.getString("EVENT_OBJECT_TABLE");
                String functionDefinition = resultSet.getString("FUNCTION_DEFINITION");
                String createDefinition = resultSet.getString("DLL");
                String events = resultSet.getString("EVENTS");
                trigger.setName(name);
                trigger.setSchema(schema);
                trigger.setTableName(tableName);
                trigger.setCreateDefinition(createDefinition);
                trigger.setFunctionDefinition(functionDefinition);
                String policy = (insteadOf ? "INSTEAD OF " : "AFTER ")
                        + (StringUtil.isBlank(events) ? "INSERT" : events);
                trigger.setPolicy(policy);
                trigger.setDefinition(this.sqlServerTriggerBody(functionDefinition));
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

    public List<SqlServerSequence> selectSequences(String schema) {
        SqlServerSelectSequenceParam param = new SqlServerSelectSequenceParam();
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
    public DBObjects<SqlServerSequence> selectSequences(SqlServerSelectSequenceParam param) {
        try {
            String schema = param.getSchema();
            PreparedStatement statement;
            Connection connection = this.getConnManager().connection();
            String sql = """
                    SELECT
                        SC.NAME AS "SEQUENCE_OWNER",
                        S.NAME AS "SEQUENCE_NAME",
                        S.MINIMUM_VALUE AS MIN_VALUE,
                        S.MAXIMUM_VALUE AS MAX_VALUE,
                        S.CACHE_SIZE,
                        S.IS_CYCLING AS "CYCLE_FLAG",
                        S.INCREMENT AS INCREMENT_BY,
                        S.CURRENT_VALUE,
                        S.START_VALUE
                    FROM SYS.SEQUENCES S
                    JOIN SYS.SCHEMAS SC ON SC.SCHEMA_ID = S.SCHEMA_ID
                    WHERE SC.NAME = ?
                    ORDER BY S.NAME
                    """;
            this.printSql(sql);
            statement = connection.prepareStatement(sql);
            statement.setString(1, schema);
            ResultSet resultSet = statement.executeQuery();
            DBObjects<SqlServerSequence> list = new DBObjects<>();
            while (resultSet.next()) {
                SqlServerSequence sequence = new SqlServerSequence();
                BigInteger minVal = ShellSequenceUtil.getBigInteger(resultSet, "MIN_VALUE");
                BigInteger maxVal = ShellSequenceUtil.getBigInteger(resultSet, "MAX_VALUE");
                BigInteger cache = ShellSequenceUtil.getBigInteger(resultSet, "CACHE_SIZE");
                String name = resultSet.getString("SEQUENCE_NAME");
                boolean cycle = resultSet.getBoolean("CYCLE_FLAG");
                BigInteger increment = ShellSequenceUtil.getBigInteger(resultSet, "INCREMENT_BY");
                BigInteger currentVal = ShellSequenceUtil.getBigInteger(resultSet, "CURRENT_VALUE");
                sequence.setName(name);
                sequence.setSchema(schema);
                sequence.setMinVal(minVal);
                sequence.setMaxVal(maxVal);
                sequence.setCacheSize(cache);
                sequence.setCurrentVal(currentVal);
                sequence.setIncrementBy(increment);
                sequence.setCycle(cycle);
                // SqlServer序列不支持ORDER
                sequence.setOrder(false);
                list.add(sequence);
            }
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            // 生成建序列语句
            for (SqlServerSequence sequence : list) {
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
            ResultSet resultSet = stmt.executeQuery(
                    "SELECT CAST(SERVERPROPERTY('ProductVersion') AS NVARCHAR(128))");
            if (resultSet.next()) {
                version = resultSet.getString(1);
            }
            IOUtil.close(resultSet);
            IOUtil.close(stmt);
        } catch (Exception e) {
            // 兼容SHOW不可用的场景
            try {
                Statement stmt = this.getConnManager().connection().createStatement();
                ResultSet resultSet = stmt.executeQuery(
                        "SELECT CAST(SERVERPROPERTY('ProductVersion') AS NVARCHAR(128))");
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
            ResultSet resultSet = stmt.executeQuery("SELECT @@VERSION");
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

    //public void dropEvent(String schema, SqlServerEvent event) {
    //    try {
    //        String sql = "DROP EVENT " + DBUtil.wrap(event.getschema(), event.getName(), DBDialect.SQLSERVER);
    //        this.printSql(sql);
    //        Statement statement = this.getConnManager().connection(schema).createStatement();
    //        statement.executeUpdate(sql);
    //        IOUtil.close(statement);
    //    } catch (Exception ex) {
    //        ex.printStackTrace();
    //        throw new ShellException(ex);
    //    }
    //}

    //    public void createEvent(String schema, SqlServerEvent event) {
    //        try {
    //            String sql = SqlServerEventCreateSqlGenerator.generateSql(event);
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
    //    public void alertEvent(String schema, SqlServerEvent event) {
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

    //    public SqlServerEvent selectEvent(String schema, String eventName) {
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
    //            SqlServerEvent event = new SqlServerEvent();
    //            while (resultSet.next()) {
    //                String expr = resultSet.getString("EXPR");
    //                String status = resultSet.getString("STATUS");
    //                String action = resultSet.getString("ACTION");
    //                event.setName(eventName);
    //                event.setStatus("TRUE".equalsIgnoreCase(status) ? "ENABLE" : "DISABLE");
    //                SqlServerHelper.parseJobExpr(expr, event);
    //                SqlServerHelper.parseJobAction(action, event);
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

    //    public List<SqlServerEvent> events(String schema) {
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
    //            List<SqlServerEvent> list = new ArrayList<>();
    //            while (resultSet.next()) {
    //                String name = resultSet.getString("NAME");
    //                String expr = resultSet.getString("EXPR");
    //                String status = resultSet.getString("STATUS");
    //                String action = resultSet.getString("ACTION");
    //                SqlServerEvent event = new SqlServerEvent();
    //                event.setName(name);
    //                event.setStatus("TRUE".equalsIgnoreCase(status) ? "ENABLE" : "DISABLE");
    //                SqlServerHelper.parseJobExpr(expr, event);
    //                SqlServerHelper.parseJobAction(action, event);
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
            // SqlServer部分场景不支持getGeneratedKeys，使用lastval()兜底
            JulLog.warn("getGeneratedKeys error", ex);
        } finally {
            IOUtil.close(rs);
        }
        if (newId == null) {
            Connection connection = statement.getConnection();
            IOUtil.close(statement);
            statement = connection.createStatement();
            try {
                ResultSet resultSet = statement.executeQuery("SELECT CAST(SCOPE_IDENTITY() AS BIGINT)");
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
        // 序列名称可能是[schema].[seq]或schema.seq形式
        sequenceName = sequenceName.replace("[", "").replace("]", "").replace("\"", "");
        String schema = column.getSchema();
        String name = sequenceName;
        if (StringUtil.contains(sequenceName, ".")) {
            String[] arr = sequenceName.split("\\.", 2);
            schema = arr[0];
            name = arr[1];
        }
        if (schema != null) {
            schema = schema.replace("[", "").replace("]", "").replace("\"", "");
        }
        String objectName = StringUtil.isBlank(schema)
                ? DBUtil.wrap(name, DBDialect.SQLSERVER)
                : DBUtil.wrap(schema, name, DBDialect.SQLSERVER);
        String sql = "SELECT CAST(CURRENT_VALUE AS BIGINT) FROM SYS.SEQUENCES WHERE OBJECT_ID = OBJECT_ID("
                + DBUtil.wrapString(objectName, DBDialect.SQLSERVER) + ")";
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
    public List<SqlServerTable> selectTables(String schema) {
        SqlServerSelectTableParam param = new SqlServerSelectTableParam();
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
    public List<SqlServerTable> selectTablesSimple(String schema) {
        SqlServerSelectTableParam param = new SqlServerSelectTableParam();
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
    public List<SqlServerTable> selectTables(SqlServerSelectTableParam param) {
        try {
            String schema = param.getSchema();
            List<SqlServerTable> tables = new ArrayList<>();
            Connection connection = this.getConnManager().connection();
            String sql = """
                    SELECT
                        T.NAME AS TABLE_NAME,
                        CAST(EP.VALUE AS NVARCHAR(4000)) AS TABLE_COMMENT,
                        DS.NAME AS TABLE_SPACE
                    FROM SYS.TABLES T
                    JOIN SYS.SCHEMAS S ON S.SCHEMA_ID = T.SCHEMA_ID
                    LEFT JOIN SYS.EXTENDED_PROPERTIES EP
                        ON EP.MAJOR_ID = T.OBJECT_ID
                        AND EP.MINOR_ID = 0
                        AND EP.CLASS = 1
                        AND EP.NAME = 'MS_Description'
                    OUTER APPLY (
                        SELECT TOP 1 I.DATA_SPACE_ID
                        FROM SYS.INDEXES I
                        WHERE I.OBJECT_ID = T.OBJECT_ID AND I.INDEX_ID IN (0, 1)
                        ORDER BY I.INDEX_ID
                    ) IX
                    LEFT JOIN SYS.DATA_SPACES DS ON DS.DATA_SPACE_ID = IX.DATA_SPACE_ID
                    WHERE S.NAME = ?
                    ORDER BY T.NAME
                    """;
            this.printSql(sql);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, schema);
            ResultSet resultSet = statement.executeQuery();
            DBUtil.printMetaData(resultSet);
            while (resultSet.next()) {
                SqlServerTable table = new SqlServerTable();
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
                for (SqlServerTable table : tables) {
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

    //    public List<SqlServerTable> selectTablesSimple(String schema) {
    //        try {
    //            List<SqlServerTable> tables = new ArrayList<>();
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
    //                SqlServerTable table = new SqlServerTable();
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
    public List<SqlServerColumn> selectColumns(SqlServerSelectColumnParam param) {
        try {
            String schema = param.getSchema();
            String tableName = param.getTableName();
            SqlServerColumns columns = new SqlServerColumns();
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
                        C.NAME AS "Field",
                        TY.NAME +
                        CASE
                            WHEN TY.NAME IN ('varchar', 'char', 'varbinary', 'binary')
                                THEN '(' + CASE WHEN C.MAX_LENGTH = -1 THEN 'MAX'
                                    ELSE CAST(C.MAX_LENGTH AS VARCHAR(20)) END + ')'
                            WHEN TY.NAME IN ('nvarchar', 'nchar')
                                THEN '(' + CASE WHEN C.MAX_LENGTH = -1 THEN 'MAX'
                                    ELSE CAST(C.MAX_LENGTH / 2 AS VARCHAR(20)) END + ')'
                            WHEN TY.NAME IN ('decimal', 'numeric')
                                THEN '(' + CAST(C.PRECISION AS VARCHAR(20)) + ',' +
                                    CAST(C.SCALE AS VARCHAR(20)) + ')'
                            WHEN TY.NAME IN ('datetime2', 'datetimeoffset', 'time')
                                THEN '(' + CAST(C.SCALE AS VARCHAR(20)) + ')'
                            ELSE ''
                        END AS "Type",
                        CAST(EP.VALUE AS NVARCHAR(4000)) AS "Comment",
                        C.COLUMN_ID AS "POSITION",
                        DC.DEFINITION AS "Default",
                        C.IS_NULLABLE AS "NotNull",
                        CASE WHEN PK.COLUMN_ID IS NULL THEN '' ELSE 'PRI' END AS "Key",
                        C.IS_IDENTITY AS "IDENTITY",
                        C.IS_COMPUTED AS "GENERATED",
                        CC.DEFINITION AS "GENERATED_EXPRESSION",
                        CC.IS_PERSISTED AS "GENERATED_PERSISTED"
                    FROM SYS.COLUMNS C
                    JOIN SYS.TABLES TB ON TB.OBJECT_ID = C.OBJECT_ID
                    JOIN SYS.SCHEMAS S ON S.SCHEMA_ID = TB.SCHEMA_ID
                    JOIN SYS.TYPES TY ON TY.USER_TYPE_ID = C.USER_TYPE_ID
                    LEFT JOIN SYS.DEFAULT_CONSTRAINTS DC ON DC.PARENT_OBJECT_ID = C.OBJECT_ID
                        AND DC.PARENT_COLUMN_ID = C.COLUMN_ID
                    LEFT JOIN SYS.EXTENDED_PROPERTIES EP ON EP.MAJOR_ID = C.OBJECT_ID
                        AND EP.MINOR_ID = C.COLUMN_ID
                        AND EP.CLASS = 1
                        AND EP.NAME = 'MS_Description'
                    LEFT JOIN SYS.COMPUTED_COLUMNS CC ON CC.OBJECT_ID = C.OBJECT_ID
                        AND CC.COLUMN_ID = C.COLUMN_ID
                    LEFT JOIN (
                        SELECT IC.OBJECT_ID, IC.COLUMN_ID
                        FROM SYS.INDEX_COLUMNS IC
                        JOIN SYS.INDEXES I ON I.OBJECT_ID = IC.OBJECT_ID AND I.INDEX_ID = IC.INDEX_ID
                        WHERE I.IS_PRIMARY_KEY = 1
                    ) PK ON PK.OBJECT_ID = C.OBJECT_ID AND PK.COLUMN_ID = C.COLUMN_ID
                    WHERE S.NAME = ? AND TB.NAME = ?
                    ORDER BY C.COLUMN_ID
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
                boolean notNull = !resultSet.getBoolean("NotNull");
                String comment = resultSet.getString("Comment");
                boolean identity = resultSet.getBoolean("IDENTITY");
                boolean generated = resultSet.getBoolean("GENERATED");
                String generatedExpression = resultSet.getString("GENERATED_EXPRESSION");
                boolean generatedPersisted = resultSet.getBoolean("GENERATED_PERSISTED");
                // 自动递增
                boolean autoIncrement = identity;
                SqlServerColumn column = new SqlServerColumn();
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
                column.setGenerated(generated);
                if (StringUtil.isNotBlank(generatedExpression)) {
                    column.setGeneratedExpression(generatedExpression);
                    column.setGeneratedStorage(generatedPersisted ? "PERSISTED" : "VIRTUAL");
                }
                if (identity) {
                    column.setAutoIncrementType("IDENTITY");
                }
                columns.add(column);
            }
            IOUtil.close(resultSet);
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
    private void parseColumnType(SqlServerColumn column, String columnType) {
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
    private void initAutoIncrement(Connection connection, String schema, String name, SqlServerColumns columns, boolean isView) throws SQLException {
        if (isView) {
            this.initViewAutoIncrement(connection, schema, name, columns);
            return;
        }
        String sql = """
                SELECT C.NAME AS "COLUMN_NAME", C.IS_IDENTITY AS "IDENTITY"
                FROM SYS.COLUMNS C
                JOIN SYS.TABLES T ON T.OBJECT_ID = C.OBJECT_ID
                JOIN SYS.SCHEMAS S ON S.SCHEMA_ID = T.SCHEMA_ID
                WHERE S.NAME = ? AND T.NAME = ?
                """;
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, schema);
        statement.setString(2, name);
        ResultSet resultSet = statement.executeQuery();
        DBUtil.printMetaData(resultSet);
        while (resultSet.next()) {
            String columnName = resultSet.getString("COLUMN_NAME");
            String identity = resultSet.getString("IDENTITY");
            boolean autoIncrement = resultSet.getBoolean("IDENTITY");
            SqlServerColumn column = columns.column(columnName);
            if (column != null && autoIncrement) {
                column.setAutoIncrement(true);
                if (!isView && StringUtil.isBlank(column.getAutoIncrementType())) {
                    column.setAutoIncrementType("IDENTITY");
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
    private void initViewAutoIncrement(Connection connection, String schema, String viewName, SqlServerColumns columns) throws SQLException {
        String sql = """
                SELECT DISTINCT C.NAME AS "COLUMN_NAME"
                FROM SYS.COLUMNS C
                JOIN SYS.VIEWS V ON V.OBJECT_ID = C.OBJECT_ID
                JOIN SYS.SCHEMAS S ON S.SCHEMA_ID = V.SCHEMA_ID
                WHERE S.NAME = ? AND V.NAME = ? AND C.IS_IDENTITY = 1
                """;
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, schema);
        statement.setString(2, viewName);
        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
            SqlServerColumn column = columns.column(resultSet.getString("COLUMN_NAME"));
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
    public List<SqlServerRecord> selectRecords(SqlServerSelectRecordParam param) {
        try {
            Connection connection = this.getConnManager().connection();
            StringBuilder builder = new StringBuilder("SELECT * FROM ");
            builder.append(DBUtil.wrap(param.getSchema(), param.getTableName(), DBDialect.SQLSERVER));
            String filterCondition = DBUtil.buildCondition(param.getFilters());
            if (StringUtil.isNotBlank(filterCondition)) {
                builder.append(" WHERE ").append(filterCondition);
            }
            if (param.hasPageControl()) {
                builder.append(" ORDER BY (SELECT NULL) OFFSET ")
                        .append(param.getStart())
                        .append(" ROWS FETCH NEXT ")
                        .append(param.getLimit())
                        .append(" ROWS ONLY");
            }
            String sql = builder.toString();
            this.printSql(sql);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            DBUtil.printMetaData(resultSet);
            List<SqlServerRecord> records = new ArrayList<>();
            List<SqlServerColumn> columns;
            if (param.getColumns() != null) {
                columns = param.getColumns();
            } else {
                columns = ShellSqlServerHelper.parseColumns(resultSet);
            }
            while (resultSet.next()) {
                SqlServerRecord record = new SqlServerRecord(columns, param.isReadonly());
                for (SqlServerColumn column : columns) {
                    Object data = resultSet.getObject(column.getName());
                    data = ShellSqlServerDataUtil.valueStandardization(data);
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
    public long selectRecordCount(SqlServerSelectRecordParam param) {
        long count = 0;
        try {
            Connection connection = this.getConnManager().connection();
            StringBuilder builder = new StringBuilder("SELECT COUNT(*) FROM ");
            builder.append(DBUtil.wrap(param.getSchema(), param.getTableName(), DBDialect.SQLSERVER));
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
    public int insertRecord(SqlServerInsertRecordParam param) {
        if (param == null || param.getRecord() == null) {
            return 0;
        }
        try {
            DBRecordData recordData = param.getRecord();
            String tableName = DBUtil.wrap(param.getSchema(), param.getTableName(), DBDialect.SQLSERVER);
            // 需要插入的字段
            List<Map.Entry<DBColumn, Object>> insertValues = new ArrayList<>();
            for (Map.Entry<DBColumn, Object> entry : recordData.entries()) {
                if (this.skipInsertColumn(recordData, entry.getKey(), entry.getValue())) {
                    continue;
                }
                insertValues.add(entry);
            }
            SqlServerRecordPrimaryKey primaryKey = param.getPrimaryKey();
            StringBuilder builder = new StringBuilder("INSERT INTO ").append(tableName);
            if (insertValues.isEmpty()) {
                builder.append(" DEFAULT VALUES");
            } else {
                builder.append("(");
                for (Map.Entry<DBColumn, Object> entry : insertValues) {
                    builder.append(DBUtil.wrap(entry.getKey().getName(), DBDialect.SQLSERVER)).append(",");
                }
                StringUtil.deleteLast(builder);
                builder.append(")");
                builder.append(" VALUES(");
                for (Map.Entry<DBColumn, Object> entry : insertValues) {
                    builder.append(ShellSqlServerClient.valuePlaceholder(entry.getKey())).append(",");
                }
                StringUtil.deleteLast(builder);
                builder.append(")");
            }
            String sql = builder.toString();
            this.printSql(sql);
            Connection connection = this.getConnManager().connection();
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            int index = 1;
            for (Map.Entry<DBColumn, Object> entry : insertValues) {
                this.setParameter(statement, entry.getKey(), entry.getValue(), index++);
            }
            int count = statement.executeUpdate();
            Object newId = null;
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
     * @param column     字段
     * @param value      字段值
     * @return 结果
     */
    private boolean skipInsertColumn(DBRecordData recordData, DBColumn column, Object value) {
        if (column != null && column.isGenerated()) {
            return true;
        }
        if (column instanceof SqlServerColumn sqlServerColumn && sqlServerColumn.isRowVersion()) {
            return true;
        }
        if (recordData.isDefaultValueSequence(column.getName())) {
            return true;
        }
        if (column != null && column.isAutoIncrement()) {
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
    public int deleteRecord(SqlServerDeleteRecordParam param) {
        try {
            int updateCount;
            String schema = param.getSchema();
            String tableName = param.getTableName();
            Connection connection = this.getConnManager().connection();
            StringBuilder builder = new StringBuilder();
            builder.append("DELETE FROM ")
                    .append(DBUtil.wrap(schema, tableName, DBDialect.SQLSERVER))
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
                        builder.append(DBUtil.wrap(colName, DBDialect.SQLSERVER))
                                .append(" = ?");
                    } else {
                        builder.append(DBUtil.wrap(colName, DBDialect.SQLSERVER))
                                .append(" IS NULL");
                    }
                }
                String sql = builder.toString();
                this.printSql(sql);
                PreparedStatement statement = connection.prepareStatement(sql);
                int index = 1;
                // 设置参数
                for (String colName : recordData.notNullColumns()) {
                    DBColumn dbColumn = recordData.column(colName);
                    this.setParameter(statement, dbColumn, recordData.value(colName), index++);
                }
                updateCount = DBUtil.executeUpdate(statement);
            } else {
                SqlServerRecordPrimaryKey primaryKey = param.getPrimaryKey();
                builder.append(DBUtil.wrap(primaryKey.getColumnName(), DBDialect.SQLSERVER))
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
    public int updateRecord(SqlServerUpdateRecordParam param) {
        try {
            int updateCount;
            String schema = param.getSchema();
            String tableName = param.getTableName();
            DBRecordData recordData = param.getUpdateRecord();
            List<String> updateColumns = new ArrayList<>();
            for (String column : recordData.columns()) {
                DBColumn dbColumn = recordData.column(column);
                if (dbColumn == null || !dbColumn.isGenerated()) {
                    updateColumns.add(column);
                }
            }
            if (updateColumns.isEmpty()) {
                return 0;
            }
            StringBuilder builder = new StringBuilder();
            builder.append("UPDATE ")
                    .append(DBUtil.wrap(schema, tableName, DBDialect.SQLSERVER))
                    .append(" SET ");
            for (String column : updateColumns) {
                DBColumn dbColumn = recordData.column(column);
                builder.append(DBUtil.wrap(column, DBDialect.SQLSERVER))
                        .append(" = ").append(ShellSqlServerClient.valuePlaceholder(dbColumn)).append(",");
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
                        builder.append(DBUtil.wrap(column, DBDialect.SQLSERVER)).append(" IS NULL");
                    } else {
                        DBColumn dbColumn = originalRecordData.column(column);
                        builder.append(DBUtil.wrap(column, DBDialect.SQLSERVER))
                                .append(" = ").append(ShellSqlServerClient.valuePlaceholder(dbColumn));
                    }
                }
                int index = 1;
                String sql = builder.toString();
                this.printSql(sql);
                PreparedStatement statement = connection.prepareStatement(sql);
                // 设置值
                for (String colName : updateColumns) {
                    DBColumn dbColumn = recordData.column(colName);
                    this.setParameter(statement, dbColumn, recordData.value(colName), index++);
                }
                // 设置参数
                for (String colName : originalRecordData.columns()) {
                    Object value = originalRecordData.value(colName);
                    if (value != null) {
                        DBColumn dbColumn = originalRecordData.column(colName);
                        this.setParameter(statement, dbColumn, value, index++);
                    }
                }
                updateCount = DBUtil.executeUpdate(statement);
                IOUtil.close(statement);
            } else {
                SqlServerRecordPrimaryKey primaryKey = param.getPrimaryKey();
                builder.append(DBUtil.wrap(primaryKey.getColumnName(), DBDialect.SQLSERVER)).append(" = ?");
                String sql = builder.toString();
                DBUtil.printInfo(sql, recordData);
                PreparedStatement statement = connection.prepareStatement(sql);
                int index = 1;
                // 设置值
                for (String colName : updateColumns) {
                    DBColumn dbColumn = recordData.column(colName);
                    this.setParameter(statement, dbColumn, recordData.value(colName), index++);
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
     * 获取参数占位符。
     * 所有 SqlServer 参数都显式转换为列类型，避免 jsonpath、范围类型、枚举、
     * 域类型等被 JDBC 驱动当作 character varying 处理。
     *
     * @param column 字段
     * @return 占位符
     */
    static String valuePlaceholder(DBColumn column) {
        return "?";
    }

    /**
     * 设置SqlServer查询参数，JSON值传递为文本。
     *
     * @param statement 预处理语句
     * @param column    字段
     * @param value     值
     * @param index     参数索引
     * @throws SQLException 异常
     */
    private void setParameter(PreparedStatement statement, DBColumn column, Object value, int index) throws SQLException {
        if (value == null) {
            // Types.NULL会让SQL Server JDBC用后续参数推断NULL列类型，
            // 例如DATETIMEOFFSET为空时会把下一个VARBINARY误判成该列的类型。
            statement.setNull(index, ShellSqlServerClient.nullSqlType(column));
        } else if (column != null && column.supportBit()) {
            statement.setString(index, ShellSqlServerDataUtil.toBitText(column, value));
        } else if (column != null && column.supportJson()) {
            statement.setString(index, ShellSqlServerDataUtil.toJsonText(value));
        } else {
            DBUtil.setVal(statement, value, index);
        }
    }

    /**
     * 获取NULL参数对应的JDBC类型，避免驱动根据后续参数错误推断类型。
     *
     * @param column 字段
     * @return JDBC类型
     */
    static int nullSqlType(DBColumn column) {
        if (column == null) {
            return Types.VARCHAR;
        }
        String type = column.getType();
        if (column.supportBinary()) {
            return Types.VARBINARY;
        }
        if (column.supportTimestamp()) {
            return ShellSqlServerClient.isTimeZoneType(type)
                    ? Types.TIMESTAMP_WITH_TIMEZONE
                    : Types.TIMESTAMP;
        }
        if (column.isDateType()) {
            return Types.DATE;
        }
        if (column.isTimeType()) {
            return ShellSqlServerClient.isTimeZoneType(type)
                    ? Types.TIME_WITH_TIMEZONE
                    : Types.TIME;
        }
        if (column.supportBit() || StringUtil.containsIgnoreCase(type, "BIT")) {
            return Types.BIT;
        }
        if (column.supportBoolean()) {
            return Types.BOOLEAN;
        }
        if (column.supportDigits()) {
            return Types.DECIMAL;
        }
        if (column.supportInteger()) {
            if (StringUtil.containsIgnoreCase(type, "TINYINT")) {
                return Types.TINYINT;
            }
            if (StringUtil.containsIgnoreCase(type, "SMALLINT")) {
                return Types.SMALLINT;
            }
            if (StringUtil.containsIgnoreCase(type, "BIGINT")) {
                return Types.BIGINT;
            }
            return Types.INTEGER;
        }
        return Types.NVARCHAR;
    }

    private static boolean isTimeZoneType(String type) {
        return StringUtil.containsIgnoreCase(type, "TIME ZONE")
                || StringUtil.containsIgnoreCase(type, "TIMESTAMPTZ")
                || StringUtil.containsIgnoreCase(type, "DATETIMEOFFSET");
    }

    @Override
    public List<SqlServerTypeDefinition> selectTypes(String schema) {
        try {
            Connection connection = this.getConnManager().connection();
            String sql = """
                    SELECT
                        T.NAME AS TYPE_NAME,
                        S.NAME AS TYPE_SCHEMA,
                        B.NAME AS BASE_TYPE,
                        T.MAX_LENGTH AS MAX_LENGTH,
                        T.PRECISION AS PRECISION,
                        T.SCALE AS SCALE,
                        T.IS_NULLABLE AS IS_NULLABLE
                    FROM
                        SYS.TYPES T
                    JOIN
                        SYS.SCHEMAS S ON S.SCHEMA_ID = T.SCHEMA_ID
                    JOIN
                        SYS.TYPES B ON B.USER_TYPE_ID = T.SYSTEM_TYPE_ID
                    WHERE
                        S.NAME = ?
                    AND
                        T.IS_USER_DEFINED = 1
                    AND
                        T.IS_TABLE_TYPE = 0
                    AND
                        T.IS_ASSEMBLY_TYPE = 0
                    ORDER BY
                        T.NAME
                    """;
            this.printSql(sql);
            List<SqlServerTypeDefinition> types = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, schema);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        String typeName = DBUtil.wrap(schema,
                                resultSet.getString("TYPE_NAME"), DBDialect.SQLSERVER);
                        String baseType = formatSqlServerType(
                                resultSet.getString("BASE_TYPE"),
                                resultSet.getInt("MAX_LENGTH"),
                                resultSet.getInt("PRECISION"),
                                resultSet.getInt("SCALE"));
                        String nullable = resultSet.getBoolean("IS_NULLABLE") ? " NULL" : " NOT NULL";
                        SqlServerTypeDefinition type = new SqlServerTypeDefinition();
                        type.setName(resultSet.getString("TYPE_NAME"));
                        type.setObjectType(SqlServerTypeDefinition.OBJECT_TYPE_ALIAS);
                        type.setCreateDefinition("CREATE TYPE " + typeName
                                + " FROM " + baseType + nullable);
                        types.add(type);
                    }
                }
            }
            types.addAll(this.selectTableTypeDefinitions(connection, schema));
            return types;
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    private List<SqlServerTypeDefinition> selectTableTypeDefinitions(Connection connection, String schema) throws SQLException {
        String columnSql = """
                SELECT
                    TT.NAME AS TYPE_NAME,
                    C.NAME AS COLUMN_NAME,
                    TY.NAME AS COLUMN_TYPE,
                    TY.IS_USER_DEFINED AS USER_DEFINED_TYPE,
                    TYS.NAME AS COLUMN_TYPE_SCHEMA,
                    C.MAX_LENGTH AS MAX_LENGTH,
                    C.PRECISION AS PRECISION,
                    C.SCALE AS SCALE,
                    C.IS_NULLABLE AS IS_NULLABLE,
                    C.IS_IDENTITY AS IS_IDENTITY,
                    CAST(IC.SEED_VALUE AS NVARCHAR(100)) AS IDENTITY_SEED,
                    CAST(IC.INCREMENT_VALUE AS NVARCHAR(100)) AS IDENTITY_INCREMENT,
                    C.IS_COMPUTED AS IS_COMPUTED,
                    CC.DEFINITION AS COMPUTED_DEFINITION,
                    CC.IS_PERSISTED AS COMPUTED_PERSISTED,
                    DC.DEFINITION AS DEFAULT_DEFINITION
                FROM
                    SYS.TABLE_TYPES TT
                JOIN
                    SYS.SCHEMAS S ON S.SCHEMA_ID = TT.SCHEMA_ID
                JOIN
                    SYS.COLUMNS C ON C.OBJECT_ID = TT.TYPE_TABLE_OBJECT_ID
                JOIN
                    SYS.TYPES TY ON TY.USER_TYPE_ID = C.USER_TYPE_ID
                JOIN
                    SYS.SCHEMAS TYS ON TYS.SCHEMA_ID = TY.SCHEMA_ID
                LEFT JOIN
                    SYS.COMPUTED_COLUMNS CC
                    ON CC.OBJECT_ID = C.OBJECT_ID AND CC.COLUMN_ID = C.COLUMN_ID
                LEFT JOIN
                    SYS.DEFAULT_CONSTRAINTS DC
                    ON DC.PARENT_OBJECT_ID = C.OBJECT_ID AND DC.PARENT_COLUMN_ID = C.COLUMN_ID
                LEFT JOIN
                    SYS.IDENTITY_COLUMNS IC
                    ON IC.OBJECT_ID = C.OBJECT_ID AND IC.COLUMN_ID = C.COLUMN_ID
                WHERE
                    S.NAME = ?
                ORDER BY
                    TT.NAME, C.COLUMN_ID
                """;
        this.printSql(columnSql);
        Map<String, List<SqlServerTableTypeColumn>> columnsByType = new LinkedHashMap<>();
        try (PreparedStatement statement = connection.prepareStatement(columnSql)) {
            statement.setString(1, schema);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String columnType = resultSet.getString("COLUMN_TYPE");
                    if (resultSet.getBoolean("USER_DEFINED_TYPE")) {
                        columnType = DBUtil.wrap(resultSet.getString("COLUMN_TYPE_SCHEMA"),
                                columnType, DBDialect.SQLSERVER);
                    } else {
                        columnType = formatSqlServerType(
                                columnType,
                                resultSet.getInt("MAX_LENGTH"),
                                resultSet.getInt("PRECISION"),
                                resultSet.getInt("SCALE"));
                    }
                    SqlServerTableTypeColumn column = new SqlServerTableTypeColumn(
                            resultSet.getString("COLUMN_NAME"),
                            columnType,
                            resultSet.getString("DEFAULT_DEFINITION"),
                            resultSet.getBoolean("IS_NULLABLE"),
                            resultSet.getBoolean("IS_IDENTITY"),
                            resultSet.getString("IDENTITY_SEED"),
                            resultSet.getString("IDENTITY_INCREMENT"),
                            resultSet.getBoolean("IS_COMPUTED"),
                            resultSet.getString("COMPUTED_DEFINITION"),
                            resultSet.getBoolean("COMPUTED_PERSISTED")
                    );
                    columnsByType.computeIfAbsent(resultSet.getString("TYPE_NAME"),
                            key -> new ArrayList<>()).add(column);
                }
            }
        }
        if (columnsByType.isEmpty()) {
            return List.of();
        }

        String keySql = """
                SELECT
                    TT.NAME AS TYPE_NAME,
                    KC.NAME AS KEY_NAME,
                    KC.TYPE AS KEY_TYPE,
                    C.NAME AS COLUMN_NAME
                FROM
                    SYS.TABLE_TYPES TT
                JOIN
                    SYS.SCHEMAS S ON S.SCHEMA_ID = TT.SCHEMA_ID
                JOIN
                    SYS.KEY_CONSTRAINTS KC ON KC.PARENT_OBJECT_ID = TT.TYPE_TABLE_OBJECT_ID
                JOIN
                    SYS.INDEX_COLUMNS IC
                    ON IC.OBJECT_ID = KC.PARENT_OBJECT_ID AND IC.INDEX_ID = KC.UNIQUE_INDEX_ID
                JOIN
                    SYS.COLUMNS C ON C.OBJECT_ID = IC.OBJECT_ID AND C.COLUMN_ID = IC.COLUMN_ID
                WHERE
                    S.NAME = ?
                ORDER BY
                    TT.NAME, KC.NAME, IC.KEY_ORDINAL
                """;
        this.printSql(keySql);
        Map<String, List<SqlServerTableTypeKey>> keysByType = new LinkedHashMap<>();
        try (PreparedStatement statement = connection.prepareStatement(keySql)) {
            statement.setString(1, schema);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String typeName = resultSet.getString("TYPE_NAME");
                    String keyName = resultSet.getString("KEY_NAME");
                    String keyType = resultSet.getString("KEY_TYPE");
                    List<SqlServerTableTypeKey> keys = keysByType.computeIfAbsent(typeName,
                            key -> new ArrayList<>());
                    SqlServerTableTypeKey key = keys.stream()
                            .filter(item -> item.name().equalsIgnoreCase(keyName))
                            .findFirst()
                            .orElseGet(() -> {
                                SqlServerTableTypeKey created = new SqlServerTableTypeKey(
                                        keyName, keyType, new ArrayList<>());
                                keys.add(created);
                                return created;
                            });
                    key.columns().add(resultSet.getString("COLUMN_NAME"));
                }
            }
        }

        List<SqlServerTypeDefinition> definitions = new ArrayList<>();
        for (Map.Entry<String, List<SqlServerTableTypeColumn>> entry : columnsByType.entrySet()) {
            String typeName = entry.getKey();
            String wrappedType = DBUtil.wrap(schema, typeName, DBDialect.SQLSERVER);
            Set<String> primaryKeyColumns = new HashSet<>();
            for (SqlServerTableTypeKey key : keysByType.getOrDefault(typeName, List.of())) {
                if ("PK".equalsIgnoreCase(key.type())) {
                    primaryKeyColumns.addAll(key.columns());
                }
            }
            List<String> parts = new ArrayList<>();
            for (SqlServerTableTypeColumn column : entry.getValue()) {
                StringBuilder builder = new StringBuilder(DBUtil.wrap(column.name(), DBDialect.SQLSERVER));
                if (column.computed()) {
                    builder.append(" AS (").append(column.computedDefinition()).append(")");
                    if (column.computedPersisted()) {
                        builder.append(" PERSISTED");
                    }
                } else {
                    builder.append(" ").append(column.type());
                    if (column.identity()) {
                        builder.append(" IDENTITY(").append(column.identitySeed())
                                .append(",").append(column.identityIncrement()).append(")");
                    }
                    if (StringUtil.isNotBlank(column.defaultValue())) {
                        builder.append(" DEFAULT ").append(column.defaultValue());
                    }
                    boolean primaryKey = primaryKeyColumns.stream()
                            .anyMatch(name -> name.equalsIgnoreCase(column.name()));
                    builder.append(primaryKey || !column.nullable() ? " NOT NULL" : " NULL");
                }
                parts.add(builder.toString());
            }
            for (SqlServerTableTypeKey key : keysByType.getOrDefault(typeName, List.of())) {
                String keyword = "PK".equalsIgnoreCase(key.type()) ? "PRIMARY KEY" : "UNIQUE";
                parts.add(keyword + " (" + key.columns().stream()
                        .map(column -> DBUtil.wrap(column, DBDialect.SQLSERVER))
                        .collect(java.util.stream.Collectors.joining(",")) + ")");
            }
            SqlServerTypeDefinition definition = new SqlServerTypeDefinition();
            definition.setName(typeName);
            definition.setObjectType(SqlServerTypeDefinition.OBJECT_TYPE_TABLE);
            definition.setCreateDefinition("CREATE TYPE " + wrappedType + " AS TABLE ("
                    + String.join(", ", parts) + ")");
            definitions.add(definition);
        }
        return definitions;
    }

    private static String formatSqlServerType(String baseType, int maxLength, int precision, int scale) {
        String type = baseType.toUpperCase();
        return switch (type) {
            case "VARCHAR", "CHAR", "VARBINARY", "BINARY" ->
                    baseType + "(" + (maxLength == -1 ? "MAX" : maxLength) + ")";
            case "NVARCHAR", "NCHAR" ->
                    baseType + "(" + (maxLength == -1 ? "MAX" : maxLength / 2) + ")";
            case "DECIMAL", "NUMERIC" ->
                    baseType + "(" + precision + "," + scale + ")";
            case "DATETIME2", "DATETIMEOFFSET", "TIME" ->
                    baseType + "(" + scale + ")";
            case "FLOAT" ->
                    precision == 53 ? baseType : baseType + "(" + precision + ")";
            default -> baseType;
        };
    }

    private record SqlServerTableTypeColumn(String name, String type, String defaultValue,
                                            boolean nullable, boolean identity,
                                            String identitySeed, String identityIncrement,
                                            boolean computed, String computedDefinition,
                                            boolean computedPersisted) {
    }

    private record SqlServerTableTypeKey(String name, String type, List<String> columns) {
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
            String fullName = DBUtil.wrap(schema, tableName, DBDialect.SQLSERVER);
            List<String> definitions = new ArrayList<>();
            List<String> comments = new ArrayList<>();
            String columnSql = """
                    SELECT
                        C.NAME AS FIELD,
                        TY.NAME +
                        CASE
                            WHEN TY.NAME IN ('varchar','char','varbinary','binary')
                                THEN '(' + CASE WHEN C.MAX_LENGTH = -1 THEN 'MAX'
                                    ELSE CAST(C.MAX_LENGTH AS VARCHAR(20)) END + ')'
                            WHEN TY.NAME IN ('nvarchar','nchar')
                                THEN '(' + CASE WHEN C.MAX_LENGTH = -1 THEN 'MAX'
                                    ELSE CAST(C.MAX_LENGTH / 2 AS VARCHAR(20)) END + ')'
                            WHEN TY.NAME IN ('decimal','numeric')
                                THEN '(' + CAST(C.PRECISION AS VARCHAR(20)) + ',' +
                                    CAST(C.SCALE AS VARCHAR(20)) + ')'
                            WHEN TY.NAME IN ('datetime2','datetimeoffset','time')
                                THEN '(' + CAST(C.SCALE AS VARCHAR(20)) + ')'
                            ELSE ''
                        END AS TYPE_NAME,
                        DC.DEFINITION AS DEFAULT_VALUE,
                        C.IS_NULLABLE AS NULLABLE,
                        C.IS_IDENTITY AS IDENTITY_COLUMN,
                        IC.SEED_VALUE AS IDENTITY_SEED,
                        IC.INCREMENT_VALUE AS IDENTITY_INCREMENT,
                        C.IS_COMPUTED AS GENERATED_COLUMN,
                        CC.DEFINITION AS GENERATED_EXPRESSION,
                        CC.IS_PERSISTED AS GENERATED_PERSISTED,
                        TY.IS_USER_DEFINED AS USER_DEFINED_TYPE,
                        TYS.NAME AS TYPE_SCHEMA,
                        CAST(EP.VALUE AS NVARCHAR(4000)) AS COLUMN_COMMENT
                    FROM SYS.COLUMNS C
                    JOIN SYS.TABLES TB ON TB.OBJECT_ID = C.OBJECT_ID
                    JOIN SYS.SCHEMAS S ON S.SCHEMA_ID = TB.SCHEMA_ID
                    JOIN SYS.TYPES TY ON TY.USER_TYPE_ID = C.USER_TYPE_ID
                    JOIN SYS.SCHEMAS TYS ON TYS.SCHEMA_ID = TY.SCHEMA_ID
                    LEFT JOIN SYS.DEFAULT_CONSTRAINTS DC
                        ON DC.PARENT_OBJECT_ID = C.OBJECT_ID AND DC.PARENT_COLUMN_ID = C.COLUMN_ID
                    LEFT JOIN SYS.COMPUTED_COLUMNS CC
                        ON CC.OBJECT_ID = C.OBJECT_ID AND CC.COLUMN_ID = C.COLUMN_ID
                    LEFT JOIN SYS.IDENTITY_COLUMNS IC
                        ON IC.OBJECT_ID = C.OBJECT_ID AND IC.COLUMN_ID = C.COLUMN_ID
                    LEFT JOIN SYS.EXTENDED_PROPERTIES EP
                        ON EP.MAJOR_ID = C.OBJECT_ID AND EP.MINOR_ID = C.COLUMN_ID
                        AND EP.CLASS = 1 AND EP.NAME = 'MS_Description'
                    WHERE S.NAME = ? AND TB.NAME = ?
                    ORDER BY C.COLUMN_ID
                    """;
            this.printSql(columnSql);
            try (PreparedStatement statement = connection.prepareStatement(columnSql)) {
                statement.setString(1, schema);
                statement.setString(2, tableName);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        String field = resultSet.getString("FIELD");
                        StringBuilder builder = new StringBuilder("  ")
                                .append(DBUtil.wrap(field, DBDialect.SQLSERVER));
                        if (resultSet.getBoolean("GENERATED_COLUMN")) {
                            builder.append(" AS (").append(resultSet.getString("GENERATED_EXPRESSION")).append(")");
                            if (resultSet.getBoolean("GENERATED_PERSISTED")) {
                                builder.append(" PERSISTED");
                            }
                        } else {
                            String typeName = resultSet.getString("TYPE_NAME");
                            if (resultSet.getBoolean("USER_DEFINED_TYPE")) {
                                typeName = DBUtil.wrap(resultSet.getString("TYPE_SCHEMA"),
                                        typeName, DBDialect.SQLSERVER);
                            }
                            builder.append(" ").append(typeName);
                            if (resultSet.getBoolean("IDENTITY_COLUMN")) {
                                builder.append(" IDENTITY(")
                                        .append(resultSet.getBigDecimal("IDENTITY_SEED")
                                                .stripTrailingZeros().toPlainString())
                                        .append(",")
                                        .append(resultSet.getBigDecimal("IDENTITY_INCREMENT")
                                                .stripTrailingZeros().toPlainString())
                                        .append(")");
                            }
                            Object defaultValue = resultSet.getObject("DEFAULT_VALUE");
                            if (defaultValue != null) {
                                builder.append(" DEFAULT ").append(defaultValue);
                            }
                            builder.append(resultSet.getBoolean("NULLABLE") ? " NULL" : " NOT NULL");
                        }
                        definitions.add(builder.toString());
                        String comment = resultSet.getString("COLUMN_COMMENT");
                        if (StringUtil.isNotBlank(comment)) {
                            comments.add(this.extendedPropertySql(schema, tableName, field, comment));
                        }
                    }
                }
            }
            String keySql = """
                    SELECT KC.NAME, KC.TYPE, C.NAME AS COLUMN_NAME, IC.KEY_ORDINAL
                    FROM SYS.KEY_CONSTRAINTS KC
                    JOIN SYS.TABLES T ON T.OBJECT_ID = KC.PARENT_OBJECT_ID
                    JOIN SYS.SCHEMAS S ON S.SCHEMA_ID = T.SCHEMA_ID
                    JOIN SYS.INDEX_COLUMNS IC ON IC.OBJECT_ID = T.OBJECT_ID
                        AND IC.INDEX_ID = KC.UNIQUE_INDEX_ID
                    JOIN SYS.COLUMNS C ON C.OBJECT_ID = IC.OBJECT_ID AND C.COLUMN_ID = IC.COLUMN_ID
                    WHERE S.NAME = ? AND T.NAME = ? AND KC.TYPE IN ('PK','UQ')
                    ORDER BY KC.NAME, IC.KEY_ORDINAL
                    """;
            Map<String, List<String>> keyColumns = new LinkedHashMap<>();
            Map<String, String> keyTypes = new LinkedHashMap<>();
            try (PreparedStatement statement = connection.prepareStatement(keySql)) {
                statement.setString(1, schema);
                statement.setString(2, tableName);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        String name = resultSet.getString("NAME");
                        keyTypes.put(name, resultSet.getString("TYPE"));
                        keyColumns.computeIfAbsent(name, key -> new ArrayList<>())
                                .add(resultSet.getString("COLUMN_NAME"));
                    }
                }
            }
            for (Map.Entry<String, List<String>> entry : keyColumns.entrySet()) {
                String type = keyTypes.get(entry.getKey());
                definitions.add("  CONSTRAINT " + DBUtil.wrap(entry.getKey(), DBDialect.SQLSERVER)
                        + ("PK".equals(type) ? " PRIMARY KEY (" : " UNIQUE (")
                        + entry.getValue().stream().map(column ->
                        DBUtil.wrap(column, DBDialect.SQLSERVER)).collect(java.util.stream.Collectors.joining(","))
                        + ")");
            }
            for (SqlServerForeignKey foreignKey : this.selectForeignKeys(schema, tableName)) {
                StringBuilder builder = new StringBuilder("  CONSTRAINT ")
                        .append(DBUtil.wrap(foreignKey.getName(), DBDialect.SQLSERVER))
                        .append(" FOREIGN KEY (")
                        .append(foreignKey.getColumns().stream().map(column ->
                                DBUtil.wrap(column, DBDialect.SQLSERVER))
                                .collect(java.util.stream.Collectors.joining(",")))
                        .append(") REFERENCES ")
                        .append(DBUtil.wrap(foreignKey.getReferencedDatabase(),
                                foreignKey.getReferencedTable(), DBDialect.SQLSERVER))
                        .append(" (")
                        .append(foreignKey.getReferencedColumns().stream().map(column ->
                                DBUtil.wrap(column, DBDialect.SQLSERVER))
                                .collect(java.util.stream.Collectors.joining(",")))
                        .append(")");
                if (StringUtil.isNotBlank(foreignKey.getDeletePolicy())) {
                    builder.append(" ON DELETE ").append(foreignKey.getDeletePolicy());
                }
                definitions.add(builder.toString());
            }
            for (SqlServerCheck check : this.selectChecks(schema, tableName)) {
                if (StringUtil.isNotBlank(check.getClause())) {
                    definitions.add("  CONSTRAINT " + DBUtil.wrap(check.getName(), DBDialect.SQLSERVER)
                            + " CHECK (" + check.getClause() + ")");
                }
            }
            StringBuilder result = new StringBuilder("CREATE TABLE ").append(fullName).append(" (\n")
                    .append(String.join(",\n", definitions)).append("\n);");
            String tableComment = this.selectTableComment(schema, tableName);
            if (StringUtil.isNotBlank(tableComment)) {
                result.append("\n").append(this.extendedPropertySql(schema, tableName, null, tableComment));
            }
            comments.forEach(comment -> result.append("\n").append(comment));
            for (SqlServerIndex index : this.indexes(schema, tableName)) {
                if (index.getColumns() == null || index.getColumns().isEmpty()) {
                    continue;
                }
                result.append("\nCREATE ");
                if (StringUtil.equalsIgnoreCase(index.getType(), "UNIQUE")) {
                    result.append("UNIQUE ");
                }
                if (StringUtil.isNotBlank(index.getMethod())) {
                    result.append(index.getMethod()).append(" ");
                }
                result.append("INDEX ").append(DBUtil.wrap(index.getName(), DBDialect.SQLSERVER))
                        .append(" ON ").append(fullName).append(" (")
                        .append(index.getColumns().stream().map(column ->
                                DBUtil.wrap(column.getColumnName(), DBDialect.SQLSERVER))
                                .collect(java.util.stream.Collectors.joining(",")))
                        .append(");");
            }
            return result.toString();
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    private String showCreateTableLegacy(String schema, String tableName) {
        try {
            Connection connection = this.getConnManager().connection();
            String fullName = DBUtil.wrap(schema, tableName, DBDialect.SQLSERVER);
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
                        A.ATTGENERATED AS "GENERATED",
                        PG_CATALOG.PG_GET_SERIAL_SEQUENCE(
                            PG_CATALOG.QUOTE_IDENT(N.NSPNAME) || '.' || PG_CATALOG.QUOTE_IDENT(C.RELNAME),
                            A.ATTNAME
                        ) AS "SERIAL_SEQUENCE",
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
                String generated = resultSet.getString("GENERATED");
                String serialSequence = resultSet.getString("SERIAL_SEQUENCE");
                String comment = resultSet.getString("Comment");
                // 旧式serial列由物理整型和nextval默认值组成，导出时还原为SERIAL伪类型
                boolean generatedColumn = StringUtil.isNotBlank(generated);
                String serialType = !generatedColumn && StringUtil.isBlank(identity) && this.isNextValDefault(defaultValue)
                        && StringUtil.isNotBlank(serialSequence)
                        ? ShellSqlServerColumnUtil.serialTypeFor(type)
                        : null;
                boolean serial = serialType != null;
                StringBuilder builder = new StringBuilder("  ");
                builder.append(DBUtil.wrap(field, DBDialect.SQLSERVER))
                        .append(" ").append(serial ? serialType : type);
                if (generatedColumn) {
                    builder.append(" GENERATED ALWAYS AS (").append(defaultValue).append(") ")
                            .append(StringUtil.equalsIgnoreCase(generated, "v") ? "VIRTUAL" : "STORED");
                } else if (StringUtil.isNotBlank(defaultValue) && !serial) {
                    builder.append(" DEFAULT ").append(defaultValue);
                }
                if (notNull) {
                    builder.append(" NOT NULL");
                }
                if (!generatedColumn && !serial && StringUtil.equalsIgnoreCase(identity, "a")) {
                    builder.append(" GENERATED ALWAYS AS IDENTITY");
                } else if (!generatedColumn && !serial && StringUtil.equalsIgnoreCase(identity, "d")) {
                    builder.append(" GENERATED BY DEFAULT AS IDENTITY");
                }
                definitions.add(builder.toString());
                if (StringUtil.isNotBlank(comment)) {
                    comments.add("COMMENT ON COLUMN " + fullName + "."
                            + DBUtil.wrap(field, DBDialect.SQLSERVER) + " IS " + this.quoteLiteral(comment) + ";");
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
                builder.append(DBUtil.wrap(primaryKeyName, DBDialect.SQLSERVER)).append(" PRIMARY KEY (");
                for (String primaryKey : primaryKeys) {
                    builder.append(DBUtil.wrap(primaryKey, DBDialect.SQLSERVER)).append(",");
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
                builder.append(DBUtil.wrap(entry.getKey(), DBDialect.SQLSERVER)).append(" UNIQUE (");
                for (String column : entry.getValue()) {
                    builder.append(DBUtil.wrap(column, DBDialect.SQLSERVER)).append(",");
                }
                StringUtil.deleteLast(builder, ",");
                builder.append(")");
                definitions.add(builder.toString());
            }
            // 外键定义
            List<SqlServerForeignKey> foreignKeys = this.selectForeignKeys(schema, tableName);
            if (foreignKeys != null) {
                for (SqlServerForeignKey foreignKey : foreignKeys) {
                    StringBuilder builder = new StringBuilder("  CONSTRAINT ");
                    builder.append(DBUtil.wrap(foreignKey.getName(), DBDialect.SQLSERVER)).append(" FOREIGN KEY (");
                    for (String column : foreignKey.getColumns()) {
                        builder.append(DBUtil.wrap(column, DBDialect.SQLSERVER)).append(",");
                    }
                    StringUtil.deleteLast(builder, ",");
                    builder.append(") REFERENCES ")
                            .append(DBUtil.wrap(foreignKey.getReferencedDatabase(), foreignKey.getReferencedTable(), DBDialect.SQLSERVER))
                            .append(" (");
                    for (String column : foreignKey.getReferencedColumns()) {
                        builder.append(DBUtil.wrap(column, DBDialect.SQLSERVER)).append(",");
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
            List<SqlServerCheck> checks = this.selectChecks(schema, tableName);
            if (checks != null) {
                for (SqlServerCheck check : checks) {
                    if (StringUtil.isBlank(check.getClause())) {
                        continue;
                    }
                    definitions.add("  CONSTRAINT " + DBUtil.wrap(check.getName(), DBDialect.SQLSERVER)
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
            List<SqlServerIndex> indexes = this.indexes(schema, tableName);
            if (indexes != null) {
                for (SqlServerIndex index : indexes) {
                    if (index == null || index.getColumns() == null || index.getColumns().isEmpty()) {
                        continue;
                    }
                    builder.append("\n\nCREATE ");
                    if ("UNIQUE".equalsIgnoreCase(index.getType())) {
                        builder.append("UNIQUE ");
                    }
                    builder.append("INDEX ").append(DBUtil.wrap(index.getName(), DBDialect.SQLSERVER))
                            .append(" ON ").append(fullName);
                    if (StringUtil.isNotBlank(index.getMethod())) {
                        builder.append(" USING ").append(index.getMethod());
                    }
                    builder.append(" (");
                    for (SqlServerIndex.IndexColumn column : index.getColumns()) {
                        builder.append(DBUtil.wrap(column.getColumnName(), DBDialect.SQLSERVER)).append(",");
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
                SELECT CAST(EP.VALUE AS NVARCHAR(4000)) AS TABLE_COMMENT
                FROM SYS.TABLES T
                JOIN SYS.SCHEMAS S ON S.SCHEMA_ID = T.SCHEMA_ID
                LEFT JOIN SYS.EXTENDED_PROPERTIES EP
                    ON EP.MAJOR_ID = T.OBJECT_ID AND EP.MINOR_ID = 0
                    AND EP.CLASS = 1 AND EP.NAME = 'MS_Description'
                WHERE S.NAME = ? AND T.NAME = ?
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

    private String extendedPropertySql(String schema, String tableName, String columnName, Object value) {
        String sql = "EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value="
                + "N" + DBUtil.wrapString(String.valueOf(value), DBDialect.SQLSERVER)
                + ", @level0type=N'SCHEMA', @level0name="
                + "N" + DBUtil.wrapString(schema, DBDialect.SQLSERVER)
                + ", @level1type=N'TABLE', @level1name="
                + "N" + DBUtil.wrapString(tableName, DBDialect.SQLSERVER);
        if (StringUtil.isNotBlank(columnName)) {
            sql += ", @level2type=N'COLUMN', @level2name="
                    + "N" + DBUtil.wrapString(columnName, DBDialect.SQLSERVER);
        }
        return sql + ";";
    }

    private String moduleDefinition(String types, String schema, String name) {
        try {
            Connection connection = this.getConnManager().connection();
            String typeCondition = java.util.Arrays.stream(types.split(","))
                    .map(type -> "'" + type.trim().replace("'", "''") + "'")
                    .collect(java.util.stream.Collectors.joining(","));
            String sql = "SELECT M.DEFINITION FROM SYS.OBJECTS O "
                    + "JOIN SYS.SCHEMAS S ON S.SCHEMA_ID = O.SCHEMA_ID "
                    + "JOIN SYS.SQL_MODULES M ON M.OBJECT_ID = O.OBJECT_ID "
                    + "WHERE S.NAME = ? AND O.NAME = ? AND O.TYPE IN (" + typeCondition + ")";
            this.printSql(sql);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, schema);
                statement.setString(2, name);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next() ? resultSet.getString(1) : "";
                }
            }
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    /**
     * 查询建视图语句
     *
     * @param schema   模式名称
     * @param viewName 视图名称
     * @return 建视图语句
     */
    public String showCreateView(String schema, String viewName) {
        return this.moduleDefinition("V", schema, viewName);
    }

    /**
     * 查询建函数语句
     *
     * @param schema       模式名称
     * @param functionName 函数名称
     * @return 建函数语句
     */
    public String showCreateFunction(String schema, String functionName) {
        String definition = this.moduleDefinition("FN,IF,TF,FS,FT,AF", schema, functionName);
        return StringUtil.isBlank(definition) ? "" : definition;
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
                        MINIMUM_VALUE AS MIN_VALUE,
                        MAXIMUM_VALUE AS MAX_VALUE,
                        INCREMENT AS INCREMENT_BY,
                        CACHE_SIZE,
                        IS_CYCLING AS CYCLE
                    FROM SYS.SEQUENCES Q
                    JOIN SYS.SCHEMAS S ON S.SCHEMA_ID = Q.SCHEMA_ID
                    WHERE S.NAME = ? AND Q.NAME = ?
                    """;
            this.printSql(sql);
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, schema);
            stmt.setString(2, sequenceName);
            ResultSet resultSet = stmt.executeQuery();
            StringBuilder builder = new StringBuilder();
            if (resultSet.next()) {
                BigInteger startValue = ShellSequenceUtil.getBigInteger(resultSet, "START_VALUE");
                BigInteger minValue = ShellSequenceUtil.getBigInteger(resultSet, "MIN_VALUE");
                BigInteger maxValue = ShellSequenceUtil.getBigInteger(resultSet, "MAX_VALUE");
                BigInteger incrementBy = ShellSequenceUtil.getBigInteger(resultSet, "INCREMENT_BY");
                BigInteger cacheSize = ShellSequenceUtil.getBigInteger(resultSet, "CACHE_SIZE");
                boolean cycle = resultSet.getBoolean("CYCLE");
                builder.append("CREATE SEQUENCE ")
                        .append(DBUtil.wrap(schema, sequenceName, DBDialect.SQLSERVER));
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
     * 查询建过程语句
     *
     * @param schema        模式名称
     * @param procedureName 过程名称
     * @return 建过程语句
     */
    public String showCreateProcedure(String schema, String procedureName) {
        return this.moduleDefinition("P", schema, procedureName);
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
                        M.DEFINITION AS "DEFINITION"
                    FROM SYS.TRIGGERS TR
                    JOIN SYS.TABLES TB ON TB.OBJECT_ID = TR.PARENT_ID
                    JOIN SYS.SCHEMAS S ON S.SCHEMA_ID = TB.SCHEMA_ID
                    JOIN SYS.SQL_MODULES M ON M.OBJECT_ID = TR.OBJECT_ID
                    WHERE S.NAME = ? AND TR.NAME = ?
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
            if (StringUtil.isNotBlank(createDefinition)) {
                createDefinition = createDefinition.stripTrailing().replaceFirst(";+$", "") + ";";
            }
            return createDefinition;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    private String sqlServerTriggerBody(String definition) {
        if (StringUtil.isBlank(definition)) {
            return "";
        }
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("(?is)\\bAS\\s+(.*)$")
                .matcher(definition);
        String body = matcher.find() ? matcher.group(1).stripTrailing() : definition.stripTrailing();
        return body.replaceFirst(";+$", ";");
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
            sql = "SELECT NAME FROM SYS.FILEGROUPS ORDER BY NAME";
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
    public List<SqlServerSchema> selectSchemas() {
        try {
            Statement statement = this.getConnManager().connection().createStatement();
            // 查询当前用户有权限访问的schema：优先显示有对象的schema，也包含当前用户自己的schema
            //            String sql = """
            //                    SELECT DISTINCT OWNER FROM ALL_OBJECTS
            //                    UNION
            //                    SELECT USERNAME FROM ALL_USERS
            //                    ORDER BY 1
            //                    """;

            List<SqlServerSchema> list = new ArrayList<>();
            String sql = """
                    SELECT SCHEMA_ID, NAME
                    FROM SYS.SCHEMAS
                    ORDER BY NAME
                    """;
            this.printSql(sql);
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                SqlServerSchema schema = new SqlServerSchema();
                schema.setSchemaId(resultSet.getInt(1));
                String name = resultSet.getString(2);
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
     * 一次查询当前数据库的查询补全对象。
     * 使用独立连接，避免后台建立索引时阻塞数据库树使用的连接。
     *
     * @return 表和视图等对象索引项
     */
    public List<SqlServerQueryObject> selectQueryObjects() {
        String sql = """
                SELECT S.NAME AS SCHEMA_NAME, O.NAME AS OBJECT_NAME, O.TYPE AS OBJECT_TYPE
                FROM SYS.OBJECTS O
                JOIN SYS.SCHEMAS S ON S.SCHEMA_ID = O.SCHEMA_ID
                WHERE O.IS_MS_SHIPPED = 0
                  AND O.TYPE IN ('U','V','FN','IF','TF','FS','FT','AF','P')
                  AND S.NAME NOT IN (
                      'sys', 'INFORMATION_SCHEMA', 'guest',
                      'db_owner', 'db_accessadmin', 'db_backupoperator',
                      'db_datareader', 'db_datawriter', 'db_ddladmin',
                      'db_denydatareader', 'db_denydatawriter', 'db_securityadmin'
                  )
                ORDER BY S.NAME, O.NAME, O.TYPE
                """;
        this.printSql(sql);
        try (Connection connection = this.getConnManager()
                .newConnection(this.shellConnect.getDatabase());
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<SqlServerQueryObject> objects = new ArrayList<>();
            while (resultSet.next()) {
                objects.add(new SqlServerQueryObject(
                        resultSet.getString("SCHEMA_NAME"),
                        resultSet.getString("OBJECT_NAME"),
                        resultSet.getString("OBJECT_TYPE").strip()
                ));
            }
            return objects;
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    /**
     * 查询模式信息
     *
     * @param schema 模式名称
     * @return 模式对象
     */
    public SqlServerSchema schema(String schema) {
        try {
            SqlServerSchema dbSchema = new SqlServerSchema();
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
    public SqlServerTable selectTable(String schema, String tableName) {
        SqlServerSelectTableParam param = new SqlServerSelectTableParam();
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
    public SqlServerTable selectTableSimple(String schema, String tableName) {
        SqlServerSelectTableParam param = new SqlServerSelectTableParam();
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
    public SqlServerTable selectTable(SqlServerSelectTableParam param) {
        try {
            String schema = param.getSchema();
            String tableName = param.getTableName();
            SqlServerTable table = new SqlServerTable();
            table.setSchema(schema);
            table.setName(tableName);
            Connection connection = this.getConnManager().connection();
            String sql = """
                    SELECT
                        CAST(EP.VALUE AS NVARCHAR(4000)) AS "TABLE_COMMENT",
                        DS.NAME AS "TABLE_SPACE"
                    FROM SYS.TABLES T
                    JOIN SYS.SCHEMAS S ON S.SCHEMA_ID = T.SCHEMA_ID
                    LEFT JOIN SYS.EXTENDED_PROPERTIES EP
                        ON EP.MAJOR_ID = T.OBJECT_ID AND EP.MINOR_ID = 0
                        AND EP.CLASS = 1 AND EP.NAME = 'MS_Description'
                    OUTER APPLY (
                        SELECT TOP 1 I.DATA_SPACE_ID
                        FROM SYS.INDEXES I
                        WHERE I.OBJECT_ID = T.OBJECT_ID AND I.INDEX_ID IN (0, 1)
                        ORDER BY I.INDEX_ID
                    ) IX
                    LEFT JOIN SYS.DATA_SPACES DS ON DS.DATA_SPACE_ID = IX.DATA_SPACE_ID
                    WHERE S.NAME = ? AND T.NAME = ?
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
    public SqlServerView selectView(String schema, String viewName) {
        SqlServerSelectViewParam param = new SqlServerSelectViewParam();
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
    public SqlServerView selectViewSimple(String schema, String viewName) {
        SqlServerSelectViewParam param = new SqlServerSelectViewParam();
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
    public SqlServerView selectView(SqlServerSelectViewParam param) {
        try {
            String schema = param.getSchema();
            String viewName = param.getViewName();
            String sql;
            sql = """
                    SELECT
                        V.NAME AS "VIEW_NAME",
                        CAST(NULL AS NCHAR(1)) AS "READ_ONLY",
                        M.DEFINITION AS "DEFINITION",
                        CAST(EP.VALUE AS NVARCHAR(4000)) AS "TABLE_COMMENT"
                    FROM SYS.VIEWS V
                    JOIN SYS.SCHEMAS S ON S.SCHEMA_ID = V.SCHEMA_ID
                    JOIN SYS.SQL_MODULES M ON M.OBJECT_ID = V.OBJECT_ID
                    LEFT JOIN SYS.EXTENDED_PROPERTIES EP ON EP.MAJOR_ID = V.OBJECT_ID
                        AND EP.MINOR_ID = 0 AND EP.CLASS = 1 AND EP.NAME = 'MS_Description'
                    WHERE S.NAME = ? AND V.NAME = ?
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
            SqlServerView view = new SqlServerView();
            while (resultSet.next()) {
                String name = resultSet.getString("VIEW_NAME");
                String readOnly = resultSet.getString("READ_ONLY");
                String definition = resultSet.getString("DEFINITION");
                String tableComment = resultSet.getString("TABLE_COMMENT");
                if (readOnly == null) {
                    readOnly = ShellSqlServerHelper.getViewReadOnly(connection, schema, viewName);
                }
                if (readOnly == null) {
                    // SqlServer视图默认不可更新
                    readOnly = "Y";
                }
                view.setName(name);
                view.setSchema(schema);
                view.setComment(tableComment);
                view.setDefinition(ShellSqlServerHelper.normalizeViewDefinition(definition));
                view.setUpdatable(!StringUtil.equalsIgnoreCase("Y", readOnly));
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
    public List<SqlServerView> selectViews(String schema) {
        SqlServerSelectViewParam param = new SqlServerSelectViewParam();
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
    public List<SqlServerView> selectViewsSimple(String schema) {
        SqlServerSelectViewParam param = new SqlServerSelectViewParam();
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
    public List<SqlServerView> selectViews(SqlServerSelectViewParam param) {
        try {
            String schema = param.getSchema();
            List<SqlServerView> list = new ArrayList<>();
            String sql = """
                    SELECT
                        V.NAME AS "VIEW_NAME",
                        M.DEFINITION AS "DEFINITION",
                        CAST(EP.VALUE AS NVARCHAR(4000)) AS "TABLE_COMMENT"
                    FROM SYS.VIEWS V
                    JOIN SYS.SCHEMAS S ON S.SCHEMA_ID = V.SCHEMA_ID
                    JOIN SYS.SQL_MODULES M ON M.OBJECT_ID = V.OBJECT_ID
                    LEFT JOIN SYS.EXTENDED_PROPERTIES EP ON EP.MAJOR_ID = V.OBJECT_ID
                        AND EP.MINOR_ID = 0 AND EP.CLASS = 1 AND EP.NAME = 'MS_Description'
                    WHERE S.NAME = ?
                    ORDER BY V.NAME
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
                SqlServerView view = new SqlServerView();
                String name = resultSet.getString("VIEW_NAME");
                String definition = resultSet.getString("DEFINITION");
                String tableComment = resultSet.getString("TABLE_COMMENT");
                view.setSchema(schema);
                view.setName(name);
                view.setComment(tableComment);
                view.setDefinition(ShellSqlServerHelper.normalizeViewDefinition(definition));
                list.add(view);
            }
            // 关闭连接和释放资源
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            // 完整信息才生成建视图语句
            if (param.isFull()) {
                for (SqlServerView view : list) {
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
     * 判断视图是否可更新。
     *
     * @param schema   模式名称
     * @param viewName 视图名称
     * @return 可更新返回true
     */
    public boolean isViewUpdatable(String schema, String viewName) {
        try {
            return ShellSqlServerHelper.isViewUpdatable(
                    this.getConnManager().connection(), schema, viewName);
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    /**
     * 删除视图
     *
     * @param view 视图
     */
    public void dropView(SqlServerView view) {
        if (view == null || StringUtil.isBlank(view.getSchema()) || StringUtil.isBlank(view.getName())) {
            return;
        }
        try {
            String sql = "DROP VIEW IF EXISTS " + DBUtil.wrap(view.getSchema(), view.getName(), DBDialect.SQLSERVER);
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
                    SELECT COUNT(*)
                    FROM SYS.VIEWS V
                    JOIN SYS.SCHEMAS S ON S.SCHEMA_ID = V.SCHEMA_ID
                    WHERE S.NAME = ? AND V.NAME = ?
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
    public void createView(SqlServerCreateViewParam param) {
        Connection connection = null;
        boolean autoCommit = true;
        Statement statement = null;
        try {
            List<String> sqlList = SqlServerViewCreateSqlGenerator.generateSql(param);
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
    public void alertView(SqlServerAlertViewParam param) {
        Connection connection = null;
        boolean autoCommit = true;
        Statement statement = null;
        try {
            List<String> sqlList = SqlServerViewAlertSqlGenerator.generateSql(param);
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
    public List<SqlServerIndex> indexes(String schema, String tableName) {
        try {
            Connection connection = this.getConnManager().connection();
            String sql = """
                    SELECT
                        IX.NAME AS "Key_name",
                        A.NAME AS "Column_name",
                        K.KEY_ORDINAL AS "Seq_in_index",
                        CASE WHEN IX.IS_UNIQUE = 1 THEN 0 ELSE 1 END AS "Non_unique",
                        IX.TYPE_DESC AS "Index_type"
                    FROM SYS.INDEXES IX
                    JOIN SYS.TABLES TB ON TB.OBJECT_ID = IX.OBJECT_ID
                    JOIN SYS.SCHEMAS S ON S.SCHEMA_ID = TB.SCHEMA_ID
                    JOIN SYS.INDEX_COLUMNS K ON K.OBJECT_ID = IX.OBJECT_ID AND K.INDEX_ID = IX.INDEX_ID
                    JOIN SYS.COLUMNS A ON A.OBJECT_ID = K.OBJECT_ID AND A.COLUMN_ID = K.COLUMN_ID
                    JOIN SYS.DATA_SPACES AM ON AM.DATA_SPACE_ID = IX.DATA_SPACE_ID
                    WHERE S.NAME = ? AND TB.NAME = ?
                    AND IX.IS_PRIMARY_KEY = 0 AND IX.IS_UNIQUE_CONSTRAINT = 0
                    AND IX.IS_HYPOTHETICAL = 0
                    ORDER BY IX.NAME, K.KEY_ORDINAL
                    """;
            this.printSql(sql);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, schema);
            statement.setString(2, tableName);
            ResultSet resultSet = statement.executeQuery();
            // 打印元数据
            DBUtil.printMetaData(resultSet);
            Map<String, SqlServerIndex> indexMap = new HashMap<>();
            while (resultSet.next()) {
                String keyName = resultSet.getString("Key_name");
                SqlServerIndex tableIndex = indexMap.get(keyName);
                String columnName = resultSet.getString("Column_name");
                if (tableIndex == null) {
                    int noneUnique = resultSet.getInt("Non_unique");
                    int seqInIndex = resultSet.getInt("Seq_in_index");
                    String indexType = resultSet.getString("Index_type");
                    tableIndex = new SqlServerIndex();
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
     * 查询索引列表（模式级别，包含所属表名称）
     *
     * @param param 查询参数，包含模式名称与索引名称
     * @return 索引列表
     */
    public List<SqlServerIndex> selectSchemaIndexes(ShellSelectIndexParam param) {
        String schema = param.getScope();
        String indexName = param.getIndexName();
        try {
            Connection connection = this.getConnManager().connection();
            String sql = """
                    SELECT
                        TB.NAME AS "Table_name",
                        IX.NAME AS "Key_name",
                        A.NAME AS "Column_name",
                        K.KEY_ORDINAL AS "Seq_in_index",
                        CASE WHEN IX.IS_UNIQUE = 1 THEN 0 ELSE 1 END AS "Non_unique",
                        IX.TYPE_DESC AS "Index_type"
                    FROM SYS.INDEXES IX
                    JOIN SYS.TABLES TB ON TB.OBJECT_ID = IX.OBJECT_ID
                    JOIN SYS.SCHEMAS S ON S.SCHEMA_ID = TB.SCHEMA_ID
                    JOIN SYS.INDEX_COLUMNS K ON K.OBJECT_ID = IX.OBJECT_ID AND K.INDEX_ID = IX.INDEX_ID
                    JOIN SYS.COLUMNS A ON A.OBJECT_ID = K.OBJECT_ID AND A.COLUMN_ID = K.COLUMN_ID
                    WHERE S.NAME = ?
                    AND IX.IS_PRIMARY_KEY = 0 AND IX.IS_UNIQUE_CONSTRAINT = 0
                    AND IX.IS_HYPOTHETICAL = 0
                    """;
            if (StringUtil.isNotBlank(indexName)) {
                sql += " AND IX.NAME = ?";
            }
            sql += " ORDER BY TB.NAME, IX.NAME, K.KEY_ORDINAL";
            this.printSql(sql);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, schema);
            if (StringUtil.isNotBlank(indexName)) {
                statement.setString(2, indexName);
            }
            ResultSet resultSet = statement.executeQuery();
            Map<String, SqlServerIndex> indexMap = new HashMap<>();
            while (resultSet.next()) {
                String tableName = resultSet.getString("Table_name");
                String keyName = resultSet.getString("Key_name");
                String columnName = resultSet.getString("Column_name");
                String mapKey = tableName + "." + keyName;
                SqlServerIndex index = indexMap.get(mapKey);
                if (index == null) {
                    int noneUnique = resultSet.getInt("Non_unique");
                    int seqInIndex = resultSet.getInt("Seq_in_index");
                    String indexType = resultSet.getString("Index_type");
                    index = new SqlServerIndex();
                    index.setName(keyName);
                    index.setTableName(tableName);
                    index.setSeqIndex(seqInIndex);
                    index.setType(noneUnique == 0 ? "UNIQUE" : "NORMAL");
                    index.setMethod(indexType == null ? null : indexType.toUpperCase());
                    indexMap.put(mapKey, index);
                }
                if (StringUtil.isNotBlank(columnName)) {
                    index.addColumn(columnName);
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
     * 查询索引定义
     *
     * @param schema    模式名称
     * @param indexName 索引名称
     * @return 索引定义
     */
    public String selectIndexDefinition(String schema, String indexName) {
        try {
            Connection connection = this.getConnManager().connection();
            String sql = """
                    SELECT
                        TB.NAME AS "Table_name",
                        IX.NAME AS "Key_name",
                        IX.TYPE_DESC AS "Index_type",
                        IX.IS_UNIQUE AS "Is_unique",
                        IX.FILTER_DEFINITION AS "Filter_definition",
                        A.NAME AS "Column_name",
                        K.KEY_ORDINAL AS "Key_ordinal",
                        K.IS_INCLUDED_COLUMN AS "Is_included",
                        K.IS_DESCENDING_KEY AS "Is_descending"
                    FROM SYS.INDEXES IX
                    JOIN SYS.TABLES TB ON TB.OBJECT_ID = IX.OBJECT_ID
                    JOIN SYS.SCHEMAS S ON S.SCHEMA_ID = TB.SCHEMA_ID
                    JOIN SYS.INDEX_COLUMNS K ON K.OBJECT_ID = IX.OBJECT_ID AND K.INDEX_ID = IX.INDEX_ID
                    JOIN SYS.COLUMNS A ON A.OBJECT_ID = K.OBJECT_ID AND A.COLUMN_ID = K.COLUMN_ID
                    WHERE S.NAME = ? AND IX.NAME = ?
                    AND IX.IS_HYPOTHETICAL = 0
                    AND (K.KEY_ORDINAL > 0 OR K.IS_INCLUDED_COLUMN = 1)
                    ORDER BY K.IS_INCLUDED_COLUMN, K.KEY_ORDINAL, K.INDEX_COLUMN_ID
                    """;
            this.printSql(sql);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, schema);
            statement.setString(2, indexName);
            ResultSet resultSet = statement.executeQuery();
            String tableName = null;
            String indexType = null;
            boolean unique = false;
            String filter = null;
            StringBuilder columns = new StringBuilder();
            StringBuilder includes = new StringBuilder();
            while (resultSet.next()) {
                tableName = resultSet.getString("Table_name");
                indexType = resultSet.getString("Index_type");
                unique = resultSet.getInt("Is_unique") == 1;
                filter = resultSet.getString("Filter_definition");
                String columnName = resultSet.getString("Column_name");
                if (StringUtil.isBlank(columnName)) {
                    continue;
                }
                boolean included = resultSet.getInt("Is_included") == 1;
                boolean descending = resultSet.getInt("Is_descending") == 1;
                if (included) {
                    includes.append(DBUtil.wrap(columnName, DBDialect.SQLSERVER)).append(",");
                } else {
                    columns.append(DBUtil.wrap(columnName, DBDialect.SQLSERVER))
                            .append(descending ? " DESC" : " ASC").append(",");
                }
            }
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            if (StringUtil.isBlank(tableName) || columns.length() == 0) {
                return null;
            }
            StringBuilder builder = new StringBuilder("CREATE ");
            if (unique) {
                builder.append("UNIQUE ");
            }
            // 索引类型（CLUSTERED/NONCLUSTERED），列存储等类型不加前缀
            if (StringUtil.equalsIgnoreCase(indexType, "CLUSTERED")
                    || StringUtil.equalsIgnoreCase(indexType, "NONCLUSTERED")) {
                builder.append(indexType.toUpperCase()).append(" ");
            }
            builder.append("INDEX ")
                    .append(DBUtil.wrap(indexName, DBDialect.SQLSERVER))
                    .append(" ON ")
                    .append(DBUtil.wrap(schema, tableName, DBDialect.SQLSERVER))
                    .append(" (");
            StringUtil.deleteLast(columns, ",");
            builder.append(columns).append(")");
            if (includes.length() > 0) {
                StringUtil.deleteLast(includes, ",");
                builder.append(" INCLUDE (").append(includes).append(")");
            }
            if (StringUtil.isNotBlank(filter)) {
                builder.append(" WHERE ").append(filter);
            }
            return builder.toString();
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    /**
     * 判断索引是否存在
     *
     * @param schema    模式名称
     * @param indexName 索引名称
     * @return 是否存在
     */
    public boolean existIndex(String schema, String indexName) {
        try {
            String sql = """
                    SELECT COUNT(1)
                    FROM SYS.INDEXES IX
                    JOIN SYS.TABLES TB ON TB.OBJECT_ID = IX.OBJECT_ID
                    JOIN SYS.SCHEMAS S ON S.SCHEMA_ID = TB.SCHEMA_ID
                    WHERE S.NAME = ? AND IX.NAME = ?
                    """;
            this.printSql(sql);
            Connection connection = this.getConnManager().connection();
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, schema);
            statement.setString(2, indexName);
            ResultSet resultSet = statement.executeQuery();
            boolean exist = false;
            if (resultSet.next()) {
                exist = resultSet.getInt(1) > 0;
            }
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            return exist;
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    /**
     * 删除索引
     *
     * @param schema    模式名称
     * @param indexName 索引名称
     * @param tableName 表名称
     */
    public void dropIndex(String schema, String indexName, String tableName) {
        String sql = "DROP INDEX " + DBUtil.wrap(indexName, DBDialect.SQLSERVER)
                + " ON " + DBUtil.wrap(schema, tableName, DBDialect.SQLSERVER);
        this.executeIndexSql(schema, List.of(sql));
    }

    /**
     * 查询触发器列表（模式/数据库级别，包含所属表名称）
     *
     * @param param 查询参数
     * @return 触发器列表
     */
    public List<SqlServerTrigger> selectSchemaTriggers(ShellSelectTriggerParam param) {
        SqlServerSelectTriggerParam selectParam = new SqlServerSelectTriggerParam();
        selectParam.setSchema(param.getScope());
        selectParam.setTriggerName(param.getTriggerName());
        // 树节点仅需要名称与所属表，不查询完整定义
        List<SqlServerTrigger> triggers = this.selectTriggers(selectParam);
        String triggerName = param.getTriggerName();
        if (StringUtil.isNotBlank(triggerName)) {
            return triggers.stream()
                    .filter(item -> StringUtil.equalsIgnoreCase(item.getName(), triggerName))
                    .toList();
        }
        return triggers;
    }

    /**
     * 查询触发器定义
     *
     * @param scope       模式/数据库名称
     * @param triggerName 触发器名称
     * @return 触发器定义
     */
    public String selectTriggerDefinition(String scope, String triggerName) {
        return ShellTriggerHelper.stripTerminator(this.showCreateTrigger(scope, triggerName));
    }

    /**
     * 判断触发器是否存在
     *
     * @param scope       模式/数据库名称
     * @param triggerName 触发器名称
     * @return 是否存在
     */
    public boolean existTrigger(String scope, String triggerName) {
        return !this.selectSchemaTriggers(new ShellSelectTriggerParam(scope, triggerName)).isEmpty();
    }

    /**
     * 删除触发器
     *
     * @param scope       模式/数据库名称
     * @param triggerName 触发器名称
     * @param tableName   所属表名称
     */
    public void dropTrigger(String scope, String triggerName, String tableName) {
        String sql = "DROP TRIGGER IF EXISTS " + DBUtil.wrap(scope, triggerName, DBDialect.SQLSERVER);
        this.executeTriggerSql(scope, List.of(sql));
    }

    /**
     * 执行触发器相关SQL
     *
     * @param scope   模式/数据库名称
     * @param sqlList SQL列表
     */
    public void executeTriggerSql(String scope, List<String> sqlList) {
        // 复用与索引一致的通用DDL执行逻辑
        this.executeIndexSql(scope, sqlList);
    }

    /**
     * 查询类型列表（模式级别，仅名称与对象类型，不查询完整定义）
     *
     * @param param 查询参数
     * @return 类型列表
     */
    public List<SqlServerTypeDefinition> selectSchemaTypes(ShellSelectTypeParam param) {
        try {
            Connection connection = this.getConnManager().connection();
            String sql = """
                    SELECT T.NAME AS TYPE_NAME, T.IS_TABLE_TYPE
                    FROM SYS.TYPES T
                    JOIN SYS.SCHEMAS S ON S.SCHEMA_ID = T.SCHEMA_ID
                    WHERE S.NAME = ?
                      AND T.IS_USER_DEFINED = 1
                      AND T.IS_ASSEMBLY_TYPE = 0
                    ORDER BY T.NAME
                    """;
            this.printSql(sql);
            List<SqlServerTypeDefinition> types = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, param.getScope());
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        SqlServerTypeDefinition type = new SqlServerTypeDefinition();
                        type.setName(resultSet.getString("TYPE_NAME"));
                        boolean tableType = resultSet.getBoolean("IS_TABLE_TYPE");
                        type.setObjectType(tableType ? SqlServerTypeDefinition.OBJECT_TYPE_TABLE : SqlServerTypeDefinition.OBJECT_TYPE_ALIAS);
                        types.add(type);
                    }
                }
            }
            String typeName = param.getTypeName();
            if (StringUtil.isNotBlank(typeName)) {
                return types.stream()
                        .filter(item -> StringUtil.equalsIgnoreCase(item.getName(), typeName))
                        .toList();
            }
            return types;
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    /**
     * 查询类型定义
     *
     * @param scope    模式名称
     * @param typeName 类型名称
     * @return 类型定义
     */
    public String selectTypeDefinition(String scope, String typeName) {
        // 复用已有的完整类型查询，避免重复拼装DDL
        for (SqlServerTypeDefinition type : this.selectTypes(scope)) {
            if (StringUtil.equalsIgnoreCase(type.getName(), typeName)) {
                return ShellTypeHelper.stripTerminator(type.getCreateDefinition());
            }
        }
        return null;
    }

    /**
     * 判断类型是否存在
     *
     * @param scope    模式名称
     * @param typeName 类型名称
     * @return 是否存在
     */
    public boolean existType(String scope, String typeName) {
        return !this.selectSchemaTypes(new ShellSelectTypeParam(scope, typeName)).isEmpty();
    }

    /**
     * 删除类型
     *
     * @param scope      模式名称
     * @param typeName   类型名称
     * @param objectType 对象类型
     */
    public void dropType(String scope, String typeName, String objectType) {
        String sql = "DROP TYPE " + DBUtil.wrap(scope, typeName, DBDialect.SQLSERVER);
        this.executeTypeSql(scope, List.of(sql));
    }

    /**
     * 执行类型相关SQL
     *
     * @param scope   模式名称
     * @param sqlList SQL列表
     */
    public void executeTypeSql(String scope, List<String> sqlList) {
        // 复用与索引一致的通用DDL执行逻辑
        this.executeIndexSql(scope, sqlList);
    }

    /**
     * 执行索引相关SQL
     *
     * @param schema  模式名称
     * @param sqlList SQL列表
     */
    public void executeIndexSql(String schema, List<String> sqlList) {
        if (CollectionUtil.isEmpty(sqlList)) {
            return;
        }
        Connection connection = null;
        try {
            connection = this.getConnManager().connection();
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();
            for (String sql : sqlList) {
                if (StringUtil.isBlank(sql)) {
                    continue;
                }
                String execSql = ShellIndexHelper.stripTerminator(sql);
                this.printSql(execSql);
                statement.execute(execSql);
            }
            connection.commit();
            IOUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            DBUtil.rollback(connection);
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
    public List<SqlServerCheck> selectChecks(String schema, String tableName) {
        if (!this.isSupportCheckFeature()) {
            return null;
        }
        try {
            String sql = """
                    SELECT
                        CC.NAME AS "NAME",
                        CC.DEFINITION AS "CLAUSE"
                    FROM SYS.CHECK_CONSTRAINTS CC
                    JOIN SYS.TABLES T ON T.OBJECT_ID = CC.PARENT_OBJECT_ID
                    JOIN SYS.SCHEMAS S ON S.SCHEMA_ID = T.SCHEMA_ID
                    WHERE S.NAME = ? AND T.NAME = ?
                    ORDER BY CC.NAME
                    """;
            this.printSql(sql);
            PreparedStatement statement = this.getConnManager().connection().prepareStatement(sql);
            statement.setString(1, schema);
            statement.setString(2, tableName);
            ResultSet resultSet = statement.executeQuery();
            DBUtil.printMetaData(resultSet);
            List<SqlServerCheck> checks = new ArrayList<>();
            while (resultSet.next()) {
                SqlServerCheck check = new SqlServerCheck();
                String name = resultSet.getString("NAME");
                String clause = resultSet.getString("CLAUSE");
                if (StringUtil.startWithIgnoreCase(clause, "(")
                        && StringUtil.endsWithIgnoreCase(clause, ")")) {
                    clause = clause.substring(1, clause.length() - 1).trim();
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
                    SELECT DISTINCT T.NAME AS "FKTABLE_NAME"
                    FROM SYS.FOREIGN_KEYS FK
                    JOIN SYS.TABLES T ON T.OBJECT_ID = FK.PARENT_OBJECT_ID
                    JOIN SYS.SCHEMAS S ON S.SCHEMA_ID = T.SCHEMA_ID
                    WHERE S.NAME = ?
                    ORDER BY T.NAME
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
    public List<SqlServerForeignKey> selectForeignKeys(String schema, String tableName) {
        try {
            String sql = """
                    SELECT
                        FK.NAME AS "FK_NAME",
                        PT.NAME AS "PKTABLE_NAME",
                        PS.NAME AS "PKTABLE_CAT",
                        FC.NAME AS "FKCOLUMN_NAME",
                        PC.NAME AS "PKCOLUMN_NAME",
                        FK.DELETE_REFERENTIAL_ACTION_DESC AS "DELETE_RULE",
                        FK.UPDATE_REFERENTIAL_ACTION_DESC AS "UPDATE_RULE"
                    FROM SYS.FOREIGN_KEYS FK
                    JOIN SYS.TABLES T ON T.OBJECT_ID = FK.PARENT_OBJECT_ID
                    JOIN SYS.SCHEMAS S ON S.SCHEMA_ID = T.SCHEMA_ID
                    JOIN SYS.TABLES PT ON PT.OBJECT_ID = FK.REFERENCED_OBJECT_ID
                    JOIN SYS.SCHEMAS PS ON PS.SCHEMA_ID = PT.SCHEMA_ID
                    JOIN SYS.FOREIGN_KEY_COLUMNS FKC ON FKC.CONSTRAINT_OBJECT_ID = FK.OBJECT_ID
                    JOIN SYS.COLUMNS FC ON FC.OBJECT_ID = FKC.PARENT_OBJECT_ID
                        AND FC.COLUMN_ID = FKC.PARENT_COLUMN_ID
                    JOIN SYS.COLUMNS PC ON PC.OBJECT_ID = FKC.REFERENCED_OBJECT_ID
                        AND PC.COLUMN_ID = FKC.REFERENCED_COLUMN_ID
                    WHERE S.NAME = ? AND T.NAME = ?
                    ORDER BY FK.NAME, FKC.CONSTRAINT_COLUMN_ID
                    """;
            this.printSql(sql);
            PreparedStatement statement = this.getConnManager().connection().prepareStatement(sql);
            statement.setString(1, schema);
            statement.setString(2, tableName);
            ResultSet resultSet = statement.executeQuery();
            DBUtil.printMetaData(resultSet);
            Map<String, SqlServerForeignKey> foreignKeyMap = new HashMap<>();
            while (resultSet.next()) {
                String fkName = resultSet.getString("FK_NAME");
                String fkColumnName = resultSet.getString("FKCOLUMN_NAME");
                String pkColumnName = resultSet.getString("PKCOLUMN_NAME");
                SqlServerForeignKey foreignKey = foreignKeyMap.get(fkName);
                if (foreignKey == null) {
                    String pkTableName = resultSet.getString("PKTABLE_NAME");
                    String pkTableCat = resultSet.getString("PKTABLE_CAT");
                    String updateRule = this.foreignKeyPolicy(resultSet.getString("UPDATE_RULE"));
                    String deleteRule = this.foreignKeyPolicy(resultSet.getString("DELETE_RULE"));
                    foreignKey = new SqlServerForeignKey();
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
        return switch (code.toUpperCase().replace('_', ' ')) {
            case "A", "NO ACTION" -> "NO ACTION";
            case "R", "RESTRICT" -> "RESTRICT";
            case "C", "CASCADE" -> "CASCADE";
            case "N", "SET NULL" -> "SET NULL";
            case "D", "SET DEFAULT" -> "SET DEFAULT";
            default -> code.toUpperCase().replace('_', ' ');
        };
    }

    /**
     * 查询视图字段列表
     *
     * @param schema   模式名称
     * @param viewName 视图名称
     * @return 字段列表
     */
    public List<SqlServerColumn> viewColumns(String schema, String viewName) {
        try {
            SqlServerColumns columns = new SqlServerColumns();
            Connection connection = this.getConnManager().connection();
            java.util.Set<String> primaryKeys = ShellSqlServerHelper
                    .updatableViewPrimaryKeys(connection, schema, viewName);
            String sql = """
                    SELECT
                        C.NAME AS "Field",
                        TY.NAME +
                        CASE
                            WHEN TY.NAME IN ('varchar', 'char', 'varbinary', 'binary')
                                THEN '(' + CASE WHEN C.MAX_LENGTH = -1 THEN 'MAX'
                                    ELSE CAST(C.MAX_LENGTH AS VARCHAR(20)) END + ')'
                            WHEN TY.NAME IN ('nvarchar', 'nchar')
                                THEN '(' + CASE WHEN C.MAX_LENGTH = -1 THEN 'MAX'
                                    ELSE CAST(C.MAX_LENGTH / 2 AS VARCHAR(20)) END + ')'
                            WHEN TY.NAME IN ('decimal', 'numeric')
                                THEN '(' + CAST(C.PRECISION AS VARCHAR(20)) + ',' +
                                    CAST(C.SCALE AS VARCHAR(20)) + ')'
                            WHEN TY.NAME IN ('datetime2', 'datetimeoffset', 'time')
                                THEN '(' + CAST(C.SCALE AS VARCHAR(20)) + ')'
                            ELSE ''
                        END AS "Type",
                        CAST(EP.VALUE AS NVARCHAR(4000)) AS "Comment",
                        C.COLUMN_ID AS "POSITION",
                        NULL AS "Default",
                        C.IS_NULLABLE AS "NotNull",
                        '' AS "Key",
                        C.IS_IDENTITY AS "IDENTITY"
                    FROM SYS.COLUMNS C
                    JOIN SYS.VIEWS V ON V.OBJECT_ID = C.OBJECT_ID
                    JOIN SYS.SCHEMAS S ON S.SCHEMA_ID = V.SCHEMA_ID
                    JOIN SYS.TYPES TY ON TY.USER_TYPE_ID = C.USER_TYPE_ID
                    LEFT JOIN SYS.EXTENDED_PROPERTIES EP ON EP.MAJOR_ID = C.OBJECT_ID
                        AND EP.MINOR_ID = C.COLUMN_ID AND EP.CLASS = 1
                        AND EP.NAME = 'MS_Description'
                    WHERE S.NAME = ? AND V.NAME = ?
                    ORDER BY C.COLUMN_ID
                    """;
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, schema);
            statement.setString(2, viewName);
            ResultSet resultSet = statement.executeQuery();
            DBUtil.printMetaData(resultSet);
            while (resultSet.next()) {
                String type = resultSet.getString("Type");
                String field = resultSet.getString("Field");
                Object def = resultSet.getObject("Default");
                int position = resultSet.getInt("POSITION");
                boolean notNull = resultSet.getBoolean("NotNull");
                String comment = resultSet.getString("Comment");
                boolean identity = resultSet.getBoolean("IDENTITY");
                boolean autoIncrement = identity;
                SqlServerColumn column = new SqlServerColumn();
                column.parseKey(primaryKeys.contains(field) ? "PRI" : "");
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
            this.initViewGeneratedColumns(connection, schema, viewName, columns);
            // 返回排序后的数据
            return new ArrayList<>(columns.sortOfPosition());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 初始化视图计算列。SQL Server 视图列不会保留基表的 IS_COMPUTED 标记。
     *
     * @param connection 连接
     * @param schema     模式名称
     * @param viewName   视图名称
     * @param columns    视图字段列表
     */
    private void initViewGeneratedColumns(Connection connection, String schema, String viewName,
                                          SqlServerColumns columns) {
        String sql = """
                SELECT DISTINCT BC.NAME AS "COLUMN_NAME"
                FROM SYS.SQL_EXPRESSION_DEPENDENCIES D
                JOIN SYS.VIEWS V ON V.OBJECT_ID = D.REFERENCING_ID
                JOIN SYS.SCHEMAS S ON S.SCHEMA_ID = V.SCHEMA_ID
                JOIN SYS.TABLES T ON T.OBJECT_ID = D.REFERENCED_ID
                JOIN SYS.COLUMNS BC ON BC.OBJECT_ID = T.OBJECT_ID
                WHERE S.NAME = ?
                  AND V.NAME = ?
                  AND BC.IS_COMPUTED = 1
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, schema);
            statement.setString(2, viewName);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String columnName = resultSet.getString("COLUMN_NAME");
                    SqlServerColumn column = columns.stream()
                            .filter(item -> StringUtil.equalsIgnoreCase(
                                    item.getName(), columnName))
                            .findFirst().orElse(null);
                    if (column != null) {
                        column.setGenerated(true);
                    }
                }
            }
        } catch (Exception ex) {
            // 某些兼容级别不提供列级依赖，不影响视图基础字段读取。
            ex.printStackTrace();
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
    public List<SqlServerRecord> viewRecords(String schema, String viewName, Long start, Long limit, List<SqlServerRecordFilter> filters) {
        return this.viewRecords(schema, viewName, start, limit, filters, null);
    }

    /**
     * 查询视图记录列表
     *
     * @param schema   模式名称
     * @param viewName 视图名称
     * @param start    起始位置
     * @param limit    查询条数
     * @param filters  过滤条件
     * @param columns  视图字段元数据
     * @return 记录列表
     */
    public List<SqlServerRecord> viewRecords(String schema, String viewName, Long start, Long limit,
                                             List<SqlServerRecordFilter> filters,
                                             List<SqlServerColumn> columns) {
        try {
            Connection connection = this.getConnManager().connection();
            StringBuilder builder = new StringBuilder("SELECT * FROM ");
            builder.append(DBUtil.wrap(schema, viewName, DBDialect.SQLSERVER));
            String filterCondition = DBUtil.buildCondition(filters);
            if (StringUtil.isNotBlank(filterCondition)) {
                builder.append(" WHERE ").append(filterCondition);
            }
            if (start != null && limit != null) {
                builder.append(" ORDER BY (SELECT NULL) OFFSET ")
                        .append(start).append(" ROWS FETCH NEXT ")
                        .append(limit).append(" ROWS ONLY");
            }
            String sql = builder.toString();
            this.printSql(sql);
            List<SqlServerRecord> records = new ArrayList<>();
            boolean updatable = ShellSqlServerHelper.isViewUpdatable(connection, schema, viewName);
            SqlServerColumns recordColumns = columns == null || columns.isEmpty()
                    ? new SqlServerColumns(this.viewColumns(schema, viewName))
                    : new SqlServerColumns(columns);
            for (SqlServerColumn column : recordColumns) {
                column.setTableName(viewName);
            }
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(sql)) {
                DBUtil.printMetaData(resultSet);
                while (resultSet.next()) {
                    SqlServerRecord record = new SqlServerRecord(recordColumns, !updatable);
                    for (SqlServerColumn column : recordColumns) {
                        Object data = resultSet.getObject(column.getName());
                        data = ShellSqlServerDataUtil.valueStandardization(data);
                        record.putValue(column, data);
                    }
                    records.add(record);
                }
            }
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
    public void createTable(SqlServerCreateTableParam param) {
        Connection connection = null;
        boolean autoCommit = true;
        Statement statement = null;
        try {
            connection = this.getConnManager().connection();
            autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            statement = connection.createStatement();
            List<String> sqlList = SqlServerTableCreateSqlGenerator.generateSql(param);
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
    public void alertTable(SqlServerAlertTableParam param) {
        Connection connection = null;
        boolean autoCommit = true;
        Statement statement = null;
        try {
            List<String> sqlList = SqlServerTableAlertSqlGenerator.generateSql(param);
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
                // 序列同步会生成SELECT setval(...)，该语句有结果集，不能使用executeUpdate
                statement.execute(sql);
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
            this.renameObject(schema, oldTableName, newTableName);
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
            this.renameObject(schema, oldViewName, newViewName);
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
            this.renameObject(schema, oldFunctionName, newFunctionName);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
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
            this.renameObject(schema, oldProcedureName, newProcedureName);
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
            String sql = "DELETE FROM " + DBUtil.wrap(schema, tableName, DBDialect.SQLSERVER);
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
            String sql = "TRUNCATE TABLE " + DBUtil.wrap(schema, tableName, DBDialect.SQLSERVER);
            this.printSql(sql);
            statement.executeUpdate(sql);
            String checkIdent = "DBCC CHECKIDENT ("
                    + DBUtil.wrapString(DBUtil.wrap(schema, tableName, DBDialect.SQLSERVER), DBDialect.SQLSERVER)
                    + ", RESEED, 0) WITH NO_INFOMSGS";
            this.printSql(checkIdent);
            statement.execute(checkIdent);
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
            String sql = "DROP TABLE IF EXISTS " + DBUtil.wrap(schema, tableName, DBDialect.SQLSERVER);
            this.printSql(sql);
            statement.executeUpdate(sql);
            IOUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    private void renameObject(String schema, String oldName, String newName) throws Exception {
        String objectName = DBUtil.wrap(schema, oldName, DBDialect.SQLSERVER);
        String sql = "EXEC sp_rename "
                + DBUtil.wrapString(objectName, DBDialect.SQLSERVER)
                + ", " + DBUtil.wrapString(newName, DBDialect.SQLSERVER)
                + ", N'OBJECT'";
        try (Statement statement = this.getConnManager().connection().createStatement()) {
            this.printSql(sql);
            statement.execute(sql);
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
                    SELECT COUNT(*) FROM SYS.SCHEMAS WHERE NAME = ?
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
    public void createSchema(SqlServerSchema schema) {
        try {
            StringBuilder builder = new StringBuilder("CREATE SCHEMA ");
            builder.append(DBUtil.wrap(schema.getName(), DBDialect.SQLSERVER));
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
    public boolean alterSchema(SqlServerSchema schema) {
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
            String sql = "DROP SCHEMA " + DBUtil.wrap(schema, DBDialect.SQLSERVER);
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
    public DBQueryResults<SqlServerExplainResult> explainSql(String schema, String sql) {
        DBQueryResults<SqlServerExplainResult> results = new DBQueryResults<>();
        Connection connection = null;
        try {
            this.printSql(sql);
            DBSqlParser parser = DBSqlParser.getParser(sql, DBDialect.SQLSERVER);
            List<String> list = ShellSqlServerUtil.splitSqlServerBatches(sql);
            connection = this.getConnManager().connection(schema);
            Statement statement = connection.createStatement();
            for (String execSql : list) {
                SqlServerExplainResult result = new SqlServerExplainResult();
                try {
                    String querySql = execSql.stripLeading();
                    result.setContent(querySql);
                    long startTime = System.nanoTime();
                    statement.execute("SET SHOWPLAN_TEXT ON");
                    try (ResultSet resultSet = statement.executeQuery(querySql)) {
                        result.parseResult(resultSet, connection);
                        result.fillSchema(schema);
                        result.setUsed(System.nanoTime() - startTime);
                        result.setSuccess(true);
                    } finally {
                        statement.execute("SET SHOWPLAN_TEXT OFF");
                    }
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
    public SqlServerExecuteResult executeSingleSql(String schema, String sql) {
        Connection connection = null;
        boolean autoCommit = true;
        Statement statement = null;
        SqlServerExecuteResult result = new SqlServerExecuteResult();
        result.setContent(sql);
        try {
            this.printSql(sql);
            DBSqlParser parser = DBSqlParser.getParser(sql, DBDialect.SQLSERVER);
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
            for (String statementSql : ShellSqlServerUtil.splitSqlServerBatches(sql)) {
                statement.execute(statementSql);
            }
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
     * @param newConnection 是否使用新连接
     * @return 受影响的行数
     */
    public int insertBatch(String schema, List<String> sqlList, boolean newConnection) {
        Connection connection = null;
        boolean autoCommit = true;
        Statement statement = null;
        int result = 0;
        try {
            connection = newConnection ? this.getConnManager().newConnection(null) : this.getConnManager().connection(schema);
            if (newConnection && StringUtil.isNotBlank(schema)) {
                connection.setSchema(schema);
            }
            autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            statement = connection.createStatement();
            Map<String, Boolean> identityCache = new HashMap<>();
            String identityInsertTable = null;
            for (String sql : sqlList) {
                String[] target = ShellSqlServerUtil.parseInsertTarget(sql);
                String wrappedTable = null;
                if (target != null) {
                    String targetSchema = StringUtil.isBlank(target[0]) ? schema : target[0];
                    String cacheKey = targetSchema + "." + target[1];
                    Boolean identity = identityCache.get(cacheKey);
                    if (identity == null) {
                        identity = this.hasAlwaysIdentityColumn(targetSchema, target[1]);
                        identityCache.put(cacheKey, identity);
                    }
                    if (identity && this.insertContainsIdentityColumn(sql, targetSchema, target[1])) {
                        wrappedTable = DBUtil.wrap(targetSchema, target[1], DBDialect.SQLSERVER);
                        if (!StringUtil.equalsIgnoreCase(identityInsertTable, wrappedTable)) {
                            if (identityInsertTable != null) {
                                statement.execute("SET IDENTITY_INSERT " + identityInsertTable + " OFF;");
                            }
                            statement.execute("SET IDENTITY_INSERT " + wrappedTable + " ON;");
                            identityInsertTable = wrappedTable;
                        }
                    }
                }
                this.printSql(sql);
                result += statement.executeUpdate(sql);
            }
            if (identityInsertTable != null) {
                statement.execute("SET IDENTITY_INSERT " + identityInsertTable + " OFF;");
            }
            connection.commit();
        } catch (Exception ex) {
            JulLog.warn("sqlList:{}", sqlList);
            ex.printStackTrace();
            DBUtil.rollback(connection);
            throw new ShellException(ex);
        } finally {
            IOUtil.close(statement);
            if (connection != null) {
                if (newConnection) {
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

    /**
     * 判断INSERT列清单中是否显式包含IDENTITY列。
     */
    private boolean insertContainsIdentityColumn(String sql, String schema, String tableName) throws Exception {
        String sqlText = sql;
        int columnsStart = sqlText.indexOf('(');
        if (columnsStart < 0) {
            return false;
        }
        int columnsEnd = sqlText.indexOf(')', columnsStart);
        if (columnsEnd < 0) {
            return false;
        }
        java.util.Set<String> insertColumns = java.util.Arrays.stream(
                        sqlText.substring(columnsStart + 1, columnsEnd).split(","))
                .map(String::trim)
                .map(column -> column.replace("[", "").replace("]", ""))
                .map(column -> column.toUpperCase(java.util.Locale.ROOT))
                .collect(java.util.stream.Collectors.toSet());
        return this.selectColumns(new SqlServerSelectColumnParam(schema, tableName)).stream()
                .filter(DBColumn::isAutoIncrement)
                .map(column -> column.getName().toUpperCase(java.util.Locale.ROOT))
                .anyMatch(insertColumns::contains);
    }

    @Override
    public DBDialect dialect() {
        return DBDialect.SQLSERVER;
    }

    /**
     * 查询函数列表
     *
     * @param schema 模式名称
     * @return 函数列表
     */
    public List<SqlServerFunction> selectFunctions(String schema) {
        SqlServerSelectFunctionParam param = new SqlServerSelectFunctionParam();
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
    public List<SqlServerFunction> selectFunctionsSimple(String schema) {
        SqlServerSelectFunctionParam param = new SqlServerSelectFunctionParam();
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
    public List<SqlServerFunction> selectFunctions(SqlServerSelectFunctionParam param) {
        try {
            String schema = param.getSchema();
            List<SqlServerFunction> list = new ArrayList<>();
            String sql = """
                    SELECT
                        O.OBJECT_ID AS "OID",
                        O.NAME AS "ROUTINE_NAME",
                        CASE
                            WHEN UPPER(M.DEFINITION) LIKE N'%EXECUTE AS SELF%' THEN 'SELF'
                            WHEN M.EXECUTE_AS_PRINCIPAL_ID = -2 THEN 'OWNER'
                            ELSE 'CALLER'
                        END AS "SECURITY_TYPE",
                        'v' AS "VOLATILE",
                        CAST(0 AS BIT) AS "STRICT",
                        M.DEFINITION AS "DEFINITION",
                        M.DEFINITION AS "DLL"
                    FROM SYS.OBJECTS O
                    JOIN SYS.SCHEMAS S ON S.SCHEMA_ID = O.SCHEMA_ID
                    JOIN SYS.SQL_MODULES M ON M.OBJECT_ID = O.OBJECT_ID
                    WHERE S.NAME = ? AND O.TYPE IN ('FN','IF','TF','FS','FT','AF')
                    ORDER BY O.NAME, O.OBJECT_ID
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
                SqlServerFunction function = new SqlServerFunction();
                long oid = resultSet.getLong("OID");
                String name = resultSet.getString("ROUTINE_NAME");
                String createDefinition = resultSet.getString("DLL");
                String definition = resultSet.getString("DEFINITION");
                boolean strict = resultSet.getBoolean("STRICT");
                String volatility = resultSet.getString("VOLATILE");
                String securityType = resultSet.getString("SECURITY_TYPE");
                StringBuilder characteristic = new StringBuilder();
                if (param.isFull()) {
                    List<SqlServerRoutineParam> params = this.listFunctionParam(oid);
                    function.setParams(params);
                }
                if (StringUtil.isBlank(definition)) {
                    definition = ShellSqlServerHelper.parseFunctionBody(createDefinition);
                }
                definition = ShellSqlServerHelper.normalizeFunctionDefinition(definition);
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
    public List<SqlServerProcedure> selectProcedures(String schema) {
        SqlServerSelectProcedureParam param = new SqlServerSelectProcedureParam();
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
    public List<SqlServerProcedure> selectProceduresSimple(String schema) {
        SqlServerSelectProcedureParam param = new SqlServerSelectProcedureParam();
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
    public List<SqlServerProcedure> selectProcedures(SqlServerSelectProcedureParam param) {
        try {
            String schema = param.getSchema();
            List<SqlServerProcedure> list = new ArrayList<>();
            String sql = """
                    SELECT
                        O.OBJECT_ID AS "OID",
                        O.NAME AS "ROUTINE_NAME",
                        CASE
                            WHEN UPPER(M.DEFINITION) LIKE N'%EXECUTE AS SELF%' THEN 'SELF'
                            WHEN M.EXECUTE_AS_PRINCIPAL_ID = -2 THEN 'OWNER'
                            ELSE 'CALLER'
                        END AS "SECURITY_TYPE",
                        'v' AS "VOLATILE",
                        CAST(0 AS BIT) AS "STRICT",
                        M.DEFINITION AS "DEFINITION",
                        M.DEFINITION AS "DLL"
                    FROM SYS.OBJECTS O
                    JOIN SYS.SCHEMAS S ON S.SCHEMA_ID = O.SCHEMA_ID
                    JOIN SYS.SQL_MODULES M ON M.OBJECT_ID = O.OBJECT_ID
                    WHERE S.NAME = ? AND O.TYPE = 'P'
                    ORDER BY O.NAME, O.OBJECT_ID
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
                SqlServerProcedure procedure = new SqlServerProcedure();
                long oid = resultSet.getLong("OID");
                String name = resultSet.getString("ROUTINE_NAME");
                String createDefinition = resultSet.getString("DLL");
                String definition = resultSet.getString("DEFINITION");
                boolean strict = resultSet.getBoolean("STRICT");
                String volatility = resultSet.getString("VOLATILE");
                String securityType = resultSet.getString("SECURITY_TYPE");
                StringBuilder characteristic = new StringBuilder();
                if (param.isFull()) {
                    List<SqlServerRoutineParam> params = this.listProcedureParam(oid);
                    procedure.setParams(params);
                }
                if (StringUtil.isBlank(definition)) {
                    definition = ShellSqlServerHelper.parseFunctionBody(createDefinition);
                }
                definition = ShellSqlServerHelper.normalizeProcedureDefinition(definition);
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
    public SqlServerProcedure selectProcedure(String schema, String procedureName) {
        SqlServerSelectProcedureParam param = new SqlServerSelectProcedureParam();
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
    public SqlServerProcedure selectProcedureSimple(String schema, String procedureName) {
        SqlServerSelectProcedureParam param = new SqlServerSelectProcedureParam();
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
    public SqlServerProcedure selectProcedure(SqlServerSelectProcedureParam param) {
        try {
            String schema = param.getSchema();
            String produceName = param.getProcedureName();
            String sql = """
                    SELECT
                        O.OBJECT_ID AS "OID",
                        CASE
                            WHEN UPPER(M.DEFINITION) LIKE N'%EXECUTE AS SELF%' THEN 'SELF'
                            WHEN M.EXECUTE_AS_PRINCIPAL_ID = -2 THEN 'OWNER'
                            ELSE 'CALLER'
                        END AS "SECURITY_TYPE",
                        CAST('v' AS NVARCHAR(10)) AS "VOLATILE",
                        CAST(0 AS BIT) AS "STRICT",
                        M.DEFINITION AS "DEFINITION",
                        M.DEFINITION AS "DLL"
                    FROM SYS.OBJECTS O
                    JOIN SYS.SCHEMAS S ON S.SCHEMA_ID = O.SCHEMA_ID
                    JOIN SYS.SQL_MODULES M ON M.OBJECT_ID = O.OBJECT_ID
                    WHERE S.NAME = ? AND O.NAME = ? AND O.TYPE = 'P'
                    """;
            this.printSql(sql);
            PreparedStatement statement = this.getConnManager().connection().prepareStatement(sql);
            statement.setString(1, schema);
            statement.setString(2, produceName);
            // 执行SQL查询并获取结果集
            ResultSet resultSet = statement.executeQuery();
            // 打印元数据
            DBUtil.printMetaData(resultSet);
            SqlServerProcedure procedure = new SqlServerProcedure();
            procedure.setSchema(schema);
            procedure.setName(produceName);
            // 遍历结果集
            while (resultSet.next()) {
                long oid = resultSet.getLong("OID");
                String createDefinition = resultSet.getString("DLL");
                String definition = resultSet.getString("DEFINITION");
                boolean strict = resultSet.getBoolean("STRICT");
                String volatility = resultSet.getString("VOLATILE");
                String securityType = resultSet.getString("SECURITY_TYPE");
                StringBuilder characteristic = new StringBuilder();
                if (param.isFull()) {
                    List<SqlServerRoutineParam> params = this.listProcedureParam(oid);
                    procedure.setParams(params);
                }
                if (StringUtil.isBlank(definition)) {
                    definition = ShellSqlServerHelper.parseFunctionBody(createDefinition);
                }
                definition = ShellSqlServerHelper.normalizeProcedureDefinition(definition);
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
    public void dropProcedure(SqlServerProcedure routine) {
        if (routine == null || StringUtil.isBlank(routine.getSchema()) || StringUtil.isBlank(routine.getName())) {
            return;
        }
        try {
            String sql = "DROP PROCEDURE IF EXISTS " + DBUtil.wrap(routine.getSchema(), routine.getName(), DBDialect.SQLSERVER);
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
    public void createProcedure(SqlServerCreateProcedureParam param) {
        try {
            String sql = SqlServerProcedureCreateSqlGenerator.generateSqlSingle(param);
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
    public void alertProcedure(SqlServerAlertProcedureParam param) {
        try {
            String sql = SqlServerProcedureAlertSqlGenerator.generateSqlSingle(param);
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
    public void dropFunction(SqlServerFunction function) {
        if (function == null || StringUtil.isBlank(function.getSchema()) || StringUtil.isBlank(function.getName())) {
            return;
        }
        try {
            String sql = "DROP FUNCTION IF EXISTS " + DBUtil.wrap(function.getSchema(), function.getName(), DBDialect.SQLSERVER);
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
    public SqlServerFunction selectFunction(String schema, String functionName) {
        SqlServerSelectFunctionParam param = new SqlServerSelectFunctionParam();
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
    public SqlServerFunction selectFunctionSimple(String schema, String functionName) {
        SqlServerSelectFunctionParam param = new SqlServerSelectFunctionParam();
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
    public SqlServerFunction selectFunction(SqlServerSelectFunctionParam param) {
        try {
            String schema = param.getSchema();
            String functionName = param.getFunctionName();
            String sql = """
                    SELECT
                        O.OBJECT_ID AS "OID",
                        CASE
                            WHEN UPPER(M.DEFINITION) LIKE N'%EXECUTE AS SELF%' THEN 'SELF'
                            WHEN M.EXECUTE_AS_PRINCIPAL_ID = -2 THEN 'OWNER'
                            ELSE 'CALLER'
                        END AS "SECURITY_TYPE",
                        CAST('v' AS NVARCHAR(10)) AS "VOLATILE",
                        CAST(0 AS BIT) AS "STRICT",
                        M.DEFINITION AS "DEFINITION",
                        M.DEFINITION AS "DLL"
                    FROM SYS.OBJECTS O
                    JOIN SYS.SCHEMAS S ON S.SCHEMA_ID = O.SCHEMA_ID
                    JOIN SYS.SQL_MODULES M ON M.OBJECT_ID = O.OBJECT_ID
                    WHERE S.NAME = ? AND O.NAME = ?
                        AND O.TYPE IN ('FN','IF','TF','FS','FT','AF')
                    """;
            this.printSql(sql);
            PreparedStatement statement = this.getConnManager().connection().prepareStatement(sql);
            statement.setString(1, schema);
            statement.setString(2, functionName);
            // 执行SQL查询并获取结果集
            ResultSet resultSet = statement.executeQuery();
            // 打印元数据
            DBUtil.printMetaData(resultSet);
            SqlServerFunction function = new SqlServerFunction();
            function.setSchema(schema);
            function.setName(functionName);
            // 遍历结果集
            while (resultSet.next()) {
                long oid = resultSet.getLong("OID");
                String createDefinition = resultSet.getString("DLL");
                String definition = resultSet.getString("DEFINITION");
                boolean strict = resultSet.getBoolean("STRICT");
                String volatility = resultSet.getString("VOLATILE");
                String securityType = resultSet.getString("SECURITY_TYPE");
                StringBuilder characteristic = new StringBuilder();
                if (param.isFull()) {
                    List<SqlServerRoutineParam> params = this.listFunctionParam(oid);
                    function.setParams(params);
                }
                if (StringUtil.isBlank(definition)) {
                    definition = ShellSqlServerHelper.parseFunctionBody(createDefinition);
                }
                definition = ShellSqlServerHelper.normalizeFunctionDefinition(definition);
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
    public void createFunction(SqlServerCreateFunctionParam param) {
        try {
            String sql = SqlServerFunctionCreateSqlGenerator.generateSqlSingle(param);
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
    public SqlServerRecord selectRecord(SqlServerSelectRecordParam param) {
        try {
            String schema = param.getSchema();
            String tableName = param.getTableName();
            Connection connection = this.getConnManager().connection();
            SqlServerRecordPrimaryKey primaryKey = param.getPrimaryKey();
            StringBuilder builder = new StringBuilder("SELECT * FROM ");
            builder.append(DBUtil.wrap(schema, tableName, DBDialect.SQLSERVER))
                    .append(" WHERE ")
                    .append(DBUtil.wrap(primaryKey.getColumnName(), DBDialect.SQLSERVER))
                    .append(" = ?");
            String sql = builder.toString();
            this.printSql(sql);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setObject(1, primaryKey.data());
            ResultSet resultSet = statement.executeQuery();
            DBUtil.printMetaData(resultSet);
            SqlServerColumns columns = ShellSqlServerHelper.parseColumns(resultSet);
            SqlServerRecord record = new SqlServerRecord(columns);
            while (resultSet.next()) {
                for (SqlServerColumn column : columns) {
                    Object data = resultSet.getObject(column.getName());
                    data = ShellSqlServerDataUtil.valueStandardization(data);
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
        // SqlServer客户端字符集，返回默认值
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
                    SELECT COUNT(*)
                    FROM SYS.COLUMNS C
                    JOIN SYS.TABLES T ON T.OBJECT_ID = C.OBJECT_ID
                    JOIN SYS.SCHEMAS S ON S.SCHEMA_ID = T.SCHEMA_ID
                    WHERE S.NAME = ? AND T.NAME = ? AND C.IS_IDENTITY = 1
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
                    SELECT KC.NAME AS "CONSTRAINT_NAME"
                    FROM SYS.KEY_CONSTRAINTS KC
                    JOIN SYS.TABLES T ON T.OBJECT_ID = KC.PARENT_OBJECT_ID
                    JOIN SYS.SCHEMAS S ON S.SCHEMA_ID = T.SCHEMA_ID
                    WHERE KC.TYPE = 'PK' AND S.NAME = ? AND T.NAME = ?
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
        Connection connection = null;
        boolean autoCommit = true;
        try {
            connection = this.getConnManager().connection();
            String sourceTable = DBUtil.wrap(schema, tableName, DBDialect.SQLSERVER);
            String newTable = DBUtil.wrap(schema, newTableName, DBDialect.SQLSERVER);
            String definition = this.showCreateTable(schema, tableName)
                    .replace(sourceTable, newTable)
                    // 约束名在SQL Server的同一Schema内必须唯一，克隆时交给数据库重新生成
                    .replaceAll("(?i)CONSTRAINT\\s+\\[[^\\]]+\\]\\s+"
                            + "(?=(PRIMARY\\s+KEY|UNIQUE|FOREIGN\\s+KEY|CHECK))", "");
            autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try (Statement statement = connection.createStatement()) {
                this.printSql(definition);
                statement.execute(definition);
                if (includeRecord) {
                    SqlServerColumns columns = new SqlServerColumns(
                            this.selectColumns(new SqlServerSelectColumnParam(schema, tableName)));
                    List<SqlServerColumn> insertColumns = columns.sortOfPosition().stream()
                            .filter(column -> !column.isGenerated())
                            .toList();
                    String names = insertColumns.stream()
                            .map(column -> DBUtil.wrap(column.getName(), DBDialect.SQLSERVER))
                            .collect(java.util.stream.Collectors.joining(","));
                    boolean identity = insertColumns.stream().anyMatch(DBColumn::isAutoIncrement);
                    StringBuilder insert = new StringBuilder();
                    if (identity) {
                        insert.append("SET IDENTITY_INSERT ").append(newTable).append(" ON;\n");
                    }
                    insert.append("INSERT INTO ").append(newTable).append(" (").append(names)
                            .append(") SELECT ").append(names).append(" FROM ").append(sourceTable).append(";\n");
                    if (identity) {
                        insert.append("SET IDENTITY_INSERT ").append(newTable).append(" OFF;");
                    }
                    this.printSql(insert.toString());
                    statement.execute(insert.toString());
                }
                connection.commit();
            }
        } catch (Exception ex) {
            DBUtil.rollback(connection);
            try {
                this.dropTable(schema, newTableName);
            } catch (Exception cleanup) {
                JulLog.warn("cleanup clone table error", cleanup);
            }
            ex.printStackTrace();
            throw new ShellException(ex);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(autoCommit);
                } catch (SQLException ex) {
                    JulLog.warn("restore autocommit error", ex);
                }
            }
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
                SELECT COUNT(*)
                FROM SYS.COLUMNS C
                JOIN SYS.TABLES T ON T.OBJECT_ID = C.OBJECT_ID
                JOIN SYS.SCHEMAS S ON S.SCHEMA_ID = T.SCHEMA_ID
                WHERE S.NAME = ? AND T.NAME = ? AND C.IS_IDENTITY = 1
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
            sql = sql.replace("VIEW " + DBUtil.wrap(viewName, DBDialect.SQLSERVER), "VIEW " + DBUtil.wrap(newViewName, DBDialect.SQLSERVER));
            sql = sql.replace("VIEW " + DBUtil.wrap(schema, viewName, DBDialect.SQLSERVER), "VIEW " + DBUtil.wrap(schema, newViewName, DBDialect.SQLSERVER));
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
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("(?i)\\b" + java.util.regex.Pattern.quote(keyword) + "\\b")
                .matcher(definition);
        if (!matcher.find()) {
            return definition;
        }
        int nameStart = matcher.end();
        while (nameStart < definition.length()
                && Character.isWhitespace(definition.charAt(nameStart))) {
            nameStart++;
        }
        int nameEnd = nameStart;
        while (nameEnd < definition.length()) {
            char ch = definition.charAt(nameEnd);
            if (ch == '[') {
                int close = definition.indexOf(']', nameEnd + 1);
                if (close < 0) {
                    return definition;
                }
                nameEnd = close + 1;
            } else if (ch == '"') {
                int close = definition.indexOf('"', nameEnd + 1);
                if (close < 0) {
                    return definition;
                }
                nameEnd = close + 1;
            } else if (ch == '.' || ch == '_' || ch == '$' || ch == '#' || ch == '@'
                    || Character.isLetterOrDigit(ch)) {
                nameEnd++;
            } else {
                break;
            }
        }
        if (nameEnd == nameStart) {
            return definition;
        }
        String replacement = DBUtil.wrap(schema, newName, DBDialect.SQLSERVER);
        return definition.substring(0, nameStart) + replacement + definition.substring(nameEnd);
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
    public List<SqlServerRoutineParam> listRoutineParam(String schema, String routineName, String routineType) throws Exception {
        try {
            Connection connection = this.getConnManager().connection();
            boolean procedure = StringUtil.equalsIgnoreCase(routineType, "PROCEDURE");
            String typeList = procedure ? "'P'" : "'FN','IF','TF','FS','FT','AF'";
            String procSql = "SELECT O.OBJECT_ID AS OID FROM SYS.OBJECTS O "
                    + "JOIN SYS.SCHEMAS S ON S.SCHEMA_ID = O.SCHEMA_ID "
                    + "WHERE S.NAME = ? AND O.NAME = ? AND O.TYPE IN (" + typeList + ") "
                    + "ORDER BY O.OBJECT_ID";
            this.printSql(procSql);
            PreparedStatement procStatement = connection.prepareStatement(procSql);
            procStatement.setString(1, schema);
            procStatement.setString(2, routineName);
            ResultSet procResultSet = procStatement.executeQuery();
            long oid = 0;
            if (procResultSet.next()) {
                oid = procResultSet.getLong("OID");
            }
            IOUtil.close(procResultSet);
            IOUtil.close(procStatement);
            if (oid == 0) {
                return Collections.emptyList();
            }
            return this.listRoutineParamByOid(oid, routineType);
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
    public List<SqlServerRoutineParam> listRoutineParamByOid(long oid, String routineType) throws Exception {
        try {
            Connection connection = this.getConnManager().connection();
            String sql = """
                    SELECT
                        P.PARAMETER_ID AS "POSITION",
                        TYPE_NAME(P.USER_TYPE_ID) AS "DATA_TYPE",
                        P.NAME AS "PARAMETER_NAME",
                        P.IS_OUTPUT AS "IS_OUTPUT",
                        P.MAX_LENGTH,
                        P.PRECISION,
                        P.SCALE
                    FROM SYS.PARAMETERS P
                    WHERE P.OBJECT_ID = ?
                    ORDER BY P.PARAMETER_ID
                    """;
            this.printSql(sql);
            List<SqlServerRoutineParam> params = new ArrayList<>();
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, oid);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                SqlServerRoutineParam param = new SqlServerRoutineParam();
                int position = resultSet.getInt("POSITION");
                param.setPosition(position);
                param.setType(resultSet.getString("DATA_TYPE"));
                boolean procedure = StringUtil.equalsIgnoreCase(routineType, "PROCEDURE");
                String parameterName = resultSet.getString("PARAMETER_NAME");
                if (parameterName != null && parameterName.startsWith("@")) {
                    parameterName = parameterName.substring(1);
                }
                // sys.parameters中用PARAMETER_ID=0表示标量函数返回值，映射为通用返回参数
                if (!procedure && position == 0) {
                    parameterName = "V_RET";
                }
                param.setName(parameterName);
                boolean output = resultSet.getBoolean("IS_OUTPUT");
                param.setMode(!procedure && position == 0 ? "OUT" : output ? "OUT" : "IN");
                int maxLength = resultSet.getInt("MAX_LENGTH");
                if (maxLength > 0 && StringUtil.equalsAnyIgnoreCase(param.getType(),
                        "varchar", "char", "varbinary", "binary")) {
                    param.setSize(maxLength);
                } else if (maxLength > 0 && StringUtil.equalsAnyIgnoreCase(param.getType(),
                        "nvarchar", "nchar")) {
                    param.setSize(maxLength / 2);
                }
                int precision = resultSet.getInt("PRECISION");
                int scale = resultSet.getInt("SCALE");
                if (StringUtil.equalsAnyIgnoreCase(param.getType(), "decimal", "numeric")) {
                    param.setSize(precision);
                    param.setDigits(scale);
                }
                param.setCharset(null);
                params.add(param);
            }
            IOUtil.close(resultSet);
            IOUtil.close(statement);
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
    public List<SqlServerRoutineParam> listFunctionParam(String schema, String functionName) throws Exception {
        return listRoutineParam(schema, functionName, "FUNCTION");
    }

    /**
     * 按函数OID查询参数
     *
     * @param oid 函数OID
     * @return 参数列表
     * @throws Exception 异常
     */
    public List<SqlServerRoutineParam> listFunctionParam(long oid) throws Exception {
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
    public List<SqlServerRoutineParam> listProcedureParam(String schema, String procedureName) throws Exception {
        return listRoutineParam(schema, procedureName, "PROCEDURE");
    }

    /**
     * 按过程OID查询参数
     *
     * @param oid 过程OID
     * @return 参数列表
     * @throws Exception 异常
     */
    public List<SqlServerRoutineParam> listProcedureParam(long oid) throws Exception {
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
        List<SqlServerForeignKey> foreignKeys = this.selectForeignKeys(schema, table);
        for (SqlServerForeignKey foreignKey : foreignKeys) {
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
    public void createSequence(SqlServerCreateSequenceParam param) {
        try {
            String sql = SqlServerSequenceCreateSqlGenerator.generateSqlSingle(param);
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
                    SELECT COUNT(*)
                    FROM SYS.SEQUENCES Q
                    JOIN SYS.SCHEMAS S ON S.SCHEMA_ID = Q.SCHEMA_ID
                    WHERE S.NAME = ?
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

    public List<SqlServerSequence> selectSequencesSimple(String schema) {
        SqlServerSelectSequenceParam param = new SqlServerSelectSequenceParam();
        param.setFull(false);
        param.setSchema(schema);
        return this.selectSequences(param);
    }

    public void cloneSequence(String schema, String sequenceName, String newSequenceName) {
        String sql = this.showCreateSequence(schema, sequenceName);
        try {
            sql = sql.replace("SEQUENCE " + DBUtil.wrap(sequenceName, DBDialect.SQLSERVER), "SEQUENCE " + DBUtil.wrap(newSequenceName, DBDialect.SQLSERVER));
            sql = sql.replace("SEQUENCE " + DBUtil.wrap(schema, sequenceName, DBDialect.SQLSERVER), "SEQUENCE " + DBUtil.wrap(schema, newSequenceName, DBDialect.SQLSERVER));
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

    public SqlServerSequence selectSequence(String schema, String sequenceName) {
        try {
            PreparedStatement statement;
            Connection connection = this.getConnManager().connection();
            String sql = """
                    SELECT
                        MINIMUM_VALUE AS MIN_VALUE,
                        MAXIMUM_VALUE AS MAX_VALUE,
                        CACHE_SIZE,
                        IS_CYCLING AS CYCLE,
                        INCREMENT AS INCREMENT_BY,
                        CURRENT_VALUE,
                        START_VALUE
                    FROM SYS.SEQUENCES Q
                    JOIN SYS.SCHEMAS S ON S.SCHEMA_ID = Q.SCHEMA_ID
                    WHERE S.NAME = ? AND Q.NAME = ?
                    """;
            this.printSql(sql);
            statement = connection.prepareStatement(sql);
            statement.setString(1, schema);
            statement.setString(2, sequenceName);
            ResultSet resultSet = statement.executeQuery();
            SqlServerSequence sequence = new SqlServerSequence();
            sequence.setName(sequenceName);
            while (resultSet.next()) {
                BigInteger minValue = ShellSequenceUtil.getBigInteger(resultSet, "MIN_VALUE");
                BigInteger maxValue = ShellSequenceUtil.getBigInteger(resultSet, "MAX_VALUE");
                BigInteger cacheSize = ShellSequenceUtil.getBigInteger(resultSet, "CACHE_SIZE");
                boolean cycle = resultSet.getBoolean("CYCLE");
                BigInteger incrementBy = ShellSequenceUtil.getBigInteger(resultSet, "INCREMENT_BY");
                BigInteger currentValue = ShellSequenceUtil.getBigInteger(resultSet, "CURRENT_VALUE");
                sequence.setSchema(schema);
                sequence.setMinVal(minValue);
                sequence.setMaxVal(maxValue);
                sequence.setCacheSize(cacheSize);
                sequence.setCurrentVal(currentValue);
                sequence.setIncrementBy(incrementBy);
                sequence.setCycle(cycle);
                // SqlServer序列不支持ORDER
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

    public void dropSequence(SqlServerSequence sequence) {
        if (sequence == null || StringUtil.isBlank(sequence.getSchema()) || StringUtil.isBlank(sequence.getName())) {
            return;
        }
        try {
            String sql = "DROP SEQUENCE IF EXISTS " + DBUtil.wrap(sequence.getSchema(), sequence.getName(), DBDialect.SQLSERVER);
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
            String objectName = DBUtil.wrap(schema, oldSequenceName, DBDialect.SQLSERVER);
            String sql = "EXEC sp_rename "
                    + DBUtil.wrapString(objectName, DBDialect.SQLSERVER)
                    + ", " + DBUtil.wrapString(newSequenceName, DBDialect.SQLSERVER)
                    + ", N'OBJECT';";
            this.printSql(sql);
            Statement statement = this.getConnManager().connection().createStatement();
            statement.execute(sql);
            IOUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public void alertSequence(SqlServerAlertSequenceParam param) {
        Connection connection = null;
        boolean autoCommit = true;
        Statement statement = null;
        try {
            String sql = SqlServerSequenceAlertSqlGenerator.generateSqlSingle(param);
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
