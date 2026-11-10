package cn.oyzh.easyshell.shortcut;

import java.util.ArrayList;
import java.util.List;

/**
 * Persisted shortcut configuration. Built-in maps are recreated at runtime and
 * intentionally are not serialized, so application upgrades can always provide
 * new defaults without overwriting a user copy.
 *
 * @author oyzh
 * @since 2026-11-10
 */
public class ShellShortcutKeyConfig {

    private String selectedKeyMapId;

    private List<ShellShortcutKeyMap> keyMaps = new ArrayList<>();

    public String getSelectedKeyMapId() {
        return this.selectedKeyMapId;
    }

    public void setSelectedKeyMapId(String selectedKeyMapId) {
        this.selectedKeyMapId = selectedKeyMapId;
    }

    public List<ShellShortcutKeyMap> getKeyMaps() {
        return this.keyMaps;
    }

    public void setKeyMaps(List<ShellShortcutKeyMap> keyMaps) {
        this.keyMaps = keyMaps == null ? new ArrayList<>() : keyMaps;
    }
}
