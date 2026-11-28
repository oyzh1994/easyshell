package cn.oyzh.easyshell.util.h2;

import cn.oyzh.easyshell.h2.column.H2Column;
import cn.oyzh.easyshell.h2.record.H2RecordProperty;
import cn.oyzh.fx.db.util.DBNodeUtil;
import cn.oyzh.fx.db.util.DBUtil;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.controls.text.field.FXTextField;
import cn.oyzh.fx.plus.menu.ContextMenuManager;
import cn.oyzh.fx.plus.menu.FXContextMenu;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import javafx.scene.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * H2 记录显示工具。
 *
 * <p>H2 记录网格与 MySQL 保持一致，由记录属性创建可编辑节点。</p>
 *
 * @since 2026-11-02
 */
public final class ShellH2RecordUtil {

    private ShellH2RecordUtil() {
    }

    public static String formatValue(Object value, H2Column column) {
        return ShellH2DataUtil.formatValue(value, column);
    }

    /**
     * 获取记录编辑节点。
     *
     * @param property 记录属性
     * @param value    当前值
     * @param column   字段
     * @return 编辑节点
     */
    public static Node getNode(H2RecordProperty property, Object value, H2Column column) {
        value = ShellH2DataUtil.valueStandardization(column, value);
        Node node = DBNodeUtil.getNode(value, column);
        if (node instanceof FXTextField textField) {
            if (value == null) {
                if (column.exampleValue() == null) {
                    textField.setPromptText(DBUtil.nullPromptText());
                } else {
                    textField.setPromptText(column.exampleValue().toString());
                }
            }
            textField.setOnContextMenuRequested(event -> {
                if (textField.getContextMenu() == null) {
                    FXContextMenu contextMenu = ContextMenuManager.createContextMenu(
                            textField, getColumnMenuItem(property));
                    ContextMenuManager.setContextMenu(textField, contextMenu);
                    ContextMenuManager.showContextMenu(contextMenu, textField, event);
                }
            });
            textField.addTextChangeListener((observable, oldValue, newValue) -> property.setChanged(true));
        }
        return node;
    }

    /**
     * 获取字段菜单列表
     *
     * @param property 属性
     * @return 菜单列表
     */
    private static List<FXMenuItem> getColumnMenuItem(H2RecordProperty property) {
        List<FXMenuItem> menuItems = new ArrayList<>();
        menuItems.add(MenuItemHelper.copy_no_graphic(property::vCopy));
        menuItems.add(MenuItemHelper.paste_no_graphic(property::vPaste));
        menuItems.add(MenuItemHelper.setToNull_no_graphic(property::vSetToNull));
        menuItems.add(MenuItemHelper.setToEmptyString_no_graphic(property::vSetToEmptyString));
        menuItems.add(MenuItemHelper.copyAsInsertStatement_no_graphic(property::vCopyAsInsertSql));
        menuItems.add(MenuItemHelper.copyAsUpdateStatement_no_graphic(property::vCopyAsUpdateSql));
        return menuItems;
    }
}
