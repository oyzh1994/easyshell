package cn.oyzh.easyshell.fx.llm;

import cn.oyzh.easyshell.llm.LLMProviderTemplate;
import cn.oyzh.easyshell.llm.LLMProviderTemplateManager;
import cn.oyzh.fx.gui.text.field.SelectTextFiled;
import cn.oyzh.fx.plus.converter.SimpleStringConverter;
import cn.oyzh.i18n.I18nResourceBundle;

import java.util.List;

/**
 * Searchable LLM provider template selector.
 *
 * @author oyzh
 * @since 2026-10-31
 */
public class LLMProviderTemplateTextField extends SelectTextFiled<LLMProviderTemplate> {

    @Override
    protected boolean onTextChanged(String newValue) {
        if (!super.onTextChanged(newValue)) {
            return false;
        }
        List<LLMProviderTemplate> filtered = LLMProviderTemplateManager.INSTANCE.search(newValue);
        this.setItemList(filtered);
        if (filtered.isEmpty()) {
            this.skin().hidePopup();
        } else {
            this.skin().showPopup();
        }
        return true;
    }

    @Override
    public void initNode() {
        this.setTipText(I18nResourceBundle.i18nStringWithDefault("shell.llm.templateSelect", "Select template"));
        this.skin().setConverter(new SimpleStringConverter<>() {
            @Override
            public String toString(LLMProviderTemplate template) {
                return template == null ? "" : template.getDisplayName();
            }
        });
        this.setItemList(LLMProviderTemplateManager.INSTANCE.getTemplates());
        super.initNode();
    }
}
