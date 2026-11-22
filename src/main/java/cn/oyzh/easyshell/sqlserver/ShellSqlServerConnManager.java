package cn.oyzh.easyshell.sqlserver;


import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellProxyConfig;
import cn.oyzh.fx.db.DBConnManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;


/**
 * SqlServer连接管理器
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellSqlServerConnManager extends DBConnManager {

    @Override
    public Connection connection(String schema) throws Exception {
        if (StringUtil.isBlank(schema)) {
            return super.connection();
        }
        Connection connection = super.getConnection(schema);
        if (!this.isValid(connection)) {
            connection = this.initConnection(null, this.config.getUser(), this.config.getPassword());
            connection.setSchema(schema);
            this.addConnection(schema, connection);
            JulLog.info("initConnection for schema {}", schema);
        }
        connection.setAutoCommit(true);
        return connection;
    }

    @Override
    public Connection initConnection(String dbName, String user, String password) throws ClassNotFoundException, SQLException {
        // 加载JDBC驱动
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        // SQL Server一个连接对应一个数据库，数据库名优先取传入值，其次取配置值，最后使用master
        String database = dbName;
        if (StringUtil.isBlank(database)) {
            database = this.config.getDatabase();
        }
        if (StringUtil.isBlank(database)) {
            database = "master";
        }
        String host = "jdbc:sqlserver://" + this.config.getHost() + ":" + this.config.getPort()
                + ";databaseName=" + this.escapePropertyValue(database);
        Properties props = new Properties();
        props.put("user", StringUtil.isNotBlank(user) ? user : this.config.getUser());
        props.put("password", StringUtil.isNotBlank(password) ? password : this.config.getPassword());
        // 连接超时（SQL Server的loginTimeout单位为秒）
        int connectTimeout = Math.max(1, this.config.getConnectTimeout());
        props.put("loginTimeout", String.valueOf(connectTimeout));
        props.put("applicationName", "EasyShell");
        props.put("encrypt", String.valueOf(this.config.isUseSSL()));
        props.put("trustServerCertificate", String.valueOf(!this.config.isUseSSL()));
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
        return "jdbc:sqlserver://" + this.config.getHost() + ":" + this.config.getPort();
    }

    private String escapePropertyValue(String value) {
        return value.replace(";", ";;");
    }
}
