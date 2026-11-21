package cn.oyzh.easyshell.fx.llm;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.llm.mcp.LLMMcpServer;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.svg.glyph.llm.TestSVGGlyph;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.menu.MenuItemManager;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;

import java.util.ArrayList;
import java.util.List;

/**
 * MCP Server 表格。
 *
 * @author oyzh
 * @since 2026-11-20
 */
public class LLMMcpServerTableView extends FXTableView<LLMMcpServer> {

    {
        TableViewUtil.copyCellDataOnDoubleClicked(this);
    }

    private Runnable addAction;

    private Runnable editAction;

    private Runnable testAction;

    private Runnable deleteAction;

    public void setAddAction(Runnable addAction) {
        this.addAction = addAction;
    }

    public void setTestAction(Runnable testAction) {
        this.testAction = testAction;
    }

    public void setEditAction(Runnable editAction) {
        this.editAction = editAction;
    }

    public void setDeleteAction(Runnable deleteAction) {
        this.deleteAction = deleteAction;
    }

    /**
     * Refresh only the cells bound to the specified server.
     *
     * @param server server row
     */
    public void refreshRow(LLMMcpServer server) {
        if (server == null) {
            return;
        }
        FXUtil.runLater(() -> {
            for (Node node : this.lookupAll(".table-row-cell")) {
                if (!(node instanceof TableRow<?> row) || row.getItem() != server) {
                    continue;
                }
                for (Node cellNode : row.getChildrenUnmodifiable()) {
                    if (cellNode instanceof TableCell<?, ?> cell && cell.getIndex() >= 0) {
                        int index = cell.getIndex();
                        cell.updateIndex(-1);
                        cell.updateIndex(index);
                    }
                }
                return;
            }
        });
    }

    @Override
    protected void initEvenListener() {
        super.initEvenListener();
        this.setOnContextMenuRequested(event -> {
            List<? extends MenuItem> items = this.getMenuItems();
            if (CollectionUtil.isNotEmpty(items)) {
                this.showContextMenu(items, event.getScreenX(), event.getScreenY());
            } else {
                this.clearContextMenu();
            }
        });
    }

    @Override
    public List<? extends MenuItem> getMenuItems() {
        List<FXMenuItem> menuItems = new ArrayList<>();
        menuItems.add(MenuItemHelper.add(this.addAction));
        FXMenuItem test= (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.test(), new TestSVGGlyph(), this.testAction);
        test.setDisable(this.getSelectedItems().isEmpty());
        menuItems.add(test);
        FXMenuItem edit = MenuItemHelper.edit(this.editAction);
        edit.setDisable(this.getSelectedItems().isEmpty());
        menuItems.add(edit);
        FXMenuItem delete = MenuItemHelper.delete(this.deleteAction);
        delete.setDisable(this.getSelectedItems().isEmpty());
        menuItems.add(delete);
        return menuItems;
    }
}
