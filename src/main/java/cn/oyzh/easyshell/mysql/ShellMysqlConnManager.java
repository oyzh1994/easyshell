package cn.oyzh.easyshell.mysql;


import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.StringUtil;
import com.mysql.cj.conf.PropertyKey;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


/**
 * 连接管理器
 *
 * @author oyzh
 * @since 2024/01/28
 */
public class ShellMysqlConnManager implements AutoCloseable {

    /**
     * 配置
     */
    private ShellMysqlConnConfig config;

    // /**
    //  * 服务连接
    //  */
    // private Connection serverConnection;

    /**
     * 库连接
     */
    private Map<String, Connection> connections = new HashMap<>();

    /**
     * 添加连接
     *
     * @param dbName     数据库
     * @param connection 连接
     */
    public void addConnection(String dbName, Connection connection) {
        this.connections.put("db_connection_" + dbName, connection);
    }

    /**
     * 添加函数连接
     *
     * @param dbName     数据库
     * @param connection 数据库
     */
    public void addFunctionConnection(String dbName, Connection connection) {
        this.connections.put("function_connection_" + dbName, connection);
    }

    /**
     * 添加过程连接
     *
     * @param dbName     数据库
     * @param connection 数据库
     */
    public void addProcedureConnection(String dbName, Connection connection) {
        this.connections.put("procedure_connection_" + dbName, connection);
    }

    /**
     * 获取连接
     *
     * @param dbName 数据库
     * @return 结果
     */
    public Connection getConnection(String dbName) {
        return this.connections.get(dbName);
    }

    /**
     * 获取函数连接
     *
     * @param dbName 数据库
     * @return 结果
     */
    public Connection getFunctionConnection(String dbName) {
        return this.connections.get("function_connection_" + dbName);
    }

    /**
     * 获取过程连接
     *
     * @param dbName 数据库
     * @return 结果
     */
    public Connection getProcedureConnection(String dbName) {
        return this.connections.get("procedure_connection_" + dbName);
    }

    // public boolean hasConnection(String dbName) {
    //     return this.connections.containsKey(dbName);
    // }

    @Override
    public void close() {
        // if (this.serverConnection != null) {
        //     try {
        //         this.serverConnection.close();
        //     } catch (SQLException ignored) {
        //     }
        // }
        // this.serverConnection = null;
        for (Connection connection : this.connections.values()) {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
        }
        this.connections.clear();
        this.connections = null;
        this.config = null;
    }

    /**
     * 获取服务连接
     *
     * @return 服务连接
     */
    public Connection getServerConnection() {
        // return serverConnection;
        return this.connections.get("server_connection");
    }

    /**
     * 设置服务连接
     *
     * @param serverConnection 服务连接
     */
    public void setServerConnection(Connection serverConnection) {
        // this.serverConnection = serverConnection;
        this.connections.put("server_connection", serverConnection);
    }

    /**
     * 获取连接列表
     *
     * @return 连接列表
     */
    public Map<String, Connection> getConnections() {
        return connections;
    }

    /**
     * 是否有效
     *
     * @param connection 连接
     * @return 结果
     * @throws SQLException 异常
     */
    public boolean isValid(Connection connection) throws SQLException {
        if (connection == null || connection.isClosed()) {
            return false;
        }
        return connection.isValid(this.getConnectTimeout() / 1000);
    }

    /**
     * 执行连接
     *
     * @return 结果
     * @throws SQLException           异常
     * @throws ClassNotFoundException 异常
     */
    public Connection connection() throws SQLException, ClassNotFoundException {
        Connection connection = this.getServerConnection();
        if (!this.isValid(connection)) {
            connection = this.initConnection(null, this.config.getUser(), this.config.getPassword());
            this.setServerConnection(connection);
        }
        return connection;
    }

    /**
     * 执行连接
     *
     * @param dbName 数据库
     * @return 结果
     * @throws SQLException           异常
     * @throws ClassNotFoundException 异常
     */
    public Connection connection(String dbName) throws SQLException, ClassNotFoundException {
        Connection connection = this.getConnection(dbName);
        if (!this.isValid(connection)) {
            connection = this.initConnection(dbName, this.config.getUser(), this.config.getPassword());
            this.addConnection(dbName, connection);
        }
        connection.setAutoCommit(true);
        return connection;
    }

