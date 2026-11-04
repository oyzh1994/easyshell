package cn.oyzh.easyshell.fx.llm;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.llm.LLMExecutionMode;
import cn.oyzh.easyshell.llm.LLMMessage;
import cn.oyzh.easyshell.llm.LLMProvider;
import cn.oyzh.easyshell.llm.LLMSendHandler;
import cn.oyzh.easyshell.llm.LLMSessionTab;
import cn.oyzh.easyshell.llm.LLMThinkingLevel;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.easyshell.store.llm.LLMProviderStore;
import cn.oyzh.fx.gui.svg.glyph.SettingSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.StopSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.FileSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.llm.SendSVGGlyph;
import cn.oyzh.fx.plus.chooser.FileChooserHelper;
import cn.oyzh.fx.plus.controls.FXProgressBar;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.button.FXButton;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.pane.FXSplitPane;
import cn.oyzh.fx.plus.controls.text.area.FXTextArea;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.i18n.I18nHelper;
import cn.oyzh.i18n.I18nResourceBundle;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
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
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.BooleanSupplier;

public class LLMView extends FXSplitPane {

    private static final double SELECTOR_LABEL_WIDTH = 44;

    private static final double SELECTOR_WIDTH = 180;

    private static final String EXECUTION_MODE_TOOLTIP = "llm.executionMode.tooltip";

    private final LLMListView messageListView = new LLMListView();

    private final FXTextArea inputArea = new FXTextArea();

    private final LLMProviderComboBox providerComboBox = new LLMProviderComboBox();

    private final FXComboBox<String> modelComboBox = new FXComboBox<>();

    private final LLMThinkingLevelComboBox thinkingLevelComboBox = new LLMThinkingLevelComboBox();

    private final LLMSessionTabComboBox sessionTabComboBox = new LLMSessionTabComboBox();

    private final FXButton sendButton = new FXButton();

    private final SendSVGGlyph sendGlyph = new SendSVGGlyph();

    private final StopSVGGlyph stopGlyph = new StopSVGGlyph();

    private final Tooltip sendButtonTooltip = new Tooltip();

    private final FXButton attachButton = new FXButton();

    private final FXButton settingsButton = new FXButton();

    private final FXProgressBar contextProgress = new FXProgressBar();

    private final FXLabel contextLabel = new FXLabel(String.format(I18nResourceBundle.i18nString("shell.llm.contextUsage"), 0));

    private final FXLabel attachmentLabel = new FXLabel();

    private final FXHBox attachmentBar = new FXHBox();

    private final FXButton attachmentRemoveButton = new FXButton("×");

    private final ShellSetting setting = ShellSettingStore.SETTING;

    private final ShellSettingStore settingStore = ShellSettingStore.INSTANCE;

    private LLMSendHandler sendHandler;

    private Runnable stopHandler;

    private BooleanSupplier sendValidator;

    private File selectedFile;

    private volatile boolean busy;

    public LLMView() {
        this.getStyleClass().add("llm-view");
        FXVBox composer = this.buildComposer();
        this.buildResizableLayout(composer);
        this.initSelectors();
        this.updateSendState();
        FXUtil.runLater(this.inputArea::requestFocus);
    }

    @Override
    public void initNode() {
        this.loadStyle();
        this.getStyleClass().add("llm-layout");
        this.setOrientation(Orientation.VERTICAL);
        this.setShowDivider(true);
        super.initNode();
    }

    private FXVBox buildComposer() {
        FXVBox composer = new FXVBox();
        composer.getStyleClass().add("llm-composer");
        composer.setSpacing(4);
        composer.setPadding(new Insets(4, 14, 6, 14));

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
        this.inputArea.setPromptText(I18nResourceBundle.i18nString("shell.llm.inputPlaceholder"));
        FXVBox.setVgrow(this.inputArea, Priority.ALWAYS);
        this.inputArea.textProperty().addListener((observable, oldValue, newValue) -> this.updateSendState());
        this.inputArea.setOnKeyPressed(event -> {
            if (event.getCode() != KeyCode.ENTER) {
                return;
            }
            if (event.isShiftDown()) {
                this.inputArea.replaceSelection("\n");
                event.consume();
            } else if (!event.isControlDown() && !event.isAltDown()) {
                this.sendMessage();
                event.consume();
            }
        });
        composer.addChild(this.inputArea);

        FXHBox toolbar = new FXHBox();
        toolbar.getStyleClass().add("llm-toolbar");
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setSpacing(8);

        Region spacer = new Region();
        FXHBox.setHgrow(spacer, Priority.ALWAYS);

        toolbar.addChild(
                this.labeledCombo(I18nHelper.model(), this.modelComboBox),
                this.buildSettingsButton(),
                spacer,
                this.buildSendButton()
        );
        composer.addChild(toolbar);
        return composer;
    }

