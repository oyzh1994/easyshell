package cn.oyzh.easyshell.shortcut;

import cn.oyzh.easyshell.domain.ShellShortcut;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

import java.util.Objects;

/**
 * A live key combination backed by the current shortcut key map.
 *
 * <p>Terminal action providers cache their shortcuts when a terminal is
 * created. This combination resolves the setting at event time, so changing a
 * key map takes effect without restarting the application.</p>
 *
 * @author oyzh
 * @since 2026-11-10
 */
public final class ShellShortcutKeyCombination extends KeyCombination {

    private final ShellShortcut shortcut;

    private final String actionId;

    private String shortcutKeyConfig;

    private ShellShortcutManager manager;

    public ShellShortcutKeyCombination(ShellShortcut shortcut, String actionId) {
        this.shortcut = Objects.requireNonNull(shortcut, "shortcut");
        this.actionId = Objects.requireNonNull(actionId, "actionId");
    }

    @Override
    public boolean match(KeyEvent event) {
        return this.shortcut.isEnabled() && this.manager().matches(this.actionId, event);
    }

    @Override
    public String getName() {
        return this.manager().getBindingDisplay(this.actionId);
    }

    @Override
    public String getDisplayText() {
        return this.getName();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof ShellShortcutKeyCombination other
                && this.shortcut == other.shortcut
                && Objects.equals(this.actionId, other.actionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(System.identityHashCode(this.shortcut), this.actionId);
    }

    private ShellShortcutManager manager() {
        String config = this.shortcut.getKeyMapsJson();
        if (this.manager == null || !Objects.equals(this.shortcutKeyConfig, config)) {
            this.shortcutKeyConfig = config;
            this.manager = ShellShortcutManager.loadJson(config);
        }
        return this.manager;
    }
}
