package cn.oyzh.easyshell.shortcut;

import cn.oyzh.common.system.OSUtil;

/**
 * Platforms with independent built-in shortcut defaults.
 *
 * @author oyzh
 * @since 2026-11-10
 */
public enum ShellShortcutPlatform {

    MACOS("macos"),

    WINDOWS_LINUX("windows-linux");

    private final String id;

    ShellShortcutPlatform(String id) {
        this.id = id;
    }

    public String id() {
        return this.id;
    }

    public static ShellShortcutPlatform current() {
        return OSUtil.isMacOS() ? MACOS : WINDOWS_LINUX;
    }

    public static ShellShortcutPlatform fromId(String id) {
        if (MACOS.id.equalsIgnoreCase(id)) {
            return MACOS;
        }
        return WINDOWS_LINUX;
    }
}
