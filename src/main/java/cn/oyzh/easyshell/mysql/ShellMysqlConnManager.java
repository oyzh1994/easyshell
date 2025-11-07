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

    /**
     * 服务连接
     */
    private Connection serverConnection;

    /**
     * 库连接
     */
    private Map<String, Connection> connections = new HashMap<>();

    public void addConnection(String dbName, Connection connection) {
        this.connections.put(dbName, connection);
    }

    public void addFunctionConnection(String dbName, Connection connection) {
        this.connections.put(dbName + "_function", connection);
    }

    public void addProcedureConnection(String dbName, Connection connection) {
        this.connections.put(dbName + "_procedure", connection);
    }

    public Connection getConnection(String dbName) {
        return this.connections.get(dbName);
    }

    public Connection getFunctionConnection(String dbName) {
        return this.connections.get(dbName + "_function");
    }

    public Connection getProcedureConnection(String dbName) {
        return this.connections.get(dbName + "_procedure");
    }

    public boolean hasConnection(String dbName) {
        return this.connections.containsKey(dbName);
    }

    @Override
    public void close() {
        if (this.serverConnection != null) {
            try {
                this.serverConnection.close();
            } catch (SQLException ignored) {
            }
        }
        this.serverConnection = null;
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

    public Connection getServerConnection() {
        return serverConnection;
    }

    public void setServerConnection(Connection serverConnection) {
        this.serverConnection = serverConnection;
    }

    public Map<String, Connection> getConnections() {
        return connections;
    }

    public boolean isValid(Connection connection) throws SQLException {
        if (connection == null || connection.isClosed()) {
            return true;
        }
        return !connection.isValid(this.config.getConnectTimeout());
    }

    public Connection connection() throws SQLException, ClassNotFoundException {
        Connection connection = this.getServerConnection();
        if (this.isValid(connection)) {
            connection = this.initConnection(null, this.config.getUser(), this.config.getPassword());
            this.setServerConnection(connection);
        }
        return connection;
    }

    public Connection connection(String dbName) throws SQLException, ClassNotFoundException {
        Connection connection = this.getConnection(dbName);
        if (this.isValid(connection)) {
            connection = this.initConnection(dbName, this.config.getUser(), this.config.getPassword());
            this.addConnection(dbName, connection);
        }
        connection.setAutoCommit(true);
        return connection;
    }

    public Connection functionConnection(String dbName) throws SQLException, ClassNotFoundException {
        Connection connection = this.getFunctionConnection(dbName);
        if (this.isValid(connection)) {
            connection = this.initConnection(dbName, this.config.getUser(), this.config.getPassword());
            this.addFunctionConnection(dbName, connection);
        }
        connection.setAutoCommit(true);
        return connection;
    }

    public Connection procedureConnection(String dbName) throws SQLException, ClassNotFoundException {
        Connection connection = this.getProcedureConnection(dbName);
        if (this.isValid(connection)) {
            connection = this.initConnection(dbName, this.config.getUser(), this.config.getPassword());
            this.addProcedureConnection(dbName, connection);
        }
        connection.setAutoCommit(true);
        return connection;
    }

    public Connection newConnection(String dbName) throws SQLException, ClassNotFoundException {
        Connection connection = this.initConnection(dbName, this.config.getUser(), this.config.getPassword());
        connection.setAutoCommit(true);
        return connection;
    }

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
        props.put(PropertyKey.socketTimeout.getKeyName(), this.config.getConnectTimeout() + "");
        props.put(PropertyKey.connectTimeout.getKeyName(), this.config.getConnectTimeout() + "");
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
