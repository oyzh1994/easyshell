package cn.oyzh.easyshell.store.llm;

import cn.oyzh.easyshell.llm.skill.LLMSkill;
import cn.oyzh.store.jdbc.JdbcStandardStore;

import java.util.List;

/**
 * Agent Skill 存储。
 *
 * @author oyzh
 * @since 2026-11-21
 */
public class LLMSkillStore extends JdbcStandardStore<LLMSkill> {

    public static final LLMSkillStore INSTANCE = new LLMSkillStore();

    public List<LLMSkill> selectEnabled() {
        List<LLMSkill> skills = super.selectList();
        if (skills == null) {
            return List.of();
        }
        return skills.stream().filter(LLMSkill::isEnabled).toList();
    }

    public boolean setEnabled(LLMSkill skill, boolean enabled) {
        if (skill == null || skill.getUid() == null) {
            return false;
        }
        skill.setEnabled(enabled);
        return super.update(skill);
    }

    public boolean nameExists(String name, String excludeUid) {
        if (name == null || name.isBlank()) {
            return false;
        }
        List<LLMSkill> skills = super.selectList();
        if (skills == null) {
            return false;
        }
        return skills.stream()
                .filter(skill -> skill != null)
                .filter(skill -> excludeUid == null || !excludeUid.equals(skill.getUid()))
                .anyMatch(skill -> name.trim().equalsIgnoreCase(skill.getName()));
    }

    @Override
    protected Class<LLMSkill> modelClass() {
        return LLMSkill.class;
    }
}
