package cn.oyzh.easyssh.domain;


import cn.oyzh.common.util.BooleanUtil;
import cn.oyzh.fx.plus.domain.AppSetting;
import cn.oyzh.store.jdbc.Column;
import cn.oyzh.store.jdbc.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ssh设置
 *
 * @author oyzh
 * @since 2023/6/16
 */
@Data
@Table("t_setting")
@EqualsAndHashCode(callSuper = true)
public class SSHSetting extends AppSetting {

    /**
     * 是否显示隐藏文件
     */
    @Column
    private Boolean showHiddenFile;

    public boolean isShowHiddenFile() {
        return this.showHiddenFile == null || BooleanUtil.isTrue(this.showHiddenFile);
    }
}
