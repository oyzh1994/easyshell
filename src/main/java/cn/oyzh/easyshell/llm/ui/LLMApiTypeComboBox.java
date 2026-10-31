package cn.oyzh.easyshell.llm.ui;

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

}
