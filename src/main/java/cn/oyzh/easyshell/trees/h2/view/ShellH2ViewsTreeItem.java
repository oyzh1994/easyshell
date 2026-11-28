package cn.oyzh.easyshell.trees.h2.view;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.h2.ShellH2EventUtil;
import cn.oyzh.easyshell.h2.view.H2View;
import cn.oyzh.easyshell.h2.ShellH2Client;
import cn.oyzh.easyshell.trees.h2.ShellH2TreeItem;
import cn.oyzh.easyshell.trees.h2.schema.ShellH2SchemaTreeItem;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.List;

/**
 * H2视图类型节点
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellH2ViewsTreeItem extends ShellH2TreeItem<ShellH2ViewsTreeItemValue> {

    // [已停用] 旧的数量缓存字段（保留备用）
//    /**
//     * 视图数量缓存
//     */
//    private Integer viewSize;

    /**
     * 构造视图类型节点
     *
     * @param treeView 树视图
     */
    public ShellH2ViewsTreeItem(RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.setValue(new ShellH2ViewsTreeItemValue(this));
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
//     * 获取视图数量
//     *
//     * @return 视图数量
//     */
//    public int viewSize() {
//        try {
//            return this.client().viewSize(this.dbName());
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return 0;
//    }

    /**
     * 获取视图数量
     * <p>
     * 懒加载：仅在视图列表加载完成后直接使用已加载的子节点数量，避免打开模式时执行数量查询导致卡顿。
     *
     * @return 视图数量，未加载时返回null表示暂无数量
     */
    public Integer getViewSize() {
        // 未加载或加载中不显示数量，数量取自已加载的子节点，不访问数据库
        if (!this.isLoaded() || this.isLoading()) {
            return null;
        }
        return this.unfilteredChildrenSize();
    }

    // [已停用] 旧的缓存数量实现（保留备用）
//    /**
//     * 获取视图数量（带缓存）
//     *
//     * @return 视图数量
//     */
//    public synchronized int getViewSize() {
//        if (this.viewSize == null) {
//            this.viewSize = this.viewSize();
//        }
//        return this.viewSize;
//    }

    // [已停用] 旧的缓存清理实现（保留备用）
//    /**
//     * 清空视图数量缓存
//     */
//    public void clearViewSize() {
//        this.viewSize = null;
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
            for (H2View view : this.client().selectViewsSimple(this.dbName())) {
                items.add(new ShellH2ViewTreeItem(view, this.getTreeView()));
            }
            this.setChild(items);
//        this.clearViewSize();
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
     * 新增视图并刷新排序
     *
     * @param view 视图对象
     */
    public void addView(H2View view) {
        if (view == null || StringUtil.isBlank(view.getName())) {
            return;
        }
        for (TreeItem<?> child : this.unfilteredChildren()) {
            if (child instanceof ShellH2ViewTreeItem item
                    && StringUtil.equals(item.viewName(), view.getName())) {
                this.refreshCount();
                return;
            }
        }
        this.addChild(new ShellH2ViewTreeItem(view, this.getTreeView()));
        this.doFilter();
        this.doSort();
        this.expend();
        this.refreshCount();
    }

    /**
     * 刷新视图数量
     */
    public void refreshCount() {
//        this.clearViewSize();
        this.refresh();
    }

    @Override
    public void reloadChild() {
//        this.clearViewSize();
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
        FXMenuItem add = MenuItemHelper.addView(
                () -> ShellH2EventUtil.designView(this.client(), this.dbName(), null));
        FXMenuItem reload = MenuItemHelper.refreshData(this::reloadChild);
        items.add(add);
        items.add(reload);
        return items;
    }
}
