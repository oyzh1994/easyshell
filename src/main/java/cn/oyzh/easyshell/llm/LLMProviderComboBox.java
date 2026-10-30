package cn.oyzh.easyshell.llm;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;

import java.util.List;

/**
 * LLM供应商下拉框
 *
 * @author oyzh
 * @since 2026-10-30
 */
public class LLMProviderComboBox extends FXComboBox<LLMProvider> {

    public LLMProviderComboBox() {
        this.getItems().setAll(
                new LLMProvider("DeepSeek", "https://api.deepseek.com/v1", null,
                        List.of("deepseek-chat", "deepseek-reasoner")),
                new LLMProvider("OpenAI", "https://api.openai.com/v1", null,
                        List.of("gpt-4.1", "gpt-4.1-mini", "o3-mini")),
                new LLMProvider("Anthropic", "https://api.anthropic.com/v1", null,
                        List.of("claude-sonnet-4", "claude-opus-4")),
                new LLMProvider("Ollama", "http://localhost:11434/v1", null,
                        List.of("qwen3:8b", "llama3.3"))
        );
        this.setCellFactory(list -> new LLMProviderCell());
        this.setButtonCell(new LLMProviderCell());
        this.getSelectionModel().selectFirst();
    }

    /**
     * 设置供应商列表
     *
     * @param providers 供应商列表
     */
    public void setProviders(List<LLMProvider> providers) {
        this.getItems().setAll(providers == null ? List.of() : providers);
        if (!this.getItems().isEmpty()) {
            this.getSelectionModel().selectFirst();
        }
    }

    /**
     * 获取当前供应商
     *
     * @return 供应商
     */
    public LLMProvider getSelectedProvider() {
        return this.getValue();
    }

    /**
     * 获取当前供应商名称
     *
     * @return 供应商名称
     */
    public String getSelectedProviderName() {
        LLMProvider provider = this.getSelectedProvider();
        return provider == null ? null : provider.getName();
    }

    /**
     * 获取当前供应商基础地址
     *
     * @return 基础地址
     */
    public String getSelectedBaseUrl() {
        LLMProvider provider = this.getSelectedProvider();
        return provider == null ? null : provider.getBaseUrl();
    }

    /**
     * 获取当前供应商API密钥
     *
     * @return API密钥
     */
    public String getSelectedSk() {
        LLMProvider provider = this.getSelectedProvider();
        return provider == null ? null : provider.getSk();
    }

    /**
     * 根据名称选择供应商
     *
     * @param name 供应商名称
     */
    public void selectProvider(String name) {
        for (LLMProvider provider : this.getItems()) {
            if (provider.getName() != null && provider.getName().equalsIgnoreCase(name)) {
                this.getSelectionModel().select(provider);
                return;
            }
        }
    }

    private static final class LLMProviderCell extends ListCell<LLMProvider> {

        @Override
        protected void updateItem(LLMProvider item, boolean empty) {
            super.updateItem(item, empty);
            this.setText(empty || item == null ? null : item.getName());
            this.setTooltip(item == null || item.getBaseUrl() == null ? null
                    : new Tooltip(item.getBaseUrl()));
        }
    }
}
