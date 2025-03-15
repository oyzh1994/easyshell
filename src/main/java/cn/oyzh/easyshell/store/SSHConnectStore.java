package cn.oyzh.easyshell.store;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.SSHX11Config;
import cn.oyzh.easyshell.domain.ShellSSHConfig;
import cn.oyzh.store.jdbc.JdbcStandardStore;

import java.util.List;

/**
 * ssh信息存储
 *
 * @author oyzh
 * @since 2023/6/23
 */
public class SSHConnectStore extends JdbcStandardStore<ShellConnect> {

    /**
     * 当前实例
     */
    public static final SSHConnectStore INSTANCE = new SSHConnectStore();

    public final SSHX11ConfigStore x11ConfigStore = SSHX11ConfigStore.INSTANCE;

    public final ShellSSHConfigStore sshConfigStore = ShellSSHConfigStore.INSTANCE;

    public synchronized List<ShellConnect> load() {
        return super.selectList();
    }

    @Override
    protected Class<ShellConnect> modelClass() {
        return ShellConnect.class;
    }

    public List<ShellConnect> loadFull() {
        return this.load();
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

            // ssh处理
            ShellSSHConfig sshConfig = model.getSshConfig();
            if (sshConfig != null) {
                sshConfig.setIid(model.getId());
                this.sshConfigStore.replace(sshConfig);
            } else {
                this.sshConfigStore.deleteByIid(model.getId());
            }

            // x11处理
            SSHX11Config x11Config = model.getX11Config();
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

