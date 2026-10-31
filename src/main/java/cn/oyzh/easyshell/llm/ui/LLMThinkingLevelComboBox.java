package cn.oyzh.easyshell.llm.ui;

import cn.oyzh.easyshell.llm.LLMThinkingLevel;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.ListCell;

/**
 * LLM思考强度下拉框
 *
 * @author oyzh
 * @since 2026-10-30
 */
public class LLMThinkingLevelComboBox extends FXComboBox<LLMThinkingLevel> {

    public LLMThinkingLevelComboBox() {
        this.getItems().setAll(LLMThinkingLevel.values());
        this.setCellFactory(list -> new LLMThinkingLevelCell());
        this.setButtonCell(new LLMThinkingLevelCell());
        this.getSelectionModel().select(LLMThinkingLevel.MEDIUM);
    }

    /**
     * 获取当前思考强度
     *
     * @return 思考强度
     */
    public LLMThinkingLevel getSelectedLevel() {
        return this.getValue();
    }

    /**
     * 选择思考强度
     *
     * @param level 思考强度
     */
    public void selectLevel(LLMThinkingLevel level) {
        this.getSelectionModel().select(level);
    }

    private static final class LLMThinkingLevelCell extends ListCell<LLMThinkingLevel> {

        @Override
        protected void updateItem(LLMThinkingLevel item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                this.setText(null);
            } else if (item == LLMThinkingLevel.OFF) {
                this.setText(I18nHelper.off());
            } else if (item == LLMThinkingLevel.LOW) {
                this.setText(I18nHelper.low());
            } else if (item == LLMThinkingLevel.MEDIUM) {
                this.setText(I18nHelper.medium());
            } else if (item == LLMThinkingLevel.HIGH) {
                this.setText(I18nHelper.high());
            }
        }
    }
}
