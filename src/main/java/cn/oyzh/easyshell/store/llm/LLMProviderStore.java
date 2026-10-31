package cn.oyzh.easyshell.store.llm;

import cn.oyzh.easyshell.llm.LLMProvider;
import cn.oyzh.store.jdbc.JdbcStandardStore;

/**
 * llm供应商存储
 *
 * @author oyzh
 * @since 2025-09-04
 */
public class LLMProviderStore extends JdbcStandardStore<LLMProvider> {

    /**
     * 当前实例
     */
    public static final LLMProviderStore INSTANCE = new LLMProviderStore();

    @Override
    protected Class<LLMProvider> modelClass() {
        return LLMProvider.class;
    }
}
