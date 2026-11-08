package cn.oyzh.easyshell.store.http;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.http.ShellHttpSetting;
import cn.oyzh.store.jdbc.JdbcStandardStore;
import cn.oyzh.store.jdbc.param.DeleteParam;
import cn.oyzh.store.jdbc.param.QueryParam;

/**
 * HTTP 工作区设置存储。
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellHttpSettingStore extends JdbcStandardStore<ShellHttpSetting> {

    public static final ShellHttpSettingStore INSTANCE = new ShellHttpSettingStore();

    /**
     * 根据连接id获取设置。
     *
     * @param iid 连接id
     * @return 设置
     */
    public ShellHttpSetting getByIid(String iid) {
        if (StringUtil.isBlank(iid)) {
            return null;
        }
        return super.selectOne(QueryParam.of("iid", iid));
    }

    /**
     * 新增或更新设置。
     *
     * @param model 设置
     * @return 结果
     */
    public boolean replace(ShellHttpSetting model) {
        if (model == null) {
            return false;
        }
        ShellHttpSetting saved = this.getByIid(model.getIid());
        if (saved != null) {
            model.setId(saved.getId());
            return super.update(model);
        }
        return super.insert(model);
    }

    /**
     * 根据连接id删除设置。
     *
     * @param iid 连接id
     * @return 结果
     */
    public boolean deleteByIid(String iid) {
        if (StringUtil.isBlank(iid)) {
            return false;
        }
        DeleteParam param = new DeleteParam();
        param.addQueryParam(QueryParam.of("iid", iid));
        return super.delete(param);
    }

    @Override
    protected Class<ShellHttpSetting> modelClass() {
        return ShellHttpSetting.class;
    }
}
