package cn.oyzh.easyshell.fx.llm;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.llm.LLMMessage;
import cn.oyzh.fx.plus.controls.list.FXListView;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class LLMListView extends FXListView<LLMMessage> {

    private static final double BOTTOM_TOLERANCE = 0.01;

    private Runnable messageUpdateHandler;

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

        //this.sceneProperty().addListener((observable, oldScene, newScene) -> {
        //    if (newScene != null) {
        //        FXUtil.runLater(this::installScrollBarListener);
        //    }
        //});
        //this.skinProperty().addListener((observable, oldSkin, newSkin) -> {
        //    if (newSkin != null) {
        //        FXUtil.runLater(this::installScrollBarListener);
        //    }
        //});
        this.addEventFilter(ScrollEvent.SCROLL, event -> this.onUserScroll());
        FXUtil.runLater(this::installScrollBarListener);
    }

    @Override
    public void initNode() {
        super.initNode();
        this.setCursor(Cursor.DEFAULT);
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        if (this.autoScroll && !this.userScrolling) {
            ScrollBar scrollBar = this.verticalScrollBar;
            if (scrollBar == null) {
                scrollBar = FXUtil.getVScrollBar(this);
            }
            if (scrollBar != null && scrollBar.getValue() < scrollBar.getMax()) {
                scrollBar.setValue(scrollBar.getMax());
            }
        }
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

    public LLMMessage addErrorMessage(String message) {
        LLMMessage result = LLMMessage.error(message);
        this.addMessage(result);
        return result;
    }

    public void addMessage(LLMMessage message) {
        boolean shouldScroll = this.autoScroll && !this.userScrolling;
        this.addItem(message);
        this.notifyMessageUpdated();
        if (shouldScroll) {
            this.scheduleScrollToBottom();
        }
    }

    public void appendMessage(LLMMessage message, String text) {
        boolean shouldScroll = this.autoScroll && !this.userScrolling;
        message.appendMessage(text);
        this.notifyMessageUpdated();
        if (shouldScroll) {
            this.scheduleScrollToBottom();
        }
    }

    /**
     * 覆盖已有消息内容
     *
     * @param message 目标消息
     * @param text    新内容
     */
    public void replaceMessage(LLMMessage message, String text) {
        if (message == null) {
            return;
        }
        boolean shouldScroll = this.autoScroll && !this.userScrolling;
        message.replaceMessage(text);
        this.notifyMessageUpdated();
        if (shouldScroll) {
            this.scheduleScrollToBottom();
        }
    }

    public void clearMessages() {
        this.clearItems();
        this.resetScrollState();
    }

    /**
     * 初始化全量消息，用于会话恢复
     *
     * @param messages 完整消息列表
     */
    public void setMessages(List<LLMMessage> messages) {
        this.setItem(CollectionUtil.isEmpty(messages) ? List.of() : List.copyOf(messages));
        this.resetScrollState();
        this.scrollToBottom();
    }

    public List<LLMMessage> getMessages() {
        AtomicReference<List<LLMMessage>> result = new AtomicReference<>(List.of());
        FXUtil.runWait(() -> result.set(List.copyOf(this.getItems())));
        return result.get();
    }

    /**
     * 设置消息变更处理器
     *
     * @param messageUpdateHandler 消息变更处理器
     */
    public void setMessageUpdateHandler(Runnable messageUpdateHandler) {
        this.messageUpdateHandler = messageUpdateHandler;
    }

    private void notifyMessageUpdated() {
        if (this.messageUpdateHandler != null) {
            this.messageUpdateHandler.run();
        }
    }

    private void resetScrollState() {
        this.scrollToBottomFlag.incrementAndGet();
        this.userScrollActivity++;
        this.userScrolling = false;
        this.autoScroll = true;
    }

    private final AtomicInteger scrollToBottomFlag = new AtomicInteger();

    private ScrollBar verticalScrollBar;

    private boolean autoScroll = true;

    private boolean userScrolling;

    private int userScrollActivity;

    private final ChangeListener<Number> scrollBarValueListener = (observable, oldValue, newValue) -> {
        if (this.userScrolling && this.verticalScrollBar != null) {
            this.autoScroll = this.isAtBottom(this.verticalScrollBar);
        }
    };

    private final EventHandler<MouseEvent> scrollBarPressedHandler = event -> this.beginUserScroll();

    private final EventHandler<MouseEvent> scrollBarReleasedHandler = event -> this.endUserScrollLater();

    /**
     * 滚动到底部，并恢复自动跟随
     */
    public void scrollToBottom() {
        this.autoScroll = true;
        this.userScrolling = false;
        this.userScrollActivity++;
        this.scheduleScrollToBottom();
    }

    private void scheduleScrollToBottom() {
        int flag = this.scrollToBottomFlag.incrementAndGet();
        FXUtil.runLater(() -> {
            if (flag != this.scrollToBottomFlag.get() || !this.autoScroll || this.userScrolling) {
                return;
            }
            ScrollBar scrollBar = this.verticalScrollBar;
            if (scrollBar == null) {
                scrollBar = FXUtil.getVScrollBar(this);
            }
            if (scrollBar != null) {
                if (scrollBar.getValue() < scrollBar.getMax()) {
                    scrollBar.setValue(scrollBar.getMax());
                }
            } else {
                int lastIndex = this.getItems().size() - 1;
                if (lastIndex >= 0) {
                    super.scrollTo(lastIndex);
                }
            }
        });
    }

    private void installScrollBarListener() {
        ScrollBar current = FXUtil.getVScrollBar(this);
        if (current == this.verticalScrollBar) {
            return;
        }
        if (this.verticalScrollBar != null) {
            this.verticalScrollBar.valueProperty().removeListener(this.scrollBarValueListener);
            this.verticalScrollBar.removeEventFilter(MouseEvent.MOUSE_PRESSED, this.scrollBarPressedHandler);
            this.verticalScrollBar.removeEventFilter(MouseEvent.MOUSE_RELEASED, this.scrollBarReleasedHandler);
        }
        this.verticalScrollBar = current;
        if (current != null) {
            current.valueProperty().addListener(this.scrollBarValueListener);
            current.addEventFilter(MouseEvent.MOUSE_PRESSED, this.scrollBarPressedHandler);
            current.addEventFilter(MouseEvent.MOUSE_RELEASED, this.scrollBarReleasedHandler);
        }
    }

    private void onUserScroll() {
        this.beginUserScroll();
        this.endUserScrollLater();
    }

    private void beginUserScroll() {
        this.userScrolling = true;
        this.userScrollActivity++;
    }

    private void endUserScrollLater() {
        int activity = this.userScrollActivity;
        Platform.runLater(() -> {
            if (activity != this.userScrollActivity) {
                return;
            }
            this.userScrolling = false;
            if (this.verticalScrollBar != null) {
                this.autoScroll = this.isAtBottom(this.verticalScrollBar);
            }
        });
    }

    private boolean isAtBottom(ScrollBar scrollBar) {
        double range = scrollBar.getMax() - scrollBar.getMin();
        if (range <= 0 || scrollBar.getVisibleAmount() >= range - range * BOTTOM_TOLERANCE) {
            return true;
        }
        return scrollBar.getValue() >= scrollBar.getMax() - range * BOTTOM_TOLERANCE;
    }
}
