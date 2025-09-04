package cn.oyzh.easyshell.store.zk;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.zk.ZKGroup;
import cn.oyzh.store.jdbc.DeleteParam;
import cn.oyzh.store.jdbc.JdbcStandardStore;
import cn.oyzh.store.jdbc.QueryParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * zk分组存储
 *
 * @author oyzh
 * @since 2023/5/12
 */
public class ZKGroupStore extends JdbcStandardStore<ZKGroup> {

    /**
     * 当前实例
     */
    public static final ZKGroupStore INSTANCE = new ZKGroupStore();

    /**
     * 加载数据
     *
     * @return 数据列表
     */
    public List<ZKGroup> load() {
        return super.selectList();
    }

    /**
     * 替换
     *
     * @param model 模型
     * @return 结果
     */
    public boolean replace(ZKGroup model) {
        if (model != null) {
            if (this.exist(model.getName()) || super.exist(model.getGid())) {
                return this.update(model);
            }
            return this.insert(model);
        }
        return false;
    }

    /**
     * 根据分组名称删除分组
     *
     * @param name 分组名称
     * @return 结果
     */
    public boolean delete(String name) {
        if (StringUtil.isNotBlank(name)) {
            DeleteParam param = new DeleteParam();
            param.addQueryParam(new QueryParam("name", name));
            return this.delete(param);
        }
        return false;
    }

    /**
     * 是否存在此分组
     *
     * @param name 分组名称
     * @return 结果
     */
    public boolean exist(String name) {
        if (StringUtil.isNotBlank(name)) {
            Map<String, Object> params = new HashMap<>();
            params.put("name", name);
            return super.exist(params);
        }
        return false;
    }

    @Override
    protected Class<ZKGroup> modelClass() {
        return ZKGroup.class;
    }
}
