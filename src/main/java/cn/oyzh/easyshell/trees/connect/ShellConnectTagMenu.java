package cn.oyzh.easyshell.trees.connect;

import cn.oyzh.easyshell.domain.ShellConnectTag;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.svg.glyph.TagSVGGlyph;
import cn.oyzh.fx.plus.menu.MenuItemManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.shape.Circle;

import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * Builds the context menu for connection and group tags.
 *
 * @author oyzh
 * @since 2026-11-10
 */
public final class ShellConnectTagMenu {

    private ShellConnectTagMenu() {
    }

    /**
     * Creates a multi-select tag menu.
     *
     * @param tags   current persisted tags
     * @param update save callback; returns whether persistence succeeded
     */
    public static Menu create(String tags, Function<String, Boolean> update) {
        Menu menu = MenuItemHelper.menu(ShellI18nHelper.tags(), new TagSVGGlyph(), null);
        AtomicReference<String> currentTags = new AtomicReference<>(tags);
        AtomicReference<MenuItem> clearRef = new AtomicReference<>();
        EnumSet<ShellConnectTag> selectedTags = ShellConnectTag.parse(tags);

        for (ShellConnectTag tag : ShellConnectTag.values()) {
            Circle graphic = new Circle(5, tag.color());
            CheckMenuItem item = MenuItemManager.getCheckMenuItem(
                    tag.displayName(), graphic, null, selectedTags.contains(tag));
            item.setOnAction(event -> {
                EnumSet<ShellConnectTag> selected = ShellConnectTag.parse(currentTags.get());
                if (item.isSelected()) {
                    selected.add(tag);
                } else {
                    selected.remove(tag);
                }
                String updated = ShellConnectTag.serialize(selected);
                if (Boolean.TRUE.equals(update.apply(updated))) {
                    currentTags.set(updated);
                    MenuItem clear = clearRef.get();
                    if (clear != null) {
                        clear.setDisable(selected.isEmpty());
                    }
                } else {
                    item.setSelected(!item.isSelected());
                }
            });
            menu.getItems().add(item);
        }

        menu.getItems().add(MenuItemHelper.separator());
        MenuItem clear = MenuItemHelper.menuItem(I18nHelper.clear(), () -> {
            if (Boolean.TRUE.equals(update.apply(null))) {
                currentTags.set(null);
                clearRef.get().setDisable(true);
                menu.getItems().stream()
                        .filter(CheckMenuItem.class::isInstance)
                        .map(CheckMenuItem.class::cast)
                        .forEach(item -> item.setSelected(false));
            }
        });
        clear.setDisable(selectedTags.isEmpty());
        clearRef.set(clear);
        menu.getItems().add(clear);
        return menu;
    }
}
