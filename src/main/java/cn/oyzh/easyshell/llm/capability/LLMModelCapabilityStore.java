package cn.oyzh.easyshell.llm.capability;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.util.UUIDUtil;
import cn.oyzh.store.jdbc.JdbcStandardStore;

import java.util.List;
import java.util.Objects;

/**
 * Persists user overrides for model capabilities.
 *
 * @author oyzh
 * @since 2026-11-21
 */
public class LLMModelCapabilityStore extends JdbcStandardStore<LLMModelCapability> {

    public static final LLMModelCapabilityStore INSTANCE = new LLMModelCapabilityStore();

    /**
     * Finds a user override.
     *
     * @param providerUid provider id
     * @param model       model id
     * @return override, or {@code null}
     */
    public LLMModelCapability find(String providerUid, String model) {
        if (StringUtil.isBlank(providerUid) || StringUtil.isBlank(model)) {
            return null;
        }
        for (LLMModelCapability item : this.selectList()) {
            if (Objects.equals(providerUid, item.getProviderUid())
                    && Objects.equals(model, item.getModel())) {
                return item.copy();
            }
        }
        return null;
    }

    /**
     * Inserts or updates a user override.
     *
     * @param capability capability
     * @return whether saved
     */
    public boolean save(LLMModelCapability capability) {
        if (capability == null
                || StringUtil.isBlank(capability.getProviderUid())
                || StringUtil.isBlank(capability.getModel())) {
            return false;
        }
        LLMModelCapability existing = this.find(
                capability.getProviderUid(), capability.getModel());
        if (existing == null) {
            capability.setUid(UUIDUtil.uuidSimple());
            return this.insert(capability);
        }
        capability.setUid(existing.getUid());
        return this.update(capability);
    }

    /**
     * Deletes all overrides for a removed provider.
     *
     * @param providerUid provider id
     */
    public void deleteByProvider(String providerUid) {
        if (StringUtil.isBlank(providerUid)) {
            return;
        }
        List<LLMModelCapability> items = this.selectList();
        for (LLMModelCapability item : items) {
            if (Objects.equals(providerUid, item.getProviderUid())) {
                this.delete(item);
            }
        }
    }

    @Override
    protected Class<LLMModelCapability> modelClass() {
        return LLMModelCapability.class;
    }
}
