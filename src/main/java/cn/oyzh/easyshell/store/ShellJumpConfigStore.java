package cn.oyzh.easyshell.store;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellJumpConfig;
import cn.oyzh.ssh.domain.SSHConnect;
import cn.oyzh.store.jdbc.DeleteParam;
import cn.oyzh.store.jdbc.JdbcStandardStore;
import cn.oyzh.store.jdbc.QueryParam;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * shell跳板配置存储
 *
 * @author oyzh
 * @since 2025/03/15
 */
public class ShellJumpConfigStore extends JdbcStandardStore<ShellJumpConfig> {

    /**
     * 当前实例
     */
    public static final ShellJumpConfigStore INSTANCE = new ShellJumpConfigStore();

    public boolean replace(List<ShellJumpConfig> models) {
        try {
            for (ShellJumpConfig model : models) {
                this.replace(model);
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean replace(ShellJumpConfig model) {
        if (super.exist(model.getId())) {
            return super.update(model);
        }
        return this.insert(model);
    }

    @Override
    protected Class<ShellJumpConfig> modelClass() {
        return ShellJumpConfig.class;
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
    public List<ShellJumpConfig> listByIid(String iid) {
        if (StringUtil.isEmpty(iid)) {
            return null;
        }
        List<ShellJumpConfig> configs = super.selectList(QueryParam.of("iid", iid));
        // 过滤历史原因造成的无效配置
        List<ShellJumpConfig> results = new ArrayList<>();
        for (ShellJumpConfig config : configs) {
            if (StringUtil.isNotBlank(config.getUser(), config.getHost())) {
                results.add(config);
            }
        }
        // 执行排序
        results.sort(Comparator.comparingInt(SSHConnect::getOrder));
        return results;
    }
}
