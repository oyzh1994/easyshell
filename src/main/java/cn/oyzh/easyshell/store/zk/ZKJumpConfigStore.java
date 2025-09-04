package cn.oyzh.easyshell.store.zk;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.zk.ZKJumpConfig;
import cn.oyzh.ssh.domain.SSHConnect;
import cn.oyzh.store.jdbc.DeleteParam;
import cn.oyzh.store.jdbc.JdbcStandardStore;
import cn.oyzh.store.jdbc.QueryParam;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * zk跳板配置存储
 *
 * @author oyzh
 * @since 2025/05/19
 */
public class ZKJumpConfigStore extends JdbcStandardStore<ZKJumpConfig> {

    /**
     * 当前实例
     */
    public static final ZKJumpConfigStore INSTANCE = new ZKJumpConfigStore();

    public boolean replace(List<ZKJumpConfig> models) {
        try {
            for (ZKJumpConfig model : models) {
                this.replace(model);
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean replace(ZKJumpConfig model) {
        if (super.exist(model.getId())) {
            return super.update(model);
        }
        return this.insert(model);
    }

    @Override
    protected Class<ZKJumpConfig> modelClass() {
        return ZKJumpConfig.class;
    }

    /**
     * 根据iid删除
     *
     * @param iid shell连接id
     * @return 结果
     */
    public boolean deleteByIid(String iid) {
        if (StringUtil.isEmpty(iid)) {
            return false;
        }
        DeleteParam param = new DeleteParam();
        param.addQueryParam(new QueryParam("iid", iid));
        return super.delete(param);
    }

    /**
     * 根据shell连接id获取配置
     *
     * @param iid shell连接id
     * @return ssh跳板配置
     */
    public List<ZKJumpConfig> loadByIid(String iid) {
        if (StringUtil.isEmpty(iid)) {
            return null;
        }
        List<ZKJumpConfig> configs = super.selectList(QueryParam.of("iid", iid));
        // 过滤历史原因造成的无效配置
        List<ZKJumpConfig> results = new ArrayList<>();
        for (ZKJumpConfig config : configs) {
            if (StringUtil.isNotBlank(config.getUser(), config.getHost())) {
                results.add(config);
            }
        }
        // 执行排序
        results.sort(Comparator.comparingInt(SSHConnect::getOrder));
        return results;
    }
}
