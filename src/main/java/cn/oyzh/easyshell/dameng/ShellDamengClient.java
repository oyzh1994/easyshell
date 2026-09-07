package cn.oyzh.easyshell.dameng;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.dameng.check.DamengCheck;
import cn.oyzh.easyshell.dameng.column.DamengColumn;
import cn.oyzh.easyshell.dameng.column.DamengColumns;
import cn.oyzh.easyshell.dameng.column.DamengSelectColumnParam;
import cn.oyzh.easyshell.dameng.condition.DamengConditionUtil;
import cn.oyzh.easyshell.dameng.foreignKey.DamengForeignKey;
import cn.oyzh.easyshell.dameng.function.DamengAlertFunctionParam;
import cn.oyzh.easyshell.dameng.function.DamengCreateFunctionParam;
import cn.oyzh.easyshell.dameng.function.DamengFunction;
import cn.oyzh.easyshell.dameng.function.DamengSelectFunctionParam;
import cn.oyzh.easyshell.dameng.generator.function.DamengFunctionAlertSqlGenerator;
import cn.oyzh.easyshell.dameng.generator.function.DamengFunctionCreateSqlGenerator;
import cn.oyzh.easyshell.dameng.generator.procedure.DamengProcedureAlertSqlGenerator;
import cn.oyzh.easyshell.dameng.generator.procedure.DamengProcedureCreateSqlGenerator;
import cn.oyzh.easyshell.dameng.generator.table.DamengTableAlertSqlGenerator;
import cn.oyzh.easyshell.dameng.generator.table.DamengTableCreateSqlGenerator;
import cn.oyzh.easyshell.dameng.generator.view.DamengViewAlertSqlGenerator;
import cn.oyzh.easyshell.dameng.generator.view.DamengViewCreateSqlGenerator;
import cn.oyzh.easyshell.dameng.index.DamengIndex;
import cn.oyzh.easyshell.dameng.procedure.DamengAlertProcedureParam;
import cn.oyzh.easyshell.dameng.procedure.DamengCreateProcedureParam;
import cn.oyzh.easyshell.dameng.procedure.DamengProcedure;
import cn.oyzh.easyshell.dameng.procedure.DamengSelectProcedureParam;
import cn.oyzh.easyshell.dameng.record.DamengDeleteRecordParam;
import cn.oyzh.easyshell.dameng.record.DamengInsertRecordParam;
import cn.oyzh.easyshell.dameng.record.DamengRecord;
import cn.oyzh.easyshell.dameng.record.DamengRecordFilter;
import cn.oyzh.easyshell.dameng.record.DamengRecordPrimaryKey;
import cn.oyzh.easyshell.dameng.record.DamengSelectRecordParam;
import cn.oyzh.easyshell.dameng.record.DamengUpdateRecordParam;
import cn.oyzh.easyshell.dameng.routine.DamengRoutineParam;
import cn.oyzh.easyshell.dameng.schema.DamengSchema;
import cn.oyzh.easyshell.dameng.table.DamengAlertTableParam;
import cn.oyzh.easyshell.dameng.table.DamengCreateTableParam;
import cn.oyzh.easyshell.dameng.table.DamengSelectTableParam;
import cn.oyzh.easyshell.dameng.table.DamengTable;
import cn.oyzh.easyshell.dameng.trigger.DamengTrigger;
import cn.oyzh.easyshell.dameng.view.DamengAlertViewParam;
import cn.oyzh.easyshell.dameng.view.DamengCreateViewParam;
import cn.oyzh.easyshell.dameng.view.DamengSelectViewParam;
import cn.oyzh.easyshell.dameng.view.DamengView;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellJumpConfig;
import cn.oyzh.easyshell.domain.ShellProxyConfig;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.internal.ShellBaseClient;
import cn.oyzh.easyshell.internal.ShellClientChecker;
import cn.oyzh.easyshell.internal.ShellConnState;
import cn.oyzh.easyshell.query.dameng.DamengExecuteResult;
import cn.oyzh.easyshell.query.dameng.DamengExplainResult;
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
import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 达梦数据库客户端封装，提供数据库连接、表/视图/函数/过程/触发器等对象的CRUD操作，
 * 以及SQL执行、记录管理、索引/外键/检查约束管理等功能
 *
 * @author oyzh
 * @since 2023/11/06
 */
public class ShellDamengClient implements ShellBaseClient, DBClient {

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
            this.connManager = new ShellDamengConnManager();
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
     * 构造达梦客户端
     *
     * @param shellConnect 连接信息
     */
    public ShellDamengClient(ShellConnect shellConnect) {
        this.shellConnect = shellConnect;
        this.addStateListener(this.stateListener);
    }

    /**
     * 是否只读模式
     *
     * @return 结果
     */
    @Override
    /**
     * 是否只读模式
     *
     * @return 结果
     */
    public boolean isReadonly() {
        return this.shellConnect.isReadonly();
    }

