package cn.oyzh.easyshell.fx.llm;

import cn.oyzh.easyshell.llm.LLMModelContextWindowUtil;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import javafx.scene.control.ListCell;

/**
 * 自定义模型上下文窗口选择框。
 *
 * @author oyzh
 * @since 2026-11-24
 */
public class LLMModelContextWindowComboBox extends FXComboBox<Integer> {

    public LLMModelContextWindowComboBox() {
        this.setCellFactory(list -> new ContextWindowCell());
        this.setButtonCell(new ContextWindowCell());
        this.getItems().setAll(LLMModelContextWindowUtil.OPTIONS);
        this.setTokens(LLMModelContextWindowUtil.DEFAULT_TOKENS);
    }

    public int getSelectedTokens() {
        Integer value = this.getValue();
        return value == null ? LLMModelContextWindowUtil.DEFAULT_TOKENS : value;
    }

    public void setTokens(int tokens) {
        int normalized = LLMModelContextWindowUtil.normalize(tokens);
        this.getSelectionModel().select(normalized);
        this.setValue(normalized);
    }

    private static final class ContextWindowCell extends ListCell<Integer> {

        @Override
        protected void updateItem(Integer item, boolean empty) {
            super.updateItem(item, empty);
            this.setText(empty || item == null
                    ? null : LLMModelContextWindowUtil.label(item));
        }
    }
}
