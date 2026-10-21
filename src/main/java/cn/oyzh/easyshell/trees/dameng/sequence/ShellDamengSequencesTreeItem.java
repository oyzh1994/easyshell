package cn.oyzh.easyshell.trees.dameng.sequence;

import cn.oyzh.common.thread.Task;
import cn.oyzh.common.thread.TaskBuilder;
import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.easyshell.dameng.sequence.DamengSequence;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.dameng.ShellDamengEventUtil;
import cn.oyzh.easyshell.trees.dameng.ShellDamengTreeItem;
import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import javafx.collections.ObservableList;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 达梦数据库树序列类型节点
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellDamengSequencesTreeItem extends ShellDamengTreeItem<ShellDamengSequencesTreeItemValue> {

    /**
     * 构造达梦数据库树序列类型节点
     *
     * @param treeView 树视图
     */
    public ShellDamengSequencesTreeItem(RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.setValue(new ShellDamengSequencesTreeItemValue(this));
    }

    @Override
    public ShellDamengSchemaTreeItem parent() {
        return (ShellDamengSchemaTreeItem) super.parent();
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        FXMenuItem add = MenuItemHelper.addSequence(this::add);
        FXMenuItem reload = MenuItemHelper.refreshData(this::reloadChild);
        items.add(add);
        items.add(reload);
        return items;
    }

    /**
     * 新增序列
     */
    private void add() {
        DamengSequence Sequence = new DamengSequence();
        Sequence.setSchema(this.schema());
        ShellDamengEventUtil.designSequence(Sequence, this.parent());
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
                        List<DamengSequence> sequences = this.client().selectSequencesSimple(this.schema());
                        // 无数据直接更新列表
                        if (this.isChildEmpty()) {
                            List<TreeItem<?>> list = new ArrayList<>();
                            for (DamengSequence Sequence : sequences) {
                                list.add(new ShellDamengSequenceTreeItem(Sequence, this.getTreeView()));
                            }
                            this.setChild(list);
                        } else {// 有数据则执行删除、新增、更新操作
                            ObservableList children = this.richChildren();
                            ObservableList<ShellDamengSequenceTreeItem> list = children;
                            List<ShellDamengSequenceTreeItem> delList = new ArrayList<>();
                            List<ShellDamengSequenceTreeItem> addList = new ArrayList<>();
                            // 删除
                            for (ShellDamengSequenceTreeItem item : list) {
                                if (sequences.parallelStream().noneMatch(f -> f.compare(item.value()))) {
                                    delList.add(item);
                                }
                            }
                            // 新增
                            for (DamengSequence f : sequences) {
                                if (list.parallelStream().noneMatch(item -> f.compare(item.value()))) {
                                    addList.add(new ShellDamengSequenceTreeItem(f, this.getTreeView()));
                                }
                            }
                            // 更新
                            for (ShellDamengSequenceTreeItem item : list) {
                                if (!addList.contains(item) && !delList.contains(item)) {
                                    sequences.parallelStream().filter(f -> f.compare(item.value())).findFirst().ifPresent(f -> item.value().copy(f));
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

    @Override
    public void reloadChild() {
        this.clearSequenceSize();
        this.clearChild();
        this.setLoaded(false);
        this.loadChild();
    }

    /**
     * 获取模式名称
     *
     * @return 模式名称
     */
    public String schema() {
        return this.parent().schema();
    }

    /**
     * 获取达梦数据库客户端
     *
     * @return 达梦数据库客户端
     */
    public ShellDamengClient client() {
        return this.parent().client();
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

    @Override
    public void onPrimaryDoubleClick() {
        if (!this.isLoaded()) {
            this.loadChild();
        } else {
            super.onPrimaryDoubleClick();
        }
    }

    /**
     * 获取序列数量
     *
     * @return 序列数量
     */
    public int sequenceSize() {
        try {
            return this.client().sequenceSize(this.schema());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    /**
     * 序列数量
     */
    private Integer sequenceSize;

    /**
     * 获取序列数量
     *
     * @return 序列数量
     */
    public Integer getSequenceSize() {
        if (this.sequenceSize == null) {
            this.sequenceSize = this.sequenceSize();
        }
        return this.sequenceSize;
    }

    /**
     * 添加序列
     *
     * @param Sequence 序列
     */
    public void addSequence(DamengSequence Sequence) {
        this.addChild(new ShellDamengSequenceTreeItem(Sequence, this.getTreeView()));
        this.sortChild(this.isSortAsc());
        this.clearSequenceSize();
    }

    /**
     * 清空序列数量缓存
     */
    public void clearSequenceSize() {
        this.sequenceSize = null;
    }
}
