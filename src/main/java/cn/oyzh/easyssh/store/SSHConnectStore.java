package cn.oyzh.easyssh.store;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyssh.domain.SSHConnect;
import cn.oyzh.easyssh.domain.SSHX11Config;
import cn.oyzh.store.jdbc.DeleteParam;
import cn.oyzh.store.jdbc.JdbcStandardStore;
import cn.oyzh.store.jdbc.QueryParam;

import java.util.List;

/**
 * ssh信息存储
 *
 * @author oyzh
 * @since 2023/6/23
 */
public class SSHConnectStore extends JdbcStandardStore<SSHConnect> {

    /**
     * 当前实例
     */
    public static final SSHConnectStore INSTANCE = new SSHConnectStore();

    public final SSHX11ConfigStore x11ConfigStore =SSHX11ConfigStore.INSTANCE;

    public synchronized List<SSHConnect> load() {
        return super.selectList();
    }

    @Override
    protected Class<SSHConnect> modelClass() {
        return SSHConnect.class;
    }

    public List<SSHConnect> loadFull() {
        return this.load();
    }

    /**
     * 替换
     *
     * @param model 模型
     * @return 结果
     */
    public boolean replace(SSHConnect model) {
        boolean result = false;
        if (model != null) {
            if (super.exist(model.getId())) {
                result = this.update(model);
            } else {
                result = this.insert(model);
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

