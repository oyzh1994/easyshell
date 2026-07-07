package cn.oyzh.easyshell.mosh;

import cn.oyzh.easyshell.terminal.ShellSettingsProvider;

/**
 *
 * @author oyzh
 * @since 2026-07-07
 */
public class ShellMoshSettingsProvider extends ShellSettingsProvider {

    @Override
    public byte[] getCodeForKey(int key, int modifiers) {
        if (modifiers != 0) {
            return null;
        }
        return ShellMoshHelper.mapKeyToAnsiSequence(key);
    }
}