    private void buildResizableLayout(FXVBox composer) {
        this.getItems().addAll(this.messageListView, composer);
        this.setDividerPositions(0.7, 0.3);
        composer.setMinHeight(132);
        this.messageListView.setMinHeight(250);
    }

    private <T> FXHBox labeledCombo(String labelText, FXComboBox<T> comboBox) {
        FXLabel label = new FXLabel(labelText);
        label.getStyleClass().add("llm-field-label");
        label.setPrefWidth(SELECTOR_LABEL_WIDTH);
        comboBox.getStyleClass().add("llm-combo");
        comboBox.setPrefWidth(SELECTOR_WIDTH);
        FXHBox box = new FXHBox(label, comboBox);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setSpacing(3);
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
        this.sendGlyph.setColor(Color.WHITE);
        this.stopGlyph.setColor(Color.WHITE);
        this.sendButton.setGraphic(this.sendGlyph);
        this.sendButtonTooltip.setText(I18nHelper.send());
        this.sendButton.setTooltip(this.sendButtonTooltip);
        this.sendButton.getStyleClass().add("llm-send-button");
        this.sendButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        this.sendButton.setOnAction(event -> {
            if (this.busy) {
                if (this.stopHandler != null) {
                    this.stopHandler.run();
                } else {
                    this.setBusy(false);
                }
            } else {
                this.sendMessage();
            }
        });
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
                this.buildExecutionModeMenu(),
                this.buildStreamingMenu(),
                this.buildMessageLimitMenu()
        );
        menu.show(this.settingsButton, Side.TOP, 0, -4);
    }

    private Menu buildProviderMenu() {
        Menu menu = new Menu(I18nHelper.provider());
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

    private Menu buildExecutionModeMenu() {
        LLMExecutionMode selected = this.getSelectedExecutionMode();
        Menu menu = new Menu(I18nResourceBundle.i18nString("shell.llm.executionMode"));
        ToggleGroup group = new ToggleGroup();
        for (LLMExecutionMode mode : LLMExecutionMode.values()) {
            RadioMenuItem item = new RadioMenuItem(this.getExecutionModeName(mode));
            item.setToggleGroup(group);
            item.setSelected(mode == selected);
            item.setOnAction(event -> this.setExecutionMode(mode));
            item.getProperties().put(EXECUTION_MODE_TOOLTIP, this.getExecutionModeDescription(mode));
            menu.getItems().add(item);
        }
        menu.setOnShown(event -> FXUtil.runLater(() -> {
            for (MenuItem item : menu.getItems()) {
                this.installMenuItemTooltip(item);
            }
        }));
        return menu;
    }

    private void installMenuItemTooltip(MenuItem item) {
        Node node = item.getStyleableNode();
        Object text = item.getProperties().get(EXECUTION_MODE_TOOLTIP);
        if (node == null || !(text instanceof String tooltipText) || tooltipText.isBlank()) {
            return;
        }
        if (node.getProperties().containsKey(EXECUTION_MODE_TOOLTIP)) {
            return;
        }
        Tooltip tooltip = new Tooltip(tooltipText);
        tooltip.setWrapText(true);
        tooltip.setMaxWidth(360);
        tooltip.setShowDelay(Duration.millis(250));
        Tooltip.install(node, tooltip);
        node.getProperties().put(EXECUTION_MODE_TOOLTIP, tooltip);
    }

    private String getExecutionModeName(LLMExecutionMode mode) {
        return switch (mode) {
            case SUGGEST -> I18nResourceBundle.i18nString("shell.llm.executionMode.suggest");
            case APPROVE -> I18nResourceBundle.i18nString("shell.llm.executionMode.approve");
            case UNRESTRICTED -> I18nResourceBundle.i18nString("shell.llm.executionMode.unrestricted");
        };
    }

    private String getExecutionModeDescription(LLMExecutionMode mode) {
        return switch (mode) {
            case SUGGEST -> I18nResourceBundle.i18nString("shell.llm.executionMode.suggest.desc");
            case APPROVE -> I18nResourceBundle.i18nString("shell.llm.executionMode.approve.desc");
            case UNRESTRICTED -> I18nResourceBundle.i18nString("shell.llm.executionMode.unrestricted.desc");
        };
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

    /**
     * LLM消息上限可选项
     */
    public static final List<Integer> LLM_MAX_MESSAGE_OPTIONS = List.of(10, 20, 50, 100);

    private Menu buildMessageLimitMenu() {
        int selected = this.setting.getLlmMaxMessages();
        Menu menu = new Menu(I18nResourceBundle.i18nString("shell.llm.messageLimit"));
        ToggleGroup group = new ToggleGroup();
        for (Integer limit : LLM_MAX_MESSAGE_OPTIONS) {
            RadioMenuItem item = new RadioMenuItem(String.valueOf(limit));
            item.setToggleGroup(group);
            item.setSelected(limit == selected);
            item.setOnAction(event -> this.setLlmMaxMessages(limit));
            menu.getItems().add(item);
        }
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
        File file = FileChooserHelper.choose(I18nHelper.pleaseSelectFile(), this.getWindow());
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

    public void sendMessage() {
        if (this.busy) {
            return;
        }
        if (this.sendValidator != null && !this.sendValidator.getAsBoolean()) {
            return;
        }
        String text = this.inputArea.getTextTrim();
        if (StringUtil.isBlank(text) && this.selectedFile == null) {
            return;
        }
        LLMMessage message = this.addUserMessage(text, this.selectedFile);
        this.inputArea.clear();
        this.setSelectedFile(null);
        this.updateSendState();
        if (this.sendHandler != null) {
            this.sendHandler.send(message);
        }
    }

    private void updateSendState() {
        String text = this.inputArea.getText();
        boolean hasText = StringUtil.isNotBlank(text);
        this.sendButton.setDisable(!this.busy && !hasText && this.selectedFile == null);
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

    public void flushMessageUpdates() {
        this.messageListView.flushMessageUpdates();
    }

    public boolean isMessageEmpty() {
        return CollectionUtil.isEmpty(this.getMessages());
    }

    public LLMMessage addAssistantMessage(String text) {
        return this.addAssistantMessage(text, this.getSelectedSessionTabId());
    }

    public LLMMessage addAssistantMessage(String text, String sessionTabId) {
        LLMMessage message = LLMMessage.llm(text);
        message.setModel(this.getSelectedModel());
        message.setSessionTabId(sessionTabId);
        this.messageListView.addMessage(message);
        return message;
    }

    public LLMMessage addUserMessage(String text) {
        return this.addUserMessage(text, null);
    }

    public LLMMessage addUserMessage(String text, File file) {
        LLMMessage message = LLMMessage.user(text, file);
        message.setSessionTabId(this.getSelectedSessionTabId());
        this.messageListView.addMessage(message);
        return message;
    }

    public LLMMessage addErrorMessage(String text) {
        return this.addErrorMessage(text, this.getSelectedSessionTabId());
    }

    public LLMMessage addErrorMessage(String text, String sessionTabId) {
        LLMMessage message = LLMMessage.error(text);
        message.setSessionTabId(sessionTabId);
        this.messageListView.addMessage(message);
        return message;
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
            this.contextLabel.setText(String.format(I18nResourceBundle.i18nString("shell.llm.contextUsage"), Math.round(value * 100)));
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
        FXUtil.runLater(this::applyBusyState);
    }

    private void applyBusyState() {
        boolean currentBusy = this.busy;
        this.sendButton.setGraphic(currentBusy ? this.stopGlyph : this.sendGlyph);
        this.sendButtonTooltip.setText(currentBusy ? I18nHelper.stop() : I18nHelper.send());
        this.sendButton.getStyleClass().removeAll("llm-send-button", "llm-stop-button");
        this.sendButton.getStyleClass().add(currentBusy ? "llm-stop-button" : "llm-send-button");
        this.updateSendState();
    }

    public boolean isBusy() {
        return busy;
    }

    public void setSendHandler(LLMSendHandler sendHandler) {
        this.sendHandler = sendHandler;
    }

    /**
     * 设置停止当前回复处理器
     *
     * @param stopHandler 停止处理器
     */
    public void setStopHandler(Runnable stopHandler) {
        this.stopHandler = stopHandler;
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

    private String getSelectedSessionTabId() {
        LLMSessionTab sessionTab = this.getSelectedSession();
        return sessionTab == null ? null : sessionTab.getId();
    }

    public String getSelectedModel() {
        return modelComboBox.getValue();
    }

    public LLMThinkingLevel getSelectedThinkingLevel() {
        return thinkingLevelComboBox.getSelectedLevel();
    }

    /**
     * 获取执行模式
     *
     * @return 执行模式
     */
    public LLMExecutionMode getSelectedExecutionMode() {
        return LLMExecutionMode.fromOrdinal(this.setting.getLlmExecutionMode());
    }

    /**
     * 设置执行模式，并持久化到设置
     *
     * @param executionMode 执行模式
     */
    public void setExecutionMode(LLMExecutionMode executionMode) {
        if (executionMode != null) {
            this.setting.setLlmExecutionMode(executionMode.ordinal());
            this.settingStore.replace(this.setting);
        }
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
     * 获取消息上限
     *
     * @return 消息上限
     */
    public int getLlmMaxMessages() {
        return this.setting.getLlmMaxMessages();
    }

    /**
     * 设置消息上限，并持久化到设置
     *
     * @param limit 消息上限
     */
    public void setLlmMaxMessages(int limit) {
        this.setting.setLlmMaxMessages(limit);
        this.settingStore.replace(this.setting);
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
            this.attachmentLabel.setText(String.format(I18nResourceBundle.i18nString("shell.llm.attachment"), file.getName()));
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
