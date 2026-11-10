package cn.oyzh.easyshell.shortcut;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A named shortcut key map.
 *
 * @author oyzh
 * @since 2026-11-10
 */
public class ShellShortcutKeyMap {

    private String id;

    private String name;

    private String platform;

    private boolean builtIn;

    private Map<String, List<String>> bindings = new LinkedHashMap<>();

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlatform() {
        return this.platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public boolean isBuiltIn() {
        return this.builtIn;
    }

    public void setBuiltIn(boolean builtIn) {
        this.builtIn = builtIn;
    }

    public Map<String, List<String>> getBindings() {
        return this.bindings;
    }

    public void setBindings(Map<String, List<String>> bindings) {
        this.bindings = bindings == null ? new LinkedHashMap<>() : new LinkedHashMap<>(bindings);
    }

    public List<String> bindings(String actionId) {
        List<String> values = this.bindings.get(actionId);
        return values == null ? List.of() : List.copyOf(values);
    }

    public void bindings(String actionId, List<String> values) {
        if (values == null || values.isEmpty()) {
            this.bindings.remove(actionId);
        } else {
            this.bindings.put(actionId, new ArrayList<>(values));
        }
    }

    public ShellShortcutPlatform platformType() {
        return ShellShortcutPlatform.fromId(this.platform);
    }

    /**
     * Creates a deep copy. Copies are always editable.
     */
    public ShellShortcutKeyMap copy() {
        ShellShortcutKeyMap copy = new ShellShortcutKeyMap();
        copy.id = this.id;
        copy.name = this.name;
        copy.platform = this.platform;
        copy.builtIn = false;
        this.bindings.forEach((key, value) -> copy.bindings.put(key, new ArrayList<>(value)));
        return copy;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
