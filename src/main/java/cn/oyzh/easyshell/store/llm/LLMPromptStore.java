package cn.oyzh.easyshell.store.llm;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.llm.prompt.LLMPrompt;
import cn.oyzh.easyshell.llm.prompt.LLMPromptUtil;
import cn.oyzh.store.jdbc.JdbcStandardStore;

import java.util.List;

/**
 * 用户提示词存储。
 *
 * @author oyzh
 * @since 2026-11-21
 */
public class LLMPromptStore extends JdbcStandardStore<LLMPrompt> {

    public static final LLMPromptStore INSTANCE = new LLMPromptStore();

    public LLMPrompt selectByUid(String uid) {
        if (StringUtil.isBlank(uid)) {
            return null;
        }
        List<LLMPrompt> prompts = super.selectList();
        if (prompts == null) {
            return null;
        }
        return prompts.stream()
                .filter(prompt -> prompt != null && uid.equals(prompt.getUid()))
                .findFirst()
                .orElse(null);
    }

    public boolean nameExists(String name, String excludeUid) {
        String normalized = LLMPromptUtil.normalizeName(name);
        if (normalized.isEmpty()) {
            return false;
        }
        List<LLMPrompt> prompts = super.selectList();
        if (prompts == null) {
            return false;
        }
        return prompts.stream()
                .filter(prompt -> prompt != null)
                .filter(prompt -> excludeUid == null || !excludeUid.equals(prompt.getUid()))
                .anyMatch(prompt -> normalized.equalsIgnoreCase(
                        LLMPromptUtil.normalizeName(prompt.getName())));
    }

    @Override
    protected Class<LLMPrompt> modelClass() {
        return LLMPrompt.class;
    }
}
