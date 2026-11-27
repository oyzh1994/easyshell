package cn.oyzh.easyshell.trees.trigger;

import cn.oyzh.common.thread.Task;
import cn.oyzh.common.thread.TaskBuilder;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.db.DBTrigger;
import cn.oyzh.easyshell.db.trigger.ShellTriggerTarget;
import cn.oyzh.easyshell.event.db.trigger.ShellTriggerEventUtil;
import cn.oyzh.easyshell.util.ShellI18nHelper;
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
 * 数据库触发器类型节点
 * <p>
 * 各数据库共用此节点，触发器数据与操作由 {@link ShellTriggerTarget} 提供。
 *
 * @author oyzh
 * @since 2026-11-28
 */
public class ShellTriggersTreeItem extends RichTreeItem<ShellTriggersTreeItemValue> {

    /**
     * 触发器设计目标
     */
    private final ShellTriggerTarget target;

    /**
     * 构造数据库触发器类型节点
     *
     * @param treeView 树视图
     * @param target   触发器设计目标
     */
    public ShellTriggersTreeItem(RichTreeView treeView, ShellTriggerTarget target) {
        super(treeView);
        super.setFilterable(true);
        this.target = target;
        this.setValue(new ShellTriggersTreeItemValue(this));
    }

    /**
     * 获取触发器设计目标
     *
     * @return 触发器设计目标
     */
    public ShellTriggerTarget target() {
        return target;
    }

    /**
     * 获取触发器所属范围（模式名称或数据库名称）
     *
     * @return 范围名称
     */
    public String scope() {
        return this.target.triggerScope();
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        FXMenuItem add = new FXMenuItem(new AddSVGGlyph(), ShellI18nHelper.addTrigger(), this::add);
        items.add(add);
        FXMenuItem reload = MenuItemHelper.refreshData(this::reloadChild);
        items.add(reload);
        return items;
    }

    /**
     * 新增触发器
     */
    private void add() {
        ShellTriggerEventUtil.designTrigger(this.target, this.target.newTrigger());
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
                        List<? extends DBTrigger> triggers = this.target.selectTriggers();
                        // 无数据直接更新列表
                        if (this.isChildEmpty()) {
                            List<TreeItem<?>> list = new ArrayList<>();
                            for (DBTrigger trigger : triggers) {
                                list.add(new ShellTriggerTreeItem(trigger, this.getTreeView(), this.target));
                            }
                            this.setChild(list);
                        } else {// 有数据则执行删除、新增、更新操作
                            ObservableList children = this.richChildren();
                            ObservableList<ShellTriggerTreeItem> list = children;
                            List<ShellTriggerTreeItem> delList = new ArrayList<>();
                            List<ShellTriggerTreeItem> addList = new ArrayList<>();
                            // 删除
                            for (ShellTriggerTreeItem item : list) {
                                if (triggers.parallelStream().noneMatch(f -> this.isSame(f, item.value()))) {
                                    delList.add(item);
                                }
                            }
                            // 新增
                            for (DBTrigger trigger : triggers) {
                                if (list.parallelStream().noneMatch(item -> this.isSame(trigger, item.value()))) {
                                    addList.add(new ShellTriggerTreeItem(trigger, this.getTreeView(), this.target));
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
     * 是否为同一个触发器
     *
     * @param trigger1 触发器1
     * @param trigger2 触发器2
     * @return 是否为同一个触发器
     */
    private boolean isSame(DBTrigger trigger1, DBTrigger trigger2) {
        if (trigger1 == null || trigger2 == null) {
            return false;
        }
        return StringUtil.equalsIgnoreCase(trigger1.getName(), trigger2.getName())
                && StringUtil.equalsIgnoreCase(trigger1.getTableName(), trigger2.getTableName());
    }

    @Override
    public void reloadChild() {
        this.clearChild();
        this.setLoaded(false);
        this.loadChild();
    }

    /**
     * 获取触发器数量
     * <p>
     * 懒加载：仅在触发器列表加载完成后直接使用已加载的子节点数量，避免打开模式时执行数量查询导致卡顿。
     *
     * @return 触发器数量，未加载时返回null表示暂无数量
     */
    public Integer getTriggerSize() {
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
