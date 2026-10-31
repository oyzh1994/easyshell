package cn.oyzh.easyshell.llm.ui;

import cn.oyzh.easyshell.llm.LLMMessage;
import cn.oyzh.easyshell.llm.LLMProvider;
import cn.oyzh.easyshell.llm.LLMSendHandler;
import cn.oyzh.easyshell.llm.LLMSessionTab;
import cn.oyzh.easyshell.llm.LLMThinkingLevel;
import cn.oyzh.fx.plus.controls.FXProgressBar;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.button.FXButton;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.pane.FXBorderPane;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.text.area.FXTextArea;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.OverrunStyle;
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

public class LLMView extends FXBorderPane {

    private final LLMListView messageListView = new LLMListView();

    private final FXTextArea inputArea = new FXTextArea();

    private final LLMProviderComboBox providerComboBox = new LLMProviderComboBox();

    private final FXComboBox<String> modelComboBox = new FXComboBox<>();

    private final LLMThinkingLevelComboBox thinkingLevelComboBox = new LLMThinkingLevelComboBox();

    private final LLMSessionTabComboBox sessionTabComboBox = new LLMSessionTabComboBox();

    private final FXButton sendButton = new FXButton("发送");

    private final FXButton attachButton = new FXButton();

    private final FXProgressBar contextProgress = new FXProgressBar();

    private final FXLabel contextLabel = new FXLabel("上下文 0%");

    private final FXLabel attachmentLabel = new FXLabel();

    private final FXHBox attachmentBar = new FXHBox();

    private final FXButton attachmentRemoveButton = new FXButton("×");

    private LLMSendHandler sendHandler;

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
                this.labeledCombo("会话Tab", this.sessionTabComboBox),
                this.buildContextUsage(),
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
                this.labeledCombo("供应商", this.providerComboBox),
                this.labeledCombo("模型", this.modelComboBox),
                this.labeledCombo("思考强度", this.thinkingLevelComboBox),
                spacer,
                this.buildSendButton()
        );
        composer.getChildren().add(toolbar);
        return composer;
    }

    private <T> FXHBox labeledCombo(String labelText, FXComboBox<T> comboBox) {
        FXLabel label = new FXLabel(labelText);
        label.getStyleClass().add("llm-field-label");
        comboBox.getStyleClass().add("llm-combo");
        FXHBox box = new FXHBox(label, comboBox);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setSpacing(5);
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
        SVGGlyph glyph = new SVGGlyph("/fx-svg/file.svg", 15);
        this.attachButton.setGraphic(glyph);
        this.attachButton.getStyleClass().add("llm-file-button");
        this.attachButton.setTooltip(new Tooltip("选择附件"));
        this.attachButton.setOnAction(event -> this.chooseFile());
        return this.attachButton;
    }

    private FXButton buildSendButton() {
        SVGGlyph glyph = new SVGGlyph("/fx-svg/arrow-up-filling.svg", 13);
        glyph.setColor(Color.WHITE);
        this.sendButton.setGraphic(glyph);
        this.sendButton.setContentDisplay(ContentDisplay.RIGHT);
        this.sendButton.setGraphicTextGap(6);
        this.sendButton.getStyleClass().add("llm-send-button");
        this.sendButton.setOnAction(event -> this.sendMessage());
        return this.sendButton;
    }

    private void initSelectors() {
        this.providerComboBox.valueProperty().addListener((observable, oldValue, newValue) -> this.updateModels(newValue));
        this.updateModels(this.providerComboBox.getSelectedProvider());
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
        chooser.setTitle("选择附件");
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
        String text = this.inputArea.getText();
        if (text == null || text.isBlank()) {
            return;
        }
        LLMMessage message = this.messageListView.addUserMessage(text.trim(), this.selectedFile);
        this.inputArea.clear();
        this.setSelectedFile(null);
        this.updateSendState();
        if (this.sendHandler != null) {
            this.sendHandler.send(message);
        }
    }

    private void updateSendState() {
        String text = this.inputArea.getText();
        this.sendButton.setDisable(this.busy || text == null || text.isBlank());
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
        this.sendButton.setText(busy ? "生成中" : "发送");
        this.updateSendState();
    }

    public boolean isBusy() {
        return busy;
    }

    public void setSendHandler(LLMSendHandler sendHandler) {
        this.sendHandler = sendHandler;
    }

    public LLMListView getMessageListView() {
        return messageListView;
    }

    public FXTextArea getInputArea() {
        return inputArea;
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

    public String getSelectedModel() {
        return modelComboBox.getValue();
    }

    public LLMThinkingLevel getSelectedThinkingLevel() {
        return thinkingLevelComboBox.getSelectedLevel();
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
}
