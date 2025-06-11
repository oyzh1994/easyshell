package cn.oyzh.easyshell.store;

import cn.oyzh.easyshell.domain.ShellSnippet;
import cn.oyzh.store.jdbc.JdbcStandardStore;

/**
 * shell 代码片段存储
 *
 * @author oyzh
 * @since 2025/06/11
 */
public class ShellSnippetStore extends JdbcStandardStore<ShellSnippet> {

    /**
     * 当前实例
     */
    public static final ShellSnippetStore INSTANCE = new ShellSnippetStore();

    public boolean replace(ShellSnippet model) {
        String id = model.getId();
        if (super.exist(id)) {
            return super.update(model);
        }
        return this.insert(model);
    }

    @Override
    protected Class<ShellSnippet> modelClass() {
        return ShellSnippet.class;
    }
}
