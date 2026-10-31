package cn.oyzh.easyshell.fx.llm;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.llm.LLMMessage;
import cn.oyzh.easyshell.llm.LLMProvider;
import cn.oyzh.easyshell.llm.LLMSendHandler;
import cn.oyzh.easyshell.llm.LLMSessionTab;
import cn.oyzh.easyshell.llm.LLMThinkingLevel;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.easyshell.store.llm.LLMProviderStore;
import cn.oyzh.fx.gui.svg.glyph.SettingSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.FileSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.llm.SendSVGGlyph;
import cn.oyzh.fx.plus.controls.FXProgressBar;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.button.FXButton;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.pane.FXBorderPane;
import cn.oyzh.fx.plus.controls.text.area.FXTextArea;
import cn.oyzh.i18n.I18nResourceBundle;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.i18n.I18nHelper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.BooleanSupplier;

public class LLMView extends FXBorderPane {

    private static final double SELECTOR_LABEL_WIDTH = 44;

    private static final double SELECTOR_WIDTH = 120;

    private final LLMListView messageListView = new LLMListView();

    private final FXTextArea inputArea = new FXTextArea();

    private final LLMProviderComboBox providerComboBox = new LLMProviderComboBox();

    private final FXComboBox<String> modelComboBox = new FXComboBox<>();

    private final LLMThinkingLevelComboBox thinkingLevelComboBox = new LLMThinkingLevelComboBox();

    private final LLMSessionTabComboBox sessionTabComboBox = new LLMSessionTabComboBox();

    private final FXButton sendButton = new FXButton();

    private final FXButton attachButton = new FXButton();

    private final FXButton settingsButton = new FXButton();

    private final FXProgressBar contextProgress = new FXProgressBar();

    private final FXLabel contextLabel = new FXLabel("上下文 0%");

    private final FXLabel attachmentLabel = new FXLabel();

    private final FXHBox attachmentBar = new FXHBox();

    private final FXButton attachmentRemoveButton = new FXButton("×");

    private final ShellSetting setting = ShellSettingStore.SETTING;

    private final ShellSettingStore settingStore = ShellSettingStore.INSTANCE;

    private LLMSendHandler sendHandler;

    private BooleanSupplier sendValidator;

    private File selectedFile;

    private boolean busy;

    public LLMView() {
        this.getStyleClass().add("llm-view");
        this.setCenter(this.messageListView);
        this.setBottom(this.buildComposer());
        this.loadStyle();
        this.initSelectors();
        this.updateSendState();
        FXUtil.runLater(this.inputArea::requestFocus);
    }

