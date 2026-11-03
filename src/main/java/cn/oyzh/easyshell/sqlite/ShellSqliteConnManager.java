package cn.oyzh.easyshell.sqlite;

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
 * SQLite 文件连接管理器。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellSqliteConnManager extends DBConnManager {

    @Override
    public Connection initConnection(String name, String user, String password) throws Exception {
        Class.forName("org.sqlite.JDBC");
        ShellSqliteConnConfig fileConfig = this.fileConfig();
        Path file = fileConfig.resolveFilePath();
        this.prepareFile(file, fileConfig);

        Properties props = new Properties();
        props.putAll(ShellSqliteHelper.parseEnvironment(fileConfig.getEnv()));
        props.remove("open_mode");
        props.remove("busy_timeout");
        props.remove("journal_mode");
        props.remove("foreign_keys");
        props.remove("synchronous");
        props.setProperty("open_mode", String.valueOf(fileConfig.isReadOnly() ? 1 : fileConfig.isCreateIfMissing() ? 6 : 2));
        props.setProperty("busy_timeout", String.valueOf(fileConfig.getBusyTimeout()));
        props.setProperty("journal_mode", fileConfig.getJournalMode());
        props.setProperty("foreign_keys", fileConfig.isForeignKeys() ? "1" : "0");
        props.setProperty("synchronous", fileConfig.getSynchronous());
        for (Map.Entry<Object, Object> entry : props.entrySet()) {
            JulLog.info("sqlite connect prop: {}={}", entry.getKey(), entry.getValue());
        }
        return DriverManager.getConnection(this.getConnectionString(), props);
    }

    /**
     * 获取文件连接配置。
     *
     * @return 文件连接配置
     */
    public ShellSqliteConnConfig fileConfig() {
        if (!(this.config instanceof ShellSqliteConnConfig fileConfig)) {
            throw new IllegalStateException("SQLite file connection config is not initialized");
        }
        return fileConfig;
    }

    /**
     * 按连接策略准备文件。
     *
     * @param file   文件
     * @param config 配置
     * @throws Exception 文件异常
     */
    private void prepareFile(Path file, ShellSqliteConnConfig config) throws Exception {
        if (Files.exists(file)) {
            if (!Files.isRegularFile(file)) {
                throw new IllegalArgumentException("Database path is not a regular file: " + file);
            }
            if (config.isReadOnly() && !Files.isReadable(file)) {
                throw new IllegalArgumentException("Database file is not readable: " + file);
            }
            return;
        }
        if (config.isReadOnly() || !config.isCreateIfMissing()) {
            throw new IllegalArgumentException("Database file does not exist: " + file);
        }
        Path parent = file.getParent();
        if (parent == null) {
            throw new IllegalArgumentException("Database file parent directory is empty: " + file);
        }
        Files.createDirectories(parent);
        if (!Files.isWritable(parent)) {
            throw new IllegalArgumentException("Database directory is not writable: " + parent);
        }
        Files.createFile(file);
    }

    @Override
    public String getConnectionString() {
        return "jdbc:sqlite:" + this.fileConfig().resolveFilePath();
    }
}
