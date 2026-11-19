package cn.oyzh.easyshell.oracle;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellProxyConfig;
import cn.oyzh.fx.db.DBConnConfig;
import cn.oyzh.fx.db.DBConnManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;

/**
 * Oracle连接管理器，支持Service Name、SID和完整连接描述符。
 *
 * @author oyzh
 * @since 2026-11-18
 */
public class ShellOracleConnManager extends DBConnManager {

    @Override
    public Connection initConnection(String schema, String user, String password) throws ClassNotFoundException, SQLException {
        Class.forName("oracle.jdbc.OracleDriver");
        DBConnConfig config = this.getConfig();
        Properties properties = new Properties();
        properties.setProperty("user", StringUtil.blankToDefault(user, config.getUser()));
        properties.setProperty("password", StringUtil.blankToDefault(password, config.getPassword()));
        properties.setProperty("oracle.net.CONNECT_TIMEOUT",
                String.valueOf(Math.max(1, config.getConnectTimeout()) * 1000L));
        // Oracle 23c原生JSON列在getObject()时需要显式指定默认类型
        properties.setProperty("oracle.jdbc.jsonDefaultGetObjectType", "java.lang.String");
        // Oracle 23c VECTOR列同样没有JDBC默认转换
        properties.setProperty("oracle.jdbc.vectorDefaultGetObjectType", "String");
        if (config.isUseSSL()) {
            properties.setProperty("oracle.net.ssl_server_dn_match", "true");
        }

        String envs = config.getEnv();
        if (StringUtil.isNotBlank(envs)) {
            envs.lines()
                    .map(String::trim)
                    .filter(line -> !line.isBlank() && !line.startsWith("#"))
                    .forEach(line -> {
                        int index = line.indexOf('=');
                        if (index > 0) {
                            properties.setProperty(line.substring(0, index).trim(), line.substring(index + 1).trim());
                        }
                    });
        }

        ShellOracleProxyTunnel proxyTunnel = null;
        try {
            proxyTunnel = this.configureProxy(config, properties);
            String connectionString = buildConnectionString(config);
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                if (!"password".equalsIgnoreCase(String.valueOf(entry.getKey()))) {
                    JulLog.info("oracle connect prop: {}={}", entry.getKey(), entry.getValue());
                }
            }
            Connection connection = DriverManager.getConnection(connectionString, properties);
            if (StringUtil.isNotBlank(schema)) {
                this.useSchema(connection, schema);
            }
            return proxyTunnel == null ? connection : ShellOracleConnectionWrapper.wrap(connection, proxyTunnel);
        } catch (SQLException | RuntimeException ex) {
            closeQuietly(proxyTunnel);
            throw ex;
        } catch (Exception ex) {
            closeQuietly(proxyTunnel);
            throw new SQLException("Failed to initialize Oracle connection", ex);
        }
    }

    @Override
    public String getConnectionString() {
        return buildConnectionString(this.getConfig());
    }

    /**
     * 生成Oracle JDBC URL。
     *
     * @param config 连接配置
     * @return JDBC URL
     */
    public static String buildConnectionString(DBConnConfig config) {
        String database = StringUtil.blankToDefault(config.getDatabase(), "");
        String protocol = config.isUseSSL() ? "tcps" : "tcp";
        if (database.strip().startsWith("(")) {
            return "jdbc:oracle:thin:@" + database.strip();
        }
        if (database.regionMatches(true, 0, "sid:", 0, 4)) {
            String sid = database.substring(4).strip();
            if (!config.isUseSSL()) {
                return "jdbc:oracle:thin:@" + config.getHost() + ":" + config.getPort() + ":" + sid;
            }
            return "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCPS)(HOST=" + config.getHost()
                    + ")(PORT=" + config.getPort() + "))(CONNECT_DATA=(SID=" + sid + ")))";
        }
        return "jdbc:oracle:thin:@" + protocol + "://" + config.getHost() + ":" + config.getPort() + "/"
                + database.strip().replaceFirst("^/+", "");
    }

    private ShellOracleProxyTunnel configureProxy(DBConnConfig config, Properties properties) throws SQLException {
        String type = config.getProxyType();
        if (StringUtil.isBlank(config.getProxyHost()) || config.getProxyPort() == null || config.getProxyPort() <= 0) {
            return null;
        }
        if (!isSocksProxy(type) && !"http".equalsIgnoreCase(type)) {
            throw new SQLException("Unsupported Oracle proxy protocol: " + type);
        }
        boolean socks = isSocksProxy(type);
        boolean authentication = StringUtil.isNotBlank(config.getProxyUser()) || StringUtil.isNotBlank(config.getProxyPassword());
        if (!authentication) {
            this.applyNativeProxy(config, properties, socks);
            return null;
        }
        ShellOracleProxyTunnel tunnel = new ShellOracleProxyTunnel(this.createProxyConfig(config),
                Math.max(1, config.getConnectTimeout()) * 1000);
        try {
            InetSocketAddress endpoint = tunnel.start();
            if (socks) {
                properties.setProperty("oracle.net.socksProxyHost", endpoint.getHostString());
                properties.setProperty("oracle.net.socksProxyPort", String.valueOf(endpoint.getPort()));
                properties.setProperty("oracle.net.socksRemoteDNS", "true");
            } else {
                properties.setProperty("oracle.net.httpsProxyHost", endpoint.getHostString());
                properties.setProperty("oracle.net.httpsProxyPort", String.valueOf(endpoint.getPort()));
            }
            properties.setProperty("oracle.net.proxyRemoteDNS", "true");
            return tunnel;
        } catch (IOException | RuntimeException ex) {
            closeQuietly(tunnel);
            throw new SQLException("Failed to start Oracle proxy tunnel", ex);
        }
    }

    private void applyNativeProxy(DBConnConfig config, Properties properties, boolean socks) {
        if (socks) {
            properties.setProperty("oracle.net.socksProxyHost", config.getProxyHost());
            properties.setProperty("oracle.net.socksProxyPort", String.valueOf(config.getProxyPort()));
            properties.setProperty("oracle.net.socksRemoteDNS", "true");
        } else {
            properties.setProperty("oracle.net.httpsProxyHost", config.getProxyHost());
            properties.setProperty("oracle.net.httpsProxyPort", String.valueOf(config.getProxyPort()));
        }
        properties.setProperty("oracle.net.proxyRemoteDNS", "true");
    }

    private ShellProxyConfig createProxyConfig(DBConnConfig config) {
        ShellProxyConfig proxyConfig = new ShellProxyConfig();
        proxyConfig.setProtocol(config.getProxyType());
        proxyConfig.setHost(config.getProxyHost());
        proxyConfig.setPort(config.getProxyPort());
        proxyConfig.setUser(config.getProxyUser());
        proxyConfig.setPassword(config.getProxyPassword());
        proxyConfig.setAuthType(
                StringUtil.isNotBlank(config.getProxyUser()) || StringUtil.isNotBlank(config.getProxyPassword())
                        ? "password" : "none"
        );
        return proxyConfig;
    }

    private static boolean isSocksProxy(String type) {
        return StringUtil.equalsAnyIgnoreCase(type, "socks", "socks4", "socks5");
    }

    private static void closeQuietly(AutoCloseable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception ignored) {
        }
    }

    private void useSchema(Connection connection, String schema) throws SQLException {
        String normalized = schema.trim();
        if (normalized.isEmpty() || normalized.length() > 128 || !normalized.matches("[A-Za-z0-9_$#]+")) {
            throw new SQLException("Invalid Oracle schema name: " + schema);
        }
        String sql = "ALTER SESSION SET CURRENT_SCHEMA = \"" + normalized.replace("\"", "\"\"") + "\"";
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
}
