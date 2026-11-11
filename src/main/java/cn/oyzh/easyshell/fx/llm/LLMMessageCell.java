package cn.oyzh.easyshell.fx.llm;

import cn.oyzh.easyshell.llm.LLMMessage;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.editor.incubator.EditorFormatType;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import cn.oyzh.i18n.I18nHelper;
import cn.oyzh.i18n.I18nResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.File;

public final class LLMMessageCell extends ListCell<LLMMessage> {

    private final HBox row = new HBox();

    private final VBox bubble = new VBox(5);

    private final Label meta = new Label();

    private final Label attachment = new Label();

    private final VBox bubbleStack = new VBox(4);

    private final LLMMessageView text = new LLMMessageView();

    /**
     * 当前绑定的消息
     */
    private LLMMessage boundMessage;

    /**
     * 已应用到消息编辑器的主题标识
     */
    private String appliedThemeKey;

    /**
     * 消息内容变更监听
     */
    private final ChangeListener<String> messageListener = (observable, oldValue, newValue) -> {
        this.refreshText();
    };

    /**
     * 思考内容变更监听
     */
    private final ChangeListener<String> thinkingListener = (observable, oldValue, newValue) -> {
        this.refreshText();
    };

    public LLMMessageCell() {
        this.getStyleClass().add("llm-message-cell");
        this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        this.setPadding(Insets.EMPTY);

        this.row.getStyleClass().add("llm-message-row");
        this.row.setAlignment(Pos.CENTER_LEFT);
        this.row.setMinWidth(0);
        this.row.prefWidthProperty().bind(Bindings.createDoubleBinding(
                () -> Math.max(0, this.getWidth() - this.getInsets().getLeft() - this.getInsets().getRight()),
                this.widthProperty(), this.insetsProperty()));
        this.row.maxWidthProperty().bind(this.row.prefWidthProperty());

        this.bubbleStack.getStyleClass().add("llm-bubble-stack");
        this.bubbleStack.setFillWidth(true);

        this.bubble.getStyleClass().add("llm-bubble");
        this.bubble.setFillWidth(true);
        this.bubble.maxWidthProperty().bind(this.row.widthProperty().multiply(0.78));

        this.text.getStyleClass().add("llm-bubble-text");
        this.text.setWrapText(true);
        this.text.setMaxWidth(Double.MAX_VALUE);
        this.text.setMinWidth(0);
        this.text.measurementWidthProperty().bind(this.text.maxWidthProperty());
        this.text.maxWidthProperty().bind(Bindings.max(40, this.bubble.maxWidthProperty().subtract(28)));
        this.text.maxWidthProperty().addListener((observable, oldValue, newValue) -> this.requestLayout());

        this.attachment.getStyleClass().add("llm-bubble-attachment");
        this.attachment.setWrapText(true);
        this.attachment.setMaxWidth(Double.MAX_VALUE);
        this.attachment.setMinWidth(0);
        this.attachment.maxWidthProperty().bind(Bindings.max(40, this.bubble.maxWidthProperty().subtract(28)));

        this.meta.getStyleClass().add("llm-meta");
        this.meta.setMaxWidth(Double.MAX_VALUE);

        this.bubble.getChildren().addAll(this.text, this.attachment);
        this.bubbleStack.getChildren().addAll(this.bubble, this.meta);
        this.row.getChildren().add(this.bubbleStack);
        this.setGraphic(this.row);
    }

    /**
     * 将当前主题应用到消息编辑器。
     */
    private void applyTheme() {
        ThemeStyle style = ThemeManager.currentTheme();
        if (style != null) {
            String key = style.getName() + "|" + style.getForegroundColorHex()
                    + "|" + style.getBackgroundColorHex() + "|" + style.getAccentColorHex();
            if (!key.equals(this.appliedThemeKey)) {
                this.appliedThemeKey = key;
                this.text.changeTheme(style);
            }
        }
    }

