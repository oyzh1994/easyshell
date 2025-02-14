package cn.oyzh.easyssh.store;

import cn.oyzh.easyssh.domain.SSHConnect;
import cn.oyzh.store.jdbc.JdbcStandardStore;

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

    public synchronized List<SSHConnect> load() {
        return super.selectList();
    }

    @Override
    protected Class<SSHConnect> modelClass() {
        return SSHConnect.class;
    }

}
