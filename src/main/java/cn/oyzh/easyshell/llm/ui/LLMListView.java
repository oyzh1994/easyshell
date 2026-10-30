package cn.oyzh.easyshell.llm.ui;

import cn.oyzh.easyshell.llm.LLMMessage;
import cn.oyzh.fx.plus.controls.list.FXListView;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

public class LLMListView extends FXListView<LLMMessage> {

    public LLMListView() {
        this.getStyleClass().add("llm-message-list");
        this.setFocusTraversable(false);
        this.setPadding(Insets.EMPTY);
        this.setMinWidth(0);
        this.setMaxWidth(Double.MAX_VALUE);
        this.setCellFactory(list -> new LLMMessageCell());

        Label placeholder = new Label("开始一段对话吧");
        placeholder.getStyleClass().add("llm-placeholder");
        this.setPlaceholder(placeholder);
    }

    @Override
    public void initNode() {
        super.initNode();
        this.setCursor(Cursor.DEFAULT);
    }

    public LLMMessage addUserMessage(String message) {
        return this.addUserMessage(message, null);
    }

    public LLMMessage addUserMessage(String message, File file) {
        LLMMessage result = LLMMessage.user(message, file);
        this.addMessage(result);
        return result;
    }

    public LLMMessage addAssistantMessage(String message) {
        LLMMessage result = LLMMessage.llm(message);
        this.addMessage(result);
        return result;
    }

    public void addMessage(LLMMessage message) {
        this.addItem(message);
        this.scrollToBottom();
    }

    public void appendMessage(LLMMessage message, String text) {
        message.appendMessage(text);
        this.scrollToBottom();
    }

    public void clearMessages() {
        this.clearItems();
    }

    private final AtomicInteger scrollToBottomFlag = new AtomicInteger();

    public void scrollToBottom() {
        this.scrollToBottom(this.scrollToBottomFlag.incrementAndGet());
    }

    private void scrollToBottom(int flag) {
        FXUtil.runLater(() -> {
            if (flag != scrollToBottomFlag.get()) {
                return;
            }
            LLMMessage lastItem = (LLMMessage) super.lastItem();
            super.scrollTo(lastItem);
            ScrollBar scrollBar = FXUtil.getVScrollBar(this);
            if (scrollBar != null) {
                scrollBar.setValue(Double.MAX_VALUE);
            }
        });
    }
}
