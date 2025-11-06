package cn.oyzh.easyshell.mysql;


import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


/**
 * 连接管理器
 *
 * @author oyzh
 * @since 2024/01/28
 */
public class DBConnectionManager {

    /**
     * 服务连接
     */
    private Connection serverConnection;

    /**
     * 库连接
     */
    private final Map<String, Connection> connections = new HashMap<>();

    public void addConnection(String dbName, Connection connection) {
        this.connections.put(dbName, connection);
    }

    public void addSchemaConnection(String dbName, String schema, Connection connection) {
        this.connections.put(dbName + "_" + schema, connection);
    }

    public void addFunctionConnection(String dbName, String schema, Connection connection) {
        this.connections.put(dbName + "_" + schema + "_function", connection);
    }

    public void addProcedureConnection(String dbName, String schema, Connection connection) {
        this.connections.put(dbName + "_" + schema + "_procedure", connection);
    }

    public Connection getConnection(String dbName) {
        return this.connections.get(dbName);
    }

    public Connection getSchemaConnection(String dbName, String schema) {
        return this.connections.get(dbName + "_" + schema);
    }

    public Connection getFunctionConnection(String dbName, String schema) {
        return this.connections.get(dbName + "_" + schema + "_function");
    }

    public Connection getProcedureConnection(String dbName, String schema) {
        return this.connections.get(dbName + "_" + schema + "_procedure");
    }

    public boolean hasConnection(String dbName) {
        return this.connections.containsKey(dbName);
    }

    public void destroy() {
        if (this.serverConnection != null) {
            try {
                this.serverConnection.close();
            } catch (SQLException ignored) {
            }
        }
        for (Connection value : connections.values()) {
            try {
                value.close();
            } catch (SQLException ignored) {
            }
        }
        this.connections.clear();
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
}
