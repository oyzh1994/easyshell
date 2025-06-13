package cn.oyzh.easyshell.domain;


import cn.oyzh.common.system.OSUtil;
import cn.oyzh.common.util.BooleanUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.domain.AppSetting;
import cn.oyzh.store.jdbc.Column;
import cn.oyzh.store.jdbc.Table;

/**
 * shell设置
 *
 * @author oyzh
 * @since 2023/6/16
 */
@Table("t_setting")
public class ShellSetting extends AppSetting {

    /**
     * x11目录
     */
    @Column
    private String x11Path;

    /**
     * ssh效率模式
     */
    @Column
    private Boolean efficiencyMode;

    /**
     * 是否显示隐藏文件
     */
    @Column
    private Boolean showHiddenFile;

    /**
     * 连接后收起左侧
     */
    @Column
    private Boolean hiddenLeftAfterConnected;

    /**
     * 终端类型
     */
    @Column
    private String termType;

    /**
     * 蜂鸣声-终端
     */
    @Column
    private Boolean termBeep;

    /**
     * 刷新率-终端
     */
    @Column
    private Integer termRefreshRate;

    /**
     * 光标闪烁-终端
     */
    @Column
    private Integer termCursorBlinks;

    /**
     * 最大行数-终端
     */
    @Column
    private Integer termMaxLineCount;

    /**
     * 选中时复制-终端
     */
    @Column
    private Boolean termCopyOnSelected;

    /**
     * 使用抗锯齿-终端
     */
    @Column
    private Boolean termUseAntialiasing;

    /**
     * 解析超链接-终端
     */
    @Column
    private Boolean termParseHyperlink;

    public boolean isHiddenLeftAfterConnected() {
        return this.hiddenLeftAfterConnected == null || BooleanUtil.isTrue(this.hiddenLeftAfterConnected);
    }

    public Boolean getHiddenLeftAfterConnected() {
        return hiddenLeftAfterConnected;
    }

    public void setHiddenLeftAfterConnected(Boolean hiddenLeftAfterConnected) {
        this.hiddenLeftAfterConnected = hiddenLeftAfterConnected;
    }

    @Override
    public void copy(Object o) {
        super.copy(o);
        if (o instanceof ShellSetting setting) {
            this.x11Path = setting.x11Path;
            this.termBeep = setting.termBeep;
            this.termType = setting.termType;
            this.showHiddenFile = setting.showHiddenFile;
            this.efficiencyMode = setting.efficiencyMode;
            this.termRefreshRate = setting.termRefreshRate;
            this.termCursorBlinks = setting.termCursorBlinks;
            this.termMaxLineCount = setting.termMaxLineCount;
            this.termParseHyperlink = setting.termParseHyperlink;
            this.termCopyOnSelected = setting.termCopyOnSelected;
            this.termUseAntialiasing = setting.termUseAntialiasing;
            this.hiddenLeftAfterConnected = setting.hiddenLeftAfterConnected;
        }
    }

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
            return new String[]{"vcxsrv.exe", "XWin_MobaX.exe", "XWin.exe"};
        }
        return null;
    }

    public String getX11Path() {
        return x11Path;
    }

    public void setX11Path(String x11Path) {
        this.x11Path = x11Path;
    }

    public Boolean getShowHiddenFile() {
        return showHiddenFile;
    }

    public void setShowHiddenFile(Boolean showHiddenFile) {
        this.showHiddenFile = showHiddenFile;
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

    public String getTermType() {
        return termType;
    }

    public void setTermType(String termType) {
        this.termType = termType;
    }

    public boolean isTermBeep() {
        return termBeep == null || termBeep;
    }

    public void setTermBeep(Boolean termBeep) {
        this.termBeep = termBeep;
    }

    public int getTermCursorBlinks() {
        return termCursorBlinks == null ? 500 : termCursorBlinks;
    }

    public void setTermCursorBlinks(Integer termCursorBlinks) {
        this.termCursorBlinks = termCursorBlinks;
    }

    public Integer getTermMaxLineCount() {
        return termMaxLineCount == null ? 5000 : termMaxLineCount;
    }

    public void setTermMaxLineCount(Integer termMaxLineCount) {
        this.termMaxLineCount = termMaxLineCount;
    }

    public boolean isTermCopyOnSelected() {
        return termCopyOnSelected == null ? Boolean.FALSE : termCopyOnSelected;
    }

    public void setTermCopyOnSelected(Boolean termCopyOnSelected) {
        this.termCopyOnSelected = termCopyOnSelected;
    }

    public Integer getTermRefreshRate() {
        return termRefreshRate == null || termRefreshRate <= 0 ? -1 : termRefreshRate;
    }

    public void setTermRefreshRate(Integer termRefreshRate) {
        this.termRefreshRate = termRefreshRate;
    }

    public boolean isTermUseAntialiasing() {
        return termUseAntialiasing == null ? Boolean.TRUE : termUseAntialiasing;
    }

    public Boolean getTermUseAntialiasing() {
        return termUseAntialiasing;
    }

    public void setTermUseAntialiasing(Boolean termUseAntialiasing) {
        this.termUseAntialiasing = termUseAntialiasing;
    }

    public boolean isEfficiencyMode() {
        return this.efficiencyMode == null || BooleanUtil.isTrue(this.efficiencyMode);
    }

    public void setEfficiencyMode(Boolean efficiencyMode) {
        this.efficiencyMode = efficiencyMode;
    }

    public boolean isTermParseHyperlink() {
        return termParseHyperlink == null ? Boolean.TRUE : termParseHyperlink;
    }

    public void setTermParseHyperlink(Boolean termParseHyperlink) {
        this.termParseHyperlink = termParseHyperlink;
    }
}
