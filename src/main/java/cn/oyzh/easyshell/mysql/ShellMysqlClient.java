package cn.oyzh.easyshell.mysql;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.db.DBDialect;
import cn.oyzh.easyshell.db.DBFeature;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellJumpConfig;
import cn.oyzh.easyshell.dto.mysql.MysqlDatabase;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.internal.ShellBaseClient;
import cn.oyzh.easyshell.internal.ShellConnState;
import cn.oyzh.easyshell.mysql.check.MysqlCheck;
import cn.oyzh.easyshell.mysql.check.MysqlChecks;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.mysql.column.MysqlColumns;
import cn.oyzh.easyshell.mysql.column.MysqlSelectColumnParam;
import cn.oyzh.easyshell.mysql.condition.MysqlConditionUtil;
import cn.oyzh.easyshell.mysql.event.MysqlEvent;
import cn.oyzh.easyshell.mysql.foreignKey.MysqlForeignKey;
import cn.oyzh.easyshell.mysql.foreignKey.MysqlForeignKeys;
import cn.oyzh.easyshell.mysql.function.MysqlFunction;
import cn.oyzh.easyshell.mysql.generator.event.EventAlertSqlGenerator;
import cn.oyzh.easyshell.mysql.generator.event.EventCreateSqlGenerator;
import cn.oyzh.easyshell.mysql.generator.routine.MysqlFunctionSqlGenerator;
import cn.oyzh.easyshell.mysql.generator.routine.MysqlProcedureSqlGenerator;
import cn.oyzh.easyshell.mysql.generator.table.MysqlTableAlertSqlGenerator;
import cn.oyzh.easyshell.mysql.generator.table.MysqlTableCreateSqlGenerator;
import cn.oyzh.easyshell.mysql.index.MysqlIndex;
import cn.oyzh.easyshell.mysql.index.MysqlIndexes;
import cn.oyzh.easyshell.mysql.procedure.MysqlProcedure;
import cn.oyzh.easyshell.mysql.query.MysqlExecuteResult;
import cn.oyzh.easyshell.mysql.query.MysqlExplainResult;
import cn.oyzh.easyshell.mysql.query.MysqlQueryResults;
import cn.oyzh.easyshell.mysql.record.MysqlDeleteRecordParam;
import cn.oyzh.easyshell.mysql.record.MysqlInsertRecordParam;
import cn.oyzh.easyshell.mysql.record.MysqlRecord;
import cn.oyzh.easyshell.mysql.record.MysqlRecordData;
import cn.oyzh.easyshell.mysql.record.MysqlRecordFilter;
import cn.oyzh.easyshell.mysql.record.MysqlRecordPrimaryKey;
import cn.oyzh.easyshell.mysql.record.MysqlSelectRecordParam;
import cn.oyzh.easyshell.mysql.record.MysqlUpdateRecordParam;
import cn.oyzh.easyshell.mysql.routine.MysqlRoutineParam;
import cn.oyzh.easyshell.mysql.sql.DBSqlParser;
import cn.oyzh.easyshell.mysql.table.MysqlAlertTableParam;
import cn.oyzh.easyshell.mysql.table.MysqlCreateTableParam;
import cn.oyzh.easyshell.mysql.table.MysqlSelectTableParam;
import cn.oyzh.easyshell.mysql.table.MysqlTable;
import cn.oyzh.easyshell.mysql.trigger.MysqlTrigger;
import cn.oyzh.easyshell.mysql.trigger.MysqlTriggers;
import cn.oyzh.easyshell.mysql.view.MysqlView;
import cn.oyzh.easyshell.util.mysql.ShellMysqlUtil;
import cn.oyzh.ssh.domain.SSHConnect;
import cn.oyzh.ssh.jump.SSHJumpForwarder2;
import com.alibaba.druid.DbType;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * db客户端封装
 *
 * @author oyzh
 * @since 2023/11/06
 */
public class ShellMysqlClient implements ShellBaseClient {

    /**
     * ssh端口转发器
     */
    private SSHJumpForwarder2 jumpForwarder;

    /**
     * db信息
     */
    protected ShellConnect shellConnect;

    // /**
    //  * 连接配置
    //  */
    // protected final MysqlConnConfig connConfig = new MysqlConnConfig();

    /**
     * 数据库连接管理器
     */
    protected ShellMysqlConnManager connManager = new ShellMysqlConnManager();

    // private boolean isInvalid(Connection connection) throws SQLException {
    //     if (connection == null || connection.isClosed()) {
    //         return true;
    //     }
    //     return !connection.isValid(10);
    // }
    //
    // public Connection connection() throws SQLException, ClassNotFoundException {
    //     Connection connection = this.connectionManager.getServerConnection();
    //     if (this.isInvalid(connection)) {
    //         connection = this.initConnection(this.connConfig, null, this.shellConnect.getUser(), this.shellConnect.getPassword());
    //         this.connectionManager.setServerConnection(connection);
    //     }
    //     return connection;
    // }
    //
    // public Connection connection(String dbName) throws SQLException, ClassNotFoundException {
    //     Connection connection = this.connectionManager.getConnection(dbName);
    //     if (this.isInvalid(connection)) {
    //         connection = this.initConnection(this.connConfig, dbName, this.shellConnect.getUser(), this.shellConnect.getPassword());
    //         this.connectionManager.addConnection(dbName, connection);
    //     }
    //     connection.setAutoCommit(true);
    //     return connection;
    // }
    //
    // public Connection connection(String dbName, String schema) throws SQLException, ClassNotFoundException {
    //     if (schema == null) {
    //         return this.connection(dbName);
    //     }
    //     Connection connection = this.connectionManager.getSchemaConnection(dbName, schema);
    //     if (this.isInvalid(connection)) {
    //         connection = this.initConnection(this.connConfig, dbName, this.shellConnect.getUser(), this.shellConnect.getPassword());
    //         this.connectionManager.addSchemaConnection(dbName, schema, connection);
    //     }
    //     connection.setAutoCommit(true);
    //     return connection;
    // }
    //
    // public Connection functionConnection(String dbName, String schema) throws SQLException, ClassNotFoundException {
    //     Connection connection = this.connectionManager.getFunctionConnection(dbName, schema);
    //     if (this.isInvalid(connection)) {
    //         connection = this.initConnection(this.connConfig, dbName, this.shellConnect.getUser(), this.shellConnect.getPassword());
    //         this.connectionManager.addFunctionConnection(dbName, schema, connection);
    //     }
    //     connection.setAutoCommit(true);
    //     return connection;
    // }
    //
    // public Connection procedureConnection(String dbName, String schema) throws SQLException, ClassNotFoundException {
    //     Connection connection = this.connectionManager.getProcedureConnection(dbName, schema);
    //     if (this.isInvalid(connection)) {
    //         connection = this.initConnection(this.connConfig, dbName, this.shellConnect.getUser(), this.shellConnect.getPassword());
    //         this.connectionManager.addProcedureConnection(dbName, schema, connection);
    //     }
    //     connection.setAutoCommit(true);
    //     return connection;
    // }
    //
    // public Connection newConnection(String dbName) throws SQLException, ClassNotFoundException {
    //     Connection connection = this.initConnection(this.connConfig, dbName, this.shellConnect.getUser(), this.shellConnect.getPassword());
    //     connection.setAutoCommit(true);
    //     return connection;
    // }
    //
    // protected Connection initConnection(MysqlConnConfig connConfig, String dbName, String user, String password) throws ClassNotFoundException, SQLException {
    //     // 加载JDBC驱动
    //     Class.forName("com.mysql.cj.jdbc.Driver");
    //     String host = connConfig.getConnectionString(this.dialect());
    //     if (dbName != null) {
    //         host += dbName;
    //     }
    //     host = host +
    //             "?testOnBorrow=true" +
    //             "&tcpKeepAlive=true" +
    //             "&autoReconnect=true" +
    //             "&testWhileIdle=true" +
    //             "&validationQuery=SELECT 1" +
    //             "&zeroDateTimeBehavior=convertToNull"
    //     ;
    //     // 创建数据库连接
    //     return DriverManager.getConnection(host, user, password);
    // }

    /**
     * 属性列表
     */
    private Map<String, Object> properties;

    /**
     * 获取属性
     *
     * @param key 键
     * @param <T> 属性类型
     * @return 属性
     */
    protected <T> T getProperty(String key) {
        return this.properties == null || key == null ? null : (T) this.properties.get(key);
    }

    /**
     * 是否有此属性
     *
     * @param key 键
     * @return 结果
     */
    protected boolean hasProperty(String key) {
        return this.properties != null && this.properties.containsKey(key);
    }

    /**
     * 添加属性
     *
     * @param key   键
     * @param value 值
     */
    protected void putProperty(String key, Object value) {
        if (key != null && value != null) {
            if (this.properties == null) {
                this.properties = new HashMap<>();
            }
            this.properties.put(key, value);
        }
    }

    /**
     * 获取连接状态
     *
     * @return 连接状态
     */
    public ShellConnState state() {
        return this.stateProperty().get();
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

    public ShellMysqlClient(ShellConnect shellConnect) {
        this.shellConnect = shellConnect;
        this.addStateListener(this.stateListener);
    }

    /**
     * 是否只读模式
     *
     * @return 结果
     */
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
        try {
            // 开始连接时间
            final AtomicLong starTime = new AtomicLong();
            // 开始连接时间
            starTime.set(System.currentTimeMillis());
            // 更新连接状态
            this.state.set(ShellConnState.CONNECTING);
            // 连接成功前阻塞线程
            if (this.connManager.connection().isValid(timeout / 1000)) {
                // 更新连接状态
                this.state.set(ShellConnState.CONNECTED);
            } else {// 连接未成功则关闭
                this.close();
                if (this.state.get() == ShellConnState.FAILED) {
                    this.state.set(null);
                } else {
                    this.state.set(ShellConnState.FAILED);
                }
            }
        } catch (Exception ex) {
            this.state.set(ShellConnState.FAILED);
            JulLog.warn("Mysql client start error", ex);
            throw new ShellException(ex);
        }
    }

    @Override
    public ShellConnect getShellConnect() {
        return this.shellConnect;
    }

    /**
     * 初始化连接
     *
     * @return 连接
     */
    private String initHost() {
        // 连接地址
        String host;
        // 初始化跳板转发
        if (this.shellConnect.isEnableJump()) {
            if (this.jumpForwarder == null) {
                this.jumpForwarder = new SSHJumpForwarder2();
            }
            // 初始化跳板配置
            List<ShellJumpConfig> jumpConfigs = this.shellConnect.getJumpConfigs();
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
        this.connManager.setHost(ip);
        this.connManager.setPort(port);
        this.connManager.setUser(this.shellConnect.getUser());
        this.connManager.setPassword(this.shellConnect.getPassword());
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
            JulLog.warn("Mysql client close error.", ex);
        }
    }

    @Override
    public boolean isConnected() {
        ShellConnState state = this.state.get();
        return state != null && state.isConnected();
    }

    /**
     * db是否连接中
     *
     * @return 结果
     */
    public boolean isConnecting() {
        return this.state() == ShellConnState.CONNECTING;
    }

