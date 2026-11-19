package cn.oyzh.easyshell.oracle;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.oracle.check.OracleCheck;
import cn.oyzh.easyshell.oracle.column.OracleColumn;
import cn.oyzh.easyshell.oracle.column.OracleColumns;
import cn.oyzh.easyshell.oracle.column.OracleSelectColumnParam;
import cn.oyzh.easyshell.oracle.condition.OracleConditionUtil;
import cn.oyzh.easyshell.oracle.foreignKey.OracleForeignKey;
import cn.oyzh.easyshell.oracle.function.OracleAlertFunctionParam;
import cn.oyzh.easyshell.oracle.function.OracleCreateFunctionParam;
import cn.oyzh.easyshell.oracle.function.OracleFunction;
import cn.oyzh.easyshell.oracle.function.OracleSelectFunctionParam;
import cn.oyzh.easyshell.oracle.generator.function.OracleFunctionAlertSqlGenerator;
import cn.oyzh.easyshell.oracle.generator.function.OracleFunctionCreateSqlGenerator;
import cn.oyzh.easyshell.oracle.generator.procedure.OracleProcedureAlertSqlGenerator;
import cn.oyzh.easyshell.oracle.generator.procedure.OracleProcedureCreateSqlGenerator;
import cn.oyzh.easyshell.oracle.generator.sequence.OracleSequenceAlertSqlGenerator;
import cn.oyzh.easyshell.oracle.generator.sequence.OracleSequenceCreateSqlGenerator;
import cn.oyzh.easyshell.oracle.generator.table.OracleTableAlertSqlGenerator;
import cn.oyzh.easyshell.oracle.generator.table.OracleTableCreateSqlGenerator;
import cn.oyzh.easyshell.oracle.generator.view.OracleViewAlertSqlGenerator;
import cn.oyzh.easyshell.oracle.generator.view.OracleViewCreateSqlGenerator;
import cn.oyzh.easyshell.oracle.index.OracleIndex;
import cn.oyzh.easyshell.oracle.procedure.OracleAlertProcedureParam;
import cn.oyzh.easyshell.oracle.procedure.OracleCreateProcedureParam;
import cn.oyzh.easyshell.oracle.procedure.OracleProcedure;
import cn.oyzh.easyshell.oracle.procedure.OracleSelectProcedureParam;
import cn.oyzh.easyshell.oracle.record.OracleDeleteRecordParam;
import cn.oyzh.easyshell.oracle.record.OracleInsertRecordParam;
import cn.oyzh.easyshell.oracle.record.OracleRecord;
import cn.oyzh.easyshell.oracle.record.OracleRecordFilter;
import cn.oyzh.easyshell.oracle.record.OracleRecordPrimaryKey;
import cn.oyzh.easyshell.oracle.record.OracleSelectRecordParam;
import cn.oyzh.easyshell.oracle.record.OracleUpdateRecordParam;
import cn.oyzh.easyshell.oracle.routine.OracleRoutineParam;
import cn.oyzh.easyshell.oracle.schema.OracleSchema;
import cn.oyzh.easyshell.oracle.sequence.OracleAlertSequenceParam;
import cn.oyzh.easyshell.oracle.sequence.OracleCreateSequenceParam;
import cn.oyzh.easyshell.oracle.sequence.OracleSelectSequenceParam;
import cn.oyzh.easyshell.oracle.sequence.OracleSequence;
import cn.oyzh.easyshell.oracle.table.OracleAlertTableParam;
import cn.oyzh.easyshell.oracle.table.OracleCreateTableParam;
import cn.oyzh.easyshell.oracle.table.OracleSelectTableParam;
import cn.oyzh.easyshell.oracle.table.OracleTable;
import cn.oyzh.easyshell.oracle.trigger.OracleSelectTriggerParam;
import cn.oyzh.easyshell.oracle.trigger.OracleTrigger;
import cn.oyzh.easyshell.oracle.type.OracleTypeDefinition;
import cn.oyzh.easyshell.oracle.view.OracleAlertViewParam;
import cn.oyzh.easyshell.oracle.view.OracleCreateViewParam;
import cn.oyzh.easyshell.oracle.view.OracleSelectViewParam;
import cn.oyzh.easyshell.oracle.view.OracleView;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellJumpConfig;
import cn.oyzh.easyshell.domain.ShellProxyConfig;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.internal.ShellBaseClient;
import cn.oyzh.easyshell.internal.ShellClientChecker;
import cn.oyzh.easyshell.internal.ShellConnState;
import cn.oyzh.easyshell.query.oracle.OracleExecuteResult;
import cn.oyzh.easyshell.query.oracle.OracleExplainResult;
import cn.oyzh.easyshell.util.db.ShellSequenceUtil;
import cn.oyzh.easyshell.util.oracle.OracleComplexValueUtil;
import cn.oyzh.easyshell.util.oracle.ShellOracleDataUtil;
import cn.oyzh.fx.db.DBClient;
import cn.oyzh.fx.db.DBColumn;
import cn.oyzh.fx.db.DBConnConfig;
import cn.oyzh.fx.db.DBConnManager;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBFeature;
import cn.oyzh.fx.db.DBName;
import cn.oyzh.fx.db.DBObjects;
import cn.oyzh.fx.db.DBRecordData;
import cn.oyzh.fx.db.query.DBQueryResults;
import cn.oyzh.fx.db.sql.DBSqlParser;
import cn.oyzh.fx.db.sql.DBOraclePagination;
import cn.oyzh.fx.db.util.DBUtil;
import cn.oyzh.ssh.domain.SSHConnect;
import cn.oyzh.ssh.jump.SSHJumpForwarder;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.CallableStatement;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
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
import java.util.regex.Pattern;