    @Override
    /**
     * 启动数据库连接
     *
     * @param timeout 连接超时时间(毫秒)
     * @throws Throwable 连接异常
     */
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
            JulLog.warn("Dameng client start error", ex);
            throw new ShellException(ex);
        }
    }

    @Override
    /**
     * 获取连接信息
     *
     * @return 连接信息
     */
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
        }
        this.getConnManager().setConfig(connConfig);
    }

    @Override
    /**
     * 关闭数据库连接，释放资源
     */
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
            JulLog.warn("Dameng client close error.", ex);
        }
    }

    @Override
    /**
     * 判断数据库是否已连接
     *
     * @return 是否已连接
     */
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

    /**
     * 获取表数量
     *
     * @param schema 库名称或者模式名称
     * @return 表数量
     */
    @Override
    /**
     * 获取表数量
     *
     * @param schema 模式名称
     * @return 表数量
     */
    public int tableSize(String schema) {
        int size = 0;
        try {
            Connection connection = this.getConnManager().connection(schema);
            String sql = "SELECT COUNT(*) FROM ALL_TABLES WHERE OWNER = ?";
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
     * 获取视图数量
     *
     * @param schema 库名称或者模式名称
     * @return 视图数量
     */
    @Override
    /**
     * 获取视图数量
     *
     * @param schema 模式名称
     * @return 视图数量
     */
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
    public DBQueryResults<DamengExecuteResult> executeSql(String schema, String sql) {
        DBQueryResults<DamengExecuteResult> results = new DBQueryResults<>();
        Connection connection = null;
        try {
            this.printSql(sql);
            DBSqlParser parser = DBSqlParser.getParser(sql, DBDialect.DAMENG);
            List<String> list = parser.parseSql();
            connection = this.getConnManager().connection(schema);
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();
            for (String execSql : list) {
                DamengExecuteResult result = new DamengExecuteResult();
                result.setContent(execSql);
                try {
                    long startTime = System.nanoTime();
                    boolean isQuery = statement.execute(execSql);
                    if (isQuery) {
                        ResultSet resultSet = statement.getResultSet();
                        if (parser.isSingle()) {
                            result.setFullColumn(parser.isFullColumn());
                        } else {
                            result.setFullColumn(DBUtil.isFullColumn(this.dialect(), execSql));
                        }
                        result.parseResult(resultSet, connection, !parser.isSelect());
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
    /**
     * 批量插入SQL
     *
     * @param schema 模式名称
     * @param sqlList SQL列表
     * @return 插入行数
     */
    public int insertBatch(String schema, List<String> sqlList) {
        return this.insertBatch(schema, sqlList, false);
    }

    @Override
    /**
     * 获取存储过程数量
     *
     * @param schema 模式名称
     * @return 存储过程数量
     */
    public int procedureSize(String schema) {
        int size = 0;
        try {
            Connection connection = this.getConnManager().procedureConnection(schema);
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
    /**
     * 获取函数数量
     *
     * @param schema 模式名称
     * @return 函数数量
     */
    public int functionSize(String schema) {
        int size = 0;
        try {
            Connection connection = this.getConnManager().functionConnection(schema);
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
    public void alertFunction(DamengAlertFunctionParam param) {
        try {
            Connection connection = this.getConnManager().functionConnection(param.getSchema());
            Statement statement = connection.createStatement();
            String sql = DamengFunctionAlertSqlGenerator.generateSqlSingle(param);
            this.printSql(sql);
            statement.executeUpdate(sql);
            IOUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    /**
     * 查询触发器列表
     *
     * @param schema 模式名称
     * @return 触发器列表
     */
    public List<DamengTrigger> selectTriggers(String schema) {
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
            //                    """;
            String sql = """
                        SELECT
                            a.TRIGGER_NAME, a.TRIGGERING_EVENT AS EVENT_MANIPULATION, a.TRIGGERING_TYPE AS TRIGGER_TYPE, a.TABLE_NAME AS EVENT_OBJECT_TABLE, a.TRIGGER_BODY AS ACTION_STATEMENT    
                        FROM
                            ALL_TRIGGERS a
                        WHERE
                            a.OWNER = ?
                    """;
            this.printSql(sql);
            PreparedStatement statement = this.getConnManager().connection(schema).prepareStatement(sql);
            statement.setString(1, schema);
            ResultSet resultSet = statement.executeQuery();
            List<DamengTrigger> list = new ArrayList<>();
            while (resultSet.next()) {
                DamengTrigger trigger = new DamengTrigger();
                String name = resultSet.getString("TRIGGER_NAME");
                String type = resultSet.getString("TRIGGER_TYPE");
                String definition = resultSet.getString("ACTION_STATEMENT");
                String tableName = resultSet.getString("EVENT_OBJECT_TABLE");
                String manipulation = resultSet.getString("EVENT_MANIPULATION");
                trigger.setName(name);
                trigger.setTableName(tableName);
                trigger.setDefinition(ShellDamengHelper.fixTiggerDefinition(definition));
                trigger.setPolicy(type.contains("BEFORE") ? "BEFORE" : "AFTER", manipulation);
                list.add(trigger);
            }
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            return list;
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    public DBObjects<DamengTrigger> selectTriggers(String schema, String tableName) {
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
            String sql = """
                        SELECT
                            a.TRIGGER_NAME, a.TRIGGERING_EVENT AS EVENT_MANIPULATION, a.TRIGGERING_TYPE AS TRIGGER_TYPE, a.TABLE_NAME AS EVENT_OBJECT_TABLE, a.TRIGGER_BODY AS ACTION_STATEMENT    
                        FROM
                            ALL_TRIGGERS a
                        WHERE
                            a.OWNER = ?
                        AND
                            a.TABLE_NAME = ?
                    """;
            this.printSql(sql);
            PreparedStatement statement = this.getConnManager().connection(schema).prepareStatement(sql);
            statement.setString(1, schema);
            statement.setString(2, tableName);
            ResultSet resultSet = statement.executeQuery();
            DBObjects<DamengTrigger> list = new DBObjects<>();
            while (resultSet.next()) {
                DamengTrigger trigger = new DamengTrigger();
                String name = resultSet.getString("TRIGGER_NAME");
                String type = resultSet.getString("TRIGGER_TYPE");
                String definition = resultSet.getString("ACTION_STATEMENT");
                String manipulation = resultSet.getString("EVENT_MANIPULATION");
                trigger.setName(name);
                trigger.setTableName(tableName);
                trigger.setDefinition(ShellDamengHelper.fixTiggerDefinition(definition));
                trigger.setPolicy(type.contains("BEFORE") ? "BEFORE" : "AFTER", manipulation);
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

    //    public String getTriggerDefinition(String schema, String triggerName) {
    //        try {
    //            String sql = """
    //                        SELECT
    //                            TO_CHAR(DBMS_METADATA.GET_DDL('TRIGGER', ?, ?))
    //                        AS
    //                            TRIGGER_DDL
    //                    """;
    //            this.printSql(sql);
    //            PreparedStatement statement = this.getConnManager().connection().prepareStatement(sql);
    //            statement.setString(1, triggerName);
    //            statement.setString(2, schema);
    //            ResultSet resultSet = statement.executeQuery();
    //            List<DamengTrigger> list = new ArrayList<>();
    //            String definition = null;
    //            if (resultSet.next()) {
    //                definition = resultSet.getString(1);
    //            }
    //            IOUtil.close(resultSet);
    //            IOUtil.close(statement);
    //            return definition;
    //        } catch (Exception ex) {
    //            throw new ShellException(ex);
    //        }
    //    }

    @Override
    /**
     * 查询数据库版本
     *
     * @return 版本信息
     */
    public String selectVersion() {
        if (this.hasProperty("version")) {
            return this.getProperty("version");
        }
        String version = "";
        try {
            Statement stmt = this.getConnManager().connection().createStatement();
            ResultSet resultSet = stmt.executeQuery("SELECT BUILD_VERSION FROM V$INSTANCE;");
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
    /**
     * 查询数据库产品信息
     *
     * @return 产品信息
     */
    public String selectProduct() {
        if (this.hasProperty("product")) {
            return this.getProperty("product");
        }
        String product = "";
        try {
            Connection conn = this.getConnManager().connection();
            Statement stmt = conn.createStatement();
            ResultSet resultSet = stmt.executeQuery("SELECT BANNER FROM V$VERSION;");
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

    //public void dropEvent(String schema, DamengEvent event) {
    //    try {
    //        String sql = "DROP EVENT " + DBUtil.wrap(event.getschema(), event.getName(), DBDialect.DAMENG);
    //        this.printSql(sql);
    //        Statement statement = this.getConnManager().connection(schema).createStatement();
    //        statement.executeUpdate(sql);
    //        IOUtil.close(statement);
    //    } catch (Exception ex) {
    //        ex.printStackTrace();
    //        throw new ShellException(ex);
    //    }
    //}

    //    public void createEvent(String schema, DamengEvent event) {
    //        try {
    //            String sql = DamengEventCreateSqlGenerator.generateSql(event);
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
    //    public void alertEvent(String schema, DamengEvent event) {
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

    //    public DamengEvent selectEvent(String schema, String eventName) {
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
    //            DamengEvent event = new DamengEvent();
    //            while (resultSet.next()) {
    //                String expr = resultSet.getString("EXPR");
    //                String status = resultSet.getString("STATUS");
    //                String action = resultSet.getString("ACTION");
    //                event.setName(eventName);
    //                event.setStatus("TRUE".equalsIgnoreCase(status) ? "ENABLE" : "DISABLE");
    //                DamengHelper.parseJobExpr(expr, event);
    //                DamengHelper.parseJobAction(action, event);
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

    //    public List<DamengEvent> events(String schema) {
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
    //            List<DamengEvent> list = new ArrayList<>();
    //            while (resultSet.next()) {
    //                String name = resultSet.getString("NAME");
    //                String expr = resultSet.getString("EXPR");
    //                String status = resultSet.getString("STATUS");
    //                String action = resultSet.getString("ACTION");
    //                DamengEvent event = new DamengEvent();
    //                event.setName(name);
    //                event.setStatus("TRUE".equalsIgnoreCase(status) ? "ENABLE" : "DISABLE");
    //                DamengHelper.parseJobExpr(expr, event);
    //                DamengHelper.parseJobAction(action, event);
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
    /**
     * 是否支持指定特性
     *
     * @param feature 特性
     * @return 是否支持
     */
    public boolean isSupportFeature(DBFeature feature) {
        try {
            if (feature == DBFeature.EVENT) {
                return true;
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

    public boolean isSupportCheckFeature() {
        return this.isSupportFeature(DBFeature.CHECK);
    }

    @Override
    public Long getGeneratedKeys(Statement statement) throws Exception {
        ResultSet rs = statement.getGeneratedKeys();
        Long newId = null;
        if (rs.next()) {
            newId = rs.getLong(1);
        } else {
            IOUtil.close(statement);
            String sql = "SELECT @@IDENTITY FROM DUAL";
            statement = statement.getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
                newId = resultSet.getLong(1);
            }
        }
        IOUtil.close(statement);
        IOUtil.close(rs);
        return newId;
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
    public List<DamengTable> selectTables(String schema) {
        DamengSelectTableParam param = new DamengSelectTableParam();
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
    public List<DamengTable> selectTablesSimple(String schema) {
        DamengSelectTableParam param = new DamengSelectTableParam();
        param.setFull(false);
        param.setSchema(schema);
        return this.selectTables(param);
    }

    public List<DamengTable> selectTables(DamengSelectTableParam param) {
        try {
            String schema = param.getSchema();
            List<DamengTable> tables = new ArrayList<>();
            Connection connection = this.getConnManager().connection(schema);
            String sql;
            if (param.isFull()) {
                sql = """
                            SELECT 
                                T.TABLE_NAME,
                                C.COMMENTS AS "TABLE_COMMENT",
                                T.TABLESPACE_NAME AS "TABLE_SPACE",
                                DBMS_METADATA.GET_DDL('TABLE', T.TABLE_NAME, T.OWNER) AS "TABLE_DDL"
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
                        """;
            } else {
                sql = """
                            SELECT 
                                T.TABLE_NAME,
                                C.COMMENTS AS "TABLE_COMMENT",
                                T.TABLESPACE_NAME AS "TABLE_SPACE"
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
                        """;
            }
            this.printSql(sql);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, schema);
            ResultSet resultSet = statement.executeQuery();
            DBUtil.printMetaData(resultSet);
            while (resultSet.next()) {
                DamengTable table = new DamengTable();
                String tableName = resultSet.getString("TABLE_NAME");
                String tableSpace = resultSet.getString("TABLE_SPACE");
                String tableComment = resultSet.getString("TABLE_COMMENT");
                if (param.isFull()) {
                    //                    String showCreateTable = this.showCreateTable(schema, tableName);
                    //                    table.setCreateDefinition(showCreateTable);
                    String tableDdl = resultSet.getString("TABLE_DDL");
                    table.setCreateDefinition(tableDdl);
                }
                table.setSchema(schema);
                table.setName(tableName);
                table.setComment(tableComment);
                table.setTableSpace(tableSpace);
                tables.add(table);
            }
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            return tables;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    //    public List<DamengTable> selectTablesSimple(String schema) {
    //        try {
    //            List<DamengTable> tables = new ArrayList<>();
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
    //                DamengTable table = new DamengTable();
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

    public DamengColumns selectColumns(DamengSelectColumnParam param) {
        try {
            String schema = param.getSchema();
            String tableName = param.getTableName();
            DamengColumns columns = new DamengColumns();
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
                        C.COLUMN_ID AS "POSITION",
                        C.COLUMN_NAME AS "Field",
                        C.DATA_SCALE AS "DATA_SCALE",
                        C.DATA_LENGTH AS "DATA_LENGTH",
                        CASE WHEN C.NULLABLE='Y' THEN 'YES' ELSE 'NO' END AS "Null",
                        C.DATA_TYPE AS "Type",
                        CC.COMMENTS AS "Comment",
                        C.DATA_DEFAULT AS "Default",
                        CASE WHEN CONS.COLUMN_NAME IS NOT NULL THEN 'PRI' ELSE '' END AS "Key"
                    FROM
                        ALL_TAB_COLUMNS C
                    LEFT JOIN
                        ALL_COL_COMMENTS CC
                    ON
                        C.OWNER = CC.SCHEMA_NAME
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
                        EXISTS (SELECT 1 FROM ALL_CONSTRAINTS AC WHERE AC.OWNER=CONS.OWNER AND AC.CONSTRAINT_NAME=CONS.CONSTRAINT_NAME AND AC.CONSTRAINT_TYPE='P')
                    WHERE
                        C.OWNER = ?
                    AND
                        C.TABLE_NAME = ?
                    """;
            //            }
            Connection connection = this.getConnManager().connection(schema);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, schema);
            statement.setString(2, tableName);
            ResultSet resultSet = statement.executeQuery();
            DatabaseMetaData metaData = connection.getMetaData();
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
                // 自动递增
                Boolean autoIncrement = null;
                ResultSet rs = metaData.getColumns(null, null, tableName, field);
                if (rs.next()) {
                    String is_autoincrement = rs.getString("IS_AUTOINCREMENT");
                    autoIncrement = StringUtil.equalsIgnoreCase("YES", is_autoincrement);
                }
                IOUtil.close(rs);
                DamengColumn column = new DamengColumn();
                column.parseKey(key);
                column.setName(field);
                column.setSize(dataLength);
                column.setDigits(dataScale);
                column.setType(type);
                column.setSchema(schema);
                column.setComment(comment);
                column.setDefaultValue(def);
                column.setPosition(position);
                column.setTableName(tableName);
                column.setAutoIncrement(autoIncrement);
                column.setNullable("yes".equalsIgnoreCase(nullable));
                columns.add(column);
            }
            IOUtil.close(resultSet);
            // 返回排序后的数据
            return new DamengColumns(columns.sortOfPosition());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public List<DamengRecord> selectRecords(DamengSelectRecordParam param) {
        try {
            Connection connection = this.getConnManager().connection(param.getSchema());
            StringBuilder builder = new StringBuilder("SELECT * FROM ");
            builder.append(DBUtil.wrap(param.getSchema(), param.getTableName(), DBDialect.DAMENG));
            String filterCondition = DamengConditionUtil.buildCondition(param.getFilters());
            if (StringUtil.isNotBlank(filterCondition)) {
                builder.append(" WHERE ").append(filterCondition);
            }
            if (param.hasPageControl()) {
                builder.append(" OFFSET ")
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
            List<DamengRecord> records = new ArrayList<>();
            List<DamengColumn> columns;
            if (param.getColumns() != null) {
                columns = param.getColumns();
            } else {
                columns = ShellDamengHelper.parseColumns(resultSet);
            }
            while (resultSet.next()) {
                DamengRecord record = new DamengRecord(columns, param.isReadonly());
                for (DamengColumn column : columns) {
                    Object data = resultSet.getObject(column.getName());
                    //                    // 获取几何值
                    //                    if (column.supportGeometry()) {
                    //                        data = DamengHelper.getGeometryString(connection, data);
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

    public long selectRecordCount(DamengSelectRecordParam param) {
        long count = 0;
        try {
            Connection connection = this.getConnManager().connection(param.getSchema());
            StringBuilder builder = new StringBuilder("SELECT COUNT(*) FROM");
            builder.append(DBUtil.wrap(param.getSchema(), param.getTableName(), DBDialect.DAMENG));
            String filterCondition = DamengConditionUtil.buildCondition(param.getFilters());
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

    public int insertRecord(DamengInsertRecordParam param) {
        if (param == null || param.getRecord() == null) {
            return 0;
        }
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("INSERT INTO ")
                    .append(DBUtil.wrap(param.getSchema(), param.getTableName(), DBDialect.DAMENG))
                    .append("(");
            for (String column : param.getRecord().columns()) {
                builder.append(DBUtil.wrap(column, DBDialect.DAMENG)).append(",");
            }
            builder.append(")");
            builder.append(" VALUES(");
            for (String column : param.getRecord().columns()) {
                //                if (param.getRecord().isTypeGeometry(column)) {
                //                    builder.append("ST_GeomFromText(?),");
                //                } else {
                builder.append("?,");
                //                }
            }
            builder.append(")");
            String sql = builder.toString();
            sql = sql.replaceAll(",\\)", ")");
            this.printSql(sql);
            Connection connection = this.getConnManager().connection(param.getSchema());
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            int index = 1;
            for (String colName : param.getRecord().columns()) {
                DBUtil.setVal(statement, param.getRecord().value(colName), index++);
            }
            int count = statement.executeUpdate();
            DamengRecordPrimaryKey primaryKey = param.getPrimaryKey();
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

    public int deleteRecord(DamengDeleteRecordParam param) {
        try {
            int updateCount;
            String schema = param.getSchema();
            String tableName = param.getTableName();
            Connection connection = this.getConnManager().connection(schema);
            StringBuilder builder = new StringBuilder();
            builder.append("DELETE FROM ")
                    .append(DBUtil.wrap(schema, tableName, DBDialect.DAMENG))
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
                        builder.append(DBUtil.wrap(colName, DBDialect.DAMENG))
                                .append(" = ?");
                    } else {
                        builder.append(DBUtil.wrap(colName, DBDialect.DAMENG))
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
                DamengRecordPrimaryKey primaryKey = param.getPrimaryKey();
                builder.append(DBUtil.wrap(primaryKey.getColumnName(), DBDialect.DAMENG))
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

    public int updateRecord(DamengUpdateRecordParam param) {
        try {
            int updateCount;
            String schema = param.getSchema();
            String tableName = param.getTableName();
            DBRecordData recordData = param.getUpdateRecord();
            StringBuilder builder = new StringBuilder();
            builder.append("UPDATE ")
                    .append(DBUtil.wrap(schema, tableName, DBDialect.DAMENG))
                    .append(" SET ");
            for (String column : recordData.columns()) {
                builder.append(DBUtil.wrap(column, DBDialect.DAMENG)).append(" = ?,");
            }
            builder.deleteCharAt(builder.length() - 1);
            builder.append(" WHERE ");
            Connection connection = this.getConnManager().connection(schema);
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
                        builder.append(DBUtil.wrap(column, DBDialect.DAMENG)).append(" IS NULL");
                    } else {
                        builder.append(DBUtil.wrap(column, DBDialect.DAMENG)).append(" = ?");
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
                DamengRecordPrimaryKey primaryKey = param.getPrimaryKey();
                builder.append(DBUtil.wrap(primaryKey.getColumnName(), DBDialect.DAMENG)).append(" = ?");
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

    public String showCreateTable(String schema, String tableName) {
        try {
            Connection connection = this.getConnManager().connection(schema);
            String sql = "SELECT DBMS_METADATA.GET_DDL('TABLE', ?) FROM DUAL";
            this.printSql(sql);
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, tableName);
            ResultSet resultSet = stmt.executeQuery();
            String createDefinition = "";
            if (resultSet.next()) {
                createDefinition = resultSet.getString(1);
            }
            IOUtil.close(resultSet);
            IOUtil.close(stmt);
            return createDefinition;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public String showCreateView(String schema, String viewName) {
        try {
            Connection connection = this.getConnManager().connection(schema);
            //            String sql = "SELECT TEXT FROM USER_VIEWS WHERE VIEW_NAME = ?";
            String sql = "SELECT DBMS_METADATA.GET_DDL('VIEW', ?) FROM DUAL";
            this.printSql(sql);
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, viewName);
            ResultSet resultSet = stmt.executeQuery();
            String createDefinition = "";
            if (resultSet.next()) {
                //                createDefinition = "CREATE OR REPLACE VIEW " + viewName + " AS\n" + resultSet.getString(1);
                createDefinition = resultSet.getString(1);
            }
            IOUtil.close(resultSet);
            IOUtil.close(stmt);
            return createDefinition;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public String showCreateFunction(String schema, String functionName) {
        try {
            Connection connection = this.getConnManager().functionConnection(schema);
            String sql = "SELECT DBMS_METADATA.GET_DDL('FUNCTION', ?) FROM DUAL";
            this.printSql(sql);
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, functionName);
            ResultSet resultSet = stmt.executeQuery();
            String createDefinition = "";
            if (resultSet.next()) {
                createDefinition = resultSet.getString(1);
            }
            IOUtil.close(resultSet);
            IOUtil.close(stmt);
            return createDefinition;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public String showCreateProcedure(String schema, String procedureName) {
        try {
            Connection connection = this.getConnManager().procedureConnection(schema);
            String sql = "SELECT DBMS_METADATA.GET_DDL('PROCEDURE', ?) FROM DUAL";
            this.printSql(sql);
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, procedureName);
            ResultSet resultSet = stmt.executeQuery();
            String createDefinition = "";
            if (resultSet.next()) {
                createDefinition = resultSet.getString(1);
            }
            IOUtil.close(resultSet);
            IOUtil.close(stmt);
            return createDefinition;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public String showCreateTrigger(String schema, String triggerName) {
        try {
            Connection connection = this.getConnManager().connection(schema);
            String sql = "SELECT DBMS_METADATA.GET_DDL('TRIGGER', ?) FROM DUAL";
            this.printSql(sql);
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, triggerName);
            ResultSet resultSet = stmt.executeQuery();
            String createDefinition = "";
            if (resultSet.next()) {
                createDefinition = resultSet.getString(1);
            }
            IOUtil.close(resultSet);
            IOUtil.close(stmt);
            return createDefinition;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    //    public String showCreateEvent(String schema, String eventName) {
    //        try {
    //            Connection connection = this.getConnManager().connection(schema);
    //            String sql = "SELECT DBMS_METADATA.GET_DDL('EVENT', ?) FROM DUAL";
    //            this.printSql(sql);
    //            PreparedStatement stmt = connection.prepareStatement(sql);
    //            stmt.setString(1, eventName);
    //            ResultSet resultSet = stmt.executeQuery();
    //            String createDefinition = "";
    //            if (resultSet.next()) {
    //                createDefinition = resultSet.getString(1);
    //            }
    //            IOUtil.close(resultSet);
    //            IOUtil.close(stmt);
    //            return createDefinition;
    //        } catch (Exception ex) {
    //            ex.printStackTrace();
    //            throw new ShellException(ex);
    //        }
    //    }

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
            sql = "SELECT NAME AS TABLESPACE_NAME FROM V$TABLESPACE WHERE STATUS$ = 0";
            //            }
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
    public List<DamengSchema> selectSchemas() {
        try {
            Statement statement = this.getConnManager().connection().createStatement();
            // 查询当前用户有权限访问的schema：优先显示有对象的schema，也包含当前用户自己的schema
            //            String sql = """
            //                    SELECT DISTINCT OWNER FROM ALL_OBJECTS
            //                    UNION
            //                    SELECT USERNAME FROM ALL_USERS
            //                    ORDER BY 1
            //                    """;

            ResultSet resultSet = null;
            List<DamengSchema> list = new ArrayList<>();
            String sql;
            //            if (this.isDbaRole()) {
            //                sql = "SELECT * FROM SYSOBJECTS WHERE TYPE$ = 'SCH'";
            //            } else {
            sql = "SELECT DISTINCT OBJECT_NAME FROM ALL_OBJECTS WHERE OBJECT_TYPE = 'SCH'";
            //                sql = "SELECT SF_GET_SCHEMA_NAME_BY_ID(CURRENT_SCHID);";
            //            }
            this.printSql(sql);
            resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                DamengSchema schema = new DamengSchema();
                String name = resultSet.getString(1);
                schema.setName(name);
                list.add(schema);
            }
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            return list;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public DamengSchema schema(String schema) {
        try {
            DamengSchema dbSchema = new DamengSchema();
            dbSchema.setName(schema);
            return dbSchema;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public DamengTable selectTable(String schema, String tableName) {
        DamengSelectTableParam param = new DamengSelectTableParam();
        param.setFull(true);
        param.setSchema(schema);
        param.setTableName(tableName);
        return this.selectTable(param);
    }

    public DamengTable selectTableSimple(String schema, String tableName) {
        DamengSelectTableParam param = new DamengSelectTableParam();
        param.setFull(false);
        param.setSchema(schema);
        param.setTableName(tableName);
        return this.selectTable(param);
    }

    public DamengTable selectTable(DamengSelectTableParam param) {
        try {
            String schema = param.getSchema();
            String tableName = param.getTableName();
            DamengTable table = new DamengTable();
            table.setSchema(schema);
            table.setName(tableName);
            Connection connection = this.getConnManager().connection(schema);
            String sql = """
                        SELECT 
                            C.COMMENTS AS "TABLE_COMMENT", T.TABLESPACE_NAME AS "TABLE_SPACE" 
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
            if (param.isFull()) {
                String showCreateTable = this.showCreateTable(schema, tableName);
                table.setCreateDefinition(showCreateTable);
            }
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            return table;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    //public DamengTable selectFullTable(DamengSelectTableParam param) {
    //    try {
    //        String schema = param.getschema();
    //        String tableName = param.getTableName();
    //        DamengTable table = new DamengTable();
    //        table.setschema(schema);
    //        table.setName(tableName);
    //        Connection connection = this.getConnManager().connection(schema);
    //        String sql = """
    //                        SELECT
    //                            C.COMMENTS AS "TABLE_COMMENT", T.TABLESPACE_NAME AS "TABLE_SPACE"
    //                        FROM
    //                            ALL_TABLES T
    //                        LEFT JOIN
    //                            ALL_TAB_COMMENTS C
    //                        ON
    //                            T.OWNER = C.OWNER
    //                        AND
    //                            T.TABLE_NAME = C.TABLE_NAME
    //                        WHERE
    //                            T.OWNER = ?
    //                        AND
    //                            T.TABLE_NAME = ?
    //                """;
    //        this.printSql(sql);
    //        PreparedStatement statement = connection.prepareStatement(sql);
    //        statement.setString(1, schema);
    //        statement.setString(2, tableName);
    //        ResultSet resultSet = statement.executeQuery();
    //        DBUtil.printMetaData(resultSet);
    //        String showCreateTable = this.showCreateTable(schema, tableName);
    //        while (resultSet.next()) {
    //            String tableSpace = resultSet.getString("TABLE_SPACE");
    //            String tableComment = resultSet.getString("TABLE_COMMENT");
    //            table.setComment(tableComment);
    //            table.setTableSpace(tableSpace);
    //            table.setCreateDefinition(showCreateTable);
    //        }
    //        IOUtil.close(resultSet);
    //        IOUtil.close(statement);
    //        return table;
    //    } catch (Exception ex) {
    //        ex.printStackTrace();
    //        throw new ShellException(ex);
    //    }
    //}

    public DamengView selectView(String schema, String viewName) {
        DamengSelectViewParam param = new DamengSelectViewParam();
        param.setFull(true);
        param.setSchema(schema);
        param.setViewName(viewName);
        return this.selectView(param);
    }

    public DamengView selectViewSimple(String schema, String viewName) {
        DamengSelectViewParam param = new DamengSelectViewParam();
        param.setFull(false);
        param.setSchema(schema);
        param.setViewName(viewName);
        return this.selectView(param);
    }

    public DamengView selectView(DamengSelectViewParam param) {
        try {
            String schema = param.getSchema();
            String viewName = param.getViewName();
            String sql = """
                        SELECT 
                            v.TEXT AS DEFINITION, v.VIEW_NAME, v.READ_ONLY, c.COMMENTS AS TABLE_COMMENT 
                        FROM 
                            ALL_VIEWS v
                        LEFT JOIN 
                            ALL_TAB_COMMENTS C 
                        ON 
                            V.OWNER = C.OWNER
                        AND 
                            V.VIEW_NAME = C.TABLE_NAME 
                        WHERE 
                            v.OWNER = ?
                        AND
                            VIEW_NAME = ?
                    """;
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
            DamengView view = new DamengView();
            while (resultSet.next()) {
                String name = resultSet.getString("VIEW_NAME");
                String readOnly = resultSet.getString("READ_ONLY");
                String definition = resultSet.getString("DEFINITION");
                String tableComment = resultSet.getString("TABLE_COMMENT");
                if (param.isFull()) {
                    view.setCreateDefinition(this.showCreateView(schema, viewName));
                }
                if (readOnly == null) {
                    view.setUpdatable(ShellDamengHelper.isViewUpdatable(connection, schema, viewName));
                } else {
                    view.setUpdatable(!StringUtil.equalsIgnoreCase("Y", readOnly));
                }
                view.setName(name);
                view.setSchema(schema);
                view.setComment(tableComment);
                view.setDefinition(definition);
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

    public List<DamengView> selectViews(String schema) {
        DamengSelectViewParam param = new DamengSelectViewParam();
        param.setFull(true);
        param.setSchema(schema);
        return this.selectViews(param);
    }

    public List<DamengView> selectViewsSimple(String schema) {
        DamengSelectViewParam param = new DamengSelectViewParam();
        param.setFull(false);
        param.setSchema(schema);
        return this.selectViews(param);
    }

    public List<DamengView> selectViews(DamengSelectViewParam param) {
        try {
            String schema = param.getSchema();
            List<DamengView> list = new ArrayList<>();
            String sql = """
                        SELECT 
                            v.TEXT AS DEFINITION, v.VIEW_NAME, v.READ_ONLY,  C.COMMENTS AS TABLE_COMMENT 
                        FROM 
                            ALL_VIEWS v
                        LEFT JOIN 
                            ALL_TAB_COMMENTS C 
                        ON 
                            V.OWNER = C.OWNER
                        AND 
                            V.VIEW_NAME = C.TABLE_NAME 
                        WHERE 
                            V.OWNER = ?
                    """;
            this.printSql(sql);
            Connection connection = this.getConnManager().connection(schema);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, schema);
            ResultSet resultSet = statement.executeQuery();
            // 打印元数据
            DBUtil.printMetaData(resultSet);
            // 遍历结果集
            while (resultSet.next()) {
                DamengView view = new DamengView();
                String name = resultSet.getString("VIEW_NAME");
                String readOnly = resultSet.getString("READ_ONLY");
                String definition = resultSet.getString("DEFINITION");
                String tableComment = resultSet.getString("TABLE_COMMENT");
                if (param.isFull()) {
                    view.setCreateDefinition(this.showCreateView(schema, name));
                }
                if (readOnly == null) {
                    view.setUpdatable(ShellDamengHelper.isViewUpdatable(connection, schema, name));
                } else {
                    view.setUpdatable(!StringUtil.equalsIgnoreCase("Y", readOnly));
                }
                view.setSchema(schema);
                view.setName(name);
                view.setComment(tableComment);
                view.setDefinition(definition);
                view.setUpdatable(!StringUtil.equalsIgnoreCase("Y", readOnly));
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

    public void dropView(DamengView view) {
        try {
            String sql = "DROP VIEW IF EXISTS " + DBUtil.wrap(view.getSchema(), view.getName(), DBDialect.DAMENG);
            Statement statement = this.getConnManager().connection(view.getSchema()).createStatement();
            this.printSql(sql);
            statement.executeUpdate(sql);
            IOUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

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
     * 创建视图
     *
     * @param param 参数
     */
    public void createView(DamengCreateViewParam param) {
        Connection connection = null;
        try {
            List<String> sqlList = DamengViewCreateSqlGenerator.generateSql(param);
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
     * 修改视图
     *
     * @param param 参数
     */
    public void alertView(DamengAlertViewParam param) {
        Connection connection = null;
        try {
            List<String> sqlList = DamengViewAlertSqlGenerator.generateSql(param);
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

    public DBObjects<DamengIndex> indexes(String schema, String tableName) {
        try {
            Connection connection = this.getConnManager().connection(schema);
            String sql = """
                    SELECT
                        i.INDEX_NAME AS "Key_name",
                        c.COLUMN_NAME AS "Column_name",
                        CASE WHEN i.UNIQUENESS='UNIQUE' THEN 0 ELSE 1 END AS "Non_unique",
                        c.COLUMN_POSITION AS "Seq_in_index",
                        CASE WHEN i.INDEX_TYPE='NORMAL' THEN 'BTREE' ELSE i.INDEX_TYPE END AS "Index_type"
                    FROM ALL_INDEXES i
                    JOIN ALL_IND_COLUMNS c ON i.INDEX_NAME = c.INDEX_NAME AND i.OWNER = c.INDEX_OWNER
                    WHERE i.TABLE_OWNER = ? AND i.TABLE_NAME = ? AND i.INDEX_TYPE != 'PRIMARY'
                    ORDER BY i.INDEX_NAME, c.COLUMN_POSITION
                    """;
            this.printSql(sql);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, schema);
            statement.setString(2, tableName);
            ResultSet resultSet = statement.executeQuery();
            // 打印元数据
            DBUtil.printMetaData(resultSet);
            Map<String, DamengIndex> indexMap = new HashMap<>();
            while (resultSet.next()) {
                String keyName = resultSet.getString("Key_name");
                // 主键类型的跳过
                if ("Primary".equalsIgnoreCase(keyName)) {
                    continue;
                }
                DamengIndex tableIndex = indexMap.get(keyName);
                String columnName = resultSet.getString("Column_name");
                if (tableIndex == null) {
                    int noneUnique = resultSet.getInt("Non_unique");
                    int seqInIndex = resultSet.getInt("Seq_in_index");
                    String indexType = resultSet.getString("Index_type");
                    tableIndex = new DamengIndex();
                    tableIndex.setName(keyName);
                    tableIndex.setSeqIndex(seqInIndex);
                    tableIndex.type(indexType, noneUnique);
                    indexMap.put(keyName, tableIndex);
                }
                tableIndex.addColumn(columnName);
            }
            IOUtil.close(resultSet);
            IOUtil.close(statement);
            return new DBObjects<DamengIndex>(indexMap.values());
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    public DBObjects<DamengCheck> checks(String schema, String tableName) {
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
                        FROM ALL_CONSTRAINTS
                        WHERE CONSTRAINT_TYPE = 'C'
                        AND OWNER = ?
                        AND TABLE_NAME = ?
                    """;
            this.printSql(sql);
            PreparedStatement statement = this.getConnManager().connection(schema).prepareStatement(sql);
            statement.setString(1, schema);
            statement.setString(2, tableName);
            ResultSet resultSet = statement.executeQuery();
            DBUtil.printMetaData(resultSet);
            DBObjects<DamengCheck> checks = new DBObjects<>();
            while (resultSet.next()) {
                DamengCheck check = new DamengCheck();
                String name = resultSet.getString("NAME");
                String clause = resultSet.getString("CLAUSE");
                check.setName(name);
                check.setClause(clause);
                check.setSchema(schema);
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

    public DBObjects<DamengForeignKey> foreignKeys(String schema, String tableName) {
        try {
            // 查询外键 - Dameng uses ALL_CONSTRAINTS + ALL_CONS_COLUMNS
            String sql = """
                    SELECT
                        acc.COLUMN_NAME AS "FKCOLUMN_NAME",
                        ac_r.OWNER AS "PKTABLE_CAT",
                        ac_r.TABLE_NAME AS "PKTABLE_NAME",
                        acc_r.COLUMN_NAME AS "PKCOLUMN_NAME",
                        ac.CONSTRAINT_NAME AS "FK_NAME",
                        NULL AS "UPDATE_RULE",
                        ac.DELETE_RULE
                    FROM 
                        ALL_CONSTRAINTS ac
                    JOIN 
                        ALL_CONS_COLUMNS acc 
                    ON 
                        ac.CONSTRAINT_NAME = acc.CONSTRAINT_NAME 
                    AND 
                        ac.OWNER = acc.OWNER
                    JOIN 
                        ALL_CONSTRAINTS ac_r 
                    ON 
                        ac.R_OWNER = ac_r.OWNER 
                    AND 
                        ac.R_CONSTRAINT_NAME = ac_r.CONSTRAINT_NAME
                    JOIN 
                        ALL_CONS_COLUMNS acc_r 
                    ON 
                        ac_r.CONSTRAINT_NAME = acc_r.CONSTRAINT_NAME 
                    AND 
                        ac_r.OWNER = acc_r.OWNER 
                    AND 
                        acc.POSITION = acc_r.POSITION
                    WHERE 
                        ac.CONSTRAINT_TYPE = 'R'
                    AND 
                        ac.OWNER = ?
                    AND 
                        ac.TABLE_NAME = ?
                    """;
            this.printSql(sql);
            PreparedStatement statement = this.getConnManager().connection(schema).prepareStatement(sql);
            statement.setString(1, schema);
            statement.setString(2, tableName);
            ResultSet resultSet = statement.executeQuery();
            DBUtil.printMetaData(resultSet);
            Map<String, DamengForeignKey> foreignKeyMap = new HashMap<>();
            while (resultSet.next()) {
                String fkName = resultSet.getString("FK_NAME");
                String fkColumnName = resultSet.getString("FKCOLUMN_NAME");
                String pkColumnName = resultSet.getString("PKCOLUMN_NAME");
                DamengForeignKey foreignKey = foreignKeyMap.get(fkName);
                if (foreignKey == null) {
                    String pkTableName = resultSet.getString("PKTABLE_NAME");
                    String pkTableCat = resultSet.getString("PKTABLE_CAT");
                    String updateRule = resultSet.getString("UPDATE_RULE");
                    String deleteRule = resultSet.getString("DELETE_RULE");
                    foreignKey = new DamengForeignKey();
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
            return new DBObjects<DamengForeignKey>(foreignKeyMap.values());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public List<DamengColumn> viewColumns(String schema, String viewName) {
        //        try {
        //            if (StringUtil.isBlank(viewName)) {
        //                return Collections.emptyList();
        //            }
        //            //            String sql = """
        //            //                    SELECT
        //            //                        C.COLUMN_NAME AS "COLUMN_NAME",
        //            //                        CASE WHEN C.NULLABLE='Y' THEN 'YES' ELSE 'NO' END AS "IS_NULLABLE",
        //            //                        C.DATA_TYPE AS "COLUMN_TYPE",
        //            //                        CC.COMMENT$ AS "REMARKS",
        //            //                        C.DATA_DEFAULT AS "COLUMN_DEF",
        //            //                        CASE WHEN CONS.COLUMN_NAME IS NOT NULL THEN 'PRI' ELSE '' END AS "COLUMN_KEY",
        //            //                        C.COLUMN_ID AS ORDINAL_POSITION
        //            //                    FROM
        //            //                        ALL_TAB_COLUMNS C
        //            //                    LEFT JOIN
        //            //                        SYS.SYSCOLUMNCOMMENTS CC
        //            //                    ON
        //            //                        C.OWNER = CC.SCHNAME
        //            //                    AND
        //            //                        C.TABLE_NAME = CC.TVNAME
        //            //                    AND
        //            //                        C.COLUMN_NAME = CC.COLNAME
        //            //                    LEFT JOIN
        //            //                        ALL_CONS_COLUMNS CONS
        //            //                    ON
        //            //                        C.OWNER = CONS.OWNER
        //            //                    AND
        //            //                        C.TABLE_NAME = CONS.TABLE_NAME
        //            //                    AND
        //            //                        C.COLUMN_NAME = CONS.COLUMN_NAME
        //            //                    AND
        //            //                        EXISTS (SELECT 1 FROM ALL_CONSTRAINTS AC WHERE AC.OWNER=CONS.OWNER AND AC.CONSTRAINT_NAME=CONS.CONSTRAINT_NAME AND AC.CONSTRAINT_TYPE='P')
        //            //                    WHERE
        //            //                        C.OWNER = ?
        //            //                    AND
        //            //                        C.TABLE_NAME = ?
        //            //                    ORDER BY
        //            //                        C.COLUMN_ID
        //            //                    """;
        //            String sql = """
        //                    SELECT
        //                        C.COLUMN_ID AS "POSITION",
        //                        C.COLUMN_NAME AS "Field",
        //                        C.DATA_SCALE AS "DATA_SCALE",
        //                        C.DATA_LENGTH AS "DATA_LENGTH",
        //                        CASE WHEN C.NULLABLE='Y' THEN 'YES' ELSE 'NO' END AS "Null",
        //                        C.DATA_TYPE AS "Type",
        //                        CC.COMMENTS AS "Comment",
        //                        C.DATA_DEFAULT AS "Default",
        //                        CASE WHEN CONS.COLUMN_NAME IS NOT NULL THEN 'PRI' ELSE '' END AS "Key"
        //                    FROM
        //                        ALL_TAB_COLUMNS C
        //                    LEFT JOIN
        //                        ALL_COL_COMMENTS CC
        //                    ON
        //                        C.OWNER = CC.OWNER
        //                    AND
        //                        C.TABLE_NAME = CC.TABLE_NAME
        //                    AND
        //                        C.COLUMN_NAME = CC.COLUMN_NAME
        //                    LEFT JOIN
        //                        ALL_CONS_COLUMNS CONS
        //                    ON
        //                        C.OWNER = CONS.OWNER
        //                    AND
        //                        C.TABLE_NAME = CONS.TABLE_NAME
        //                    AND
        //                        C.COLUMN_NAME = CONS.COLUMN_NAME
        //                    AND
        //                        EXISTS (SELECT 1 FROM ALL_CONSTRAINTS AC WHERE AC.OWNER=CONS.OWNER AND AC.CONSTRAINT_NAME=CONS.CONSTRAINT_NAME AND AC.CONSTRAINT_TYPE='P')
        //                    WHERE
        //                        C.OWNER = ?
        //                    AND
        //                        C.TABLE_NAME = ?
        //                    """;
        //            //            String sql = """
        //            //                    SELECT
        //            //                        '' AS COLUMN_EXTRA,
        //            //                        NULL AS COLUMN_KEY,
        //            //                        CC.COMMENTS AS REMARKS,
        //            //                        C.DATA_TYPE AS COLUMN_TYPE,
        //            //                        C.COLUMN_NAME AS COLUMN_NAME,
        //            //                        CASE WHEN C.NULLABLE='Y' THEN 'YES' ELSE 'NO' END AS IS_NULLABLE,
        //            //                        C.DATA_DEFAULT AS COLUMN_DEF,
        //            //                        NULL AS COLLATION_NAME,
        //            //                        NULL AS CHARSET_NAME,
        //            //                        C.COLUMN_ID AS ORDINAL_POSITION
        //            //                    FROM
        //            //                        ALL_TAB_COLUMNS C
        //            //                    LEFT JOIN
        //            //                        ALL_COL_COMMENTS CC ON C.OWNER=CC.OWNER AND C.TABLE_NAME=CC.TABLE_NAME AND C.COLUMN_NAME=CC.COLUMN_NAME
        //            //                    WHERE
        //            //                        C.OWNER = ?
        //            //                    AND
        //            //                        C.TABLE_NAME = ?
        //            //                    ORDER BY
        //            //                        C.COLUMN_ID
        //            //                    """;
        //            this.printSql(sql);
        //            Connection connection = this.getConnManager().connection(schema);
        //            PreparedStatement statement = connection.prepareStatement(sql);
        //            statement.setString(1, schema);
        //            statement.setString(2, viewName);
        //            ResultSet resultSet = statement.executeQuery();
        //            // 打印元数据
        //            DBUtil.printMetaData(resultSet);
        //            DatabaseMetaData metaData = connection.getMetaData();
        //            Map<String, DamengColumn> columns = new HashMap<>();
        //            while (resultSet.next()) {
        //                String key = resultSet.getString("Key");
        //                String type = resultSet.getString("Type");
        //                String field = resultSet.getString("Field");
        //                Object def = resultSet.getObject("Default");
        //                int position = resultSet.getInt("POSITION");
        //                String nullable = resultSet.getString("Null");
        //                int dataScale = resultSet.getInt("DATA_SCALE");
        //                String comment = resultSet.getString("Comment");
        //                int dataLength = resultSet.getInt("DATA_LENGTH");
        //                // 自动递增
        //                Boolean autoIncrement = null;
        //                ResultSet rs = metaData.getColumns(null, null, viewName, field);
        //                if (rs.next()) {
        //                    autoIncrement = StringUtil.equalsIgnoreCase("YES", rs.getString("IS_AUTOINCREMENT"));
        //                }
        //                IOUtil.close(rs);
        //                DamengColumn column = new DamengColumn();
        //                column.parseKey(key);
        //                column.setName(field);
        //                column.setSize(dataLength);
        //                column.setDigits(dataScale);
        //                column.setType(type);
        //                column.setSchema(schema);
        //                column.setComment(comment);
        //                column.setDefaultValue(def);
        //                column.setPosition(position);
        //                column.setTableName(viewName);
        //                column.setAutoIncrement(autoIncrement);
        //                column.setNullable("yes".equalsIgnoreCase(nullable));
        //                columns.put(column.getName(), column);
        //            }
        //            IOUtil.close(resultSet);
        //            IOUtil.close(statement);
        //
        //            sql = "SELECT * FROM " + DBUtil.wrap(schema, viewName, DBDialect.DAMENG) + " FETCH FIRST 1 ROWS ONLY";
        //            this.printSql(sql);
        //            PreparedStatement statement1 = this.getConnManager().connection(schema).prepareStatement(sql);
        //            ResultSet resultSet1 = statement1.executeQuery();
        //            DBUtil.printMetaData(resultSet1);
        //            DamengColumns dbColumns = DamengHelper.parseColumns(resultSet1);
        //            IOUtil.close(resultSet1);
        //            IOUtil.close(statement1);
        //
        //            // 初始化状态
        //            for (DamengColumn value : columns.values()) {
        //                DamengColumn dbColumn = dbColumns.column(value.getName());
        //                if (dbColumn != null) {
        //                    value.setNullable(dbColumn.isNullable());
        //                    value.setAutoIncrement(dbColumn.isAutoIncrement());
        //                }
        //                value.setTableName(viewName);
        //                value.initStatus();
        //            }
        //            // 返回排序后的数据
        //            return CollectionUtil.sort(columns.values(), Comparator.comparingInt(DamengColumn::getPosition));
        //        } catch (Exception ex) {
        //            ex.printStackTrace();
        //            throw new ShellException(ex);
        //        }

        DamengSelectColumnParam param = new DamengSelectColumnParam();
        param.setSchema(schema);
        param.setTableName(viewName);
        DamengColumns columns = this.selectColumns(param);
        try {

            Connection conn = this.getConnManager().connection(schema);

            // 2. 获取 DatabaseMetaData 对象
            DatabaseMetaData dbmd = conn.getMetaData();
            ResultSet resultSet = dbmd.getColumns(null, schema, viewName, null);
            DBUtil.printMetaData(resultSet);
            while (resultSet.next()) {
                // 关键: 读取 IS_AUTOINCREMENT 列
                String columnName = resultSet.getString("COLUMN_NAME");
                String isAutoIncrement = resultSet.getString("IS_AUTOINCREMENT");
                boolean isIdentity = "YES".equals(isAutoIncrement);
                DamengColumn column = columns.column(columnName);
                column.setAutoIncrement(isIdentity);
            }
            IOUtil.close(resultSet);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
        return columns;
    }

    public List<DamengRecord> viewRecords(String schema, String viewName, Long start, Long limit, List<DamengRecordFilter> filters) {
        try {
            Connection connection = this.getConnManager().connection(schema);
            StringBuilder builder = new StringBuilder("SELECT * FROM ");
            builder.append(DBUtil.wrap(schema, viewName, DBDialect.DAMENG));
            String filterCondition = DamengConditionUtil.buildCondition(filters);
            if (StringUtil.isNotBlank(filterCondition)) {
                builder.append(" WHERE ").append(filterCondition);
            }
            if (start != null && limit != null) {
                builder.append(" OFFSET ").append(start).append(" ROWS FETCH NEXT ").append(limit).append(" ROWS ONLY");
            }
            String sql = builder.toString();
            this.printSql(sql);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            DBUtil.printMetaData(resultSet);
            List<DamengRecord> records = new ArrayList<>();
            boolean updatable = ShellDamengHelper.isViewUpdatable(connection, schema, viewName);
            DamengColumns columns = ShellDamengHelper.parseColumns(resultSet);
            for (DamengColumn column : columns) {
                column.setTableName(viewName);
            }
            while (resultSet.next()) {
                DamengRecord record = new DamengRecord(columns, !updatable);
                for (DamengColumn column : columns) {
                    Object data = resultSet.getObject(column.getName());
                    //                    // 获取几何值
                    //                    if (column.supportGeometry()) {
                    //                        data = DamengHelper.getGeometryString(connection, data);
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

    public void createTable(DamengCreateTableParam param) {
        Connection connection = null;
        try {
            String schema = param.schema();
            connection = this.getConnManager().connection(schema);
            Statement statement = connection.createStatement();
            List<String> sqlList = DamengTableCreateSqlGenerator.generateSql(param);
            connection.setAutoCommit(false);
            for (String sqlStr : sqlList) {
                this.printSql(sqlStr);
                statement.executeUpdate(sqlStr);
            }
            connection.commit();
            IOUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            DBUtil.rollback(connection);
            throw new ShellException(ex);
        }
    }

    public void alertTable(DamengAlertTableParam param) {
        Connection connection = null;
        try {
            List<String> sqlList = DamengTableAlertSqlGenerator.generateSql(param);
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

    public void renameTable(String schema, String oldTableName, String newTableName) {
        try {
            String sql = "ALTER TABLE " + DBUtil.wrap(schema, oldTableName, DBDialect.DAMENG)
                    + " RENAME TO " + DBUtil.wrap(newTableName, DBDialect.DAMENG);
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
     * 重命名函数
     *
     * @param schema          库名称
     * @param oldFunctionName 函数名称
     * @param newFunctionName 新函数名称
     */
    public void renameFunction(String schema, String oldFunctionName, String newFunctionName) {
        try {
            this.cloneFunction(schema, oldFunctionName, newFunctionName);
            DamengFunction function = new DamengFunction();
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
            DamengProcedure procedure = new DamengProcedure();
            procedure.setSchema(schema);
            procedure.setName(oldProcedureName);
            this.dropProcedure(procedure);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public void clearTable(String schema, String tableName) {
        try {
            Statement statement = this.getConnManager().connection(schema).createStatement();
            String sql = "DELETE FROM " + DBUtil.wrap(schema, tableName, DBDialect.DAMENG);
            this.printSql(sql);
            statement.executeUpdate(sql);
            IOUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public void truncateTable(String schema, String tableName) {
        try {
            Statement statement = this.getConnManager().connection(schema).createStatement();
            String sql = "TRUNCATE TABLE " + DBUtil.wrap(schema, tableName, DBDialect.DAMENG);
            this.printSql(sql);
            statement.executeUpdate(sql);
            IOUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public void dropTable(String schema, String tableName) {
        try {
            Statement statement = this.getConnManager().connection(schema).createStatement();
            String sql = "DROP TABLE " + DBUtil.wrap(schema, tableName, DBDialect.DAMENG);
            this.printSql(sql);
            statement.executeUpdate(sql);
            IOUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    //    public List<String> charsets() {
    //        if (this.hasProperty("charsets")) {
    //            return this.getProperty("charsets");
    //        }
    //        try {
    //            // 达梦常用字符集列表（系统视图查询可能因版本差异失败，直接返回静态列表）
    //            List<String> charsets = new ArrayList<>();
    //            charsets.add("UTF-8");
    //            charsets.add("GBK");
    //            charsets.add("GB18030");
    //            charsets.add("GB2312");
    //            charsets.add("ISO-8859-1");
    //            this.putProperty("charsets", charsets);
    //            return charsets;
    //        } catch (Exception ex) {
    //            throw new ShellException(ex);
    //        }
    //    }

    //    public List<String> collation(String charset) {
    //        try {
    //            Map<String, List<String>> collations = this.getProperty("collation");
    //            if (collations == null) {
    //                collations = new HashMap<>();
    //                this.putProperty("collations", collations);
    //            }
    //            charset = charset.toUpperCase();
    //            if (collations.containsKey(charset)) {
    //                return collations.get(charset.toUpperCase());
    //            }
    //            // 达梦排序规则：返回默认列表
    //            List<String> list = new ArrayList<>();
    //            list.add("BINARY");
    //            list.add("BINARY_CI");
    //            collations.put(charset, list);
    //            return list;
    //        } catch (Exception ex) {
    //            throw new ShellException(ex);
    //        }
    //    }

    public boolean existSchema(String schema) {
        boolean result = false;
        try {
            String sql;
            //            if (this.isDbaRole()) {
            //                sql = "SELECT COUNT(*) FROM SYSOBJECTS WHERE TYPE$ = 'SCH' AND NAME = ?";
            //            } else {
            sql = "SELECT COUNT(*) FROM ALL_OBJECTS WHERE OBJECT_TYPE = 'SCH' AND OBJECT_NAME = ?;";
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

    public void createSchema(DamengSchema schema) {
        try {
            StringBuilder builder = new StringBuilder("CREATE SCHEMA ");
            builder.append(DBUtil.wrap(schema.getName(), DBDialect.DAMENG));
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

    public boolean alterSchema(DamengSchema schema) {
        try {
            return true;
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    public boolean dropSchema(String schema) {
        try {
            String sql = "DROP SCHEMA " + DBUtil.wrap(schema, DBDialect.DAMENG) + " CASCADE";
            this.printSql(sql);
            Statement statement = this.getConnManager().connection().createStatement();
            statement.executeUpdate(sql);
            IOUtil.close(statement);
            return true;
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    public DBQueryResults<DamengExplainResult> explainSql(String schema, String sql) {
        DBQueryResults<DamengExplainResult> results = new DBQueryResults<>();
        Connection connection = null;
        try {
            this.printSql(sql);
            DBSqlParser parser = DBSqlParser.getParser(sql, DBDialect.DAMENG);
            List<String> list = parser.parseSql();
            connection = this.getConnManager().connection(schema);
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();
            for (String execSql : list) {
                DamengExplainResult result = new DamengExplainResult();
                try {
                    execSql = "EXPLAIN FOR " + execSql.stripLeading();
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
            ex.printStackTrace();
            DBUtil.rollback(connection);
            results.parseError(ex);
        }
        return results;
    }

    public DamengExecuteResult executeSingleSql(String schema, String sql) {
        Connection connection = null;
        DamengExecuteResult result = new DamengExecuteResult();
        result.setContent(sql);
        try {
            this.printSql(sql);
            DBSqlParser parser = DBSqlParser.getParser(sql, DBDialect.DAMENG);
            String execSql = parser.parseSingleSql();
            connection = this.getConnManager().connection(schema);
            Statement statement = connection.createStatement();
            try {
                long startTime = System.nanoTime();
                boolean isQuery = statement.execute(execSql);
                if (isQuery) {
                    ResultSet resultSet = statement.getResultSet();
                    if (parser.isSingle()) {
                        result.setFullColumn(parser.isFullColumn());
                    } else {
                        result.setFullColumn(DBUtil.isFullColumn(this.dialect(), execSql));
                    }
                    result.parseResult(resultSet, connection, !parser.isSelect());
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

    public void executeSqlSimple(String schema, String sql) {
        Connection connection = null;
        try {
            this.printSql(sql);
            connection = this.getConnManager().connection(schema);
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

    public int insertBatch(String schema, List<String> sqlList, boolean parallel) {
        Connection connection = null;
        int result = 0;
        try {
            connection = parallel ? this.getConnManager().newConnection(schema) : this.getConnManager().connection(schema);
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();
            for (String sql : sqlList) {
                this.printSql(sql);
                statement.addBatch(sql);
            }
            int[] results = statement.executeBatch();
            connection.commit();
            IOUtil.close(statement);
            if (parallel) {
                IOUtil.close(connection);
            }
            for (int i : results) {
                result += i;
            }
        } catch (Exception ex) {
            JulLog.warn("sqlList:{}", sqlList);
            ex.printStackTrace();
            DBUtil.rollback(connection);
            throw new ShellException(ex);
        }
        return result;
    }

    public DbType dbType() {
        return DBDialect.DAMENG.dbType();
    }

    @Override
    /**
     * 获取数据库方言
     *
     * @return 数据库方言
     */
    public DBDialect dialect() {
        return DBDialect.DAMENG;
    }

    /**
     * 查询函数列表
     *
     * @param schema 模式名称
     * @return 函数列表
     */
    public List<DamengFunction> selectFunctions(String schema) {
        DamengSelectFunctionParam param = new DamengSelectFunctionParam();
        param.setFull(true);
        param.setSchema(schema);
        return this.selectFunctions(param);
    }

    public List<DamengFunction> selectFunctionsSimple(String schema) {
        DamengSelectFunctionParam param = new DamengSelectFunctionParam();
        param.setFull(false);
        param.setSchema(schema);
        return this.selectFunctions(param);
    }

    public List<DamengFunction> selectFunctions(DamengSelectFunctionParam param) {
        try {
            String schema = param.getSchema();
            List<DamengFunction> list = new ArrayList<>();
            String sql = """
                    SELECT
                        O.OBJECT_NAME AS "ROUTINE_NAME",
                        P.AUTHID AS "SECURITY_TYPE",
                        P.AGGREGATE,
                        P.PIPELINED,
                        P.DETERMINISTIC
                    FROM 
                        ALL_OBJECTS O
                    LEFT JOIN 
                        ALL_PROCEDURES P ON O.OWNER = P.OWNER AND O.OBJECT_NAME = P.OBJECT_NAME
                    WHERE 
                        O.OWNER = ?
                    AND 
                        O.OBJECT_TYPE = 'FUNCTION'
                    """;
            this.printSql(sql);
            PreparedStatement statement = this.getConnManager().functionConnection(schema).prepareStatement(sql);
            statement.setString(1, schema);
            // 执行SQL查询并获取结果集
            ResultSet resultSet = statement.executeQuery();
            // 打印元数据
            DBUtil.printMetaData(resultSet);
            // 遍历结果集
            while (resultSet.next()) {
                DamengFunction function = new DamengFunction();
                String name = resultSet.getString("ROUTINE_NAME");
                List<DamengRoutineParam> params = this.listFunctionParam(schema, name);
                String securityType = resultSet.getString("SECURITY_TYPE");
                String aggregate = resultSet.getString("AGGREGATE");
                String pipelined = resultSet.getString("PIPELINED");
                String deterministic = resultSet.getString("DETERMINISTIC");
                StringBuilder characteristic = new StringBuilder();
                if (param.isFull()) {
                    String createDefinition = this.showCreateFunction(schema, name);
                    String definition = createDefinition.substring(createDefinition.indexOf("\nAS\n") + 4);
                    if (StringUtil.contains(createDefinition, "PARALLEL_ENABLE")) {
                        characteristic.append(",PARALLEL_ENABLE");
                    }
                    if (StringUtil.contains(createDefinition, "RESULT_CACHE")) {
                        characteristic.append(",RESULT_CACHE");
                    }
                    function.setDefinition(definition);
                    function.setCreateDefinition(createDefinition);
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
                    function.setCharacteristic(characteristic.substring(1));
                }
                function.setName(name);
                function.setSchema(schema);
                function.setParams(params);
                function.setSecurityType(securityType);
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
     * 查询存储过程列表
     *
     * @param schema 模式名称
     * @return 存储过程列表
     */
    public List<DamengProcedure> selectProcedures(String schema) {
        DamengSelectProcedureParam param = new DamengSelectProcedureParam();
        param.setFull(true);
        param.setSchema(schema);
        return this.selectProcedures(param);
    }

    public List<DamengProcedure> selectProceduresSimple(String schema) {
        DamengSelectProcedureParam param = new DamengSelectProcedureParam();
        param.setFull(false);
        param.setSchema(schema);
        return this.selectProcedures(param);
    }

    public List<DamengProcedure> selectProcedures(DamengSelectProcedureParam param) {
        try {
            String schema = param.getSchema();
            List<DamengProcedure> list = new ArrayList<>();
            String sql = """
                    SELECT
                        O.OBJECT_NAME AS "ROUTINE_NAME",
                        P.AUTHID AS "SECURITY_TYPE",
                        P.AGGREGATE,
                        P.PIPELINED,
                        P.DETERMINISTIC
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
            PreparedStatement statement = this.getConnManager().procedureConnection(schema).prepareStatement(sql);
            statement.setString(1, schema);
            // 执行SQL查询并获取结果集
            ResultSet resultSet = statement.executeQuery();
            // 打印元数据
            DBUtil.printMetaData(resultSet);
            // 遍历结果集
            while (resultSet.next()) {
                DamengProcedure procedure = new DamengProcedure();
                String name = resultSet.getString("ROUTINE_NAME");
                String aggregate = resultSet.getString("AGGREGATE");
                String pipelined = resultSet.getString("PIPELINED");
                String securityType = resultSet.getString("SECURITY_TYPE");
                String deterministic = resultSet.getString("DETERMINISTIC");
                StringBuilder characteristic = new StringBuilder();
                if (param.isFull()) {
                    List<DamengRoutineParam> params = this.listProcedureParam(schema, name);
                    procedure.setParams(params);
                    String createDefinition = this.showCreateProcedure(schema, name);
                    String definition = createDefinition.substring(createDefinition.indexOf("\nAS\n") + 4);
                    if (StringUtil.contains(createDefinition, "PARALLEL_ENABLE")) {
                        characteristic.append(",PARALLEL_ENABLE");
                    }
                    if (StringUtil.contains(createDefinition, "RESULT_CACHE")) {
                        characteristic.append(",RESULT_CACHE");
                    }
                    procedure.setDefinition(definition);
                    procedure.setCreateDefinition(createDefinition);
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
                procedure.setName(name);
                procedure.setSchema(schema);
                procedure.setSecurityType(securityType);
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

    public DamengProcedure selectProcedure(String schema, String procedureName) {
        DamengSelectProcedureParam param = new DamengSelectProcedureParam();
        param.setFull(true);
        param.setSchema(schema);
        param.setProcedureName(procedureName);
        return this.selectProcedure(param);
    }

    public DamengProcedure selectProcedureSimple(String schema, String procedureName) {
        DamengSelectProcedureParam param = new DamengSelectProcedureParam();
        param.setFull(false);
        param.setSchema(schema);
        param.setProcedureName(procedureName);
        return this.selectProcedure(param);
    }

    public DamengProcedure selectProcedure(DamengSelectProcedureParam param) {
        try {
            String schema = param.getSchema();
            String produceName = param.getProcedureName();
            String sql = """
                    SELECT
                        P.AUTHID AS "SECURITY_TYPE",
                        P.AGGREGATE,
                        P.PIPELINED,
                        P.DETERMINISTIC
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
            PreparedStatement statement = this.getConnManager().procedureConnection(schema).prepareStatement(sql);
            statement.setString(1, schema);
            statement.setString(2, produceName);
            // 执行SQL查询并获取结果集
            ResultSet resultSet = statement.executeQuery();
            // 打印元数据
            DBUtil.printMetaData(resultSet);
            DamengProcedure procedure = new DamengProcedure();
            procedure.setSchema(schema);
            procedure.setName(produceName);
            // 遍历结果集
            while (resultSet.next()) {
                String aggregate = resultSet.getString("AGGREGATE");
                String pipelined = resultSet.getString("PIPELINED");
                String securityType = resultSet.getString("SECURITY_TYPE");
                String deterministic = resultSet.getString("DETERMINISTIC");
                StringBuilder characteristic = new StringBuilder();
                if (param.isFull()) {
                    List<DamengRoutineParam> params = this.listProcedureParam(schema, produceName);
                    procedure.setParams(params);
                    String createDefinition = this.showCreateProcedure(schema, produceName);
                    String definition = createDefinition.substring(createDefinition.indexOf("\nAS\n") + 4);
                    if (StringUtil.contains(createDefinition, "PARALLEL_ENABLE")) {
                        characteristic.append(",PARALLEL_ENABLE");
                    }
                    if (StringUtil.contains(createDefinition, "RESULT_CACHE")) {
                        characteristic.append(",RESULT_CACHE");
                    }
                    procedure.setDefinition(definition);
                    procedure.setCreateDefinition(createDefinition);
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
                procedure.setSecurityType(securityType);
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

    public void dropProcedure(DamengProcedure routine) {
        try {
            String sql = "DROP PROCEDURE IF EXISTS " + DBUtil.wrap(routine.getSchema(), routine.getName(), DBDialect.DAMENG);
            this.printSql(sql);
            Statement statement = this.getConnManager().procedureConnection(routine.getSchema()).createStatement();
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
    public void createProcedure(DamengCreateProcedureParam param) {
        try {
            String sql = DamengProcedureCreateSqlGenerator.generateSqlSingle(param);
            this.printSql(sql);
            Statement statement = this.getConnManager().procedureConnection(param.getSchema()).createStatement();
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
    public void alertProcedure(DamengAlertProcedureParam param) {
        try {
            String sql = DamengProcedureAlertSqlGenerator.generateSqlSingle(param);
            this.printSql(sql);
            Statement statement = this.getConnManager().procedureConnection(param.getSchema()).createStatement();
            statement.executeUpdate(sql);
            IOUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public void dropFunction(DamengFunction function) {
        try {
            String sql = "DROP function IF EXISTS " + DBUtil.wrap(function.getSchema(), function.getName(), DBDialect.DAMENG);
            this.printSql(sql);
            Statement statement = this.getConnManager().functionConnection(function.getSchema()).createStatement();
            statement.executeUpdate(sql);
            IOUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public DamengFunction selectFunction(String schema, String functionName) {
        DamengSelectFunctionParam param = new DamengSelectFunctionParam();
        param.setFull(true);
        param.setSchema(schema);
        param.setFunctionName(functionName);
        return this.selectFunction(param);
    }

    public DamengFunction selectFunctionSimple(String schema, String functionName) {
        DamengSelectFunctionParam param = new DamengSelectFunctionParam();
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
    public DamengFunction selectFunction(DamengSelectFunctionParam param) {
        try {
            String schema = param.getSchema();
            String functionName = param.getFunctionName();
            String sql = """
                    SELECT
                        P.AUTHID AS "SECURITY_TYPE",
                        P.AGGREGATE,
                        P.PIPELINED,
                        P.DETERMINISTIC
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
            PreparedStatement statement = this.getConnManager().functionConnection(schema).prepareStatement(sql);
            statement.setString(1, schema);
            statement.setString(2, functionName);
            // 执行SQL查询并获取结果集
            ResultSet resultSet = statement.executeQuery();
            // 打印元数据
            DBUtil.printMetaData(resultSet);
            DamengFunction function = new DamengFunction();
            function.setSchema(schema);
            function.setName(functionName);
            // 遍历结果集
            while (resultSet.next()) {
                String aggregate = resultSet.getString("AGGREGATE");
                String pipelined = resultSet.getString("PIPELINED");
                String securityType = resultSet.getString("SECURITY_TYPE");
                String deterministic = resultSet.getString("DETERMINISTIC");
                StringBuilder characteristic = new StringBuilder();
                if (param.isFull()) {
                    List<DamengRoutineParam> params = this.listFunctionParam(schema, functionName);
                    function.setParams(params);
                    String createDefinition = this.showCreateFunction(schema, functionName);
                    String definition = createDefinition.substring(createDefinition.indexOf("\nAS\n") + 4);
                    if (StringUtil.contains(createDefinition, "PARALLEL_ENABLE")) {
                        characteristic.append(",PARALLEL_ENABLE");
                    }
                    if (StringUtil.contains(createDefinition, "RESULT_CACHE")) {
                        characteristic.append(",RESULT_CACHE");
                    }
                    function.setDefinition(definition);
                    function.setCreateDefinition(createDefinition);
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
                    function.setCharacteristic(characteristic.substring(1));
                }
                function.setSchema(schema);
                function.setSecurityType(securityType);
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
    public void createFunction(DamengCreateFunctionParam param) {
        try {
            String sql = DamengFunctionCreateSqlGenerator.generateSqlSingle(param);
            this.printSql(sql);
            Statement statement = this.getConnManager().functionConnection(param.getSchema()).createStatement();
            statement.executeUpdate(sql);
            IOUtil.close(statement);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    public DamengRecord selectRecord(DamengSelectRecordParam param) {
        try {
            String schema = param.getSchema();
            String tableName = param.getTableName();
            Connection connection = this.getConnManager().connection(schema);
            DamengRecordPrimaryKey primaryKey = param.getPrimaryKey();
            StringBuilder builder = new StringBuilder("SELECT * FROM ");
            builder.append(DBUtil.wrap(schema, tableName, DBDialect.DAMENG))
                    .append(" WHERE ")
                    .append(DBUtil.wrap(primaryKey.getColumnName(), DBDialect.DAMENG))
                    .append(" = ?");
            String sql = builder.toString();
            this.printSql(sql);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setObject(1, primaryKey.data());
            ResultSet resultSet = statement.executeQuery();
            DBUtil.printMetaData(resultSet);
            DamengColumns columns = ShellDamengHelper.parseColumns(resultSet);
            DamengRecord record = new DamengRecord(columns);
            while (resultSet.next()) {
                for (DamengColumn column : columns) {
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

    public String selectClientCharacter() {
        // 达梦客户端字符集，返回默认值
        return "UTF-8";
    }

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
            String sql = "SELECT * FROM " + DBUtil.wrap(schema, tableName, DBDialect.DAMENG) + " WHERE 1=0";
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
        // 查询检查
        DBObjects<DamengCheck> checks = this.checks(schema, tableName);
        //        // 查询索引
        //        DamengIndexes indexes = this.indexes(schema, tableName);
        // 查询触发器
        DBObjects<DamengTrigger> triggers = this.selectTriggers(schema, tableName);
        // 查询外键
        DBObjects<DamengForeignKey> foreignKeys = this.foreignKeys(schema, tableName);
        if (checks != null) {
            for (DamengCheck check : checks) {
                check.setName(check.getName() + DBUtil.genCloneName());
                check.clearStatus();
                check.clearOriginalData();
                check.setCreated(true);
            }
        }
        if (triggers != null) {
            for (DamengTrigger trigger : triggers) {
                trigger.setName(trigger.getName() + DBUtil.genCloneName());
                trigger.clearStatus();
                trigger.clearOriginalData();
                trigger.setCreated(true);
            }
        }
        //         if (indexes != null) {
        //             for (DamengIndex index : indexes) {
        //                 index.setName(index.getName() + DBUtil.genCloneName());
        //                 index.clearStatus();
        //                 index.clearOriginalData();
        //                 index.setCreated(true);
        //             }
        //         }
        if (foreignKeys != null) {
            for (DamengForeignKey foreignKey : foreignKeys) {
                foreignKey.setName(foreignKey.getName() + DBUtil.genCloneName());
                foreignKey.clearStatus();
                foreignKey.clearOriginalData();
                foreignKey.setCreated(true);
            }
        }

        try {
            Connection connection = this.getConnManager().connection(schema);
            // 克隆基本的表结构
            //            String sql = "CREATE TABLE " + DBUtil.wrap(schema, newTableName, DBDialect.DAMENG);
            //            if (!includeRecord) {
            //                sql = sql + " AS SELECT * FROM " + DBUtil.wrap(schema, tableName, DBDialect.DAMENG);
            //                sql = sql + " WHERE 1 = 0";
            //            } else {
            //                sql = sql + " LIKE " + DBUtil.wrap(schema, tableName, DBDialect.DAMENG);
            //            }
            String sql = this.showCreateTable(schema, tableName);
            sql = sql.replace(DBUtil.wrap(schema, tableName, DBDialect.DAMENG), DBUtil.wrap(schema, newTableName, DBDialect.DAMENG));
            this.printSql(sql);
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.execute();
            IOUtil.close(stmt);
            DamengTable table = new DamengTable();
            table.setSchema(schema);
            table.setName(newTableName);

            // 克隆表结构的检查、外键、触发器
            DamengAlertTableParam alertTableParam = new DamengAlertTableParam();
            alertTableParam.setTable(table);
            alertTableParam.setChecks(checks);
            alertTableParam.setTriggers(triggers);
            alertTableParam.setForeignKeys(foreignKeys);
            this.alertTable(alertTableParam);

            // 克隆数据
            if (includeRecord) {
                // 查询字段列表
                DamengSelectColumnParam param = new DamengSelectColumnParam();
                param.setSchema(schema);
                param.setTableName(newTableName);
                DamengColumns columns = this.selectColumns(param);
                StringBuilder builder = new StringBuilder();
                for (DamengColumn column : columns) {
                    builder.append(DBUtil.wrap(column.getName(), DBDialect.DAMENG))
                            .append(" ,");
                }
                StringUtil.deleteLast(builder, ",");

                // 处理数据
                String sql1 = "SET IDENTITY_INSERT " + DBUtil.wrap(newTableName, DBDialect.DAMENG) + " ON;";
                String sql2 = "INSERT INTO " + DBUtil.wrap(schema, newTableName, this.dialect())
                        + "(" + builder + ")"
                        + " SELECT " + builder + " FROM " + DBUtil.wrap(schema, tableName, this.dialect());
                String sql3 = "SET IDENTITY_INSERT " + DBUtil.wrap(newTableName, DBDialect.DAMENG) + " OFF;";
                connection.setAutoCommit(false);
                Statement statement = connection.createStatement();
                this.printSql(sql1);
                statement.execute(sql1);
                this.printSql(sql2);
                statement.executeUpdate(sql2);
                this.printSql(sql3);
                statement.execute(sql3);
                connection.commit();
                IOUtil.close(statement);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
        return newTableName;
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
            sql = sql.replace("VIEW `" + viewName + "`", "VIEW `" + newViewName + "`");
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
            sql = sql.replace("FUNCTION `" + functionName + "`", "FUNCTION `" + newFunctionName + "`");
            this.printSql(sql);
            Connection connection = this.getConnManager().functionConnection(schema);
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
            sql = sql.replace("PROCEDURE `" + procedureName + "`", "PROCEDURE `" + newProcedureName + "`");
            this.printSql(sql);
            Connection connection = this.getConnManager().procedureConnection(schema);
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.execute();
            IOUtil.close(stmt);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ShellException(ex);
        }
    }

    //    /**
    //     * 克隆事件
    //     *
    //     * @param schema       数据库
    //     * @param eventName    事件名称
    //     * @param newEventName 新事件名称
    //     */
    //    public void cloneEvent(String schema, String eventName, String newEventName) {
    //        String sql = this.showCreateEvent(schema, eventName);
    //        try {
    //            sql = sql.replace("EVENT `" + eventName + "`", "EVENT `" + newEventName + "`");
    //            this.printSql(sql);
    //            Connection connection = this.getConnManager().connection(schema);
    //            PreparedStatement stmt = connection.prepareStatement(sql);
    //            stmt.execute();
    //            IOUtil.close(stmt);
    //        } catch (Exception ex) {
    //            ex.printStackTrace();
    //            throw new ShellException(ex);
    //        }
    //    }

    public List<DamengRoutineParam> listRoutineParam(String schema, String routineName, String routineType) throws Exception {
        try {
            Connection connection = this.getConnManager().connection(schema);
            String sql = """
                        SELECT
                    	a.POSITION,
                    	a.DATA_TYPE,
                    	a.DATA_LENGTH AS SIZE,
                    	a.DATA_SCALE AS DIGITS,
                    	a.IN_OUT AS PARAMETER_MODE,
                    	a.ARGUMENT_NAME AS PARAMETER_NAME,
                    	a.CHARACTER_SET_NAME
                    FROM
                    	ALL_ARGUMENTS a
                    WHERE
                    	a.OWNER = ?
                    AND
                    	a.OBJECT_NAME = ?
                    AND
                    	a.PACKAGE_NAME IS NULL
                    
                    """;
            List<DamengRoutineParam> params = new
                    ArrayList<>();
            PreparedStatement statement = connection.
                    prepareStatement(sql);
            statement.setString(1, schema);
            statement.
                    setString(2,
                            routineName);
            // 执行SQL查询并获取结果集
            ResultSet resultSet = statement.executeQuery
                    ();
            while (resultSet.next()) {
                DamengRoutineParam param = new DamengRoutineParam();
                param.setSize(resultSet.getInt("SIZE"));
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
            return params;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Collections.emptyList();
    }

    public List<DamengRoutineParam> listFunctionParam(String schema, String functionName) throws Exception {
        return listRoutineParam(schema, functionName, "FUNCTION");
    }

    public List<DamengRoutineParam> listProcedureParam(String schema, String procedureName) throws Exception {
        return listRoutineParam(schema, procedureName, "PROCEDURE");
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
     * 打印sql
     *
     * @param sql sql
     */
    private void printSql(String sql) {
        DBUtil.printSql(sql);
        String compressedSql = sql;
        try {
            // 压缩sql
            SQLUtils.FormatOption formatOption = new SQLUtils.FormatOption();
            formatOption.setUppCase(true);
            formatOption.setPrettyFormat(false);
            compressedSql = SQLUtils.format(sql, this.dbType(), formatOption);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        ShellEventUtil.printSql(compressedSql, this.shellConnect);
    }
}