    @Override
    protected void updateItem(LLMMessage item, boolean empty) {
        super.updateItem(item, empty);
        if (this.boundMessage != null && this.boundMessage != item) {
            this.boundMessage.messageProperty().removeListener(this.messageListener);
            this.boundMessage.thinkingProperty().removeListener(this.thinkingListener);
            this.boundMessage = null;
        }
        if (empty || item == null) {
            this.text.setMessageNow("", EditorFormatType.RAW);
            this.setGraphic(null);
            return;
        }

        this.setGraphic(this.row);
        // 虚拟列表复用单元格时，确保编辑器使用当前主题。
        this.applyTheme();
        boolean user = item.isUser();
        boolean error = item.isError();
        this.row.setAlignment(user ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        this.row.getStyleClass().removeAll("llm-message-row-user", "llm-message-row-llm", "llm-message-row-error");
        this.row.getStyleClass().add(error ? "llm-message-row-error"
                : user ? "llm-message-row-user" : "llm-message-row-llm");

        this.bubble.getStyleClass().removeAll("llm-bubble-user", "llm-bubble-assistant", "llm-bubble-error");
        this.bubble.getStyleClass().add(error ? "llm-bubble-error"
                : user ? "llm-bubble-user" : "llm-bubble-assistant");

        this.text.getStyleClass().removeAll("llm-bubble-text-user", "llm-bubble-text-assistant",
                "llm-bubble-text-error");
        this.text.getStyleClass().add(error ? "llm-bubble-text-error"
                : user ? "llm-bubble-text-user" : "llm-bubble-text-assistant");
        item.messageProperty().removeListener(this.messageListener);
        item.messageProperty().addListener(this.messageListener);
        item.thinkingProperty().removeListener(this.thinkingListener);
        item.thinkingProperty().addListener(this.thinkingListener);
        this.boundMessage = item;
        this.text.setMessageNow(this.displayText(item), this.messageFormat(item));

        File file = item.getFile();
        boolean hasFile = file != null;
        this.attachment.setVisible(hasFile);
        this.attachment.setManaged(hasFile);
        this.attachment.getStyleClass().removeAll("llm-bubble-attachment-user", "llm-bubble-attachment-assistant");
        if (hasFile) {
            this.attachment.setText(String.format(I18nResourceBundle.i18nString("shell.llm.attachment"), file.getName()));
            this.attachment.getStyleClass().add(user ? "llm-bubble-attachment-user" : "llm-bubble-attachment-assistant");
        }

        String model = item.getModel();
        String prefix;
        if (user) {
            prefix = "";
        } else if (error) {
            prefix = ShellI18nHelper.errorPrefix();
        } else {
            prefix = (model == null || model.isBlank() ? "AI" : model) + " · ";
        }
        this.meta.setText(prefix + item.getTimeText());
        this.meta.setAlignment(user ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
    }

    /**
     * 获取消息内容格式：用户消息和错误消息按纯文本显示，助手消息支持 Markdown/聊天语法高亮
     *
     * @param item 消息
     * @return 内容格式
     */
    private EditorFormatType messageFormat(LLMMessage item) {
        return item.isUser() || item.isError() ? EditorFormatType.RAW : EditorFormatType.LLM_CHAT;
    }

    private void refreshText() {
        LLMMessage item = this.getItem();
        if (item != null) {
            this.text.setMessage(this.displayText(item), this.messageFormat(item));
        }
    }

    private String displayText(LLMMessage item) {
        String thinking = item.getThinking();
        String message = item.getMessage();
        boolean hasThinking = thinking != null && !thinking.isBlank();
        boolean hasMessage = message != null && !message.isBlank();
        if (!hasThinking) {
            return message == null ? "" : message;
        }
        StringBuilder result = new StringBuilder()
                .append("**").append(ShellI18nHelper.thinkingLabel()).append("**\n\n")
                .append(thinking.trim());
        if (hasMessage) {
            result.append("\n\n---\n\n")
                    .append("**").append(ShellI18nHelper.answerLabel()).append("**\n\n")
                    .append(message.trim());
        }
        return result.toString();
    }
}
