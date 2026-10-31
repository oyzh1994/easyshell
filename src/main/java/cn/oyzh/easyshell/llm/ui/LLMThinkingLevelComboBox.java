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

    /**
     * 获取思考强度显示名称
     *
     * @param level 思考强度
     * @return 显示名称
     */
    public static String getDisplayName(LLMThinkingLevel level) {
        if (level == null) {
            return null;
        }
        return switch (level) {
            case OFF -> I18nHelper.off();
            case LOW -> I18nHelper.low();
            case MEDIUM -> I18nHelper.medium();
            case HIGH -> I18nHelper.high();
        };
    }

    private static final class LLMThinkingLevelCell extends ListCell<LLMThinkingLevel> {

        @Override
        protected void updateItem(LLMThinkingLevel item, boolean empty) {
            super.updateItem(item, empty);
            this.setText(empty || item == null ? null : LLMThinkingLevelComboBox.getDisplayName(item));
        }
    }
}
