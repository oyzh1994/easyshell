package cn.oyzh.easyshell.store.llm;

import cn.oyzh.easyshell.llm.LLMProvider;
import cn.oyzh.store.jdbc.JdbcStandardStore;

import java.util.List;

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

    /**
     * 将指定供应商设为当前使用中的供应商，并停用其他供应商。
     *
     * @param provider 供应商
     * @return 是否成功
     */
    public synchronized boolean activate(LLMProvider provider) {
        if (provider == null || provider.getUid() == null) {
            return false;
        }
        List<LLMProvider> providers = super.selectList();
        boolean found = false;
        for (LLMProvider item : providers) {
            boolean active = provider.getUid().equals(item.getUid());
            found = found || active;
            if (item.isActive() != active) {
                item.setActive(active);
                if (!super.update(item)) {
                    return false;
                }
            }
        }
        if (!found) {
            return false;
        }
        provider.setActive(true);
        return true;
    }

    /**
     * 停用指定供应商。
     *
     * @param provider 供应商
     * @return 是否成功
     */
    public synchronized boolean deactivate(LLMProvider provider) {
        if (provider == null || provider.getUid() == null) {
            return false;
        }
        provider.setActive(false);
        return super.update(provider);
    }

    @Override
    protected Class<LLMProvider> modelClass() {
        return LLMProvider.class;
    }
}
