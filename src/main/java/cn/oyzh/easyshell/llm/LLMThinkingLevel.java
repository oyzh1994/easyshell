package cn.oyzh.easyshell.llm;

/**
 * LLM思考强度
 *
 * @author oyzh
 * @since 2026-10-30
 */
public enum LLMThinkingLevel {

    /**
     * 关闭
     */
    OFF("关闭"),

    /**
     * 低
     */
    LOW("低"),

    /**
     * 中
     */
    MEDIUM("中"),

    /**
     * 高
     */
    HIGH("高");

    /**
     * 显示名称
     */
    private final String displayName;

    LLMThinkingLevel(String displayName) {
        this.displayName = displayName;
    }

    /**
     * 获取显示名称
     *
     * @return 显示名称
     */
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
