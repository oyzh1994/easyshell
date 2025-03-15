package cn.oyzh.easyshell.store;

import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.store.jdbc.JdbcKeyValueStore;


/**
 * shell设置存储
 *
 * @author oyzh
 * @since 2024/09/23
 */
public class ShellSettingStore extends JdbcKeyValueStore<ShellSetting> {

    /**
     * 当前实例
     */
    public static final ShellSettingStore INSTANCE = new ShellSettingStore();

    /**
     * 当前设置
     */
    public static final ShellSetting SETTING = INSTANCE.load();

    /**
     * 加载
     *
     * @return shell设置
     */
    public ShellSetting load() {
        ShellSetting setting = null;
        try {
            setting = super.select();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (setting == null) {
            setting = new ShellSetting();
        }
        return setting;
    }

    /**
     * 替换
     *
     * @param model 模型
     * @return 结果
     */
    public boolean replace(ShellSetting model) {
        if (model != null) {
            return this.update(model);
        }
        return false;
    }

    @Override
    protected Class<ShellSetting> modelClass() {
        return ShellSetting.class;
    }
}
