package cn.oyzh.easyshell.domain;


import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.system.OSUtil;
import cn.oyzh.common.util.BooleanUtil;
import cn.oyzh.common.util.ResourceUtil;
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
    @Deprecated
    @Column
    private String x11Path;

    //    /**
    //     * ssh效率模式
    //     */
    //    @Column
    //    private Boolean efficiencyMode;

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
     * 光标样式-终端
     */
    @Column
    private int termCursorStyle;

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
    @Deprecated
    private Boolean termPasteByMiddle;

    /**
     * 背景图片-终端
     */
    @Column
    private String termBackgroundImage;

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
     * 同步令牌
     */
    @Column
    private String syncToken;

    /**
     * 同步id
     */
    @Column
    private String syncId;

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

    /**
     * 启用快捷键
     */
    @Column
    private Boolean enableShortcutKey;

    /** 获取redis键加载上限 */
    public int getKeyLoadLimit() {
        return this.keyLoadLimit == null ? 1000 : this.keyLoadLimit;
    }

    /** 设置redis键加载上限 */
    public void setKeyLoadLimit(int keyLoadLimit) {
        this.keyLoadLimit = keyLoadLimit;
    }

    /** 设置redis行页码限制 */
    public void setRowPageLimit(int rowPageLimit) {
        this.rowPageLimit = rowPageLimit;
    }

    /** 获取redis行页码限制 */
    public int getRowPageLimit() {
        return this.rowPageLimit == null ? 100 : this.rowPageLimit;
    }

    /** 是否连接后收起左侧 */
    public boolean isHiddenLeftAfterConnected() {
        return this.hiddenLeftAfterConnected == null || BooleanUtil.isTrue(this.hiddenLeftAfterConnected);
    }

    /** 设置是否连接后收起左侧 */
    public void setHiddenLeftAfterConnected(boolean hiddenLeftAfterConnected) {
        this.hiddenLeftAfterConnected = hiddenLeftAfterConnected;
    }

    @Override
    public void copy(Object o) {
        super.copy(o);
        if (o instanceof ShellSetting setting) {
            this.x11Path = setting.x11Path;
            //            this.efficiencyMode = setting.efficiencyMode;
            this.connectShowType = setting.connectShowType;
            this.enableShortcutKey = setting.enableShortcutKey;
            this.connectShowMoreInfo = setting.connectShowMoreInfo;
            this.hiddenLeftAfterConnected = setting.hiddenLeftAfterConnected;
            // 终端
            this.termBeep = setting.termBeep;
            this.termType = setting.termType;
            this.termRefreshRate = setting.termRefreshRate;
            this.termCursorStyle = setting.termCursorStyle;
            this.termCursorBlinks = setting.termCursorBlinks;
            this.termMaxLineCount = setting.termMaxLineCount;
            this.termParseHyperlink = setting.termParseHyperlink;
            this.termCopyOnSelected = setting.termCopyOnSelected;
            this.termUseAntialiasing = setting.termUseAntialiasing;
            this.termBackgroundImage = setting.termBackgroundImage;
            // redis
            this.rowPageLimit = setting.rowPageLimit;
            this.keyLoadLimit = setting.keyLoadLimit;
            // zookeeper
            this.loadMode = setting.loadMode;
            this.viewport = setting.viewport;
            //            this.authMode = setting.authMode;
            this.nodeLoadLimit = setting.nodeLoadLimit;
            this.zkContentViewport = setting.zkContentViewport;
        }
    }

    // public boolean isShowHiddenFile() {
    //     return this.showHiddenFile == null || BooleanUtil.isTrue(this.showHiddenFile);
    // }

    /**
     * 获取x11目录
     *
     * @return x11目录
     */
    @Deprecated
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

    /**
     * 获取x11可执行文件
     *
     * @return x11可执行文件
     */
    @Deprecated
    public String[] x11Binary() {
        if (OSUtil.isMacOS()) {
            return new String[]{"startx"};
        }
        if (OSUtil.isWindows()) {
            return new String[]{"vcxsrv.exe", "XWin_MobaX.exe", "XWin.exe"};
        }
        return null;
    }

    /** 获取x11目录 */
    @Deprecated
    public String getX11Path() {
        return x11Path;
    }

    /** 设置x11目录 */
    @Deprecated
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

    /**
     * 获取x11工作目录
     *
     * @return x11工作目录
     */
    @Deprecated
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

    /** 获取终端背景图片 */
    public String getTermBackgroundImage() {
        return termBackgroundImage;
    }

    /** 设置终端背景图片 */
    public void setTermBackgroundImage(String termBackgroundImage) {
        this.termBackgroundImage = termBackgroundImage;
    }

    /** 获取终端类型 */
    public String getTermType() {
        return termType;
    }

    /** 设置终端类型 */
    public void setTermType(String termType) {
        this.termType = termType;
    }

    /** 是否终端蜂鸣声 */
    public boolean isTermBeep() {
        return termBeep == null || termBeep;
    }

    /** 设置是否终端蜂鸣声 */
    public void setTermBeep(boolean termBeep) {
        this.termBeep = termBeep;
    }

    /** 获取终端光标样式 */
    public int getTermCursorStyle() {
        return termCursorStyle;
    }

    /** 设置终端光标样式 */
    public void setTermCursorStyle(int termCursorStyle) {
        this.termCursorStyle = termCursorStyle;
    }

    /** 获取终端光标闪烁 */
    public int getTermCursorBlinks() {
        return termCursorBlinks == null ? 500 : termCursorBlinks;
    }

    /** 设置终端光标闪烁 */
    public void setTermCursorBlinks(Integer termCursorBlinks) {
        this.termCursorBlinks = termCursorBlinks;
    }

    /** 获取终端最大行数 */
    public Integer getTermMaxLineCount() {
        return termMaxLineCount == null ? 5000 : termMaxLineCount;
    }

    /** 设置终端最大行数 */
    public void setTermMaxLineCount(Integer termMaxLineCount) {
        this.termMaxLineCount = termMaxLineCount;
    }

    /** 是否终端选中时复制 */
    public boolean isTermCopyOnSelected() {
        return BooleanUtil.isTrue(this.termCopyOnSelected);
    }

    /** 设置是否终端选中时复制 */
    public void setTermCopyOnSelected(boolean termCopyOnSelected) {
        this.termCopyOnSelected = termCopyOnSelected;
    }

    /** 是否终端鼠标中键粘贴 */
    @Deprecated
    public boolean isTermPasteByMiddle() {
        return this.termPasteByMiddle == null || this.termPasteByMiddle;
    }

    /** 设置是否终端鼠标中键粘贴 */
    @Deprecated
    public void setTermPasteByMiddle(boolean termPasteByMiddle) {
        this.termPasteByMiddle = termPasteByMiddle;
    }

    /** 获取终端刷新率 */
    public Integer getTermRefreshRate() {
        return termRefreshRate == null || termRefreshRate <= 0 ? -1 : termRefreshRate;
    }

    /** 设置终端刷新率 */
    public void setTermRefreshRate(Integer termRefreshRate) {
        this.termRefreshRate = termRefreshRate;
    }

    /** 是否终端使用抗锯齿 */
    public boolean isTermUseAntialiasing() {
        return termUseAntialiasing == null ? Boolean.TRUE : termUseAntialiasing;
    }

    /** 获取终端是否使用抗锯齿 */
    public boolean getTermUseAntialiasing() {
        return termUseAntialiasing;
    }

    /** 设置终端是否使用抗锯齿 */
    public void setTermUseAntialiasing(boolean termUseAntialiasing) {
        this.termUseAntialiasing = termUseAntialiasing;
    }

    //    public boolean isEfficiencyMode() {
    //        return this.efficiencyMode == null || BooleanUtil.isTrue(this.efficiencyMode);
    //    }
    //
    //    public void setEfficiencyMode(boolean efficiencyMode) {
    //        this.efficiencyMode = efficiencyMode;
    //    }

    /** 是否终端解析超链接 */
    public boolean isTermParseHyperlink() {
        return termParseHyperlink == null ? Boolean.TRUE : termParseHyperlink;
    }

    /** 设置是否终端解析超链接 */
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

    /** 是否连接显示类型 */
    public boolean isConnectShowType() {
        return this.connectShowType == null || this.connectShowType;
    }

    /** 设置是否连接显示类型 */
    public void setConnectShowType(Boolean connectShowType) {
        this.connectShowType = connectShowType;
    }

    /** 是否连接显示更多信息 */
    public boolean isConnectShowMoreInfo() {
        return BooleanUtil.isTrue(this.connectShowMoreInfo);
    }

    /** 设置是否连接显示更多信息 */
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
     * zookeeper 内容视图
     * 0|null list
     * 1 tree
     */
    @Column
    private Byte zkContentViewport;

    //    /**
    //     * zookeeper 节点认证
    //     * 0|null 自动认证
    //     * 1 不自动认证
    //     */
    //    @Column
    //    @Deprecated
    //    private Byte authMode;

    /**
     * zookeeper 节点加载限制
     * 0 无限制
     */
    @Column
    private Integer nodeLoadLimit;

    /**
     * 数据库记录加载限制
     */
    @Column
    private Integer recordPageLimit;

    //    /**
    //     * zookeeper 是否自动认证
    //     *
    //     * @return 结果
    //     */
    //    @JSONField(serialize = false, deserialize = false)
    //    public boolean isAutoAuth() {
    //        return this.authMode == null || this.authMode == 0;
    //    }

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
     * zookeeper 是否显示节点名称
     *
     * @return 结果
     */
    @JSONField(serialize = false, deserialize = false)
    public boolean isShowNodeName() {
        return this.viewport != null && this.viewport == 0;
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
        return this.nodeLoadLimit == null ? 1000 : this.nodeLoadLimit;
    }

    /** 获取节点加载模式 */
    public Byte getLoadMode() {
        return loadMode;
    }

    /** 设置节点加载模式 */
    public void setLoadMode(Byte loadMode) {
        this.loadMode = loadMode;
    }

    /** 获取节点视图 */
    public Byte getViewport() {
        return viewport;
    }

    /** 设置节点视图 */
    public void setViewport(Byte viewport) {
        this.viewport = viewport;
    }

    /** 获取zk内容视图 */
    public Byte getZkContentViewportViewport() {
        return zkContentViewport;
    }

    /** 设置zk内容视图 */
    public void setZkContentViewport(Byte zkContentViewport) {
        this.zkContentViewport = zkContentViewport;
    }

    /**
     * zookeeper 是否list视图
     *
     * @return 结果
     */
    @JSONField(serialize = false, deserialize = false)
    public boolean isZkContentListViewport() {
        return zkContentViewport == null || zkContentViewport == 0;
    }

    /**
     * zookeeper 是否tree视图
     *
     * @return 结果
     */
    @JSONField(serialize = false, deserialize = false)
    public boolean isZkContentTreeViewport() {
        return zkContentViewport != null && zkContentViewport == 1;
    }

    //
    //    public Byte getAuthMode() {
    //        return authMode;
    //    }
    //
    //    public void setAuthMode(Byte authMode) {
    //        this.authMode = authMode;
    //    }

    /** 获取节点加载限制 */
    public Integer getNodeLoadLimit() {
        return nodeLoadLimit;
    }

    /** 设置节点加载限制 */
    public void setNodeLoadLimit(Integer nodeLoadLimit) {
        this.nodeLoadLimit = nodeLoadLimit;
    }

    /** 获取同步id */
    public String getSyncId() {
        return syncId;
    }

    /** 设置同步id */
    public void setSyncId(String syncId) {
        this.syncId = syncId;
    }

    /** 获取同步时间 */
    public Long getSyncTime() {
        return syncTime;
    }

    /** 设置同步时间 */
    public void setSyncTime(Long syncTime) {
        this.syncTime = syncTime;
    }

    /** 获取同步类型 */
    public String getSyncType() {
        return syncType;
    }

    /** 设置同步类型 */
    public void setSyncType(String syncType) {
        this.syncType = syncType;
    }

    /** 是否同步密钥 */
    public boolean isSyncKey() {
        return syncKey == null || syncKey;
    }

    /** 设置是否同步密钥 */
    public void setSyncKey(boolean syncKey) {
        this.syncKey = syncKey;
    }

    /** 是否同步分组 */
    public boolean isSyncGroup() {
        return syncGroup == null || syncGroup;
    }

    /** 设置是否同步分组 */
    public void setSyncGroup(boolean syncGroup) {
        this.syncGroup = syncGroup;
    }

    /** 是否同步片段 */
    public boolean isSyncSnippet() {
        return syncSnippet == null || syncSnippet;
    }

    /** 设置是否同步片段 */
    public void setSyncSnippet(boolean syncSnippet) {
        this.syncSnippet = syncSnippet;
    }

    /** 是否同步连接 */
    public boolean isSyncConnect() {
        return syncConnect == null || syncConnect;
    }

    /** 设置是否同步连接 */
    public void setSyncConnect(boolean syncConnect) {
        this.syncConnect = syncConnect;
    }

    /** 获取同步令牌 */
    public String getSyncToken() {
        return syncToken;
    }

    /** 设置同步令牌 */
    public void setSyncToken(String syncToken) {
        this.syncToken = syncToken;
    }

    /**
     * 是否gitee同步类型
     *
     * @return 结果
     */
    @JSONField(serialize = false, deserialize = false)
    public boolean isGiteeType() {
        return syncType == null || StringUtil.equalsIgnoreCase(syncType, "gitee");
    }

    /**
     * 是否github同步类型
     *
     * @return 结果
     */
    @JSONField(serialize = false, deserialize = false)
    public boolean isGithubType() {
        return StringUtil.equalsIgnoreCase(syncType, "github");
    }

    /** 获取数据库记录加载限制 */
    public int getRecordPageLimit() {
        return recordPageLimit == null ? 100 : recordPageLimit;
    }

    /** 设置数据库记录加载限制 */
    public void setRecordPageLimit(int recordPageLimit) {
        this.recordPageLimit = recordPageLimit;
    }

    /** 是否启用快捷键 */
    public boolean isEnableShortcutKey() {
        return this.enableShortcutKey == null || this.enableShortcutKey;
    }

    /** 设置是否启用快捷键 */
    public void setEnableShortcutKey(boolean enableShortcutKey) {
        this.enableShortcutKey = enableShortcutKey;
    }

    /**
     * mongo记录每页限制
     */
    @Column
    private Integer mongoRecordPageLimit;

    /** 设置mongo记录每页限制 */
    public void setMongoRecordPageLimit(int mongoRecordPageLimit) {
        this.mongoRecordPageLimit = mongoRecordPageLimit;
    }

    /** 获取mongo记录每页限制 */
    public int getMongoRecordPageLimit() {
        if (this.mongoRecordPageLimit == null || this.mongoRecordPageLimit <= 0) {
            return 100;
        }
        return this.mongoRecordPageLimit;
    }

    /**
     * 终端背景图片是否失效
     *
     * @return 结果
     */
    @JSONField(serialize = false, deserialize = false)
    public boolean isTermBackgroundImageInvalid() {
        if (StringUtil.startWithAnyIgnoreCase(this.termBackgroundImage, "http", "https")) {
            return false;
        }
        if (FileUtil.exists(this.termBackgroundImage)) {
            return false;
        }
        return true;
    }

    /**
     * 获取终端背景图片url
     *
     * @return 终端背景图片url
     */
    @JSONField(serialize = false, deserialize = false)
    public String getTermBackgroundImageUrl() {
        // 处理图片
        if (StringUtil.isNotBlank(this.termBackgroundImage) && !StringUtil.startWithAnyIgnoreCase(this.termBackgroundImage, "http", "https")) {
            return ResourceUtil.getLocalFileUrl(this.termBackgroundImage);
        }
        return this.termBackgroundImage;
    }
}
