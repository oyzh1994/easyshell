package cn.oyzh.easyshell.zk;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.zk.ShellZKSASLConfig;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;

/**
 * sasl工具类
 *
 * @author oyzh
 * @since 2024-12-20
 */

public class ShellZKSASLUtil {

    // /**
    //  * 连接存储
    //  */
    // private static final ShellConnectStore CONNECT_STORE = ShellConnectStore.INSTANCE;

    // /**
    //  * sasl配置存储
    //  */
    // private static final ShellZKSASLConfigStore CONFIG_STORE = ShellZKSASLConfigStore.INSTANCE;

    /**
     * 注册配置类
     */
    public static void registerConfiguration() {
        Security.setProperty("login.configuration.provider", ShellZKSASLConfiguration.class.getName());
    }

    /**
     * 移除sasl配置
     *
     * @param iid zk连接id
     * @see ShellConnect
     */
    public synchronized static void removeSasl(String iid) {
        if (Configuration.getConfiguration() instanceof ShellZKSASLConfiguration configuration) {
            configuration.removeAppConfigurationEntry(iid);
        }
    }

    /**
     * 是否开启sasl
     *
     * @param iid zk连接id
     * @param saslConfig sasl配置
     * @return 结果
     * @see ShellConnect
     */
    public static boolean isNeedSasl(String iid,ShellZKSASLConfig saslConfig) {
        if (StringUtil.isNotBlank(iid) && Configuration.getConfiguration() instanceof ShellZKSASLConfiguration configuration) {
            // 缓存里存在直接返回
            if (configuration.containsAppConfigurationEntry(iid)) {
                return true;
            }
            // SelectParam selectParam = new SelectParam();
            // selectParam.addQueryParam(QueryParam.of("id", iid));
            // selectParam.addQueryColumn("saslAuth");
            // ShellConnect connect = CONNECT_STORE.selectOne(selectParam);
            // if (connect != null && connect.isSASLAuth()) {
            //     ShellZKSASLConfig config = CONFIG_STORE.getByIid(iid);
                if (saslConfig == null || saslConfig.checkInvalid()) {
                    return false;
                }
                // 添加到缓存
                addSaslEntry(saslConfig);
                return true;
            // }
        }
        return false;
    }

    /**
     * 添加sasl配置
     *
     * @param config sasl配置
     */
    private static void addSaslEntry(ShellZKSASLConfig config) {
        if (Configuration.getConfiguration() instanceof ShellZKSASLConfiguration configuration) {
            if ("Digest".equalsIgnoreCase(config.getType())) {
                Map<String, String> options = new HashMap<>();
                options.put("username", config.getUserName());
                options.put("password", config.getPassword());
                AppConfigurationEntry entry = new AppConfigurationEntry("org.apache.zookeeper.server.auth.DigestLoginModule", AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, options);
                configuration.putAppConfigurationEntry(config.getIid(), entry);
            }
        }
    }
}
