package cn.oyzh.easyshell.llm;

import cn.oyzh.common.util.UUIDUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class LLMMessage {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final LLMMessageType type;

    private String messageId = UUIDUtil.uuidSimple();

    private final long time;

    private final StringProperty message = new SimpleStringProperty("");

    private final ObjectProperty<File> file = new SimpleObjectProperty<>();

    private final StringProperty model = new SimpleStringProperty("");

    public LLMMessage(LLMMessageType type) {
        this(type, System.currentTimeMillis());
    }

    public LLMMessage(LLMMessageType type, long time) {
        this.type = type;
        this.time = time;
    }

    public static LLMMessage user(String message) {
        return user(message, null);
    }

    public static LLMMessage user(String message, File file) {
        LLMMessage result = new LLMMessage(LLMMessageType.USER);
        result.setMessage(message);
        result.setFile(file);
        return result;
    }

    public static LLMMessage llm(String message) {
        LLMMessage result = new LLMMessage(LLMMessageType.LLM);
        result.setMessage(message);
        return result;
    }

    public static LLMMessage error(String message) {
        LLMMessage result = new LLMMessage(LLMMessageType.ERROR);
        result.setMessage(message);
        return result;
    }

    public LLMMessageType getType() {
        return type;
    }

    public boolean isUser() {
        return this.type == LLMMessageType.USER;
    }

    public boolean isError() {
        return this.type == LLMMessageType.ERROR;
    }

    public long getTime() {
        return time;
    }

    public String getTimeText() {
        return TIME_FORMATTER.format(Instant.ofEpochMilli(this.time).atZone(ZoneId.systemDefault()));
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessage() {
        return message.get();
    }

    public void setMessage(String message) {
        this.message.set(message == null ? "" : message);
    }

    /**
     * 覆盖消息内容，适用于将占位消息替换为真实回复
     *
     * @param message 消息内容
     */
    public void replaceMessage(String message) {
        FXUtil.runWait(() -> this.message.set(message == null ? "" : message));
    }

    public StringProperty messageProperty() {
        return message;
    }

    public void appendMessage(String message) {
        if (message != null && !message.isEmpty()) {
            FXUtil.runWait(() -> this.message.set(this.message.get() + message));
        }
    }

    public File getFile() {
        return file.get();
    }

    public void setFile(File file) {
        this.file.set(file);
    }

    public ObjectProperty<File> fileProperty() {
        return file;
    }

    public String getModel() {
        return model.get();
    }

    public void setModel(String model) {
        this.model.set(model == null ? "" : model);
    }

    public StringProperty modelProperty() {
        return model;
    }
}
