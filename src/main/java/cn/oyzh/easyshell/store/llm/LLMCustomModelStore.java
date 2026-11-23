package cn.oyzh.easyshell.store.llm;

import cn.oyzh.easyshell.llm.model.LLMCustomModel;
import cn.oyzh.store.jdbc.JdbcStandardStore;

import java.util.List;

/**
 * 自定义 LLM 模型存储。
 *
 * @author oyzh
 * @since 2026-11-24
 */
public class LLMCustomModelStore extends JdbcStandardStore<LLMCustomModel> {

    public static final LLMCustomModelStore INSTANCE = new LLMCustomModelStore();

    /**
     * 获取所有启用模型。
     *
     * @return 启用模型
     */
    public List<LLMCustomModel> selectEnabled() {
        return this.selectList().stream()
                .filter(LLMCustomModel::isEnabled)
                .toList();
    }

    /**
     * 修改启用状态。
     *
     * @param model   模型
     * @param enabled 是否启用
     * @return 是否成功
     */
    public boolean setEnabled(LLMCustomModel model, boolean enabled) {
        if (model == null) {
            return false;
        }
        model.setEnabled(enabled);
        return this.update(model);
    }

    @Override
    protected Class<LLMCustomModel> modelClass() {
        return LLMCustomModel.class;
    }
}
