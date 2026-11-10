package cn.oyzh.easyshell.domain;

import cn.oyzh.store.jdbc.Column;
import cn.oyzh.store.jdbc.PrimaryKey;
import cn.oyzh.store.jdbc.Table;

import java.io.Serializable;

/**
 * Shell shortcut configuration.
 *
 * @author oyzh
 * @since 2026-11-10
 */
@Table("t_shortcut")
public class ShellShortcut implements Serializable {

    /**
     * The single shortcut configuration row.
     */
    public static final String DEFAULT_ID = "default";

    @Column
    @PrimaryKey
    private String id = DEFAULT_ID;

    /**
     * Whether shortcut handling is enabled.
     */
    @Column
    private Boolean enabled;

    /**
     * Serialized user key maps and selected key map id.
     */
    @Column
    private String keyMapsJson;

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isEnabled() {
        return this.enabled == null || this.enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getKeyMapsJson() {
        return this.keyMapsJson;
    }

    public void setKeyMapsJson(String keyMapsJson) {
        this.keyMapsJson = keyMapsJson;
    }

    /**
     * Replaces this object's persisted fields with a synchronized copy.
     */
    public void copy(ShellShortcut shortcut) {
        if (shortcut != null) {
            this.id = DEFAULT_ID;
            this.enabled = shortcut.enabled;
            this.keyMapsJson = shortcut.keyMapsJson;
        }
    }

    /**
     * Creates an independent copy for export and synchronization.
     */
    public ShellShortcut copy() {
        ShellShortcut copy = new ShellShortcut();
        copy.copy(this);
        return copy;
    }
}
