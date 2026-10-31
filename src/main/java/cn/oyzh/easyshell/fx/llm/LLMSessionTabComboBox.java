package cn.oyzh.easyshell.fx.llm;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.llm.LLMSessionTab;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;

import java.util.List;

/**
 * LLM会话Tab下拉框
 *
 * @author oyzh
 * @since 2026-10-30
 */
public class LLMSessionTabComboBox extends FXComboBox<LLMSessionTab> {

    public LLMSessionTabComboBox() {
        this.setCellFactory(list -> new LLMSessionTabCell(null));
        this.setButtonCell(new LLMSessionTabCell("未绑定Tab"));
    }

    /**
     * 设置Tab列表
     *
     * @param tabs Tab列表
     */
    public void setTabs(List<LLMSessionTab> tabs) {
        if (tabs == null) {
            return;
        }
        String selectedId = this.getSelectedTabId();
        this.setItem(tabs);
        if (selectedId != null) {
            this.selectTab(selectedId);
        }
        if (this.getValue() == null && !this.getItems().isEmpty()) {
            this.getSelectionModel().selectFirst();
        }
    }

    /**
     * 获取当前绑定的Tab
     *
     * @return 当前绑定的Tab
     */
    public LLMSessionTab getSelectedTab() {
        return this.getValue();
    }

    /**
     * 获取当前绑定的Tab id
     *
     * @return Tab id
     */
    public String getSelectedTabId() {
        LLMSessionTab tab = this.getSelectedTab();
        return tab == null ? null : tab.getId();
    }

    /**
     * 获取当前绑定的Tab名称
     *
     * @return Tab名称
     */
    public String getSelectedTabName() {
        LLMSessionTab tab = this.getSelectedTab();
        return tab == null ? null : tab.getName();
    }

    /**
     * 根据id选择Tab
     *
     * @param id Tab id
     */
    public void selectTab(String id) {
        if (id == null) {
            this.getSelectionModel().clearSelection();
            return;
        }
        for (LLMSessionTab tab : this.getItems()) {
            if (id.equals(tab.getId())) {
                this.getSelectionModel().select(tab);
                return;
            }
        }
    }

    /**
     * 选择Tab，不存在时添加到列表
     *
     * @param tab Tab
     */
    public void selectTab(LLMSessionTab tab) {
        if (tab == null) {
            this.getSelectionModel().clearSelection();
            return;
        }
        if (!this.getItems().contains(tab)) {
            this.getItems().add(tab);
        }
        this.getSelectionModel().select(tab);
    }

    public void removeTab(String tabId) {
        LLMSessionTab removed = null;
        for (LLMSessionTab item : this.getItems()) {
            if (StringUtil.equals(item.getId(), tabId)) {
                removed = item;
                break;
            }
        }
        if (removed != null) {
            this.removeItem(removed);
        }
    }

    private static final class LLMSessionTabCell extends ListCell<LLMSessionTab> {

        private final String emptyText;

        private LLMSessionTabCell(String emptyText) {
            this.emptyText = emptyText;
        }

        @Override
        protected void updateItem(LLMSessionTab item, boolean empty) {
            super.updateItem(item, empty);
            this.setText(empty || item == null ? this.emptyText : item.getName());
            this.setTooltip(item == null ? null
                    : new Tooltip(item.getName() + " (" + item.getId() + ")"));
        }
    }
}
