package cn.oyzh.easyshell.trees.h2.table;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.h2.ShellH2EventUtil;
import cn.oyzh.easyshell.h2.table.H2Table;
import cn.oyzh.easyshell.h2.ShellH2Client;
import cn.oyzh.easyshell.trees.h2.ShellH2TreeItem;
import cn.oyzh.easyshell.trees.h2.schema.ShellH2SchemaTreeItem;
import cn.oyzh.easyshell.util.h2.ShellH2ViewFactory;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.List;

/**
 * H2表类型节点
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellH2TablesTreeItem extends ShellH2TreeItem<ShellH2TablesTreeItemValue> {

    // [已停用] 旧的数量缓存字段（保留备用）
//    /**
//     * 表数量缓存
//     */
//    private Integer tableSize;

    /**
     * 构造表类型节点
     *
     * @param treeView 树视图
     */
    public ShellH2TablesTreeItem(RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.setValue(new ShellH2TablesTreeItemValue(this));
    }

    @Override
    public ShellH2SchemaTreeItem parent() {
        return (ShellH2SchemaTreeItem) super.parent();
    }

    /**
     * 获取H2客户端
     *
     * @return H2客户端
     */
    public ShellH2Client client() {
        return this.parent().client();
    }

    /**
     * 获取数据库名称
     *
     * @return 数据库名称
     */
    public String dbName() {
        return this.parent().schema();
    }

    /**
     * 获取连接信息
     *
     * @return 连接信息
     */
    public ShellConnect info() {
        return this.parent().info();
    }

    /**
     * 获取连接名称
     *
     * @return 连接名称
     */
    public String infoName() {
        return this.parent().infoName();
    }

    // [已停用] 旧的数量查询实现（保留备用）
//    /**
//     * 获取表数量
//     *
//     * @return 表数量
//     */
//    public int tableSize() {
//        try {
//            return this.client().tableSize(this.dbName());
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return 0;
//    }

    /**
     * 获取表数量
     * <p>
     * 懒加载：仅在表列表加载完成后直接使用已加载的子节点数量，避免打开模式时执行数量查询导致卡顿。
     *
     * @return 表数量，未加载时返回null表示暂无数量
     */
    public Integer getTableSize() {
        // 未加载或加载中不显示数量，数量取自已加载的子节点，不访问数据库
        if (!this.isLoaded() || this.isLoading()) {
            return null;
        }
        return this.unfilteredChildrenSize();
    }

    // [已停用] 旧的缓存数量实现（保留备用）
//    /**
//     * 获取表数量（带缓存）
//     *
//     * @return 表数量
//     */
//    public synchronized int getTableSize() {
//        if (this.tableSize == null) {
//            this.tableSize = this.tableSize();
//        }
//        return this.tableSize;
//    }

    // [已停用] 旧的缓存清理实现（保留备用）
//    /**
//     * 清空表数量缓存
//     */
//    public void clearTableSize() {
//        this.tableSize = null;
//    }

    @Override
    public void loadChild() {
        if (this.isLoading() || this.isLoaded()) {
            return;
        }
        try {
            this.setLoading(true);
            this.clearChild();
            List<TreeItem<?>> items = new ArrayList<>();
            for (H2Table table : this.client().selectTablesSimple(this.dbName())) {
                items.add(new ShellH2TableTreeItem(table, this.getTreeView()));
            }
            this.setChild(items);
//        this.clearTableSize();
            this.setLoaded(true);
            this.doFilter();
            this.doSort();
            this.expend();
            this.refresh();
        } catch (Exception ex) {
            this.setLoaded(false);
            MessageBox.exception(ex, this.getWindow());
        } finally {
            this.setLoading(false);
        }
    }

    /**
     * 新增表并刷新排序
     *
     * @param table 表对象
     */
    public void addTable(H2Table table) {
        if (table == null || StringUtil.isBlank(table.getName())) {
            return;
        }
        for (TreeItem<?> child : this.unfilteredChildren()) {
            if (child instanceof ShellH2TableTreeItem item
                    && StringUtil.equals(item.tableName(), table.getName())) {
                this.refreshCount();
                return;
            }
        }
        this.addChild(new ShellH2TableTreeItem(table, this.getTreeView()));
        this.doFilter();
        this.doSort();
        this.expend();
        this.refreshCount();
    }

    /**
     * 刷新表数量
     */
    public void refreshCount() {
//        this.clearTableSize();
        this.refresh();
    }

    @Override
    public void reloadChild() {
//        this.clearTableSize();
        this.clearChild();
        this.setLoaded(false);
        this.loadChild();
    }

    @Override
    public void onPrimaryDoubleClick() {
        if (!this.isLoaded()) {
            this.loadChild();
        } else {
            super.onPrimaryDoubleClick();
        }
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        FXMenuItem add = MenuItemHelper.addTable(
                () -> ShellH2EventUtil.designTable(this.client(), this.dbName(), null));
        FXMenuItem reload = MenuItemHelper.reloadData(this::reloadChild);
        FXMenuItem exportData = MenuItemHelper.exportData(() -> ShellH2ViewFactory.exportData(this.client(), null));
        FXMenuItem importData = MenuItemHelper.importData(() -> ShellH2ViewFactory.importData(this.client()));
        items.add(add);
        items.add(reload);
        items.add(exportData);
        items.add(importData);
        return items;
    }
}
