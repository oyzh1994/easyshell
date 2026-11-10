package cn.oyzh.easyshell.shortcut;

import cn.oyzh.common.json.JSONUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.util.UUIDUtil;
import cn.oyzh.easyshell.domain.ShellShortcut;
import cn.oyzh.i18n.I18nHelper;
import cn.oyzh.i18n.I18nResourceBundle;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Runtime shortcut key-map manager.
 *
 * <p>Built-in maps are immutable. The first edit of a built-in map
 * automatically creates a user-owned copy and continues editing the copy.</p>
 *
 * @author oyzh
 * @since 2026-11-10
 */
public final class ShellShortcutManager {

    public static final String MACOS_KEY_MAP_ID = "builtin.macos";

    public static final String WINDOWS_LINUX_KEY_MAP_ID = "builtin.windows-linux";

    private final List<ShellShortcutKeyMap> keyMaps = new ArrayList<>();

    private String selectedKeyMapId;

    private ShellShortcutManager() {
        this.keyMaps.add(this.createBuiltInKeyMap(ShellShortcutPlatform.MACOS));
        this.keyMaps.add(this.createBuiltInKeyMap(ShellShortcutPlatform.WINDOWS_LINUX));
        this.selectedKeyMapId = defaultKeyMapId(ShellShortcutPlatform.current());
    }

    /**
     * Loads a manager from the persisted JSON. Invalid or obsolete bindings are
     * ignored and built-in defaults remain available.
     */
    public static ShellShortcutManager loadJson(String json) {
        ShellShortcutManager manager = new ShellShortcutManager();
        if (StringUtil.isBlank(json)) {
            return manager;
        }
        try {
            ShellShortcutKeyConfig config = JSONUtil.toBean(json, ShellShortcutKeyConfig.class);
            if (config == null) {
                return manager;
            }
            if (config.getKeyMaps() != null) {
                for (ShellShortcutKeyMap keyMap : config.getKeyMaps()) {
                    if (keyMap == null || StringUtil.isBlank(keyMap.getId())) {
                        continue;
                    }
                    keyMap.setBuiltIn(false);
                    if (StringUtil.isBlank(keyMap.getName())) {
                        keyMap.setName(I18nHelper.custom());
                    }
                    if (StringUtil.isBlank(keyMap.getPlatform())) {
                        keyMap.setPlatform(ShellShortcutPlatform.current().id());
                    }
                    if (keyMap.getBindings() == null) {
                        keyMap.setBindings(new LinkedHashMap<>());
                    }
                    manager.keyMaps.add(keyMap);
                }
            }
            if (StringUtil.isNotBlank(config.getSelectedKeyMapId())
                    && manager.containsKeyMap(config.getSelectedKeyMapId())) {
                manager.selectedKeyMapId = config.getSelectedKeyMapId();
            }
        } catch (Exception ignored) {
            // A broken user copy must never make the settings page unusable.
        }
        return manager;
    }

    /**
     * Loads a manager from the dedicated shortcut domain object.
     */
    public static ShellShortcutManager load(ShellShortcut shortcut) {
        return loadJson(shortcut == null ? null : shortcut.getKeyMapsJson());
    }

    /**
     * Returns all built-in and user key maps in display order.
     */
    public List<ShellShortcutKeyMap> getKeyMaps() {
        return List.copyOf(this.keyMaps);
    }

    /**
     * Returns the active key map.
     */
    public ShellShortcutKeyMap getSelectedKeyMap() {
        ShellShortcutKeyMap keyMap = this.findKeyMap(this.selectedKeyMapId);
        if (keyMap == null) {
            this.selectedKeyMapId = defaultKeyMapId(ShellShortcutPlatform.current());
            keyMap = this.findKeyMap(this.selectedKeyMapId);
        }
        return keyMap;
    }

    public String getSelectedKeyMapId() {
        return this.getSelectedKeyMap().getId();
    }

    /**
     * Selects a key map without mutating either the key map or its bindings.
     */
    public boolean select(String keyMapId) {
        if (this.findKeyMap(keyMapId) == null) {
            return false;
        }
        this.selectedKeyMapId = keyMapId;
        return true;
    }

