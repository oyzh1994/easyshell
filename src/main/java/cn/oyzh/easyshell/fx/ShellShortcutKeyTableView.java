package cn.oyzh.easyshell.fx;

import cn.oyzh.easyshell.shortcut.ShellShortcutAction;
import cn.oyzh.easyshell.shortcut.ShellShortcutKeyEntry;
import cn.oyzh.easyshell.shortcut.ShellShortcutKeyMap;
import cn.oyzh.easyshell.shortcut.ShellShortcutManager;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.i18n.I18nHelper;
import cn.oyzh.i18n.I18nResourceBundle;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Shortcut key list.
 *
 * <p>Double-click a row and press the new key combination. Built-in maps are
 * read-only; the first edit automatically switches to a user copy.</p>
 *
 * @author oyzh
 * @since 2025-04-13
 */
public class ShellShortcutKeyTableView extends FXTableView<ShellShortcutKeyEntry> {

    private ShellShortcutManager manager;

    private Consumer<ShellShortcutKeyMap> keyMapChanged;

    private String capturingActionId;

    private Scene captureScene;

    private javafx.event.EventHandler<KeyEvent> captureHandler;

    private javafx.event.EventHandler<MouseEvent> captureMouseHandler;

    private boolean refreshingItems;

    private final Set<String> tabSwitchModifiers = new LinkedHashSet<>();

