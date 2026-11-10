package cn.oyzh.easyshell.shortcut;

import cn.oyzh.easyshell.domain.ShellShortcut;
import cn.oyzh.easyshell.dto.ShellDataExport;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ShellShortcutManagerTest {

    @Test
    public void editingBuiltInMapCreatesAnIndependentCopy() {
        ShellShortcutManager manager = ShellShortcutManager.loadJson(null);
        ShellShortcutKeyMap builtIn = manager.getSelectedKeyMap();
        String builtInId = builtIn.getId();
        List<String> originalBindings = manager.getBindings(ShellShortcutAction.TERMINAL_COPY);

        ShellShortcutKeyMap copy = manager.updateBinding(
                ShellShortcutAction.TERMINAL_COPY, List.of("CTRL+ALT+K"));

        assertNotNull(copy);
        assertFalse(copy.isBuiltIn());
        assertNotEquals(builtInId, copy.getId());
        assertEquals(originalBindings, builtIn.bindings(ShellShortcutAction.TERMINAL_COPY));
        assertEquals(List.of("CTRL+ALT+K"), manager.getBindings(ShellShortcutAction.TERMINAL_COPY));
        assertTrue(manager.getKeyMaps().stream().anyMatch(keyMap -> keyMap.getId().equals(builtInId)));
    }

    @Test
    public void platformDefaultsRemainAvailable() {
        assertEquals(List.of("META+C"),
                ShellShortcutManager.defaultBindings(
                        ShellShortcutAction.TERMINAL_COPY, ShellShortcutPlatform.MACOS));
        assertEquals(List.of("CTRL+SHIFT+C"),
                ShellShortcutManager.defaultBindings(
                        ShellShortcutAction.TERMINAL_COPY, ShellShortcutPlatform.WINDOWS_LINUX));
    }

    @Test
    public void resettingBindingRestoresThePlatformDefault() {
        ShellShortcutManager manager = ShellShortcutManager.loadJson(null);
        ShellShortcutKeyMap copy = manager.duplicateSelected();
        manager.updateBinding(ShellShortcutAction.TERMINAL_PASTE, List.of("ALT+P"));

        assertTrue(manager.resetBinding(ShellShortcutAction.TERMINAL_PASTE));

        List<String> defaults = ShellShortcutManager.defaultBindings(
                ShellShortcutAction.TERMINAL_PASTE, copy.platformType());
        assertEquals(defaults, manager.getBindings(ShellShortcutAction.TERMINAL_PASTE));
    }

    @Test
    public void userMapsSurviveSerialization() {
        ShellShortcutManager manager = ShellShortcutManager.loadJson(null);
        ShellShortcutKeyMap copy = manager.updateBinding(
                ShellShortcutAction.TERMINAL_FIND, List.of("META+ALT+F"));
        assertNotNull(copy);

        ShellShortcutManager loaded = ShellShortcutManager.loadJson(manager.toJson());

        assertEquals(copy.getId(), loaded.getSelectedKeyMapId());
        assertEquals(List.of("META+ALT+F"),
                loaded.getBindings(ShellShortcutAction.TERMINAL_FIND));
        assertFalse(loaded.getSelectedKeyMap().isBuiltIn());
    }

    @Test
    public void fixedTerminalKeysCannotBeCopiedIntoEditableBindings() {
        ShellShortcutManager manager = ShellShortcutManager.loadJson(null);
        int mapCount = manager.getKeyMaps().size();

        ShellShortcutKeyMap result = manager.updateBinding(
                ShellShortcutAction.TERMINAL_HOME, List.of("CTRL+ALT+H"));

        assertNull(result);
        assertTrue(manager.getSelectedKeyMap().isBuiltIn());
        assertEquals(mapCount, manager.getKeyMaps().size());
    }

    @Test
    public void digitBindingsConflictWithTheTabSwitcher() {
        ShellShortcutManager manager = ShellShortcutManager.loadJson(null);

        assertEquals(ShellShortcutAction.TAB_SWITCH,
                manager.findConflict(ShellShortcutAction.TERMINAL_COPY, "META+DIGIT1"));
    }

    @Test
    public void liveCombinationUsesTheLatestPersistedKeyMap() {
        ShellShortcut shortcut = new ShellShortcut();
        ShellShortcutKeyCombination combination = new ShellShortcutKeyCombination(
                shortcut, ShellShortcutAction.TERMINAL_COPY);
        ShellShortcutManager manager = ShellShortcutManager.loadJson(null);
        manager.updateBinding(ShellShortcutAction.TERMINAL_COPY, List.of("CTRL+ALT+K"));
        shortcut.setKeyMapsJson(manager.toJson());

        KeyEvent event = new KeyEvent(
                KeyEvent.KEY_PRESSED, "", "", KeyCode.K, false, true, true, false);

        assertTrue(combination.match(event));
    }

    @Test
    public void bracketKeysUseReadableLabels() {
        KeyEvent openBracket = new KeyEvent(
                KeyEvent.KEY_PRESSED, "[", "[", KeyCode.OPEN_BRACKET, false, false, false, true);
        KeyEvent closeBracket = new KeyEvent(
                KeyEvent.KEY_PRESSED, "]", "]", KeyCode.CLOSE_BRACKET, false, false, false, true);
        ShellShortcutManager manager = ShellShortcutManager.loadJson(null);

        manager.updateBinding(ShellShortcutAction.TERMINAL_FONT_INCREASE,
                List.of(ShellShortcutManager.bindingOf(
                        openBracket, ShellShortcutAction.TERMINAL_FONT_INCREASE)));
        manager.updateBinding(ShellShortcutAction.TERMINAL_FONT_DECREASE,
                List.of(ShellShortcutManager.bindingOf(
                        closeBracket, ShellShortcutAction.TERMINAL_FONT_DECREASE)));

        assertEquals("Meta + [ (⌘ + [)",
                manager.getBindingDisplay(ShellShortcutAction.TERMINAL_FONT_INCREASE));
        assertEquals("Meta + ] (⌘ + ])",
                manager.getBindingDisplay(ShellShortcutAction.TERMINAL_FONT_DECREASE));
    }

    @Test
    public void onlyTabSwitchGeneralizesDigitKeys() {
        KeyEvent digit = new KeyEvent(
                KeyEvent.KEY_PRESSED, "1", "1", KeyCode.DIGIT1, false, false, false, true);

        assertEquals("META+DIGIT1",
                ShellShortcutManager.bindingOf(digit, ShellShortcutAction.TERMINAL_COPY));
        assertEquals("META+DIGIT",
                ShellShortcutManager.bindingOf(digit, ShellShortcutAction.TAB_SWITCH));
        assertEquals("CTRL+ALT+SHIFT+META+DIGIT",
                ShellShortcutManager.tabSwitchBinding(Set.of("META", "SHIFT", "ALT", "CTRL")));
        assertNull(ShellShortcutManager.tabSwitchBinding(Set.of()));
    }

    @Test
    public void shortcutConfigurationRoundTripsThroughDataExport() {
        ShellShortcut shortcut = new ShellShortcut();
        shortcut.setEnabled(false);
        shortcut.setKeyMapsJson("{\"selectedKeyMapId\":\"builtin.macos\"}");
        ShellDataExport export = ShellDataExport.of();
        export.setShortcuts(List.of(shortcut));

        ShellDataExport loaded = ShellDataExport.fromJSON(export.toJSONString());

        assertEquals(1, loaded.getShortcuts().size());
        assertFalse(loaded.getShortcuts().getFirst().isEnabled());
        assertEquals("{\"selectedKeyMapId\":\"builtin.macos\"}",
                loaded.getShortcuts().getFirst().getKeyMapsJson());
    }
}