    /**
     * Duplicates the active map. The returned map is a user-owned copy.
     */
    public ShellShortcutKeyMap duplicateSelected() {
        ShellShortcutKeyMap selected = this.getSelectedKeyMap();
        ShellShortcutKeyMap copy = selected.copy();
        copy.setId("custom." + UUIDUtil.uuidSimple());
        copy.setName(this.nextCustomName());
        this.keyMaps.add(copy);
        this.selectedKeyMapId = copy.getId();
        return copy;
    }

    /**
     * Deletes the active user key map and selects the platform default.
     */
    public boolean deleteSelected() {
        ShellShortcutKeyMap selected = this.getSelectedKeyMap();
        if (selected == null || selected.isBuiltIn()) {
            return false;
        }
        this.keyMaps.remove(selected);
        this.selectedKeyMapId = defaultKeyMapId(selected.platformType());
        return true;
    }

    /**
     * Restores all editable bindings of the active user key map to defaults.
     */
    public boolean restoreSelected() {
        ShellShortcutKeyMap selected = this.getSelectedKeyMap();
        if (selected == null || selected.isBuiltIn()) {
            return false;
        }
        selected.setBindings(this.createDefaultBindings(selected.platformType()));
        return true;
    }

    /**
     * Updates one binding. Editing a built-in map first creates a copy.
     *
     * @return the map that now contains the binding, or {@code null} when the
     * action is unknown or is fixed by the terminal implementation
     */
    public ShellShortcutKeyMap updateBinding(String actionId, List<String> bindings) {
        ShellShortcutAction.Definition action = ShellShortcutAction.get(actionId);
        if (action == null || !action.editable()) {
            return null;
        }
        ShellShortcutKeyMap selected = this.getSelectedKeyMap();
        if (selected.isBuiltIn()) {
            selected = this.duplicateSelected();
        }
        selected.bindings(actionId, this.normalizeBindings(bindings));
        return selected;
    }

    /**
     * Restores one binding in a user key map.
     */
    public boolean resetBinding(String actionId) {
        ShellShortcutKeyMap selected = this.getSelectedKeyMap();
        ShellShortcutAction.Definition action = ShellShortcutAction.get(actionId);
        if (selected.isBuiltIn() || action == null || !action.editable()) {
            return false;
        }
        selected.bindings(actionId, defaultBindings(action, selected.platformType()));
        return true;
    }

    /**
     * Finds the active bindings for an action. If a newly registered action is
     * absent from an older user copy, the platform default remains usable.
     */
    public List<String> getBindings(String actionId) {
        ShellShortcutAction.Definition action = ShellShortcutAction.get(actionId);
        if (action == null) {
            return List.of();
        }
        ShellShortcutKeyMap selected = this.getSelectedKeyMap();
        List<String> bindings = selected.bindings(actionId);
        if (bindings.isEmpty()) {
            bindings = defaultBindings(action, selected.platformType());
        }
        return bindings;
    }

    /**
     * Returns a human-readable shortcut. Aliases that render identically are
     * collapsed.
     */
    public String getBindingDisplay(String actionId) {
        Set<String> displays = new LinkedHashSet<>();
        for (String binding : this.getBindings(actionId)) {
            displays.add(formatBinding(binding));
        }
        return String.join(" / ", displays);
    }

