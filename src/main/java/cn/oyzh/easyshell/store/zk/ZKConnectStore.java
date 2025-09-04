package cn.oyzh.easyshell.store.zk;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.domain.zk.ZKAuth;
import cn.oyzh.easyshell.domain.zk.ZKCollect;
import cn.oyzh.easyshell.domain.zk.ZKConnect;
import cn.oyzh.easyshell.domain.zk.ZKFilter;
import cn.oyzh.easyshell.domain.zk.ZKJumpConfig;
import cn.oyzh.easyshell.domain.zk.ZKSASLConfig;
import cn.oyzh.easyshell.store.zk.ZKCollectStore;
import cn.oyzh.easyshell.store.zk.ZKFilterStore;
import cn.oyzh.easyshell.store.zk.ZKJumpConfigStore;
import cn.oyzh.easyshell.store.zk.ZKSASLConfigStore;
import cn.oyzh.store.jdbc.DeleteParam;
import cn.oyzh.store.jdbc.JdbcStandardStore;
import cn.oyzh.store.jdbc.QueryParam;

import java.util.List;

/**
 * zk连接存储
 *
 * @author oyzh
 * @since 2024/09/26
 */
public class ZKConnectStore extends JdbcStandardStore<ZKConnect> {

    /**
     * 当前实例
     */
    public static final ZKConnectStore INSTANCE = new ZKConnectStore();

    /**
     * 认证存储
     */
    private final ZKAuthStore authStore = ZKAuthStore.INSTANCE;

    /**
     * 过滤存储
     */
    private final ZKFilterStore filterStore = ZKFilterStore.INSTANCE;

    /**
     * 收藏存储
     */
    private final ZKCollectStore collectStore = ZKCollectStore.INSTANCE;

    /**
     * 跳板配置存储
     */
    private final ZKJumpConfigStore jumpConfigStore = ZKJumpConfigStore.INSTANCE;

    /**
     * sasl配置存储
     */
    private final ZKSASLConfigStore saslConfigStore = ZKSASLConfigStore.INSTANCE;

    /**
     * 加载列表
     *
     * @return zk连接列表
     */
    public List<ZKConnect> load() {
        return super.selectList();
    }

    /**
     * 加载列表，完整信息，给导出用
     *
     * @return zk连接列表
     */
    public List<ZKConnect> loadFull() {
        List<ZKConnect> connects = super.selectList();
        for (ZKConnect connect : connects) {
            connect.setAuths(this.authStore.loadByIid(connect.getId()));
            connect.setFilters(this.filterStore.loadByIid(connect.getId()));
            connect.setCollects(this.collectStore.loadByIid(connect.getId()));
            connect.setSaslConfig(this.saslConfigStore.getByIid(connect.getId()));
            connect.setJumpConfigs(this.jumpConfigStore.loadByIid(connect.getId()));
        }
        return connects;
    }

    /**
     * 替换
     *
     * @param model 模型
     * @return 结果
     */
    public boolean replace(ZKConnect model) {
        boolean result = false;
        if (model != null) {
            if (super.exist(model.getId())) {
                result = this.update(model);
            } else {
                result = this.insert(model);
            }

            // 跳板机处理
            List<ZKJumpConfig> jumpConfigs = model.getJumpConfigs();
            if (CollectionUtil.isNotEmpty(jumpConfigs)) {
                for (ZKJumpConfig jumpConfig : jumpConfigs) {
                    jumpConfig.setIid(model.getId());
                    this.jumpConfigStore.replace(jumpConfig);
                }
            }

            // sasl处理
            ZKSASLConfig saslConfig = model.getSaslConfig();
            if (saslConfig != null) {
                saslConfig.setIid(model.getId());
                this.saslConfigStore.replace(saslConfig);
            } else {
                DeleteParam param = new DeleteParam();
                param.addQueryParam(new QueryParam("iid", model.getId()));
                this.saslConfigStore.delete(param);
            }

            // 收藏处理
            List<ZKCollect> collects = model.getCollects();
            if (CollectionUtil.isNotEmpty(collects)) {
                for (ZKCollect collect : collects) {
                    collect.setIid(model.getId());
                    this.collectStore.replace(collect);
                }
            }

            // 认证处理
            List<ZKAuth> auths = model.getAuths();
            if (CollectionUtil.isNotEmpty(auths)) {
                for (ZKAuth auth : auths) {
                    auth.setIid(model.getId());
                    this.authStore.replace(auth);
                }
            }

            // 过滤处理
            List<ZKFilter> filters = model.getFilters();
            if (CollectionUtil.isNotEmpty(filters)) {
                for (ZKFilter filter : filters) {
                    filter.setIid(model.getId());
                    this.filterStore.replace(filter);
                }
            }
        }
        return result;
    }

    @Override
    public boolean delete(ZKConnect model) {
        boolean result = super.delete(model);
        // 删除关联配置
        if (result) {
            this.authStore.deleteByIid(model.getId());
            this.filterStore.deleteByIid(model.getId());
            this.collectStore.deleteByIid(model.getId());
            this.jumpConfigStore.deleteByIid(model.getId());
            this.saslConfigStore.deleteByIid(model.getId());
        }
        return result;
    }

    @Override
    protected Class<ZKConnect> modelClass() {
        return ZKConnect.class;
    }
}
