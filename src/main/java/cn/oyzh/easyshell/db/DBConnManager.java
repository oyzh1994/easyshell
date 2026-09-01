//package cn.oyzh.fx.db;
//
//
//import java.sql.Connection;
//import java.sql.SQLException;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//
///**
// * 连接管理器
// *
// * @author oyzh
// * @since 2024/01/28
// */
//public abstract class DBConnManager implements AutoCloseable {
//
//    /**
//     * 连接配置
//     */
//    protected DBConnConfig config;
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
//    @Override
//    public void close() {
//        for (Connection connection : this.connections.values()) {
//            try {
//                connection.close();
//            } catch (SQLException ignored) {
//            }
//        }
//        this.connections.clear();
//    }
//
//    /**
//     * 获取服务连接
//     *
//     * @return 服务连接
//     */
//    public Connection getServerConnection() {
//        return this.connections.get("server_connection");
//    }
//
//    /**
//     * 设置服务连接
//     *
//     * @param serverConnection 服务连接
//     */
//    public void setServerConnection(Connection serverConnection) {
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
//    public Connection connection() throws Exception {
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
//    public Connection connection(String dbName) throws Exception {
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
//    public Connection functionConnection(String dbName) throws Exception {
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
//    public Connection procedureConnection(String dbName) throws Exception {
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
//    public Connection newConnection(String dbName) throws Exception {
//        Connection connection = this.initConnection(dbName, this.config.getUser(), this.config.getPassword());
//        connection.setAutoCommit(true);
//        return connection;
//    }
//
//    /**
//     * 初始化连接
//     *
//     * @param dbName   数据库
//     * @param user     用户名
//     * @param password 密码
//     * @return 结果
//     * @throws SQLException           异常
//     * @throws ClassNotFoundException 异常
//     */
//    public abstract Connection initConnection(String dbName, String user, String password) throws Exception;
//
//    /**
//     * 获取连接字符串
//     *
//     * @return 结果
//     */
//    public abstract String getConnectionString();
//
//    public DBConnConfig getConfig() {
//        return config;
//    }
//
//    public void setConfig(DBConnConfig config) {
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
//
//}
