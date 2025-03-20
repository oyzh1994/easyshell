package cn.oyzh.easyshell.store;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellSSHConfig;
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

    public final ShellX11ConfigStore x11ConfigStore = ShellX11ConfigStore.INSTANCE;

    public final ShellSSHConfigStore sshConfigStore = ShellSSHConfigStore.INSTANCE;

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
            connect.setSshConfig(this.sshConfigStore.getByIid(connect.getId()));
        }
        return connects;
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

            // shell处理
            ShellSSHConfig sshConfig = model.getSshConfig();
            if (sshConfig != null) {
                sshConfig.setIid(model.getId());
                this.sshConfigStore.replace(sshConfig);
            } else {
                this.sshConfigStore.deleteByIid(model.getId());
            }

            // x11处理
            ShellX11Config x11Config = model.getX11Config();
            if (x11Config != null) {
                x11Config.setIid(model.getId());
                this.x11ConfigStore.replace(x11Config);
            } else {
                this.x11ConfigStore.deleteByIid(model.getId());
            }
        }
        return result;
    }
}

