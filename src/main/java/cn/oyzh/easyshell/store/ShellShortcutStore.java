package cn.oyzh.easyshell.store;

import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.domain.ShellShortcut;
import cn.oyzh.store.jdbc.JdbcStandardStore;

/**
 * Shell shortcut storage.
 *
 * @author oyzh
 * @since 2026-11-10
 */
public class ShellShortcutStore extends JdbcStandardStore<ShellShortcut> {

    public static final ShellShortcutStore INSTANCE = new ShellShortcutStore();

    public static final ShellShortcut SHORTCUT = INSTANCE.load();

    /**
     * Loads the shortcut configuration, migrating the old ShellSetting fields
     * once when the dedicated table is first introduced.
     */
    public ShellShortcut load() {
        ShellShortcut shortcut = super.selectOne(ShellShortcut.DEFAULT_ID);
        if (shortcut == null) {
            shortcut = new ShellShortcut();
            ShellSetting setting = ShellSettingStore.SETTING;
            shortcut.setEnabled(setting.isEnableShortcutKey());
            shortcut.setKeyMapsJson(setting.getShortcutKeyConfig());
            this.insert(shortcut);
        }
        return shortcut;
    }

    /**
     * Saves and replaces the active in-memory shortcut configuration.
     */
    public boolean replace(ShellShortcut shortcut) {
        if (shortcut == null) {
            return false;
        }
        shortcut.setId(ShellShortcut.DEFAULT_ID);
        boolean result;
        if (this.exist(shortcut.getId())) {
            result = this.update(shortcut);
        } else {
            result = this.insert(shortcut);
        }
        if (result && SHORTCUT != shortcut) {
            SHORTCUT.copy(shortcut);
        }
        return result;
    }

    @Override
    protected Class<ShellShortcut> modelClass() {
        return ShellShortcut.class;
    }
}
