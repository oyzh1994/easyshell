package cn.oyzh.easyshell.llm;

/**
 * LLM api类型
 *
 * @author oyzh
 * @since 2026-10-30
 */
public enum LLMApiType {
    OPENAI,
    ANTHROPIC,
    GEMINI,
    OLLAMA;

    public static LLMApiType fromOrdinal(int ordinal) {
        LLMApiType[] types = values();
        return ordinal < 0 || ordinal >= types.length
                ? OPENAI
                : types[ordinal];
    }
}
