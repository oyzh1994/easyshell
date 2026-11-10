package cn.oyzh.easyshell.shortcut;

import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.i18n.I18nHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Shell shortcut action registry.
 *
 * <p>Every shortcut shown in the settings page and handled by the application is
 * registered here. The action id is the stable key used by persisted user key
 * maps, therefore it must not be renamed after release.</p>
 *
 * @author oyzh
 * @since 2026-11-10
 */
public final class ShellShortcutAction {

    public static final String TERMINAL_COPY = "terminal.copy";

    public static final String TERMINAL_PASTE = "terminal.paste";

    public static final String TAB_CLOSE = "tab.close";

    public static final String TERMINAL_FIND = "terminal.find";

    public static final String TERMINAL_CLEAR = "terminal.clear";

    public static final String TERMINAL_SELECT_ALL = "terminal.selectAll";

    public static final String TERMINAL_FONT_INCREASE = "terminal.font.increase";

    public static final String TERMINAL_FONT_DECREASE = "terminal.font.decrease";

    public static final String TERMINAL_FONT_RESET = "terminal.font.reset";

    public static final String TAB_SWITCH = "tab.switch";

    public static final String TERMINAL_HOME = "terminal.home";

    public static final String TERMINAL_END = "terminal.end";

    public static final String TERMINAL_PAGE_UP = "terminal.pageUp";

    public static final String TERMINAL_PAGE_DOWN = "terminal.pageDown";

    public static final String TERMINAL_LINE_UP = "terminal.lineUp";

    public static final String TERMINAL_LINE_DOWN = "terminal.lineDown";

    /**
     * The switch-tab action matches any digit key.
     */
    public static final String DIGIT_KEY = "DIGIT";

    private static final Map<String, Definition> DEFINITIONS = new LinkedHashMap<>();

    static {
        register(TERMINAL_COPY, ShellI18nHelper::termTip1,
                List.of("META+C"), List.of("CTRL+SHIFT+C"), true);
        register(TERMINAL_PASTE, ShellI18nHelper::termTip2,
                List.of("META+V"), List.of("CTRL+SHIFT+V"), true);
        register(TAB_CLOSE, I18nHelper::closeTab,
                List.of("META+W"), List.of("CTRL+SHIFT+W"), true);
        register(TERMINAL_FIND, ShellI18nHelper::termTip3,
                List.of("META+F"), List.of("CTRL+SHIFT+F"), true);
        register(TERMINAL_CLEAR, ShellI18nHelper::termTip4,
                List.of("META+L"), List.of("CTRL+SHIFT+L"), true);
        register(TERMINAL_SELECT_ALL, ShellI18nHelper::termTip15,
                List.of("META+A"), List.of("CTRL+SHIFT+A"), true);
        register(TERMINAL_FONT_INCREASE, I18nHelper::incrFont,
                List.of("META+ADD", "META+EQUALS"), List.of("CTRL+SHIFT+ADD", "CTRL+SHIFT+EQUALS"), true);
        register(TERMINAL_FONT_DECREASE, I18nHelper::decrFont,
                List.of("META+SUBTRACT", "META+MINUS"), List.of("CTRL+SHIFT+SUBTRACT", "CTRL+SHIFT+MINUS"), true);
        register(TERMINAL_FONT_RESET, I18nHelper::resetFont,
                List.of("META+SLASH"), List.of("CTRL+SHIFT+SLASH"), true);
        register(TAB_SWITCH, I18nHelper::switchTab,
                List.of("META+" + DIGIT_KEY), List.of("CTRL+" + DIGIT_KEY), true);
        register(TERMINAL_HOME, I18nHelper::headOfLine,
                List.of("HOME"), List.of("HOME"), false);
        register(TERMINAL_END, I18nHelper::endOfLine,
                List.of("END"), List.of("END"), false);
        register(TERMINAL_PAGE_UP, ShellI18nHelper::termTip5,
                List.of("SHIFT+PAGE_UP"), List.of("SHIFT+PAGE_UP"), true);
        register(TERMINAL_PAGE_DOWN, ShellI18nHelper::termTip6,
                List.of("SHIFT+PAGE_DOWN"), List.of("SHIFT+PAGE_DOWN"), true);
        register(TERMINAL_LINE_UP, ShellI18nHelper::termTip7,
                List.of("META+UP"), List.of("CTRL+SHIFT+UP"), true);
        register(TERMINAL_LINE_DOWN, ShellI18nHelper::termTip8,
                List.of("META+DOWN"), List.of("CTRL+SHIFT+DOWN"), true);
    }

    private ShellShortcutAction() {
    }

    /**
     * Registers a shortcut action. Applications can add their own action and
     * persist bindings by using this stable id.
     */
    public static synchronized void register(String id,
                                             Supplier<String> displayName,
                                             List<String> macBindings,
                                             List<String> windowsLinuxBindings,
                                             boolean editable) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(displayName, "displayName");
        if (DEFINITIONS.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate shortcut action id: " + id);
        }
        DEFINITIONS.put(id, new Definition(id, displayName,
                copyBindings(macBindings), copyBindings(windowsLinuxBindings), editable));
    }

    /**
     * Returns all registered actions in display order.
     */
    public static List<Definition> all() {
        return List.copyOf(DEFINITIONS.values());
    }

    /**
     * Returns an action definition by its stable id.
     */
    public static Definition get(String id) {
        return DEFINITIONS.get(id);
    }

    /**
     * Returns all known action ids.
     */
    public static List<String> ids() {
        return List.copyOf(DEFINITIONS.keySet());
    }

    private static List<String> copyBindings(List<String> bindings) {
        if (bindings == null || bindings.isEmpty()) {
            return List.of();
        }
        return Collections.unmodifiableList(new ArrayList<>(bindings));
    }

    /**
     * Immutable shortcut action definition.
     */
    public static final class Definition {

        private final String id;

        private final Supplier<String> displayName;

        private final List<String> macBindings;

        private final List<String> windowsLinuxBindings;

        private final boolean editable;

        private Definition(String id,
                           Supplier<String> displayName,
                           List<String> macBindings,
                           List<String> windowsLinuxBindings,
                           boolean editable) {
            this.id = id;
            this.displayName = displayName;
            this.macBindings = macBindings;
            this.windowsLinuxBindings = windowsLinuxBindings;
            this.editable = editable;
        }

        public String id() {
            return this.id;
        }

        public String displayName() {
            return this.displayName.get();
        }

        public List<String> macBindings() {
            return this.macBindings;
        }

        public List<String> windowsLinuxBindings() {
            return this.windowsLinuxBindings;
        }

        public boolean editable() {
            return this.editable;
        }
    }
}
