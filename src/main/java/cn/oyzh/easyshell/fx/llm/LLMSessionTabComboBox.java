package cn.oyzh.easyshell.fx.llm;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.llm.session.LLMSessionTab;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.i18n.I18nResourceBundle;
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

    /**
     * 未绑定会话选项，选中后取消当前会话绑定
     */
    private final LLMSessionTab unboundItem = new LLMSessionTab(null, I18nResourceBundle.i18nString("shell.llm.unboundTab"));

    public LLMSessionTabComboBox() {
        this.addItem(this.unboundItem);
        this.setCellFactory(list -> new LLMSessionTabCell(null));
        this.setButtonCell(new LLMSessionTabCell(I18nResourceBundle.i18nString("shell.llm.unboundTab")));
    }

    /**
     * 设置Tab列表
     *
     * @param tabs Tab列表
     */
    public void setTabs(List<LLMSessionTab> tabs) {
        LLMSessionTab selected = this.getSelectedTab();
        this.setItem(this.unboundItem);
        if (tabs != null) {
            this.addItem(tabs);
        }
        if (selected != null) {
            this.selectTab(selected.getId());
        }
    }

    /**
     * 获取当前绑定的Tab
     *
     * @return 当前绑定的Tab
     */
    public LLMSessionTab getSelectedTab() {
        LLMSessionTab tab = this.getValue();
        return tab == this.unboundItem ? null : tab;
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
        if (tab == null || tab == this.unboundItem) {
            this.getSelectionModel().clearSelection();
            return;
        }
        if (!this.containsItem(tab)) {
            this.addItem(tab);
        }
        this.getSelectionModel().select(tab);
    }

    /**
     * 选中首个真实会话，跳过未绑定选项
     */
    @Override
    public void selectFirst() {
        for (LLMSessionTab tab : this.getItems()) {
            if (tab != this.unboundItem) {
                this.getSelectionModel().select(tab);
                return;
            }
        }
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
            if (item == null || item.getId() == null) {
                this.setTooltip(item == null ? null : new Tooltip(item.getName()));
            } else {
                this.setTooltip(new Tooltip(item.getName() + " (" + item.getId() + ")"));
            }
        }
    }
}
