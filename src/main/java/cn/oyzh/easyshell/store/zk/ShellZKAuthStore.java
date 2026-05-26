package cn.oyzh.easyshell.store.zk;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.zk.ShellZKAuth;
import cn.oyzh.store.jdbc.JdbcStandardStore;
import cn.oyzh.store.jdbc.param.DeleteParam;
import cn.oyzh.store.jdbc.param.QueryParam;
import cn.oyzh.store.jdbc.param.SelectParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * zk认证存储
 *
 * @author oyzh
 * @since 2024/09/24
 */
public class ShellZKAuthStore extends JdbcStandardStore<ShellZKAuth> {

    /**
     * 当前实例
     */
    public static final ShellZKAuthStore INSTANCE = new ShellZKAuthStore();

    /**
     * 加载数据列表
     *
     * @param iid zk连接id
     * @return 数据列表
     * @see cn.oyzh.easyshell.domain.ShellConnect
     */
    public List<ShellZKAuth> loadByIid(String iid) {
        return super.selectList(QueryParam.of("iid", iid));
    }

    /**
     * 加载已启用的数据列表
     *
     * @param iid zk连接id
     * @return 数据列表
     * @see cn.oyzh.easyshell.domain.ShellConnect
     */
    public List<ShellZKAuth> loadEnableByIid(String iid) {
        List<ShellZKAuth> auths = this.loadByIid(iid);
        return auths.stream().filter(ShellZKAuth::isEnable).toList();
    }

    /**
     * 替换
     *
     * @param model 模型
     * @return 结果
     */
    public boolean replace(ShellZKAuth model) {
        boolean result = false;
        if (model != null) {
            ShellZKAuth auth = this.select(model.getUser(), model.getPassword(), model.getIid());
            if (auth != null) {
                auth.setUser(model.getUser());
                auth.setEnable(model.isEnable());
                auth.setPassword(model.getPassword());
                result = this.update(auth);
            } else {
                result = this.insert(model);
            }
        }
        return result;
    }

    /**
     * 根据iid删除数据
     *
     * @param iid zk连接id
     * @return 结果
     * @see cn.oyzh.easyshell.domain.ShellConnect
     */
    public boolean deleteByIid(String iid) {
        if (StringUtil.isNotBlank(iid)) {
            DeleteParam param = new DeleteParam();
            param.addQueryParam(new QueryParam("iid", iid));
            return this.delete(param);
        }
        return false;
    }

    /**
     * 是否存在认证
     *
     * @param user     用户名
     * @param password 密码
     * @param iid      zk连接id
     * @return 结果
     * @see cn.oyzh.easyshell.domain.ShellConnect
     */
    public boolean exist(String user, String password, String iid) {
        if (StringUtil.isNotBlank(user) && StringUtil.isNotBlank(password) && StringUtil.isNotBlank(iid)) {
            Map<String, Object> params = new HashMap<>();
            params.put("iid", iid);
            params.put("user", user);
            params.put("password", password);
            return super.exist(params);
        }
        return false;
    }

    /**
     * 查询认证
     *
     * @param user     用户名
     * @param password 密码
     * @param iid      zk连接id
     * @return 结果
     * @see cn.oyzh.easyshell.domain.ShellConnect
     */
    public ShellZKAuth select(String user, String password, String iid) {
        if (StringUtil.isNotBlank(user) && StringUtil.isNotBlank(password) && StringUtil.isNotBlank(iid)) {
            SelectParam params = new SelectParam();
            params.addQueryParam(new QueryParam("iid", iid));
            params.addQueryParam(new QueryParam("user", user));
            params.addQueryParam(new QueryParam("password", password));
            return super.selectOne(params);
        }
        return null;
    }

    @Override
    protected Class<ShellZKAuth> modelClass() {
        return ShellZKAuth.class;
    }
}
