package cn.oyzh.easyshell.fx;

import cn.oyzh.easyshell.shortcut.ShellShortcutKeyMap;
import cn.oyzh.easyshell.shortcut.ShellShortcutManager;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.ListCell;

/**
 * Shortcut key-map selector.
 *
 * @author oyzh
 * @since 2026-11-10
 */
public class ShellShortcutKeyMapComboBox extends FXComboBox<ShellShortcutKeyMap> {

    private ShellShortcutManager manager;

    @Override
    public void initNode() {
        this.setCellFactory(listView -> this.createCell());
        this.setButtonCell(this.createCell());
        super.initNode();
    }

    /**
     * Refreshes the key-map options and current selection.
     */
    public void setManager(ShellShortcutManager manager) {
        this.manager = manager;
        this.refreshItems();
    }

    /**
     * Re-reads all key maps from the manager.
     */
    public void refreshItems() {
        if (this.manager == null) {
            return;
        }
        this.getItems().setAll(this.manager.getKeyMaps());
        this.getSelectionModel().select(this.manager.getSelectedKeyMap());
    }

    private ListCell<ShellShortcutKeyMap> createCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(ShellShortcutKeyMap item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    this.setText(null);
                } else if (item.isBuiltIn()) {
                    this.setText(item.getName() + " (" + I18nHelper.readonly() + ")");
                } else {
                    this.setText(item.getName());
                }
            }
        };
    }
}