    /**
     * 执行函数连接
     *
     * @param dbName 数据库
     * @return 结果
     * @throws SQLException           异常
     * @throws ClassNotFoundException 异常
     */
    public Connection functionConnection(String dbName) throws SQLException, ClassNotFoundException {
        Connection connection = this.getFunctionConnection(dbName);
        if (!this.isValid(connection)) {
            connection = this.initConnection(dbName, this.config.getUser(), this.config.getPassword());
            this.addFunctionConnection(dbName, connection);
        }
        connection.setAutoCommit(true);
        return connection;
    }

    /**
     * 执行过程连接
     *
     * @param dbName 数据库
     * @return 结果
     * @throws SQLException           异常
     * @throws ClassNotFoundException 异常
     */
    public Connection procedureConnection(String dbName) throws SQLException, ClassNotFoundException {
        Connection connection = this.getProcedureConnection(dbName);
        if (!this.isValid(connection)) {
            connection = this.initConnection(dbName, this.config.getUser(), this.config.getPassword());
            this.addProcedureConnection(dbName, connection);
        }
        connection.setAutoCommit(true);
        return connection;
    }

    /**
     * 执行新连接
     *
     * @param dbName 数据库
     * @return 结果
     * @throws SQLException           异常
     * @throws ClassNotFoundException 异常
     */
    public Connection newConnection(String dbName) throws SQLException, ClassNotFoundException {
        Connection connection = this.initConnection(dbName, this.config.getUser(), this.config.getPassword());
        connection.setAutoCommit(true);
        return connection;
    }

    /**
     * 初始化连接
     *
     * @param dbName   数据库
     * @param user     用户名
     * @param password 密码
     * @return 结果
     * @throws SQLException           异常
     * @throws ClassNotFoundException 异常
     */
    public Connection initConnection(String dbName, String user, String password) throws ClassNotFoundException, SQLException {
        // 加载JDBC驱动
        Class.forName("com.mysql.cj.jdbc.Driver");
        String host = this.getConnectionString();
        if (dbName != null) {
            host += dbName;
        }
        Properties props = new Properties();
        props.put(PropertyKey.USER.getKeyName(), user);
        props.put(PropertyKey.PASSWORD.getKeyName(), password);
        // props.put(PropertyKey.tcpNoDelay.getKeyName(), "true");
        // props.put(PropertyKey.tcpKeepAlive.getKeyName(), "true");
        // props.put(PropertyKey.autoReconnect.getKeyName(), "true");
        // props.put(PropertyKey.zeroDateTimeBehavior.getKeyName(), "convertToNull");
        // 代理配置
        if (this.config.getSocketFactory() != null) {
            props.put("_proxyType", this.config.getProxyType());
            props.put("_proxyHost", this.config.getProxyHost());
            props.put("_proxyUser", this.config.getProxyUser());
            props.put("_proxyPort", this.config.getProxyPort() + "");
            props.put("_proxyPassword", this.config.getProxyPassword());
            props.put(PropertyKey.socketFactory.getKeyName(), this.config.getSocketFactory());
        }
        // 预设环境参数设置
        props.put(PropertyKey.useSSL.getKeyName(), this.config.isUseSSL() ? "true" : "false");
        props.put(PropertyKey.socketTimeout.getKeyName(), this.getConnectTimeout() + "");
        props.put(PropertyKey.connectTimeout.getKeyName(), this.getConnectTimeout() + "");
        props.putAll(ShellMysqlHelper.DEFAULT_ENVIRONMENT);

        // 自定义环境参数设置
        String envs = this.config.getEnv();
        if (StringUtil.isNotBlank(envs)) {
            envs.lines().forEach(line -> {
                String[] split = line.split("=");
                props.put(split[0].trim(), split[1].trim());
            });
        }
        // 打印信息
        for (Map.Entry<Object, Object> entry : props.entrySet()) {
            JulLog.info("connect prop: {}={}", entry.getKey(), entry.getValue());
        }
        // 创建数据库连接
        Connection connection = DriverManager.getConnection(host, props);
        // 打印信息
        Properties info = connection.getClientInfo();
        for (Map.Entry<Object, Object> entry : info.entrySet()) {
            JulLog.info("connect info: {}={}", entry.getKey(), entry.getValue());
        }
        return connection;
    }

    /**
     * 获取连接字符串
     *
     * @return 结果
     */
    public String getConnectionString() {
        return "jdbc:mysql://" + this.config.getHost() + ":" + this.config.getPort() + "/";
    }

    public ShellMysqlConnConfig getConfig() {
        return config;
    }

    public void setConfig(ShellMysqlConnConfig config) {
        this.config = config;
    }

    public int getConnectTimeout() {
        return this.config.getConnectTimeout();
    }

    public void setConnectTimeout(int connectTimeout) {
        this.config.setConnectTimeout(connectTimeout);
    }

}