/**
 * Oracle数据库客户端封装，提供数据库连接、表/视图/函数/过程/触发器等对象的CRUD操作，
 * 以及SQL执行、记录管理、索引/外键/检查约束管理等功能
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellOracleClient implements ShellBaseClient, DBClient {

    private static final Pattern SEQUENCE_NEXTVAL = Pattern.compile(
            "((?:\"[^\"]+\"|[A-Za-z][A-Za-z0-9_$#]*)(?:\\s*\\.\\s*(?:\"[^\"]+\"|[A-Za-z][A-Za-z0-9_$#]*))*)"
                    + "\\s*\\.\\s*NEXTVAL",
            Pattern.CASE_INSENSITIVE);

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
            this.connManager = new ShellOracleConnManager();
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
     * 构造Oracle客户端
     *
     * @param shellConnect 连接信息
     */
    public ShellOracleClient(ShellConnect shellConnect) {
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
            JulLog.warn("Oracle client start error", ex);
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
        int separator = host.lastIndexOf(':');
        String ip = host.substring(0, separator);
        int port = Integer.parseInt(host.substring(separator + 1));
        // 连接配置
        DBConnConfig connConfig = new DBConnConfig();
        connConfig.setHost(ip);
        connConfig.setPort(port);
        connConfig.setDatabase(this.shellConnect.getDatabase());
        connConfig.setUser(this.shellConnect.getUser());
        connConfig.setPassword(this.shellConnect.getPassword());
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
            JulLog.warn("Oracle client close error.", ex);
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
            Connection connection = this.getConnManager().connection(schema);
            String sql = "SELECT COUNT(*) FROM ALL_TABLES WHERE OWNER = ? AND NVL(NESTED, 'NO') = 'NO'";
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
            Connection connection = this.getConnManager().connection(schema);
            String sql = "SELECT COUNT(*) FROM ALL_VIEWS WHERE OWNER = ?";
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
    public DBQueryResults<OracleExecuteResult> executeSql(String schema, String sql) {
        DBQueryResults<OracleExecuteResult> results = new DBQueryResults<>();
        Connection connection = null;
        try {
            this.printSql(sql);
            DBSqlParser parser = DBSqlParser.getParser(sql, DBDialect.ORACLE);
            List<String> list = parser.parseSql();
            connection = this.getConnManager().connection(schema);
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();
            for (String execSql : list) {
                OracleExecuteResult result = new OracleExecuteResult();
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
            IOUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            DBUtil.rollback(connection);
            results.parseError(ex);
        }
        return results;
    }

    /**
     * 执行SQL语句
     *
     * @param sql SQL语句
     * @return 执行结果
     */
    public DBQueryResults<OracleExecuteResult> executeSql(String sql) {
        return this.executeSql(null, sql);
    }

    @Override
    public int insertBatch(String schema, List<String> sqlList) {
        return this.insertBatch(schema, sqlList, false);
    }

    @Override
    public int procedureSize(String schema) {
        int size = 0;
        try {
            Connection connection = this.getConnManager().connection(schema);
            String sql = """
                    SELECT
                        COUNT(*)
                    FROM
                        ALL_OBJECTS
                    WHERE
                        OWNER = ?
                    AND
                        OBJECT_TYPE = 'PROCEDURE'
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
            Connection connection = this.getConnManager().connection(schema);
            String sql = """
                    SELECT
                        COUNT(*)
                    FROM
                        ALL_OBJECTS
                    WHERE
                        OWNER = ?
                    AND
                        OBJECT_TYPE = 'FUNCTION'
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
    public void alertFunction(OracleAlertFunctionParam param) {
        try {
            OracleFunction function = param.getFunction();
            if (function != null && StringUtil.isBlank(function.getReturnType())) {
                OracleFunction original = this.selectFunction(param.getSchema(), function.getName());
                if (original != null && StringUtil.isNotBlank(original.getReturnType())) {
                    function.setReturnParam(original.getReturnParam());
                }
            }
            if (function == null || StringUtil.isBlank(function.getReturnType())) {
                throw new ShellException("Oracle function return type is required");
            }
            Connection connection = this.getConnManager().connection(param.getSchema());
            Statement statement = connection.createStatement();
            String sql = OracleFunctionAlertSqlGenerator.generateSqlSingle(param);
            this.printSql(sql);
            statement.executeUpdate(sql);
            IOUtil.close(statement);
            this.assertFunctionValid(connection, param.getSchema(), function.getName());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 查询触发器列表
     *
     * @param schema 模式
     * @return 触发器列表
     */
    public List<OracleTrigger> selectTriggers(String schema) {
        OracleSelectTriggerParam param = new OracleSelectTriggerParam();
        param.setFull(true);
        param.setSchema(schema);
        return this.selectTriggers(param);
    }

    /**
     * 查询触发器列表(简要信息)
     *
     * @param schema 模式
     * @return 触发器列表
     */
    public List<OracleTrigger> selectTriggersSimple(String schema) {
        OracleSelectTriggerParam param = new OracleSelectTriggerParam();
        param.setFull(false);
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
    public List<OracleTrigger> selectTriggers(String schema, String tableName) {
        OracleSelectTriggerParam param = new OracleSelectTriggerParam();
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
    //    public List<OracleTrigger> selectTriggers(OracleSelectTriggerParam param) {
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
    //            List<OracleTrigger> list = new ArrayList<>();
    //            while (resultSet.next()) {
    //                OracleTrigger trigger = new OracleTrigger();
    //                String name = resultSet.getString("TRIGGER_NAME");
    //                String type = resultSet.getString("TRIGGER_TYPE");
    //                String createDefinition = resultSet.getString("DLL");
    //                String definition = resultSet.getString("ACTION_STATEMENT");
    //                String tableName = resultSet.getString("EVENT_OBJECT_TABLE");
    //                String manipulation = resultSet.getString("EVENT_MANIPULATION");
    //                trigger.setName(name);
    //                trigger.setTableName(tableName);
    //                trigger.setCreateDefinition(createDefinition);
    //                trigger.setDefinition(ShellOracleHelper.fixTiggerDefinition(definition));
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
    public DBObjects<OracleTrigger> selectTriggers(OracleSelectTriggerParam param) {
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
            Connection connection = this.getConnManager().connection(schema);
            String actionStatementColumn = param.isFull()
                    ? "A.TRIGGER_BODY AS ACTION_STATEMENT"
                    : "NULL AS ACTION_STATEMENT";
            if (param.getTableName() == null) {
                String sql = """
                        SELECT
                            A.TRIGGER_NAME,
                            A.TRIGGER_TYPE AS TRIGGER_TYPE,
                            A.TABLE_NAME AS EVENT_OBJECT_TABLE,
                            %s,
                            A.TRIGGERING_EVENT AS EVENT_MANIPULATION,
                            NULL AS DLL
                        FROM
                            ALL_TRIGGERS A
                        WHERE
                            A.OWNER = ?
                        """.formatted(actionStatementColumn);
                statement = connection.prepareStatement(sql);
                statement.setString(1, schema);
            } else {
                String sql = """
                        SELECT
                            A.TRIGGER_NAME, 
                            A.TRIGGER_TYPE AS TRIGGER_TYPE, 
                            A.TABLE_NAME AS EVENT_OBJECT_TABLE,
                            %s,
                            A.TRIGGERING_EVENT AS EVENT_MANIPULATION, 
                            NULL AS DLL
                        FROM
                            ALL_TRIGGERS A
                        WHERE
                            A.OWNER = ?
                        AND
                            A.TABLE_NAME = ?
                        """.formatted(actionStatementColumn);
                this.printSql(sql);
                statement = connection.prepareStatement(sql);
                statement.setString(1, schema);
                statement.setString(2, param.getTableName());
            }
            ResultSet resultSet = statement.executeQuery();
            DBObjects<OracleTrigger> list = new DBObjects<>();
            while (resultSet.next()) {
                OracleTrigger trigger = new OracleTrigger();
                String name = resultSet.getString("TRIGGER_NAME");
                String type = resultSet.getString("TRIGGER_TYPE");
                String actionStatement = resultSet.getString("ACTION_STATEMENT");
                String tableName = resultSet.getString("EVENT_OBJECT_TABLE");
                String manipulation = resultSet.getString("EVENT_MANIPULATION");
                trigger.setName(name);
                trigger.setSchema(schema);
                trigger.setTableName(tableName);
                trigger.setDefinition(param.isFull()
                        ? ShellOracleHelper.fixTiggerDefinition(actionStatement) : "");
                trigger.setPolicy(type.contains("BEFORE") ? "BEFORE" : "AFTER", manipulation);
                list.add(trigger);
            }
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            if (param.isFull()) {
                for (OracleTrigger trigger : list) {
                    trigger.setCreateDefinition(ShellOracleHelper.getTriggerDefinition(connection, schema, trigger.getName()));
                }
            }
            DBName.sortItems(list);
            return list;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public List<OracleSequence> selectSequences(String schema) {
        OracleSelectSequenceParam param = new OracleSelectSequenceParam();
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
    public DBObjects<OracleSequence> selectSequences(OracleSelectSequenceParam param) {
        try {
            String schema = param.getSchema();
            PreparedStatement statement;
            Connection connection = this.getConnManager().connection(schema);
            String sql = """
                    SELECT
                        SEQUENCE_OWNER,
                        SEQUENCE_NAME,
                        MIN_VALUE,
                        MAX_VALUE,
                        CACHE_SIZE,
                        CYCLE_FLAG,
                        ORDER_FLAG,
                        INCREMENT_BY,
                        LAST_NUMBER AS CURRENT_VALUE,
                        NULL AS DLL
                    FROM
                        ALL_SEQUENCES
                    WHERE
                        SEQUENCE_OWNER = ?
                    """;
            this.printSql(sql);
            statement = connection.prepareStatement(sql);
            statement.setString(1, schema);
            ResultSet resultSet = statement.executeQuery();
            DBObjects<OracleSequence> list = new DBObjects<>();
            while (resultSet.next()) {
                OracleSequence sequence = new OracleSequence();
                String name = resultSet.getString("SEQUENCE_NAME");
                String cycleFlag = resultSet.getString("CYCLE_FLAG");
                String orderFlag = resultSet.getString("ORDER_FLAG");
                sequence.setName(name);
                sequence.setSchema(schema);
                sequence.setMinVal(sequenceNumber(resultSet, "MIN_VALUE"));
                sequence.setMaxVal(sequenceNumber(resultSet, "MAX_VALUE"));
                sequence.setCacheSize(sequenceNumber(resultSet, "CACHE_SIZE"));
                sequence.setCurrentVal(sequenceNumber(resultSet, "CURRENT_VALUE"));
                sequence.setIncrementBy(sequenceNumber(resultSet, "INCREMENT_BY"));
                sequence.setCycle(StringUtil.equalsIgnoreCase("Y", cycleFlag));
                sequence.setOrder(StringUtil.equalsIgnoreCase("Y", orderFlag));
                list.add(sequence);
            }
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            if (param.isFull()) {
                for (OracleSequence sequence : list) {
                    sequence.setCreateDefinition(ShellOracleHelper.getSequenceDefinition(
                            connection, schema, sequence.getName()));
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
     * 查询模式下的用户自定义类型，并按依赖顺序排列。
     *
     * @param schema 模式名称
     * @return 类型定义列表
     */
    public List<OracleTypeDefinition> selectTypes(String schema) {
        try {
            Connection connection = this.getConnManager().connection(schema);
            List<OracleTypeDefinition> list = new ArrayList<>();
            String sql = """
                    SELECT TYPE_NAME, TYPECODE
                    FROM ALL_TYPES
                    WHERE OWNER = ?
                      AND PREDEFINED = 'NO'
                      AND TYPECODE IN ('OBJECT', 'COLLECTION')
                    ORDER BY TYPE_NAME
                    """;
            this.printSql(sql);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, schema);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        OracleTypeDefinition type = new OracleTypeDefinition();
                        type.setName(resultSet.getString("TYPE_NAME"));
                        type.setTypeCode(resultSet.getString("TYPECODE"));
                        list.add(type);
                    }
                }
            }
            if (list.isEmpty()) {
                return list;
            }
            Map<String, OracleTypeDefinition> typeMap = new LinkedHashMap<>();
            for (OracleTypeDefinition type : list) {
                typeMap.put(type.getName().toUpperCase(), type);
            }
            this.collectTypeDependencies(connection, schema, typeMap, """
                    SELECT TYPE_NAME, ATTR_TYPE_NAME AS REFERENCED_NAME
                    FROM ALL_TYPE_ATTRS
                    WHERE OWNER = ? AND ATTR_TYPE_OWNER = ?
                    """);
            this.collectTypeDependencies(connection, schema, typeMap, """
                    SELECT TYPE_NAME, ELEM_TYPE_NAME AS REFERENCED_NAME
                    FROM ALL_COLL_TYPES
                    WHERE OWNER = ? AND ELEM_TYPE_OWNER = ?
                    """);
            this.collectTypeDependencies(connection, schema, typeMap, """
                    SELECT TYPE_NAME, SUPERTYPE_NAME AS REFERENCED_NAME
                    FROM ALL_TYPES
                    WHERE OWNER = ? AND SUPERTYPE_OWNER = ?
                    """);
            Set<String> typeBodies = new HashSet<>();
            String bodySql = "SELECT OBJECT_NAME FROM ALL_OBJECTS WHERE OWNER = ? AND OBJECT_TYPE = 'TYPE BODY'";
            try (PreparedStatement statement = connection.prepareStatement(bodySql)) {
                statement.setString(1, schema);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        typeBodies.add(resultSet.getString(1).toUpperCase());
                    }
                }
            }
            for (OracleTypeDefinition type : list) {
                type.setCreateDefinition(ShellOracleHelper.getTypeDefinition(connection, schema, type.getName()));
                if (typeBodies.contains(type.getName().toUpperCase())) {
                    type.setBodyDefinition(ShellOracleHelper.getTypeBodyDefinition(connection, schema, type.getName()));
                }
            }
            return sortTypes(list);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    private void collectTypeDependencies(Connection connection, String schema,
                                         Map<String, OracleTypeDefinition> typeMap, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, schema);
            statement.setString(2, schema);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    OracleTypeDefinition type = typeMap.get(resultSet.getString(1).toUpperCase());
                    String referenced = resultSet.getString(2);
                    if (type == null || StringUtil.isBlank(referenced)) {
                        continue;
                    }
                    String dependency = referenced.toUpperCase();
                    if (typeMap.containsKey(dependency) && !type.getDependencies().contains(dependency)) {
                        type.getDependencies().add(dependency);
                    }
                }
            }
        }
    }

    private static List<OracleTypeDefinition> sortTypes(List<OracleTypeDefinition> types) {
        Map<String, OracleTypeDefinition> typeMap = new LinkedHashMap<>();
        for (OracleTypeDefinition type : types) {
            typeMap.put(type.getName().toUpperCase(), type);
        }
        List<OracleTypeDefinition> result = new ArrayList<>(types.size());
        Set<String> visited = new HashSet<>();
        Set<String> visiting = new HashSet<>();
        for (OracleTypeDefinition type : types) {
            sortType(type, typeMap, visited, visiting, result);
        }
        return result;
    }

    private static void sortType(OracleTypeDefinition type, Map<String, OracleTypeDefinition> typeMap,
                                 Set<String> visited, Set<String> visiting, List<OracleTypeDefinition> result) {
        String name = type.getName().toUpperCase();
        if (visited.contains(name) || visiting.contains(name)) {
            return;
        }
        visiting.add(name);
        for (String dependency : type.getDependencies()) {
            OracleTypeDefinition referenced = typeMap.get(dependency);
            if (referenced != null) {
                sortType(referenced, typeMap, visited, visiting, result);
            }
        }
        visiting.remove(name);
        if (visited.add(name)) {
            result.add(type);
        }
    }

    @Override
    public String selectVersion() {
        if (this.hasProperty("version")) {
            return this.getProperty("version");
        }
        String version = "";
        try {
            Connection connection = this.getConnManager().connection();
            version = connection.getMetaData().getDatabaseProductVersion();
            this.putProperty("version", version);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return version;
    }

    /**
     * 当前Oracle版本是否支持列IDENTITY。
     *
     * @return 是否支持
     */
    public boolean supportsIdentity() {
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("\\d+")
                .matcher(this.selectVersion());
        if (!matcher.find()) {
            return false;
        }
        try {
            return Integer.parseInt(matcher.group()) >= 12;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    @Override
    public String selectProduct() {
        if (this.hasProperty("product")) {
            return this.getProperty("product");
        }
        String product = "";
        try {
            Connection connection = this.getConnManager().connection();
            product = connection.getMetaData().getDatabaseProductName();
            this.putProperty("product", product);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return product;
    }

    //public void dropEvent(String schema, OracleEvent event) {
    //    try {
    //        String sql = "DROP EVENT " + DBUtil.wrap(event.getschema(), event.getName(), DBDialect.ORACLE);
    //        this.printSql(sql);
    //        Statement statement = this.getConnManager().connection(schema).createStatement();
    //        statement.executeUpdate(sql);
    //        IOUtil.close(statement);
    //    } catch (Exception ex) {
    //        ex.printStackTrace();
    //        throw new ShellException(ex);
    //    }
    //}

    //    public void createEvent(String schema, OracleEvent event) {
    //        try {
    //            String sql = OracleEventCreateSqlGenerator.generateSql(event);
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
    //    public void alertEvent(String schema, OracleEvent event) {
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

    //    public OracleEvent selectEvent(String schema, String eventName) {
    //        try {
    //            Connection connection = this.connection(schema, null);
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
    //            OracleEvent event = new OracleEvent();
    //            while (resultSet.next()) {
    //                String expr = resultSet.getString("EXPR");
    //                String status = resultSet.getString("STATUS");
    //                String action = resultSet.getString("ACTION");
    //                event.setName(eventName);
    //                event.setStatus("TRUE".equalsIgnoreCase(status) ? "ENABLE" : "DISABLE");
    //                OracleHelper.parseJobExpr(expr, event);
    //                OracleHelper.parseJobAction(action, event);
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
    //            Connection connection = this.connection(schema, null);
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

    //    public List<OracleEvent> events(String schema) {
    //        try {
    //            Connection connection = this.connection(schema, null);
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
    //            List<OracleEvent> list = new ArrayList<>();
    //            while (resultSet.next()) {
    //                String name = resultSet.getString("NAME");
    //                String expr = resultSet.getString("EXPR");
    //                String status = resultSet.getString("STATUS");
    //                String action = resultSet.getString("ACTION");
    //                OracleEvent event = new OracleEvent();
    //                event.setName(name);
    //                event.setStatus("TRUE".equalsIgnoreCase(status) ? "ENABLE" : "DISABLE");
    //                OracleHelper.parseJobExpr(expr, event);
    //                OracleHelper.parseJobAction(action, event);
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
        ResultSet rs = statement.getGeneratedKeys();
        Long newId = null;
        if (rs.next()) {
            newId = rs.getLong(1);
        }
        IOUtil.close(rs);
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
        if (column.isDefaultValueSequence()) {
            Connection connection = statement.getConnection();
            IOUtil.close(statement);
            String sequenceName = column.getSequenceName();
            // DATA_DEFAULT中的序列引用已经带有必要的模式名和引号，不能再整体包装
            String sql = "SELECT " + sequenceName + ".CURRVAL FROM DUAL";
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
                return resultSet.getLong(1);
            }
        }
        return null;
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
    public List<OracleTable> selectTables(String schema) {
        OracleSelectTableParam param = new OracleSelectTableParam();
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
    public List<OracleTable> selectTablesSimple(String schema) {
        OracleSelectTableParam param = new OracleSelectTableParam();
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
    public List<OracleTable> selectTables(OracleSelectTableParam param) {
        try {
            String schema = param.getSchema();
            List<OracleTable> tables = new ArrayList<>();
            Connection connection = this.getConnManager().connection(schema);
            String sql = """
                    SELECT 
                        T.TABLE_NAME,
                        C.COMMENTS AS "TABLE_COMMENT",
                        T.TABLESPACE_NAME AS "TABLE_SPACE",
                        NULL AS "DLL"
                    FROM 
                        ALL_TABLES T 
                    LEFT JOIN 
                        ALL_TAB_COMMENTS C 
                    ON 
                        T.OWNER = C.OWNER 
                    AND 
                        T.TABLE_NAME = C.TABLE_NAME 
                    WHERE 
                        T.OWNER = ?
                    AND
                        NVL(T.NESTED, 'NO') = 'NO'
                    """;
            this.printSql(sql);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, schema);
            ResultSet resultSet = statement.executeQuery();
            DBUtil.printMetaData(resultSet);
            while (resultSet.next()) {
                OracleTable table = new OracleTable();
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
            if (param.isFull()) {
                for (OracleTable table : tables) {
                    table.setCreateDefinition(ShellOracleHelper.getTableDefinition(
                            connection, schema, table.getName()));
                }
            }
            DBName.sortItems(tables);
            return tables;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    //    public List<OracleTable> selectTablesSimple(String schema) {
    //        try {
    //            List<OracleTable> tables = new ArrayList<>();
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
    //                OracleTable table = new OracleTable();
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
    public List<OracleColumn> selectColumns(OracleSelectColumnParam param) {
        try {
            String schema = param.getSchema();
            String tableName = param.getTableName();
            OracleColumns columns = new OracleColumns();
            String sql = """
                    SELECT
                        C.DATA_TYPE AS "Type",
                        C.DATA_TYPE_OWNER AS "TYPE_OWNER",
                        T.TYPECODE AS "TYPE_CODE",
                        C.COLUMN_NAME AS "Field",
                        CC.COMMENTS AS "Comment",
                        C.COLUMN_ID AS "POSITION",
                        C.DATA_DEFAULT AS "Default",
                        C.DATA_SCALE AS "DATA_SCALE",
                        C.DATA_PRECISION AS "DATA_PRECISION",
                        C.DATA_LENGTH AS "DATA_LENGTH",
                        C.CHAR_LENGTH AS "CHAR_LENGTH",
                        C.CHAR_USED AS "CHAR_USED",
                        C.VIRTUAL_COLUMN AS "VIRTUAL",
                        CASE WHEN C.NULLABLE = 'Y' THEN 'YES' ELSE 'NO' END AS "Null",
                        CASE WHEN CONS.COLUMN_NAME IS NOT NULL THEN 'PRI' ELSE '' END AS "Key"
                    FROM
                        ALL_TAB_COLS C
                    LEFT JOIN
                        ALL_COL_COMMENTS CC
                    ON
                        C.OWNER = CC.OWNER
                    AND
                        C.TABLE_NAME = CC.TABLE_NAME
                    AND
                        C.COLUMN_NAME = CC.COLUMN_NAME
                    LEFT JOIN
                        ALL_TYPES T
                    ON
                        T.OWNER = C.DATA_TYPE_OWNER
                    AND
                        T.TYPE_NAME = C.DATA_TYPE
                    LEFT JOIN
                        ALL_CONS_COLUMNS CONS
                    ON
                        C.OWNER = CONS.OWNER
                    AND
                        C.TABLE_NAME = CONS.TABLE_NAME
                    AND
                        C.COLUMN_NAME = CONS.COLUMN_NAME
                    AND
                        EXISTS (SELECT 1 FROM ALL_CONSTRAINTS AC WHERE AC.OWNER = CONS.OWNER AND AC.CONSTRAINT_NAME = CONS.CONSTRAINT_NAME AND AC.CONSTRAINT_TYPE = 'P')
                    WHERE
                        C.OWNER = ?
                    AND
                        C.TABLE_NAME = ?
                    AND
                        C.HIDDEN_COLUMN = 'NO'
                    """;
            Connection connection = this.getConnManager().connection(schema);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, schema);
            statement.setString(2, tableName);
            ResultSet resultSet = statement.executeQuery();
            DBUtil.printMetaData(resultSet);
            while (resultSet.next()) {
                Object def = resultSet.getObject("Default");
                if (def instanceof CharSequence sequence) {
                    def = sequence.toString().strip();
                }
                String key = resultSet.getString("Key");
                String type = resultSet.getString("Type");
                String field = resultSet.getString("Field");
                int position = resultSet.getInt("POSITION");
                String nullable = resultSet.getString("Null");
                int dataScale = resultSet.getInt("DATA_SCALE");
                String comment = resultSet.getString("Comment");
                int dataLength = resultSet.getInt("DATA_LENGTH");
                OracleColumn column = new OracleColumn();
                column.parseKey(key);
                column.setName(field);
                column.setSize(columnSize(type, dataLength, resultSet.getInt("DATA_PRECISION"),
                        resultSet.getInt("CHAR_LENGTH"), resultSet.getString("CHAR_USED")));
                column.setDigits(dataScale);
                column.setType(type);
                column.setTypeOwner(resultSet.getString("TYPE_OWNER"));
                column.setTypeCode(resultSet.getString("TYPE_CODE"));
                column.setSchema(schema);
                column.setComment(comment);
                column.setDefaultValue(def);
                column.setPosition(position);
                column.setTableName(tableName);
                column.setNullable("yes".equalsIgnoreCase(nullable));
                column.setVirtual(StringUtil.equalsIgnoreCase("YES", resultSet.getString("VIRTUAL")));
                columns.add(column);
            }
            IOUtil.close(resultSet);
            for (OracleColumn column : columns) {
                if (column.isObjectType() || column.isCollectionType()) {
                    try {
                        column.setTypeMeta(OracleComplexValueUtil.loadTypeMeta(
                                connection, column.getTypeOwner(), column.getType()));
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
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
     * 初始化自动递增
     *
     * @param connection 连接
     * @param schema     模式
     * @param name       名称
     * @param columns    字段列表
     * @throws SQLException 异常
     */
    private void initAutoIncrement(Connection connection, String schema, String name, OracleColumns columns, boolean isView) throws SQLException {
        // 判断自增
        DatabaseMetaData dbmd = connection.getMetaData();
        ResultSet resultSet = dbmd.getColumns(null, schema, name, null);
        DBUtil.printMetaData(resultSet);
        while (resultSet.next()) {
            String columnName = resultSet.getString("COLUMN_NAME");
            String isAutoIncrement = resultSet.getString("IS_AUTOINCREMENT");
            boolean isIdentity = StringUtil.equalsIgnoreCase(isAutoIncrement, "YES");
            OracleColumn column = columns.column(columnName);
            if (column != null && isIdentity) {
                column.setAutoIncrement(isIdentity);
                column.setAutoIncrementType("IDENTITY");
            }
        }
        IOUtil.close(resultSet);
        if (isView) {
            return;
        }
        String triggerSql = "SELECT TRIGGER_NAME, TRIGGER_BODY FROM ALL_TRIGGERS WHERE OWNER = ? AND TABLE_NAME = ?";
        try (PreparedStatement triggerStatement = connection.prepareStatement(triggerSql)) {
            triggerStatement.setString(1, schema);
            triggerStatement.setString(2, name);
            try (ResultSet triggerResultSet = triggerStatement.executeQuery()) {
                while (triggerResultSet.next()) {
                    String triggerName = triggerResultSet.getString("TRIGGER_NAME");
                    String triggerBody = StringUtil.blankToDefault(triggerResultSet.getString("TRIGGER_BODY"), "");
                    for (OracleColumn column : columns) {
                        if (!column.isIdentityAutoIncrementType()
                                && assignsSequenceNextValue(triggerBody, column.getName())) {
                            column.setAutoIncrement(true);
                            column.setAutoIncrementType("SEQUENCE_TRIGGER");
                            column.setAutoIncrementTriggerName(triggerName);
                            column.setAutoIncrementSequenceName(sequenceName(triggerBody, column.getName()));
                        }
                    }
                }
            }
        }
    }

    private static boolean assignsSequenceNextValue(String triggerBody, String columnName) {
        return findSequenceNextValueStatement(triggerBody, columnName) != null;
    }

    private static String sequenceName(String triggerBody, String columnName) {
        String statement = findSequenceNextValueStatement(triggerBody, columnName);
        if (statement == null) {
            return null;
        }
        java.util.regex.Matcher matcher = SEQUENCE_NEXTVAL.matcher(statement);
        if (!matcher.find()) {
            return null;
        }
        String qualifiedName = matcher.group(1);
        int separator = qualifiedName.lastIndexOf('.');
        String sequenceName = separator >= 0 ? qualifiedName.substring(separator + 1) : qualifiedName;
        if (sequenceName.length() > 1 && sequenceName.startsWith("\"") && sequenceName.endsWith("\"")) {
            sequenceName = sequenceName.substring(1, sequenceName.length() - 1).replace("\"\"", "\"");
        }
        return sequenceName;
    }

    private static String findSequenceNextValueStatement(String triggerBody, String columnName) {
        if (StringUtil.isBlank(triggerBody) || StringUtil.isBlank(columnName)) {
            return null;
        }
        String normalizedColumn = Pattern.quote(columnName.toUpperCase());
        Pattern target = Pattern.compile(":NEW\\s*\\.\\s*\"?" + normalizedColumn + "\"?", Pattern.CASE_INSENSITIVE);
        for (String statement : triggerBody.split(";")) {
            if (statement.toUpperCase().contains("NEXTVAL") && target.matcher(statement).find()) {
                return statement;
            }
        }
        return null;
    }

    private static int columnSize(String type, int dataLength, int precision, int charLength, String charUsed) {
        if (StringUtil.containsAnyIgnoreCase(type, "NUMBER", "DECIMAL", "NUMERIC", "FLOAT", "REAL", "DOUBLE PRECISION")
                && precision > 0) {
            return precision;
        }
        return "C".equalsIgnoreCase(charUsed) && charLength > 0 ? charLength : dataLength;
    }

    private static BigInteger sequenceNumber(ResultSet resultSet, String column) throws SQLException {
        return ShellSequenceUtil.getBigInteger(resultSet, column);
    }

    /**
     * 查询记录列表
     *
     * @param param 查询参数
     * @return 记录列表
     */
    public List<OracleRecord> selectRecords(OracleSelectRecordParam param) {
        try {
            Connection connection = this.getConnManager().connection(param.getSchema());
            StringBuilder builder = new StringBuilder("SELECT * FROM ");
            builder.append(DBUtil.wrap(param.getSchema(), param.getTableName(), DBDialect.ORACLE));
            String filterCondition = DBUtil.buildCondition(param.getFilters());
            if (StringUtil.isNotBlank(filterCondition)) {
                builder.append(" WHERE ").append(filterCondition);
            }
            String sql = builder.toString();
            if (param.hasPageControl()) {
                sql = DBOraclePagination.paginate(sql, param.getStart(), param.getLimit(), this.selectVersion());
            }
            this.printSql(sql);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            DBUtil.printMetaData(resultSet);
            List<OracleRecord> records = new ArrayList<>();
            List<OracleColumn> columns;
            if (param.getColumns() != null) {
                columns = param.getColumns();
            } else {
                columns = ShellOracleHelper.parseColumns(resultSet);
            }
            while (resultSet.next()) {
                OracleRecord record = new OracleRecord(columns, param.isReadonly());
                for (OracleColumn column : columns) {
                    Object data = resultSet.getObject(column.getName());
                    if (column.isObjectType() || column.isCollectionType()) {
                        data = OracleComplexValueUtil.toJsonValue(connection, data);
                    } else if (data instanceof oracle.sql.TIMESTAMPLTZ) {
                        data = ShellOracleDataUtil.valueStandardization(connection, data);
                    }
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
    public long selectRecordCount(OracleSelectRecordParam param) {
        long count = 0;
        try {
            Connection connection = this.getConnManager().connection(param.getSchema());
            StringBuilder builder = new StringBuilder("SELECT COUNT(*) FROM ");
            builder.append(DBUtil.wrap(param.getSchema(), param.getTableName(), DBDialect.ORACLE));
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
    public int insertRecord(OracleInsertRecordParam param) {
        if (param == null || param.getRecord() == null) {
            return 0;
        }
        try {
            OracleRecordPrimaryKey primaryKey = param.getPrimaryKey();
            boolean returnGeneratedKey = primaryKey != null && primaryKey.shouldReturnData();
            DBRecordData recordData = param.getRecord();
            List<DBColumn> insertColumns = recordData.entries().stream()
                    .map(Map.Entry::getKey)
                    .filter(column -> !recordData.isDefaultValueSequence(column.getName()))
                    .toList();
            boolean directLobInsert = insertColumns.stream()
                    .anyMatch(column -> this.isLobColumn(column) || this.isComplexType(column));
            StringBuilder builder = new StringBuilder();
            builder.append("INSERT INTO ")
                    .append(DBUtil.wrap(param.getSchema(), param.getTableName(), DBDialect.ORACLE))
                    .append("(");
            for (DBColumn column : insertColumns) {
                builder.append(DBUtil.wrap(column.getName(), DBDialect.ORACLE)).append(",");
            }
            StringUtil.deleteLast(builder);
            builder.append(")");
            builder.append(" VALUES(");
            for (DBColumn column : insertColumns) {
                if (this.isXmlTypeColumn(column) && this.hasXmlContent(recordData.value(column.getName()))) {
                    builder.append("XMLTYPE(?),");
                } else {
                    builder.append("?,");
                }
            }
            StringUtil.deleteLast(builder);
            builder.append(")");
            Connection connection = this.getConnManager().connection(param.getSchema());
            String insertSql = builder.toString();
            if (directLobInsert) {
                return this.insertRecordDirect(connection, insertSql, insertColumns, recordData, primaryKey,
                        param.getSchema(), param.getTableName());
            }
            String sql = insertSql;
            if (returnGeneratedKey) {
                sql = "BEGIN " + sql + " RETURNING "
                        + DBUtil.wrap(primaryKey.getColumnName(), DBDialect.ORACLE)
                        + " INTO ?; END;";
            }
            this.printSql(sql);
            CallableStatement statement = connection.prepareCall(sql);
            int index = 1;
            for (DBColumn column : insertColumns) {
                DBUtil.setVal(statement, recordData.value(column.getName()), index++);
            }
            if (returnGeneratedKey) {
                statement.registerOutParameter(index, java.sql.Types.NUMERIC);
            }
            int count;
            if (returnGeneratedKey) {
                statement.execute();
                count = 1;
            } else {
                count = statement.executeUpdate();
            }
            // 处理自动递增值
            if (primaryKey != null) {
                Long newId;
                // 正常主键
                if (primaryKey.shouldReturnData()) {
                    newId = statement.getLong(index);
                } else {// 处理序列值
                    newId = this.getSequenceCurrVal(statement, primaryKey.getColumn());
                    primaryKey.setData(newId);
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

    private int insertRecordDirect(Connection connection, String insertSql, List<DBColumn> insertColumns,
                                   DBRecordData recordData, OracleRecordPrimaryKey primaryKey,
                                   String schema, String tableName) throws Exception {
        try {
            return this.insertRecordDirectOnce(connection, insertSql, insertColumns, recordData, primaryKey);
        } catch (SQLException ex) {
            if (ex.getErrorCode() != 22295 || !this.requiresSplitInsert(insertColumns)) {
                throw ex;
            }
            return this.insertRecordSplit(connection, insertColumns, recordData, primaryKey, schema, tableName);
        }
    }

    private int insertRecordDirectOnce(Connection connection, String insertSql, List<DBColumn> insertColumns,
                                       DBRecordData recordData, OracleRecordPrimaryKey primaryKey) throws Exception {
        boolean returnGeneratedKey = primaryKey != null && primaryKey.shouldReturnData();
        boolean identity = returnGeneratedKey && primaryKey.getColumn().isIdentityAutoIncrementType();
        this.printSql(insertSql);
        int count;
        Long generatedKey = null;
        if (identity) {
            try (PreparedStatement statement = connection.prepareStatement(
                    insertSql, new String[]{primaryKey.getColumnName()})) {
                this.bindInsertValues(statement, insertColumns, recordData);
                count = statement.executeUpdate();
                try (ResultSet resultSet = statement.getGeneratedKeys()) {
                    if (resultSet.next()) {
                        generatedKey = resultSet.getLong(1);
                    }
                }
            }
        } else {
            try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
                this.bindInsertValues(statement, insertColumns, recordData);
                count = statement.executeUpdate();
            }
            if (returnGeneratedKey) {
                generatedKey = this.getAutoIncrementCurrVal(connection, primaryKey.getColumn());
            }
        }
        if (generatedKey == null && primaryKey != null && primaryKey.getColumn().isDefaultValueSequence()) {
            generatedKey = this.getAutoIncrementCurrVal(connection, primaryKey.getColumn());
        }
        if (primaryKey != null && generatedKey != null) {
            primaryKey.setReturnData(generatedKey);
            if (primaryKey.getData() == null) {
                primaryKey.setData(generatedKey);
            }
        }
        return count;
    }

    /**
     * Oracle 11g不允许在同一条语句中同时绑定超过4000字节的LONG和LOB数据，
     * 此时先插入普通列和LONG列并返回ROWID，再按ROWID更新LOB列。
     */
    private int insertRecordSplit(Connection connection, List<DBColumn> insertColumns,
                                  DBRecordData recordData, OracleRecordPrimaryKey primaryKey,
                                  String schema, String tableName) throws Exception {
        List<DBColumn> deferredColumns = insertColumns.stream()
                .filter(this::isDeferredLobColumn)
                .toList();
        List<DBColumn> baseColumns = insertColumns.stream()
                .filter(column -> !this.isDeferredLobColumn(column))
                .toList();
        boolean returnGeneratedKey = primaryKey != null && primaryKey.shouldReturnData();
        StringBuilder builder = new StringBuilder("BEGIN INSERT INTO ")
                .append(DBUtil.wrap(schema, tableName, DBDialect.ORACLE))
                .append("(");
        for (DBColumn column : baseColumns) {
            builder.append(DBUtil.wrap(column.getName(), DBDialect.ORACLE)).append(",");
        }
        StringUtil.deleteLast(builder);
        builder.append(") VALUES(");
        for (int i = 0; i < baseColumns.size(); i++) {
            builder.append("?,");
        }
        StringUtil.deleteLast(builder);
        builder.append(") RETURNING ROWID");
        if (returnGeneratedKey) {
            builder.append(", ").append(DBUtil.wrap(primaryKey.getColumnName(), DBDialect.ORACLE));
        }
        builder.append(" INTO ?");
        if (returnGeneratedKey) {
            builder.append(", ?");
        }
        builder.append("; END;");

        String sql = builder.toString();
        this.printSql(sql);
        String rowid;
        Long generatedKey = null;
        try (CallableStatement statement = connection.prepareCall(sql)) {
            int index = 1;
            for (DBColumn column : baseColumns) {
                this.bindInsertValue(statement, column, recordData.value(column.getName()), index++);
            }
            statement.registerOutParameter(index, Types.ROWID);
            int rowidIndex = index++;
            int keyIndex = 0;
            if (returnGeneratedKey) {
                statement.registerOutParameter(index, Types.NUMERIC);
                keyIndex = index;
            }
            statement.execute();
            rowid = statement.getString(rowidIndex);
            if (returnGeneratedKey) {
                generatedKey = statement.getLong(keyIndex);
            }
        }
        if (generatedKey == null && primaryKey != null && primaryKey.getColumn().isDefaultValueSequence()) {
            generatedKey = this.getAutoIncrementCurrVal(connection, primaryKey.getColumn());
        }
        if (primaryKey != null && generatedKey != null) {
            primaryKey.setReturnData(generatedKey);
            if (primaryKey.getData() == null) {
                primaryKey.setData(generatedKey);
            }
        }
        for (DBColumn column : deferredColumns) {
            StringBuilder update = new StringBuilder("UPDATE ")
                    .append(DBUtil.wrap(schema, tableName, DBDialect.ORACLE))
                    .append(" SET ")
                    .append(DBUtil.wrap(column.getName(), DBDialect.ORACLE))
                    .append("=")
                    .append(this.isXmlTypeColumn(column)
                            && this.hasXmlContent(recordData.value(column.getName())) ? "XMLTYPE(?)" : "?")
                    .append(" WHERE ROWID=?");
            String updateSql = update.toString();
            this.printSql(updateSql);
            try (PreparedStatement statement = connection.prepareStatement(updateSql)) {
                this.bindInsertValue(statement, column, recordData.value(column.getName()), 1);
                statement.setString(2, rowid);
                statement.executeUpdate();
            }
        }
        return 1;
    }

    private void bindInsertValues(PreparedStatement statement, List<DBColumn> insertColumns,
                                  DBRecordData recordData) throws SQLException {
        int index = 1;
        for (DBColumn column : insertColumns) {
            this.bindInsertValue(statement, column, recordData.value(column.getName()), index++);
        }
    }

    private void bindInsertValue(PreparedStatement statement, DBColumn column, Object value, int index)
            throws SQLException {
        this.bindValue(statement.getConnection(), statement, column, value, index);
    }

    private void bindValue(Connection connection, PreparedStatement statement, DBColumn column, Object value,
                           int index) throws SQLException {
        if (value == null) {
            if (column instanceof OracleColumn oracleColumn
                    && (oracleColumn.isObjectType() || oracleColumn.isCollectionType())) {
                OracleComplexValueUtil.setNull(statement, index, oracleColumn);
            } else {
                statement.setNull(index, Types.NULL);
            }
        } else if (column == null) {
            DBUtil.setVal(statement, value, index);
        } else if (column instanceof OracleColumn oracleColumn
                && (oracleColumn.isObjectType() || oracleColumn.isCollectionType())) {
            Object jdbcValue = OracleComplexValueUtil.toJdbcValue(connection, oracleColumn, value);
            if (jdbcValue == null) {
                OracleComplexValueUtil.setNull(statement, index, oracleColumn);
            } else {
                statement.setObject(index, jdbcValue);
            }
        } else if (this.isXmlTypeColumn(column) && value instanceof CharSequence sequence) {
            String text = sequence.toString();
            if (text.isEmpty()) {
                statement.setNull(index, Types.NULL);
            } else {
                statement.setClob(index, new StringReader(text), text.length());
            }
        } else if (this.isClobColumn(column) && value instanceof CharSequence sequence) {
            String text = sequence.toString();
            statement.setCharacterStream(index, new StringReader(text), text.length());
        } else if (this.isNclobColumn(column) && value instanceof CharSequence sequence) {
            String text = sequence.toString();
            statement.setNCharacterStream(index, new StringReader(text), text.length());
        } else if (this.isBlobColumn(column) && value instanceof byte[] bytes) {
            statement.setBinaryStream(index, new ByteArrayInputStream(bytes), bytes.length);
        } else {
            DBUtil.setVal(statement, value, index);
        }
    }

    private boolean isLobColumn(DBColumn column) {
        return column.supportText() || column.supportBinary();
    }

    private boolean isComplexType(DBColumn column) {
        return column instanceof OracleColumn oracleColumn
                && (oracleColumn.isObjectType() || oracleColumn.isCollectionType());
    }

    private boolean isLongColumn(DBColumn column) {
        return StringUtil.equalsAnyIgnoreCase(column.getType(), "LONG", "LONG RAW");
    }

    private boolean isActualLobColumn(DBColumn column) {
        return StringUtil.equalsAnyIgnoreCase(column.getType(), "CLOB", "NCLOB", "BLOB");
    }

    private boolean isDeferredLobColumn(DBColumn column) {
        return this.isActualLobColumn(column) || this.isXmlTypeColumn(column);
    }

    private boolean requiresSplitInsert(List<DBColumn> insertColumns) {
        return insertColumns.stream().anyMatch(this::isLongColumn)
                && insertColumns.stream().anyMatch(this::isActualLobColumn);
    }

    private boolean isXmlTypeColumn(DBColumn column) {
        return StringUtil.equalsIgnoreCase(column.getType(), "XMLTYPE");
    }

    private boolean hasXmlContent(Object value) {
        return value instanceof CharSequence sequence && !sequence.isEmpty();
    }

    private boolean isClobColumn(DBColumn column) {
        return StringUtil.equalsAnyIgnoreCase(column.getType(), "CLOB", "LONG");
    }

    private boolean isNclobColumn(DBColumn column) {
        return StringUtil.equalsIgnoreCase(column.getType(), "NCLOB");
    }

    private boolean isBlobColumn(DBColumn column) {
        return StringUtil.equalsAnyIgnoreCase(column.getType(), "BLOB", "LONG RAW");
    }

    private Long getAutoIncrementCurrVal(Connection connection, OracleColumn column) throws SQLException {
        String sequenceName = StringUtil.blankToDefault(
                column.getAutoIncrementSequenceName(), column.getSequenceName());
        if (StringUtil.isBlank(sequenceName)) {
            return null;
        }
        // 序列名可能来自触发器或DATA_DEFAULT，可能已经带模式名/引号
        String sql = "SELECT " + sequenceName + ".CURRVAL FROM DUAL";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            return resultSet.next() ? resultSet.getLong(1) : null;
        }
    }

    /**
     * 删除记录
     *
     * @param param 删除参数
     * @return 受影响的行数
     */
    public int deleteRecord(OracleDeleteRecordParam param) {
        try {
            int updateCount;
            String schema = param.getSchema();
            String tableName = param.getTableName();
            Connection connection = this.getConnManager().connection(schema);
            StringBuilder builder = new StringBuilder();
            builder.append("DELETE FROM ")
                    .append(DBUtil.wrap(schema, tableName, DBDialect.ORACLE))
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
                        builder.append(DBUtil.wrap(colName, DBDialect.ORACLE))
                                .append(" = ?");
                    } else {
                        builder.append(DBUtil.wrap(colName, DBDialect.ORACLE))
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
                OracleRecordPrimaryKey primaryKey = param.getPrimaryKey();
                builder.append(DBUtil.wrap(primaryKey.getColumnName(), DBDialect.ORACLE))
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
    public int updateRecord(OracleUpdateRecordParam param) {
        try {
            int updateCount;
            String schema = param.getSchema();
            String tableName = param.getTableName();
            DBRecordData recordData = param.getUpdateRecord();
            List<Map.Entry<DBColumn, Object>> setEntries = new ArrayList<>(recordData.entries());
            StringBuilder builder = new StringBuilder();
            builder.append("UPDATE ")
                    .append(DBUtil.wrap(schema, tableName, DBDialect.ORACLE))
                    .append(" SET ");
            for (Map.Entry<DBColumn, Object> entry : setEntries) {
                builder.append(DBUtil.wrap(entry.getKey().getName(), DBDialect.ORACLE)).append(" = ?,");
            }
            builder.deleteCharAt(builder.length() - 1);
            builder.append(" WHERE ");
            Connection connection = this.getConnManager().connection(schema);
            if (param.getPrimaryKey() == null) {
                DBRecordData originalRecordData = param.getRecord();
                List<Map.Entry<DBColumn, Object>> whereEntries = new ArrayList<>(originalRecordData.entries());
                // 参数
                boolean first = true;
                for (Map.Entry<DBColumn, Object> entry : whereEntries) {
                    if (first) {
                        first = false;
                    } else {
                        builder.append(" AND ");
                    }
                    String column = entry.getKey().getName();
                    Object value = entry.getValue();
                    if (value == null) {
                        builder.append(DBUtil.wrap(column, DBDialect.ORACLE)).append(" IS NULL");
                    } else {
                        builder.append(DBUtil.wrap(column, DBDialect.ORACLE)).append(" = ?");
                    }
                }
                int index = 1;
                String sql = builder.toString();
                this.printSql(sql);
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    // 设置值
                    for (Map.Entry<DBColumn, Object> entry : setEntries) {
                        this.bindValue(connection, statement, entry.getKey(), entry.getValue(), index++);
                    }
                    // 设置参数
                    for (Map.Entry<DBColumn, Object> entry : whereEntries) {
                        if (entry.getValue() != null) {
                            this.bindValue(connection, statement, entry.getKey(), entry.getValue(), index++);
                        }
                    }
                    updateCount = DBUtil.executeUpdate(statement);
                }
            } else {
                OracleRecordPrimaryKey primaryKey = param.getPrimaryKey();
                builder.append(DBUtil.wrap(primaryKey.getColumnName(), DBDialect.ORACLE)).append(" = ?");
                String sql = builder.toString();
                DBUtil.printInfo(sql, recordData);
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    int index = 1;
                    // 设置值
                    for (Map.Entry<DBColumn, Object> entry : setEntries) {
                        this.bindValue(connection, statement, entry.getKey(), entry.getValue(), index++);
                    }
                    // 设置参数
                    this.bindValue(connection, statement, primaryKey.getColumn(), primaryKey.originalData(), index);
                    updateCount = DBUtil.executeUpdate(statement);
                }
            }
            return updateCount;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
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
            Connection connection = this.getConnManager().connection(schema);
            return ShellOracleHelper.getTableDefinition(connection, schema, tableName);
        } catch (Exception ex) {
            ex.printStackTrace();
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
        try {
            Connection connection = this.getConnManager().connection(schema);
            return ShellOracleHelper.getViewDefinition(connection, schema, viewName);
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
            Connection connection = this.getConnManager().connection(schema);
            return ShellOracleHelper.getFunctionDefinition(connection, schema, functionName);
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
            Connection connection = this.getConnManager().connection(schema);
            return ShellOracleHelper.getSequenceDefinition(connection, schema, sequenceName);
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
        try {
            Connection connection = this.getConnManager().connection(schema);
            return ShellOracleHelper.getProcedureDefinition(connection, schema, procedureName);
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
            Connection connection = this.getConnManager().connection(schema);
            return ShellOracleHelper.getTriggerDefinition(connection, schema, triggerName);
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
            String sql = "SELECT TABLESPACE_NAME FROM USER_TABLESPACES ORDER BY TABLESPACE_NAME";
            this.printSql(sql);
            Statement statement = this.getConnManager().connection().createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            DBUtil.printMetaData(resultSet);
            while (resultSet.next()) {
                engines.add(resultSet.getString(1));
            }
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
    public List<OracleSchema> selectSchemas() {
        try {
            Statement statement = this.getConnManager().connection().createStatement();
            // 查询当前用户有权限访问的schema：优先显示有对象的schema，也包含当前用户自己的schema
            //            String sql = """
            //                    SELECT DISTINCT OWNER FROM ALL_OBJECTS
            //                    UNION
            //                    SELECT USERNAME FROM ALL_USERS
            //                    ORDER BY 1
            //                    """;

            List<OracleSchema> list = new ArrayList<>();
            String sql = "SELECT USERNAME AS SCHEMA_NAME FROM ALL_USERS ORDER BY USERNAME";
            this.printSql(sql);
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                OracleSchema schema = new OracleSchema();
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
    public OracleSchema schema(String schema) {
        try {
            OracleSchema dbSchema = new OracleSchema();
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
    public OracleTable selectTable(String schema, String tableName) {
        OracleSelectTableParam param = new OracleSelectTableParam();
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
    public OracleTable selectTableSimple(String schema, String tableName) {
        OracleSelectTableParam param = new OracleSelectTableParam();
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
    public OracleTable selectTable(OracleSelectTableParam param) {
        try {
            String schema = param.getSchema();
            String tableName = param.getTableName();
            OracleTable table = new OracleTable();
            table.setSchema(schema);
            table.setName(tableName);
            Connection connection = this.getConnManager().connection(schema);
            String sql = """
                    SELECT 
                        C.COMMENTS AS "TABLE_COMMENT",
                        T.TABLESPACE_NAME AS "TABLE_SPACE",
                        NULL AS "DLL"
                    FROM 
                        ALL_TABLES T 
                    LEFT JOIN 
                        ALL_TAB_COMMENTS C
                    ON 
                        T.OWNER = C.OWNER 
                    AND 
                        T.TABLE_NAME = C.TABLE_NAME 
                    WHERE 
                        T.OWNER = ? 
                    AND 
                        T.TABLE_NAME = ?    
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
            if (param.isFull()) {
                table.setCreateDefinition(ShellOracleHelper.getTableDefinition(connection, schema, tableName));
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
    public OracleView selectView(String schema, String viewName) {
        OracleSelectViewParam param = new OracleSelectViewParam();
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
    public OracleView selectViewSimple(String schema, String viewName) {
        OracleSelectViewParam param = new OracleSelectViewParam();
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
    public OracleView selectView(OracleSelectViewParam param) {
        try {
            String schema = param.getSchema();
            String viewName = param.getViewName();
            String definitionColumn = param.isFull() ? "V.TEXT AS DEFINITION" : "NULL AS DEFINITION";
            String securityTypeColumn = this.supportsBequeath()
                    ? "V.BEQUEATH AS SECURITY_TYPE"
                    : "NULL AS SECURITY_TYPE";
            String sql = """
                    SELECT
                        V.VIEW_NAME,
                        %s,
                        %s,
                        C.COMMENTS AS TABLE_COMMENT,
                        NULL AS "DLL"
                    FROM 
                        ALL_VIEWS V
                    LEFT JOIN 
                        ALL_TAB_COMMENTS C 
                    ON 
                        V.OWNER = C.OWNER
                    AND 
                        V.VIEW_NAME = C.TABLE_NAME 
                    WHERE 
                        V.OWNER = ?
                    AND
                        V.VIEW_NAME = ?
                    """.formatted(definitionColumn, securityTypeColumn);
            this.printSql(sql);
            Connection connection = this.getConnManager().connection(schema);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, schema);
            statement.setString(2, viewName);
            // 执行SQL查询并获取结果集
            ResultSet resultSet = statement.executeQuery();
            // 打印元数据
            DBUtil.printMetaData(resultSet);
            // 遍历结果集
            OracleView view = new OracleView();
            while (resultSet.next()) {
                String name = resultSet.getString("VIEW_NAME");
                String definition = param.isFull()
                        ? ShellOracleHelper.stripViewReadOnlyClause(resultSet.getString("DEFINITION"))
                        : "";
                String tableComment = resultSet.getString("TABLE_COMMENT");
                view.setName(name);
                view.setSchema(schema);
                view.setComment(tableComment);
                view.setDefinition(definition);
                view.setSecurityType(resultSet.getString("SECURITY_TYPE"));
                view.setUpdatable(true);
            }
            // 关闭连接和释放资源
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            if (param.isFull()) {
                String createDefinition = ShellOracleHelper.getViewDefinition(connection, schema, viewName);
                String readOnly = ShellOracleHelper.getViewReadOnly(connection, schema, viewName);
                if (readOnly == null) {
                    readOnly = StringUtil.containsIgnoreCase(createDefinition, "WITH READ ONLY") ? "Y" : "N";
                }
                view.setCreateDefinition(createDefinition);
                view.setUpdatable(!StringUtil.equalsIgnoreCase("Y", readOnly));
                if (StringUtil.isBlank(view.getSecurityType())) {
                    view.setSecurityType(parseSecurityType(createDefinition));
                }
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
    public List<OracleView> selectViews(String schema) {
        OracleSelectViewParam param = new OracleSelectViewParam();
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
    public List<OracleView> selectViewsSimple(String schema) {
        OracleSelectViewParam param = new OracleSelectViewParam();
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
    public List<OracleView> selectViews(OracleSelectViewParam param) {
        try {
            String schema = param.getSchema();
            List<OracleView> list = new ArrayList<>();
            String definitionColumn = param.isFull() ? "V.TEXT AS DEFINITION" : "NULL AS DEFINITION";
            String securityTypeColumn = this.supportsBequeath()
                    ? "V.BEQUEATH AS SECURITY_TYPE"
                    : "NULL AS SECURITY_TYPE";
            String sql = """
                    SELECT
                        V.VIEW_NAME,
                        %s,
                        %s,
                        C.COMMENTS AS TABLE_COMMENT,
                        NULL AS "DLL"
                    FROM
                        ALL_VIEWS V
                    LEFT JOIN
                        ALL_TAB_COMMENTS C
                    ON
                        V.OWNER = C.OWNER
                    AND
                        V.VIEW_NAME = C.TABLE_NAME
                    WHERE
                        V.OWNER = ?
                    """.formatted(definitionColumn, securityTypeColumn);

            this.printSql(sql);
            Connection connection = this.getConnManager().connection(schema);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, schema);
            ResultSet resultSet = statement.executeQuery();
            // 打印元数据
            DBUtil.printMetaData(resultSet);
            // 遍历结果集
            while (resultSet.next()) {
                OracleView view = new OracleView();
                String name = resultSet.getString("VIEW_NAME");
                String definition = param.isFull()
                        ? ShellOracleHelper.stripViewReadOnlyClause(resultSet.getString("DEFINITION"))
                        : "";
                String tableComment = resultSet.getString("TABLE_COMMENT");
                view.setSchema(schema);
                view.setName(name);
                view.setComment(tableComment);
                view.setDefinition(definition);
                view.setSecurityType(resultSet.getString("SECURITY_TYPE"));
                view.setUpdatable(true);
                list.add(view);
            }
            // 关闭连接和释放资源
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            if (param.isFull()) {
                for (OracleView view : list) {
                    String name = view.getName();
                    String createDefinition = ShellOracleHelper.getViewDefinition(connection, schema, name);
                    String readOnly = ShellOracleHelper.getViewReadOnly(connection, schema, name);
                    if (readOnly == null) {
                        readOnly = StringUtil.containsIgnoreCase(createDefinition, "WITH READ ONLY") ? "Y" : "N";
                    }
                    view.setCreateDefinition(createDefinition);
                    view.setUpdatable(!StringUtil.equalsIgnoreCase("Y", readOnly));
                    if (StringUtil.isBlank(view.getSecurityType())) {
                        view.setSecurityType(parseSecurityType(createDefinition));
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
    public void dropView(OracleView view) {
        try {
            String sql = "DROP VIEW " + DBUtil.wrap(view.getSchema(), view.getName(), DBDialect.ORACLE);
            Statement statement = this.getConnManager().connection(view.getSchema()).createStatement();
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
            String sql = "SELECT COUNT(*) FROM ALL_VIEWS WHERE OWNER = ? AND VIEW_NAME = ?";
            PreparedStatement statement = this.getConnManager().connection(schema).prepareStatement(sql);
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
     * 判断数据库对象是否存在
     *
     * @param schema     模式名称
     * @param objectName 对象名称
     * @param objectType 对象类型
     * @return 存在返回true，否则返回false
     */
    public boolean existObject(String schema, String objectName, String objectType) {
        boolean result;
        try {
            String sql = "SELECT COUNT(*) FROM ALL_OBJECTS WHERE OWNER = ? AND OBJECT_NAME = ? AND OBJECT_TYPE = ?";
            PreparedStatement statement = this.getConnManager().connection(schema).prepareStatement(sql);
            statement.setString(1, schema);
            statement.setString(2, objectName);
            statement.setString(3, objectType);
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
     * 查询当前连接用户的默认表空间和临时表空间。
     *
     * @param schema 模式名称
     * @return 表空间描述
     */
    public String describeUserTablespaces(String schema) {
        String sql = "SELECT USERNAME, DEFAULT_TABLESPACE, TEMPORARY_TABLESPACE FROM USER_USERS";
        try (PreparedStatement statement = this.getConnManager().connection(schema).prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getString(1)
                        + " (default=" + resultSet.getString(2)
                        + ", temporary=" + resultSet.getString(3) + ")";
            }
        } catch (Exception ex) {
            JulLog.warn("query user tablespaces failed", ex);
        }
        return "";
    }

    /**
     * 创建视图
     *
     * @param param 参数
     */
    public void createView(OracleCreateViewParam param) {
        Connection connection = null;
        try {
            List<String> sqlList = OracleViewCreateSqlGenerator.generateSql(param, this.supportsBequeath());
            // 无变化
            if (CollectionUtil.isEmpty(sqlList)) {
                return;
            }
            String schema = param.getSchema();
            connection = this.getConnManager().connection(schema);
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();
            for (String sql : sqlList) {
                if (StringUtil.isBlank(sql)) {
                    continue;
                }
                String executableSql = this.stripStatementTerminator(sql);
                this.printSql(executableSql);
                statement.executeUpdate(executableSql);
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
     * 修改视图
     *
     * @param param 参数
     */
    public void alertView(OracleAlertViewParam param) {
        Connection connection = null;
        try {
            List<String> sqlList = OracleViewAlertSqlGenerator.generateSql(param, this.supportsBequeath());
            // 无变化
            if (CollectionUtil.isEmpty(sqlList)) {
                return;
            }
            String schema = param.getSchema();
            connection = this.getConnManager().connection(schema);
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();
            for (String sql : sqlList) {
                if (StringUtil.isBlank(sql)) {
                    continue;
                }
                String executableSql = this.stripStatementTerminator(sql);
                this.printSql(executableSql);
                statement.executeUpdate(executableSql);
            }
            connection.commit();
            IOUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            DBUtil.rollback(connection);
            throw new ShellException(ex);
        }
    }

    private String stripStatementTerminator(String sql) {
        String result = sql.strip();
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1).stripTrailing();
        }
        String executable = ShellOracleHelper.stripLeadingComments(result);
        if (this.isPlSqlStatement(executable)) {
            return result;
        }
        while (result.endsWith(";")) {
            result = result.substring(0, result.length() - 1).stripTrailing();
        }
        return result;
    }

    private boolean isPlSqlStatement(String sql) {
        String upper = sql.toUpperCase(java.util.Locale.ROOT);
        return upper.startsWith("BEGIN")
                || upper.startsWith("DECLARE")
                || upper.matches("(?s)^CREATE\\s+(?:OR\\s+REPLACE\\s+)?"
                + "(?:(?:NON)?EDITIONABLE\\s+)?"
                + "(?:FUNCTION|PROCEDURE|TRIGGER|TYPE|PACKAGE(?:\\s+BODY)?)\\b.*");
    }

    private boolean supportsBequeath() {
        return DBOraclePagination.supportsOffsetFetch(this.selectVersion());
    }

    /**
     * 查询索引列表
     *
     * @param schema    模式名称
     * @param tableName 表名称
     * @return 索引列表
     */
    public List<OracleIndex> indexes(String schema, String tableName) {
        try {
            Connection connection = this.getConnManager().connection(schema);
            String sql = """
                    SELECT
                        I.INDEX_NAME AS "Key_name",
                        C.COLUMN_NAME AS "Column_name",
                        C.COLUMN_POSITION AS "Seq_in_index",
                        CASE WHEN I.UNIQUENESS='UNIQUE' THEN 0 ELSE 1 END AS "Non_unique",
                        CASE WHEN I.INDEX_TYPE='NORMAL' THEN 'BTREE' ELSE I.INDEX_TYPE END AS "Index_type"
                    FROM 
                        ALL_INDEXES I
                    JOIN 
                        ALL_IND_COLUMNS C 
                    ON 
                        I.INDEX_NAME = C.INDEX_NAME 
                    AND 
                        I.OWNER = C.INDEX_OWNER
                    WHERE 
                        I.TABLE_OWNER = ? 
                    AND 
                        I.TABLE_NAME = ? 
                    AND 
                        NOT EXISTS (
                            SELECT 1
                            FROM ALL_CONSTRAINTS P
                            WHERE P.OWNER = I.OWNER
                            AND P.INDEX_NAME = I.INDEX_NAME
                            AND P.CONSTRAINT_TYPE = 'P'
                        )
                    ORDER BY 
                        I.INDEX_NAME, C.COLUMN_POSITION
                    """;
            this.printSql(sql);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, schema);
            statement.setString(2, tableName);
            ResultSet resultSet = statement.executeQuery();
            // 打印元数据
            DBUtil.printMetaData(resultSet);
            Map<String, OracleIndex> indexMap = new HashMap<>();
            while (resultSet.next()) {
                String keyName = resultSet.getString("Key_name");
                // 主键类型的跳过
                if ("Primary".equalsIgnoreCase(keyName)) {
                    continue;
                }
                OracleIndex tableIndex = indexMap.get(keyName);
                String columnName = resultSet.getString("Column_name");
                if (tableIndex == null) {
                    int noneUnique = resultSet.getInt("Non_unique");
                    int seqInIndex = resultSet.getInt("Seq_in_index");
                    String indexType = resultSet.getString("Index_type");
                    tableIndex = new OracleIndex();
                    tableIndex.setName(keyName);
                    tableIndex.setSeqIndex(seqInIndex);
                    tableIndex.type(indexType, noneUnique);
                    indexMap.put(keyName, tableIndex);
                }
                tableIndex.addColumn(columnName);
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
    public List<OracleCheck> selectChecks(String schema, String tableName) {
        if (!this.isSupportCheckFeature()) {
            return null;
        }
        try {
            String sql = """
                    SELECT
                        OWNER AS "DB_NAME",
                        CONSTRAINT_NAME AS "NAME",
                        TABLE_NAME AS "TABLE_NAME",
                        SEARCH_CONDITION AS "CLAUSE"
                    FROM 
                        ALL_CONSTRAINTS
                    WHERE 
                        CONSTRAINT_TYPE = 'C'
                    AND
                        GENERATED = 'USER NAME'
                    AND 
                        OWNER = ?
                    AND 
                        TABLE_NAME = ?
                    """;
            this.printSql(sql);
            PreparedStatement statement = this.getConnManager().connection(schema).prepareStatement(sql);
            statement.setString(1, schema);
            statement.setString(2, tableName);
            ResultSet resultSet = statement.executeQuery();
            DBUtil.printMetaData(resultSet);
            List<OracleCheck> checks = new ArrayList<>();
            while (resultSet.next()) {
                OracleCheck check = new OracleCheck();
                String name = resultSet.getString("NAME");
                String clause = resultSet.getString("CLAUSE");
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
                    SELECT
                        AC.TABLE_NAME AS "FKTABLE_NAME"
                    FROM 
                        ALL_CONSTRAINTS AC
                    JOIN 
                        ALL_CONS_COLUMNS ACC 
                    ON 
                        AC.CONSTRAINT_NAME = ACC.CONSTRAINT_NAME 
                    AND 
                        AC.OWNER = ACC.OWNER
                    JOIN 
                        ALL_CONSTRAINTS ACR 
                    ON 
                        AC.R_OWNER = ACR.OWNER 
                    AND 
                        AC.R_CONSTRAINT_NAME = ACR.CONSTRAINT_NAME
                    JOIN 
                        ALL_CONS_COLUMNS ACCR 
                    ON 
                        ACR.CONSTRAINT_NAME = ACCR.CONSTRAINT_NAME 
                    AND 
                        ACR.OWNER = ACCR.OWNER 
                    AND 
                        ACC.POSITION = ACCR.POSITION
                    WHERE 
                        AC.CONSTRAINT_TYPE = 'R'
                    AND 
                        AC.OWNER = ?
                    GROUP BY 
                        AC.TABLE_NAME
                    """;
            this.printSql(sql);
            PreparedStatement statement = this.getConnManager().connection(schema).prepareStatement(sql);
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
    public List<OracleForeignKey> selectForeignKeys(String schema, String tableName) {
        try {
            String sql = """
                    SELECT
                        AC.DELETE_RULE,
                        'NO ACTION' AS "UPDATE_RULE",
                        ACR.OWNER AS "PKTABLE_CAT",
                        AC.CONSTRAINT_NAME AS "FK_NAME",
                        ACR.TABLE_NAME AS "PKTABLE_NAME",
                        acc.COLUMN_NAME AS "FKCOLUMN_NAME",
                        ACCR.COLUMN_NAME AS "PKCOLUMN_NAME"
                    FROM 
                        ALL_CONSTRAINTS AC
                    JOIN 
                        ALL_CONS_COLUMNS ACC 
                    ON 
                        AC.CONSTRAINT_NAME = ACC.CONSTRAINT_NAME 
                    AND 
                        AC.OWNER = ACC.OWNER
                    JOIN 
                        ALL_CONSTRAINTS ACR 
                    ON 
                        AC.R_OWNER = ACR.OWNER 
                    AND 
                        AC.R_CONSTRAINT_NAME = ACR.CONSTRAINT_NAME
                    JOIN 
                        ALL_CONS_COLUMNS ACCR 
                    ON 
                        ACR.CONSTRAINT_NAME = ACCR.CONSTRAINT_NAME 
                    AND 
                        ACR.OWNER = ACCR.OWNER 
                    AND 
                        ACC.POSITION = ACCR.POSITION
                    WHERE 
                        AC.CONSTRAINT_TYPE = 'R'
                    AND 
                        AC.OWNER = ?
                    AND 
                        AC.TABLE_NAME = ?
                    ORDER BY
                        AC.CONSTRAINT_NAME,
                        ACC.POSITION
                    """;
            this.printSql(sql);
            PreparedStatement statement = this.getConnManager().connection(schema).prepareStatement(sql);
            statement.setString(1, schema);
            statement.setString(2, tableName);
            ResultSet resultSet = statement.executeQuery();
            DBUtil.printMetaData(resultSet);
            Map<String, OracleForeignKey> foreignKeyMap = new LinkedHashMap<>();
            while (resultSet.next()) {
                String fkName = resultSet.getString("FK_NAME");
                String fkColumnName = resultSet.getString("FKCOLUMN_NAME");
                String pkColumnName = resultSet.getString("PKCOLUMN_NAME");
                OracleForeignKey foreignKey = foreignKeyMap.get(fkName);
                if (foreignKey == null) {
                    String pkTableName = resultSet.getString("PKTABLE_NAME");
                    String pkTableCat = resultSet.getString("PKTABLE_CAT");
                    String updateRule = resultSet.getString("UPDATE_RULE");
                    String deleteRule = resultSet.getString("DELETE_RULE");
                    foreignKey = new OracleForeignKey();
                    foreignKey.setName(fkName);
                    foreignKey.setUpdatePolicy(updateRule == null ? null : updateRule.toUpperCase());
                    foreignKey.setDeletePolicy(deleteRule == null ? null : deleteRule.toUpperCase());
                    foreignKey.setReferencedTable(pkTableName);
                    foreignKey.setReferencedDatabase(pkTableCat);
                    foreignKeyMap.put(fkName, foreignKey);
                }
                foreignKey.addColumn(fkColumnName);
                foreignKey.addReferencedColumn(pkColumnName);
            }
            IOUtil.close(resultSet);
            return new ArrayList<>(foreignKeyMap.values());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 查询视图字段列表
     *
     * @param schema   模式名称
     * @param viewName 视图名称
     * @return 字段列表
     */
    public List<OracleColumn> viewColumns(String schema, String viewName) {
        try {
            OracleColumns columns = new OracleColumns();
            String sql = """
                    SELECT
                        C.DATA_TYPE AS "Type",
                        C.COLUMN_NAME AS "Field",
                        CC.COMMENTS AS "Comment",
                        C.COLUMN_ID AS "POSITION",
                        C.DATA_DEFAULT AS "Default",
                        C.DATA_SCALE AS "DATA_SCALE",
                        C.DATA_PRECISION AS "DATA_PRECISION",
                        C.DATA_LENGTH AS "DATA_LENGTH",
                        C.CHAR_LENGTH AS "CHAR_LENGTH",
                        C.CHAR_USED AS "CHAR_USED",
                        CASE WHEN C.NULLABLE = 'Y' THEN 'YES' ELSE 'NO' END AS "Null",
                        CASE WHEN CONS.COLUMN_NAME IS NOT NULL THEN 'PRI' ELSE '' END AS "Key"
                    FROM
                        ALL_TAB_COLUMNS C
                    LEFT JOIN
                        ALL_COL_COMMENTS CC
                    ON
                        C.OWNER = CC.OWNER
                    AND
                        C.TABLE_NAME = CC.TABLE_NAME
                    AND
                        C.COLUMN_NAME = CC.COLUMN_NAME
                    LEFT JOIN
                        ALL_CONS_COLUMNS CONS
                    ON
                        C.OWNER = CONS.OWNER
                    AND
                        C.TABLE_NAME = CONS.TABLE_NAME
                    AND
                        C.COLUMN_NAME = CONS.COLUMN_NAME
                    AND
                        EXISTS (SELECT 1 FROM ALL_CONSTRAINTS AC WHERE AC.OWNER = CONS.OWNER AND AC.CONSTRAINT_NAME = CONS.CONSTRAINT_NAME AND AC.CONSTRAINT_TYPE = 'P')
                    WHERE
                        C.OWNER = ?
                    AND
                        C.TABLE_NAME = ?
                    """;
            Connection connection = this.getConnManager().connection(schema);
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
                String nullable = resultSet.getString("Null");
                int dataScale = resultSet.getInt("DATA_SCALE");
                String comment = resultSet.getString("Comment");
                int dataLength = resultSet.getInt("DATA_LENGTH");
                OracleColumn column = new OracleColumn();
                column.parseKey(key);
                column.setName(field);
                column.setSize(columnSize(type, dataLength, resultSet.getInt("DATA_PRECISION"),
                        resultSet.getInt("CHAR_LENGTH"), resultSet.getString("CHAR_USED")));
                column.setDigits(dataScale);
                column.setType(type);
                column.setSchema(schema);
                column.setComment(comment);
                column.setDefaultValue(def);
                column.setPosition(position);
                column.setTableName(viewName);
                column.setNullable("yes".equalsIgnoreCase(nullable));
                columns.add(column);
            }
            IOUtil.close(resultSet);
            // 初始化自动递增
            this.initAutoIncrement(connection, schema, viewName, columns, true);
            this.initViewPrimaryKeys(connection, schema, viewName, columns);
            // 返回排序后的数据
            return new ArrayList<>(columns.sortOfPosition());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    private void initViewPrimaryKeys(Connection connection, String schema, String viewName,
                                     OracleColumns columns) throws SQLException {
        if (connection == null || StringUtil.isBlank(schema) || StringUtil.isBlank(viewName)
                || columns == null || columns.isEmpty()) {
            return;
        }
        Set<String> primaryKeys = new HashSet<>();
        String dependencySql = """
                SELECT DISTINCT REFERENCED_OWNER, REFERENCED_NAME
                FROM ALL_DEPENDENCIES
                WHERE OWNER = ?
                  AND NAME = ?
                  AND REFERENCED_TYPE = 'TABLE'
                """;
        try (PreparedStatement statement = connection.prepareStatement(dependencySql)) {
            statement.setString(1, schema);
            statement.setString(2, viewName);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String owner = resultSet.getString(1);
                    String tableName = resultSet.getString(2);
                    if (StringUtil.isBlank(owner) || StringUtil.isBlank(tableName)) {
                        continue;
                    }
                    try (ResultSet keys = connection.getMetaData().getPrimaryKeys(null, owner, tableName)) {
                        while (keys.next()) {
                            primaryKeys.add(keys.getString("COLUMN_NAME"));
                        }
                    }
                }
            }
        }
        if (primaryKeys.isEmpty()) {
            return;
        }
        for (OracleColumn column : columns) {
            if (primaryKeys.contains(column.getName())) {
                column.setPrimaryKey(true);
            }
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
    public List<OracleRecord> viewRecords(String schema, String viewName, Long start, Long limit, List<OracleRecordFilter> filters) {
        try {
            Connection connection = this.getConnManager().connection(schema);
            StringBuilder builder = new StringBuilder("SELECT * FROM ");
            builder.append(DBUtil.wrap(schema, viewName, DBDialect.ORACLE));
            String filterCondition = DBUtil.buildCondition(filters);
            if (StringUtil.isNotBlank(filterCondition)) {
                builder.append(" WHERE ").append(filterCondition);
            }
            String sql = builder.toString();
            if (start != null && limit != null) {
                sql = DBOraclePagination.paginate(sql, start, limit, this.selectVersion());
            }
            this.printSql(sql);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            DBUtil.printMetaData(resultSet);
            List<OracleRecord> records = new ArrayList<>();
            boolean updatable = ShellOracleHelper.isViewUpdatable(connection, schema, viewName);
            OracleColumns columns = ShellOracleHelper.parseColumns(resultSet);
            for (OracleColumn column : columns) {
                column.setTableName(viewName);
            }
            while (resultSet.next()) {
                OracleRecord record = new OracleRecord(columns, !updatable);
                for (OracleColumn column : columns) {
                    Object data = resultSet.getObject(column.getName());
                    //                    // 获取几何值
                    //                    if (column.supportGeometry()) {
                    //                        data = OracleHelper.getGeometryString(connection, data);
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
    public void createTable(OracleCreateTableParam param) {
        Connection connection = null;
        try {
            String schema = param.schema();
            connection = this.getConnManager().connection(schema);
            Statement statement = connection.createStatement();
            List<String> sqlList = OracleTableCreateSqlGenerator.generateSql(param, this.supportsIdentity());
            connection.setAutoCommit(false);
            for (String sqlStr : sqlList) {
                String executableSql = this.stripStatementTerminator(sqlStr);
                this.printSql(executableSql);
                statement.executeUpdate(executableSql);
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
     * 修改表
     *
     * @param param 修改参数
     */
    public void alertTable(OracleAlertTableParam param) {
        Connection connection = null;
        try {
            List<String> sqlList = OracleTableAlertSqlGenerator.generateSql(param, this.supportsIdentity());
            // 无变化
            if (CollectionUtil.isEmpty(sqlList)) {
                return;
            }
            String schema = param.getSchema();
            connection = this.getConnManager().connection(schema);
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();
            for (String sql : sqlList) {
                if (StringUtil.isBlank(sql)) {
                    continue;
                }
                String executableSql = this.stripStatementTerminator(sql);
                this.printSql(executableSql);
                statement.executeUpdate(executableSql);
            }
            connection.commit();
            IOUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            DBUtil.rollback(connection);
            throw new ShellException(ex);
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
            String sql = "ALTER TABLE " + DBUtil.wrap(schema, oldTableName, DBDialect.ORACLE)
                    + " RENAME TO " + DBUtil.wrap(newTableName, DBDialect.ORACLE);
            Connection connection = this.getConnManager().connection(schema);
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
            // 查询
            OracleView view = this.selectView(schema, oldViewName);
            view.setName(newViewName);
            OracleCreateViewParam param = new OracleCreateViewParam();
            param.setSchema(schema);
            param.setView(view);
            // 创建新的
            this.createView(param);
            view.setName(oldViewName);
            // 删除旧的
            this.dropView(view);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
        //try {
        //String sql = "ALTER VIEW " + DBUtil.wrap(schema, oldViewName, DBDialect.ORACLE)
        //        + " RENAME TO " + DBUtil.wrap(schema, newViewName, DBDialect.ORACLE);
        //Connection connection = this.getConnManager().connection(schema);
        //Statement statement = connection.createStatement();
        //this.printSql(sql);
        //statement.execute(sql);
        //IOUtil.close(statement);
        //} catch (Exception ex) {
        //    ex.printStackTrace();
        //    throw new ShellException(ex);
        //}
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
            this.cloneFunction(schema, oldFunctionName, newFunctionName);
            OracleFunction function = new OracleFunction();
            function.setSchema(schema);
            function.setName(oldFunctionName);
            this.dropFunction(function);
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
            this.cloneProcedure(schema, oldProcedureName, newProcedureName);
            OracleProcedure procedure = new OracleProcedure();
            procedure.setSchema(schema);
            procedure.setName(oldProcedureName);
            this.dropProcedure(procedure);
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
            Statement statement = this.getConnManager().connection(schema).createStatement();
            String sql = "DELETE FROM " + DBUtil.wrap(schema, tableName, DBDialect.ORACLE);
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
            Statement statement = this.getConnManager().connection(schema).createStatement();
            String sql = "TRUNCATE TABLE " + DBUtil.wrap(schema, tableName, DBDialect.ORACLE);
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
            Statement statement = this.getConnManager().connection(schema).createStatement();
            String sql = "DROP TABLE " + DBUtil.wrap(schema, tableName, DBDialect.ORACLE);
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
            String sql = "SELECT COUNT(*) FROM ALL_USERS WHERE USERNAME = ?";
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
     * 移除指定模式的缓存连接。模式被删除重建、会话失效或权限变化后需要重新建立连接。
     *
     * @param schema 模式名称
     */
    public void resetSchemaConnection(String schema) {
        this.getConnManager().removeConnection(schema);
    }

    /**
     * 移除服务连接。连接用户被删除重建或会话失效后需要重新建立连接。
     */
    public void resetServerConnection() {
        this.getConnManager().removeServerConnection();
    }

    /**
     * 查询当前会话所在容器名称；非CDB环境或旧版本返回服务名。
     *
     * @return 容器或服务名
     */
    public String selectCurrentContainer() {
        String container = this.selectUserEnv("CON_NAME");
        if (StringUtil.isNotBlank(container)) {
            return container;
        }
        return this.selectUserEnv("SERVICE_NAME");
    }

    private String selectUserEnv(String name) {
        try {
            String sql = "SELECT SYS_CONTEXT('USERENV', '" + name + "') FROM DUAL";
            Connection connection = this.getConnManager().connection();
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(sql)) {
                return resultSet.next() ? resultSet.getString(1) : "";
            }
        } catch (Exception ignored) {
            return "";
        }
    }

    /**
     * 判断当前会话是否具备创建类型的能力。
     *
     * @return 结果
     */
    public boolean hasCreateTypePrivilege() {
        try {
            String sql = "SELECT COUNT(*) FROM SESSION_PRIVS"
                    + " WHERE PRIVILEGE IN ('CREATE TYPE', 'CREATE ANY TYPE')";
            Connection connection = this.getConnManager().connection();
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(sql)) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * 创建模式
     *
     * @param schema 模式
     */
    public void createSchema(OracleSchema schema) {
        try {
            String user = DBUtil.wrap(schema.getName(), DBDialect.ORACLE);
            String password = oraclePassword(schema.getPassword());
            String[] sqlList = {
                    "CREATE USER " + user + " IDENTIFIED BY " + password,
                    "GRANT CREATE SESSION, CREATE TABLE, CREATE VIEW, CREATE SEQUENCE, CREATE PROCEDURE, CREATE TRIGGER, CREATE TYPE TO " + user,
                    "GRANT UNLIMITED TABLESPACE TO " + user
            };
            for (String sql : sqlList) {
                this.printSql(sql);
                try (Statement statement = this.getConnManager().connection().createStatement()) {
                    statement.execute(sql);
                }
            }
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
    public boolean alterSchema(OracleSchema schema) {
        try {
            if (StringUtil.isNotBlank(schema.getPassword())) {
                String sql = "ALTER USER " + DBUtil.wrap(schema.getName(), DBDialect.ORACLE)
                        + " IDENTIFIED BY " + oraclePassword(schema.getPassword());
                this.printSql(sql);
                try (Statement statement = this.getConnManager().connection().createStatement()) {
                    statement.execute(sql);
                }
            }
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
            String sql = "DROP USER " + DBUtil.wrap(schema, DBDialect.ORACLE) + " CASCADE";
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
     * 生成兼容Oracle 11g的用户密码标识符。
     */
    private static String oraclePassword(String password) {
        if (StringUtil.isBlank(password)) {
            throw new IllegalArgumentException("Oracle user password must not be blank");
        }
        String value = password.trim();
        if (value.length() > 30 || value.contains("\"")) {
            throw new IllegalArgumentException("Oracle user password must be 1-30 characters without a double quote");
        }
        return "\"" + value + "\"";
    }

    /**
     * 解释SQL
     *
     * @param schema 模式名称
     * @param sql    SQL语句
     * @return 执行计划结果
     */
    public DBQueryResults<OracleExplainResult> explainSql(String schema, String sql) {
        DBQueryResults<OracleExplainResult> results = new DBQueryResults<>();
        Connection connection = null;
        try {
            this.printSql(sql);
            DBSqlParser parser = DBSqlParser.getParser(sql, DBDialect.ORACLE);
            List<String> list = parser.parseSql();
            connection = this.getConnManager().connection(schema);
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();
            for (String execSql : list) {
                OracleExplainResult result = new OracleExplainResult();
                try {
                    String statementId = "EASYSHELL_" + System.nanoTime();
                    String explainSql = "EXPLAIN PLAN SET STATEMENT_ID = '" + statementId + "' FOR " + execSql.stripLeading();
                    result.setContent(explainSql);
                    long startTime = System.nanoTime();
                    statement.executeUpdate(explainSql);
                    String planSql = """
                            SELECT ID, PARENT_ID, OPERATION, OPTIONS, OBJECT_NAME, COST, CARDINALITY
                            FROM PLAN_TABLE
                            WHERE STATEMENT_ID = '""" + statementId + "' ORDER BY ID";
                    ResultSet resultSet = statement.executeQuery(planSql);
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
    public OracleExecuteResult executeSingleSql(String schema, String sql) {
        Connection connection = null;
        OracleExecuteResult result = new OracleExecuteResult();
        result.setContent(sql);
        try {
            this.printSql(sql);
            DBSqlParser parser = DBSqlParser.getParser(sql, DBDialect.ORACLE);
            String execSql = parser.parseSingleSql();
            connection = this.getConnManager().connection(schema);
            Statement statement = connection.createStatement();
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
                    connection.setAutoCommit(false);
                    int updateCount = statement.getUpdateCount();
                    connection.commit();
                    result.setUpdateCount(updateCount);
                    result.setSuccess(true);
                }
                long endTime = System.nanoTime();
                result.setUsed(endTime - startTime);
            } catch (SQLException ex) {
                result.setMsg(ex.getMessage());
            }
            IOUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            DBUtil.rollback(connection);
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
        try {
            String executableSql = this.stripStatementTerminator(sql);
            this.printSql(executableSql);
            connection = this.getConnManager().connection(schema);
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();
            statement.execute(executableSql);
            connection.commit();
            IOUtil.close(statement);
        } catch (Exception ex) {
            if (!ShellOracleHelper.isIgnorableDropError(sql, ex)) {
                JulLog.warn("sql:\n{}", sql);
                ex.printStackTrace();
            }
            DBUtil.rollback(connection);
            throw new ShellException(ex);
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
        int result = 0;
        try {
            connection = newConnection ? this.getConnManager().newConnection(schema) : this.getConnManager().connection(schema);
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();
            for (String sql : sqlList) {
                String executableSql = this.stripStatementTerminator(sql);
                this.printSql(executableSql);
                statement.addBatch(executableSql);
            }
            int[] results = statement.executeBatch();
            connection.commit();
            IOUtil.close(statement);
            for (int i : results) {
                result += i;
            }
        } catch (Exception ex) {
            JulLog.warn("sqlList:{}", sqlList);
            ex.printStackTrace();
            DBUtil.rollback(connection);
            throw new ShellException(ex);
        } finally {
            if (newConnection) {
                IOUtil.close(connection);
            }
        }
        return result;
    }

    @Override
    public DBDialect dialect() {
        return DBDialect.ORACLE;
    }

    /**
     * 查询函数列表
     *
     * @param schema 模式名称
     * @return 函数列表
     */
    public List<OracleFunction> selectFunctions(String schema) {
        OracleSelectFunctionParam param = new OracleSelectFunctionParam();
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
    public List<OracleFunction> selectFunctionsSimple(String schema) {
        OracleSelectFunctionParam param = new OracleSelectFunctionParam();
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
    public List<OracleFunction> selectFunctions(OracleSelectFunctionParam param) {
        try {
            String schema = param.getSchema();
            List<OracleFunction> list = new ArrayList<>();
            String sql = """
                    SELECT
                        P.AGGREGATE,
                        P.PIPELINED,
                        P.DETERMINISTIC,
                        P.AUTHID AS "SECURITY_TYPE",
                        O.OBJECT_NAME AS "ROUTINE_NAME",
                        NULL AS DLL
                    FROM 
                        ALL_OBJECTS O
                    LEFT JOIN 
                        ALL_PROCEDURES P 
                    ON 
                        O.OWNER = P.OWNER 
                    AND 
                        O.OBJECT_NAME = P.OBJECT_NAME
                    WHERE 
                        O.OWNER = ?
                    AND 
                        O.OBJECT_TYPE = 'FUNCTION'
                    """;
            this.printSql(sql);
            Connection connection = this.getConnManager().connection(schema);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, schema);
            // 执行SQL查询并获取结果集
            ResultSet resultSet = statement.executeQuery();
            // 打印元数据
            DBUtil.printMetaData(resultSet);
            // 遍历结果集
            while (resultSet.next()) {
                OracleFunction function = new OracleFunction();
                String name = resultSet.getString("ROUTINE_NAME");
                String securityType = resultSet.getString("SECURITY_TYPE");
                String aggregate = resultSet.getString("AGGREGATE");
                String pipelined = resultSet.getString("PIPELINED");
                String deterministic = resultSet.getString("DETERMINISTIC");
                StringBuilder characteristic = new StringBuilder();
                if (StringUtil.equalsIgnoreCase("YES", aggregate)) {
                    characteristic.append(",AGGREGATE");
                }
                if (StringUtil.equalsIgnoreCase("YES", pipelined)) {
                    characteristic.append(",PIPELINED");
                }
                if (StringUtil.equalsIgnoreCase("YES", deterministic)) {
                    characteristic.append(",DETERMINISTIC");
                }
                if (!characteristic.isEmpty()) {
                    function.setCharacteristic(characteristic.substring(1));
                }
                function.setName(name);
                function.setSchema(schema);
                function.setDefinition("");
                function.setSecurityType(securityType);
                list.add(function);
            }
            // 关闭连接和释放资源
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            if (param.isFull()) {
                for (OracleFunction function : list) {
                    String createDefinition = ShellOracleHelper.getFunctionDefinition(
                            connection, schema, function.getName());
                    function.setParams(this.listFunctionParam(schema, function.getName()));
                    function.setDefinition(routineBody(createDefinition));
                    function.setCreateDefinition(createDefinition);
                    String characteristic = function.getCharacteristic();
                    if (StringUtil.contains(createDefinition, "PARALLEL_ENABLE")) {
                        characteristic = appendCharacteristic(characteristic, "PARALLEL_ENABLE");
                    }
                    if (StringUtil.contains(createDefinition, "RESULT_CACHE")) {
                        characteristic = appendCharacteristic(characteristic, "RESULT_CACHE");
                    }
                    function.setCharacteristic(characteristic);
                }
            }
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
    public List<OracleProcedure> selectProcedures(String schema) {
        OracleSelectProcedureParam param = new OracleSelectProcedureParam();
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
    public List<OracleProcedure> selectProceduresSimple(String schema) {
        OracleSelectProcedureParam param = new OracleSelectProcedureParam();
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
    public List<OracleProcedure> selectProcedures(OracleSelectProcedureParam param) {
        try {
            String schema = param.getSchema();
            List<OracleProcedure> list = new ArrayList<>();
            String sql = """
                    SELECT
                        P.AGGREGATE,
                        P.PIPELINED,
                        P.DETERMINISTIC,
                        P.AUTHID AS "SECURITY_TYPE",
                        O.OBJECT_NAME AS "ROUTINE_NAME",
                        NULL AS DLL
                    FROM 
                        ALL_OBJECTS O
                    LEFT JOIN
                        ALL_PROCEDURES P ON O.OWNER = P.OWNER AND O.OBJECT_NAME = P.OBJECT_NAME
                    WHERE 
                        O.OWNER = ?
                    AND 
                        O.OBJECT_TYPE = 'PROCEDURE'
                    """;
            this.printSql(sql);
            Connection connection = this.getConnManager().connection(schema);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, schema);
            // 执行SQL查询并获取结果集
            ResultSet resultSet = statement.executeQuery();
            // 打印元数据
            DBUtil.printMetaData(resultSet);
            // 遍历结果集
            while (resultSet.next()) {
                OracleProcedure procedure = new OracleProcedure();
                String name = resultSet.getString("ROUTINE_NAME");
                String aggregate = resultSet.getString("AGGREGATE");
                String pipelined = resultSet.getString("PIPELINED");
                String securityType = resultSet.getString("SECURITY_TYPE");
                String deterministic = resultSet.getString("DETERMINISTIC");
                StringBuilder characteristic = new StringBuilder();
                if (StringUtil.equalsIgnoreCase("YES", aggregate)) {
                    characteristic.append(",AGGREGATE");
                }
                if (StringUtil.equalsIgnoreCase("YES", pipelined)) {
                    characteristic.append(",PIPELINED");
                }
                if (StringUtil.equalsIgnoreCase("YES", deterministic)) {
                    characteristic.append(",DETERMINISTIC");
                }
                if (!characteristic.isEmpty()) {
                    procedure.setCharacteristic(characteristic.substring(1));
                }
                procedure.setName(name);
                procedure.setSchema(schema);
                procedure.setDefinition("");
                procedure.setSecurityType(securityType);
                list.add(procedure);
            }
            // 关闭连接和释放资源
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            if (param.isFull()) {
                for (OracleProcedure procedure : list) {
                    String createDefinition = ShellOracleHelper.getProcedureDefinition(
                            connection, schema, procedure.getName());
                    procedure.setParams(this.listProcedureParam(schema, procedure.getName()));
                    procedure.setDefinition(routineBody(createDefinition));
                    procedure.setCreateDefinition(createDefinition);
                    String characteristic = procedure.getCharacteristic();
                    if (StringUtil.contains(createDefinition, "PARALLEL_ENABLE")) {
                        characteristic = appendCharacteristic(characteristic, "PARALLEL_ENABLE");
                    }
                    if (StringUtil.contains(createDefinition, "RESULT_CACHE")) {
                        characteristic = appendCharacteristic(characteristic, "RESULT_CACHE");
                    }
                    procedure.setCharacteristic(characteristic);
                }
            }
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
    public OracleProcedure selectProcedure(String schema, String procedureName) {
        OracleSelectProcedureParam param = new OracleSelectProcedureParam();
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
    public OracleProcedure selectProcedureSimple(String schema, String procedureName) {
        OracleSelectProcedureParam param = new OracleSelectProcedureParam();
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
    public OracleProcedure selectProcedure(OracleSelectProcedureParam param) {
        try {
            String schema = param.getSchema();
            String produceName = param.getProcedureName();
            String sql = """
                    SELECT
                        P.AGGREGATE,
                        P.PIPELINED,
                        P.DETERMINISTIC,
                        P.AUTHID AS "SECURITY_TYPE",
                        NULL AS DLL
                    FROM 
                        ALL_OBJECTS O
                    LEFT JOIN 
                        ALL_PROCEDURES P ON O.OWNER = P.OWNER AND O.OBJECT_NAME = P.OBJECT_NAME
                    WHERE 
                        O.OWNER = ?
                    AND 
                        O.OBJECT_NAME = ?
                    AND 
                        O.OBJECT_TYPE = 'PROCEDURE'
                    """;
            this.printSql(sql);
            Connection connection = this.getConnManager().connection(schema);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, schema);
            statement.setString(2, produceName);
            // 执行SQL查询并获取结果集
            ResultSet resultSet = statement.executeQuery();
            // 打印元数据
            DBUtil.printMetaData(resultSet);
            OracleProcedure procedure = new OracleProcedure();
            procedure.setSchema(schema);
            procedure.setName(produceName);
            // 遍历结果集
            while (resultSet.next()) {
                String aggregate = resultSet.getString("AGGREGATE");
                String pipelined = resultSet.getString("PIPELINED");
                String createDefinition = param.isFull()
                        ? ShellOracleHelper.getProcedureDefinition(connection, schema, produceName) : "";
                String securityType = resultSet.getString("SECURITY_TYPE");
                String deterministic = resultSet.getString("DETERMINISTIC");
                StringBuilder characteristic = new StringBuilder();
                if (param.isFull()) {
                    List<OracleRoutineParam> params = this.listProcedureParam(schema, produceName);
                    procedure.setParams(params);
                }
                String definition = routineBody(createDefinition);
                if (StringUtil.contains(createDefinition, "PARALLEL_ENABLE")) {
                    characteristic.append(",PARALLEL_ENABLE");
                }
                if (StringUtil.contains(createDefinition, "RESULT_CACHE")) {
                    characteristic.append(",RESULT_CACHE");
                }
                if (StringUtil.equalsIgnoreCase("YES", aggregate)) {
                    characteristic.append(",AGGREGATE");
                }
                if (StringUtil.equalsIgnoreCase("YES", pipelined)) {
                    characteristic.append(",PIPELINED");
                }
                if (StringUtil.equalsIgnoreCase("YES", deterministic)) {
                    characteristic.append(",DETERMINISTIC");
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
    public void dropProcedure(OracleProcedure routine) {
        try {
            String sql = "DROP PROCEDURE " + DBUtil.wrap(routine.getSchema(), routine.getName(), DBDialect.ORACLE);
            this.printSql(sql);
            Statement statement = this.getConnManager().connection(routine.getSchema()).createStatement();
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
    public void createProcedure(OracleCreateProcedureParam param) {
        try {
            String sql = OracleProcedureCreateSqlGenerator.generateSqlSingle(param);
            this.printSql(sql);
            Statement statement = this.getConnManager().connection(param.getSchema()).createStatement();
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
    public void alertProcedure(OracleAlertProcedureParam param) {
        try {
            String sql = OracleProcedureAlertSqlGenerator.generateSqlSingle(param);
            this.printSql(sql);
            Statement statement = this.getConnManager().connection(param.getSchema()).createStatement();
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
    public void dropFunction(OracleFunction function) {
        try {
            String sql = "DROP FUNCTION " + DBUtil.wrap(function.getSchema(), function.getName(), DBDialect.ORACLE);
            this.printSql(sql);
            Statement statement = this.getConnManager().connection(function.getSchema()).createStatement();
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
    public OracleFunction selectFunction(String schema, String functionName) {
        OracleSelectFunctionParam param = new OracleSelectFunctionParam();
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
    public OracleFunction selectFunctionSimple(String schema, String functionName) {
        OracleSelectFunctionParam param = new OracleSelectFunctionParam();
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
    public OracleFunction selectFunction(OracleSelectFunctionParam param) {
        try {
            String schema = param.getSchema();
            String functionName = param.getFunctionName();
            String sql = """
                    SELECT
                        P.AGGREGATE,
                        P.PIPELINED,
                        P.DETERMINISTIC,
                        P.AUTHID AS "SECURITY_TYPE",
                        NULL AS DLL
                    FROM 
                        ALL_OBJECTS O
                    LEFT JOIN 
                        ALL_PROCEDURES P ON O.OWNER = P.OWNER AND O.OBJECT_NAME = P.OBJECT_NAME
                    WHERE 
                        O.OWNER = ?
                    AND 
                        O.OBJECT_NAME = ?
                    AND 
                        O.OBJECT_TYPE = 'FUNCTION'
                    """;
            this.printSql(sql);
            Connection connection = this.getConnManager().connection(schema);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, schema);
            statement.setString(2, functionName);
            // 执行SQL查询并获取结果集
            ResultSet resultSet = statement.executeQuery();
            // 打印元数据
            DBUtil.printMetaData(resultSet);
            OracleFunction function = new OracleFunction();
            function.setSchema(schema);
            function.setName(functionName);
            // 遍历结果集
            while (resultSet.next()) {
                String aggregate = resultSet.getString("AGGREGATE");
                String pipelined = resultSet.getString("PIPELINED");
                String createDefinition = param.isFull()
                        ? ShellOracleHelper.getFunctionDefinition(connection, schema, functionName) : "";
                String securityType = resultSet.getString("SECURITY_TYPE");
                String deterministic = resultSet.getString("DETERMINISTIC");
                String characteristic = "";
                if (param.isFull()) {
                    List<OracleRoutineParam> params = this.listFunctionParam(schema, functionName);
                    function.setParams(params);
                }
                String definition = routineBody(createDefinition);
                characteristic = this.appendCharacteristicFromDefinition(characteristic, createDefinition, "PARALLEL_ENABLE");
                characteristic = this.appendCharacteristicFromDefinition(characteristic, createDefinition, "RESULT_CACHE");
                characteristic = this.appendCharacteristicFromDefinition(characteristic, createDefinition, "AGGREGATE");
                characteristic = this.appendCharacteristicFromDefinition(characteristic, createDefinition, "PIPELINED");
                characteristic = this.appendCharacteristicFromDefinition(characteristic, createDefinition, "DETERMINISTIC");
                if (StringUtil.equalsIgnoreCase("YES", aggregate)) {
                    characteristic = appendCharacteristicIfMissing(characteristic, "AGGREGATE");
                }
                if (StringUtil.equalsIgnoreCase("YES", pipelined)) {
                    characteristic = appendCharacteristicIfMissing(characteristic, "PIPELINED");
                }
                if (StringUtil.equalsIgnoreCase("YES", deterministic)) {
                    characteristic = appendCharacteristicIfMissing(characteristic, "DETERMINISTIC");
                }
                if (StringUtil.isNotBlank(characteristic)) {
                    function.setCharacteristic(characteristic);
                }
                if (StringUtil.isBlank(securityType) && param.isFull()) {
                    securityType = this.parseSecurityType(createDefinition);
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
    public void createFunction(OracleCreateFunctionParam param) {
        try {
            if (param.getFunction() == null || StringUtil.isBlank(param.getFunction().getReturnType())) {
                throw new ShellException("Oracle function return type is required");
            }
            String sql = OracleFunctionCreateSqlGenerator.generateSqlSingle(param);
            this.printSql(sql);
            Connection connection = this.getConnManager().connection(param.getSchema());
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
            IOUtil.close(statement);
            this.assertFunctionValid(connection, param.getSchema(), param.getFunction().getName());
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
    public OracleRecord selectRecord(OracleSelectRecordParam param) {
        try {
            String schema = param.getSchema();
            String tableName = param.getTableName();
            Connection connection = this.getConnManager().connection(schema);
            OracleRecordPrimaryKey primaryKey = param.getPrimaryKey();
            StringBuilder builder = new StringBuilder("SELECT * FROM ");
            builder.append(DBUtil.wrap(schema, tableName, DBDialect.ORACLE))
                    .append(" WHERE ")
                    .append(DBUtil.wrap(primaryKey.getColumnName(), DBDialect.ORACLE))
                    .append(" = ?");
            String sql = builder.toString();
            this.printSql(sql);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setObject(1, primaryKey.data());
            ResultSet resultSet = statement.executeQuery();
            DBUtil.printMetaData(resultSet);
            OracleColumns columns = ShellOracleHelper.parseColumns(resultSet);
            OracleRecord record = new OracleRecord(columns);
            while (resultSet.next()) {
                for (OracleColumn column : columns) {
                    Object data = resultSet.getObject(column.getName());
                    if (column.isObjectType() || column.isCollectionType()) {
                        data = OracleComplexValueUtil.toJsonValue(connection, data);
                    } else if (data instanceof oracle.sql.TIMESTAMPLTZ) {
                        data = ShellOracleDataUtil.valueStandardization(connection, data);
                    }
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
        // Oracle客户端字符集，返回默认值
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
            Connection connection = this.getConnManager().connection(schema);
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
            String sql = "SELECT * FROM " + DBUtil.wrap(schema, tableName, DBDialect.ORACLE) + " WHERE 1=0";
            this.printSql(sql);
            Statement stmt = connection.createStatement();
            ResultSet resultSet = stmt.executeQuery(sql);
            DBUtil.printMetaData(resultSet);
            ResultSetMetaData rsmd = resultSet.getMetaData();
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                if (rsmd.isAutoIncrement(i)) {
                    exist = true;
                    break;
                }
            }
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
            Connection connection = this.getConnManager().connection(schema);
            String sql = "SELECT *  FROM ALL_CONSTRAINTS WHERE OWNER = ? AND TABLE_NAME = ? AND CONSTRAINT_TYPE = 'P'";
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
        String cloneSuffix = DBUtil.genCloneName();
        // 查询检查
        List<OracleCheck> checks = this.selectChecks(schema, tableName);
        // 查询触发器
        List<OracleTrigger> triggers = this.selectTriggers(schema, tableName);
        // 查询外键
        List<OracleForeignKey> foreignKeys = this.selectForeignKeys(schema, tableName);
        if (checks != null) {
            for (OracleCheck check : checks) {
                check.setName(cloneConstraintName(check.getName(), cloneSuffix));
                check.clearStatus();
                check.clearOriginalData();
                check.setCreated(true);
            }
        }
        if (triggers != null) {
            for (OracleTrigger trigger : triggers) {
                trigger.setName(cloneConstraintName(trigger.getName(), cloneSuffix));
                trigger.clearStatus();
                trigger.clearOriginalData();
                trigger.setCreated(true);
            }
        }
        //         if (indexes != null) {
        //             for (OracleIndex index : indexes) {
        //                 index.setName(index.getName() + DBUtil.genCloneName());
        //                 index.clearStatus();
        //                 index.clearOriginalData();
        //                 index.setCreated(true);
        //             }
        //         }
        if (foreignKeys != null) {
            for (OracleForeignKey foreignKey : foreignKeys) {
                foreignKey.setName(cloneConstraintName(foreignKey.getName(), cloneSuffix));
                foreignKey.clearStatus();
                foreignKey.clearOriginalData();
                foreignKey.setCreated(true);
            }
        }

        try {
            Connection connection = this.getConnManager().connection(schema);
            // 克隆基本的表结构
            //            String sql = "CREATE TABLE " + DBUtil.wrap(schema, newTableName, DBDialect.ORACLE);
            //            if (!includeRecord) {
            //                sql = sql + " AS SELECT * FROM " + DBUtil.wrap(schema, tableName, DBDialect.ORACLE);
            //                sql = sql + " WHERE 1 = 0";
            //            } else {
            //                sql = sql + " LIKE " + DBUtil.wrap(schema, tableName, DBDialect.ORACLE);
            //            }
            String sql = this.showCreateTable(schema, tableName);
            sql = sql.replace(DBUtil.wrap(schema, tableName, DBDialect.ORACLE), DBUtil.wrap(schema, newTableName, DBDialect.ORACLE));
            sql = renameTableConstraints(sql, cloneSuffix);
            this.printSql(sql);
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.execute();
            IOUtil.close(stmt);
            OracleTable table = new OracleTable();
            table.setSchema(schema);
            table.setName(newTableName);

            // 克隆表结构的检查、外键、触发器
            OracleAlertTableParam alertTableParam = new OracleAlertTableParam();
            alertTableParam.setTable(table);
            alertTableParam.setChecks(DBObjects.of(filterConstraintsAlreadyInTableSql(checks, sql)));
            alertTableParam.setTriggers(DBObjects.of(triggers));
            alertTableParam.setForeignKeys(DBObjects.of(filterConstraintsAlreadyInTableSql(foreignKeys, sql)));

            // 克隆数据
            if (includeRecord) {
                // 查询字段列表
                OracleSelectColumnParam param = new OracleSelectColumnParam();
                param.setSchema(schema);
                param.setTableName(newTableName);
                OracleColumns columns = new OracleColumns(this.selectColumns(param));
                // 虚拟列、BFILE、ANYDATA等不可直接写入的列不参与数据复制
                List<OracleColumn> copyColumns = columns.stream()
                        .filter(OracleColumn::isDmlWritable)
                        .toList();
                if (!copyColumns.isEmpty()) {
                    StringBuilder builder = new StringBuilder();
                    for (OracleColumn column : copyColumns) {
                        builder.append(DBUtil.wrap(column.getName(), DBDialect.ORACLE))
                                .append(" ,");
                    }
                    StringUtil.deleteLast(builder, ",");

                    // 处理数据
                    String sql2 = "INSERT INTO " + DBUtil.wrap(schema, newTableName, this.dialect())
                            + "(" + builder + ")"
                            + " SELECT " + builder + " FROM " + DBUtil.wrap(schema, tableName, this.dialect());
                    connection.setAutoCommit(false);
                    Statement statement = connection.createStatement();
                    this.printSql(sql2);
                    statement.executeUpdate(sql2);
                    connection.commit();
                    IOUtil.close(statement);
                }
            }
            // 数据复制完成后再创建检查、外键和触发器，避免复制数据时触发源表上的业务触发器
            this.alertTable(alertTableParam);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
        return newTableName;
    }

    /**
     * 重命名建表语句中已命名的约束，避免克隆表复用源表约束名。
     *
     * @param sql         建表语句
     * @param cloneSuffix 克隆名称后缀
     * @return 重命名后的建表语句
     */
    private static String renameTableConstraints(String sql, String cloneSuffix) {
        java.util.regex.Matcher matcher = Pattern
                .compile("(\\bCONSTRAINT\\s+)(\"[^\"]+\"|[A-Za-z][A-Za-z0-9_$#]*)",
                        Pattern.CASE_INSENSITIVE)
                .matcher(sql);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String oldName = matcher.group(2);
            String unwrappedName = oldName.startsWith("\"") && oldName.endsWith("\"")
                    ? oldName.substring(1, oldName.length() - 1)
                    : oldName;
            String replacement = matcher.group(1)
                    + DBUtil.wrap(cloneConstraintName(unwrappedName, cloneSuffix), DBDialect.ORACLE);
            matcher.appendReplacement(result, java.util.regex.Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * 生成不超过 Oracle 11g 标识符长度限制的克隆对象名称。
     *
     * @param name   原始名称
     * @param suffix 克隆后缀
     * @return 克隆后的名称
     */
    private static String cloneConstraintName(String name, String suffix) {
        String suffixPart = suffix.length() >= 30 ? suffix.substring(suffix.length() - 30) : suffix;
        int prefixLength = 30 - suffixPart.length();
        if (prefixLength <= 0) {
            return suffixPart;
        }
        if (name == null || name.isEmpty()) {
            return suffixPart;
        }
        if (name.length() <= prefixLength) {
            return name + suffixPart;
        }
        String hash = Integer.toHexString(name.hashCode());
        int hashLength = Math.min(hash.length(), Math.min(6, prefixLength));
        int availablePrefixLength = prefixLength - hashLength - (hashLength > 0 ? 1 : 0);
        if (availablePrefixLength < 0) {
            availablePrefixLength = 0;
        }
        return name.substring(0, availablePrefixLength)
                + (hashLength > 0 ? "_" + hash.substring(0, hashLength) : "")
                + suffixPart;
    }

    /**
     * 过滤建表语句中已经包含的检查、外键约束。
     *
     * @param constraints 约束列表
     * @param tableSql    建表语句
     * @param <T>         约束类型
     * @return 需要通过 ALTER TABLE 创建的约束列表
     */
    private static <T extends DBName> List<T> filterConstraintsAlreadyInTableSql(
            List<T> constraints, String tableSql) {
        if (constraints == null || constraints.isEmpty()) {
            return List.of();
        }
        List<T> result = new ArrayList<>();
        for (T constraint : constraints) {
            String constraintName = constraint.getName();
            if (StringUtil.isBlank(constraintName)) {
                result.add(constraint);
                continue;
            }
            Pattern pattern = Pattern.compile(
                    "\\bCONSTRAINT\\s+(?:\"" + Pattern.quote(constraintName) + "\"|"
                            + Pattern.quote(constraintName) + "\\b)",
                    Pattern.CASE_INSENSITIVE);
            if (!pattern.matcher(tableSql).find()) {
                result.add(constraint);
            }
        }
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
            sql = sql.replace("VIEW " + DBUtil.wrap(viewName, DBDialect.ORACLE), "VIEW " + DBUtil.wrap(newViewName, DBDialect.ORACLE));
            sql = sql.replace("VIEW " + DBUtil.wrap(schema, viewName, DBDialect.ORACLE), "VIEW " + DBUtil.wrap(schema, newViewName, DBDialect.ORACLE));
            this.printSql(sql);
            Connection connection = this.getConnManager().connection(schema);
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
            if (StringUtil.isBlank(sql)) {
                throw new ShellException("Function definition is empty");
            }
            sql = ShellOracleHelper.renameRoutineDefinition(sql, "FUNCTION", functionName, newFunctionName);
            this.printSql(sql);
            Connection connection = this.getConnManager().connection(schema);
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
            if (StringUtil.isBlank(sql)) {
                throw new ShellException("Procedure definition is empty");
            }
            sql = ShellOracleHelper.renameRoutineDefinition(sql, "PROCEDURE", procedureName, newProcedureName);
            this.printSql(sql);
            Connection connection = this.getConnManager().connection(schema);
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.execute();
            IOUtil.close(stmt);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
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
    public List<OracleRoutineParam> listRoutineParam(String schema, String routineName, String routineType) throws Exception {
        try {
            Connection connection = this.getConnManager().connection(schema);
            String sql = """
                    SELECT
                    	A.POSITION,
                    	NVL(A.TYPE_NAME, A.DATA_TYPE) AS DATA_TYPE,
                    	COALESCE(NULLIF(A.DATA_PRECISION, 0), NULLIF(A.CHAR_LENGTH, 0), A.DATA_LENGTH) AS DATA_SIZE,
                    	A.DATA_SCALE AS DIGITS,
                    	A.IN_OUT AS PARAMETER_MODE,
                    	A.ARGUMENT_NAME AS PARAMETER_NAME,
                    	A.DEFAULT_VALUE,
                    	A.CHARACTER_SET_NAME
                    FROM
                    	ALL_ARGUMENTS A
                    WHERE
                    	A.OWNER = ?
                    AND
                    	A.OBJECT_NAME = ?
                    AND
                    	A.PACKAGE_NAME IS NULL
                    	AND A.OBJECT_ID = (
                    		SELECT O.OBJECT_ID
                    		FROM ALL_OBJECTS O
                    		WHERE O.OWNER = A.OWNER
                    		AND O.OBJECT_NAME = A.OBJECT_NAME
                    		AND O.OBJECT_TYPE = ?
                    	)
                    """;
            List<OracleRoutineParam> params = new ArrayList<>();
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, schema);
            statement.setString(2, routineName);
            statement.setString(3, routineType.toUpperCase());
            // 执行SQL查询并获取结果集
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                OracleRoutineParam param = new OracleRoutineParam();
                param.setValue(resultSet.getString("DEFAULT_VALUE"));
                param.setSize(resultSet.getInt("DATA_SIZE"));
                param.setDigits(resultSet.getInt("DIGITS"));
                param.setPosition(resultSet.getInt("POSITION"));
                param.setType(resultSet.getString("DATA_TYPE"));
                param.setName(resultSet.getString("PARAMETER_NAME"));
                param.setMode(resultSet.getString("PARAMETER_MODE"));
                param.setCharset(resultSet.getString("CHARACTER_SET_NAME"));
                params.add(param);
            }
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            DBName.sortItems(params);
            return params;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Collections.emptyList();
    }

    /**
     * 查询函数参数列表
     *
     * @param schema       模式名称
     * @param functionName 函数名称
     * @return 参数列表
     * @throws Exception 异常
     */
    public List<OracleRoutineParam> listFunctionParam(String schema, String functionName) throws Exception {
        return listRoutineParam(schema, functionName, "FUNCTION");
    }

    /**
     * 查询过程参数列表
     *
     * @param schema        模式名称
     * @param procedureName 过程名称
     * @return 参数列表
     * @throws Exception 异常
     */
    public List<OracleRoutineParam> listProcedureParam(String schema, String procedureName) throws Exception {
        return listRoutineParam(schema, procedureName, "PROCEDURE");
    }

    private static String routineBody(String createDefinition) {
        if (StringUtil.isBlank(createDefinition)) {
            return "";
        }
        String upper = createDefinition.toUpperCase();
        int start = -1;
        int markerLength = 0;
        for (String marker : new String[]{"\nAS\n", " AS\n", "\nAS ", " AS "}) {
            int index = upper.indexOf(marker);
            if (index >= 0 && (start < 0 || index < start)) {
                start = index;
                markerLength = marker.length();
            }
        }
        return start < 0 ? createDefinition : createDefinition.substring(start + markerLength).strip();
    }

    private static String appendCharacteristic(String characteristic, String value) {
        return StringUtil.isBlank(characteristic) ? value : characteristic + "," + value;
    }

    private static String appendCharacteristicIfMissing(String characteristic, String value) {
        if (StringUtil.containsIgnoreCase(characteristic, value)) {
            return characteristic;
        }
        return appendCharacteristic(characteristic, value);
    }

    private static String appendCharacteristicFromDefinition(String characteristic, String definition, String value) {
        if (StringUtil.containsIgnoreCase(definition, value)) {
            return appendCharacteristicIfMissing(characteristic, value);
        }
        return characteristic;
    }

    private static String parseSecurityType(String definition) {
        if (StringUtil.containsIgnoreCase(definition, "AUTHID")
                || StringUtil.containsIgnoreCase(definition, "BEQUEATH")) {
            if (StringUtil.containsIgnoreCase(definition, "CURRENT_USER")) {
                return "CURRENT_USER";
            }
            if (StringUtil.containsIgnoreCase(definition, "DEFINER")) {
                return "DEFINER";
            }
        }
        return null;
    }

    private void assertFunctionValid(Connection connection, String schema, String functionName) throws SQLException {
        String sql = """
                SELECT STATUS
                FROM ALL_OBJECTS
                WHERE OWNER = ?
                AND OBJECT_NAME = ?
                AND OBJECT_TYPE = 'FUNCTION'
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, schema);
            statement.setString(2, functionName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next() || !"VALID".equalsIgnoreCase(resultSet.getString("STATUS"))) {
                    throw new ShellException("Oracle function compilation failed: " + schema + "." + functionName);
                }
            }
        }
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
        List<OracleForeignKey> foreignKeys = this.selectForeignKeys(schema, table);
        for (OracleForeignKey foreignKey : foreignKeys) {
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
    public void createSequence(OracleCreateSequenceParam param) {
        try {
            String sql = OracleSequenceCreateSqlGenerator.generateSqlSingle(param);
            this.printSql(sql);
            Statement statement = this.getConnManager().connection(param.getSchema()).createStatement();
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
            Connection connection = this.getConnManager().connection(schema);
            String sql = """
                    SELECT
                        COUNT(*)
                    FROM
                        ALL_OBJECTS
                    WHERE
                        OWNER = ?
                    AND
                        OBJECT_TYPE = 'SEQUENCE'
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

    public List<OracleSequence> selectSequencesSimple(String schema) {
        OracleSelectSequenceParam param = new OracleSelectSequenceParam();
        param.setFull(false);
        param.setSchema(schema);
        return this.selectSequences(param);
    }

    public void cloneSequence(String schema, String sequenceName, String newSequenceName) {
        String sql = this.showCreateSequence(schema, sequenceName);
        try {
            sql = sql.replace("SEQUENCE " + DBUtil.wrap(sequenceName, DBDialect.ORACLE), "SEQUENCE " + DBUtil.wrap(newSequenceName, DBDialect.ORACLE));
            sql = sql.replace("SEQUENCE " + DBUtil.wrap(schema, sequenceName, DBDialect.ORACLE), "SEQUENCE " + DBUtil.wrap(schema, newSequenceName, DBDialect.ORACLE));
            this.printSql(sql);
            Connection connection = this.getConnManager().connection(schema);
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.execute();
            IOUtil.close(stmt);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public OracleSequence selectSequence(String schema, String sequenceName) {
        try {
            PreparedStatement statement;
            Connection connection = this.getConnManager().connection(schema);
            String sql = """
                    SELECT
                        SEQUENCE_OWNER,
                        MIN_VALUE,
                        MAX_VALUE,
                        CACHE_SIZE,
                        CYCLE_FLAG,
                        ORDER_FLAG,
                        INCREMENT_BY,
                        LAST_NUMBER AS CURRENT_VALUE,
                        NULL AS DLL
                    FROM
                        ALL_SEQUENCES
                    WHERE
                        SEQUENCE_OWNER = ?
                    AND
                        SEQUENCE_NAME = ?
                    """;
            this.printSql(sql);
            statement = connection.prepareStatement(sql);
            statement.setString(1, schema);
            statement.setString(2, sequenceName);
            ResultSet resultSet = statement.executeQuery();
            OracleSequence sequence = new OracleSequence();
            sequence.setName(sequenceName);
            while (resultSet.next()) {
                String createDefinition = ShellOracleHelper.getSequenceDefinition(connection, schema, sequenceName);
                String cycleFlag = resultSet.getString("CYCLE_FLAG");
                String orderFlag = resultSet.getString("ORDER_FLAG");
                sequence.setSchema(schema);
                sequence.setMinVal(sequenceNumber(resultSet, "MIN_VALUE"));
                sequence.setMaxVal(sequenceNumber(resultSet, "MAX_VALUE"));
                sequence.setCacheSize(sequenceNumber(resultSet, "CACHE_SIZE"));
                sequence.setCurrentVal(sequenceNumber(resultSet, "CURRENT_VALUE"));
                sequence.setIncrementBy(sequenceNumber(resultSet, "INCREMENT_BY"));
                sequence.setCreateDefinition(createDefinition);
                sequence.setCycle(StringUtil.equalsIgnoreCase("Y", cycleFlag));
                sequence.setOrder(StringUtil.equalsIgnoreCase("Y", orderFlag));
            }
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            return sequence;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public void dropSequence(OracleSequence sequence) {
        try {
            String sql = "DROP SEQUENCE " + DBUtil.wrap(sequence.getSchema(), sequence.getName(), DBDialect.ORACLE);
            this.printSql(sql);
            Statement statement = this.getConnManager().getConnection(sequence.getSchema()).createStatement();
            statement.executeUpdate(sql);
            IOUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public void renameSequence(String schema, String oldSequenceName, String newSequenceName) {
        try {
            this.cloneSequence(schema, oldSequenceName, newSequenceName);
            OracleSequence sequence = new OracleSequence();
            sequence.setSchema(schema);
            sequence.setName(oldSequenceName);
            this.dropSequence(sequence);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public void alertSequence(OracleAlertSequenceParam param) {
        Connection connection = null;
        try {
            String schema = param.getSchema();
            param.setOriginalSequence(this.selectSequence(schema, param.getSequenceName()));
            String sql = OracleSequenceAlertSqlGenerator.generateSqlSingle(param);
            // 无变化
            if (StringUtil.isBlank(sql)) {
                return;
            }
            connection = this.getConnManager().connection(schema);
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();
            this.printSql(sql);
            statement.executeUpdate(sql);
            connection.commit();
            IOUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            DBUtil.rollback(connection);
            throw new ShellException(ex);
        }
    }

    @Override
    public void printSql(String sql) {
        DBUtil.printSql(sql);
        String compressedSql = DBSqlParser.compressSql(sql, this.dialect());
        ShellEventUtil.printSql(compressedSql, this.shellConnect);
    }
}
