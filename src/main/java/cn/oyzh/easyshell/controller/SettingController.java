package cn.oyzh.easyshell.controller;


import cn.oyzh.common.date.DateHelper;
import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.system.OSUtil;
import cn.oyzh.common.system.RuntimeUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.fx.sync.ShellSyncTypeCombobox;
import cn.oyzh.easyshell.fx.term.ShellTemShellComboBox;
import cn.oyzh.easyshell.fx.term.ShellTermCursorComboBox;
import cn.oyzh.easyshell.fx.term.ShellTermFpsComboBox;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.easyshell.sync.ShellSyncManager;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.easyshell.util.ShellProcessUtil;
import cn.oyzh.easyshell.x11.ShellX11Util;
import cn.oyzh.fx.gui.font.FontFamilyTextField;
import cn.oyzh.fx.gui.setting.SettingLeftItem;
import cn.oyzh.fx.gui.setting.SettingLeftTreeView;
import cn.oyzh.fx.gui.setting.SettingMainPane;
import cn.oyzh.fx.gui.setting.SettingTreeItem;
import cn.oyzh.fx.gui.text.field.ChooseDirTextField;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.text.field.NumberTextField;
import cn.oyzh.fx.gui.text.field.PasswordTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.picker.FXColorPicker;
import cn.oyzh.fx.plus.controls.text.FXSlider;
import cn.oyzh.fx.plus.controls.toggle.FXToggleGroup;
import cn.oyzh.fx.plus.controls.toggle.FXToggleSwitch;
import cn.oyzh.fx.plus.domain.AppSetting;
import cn.oyzh.fx.plus.font.FontManager;
import cn.oyzh.fx.plus.font.FontSizeComboBox;
import cn.oyzh.fx.plus.font.FontWeightComboBox;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;
import cn.oyzh.fx.plus.i18n.LocaleComboBox;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeGroupUtil;
import cn.oyzh.fx.plus.opacity.OpacityManager;
import cn.oyzh.fx.plus.theme.ThemeComboBox;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import cn.oyzh.i18n.I18nManager;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

import java.util.Date;
import java.util.Objects;

/**
 * 应用设置业务
 *
 * @author oyzh
 * @since 2023/06/16
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "setting.fxml"
)
public class SettingController extends StageController {

    /**
     * 主面板
     */
    @FXML
    private SettingMainPane mainPane;

    /**
     * 退出方式
     */
    @FXML
    private FXToggleGroup exitMode;

    // /**
    //  * 退出方式0
    //  */
    // @FXML
    // private RadioButton exitMode0;

    /**
     * 退出方式1
     */
    @FXML
    private RadioButton exitMode1;

    /**
     * 退出方式2
     */
    @FXML
    private RadioButton exitMode2;

    /**
     * 记住页面大小
     */
    @FXML
    private FXCheckBox pageSize;

    // /**
    //  * 记住页面拉伸
    //  */
    // @FXML
    // private FXCheckBox pageResize;

    /**
     * 记住页面位置
     */
    @FXML
    private FXCheckBox pageLocation;

    /**
     * 主题
     */
    @FXML
    private ThemeComboBox theme;

    /**
     * 背景色
     */
    @FXML
    private FXColorPicker bgColor;

    /**
     * 前景色
     */
    @FXML
    private FXColorPicker fgColor;

    /**
     * 强调色
     */
    @FXML
    private FXColorPicker accentColor;

    /**
     * 背景色
     */
    @FXML
    private FXHBox bgColorBox;

    /**
     * 前景色
     */
    @FXML
    private FXHBox fgColorBox;

    /**
     * 强调色
     */
    @FXML
    private FXHBox accentColorBox;

    /**
     * 字体大小
     */
    @FXML
    private FontSizeComboBox fontSize;

    /**
     * 字体粗细
     */
    @FXML
    private FontWeightComboBox fontWeight;

    /**
     * 字体名称
     */
    @FXML
    private FontFamilyTextField fontFamily;

    /**
     * 编辑器字体大小
     */
    @FXML
    private FontSizeComboBox editorFontSize;

    /**
     * 编辑器字体粗细
     */
    @FXML
    private FontWeightComboBox editorFontWeight;

    /**
     * 编辑器字体名称
     */
    @FXML
    private FontFamilyTextField editorFontFamily;

    /**
     * 终端字体大小
     */
    @FXML
    private FontSizeComboBox terminalFontSize;

    /**
     * 终端字体粗细
     */
    @FXML
    private FontWeightComboBox terminalFontWeight;

    /**
     * 终端字体名称
     */
    @FXML
    private FontFamilyTextField terminalFontFamily;

    /**
     * 区域
     */
    @FXML
    private LocaleComboBox locale;

    /**
     * 窗口透明度
     */
    @FXML
    private FXSlider opacity;

    // /**
    //  * 标题栏透明度
    //  */
    // @FXML
    // private FXSlider titleBarOpacity;

    /**
     * x11目录
     */
    @FXML
    private ChooseDirTextField x11Path;

