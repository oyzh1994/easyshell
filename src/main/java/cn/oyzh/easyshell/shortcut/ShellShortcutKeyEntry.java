package cn.oyzh.easyshell.shortcut;

/**
 * Row model used by the shortcut settings table.
 *
 * @author oyzh
 * @since 2026-11-10
 */
public class ShellShortcutKeyEntry {

    private final String actionId;

    private final String key;

    private final String value;

    private final String state;

    private final boolean editable;

    public ShellShortcutKeyEntry(String actionId, String key, String value, String state, boolean editable) {
        this.actionId = actionId;
        this.key = key;
        this.value = value;
        this.state = state;
        this.editable = editable;
    }

    public String getActionId() {
        return this.actionId;
    }

    public String getKey() {
        return this.key;
    }

    public String getValue() {
        return this.value;
    }

    public String getState() {
        return this.state;
    }

    public boolean isEditable() {
        return this.editable;
    }
}
