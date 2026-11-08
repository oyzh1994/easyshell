package cn.oyzh.easyshell.tabs.http.settings;

import cn.oyzh.easyshell.domain.http.ShellHttpEnvironment;
import cn.oyzh.easyshell.domain.http.ShellHttpSetting;
import cn.oyzh.easyshell.http.ShellHttpWorkspace;
import cn.oyzh.easyshell.store.http.ShellHttpWorkspaceStore;
import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.easyshell.tabs.http.ShellHttpTabPane;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.keyboard.KeyHandler;
import cn.oyzh.fx.plus.keyboard.KeyListener;
import cn.oyzh.fx.gui.text.field.NumberTextField;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * HTTP 设置标签页控制器。
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellHttpSettingsTabController extends RichTabController {

    /**
     * 当前环境
     */
    @FXML
    private FXComboBox<ShellHttpEnvironment> environmentCombo;

    /**
     * 默认超时时间
     */
    @FXML
    private NumberTextField timeout;

    /**
     * HTTP 版本
     */
    @FXML
    private FXComboBox<String> httpVersion;

    /**
     * 跟随重定向
     */
    @FXML
    private FXCheckBox followRedirects;

    /**
     * 校验 HTTPS 证书
     */
    @FXML
    private FXCheckBox verifySsl;

    /**
     * 美化 JSON
     */
    @FXML
    private FXCheckBox prettyJson;

    /**
     * 工作区
     */
    private ShellHttpWorkspace workspace;

    /**
     * 标签页面板
     */
    private ShellHttpTabPane tabPane;

    /**
     * 无环境占位
     */
    private ShellHttpEnvironment noEnvironment;

    /**
     * 是否正在加载
     */
    private boolean loading;

    /**
     * 延迟保存任务
     */
    private Future<?> saveFuture;

    /**
     * 是否正在立即保存
     */
    private boolean flushing;

    /**
     * 初始化
     *
     * @param workspace 工作区
     * @param tabPane   标签页面板
     */
    public void init(ShellHttpWorkspace workspace, ShellHttpTabPane tabPane) {
        this.workspace = workspace;
        this.tabPane = tabPane;
        this.loading = true;
        ShellHttpSetting settings = workspace.getSettings();
        this.noEnvironment = new ShellHttpEnvironment();
        this.noEnvironment.setId(null);
        this.refreshEnvironments();
        this.timeout.setValue(settings.getTimeoutSeconds());
        this.httpVersion.getItems().setAll(
                ShellHttpSetting.VERSION_HTTP_1_1, ShellHttpSetting.VERSION_HTTP_2);
        this.httpVersion.setValue(settings.getHttpVersion());
        this.followRedirects.setSelected(settings.isFollowRedirects());
        this.verifySsl.setSelected(settings.isVerifySsl());
        this.prettyJson.setSelected(settings.isPrettyJson());
        this.loading = false;

        this.environmentCombo.selectedItemChanged((observable, oldValue, newValue) -> this.switchEnvironment(newValue));
        this.timeout.textProperty().addListener((observable, oldValue, newValue) -> this.apply());
        this.httpVersion.selectedItemChanged((observable, oldValue, newValue) -> this.apply());
        this.followRedirects.selectedProperty().addListener((observable, oldValue, newValue) -> this.apply());
        this.verifySsl.selectedProperty().addListener((observable, oldValue, newValue) -> this.apply());
        this.prettyJson.selectedProperty().addListener((observable, oldValue, newValue) -> this.apply());
        this.bindSaveKey();
    }

    /**
     * 刷新环境列表。
     */
    public void refreshEnvironments() {
        if (this.workspace == null || this.environmentCombo == null) {
            return;
        }
        boolean oldLoading = this.loading;
        this.loading = true;
        try {
            if (this.noEnvironment == null) {
                this.noEnvironment = new ShellHttpEnvironment();
                this.noEnvironment.setId(null);
            }
            this.noEnvironment.setName(ShellI18nHelper.httpNoEnvironment());
            List<ShellHttpEnvironment> items = new ArrayList<>();
            items.add(this.noEnvironment);
            items.addAll(this.workspace.getEnvironments());
            this.environmentCombo.getItems().setAll(items);
            ShellHttpEnvironment active = this.workspace.activeEnvironment();
            this.environmentCombo.setValue(active == null ? this.noEnvironment : active);
        } finally {
            this.loading = oldLoading;
        }
    }

    /**
     * 切换当前环境。
     *
     * @param environment 环境
     */
    private void switchEnvironment(ShellHttpEnvironment environment) {
        if (this.loading || this.workspace == null) {
            return;
        }
        this.workspace.setActiveEnvironmentId(
                environment == null || environment.getId() == null ? null : environment.getId());
        if (this.saveFuture != null) {
            this.saveFuture.cancel(false);
            this.saveFuture = null;
        }
        ShellHttpWorkspaceStore.INSTANCE.saveSetting(this.workspace);
        if (this.tabPane.tabController() != null) {
            this.tabPane.tabController().refreshEnvironmentState();
        }
    }

    /**
     * 绑定保存快捷键
     */
    private void bindSaveKey() {
        KeyHandler saveHandler = new KeyHandler();
        saveHandler.setHandler(e -> this.save());
        saveHandler.setKeyCode(KeyCode.S);
        saveHandler.setMainModifierDown(true);
        saveHandler.setKeyType(KeyEvent.KEY_RELEASED);
        KeyListener.addHandler(this.getTabContent(), saveHandler);
    }

    /**
     * 应用设置
     */
    private void apply() {
        if (this.loading || this.workspace == null) {
            return;
        }
        ShellHttpSetting settings = this.workspace.getSettings();
        int value = this.timeout.getIntValue();
        settings.setTimeoutSeconds(value <= 0 ? 30 : value);
        settings.setHttpVersion(this.httpVersion.getValue());
        settings.setFollowRedirects(this.followRedirects.isSelected());
        settings.setVerifySsl(this.verifySsl.isSelected());
        settings.setPrettyJson(this.prettyJson.isSelected());
        if (!this.flushing) {
            this.schedulePersist();
        }
        this.tabPane.refreshHomeTab();
    }

    /**
     * 保存
     */
    @FXML
    private void save() {
        this.flush();
        MessageBox.okToast(I18nHelper.operationSuccess(), this.getWindow());
    }

    /**
     * 刷新数据
     */
    public void flush() {
        if (this.saveFuture != null) {
            this.saveFuture.cancel(false);
            this.saveFuture = null;
        }
        this.flushing = true;
        try {
            this.apply();
        } finally {
            this.flushing = false;
        }
        ShellHttpWorkspaceStore.INSTANCE.saveSetting(this.workspace);
    }

    /**
     * 延迟保存设置。
     */
    private void schedulePersist() {
        if (this.loading || this.workspace == null) {
            return;
        }
        if (this.saveFuture != null) {
            this.saveFuture.cancel(false);
        }
        this.saveFuture = TaskManager.startDelay(() -> FXUtil.runLater(
                () -> ShellHttpWorkspaceStore.INSTANCE.saveSetting(this.workspace)), 300);
    }
}
