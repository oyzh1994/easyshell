package cn.oyzh.easyshell.store.zk;

import cn.oyzh.easyshell.domain.zk.ZKSetting;
import cn.oyzh.store.jdbc.JdbcKeyValueStore;


/**
 * zk设置存储
 *
 * @author oyzh
 * @since 2024/09/23
 */
public class ZKSettingStore extends JdbcKeyValueStore<ZKSetting> {

    /**
     * 当前实例
     */
    public static final ZKSettingStore INSTANCE = new ZKSettingStore();

    /**
     * 当前设置
     */
    public static final ZKSetting SETTING = INSTANCE.load();

    /**
     * 加载
     *
     * @return zk设置
     */
    public ZKSetting load() {
        ZKSetting setting = null;
        try {
            setting = super.select();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (setting == null) {
            setting = new ZKSetting();
        }
        return setting;
    }

    /**
     * 替换
     *
     * @param model 模型
     * @return 结果
     */
    public boolean replace(ZKSetting model) {
        if (model != null) {
            return this.update(model);
        }
        return false;
    }

    @Override
    protected Class<ZKSetting> modelClass() {
        return ZKSetting.class;
    }
}
