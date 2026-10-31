package cn.oyzh.easyshell.fx.llm;

import cn.oyzh.easyshell.llm.LLMProvider;
import cn.oyzh.easyshell.store.llm.LLMProviderStore;
import cn.oyzh.fx.plus.controls.table.FXTableCell;
import cn.oyzh.fx.plus.controls.table.FXTableColumn;
import cn.oyzh.fx.plus.controls.toggle.FXToggleSwitch;
import cn.oyzh.i18n.I18nResourceBundle;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;

/**
 * LLM 供应商当前使用状态列。
 *
 * @author oyzh
 * @since 2026-10-31
 */
public class LLMProviderActiveTableColumn extends FXTableColumn<LLMProvider, LLMProvider> {

    public LLMProviderActiveTableColumn() {
        this.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));
        this.setCellFactory(this.cellFactory());
    }

    @Override
    protected Callback<TableColumn<LLMProvider, LLMProvider>, TableCell<LLMProvider, LLMProvider>> cellFactory() {
        return column -> new ActiveCell();
    }

    private static final class ActiveCell extends FXTableCell<LLMProvider, LLMProvider> {

        private final FXToggleSwitch toggleSwitch = new FXToggleSwitch();

        private LLMProvider provider;

        private boolean updating;

        private ActiveCell() {
            this.toggleSwitch.selectedChanged((observable, oldValue, newValue) -> {
                if (this.updating || this.provider == null) {
                    return;
                }
                boolean success = newValue
                        ? LLMProviderStore.INSTANCE.activate(this.provider)
                        : LLMProviderStore.INSTANCE.deactivate(this.provider);
                if (!success) {
                    this.updating = true;
                    this.toggleSwitch.setSelected(oldValue);
                    this.updateText(oldValue);
                    this.updating = false;
                    return;
                }
                this.updateText(newValue);
                TableView<LLMProvider> tableView = this.getTableView();
                if (tableView != null) {
                    if (newValue) {
                        String uid = this.provider.getUid();
                        for (LLMProvider provider : tableView.getItems()) {
                            provider.setActive(uid.equals(provider.getUid()));
                        }
                    }
                    tableView.refresh();
                }
            });
        }

        @Override
        protected void updateItem(LLMProvider item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                this.provider = null;
                super.setGraphic(null);
                return;
            }
            this.provider = item;
            this.updating = true;
            this.toggleSwitch.setSelected(item.isActive());
            this.updateText(item.isActive());
            this.updating = false;
            super.setGraphic(this.toggleSwitch);
        }

        private void updateText(boolean active) {
            this.toggleSwitch.setText(active ? I18nResourceBundle.i18nString("shell.llm.inUse") : "");
        }
    }
}
