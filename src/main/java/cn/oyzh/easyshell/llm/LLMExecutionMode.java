package cn.oyzh.easyshell.llm;

/**
 * LLM执行模式
 *
 * @author oyzh
 * @since 2026-11-01
 */
public enum LLMExecutionMode {

    /**
     * 建议模式
     */
    SUGGEST,

    /**
     * 替我审批
     */
    APPROVE,

    /**
     * 不受限制
     */
    UNRESTRICTED;

    /**
     * 根据序号获取执行模式
     *
     * @param ordinal 序号
     * @return 执行模式
     */
    public static LLMExecutionMode fromOrdinal(int ordinal) {
        LLMExecutionMode[] modes = values();
        if (ordinal < 0 || ordinal >= modes.length) {
            return APPROVE;
        }
        return modes[ordinal];
    }
}
