package cn.oyzh.easyshell.llm.session;

import cn.oyzh.common.date.DateHelper;
import cn.oyzh.easyshell.llm.LLMTokenUsageStats;
import cn.oyzh.store.jdbc.Column;
import cn.oyzh.store.jdbc.PrimaryKey;
import cn.oyzh.store.jdbc.Table;
import com.alibaba.fastjson2.annotation.JSONField;

import java.io.Serializable;
import java.time.Instant;

/**
 * LLM会话
 *
 * @author oyzh
 * @since 2026-10-31
 */
@Table("t_llm_session")
public class LLMSession implements Serializable {

    /**
     * 数据id
     */
    @Column
    @PrimaryKey
    private String uid;

    /**
     * 会话标题
     */
    @Column
    private String title;

    /**
     * 消息列表
     *
     * @see LLMSessionMessage
     */
    @Column
    private String messages;

    private transient int messageCount = -1;

    /**
     * 会话绑定的Tab id
     *
     * @see LLMSessionTab#getId()
     */
    @Column
    private String sessionTabId;

    /**
     * 创建时间
     */
    @Column
    private long createTime = System.currentTimeMillis();

    /**
     * 更新时间
     */
    @Column
    private long updateTime = System.currentTimeMillis();

    /**
     * Token usage from the most recent model request.
     */
    @Column
    private long lastInputTokens = LLMTokenUsageStats.UNKNOWN;

    @Column
    private long lastOutputTokens = LLMTokenUsageStats.UNKNOWN;

    @Column
    private long lastTotalTokens = LLMTokenUsageStats.UNKNOWN;

    /**
     * Cumulative token usage for this session.
     */
    @Column
    private long sessionInputTokens = LLMTokenUsageStats.UNKNOWN;

    @Column
    private long sessionOutputTokens = LLMTokenUsageStats.UNKNOWN;

    @Column
    private long sessionTotalTokens = LLMTokenUsageStats.UNKNOWN;

    /**
     * Whether every completed request supplied complete provider usage.
     */
    @Column
    private boolean cumulativeUsageComplete = true;

    /**
     * Duration of the most recent request.
     */
    @Column
    private long lastDurationMillis = LLMTokenUsageStats.UNKNOWN;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessages() {
        return messages;
    }

    public void setMessages(String messages) {
        this.messages = messages;
        this.messageCount = -1;
    }

    public String getSessionTabId() {
        return sessionTabId;
    }

    public void setSessionTabId(String sessionTabId) {
        this.sessionTabId = sessionTabId;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * Returns persisted token usage statistics.
     *
     * @return statistics
     */
    @JSONField(serialize = false, deserialize = false)
    public LLMTokenUsageStats getTokenUsageStats() {
        // Columns added by schema migration default to zero on existing rows.
        if (this.lastInputTokens == 0
                && this.lastOutputTokens == 0
                && this.lastTotalTokens == 0
                && this.sessionInputTokens == 0
                && this.sessionOutputTokens == 0
                && this.sessionTotalTokens == 0
                && this.lastDurationMillis == 0) {
            return LLMTokenUsageStats.EMPTY;
        }
        return new LLMTokenUsageStats(
                this.lastInputTokens,
                this.lastOutputTokens,
                this.lastTotalTokens,
                this.sessionInputTokens,
                this.sessionOutputTokens,
                this.sessionTotalTokens,
                this.cumulativeUsageComplete,
                this.lastDurationMillis);
    }

    /**
     * Replaces persisted token usage statistics.
     *
     * @param stats statistics
     */
    @JSONField(serialize = false, deserialize = false)
    public void setTokenUsageStats(LLMTokenUsageStats stats) {
        LLMTokenUsageStats value = stats == null ? LLMTokenUsageStats.EMPTY : stats;
        this.lastInputTokens = value.lastInputTokens();
        this.lastOutputTokens = value.lastOutputTokens();
        this.lastTotalTokens = value.lastTotalTokens();
        this.sessionInputTokens = value.sessionInputTokens();
        this.sessionOutputTokens = value.sessionOutputTokens();
        this.sessionTotalTokens = value.sessionTotalTokens();
        this.cumulativeUsageComplete = value.cumulativeUsageComplete();
        this.lastDurationMillis = value.lastDurationMillis();
    }

    /**
     * 是否为未绑定Tab的临时会话
     *
     * @return 结果
     */
    @JSONField(serialize = false, deserialize = false)
    public boolean isTemporary() {
        return this.sessionTabId == null;
    }

    /**
     * 获取更新时间文本
     *
     * @return 更新时间文本
     */
    @JSONField(serialize = false, deserialize = false)
    public String getUpdateTimeText() {
        return DateHelper.formatDateTime(Instant.ofEpochMilli(this.updateTime));
    }

    /**
     * 刷新更新时间
     */
    public void touch() {
        this.updateTime = System.currentTimeMillis();
    }

    @JSONField(serialize = false, deserialize = false)
    public int getMessageCount() {
        if (this.messageCount < 0) {
            this.messageCount = LLMSessionUtil.fromJson(this.messages).size();
        }
        return this.messageCount;
    }
}