//    /**
//     * ssh效率模式
//     */
//    @FXML
//    private FXToggleSwitch efficiencyMode;

    /**
     * 连接后收起左侧
     */
    @FXML
    private FXToggleSwitch hiddenLeftAfterConnected;

    /**
     * 终端类型-终端
     */
    @FXML
    private ShellTemShellComboBox termType;

    /**
     * 蜂鸣声-终端
     */
    @FXML
    private FXToggleSwitch termBeep;

    /**
     * 最大行数-终端
     */
    @FXML
    private NumberTextField termMaxLineCount;

    /**
     * 选中时复制-终端
     */
    @FXML
    private FXToggleSwitch termCopyOnSelected;

    /**
     * 刷新率-终端
     */
    @FXML
    private ShellTermFpsComboBox termFps;

    /**
     * 光标闪烁-终端
     */
    @FXML
    private ShellTermCursorComboBox termCursorBlinks;

    /**
     * 使用抗锯齿-终端
     */
    @FXML
    private FXToggleSwitch termUseAntialiasing;

    /**
     * 解析超链接-终端
     */
    @FXML
    private FXToggleSwitch termParseHyperlink;

    /**
     * 鼠标中键粘贴-终端
     */
    @FXML
    private FXToggleSwitch termPasteByMiddle;

    /**
     * 键加载限制，redis
     */
    @FXML
    private NumberTextField keyLoadLimit;

    /**
     * 节点加载，zookeeper
     */
    @FXML
    private FXToggleGroup loadMode;

    /**
     * 节点加载方式0，zookeeper
     */
    @FXML
    private RadioButton loadMode0;

    /**
     * 节点加载方式1，zookeeper
     */
    @FXML
    private RadioButton loadMode1;

    /**
     * ZK连接后加载方式2，zookeeper
     */
    @FXML
    private RadioButton loadMode2;

    /**
     * 节点视图，zookeeper
     */
    @FXML
    private FXToggleGroup viewport;

    /**
     * 节点视图0，zookeeper
     */
    @FXML
    private RadioButton viewport0;

    /**
     * 节点视图1，zookeeper
     */
    @FXML
    private RadioButton viewport1;

    ///**
    // * 节点自动认证，zookeeper
    // */
    //@FXML
    // private FXCheckBox authMode;

    /**
     * 节点加载限制，zookeeper
     */
    @FXML
    private NumberTextField nodeLoadLimit;

    /**
     * 配置对象
     */
    private final ShellSetting setting = ShellSettingStore.SETTING;

    /**
     * 配置持久化对象
     */
    private final ShellSettingStore settingStore = ShellSettingStore.INSTANCE;

    /**
     * 同步-类型
     */
    @FXML
    private ShellSyncTypeCombobox syncType;

    /**
     * 同步-token
     */
    @FXML
    private PasswordTextField syncToken;

    /**
     * 同步-id
     */
    @FXML
    private ClearableTextField syncId;

    /**
     * 同步-密钥
     */
    @FXML
    private CheckBox syncKey;

    /**
     * 同步-分组
     */
    @FXML
    private CheckBox syncGroup;

    /**
     * 同步-片段
     */
    @FXML
    private CheckBox syncSnippet;

    /**
     * 同步-连接
     */
    @FXML
    private CheckBox syncConnect;

    /**
     * 同步-更新时间
     */
    @FXML
    private FXLabel syncTime;

    /**
     * 启用快捷键
     */
    @FXML
    private FXToggleSwitch enableShortcutKey;

    @Override
    public void onWindowShowing(WindowEvent event) {
        super.onWindowShowing(event);
        // 应用退出处理
        if (this.setting.getExitMode() != null) {
            switch (this.setting.getExitMode()) {
                // case 0 -> this.exitMode0.setSelected(true);
                case 1 -> this.exitMode1.setSelected(true);
                default -> this.exitMode2.setSelected(true);
            }
        }
        // 记住页面大小处理
        if (this.setting.getRememberPageSize() != null) {
            this.pageSize.setSelected(this.setting.isRememberPageSize());
        }
        // // 记住页面拉伸处理
        // if (this.setting.getRememberPageResize() != null) {
        //     this.pageResize.setSelected(this.setting.isRememberPageResize());
        // }
        // 记住页面位置处理
        if (this.setting.getRememberPageLocation() != null) {
            this.pageLocation.setSelected(this.setting.isRememberPageLocation());
        }
        // 主题相关处理
        this.theme.select(this.setting.getTheme());
        this.fgColor.setColor(StringUtil.emptyToDefault(this.setting.getFgColor(), this.theme.getFgColorHex()));
        this.bgColor.setColor(StringUtil.emptyToDefault(this.setting.getBgColor(), this.theme.getBgColorHex()));
        this.accentColor.setColor(StringUtil.emptyToDefault(this.setting.getAccentColor(), this.theme.getAccentColorHex()));
        // 字体相关处理
        this.fontSize.selectSize(this.setting.getFontSize());
        this.fontFamily.selectItem(this.setting.getFontFamily());
        this.fontWeight.selectWeight(this.setting.getFontWeight());
        this.editorFontSize.selectSize(this.setting.getEditorFontSize());
        this.editorFontFamily.selectItem(this.setting.getEditorFontFamily());
        this.editorFontWeight.selectWeight(this.setting.getEditorFontWeight());
        this.terminalFontSize.selectSize(this.setting.getTerminalFontSize());
        this.terminalFontFamily.selectItem(this.setting.getTerminalFontFamily());
        this.terminalFontWeight.selectWeight(this.setting.getTerminalFontWeight());
        // 区域相关处理
        this.locale.select(this.setting.getLocale());
        // 透明度相关处理
        if (this.setting.getOpacity() != null) {
            this.opacity.setValue(this.setting.getOpacity());
        }
        // if (this.setting.getTitleBarOpacity() != null) {
        //     this.titleBarOpacity.setValue(this.setting.getTitleBarOpacity());
        // }
        // x11目录
        this.x11Path.setText(this.setting.x11Path());
        // 终端设置
        this.termType.select(this.setting.getTermType());
        this.termBeep.setSelected(this.setting.isTermBeep());
        this.termFps.selectFps(this.setting.getTermRefreshRate());
        this.termMaxLineCount.setValue(this.setting.getTermMaxLineCount());
        this.termPasteByMiddle.setSelected(this.setting.isTermPasteByMiddle());
        this.termCopyOnSelected.setSelected(this.setting.isTermCopyOnSelected());
        this.termParseHyperlink.setSelected(this.setting.isTermParseHyperlink());
        this.termUseAntialiasing.setSelected(this.setting.isTermUseAntialiasing());
        this.termCursorBlinks.selectCursorBlinks(this.setting.getTermCursorBlinks());
//        // 效率模式
//        this.efficiencyMode.setSelected(this.setting.isEfficiencyMode());
        // 连接后收起左侧
        this.hiddenLeftAfterConnected.setSelected(this.setting.isHiddenLeftAfterConnected());
        // redis
        this.keyLoadLimit.setValue(this.setting.getKeyLoadLimit());
        // zookeeper
        // 节点加载处理
        if (this.setting.getLoadMode() != null) {
            switch (this.setting.getLoadMode()) {
                case 0 -> this.loadMode0.setSelected(true);
                case 1 -> this.loadMode1.setSelected(true);
                case 2 -> this.loadMode2.setSelected(true);
            }
        }
        // 节点显示处理
        if (this.setting.getViewport() != null) {
            switch (this.setting.getViewport()) {
                case 0 -> this.viewport0.setSelected(true);
                case 1 -> this.viewport1.setSelected(true);
            }
        }
        //// 节点认证处理
        // if (this.setting.getAuthMode() != null) {
        //    this.authMode.setSelected(this.setting.isAutoAuth());
        //}
        // 节点加载限制
        this.nodeLoadLimit.setValue(this.setting.nodeLoadLimit());

        // 同步
        this.syncToken.setValue(this.setting.getSyncToken());
        if (this.setting.isGiteeType()) {
            this.syncType.selectFirst();
        } else if (this.setting.isGithubType()) {
            this.syncType.select(1);
        }
        this.syncId.setText(this.setting.getSyncId());
        this.syncKey.setSelected(this.setting.isSyncKey());
        this.syncGroup.setSelected(this.setting.isSyncGroup());
        this.syncSnippet.setSelected(this.setting.isSyncSnippet());
        this.syncConnect.setSelected(this.setting.isSyncConnect());

        // 启用快捷键
        this.enableShortcutKey.setSelected(this.setting.isEnableShortcutKey());
    }

    /**
     * 保存设置
     */
    @FXML
    private void saveSetting() {
        try {
            String locale = this.locale.name();
            Byte fontSize = this.fontSize.byteValue();
            String fontFamily = this.fontFamily.getText();
            short fontWeight = this.fontWeight.getWeight();
            Byte editorFontSize = this.editorFontSize.byteValue();
            short editorFontWeight = this.editorFontWeight.getWeight();
            String editorFontFamily = this.editorFontFamily.getText();
            Byte terminalFontSize = this.terminalFontSize.byteValue();
            short terminalFontWeight = this.terminalFontWeight.getWeight();
            String terminalFontFamily = this.terminalFontFamily.getText();
            // redis
            int keyLoadLimit = this.keyLoadLimit.getIntValue();
            // zookeeper
            // byte authMode = (byte) (this.authMode.isSelected() ? 0 : 1);
            int nodeLoadLimit = this.nodeLoadLimit.getIntValue();
            byte loadMode = Byte.parseByte(this.loadMode.selectedUserData());
            byte viewport = Byte.parseByte(this.viewport.selectedUserData());
            byte exitMode = Byte.parseByte(this.exitMode.selectedUserData());

            // 提示文字
            String tips = this.checkConfigForRestart(locale);

            // 终端设置
            this.setting.setTermBeep(this.termBeep.isSelected());
            this.setting.setTermRefreshRate(this.termFps.getFps());
            this.setting.setTermType(this.termType.getSelectedItem());
            this.setting.setTermMaxLineCount(this.termMaxLineCount.getIntValue());
            this.setting.setTermPasteByMiddle(this.termPasteByMiddle.isSelected());
            this.setting.setTermCopyOnSelected(this.termCopyOnSelected.isSelected());
            this.setting.setTermParseHyperlink(this.termParseHyperlink.isSelected());
            this.setting.setTermCursorBlinks(this.termCursorBlinks.getCursorBlinks());
            this.setting.setTermUseAntialiasing(this.termUseAntialiasing.isSelected());
            // 字体相关
            this.setting.setFontSize(fontSize);
            this.setting.setFontFamily(fontFamily);
            this.setting.setFontWeight(fontWeight);
            this.setting.setEditorFontSize(editorFontSize);
            this.setting.setEditorFontFamily(editorFontFamily);
            this.setting.setEditorFontWeight(editorFontWeight);
            this.setting.setTerminalFontSize(terminalFontSize);
            this.setting.setTerminalFontFamily(terminalFontFamily);
            this.setting.setTerminalFontWeight(terminalFontWeight);
            // 主题相关
            this.setting.setTheme(this.theme.name());
            this.setting.setBgColor(this.bgColor.getColor());
            this.setting.setFgColor(this.fgColor.getColor());
            this.setting.setAccentColor(this.accentColor.getColor());
            // 区域相关处理
            this.setting.setLocale(locale);
            // x11目录
            this.setting.setX11Path(this.x11Path.getText());
            // 透明度相关处理
            this.setting.setOpacity((float) this.opacity.getValue());
            // this.setting.setTitleBarOpacity((float) this.titleBarOpacity.getValue());
            // 页面设置
            this.setting.setRememberPageSize((byte) (this.pageSize.isSelected() ? 1 : 0));
            // this.setting.setRememberPageResize((byte) (this.pageResize.isSelected() ? 1 : 0));
            this.setting.setRememberPageLocation((byte) (this.pageLocation.isSelected() ? 1 : 0));
            this.setting.setExitMode(exitMode);
            // 其他设置
//            this.setting.setEfficiencyMode(this.efficiencyMode.isSelected());
            this.setting.setEnableShortcutKey(this.enableShortcutKey.isSelected());
            this.setting.setHiddenLeftAfterConnected(this.hiddenLeftAfterConnected.isSelected());
            // redis
            this.setting.setKeyLoadLimit(keyLoadLimit);
            // zookeeper
            this.setting.setLoadMode(loadMode);
            this.setting.setViewport(viewport);
            // this.setting.setAuthMode(authMode);
            this.setting.setNodeLoadLimit(nodeLoadLimit);
            // 同步
            this.applySync();
            // 更新设置
            if (this.settingStore.update(this.setting)) {
                // 关闭窗口
                this.closeWindow();
                // 应用区域配置
                I18nManager.apply(this.setting.getLocale());
                // 应用字体配置
                FontManager.apply(this.setting.fontConfig());
                // 应用主题配置
                ThemeManager.apply(this.setting.themeConfig());
                // 应用透明度配置
                OpacityManager.apply(this.setting.opacityConfig());
                // 提示不为空，说明需要重启，则执行重启
                if (StringUtil.isNotBlank(tips) && MessageBox.confirm(tips)) {
                    ShellProcessUtil.restartApplication();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 检查重启软件配置
     *
     * @param locale 区域
     * @return 结果
     */
    private String checkConfigForRestart(String locale) {
        if (!Objects.equals(this.setting.getLocale(), locale)) {
            return I18nResourceBundle.i18nString("base.restartTip1");
        }
        return "";
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        this.fgColorBox.disableProperty().bind(this.accentColorBox.disabledProperty());
        this.bgColorBox.disableProperty().bind(this.accentColorBox.disabledProperty());
        this.theme.selectedItemChanged((observableValue, number, t1) -> {
            this.accentColorBox.setDisable(this.theme.isSystem());
            this.fgColor.setValue(t1.getForegroundColor());
            this.bgColor.setValue(t1.getBackgroundColor());
            this.accentColor.setValue(t1.getAccentColor());
        });
        if (!this.theme.isSystem()) {
            this.accentColorBox.enable();
        }
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        SettingLeftTreeView treeView = this.mainPane.getLeftTreeView();
        treeView.addItem(SettingLeftItem.of(I18nHelper.base(), "ssh_box"));
        treeView.addItem(SettingLeftItem.of(I18nHelper.terminal(), "term_box"));
        treeView.addItem(SettingLeftItem.of(I18nHelper.window(), "window_box"));
        treeView.addItem(SettingLeftItem.of(I18nHelper.shortcutKey(), "shortcut_box"));

        // 同步
        treeView.addItem(SettingLeftItem.of(I18nHelper.sync(), "sync_box"));

        // redis
        treeView.addItem(SettingLeftItem.of(I18nHelper.redis(), "redis_box"));
        // zookeeper
        treeView.addItem(SettingLeftItem.of(I18nHelper.zk(), "zk_box"));

        SettingTreeItem fontItem = treeView.addItem(SettingLeftItem.of(I18nHelper.font(), "font_box"));
        fontItem.addItem(SettingLeftItem.of(I18nHelper.general(), "font_general_box"));
        fontItem.addItem(SettingLeftItem.of(I18nHelper.editor(), "font_editor_box"));
        fontItem.addItem(SettingLeftItem.of(I18nHelper.terminal(), "font_terminal_box"));

        treeView.addItem(SettingLeftItem.of(I18nHelper.theme(), "theme_box"));
        treeView.addItem(SettingLeftItem.of(I18nHelper.locale(), "locale_box"));
        treeView.selectItem("ssh_box");
        // linux隐藏x11
        if (OSUtil.isLinux()) {
            NodeGroupUtil.disappear(this.getStage(), "x11");
        }

        // 更新同步信息
        this.updateSyncInfo();
        super.onWindowShown(event);
        this.stage.hideOnEscape();
    }

    /**
     * 重置前景色
     */
    @FXML
    private void resetFgColor() {
        this.fgColor.setValue(this.theme.getValue().getForegroundColor());
    }

    /**
     * 重置背景色
     */
    @FXML
    private void resetBgColor() {
        this.bgColor.setValue(this.theme.getValue().getBackgroundColor());
    }

    /**
     * 重置强调色
     */
    @FXML
    private void resetAccentColor() {
        this.accentColor.setValue(this.theme.getValue().getAccentColor());
    }

    /**
     * 重置区域
     */
    @FXML
    private void resetLocale() {
        this.locale.select((String) null);
    }

    /**
     * 重置透明度
     */
    @FXML
    private void resetOpacity() {
        this.opacity.setValue(OpacityManager.defaultOpacity * 100);
    }

    // /**
    //  * 重置标题栏透明度
    //  */
    // @FXML
    // private void resetTitleBarOpacity() {
    //     this.titleBarOpacity.setValue(OpacityManager.defaultOpacity * 100);
    // }

    @Override
    public String getViewTitle() {
        return I18nHelper.settingTitle();
    }

    /**
     * 重置字体名称
     */
    @FXML
    private void resetFontFamily() {
        this.fontFamily.selectItem(AppSetting.defaultFontFamily());
    }

    /**
     * 重置字体大小
     */
    @FXML
    private void resetFontSize() {
        this.fontSize.selectSize(AppSetting.defaultFontSize());
    }

    /**
     * 重置字体粗细
     */
    @FXML
    private void resetFontWeight() {
        this.fontWeight.selectWeight(AppSetting.defaultFontWeight());
    }

    @FXML
    private void resetEditorFontFamily() {
        this.editorFontFamily.selectItem(AppSetting.defaultEditorFontFamily());
    }

    @FXML
    private void resetEditorFontSize() {
        this.editorFontSize.selectSize(AppSetting.defaultEditorFontSize());
    }

    @FXML
    private void resetEditorFontWeight() {
        this.editorFontWeight.selectWeight(AppSetting.defaultEditorFontWeight());
    }

    @FXML
    private void resetTerminalFontFamily() {
        this.terminalFontFamily.selectItem(AppSetting.defaultTerminalFontFamily());
    }

    @FXML
    private void resetTerminalFontSize() {
        this.terminalFontSize.selectSize(AppSetting.defaultTerminalFontSize());
    }

    @FXML
    private void resetTerminalFontWeight() {
        this.terminalFontWeight.selectWeight(AppSetting.defaultTerminalFontWeight());
    }

//    @FXML
//    private void chooseX11Path() {
//        File dir = null;
//        if (OSUtil.isWindows()) {
//            String initDir;
//            if (FileUtil.exist("C:/Program Files/VcXsrv")) {
//                initDir = "C:/Program Files/VcXsrv";
//            } else {
//                initDir = FXChooser.HOME_DIR.getPath();
//            }
//            dir = DirChooserHelper.choose(I18nHelper.pleaseSelectDirectory(), initDir, null);
//
//        } else if (OSUtil.isMacOS()) {
//            String initDir;
//            if (FileUtil.exist("/opt/X11")) {
//                initDir = "/opt/X11";
//            } else {
//                initDir = FXChooser.HOME_DIR.getPath();
//            }
//            dir = DirChooserHelper.choose(I18nHelper.pleaseSelectDirectory(), initDir, null);
//        }
//        if (dir != null && dir.isDirectory() && dir.exists()) {
//            this.x11Path.setText(dir.getPath());
//        }
//    }

    @FXML
    private void testBashPath() {
        String bash = this.termType.getSelectedItem();
        if (OSUtil.isWindows()) {
            String result;
            // git-bash
            if (bash.contains("git-bash")) {
                String filePath = "C:\\Program Files\\Git\\bin\\bash.exe";
                result = FileUtil.exist(filePath) ? "find" : null;
            } else if (bash.contains("git-sh")) {
                String filePath = "C:\\Program Files\\Git\\bin\\sh.exe";
                result = FileUtil.exist(filePath) ? "find" : null;
            } else {// cmd、powershell
                result = RuntimeUtil.execForStr("where " + bash);
            }
            if (StringUtil.isNotBlank(result)) {
                MessageBox.info(I18nHelper.testSuccess());
            } else {
                MessageBox.warn(I18nHelper.testFailed());
            }
        } else {
            String result = RuntimeUtil.execForStr("which " + bash);
            if (StringUtil.isNotBlank(result) && !StringUtil.containsAnyIgnoreCase(result, "not found")) {
                MessageBox.info(I18nHelper.testSuccess());
            } else {
                MessageBox.warn(I18nHelper.testFailed());
            }
        }
    }

    @FXML
    private void testX11Path() {
        String dir = this.x11Path.getText();
        // 寻找存在的二进制命令
        String bin;
        if (OSUtil.isWindows()) {
            // 寻找存在的二进制命令
            bin = ShellX11Util.findExist(dir, setting.x11Binary());
        } else {
            bin = ShellX11Util.findExist(dir, "/bin/", setting.x11Binary());
        }
        if (bin != null) {
            MessageBox.info(I18nHelper.testSuccess());
        } else {
            MessageBox.warn(I18nHelper.testFailed());
        }
    }

    /**
     * 执行更新
     */
    @FXML
    private void doSync() {
        // if (this.syncId.isEmpty()) {
        //     this.syncId.requestFocus();
        //     MessageBox.warn(I18nHelper.pleaseInputContent());
        //     return;
        // }
        if (this.syncToken.isEmpty()) {
            this.syncToken.requestFocus();
            MessageBox.warn(I18nHelper.pleaseInputContent());
            return;
        }
        this.applySync();
        this.settingStore.replace(this.setting);
        StageManager.showMask(() -> {
            try {
                ShellSyncManager.doSync();
                this.updateSyncInfo();
            } catch (Exception ex) {
                JulLog.warn("do sync error", ex);
                MessageBox.exception(ex);
            }
        });
    }

    /**
     * 清除更新
     */
    @FXML
    private void clearSync() {
        if (!MessageBox.confirm(I18nHelper.clearSyncData())) {
            return;
        }
        StageManager.showMask(() -> {
            try {
                ShellSyncManager.clearSync();
                this.updateSyncInfo();
            } catch (Exception ex) {
                JulLog.warn("clear sync error", ex);
                MessageBox.exception(ex);
            }
        });
    }

    /**
     * 更新同步信息
     */
    private void updateSyncInfo() {
        Long time = this.setting.getSyncTime();
        if (time == null) {
            this.syncTime.clear();
        } else {
            this.syncTime.text(ShellI18nHelper.settingTip1() + " : " + DateHelper.formatDateTimeSimple(new Date(time)));
        }
        String syncId = this.setting.getSyncId();
        if (StringUtil.isEmpty(syncId)) {
            this.syncId.clear();
        } else {
            this.syncId.text(syncId);
        }
    }

    /**
     * 应用同步设置
     */
    private void applySync() {
        // 同步
        this.setting.setSyncId(this.syncId.getTextTrim());
        this.setting.setSyncToken(this.syncToken.getPassword());
        this.setting.setSyncType(this.syncType.getSelectedItem());
        this.setting.setSyncKey(this.syncKey.isSelected());
        this.setting.setSyncGroup(this.syncGroup.isSelected());
        this.setting.setSyncSnippet(this.syncSnippet.isSelected());
        this.setting.setSyncConnect(this.syncConnect.isSelected());
    }

    @Override
    public void onStageInitialize(StageAdapter stage) {
        super.onStageInitialize(stage);
        String initDir = null;
        if (OSUtil.isWindows()) {
            if (FileUtil.exist("C:/Program Files/VcXsrv")) {
                initDir = "C:/Program Files/VcXsrv";
            }
        } else if (OSUtil.isMacOS()) {
            if (FileUtil.exist("/opt/X11")) {
                initDir = "/opt/X11";
            }
        }
        this.x11Path.setInitDir(initDir);
    }
}