    private FXVBox buildComposer() {
        FXVBox composer = new FXVBox();
        composer.getStyleClass().add("llm-composer");
        composer.setSpacing(8);
        composer.setPadding(new Insets(10, 14, 12, 14));

        this.attachmentBar.getStyleClass().add("llm-attachment-bar");
        this.attachmentBar.setAlignment(Pos.CENTER_LEFT);
        this.attachmentBar.setSpacing(6);
        this.attachmentLabel.getStyleClass().add("llm-attachment-chip");
        this.attachmentLabel.setTextOverrun(OverrunStyle.CENTER_ELLIPSIS);
        this.attachmentLabel.setMaxWidth(360);
        this.attachmentLabel.setVisible(false);
        this.attachmentLabel.setManaged(false);
        this.attachmentRemoveButton.getStyleClass().add("llm-attachment-remove");
        this.attachmentRemoveButton.setVisible(false);
        this.attachmentRemoveButton.setManaged(false);
        this.attachmentRemoveButton.setOnAction(event -> this.setSelectedFile(null));

        Region attachmentSpacer = new Region();
        FXHBox.setHgrow(attachmentSpacer, Priority.ALWAYS);
        this.attachmentBar.addChild(
                this.labeledCombo(I18nResourceBundle.i18nString(I18nHelper.SESSION), this.sessionTabComboBox),
                this.buildAttachButton(),
                this.attachmentLabel,
                this.attachmentRemoveButton,
                attachmentSpacer
        );
        this.attachmentBar.setVisible(true);
        this.attachmentBar.setManaged(true);
        composer.getChildren().add(this.attachmentBar);

        this.inputArea.getStyleClass().add("llm-input");
        this.inputArea.setPromptText("输入消息，Enter 发送，Shift+Enter 换行");
        this.inputArea.setPrefRowCount(3);
        this.inputArea.setMaxHeight(120);
        this.inputArea.textProperty().addListener((observable, oldValue, newValue) -> this.updateSendState());
        this.inputArea.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && !event.isShiftDown() && !event.isControlDown() && !event.isAltDown()) {
                this.sendMessage();
                event.consume();
            }
        });
        composer.getChildren().add(this.inputArea);

        FXHBox toolbar = new FXHBox();
        toolbar.getStyleClass().add("llm-toolbar");
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setSpacing(8);

        Region spacer = new Region();
        FXHBox.setHgrow(spacer, Priority.ALWAYS);

        toolbar.getChildren().addAll(
                this.labeledCombo(I18nHelper.model(), this.modelComboBox),
                this.buildSettingsButton(),
                spacer,
                this.buildSendButton()
        );
        composer.getChildren().add(toolbar);
        return composer;
    }

    private <T> FXHBox labeledCombo(String labelText, FXComboBox<T> comboBox) {
        FXLabel label = new FXLabel(labelText);
        label.getStyleClass().add("llm-field-label");
        label.setMinWidth(SELECTOR_LABEL_WIDTH);
        label.setPrefWidth(SELECTOR_LABEL_WIDTH);
        comboBox.getStyleClass().add("llm-combo");
        comboBox.setPrefWidth(SELECTOR_WIDTH);
        FXHBox box = new FXHBox(label, comboBox);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setSpacing(3);
        return box;
    }

    private VBox buildContextUsage() {
        VBox box = new VBox(3);
        box.getStyleClass().add("llm-context");
        box.setAlignment(Pos.CENTER_LEFT);
        this.contextLabel.getStyleClass().add("llm-context-label");
        this.contextProgress.getStyleClass().add("llm-context-progress");
        this.contextProgress.setPrefWidth(92);
        this.contextProgress.setPrefHeight(4);
        this.contextProgress.setProgress(0);
        box.getChildren().addAll(this.contextLabel, this.contextProgress);
        return box;
    }

    private FXButton buildAttachButton() {
        FileSVGGlyph glyph = new FileSVGGlyph();
        this.attachButton.setGraphic(glyph);
        this.attachButton.getStyleClass().add("llm-file-button");
        this.attachButton.setTooltip(new Tooltip(I18nHelper.pleaseSelectFile()));
        this.attachButton.setOnAction(event -> this.chooseFile());
        return this.attachButton;
    }

    private FXButton buildSendButton() {
        SendSVGGlyph glyph = new SendSVGGlyph();
        glyph.setColor(Color.WHITE);
        this.sendButton.setGraphic(glyph);
        this.sendButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        this.sendButton.setTooltip(new Tooltip(I18nHelper.send()));
        this.sendButton.getStyleClass().add("llm-send-button");
        this.sendButton.setOnAction(event -> this.sendMessage());
        return this.sendButton;
    }

    private FXButton buildSettingsButton() {
        SettingSVGGlyph glyph = new SettingSVGGlyph();
        this.settingsButton.setGraphic(glyph);
        this.settingsButton.getStyleClass().add("llm-settings-button");
        this.settingsButton.setTooltip(new Tooltip(I18nHelper.setting()));
        this.settingsButton.setOnAction(event -> this.showSettingsMenu());
        return this.settingsButton;
    }

    private void showSettingsMenu() {
        ContextMenu menu = new ContextMenu();
        menu.getItems().addAll(
                this.buildProviderMenu(),
                this.buildThinkingMenu(),
                this.buildStreamingMenu()
        );
        menu.show(this.settingsButton, Side.TOP, 0, -4);
    }

    private Menu buildProviderMenu() {
        LLMProvider selected = this.providerComboBox.getSelectedProvider();
        String selectedName = selected == null ? I18nHelper.none() : selected.getName();
        Menu menu = new Menu(I18nHelper.provider() + ": " + selectedName);
        List<LLMProvider> providers = LLMProviderStore.INSTANCE.selectList();
        if (providers.isEmpty()) {
            MenuItem emptyItem = new MenuItem(I18nHelper.none());
            emptyItem.setDisable(true);
            menu.getItems().add(emptyItem);
            return menu;
        }
        ToggleGroup group = new ToggleGroup();
        for (LLMProvider provider : providers) {
            RadioMenuItem item = new RadioMenuItem(provider.getName());
            item.setToggleGroup(group);
            item.setSelected(provider.isActive());
            item.setOnAction(event -> {
                LLMProviderStore.INSTANCE.activate(provider);
                this.refreshProviders();
            });
            menu.getItems().add(item);
        }
        return menu;
    }

    private Menu buildThinkingMenu() {
        LLMThinkingLevel selected = this.getSelectedThinkingLevel();
        Menu menu = new Menu(I18nHelper.thinkingIntensity());
        ToggleGroup group = new ToggleGroup();
        for (LLMThinkingLevel level : LLMThinkingLevel.values()) {
            RadioMenuItem item = new RadioMenuItem(LLMThinkingLevelComboBox.getDisplayName(level));
            item.setToggleGroup(group);
            item.setSelected(level == selected);
            item.setOnAction(event -> this.setThinkingLevel(level));
            menu.getItems().add(item);
        }
        return menu;
    }

    private Menu buildStreamingMenu() {
        boolean streaming = this.isStreamingEnabled();
        Menu menu = new Menu(I18nResourceBundle.i18nString("base.streaming"));
        ToggleGroup group = new ToggleGroup();
        RadioMenuItem enabledItem = new RadioMenuItem(I18nHelper.enable());
        enabledItem.setToggleGroup(group);
        enabledItem.setSelected(streaming);
        enabledItem.setOnAction(event -> this.setStreamingEnabled(true));
        RadioMenuItem disabledItem = new RadioMenuItem(I18nHelper.disable());
        disabledItem.setToggleGroup(group);
        disabledItem.setSelected(!streaming);
        disabledItem.setOnAction(event -> this.setStreamingEnabled(false));
        menu.getItems().addAll(enabledItem, disabledItem);
        return menu;
    }

    private void initSelectors() {
        this.providerComboBox.valueProperty().addListener((observable, oldValue, newValue) -> this.updateModels(newValue));
        this.thinkingLevelComboBox.selectLevel(this.getSavedThinkingLevel());
        this.thinkingLevelComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                this.setting.setLlmThinkingLevel(newValue.ordinal());
                this.settingStore.replace(this.setting);
            }
        });
        LLMProvider provider = this.providerComboBox.getSelectedProvider();
        this.syncProviderSetting(provider);
        this.updateModels(provider);
    }

    private LLMThinkingLevel getSavedThinkingLevel() {
        LLMThinkingLevel[] levels = LLMThinkingLevel.values();
        int ordinal = this.setting.getLlmThinkingLevel();
        if (ordinal < 0 || ordinal >= levels.length) {
            return LLMThinkingLevel.MEDIUM;
        }
        return levels[ordinal];
    }

    private void updateModels(LLMProvider provider) {
        List<String> models = provider == null ? List.of() : provider.getModels();
        String selected = this.modelComboBox.getValue();
        this.modelComboBox.getItems().setAll(models);
        if (selected != null && models.contains(selected)) {
            this.modelComboBox.setValue(selected);
        } else if (provider != null && models.contains(provider.getDefaultModel())) {
            this.modelComboBox.getSelectionModel().select(provider.getDefaultModel());
        } else if (!models.isEmpty()) {
            this.modelComboBox.getSelectionModel().selectFirst();
        } else {
            this.modelComboBox.setValue(null);
        }
    }

    private void chooseFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(I18nHelper.pleaseSelectFile());
        Window window = this.getScene() == null ? null : this.getScene().getWindow();
        File file = chooser.showOpenDialog(window);
        if (file != null) {
            this.setSelectedFile(file);
        }
    }

    private void loadStyle() {
        URL style = LLMView.class.getResource("/css/llm/llm-chat.css");
        if (style != null) {
            this.getStylesheets().add(style.toExternalForm());
        }
    }

    @Override
    public void changeTheme(ThemeStyle style) {
        this.getStyleClass().remove("llm-dark");
        if (ThemeManager.isDarkMode()) {
            this.getStyleClass().add("llm-dark");
        }
        super.changeTheme(style);
    }

    public void sendMessage() {
        if (this.busy) {
            return;
        }
        if (this.sendValidator != null && !this.sendValidator.getAsBoolean()) {
            return;
        }
        String text = this.inputArea.getText();
        boolean hasText = text != null && !text.isBlank();
        if (!hasText && this.selectedFile == null) {
            return;
        }
        LLMMessage message = this.messageListView.addUserMessage(hasText ? text.trim() : "", this.selectedFile);
        this.inputArea.clear();
        this.setSelectedFile(null);
        this.updateSendState();
        if (this.sendHandler != null) {
            this.sendHandler.send(message);
        }
    }

    private void updateSendState() {
        String text = this.inputArea.getText();
        boolean hasText = text != null && !text.isBlank();
        this.sendButton.setDisable(this.busy || (!hasText && this.selectedFile == null));
    }

    /**
     * 初始化全量消息，用于会话恢复
     *
     * @param messages 完整消息列表
     */
    public void setMessages(List<LLMMessage> messages) {
        this.messageListView.setMessages(messages);
    }

    /**
     * 获取当前消息快照，用于会话持久化
     *
     * @return 当前消息列表
     */
    public List<LLMMessage> getMessages() {
        return this.messageListView.getMessages();
    }

    public boolean isMessageEmpty() {
        return CollectionUtil.isEmpty(this.getMessages());
    }

    public LLMMessage addAssistantMessage(String text) {
        LLMMessage message = LLMMessage.llm(text);
        message.setModel(this.getSelectedModel());
        this.messageListView.addMessage(message);
        return message;
    }

    public LLMMessage addUserMessage(String text) {
        return this.messageListView.addUserMessage(text);
    }

    public LLMMessage addUserMessage(String text, File file) {
        return this.messageListView.addUserMessage(text, file);
    }

    public LLMMessage addErrorMessage(String text) {
        return this.messageListView.addErrorMessage(text);
    }

    public void appendMessage(LLMMessage message, String text) {
        this.messageListView.appendMessage(message, text);
    }

    /**
     * 覆盖已有消息内容，可用于将思考占位消息替换为真实回复
     *
     * @param message 目标消息
     * @param text    新内容
     */
    public void replaceMessage(LLMMessage message, String text) {
        this.messageListView.replaceMessage(message, text);
    }

    public void clearMessages() {
        this.messageListView.clearMessages();
    }

    public void setContextUsage(long usedTokens, long maxTokens) {
        double ratio = maxTokens <= 0 ? 0 : Math.min(1.0, usedTokens / (double) maxTokens);
        this.setContextUsage(ratio, usedTokens, maxTokens);
    }

    public void setContextUsage(double ratio) {
        this.setContextUsage(ratio, -1, -1);
    }

    private void setContextUsage(double ratio, long usedTokens, long maxTokens) {
        double value = Math.max(0, Math.min(1, ratio));
        FXUtil.runLater(() -> {
            this.contextProgress.setProgress(value);
            this.contextLabel.setText("上下文 " + Math.round(value * 100) + "%");
            this.contextProgress.getStyleClass().removeAll("llm-warning", "llm-danger");
            if (value >= 0.9) {
                this.contextProgress.getStyleClass().add("llm-danger");
            } else if (value >= 0.7) {
                this.contextProgress.getStyleClass().add("llm-warning");
            }
            if (usedTokens >= 0 && maxTokens > 0) {
                this.contextLabel.setTooltip(new Tooltip(formatTokens(usedTokens) + " / " + formatTokens(maxTokens) + " tokens"));
            } else {
                this.contextLabel.setTooltip(null);
            }
        });
    }

    public void setBusy(boolean busy) {
        this.busy = busy;
        this.updateSendState();
    }

    public boolean isBusy() {
        return busy;
    }

    public void setSendHandler(LLMSendHandler sendHandler) {
        this.sendHandler = sendHandler;
    }

    /**
     * 设置发送前校验器
     *
     * @param sendValidator 发送前校验器
     */
    public void setSendValidator(BooleanSupplier sendValidator) {
        this.sendValidator = sendValidator;
    }

    public LLMListView getMessageListView() {
        return messageListView;
    }

    public FXTextArea getInputArea() {
        return inputArea;
    }

    public void refreshProviders() {
        this.providerComboBox.reloadProviders();
        LLMProvider provider = this.providerComboBox.getSelectedProvider();
        this.syncProviderSetting(provider);
        this.updateModels(provider);
    }

    private void syncProviderSetting(LLMProvider provider) {
        String uid = provider == null ? null : provider.getUid();
        if (Objects.equals(this.setting.getLlmProviderUid(), uid)) {
            return;
        }
        this.setting.setLlmProviderUid(uid);
        this.settingStore.replace(this.setting);
    }

    public void refreshSessions() {
        if (!this.sessionTabComboBox.isItemEmpty() && this.sessionTabComboBox.getSelectedTab() == null) {
            this.sessionTabComboBox.selectFirst();
        }
    }

    public LLMProviderComboBox getProviderComboBox() {
        return providerComboBox;
    }

    public FXComboBox<String> getModelComboBox() {
        return modelComboBox;
    }

    public LLMThinkingLevelComboBox getThinkingLevelComboBox() {
        return thinkingLevelComboBox;
    }

    public LLMProvider getSelectedProvider() {
        return providerComboBox.getSelectedProvider();
    }

    public LLMSessionTab getSelectedSession() {
        return sessionTabComboBox.getSelectedItem();
    }

    public String getSelectedModel() {
        return modelComboBox.getValue();
    }

    public LLMThinkingLevel getSelectedThinkingLevel() {
        return thinkingLevelComboBox.getSelectedLevel();
    }

    /**
     * 设置思考强度，并持久化到设置
     *
     * @param level 思考强度
     */
    public void setThinkingLevel(LLMThinkingLevel level) {
        if (level != null) {
            this.thinkingLevelComboBox.selectLevel(level);
        }
    }

    /**
     * 是否启用流式传输
     *
     * @return 是否启用
     */
    public boolean isStreamingEnabled() {
        return this.setting.isLlmStreaming();
    }

    /**
     * 设置是否启用流式传输，并持久化到设置
     *
     * @param streaming 是否启用
     */
    public void setStreamingEnabled(boolean streaming) {
        this.setting.setLlmStreaming(streaming);
        this.settingStore.replace(this.setting);
    }

    public LLMSessionTabComboBox getSessionTabComboBox() {
        return sessionTabComboBox;
    }

    /**
     * 获取当前会话绑定的Tab
     *
     * @return 当前会话绑定的Tab
     */
    public LLMSessionTab getBoundTab() {
        return this.sessionTabComboBox.getSelectedTab();
    }

    /**
     * 设置当前会话绑定的Tab
     *
     * @param tab Tab
     */
    public void setBoundTab(LLMSessionTab tab) {
        this.sessionTabComboBox.selectTab(tab);
    }

    public File getSelectedFile() {
        return selectedFile;
    }

    public void setSelectedFile(File file) {
        this.selectedFile = file;
        boolean hasFile = file != null;
        this.attachmentLabel.setVisible(hasFile);
        this.attachmentLabel.setManaged(hasFile);
        this.attachmentRemoveButton.setVisible(hasFile);
        this.attachmentRemoveButton.setManaged(hasFile);
        if (hasFile) {
            this.attachmentLabel.setText("附件: " + file.getName());
            this.attachmentLabel.setTooltip(new Tooltip(file.getAbsolutePath()));
        } else {
            this.attachmentLabel.setText("");
            this.attachmentLabel.setTooltip(null);
        }
        this.updateSendState();
    }

    private static String formatTokens(long value) {
        if (value >= 1_000_000L) {
            return String.format(Locale.ROOT, "%.1fM", value / 1_000_000.0);
        }
        if (value >= 1_000L) {
            return String.format(Locale.ROOT, "%.1fK", value / 1_000.0);
        }
        return Long.toString(value);
    }

    public LLMSessionTab findSessionTab(String tabId) {
        if (tabId == null) {
            return null;
        }
        for (LLMSessionTab tab : this.getSessionTabComboBox().getItems()) {
            if (Objects.equals(tab.getId(), tabId)) {
                return tab;
            }
        }
        return null;
    }

    public void clearSessionTab() {
        this.sessionTabComboBox.clearSelection();
    }
}
