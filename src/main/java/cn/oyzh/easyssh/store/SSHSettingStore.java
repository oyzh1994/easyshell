package cn.oyzh.easyssh.store;

import cn.oyzh.easyssh.domain.SSHSetting;
import cn.oyzh.store.jdbc.JdbcKeyValueStore;


/**
 * zk设置存储
 *
 * @author oyzh
 * @since 2024/09/23
 */
public class SSHSettingStore extends JdbcKeyValueStore<SSHSetting> {

    /**
     * 当前实例
     */
    public static final SSHSettingStore INSTANCE = new SSHSettingStore();

    /**
     * 当前设置
     */
    public static final SSHSetting SETTING = INSTANCE.load();

    /**
     * 加载
     *
     * @return zk设置
     */
    public SSHSetting load() {
        SSHSetting setting = null;
        try {
            setting = super.select();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (setting == null) {
            setting = new SSHSetting();
        }
        return setting;
    }

    /**
     * 替换
     *
     * @param model 模型
     * @return 结果
     */
    public boolean replace(SSHSetting model) {
        if (model != null) {
            return this.update(model);
        }
        return false;
    }

    @Override
    protected Class<SSHSetting> modelClass() {
        return SSHSetting.class;
    }
}
