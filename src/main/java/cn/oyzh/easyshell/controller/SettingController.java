package cn.oyzh.easyshell.controller;


import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.system.OSUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.SSHSetting;
import cn.oyzh.easyshell.fx.SSHTerminalTypeComboBox;
import cn.oyzh.easyshell.store.SSHSettingStore;
import cn.oyzh.easyshell.util.SSHProcessUtil;
import cn.oyzh.easyshell.x11.X11Util;
import cn.oyzh.fx.gui.setting.SettingLeftItem;
import cn.oyzh.fx.gui.setting.SettingLeftTreeView;
import cn.oyzh.fx.gui.setting.SettingMainPane;
import cn.oyzh.fx.gui.setting.SettingTreeItem;
import cn.oyzh.fx.gui.text.field.ReadOnlyTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.chooser.DirChooserHelper;
import cn.oyzh.fx.plus.chooser.FXChooser;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.controls.picker.FXColorPicker;
import cn.oyzh.fx.plus.controls.text.FXSlider;
import cn.oyzh.fx.plus.controls.toggle.FXToggleGroup;
import cn.oyzh.fx.plus.domain.AppSetting;
import cn.oyzh.fx.plus.font.FontFamilyComboBox;
import cn.oyzh.fx.plus.font.FontManager;
import cn.oyzh.fx.plus.font.FontSizeComboBox;
import cn.oyzh.fx.plus.font.FontWeightComboBox;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;
import cn.oyzh.fx.plus.i18n.LocaleComboBox;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.opacity.OpacityManager;
import cn.oyzh.fx.plus.theme.ThemeComboBox;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import cn.oyzh.i18n.I18nManager;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

import java.io.File;
import java.util.Objects;

/**
 * 应用设置业务
 *
 * @author oyzh
 * @since 2023/06/16
 */
@StageAttribute(
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "setting.fxml"
)
public class SettingController extends StageController {

    /**
     * 主面板
     */
    @FXML
    private SettingMainPane root;

    /**
     * 退出方式
     */
    @FXML
    private FXToggleGroup exitMode;

    /**
     * 退出方式0
     */
    @FXML
    private RadioButton exitMode0;

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

    /**
     * 记住页面拉伸
     */
    @FXML
    private FXCheckBox pageResize;

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
    private FontFamilyComboBox fontFamily;

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
    private FontFamilyComboBox editorFontFamily;

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
    private FontFamilyComboBox terminalFontFamily;

    /**
     * 查询字体大小
     */
    @FXML
    private FontSizeComboBox queryFontSize;

    /**
     * 查询字体粗细
     */
    @FXML
    private FontWeightComboBox queryFontWeight;

    /**
     * 查询字体名称
     */
    @FXML
    private FontFamilyComboBox queryFontFamily;

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

    /**
     * 标题栏透明度
     */
    @FXML
    private FXSlider titleBarOpacity;

    /**
     * x11目录
     */
    @FXML
    private ReadOnlyTextField x11Path;

    /**
     * 终端类型
     */
    @FXML
    private SSHTerminalTypeComboBox terminalType;

    /**
     * 配置对象
     */
    private final SSHSetting setting = SSHSettingStore.SETTING;

    /**
     * 配置持久化对象
     */
    private final SSHSettingStore settingStore = SSHSettingStore.INSTANCE;

