package cn.oyzh.easyshell.llm.skill;

/**
 * Skill 脚本执行模式。
 *
 * @author oyzh
 * @since 2026-11-21
 */
public enum LLMSkillScriptMode {

    NONE,

    SAFE,

    UNSAFE;

    public static LLMSkillScriptMode fromOrdinal(Integer ordinal) {
        LLMSkillScriptMode[] values = values();
        return ordinal == null || ordinal < 0 || ordinal >= values.length
                ? NONE
                : values[ordinal];
    }
}