    /**
     * 获取表数量
     *
     * @param dbName 库名称或者模式名称
     * @return 表数量
     */
    public int tableSize(String dbName) {
        try {
            int size = 0;
            Connection connection = this.connManager.connection(dbName);
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet resultSet = metaData.getTables(null, dbName, "%", TABLE_TYPES);
            ShellMysqlUtil.printMetaData(resultSet);
            while (resultSet.next()) {
                if (ShellMysqlUtil.checkTableType(resultSet, dbName)) {
                    size++;
                }
            }
            ShellMysqlUtil.close(resultSet);
            return size;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
        // int size = 0;
        // try {
        //     Connection connection = this.connection(dbName);
        //     String sql = """
        //             SELECT
        //                 COUNT(*)
        //             FROM
        //                 information_schema.TABLES
        //             WHERE
        //                 TABLE_SCHEMA = ?
        //             AND
        //                 TABLE_TYPE = 'BASE TABLE'
        //             OR
        //                 TABLE_TYPE = 'TABLE'
        //             OR
        //                 TABLE_TYPE = 'SYSTEM VIEW'
        //             OR
        //                 TABLE_TYPE = 'SYSTEM TABLE'
        //             """;
        //     ShellMysqlUtil.printSql(sql);
        //     PreparedStatement statement = connection.prepareStatement(sql);
        //     statement.setString(1, dbName);
        //     ResultSet resultSet = statement.executeQuery();
        //     ShellMysqlUtil.printMetaData(resultSet);
        //     if (resultSet.next()) {
        //         size = resultSet.getInt(1);
        //     }
        //     ShellMysqlUtil.close(resultSet);
        // } catch (Exception ex) {
        //     ex.printStackTrace();
        //     throw new ShellException(ex);
        // }
        // return size;
    }

    /**
     * 获取视图数量
     *
     * @param dbName 库名称或者模式名称
     * @return 视图数量
     */
    public int viewSize(String dbName) {
        try {
            int size = 0;
            Connection connection = this.connManager.connection(dbName);
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet resultSet = metaData.getTables(null, dbName, "%", VIEW_TYPES);
            ShellMysqlUtil.printMetaData(resultSet);
            while (resultSet.next()) {
                if (ShellMysqlUtil.checkViewType(resultSet, dbName)) {
                    size++;
                }
            }
            ShellMysqlUtil.close(resultSet);
            return size;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public MysqlQueryResults<MysqlExecuteResult> executeSql(String dbName, String sql) {
        MysqlQueryResults<MysqlExecuteResult> results = new MysqlQueryResults<>();
        Connection connection = null;
        try {
            ShellMysqlUtil.printSql(sql);
            DBSqlParser parser = DBSqlParser.getParser(sql, this.dialect());
            List<String> list = parser.parseSql();
            connection = this.connManager.connection(dbName);
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();
            for (String execSql : list) {
                MysqlExecuteResult result = new MysqlExecuteResult();
                result.setSql(execSql);
                try {
                    long startTime = System.nanoTime();
                    boolean isQuery = statement.execute(execSql);
                    if (isQuery) {
                        ResultSet resultSet = statement.getResultSet();
                        if (parser.isSingle()) {
                            result.setFullColumn(parser.isFullColumn());
                        } else {
                            result.setFullColumn(ShellMysqlUtil.isFullColumn(execSql, this.dbType()));
                        }
                        result.parseResult(resultSet, connection, !parser.isSelect());
                        ShellMysqlUtil.close(resultSet);
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
            ShellMysqlUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            ShellMysqlUtil.rollback(connection);
            results.parseError(ex);
        }
        return results;
    }

    public int insertBatch(String dbName, List<String> sqlList) {
        return this.insertBatch(dbName, sqlList, false);
    }

    public int procedureSize(String dbName) {
        // int size = 0;
        // try {
        //     Connection connection = this.procedureConnection(dbName, schema);
        //     DatabaseMetaData metaData = connection.getMetaData();
        //     ResultSet resultSet = metaData.getProcedures(dbName, schema, "%");
        //     ShellMysqlUtil.printMetaData(resultSet);
        //     while (resultSet.next()) {
        //         if (ShellMysqlUtil.checkProcedureType(resultSet, dbName)) {
        //             size++;
        //         }
        //     }
        //     ShellMysqlUtil.close(resultSet);
        // } catch (Exception ex) {
        //     ex.printStackTrace();
        //     throw new ShellException(ex);
        // }
        // return size;
        int size = 0;
        try {
            Connection connection = this.connManager.procedureConnection(dbName);
            String sql = """
                    SELECT
                        COUNT(*)
                    FROM
                        information_schema.ROUTINES
                    WHERE
                        ROUTINE_SCHEMA = ?
                    AND
                        ROUTINE_TYPE = 'PROCEDURE';
                    """;
            ShellMysqlUtil.printSql(sql);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, dbName);
            ResultSet resultSet = statement.executeQuery();
            ShellMysqlUtil.printMetaData(resultSet);
            if (resultSet.next()) {
                size = resultSet.getInt(1);
            }
            ShellMysqlUtil.close(resultSet);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
        return size;
    }

    public int functionSize(String dbName) {
        // int size = 0;
        // try {
        //     Connection connection = this.functionConnection(dbName, schema);
        //     DatabaseMetaData metaData = connection.getMetaData();
        //     ResultSet resultSet = metaData.getFunctions(dbName, schema, "%");
        //     ShellMysqlUtil.printMetaData(resultSet);
        //     if (resultSet.next()) {
        //         if (ShellMysqlUtil.checkFunctionType(resultSet, dbName)) {
        //             size++;
        //         }
        //     }
        //     ShellMysqlUtil.close(resultSet);
        // } catch (Exception ex) {
        //     ex.printStackTrace();
        //     throw new ShellException(ex);
        // }
        // return size;
        int size = 0;
        try {
            Connection connection = this.connManager.functionConnection(dbName);
            String sql = """
                    SELECT
                        COUNT(*)
                    FROM
                        information_schema.ROUTINES
                    WHERE
                        ROUTINE_SCHEMA = ?
                    AND
                        ROUTINE_TYPE = 'FUNCTION';
                    """;
            ShellMysqlUtil.printSql(sql);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, dbName);
            ResultSet resultSet = statement.executeQuery();
            ShellMysqlUtil.printMetaData(resultSet);
            if (resultSet.next()) {
                size = resultSet.getInt(1);
            }
            ShellMysqlUtil.close(resultSet);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
        return size;
    }

    public void alertFunction(String dbName, MysqlFunction function) {
        try {
            String sql = "DROP FUNCTION IF EXISTS " + ShellMysqlUtil.wrap(dbName, function.getName(), this.dialect());
            ShellMysqlUtil.printSql(sql);
            Connection connection = this.connManager.connection(dbName);
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
            sql = MysqlFunctionSqlGenerator.INSTANCE.generate(function);
            ShellMysqlUtil.printSql(sql);
            statement.executeUpdate(sql);
            ShellMysqlUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public List<MysqlTrigger> triggers(String dbName) {
        try {
            String sql = """
                    SELECT 
                        TRIGGER_NAME,ACTION_STATEMENT,ACTION_TIMING,EVENT_MANIPULATION,EVENT_OBJECT_TABLE
                    FROM 
                        INFORMATION_SCHEMA.TRIGGERS 
                    WHERE 
                        TRIGGER_SCHEMA = ?
                    """;
            ShellMysqlUtil.printSql(sql);
            PreparedStatement statement = this.connManager.connection().prepareStatement(sql);
            statement.setString(1, dbName);
            ResultSet resultSet = statement.executeQuery();
            List<MysqlTrigger> list = new ArrayList<>();
            while (resultSet.next()) {
                MysqlTrigger trigger = new MysqlTrigger();
                String name = resultSet.getString("TRIGGER_NAME");
                String timing = resultSet.getString("ACTION_TIMING");
                String tableName = resultSet.getString("EVENT_OBJECT_TABLE");
                String manipulation = resultSet.getString("EVENT_MANIPULATION");
                String actionStatement = resultSet.getString("ACTION_STATEMENT");
                trigger.setName(name);
                trigger.setTableName(tableName);
                trigger.setDefinition(actionStatement);
                trigger.setPolicy(timing, manipulation);
                list.add(trigger);
            }
            ShellMysqlUtil.close(resultSet);
            ShellMysqlUtil.close(statement);
            return list;
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    public MysqlTriggers triggers(String dbName, String tableName) {
        try {
            String sql = """
                    SELECT 
                        TRIGGER_NAME,ACTION_STATEMENT,ACTION_TIMING,EVENT_MANIPULATION
                    FROM 
                        INFORMATION_SCHEMA.TRIGGERS
                    WHERE 
                        TRIGGER_SCHEMA = ?
                    AND 
                        EVENT_OBJECT_TABLE = ?
                    """;
            ShellMysqlUtil.printSql(sql);
            PreparedStatement statement = this.connManager.connection().prepareStatement(sql);
            statement.setString(1, dbName);
            statement.setString(2, tableName);
            ResultSet resultSet = statement.executeQuery();
            MysqlTriggers list = new MysqlTriggers();
            while (resultSet.next()) {
                MysqlTrigger trigger = new MysqlTrigger();
                String name = resultSet.getString("TRIGGER_NAME");
                String timing = resultSet.getString("ACTION_TIMING");
                String manipulation = resultSet.getString("EVENT_MANIPULATION");
                String actionStatement = resultSet.getString("ACTION_STATEMENT");
                trigger.setName(name);
                trigger.setDefinition(actionStatement);
                trigger.setPolicy(timing, manipulation);
                list.add(trigger);
            }
            ShellMysqlUtil.close(resultSet);
            ShellMysqlUtil.close(statement);
            return list;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public String selectVersion() {
        if (this.hasProperty("version")) {
            return this.getProperty("version");
        }
        String version = "";
        try {
            Connection conn = this.connManager.connection();
            Statement stmt = conn.createStatement();
            ResultSet resultSet = stmt.executeQuery("SELECT VERSION()");
            if (resultSet.next()) {
                version = resultSet.getString(1);
            }
            this.putProperty("version", version);
            ShellMysqlUtil.close(resultSet);
            ShellMysqlUtil.close(stmt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return version;
    }

    public void dropEvent(String dbName, MysqlEvent event) {
        try {
            String sql = "DROP EVENT " + ShellMysqlUtil.wrap(event.getDbName(), event.getName(), this.dialect());
            ShellMysqlUtil.printSql(sql);
            Statement statement = this.connManager.connection(dbName).createStatement();
            statement.executeUpdate(sql);
            ShellMysqlUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public void createEvent(String dbName, MysqlEvent event) {
        try {
            String sql = EventCreateSqlGenerator.generate(this.dialect(), event);
            ShellMysqlUtil.printSql(sql);
            Statement statement = this.connManager.connection(dbName).createStatement();
            statement.executeUpdate(sql);
            ShellMysqlUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public void alertEvent(String dbName, MysqlEvent event) {
        try {
            String sql = EventAlertSqlGenerator.generate(this.dialect(), event);
            ShellMysqlUtil.printSql(sql);
            Statement statement = this.connManager.connection(dbName).createStatement();
            statement.executeUpdate(sql);
            ShellMysqlUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public MysqlEvent selectEvent(String dbName, String eventName) {
        MysqlEvent event = new MysqlEvent();
        try {
            String sql = """
                    SELECT 
                        * 
                    FROM 
                        `INFORMATION_SCHEMA`.`EVENTS` 
                    WHERE 
                        `EVENT_SCHEMA` = ? 
                    AND 
                        `EVENT_NAME` = ?
                    """;
            ShellMysqlUtil.printSql(sql);
            PreparedStatement statement = this.connManager.connection().prepareStatement(sql);
            statement.setString(1, dbName);
            statement.setString(2, eventName);
            ResultSet resultSet = statement.executeQuery();
            ShellMysqlUtil.printMetaData(resultSet);
            while (resultSet.next()) {
                Date ends = resultSet.getDate("ENDS");
                Date starts = resultSet.getDate("STARTS");
                String status = resultSet.getString("STATUS");
                String type = resultSet.getString("EVENT_TYPE");
                Date executeAt = resultSet.getDate("EXECUTE_AT");
                String comment = resultSet.getString("EVENT_COMMENT");
                int intervalValue = resultSet.getInt("INTERVAL_VALUE");
                String onCompletion = resultSet.getString("ON_COMPLETION");
                String definition = resultSet.getString("EVENT_DEFINITION");
                String intervalField = resultSet.getString("INTERVAL_FIELD");
                String createDefinition = this.showCreateEvent(dbName, eventName);
                event.setName(eventName);
                event.setType(type);
                event.setEnds(ends);
                event.setStarts(starts);
                event.setDbName(dbName);
                event.setStatus(status);
                event.setComment(comment);
                event.setDefinition(definition);
                event.setOnCompletion(status);
                event.setExecuteAt(executeAt);
                event.setOnCompletion(onCompletion);
                event.setIntervalValue(intervalValue);
                event.setIntervalField(intervalField);
                event.setCreateDefinition(createDefinition);
            }
            ShellMysqlUtil.close(resultSet);
            ShellMysqlUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
        return event;
    }

    public Integer eventSize(String dbName) {
        int count = 0;
        try {
            String sql = """
                    SELECT 
                        COUNT(*) 
                    FROM 
                        `INFORMATION_SCHEMA`.`EVENTS` 
                    WHERE 
                        `EVENT_SCHEMA` = ?
                    """;
            ShellMysqlUtil.printSql(sql);
            PreparedStatement statement = this.connManager.connection().prepareStatement(sql);
            statement.setString(1, dbName);
            ResultSet resultSet = statement.executeQuery();
            ShellMysqlUtil.printMetaData(resultSet);
            while (resultSet.next()) {
                count = resultSet.getInt(1);
            }
            ShellMysqlUtil.close(resultSet);
            ShellMysqlUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
        return count;
    }

    public List<MysqlEvent> events(String dbName) {
        List<MysqlEvent> list = new ArrayList<>();
        try {
            String sql = """
                    SELECT 
                        * 
                    FROM 
                        `INFORMATION_SCHEMA`.`EVENTS` 
                    WHERE 
                        `EVENT_SCHEMA` = ?
                    """;
            ShellMysqlUtil.printSql(sql);
            PreparedStatement statement = this.connManager.connection().prepareStatement(sql);
            statement.setString(1, dbName);
            ResultSet resultSet = statement.executeQuery();
            ShellMysqlUtil.printMetaData(resultSet);
            while (resultSet.next()) {
                MysqlEvent event = new MysqlEvent();
                Date ends = resultSet.getDate("ENDS");
                Date starts = resultSet.getDate("STARTS");
                String status = resultSet.getString("STATUS");
                String name = resultSet.getString("EVENT_NAME");
                String type = resultSet.getString("EVENT_TYPE");
                Date executeAt = resultSet.getDate("EXECUTE_AT");
                String comment = resultSet.getString("EVENT_COMMENT");
                int intervalValue = resultSet.getInt("INTERVAL_VALUE");
                String onCompletion = resultSet.getString("ON_COMPLETION");
                String definition = resultSet.getString("EVENT_DEFINITION");
                String intervalField = resultSet.getString("INTERVAL_FIELD");
                String createDefinition = this.showCreateEvent(dbName, name);
                event.setName(name);
                event.setType(type);
                event.setEnds(ends);
                event.setStarts(starts);
                event.setDbName(dbName);
                event.setStatus(status);
                event.setComment(comment);
                event.setDefinition(definition);
                event.setOnCompletion(status);
                event.setExecuteAt(executeAt);
                event.setOnCompletion(onCompletion);
                event.setIntervalValue(intervalValue);
                event.setIntervalField(intervalField);
                event.setCreateDefinition(createDefinition);
                list.add(event);
            }
            ShellMysqlUtil.close(resultSet);
            ShellMysqlUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
        return list;
    }

    public boolean isSupportFeature(DBFeature feature) {
        try {
            if (feature == DBFeature.EVENT) {
                return true;
            }
            // 检查约束
            if (feature == DBFeature.CHECK) {
                // 最低支持版本8.0.16
                String version = this.selectVersion();
                String[] arr = version.split("\\.");
                if (Integer.parseInt(arr[0]) < 8) {
                    return false;
                }
                return Integer.parseInt(arr[2]) >= 16;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean isSupportCheckFeature() {
        return this.isSupportFeature(DBFeature.CHECK);
    }

    public boolean isSupportEventFeature() {
        return this.isSupportFeature(DBFeature.EVENT);
    }

    public static final String[] TABLE_TYPES = new String[]{"TABLE", "SYSTEM TABLE", "SYSTEM VIEW", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM"};

    public static final String[] VIEW_TYPES = new String[]{"VIEW"};

    public List<MysqlTable> selectTables(String dbName) {
        MysqlSelectTableParam param = new MysqlSelectTableParam();
        param.setDbName(dbName);
        return this.selectTables(param);
    }

    public List<MysqlTable> selectTables(MysqlSelectTableParam param) {
        try {
            String dbName = param.getDbName();
            List<MysqlTable> tables = new ArrayList<>();
            Connection connection = this.connManager.connection(dbName);
            if (param.isFull()) {
                String sql = """
                        SELECT 
                            `AUTO_INCREMENT`, `ROW_FORMAT`, `TABLE_COLLATION`, `TABLE_NAME`, `TABLE_COMMENT`, `ENGINE` 
                        FROM 
                            information_schema.TABLES 
                        WHERE 
                            `TABLE_SCHEMA` = ? 
                        AND 
                            `TABLE_TYPE` != 'VIEW'
                        """;
                ShellMysqlUtil.printSql(sql);
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setString(1, dbName);
                ResultSet resultSet = statement.executeQuery();
                ShellMysqlUtil.printMetaData(resultSet);
                while (resultSet.next()) {
                    MysqlTable table = new MysqlTable();
                    String tableEngine = resultSet.getString("ENGINE");
                    String tableName = resultSet.getString("TABLE_NAME");
                    String rowFormat = resultSet.getString("ROW_FORMAT");
                    Long autoIncrement = resultSet.getLong("AUTO_INCREMENT");
                    String tableComment = resultSet.getString("TABLE_COMMENT");
                    String tableCollation = resultSet.getString("TABLE_COLLATION");
                    String showCreateTable = this.showCreateTable(dbName, tableName);
                    table.setDbName(dbName);
                    table.setName(tableName);
                    table.setEngine(tableEngine);
                    table.setRowFormat(rowFormat);
                    table.setComment(tableComment);
                    table.setAutoIncrement(autoIncrement);
                    table.setCreateDefinition(showCreateTable);
                    table.setCharsetAndCollation(tableCollation);
                    tables.add(table);
                }
                ShellMysqlUtil.close(resultSet);
                ShellMysqlUtil.close(statement);
            } else {
                DatabaseMetaData metaData = connection.getMetaData();
                ResultSet resultSet = metaData.getTables(null, null, "%", TABLE_TYPES);
                while (resultSet.next()) {
                    if (ShellMysqlUtil.checkTableType(resultSet, dbName)) {
                        MysqlTable table = new MysqlTable();
                        table.setDbName(dbName);
                        String remarks = resultSet.getString("REMARKS");
                        String tableName = resultSet.getString("TABLE_NAME");
                        table.setName(tableName);
                        table.setComment(remarks);
                        tables.add(table);
                    }
                }
                ShellMysqlUtil.close(resultSet);
            }
            return tables;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public MysqlColumns selectColumns(MysqlSelectColumnParam param) {
        try {
            String dbName = param.getDbName();
            String tableName = param.getTableName();
            MysqlColumns columns = new MysqlColumns();
            String sql = "SHOW FULL COLUMNS FROM " + ShellMysqlUtil.wrap(dbName, tableName, this.dialect());
            PreparedStatement statement = this.connManager.connection().prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();
            ShellMysqlUtil.printMetaData(resultSet);
            int position = 0;
            while (resultSet.next()) {
                String key = resultSet.getString("Key");
                String type = resultSet.getString("Type");
                String field = resultSet.getString("Field");
                Object def = resultSet.getObject("Default");
                String extra = resultSet.getString("Extra");
                String nullable = resultSet.getString("Null");
                String comment = resultSet.getString("Comment");
                String collation = resultSet.getString("COLLATION");

                MysqlColumn column = new MysqlColumn();
                column.parseKey(key);
                column.parseType(type);
                column.parseExtra(extra);
                column.parseCollation(collation);

                column.setName(field);
                column.setDbName(dbName);
                column.setComment(comment);
                column.setDefaultValue(def);
                column.setPosition(position++);
                column.setTableName(tableName);
                column.setNullable("yes".equalsIgnoreCase(nullable));
                columns.add(column);
            }
            ShellMysqlUtil.close(resultSet);
            // 返回排序后的数据
            return new MysqlColumns(columns.sortOfPosition());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public List<MysqlRecord> selectRecords(MysqlSelectRecordParam param) {
        try {
            Connection connection = this.connManager.connection(param.getDbName());
            StringBuilder builder = new StringBuilder("SELECT * FROM ");
            builder.append(ShellMysqlUtil.wrap(param.getDbName(), param.getTableName(), this.dialect()));
            String filterCondition = MysqlConditionUtil.buildCondition(param.getFilters());
            if (StringUtil.isNotBlank(filterCondition)) {
                builder.append(" WHERE ").append(filterCondition);
            }
            if (param.hasPageControl()) {
                builder.append(" LIMIT ")
                        .append(param.getStart())
                        .append(",")
                        .append(param.getLimit());
            }
            String sql = builder.toString();
            ShellMysqlUtil.printSql(sql);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            ShellMysqlUtil.printMetaData(resultSet);
            List<MysqlRecord> records = new ArrayList<>();
            List<MysqlColumn> columns;
            if (param.getColumns() != null) {
                columns = param.getColumns();
            } else {
                columns = ShellMysqlHelper.parseColumns(resultSet);
            }
            while (resultSet.next()) {
                MysqlRecord record = new MysqlRecord(columns, param.isReadonly());
                for (MysqlColumn column : columns) {
                    Object data = resultSet.getObject(column.getName());
                    // 获取几何值
                    if (column.supportGeometry()) {
                        data = ShellMysqlHelper.getGeometryString(connection, data);
                    }
                    record.putValue(column, data);
                }
                records.add(record);
            }
            ShellMysqlUtil.close(resultSet);
            ShellMysqlUtil.close(statement);
            return records;
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    public long selectRecordCount(MysqlSelectRecordParam param) {
        long count = 0;
        try {
            Connection connection = this.connManager.connection(param.getDbName());
            StringBuilder builder = new StringBuilder("SELECT COUNT(*) FROM");
            builder.append(ShellMysqlUtil.wrap(param.getDbName(), param.getTableName(), this.dialect()));
            String filterCondition = MysqlConditionUtil.buildCondition(param.getFilters());
            if (StringUtil.isNotBlank(filterCondition)) {
                builder.append(" WHERE ").append(filterCondition);
            }
            String sql = builder.toString();
            ShellMysqlUtil.printSql(sql);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
                count = resultSet.getLong(1);
            }
            ShellMysqlUtil.close(resultSet);
            ShellMysqlUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
        return count;
    }

    public int insertRecord(MysqlInsertRecordParam param) {
        if (param == null || param.getRecord() == null) {
            return 0;
        }
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("INSERT INTO ")
                    .append(ShellMysqlUtil.wrap(param.getDbName(), param.getTableName(), this.dialect()))
                    .append("(");
            for (String column : param.getRecord().columns()) {
                builder.append(ShellMysqlUtil.wrap(column, this.dialect())).append(",");
            }
            builder.append(")");
            builder.append(" VALUES(");
            for (String column : param.getRecord().columns()) {
                if (param.getRecord().isTypeGeometry(column)) {
                    builder.append("ST_GeomFromText(?),");
                } else {
                    builder.append("?,");
                }
            }
            builder.append(")");
            String sql = builder.toString();
            sql = sql.replaceAll(",\\)", ")");
            ShellMysqlUtil.printSql(sql);
            Connection connection = this.connManager.connection(param.getDbName());
            PreparedStatement statement = connection.prepareStatement(sql);
            int index = 1;
            for (String colName : param.getRecord().columns()) {
                ShellMysqlUtil.setVal(statement, param.getRecord().value(colName), index++);
            }
            int count = statement.executeUpdate();
            MysqlRecordPrimaryKey primaryKey = param.getPrimaryKey();
            // 处理自动递增值
            if (primaryKey != null && primaryKey.shouldReturnData()) {
                primaryKey.setReturnData(ShellMysqlHelper.lastInsertId(connection));
            }
            ShellMysqlUtil.close(statement);
            return count;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public int deleteRecord(MysqlDeleteRecordParam param) {
        try {
            int updateCount;
            String dbName = param.getDbName();
            // String schema = param.getSchema();
            String tableName = param.getTableName();
            Connection connection = this.connManager.connection(dbName);
            StringBuilder builder = new StringBuilder();
            builder.append("DELETE FROM ")
                    .append(ShellMysqlUtil.wrap(dbName, tableName, this.dialect()))
                    .append(" WHERE ");
            if (param.getPrimaryKey() == null) {
                MysqlRecordData recordData = param.getRecord();
                boolean first = true;
                for (String colName : recordData.columns()) {
                    if (first) {
                        first = false;
                    } else {
                        builder.append(" AND ");
                    }
                    if (recordData.hasValue(colName)) {
                        builder.append(ShellMysqlUtil.wrap(colName, this.dialect()))
                                .append(" = ?");
                    } else {
                        builder.append(ShellMysqlUtil.wrap(colName, this.dialect()))
                                .append(" IS NULL");
                    }
                }
                builder.append(" LIMIT 1");
                String sql = builder.toString();
                ShellMysqlUtil.printSql(sql);
                PreparedStatement statement = connection.prepareStatement(sql);
                int index = 1;
                // 设置参数
                for (String colName : recordData.notNullColumns()) {
                    ShellMysqlUtil.setVal(statement, recordData.value(colName), index++);
                }
                updateCount = ShellMysqlUtil.executeUpdate(statement);
            } else {
                MysqlRecordPrimaryKey primaryKey = param.getPrimaryKey();
                builder.append(ShellMysqlUtil.wrap(primaryKey.getColumnName(), this.dialect()))
                        .append(" = ?");
                String sql = builder.toString();
                ShellMysqlUtil.printSql(sql);
                PreparedStatement statement = connection.prepareStatement(sql);
                ShellMysqlUtil.setVal(statement, primaryKey.originalData(), 1);
                updateCount = ShellMysqlUtil.executeUpdate(statement);
                ShellMysqlUtil.close(statement);
            }
            return updateCount;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public int updateRecord(MysqlUpdateRecordParam param) {
        try {
            int updateCount;
            String dbName = param.getDbName();
            // String schema = param.getSchema();
            String tableName = param.getTableName();
            MysqlRecordData recordData = param.getUpdateRecord();
            StringBuilder builder = new StringBuilder();
            builder.append("UPDATE ")
                    .append(ShellMysqlUtil.wrap(dbName, tableName, this.dialect()))
                    .append(" SET ");
            for (String column : recordData.columns()) {
                if (recordData.isTypeGeometry(column)) {
                    builder.append(ShellMysqlUtil.wrap(column, this.dialect())).append(" = ST_GeomFromText(?),");
                } else {
                    builder.append(ShellMysqlUtil.wrap(column, this.dialect())).append(" = ?,");
                }
            }
            builder.deleteCharAt(builder.length() - 1);
            builder.append(" WHERE ");
            Connection connection = this.connManager.connection(dbName);
            if (param.getPrimaryKey() == null) {
                MysqlRecordData originalRecordData = param.getRecord();
                // 参数
                boolean first = true;
                for (String column : originalRecordData.columns()) {
                    if (first) {
                        first = false;
                    } else {
                        builder.append(" AND ");
                    }
                    builder.append(ShellMysqlUtil.wrap(column, this.dialect())).append(" = ?");
                }
                int index = 1;
                String sql = builder.toString();
                ShellMysqlUtil.printSql(sql);
                PreparedStatement statement = connection.prepareStatement(sql);
                // 设置值
                for (String colName : recordData.columns()) {
                    ShellMysqlUtil.setVal(statement, recordData.value(colName), index++);
                }
                // 设置参数
                for (String colName : originalRecordData.columns()) {
                    ShellMysqlUtil.setVal(statement, originalRecordData.value(colName), index++);
                }
                builder.append(" LIMIT 1");
                updateCount = ShellMysqlUtil.executeUpdate(statement);
                ShellMysqlUtil.close(statement);
            } else {
                MysqlRecordPrimaryKey primaryKey = param.getPrimaryKey();
                builder.append(ShellMysqlUtil.wrap(primaryKey.getColumnName(), this.dialect())).append(" = ?");
                String sql = builder.toString();
                ShellMysqlUtil.printInfo(sql, recordData);
                PreparedStatement statement = connection.prepareStatement(sql);
                int index = 1;
                // 设置值
                for (String colName : recordData.columns()) {
                    ShellMysqlUtil.setVal(statement, recordData.value(colName), index++);
                }
                // 设置参数
                ShellMysqlUtil.setVal(statement, primaryKey.originalData(), index);
                updateCount = ShellMysqlUtil.executeUpdate(statement);
                ShellMysqlUtil.close(statement);
            }
            return updateCount;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public String showCreateTable(String dbName, String tableName) {
        try {
            Connection connection = this.connManager.connection(dbName);
            String sql = "SHOW CREATE TABLE " + ShellMysqlUtil.wrap(tableName, this.dialect());
            ShellMysqlUtil.printSql(sql);
            Statement stmt = connection.createStatement();
            ResultSet resultSet = stmt.executeQuery(sql);
            String createDefinition = "";
            if (resultSet.next()) {
                createDefinition = resultSet.getString(2);
            }
            ShellMysqlUtil.close(resultSet);
            ShellMysqlUtil.close(stmt);
            return createDefinition;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public String showCreateView(String dbName, String viewName) {
        try {
            Connection connection = this.connManager.connection(dbName);
            String sql = "SHOW CREATE VIEW " + ShellMysqlUtil.wrap(viewName, this.dialect());
            ShellMysqlUtil.printSql(sql);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            String createDefinition = "";
            if (resultSet.next()) {
                createDefinition = resultSet.getString("Create View");
            }
            ShellMysqlUtil.close(resultSet);
            ShellMysqlUtil.close(statement);
            return createDefinition;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public String showCreateFunction(String dbName, String functionName) {
        try {
            Connection connection = this.connManager.connection(dbName);
            String sql = "SHOW CREATE FUNCTION " + ShellMysqlUtil.wrap(functionName, this.dialect());
            ShellMysqlUtil.printSql(sql);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            String createDefinition = "";
            if (resultSet.next()) {
                createDefinition = resultSet.getString("Create Function");
            }
            ShellMysqlUtil.close(resultSet);
            ShellMysqlUtil.close(statement);
            return createDefinition;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public String showCreateProcedure(String dbName, String procedureName) {
        try {
            Connection connection = this.connManager.connection(dbName);
            String sql = "SHOW CREATE PROCEDURE " + ShellMysqlUtil.wrap(procedureName, this.dialect());
            ShellMysqlUtil.printSql(sql);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            String createDefinition = "";
            if (resultSet.next()) {
                createDefinition = resultSet.getString("Create Procedure");
            }
            ShellMysqlUtil.close(resultSet);
            ShellMysqlUtil.close(statement);
            return createDefinition;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public String showCreateTrigger(String dbName, String triggerName) {
        try {
            Connection connection = this.connManager.connection(dbName);
            String sql = "SHOW CREATE TRIGGER " + ShellMysqlUtil.wrap(triggerName, this.dialect());
            ShellMysqlUtil.printSql(sql);
            Statement statement = connection.createStatement();
            // 执行SQL查询并获取结果集
            ResultSet resultSet = statement.executeQuery(sql);
            String createDefinition = "";
            if (resultSet.next()) {
                createDefinition = resultSet.getString("Sql Original Statement");
            }
            ShellMysqlUtil.close(resultSet);
            ShellMysqlUtil.close(statement);
            return createDefinition;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public String showCreateEvent(String dbName, String eventName) {
        try {
            Connection connection = this.connManager.connection(dbName);
            String sql = "SHOW CREATE EVENT " + ShellMysqlUtil.wrap(eventName, this.dialect());
            ShellMysqlUtil.printSql(sql);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            String createDefinition = "";
            if (resultSet.next()) {
                createDefinition = resultSet.getString("Create Event");
            }
            ShellMysqlUtil.close(resultSet);
            ShellMysqlUtil.close(statement);
            return createDefinition;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public List<String> engines() {
        if (this.hasProperty("engines")) {
            return this.getProperty("engines");
        }
        try {
            List<String> engines = new ArrayList<>();
            String sql = """
                    SELECT 
                        ENGINE 
                    FROM 
                        information_schema.ENGINES 
                    WHERE 
                        SUPPORT = 'YES' 
                    OR 
                        SUPPORT = 'DEFAULT'
                    """;
            ShellMysqlUtil.printSql(sql);
            Statement statement = this.connManager.connection().createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            ShellMysqlUtil.printMetaData(resultSet);
            while (resultSet.next()) {
                engines.add(resultSet.getString(1));
            }
            ShellMysqlUtil.close(statement);
            this.putProperty("engines", engines);
            return engines;
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    public List<MysqlDatabase> databases() {
        try {
            Statement statement = this.connManager.connection().createStatement();
            ResultSet resultSet = statement.executeQuery("SHOW DATABASES");
            List<MysqlDatabase> list = new ArrayList<>();
            while (resultSet.next()) {
                MysqlDatabase databases = new MysqlDatabase();
                String dbName = resultSet.getString(1);
                databases.setName(dbName);
                databases.setCharsetAndCollation(this.databaseCollation(dbName));
                list.add(databases);
            }
            ShellMysqlUtil.close(resultSet);
            ShellMysqlUtil.close(statement);
            return list;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public MysqlDatabase database(String dbName) {
        try {
            MysqlDatabase database = new MysqlDatabase();
            database.setName(dbName);
            database.setCharsetAndCollation(this.databaseCollation(dbName));
            return database;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public MysqlTable selectTable(String dbName, String tableName) {
        MysqlSelectTableParam param = new MysqlSelectTableParam();
        param.setDbName(dbName);
        param.setTableName(tableName);
        return this.selectTable(param);
    }

    public MysqlTable selectTable(MysqlSelectTableParam param) {
        try {
            String dbName = param.getDbName();
            String tableName = param.getTableName();
            MysqlTable table = new MysqlTable();
            table.setDbName(dbName);
            table.setName(tableName);
            Connection connection = this.connManager.connection(dbName);
            if (param.isFull()) {
                String sql = """
                        SELECT 
                            `AUTO_INCREMENT`, `ROW_FORMAT`, `TABLE_COLLATION`, `TABLE_COMMENT`, `ENGINE` 
                        FROM 
                            information_schema.TABLES 
                        WHERE 
                            `TABLE_SCHEMA` = ? 
                        AND 
                            `TABLE_NAME` = ?  
                        AND 
                            `TABLE_TYPE` != 'VIEW'
                        """;
                ShellMysqlUtil.printSql(sql);
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setString(1, dbName);
                statement.setString(2, tableName);
                ResultSet resultSet = statement.executeQuery();
                ShellMysqlUtil.printMetaData(resultSet);
                String showCreateTable = this.showCreateTable(dbName, tableName);
                while (resultSet.next()) {
                    String tableEngine = resultSet.getString("ENGINE");
                    String rowFormat = resultSet.getString("ROW_FORMAT");
                    Long autoIncrement = resultSet.getLong("AUTO_INCREMENT");
                    String tableComment = resultSet.getString("TABLE_COMMENT");
                    String tableCollation = resultSet.getString("TABLE_COLLATION");
                    table.setEngine(tableEngine);
                    table.setRowFormat(rowFormat);
                    table.setComment(tableComment);
                    table.setAutoIncrement(autoIncrement);
                    table.setCreateDefinition(showCreateTable);
                    table.setCharsetAndCollation(tableCollation);
                }
                ShellMysqlUtil.close(resultSet);
                ShellMysqlUtil.close(statement);
            } else {
                DatabaseMetaData metaData = connection.getMetaData();
                ResultSet resultSet = metaData.getTables(null, null, tableName, TABLE_TYPES);
                while (resultSet.next()) {
                    if (ShellMysqlUtil.checkTableType(resultSet, dbName)) {
                        String remarks = resultSet.getString("REMARKS");
                        table.setComment(remarks);
                    }
                }
                ShellMysqlUtil.close(resultSet);
            }
            return table;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public MysqlTable selectFullTable(MysqlSelectTableParam param) {
        try {
            String dbName = param.getDbName();
            String tableName = param.getTableName();
            MysqlTable table = new MysqlTable();
            table.setDbName(dbName);
            table.setName(tableName);
            Connection connection = this.connManager.connection(dbName);
            String sql = """
                    SELECT 
                        `AUTO_INCREMENT`, `ROW_FORMAT`, `TABLE_COLLATION`, `TABLE_COMMENT`, `ENGINE` 
                    FROM 
                        information_schema.TABLES 
                    WHERE 
                        `TABLE_SCHEMA` = ? 
                    AND
                        `TABLE_NAME` = ?  
                    AND 
                        `TABLE_TYPE` != 'VIEW'
                    """;
            ShellMysqlUtil.printSql(sql);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, dbName);
            statement.setString(2, tableName);
            ResultSet resultSet = statement.executeQuery();
            ShellMysqlUtil.printMetaData(resultSet);
            String showCreateTable = this.showCreateTable(dbName, tableName);
            while (resultSet.next()) {
                String tableEngine = resultSet.getString("ENGINE");
                String rowFormat = resultSet.getString("ROW_FORMAT");
                Long autoIncrement = resultSet.getLong("AUTO_INCREMENT");
                String tableComment = resultSet.getString("TABLE_COMMENT");
                String tableCollation = resultSet.getString("TABLE_COLLATION");
                table.setEngine(tableEngine);
                table.setRowFormat(rowFormat);
                table.setComment(tableComment);
                table.setAutoIncrement(autoIncrement);
                table.setCreateDefinition(showCreateTable);
                table.setCharsetAndCollation(tableCollation);
            }
            ShellMysqlUtil.close(resultSet);
            ShellMysqlUtil.close(statement);
            return table;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public MysqlView view(String dbName, String viewName) {
        try {
            String sql = """
                    SELECT 
                        `TABLE_NAME`, `TABLE_COMMENT` 
                    FROM 
                        information_schema.`TABLES` 
                    WHERE
                        `TABLE_SCHEMA` = ? 
                    AND 
                        `TABLE_NAME` = ?
                    AND 
                        `TABLE_TYPE` = 'VIEW'
                    """;
            ShellMysqlUtil.printSql(sql);
            Connection connection = this.connManager.connection();
            PreparedStatement statement = this.connManager.connection().prepareStatement(sql);
            statement.setString(1, dbName);
            statement.setString(2, viewName);
            // 执行SQL查询并获取结果集
            ResultSet resultSet = statement.executeQuery();
            // 打印元数据
            ShellMysqlUtil.printMetaData(resultSet);
            // 遍历结果集
            MysqlView view = new MysqlView();
            while (resultSet.next()) {
                String tableName = resultSet.getString("TABLE_NAME");
                String tableComment = resultSet.getString("TABLE_COMMENT");
                Map<String, String> info = ShellMysqlHelper.getViewInfo(connection, dbName, tableName);
                view.setDbName(dbName);
                view.setName(tableName);
                view.setComment(tableComment);
                view.setDefiner(info.get("DEFINER"));
                view.setAlgorithm(info.get("ALGORITHM"));
                view.setDefinition(info.get("DEFINITION"));
                view.setCheckOption(info.get("CHECK_OPTION"));
                view.setSecurityType(info.get("SECURITY_TYPE"));
                view.setUpdatable(StringUtil.equalsIgnoreCase("YES", info.get("UPDATABLE")));
            }
            // 关闭连接和释放资源
            ShellMysqlUtil.close(resultSet);
            ShellMysqlUtil.close(statement);
            return view;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public List<MysqlView> views(String dbName) {
        try {
            List<MysqlView> list = new ArrayList<>();
            String sql = """
                    SELECT 
                        `TABLE_NAME`, `TABLE_COMMENT` 
                    FROM 
                        information_schema.`TABLES` 
                    WHERE 
                        `TABLE_SCHEMA` = ? 
                    AND 
                        `TABLE_TYPE` = 'VIEW'
                    """;
            ShellMysqlUtil.printSql(sql);
            Connection connection = this.connManager.connection();
            PreparedStatement statement = this.connManager.connection().prepareStatement(sql);
            statement.setString(1, dbName);
            // 执行SQL查询并获取结果集
            ResultSet resultSet = statement.executeQuery();
            // 打印元数据
            ShellMysqlUtil.printMetaData(resultSet);
            // 遍历结果集
            while (resultSet.next()) {
                MysqlView view = new MysqlView();
                String tableName = resultSet.getString("TABLE_NAME");
                String tableComment = resultSet.getString("TABLE_COMMENT");
                Map<String, String> info = ShellMysqlHelper.getViewInfo(connection, dbName, tableName);
                view.setDbName(dbName);
                view.setName(tableName);
                view.setComment(tableComment);
                view.setDefiner(info.get("DEFINER"));
                view.setAlgorithm(info.get("ALGORITHM"));
                view.setDefinition(info.get("DEFINITION"));
                view.setCheckOption(info.get("CHECK_OPTION"));
                view.setSecurityType(info.get("SECURITY_TYPE"));
                view.setUpdatable(StringUtil.equalsIgnoreCase("YES", info.get("UPDATABLE")));
                list.add(view);
            }
            // 关闭连接和释放资源
            ShellMysqlUtil.close(resultSet);
            ShellMysqlUtil.close(statement);
            return list;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public void dropView(String dbName, MysqlView view) {
        try {
            String sql = "DROP VIEW IF EXISTS " + ShellMysqlUtil.wrap(view.getDbName(), view.getName(), this.dialect());
            Statement statement = this.connManager.connection(dbName).createStatement();
            ShellMysqlUtil.printSql(sql);
            statement.executeUpdate(sql);
            ShellMysqlUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public boolean existView(String dbName, String viewName) {
        boolean result;
        try {
            DatabaseMetaData metaData = this.connManager.connection(dbName).getMetaData();
            ResultSet resultSet = metaData.getTables(null, dbName, viewName, new String[]{"VIEW"});
            result = resultSet.next();
            ShellMysqlUtil.close(resultSet);
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
        return result;
    }

    public void createView(String dbName, MysqlView view) {
        try {
            Statement statement = this.connManager.connection(dbName).createStatement();
            String sql = "CREATE ";
            if (StringUtil.isNotBlank(view.getAlgorithm())) {
                sql += " ALGORITHM = " + view.getAlgorithm();
            }
            if (StringUtil.isNotBlank(view.getDefiner())) {
                sql += " DEFINER = " + view.getDefiner();
            }
            if (StringUtil.isNotBlank(view.getSecurityType())) {
                sql += " SQL SECURITY " + view.getSecurityType();
            }
            sql = sql + " VIEW " + ShellMysqlUtil.wrap(dbName, view.getName(), this.dialect()) + " AS " + view.getDefinition();
            if (view.hasCheckOption()) {
                sql += " WITH " + view.getCheckOption() + " CHECK OPTION";
            }
            ShellMysqlUtil.printSql(sql);
            statement.execute(sql);
            ShellMysqlUtil.close(statement);
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    public void alertView(String dbName, MysqlView view) {
        try {
            Statement statement = this.connManager.connection(dbName).createStatement();
            String sql = "CREATE OR REPLACE ";
            if (StringUtil.isNotBlank(view.getAlgorithm())) {
                sql += " ALGORITHM = " + view.getAlgorithm();
            }
            if (StringUtil.isNotBlank(view.getDefiner())) {
                sql += " DEFINER = " + view.getDefiner();
            }
            if (StringUtil.isNotBlank(view.getSecurityType())) {
                sql += " SQL SECURITY " + view.getSecurityType();
            }
            sql = sql + " VIEW " + ShellMysqlUtil.wrap(dbName, view.getName(), this.dialect()) + " AS " + view.getDefinition();
            if (view.hasCheckOption()) {
                sql += " WITH " + view.getCheckOption() + " CHECK OPTION";
            }
            ShellMysqlUtil.printSql(sql);
            statement.execute(sql);
            ShellMysqlUtil.close(statement);
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    public MysqlIndexes indexes(String dbName, String tableName) {
        try {
            Connection connection = this.connManager.connection();
            Statement statement = connection.createStatement();
            String sql = "SHOW INDEX FROM " + ShellMysqlUtil.wrap(dbName, tableName, this.dialect());
            ShellMysqlUtil.printSql(sql);
            ResultSet resultSet = statement.executeQuery(sql);
            // 打印元数据
            ShellMysqlUtil.printMetaData(resultSet);
            Map<String, MysqlIndex> indexMap = new HashMap<>();
            while (resultSet.next()) {
                String keyName = resultSet.getString("Key_name");
                // 主键类型的跳过
                if ("Primary".equalsIgnoreCase(keyName)) {
                    continue;
                }
                MysqlIndex tableIndex = indexMap.get(keyName);
                String columnName = resultSet.getString("Column_name");
                if (tableIndex == null) {
                    int noneUnique = resultSet.getInt("Non_unique");
                    int seqInIndex = resultSet.getInt("Seq_in_index");
                    String indexType = resultSet.getString("Index_type");
                    String indexComment = resultSet.getString("Index_comment");
                    tableIndex = new MysqlIndex();
                    tableIndex.setName(keyName);
                    tableIndex.setSeqIndex(seqInIndex);
                    tableIndex.setComment(indexComment);
                    tableIndex.type(indexType, noneUnique);
                    indexMap.put(keyName, tableIndex);
                }
                int subPart = resultSet.getInt("Sub_Part");
                tableIndex.addColumn(columnName, subPart);
            }
            ShellMysqlUtil.close(resultSet);
            ShellMysqlUtil.close(statement);
            return new MysqlIndexes(indexMap.values());
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    public MysqlChecks checks(String dbName, String tableName) {
        if (!this.isSupportCheckFeature()) {
            return null;
        }
        try {
            // String sql = """
            //         SELECT
            //             CHECK_CLAUSE AS 'CLAUSE',
            //             CONSTRAINT_NAME AS 'NAME',
            //             TABLE_NAME AS 'TABLE_NAME',
            //             CONSTRAINT_SCHEMA AS 'DB_NAME'
            //         FROM
            //             information_schema.CHECK_CONSTRAINTS
            //         WHERE
            //             CONSTRAINT_SCHEMA = ?
            //         AND
            //             TABLE_NAME = ?;
            //         """;
            String sql = """
                        SELECT
                            tc.CONSTRAINT_SCHEMA AS 'DB_NAME',
                            tc.CONSTRAINT_NAME AS 'NAME',
                            tc.TABLE_NAME AS 'TABLE_NAME',
                            cc.CHECK_CLAUSE as 'CLAUSE'
                        FROM
                            INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc
                        LEFT JOIN
                            INFORMATION_SCHEMA.CHECK_CONSTRAINTS cc
                        ON
                            tc.CONSTRAINT_SCHEMA = cc.CONSTRAINT_SCHEMA
                        AND 
                            tc.CONSTRAINT_NAME = cc.CONSTRAINT_NAME
                        WHERE 
                            tc.CONSTRAINT_TYPE = 'CHECK'
                        AND 
                            tc.CONSTRAINT_SCHEMA = ?
                        AND 
                            tc.TABLE_NAME = ?;
                    """;
            ShellMysqlUtil.printSql(sql);
            PreparedStatement statement = this.connManager.connection().prepareStatement(sql);
            statement.setString(1, dbName);
            statement.setString(2, tableName);
            ResultSet resultSet = statement.executeQuery();
            ShellMysqlUtil.printMetaData(resultSet);
            MysqlChecks checks = new MysqlChecks();
            while (resultSet.next()) {
                MysqlCheck check = new MysqlCheck();
                String name = resultSet.getString("NAME");
                String clause = resultSet.getString("CLAUSE");
                check.setName(name);
                check.setClause(clause);
                check.setDbName(dbName);
                check.setTableName(tableName);
                checks.add(check);
            }
            ShellMysqlUtil.close(resultSet);
            return checks;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public MysqlForeignKeys foreignKeys(String dbName, String tableName) {
        try {
            // 查询外键
            String sql = """
                    SELECT
                        a.COLUMN_NAME AS 'FKCOLUMN_NAME',
                        a.REFERENCED_TABLE_SCHEMA AS 'PKTABLE_CAT',
                        a.REFERENCED_TABLE_NAME AS 'PKTABLE_NAME',
                        a.REFERENCED_COLUMN_NAME AS 'PKCOLUMN_NAME',
                        a.CONSTRAINT_NAME AS 'FK_NAME',
                        a1.UPDATE_RULE,
                        a1.DELETE_RULE 
                    FROM
                        information_schema.KEY_COLUMN_USAGE a
                    JOIN 
                        information_schema.REFERENTIAL_CONSTRAINTS a1 
                    ON 
                        a.CONSTRAINT_NAME = a1.CONSTRAINT_NAME 
                    WHERE
                        a.REFERENCED_TABLE_SCHEMA = ?
                    AND 
                        a.TABLE_NAME = ? 
                    AND 
                        a.REFERENCED_TABLE_NAME IS NOT NULL;
                    """;
            ShellMysqlUtil.printSql(sql);
            PreparedStatement statement = this.connManager.connection().prepareStatement(sql);
            statement.setString(1, dbName);
            statement.setString(2, tableName);
            ResultSet resultSet = statement.executeQuery();
            ShellMysqlUtil.printMetaData(resultSet);
            Map<String, MysqlForeignKey> foreignKeyMap = new HashMap<>();
            while (resultSet.next()) {
                String fkName = resultSet.getString("FK_NAME");
                String fkColumnName = resultSet.getString("FKCOLUMN_NAME");
                String pkColumnName = resultSet.getString("PKCOLUMN_NAME");
                MysqlForeignKey foreignKey = foreignKeyMap.get(fkName);
                if (foreignKey == null) {
                    String pkTableName = resultSet.getString("PKTABLE_NAME");
                    String pkTableCat = resultSet.getString("PKTABLE_CAT");
                    String updateRule = resultSet.getString("UPDATE_RULE");
                    String deleteRule = resultSet.getString("DELETE_RULE");
                    foreignKey = new MysqlForeignKey();
                    foreignKey.setName(fkName);
                    foreignKey.setUpdatePolicy(updateRule == null ? null : updateRule.toUpperCase());
                    foreignKey.setDeletePolicy(deleteRule == null ? null : deleteRule.toUpperCase());
                    foreignKey.setPrimaryKeyTable(pkTableName);
                    foreignKey.setPrimaryKeyDatabase(pkTableCat);
                    foreignKeyMap.put(fkName, foreignKey);
                }
                foreignKey.addColumn(fkColumnName);
                foreignKey.addPrimaryKeyColumn(pkColumnName);
            }
            ShellMysqlUtil.close(resultSet);
            return new MysqlForeignKeys(foreignKeyMap.values());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public List<MysqlColumn> viewColumns(String dbName, String viewName) {
        try {
            if (StringUtil.isBlank(viewName)) {
                return Collections.emptyList();
            }
            String sql = """
                    SELECT
                        a.EXTRA as COLUMN_EXTRA,
                        a.COLUMN_KEY as COLUMN_KEY,
                        a.COLUMN_COMMENT as REMARKS,
                        a.COLUMN_TYPE as COLUMN_TYPE,
                        a.COLUMN_NAME as COLUMN_NAME,
                        a.IS_NULLABLE as IS_NULLABLE,
                        a.COLUMN_DEFAULT as COLUMN_DEF,
                        a.COLLATION_NAME as COLLATION_NAME,
                        a.CHARACTER_SET_NAME as CHARSET_NAME,
                        a.ORDINAL_POSITION as ORDINAL_POSITION
                    FROM
                        INFORMATION_SCHEMA.`COLUMNS` a
                    WHERE
                        a.TABLE_SCHEMA = ?
                    AND
                        a.TABLE_NAME = ?
                    """;
            ShellMysqlUtil.printSql(sql);
            PreparedStatement statement = this.connManager.connection().prepareStatement(sql);
            statement.setString(1, dbName);
            statement.setString(2, viewName);
            ResultSet resultSet = statement.executeQuery();
            // 打印元数据
            ShellMysqlUtil.printMetaData(resultSet);
            Map<String, MysqlColumn> columns = new HashMap<>();
            while (resultSet.next()) {
                Object def = resultSet.getObject("COLUMN_DEF");
                String remarks = resultSet.getString("REMARKS");
                int position = resultSet.getInt("ORDINAL_POSITION");
                String nullable = resultSet.getString("IS_NULLABLE");
                String columnKey = resultSet.getString("COLUMN_KEY");
                String columnType = resultSet.getString("COLUMN_TYPE");
                String columnName = resultSet.getString("COLUMN_NAME");
                String charsetName = resultSet.getString("CHARSET_NAME");
                String columnExtra = resultSet.getString("COLUMN_EXTRA");
                String collationName = resultSet.getString("COLLATION_NAME");
                MysqlColumn column = new MysqlColumn();
                column.initColumn(columnType, columnExtra);
                column.setDbName(dbName);
                column.setName(columnName);
                column.setComment(remarks);
                column.setDefaultValue(def);
                column.setPosition(position);
                column.setCharset(charsetName);
                column.setTableName(viewName);
                column.setCollation(collationName);
                column.setNullable("yes".equalsIgnoreCase(nullable));
                // column.setPrimaryKey("pri".equalsIgnoreCase(columnKey));
                columns.put(columnName, column);
            }
            ShellMysqlUtil.close(resultSet);
            ShellMysqlUtil.close(statement);

            sql = "SELECT * FROM " + ShellMysqlUtil.wrap(dbName, viewName, this.dialect()) + " LIMIT 1";
            ShellMysqlUtil.printSql(sql);
            PreparedStatement statement1 = this.connManager.connection().prepareStatement(sql);
            ResultSet resultSet1 = statement1.executeQuery();
            ShellMysqlUtil.printMetaData(resultSet1);
            MysqlColumns dbColumns = ShellMysqlHelper.parseColumns(resultSet1);
            ShellMysqlUtil.close(resultSet1);
            ShellMysqlUtil.close(statement1);

            // 初始化状态
            for (MysqlColumn value : columns.values()) {
                MysqlColumn dbColumn = dbColumns.column(value.getName());
                if (dbColumn != null) {
                    value.setNullable(dbColumn.isNullable());
                    value.setAutoIncrement(dbColumn.isAutoIncrement());
                }
                value.initStatus();
            }
            // 返回排序后的数据
            return CollectionUtil.sort(columns.values(), Comparator.comparingInt(MysqlColumn::getPosition));
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public List<MysqlRecord> viewRecords(String dbName, String viewName, Long start, Long limit, List<MysqlRecordFilter> filters) {
        try {
            Connection connection = this.connManager.connection(dbName);
            StringBuilder builder = new StringBuilder("SELECT * FROM ");
            builder.append(ShellMysqlUtil.wrap(dbName, viewName, this.dialect()));
            String filterCondition = MysqlConditionUtil.buildCondition(filters);
            if (StringUtil.isNotBlank(filterCondition)) {
                builder.append(" WHERE ").append(filterCondition);
            }
            if (start != null && limit != null) {
                builder.append(" LIMIT ").append(start).append(",").append(limit);
            }
            String sql = builder.toString();
            ShellMysqlUtil.printSql(sql);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            ShellMysqlUtil.printMetaData(resultSet);
            List<MysqlRecord> records = new ArrayList<>();
            boolean updatable = ShellMysqlHelper.isViewUpdatable(connection, dbName, viewName);
            MysqlColumns columns = ShellMysqlHelper.parseColumns(resultSet);
            while (resultSet.next()) {
                MysqlRecord record = new MysqlRecord(columns, !updatable);
                for (MysqlColumn column : columns) {
                    Object data = resultSet.getObject(column.getName());
                    // 获取几何值
                    if (column.supportGeometry()) {
                        data = ShellMysqlHelper.getGeometryString(connection, data);
                    }
                    record.putValue(column, data);
                }
                records.add(record);
            }
            ShellMysqlUtil.close(resultSet);
            ShellMysqlUtil.close(statement);
            return records;
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    public void createTable(MysqlCreateTableParam param) {
        Connection connection = null;
        try {
            String dbName = param.dbName();
            connection = this.connManager.connection(dbName);
            Statement statement = connection.createStatement();
            String sql = MysqlTableCreateSqlGenerator.generateSql(param);
            ShellMysqlUtil.printSql(sql);
            List<String> sqlList = DBSqlParser.parseSql(sql, this.dialect());
            connection.setAutoCommit(false);
            for (String sqlStr : sqlList) {
                statement.executeUpdate(sqlStr);
            }
            connection.commit();
            ShellMysqlUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            ShellMysqlUtil.rollback(connection);
            throw new ShellException(ex);
        }
    }

    public void alertTable(MysqlAlertTableParam param) {
        Connection connection = null;
        try {
            String sql = MysqlTableAlertSqlGenerator.generateSql(param);
            // 无变化
            if (StringUtil.isBlank(sql)) {
                return;
            }
            String dbName = param.getTable().getDbName();
            connection = this.connManager.connection(dbName);
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();
            ShellMysqlUtil.printSql(sql);
            List<String> sqlList = DBSqlParser.parseSql(sql, this.dialect());
            for (String sqlStr : sqlList) {
                statement.executeUpdate(sqlStr);
            }
            connection.commit();
            ShellMysqlUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            ShellMysqlUtil.rollback(connection);
            throw new ShellException(ex);
        }
    }

    @Deprecated
    public boolean existTable(String dbName, String tableName) {
        boolean result;
        try {
            DatabaseMetaData metaData = this.connManager.connection(dbName).getMetaData();
            ResultSet resultSet = metaData.getTables(null, dbName, tableName, TABLE_TYPES);
            ShellMysqlUtil.printMetaData(resultSet);
            result = resultSet.next();
            ShellMysqlUtil.close(resultSet);
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
        return result;
    }

    public void renameTable(String dbName, String oldTableName, String newTableName) {
        try {
            StringBuilder builder = new StringBuilder("RENAME TABLE ");
            builder.append(ShellMysqlUtil.wrap(dbName, oldTableName, this.dialect()))
                    .append(" TO ")
                    .append(ShellMysqlUtil.wrap(dbName, newTableName, this.dialect()));
            String sql = builder.toString();
            Connection connection = this.connManager.connection(dbName);
            Statement statement = connection.createStatement();
            ShellMysqlUtil.printSql(sql);
            statement.execute(sql);
            ShellMysqlUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 重命名事件
     *
     * @param dbName       库名称
     * @param oldEventName 事件名称
     * @param newEventName 新事件名称
     */
    public void renameEvent(String dbName, String oldEventName, String newEventName) {
        try {
            StringBuilder builder = new StringBuilder("ALTER EVENT ");
            builder.append(ShellMysqlUtil.wrap(dbName, oldEventName, this.dialect()))
                    .append(" RENAME TO ")
                    .append(ShellMysqlUtil.wrap(dbName, newEventName, this.dialect()));
            String sql = builder.toString();
            Connection connection = this.connManager.connection(dbName);
            Statement statement = connection.createStatement();
            ShellMysqlUtil.printSql(sql);
            statement.execute(sql);
            ShellMysqlUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public void clearTable(String dbName, String tableName) {
        try {
            Statement statement = this.connManager.connection(dbName).createStatement();
            String sql = "DELETE FROM " + ShellMysqlUtil.wrap(dbName, tableName, this.dialect());
            ShellMysqlUtil.printSql(sql);
            statement.executeUpdate(sql);
            ShellMysqlUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public void truncateTable(String dbName, String tableName) {
        try {
            Statement statement = this.connManager.connection(dbName).createStatement();
            String sql = "TRUNCATE TABLE " + ShellMysqlUtil.wrap(dbName, tableName, this.dialect());
            ShellMysqlUtil.printSql(sql);
            statement.executeUpdate(sql);
            ShellMysqlUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public void dropTable(String dbName, String tableName) {
        try {
            Statement statement = this.connManager.connection(dbName).createStatement();
            String sql = "DROP TABLE " + ShellMysqlUtil.wrap(dbName, tableName, this.dialect());
            ShellMysqlUtil.printSql(sql);
            statement.executeUpdate(sql);
            ShellMysqlUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public List<String> charsets() {
        if (this.hasProperty("charsets")) {
            return this.getProperty("charsets");
        }
        try {
            List<String> charsets = new ArrayList<>();
            Statement statement = this.connManager.connection().createStatement();
            String sql = """
                    SELECT 
                        CHARACTER_SET_NAME 
                    FROM 
                        INFORMATION_SCHEMA.CHARACTER_SETS;
                    """;
            ShellMysqlUtil.printSql(sql);
            ResultSet resultSet = statement.executeQuery(sql);
            ShellMysqlUtil.printMetaData(resultSet);
            while (resultSet.next()) {
                charsets.add(resultSet.getString(1));
            }
            ShellMysqlUtil.close(resultSet);
            ShellMysqlUtil.close(statement);
            this.putProperty("charsets", charsets);
            return charsets;
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    public List<String> collation(String charset) {
        try {
            Map<String, List<String>> collations = this.getProperty("collation");
            if (collations == null) {
                collations = new HashMap<>();
                this.putProperty("collations", collations);
            }
            charset = charset.toUpperCase();
            if (collations.containsKey(charset)) {
                return collations.get(charset.toUpperCase());
            }
            String sql = """
                    SELECT 
                        COLLATION_NAME 
                    FROM 
                        INFORMATION_SCHEMA.COLLATIONS 
                    WHERE 
                        CHARACTER_SET_NAME = ?;
                    """;
            ShellMysqlUtil.printSql(sql);
            PreparedStatement statement = this.connManager.connection().prepareStatement(sql);
            statement.setString(1, charset);
            ResultSet resultSet = statement.executeQuery();
            ShellMysqlUtil.printMetaData(resultSet);
            List<String> list = new ArrayList<>();
            while (resultSet.next()) {
                list.add(resultSet.getString(1));
            }
            ShellMysqlUtil.close(resultSet);
            ShellMysqlUtil.close(statement);
            collations.put(charset, list);
            return list;
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    public boolean existDatabase(String dbName) {
        boolean result = false;
        try {
            DatabaseMetaData metaData = this.connManager.connection().getMetaData();
            // 执行查询操作，检查数据库是否存在
            ResultSet resultSet = metaData.getCatalogs();
            while (resultSet.next()) {
                String catalogName = resultSet.getString("TABLE_CAT");
                if (catalogName.equals(dbName)) {
                    result = true;
                    break;
                }
            }
            ShellMysqlUtil.close(resultSet);
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
        return result;
    }

    public void createDatabase(MysqlDatabase database) {
        try {
            StringBuilder builder = new StringBuilder("CREATE DATABASE ");
            builder.append(ShellMysqlUtil.wrap(database.getName(), this.dialect()));
            if (StringUtil.isNotBlank(database.getCharset())) {
                builder.append(" CHARACTER SET ").append(ShellMysqlUtil.wrapData(database.getCharset()));
            }
            if (StringUtil.isNotBlank(database.getCollation())) {
                builder.append(" COLLATE ").append(ShellMysqlUtil.wrapData(database.getCollation()));
            }
            String sql = builder.toString();
            ShellMysqlUtil.printSql(sql);
            Statement statement = this.connManager.connection().createStatement();
            statement.execute(sql);
            ShellMysqlUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public boolean alterDatabase(MysqlDatabase database) {
        try {
            // 无变化
            if (database.getCharset() == null && database.getCollation() == null) {
                return true;
            }
            StringBuilder builder = new StringBuilder("ALTER DATABASE ").append(ShellMysqlUtil.wrap(database.getName(), this.dialect()));
            if (database.getCharset() != null) {
                builder.append(" CHARACTER SET ").append(ShellMysqlUtil.wrapData(database.getCharset()));
            }
            if (database.getCollation() != null) {
                builder.append(" COLLATE ").append(ShellMysqlUtil.wrapData(database.getCollation()));
            }
            String sql = builder.toString();
            ShellMysqlUtil.printSql(sql);
            Statement statement = this.connManager.connection().createStatement();
            statement.execute(sql);
            ShellMysqlUtil.close(statement);
            return true;
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    public String databaseCollation(String dbName) {
        String collation = null;
        try {
            String sql = """
                    SELECT 
                        DEFAULT_COLLATION_NAME 
                    FROM 
                        information_schema.SCHEMATA 
                    WHERE 
                        SCHEMA_NAME = ?;
                    """;
            ShellMysqlUtil.printSql(sql);
            PreparedStatement statement = this.connManager.connection().prepareStatement(sql);
            statement.setString(1, dbName);
            ResultSet resultSet = statement.executeQuery();
            ShellMysqlUtil.printMetaData(resultSet);
            while (resultSet.next()) {
                collation = resultSet.getString(1);
            }
            ShellMysqlUtil.close(resultSet);
            ShellMysqlUtil.close(statement);
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
        return collation;
    }

    public boolean dropDatabase(String dbName) {
        try {
            String sql = "DROP DATABASE " + ShellMysqlUtil.wrap(dbName, this.dialect());
            ShellMysqlUtil.printSql(sql);
            Statement statement = this.connManager.connection().createStatement();
            statement.executeUpdate(sql);
            ShellMysqlUtil.close(statement);
            return true;
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    public MysqlQueryResults<MysqlExplainResult> explainSql(String dbName, String sql) {
        MysqlQueryResults<MysqlExplainResult> results = new MysqlQueryResults<>();
        Connection connection = null;
        try {
            ShellMysqlUtil.printSql(sql);
            DBSqlParser parser = DBSqlParser.getParser(sql, this.dialect());
            List<String> list = parser.parseSql();
            connection = this.connManager.connection(dbName);
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();
            for (String execSql : list) {
                MysqlExplainResult result = new MysqlExplainResult();
                try {
                    execSql = "EXPLAIN " + execSql.stripLeading();
                    result.setSql(execSql);
                    long startTime = System.nanoTime();
                    ResultSet resultSet = statement.executeQuery(execSql);
                    result.parseResult(resultSet, connection);
                    result.setUsed(System.nanoTime() - startTime);
                    ShellMysqlUtil.close(resultSet);
                    result.setSuccess(true);
                } catch (SQLException ex) {
                    result.setMsg(ex.toString());
                }
                results.addResult(result);
            }
            ShellMysqlUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            ShellMysqlUtil.rollback(connection);
            results.parseError(ex);
        }
        return results;
    }

    public MysqlExecuteResult executeSingleSql(String dbName, String sql) {
        Connection connection = null;
        MysqlExecuteResult result = new MysqlExecuteResult();
        result.setSql(sql);
        try {
            ShellMysqlUtil.printSql(sql);
            DBSqlParser parser = DBSqlParser.getParser(sql, this.dialect());
            String execSql = parser.parseSingleSql();
            connection = this.connManager.connection(dbName);
            Statement statement = connection.createStatement();
            try {
                long startTime = System.nanoTime();
                boolean isQuery = statement.execute(execSql);
                if (isQuery) {
                    ResultSet resultSet = statement.getResultSet();
                    if (parser.isSingle()) {
                        result.setFullColumn(parser.isFullColumn());
                    } else {
                        result.setFullColumn(ShellMysqlUtil.isFullColumn(execSql, this.dbType()));
                    }
                    result.parseResult(resultSet, connection, !parser.isSelect());
                    ShellMysqlUtil.close(resultSet);
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
            ShellMysqlUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            ShellMysqlUtil.rollback(connection);
        }
        return result;
    }

    public void executeSqlSimple(String dbName, String sql) {
        Connection connection = null;
        try {
            ShellMysqlUtil.printSql(sql);
            connection = this.connManager.connection(dbName);
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();
            statement.execute(sql);
            connection.commit();
            ShellMysqlUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            ShellMysqlUtil.rollback(connection);
            throw new ShellException(ex);
        }
    }

    public int insertBatch(String dbName, List<String> sqlList, boolean parallel) {
        Connection connection = null;
        int result = 0;
        try {
            connection = parallel ? this.connManager.newConnection(dbName) : this.connManager.connection(dbName);
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();
            for (String sql : sqlList) {
                ShellMysqlUtil.printSql(sql);
                statement.addBatch(sql);
            }
            int[] results = statement.executeBatch();
            connection.commit();
            ShellMysqlUtil.close(statement);
            if (parallel) {
                ShellMysqlUtil.close(connection);
            }
            for (int i : results) {
                result += i;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            ShellMysqlUtil.rollback(connection);
            throw new ShellException(ex);

        }
        return result;
    }

    public DbType dbType() {
        return DBDialect.MYSQL.dbType();
    }

    public DBDialect dialect() {
        return DBDialect.MYSQL;
    }

    public List<MysqlFunction> functions(String dbName) {
        try {
            List<MysqlFunction> list = new ArrayList<>();
            String sql = """
                    SELECT
                        `ROUTINE_NAME`,
                        `SECURITY_TYPE`,
                        `SQL_DATA_ACCESS`,
                        `ROUTINE_DEFINITION`
                    FROM
                        `INFORMATION_SCHEMA`.`ROUTINES` 
                    WHERE
                        `ROUTINE_SCHEMA` = ?
                    AND
                        `ROUTINE_TYPE` = 'FUNCTION'
                    """;
            ShellMysqlUtil.printSql(sql);
            PreparedStatement statement = this.connManager.connection().prepareStatement(sql);
            statement.setString(1, dbName);
            // 执行SQL查询并获取结果集
            ResultSet resultSet = statement.executeQuery();
            // 打印元数据
            ShellMysqlUtil.printMetaData(resultSet);
            // 遍历结果集
            while (resultSet.next()) {
                MysqlFunction function = new MysqlFunction();
                String name = resultSet.getString("ROUTINE_NAME");
                List<MysqlRoutineParam> params = ShellMysqlHelper.listFunctionParam(this.connManager.connection(), dbName, name);
                String securityType = resultSet.getString("SECURITY_TYPE");
                String definition = resultSet.getString("ROUTINE_DEFINITION");
                String sqlDataAccess = resultSet.getString("SQL_DATA_ACCESS");
                String createDefinition = ShellMysqlHelper.getFunctionDefinition(this.connManager.connection(dbName), name);
                function.setName(name);
                function.setDbName(dbName);
                function.setParams(params);
                function.setDefinition(definition);
                function.setSecurityType(securityType);
                function.setCharacteristic(sqlDataAccess);
                function.setCreateDefinition(createDefinition);
                list.add(function);
            }
            // 关闭连接和释放资源
            ShellMysqlUtil.close(resultSet);
            ShellMysqlUtil.close(statement);
            return list;
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    public List<MysqlProcedure> procedures(String dbName) {
        try {
            List<MysqlProcedure> list = new ArrayList<>();
            String sql = """
                    SELECT
                        `ROUTINE_NAME`,
                        `SECURITY_TYPE`,
                        `SQL_DATA_ACCESS`,
                        `ROUTINE_DEFINITION`
                    FROM
                        `INFORMATION_SCHEMA`.`ROUTINES` 
                    WHERE
                        `ROUTINE_SCHEMA` = ?
                    AND
                        `ROUTINE_TYPE` = 'PROCEDURE'
                    """;
            ShellMysqlUtil.printSql(sql);
            PreparedStatement statement = this.connManager.connection().prepareStatement(sql);
            statement.setString(1, dbName);
            // 执行SQL查询并获取结果集
            ResultSet resultSet = statement.executeQuery();
            // 打印元数据
            ShellMysqlUtil.printMetaData(resultSet);
            // 遍历结果集
            while (resultSet.next()) {
                MysqlProcedure procedure = new MysqlProcedure();
                String name = resultSet.getString("ROUTINE_NAME");
                String createDefinition = this.showCreateProcedure(dbName, name);
                List<MysqlRoutineParam> params = ShellMysqlHelper.listProcedureParam(this.connManager.connection(), dbName, name);
                String securityType = resultSet.getString("SECURITY_TYPE");
                String definition = resultSet.getString("ROUTINE_DEFINITION");
                String sqlDataAccess = resultSet.getString("SQL_DATA_ACCESS");
                procedure.setName(name);
                procedure.setDbName(dbName);
                procedure.setParams(params);
                procedure.setDefinition(definition);
                procedure.setSecurityType(securityType);
                procedure.setCharacteristic(sqlDataAccess);
                procedure.setCreateDefinition(createDefinition);
                list.add(procedure);
            }
            // 关闭连接和释放资源
            ShellMysqlUtil.close(resultSet);
            ShellMysqlUtil.close(statement);
            return list;
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    public MysqlProcedure selectProcedure(String dbName, String produceName) {
        try {
            String sql = """
                    SELECT
                        `SECURITY_TYPE`,
                        `SQL_DATA_ACCESS`,
                        `ROUTINE_DEFINITION`
                    FROM
                        `INFORMATION_SCHEMA`.`ROUTINES` 
                    WHERE
                        `ROUTINE_SCHEMA` = ?
                    AND   
                        `ROUTINE_NAME` = ?
                    AND
                        `ROUTINE_TYPE` = 'PROCEDURE'
                    """;
            ShellMysqlUtil.printSql(sql);
            PreparedStatement statement = this.connManager.connection().prepareStatement(sql);
            statement.setString(1, dbName);
            statement.setString(2, produceName);
            // 执行SQL查询并获取结果集
            ResultSet resultSet = statement.executeQuery();
            // 打印元数据
            ShellMysqlUtil.printMetaData(resultSet);
            MysqlProcedure procedure = new MysqlProcedure();
            procedure.setDbName(dbName);
            procedure.setName(produceName);
            // 遍历结果集
            while (resultSet.next()) {
                String createDefinition = this.showCreateProcedure(dbName, produceName);
                List<MysqlRoutineParam> params = ShellMysqlHelper.listProcedureParam(this.connManager.connection(), dbName, produceName);
                String securityType = resultSet.getString("SECURITY_TYPE");
                String definition = resultSet.getString("ROUTINE_DEFINITION");
                String sqlDataAccess = resultSet.getString("SQL_DATA_ACCESS");
                procedure.setDbName(dbName);
                procedure.setParams(params);
                procedure.setDefinition(definition);
                procedure.setSecurityType(securityType);
                procedure.setCharacteristic(sqlDataAccess);
                procedure.setCreateDefinition(createDefinition);
            }
            // 关闭连接和释放资源
            ShellMysqlUtil.close(resultSet);
            ShellMysqlUtil.close(statement);
            return procedure;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }


    public void dropProcedure(String dbName, MysqlProcedure routine) {
        try {
            String sql = "DROP PROCEDURE IF EXISTS " + ShellMysqlUtil.wrap(dbName, routine.getName(), this.dialect());
            ShellMysqlUtil.printSql(sql);
            Statement statement = this.connManager.connection(dbName).createStatement();
            statement.executeUpdate(sql);
            ShellMysqlUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public void createProcedure(String dbName, MysqlProcedure procedure) {
        try {
            String sql = MysqlProcedureSqlGenerator.INSTANCE.generate(procedure);
            ShellMysqlUtil.printSql(sql);
            Statement statement = this.connManager.connection(dbName).createStatement();
            statement.executeUpdate(sql);
            ShellMysqlUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public void alertProcedure(String dbName, MysqlProcedure procedure) {
        try {
            String sql = "DROP PROCEDURE IF EXISTS " + ShellMysqlUtil.wrap(dbName, procedure.getName(), this.dialect());
            ShellMysqlUtil.printSql(sql);
            Statement statement = this.connManager.connection(dbName).createStatement();
            statement.executeUpdate(sql);
            sql = MysqlProcedureSqlGenerator.INSTANCE.generate(procedure);
            ShellMysqlUtil.printSql(sql);
            Statement statement1 = this.connManager.connection(dbName).createStatement();
            statement1.executeUpdate(sql);
            ShellMysqlUtil.close(statement1);
            ShellMysqlUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public void dropFunction(String dbName, MysqlFunction function) {
        try {
            String sql = "DROP function IF EXISTS " + ShellMysqlUtil.wrap(dbName, function.getName(), this.dialect());
            ShellMysqlUtil.printSql(sql);
            Statement statement = this.connManager.connection(dbName).createStatement();
            statement.executeUpdate(sql);
            ShellMysqlUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public MysqlFunction selectFunction(String dbName, String functionName) {
        try {
            String sql = """
                    SELECT
                        `SECURITY_TYPE`,
                        `SQL_DATA_ACCESS`,
                        `ROUTINE_DEFINITION`
                    FROM
                        `INFORMATION_SCHEMA`.`ROUTINES` 
                    WHERE
                        `ROUTINE_SCHEMA` = ?
                    AND   
                        `ROUTINE_NAME` = ?
                    AND
                        `ROUTINE_TYPE` = 'FUNCTION'
                    """;
            ShellMysqlUtil.printSql(sql);
            PreparedStatement statement = this.connManager.connection().prepareStatement(sql);
            statement.setString(1, dbName);
            statement.setString(2, functionName);
            // 执行SQL查询并获取结果集
            ResultSet resultSet = statement.executeQuery();
            // 打印元数据
            ShellMysqlUtil.printMetaData(resultSet);
            MysqlFunction function = new MysqlFunction();
            function.setDbName(dbName);
            function.setName(functionName);
            // 遍历结果集
            while (resultSet.next()) {
                String securityType = resultSet.getString("SECURITY_TYPE");
                String definition = resultSet.getString("ROUTINE_DEFINITION");
                String sqlDataAccess = resultSet.getString("SQL_DATA_ACCESS");
                List<MysqlRoutineParam> params = ShellMysqlHelper.listFunctionParam(this.connManager.connection(), dbName, functionName);
                String createDefinition = this.showCreateFunction(dbName, functionName);
                function.setDbName(dbName);
                function.setParams(params);
                function.setDefinition(definition);
                function.setSecurityType(securityType);
                function.setCharacteristic(sqlDataAccess);
                function.setCreateDefinition(createDefinition);
            }
            // 关闭连接和释放资源
            ShellMysqlUtil.close(resultSet);
            ShellMysqlUtil.close(statement);
            return function;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public void createFunction(String dbName, MysqlFunction function) {
        try {
            String sql = MysqlFunctionSqlGenerator.INSTANCE.generate(function);
            ShellMysqlUtil.printSql(sql);
            Statement statement = this.connManager.connection(dbName).createStatement();
            statement.executeUpdate(sql);
            ShellMysqlUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public MysqlRecord selectRecord(MysqlSelectRecordParam param) {
        try {

            String dbName = param.getDbName();
            String tableName = param.getTableName();
            Connection connection = this.connManager.connection(dbName);
            MysqlRecordPrimaryKey primaryKey = param.getPrimaryKey();
            StringBuilder builder = new StringBuilder("SELECT * FROM ");
            builder.append(ShellMysqlUtil.wrap(dbName, tableName, this.dialect()))
                    .append(" WHERE ")
                    .append(ShellMysqlUtil.wrap(primaryKey.getColumnName(), this.dialect()))
                    .append(" = ?");
            String sql = builder.toString();
            ShellMysqlUtil.printSql(sql);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setObject(1, primaryKey.data());
            ResultSet resultSet = statement.executeQuery();
            ShellMysqlUtil.printMetaData(resultSet);
            MysqlColumns columns = ShellMysqlHelper.parseColumns(resultSet);
            MysqlRecord record = new MysqlRecord(columns);
            while (resultSet.next()) {
                for (MysqlColumn column : columns) {
                    Object data = resultSet.getObject(column.getName());
                    // 获取几何值
                    if (column.supportGeometry()) {
                        data = ShellMysqlHelper.getGeometryString(connection, data);
                    }
                    record.putValue(column, data);
                }
            }
            ShellMysqlUtil.close(resultSet);
            ShellMysqlUtil.close(statement);
            return record;
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    public String selectClientCharacter() {
        String character = "";
        try {
            Connection conn = this.connManager.connection();
            Statement stmt = conn.createStatement();
            ResultSet resultSet = stmt.executeQuery("SHOW VARIABLES LIKE 'character_set_%'");
            while (resultSet.next()) {
                String name = resultSet.getString(1);
                if ("character_set_client".equalsIgnoreCase(name)) {
                    character = resultSet.getString(2);
                    break;
                }
            }
            ShellMysqlUtil.close(resultSet);
            ShellMysqlUtil.close(stmt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return character;
    }

    public boolean existPrimaryKey(String dbName, String tableName) {
        try {
            Connection connection = this.connManager.connection(dbName);
            String sql = "SHOW INDEX FROM "
                    + ShellMysqlUtil.wrap(dbName, tableName, this.dialect())
                    + " WHERE Key_name = 'PRIMARY'";
            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet resultSet = stmt.executeQuery();
            ShellMysqlUtil.printMetaData(resultSet);
            boolean exist = resultSet.next();
            ShellMysqlUtil.close(resultSet);
            ShellMysqlUtil.close(stmt);
            return exist;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    // public ShellConnect getDbConnect() {
    //     return dbConnect;
    // }

    /**
     * 克隆表
     *
     * @param dbName        数据库
     * @param tableName     表名称
     * @param includeRecord 是否包含数据
     * @return 克隆表名称
     */
    public String cloneTable(String dbName, String tableName, boolean includeRecord) {
        // // 查询表
        // MysqlSelectTableParam selectTableParam = new MysqlSelectTableParam();
        // selectTableParam.setFull(true);
        // selectTableParam.setDbName(dbName);
        // selectTableParam.setTableName(tableName);
        // MysqlTable table = this.selectTable(selectTableParam);
        // 查询检查
        MysqlChecks checks = this.checks(dbName, tableName);
        // // 查询索引
        // MysqlIndexes indexes = this.indexes(dbName, tableName);
        // 查询触发器
        MysqlTriggers triggers = this.triggers(dbName, tableName);
        // 查询外键
        MysqlForeignKeys foreignKeys = this.foreignKeys(dbName, tableName);
        // // 查询字段
        // MysqlSelectColumnParam selectColumnParam = new MysqlSelectColumnParam();
        // selectColumnParam.setDbName(dbName);
        // selectColumnParam.setTableName(tableName);
        // MysqlColumns columns = this.selectColumns(selectColumnParam);
        if (checks != null) {
            for (MysqlCheck check : checks) {
                check.setName(check.getName() + ShellMysqlUtil.genCloneName());
                check.clearStatus();
                check.clearOriginalData();
                check.setCreated(true);
            }
        }
        if (triggers != null) {
            for (MysqlTrigger trigger : triggers) {
                trigger.setName(trigger.getName() + ShellMysqlUtil.genCloneName());
                trigger.clearStatus();
                trigger.clearOriginalData();
                trigger.setCreated(true);
            }
        }
        // if (indexes != null) {
        //     for (MysqlIndex index : indexes) {
        //         index.setName(index.getName() + ShellMysqlUtil.genCloneName());
        //     }
        // }
        if (foreignKeys != null) {
            for (MysqlForeignKey foreignKey : foreignKeys) {
                foreignKey.setName(foreignKey.getName() + ShellMysqlUtil.genCloneName());
                foreignKey.clearStatus();
                foreignKey.clearOriginalData();
                foreignKey.setCreated(true);
            }
        }
        // table.setName(table.getName() + ShellMysqlUtil.genCloneName());
        // // 创建表
        // MysqlCreateTableParam createTableParam = new MysqlCreateTableParam();
        // createTableParam.setTable(table);
        // createTableParam.setChecks(checks);
        // createTableParam.setIndexes(indexes);
        // createTableParam.setColumns(columns);
        // createTableParam.setTriggers(triggers);
        // createTableParam.setForeignKeys(foreignKeys);
        // this.createTable(createTableParam);
        // // 复制记录
        // if (includeRecord) {
        //     // 开始位置
        //     long start = 0;
        //     // 限制行
        //     long limit = 1000;
        //     while (true) {
        //         // 查询记录
        //         MysqlSelectRecordParam selectRecordParam = new MysqlSelectRecordParam();
        //         selectRecordParam.setDbName(dbName);
        //         selectRecordParam.setTableName(tableName);
        //         selectRecordParam.setStart(start);
        //         selectRecordParam.setLimit(limit);
        //         List<MysqlRecord> records = this.selectRecords(selectRecordParam);
        //         // 插入记录
        //         if (CollectionUtil.isNotEmpty(records)) {
        //             for (MysqlRecord record : records) {
        //                 MysqlInsertRecordParam insertRecordParam = ShellMysqlUtil.toInsertRecord(columns, record);
        //                 this.insertRecord(insertRecordParam);
        //             }
        //         }
        //         // 查询结束
        //         if (CollectionUtil.size(records) != limit) {
        //             break;
        //         }
        //         start += limit;
        //     }
        // }

        String newTableName = tableName + ShellMysqlUtil.genCloneName();
        try {
            Connection connection = this.connManager.connection(dbName);

            // 克隆基本的表结构
            String sql = "CREATE TABLE " + ShellMysqlUtil.wrap(dbName, newTableName, this.dialect())
                    + " LIKE " + ShellMysqlUtil.wrap(dbName, tableName, this.dialect());
            ShellMysqlUtil.printSql(sql);
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.execute();
            ShellMysqlUtil.close(stmt);
            MysqlTable table = new MysqlTable();
            table.setDbName(dbName);
            table.setName(newTableName);

            // 克隆表结构的检查、外键、触发器
            MysqlAlertTableParam alertTableParam = new MysqlAlertTableParam();
            alertTableParam.setTable(table);
            alertTableParam.setChecks(checks);
            alertTableParam.setTriggers(triggers);
            alertTableParam.setForeignKeys(foreignKeys);
            this.alertTable(alertTableParam);

            // 克隆数据
            if (includeRecord) {
                sql = "INSERT INTO " + ShellMysqlUtil.wrap(dbName, newTableName, this.dialect())
                        + " SELECT * FROM " + ShellMysqlUtil.wrap(dbName, tableName, this.dialect());
                ShellMysqlUtil.printSql(sql);
                stmt = connection.prepareStatement(sql);
                stmt.execute();
                ShellMysqlUtil.close(stmt);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
        return newTableName;
    }
}
