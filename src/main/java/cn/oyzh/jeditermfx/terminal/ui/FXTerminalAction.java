package cn.oyzh.jeditermfx.terminal.ui;

import com.jediterm.terminal.ui.TerminalAction;
import com.jediterm.terminal.ui.TerminalActionProvider;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author traff
 */
public class FXTerminalAction extends TerminalAction {

    private final Predicate<KeyEvent> myRunnable;

    private KeyCode myMnemonicKeyCode = null;

    public FXTerminalAction(@NotNull FXTerminalActionPresentation presentation, @NotNull Predicate<KeyEvent> runnable) {
        super(presentation, keyEvent -> true);
        myRunnable = runnable;
    }

    public @Nullable KeyCode getFXMnemonicKeyCode() {
        return myMnemonicKeyCode;
    }

    @Override
    public @NotNull FXTerminalActionPresentation getPresentation() {
        return (FXTerminalActionPresentation) super.getPresentation();
    }

    public boolean matches(KeyEvent e) {
        for (KeyCombination kc : this.getPresentation().getKeyCombinations()) {
            if (kc.match(e)) {
                return true;
            }
        }
        return false;
    }

    public boolean isEnabled(@Nullable KeyEvent e) {
        return super.isEnabled(null);
    }

    public boolean actionPerformed(@Nullable KeyEvent e) {
        return myRunnable.test(e);
    }

    public static boolean processEvent(@NotNull TerminalActionProvider actionProvider, @NotNull KeyEvent e) {
        for (TerminalAction a : actionProvider.getActions()) {
            if (a instanceof FXTerminalAction action && action.matches(e)) {
                return action.isEnabled(e) && action.actionPerformed(e);
            }
        }

        if (actionProvider.getNextProvider() != null) {
            return processEvent(actionProvider.getNextProvider(), e);
        }

        return false;
    }

    public FXTerminalAction withMnemonicKey(KeyCode keyCode) {
        myMnemonicKeyCode = keyCode;
        return this;
    }

    public FXTerminalAction withEnabledSupplier(@NotNull Supplier<Boolean> enabledSupplier) {
        super.withEnabledSupplier(enabledSupplier);
        return this;
    }

    public FXTerminalAction separatorBefore(boolean enabled) {
        super.separatorBefore(enabled);
        return this;
    }

    private @NotNull MenuItem toMenuItem() {
        var itemText = this.getName();

        if (myMnemonicKeyCode != null) {
            var key = myMnemonicKeyCode.getName();
            itemText = itemText.replace(key, "_" + key);
        }

        FXTerminalActionPresentation myPresentation = this.getPresentation();
        MenuItem menuItem = new MenuItem(itemText);
        if (!myPresentation.getKeyCombinations().isEmpty()) {
            menuItem.setAccelerator(myPresentation.getKeyCombinations().getFirst());
        }

        menuItem.setOnAction(actionEvent -> actionPerformed((KeyEvent) null));
        menuItem.setDisable(!isEnabled((KeyEvent) null));

        return menuItem;
    }

    public static void fillMenu(@NotNull ContextMenu menu, @NotNull TerminalActionProvider actionProvider) {
        buildMenu(actionProvider, new FXTerminalActionMenuBuilder() {
            @Override
            public void addAction(@NotNull FXTerminalAction action) {
                menu.getItems().add(action.toMenuItem());
            }

            @Override
            public void addSeparator() {
                menu.getItems().add(new SeparatorMenuItem());
            }
        });
    }

    public static void buildMenu(@NotNull TerminalActionProvider provider, @NotNull FXTerminalActionMenuBuilder builder) {
        List<TerminalActionProvider> actionProviders = listActionProviders(provider);
        boolean emptyGroup = true;
        for (TerminalActionProvider actionProvider : actionProviders) {
            boolean addSeparator = !emptyGroup;
            emptyGroup = true;
            for (TerminalAction action : actionProvider.getActions()) {
                if (action.isHidden()) continue;
                if (addSeparator || action.isSeparated()) {
                    builder.addSeparator();
                    addSeparator = false;
                }
                builder.addAction(action);
                emptyGroup = false;
            }
        }
    }

    private static @NotNull List<TerminalActionProvider> listActionProviders(@NotNull TerminalActionProvider provider) {
        var providers = new ArrayList<TerminalActionProvider>();
        for (var p = provider; p != null; p = p.getNextProvider()) {
            providers.addFirst(p);
        }
        return providers;
    }
}
