package cn.oyzh.easyshell.mysql;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


/**
 * 连接管理器
 *
 * @author oyzh
 * @since 2024/01/28
 */
public class ShellMysqlConnManager implements AutoCloseable {

    /**
     * 地址
     */
    private String host;

    /**
     * 用户名
     */
    private String user;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 密码
     */
    private String password;

    /**
     * 服务连接
     */
    private Connection serverConnection;

    /**
     * 库连接
     */
    private Map<String, Connection> connections = new HashMap<>();

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void addConnection(String dbName, Connection connection) {
        this.connections.put(dbName, connection);
    }

    public void addFunctionConnection(String dbName, Connection connection) {
        this.connections.put(dbName  + "_function", connection);
    }

    public void addProcedureConnection(String dbName , Connection connection) {
        this.connections.put(dbName  + "_procedure", connection);
    }

    public Connection getConnection(String dbName) {
        return this.connections.get(dbName);
    }

    public Connection getFunctionConnection(String dbName ) {
        return this.connections.get(dbName  + "_function");
    }

    public Connection getProcedureConnection(String dbName) {
        return this.connections.get(dbName  + "_procedure");
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
        this.port = null;
        this.host = null;
        this.user = null;
        this.password = null;
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
        return !connection.isValid(10);
    }

    public Connection connection() throws SQLException, ClassNotFoundException {
        Connection connection = this.getServerConnection();
        if (this.isValid(connection)) {
            connection = this.initConnection( null, this.user, this.password);
            this.setServerConnection(connection);
        }
        return connection;
    }

    public Connection connection(String dbName) throws SQLException, ClassNotFoundException {
        Connection connection = this.getConnection(dbName);
        if (this.isValid(connection)) {
            connection = this.initConnection( dbName, this.user, this.password);
            this.addConnection(dbName, connection);
        }
        connection.setAutoCommit(true);
        return connection;
    }

    public Connection functionConnection(String dbName ) throws SQLException, ClassNotFoundException {
        Connection connection = this.getFunctionConnection(dbName);
        if (this.isValid(connection)) {
            connection = this.initConnection( dbName, this.user, this.password);
            this.addFunctionConnection(dbName, connection);
        }
        connection.setAutoCommit(true);
        return connection;
    }

    public Connection procedureConnection(String dbName) throws SQLException, ClassNotFoundException {
        Connection connection = this.getProcedureConnection(dbName);
        if (this.isValid(connection)) {
            connection = this.initConnection( dbName, this.user, this.password);
            this.addProcedureConnection(dbName, connection);
        }
        connection.setAutoCommit(true);
        return connection;
    }

    public Connection newConnection(String dbName) throws SQLException, ClassNotFoundException {
        Connection connection = this.initConnection(dbName, this.user, this.password);
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
        host = host +
                "?testOnBorrow=true" +
                "&tcpKeepAlive=true" +
                "&autoReconnect=true" +
                "&testWhileIdle=true" +
                "&validationQuery=SELECT 1" +
                "&zeroDateTimeBehavior=convertToNull"
        ;
        // 创建数据库连接
        return DriverManager.getConnection(host, user, password);
    }

    public String getConnectionString() {
        return "jdbc:mysql://" + this.host + ":" + this.port + "/";
    }
}
