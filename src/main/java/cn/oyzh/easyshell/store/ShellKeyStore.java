package cn.oyzh.easyshell.store;

import cn.oyzh.easyshell.domain.ShellKey;
import cn.oyzh.store.jdbc.JdbcStandardStore;

/**
 * shell 密钥存储
 *
 * @author oyzh
 * @since 2025/04/03
 */
public class ShellKeyStore extends JdbcStandardStore<ShellKey> {

    /**
     * 当前实例
     */
    public static final ShellKeyStore INSTANCE = new ShellKeyStore();

    public boolean replace(ShellKey model) {
        String id = model.getId();
        if (super.exist(id)) {
            return super.update(model);
        }
        return this.insert(model);
    }

    @Override
    protected Class<ShellKey> modelClass() {
        return ShellKey.class;
    }
}
