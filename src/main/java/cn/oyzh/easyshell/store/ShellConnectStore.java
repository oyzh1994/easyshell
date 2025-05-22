package cn.oyzh.easyshell.store;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellProxyConfig;
import cn.oyzh.easyshell.domain.ShellJumpConfig;
import cn.oyzh.easyshell.domain.ShellTunnelingConfig;
import cn.oyzh.easyshell.domain.ShellX11Config;
import cn.oyzh.store.jdbc.JdbcStandardStore;

import java.util.List;

/**
 * shell信息存储
 *
 * @author oyzh
 * @since 2023/6/23
 */
public class ShellConnectStore extends JdbcStandardStore<ShellConnect> {

    /**
     * 当前实例
     */
    public static final ShellConnectStore INSTANCE = new ShellConnectStore();

    /**
     * x11配置存储
     */
    private final ShellX11ConfigStore x11ConfigStore = ShellX11ConfigStore.INSTANCE;

    /**
     * 跳板配置存储
     */
    private final ShellJumpConfigStore jumpConfigStore = ShellJumpConfigStore.INSTANCE;

    /**
     * 隧道配置存储
     */
    private final ShellTunnelingConfigStore tunnelingConfigStore = ShellTunnelingConfigStore.INSTANCE;

    /**
     * 代理配置存储
     */
    private final ShellProxyConfigStore proxyConfigStore = ShellProxyConfigStore.INSTANCE;

    public synchronized List<ShellConnect> load() {
        return super.selectList();
    }

    @Override
    protected Class<ShellConnect> modelClass() {
        return ShellConnect.class;
    }

    public List<ShellConnect> loadFull() {
        List<ShellConnect> connects = this.load();
        for (ShellConnect connect : connects) {
            connect.setX11Config(this.x11ConfigStore.getByIid(connect.getId()));
            connect.setProxyConfig(this.proxyConfigStore.getByIid(connect.getId()));
            connect.setJumpConfigs(this.jumpConfigStore.loadByIid(connect.getId()));
            connect.setTunnelingConfigs(this.tunnelingConfigStore.loadByIid(connect.getId()));
        }
        return connects;
    }

    @Override
    public boolean delete(ShellConnect model) {
        boolean result = super.delete(model);
        // 删除关联配置
        if (result) {
            this.x11ConfigStore.deleteByIid(model.getId());
            this.jumpConfigStore.deleteByIid(model.getId());
            this.proxyConfigStore.deleteByIid(model.getId());
            this.tunnelingConfigStore.deleteByIid(model.getId());
        }
        return result;
    }

    /**
     * 替换
     *
     * @param model 模型
     * @return 结果
     */
    public boolean replace(ShellConnect model) {
        boolean result = false;
        if (model != null) {
            if (super.exist(model.getId())) {
                result = this.update(model);
            } else {
                result = this.insert(model);
            }

            // 跳板机处理
            List<ShellJumpConfig> jumpConfigs = model.getJumpConfigs();
            if (CollectionUtil.isNotEmpty(jumpConfigs)) {
                for (ShellJumpConfig jumpConfig : jumpConfigs) {
                    jumpConfig.setIid(model.getId());
                    this.jumpConfigStore.replace(jumpConfig);
                }
            }

            // 隧道处理
            List<ShellTunnelingConfig> tunnelingConfigs = model.getTunnelingConfigs();
            if (CollectionUtil.isNotEmpty(tunnelingConfigs)) {
                for (ShellTunnelingConfig tunnelingConfig : tunnelingConfigs) {
                    tunnelingConfig.setIid(model.getId());
                    this.tunnelingConfigStore.replace(tunnelingConfig);
                }
            }

            // x11处理
            ShellX11Config x11Config = model.getX11Config();
            if (x11Config != null) {
                x11Config.setIid(model.getId());
                this.x11ConfigStore.replace(x11Config);
            } else {
                this.x11ConfigStore.deleteByIid(model.getId());
            }

            // 代理处理
            ShellProxyConfig proxyConfig = model.getProxyConfig();
            if (proxyConfig != null) {
                proxyConfig.setIid(model.getId());
                this.proxyConfigStore.replace(proxyConfig);
            } else {
                this.proxyConfigStore.deleteByIid(model.getId());
            }
        }
        return result;
    }
}