    /**
     * Tests whether a key event matches the active binding for an action.
     */
    public boolean matches(String actionId, KeyEvent event) {
        if (event == null) {
            return false;
        }
        for (String binding : this.getBindings(actionId)) {
            if (matchesBinding(binding, event)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Converts all JavaFX-compatible bindings for terminal action providers.
     */
    public List<KeyCombination> getKeyCombinations(String actionId) {
        List<KeyCombination> result = new ArrayList<>();
        for (String binding : this.getBindings(actionId)) {
            KeyCombination combination = toKeyCombination(binding);
            if (combination != null) {
                result.add(combination);
            }
        }
        return List.copyOf(result);
    }

    /**
     * Finds an action whose binding would be duplicated by the supplied event.
     */
    public String findConflict(String actionId, String binding) {
        String normalizedBinding = normalizeBinding(binding);
        for (ShellShortcutAction.Definition action : ShellShortcutAction.all()) {
            if (action.id().equals(actionId)) {
                continue;
            }
            for (String current : this.getBindings(action.id())) {
                if (bindingsConflict(normalizedBinding, current)) {
                    return action.id();
                }
            }
        }
        return null;
    }

    /**
     * Creates a key binding spec from a pressed event.
     */
    public static String bindingOf(KeyEvent event, String actionId) {
        List<String> parts = new ArrayList<>();
        if (event.isControlDown()) {
            parts.add("CTRL");
        }
        if (event.isAltDown()) {
            parts.add("ALT");
        }
        if (event.isShiftDown()) {
            parts.add("SHIFT");
        }
        if (event.isMetaDown()) {
            parts.add("META");
        }
        parts.add(event.getCode().isDigitKey() && ShellShortcutAction.TAB_SWITCH.equals(actionId)
                ? ShellShortcutAction.DIGIT_KEY
                : event.getCode().name());
        return String.join("+", parts);
    }

    /**
     * Builds a tab-switch binding from editable modifiers. The numeric range is
     * always fixed to 1..9.
     */
    public static String tabSwitchBinding(Set<String> modifiers) {
        if (modifiers == null || modifiers.isEmpty()) {
            return null;
        }
        List<String> parts = new ArrayList<>();
        if (modifiers.contains("CTRL")) {
            parts.add("CTRL");
        }
        if (modifiers.contains("ALT")) {
            parts.add("ALT");
        }
        if (modifiers.contains("SHIFT")) {
            parts.add("SHIFT");
        }
        if (modifiers.contains("META")) {
            parts.add("META");
        }
        if (parts.isEmpty()) {
            return null;
        }
        parts.add(ShellShortcutAction.DIGIT_KEY);
        return String.join("+", parts);
    }

    /**
     * Serializes user key maps only. Built-in maps are recreated by every
     * application version.
     */
    public String toJson() {
        ShellShortcutKeyConfig config = new ShellShortcutKeyConfig();
        config.setSelectedKeyMapId(this.selectedKeyMapId);
        List<ShellShortcutKeyMap> customKeyMaps = this.keyMaps.stream()
                .filter(keyMap -> !keyMap.isBuiltIn())
                .toList();
        config.setKeyMaps(customKeyMaps);
        return JSONUtil.toJson(config);
    }

    /**
     * Returns the built-in defaults for a platform.
     */
    public static List<String> defaultBindings(String actionId, ShellShortcutPlatform platform) {
        ShellShortcutAction.Definition action = ShellShortcutAction.get(actionId);
        return action == null ? List.of() : defaultBindings(action, platform);
    }

    private static List<String> defaultBindings(ShellShortcutAction.Definition action,
                                                ShellShortcutPlatform platform) {
        return platform == ShellShortcutPlatform.MACOS
                ? action.macBindings()
                : action.windowsLinuxBindings();
    }

    private ShellShortcutKeyMap createBuiltInKeyMap(ShellShortcutPlatform platform) {
        ShellShortcutKeyMap keyMap = new ShellShortcutKeyMap();
        keyMap.setId(defaultKeyMapId(platform));
        keyMap.setName((platform == ShellShortcutPlatform.MACOS ? "macOS" : "Windows / Linux")
                + " " + I18nResourceBundle.i18nString(I18nHelper.DEFAULT));
        keyMap.setPlatform(platform.id());
        keyMap.setBuiltIn(true);
        keyMap.setBindings(this.createDefaultBindings(platform));
        return keyMap;
    }

    private Map<String, List<String>> createDefaultBindings(ShellShortcutPlatform platform) {
        Map<String, List<String>> bindings = new LinkedHashMap<>();
        for (ShellShortcutAction.Definition action : ShellShortcutAction.all()) {
            bindings.put(action.id(), new ArrayList<>(defaultBindings(action, platform)));
        }
        return bindings;
    }

    private String nextCustomName() {
        String baseName = I18nHelper.custom();
        Set<String> names = new LinkedHashSet<>();
        for (ShellShortcutKeyMap keyMap : this.keyMaps) {
            names.add(keyMap.getName());
        }
        if (!names.contains(baseName)) {
            return baseName;
        }
        int index = 2;
        while (names.contains(baseName + " " + index)) {
            index++;
        }
        return baseName + " " + index;
    }

    private List<String> normalizeBindings(List<String> bindings) {
        if (bindings == null) {
            return List.of();
        }
        Set<String> result = new LinkedHashSet<>();
        for (String binding : bindings) {
            if (StringUtil.isNotBlank(binding)) {
                result.add(normalizeBinding(binding));
            }
        }
        return List.copyOf(result);
    }

    private static String normalizeBinding(String binding) {
        return binding == null ? "" : binding.replace(" ", "").toUpperCase(Locale.ROOT);
    }

    private static String formatBinding(String binding) {
        if (StringUtil.isBlank(binding)) {
            return "";
        }
        String[] tokens = normalizeBinding(binding).split("\\+");
        List<String> text = new ArrayList<>();
        List<String> symbols = new ArrayList<>();
        for (String token : tokens) {
            switch (token) {
                case "CTRL", "CONTROL" -> {
                    text.add("Ctrl");
                    symbols.add("⌃");
                }
                case "ALT", "OPTION" -> {
                    text.add("Alt");
                    symbols.add("⌥");
                }
                case "SHIFT" -> {
                    text.add("Shift");
                    symbols.add("⇧");
                }
                case "META", "COMMAND", "CMD" -> {
                    text.add("Meta");
                    symbols.add("⌘");
                }
                default -> {
                    String label = keyLabel(token);
                    text.add(label);
                    symbols.add(symbolForKey(token, label));
                }
            }
        }
        String primary = String.join(" + ", text);
        if (symbols.isEmpty() || symbols.size() == 1) {
            return primary;
        }
        return primary + " (" + String.join(" + ", symbols) + ")";
    }

    private static String keyLabel(String token) {
        return switch (token) {
            case ShellShortcutAction.DIGIT_KEY -> "1..9";
            case "ADD", "EQUALS", "PLUS" -> "+";
            case "SUBTRACT", "MINUS" -> "-";
            case "PAGE_UP" -> "PageUp";
            case "PAGE_DOWN" -> "PageDown";
            case "UP" -> "Up";
            case "DOWN" -> "Down";
            case "LEFT" -> "Left";
            case "RIGHT" -> "Right";
            case "SLASH" -> "/";
            case "OPEN_BRACKET" -> "[";
            case "CLOSE_BRACKET" -> "]";
            case "BACK_SLASH" -> "\\";
            case "SEMICOLON" -> ";";
            case "QUOTE" -> "'";
            case "COMMA" -> ",";
            case "PERIOD" -> ".";
            case "BACK_QUOTE" -> "`";
            case "SPACE" -> "Space";
            case "DIGIT0", "NUMPAD0" -> "0";
            case "DIGIT1", "NUMPAD1" -> "1";
            case "DIGIT2", "NUMPAD2" -> "2";
            case "DIGIT3", "NUMPAD3" -> "3";
            case "DIGIT4", "NUMPAD4" -> "4";
            case "DIGIT5", "NUMPAD5" -> "5";
            case "DIGIT6", "NUMPAD6" -> "6";
            case "DIGIT7", "NUMPAD7" -> "7";
            case "DIGIT8", "NUMPAD8" -> "8";
            case "DIGIT9", "NUMPAD9" -> "9";
            default -> token;
        };
    }

    private static String symbolForKey(String token, String label) {
        return switch (token) {
            case "PAGE_UP" -> "⇞";
            case "PAGE_DOWN" -> "⇟";
            case "UP" -> "↑";
            case "DOWN" -> "↓";
            case "LEFT" -> "←";
            case "RIGHT" -> "→";
            default -> label;
        };
    }

    private static boolean matchesBinding(String binding, KeyEvent event) {
        String[] tokens = normalizeBinding(binding).split("\\+");
        boolean ctrl = false;
        boolean alt = false;
        boolean shift = false;
        boolean meta = false;
        String key = null;
        for (String token : tokens) {
            switch (token) {
                case "CTRL", "CONTROL" -> ctrl = true;
                case "ALT", "OPTION" -> alt = true;
                case "SHIFT" -> shift = true;
                case "META", "COMMAND", "CMD" -> meta = true;
                default -> key = token;
            }
        }
        if (key == null
                || ctrl != event.isControlDown()
                || alt != event.isAltDown()
                || shift != event.isShiftDown()
                || meta != event.isMetaDown()) {
            return false;
        }
        if (ShellShortcutAction.DIGIT_KEY.equals(key)) {
            return event.getCode().isDigitKey();
        }
        try {
            return event.getCode() == KeyCode.valueOf(key);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private static boolean bindingsConflict(String first, String second) {
        String normalizedFirst = normalizeBinding(first);
        String normalizedSecond = normalizeBinding(second);
        if (normalizedFirst.equals(normalizedSecond)) {
            return true;
        }
        String[] firstTokens = normalizedFirst.split("\\+");
        String[] secondTokens = normalizedSecond.split("\\+");
        if (firstTokens.length != secondTokens.length || firstTokens.length == 0) {
            return false;
        }
        String firstKey = firstTokens[firstTokens.length - 1];
        String secondKey = secondTokens[secondTokens.length - 1];
        if (!firstKey.equals(secondKey)
                && !(ShellShortcutAction.DIGIT_KEY.equals(firstKey) && isDigitKey(secondKey))
                && !(ShellShortcutAction.DIGIT_KEY.equals(secondKey) && isDigitKey(firstKey))) {
            return false;
        }
        Set<String> firstModifiers = new LinkedHashSet<>();
        Set<String> secondModifiers = new LinkedHashSet<>();
        for (int i = 0; i < firstTokens.length - 1; i++) {
            firstModifiers.add(firstTokens[i]);
        }
        for (int i = 0; i < secondTokens.length - 1; i++) {
            secondModifiers.add(secondTokens[i]);
        }
        return firstModifiers.equals(secondModifiers);
    }

    private static boolean isDigitKey(String key) {
        try {
            return KeyCode.valueOf(key).isDigitKey();
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private static KeyCombination toKeyCombination(String binding) {
        String[] tokens = normalizeBinding(binding).split("\\+");
        List<KeyCombination.Modifier> modifiers = new ArrayList<>();
        KeyCode keyCode = null;
        for (String token : tokens) {
            switch (token) {
                case "CTRL", "CONTROL" -> modifiers.add(KeyCombination.CONTROL_DOWN);
                case "ALT", "OPTION" -> modifiers.add(KeyCombination.ALT_DOWN);
                case "SHIFT" -> modifiers.add(KeyCombination.SHIFT_DOWN);
                case "META", "COMMAND", "CMD" -> modifiers.add(KeyCombination.META_DOWN);
                case ShellShortcutAction.DIGIT_KEY -> keyCode = null;
                default -> {
                    try {
                        keyCode = KeyCode.valueOf(token);
                    } catch (IllegalArgumentException ignored) {
                        return null;
                    }
                }
            }
        }
        if (keyCode == null) {
            return null;
        }
        return new KeyCodeCombination(keyCode, modifiers.toArray(KeyCombination.Modifier[]::new));
    }

    private ShellShortcutKeyMap findKeyMap(String keyMapId) {
        if (keyMapId == null) {
            return null;
        }
        for (ShellShortcutKeyMap keyMap : this.keyMaps) {
            if (Objects.equals(keyMapId, keyMap.getId())) {
                return keyMap;
            }
        }
        return null;
    }

    private boolean containsKeyMap(String keyMapId) {
        return this.findKeyMap(keyMapId) != null;
    }

    private static String defaultKeyMapId(ShellShortcutPlatform platform) {
        return platform == ShellShortcutPlatform.MACOS
                ? MACOS_KEY_MAP_ID
                : WINDOWS_LINUX_KEY_MAP_ID;
    }
}
