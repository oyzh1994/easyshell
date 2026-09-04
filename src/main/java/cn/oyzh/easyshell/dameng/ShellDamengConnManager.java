package cn.oyzh.easyshell.dameng;


import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.db.DBConnManager;
import dm.jdbc.driver.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;


/**
 * 连接管理器
 *
 * @author oyzh
 * @since 2024/01/28
 */
public class ShellDamengConnManager extends DBConnManager {

    @Override
    public Connection initConnection(String dbName, String user, String password) throws ClassNotFoundException, SQLException {
        // 加载JDBC驱动
        Class.forName("dm.jdbc.driver.DmDriver");
        String host = this.getConnectionString();
        if (dbName != null) {
            host += dbName;
        }
        Properties props = new Properties();
        props.put(Configuration.user.getName(), config.getUser());
        props.put(Configuration.password.getName(), config.getPassword());
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

    @Override
    public String getConnectionString() {
        return "jdbc:dm://" + this.config.getHost() + ":" + this.config.getPort() + "/";
    }
}