    @Override
    public void onWindowShowing(WindowEvent event) {
        super.onWindowShowing(event);
        // 应用退出处理
        if (this.setting.getExitMode() != null) {
            switch (this.setting.getExitMode()) {
                case 0 -> this.exitMode0.setSelected(true);
                case 1 -> this.exitMode1.setSelected(true);
                case 2 -> this.exitMode2.setSelected(true);
            }
        }
        // 记住页面大小处理
        if (this.setting.getRememberPageSize() != null) {
            this.pageSize.setSelected(this.setting.isRememberPageSize());
        }
        // 记住页面拉伸处理
        if (this.setting.getRememberPageResize() != null) {
            this.pageResize.setSelected(this.setting.isRememberPageResize());
        }
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
        this.fontFamily.select(this.setting.getFontFamily());
        this.fontWeight.selectWeight(this.setting.getFontWeight());
        this.editorFontSize.selectSize(this.setting.getEditorFontSize());
        this.editorFontFamily.select(this.setting.getEditorFontFamily());
        this.editorFontWeight.selectWeight(this.setting.getEditorFontWeight());
        this.terminalFontSize.selectSize(this.setting.getTerminalFontSize());
        this.terminalFontFamily.select(this.setting.getTerminalFontFamily());
        this.terminalFontWeight.selectWeight(this.setting.getTerminalFontWeight());
        this.queryFontSize.selectSize(this.setting.getQueryFontSize());
        this.queryFontFamily.select(this.setting.getQueryFontFamily());
        this.queryFontWeight.selectWeight(this.setting.getQueryFontWeight());
        // 区域相关处理
        this.locale.select(this.setting.getLocale());
        // 透明度相关处理
        if (this.setting.getOpacity() != null) {
            this.opacity.setValue(this.setting.getOpacity());
        }
        if (this.setting.getTitleBarOpacity() != null) {
            this.titleBarOpacity.setValue(this.setting.getTitleBarOpacity());
        }
        // x11目录
        this.x11Path.setText(this.setting.x11Path());
        // 终端类型
        if (StringUtil.isBlank(this.setting.getTerminalType())) {
            this.terminalType.selectFirst();
        } else {
            this.terminalType.select(this.setting.getTerminalType());
        }
    }

