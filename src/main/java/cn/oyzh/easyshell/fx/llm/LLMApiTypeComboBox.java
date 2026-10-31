package cn.oyzh.easyshell.fx.llm;

import cn.oyzh.easyshell.llm.LLMApiType;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * LLM api类型下拉框
 *
 * @author oyzh
 * @since 2026-10-30
 */
public class LLMApiTypeComboBox extends FXComboBox<LLMApiType> {

    public LLMApiTypeComboBox() {
        this.getItems().setAll(LLMApiType.values());
        this.getSelectionModel().select(LLMApiType.OPENAI);
    }

    public LLMApiType getSelectedType() {
        return this.getValue();
    }

    public void setApiType(int apiType) {
        LLMApiType[] types = LLMApiType.values();
        if (apiType >= 0 && apiType < types.length) {
            this.getSelectionModel().select(types[apiType]);
        }
    }

}
