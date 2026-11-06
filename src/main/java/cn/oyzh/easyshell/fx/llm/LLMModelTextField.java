package cn.oyzh.easyshell.fx.llm;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.gui.text.field.SelectTextFiled;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Searchable LLM model selector.
 *
 * @author oyzh
 * @since 2026-11-06
 */
public class LLMModelTextField extends SelectTextFiled<String> {

    private List<String> models = List.of();

    @Override
    protected boolean onTextChanged(String newValue) {
        if (!super.onTextChanged(newValue)) {
            return false;
        }
        this.applyFilter(newValue);
        return true;
    }

    /**
     * Set the complete model list.
     *
     * @param models model names
     */
    public void setModels(List<String> models) {
        Set<String> uniqueModels = new LinkedHashSet<>();
        if (models != null) {
            for (String model : models) {
                if (StringUtil.isNotBlank(model)) {
                    uniqueModels.add(model.trim());
                }
            }
        }
        this.models = List.copyOf(uniqueModels);
        this.setItemList(this.models);
    }

    private void applyFilter(String searchText) {
        String keyword = searchText == null ? "" : searchText.trim();
        boolean showAllModels = this.models.contains(keyword);
        List<String> filtered = new ArrayList<>();
        for (String model : this.models) {
            if (keyword.isEmpty() || showAllModels || StringUtil.containsIgnoreCase(model, keyword)) {
                filtered.add(model);
            }
        }
        this.setItemList(filtered);
        if (filtered.isEmpty()) {
            this.skin().hidePopup();
        } else {
            this.skin().showPopup();
        }
    }
}
