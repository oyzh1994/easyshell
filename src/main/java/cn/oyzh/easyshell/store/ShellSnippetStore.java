package cn.oyzh.easyshell.store;

import cn.oyzh.easyshell.domain.ShellSnippet;
import cn.oyzh.store.jdbc.JdbcStandardStore;
import cn.oyzh.store.jdbc.param.QueryParam;
import cn.oyzh.store.jdbc.param.SelectParam;

import java.util.List;

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

    /**
     * 根据名称加载列表
     *
     * @param name 名称
     * @return 结果
     */
    public List<ShellSnippet> listByName(String name) {
        SelectParam param = new SelectParam();
        param.addQueryParam(QueryParam.of("name", "%" + name + "%", "LIKE"));
        return super.selectList(param);
    }

    @Override
    protected Class<ShellSnippet> modelClass() {
        return ShellSnippet.class;
    }
}
