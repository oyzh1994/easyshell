package cn.oyzh.easyshell.trees.type;

import cn.oyzh.common.thread.Task;
import cn.oyzh.common.thread.TaskBuilder;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.db.type.ShellTypeTarget;
import cn.oyzh.easyshell.event.db.type.ShellTypeEventUtil;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.db.DBTypeDefinition;
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
 * 数据库类型（自定义类型）类型节点
 * <p>
 * 各支持自定义类型的数据库共用此节点，类型数据与操作由 {@link ShellTypeTarget} 提供。
 *
 * @author oyzh
 * @since 2026-11-28
 */
public class ShellTypesTreeItem extends RichTreeItem<ShellTypesTreeItemValue> {

    /**
     * 类型设计目标
     */
    private final ShellTypeTarget target;

    /**
     * 构造数据库类型节点
     *
     * @param treeView 树视图
     * @param target   类型设计目标
     */
    public ShellTypesTreeItem(RichTreeView treeView, ShellTypeTarget target) {
        super(treeView);
        super.setFilterable(true);
        this.target = target;
        this.setValue(new ShellTypesTreeItemValue(this));
    }

    /**
     * 获取类型设计目标
     *
     * @return 类型设计目标
     */
    public ShellTypeTarget target() {
        return target;
    }

    /**
     * 获取类型所属范围（模式名称）
     *
     * @return 范围名称
     */
    public String scope() {
        return this.target.typeScope();
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        FXMenuItem add = new FXMenuItem(new AddSVGGlyph(), ShellI18nHelper.addType(), this::add);
        items.add(add);
        FXMenuItem reload = MenuItemHelper.refreshData(this::reloadChild);
        items.add(reload);
        return items;
    }

    /**
     * 新增类型
     */
    private void add() {
        ShellTypeEventUtil.designType(this.target, this.target.newType());
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
                        List<? extends DBTypeDefinition> types = this.target.selectTypes();
                        // 无数据直接更新列表
                        if (this.isChildEmpty()) {
                            List<TreeItem<?>> list = new ArrayList<>();
                            for (DBTypeDefinition type : types) {
                                list.add(new ShellTypeTreeItem(type, this.getTreeView(), this.target));
                            }
                            this.setChild(list);
                        } else {// 有数据则执行删除、新增、更新操作
                            ObservableList children = this.richChildren();
                            ObservableList<ShellTypeTreeItem> list = children;
                            List<ShellTypeTreeItem> delList = new ArrayList<>();
                            List<ShellTypeTreeItem> addList = new ArrayList<>();
                            // 删除
                            for (ShellTypeTreeItem item : list) {
                                if (types.parallelStream().noneMatch(f -> this.isSame(f, item.value()))) {
                                    delList.add(item);
                                }
                            }
                            // 新增
                            for (DBTypeDefinition type : types) {
                                if (list.parallelStream().noneMatch(item -> this.isSame(type, item.value()))) {
                                    addList.add(new ShellTypeTreeItem(type, this.getTreeView(), this.target));
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
     * 是否为同一个类型
     *
     * @param type1 类型1
     * @param type2 类型2
     * @return 是否为同一个类型
     */
    private boolean isSame(DBTypeDefinition type1, DBTypeDefinition type2) {
        if (type1 == null || type2 == null) {
            return false;
        }
        return StringUtil.equalsIgnoreCase(type1.getName(), type2.getName());
    }

    @Override
    public void reloadChild() {
        this.clearChild();
        this.setLoaded(false);
        this.loadChild();
    }

    /**
     * 获取类型数量
     * <p>
     * 懒加载：仅在类型列表加载完成后直接使用已加载的子节点数量，避免打开模式时执行数量查询导致卡顿。
     *
     * @return 类型数量，未加载时返回null表示暂无数量
     */
    public Integer getTypeSize() {
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
