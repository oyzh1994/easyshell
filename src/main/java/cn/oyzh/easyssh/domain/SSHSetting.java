package cn.oyzh.easyssh.domain;


import cn.oyzh.common.system.OSUtil;
import cn.oyzh.common.util.BooleanUtil;
import cn.oyzh.common.util.StringUtil;
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
     * x11目录
     */
    @Column
    private String x11Path;

    /**
     * 终端类型
     */
    @Column
    private String terminalType;

    /**
     * 是否显示隐藏文件
     */
    @Column
    private Boolean showHiddenFile;

    public boolean isShowHiddenFile() {
        return this.showHiddenFile == null || BooleanUtil.isTrue(this.showHiddenFile);
    }

    public String x11Path() {
        if (StringUtil.isNotBlank(this.x11Path)) {
            return this.x11Path;
        }
        if (OSUtil.isMacOS()) {
            return "/opt/X11";
        }
        if (OSUtil.isWindows()) {
            return "C:/Program Files/VcXsrv";
        }
        return "";
    }

    public String[] x11Binary() {
        if (OSUtil.isMacOS()) {
            return new String[]{"startx"};
        }
        if (OSUtil.isWindows()) {
            return new String[]{"vcxsrv.exe", "XWin_MobaX.exe","XWin.exe"};
        }
        return null;
    }

    public String x11WorkDir() {
        String x11Path = this.x11Path();
        if (StringUtil.isBlank(x11Path)) {
            return null;
        }
        if (OSUtil.isMacOS()) {
            return x11Path + "/bin/";
        }
        if (OSUtil.isWindows()) {
            return x11Path;
        }
        return null;
    }
}
