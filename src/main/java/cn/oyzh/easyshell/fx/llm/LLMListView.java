package cn.oyzh.easyshell.fx.llm;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.llm.LLMMessage;
import cn.oyzh.fx.plus.controls.list.FXListView;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.i18n.I18nResourceBundle;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.text.Font;

import java.io.File;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class LLMListView extends FXListView<LLMMessage> {

    private static final double BOTTOM_TOLERANCE = 0.0001;

    private Runnable messageUpdateHandler;

    public LLMListView() {
        this.getStyleClass().add("llm-message-list");
        this.setFocusTraversable(false);
        this.setPadding(Insets.EMPTY);
        this.setCellFactory(list -> new LLMMessageCell());
        this.setSkin(new LLMListViewSkin(this));
        Label placeholder = new Label(I18nResourceBundle.i18nString("shell.llm.chatPlaceholder"));
        placeholder.getStyleClass().add("llm-placeholder");
        this.setPlaceholder(placeholder);
        this.addEventFilter(ScrollEvent.SCROLL, this::onUserScroll);
        // 消息列表只用于展示，选中后立即清除选择，避免单元格背景覆盖聊天背景。
        this.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.intValue() >= 0) {
                FXUtil.runLater(() -> {
                    this.getSelectionModel().clearSelection();
                    this.getFocusModel().focus(-1);
                });
            }
        });
        this.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                this.scheduleMessageLayoutRefresh();
            }
        });
        FXUtil.runLater(this::installScrollBarListener);
    }

    @Override
    public void initNode() {
        super.initNode();
        this.setCursor(Cursor.DEFAULT);
    }

    @Override
    public void changeFont(Font font) {
        super.changeFont(font);
        FXUtil.runLater(this::resetMessageCellSizes);
    }

    @Override
    public void changeTheme(ThemeStyle style) {
        super.changeTheme(style);
        // ListView 不是 Parent，ThemeManager 不会继续遍历虚拟列表中的单元格，
        // 因此这里手动把新主题应用到当前可见的消息编辑器，重新生成语法高亮颜色。
        this.lookupAll(".llm-message-text").forEach(node -> {
            if (node instanceof LLMMessageView view) {
                view.changeTheme(style);
            }
        });
        // 主题变化会影响消息文本字体/行高，需要让虚拟列表重新测量单元格高度
        FXUtil.runLater(this::resetMessageCellSizes);
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        if (this.autoScroll && !this.userScrolling) {
            ScrollBar scrollBar = this.verticalScrollBar;
            if (scrollBar == null) {
                scrollBar = this.getVerticalScrollBar();
            }
            // 内容不足一屏时不滚动，否则首条消息顶部会被裁掉
            if (this.canScroll(scrollBar) && scrollBar.getValue() < scrollBar.getMax()) {
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
        this.scheduleMessageLayoutRefresh();
    }

    public void appendMessage(LLMMessage message, String text) {
        if (message == null || StringUtil.isBlank(text)) {
            return;
        }
        this.pendingAppends.offer(new MessageAppend(message, text, false));
        this.scheduleAppendFlush();
    }

    public void appendThinking(LLMMessage message, String text) {
        if (message == null || StringUtil.isBlank(text)) {
            return;
        }
        this.pendingAppends.offer(new MessageAppend(message, text, true));
        this.scheduleAppendFlush();
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
        this.flushPendingAppends();
        boolean shouldScroll = this.autoScroll && !this.userScrolling;
        message.replaceMessage(text);
        this.notifyMessageUpdated();
        if (shouldScroll) {
            this.scheduleScrollToBottom();
        }
    }

    /**
     * Replaces the thinking content of an existing message.
     *
     * @param message target message
     * @param text    new thinking content
     */
    public void replaceThinking(LLMMessage message, String text) {
        if (message == null) {
            return;
        }
        this.flushPendingAppends();
        boolean shouldScroll = this.autoScroll && !this.userScrolling;
        message.replaceThinking(text);
        this.notifyMessageUpdated();
        if (shouldScroll) {
            this.scheduleScrollToBottom();
        }
    }

    public void clearMessages() {
        this.discardPendingAppends();
        this.clearItems();
        this.resetScrollState();
    }

    /**
     * 初始化全量消息，用于会话恢复
     *
     * @param messages 完整消息列表
     */
    public void setMessages(List<LLMMessage> messages) {
        this.discardPendingAppends();
        this.setItem(CollectionUtil.isEmpty(messages) ? List.of() : List.copyOf(messages));
        this.resetScrollState();
        this.scrollToBottom();
        this.scheduleMessageLayoutRefresh();
    }

    public List<LLMMessage> getMessages() {
        this.flushPendingAppends();
        return List.copyOf(this.getItems());
    }

    public void flushMessageUpdates() {
        this.flushPendingAppends();
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
        this.userScrollActivity++;
        this.userScrolling = false;
        this.autoScroll = true;
    }

    private final ConcurrentLinkedQueue<MessageAppend> pendingAppends = new ConcurrentLinkedQueue<>();

    private final AtomicBoolean appendFlushScheduled = new AtomicBoolean();

    private boolean scrollToBottomScheduled;

    private boolean messageLayoutRefreshScheduled;

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
        if (this.scrollToBottomScheduled) {
            return;
        }
        this.scrollToBottomScheduled = true;
        FXUtil.runLater(() -> {
            this.scrollToBottomScheduled = false;
            if (!this.autoScroll || this.userScrolling) {
                return;
            }
            ScrollBar scrollBar = this.verticalScrollBar;
            if (scrollBar == null) {
                scrollBar = this.getVerticalScrollBar();
            }
            if (scrollBar != null) {
                if (this.canScroll(scrollBar) && scrollBar.getValue() < scrollBar.getMax()) {
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

    private void scheduleAppendFlush() {
        if (this.appendFlushScheduled.compareAndSet(false, true)) {
            this.flushPendingAppends();
        }
    }

    private void scheduleMessageLayoutRefresh() {
        if (this.messageLayoutRefreshScheduled) {
            return;
        }
        this.messageLayoutRefreshScheduled = true;
        Platform.runLater(() -> {
            this.messageLayoutRefreshScheduled = false;
            if (this.getScene() == null) {
                return;
            }
            this.refresh();
            if (this.autoScroll && !this.userScrolling) {
                this.scheduleScrollToBottom();
            }
        });
    }

    private void resetMessageCellSizes() {
        if (this.getSkin() instanceof LLMListViewSkin skin) {
            skin.resetCellSizeEstimates();
        }
        this.scheduleMessageLayoutRefresh();
    }

    private void flushPendingAppends() {
        boolean shouldScroll = this.autoScroll && !this.userScrolling;
        boolean updated = false;
        LLMMessage message = null;
        StringBuilder text = new StringBuilder();
        boolean thinking = false;
        MessageAppend append;
        while ((append = this.pendingAppends.poll()) != null) {
            if (message != null && (message != append.message() || thinking != append.thinking())) {
                if (thinking) {
                    message.appendThinking(text.toString());
                } else {
                    message.appendMessage(text.toString());
                }
                text.setLength(0);
                updated = true;
            }
            message = append.message();
            thinking = append.thinking();
            text.append(append.text());
        }
        if (message != null && !text.isEmpty()) {
            if (thinking) {
                message.appendThinking(text.toString());
            } else {
                message.appendMessage(text.toString());
            }
            updated = true;
        }
        this.appendFlushScheduled.set(false);
        if (!this.pendingAppends.isEmpty()) {
            this.scheduleAppendFlush();
        }
        if (updated) {
            this.notifyMessageUpdated();
            if (shouldScroll) {
                this.scheduleScrollToBottom();
            }
        }
    }

    private void discardPendingAppends() {
        this.pendingAppends.clear();
    }

    private record MessageAppend(LLMMessage message, String text, boolean thinking) {
    }

    private void installScrollBarListener() {
        ScrollBar current = this.getVerticalScrollBar();
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

    private ScrollBar getVerticalScrollBar() {
        if (this.getSkin() instanceof LLMListViewSkin skin) {
            return skin.getVerticalScrollBar();
        }
        return FXUtil.getVScrollBar(this);
    }

    private void onUserScroll(ScrollEvent event) {
        this.beginUserScroll();
        if (event.getDeltaY() > 0) {
            this.autoScroll = false;
        }
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

    /**
     * 判断内容是否超过一屏（可滚动）
     *
     * @param scrollBar 垂直滚动条
     * @return 结果
     */
    private boolean canScroll(ScrollBar scrollBar) {
        return scrollBar != null && scrollBar.getMax() > scrollBar.getVisibleAmount();
    }

    private boolean isAtBottom(ScrollBar scrollBar) {
        double range = scrollBar.getMax() - scrollBar.getMin();
        if (range <= 0 || scrollBar.getVisibleAmount() >= range - range * BOTTOM_TOLERANCE) {
            return true;
        }
        return scrollBar.getValue() >= scrollBar.getMax() - range * BOTTOM_TOLERANCE;
    }
}
