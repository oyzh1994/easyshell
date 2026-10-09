package cn.oyzh.easyshell.mariadb;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellJumpConfig;
import cn.oyzh.easyshell.domain.ShellProxyConfig;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.internal.ShellBaseClient;
import cn.oyzh.easyshell.internal.ShellClientChecker;
import cn.oyzh.easyshell.internal.ShellConnState;
import cn.oyzh.easyshell.mariadb.check.MariadbCheck;
import cn.oyzh.easyshell.mariadb.column.MariadbColumn;
import cn.oyzh.easyshell.mariadb.column.MariadbColumns;
import cn.oyzh.easyshell.mariadb.column.MariadbSelectColumnParam;
import cn.oyzh.easyshell.mariadb.condition.MariadbConditionUtil;
import cn.oyzh.easyshell.mariadb.database.MariadbDatabase;
import cn.oyzh.easyshell.mariadb.event.MariadbEvent;
import cn.oyzh.easyshell.mariadb.event.MariadbSelectEventParam;
import cn.oyzh.easyshell.mariadb.foreignKey.MariadbForeignKey;
import cn.oyzh.easyshell.mariadb.function.MariadbAlertFunctionParam;
import cn.oyzh.easyshell.mariadb.function.MariadbCreateFunctionParam;
import cn.oyzh.easyshell.mariadb.function.MariadbFunction;
import cn.oyzh.easyshell.mariadb.function.MariadbSelectFunctionParam;
import cn.oyzh.easyshell.mariadb.generator.event.MariadbEventAlertSqlGenerator;
import cn.oyzh.easyshell.mariadb.generator.event.MariadbEventCreateSqlGenerator;
import cn.oyzh.easyshell.mariadb.generator.function.MariadbFunctionAlertSqlGenerator;
import cn.oyzh.easyshell.mariadb.generator.function.MariadbFunctionCreateSqlGenerator;
import cn.oyzh.easyshell.mariadb.generator.procedure.MariadbProcedureAlertSqlGenerator;
import cn.oyzh.easyshell.mariadb.generator.procedure.MariadbProcedureCreateSqlGenerator;
import cn.oyzh.easyshell.mariadb.generator.table.MariadbTableAlertSqlGenerator;
import cn.oyzh.easyshell.mariadb.generator.table.MariadbTableCreateSqlGenerator;
import cn.oyzh.easyshell.mariadb.generator.view.MariadbViewAlertSqlGenerator;
import cn.oyzh.easyshell.mariadb.generator.view.MariadbViewCreateSqlGenerator;
import cn.oyzh.easyshell.mariadb.index.MariadbIndex;
import cn.oyzh.easyshell.mariadb.procedure.MariadbAlertProcedureParam;
import cn.oyzh.easyshell.mariadb.procedure.MariadbCreateProcedureParam;
import cn.oyzh.easyshell.mariadb.procedure.MariadbProcedure;
import cn.oyzh.easyshell.mariadb.procedure.MariadbSelectProcedureParam;
import cn.oyzh.easyshell.mariadb.record.MariadbDeleteRecordParam;
import cn.oyzh.easyshell.mariadb.record.MariadbInsertRecordParam;
import cn.oyzh.easyshell.mariadb.record.MariadbRecord;
import cn.oyzh.easyshell.mariadb.record.MariadbRecordFilter;
import cn.oyzh.easyshell.mariadb.record.MariadbRecordPrimaryKey;
import cn.oyzh.easyshell.mariadb.record.MariadbSelectRecordParam;
import cn.oyzh.easyshell.mariadb.record.MariadbUpdateRecordParam;
import cn.oyzh.easyshell.mariadb.routine.MariadbRoutineParam;
import cn.oyzh.easyshell.mariadb.table.MariadbAlertTableParam;
import cn.oyzh.easyshell.mariadb.table.MariadbCreateTableParam;
import cn.oyzh.easyshell.mariadb.table.MariadbSelectTableParam;
import cn.oyzh.easyshell.mariadb.table.MariadbTable;
import cn.oyzh.easyshell.mariadb.trigger.MariadbSelectTriggerParam;
import cn.oyzh.easyshell.mariadb.trigger.MariadbTrigger;
import cn.oyzh.easyshell.mariadb.view.MariadbAlertViewParam;
import cn.oyzh.easyshell.mariadb.view.MariadbCreateViewParam;
import cn.oyzh.easyshell.mariadb.view.MariadbSelectViewParam;
import cn.oyzh.easyshell.mariadb.view.MariadbView;
import cn.oyzh.easyshell.query.mariadb.ShellMariadbExecuteResult;
import cn.oyzh.easyshell.query.mariadb.ShellMariadbExplainResult;
import cn.oyzh.easyshell.util.mariadb.ShellMariadbUtil;
import cn.oyzh.fx.db.DBClient;
import cn.oyzh.fx.db.DBConnConfig;
import cn.oyzh.fx.db.DBConnManager;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBFeature;
import cn.oyzh.fx.db.DBObjects;
import cn.oyzh.fx.db.DBRecordData;
import cn.oyzh.fx.db.query.DBQueryResults;
import cn.oyzh.fx.db.sql.DBSqlParser;
import cn.oyzh.fx.db.util.DBUtil;
import cn.oyzh.ssh.domain.SSHConnect;
import cn.oyzh.ssh.jump.SSHJumpForwarder2;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * MariaDB数据库客户端封装，提供数据库连接、表/视图/函数/过程/事件/触发器等对象的CRUD操作，
 * 以及SQL执行、记录管理、索引/外键/检查约束管理等功能
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbClient implements ShellBaseClient, DBClient {

    /**
     * ssh端口转发器
     */
    private SSHJumpForwarder2 jumpForwarder;

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
            this.connManager = new ShellMariadbConnManager();
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
     * 构造MariaDB客户端
     *
     * @param shellConnect 连接信息
     */
    public ShellMariadbClient(ShellConnect shellConnect) {
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
            JulLog.warn("Mariadb client start error", ex);
            throw new ShellException(ex);
        }
    }

    @Override
    public ShellConnect getShellConnect() {
        return this.shellConnect;
    }

    /**
     * 初始化主机地址，开启跳板时通过SSH端口转发获取本地主机地址
     *
     * @return 主机地址
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
            connConfig.setSocketFactory(ShellMariadbProxySocketFactory.class.getName());
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
            JulLog.warn("Mariadb client close error.", ex);
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
    public int tableSize(String dbName) {
        try {
            int size = 0;
            Connection connection = this.getConnManager().connection(dbName);
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet resultSet = metaData.getTables(null, dbName, "%", TABLE_TYPES);
            DBUtil.printMetaData(resultSet);
            while (resultSet.next()) {
                if (ShellMariadbUtil.checkTableType(resultSet, dbName)) {
                    size++;
                }
            }
            IOUtil.close(resultSet);
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
        //     DBUtil.printSql(sql);
        //     PreparedStatement statement = connection.prepareStatement(sql);
        //     statement.setString(1, dbName);
        //     ResultSet resultSet = statement.executeQuery();
        //     DBUtil.printMetaData(resultSet);
        //     if (resultSet.next()) {
        //         size = resultSet.getInt(1);
        //     }
        //     IOUtil.close(resultSet);
        // } catch (Exception ex) {
        //     ex.printStackTrace();
        //     throw new ShellException(ex);
        // }
        // return size;
    }

    @Override
    public int viewSize(String dbName) {
        try {
            int size = 0;
            Connection connection = this.getConnManager().connection(dbName);
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet resultSet = metaData.getTables(null, dbName, "%", VIEW_TYPES);
            DBUtil.printMetaData(resultSet);
            while (resultSet.next()) {
                if (ShellMariadbUtil.checkViewType(resultSet, dbName)) {
                    size++;
                }
            }
            IOUtil.close(resultSet);
            return size;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 执行SQL语句
     *
     * @param dbName 数据库名称
     * @param sql    SQL语句
     * @return 执行结果
     */
    public DBQueryResults<ShellMariadbExecuteResult> executeSql(String dbName, String sql) {
        DBQueryResults<ShellMariadbExecuteResult> results = new DBQueryResults<>();
        Connection connection = null;
        try {
            this.printSql(sql);
            DBSqlParser parser = DBSqlParser.getParser(sql, this.dialect());
            List<String> list = parser.parseSql();
            connection = this.getConnManager().connection(dbName);
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();
            for (String execSql : list) {
                ShellMariadbExecuteResult result = new ShellMariadbExecuteResult();
                result.setContent(execSql);
                try {
                    long startTime = System.nanoTime();
                    boolean isQuery = statement.execute(execSql);
                    if (isQuery) {
                        ResultSet resultSet = statement.getResultSet();
                        result.setFullColumn(parser.isFullColumn(execSql));
                        result.parseResult(resultSet, connection, !parser.isSelect(execSql));
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

    @Override
    public int insertBatch(String dbName, List<String> sqlList) {
        return this.insertBatch(dbName, sqlList, false);
    }

    @Override
    public int procedureSize(String dbName) {
        // int size = 0;
        // try {
        //     Connection connection = this.procedureConnection(dbName, schema);
        //     DatabaseMetaData metaData = connection.getMetaData();
        //     ResultSet resultSet = metaData.getProcedures(dbName, schema, "%");
        //     DBUtil.printMetaData(resultSet);
        //     while (resultSet.next()) {
        //         if (ShellMariadbUtil.checkProcedureType(resultSet, dbName)) {
        //             size++;
        //         }
        //     }
        //     IOUtil.close(resultSet);
        // } catch (Exception ex) {
        //     ex.printStackTrace();
        //     throw new ShellException(ex);
        // }
        // return size;
        int size = 0;
        try {
            Connection connection = this.getConnManager().procedureConnection(dbName);
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
            this.printSql(sql);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, dbName);
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
    public int functionSize(String dbName) {
        // int size = 0;
        // try {
        //     Connection connection = this.functionConnection(dbName, schema);
        //     DatabaseMetaData metaData = connection.getMetaData();
        //     ResultSet resultSet = metaData.getFunctions(dbName, schema, "%");
        //     DBUtil.printMetaData(resultSet);
        //     if (resultSet.next()) {
        //         if (ShellMariadbUtil.checkFunctionType(resultSet, dbName)) {
        //             size++;
        //         }
        //     }
        //     IOUtil.close(resultSet);
        // } catch (Exception ex) {
        //     ex.printStackTrace();
        //     throw new ShellException(ex);
        // }
        // return size;
        int size = 0;
        try {
            Connection connection = this.getConnManager().functionConnection(dbName);
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
            this.printSql(sql);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, dbName);
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
    public void alertFunction(MariadbAlertFunctionParam param) {
        Connection connection = null;
        try {
            connection = this.getConnManager().functionConnection(param.getDbName());
            connection.setAutoCommit(false);
            List<String> list = MariadbFunctionAlertSqlGenerator.generateSql(param);
            Statement statement = connection.createStatement();
            for (String sql : list) {
                this.printSql(sql);
                statement.executeUpdate(sql);
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
     * 查询触发器列表
     *
     * @param dbName 数据库名称
     * @return 结果
     */
    public DBObjects<MariadbTrigger> selectTriggers(String dbName) {
        MariadbSelectTriggerParam param = new MariadbSelectTriggerParam();
        param.setFull(true);
        param.setDbName(dbName);
        return this.selectTriggers(param);
    }

    /**
     * 查询触发器列表
     *
     * @param dbName    数据库名称
     * @param tableName 表名称
     * @return 结果
     */
    public DBObjects<MariadbTrigger> selectTriggers(String dbName, String tableName) {
        MariadbSelectTriggerParam param = new MariadbSelectTriggerParam();
        param.setFull(true);
        param.setDbName(dbName);
        param.setTableName(tableName);
        return this.selectTriggers(param);
    }

    //    /**
    //     * 查询触发器列表
    //     *
    //     * @param dbName 数据库名称
    //     * @return 结果
    //     */
    //    public List<MariadbTrigger> selectTriggers(String dbName) {
    //        try {
    //            String sql = """
    //                    SELECT
    //                        TRIGGER_NAME,
    //                        ACTION_TIMING,
    //                        ACTION_STATEMENT,
    //                        EVENT_MANIPULATION,
    //                        EVENT_OBJECT_TABLE
    //                    FROM
    //                        INFORMATION_SCHEMA.TRIGGERS
    //                    WHERE
    //                        TRIGGER_SCHEMA = ?
    //                    """;
    //            this.printSql(sql);
    //            PreparedStatement statement = this.getConnManager().connection(dbName).prepareStatement(sql);
    //            statement.setString(1, dbName);
    //            ResultSet resultSet = statement.executeQuery();
    //            List<MariadbTrigger> list = new ArrayList<>();
    //            while (resultSet.next()) {
    //                MariadbTrigger trigger = new MariadbTrigger();
    //                String name = resultSet.getString("TRIGGER_NAME");
    //                String timing = resultSet.getString("ACTION_TIMING");
    //                String tableName = resultSet.getString("EVENT_OBJECT_TABLE");
    //                String manipulation = resultSet.getString("EVENT_MANIPULATION");
    //                String actionStatement = resultSet.getString("ACTION_STATEMENT");
    //                trigger.setName(name);
    //                trigger.setTableName(tableName);
    //                trigger.setDefinition(actionStatement);
    //                trigger.setPolicy(timing, manipulation);
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
     * @param param 参数
     * @return 结果
     */
    public DBObjects<MariadbTrigger> selectTriggers(MariadbSelectTriggerParam param) {
        try {
            String dbName = param.getDbName();
            PreparedStatement statement;
            Connection connection = this.getConnManager().connection(dbName);
            if (param.getTableName() == null) {
                String sql = """
                        SELECT
                            TRIGGER_NAME,
                            ACTION_TIMING,
                            ACTION_STATEMENT,
                            EVENT_MANIPULATION,
                            EVENT_OBJECT_TABLE
                        FROM
                            INFORMATION_SCHEMA.TRIGGERS
                        WHERE
                            TRIGGER_SCHEMA = ?
                        """;
                this.printSql(sql);
                statement = connection.prepareStatement(sql);
                statement.setString(1, dbName);
            } else {
                String sql = """
                        SELECT
                            TRIGGER_NAME,
                            ACTION_TIMING,
                            ACTION_STATEMENT,
                            EVENT_OBJECT_TABLE,
                            EVENT_MANIPULATION
                        FROM
                            INFORMATION_SCHEMA.TRIGGERS
                        WHERE
                            TRIGGER_SCHEMA = ?
                        AND
                            EVENT_OBJECT_TABLE = ?
                        """;
                this.printSql(sql);
                statement = connection.prepareStatement(sql);
                statement.setString(1, dbName);
                statement.setString(2, param.getTableName());
            }

            ResultSet resultSet = statement.executeQuery();
            DBObjects<MariadbTrigger> list = new DBObjects<>();
            while (resultSet.next()) {
                MariadbTrigger trigger = new MariadbTrigger();
                String name = resultSet.getString("TRIGGER_NAME");
                String timing = resultSet.getString("ACTION_TIMING");
                String tableName = resultSet.getString("EVENT_OBJECT_TABLE");
                String manipulation = resultSet.getString("EVENT_MANIPULATION");
                String actionStatement = resultSet.getString("ACTION_STATEMENT");
                trigger.setName(name);
                trigger.setTableName(tableName);
                trigger.setDefinition(actionStatement);
                trigger.setPolicy(timing, manipulation);
                if (param.isFull()) {
                    trigger.setCreateDefinition(this.showCreateTrigger(dbName, name));
                }
                list.add(trigger);
            }
            IOUtil.close(resultSet);
            IOUtil.close(statement);
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
            Connection conn = this.getConnManager().connection();
            Statement stmt = conn.createStatement();
            ResultSet resultSet = stmt.executeQuery("SELECT VERSION()");
            if (resultSet.next()) {
                version = resultSet.getString(1);
            }
            this.putProperty("version", version);
            IOUtil.close(resultSet);
            IOUtil.close(stmt);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            ResultSet resultSet = stmt.executeQuery("SELECT @@version_comment AS db_product");
            if (resultSet.next()) {
                product = resultSet.getString(1);
            }
            this.putProperty("product", product);
            IOUtil.close(resultSet);
            IOUtil.close(stmt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return product;
    }

    /**
     * 删除事件
     *
     * @param dbName 数据库名称
     * @param event  事件
     */
    public void dropEvent(String dbName, MariadbEvent event) {
        try {
            String sql = "DROP EVENT " + DBUtil.wrap(event.getDbName(), event.getName(), this.dialect());
            this.printSql(sql);
            Statement statement = this.getConnManager().connection(dbName).createStatement();
            statement.executeUpdate(sql);
            IOUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 创建事件
     *
     * @param dbName 数据库名称
     * @param event  事件
     */
    public void createEvent(String dbName, MariadbEvent event) {
        try {
            String sql = MariadbEventCreateSqlGenerator.generateSql(event);
            this.printSql(sql);
            Statement statement = this.getConnManager().connection(dbName).createStatement();
            statement.executeUpdate(sql);
            IOUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 修改事件
     *
     * @param dbName 数据库名称
     * @param event  事件
     */
    public void alertEvent(String dbName, MariadbEvent event) {
        try {
            String sql = MariadbEventAlertSqlGenerator.generateSql(event);
            this.printSql(sql);
            Statement statement = this.getConnManager().connection(dbName).createStatement();
            statement.executeUpdate(sql);
            IOUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 查询事件数量
     *
     * @param dbName 数据库名称
     * @return 事件数量
     */
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
            this.printSql(sql);
            PreparedStatement statement = this.getConnManager().connection(dbName).prepareStatement(sql);
            statement.setString(1, dbName);
            ResultSet resultSet = statement.executeQuery();
            DBUtil.printMetaData(resultSet);
            while (resultSet.next()) {
                count = resultSet.getInt(1);
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
     * 查询事件
     *
     * @param dbName    数据库
     * @param eventName 事件名
     * @return 结果
     */
    public MariadbEvent selectEvent(String dbName, String eventName) {
        MariadbSelectEventParam param = new MariadbSelectEventParam();
        param.setFull(true);
        param.setDbName(dbName);
        param.setEventName(eventName);
        return this.selectEvent(param);
    }

    /**
     * 查询事件
     *
     * @param dbName    数据库
     * @param eventName 事件名
     * @return 结果
     */
    public MariadbEvent selectEventSimple(String dbName, String eventName) {
        MariadbSelectEventParam param = new MariadbSelectEventParam();
        param.setFull(false);
        param.setDbName(dbName);
        param.setEventName(eventName);
        return this.selectEvent(param);
    }

    /**
     * 查询事件
     *
     * @param param 参数
     * @return 结果
     */
    public MariadbEvent selectEvent(MariadbSelectEventParam param) {
        try {
            String dbName = param.getDbName();
            String eventName = param.getEventName();
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
            this.printSql(sql);
            PreparedStatement statement = this.getConnManager().connection(dbName).prepareStatement(sql);
            statement.setString(1, dbName);
            statement.setString(2, eventName);
            ResultSet resultSet = statement.executeQuery();
            DBUtil.printMetaData(resultSet);
            MariadbEvent event = new MariadbEvent();
            event.setName(eventName);
            event.setDbName(dbName);
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
                if (param.isFull()) {
                    String createDefinition = this.showCreateEvent(dbName, eventName);
                    event.setCreateDefinition(createDefinition);
                }
                event.setType(type);
                event.setEnds(ends);
                event.setStarts(starts);
                event.setComment(comment);
                event.setEventStatus(status);
                event.setDefinition(definition);
                event.setExecuteAt(executeAt);
                event.setOnCompletion(onCompletion);
                event.setIntervalValue(intervalValue);
                event.setIntervalField(intervalField);
            }
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            return event;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 查询事件列表
     *
     * @param dbName 数据库
     * @return 结果
     */
    public List<MariadbEvent> selectEvents(String dbName) {
        MariadbSelectEventParam param = new MariadbSelectEventParam();
        param.setFull(true);
        param.setDbName(dbName);
        return this.selectEvents(param);
    }

    /**
     * 查询事件列表
     *
     * @param dbName 数据库
     * @return 结果
     */
    public List<MariadbEvent> selectEventsSimple(String dbName) {
        MariadbSelectEventParam param = new MariadbSelectEventParam();
        param.setFull(false);
        param.setDbName(dbName);
        return this.selectEvents(param);
    }

    /**
     * 查询事件列表
     *
     * @param param 参数
     * @return 结果
     */
    public List<MariadbEvent> selectEvents(MariadbSelectEventParam param) {
        List<MariadbEvent> list = new ArrayList<>();
        try {
            String dbName = param.getDbName();
            String sql = """
                    SELECT
                        *
                    FROM
                        `INFORMATION_SCHEMA`.`EVENTS`
                    WHERE
                        `EVENT_SCHEMA` = ?
                    """;
            this.printSql(sql);
            PreparedStatement statement = this.getConnManager().connection(dbName).prepareStatement(sql);
            statement.setString(1, dbName);
            ResultSet resultSet = statement.executeQuery();
            DBUtil.printMetaData(resultSet);
            while (resultSet.next()) {
                MariadbEvent event = new MariadbEvent();
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
                if (param.isFull()) {
                    String createDefinition = this.showCreateEvent(dbName, name);
                    event.setCreateDefinition(createDefinition);
                }
                event.setName(name);
                event.setType(type);
                event.setEnds(ends);
                event.setStarts(starts);
                event.setDbName(dbName);
                event.setComment(comment);
                event.setEventStatus(status);
                event.setExecuteAt(executeAt);
                event.setDefinition(definition);
                event.setOnCompletion(onCompletion);
                event.setIntervalValue(intervalValue);
                event.setIntervalField(intervalField);
                list.add(event);
            }
            IOUtil.close(resultSet);
            IOUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
        return list;
    }

    @Override
    public boolean isSupportFeature(DBFeature feature) {
        try {
            if (feature == DBFeature.EVENT) {
                return true;
            }
            // 检查约束
            if (feature == DBFeature.CHECK) {
                String version = this.selectVersion();
                // mariadb
                if (StringUtil.containsIgnoreCase(version, "mariadb")) {
                    return true;
                }
                // 最低支持版本8.0.16
                String[] arr = version.split("\\.");
                if (Integer.parseInt(arr[0]) < 8) {
                    return false;
                }
                if (Integer.parseInt(arr[0]) > 8) {
                    return true;
                }
                if (Integer.parseInt(arr[0]) == 8 && arr.length >= 2) {
                    return Integer.parseInt(arr[1]) > 0;
                }
                if (Integer.parseInt(arr[0]) == 8 && arr.length >= 3) {
                    return Integer.parseInt(arr[2]) >= 16;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean isSupportCheckFeature() {
        return this.isSupportFeature(DBFeature.CHECK);
    }

    @Override
    public boolean isSupportEventFeature() {
        return this.isSupportFeature(DBFeature.EVENT);
    }

    @Override
    public Long getGeneratedKeys(Statement statement) throws Exception {
        ResultSet rs = statement.getGeneratedKeys();
        Long newId = null;
        if (rs.next()) {
            newId = rs.getLong(1);
        } else {
            IOUtil.close(statement);
            String sql = "SELECT LAST_INSERT_ID();";
            statement = statement.getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
                newId = resultSet.getLong(1);
            }
            IOUtil.close(statement);
            IOUtil.close(resultSet);
        }
        IOUtil.close(rs);
        return newId;
    }

    /**
     * 表类型集合
     */
    public static final String[] TABLE_TYPES = new String[]{"TABLE", "SYSTEM TABLE", "SYSTEM VIEW", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM"};

    /**
     * 视图类型集合
     */
    public static final String[] VIEW_TYPES = new String[]{"VIEW"};

    /**
     * 查询表列表(完整信息)
     *
     * @param dbName 库名称
     * @return 表列表
     */
    public List<MariadbTable> selectTables(String dbName) {
        MariadbSelectTableParam param = new MariadbSelectTableParam();
        param.setFull(true);
        param.setDbName(dbName);
        return this.selectTables(param);
    }

    /**
     * 查询表列表(简要信息)
     *
     * @param dbName 库名称
     * @return 表列表
     */
    public List<MariadbTable> selectTablesSimple(String dbName) {
        MariadbSelectTableParam param = new MariadbSelectTableParam();
        param.setFull(false);
        param.setDbName(dbName);
        return this.selectTables(param);
    }

    /**
     * 查询表列表
     *
     * @param param 参数
     * @return 表列表
     */
    public List<MariadbTable> selectTables(MariadbSelectTableParam param) {
        try {
            String dbName = param.getDbName();
            List<MariadbTable> tables = new ArrayList<>();
            Connection connection = this.getConnManager().connection(dbName);
            //if (param.isFull()) {
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
            this.printSql(sql);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, dbName);
            ResultSet resultSet = statement.executeQuery();
            DBUtil.printMetaData(resultSet);
            while (resultSet.next()) {
                MariadbTable table = new MariadbTable();
                String tableEngine = resultSet.getString("ENGINE");
                String tableName = resultSet.getString("TABLE_NAME");
                String rowFormat = resultSet.getString("ROW_FORMAT");
                Long autoIncrement = resultSet.getLong("AUTO_INCREMENT");
                String tableComment = resultSet.getString("TABLE_COMMENT");
                String tableCollation = resultSet.getString("TABLE_COLLATION");
                if (param.isFull()) {
                    String showCreateTable = this.showCreateTable(dbName, tableName);
                    table.setCreateDefinition(showCreateTable);
                }
                table.setDbName(dbName);
                table.setName(tableName);
                table.setEngine(tableEngine);
                table.setRowFormat(rowFormat);
                table.setComment(tableComment);
                table.setAutoIncrement(autoIncrement);
                table.setCharsetAndCollation(tableCollation);
                tables.add(table);
            }
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            //} else {
            //    DatabaseMetaData metaData = connection.getMetaData();
            //    ResultSet resultSet = metaData.getTables(null, null, "%", TABLE_TYPES);
            //    while (resultSet.next()) {
            //        if (ShellMariadbUtil.checkTableType(resultSet, dbName)) {
            //            MariadbTable table = new MariadbTable();
            //            table.setDbName(dbName);
            //            String remarks = resultSet.getString("REMARKS");
            //            String tableName = resultSet.getString("TABLE_NAME");
            //            table.setName(tableName);
            //            table.setComment(remarks);
            //            tables.add(table);
            //        }
            //    }
            //    IOUtil.close(resultSet);
            //}
            return tables;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 查询字段
     *
     * @param param 参数
     * @return 结果
     */
    public List<MariadbColumn> selectColumns(MariadbSelectColumnParam param) {
        try {
            String dbName = param.getDbName();
            String tableName = param.getTableName();
            MariadbColumns columns = new MariadbColumns();
            String sql = "SHOW FULL COLUMNS FROM " + DBUtil.wrap(dbName, tableName, this.dialect());
            PreparedStatement statement = this.getConnManager().connection(dbName).prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();
            DBUtil.printMetaData(resultSet);
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

                MariadbColumn column = new MariadbColumn();
                column.parseKey(key);
                column.setType(type);
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
            IOUtil.close(resultSet);
            // 返回排序后的数据
            return new ArrayList<>(columns.sortOfPosition());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 查询记录列表
     *
     * @param param 参数
     * @return 记录列表
     */
    public List<MariadbRecord> selectRecords(MariadbSelectRecordParam param) {
        try {
            Connection connection = this.getConnManager().connection(param.getDbName());
            StringBuilder builder = new StringBuilder("SELECT * FROM ");
            builder.append(DBUtil.wrap(param.getDbName(), param.getTableName(), this.dialect()));
            String filterCondition = MariadbConditionUtil.buildCondition(param.getFilters());
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
            this.printSql(sql);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            DBUtil.printMetaData(resultSet);
            List<MariadbRecord> records = new ArrayList<>();
            List<MariadbColumn> columns;
            if (param.getColumns() != null) {
                columns = param.getColumns();
            } else {
                columns = ShellMariadbHelper.parseColumns(resultSet);
            }
            while (resultSet.next()) {
                MariadbRecord record = new MariadbRecord(columns, param.isReadonly());
                for (MariadbColumn column : columns) {
                    Object data = resultSet.getObject(column.getName());
                    // 获取几何值
                    if (column.supportGeometry()) {
                        data = ShellMariadbHelper.getGeometryString(connection, data);
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
     * 查询记录数量
     *
     * @param param 参数
     * @return 记录数量
     */
    public long selectRecordCount(MariadbSelectRecordParam param) {
        long count = 0;
        try {
            Connection connection = this.getConnManager().connection(param.getDbName());
            StringBuilder builder = new StringBuilder("SELECT COUNT(*) FROM ");
            builder.append(DBUtil.wrap(param.getDbName(), param.getTableName(), this.dialect()));
            String filterCondition = MariadbConditionUtil.buildCondition(param.getFilters());
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
     * 插入记录
     *
     * @param param 参数
     * @return 受影响行数
     */
    public int insertRecord(MariadbInsertRecordParam param) {
        if (param == null || param.getRecord() == null) {
            return 0;
        }
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("INSERT INTO ")
                    .append(DBUtil.wrap(param.getDbName(), param.getTableName(), this.dialect()))
                    .append("(");
            for (String column : param.getRecord().columns()) {
                builder.append(DBUtil.wrap(column, this.dialect())).append(",");
            }
            StringUtil.deleteLast(builder);
            builder.append(")");
            builder.append(" VALUES(");
            for (String column : param.getRecord().columns()) {
                if (param.getRecord().isTypeGeometry(column)) {
                    builder.append("ST_GeomFromText(?),");
                } else {
                    builder.append("?,");
                }
            }
            StringUtil.deleteLast(builder);
            builder.append(")");
            String sql = builder.toString();
            this.printSql(sql);
            Connection connection = this.getConnManager().connection(param.getDbName());
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            int index = 1;
            for (String colName : param.getRecord().columns()) {
                DBUtil.setVal(statement, param.getRecord().value(colName), index++);
            }
            int count = statement.executeUpdate();
            MariadbRecordPrimaryKey primaryKey = param.getPrimaryKey();
            // 处理自动递增值
            if (primaryKey != null && primaryKey.shouldReturnData()) {
                Long newId = this.getGeneratedKeys(statement);
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
     * 删除记录
     *
     * @param param 参数
     * @return 受影响行数
     */
    public int deleteRecord(MariadbDeleteRecordParam param) {
        try {
            int updateCount;
            String dbName = param.getDbName();
            String tableName = param.getTableName();
            Connection connection = this.getConnManager().connection(dbName);
            StringBuilder builder = new StringBuilder();
            builder.append("DELETE FROM ")
                    .append(DBUtil.wrap(dbName, tableName, this.dialect()))
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
                        builder.append(DBUtil.wrap(colName, this.dialect()))
                                .append(" = ?");
                    } else {
                        builder.append(DBUtil.wrap(colName, this.dialect()))
                                .append(" IS NULL");
                    }
                }
                builder.append(" LIMIT 1");
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
                MariadbRecordPrimaryKey primaryKey = param.getPrimaryKey();
                builder.append(DBUtil.wrap(primaryKey.getColumnName(), this.dialect()))
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
     * 更新记录
     *
     * @param param 参数
     * @return 受影响行数
     */
    public int updateRecord(MariadbUpdateRecordParam param) {
        try {
            int updateCount;
            String dbName = param.getDbName();
            String tableName = param.getTableName();
            DBRecordData recordData = param.getUpdateRecord();
            StringBuilder builder = new StringBuilder();
            builder.append("UPDATE ")
                    .append(DBUtil.wrap(dbName, tableName, this.dialect()))
                    .append(" SET ");
            for (String column : recordData.columns()) {
                if (recordData.isTypeGeometry(column)) {
                    builder.append(DBUtil.wrap(column, this.dialect())).append(" = ST_GeomFromText(?),");
                } else {
                    builder.append(DBUtil.wrap(column, this.dialect())).append(" = ?,");
                }
            }
            builder.deleteCharAt(builder.length() - 1);
            builder.append(" WHERE ");
            Connection connection = this.getConnManager().connection(dbName);
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
                    builder.append(DBUtil.wrap(column, this.dialect())).append(" = ?");
                }
                builder.append(" LIMIT 1");
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
                    DBUtil.setVal(statement, originalRecordData.value(colName), index++);
                }
                updateCount = DBUtil.executeUpdate(statement);
                IOUtil.close(statement);
            } else {
                MariadbRecordPrimaryKey primaryKey = param.getPrimaryKey();
                builder.append(DBUtil.wrap(primaryKey.getColumnName(), this.dialect())).append(" = ?");
                builder.append(" LIMIT 1");
                String sql = builder.toString();
                this.printSql(sql);
                DBUtil.printData(recordData);
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
     * 查询表的建表语句
     *
     * @param dbName    数据库名称
     * @param tableName 表名称
     * @return 建表语句
     */
    public String showCreateTable(String dbName, String tableName) {
        try {
            Connection connection = this.getConnManager().connection(dbName);
            String sql = "SHOW CREATE TABLE " + DBUtil.wrap(tableName, this.dialect());
            this.printSql(sql);
            Statement stmt = connection.createStatement();
            ResultSet resultSet = stmt.executeQuery(sql);
            String createDefinition = "";
            if (resultSet.next()) {
                createDefinition = resultSet.getString(2);
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
     * 查询视图的建视图语句
     *
     * @param dbName   数据库名称
     * @param viewName 视图名称
     * @return 建视图语句
     */
    public String showCreateView(String dbName, String viewName) {
        try {
            Connection connection = this.getConnManager().connection(dbName);
            String sql = "SHOW CREATE VIEW " + DBUtil.wrap(viewName, this.dialect());
            this.printSql(sql);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            String createDefinition = "";
            if (resultSet.next()) {
                createDefinition = resultSet.getString("Create View");
            }
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            return createDefinition;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 查询函数的建函数语句
     *
     * @param dbName       数据库名称
     * @param functionName 函数名称
     * @return 建函数语句
     */
    public String showCreateFunction(String dbName, String functionName) {
        try {
            Connection connection = this.getConnManager().functionConnection(dbName);
            String sql = "SHOW CREATE FUNCTION " + DBUtil.wrap(functionName, this.dialect());
            this.printSql(sql);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            String createDefinition = "";
            if (resultSet.next()) {
                createDefinition = resultSet.getString("Create Function");
            }
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            return createDefinition;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 查询过程的建过程语句
     *
     * @param dbName        数据库名称
     * @param procedureName 过程名称
     * @return 建过程语句
     */
    public String showCreateProcedure(String dbName, String procedureName) {
        try {
            Connection connection = this.getConnManager().procedureConnection(dbName);
            String sql = "SHOW CREATE PROCEDURE " + DBUtil.wrap(procedureName, this.dialect());
            this.printSql(sql);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            String createDefinition = "";
            if (resultSet.next()) {
                createDefinition = resultSet.getString("Create Procedure");
            }
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            return createDefinition;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 查询触发器的建触发器语句
     *
     * @param dbName      数据库名称
     * @param triggerName 触发器名称
     * @return 建触发器语句
     */
    public String showCreateTrigger(String dbName, String triggerName) {
        try {
            Connection connection = this.getConnManager().connection(dbName);
            String sql = "SHOW CREATE TRIGGER " + DBUtil.wrap(triggerName, this.dialect());
            this.printSql(sql);
            Statement statement = connection.createStatement();
            // 执行SQL查询并获取结果集
            ResultSet resultSet = statement.executeQuery(sql);
            String createDefinition = "";
            if (resultSet.next()) {
                createDefinition = resultSet.getString("Sql Original Statement");
            }
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            return createDefinition;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 查询事件的建事件语句
     *
     * @param dbName    数据库名称
     * @param eventName 事件名称
     * @return 建事件语句
     */
    public String showCreateEvent(String dbName, String eventName) {
        try {
            Connection connection = this.getConnManager().connection(dbName);
            String sql = "SHOW CREATE EVENT " + DBUtil.wrap(eventName, this.dialect());
            this.printSql(sql);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            String createDefinition = "";
            if (resultSet.next()) {
                createDefinition = resultSet.getString("Create Event");
            }
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            return createDefinition;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 查询支持的存储引擎列表
     *
     * @return 存储引擎列表
     */
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
            this.printSql(sql);
            Statement statement = this.getConnManager().connection().createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            DBUtil.printMetaData(resultSet);
            while (resultSet.next()) {
                engines.add(resultSet.getString(1));
            }
            IOUtil.close(statement);
            this.putProperty("engines", engines);
            return engines;
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    /**
     * 查询数据库列表
     *
     * @return 数据库列表
     */
    public List<MariadbDatabase> databases() {
        try {
            Statement statement = this.getConnManager().connection().createStatement();
            ResultSet resultSet = statement.executeQuery("SHOW DATABASES");
            List<MariadbDatabase> list = new ArrayList<>();
            while (resultSet.next()) {
                MariadbDatabase databases = new MariadbDatabase();
                String dbName = resultSet.getString(1);
                databases.setName(dbName);
                databases.setCharsetAndCollation(this.databaseCollation(dbName));
                list.add(databases);
            }
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            return list;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 查询数据库名称列表
     *
     * @return 数据库名称列表
     */
    public List<String> databaseNames() {
        try {
            Statement statement = this.getConnManager().connection().createStatement();
            ResultSet resultSet = statement.executeQuery("SHOW DATABASES");
            List<String> list = new ArrayList<>();
            while (resultSet.next()) {
                String dbName = resultSet.getString(1);
                list.add(dbName);
            }
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            return list;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 查询数据库
     *
     * @param dbName 数据库名称
     * @return 数据库
     */
    public MariadbDatabase database(String dbName) {
        try {
            MariadbDatabase database = new MariadbDatabase();
            database.setName(dbName);
            database.setCharsetAndCollation(this.databaseCollation(dbName));
            return database;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 查询表(完整信息)
     *
     * @param dbName    数据库名称
     * @param tableName 表名称
     * @return 表
     */
    public MariadbTable selectTable(String dbName, String tableName) {
        MariadbSelectTableParam param = new MariadbSelectTableParam();
        param.setFull(true);
        param.setDbName(dbName);
        param.setTableName(tableName);
        return this.selectTable(param);
    }

    /**
     * 查询表(简要信息)
     *
     * @param dbName    数据库名称
     * @param tableName 表名称
     * @return 表
     */
    public MariadbTable selectTableSimple(String dbName, String tableName) {
        MariadbSelectTableParam param = new MariadbSelectTableParam();
        param.setFull(false);
        param.setDbName(dbName);
        param.setTableName(tableName);
        return this.selectTable(param);
    }

    /**
     * 查询表
     *
     * @param param 参数
     * @return 表
     */
    public MariadbTable selectTable(MariadbSelectTableParam param) {
        try {
            String dbName = param.getDbName();
            String tableName = param.getTableName();
            MariadbTable table = new MariadbTable();
            table.setDbName(dbName);
            table.setName(tableName);
            Connection connection = this.getConnManager().connection(dbName);
            //            if (param.isFull()) {
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
            this.printSql(sql);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, dbName);
            statement.setString(2, tableName);
            ResultSet resultSet = statement.executeQuery();
            DBUtil.printMetaData(resultSet);
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
                table.setCharsetAndCollation(tableCollation);
            }
            if (param.isFull()) {
                String showCreateTable = this.showCreateTable(dbName, tableName);
                table.setCreateDefinition(showCreateTable);
            }
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            //            } else {
            //                DatabaseMetaData metaData = connection.getMetaData();
            //                ResultSet resultSet = metaData.getTables(null, null, tableName, TABLE_TYPES);
            //                while (resultSet.next()) {
            //                    if (ShellMariadbUtil.checkTableType(resultSet, dbName)) {
            //                        String remarks = resultSet.getString("REMARKS");
            //                        table.setComment(remarks);
            //                    }
            //                }
            //                IOUtil.close(resultSet);
            //            }
            return table;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    // public MariadbTable selectFullTable(MariadbSelectTableParam param) {
    //     try {
    //         String dbName = param.getDbName();
    //         String tableName = param.getTableName();
    //         MariadbTable table = new MariadbTable();
    //         table.setDbName(dbName);
    //         table.setName(tableName);
    //         Connection connection = this.getConnManager().connection(dbName);
    //         String sql = """
    //                 SELECT
    //                     `AUTO_INCREMENT`, `ROW_FORMAT`, `TABLE_COLLATION`, `TABLE_COMMENT`, `ENGINE`
    //                 FROM
    //                     information_schema.TABLES
    //                 WHERE
    //                     `TABLE_SCHEMA` = ?
    //                 AND
    //                     `TABLE_NAME` = ?
    //                 AND
    //                     `TABLE_TYPE` != 'VIEW'
    //                 """;
    //         this.printSql(sql);
    //         PreparedStatement statement = connection.prepareStatement(sql);
    //         statement.setString(1, dbName);
    //         statement.setString(2, tableName);
    //         ResultSet resultSet = statement.executeQuery();
    //         DBUtil.printMetaData(resultSet);
    //         String showCreateTable = this.showCreateTable(dbName, tableName);
    //         while (resultSet.next()) {
    //             String tableEngine = resultSet.getString("ENGINE");
    //             String rowFormat = resultSet.getString("ROW_FORMAT");
    //             Long autoIncrement = resultSet.getLong("AUTO_INCREMENT");
    //             String tableComment = resultSet.getString("TABLE_COMMENT");
    //             String tableCollation = resultSet.getString("TABLE_COLLATION");
    //             table.setEngine(tableEngine);
    //             table.setRowFormat(rowFormat);
    //             table.setComment(tableComment);
    //             table.setAutoIncrement(autoIncrement);
    //             table.setCreateDefinition(showCreateTable);
    //             table.setCharsetAndCollation(tableCollation);
    //         }
    //         IOUtil.close(resultSet);
    //         IOUtil.close(statement);
    //         return table;
    //     } catch (Exception ex) {
    //         ex.printStackTrace();
    //         throw new ShellException(ex);
    //     }
    // }

    /**
     * 查询视图
     *
     * @param dbName   数据库名称
     * @param viewName 视图名称
     * @return 结果
     */
    public MariadbView selectView(String dbName, String viewName) {
        MariadbSelectViewParam param = new MariadbSelectViewParam();
        param.setFull(true);
        param.setDbName(dbName);
        param.setViewName(viewName);
        return this.selectView(param);
    }

    /**
     * 查询视图
     *
     * @param dbName   数据库名称
     * @param viewName 视图名称
     * @return 结果
     */
    public MariadbView selectViewSimple(String dbName, String viewName) {
        MariadbSelectViewParam param = new MariadbSelectViewParam();
        param.setFull(false);
        param.setDbName(dbName);
        param.setViewName(viewName);
        return this.selectView(param);
    }

    /**
     * 查询视图
     *
     * @param param 参数
     * @return 结果
     */
    public MariadbView selectView(MariadbSelectViewParam param) {
        try {
            String dbName = param.getDbName();
            String viewName = param.getViewName();
            String sql = """
                    SELECT
                        t.`TABLE_NAME`, 
                        t.`TABLE_COMMENT`,
                        v.`IS_UPDATABLE` AS `UPDATABLE`,
                        v.`CHECK_OPTION` AS `CHECK_OPTION`,
                        v.`VIEW_DEFINITION` AS `DEFINITION`,
                        v.`SECURITY_TYPE` AS `SECURITY_TYPE`
                    FROM
                        information_schema.`TABLES` t
                    LEFT JOIN 
                        information_schema.`VIEWS` v
                    ON 
                        t.`TABLE_NAME` = v.`TABLE_NAME`
                    WHERE
                        t.`TABLE_TYPE` = 'VIEW'
                    AND
                        t.`TABLE_SCHEMA` = ?
                    AND
                        t.`TABLE_NAME` = ?
                    """;
            this.printSql(sql);
            Connection connection = this.getConnManager().connection(dbName);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, dbName);
            statement.setString(2, viewName);
            // 执行SQL查询并获取结果集
            ResultSet resultSet = statement.executeQuery();
            // 打印元数据
            DBUtil.printMetaData(resultSet);
            // 遍历结果集
            MariadbView view = new MariadbView();
            view.setDbName(dbName);
            while (resultSet.next()) {
                String name = resultSet.getString("TABLE_NAME");
                String updatable = resultSet.getString("UPDATABLE");
                String definition = resultSet.getString("DEFINITION");
                String checkOption = resultSet.getString("CHECK_OPTION");
                String securityType = resultSet.getString("SECURITY_TYPE");
                String tableComment = resultSet.getString("TABLE_COMMENT");
                if (param.isFull()) {
                    //Map<String, String> info = ShellMariadbHelper.getViewInfo(connection, dbName, tableName);
                    //view.setDefiner(info.get("DEFINER"));
                    //view.setAlgorithm(info.get("ALGORITHM"));
                    //view.setDefinition(info.get("DEFINITION"));
                    //view.setCheckOption(info.get("CHECK_OPTION"));
                    //view.setSecurityType(info.get("SECURITY_TYPE"));
                    //view.setCreateDefinition(info.get("CREATE_VIEW"));
                    //view.setUpdatable(StringUtil.equalsIgnoreCase("YES", info.get("UPDATABLE")));
                    String createView = this.showCreateView(dbName, name);
                    String[] arr = createView.split(" ");
                    for (String string : arr) {
                        if (StringUtil.startWithIgnoreCase(string, "DEFINER=")) {
                            view.setDefiner(string.substring(8));
                        }
                        if (StringUtil.startWithIgnoreCase(string, "ALGORITHM=")) {
                            view.setAlgorithm(string.substring(10));
                        }
                    }
                    view.setCreateDefinition(createView);
                }
                view.setName(name);
                view.setComment(tableComment);
                view.setDefinition(definition);
                view.setCheckOption(checkOption);
                view.setSecurityType(securityType);
                view.setUpdatable(StringUtil.equalsIgnoreCase("YES", updatable));
            }
            // 关闭连接和释放资源
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            return view;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 查询视图列表
     *
     * @param dbName 数据库
     * @return 结果
     */
    public List<MariadbView> selectViews(String dbName) {
        MariadbSelectViewParam param = new MariadbSelectViewParam();
        param.setFull(true);
        param.setDbName(dbName);
        return this.selectViews(param);
    }

    /**
     * 查询视图列表
     *
     * @param dbName 数据库
     * @return 结果
     */
    public List<MariadbView> selectViewsSimple(String dbName) {
        MariadbSelectViewParam param = new MariadbSelectViewParam();
        param.setFull(false);
        param.setDbName(dbName);
        return this.selectViews(param);
    }

    /**
     * 查询视图列表
     *
     * @param param 参数
     * @return 结果
     */
    public List<MariadbView> selectViews(MariadbSelectViewParam param) {
        try {
            String dbName = param.getDbName();
            List<MariadbView> list = new ArrayList<>();
            String sql = """
                    SELECT
                        t.`TABLE_NAME`, 
                        t.`TABLE_COMMENT`,
                        v.`IS_UPDATABLE` AS `UPDATABLE`,
                        v.`CHECK_OPTION` AS `CHECK_OPTION`,
                        v.`VIEW_DEFINITION` AS `DEFINITION`,
                        v.`SECURITY_TYPE` AS `SECURITY_TYPE`
                    FROM
                        information_schema.`TABLES` t
                    LEFT JOIN 
                        information_schema.`VIEWS` v
                    ON 
                        t.`TABLE_NAME` = v.`TABLE_NAME`
                    AND 
                        t.`TABLE_SCHEMA` = v.`TABLE_SCHEMA`
                    WHERE
                        t.`TABLE_TYPE` = 'VIEW'
                    AND
                        t.`TABLE_SCHEMA` = ?
                    """;
            this.printSql(sql);
            Connection connection = this.getConnManager().connection(dbName);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, dbName);
            // 执行SQL查询并获取结果集
            ResultSet resultSet = statement.executeQuery();
            // 打印元数据
            DBUtil.printMetaData(resultSet);
            // 遍历结果集
            while (resultSet.next()) {
                MariadbView view = new MariadbView();
                String name = resultSet.getString("TABLE_NAME");
                String updatable = resultSet.getString("UPDATABLE");
                String definition = resultSet.getString("DEFINITION");
                String checkOption = resultSet.getString("CHECK_OPTION");
                String tableComment = resultSet.getString("TABLE_COMMENT");
                String securityType = resultSet.getString("SECURITY_TYPE");
                if (param.isFull()) {
                    //Map<String, String> info = ShellMariadbHelper.getViewInfo(connection, dbName, tableName);
                    //view.setDefiner(info.get("DEFINER"));
                    //view.setAlgorithm(info.get("ALGORITHM"));
                    //view.setDefinition(info.get("DEFINITION"));
                    //view.setCheckOption(info.get("CHECK_OPTION"));
                    //view.setSecurityType(info.get("SECURITY_TYPE"));
                    //view.setCreateDefinition(info.get("CREATE_VIEW"));
                    //view.setUpdatable(StringUtil.equalsIgnoreCase("YES", info.get("UPDATABLE")));
                    String createView = this.showCreateView(dbName, name);
                    String[] arr = createView.split(" ");
                    for (String string : arr) {
                        if (StringUtil.startWithIgnoreCase(string, "DEFINER=")) {
                            view.setDefiner(string.substring(8));
                        }
                        if (StringUtil.startWithIgnoreCase(string, "ALGORITHM=")) {
                            view.setAlgorithm(string.substring(10));
                        }
                    }
                    view.setCreateDefinition(createView);
                }
                view.setName(name);
                view.setDbName(dbName);
                view.setComment(tableComment);
                view.setDefinition(definition);
                view.setCheckOption(checkOption);
                view.setSecurityType(securityType);
                view.setUpdatable(StringUtil.equalsIgnoreCase("YES", updatable));
                list.add(view);
            }
            // 关闭连接和释放资源
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            return list;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 删除视图
     *
     * @param dbName 数据库名称
     * @param view   视图
     */
    public void dropView(String dbName, MariadbView view) {
        try {
            String sql = "DROP VIEW IF EXISTS " + DBUtil.wrap(view.getDbName(), view.getName(), this.dialect());
            Statement statement = this.getConnManager().connection(dbName).createStatement();
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
     * @param dbName   数据库名称
     * @param viewName 视图名称
     * @return 是否存在
     */
    public boolean existView(String dbName, String viewName) {
        boolean result;
        try {
            DatabaseMetaData metaData = this.getConnManager().connection(dbName).getMetaData();
            ResultSet resultSet = metaData.getTables(null, dbName, viewName, new String[]{"VIEW"});
            result = resultSet.next();
            IOUtil.close(resultSet);
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
    public void createView(MariadbCreateViewParam param) {
        try {
            Statement statement = this.getConnManager().connection(param.getDbName()).createStatement();
            String sql = MariadbViewCreateSqlGenerator.generateSqlSingle(param);
            this.printSql(sql);
            statement.execute(sql);
            IOUtil.close(statement);
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    /**
     * 修改视图
     *
     * @param param 参数
     */
    public void alertView(MariadbAlertViewParam param) {
        try {
            Statement statement = this.getConnManager().connection(param.getDbName()).createStatement();
            String sql = MariadbViewAlertSqlGenerator.generateSqlSingle(param);
            this.printSql(sql);
            statement.execute(sql);
            IOUtil.close(statement);
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    /**
     * 查询索引
     *
     * @param dbName    数据库
     * @param tableName 表名称
     * @return 结果
     */
    public List<MariadbIndex> selectIndexes(String dbName, String tableName) {
        try {
            Connection connection = this.getConnManager().connection(dbName);
            Statement statement = connection.createStatement();
            String sql = "SHOW INDEX FROM " + DBUtil.wrap(dbName, tableName, this.dialect());
            this.printSql(sql);
            ResultSet resultSet = statement.executeQuery(sql);
            // 打印元数据
            DBUtil.printMetaData(resultSet);
            Map<String, MariadbIndex> indexMap = new HashMap<>();
            while (resultSet.next()) {
                String keyName = resultSet.getString("Key_name");
                // 主键类型的跳过
                if ("Primary".equalsIgnoreCase(keyName)) {
                    continue;
                }
                MariadbIndex tableIndex = indexMap.get(keyName);
                String columnName = resultSet.getString("Column_name");
                if (tableIndex == null) {
                    int noneUnique = resultSet.getInt("Non_unique");
                    int seqInIndex = resultSet.getInt("Seq_in_index");
                    String indexType = resultSet.getString("Index_type");
                    String indexComment = resultSet.getString("Index_comment");
                    tableIndex = new MariadbIndex();
                    tableIndex.setName(keyName);
                    tableIndex.setSeqIndex(seqInIndex);
                    tableIndex.setComment(indexComment);
                    tableIndex.type(indexType, noneUnique);
                    indexMap.put(keyName, tableIndex);
                }
                int subPart = resultSet.getInt("Sub_Part");
                tableIndex.addColumn(columnName, subPart);
            }
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            return new ArrayList<>(indexMap.values());
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    /**
     * 查询检查
     *
     * @param dbName    数据库
     * @param tableName 表名称
     * @return 结果
     */
    public List<MariadbCheck> selectChecks(String dbName, String tableName) {
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
            this.printSql(sql);
            PreparedStatement statement = this.getConnManager().connection(dbName).prepareStatement(sql);
            statement.setString(1, dbName);
            statement.setString(2, tableName);
            ResultSet resultSet = statement.executeQuery();
            DBUtil.printMetaData(resultSet);
            List<MariadbCheck> checks = new ArrayList<>();
            while (resultSet.next()) {
                MariadbCheck check = new MariadbCheck();
                String name = resultSet.getString("NAME");
                String clause = resultSet.getString("CLAUSE");
                check.setName(name);
                check.setClause(clause);
                check.setDbName(dbName);
                check.setTableName(tableName);
                checks.add(check);
            }
            IOUtil.close(resultSet);
            return checks;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 查询外键
     *
     * @param dbName    数据库
     * @param tableName 表名称
     * @return 结果
     */
    public List<MariadbForeignKey> selectForeignKeys(String dbName, String tableName) {
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
            this.printSql(sql);
            PreparedStatement statement = this.getConnManager().connection(dbName).prepareStatement(sql);
            statement.setString(1, dbName);
            statement.setString(2, tableName);
            ResultSet resultSet = statement.executeQuery();
            DBUtil.printMetaData(resultSet);
            Map<String, MariadbForeignKey> foreignKeyMap = new HashMap<>();
            while (resultSet.next()) {
                String fkName = resultSet.getString("FK_NAME");
                String fkColumnName = resultSet.getString("FKCOLUMN_NAME");
                String pkColumnName = resultSet.getString("PKCOLUMN_NAME");
                MariadbForeignKey foreignKey = foreignKeyMap.get(fkName);
                if (foreignKey == null) {
                    String pkTableName = resultSet.getString("PKTABLE_NAME");
                    String pkTableCat = resultSet.getString("PKTABLE_CAT");
                    String updateRule = resultSet.getString("UPDATE_RULE");
                    String deleteRule = resultSet.getString("DELETE_RULE");
                    foreignKey = new MariadbForeignKey();
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
            IOUtil.close(resultSet);
            return new ArrayList<>(foreignKeyMap.values());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 查询视图字段
     *
     * @param dbName   数据库名称
     * @param viewName 视图名称
     * @return 字段列表
     */
    public List<MariadbColumn> viewColumns(String dbName, String viewName) {
        //        try {
        //            if (StringUtil.isBlank(viewName)) {
        //                return Collections.emptyList();
        //            }
        //            String sql = """
        //                    SELECT
        //                        a.EXTRA as COLUMN_EXTRA,
        //                        a.COLUMN_KEY as COLUMN_KEY,
        //                        a.COLUMN_COMMENT as REMARKS,
        //                        a.COLUMN_TYPE as COLUMN_TYPE,
        //                        a.COLUMN_NAME as COLUMN_NAME,
        //                        a.IS_NULLABLE as IS_NULLABLE,
        //                        a.COLUMN_DEFAULT as COLUMN_DEF,
        //                        a.COLLATION_NAME as COLLATION_NAME,
        //                        a.CHARACTER_SET_NAME as CHARSET_NAME,
        //                        a.ORDINAL_POSITION as ORDINAL_POSITION
        //                    FROM
        //                        INFORMATION_SCHEMA.`COLUMNS` a
        //                    WHERE
        //                        a.TABLE_SCHEMA = ?
        //                    AND
        //                        a.TABLE_NAME = ?
        //                    """;
        //            this.printSql(sql);
        //            PreparedStatement statement = this.getConnManager().connection(dbName).prepareStatement(sql);
        //            statement.setString(1, dbName);
        //            statement.setString(2, viewName);
        //            ResultSet resultSet = statement.executeQuery();
        //            // 打印元数据
        //            DBUtil.printMetaData(resultSet);
        //            Map<String, MariadbColumn> columns = new HashMap<>();
        //            while (resultSet.next()) {
        //                Object def = resultSet.getObject("COLUMN_DEF");
        //                String remarks = resultSet.getString("REMARKS");
        //                int position = resultSet.getInt("ORDINAL_POSITION");
        //                String nullable = resultSet.getString("IS_NULLABLE");
        //                // String columnKey = resultSet.getString("COLUMN_KEY");
        //                String columnType = resultSet.getString("COLUMN_TYPE");
        //                String columnName = resultSet.getString("COLUMN_NAME");
        //                String charsetName = resultSet.getString("CHARSET_NAME");
        //                String columnExtra = resultSet.getString("COLUMN_EXTRA");
        //                String collationName = resultSet.getString("COLLATION_NAME");
        //                MariadbColumn column = new MariadbColumn();
        //                column.initColumn(columnType, columnExtra);
        //                column.setDbName(dbName);
        //                column.setName(columnName);
        //                column.setComment(remarks);
        //                column.setDefaultValue(def);
        //                column.setPosition(position);
        //                column.setCharset(charsetName);
        //                column.setTableName(viewName);
        //                column.setCollation(collationName);
        //                column.setNullable("yes".equalsIgnoreCase(nullable));
        //                // column.setPrimaryKey("pri".equalsIgnoreCase(columnKey));
        //                columns.put(columnName, column);
        //            }
        //            IOUtil.close(resultSet);
        //            IOUtil.close(statement);
        //
        //            sql = "SELECT * FROM " + DBUtil.wrap(dbName, viewName, this.dialect()) + " LIMIT 1";
        //            this.printSql(sql);
        //            PreparedStatement statement1 = this.getConnManager().connection(dbName).prepareStatement(sql);
        //            ResultSet resultSet1 = statement1.executeQuery();
        //            DBUtil.printMetaData(resultSet1);
        //            MariadbColumns dbColumns = ShellMariadbHelper.parseColumns(resultSet1);
        //            IOUtil.close(resultSet1);
        //            IOUtil.close(statement1);
        //
        //            // 初始化状态
        //            for (MariadbColumn value : columns.values()) {
        //                MariadbColumn dbColumn = dbColumns.column(value.getName());
        //                if (dbColumn != null) {
        //                    value.setNullable(dbColumn.isNullable());
        //                    value.setAutoIncrement(dbColumn.isAutoIncrement());
        //                }
        //                value.initStatus();
        //            }
        //            // 返回排序后的数据
        //            return CollectionUtil.sort(columns.values(), Comparator.comparingInt(MariadbColumn::getPosition));
        //        } catch (Exception ex) {
        //            ex.printStackTrace();
        //            throw new ShellException(ex);
        //        }
        MariadbSelectColumnParam param = new MariadbSelectColumnParam();
        param.setDbName(dbName);
        param.setTableName(viewName);
        return this.selectColumns(param);
    }

    /**
     * 查询视图记录
     *
     * @param dbName   数据库名称
     * @param viewName 视图名称
     * @param start    起始位置
     * @param limit    限制行数
     * @param filters  过滤条件
     * @return 记录列表
     */
    public List<MariadbRecord> viewRecords(String dbName, String viewName, Long start, Long limit, List<MariadbRecordFilter> filters) {
        try {
            Connection connection = this.getConnManager().connection(dbName);
            StringBuilder builder = new StringBuilder("SELECT * FROM ");
            builder.append(DBUtil.wrap(dbName, viewName, this.dialect()));
            String filterCondition = MariadbConditionUtil.buildCondition(filters);
            if (StringUtil.isNotBlank(filterCondition)) {
                builder.append(" WHERE ").append(filterCondition);
            }
            if (start != null && limit != null) {
                builder.append(" LIMIT ").append(start).append(",").append(limit);
            }
            String sql = builder.toString();
            this.printSql(sql);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            DBUtil.printMetaData(resultSet);
            List<MariadbRecord> records = new ArrayList<>();
            boolean updatable = ShellMariadbHelper.isViewUpdatable(connection, dbName, viewName);
            MariadbColumns columns = ShellMariadbHelper.parseColumns(resultSet);
            while (resultSet.next()) {
                MariadbRecord record = new MariadbRecord(columns, !updatable);
                for (MariadbColumn column : columns) {
                    Object data = resultSet.getObject(column.getName());
                    // 获取几何值
                    if (column.supportGeometry()) {
                        data = ShellMariadbHelper.getGeometryString(connection, data);
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
     * 创建表
     *
     * @param param 参数
     */
    public void createTable(MariadbCreateTableParam param) {
        Connection connection = null;
        try {
            String dbName = param.dbName();
            connection = this.getConnManager().connection(dbName);
            Statement statement = connection.createStatement();
            List<String> sqlList = MariadbTableCreateSqlGenerator.generateSql(param);
            connection.setAutoCommit(false);
            for (String sql : sqlList) {
                this.printSql(sql);
                statement.executeUpdate(sql);
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
     * @param param 参数
     */
    public void alertTable(MariadbAlertTableParam param) {
        Connection connection = null;
        try {
            List<String> sqlList = MariadbTableAlertSqlGenerator.generateSql(param);
            // 无变化
            if (CollectionUtil.isEmpty(sqlList)) {
                return;
            }
            String dbName = param.getTable().getDbName();
            connection = this.getConnManager().connection(dbName);
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();
            for (String sql : sqlList) {
                this.printSql(sql);
                statement.executeUpdate(sql);
            }
            connection.commit();
            IOUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            DBUtil.rollback(connection);
            throw new ShellException(ex);
        }
    }

    //@Deprecated
    //public boolean existTable(String dbName, String tableName) {
    //    boolean result;
    //    try {
    //        DatabaseMetaData metaData = this.getConnManager().connection(dbName).getMetaData();
    //        ResultSet resultSet = metaData.getTables(null, dbName, tableName, TABLE_TYPES);
    //        DBUtil.printMetaData(resultSet);
    //        result = resultSet.next();
    //        IOUtil.close(resultSet);
    //    } catch (Exception ex) {
    //        throw new ShellException(ex);
    //    }
    //    return result;
    //}

    /**
     * 重命名表
     *
     * @param dbName       数据库名称
     * @param oldTableName 原表名称
     * @param newTableName 新表名称
     */
    public void renameTable(String dbName, String oldTableName, String newTableName) {
        try {
            StringBuilder builder = new StringBuilder("RENAME TABLE ");
            builder.append(DBUtil.wrap(dbName, oldTableName, this.dialect()))
                    .append(" TO ")
                    .append(DBUtil.wrap(dbName, newTableName, this.dialect()));
            String sql = builder.toString();
            Connection connection = this.getConnManager().connection(dbName);
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
     * @param dbName      数据库名称
     * @param oldViewName 原视图名称
     * @param newViewName 新视图名称
     */
    public void renameView(String dbName, String oldViewName, String newViewName) {
        this.renameTable(dbName, oldViewName, newViewName);
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
            builder.append(DBUtil.wrap(dbName, oldEventName, this.dialect()))
                    .append(" RENAME TO ")
                    .append(DBUtil.wrap(dbName, newEventName, this.dialect()));
            String sql = builder.toString();
            this.printSql(sql);
            Connection connection = this.getConnManager().connection(dbName);
            Statement statement = connection.createStatement();
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
     * @param dbName          库名称
     * @param oldFunctionName 函数名称
     * @param newFunctionName 新函数名称
     */
    public void renameFunction(String dbName, String oldFunctionName, String newFunctionName) {
        try {
            this.cloneFunction(dbName, oldFunctionName, newFunctionName);
            MariadbFunction mariadbFunction = new MariadbFunction();
            mariadbFunction.setDbName(dbName);
            mariadbFunction.setName(oldFunctionName);
            this.dropFunction(dbName, mariadbFunction);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 重命名过程
     *
     * @param dbName           库名称
     * @param oldProcedureName 过程名称
     * @param newProcedureName 新过程名称
     */
    public void renameProcedure(String dbName, String oldProcedureName, String newProcedureName) {
        try {
            this.cloneProcedure(dbName, oldProcedureName, newProcedureName);
            MariadbProcedure mariadbProcedure = new MariadbProcedure();
            mariadbProcedure.setDbName(dbName);
            mariadbProcedure.setName(oldProcedureName);
            this.dropProcedure(dbName, mariadbProcedure);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 清空表数据
     *
     * @param dbName    数据库名称
     * @param tableName 表名称
     */
    public void clearTable(String dbName, String tableName) {
        try {
            Statement statement = this.getConnManager().connection(dbName).createStatement();
            String sql = "DELETE FROM " + DBUtil.wrap(dbName, tableName, this.dialect());
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
     * @param dbName    数据库名称
     * @param tableName 表名称
     */
    public void truncateTable(String dbName, String tableName) {
        try {
            Statement statement = this.getConnManager().connection(dbName).createStatement();
            String sql = "TRUNCATE TABLE " + DBUtil.wrap(dbName, tableName, this.dialect());
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
     * @param dbName    数据库名称
     * @param tableName 表名称
     */
    public void dropTable(String dbName, String tableName) {
        try {
            Statement statement = this.getConnManager().connection(dbName).createStatement();
            String sql = "DROP TABLE " + DBUtil.wrap(dbName, tableName, this.dialect());
            this.printSql(sql);
            statement.executeUpdate(sql);
            IOUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 查询支持的字符集列表
     *
     * @return 字符集列表
     */
    public List<String> charsets() {
        if (this.hasProperty("charsets")) {
            return this.getProperty("charsets");
        }
        try {
            List<String> charsets = new ArrayList<>();
            Statement statement = this.getConnManager().connection().createStatement();
            String sql = """
                    SELECT
                        CHARACTER_SET_NAME
                    FROM
                        INFORMATION_SCHEMA.CHARACTER_SETS;
                    """;
            this.printSql(sql);
            ResultSet resultSet = statement.executeQuery(sql);
            DBUtil.printMetaData(resultSet);
            while (resultSet.next()) {
                charsets.add(resultSet.getString(1));
            }
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            this.putProperty("charsets", charsets);
            return charsets;
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    /**
     * 查询指定字符集支持的排序规则列表
     *
     * @param charset 字符集
     * @return 排序规则列表
     */
    public List<String> collation(String charset) {
        try {
            Map<String, List<String>> collations = this.getProperty("collation");
            if (collations == null) {
                collations = new HashMap<>();
                this.putProperty("collation", collations);
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
            this.printSql(sql);
            PreparedStatement statement = this.getConnManager().connection().prepareStatement(sql);
            statement.setString(1, charset);
            ResultSet resultSet = statement.executeQuery();
            DBUtil.printMetaData(resultSet);
            List<String> list = new ArrayList<>();
            while (resultSet.next()) {
                list.add(resultSet.getString(1));
            }
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            collations.put(charset, list);
            return list;
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    /**
     * 判断数据库是否存在
     *
     * @param dbName 数据库名称
     * @return 是否存在
     */
    public boolean existDatabase(String dbName) {
        boolean result = false;
        try {
            DatabaseMetaData metaData = this.getConnManager().connection().getMetaData();
            // 执行查询操作，检查数据库是否存在
            ResultSet resultSet = metaData.getCatalogs();
            while (resultSet.next()) {
                String catalogName = resultSet.getString("TABLE_CAT");
                if (catalogName.equals(dbName)) {
                    result = true;
                    break;
                }
            }
            IOUtil.close(resultSet);
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
        return result;
    }

    /**
     * 创建数据库
     *
     * @param database 数据库
     */
    public void createDatabase(MariadbDatabase database) {
        try {
            StringBuilder builder = new StringBuilder("CREATE DATABASE ");
            builder.append(DBUtil.wrap(database.getName(), this.dialect()));
            if (StringUtil.isNotBlank(database.getCharset())) {
                builder.append(" CHARACTER SET ").append(DBUtil.wrapData(database.getCharset(), DBDialect.MARIADB));
            }
            if (StringUtil.isNotBlank(database.getCollation())) {
                builder.append(" COLLATE ").append(DBUtil.wrapData(database.getCollation(), DBDialect.MARIADB));
            }
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
     * 修改数据库
     *
     * @param database 数据库
     * @return 是否修改成功
     */
    public boolean alterDatabase(MariadbDatabase database) {
        try {
            // 无变化
            if (database.getCharset() == null && database.getCollation() == null) {
                return true;
            }
            StringBuilder builder = new StringBuilder("ALTER DATABASE ").append(DBUtil.wrap(database.getName(), this.dialect()));
            if (database.getCharset() != null) {
                builder.append(" CHARACTER SET ").append(DBUtil.wrapData(database.getCharset(), DBDialect.MARIADB));
            }
            if (database.getCollation() != null) {
                builder.append(" COLLATE ").append(DBUtil.wrapData(database.getCollation(), DBDialect.MARIADB));
            }
            String sql = builder.toString();
            this.printSql(sql);
            Statement statement = this.getConnManager().connection().createStatement();
            statement.execute(sql);
            IOUtil.close(statement);
            return true;
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    /**
     * 查询数据库的默认排序规则
     *
     * @param dbName 数据库名称
     * @return 排序规则
     */
    public String databaseCollation(String dbName) {
        String collation = this.getProperty("collation_" + dbName);
        if (StringUtil.isNotBlank(collation)) {
            return collation;
        }
        try {
            String sql = """
                    SELECT
                        DEFAULT_COLLATION_NAME
                    FROM
                        information_schema.SCHEMATA
                    WHERE
                        SCHEMA_NAME = ?;
                    """;
            this.printSql(sql);
            PreparedStatement statement = this.getConnManager().connection().prepareStatement(sql);
            statement.setString(1, dbName);
            ResultSet resultSet = statement.executeQuery();
            DBUtil.printMetaData(resultSet);
            while (resultSet.next()) {
                collation = resultSet.getString(1);
            }
            IOUtil.close(resultSet);
            IOUtil.close(statement);
        } catch (Exception ex) {
            throw new ShellException(ex);
        } finally {
            this.putProperty("collation_" + dbName, collation);
        }
        return collation;
    }

    /**
     * 删除数据库
     *
     * @param dbName 数据库名称
     * @return 是否删除成功
     */
    public boolean dropDatabase(String dbName) {
        try {
            String sql = "DROP DATABASE IF EXISTS " + DBUtil.wrap(dbName, this.dialect());
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
     * 执行计划sql
     *
     * @param dbName 数据库
     * @param sql    sql
     * @return 结果
     */
    public DBQueryResults<ShellMariadbExplainResult> explainSql(String dbName, String sql) {
        DBQueryResults<ShellMariadbExplainResult> results = new DBQueryResults<>();
        Connection connection = null;
        try {
            this.printSql(sql);
            DBSqlParser parser = DBSqlParser.getParser(sql, this.dialect());
            List<String> list = parser.parseSql();
            connection = this.getConnManager().connection(dbName);
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();
            for (String execSql : list) {
                ShellMariadbExplainResult result = new ShellMariadbExplainResult();
                try {
                    execSql = "EXPLAIN " + execSql.stripLeading();
                    result.setContent(execSql);
                    long startTime = System.nanoTime();
                    ResultSet resultSet = statement.executeQuery(execSql);
                    result.parseResult(resultSet, connection);
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
            JulLog.warn("sql:\n{}", sql);
            ex.printStackTrace();
            DBUtil.rollback(connection);
            results.parseError(ex);
        }
        return results;
    }

    /**
     * 执行单个sql
     *
     * @param dbName 数据库名称
     * @param sql    sql
     * @return 结果
     */
    public ShellMariadbExecuteResult executeSingleSql(String dbName, String sql) {
        Connection connection = null;
        ShellMariadbExecuteResult result = new ShellMariadbExecuteResult();
        result.setContent(sql);
        try {
            this.printSql(sql);
            DBSqlParser parser = DBSqlParser.getParser(sql, this.dialect());
            String execSql = parser.parseSingleSql();
            connection = this.getConnManager().connection(dbName);
            Statement statement = connection.createStatement();
            try {
                long startTime = System.nanoTime();
                boolean isQuery = statement.execute(execSql);
                if (isQuery) {
                    ResultSet resultSet = statement.getResultSet();
                    result.setFullColumn(parser.isFullColumn(execSql));
                    result.parseResult(resultSet, connection, !parser.isSelect(execSql));
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
            JulLog.warn("sql:\n{}", sql);
            ex.printStackTrace();
            DBUtil.rollback(connection);
        }
        return result;
    }

    /**
     * 简单执行sql
     *
     * @param dbName 数据库名称
     * @param sql    sql
     */
    public void executeSqlSimple(String dbName, String sql) {
        Connection connection = null;
        try {
            this.printSql(sql);
            connection = this.getConnManager().connection(dbName);
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();
            statement.execute(sql);
            connection.commit();
            IOUtil.close(statement);
        } catch (Exception ex) {
            JulLog.warn("sql:\n{}", sql);
            ex.printStackTrace();
            DBUtil.rollback(connection);
            throw new ShellException(ex);
        }
    }

    /**
     * 批量插入
     *
     * @param dbName        数据库名称
     * @param sqlList       sql列表
     * @param newConnection 是否使用新连接
     * @return 结果
     */
    public int insertBatch(String dbName, List<String> sqlList, boolean newConnection) {
        Connection connection = null;
        int result = 0;
        try {
            connection = newConnection ? this.getConnManager().newConnection(dbName) : this.getConnManager().connection(dbName);
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();
            for (String sql : sqlList) {
                this.printSql(sql);
                statement.addBatch(sql);
            }
            int[] results = statement.executeBatch();
            connection.commit();
            IOUtil.close(statement);
            // 新连接需要立刻释放
            if (newConnection) {
                IOUtil.close(connection);
            }
            for (int i : results) {
                result += i;
            }
        } catch (Exception ex) {
            JulLog.warn("sql:\n{}", sqlList);
            ex.printStackTrace();
            DBUtil.rollback(connection);
            throw new ShellException(ex);
        }
        return result;
    }

    @Override
    public DBDialect dialect() {
        return DBDialect.MARIADB;
    }

    /**
     * 查询函数
     *
     * @param dbName       数据库
     * @param functionName 函数名
     * @return 结果
     */
    public MariadbFunction selectFunction(String dbName, String functionName) {
        MariadbSelectFunctionParam param = new MariadbSelectFunctionParam();
        param.setFull(true);
        param.setDbName(dbName);
        param.setFunctionName(functionName);
        return this.selectFunction(param);
    }

    /**
     * 查询函数
     *
     * @param dbName       数据库
     * @param functionName 函数名
     * @return 结果
     */
    public MariadbFunction selectFunctionSimple(String dbName, String functionName) {
        MariadbSelectFunctionParam param = new MariadbSelectFunctionParam();
        param.setFull(false);
        param.setDbName(dbName);
        param.setFunctionName(functionName);
        return this.selectFunction(param);
    }

    /**
     * 查询函数
     *
     * @param param 参数
     * @return 结果
     */
    public MariadbFunction selectFunction(MariadbSelectFunctionParam param) {
        try {
            String dbName = param.getDbName();
            String functionName = param.getFunctionName();
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
            this.printSql(sql);
            PreparedStatement statement = this.getConnManager().functionConnection(dbName).prepareStatement(sql);
            statement.setString(1, dbName);
            statement.setString(2, functionName);
            // 执行SQL查询并获取结果集
            ResultSet resultSet = statement.executeQuery();
            // 打印元数据
            DBUtil.printMetaData(resultSet);
            MariadbFunction function = new MariadbFunction();
            function.setDbName(dbName);
            function.setName(functionName);
            // 遍历结果集
            while (resultSet.next()) {
                String securityType = resultSet.getString("SECURITY_TYPE");
                String definition = resultSet.getString("ROUTINE_DEFINITION");
                String sqlDataAccess = resultSet.getString("SQL_DATA_ACCESS");
                if (param.isFull()) {
                    List<MariadbRoutineParam> params = this.listFunctionParam(dbName, functionName);
                    function.setParams(params);
                    String createDefinition = this.showCreateFunction(dbName, functionName);
                    function.setCreateDefinition(createDefinition);
                }
                function.setDbName(dbName);
                function.setDefinition(definition);
                function.setSecurityType(securityType);
                function.setCharacteristic(sqlDataAccess);
            }
            // 关闭连接和释放资源
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            return function;
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    /**
     * 查询函数列表
     *
     * @param dbName 数据库
     * @return 结果
     */
    public List<MariadbFunction> selectFunctions(String dbName) {
        MariadbSelectFunctionParam param = new MariadbSelectFunctionParam();
        param.setFull(true);
        param.setDbName(dbName);
        return this.selectFunctions(param);
    }

    /**
     * 查询函数列表
     *
     * @param dbName 数据库
     * @return 结果
     */
    public List<MariadbFunction> selectFunctionsSimple(String dbName) {
        MariadbSelectFunctionParam param = new MariadbSelectFunctionParam();
        param.setFull(false);
        param.setDbName(dbName);
        return this.selectFunctions(param);
    }

    /**
     * 查询函数列表
     *
     * @param param 参数
     * @return 结果
     */
    public List<MariadbFunction> selectFunctions(MariadbSelectFunctionParam param) {
        try {
            String dbName = param.getDbName();
            List<MariadbFunction> list = new ArrayList<>();
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
            this.printSql(sql);
            PreparedStatement statement = this.getConnManager().functionConnection(dbName).prepareStatement(sql);
            statement.setString(1, dbName);
            // 执行SQL查询并获取结果集
            ResultSet resultSet = statement.executeQuery();
            // 打印元数据
            DBUtil.printMetaData(resultSet);
            // 遍历结果集
            while (resultSet.next()) {
                MariadbFunction function = new MariadbFunction();
                String name = resultSet.getString("ROUTINE_NAME");
                String securityType = resultSet.getString("SECURITY_TYPE");
                String definition = resultSet.getString("ROUTINE_DEFINITION");
                String sqlDataAccess = resultSet.getString("SQL_DATA_ACCESS");
                if (param.isFull()) {
                    List<MariadbRoutineParam> params = this.listFunctionParam(dbName, name);
                    function.setParams(params);
                    String createDefinition = this.showCreateFunction(dbName, name);
                    function.setCreateDefinition(createDefinition);
                }
                function.setName(name);
                function.setDbName(dbName);
                function.setDefinition(definition);
                function.setSecurityType(securityType);
                function.setCharacteristic(sqlDataAccess);
                list.add(function);
            }
            // 关闭连接和释放资源
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            return list;
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    /**
     * 查询过程
     *
     * @param dbName      数据库
     * @param produceName 过程名
     * @return 结果
     */
    public MariadbProcedure selectProcedure(String dbName, String produceName) {
        MariadbSelectProcedureParam param = new MariadbSelectProcedureParam();
        param.setFull(true);
        param.setDbName(dbName);
        param.setProcedureName(produceName);
        return this.selectProcedure(param);
    }

    /**
     * 查询过程
     *
     * @param dbName      数据库
     * @param produceName 过程名
     * @return 结果
     */
    public MariadbProcedure selectProcedureSimple(String dbName, String produceName) {
        MariadbSelectProcedureParam param = new MariadbSelectProcedureParam();
        param.setFull(false);
        param.setDbName(dbName);
        param.setProcedureName(produceName);
        return this.selectProcedure(param);
    }

    /**
     * 查询过程
     *
     * @param param 参数
     * @return 结果
     */
    public MariadbProcedure selectProcedure(MariadbSelectProcedureParam param) {
        try {
            String dbName = param.getDbName();
            String procedureName = param.getProcedureName();
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
            this.printSql(sql);
            PreparedStatement statement = this.getConnManager().procedureConnection(dbName).prepareStatement(sql);
            statement.setString(1, dbName);
            statement.setString(2, procedureName);
            // 执行SQL查询并获取结果集
            ResultSet resultSet = statement.executeQuery();
            // 打印元数据
            DBUtil.printMetaData(resultSet);
            MariadbProcedure procedure = new MariadbProcedure();
            procedure.setDbName(dbName);
            procedure.setName(procedureName);
            // 遍历结果集
            while (resultSet.next()) {
                String securityType = resultSet.getString("SECURITY_TYPE");
                String definition = resultSet.getString("ROUTINE_DEFINITION");
                String sqlDataAccess = resultSet.getString("SQL_DATA_ACCESS");
                procedure.setDbName(dbName);
                if (param.isFull()) {
                    List<MariadbRoutineParam> params = this.listProcedureParam(dbName, procedureName);
                    procedure.setParams(params);
                    String createDefinition = this.showCreateProcedure(dbName, procedureName);
                    procedure.setCreateDefinition(createDefinition);
                }
                procedure.setDefinition(definition);
                procedure.setSecurityType(securityType);
                procedure.setCharacteristic(sqlDataAccess);
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
     * 查询过程列表
     *
     * @param dbName 数据库
     * @return 结果
     */
    public List<MariadbProcedure> selectProcedures(String dbName) {
        MariadbSelectProcedureParam param = new MariadbSelectProcedureParam();
        param.setFull(true);
        param.setDbName(dbName);
        return this.selectProcedures(param);
    }

    /**
     * 查询过程列表
     *
     * @param dbName 数据库
     * @return 结果
     */
    public List<MariadbProcedure> selectProceduresSimple(String dbName) {
        MariadbSelectProcedureParam param = new MariadbSelectProcedureParam();
        param.setFull(false);
        param.setDbName(dbName);
        return this.selectProcedures(param);
    }

    /**
     * 查询过程列表
     *
     * @param param 参数
     * @return 结果
     */
    public List<MariadbProcedure> selectProcedures(MariadbSelectProcedureParam param) {
        try {
            String dbName = param.getDbName();
            List<MariadbProcedure> list = new ArrayList<>();
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
            this.printSql(sql);
            PreparedStatement statement = this.getConnManager().procedureConnection(dbName).prepareStatement(sql);
            statement.setString(1, dbName);
            // 执行SQL查询并获取结果集
            ResultSet resultSet = statement.executeQuery();
            // 打印元数据
            DBUtil.printMetaData(resultSet);
            // 遍历结果集
            while (resultSet.next()) {
                MariadbProcedure procedure = new MariadbProcedure();
                String name = resultSet.getString("ROUTINE_NAME");
                String securityType = resultSet.getString("SECURITY_TYPE");
                String definition = resultSet.getString("ROUTINE_DEFINITION");
                String sqlDataAccess = resultSet.getString("SQL_DATA_ACCESS");
                if (param.isFull()) {
                    List<MariadbRoutineParam> params = this.listProcedureParam(dbName, name);
                    procedure.setParams(params);
                    String createDefinition = this.showCreateProcedure(dbName, name);
                    procedure.setCreateDefinition(createDefinition);
                }
                procedure.setName(name);
                procedure.setDbName(dbName);
                procedure.setDefinition(definition);
                procedure.setSecurityType(securityType);
                procedure.setCharacteristic(sqlDataAccess);
                list.add(procedure);
            }
            // 关闭连接和释放资源
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            return list;
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    /**
     * 删除过程
     *
     * @param dbName  数据库名称
     * @param routine 过程
     */
    public void dropProcedure(String dbName, MariadbProcedure routine) {
        try {
            String sql = "DROP PROCEDURE IF EXISTS " + DBUtil.wrap(dbName, routine.getName(), this.dialect());
            this.printSql(sql);
            Statement statement = this.getConnManager().procedureConnection(dbName).createStatement();
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
    public void createProcedure(MariadbCreateProcedureParam param) {
        try {
            String sql = MariadbProcedureCreateSqlGenerator.generateSqlSingle(param);
            this.printSql(sql);
            Statement statement = this.getConnManager().procedureConnection(param.getDbName()).createStatement();
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
    public void alertProcedure(MariadbAlertProcedureParam param) {
        Connection connection = null;
        try {
            connection = this.getConnManager().procedureConnection(param.getDbName());
            connection.setAutoCommit(false);
            List<String> list = MariadbProcedureAlertSqlGenerator.generateSql(param);
            Statement statement = connection.createStatement();
            for (String sql : list) {
                this.printSql(sql);
                statement.executeUpdate(sql);
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
     * 删除函数
     *
     * @param dbName   数据库名称
     * @param function 函数
     */
    public void dropFunction(String dbName, MariadbFunction function) {
        try {
            String sql = "DROP function IF EXISTS " + DBUtil.wrap(dbName, function.getName(), this.dialect());
            this.printSql(sql);
            Statement statement = this.getConnManager().functionConnection(dbName).createStatement();
            statement.executeUpdate(sql);
            IOUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    // public MariadbFunction selectFunction(String dbName, String functionName) {
    //     try {
    //         String sql = """
    //                 SELECT
    //                     `SECURITY_TYPE`,
    //                     `SQL_DATA_ACCESS`,
    //                     `ROUTINE_DEFINITION`
    //                 FROM
    //                     `INFORMATION_SCHEMA`.`ROUTINES`
    //                 WHERE
    //                     `ROUTINE_SCHEMA` = ?
    //                 AND
    //                     `ROUTINE_NAME` = ?
    //                 AND
    //                     `ROUTINE_TYPE` = 'FUNCTION'
    //                 """;
    //         this.printSql(sql);
    //         PreparedStatement statement = this.getConnManager().connection().prepareStatement(sql);
    //         statement.setString(1, dbName);
    //         statement.setString(2, functionName);
    //         // 执行SQL查询并获取结果集
    //         ResultSet resultSet = statement.executeQuery();
    //         // 打印元数据
    //         DBUtil.printMetaData(resultSet);
    //         MariadbFunction function = new MariadbFunction();
    //         function.setDbName(dbName);
    //         function.setName(functionName);
    //         // 遍历结果集
    //         while (resultSet.next()) {
    //             String securityType = resultSet.getString("SECURITY_TYPE");
    //             String definition = resultSet.getString("ROUTINE_DEFINITION");
    //             String sqlDataAccess = resultSet.getString("SQL_DATA_ACCESS");
    //             List<MariadbRoutineParam> params = ShellMariadbHelper.listFunctionParam(this.getConnManager().connection(), dbName, functionName);
    //             String createDefinition = this.showCreateFunction(dbName, functionName);
    //             function.setDbName(dbName);
    //             function.setParams(params);
    //             function.setDefinition(definition);
    //             function.setSecurityType(securityType);
    //             function.setCharacteristic(sqlDataAccess);
    //             function.setCreateDefinition(createDefinition);
    //         }
    //         // 关闭连接和释放资源
    //         IOUtil.close(resultSet);
    //         IOUtil.close(statement);
    //         return function;
    //     } catch (Exception ex) {
    //         ex.printStackTrace();
    //         throw new ShellException(ex);
    //     }
    // }

    /**
     * 创建函数
     *
     * @param param 参数
     */
    public void createFunction(MariadbCreateFunctionParam param) {
        try {
            String sql = MariadbFunctionCreateSqlGenerator.generateSqlSingle(param);
            this.printSql(sql);
            Statement statement = this.getConnManager().functionConnection(param.getDbName()).createStatement();
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
     * @param param 参数
     * @return 记录
     */
    public MariadbRecord selectRecord(MariadbSelectRecordParam param) {
        try {

            String dbName = param.getDbName();
            String tableName = param.getTableName();
            Connection connection = this.getConnManager().connection(dbName);
            MariadbRecordPrimaryKey primaryKey = param.getPrimaryKey();
            StringBuilder builder = new StringBuilder("SELECT * FROM ");
            builder.append(DBUtil.wrap(dbName, tableName, this.dialect()))
                    .append(" WHERE ")
                    .append(DBUtil.wrap(primaryKey.getColumnName(), this.dialect()))
                    .append(" = ?");
            String sql = builder.toString();
            this.printSql(sql);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setObject(1, primaryKey.data());
            ResultSet resultSet = statement.executeQuery();
            DBUtil.printMetaData(resultSet);
            MariadbColumns columns = ShellMariadbHelper.parseColumns(resultSet);
            MariadbRecord record = new MariadbRecord(columns);
            while (resultSet.next()) {
                for (MariadbColumn column : columns) {
                    Object data = resultSet.getObject(column.getName());
                    // 获取几何值
                    if (column.supportGeometry()) {
                        data = ShellMariadbHelper.getGeometryString(connection, data);
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
     * @return 客户端字符集
     */
    public String selectClientCharacter() {
        String character = "";
        try {
            Connection conn = this.getConnManager().connection();
            Statement stmt = conn.createStatement();
            ResultSet resultSet = stmt.executeQuery("SHOW VARIABLES LIKE 'character_set_%'");
            while (resultSet.next()) {
                String name = resultSet.getString(1);
                if ("character_set_client".equalsIgnoreCase(name)) {
                    character = resultSet.getString(2);
                    break;
                }
            }
            IOUtil.close(resultSet);
            IOUtil.close(stmt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return character;
    }

    /**
     * 判断表是否存在主键
     *
     * @param dbName    数据库名称
     * @param tableName 表名称
     * @return 是否存在主键
     */
    public boolean existPrimaryKey(String dbName, String tableName) {
        try {
            Connection connection = this.getConnManager().connection(dbName);
            String sql = "SHOW INDEX FROM "
                    + DBUtil.wrap(dbName, tableName, this.dialect())
                    + " WHERE Key_name = 'PRIMARY'";
            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet resultSet = stmt.executeQuery();
            DBUtil.printMetaData(resultSet);
            boolean exist = resultSet.next();
            IOUtil.close(resultSet);
            IOUtil.close(stmt);
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
     * @param newTableName  新表名称
     * @param includeRecord 是否包含数据
     */
    public void cloneTable(String dbName, String tableName, String newTableName, boolean includeRecord) {
        // // 查询表
        // MariadbSelectTableParam selectTableParam = new MariadbSelectTableParam();
        // selectTableParam.setFull(true);
        // selectTableParam.setDbName(dbName);
        // selectTableParam.setTableName(tableName);
        // MariadbTable table = this.selectTable(selectTableParam);
        // 查询检查
        List<MariadbCheck> checks = this.selectChecks(dbName, tableName);
        // // 查询索引
        // DBObjects<MariadbIndex> indexes = this.indexes(dbName, tableName);
        // 查询触发器
        List<MariadbTrigger> triggers = this.selectTriggers(dbName, tableName);
        // 查询外键
        List<MariadbForeignKey> foreignKeys = this.selectForeignKeys(dbName, tableName);
        // // 查询字段
        // MariadbSelectColumnParam selectColumnParam = new MariadbSelectColumnParam();
        // selectColumnParam.setDbName(dbName);
        // selectColumnParam.setTableName(tableName);
        // MariadbColumns columns = this.selectColumns(selectColumnParam);
        if (checks != null) {
            for (MariadbCheck check : checks) {
                check.setName(check.getName() + DBUtil.genCloneName());
                check.clearStatus();
                check.clearOriginalData();
                check.setCreated(true);
            }
        }
        if (triggers != null) {
            for (MariadbTrigger trigger : triggers) {
                trigger.setName(trigger.getName() + DBUtil.genCloneName());
                trigger.clearStatus();
                trigger.clearOriginalData();
                trigger.setCreated(true);
            }
        }
        // if (indexes != null) {
        //     for (MariadbIndex index : indexes) {
        //         index.setName(index.getName() + ShellMariadbUtil.genCloneName());
        //     }
        // }
        if (foreignKeys != null) {
            for (MariadbForeignKey foreignKey : foreignKeys) {
                foreignKey.setName(foreignKey.getName() + DBUtil.genCloneName());
                foreignKey.clearStatus();
                foreignKey.clearOriginalData();
                foreignKey.setCreated(true);
            }
        }
        // table.setName(table.getName() + ShellMariadbUtil.genCloneName());
        // // 创建表
        // MariadbCreateTableParam createTableParam = new MariadbCreateTableParam();
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
        //         MariadbSelectRecordParam selectRecordParam = new MariadbSelectRecordParam();
        //         selectRecordParam.setDbName(dbName);
        //         selectRecordParam.setTableName(tableName);
        //         selectRecordParam.setStart(start);
        //         selectRecordParam.setLimit(limit);
        //         List<MariadbRecord> records = this.selectRecords(selectRecordParam);
        //         // 插入记录
        //         if (CollectionUtil.isNotEmpty(records)) {
        //             for (MariadbRecord record : records) {
        //                 MariadbInsertRecordParam insertRecordParam = ShellMariadbUtil.toInsertRecord(columns, record);
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
        // String newTableName = tableName + ShellMariadbUtil.genCloneName();
        try {
            Connection connection = this.getConnManager().connection(dbName);

            // 克隆基本的表结构
            String sql = "CREATE TABLE " + DBUtil.wrap(dbName, newTableName, this.dialect())
                    + " LIKE " + DBUtil.wrap(dbName, tableName, this.dialect());
            this.printSql(sql);
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.execute();
            IOUtil.close(stmt);
            MariadbTable table = new MariadbTable();
            table.setDbName(dbName);
            table.setName(newTableName);

            // 克隆表结构的检查、外键、触发器
            MariadbAlertTableParam alertTableParam = new MariadbAlertTableParam();
            alertTableParam.setTable(table);
            alertTableParam.setChecks(DBObjects.of(checks));
            alertTableParam.setTriggers(DBObjects.of(triggers));
            alertTableParam.setForeignKeys(DBObjects.of(foreignKeys));
            this.alertTable(alertTableParam);

            // 克隆数据
            if (includeRecord) {
                sql = "INSERT INTO " + DBUtil.wrap(dbName, newTableName, this.dialect())
                        + " SELECT * FROM " + DBUtil.wrap(dbName, tableName, this.dialect());
                this.printSql(sql);
                stmt = connection.prepareStatement(sql);
                stmt.execute();
                IOUtil.close(stmt);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 克隆视图
     *
     * @param dbName      数据库
     * @param viewName    视图名称
     * @param newViewName 新视图名称
     */
    public void cloneView(String dbName, String viewName, String newViewName) {
        String sql = this.showCreateView(dbName, viewName);
        try {
            sql = sql.replace("VIEW `" + viewName + "`", "VIEW `" + newViewName + "`");
            this.printSql(sql);
            Connection connection = this.getConnManager().connection(dbName);
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
     * @param dbName          数据库
     * @param functionName    函数名称
     * @param newFunctionName 新函数名称
     */
    public void cloneFunction(String dbName, String functionName, String newFunctionName) {
        String sql = this.showCreateFunction(dbName, functionName);
        try {
            sql = sql.replace("FUNCTION `" + functionName + "`", "FUNCTION `" + newFunctionName + "`");
            this.printSql(sql);
            Connection connection = this.getConnManager().functionConnection(dbName);
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
     * @param dbName           数据库
     * @param procedureName    过程名称
     * @param newProcedureName 新过程名称
     */
    public void cloneProcedure(String dbName, String procedureName, String newProcedureName) {
        String sql = this.showCreateProcedure(dbName, procedureName);
        try {
            sql = sql.replace("PROCEDURE `" + procedureName + "`", "PROCEDURE `" + newProcedureName + "`");
            this.printSql(sql);
            Connection connection = this.getConnManager().procedureConnection(dbName);
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.execute();
            IOUtil.close(stmt);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 克隆事件
     *
     * @param dbName       数据库
     * @param eventName    事件名称
     * @param newEventName 新事件名称
     */
    public void cloneEvent(String dbName, String eventName, String newEventName) {
        String sql = this.showCreateEvent(dbName, eventName);
        try {
            sql = sql.replace("EVENT `" + eventName + "`", "EVENT `" + newEventName + "`");
            this.printSql(sql);
            Connection connection = this.getConnManager().connection(dbName);
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
     * @param dbName      数据库名称
     * @param routineName 例程名称
     * @param routineType 例程类型
     * @return 例程参数列表
     * @throws Exception 查询异常
     */
    public List<MariadbRoutineParam> listRoutineParam(String dbName, String routineName, String routineType) throws Exception {
        try {
            Connection connection = this.getConnManager().connection(dbName);
            String sql = """
                    SELECT
                    	`DATA_TYPE`,
                    	`COLLATION_NAME`,
                    	`DTD_IDENTIFIER`,
                    	`PARAMETER_MODE`,
                    	`PARAMETER_NAME`,
                    	`CHARACTER_SET_NAME`
                    FROM
                    	INFORMATION_SCHEMA.PARAMETERS
                    WHERE
                    	ROUTINE_TYPE = ?
                    AND
                        SPECIFIC_SCHEMA = ?
                    AND
                        SPECIFIC_NAME = ?
                    """;
            List<MariadbRoutineParam> params = new ArrayList<>();
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, routineType);
            statement.setString(2, dbName);
            statement.setString(3, routineName);
            // 执行SQL查询并获取结果集
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                MariadbRoutineParam param = new MariadbRoutineParam();
                // param.setType(resultSet.getString("DATA_TYPE"));
                param.setName(resultSet.getString("PARAMETER_NAME"));
                param.setMode(resultSet.getString("PARAMETER_MODE"));
                param.setCollation(resultSet.getString("COLLATION_NAME"));
                param.setCharset(resultSet.getString("CHARACTER_SET_NAME"));
                param.setDtdIdentifier(resultSet.getString("DTD_IDENTIFIER"));
                params.add(param);
            }
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            return params;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Collections.emptyList();
    }

    /**
     * 查询函数参数列表
     *
     * @param dbName       数据库名称
     * @param functionName 函数名称
     * @return 函数参数列表
     * @throws Exception 查询异常
     */
    public List<MariadbRoutineParam> listFunctionParam(String dbName, String functionName) throws Exception {
        return listRoutineParam(dbName, functionName, "FUNCTION");
    }

    /**
     * 查询过程参数列表
     *
     * @param dbName        数据库名称
     * @param procedureName 过程名称
     * @return 过程参数列表
     * @throws Exception 查询异常
     */
    public List<MariadbRoutineParam> listProcedureParam(String dbName, String procedureName) throws Exception {
        return listRoutineParam(dbName, procedureName, "PROCEDURE");
    }

    /**
     * 打印sql
     *
     * @param sql sql
     */
    private void printSql(String sql) {
        DBUtil.printSql(sql);
        String compressedSql = DBSqlParser.compressSql(sql, this.dialect());
        ShellEventUtil.printSql(compressedSql, this.shellConnect);
    }
}
