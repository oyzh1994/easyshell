package cn.oyzh.easyshell.mariadb;


import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.db.DBConnManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;


/**
 * MariaDB连接管理器
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbConnManager extends DBConnManager {

    //    /**
    //     * 配置
    //     */
    //    private ShellMariadbConnConfig config;
    //
    //    // /**
    //    //  * 服务连接
    //    //  */
    //    // private Connection serverConnection;
    //
    //    /**
    //     * 库连接
    //     */
    //    private final Map<String, Connection> connections = new ConcurrentHashMap<>();
    //
    //    /**
    //     * 添加连接
    //     *
    //     * @param dbName     数据库
    //     * @param connection 连接
    //     */
    //    public void addConnection(String dbName, Connection connection) {
    //        this.connections.put("db_connection_" + dbName, connection);
    //    }
    //
    //    /**
    //     * 添加函数连接
    //     *
    //     * @param dbName     数据库
    //     * @param connection 数据库
    //     */
    //    public void addFunctionConnection(String dbName, Connection connection) {
    //        this.connections.put("function_connection_" + dbName, connection);
    //    }
    //
    //    /**
    //     * 添加过程连接
    //     *
    //     * @param dbName     数据库
    //     * @param connection 数据库
    //     */
    //    public void addProcedureConnection(String dbName, Connection connection) {
    //        this.connections.put("procedure_connection_" + dbName, connection);
    //    }
    //
    //    /**
    //     * 获取连接
    //     *
    //     * @param dbName 数据库
    //     * @return 结果
    //     */
    //    public Connection getConnection(String dbName) {
    //        return this.connections.get("db_connection_" + dbName);
    //    }
    //
    //    /**
    //     * 获取函数连接
    //     *
    //     * @param dbName 数据库
    //     * @return 结果
    //     */
    //    public Connection getFunctionConnection(String dbName) {
    //        return this.connections.get("function_connection_" + dbName);
    //    }
    //
    //    /**
    //     * 获取过程连接
    //     *
    //     * @param dbName 数据库
    //     * @return 结果
    //     */
    //    public Connection getProcedureConnection(String dbName) {
    //        return this.connections.get("procedure_connection_" + dbName);
    //    }
    //
    //    // public boolean hasConnection(String dbName) {
    //    //     return this.connections.containsKey(dbName);
    //    // }
    //
    //    @Override
    //    public void close() {
    //        // if (this.serverConnection != null) {
    //        //     try {
    //        //         this.serverConnection.close();
    //        //     } catch (SQLException ignored) {
    //        //     }
    //        // }
    //        // this.serverConnection = null;
    //        for (Connection connection : this.connections.values()) {
    //            try {
    //                connection.close();
    //            } catch (SQLException ignored) {
    //            }
    //        }
    //        this.connections.clear();
    //        //        this.connections = null;
    //        //        this.config = null;
    //    }
    //
    //    /**
    //     * 获取服务连接
    //     *
    //     * @return 服务连接
    //     */
    //    public Connection getServerConnection() {
    //        // return serverConnection;
    //        return this.connections.get("server_connection");
    //    }
    //
    //    /**
    //     * 设置服务连接
    //     *
    //     * @param serverConnection 服务连接
    //     */
    //    public void setServerConnection(Connection serverConnection) {
    //        // this.serverConnection = serverConnection;
    //        this.connections.put("server_connection", serverConnection);
    //    }
    //
    //    /**
    //     * 获取连接列表
    //     *
    //     * @return 连接列表
    //     */
    //    public Map<String, Connection> getConnections() {
    //        return connections;
    //    }
    //
    //    /**
    //     * 是否有效
    //     *
    //     * @param connection 连接
    //     * @return 结果
    //     * @throws SQLException 异常
    //     */
    //    public boolean isValid(Connection connection) throws SQLException {
    //        if (connection == null || connection.isClosed()) {
    //            return false;
    //        }
    //        return connection.isValid(this.getConnectTimeout() / 1000);
    //    }
    //
    //    /**
    //     * 执行连接
    //     *
    //     * @return 结果
    //     * @throws SQLException           异常
    //     * @throws ClassNotFoundException 异常
    //     */
    //    public Connection connection() throws SQLException, ClassNotFoundException {
    //        if (this.config == null) {
    //            return null;
    //        }
    //        Connection connection = this.getServerConnection();
    //        if (!this.isValid(connection)) {
    //            connection = this.initConnection(null, this.config.getUser(), this.config.getPassword());
    //            this.setServerConnection(connection);
    //        }
    //        return connection;
    //    }
    //
    //    /**
    //     * 执行连接
    //     *
    //     * @param dbName 数据库
    //     * @return 结果
    //     * @throws SQLException           异常
    //     * @throws ClassNotFoundException 异常
    //     */
    //    public Connection connection(String dbName) throws SQLException, ClassNotFoundException {
    //        Connection connection = this.getConnection(dbName);
    //        if (!this.isValid(connection)) {
    //            connection = this.initConnection(dbName, this.config.getUser(), this.config.getPassword());
    //            this.addConnection(dbName, connection);
    //        }
    //        connection.setAutoCommit(true);
    //        return connection;
    //    }
    //
    //    /**
    //     * 执行函数连接
    //     *
    //     * @param dbName 数据库
    //     * @return 结果
    //     * @throws SQLException           异常
    //     * @throws ClassNotFoundException 异常
    //     */
    //    public Connection functionConnection(String dbName) throws SQLException, ClassNotFoundException {
    //        Connection connection = this.getFunctionConnection(dbName);
    //        if (!this.isValid(connection)) {
    //            connection = this.initConnection(dbName, this.config.getUser(), this.config.getPassword());
    //            this.addFunctionConnection(dbName, connection);
    //        }
    //        connection.setAutoCommit(true);
    //        return connection;
    //    }
    //
    //    /**
    //     * 执行过程连接
    //     *
    //     * @param dbName 数据库
    //     * @return 结果
    //     * @throws SQLException           异常
    //     * @throws ClassNotFoundException 异常
    //     */
    //    public Connection procedureConnection(String dbName) throws SQLException, ClassNotFoundException {
    //        Connection connection = this.getProcedureConnection(dbName);
    //        if (!this.isValid(connection)) {
    //            connection = this.initConnection(dbName, this.config.getUser(), this.config.getPassword());
    //            this.addProcedureConnection(dbName, connection);
    //        }
    //        connection.setAutoCommit(true);
    //        return connection;
    //    }
    //
    //    /**
    //     * 执行新连接
    //     *
    //     * @param dbName 数据库
    //     * @return 结果
    //     * @throws SQLException           异常
    //     * @throws ClassNotFoundException 异常
    //     */
    //    public Connection newConnection(String dbName) throws SQLException, ClassNotFoundException {
    //        Connection connection = this.initConnection(dbName, this.config.getUser(), this.config.getPassword());
    //        connection.setAutoCommit(true);
    //        return connection;
    //    }

    @Override
    public Connection initConnection(String dbName, String user, String password) throws ClassNotFoundException, SQLException {
        // 加载JDBC驱动
        Class.forName("org.mariadb.jdbc.Driver");
        String host = this.getConnectionString();
        if (dbName != null) {
            host += dbName;
        }
        Properties props = new Properties();
        props.put("user", user);
        props.put("password", password);
        // 代理配置
        if (this.config.getSocketFactory() != null) {
            props.put("_proxyType", this.config.getProxyType());
            props.put("_proxyHost", this.config.getProxyHost());
            props.put("_proxyUser", this.config.getProxyUser());
            props.put("_proxyPort", this.config.getProxyPort() + "");
            props.put("_proxyPassword", this.config.getProxyPassword());
            props.put("socketFactory", this.config.getSocketFactory());
        }
        // 预设环境参数设置
        props.put("sslMode", this.config.isUseSSL() ? "verify-full" : "disable");
        props.put("socketTimeout", this.getConnectTimeout() + "");
        props.put("connectTimeout", this.getConnectTimeout() + "");
        props.putAll(ShellMariadbHelper.DEFAULT_ENVIRONMENT);

        // 自定义环境参数设置
        String envs = this.config.getEnv();
        if (StringUtil.isNotBlank(envs)) {
            envs.lines().forEach(line -> {
                String[] split = line.split("=", 2);
                if (split.length == 2) {
                    props.put(split[0].trim(), split[1].trim());
                }
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

    @Override
    public String getConnectionString() {
        return "jdbc:mariadb://" + this.config.getHost() + ":" + this.config.getPort() + "/";
    }

    //    public ShellMariadbConnConfig getConfig() {
    //        return config;
    //    }
    //
    //    public void setConfig(ShellMariadbConnConfig config) {
    //        this.config = config;
    //    }
    //
    //    public int getConnectTimeout() {
    //        return this.config.getConnectTimeout();
    //    }
    //
    //    public void setConnectTimeout(int connectTimeout) {
    //        this.config.setConnectTimeout(connectTimeout);
    //    }

}