    @Override
    public void initNode() {
        super.initNode();
        this.setFixedCellSize(22);
        this.setReorderable(false);
        this.getColumns().forEach(column -> column.setReorderable(false));
        this.getColumns().addListener((javafx.collections.ListChangeListener<TableColumn<ShellShortcutKeyEntry, ?>>) change -> {
            while (change.next()) {
                change.getAddedSubList().forEach(column -> column.setReorderable(false));
            }
        });
        this.setRowFactory(tableView -> {
            TableRow<ShellShortcutKeyEntry> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    this.getSelectionModel().select(row.getItem());
                    this.startCapture();
                    event.consume();
                }
            });
            return row;
        });
        this.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (!this.refreshingItems && this.capturingActionId != null
                    && (newValue == null || !this.capturingActionId.equals(newValue.getActionId()))) {
                this.cancelEditing();
            }
        });
        this.visibleProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                this.cancelEditing();
            }
        });
        this.setContextMenu(this.createContextMenu());
        this.refreshItems();
    }

    /**
     * Sets the working copy used by this table.
     */
    public void setManager(ShellShortcutManager manager) {
        this.cancelCapture();
        this.manager = manager;
        this.refreshItems();
    }

    /**
     * Notifies the owner when an edit automatically changes the active key map.
     */
    public void setKeyMapChanged(Consumer<ShellShortcutKeyMap> keyMapChanged) {
        this.keyMapChanged = keyMapChanged;
    }

    /**
     * Rebuilds rows from the active key map.
     */
    public void refreshItems() {
        if (this.manager == null) {
            return;
        }
        ShellShortcutKeyMap keyMap = this.manager.getSelectedKeyMap();
        List<ShellShortcutKeyEntry> entries = new ArrayList<>();
        for (ShellShortcutAction.Definition action : ShellShortcutAction.all()) {
            boolean capturing = action.id().equals(this.capturingActionId);
            String key;
            if (capturing && ShellShortcutAction.TAB_SWITCH.equals(action.id())) {
                key = ShellI18nHelper.shortcutTabSwitchPrompt();
            } else if (capturing) {
                key = I18nHelper.shortcutKey() + "...";
            } else {
                key = this.manager.getBindingDisplay(action.id());
            }
            String state;
            if (!action.editable()) {
                state = I18nHelper.readonly();
            } else {
                state = keyMap.isBuiltIn()
                        ? I18nResourceBundle.i18nString(I18nHelper.DEFAULT)
                        : I18nHelper.custom();
            }
            entries.add(new ShellShortcutKeyEntry(action.id(), key, action.displayName(), state, action.editable()));
        }
        this.refreshingItems = true;
        try {
            this.setItem(entries);
            if (this.capturingActionId != null) {
                for (int i = 0; i < entries.size(); i++) {
                    if (this.capturingActionId.equals(entries.get(i).getActionId())) {
                        this.getSelectionModel().select(i);
                        break;
                    }
                }
            }
        } finally {
            this.refreshingItems = false;
        }
    }

    /**
     * Stops waiting for a key and restores the current rows.
     */
    public void cancelEditing() {
        if (this.capturingActionId == null) {
            return;
        }
        this.cancelCapture();
        this.refreshItems();
    }

    private ContextMenu createContextMenu() {
        ContextMenu menu = new ContextMenu();
        MenuItem edit = new MenuItem(I18nHelper.edit());
        edit.setOnAction(event -> this.startCapture());
        MenuItem reset = new MenuItem(I18nHelper.reset());
        reset.setOnAction(event -> this.resetSelectedBinding());
        menu.getItems().addAll(edit, reset);
        return menu;
    }

    private void startCapture() {
        ShellShortcutKeyEntry entry = this.getSelectedItem();
        if (entry == null) {
            return;
        }
        if (!entry.isEditable()) {
            return;
        }
        this.cancelCapture();
        this.capturingActionId = entry.getActionId();
        this.tabSwitchModifiers.clear();
        this.refreshItems();
        this.requestFocus();

        this.captureHandler = event -> {
            if (ShellShortcutAction.TAB_SWITCH.equals(entry.getActionId())) {
                this.handleTabSwitchCapture(event, entry);
                return;
            }
            if (event.getEventType() == KeyEvent.KEY_RELEASED) {
                return;
            }
            if (event.getCode().isModifierKey()) {
                event.consume();
                return;
            }
            event.consume();
            if (event.getCode() == KeyCode.ESCAPE) {
                this.cancelCapture();
                this.refreshItems();
                return;
            }
            String binding = ShellShortcutManager.bindingOf(event, entry.getActionId());
            this.commitBinding(entry, binding);
        };
        this.captureScene = this.getScene();
        if (this.captureScene != null) {
            this.captureScene.addEventFilter(KeyEvent.KEY_PRESSED, this.captureHandler);
            this.captureScene.addEventFilter(KeyEvent.KEY_RELEASED, this.captureHandler);
            this.captureMouseHandler = event -> {
                TableRow<?> row = this.findRow(event.getTarget());
                if (row == null || row.isEmpty() || !(row.getItem() instanceof ShellShortcutKeyEntry entry1)
                        || !entry.getActionId().equals(entry1.getActionId())) {
                    this.cancelEditing();
                }
            };
            this.captureScene.addEventFilter(MouseEvent.MOUSE_PRESSED, this.captureMouseHandler);
        }
    }

    /**
     * Handles the tab switcher where only modifiers are editable and the
     * numeric range is fixed.
     */
    private void handleTabSwitchCapture(KeyEvent event, ShellShortcutKeyEntry entry) {
        event.consume();
        if (event.getCode() == KeyCode.ESCAPE) {
            this.cancelCapture();
            this.refreshItems();
            return;
        }
        String modifier = modifierToken(event.getCode());
        if (modifier != null) {
            if (event.getEventType() == KeyEvent.KEY_PRESSED) {
                this.tabSwitchModifiers.add(modifier);
            } else if (event.getEventType() == KeyEvent.KEY_RELEASED
                    && this.tabSwitchModifiers.contains(modifier)
                    && !this.tabSwitchModifiers.isEmpty()) {
                String binding = this.tabSwitchBinding();
                this.tabSwitchModifiers.remove(modifier);
                this.commitBinding(entry, binding);
            }
            return;
        }
        if (event.getEventType() == KeyEvent.KEY_PRESSED
                && event.getCode().isDigitKey()
                && !this.tabSwitchModifiers.isEmpty()) {
            this.commitBinding(entry, this.tabSwitchBinding());
        }
    }

    private String tabSwitchBinding() {
        return ShellShortcutManager.tabSwitchBinding(this.tabSwitchModifiers);
    }

    private static String modifierToken(KeyCode code) {
        return switch (code) {
            case CONTROL -> "CTRL";
            case ALT -> "ALT";
            case SHIFT -> "SHIFT";
            case META, COMMAND, WINDOWS -> "META";
            default -> null;
        };
    }

    private boolean commitBinding(ShellShortcutKeyEntry entry, String binding) {
        if (binding == null) {
            return false;
        }
        String conflictAction = this.manager.findConflict(entry.getActionId(), binding);
        if (conflictAction != null) {
            ShellShortcutAction.Definition conflict = ShellShortcutAction.get(conflictAction);
            if (conflict != null) {
                MessageBox.alert(Alert.AlertType.WARNING,
                        ShellI18nHelper.shortcutConflict(),
                        null,
                        ShellI18nHelper.shortcutConflictMessage(conflict.displayName()),
                        this.getScene() == null ? null : this.getScene().getWindow());
            }
            this.requestFocus();
            return false;
        }
        ShellShortcutKeyMap keyMap = this.manager.updateBinding(entry.getActionId(), List.of(binding));
        this.removeCaptureHandler();
        this.capturingActionId = null;
        this.tabSwitchModifiers.clear();
        this.refreshItems();
        if (keyMap != null && this.keyMapChanged != null) {
            this.keyMapChanged.accept(keyMap);
        }
        return keyMap != null;
    }

    private void resetSelectedBinding() {
        ShellShortcutKeyEntry entry = this.getSelectedItem();
        if (entry == null) {
            return;
        }
        if (!entry.isEditable() || this.manager.getSelectedKeyMap().isBuiltIn()) {
            return;
        }
        if (this.manager.resetBinding(entry.getActionId())) {
            this.refreshItems();
            if (this.keyMapChanged != null) {
                this.keyMapChanged.accept(this.manager.getSelectedKeyMap());
            }
        }
    }

    private void cancelCapture() {
        this.removeCaptureHandler();
        this.capturingActionId = null;
        this.tabSwitchModifiers.clear();
    }

    private void removeCaptureHandler() {
        if (this.captureScene != null && this.captureHandler != null) {
            this.captureScene.removeEventFilter(KeyEvent.KEY_PRESSED, this.captureHandler);
            this.captureScene.removeEventFilter(KeyEvent.KEY_RELEASED, this.captureHandler);
        }
        if (this.captureScene != null && this.captureMouseHandler != null) {
            this.captureScene.removeEventFilter(MouseEvent.MOUSE_PRESSED, this.captureMouseHandler);
        }
        this.captureScene = null;
        this.captureHandler = null;
        this.captureMouseHandler = null;
    }

    private TableRow<?> findRow(Object target) {
        Node node = target instanceof Node value ? value : null;
        while (node != null && node != this) {
            if (node instanceof TableRow<?> row) {
                return row;
            }
            node = node.getParent();
        }
        return null;
    }

    @Override
    public void destroy() {
        this.cancelCapture();
        super.destroy();
    }
}
