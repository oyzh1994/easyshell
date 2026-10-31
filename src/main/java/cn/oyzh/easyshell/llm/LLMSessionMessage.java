package cn.oyzh.easyshell.llm;

/**
 * LLM会话消息持久化对象
 *
 * @author oyzh
 * @since 2026-10-31
 */
public class LLMSessionMessage {

    private String messageId;

    private LLMMessageType type;

    private long time;

    private String message;

    private String filePath;

    private String model;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public LLMMessageType getType() {
        return type;
    }

    public void setType(LLMMessageType type) {
        this.type = type;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}
