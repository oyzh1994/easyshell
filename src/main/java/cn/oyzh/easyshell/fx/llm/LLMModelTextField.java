package cn.oyzh.easyshell.fx.llm;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.gui.text.field.SelectTextFiled;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
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
     * Set the complete model list while preserving the current selection when possible.
     *
     * @param models model names
     */
    public void setModels(List<String> models) {
        this.setModels(models, this.getSelectedModel());
    }

    /**
     * Set the complete model list and select one model as the default.
     *
     * @param models        model names
     * @param selectedModel preferred default model
     */
    public void setModels(List<String> models, String selectedModel) {
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
        String text = StringUtil.isNotBlank(selectedModel) && this.models.contains(selectedModel.trim())
                ? selectedModel.trim()
                : this.models.isEmpty() ? "" : this.models.getFirst();
        if (!Objects.equals(this.getText(), text)) {
            this.skin().setTexting();
            this.setText(text);
        }
    }

    /**
     * Returns the complete model list, independent of the currently visible selection.
     *
     * @return model names
     */
    public List<String> getModels() {
        return this.models;
    }

    /**
     * Returns the text currently selected by the user.
     *
     * @return selected model, or {@code null}
     */
    public String getSelectedModel() {
        String selected = this.getTextTrim();
        return StringUtil.isBlank(selected) ? null : selected;
    }

    /**
     * Returns the selected model when it belongs to the loaded list, otherwise the first model.
     *
     * @return effective default model, or {@code null}
     */
    public String getEffectiveDefaultModel() {
        String selected = this.getSelectedModel();
        if (selected != null && this.models.contains(selected)) {
            return selected;
        }
        return this.models.isEmpty() ? null : this.models.getFirst();
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
