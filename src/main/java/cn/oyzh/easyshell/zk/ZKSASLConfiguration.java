package cn.oyzh.easyshell.zk;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import java.util.HashMap;
import java.util.Map;

/**
 * sasl配置类
 *
 * @author oyzh
 * @since 2024-12-20
 */
public class ZKSASLConfiguration extends Configuration {

    /**
     * 配置
     */
    private Map<String, AppConfigurationEntry> config;

    @Override
    public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
        return this.config == null ? null : new AppConfigurationEntry[]{this.config.get(name)};
    }

    /**
     * 添加配置
     *
     * @param name  名称
     * @param entry 配置
     */
    public void putAppConfigurationEntry(String name, AppConfigurationEntry entry) {
        if (name != null && entry != null) {
            if (this.config == null) {
                this.config = new HashMap<>();
            }
            this.config.put(name, entry);
        }
    }

    /**
     * 移除配置
     *
     * @param name 名称
     */
    public void removeAppConfigurationEntry(String name) {
        if (name != null && this.config != null) {
            this.config.remove(name);
        }
    }

    /**
     * 是否包含配置
     *
     * @param name 名称
     * @return 结果
     */
    public boolean containsAppConfigurationEntry(String name) {
        if (name != null && this.config != null) {
            return this.config.containsKey(name);
        }
        return false;
    }
}
