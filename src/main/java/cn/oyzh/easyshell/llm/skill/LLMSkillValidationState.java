package cn.oyzh.easyshell.llm.skill;

/**
 * Skill ZIP 校验状态。
 *
 * @author oyzh
 * @since 2026-11-21
 */
public enum LLMSkillValidationState {

    UNKNOWN,

    CHECKING,

    NORMAL,

    FILE_MISSING,

    HASH_INVALID,

    INVALID
}
