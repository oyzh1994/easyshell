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
    OFF,

    /**
     * 低
     */
    LOW,

    /**
     * 中
     */
    MEDIUM,

    /**
     * 高
     */
    HIGH,

    /**
     * 更高
     */
    XHIGH,

    /**
     * 最大
     */
    MAX;

    public static LLMThinkingLevel fromOrdinal(Integer ordinal) {
        LLMThinkingLevel[] levels = values();
        return ordinal == null || ordinal < 0 || ordinal >= levels.length
                ? MEDIUM
                : levels[ordinal];
    }
}