    /**
     * 保存设置
     */
    @FXML
    private void saveSetting() {
        try {
            String locale = this.locale.name();
            Byte fontSize = this.fontSize.byteValue();
            short fontWeight = this.fontWeight.getWeight();
            String fontFamily = this.fontFamily.getValue();
            Byte editorFontSize = this.editorFontSize.byteValue();
            short editorFontWeight = this.editorFontWeight.getWeight();
            String editorFontFamily = this.editorFontFamily.getValue();
            Byte terminalFontSize = this.terminalFontSize.byteValue();
            short terminalFontWeight = this.terminalFontWeight.getWeight();
            String terminalFontFamily = this.terminalFontFamily.getValue();
            Byte queryFontSize = this.queryFontSize.byteValue();
            short queryFontWeight = this.queryFontWeight.getWeight();
            String queryFontFamily = this.queryFontFamily.getValue();

            // 提示文字
            String tips = this.checkConfigForRestart(locale);

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
            this.setting.setQueryFontSize(queryFontSize);
            this.setting.setQueryFontFamily(queryFontFamily);
            this.setting.setQueryFontWeight(queryFontWeight);
            // 主题相关
            this.setting.setTheme(this.theme.name());
            this.setting.setBgColor(this.bgColor.getColor());
            this.setting.setFgColor(this.fgColor.getColor());
            this.setting.setAccentColor(this.accentColor.getColor());
            // 区域相关处理
            this.setting.setLocale(locale);
            // x11目录
            this.setting.setX11Path(this.x11Path.getText());
            // 终端类型
            this.setting.setTerminalType(this.terminalType.getSelectedItem());
            // 透明度相关处理
            this.setting.setOpacity((float) this.opacity.getValue());
            this.setting.setTitleBarOpacity((float) this.titleBarOpacity.getValue());
            // 页面设置
            this.setting.setRememberPageSize((byte) (this.pageSize.isSelected() ? 1 : 0));
            this.setting.setRememberPageResize((byte) (this.pageResize.isSelected() ? 1 : 0));
            this.setting.setRememberPageLocation((byte) (this.pageLocation.isSelected() ? 1 : 0));
            this.setting.setExitMode((byte) Integer.parseInt(this.exitMode.selectedUserData()));
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
                    SSHProcessUtil.restartApplication();
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
        SettingLeftTreeView treeView = this.root.getLeftTreeView();
        treeView.addItem(SettingLeftItem.of(I18nHelper.base(), "ssh_box"));
        treeView.addItem(SettingLeftItem.of(I18nHelper.window(), "window_box"));
        SettingTreeItem fontItem = treeView.addItem(SettingLeftItem.of(I18nHelper.font(), "font"));
        fontItem.addItem(SettingLeftItem.of(I18nHelper.general(), "font_general_box"));
        fontItem.addItem(SettingLeftItem.of(I18nHelper.editor(), "font_editor_box"));
        fontItem.addItem(SettingLeftItem.of(I18nHelper.terminal(), "font_terminal_box"));
        fontItem.addItem(SettingLeftItem.of(I18nHelper.query(), "font_query_box"));
        treeView.addItem(SettingLeftItem.of(I18nHelper.theme(), "theme_box"));
        treeView.addItem(SettingLeftItem.of(I18nHelper.locale(), "locale_box"));
        treeView.selectItem("ssh_box");
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

    /**
     * 重置标题栏透明度
     */
    @FXML
    private void resetTitleBarOpacity() {
        this.titleBarOpacity.setValue(OpacityManager.defaultOpacity * 100);
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.settingTitle();
    }

    /**
     * 重置字体名称
     */
    @FXML
    private void resetFontFamily() {
        this.fontFamily.select(AppSetting.defaultFontFamily());
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
        this.editorFontFamily.select(AppSetting.defaultEditorFontFamily());
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
        this.terminalFontFamily.select(AppSetting.defaultTerminalFontFamily());
    }

    @FXML
    private void resetTerminalFontSize() {
        this.terminalFontSize.selectSize(AppSetting.defaultTerminalFontSize());
    }

    @FXML
    private void resetTerminalFontWeight() {
        this.terminalFontWeight.selectWeight(AppSetting.defaultTerminalFontWeight());
    }

    @FXML
    private void resetQueryFontFamily() {
        this.queryFontFamily.select(AppSetting.defaultQueryFontFamily());
    }

    @FXML
    private void resetQueryFontSize() {
        this.queryFontSize.selectSize(AppSetting.defaultQueryFontSize());
    }

    @FXML
    private void resetQueryFontWeight() {
        this.queryFontWeight.selectWeight(AppSetting.defaultQueryFontWeight());
    }

    @FXML
    private void chooseX11Path() {
        File dir = null;
        if (OSUtil.isWindows()) {
            String initDir;
            if (FileUtil.exist("C:/Program Files/VcXsrv")) {
                initDir = "C:/Program Files/VcXsrv";
            } else {
                initDir = FXChooser.DESKTOP_DIR.getPath();
            }
            dir = DirChooserHelper.choose(I18nHelper.pleaseSelectDirectory(), initDir, null);

        } else if (OSUtil.isMacOS()) {
            String initDir;
            if (FileUtil.exist("/opt/X11")) {
                initDir = "/opt/X11";
            } else {
                initDir = FXChooser.DESKTOP_DIR.getPath();
            }
            dir = DirChooserHelper.choose(I18nHelper.pleaseSelectDirectory(), initDir, null);
        }
        if (dir != null && dir.isDirectory() && dir.exists()) {
            this.x11Path.setText(dir.getPath());
        }
    }

    @FXML
    private void testX11Path() {
        String dir = this.x11Path.getText();
        // 寻找存在的二进制命令
        String bin;
        if (OSUtil.isWindows()) {
            // 寻找存在的二进制命令
            bin = X11Util.findExist(dir, setting.x11Binary());
        } else {
            bin = X11Util.findExist(dir, "/bin/", setting.x11Binary());
        }
        if (bin != null) {
            MessageBox.okToast(I18nHelper.testSuccess());
        } else {
            MessageBox.warnToast(I18nHelper.testFailed());
        }
    }
}
