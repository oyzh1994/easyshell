package cn.oyzh.easyshell.trees.index;

import cn.oyzh.common.thread.Task;
import cn.oyzh.common.thread.TaskBuilder;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.db.index.ShellIndexTarget;
import cn.oyzh.easyshell.event.db.index.ShellIndexEventUtil;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.db.DBIndex;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.svg.glyph.AddSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import javafx.collections.ObservableList;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据库索引类型节点
 * <p>
 * 各数据库共用此节点，索引数据与操作由 {@link ShellIndexTarget} 提供。
 *
 * @author oyzh
 * @since 2026-11-28
 */
public class ShellIndexesTreeItem extends RichTreeItem<ShellIndexesTreeItemValue> {

    /**
     * 索引设计目标
     */
    private final ShellIndexTarget target;

    /**
     * 构造数据库索引类型节点
     *
     * @param treeView 树视图
     * @param target   索引设计目标
     */
    public ShellIndexesTreeItem(RichTreeView treeView, ShellIndexTarget target) {
        super(treeView);
        super.setFilterable(true);
        this.target = target;
        this.setValue(new ShellIndexesTreeItemValue(this));
    }

    /**
     * 获取索引设计目标
     *
     * @return 索引设计目标
     */
    public ShellIndexTarget target() {
        return target;
    }

    /**
     * 获取索引所属范围（模式名称或数据库名称）
     *
     * @return 范围名称
     */
    public String scope() {
        return this.target.indexScope();
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        FXMenuItem add = new FXMenuItem(new AddSVGGlyph(), ShellI18nHelper.addIndex(), this::add);
        items.add(add);
        FXMenuItem reload = MenuItemHelper.refreshData(this::reloadChild);
        items.add(reload);
        return items;
    }

    /**
     * 新增索引
     */
    private void add() {
        ShellIndexEventUtil.designIndex(this.target, this.target.newIndex());
    }

    @Override
    public boolean itemVisible() {
        return this.isVisible();
    }

    @Override
    public void loadChild() {
        if (!this.isWaiting() && !this.isLoaded() && !this.isLoading()) {
            this.setLoaded(true);
            this.setLoading(true);
            Task task = TaskBuilder.newBuilder()
                    .onStart(() -> {
                        List<? extends DBIndex> indexes = this.target.selectIndexes();
                        // 无数据直接更新列表
                        if (this.isChildEmpty()) {
                            List<TreeItem<?>> list = new ArrayList<>();
                            for (DBIndex index : indexes) {
                                list.add(new ShellIndexTreeItem(index, this.getTreeView(), this.target));
                            }
                            this.setChild(list);
                        } else {// 有数据则执行删除、新增、更新操作
                            ObservableList children = this.richChildren();
                            ObservableList<ShellIndexTreeItem> list = children;
                            List<ShellIndexTreeItem> delList = new ArrayList<>();
                            List<ShellIndexTreeItem> addList = new ArrayList<>();
                            // 删除
                            for (ShellIndexTreeItem item : list) {
                                if (indexes.parallelStream().noneMatch(f -> this.isSame(f, item.value()))) {
                                    delList.add(item);
                                }
                            }
                            // 新增
                            for (DBIndex index : indexes) {
                                if (list.parallelStream().noneMatch(item -> this.isSame(index, item.value()))) {
                                    addList.add(new ShellIndexTreeItem(index, this.getTreeView(), this.target));
                                }
                            }
                            list.removeAll(delList);
                            list.addAll(addList);
                        }
                    })
                    .onSuccess(this::expend)
                    .onError(ex -> {
                        this.setLoaded(false);
                        MessageBox.exception(ex, this.getWindow());
                    })
                    .onFinish(() -> {
                        this.setLoading(false);
                        this.doFilter();
                        this.doSort();
                    })
                    .build();
            // 执行业务
            this.startWaiting(task);
        }
    }

    /**
     * 是否为同一个索引
     *
     * @param index1 索引1
     * @param index2 索引2
     * @return 是否为同一个索引
     */
    private boolean isSame(DBIndex index1, DBIndex index2) {
        if (index1 == null || index2 == null) {
            return false;
        }
        return StringUtil.equalsIgnoreCase(index1.getName(), index2.getName())
                && StringUtil.equalsIgnoreCase(index1.getTableName(), index2.getTableName());
    }

    @Override
    public void reloadChild() {
        this.clearChild();
        this.setLoaded(false);
        this.loadChild();
    }

    /**
     * 获取索引数量
     * <p>
     * 懒加载：仅在索引列表加载完成后直接使用已加载的子节点数量，避免打开模式时执行数量查询导致卡顿。
     *
     * @return 索引数量，未加载时返回null表示暂无数量
     */
    public Integer getIndexSize() {
        // 未加载或加载中不显示数量，数量取自已加载的子节点，不访问数据库
        if (!this.isLoaded() || this.isLoading()) {
            return null;
        }
        return this.unfilteredChildrenSize();
    }

    @Override
    public void onPrimaryDoubleClick() {
        if (!this.isLoaded()) {
            this.loadChild();
        } else {
            super.onPrimaryDoubleClick();
        }
    }
}
