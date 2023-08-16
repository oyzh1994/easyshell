package cn.oyzh.easyssh.store;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.oyzh.common.util.FileStore;
import cn.oyzh.easyssh.SSHConst;
import cn.oyzh.easyssh.domain.SSHSetting;
import com.alibaba.fastjson.JSON;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.List;


/**
 * ssh设置储存
 *
 * @author oyzh
 * @since 2026/06/26
 */
@Slf4j
public class SSHSettingStore extends FileStore<SSHSetting> {

    /**
     * 当前实例
     */
    public static final SSHSettingStore INSTANCE = new SSHSettingStore();

    /**
     * 当前设置
     */
    public static final SSHSetting SETTING = INSTANCE.loadOne();

    {
        this.filePath(SSHConst.STORE_PATH + "ssh_setting.json");
        log.info("SSHSettingStore filePath:{} charset:{} init {}.", this.filePath(), this.charset(), super.init() ? "success" : "fail");
    }

    @Override
    public synchronized List<SSHSetting> load() {
        SSHSetting setting = null;
        String text = FileUtil.readString(this.storeFile(), this.charset());
        if (StrUtil.isNotBlank(text)) {
            try {
                setting = JSON.parseObject(text, SSHSetting.class);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if (setting == null) {
            setting = new SSHSetting();
        }
        return List.of(setting);
    }

    @Override
    public boolean add(@NonNull SSHSetting setting) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean update(@NonNull SSHSetting setting) {
        return this.save(setting);
    }

    @Override
    public boolean delete(@NonNull SSHSetting setting) {
        throw new UnsupportedOperationException();
    }
}
