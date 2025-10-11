package cn.oyzh.easyshell.domain;


import cn.oyzh.common.system.OSUtil;
import cn.oyzh.common.util.BooleanUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.domain.AppSetting;
import cn.oyzh.store.jdbc.Column;
import cn.oyzh.store.jdbc.Table;
import com.alibaba.fastjson2.annotation.JSONField;

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

    // /**
    //  * 是否显示隐藏文件
    //  */
    // @Column
    // private Boolean showHiddenFile;

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

    /**
     * 鼠标中键粘贴-终端
     */
    @Column
    private Boolean termPasteByMiddle;

    // /**
    //  * ssh协议，显示文件
    //  */
    // @Column
    // private Boolean sshShowFile;
    //
    // /**
    //  * ssh协议，显示服务监控
    //  */
    // @Column
    // private Boolean sshServerMonitor;
    //
    // /**
    //  * ssh协议，跟随终端目录
    //  */
    // @Column
    // private Boolean sshFollowTerminalDir;

    /**
     * 连接，显示类型
     */
    @Column
    private Boolean connectShowType;

    /**
     * 连接，显示更多信息
     */
    @Column
    private Boolean connectShowMoreInfo;

    /**
     * redis 键加载上限
     */
    @Column
    private Integer keyLoadLimit;

    /**
     * redis 行页码限制
     */
    @Column
    private Integer rowPageLimit;

    /**
     * gitee更新id
     */
    @Column
    private String giteeId;

    /**
     * 同步令牌
     */
    @Column
    private String syncToken;

    /**
     * github更新id
     */
    @Column
    private String githubId;

    /**
     * 更新时间
     */
    @Column
    private Long syncTime;

    /**
     * 同步类型
     * gitee
     * github
     */
    @Column
    private String syncType;

    /**
     * 同步密钥
     */
    @Column
    private Boolean syncKey;

    /**
     * 同步分组
     */
    @Column
    private Boolean syncGroup;

    /**
     * 同步片段
     */
    @Column
    private Boolean syncSnippet;

    /**
     * 同步连接
     */
    @Column
    private Boolean syncConnect;

    public int getKeyLoadLimit() {
        return this.keyLoadLimit == null ? 500 : this.keyLoadLimit;
    }

    public void setKeyLoadLimit(int keyLoadLimit) {
        this.keyLoadLimit = keyLoadLimit;
    }

    public void setRowPageLimit(int rowPageLimit) {
        this.rowPageLimit = rowPageLimit;
    }

    public int getRowPageLimit() {
        return this.rowPageLimit == null ? 100 : this.rowPageLimit;
    }

    public boolean isHiddenLeftAfterConnected() {
        return this.hiddenLeftAfterConnected == null || BooleanUtil.isTrue(this.hiddenLeftAfterConnected);
    }

    public void setHiddenLeftAfterConnected(boolean hiddenLeftAfterConnected) {
        this.hiddenLeftAfterConnected = hiddenLeftAfterConnected;
    }

    @Override
    public void copy(Object o) {
        super.copy(o);
        if (o instanceof ShellSetting setting) {
            this.x11Path = setting.x11Path;
            this.termBeep = setting.termBeep;
            this.termType = setting.termType;
            this.efficiencyMode = setting.efficiencyMode;
            this.termRefreshRate = setting.termRefreshRate;
            this.connectShowType = setting.connectShowType;
            this.termCursorBlinks = setting.termCursorBlinks;
            this.termMaxLineCount = setting.termMaxLineCount;
            this.termParseHyperlink = setting.termParseHyperlink;
            this.termCopyOnSelected = setting.termCopyOnSelected;
            this.connectShowMoreInfo = setting.connectShowMoreInfo;
            this.termUseAntialiasing = setting.termUseAntialiasing;
            this.hiddenLeftAfterConnected = setting.hiddenLeftAfterConnected;
            // redis
            this.rowPageLimit = setting.rowPageLimit;
            this.keyLoadLimit = setting.keyLoadLimit;
            // zookeeper
            this.loadMode = setting.loadMode;
            this.viewport = setting.viewport;
            this.authMode = setting.authMode;
            this.nodeLoadLimit = setting.nodeLoadLimit;
        }
    }

    // public boolean isShowHiddenFile() {
    //     return this.showHiddenFile == null || BooleanUtil.isTrue(this.showHiddenFile);
    // }

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

    // public boolean getShowHiddenFile() {
    //     return showHiddenFile;
    // }
    //
    // public void setShowHiddenFile(Boolean showHiddenFile) {
    //     this.showHiddenFile = showHiddenFile;
    // }

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

    public void setTermBeep(boolean termBeep) {
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
        return BooleanUtil.isTrue(this.termCopyOnSelected);
    }

    public void setTermCopyOnSelected(boolean termCopyOnSelected) {
        this.termCopyOnSelected = termCopyOnSelected;
    }

    public boolean isTermPasteByMiddle() {
        return this.termPasteByMiddle == null || this.termPasteByMiddle;
    }

    public void setTermPasteByMiddle(boolean termPasteByMiddle) {
        this.termPasteByMiddle = termPasteByMiddle;
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

    public boolean getTermUseAntialiasing() {
        return termUseAntialiasing;
    }

    public void setTermUseAntialiasing(boolean termUseAntialiasing) {
        this.termUseAntialiasing = termUseAntialiasing;
    }

    public boolean isEfficiencyMode() {
        return this.efficiencyMode == null || BooleanUtil.isTrue(this.efficiencyMode);
    }

    public void setEfficiencyMode(boolean efficiencyMode) {
        this.efficiencyMode = efficiencyMode;
    }

    public boolean isTermParseHyperlink() {
        return termParseHyperlink == null ? Boolean.TRUE : termParseHyperlink;
    }

    public void setTermParseHyperlink(boolean termParseHyperlink) {
        this.termParseHyperlink = termParseHyperlink;
    }

    // public boolean isSshShowFile() {
    //     return this.sshShowFile == null || this.sshShowFile;
    // }
    //
    // public void setSshShowFile(Boolean sshShowFile) {
    //     this.sshShowFile = sshShowFile;
    // }
    //
    // public boolean isSshServerMonitor() {
    //     return BooleanUtil.isTrue(this.sshServerMonitor);
    // }
    //
    // public void setSshServerMonitor(Boolean sshServerMonitor) {
    //     this.sshServerMonitor = sshServerMonitor;
    // }
    //
    // public boolean isSshFollowTerminalDir() {
    //     return BooleanUtil.isTrue(this.sshFollowTerminalDir);
    // }
    //
    // public void setSshFollowTerminalDir(boolean sshFollowTerminalDir) {
    //     this.sshFollowTerminalDir = sshFollowTerminalDir;
    // }

    public boolean isConnectShowType() {
        return this.connectShowType == null || this.connectShowType;
    }

    public void setConnectShowType(Boolean connectShowType) {
        this.connectShowType = connectShowType;
    }

    public boolean isConnectShowMoreInfo() {
        return BooleanUtil.isTrue(this.connectShowMoreInfo);
    }

    public void setConnectShowMoreInfo(boolean connectShowMoreInfo) {
        this.connectShowMoreInfo = connectShowMoreInfo;
    }

    /**
     * zookeeper 节点加载
     * 0|null 加载一级节点
     * 1 加载所有节点
     * 2 仅加载根节点
     */
    @Column
    private Byte loadMode;

    /**
     * zookeeper 节点视图
     * 0|null 节点名称
     * 1 节点路径
     */
    @Column
    private Byte viewport;

    /**
     * zookeeper 节点认证
     * 0|null 自动认证
     * 1 不自动认证
     */
    @Column
    private Byte authMode;

    /**
     * zookeeper 节点加载限制
     * 0 无限制
     */
    @Column
    private Integer nodeLoadLimit;

    /**
     * zookeeper 是否自动认证
     *
     * @return 结果
     */
    @JSONField(serialize = false, deserialize = false)
    public boolean isAutoAuth() {
        return this.authMode == null || this.authMode == 0;
    }

    /**
     * zookeeper 是否加载所有节点
     *
     * @return 结果
     */
    @JSONField(serialize = false, deserialize = false)
    public boolean isLoadAll() {
        return this.loadMode != null && this.loadMode == 1;
    }

    /**
     * zookeeper 是否显示节点路径
     *
     * @return 结果
     */
    @JSONField(serialize = false, deserialize = false)
    public boolean isShowNodePath() {
        return this.viewport == null || this.viewport == 1;
    }

    /**
     * zookeeper 是否加载一级节点
     *
     * @return 结果
     */
    @JSONField(serialize = false, deserialize = false)
    public boolean isLoadFirst() {
        return this.loadMode == null || this.loadMode == 0;
    }

    /**
     * zookeeper 是否仅加载根节点
     *
     * @return 结果
     */
    @JSONField(serialize = false, deserialize = false)
    public boolean isLoadRoot() {
        return this.loadMode != null && this.loadMode == 2;
    }

    /**
     * zookeeper 获取节点加载限制
     *
     * @return 节点加载限制
     */
    @JSONField(serialize = false, deserialize = false)
    public int nodeLoadLimit() {
        return this.nodeLoadLimit == null ? 0 : this.nodeLoadLimit;
    }

    public Byte getLoadMode() {
        return loadMode;
    }

    public void setLoadMode(Byte loadMode) {
        this.loadMode = loadMode;
    }

    public Byte getViewport() {
        return viewport;
    }

    public void setViewport(Byte viewport) {
        this.viewport = viewport;
    }

    public Byte getAuthMode() {
        return authMode;
    }

    public void setAuthMode(Byte authMode) {
        this.authMode = authMode;
    }

    public Integer getNodeLoadLimit() {
        return nodeLoadLimit;
    }

    public void setNodeLoadLimit(Integer nodeLoadLimit) {
        this.nodeLoadLimit = nodeLoadLimit;
    }

    public String getGiteeId() {
        return giteeId;
    }

    public void setGiteeId(String giteeId) {
        this.giteeId = giteeId;
    }

    public String getGithubId() {
        return githubId;
    }

    public void setGithubId(String githubId) {
        this.githubId = githubId;
    }

    public Long getSyncTime() {
        return syncTime;
    }

    public void setSyncTime(Long syncTime) {
        this.syncTime = syncTime;
    }

    public String getSyncType() {
        return syncType;
    }

    public void setSyncType(String syncType) {
        this.syncType = syncType;
    }

    public boolean isSyncKey() {
        return syncKey == null || syncKey;
    }

    public void setSyncKey(boolean syncKey) {
        this.syncKey = syncKey;
    }

    public boolean isSyncGroup() {
        return syncGroup == null || syncGroup;
    }

    public void setSyncGroup(boolean syncGroup) {
        this.syncGroup = syncGroup;
    }

    public boolean isSyncSnippet() {
        return syncSnippet == null || syncSnippet;
    }

    public void setSyncSnippet(boolean syncSnippet) {
        this.syncSnippet = syncSnippet;
    }

    public boolean isSyncConnect() {
        return syncConnect == null || syncConnect;
    }

    public void setSyncConnect(boolean syncConnect) {
        this.syncConnect = syncConnect;
    }

    public String getSyncToken() {
        return syncToken;
    }

    public void setSyncToken(String syncToken) {
        this.syncToken = syncToken;
    }

    @JSONField(serialize = false, deserialize = false)
    public boolean isGiteeType() {
        return syncType == null || StringUtil.equalsIgnoreCase(syncType, "gitee");
    }

    @JSONField(serialize = false, deserialize = false)
    public boolean isGithubType() {
        return StringUtil.equalsIgnoreCase(syncType, "github");
    }
}
