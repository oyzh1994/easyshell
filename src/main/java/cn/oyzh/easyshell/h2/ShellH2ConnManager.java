package cn.oyzh.easyshell.h2;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.db.DBConnManager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Map;
import java.util.Properties;

/**
 * H2 连接管理器。
 *
 * @author oyzh
 * @since 2026-11-28
 */
public class ShellH2ConnManager extends DBConnManager {

    @Override
    public Connection initConnection(String name, String user, String password) throws Exception {
        Class.forName("org.h2.Driver");
        ShellH2ConnConfig h2Config = this.h2Config();

        Properties props = new Properties();
        props.putAll(ShellH2Helper.parseEnvironment(h2Config.getEnv()));
        props.remove("url_mode");
        props.remove("urlMode");
        props.remove("schema");
        props.remove("mode");
        props.remove("auto_server");
        props.remove("autoServer");
        props.remove("db_close_delay");
        props.remove("dbCloseDelay");
        props.remove("create_if_missing");
        props.remove("createIfMissing");
        props.remove("read_only");
        props.remove("readOnly");
        if (user != null) {
            props.setProperty("user", user);
        }
        if (password != null) {
            props.setProperty("password", password);
        }
        for (Map.Entry<Object, Object> entry : props.entrySet()) {
            JulLog.info("h2 connect prop: {}={}", entry.getKey(), entry.getValue());
        }
        return DriverManager.getConnection(this.getConnectionString(), props);
    }

    /**
     * 获取 H2 连接配置。
     *
     * @return H2 连接配置
     */
    public ShellH2ConnConfig h2Config() {
        if (!(this.config instanceof ShellH2ConnConfig h2Config)) {
            throw new IllegalStateException("H2 connection config is not initialized");
        }
        return h2Config;
    }

    /**
     * 文件模式下校验文件状态。
     *
     * @param file   数据库文件
     * @param config 配置
     */
    private void prepareFile(Path file, ShellH2ConnConfig config) {
        Path actual = config.actualFilePath();
        if (Files.exists(actual)) {
            if (!Files.isRegularFile(actual)) {
                throw new IllegalArgumentException("Database path is not a regular file: " + actual);
            }
            if (config.isReadOnly() && !Files.isReadable(actual)) {
                throw new IllegalArgumentException("Database file is not readable: " + actual);
            }
            return;
        }
        if (config.isReadOnly() || !config.isCreateIfMissing()) {
            throw new IllegalArgumentException("Database file does not exist: " + actual
                    + " (enable createIfMissing or check the path)");
        }
        Path parent = file.getParent();
        if (parent != null && !Files.isWritable(parent)) {
            throw new IllegalArgumentException("Database directory is not writable: " + parent);
        }
    }

    @Override
    public String getConnectionString() {
        ShellH2ConnConfig config = this.h2Config();
        if (config.isMemMode()) {
            return this.memConnectionString(config);
        }
        if (config.isTcpMode()) {
            return this.tcpConnectionString(config);
        }
        return this.fileConnectionString(config);
    }

    /**
     * 构造文件模式连接串。
     *
     * @param config 配置
     * @return 连接串
     */
    private String fileConnectionString(ShellH2ConnConfig config) {
        Path file = config.resolveFilePath();
        if (config.isReadOnly() || !config.isCreateIfMissing()) {
            this.prepareFile(file, config);
        }
        StringBuilder builder = new StringBuilder("jdbc:h2:file:").append(file);
        if (config.isReadOnly()) {
            builder.append(";ACCESS_MODE_DATA=r");
        } else if (!config.isCreateIfMissing()) {
            // 文件必须存在，避免路径拼写错误时静默创建空库
            builder.append(";IFEXISTS=TRUE");
        }
        this.appendCommonProperties(builder, config);
        return builder.toString();
    }

    /**
     * 构造内存模式连接串。
     *
     * @param config 配置
     * @return 连接串
     */
    private String memConnectionString(ShellH2ConnConfig config) {
        if (StringUtil.isBlank(config.getHost())) {
            throw new IllegalArgumentException("Memory database name is empty");
        }
        StringBuilder builder = new StringBuilder("jdbc:h2:mem:").append(config.getHost().trim());
        if (config.isDbCloseDelay()) {
            builder.append(";DB_CLOSE_DELAY=-1");
        }
        this.appendCommonProperties(builder, config);
        return builder.toString();
    }

    /**
     * 构造远程模式连接串。
     *
     * @param config 配置
     * @return 连接串
     */
    private String tcpConnectionString(ShellH2ConnConfig config) {
        if (StringUtil.isBlank(config.getHost())) {
            throw new IllegalArgumentException("H2 server host is empty");
        }
        if (StringUtil.isBlank(config.getDatabase())) {
            throw new IllegalArgumentException("H2 database name is empty");
        }
        StringBuilder builder = new StringBuilder(config.isUseSSL() ? "jdbc:h2:ssl://" : "jdbc:h2:tcp://")
                .append(config.getHost().trim());
        if (config.getPort() != null && config.getPort() > 0) {
            builder.append(':').append(config.getPort());
        }
        builder.append('/').append(config.getDatabase().trim());
        this.appendCommonProperties(builder, config);
        return builder.toString();
    }

    /**
     * 追加通用连接属性。
     *
     * @param builder 连接串构造器
     * @param config  配置
     */
    private void appendCommonProperties(StringBuilder builder, ShellH2ConnConfig config) {
        if (StringUtil.isNotBlank(config.getSchema())) {
            builder.append(";SCHEMA=").append(config.getSchema().trim());
        }
        if (StringUtil.isNotBlank(config.getMode())) {
            builder.append(";MODE=").append(config.getMode().trim());
        }
        if (config.isAutoServer()) {
            builder.append(";AUTO_SERVER=TRUE");
        }
    }
}
