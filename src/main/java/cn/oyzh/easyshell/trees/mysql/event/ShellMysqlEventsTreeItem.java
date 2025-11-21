package cn.oyzh.easyshell.trees.mysql.event;

import cn.oyzh.common.thread.Task;
import cn.oyzh.common.thread.TaskBuilder;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.mysql.ShellMysqlEventUtil;
import cn.oyzh.easyshell.mysql.ShellMysqlClient;
import cn.oyzh.easyshell.mysql.event.MysqlEvent;
import cn.oyzh.easyshell.trees.mysql.ShellMysqlTreeItem;
import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeItemFilter;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2024/09/09
 */
public class ShellMysqlEventsTreeItem extends ShellMysqlTreeItem<ShellMysqlEventsTreeItemValue> {

    public ShellMysqlEventsTreeItem(RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.setValue(new ShellMysqlEventsTreeItemValue(this));
        super.unfilteredChildren().addListener((ListChangeListener<TreeItem<?>>) change -> {
            this.eventSize = null;
        });
    }

    @Override
    public ShellMysqlDatabaseTreeItem parent() {
        return (ShellMysqlDatabaseTreeItem) super.parent();
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        FXMenuItem add = MenuItemHelper.addEvent("12", this::add);
        items.add(add);
        FXMenuItem reload = MenuItemHelper.refreshData("12", this::reloadChild);
        items.add(reload);
        return items;
    }

    private void add() {
        MysqlEvent event = new MysqlEvent();
        event.setDbName(this.dbName());
        ShellMysqlEventUtil.designEvent(event, this.parent());
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
                        List<MysqlEvent> events = this.client().selectEvents(this.dbName());
                        // 无数据直接更新列表
                        if (this.isChildEmpty()) {
                            List<TreeItem<?>> list = new ArrayList<>();
                            for (MysqlEvent event : events) {
                                list.add(new ShellMysqlEventTreeItem(event, this.getTreeView()));
                            }
                            this.setChild(list);
                        } else {// 有数据则执行删除、新增、更新操作
                            ObservableList<ShellMysqlEventTreeItem> list = (ObservableList) this.richChildren();
                            List<ShellMysqlEventTreeItem> delList = new ArrayList<>();
                            List<ShellMysqlEventTreeItem> addList = new ArrayList<>();
                            // 删除
                            for (ShellMysqlEventTreeItem item : list) {
                                if (events.parallelStream().noneMatch(f -> f.compare(item.value()))) {
                                    delList.add(item);
                                }
                            }
                            // 新增
                            for (MysqlEvent f : events) {
                                if (list.parallelStream().noneMatch(item -> f.compare(item.value()))) {
                                    addList.add(new ShellMysqlEventTreeItem(f, this.getTreeView()));
                                }
                            }
                            // 更新
                            for (ShellMysqlEventTreeItem item : list) {
                                if (!addList.contains(item) && !delList.contains(item)) {
                                    events.parallelStream().filter(f -> f.compare(item.value())).findFirst().ifPresent(f -> item.value().copy(f));
                                }
                            }
                            list.removeAll(delList);
                            list.addAll(addList);
                        }
                        this.expend();
                    })
                    .onError(ex -> {
                        this.setLoaded(false);
                        MessageBox.exception(ex);
                    })
                    .onSuccess(this::refresh)
                    .onFinish(() -> this.setLoading(false))
                    .build();
            // 执行业务
            this.startWaiting(task);
        }
    }

    @Override
    public void reloadChild() {
        this.clearChild();
        this.setLoaded(false);
        this.loadChild();
    }

    public String dbName() {
        return this.parent().dbName();
    }

    public ShellMysqlClient client() {
        return this.parent().client();
    }

    public ShellConnect info() {
        return this.parent().info();
    }

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

    @Override
    public synchronized void doFilter(RichTreeItemFilter itemFilter) {
        super.doFilter(itemFilter);
        this.refresh();
    }

    public Integer eventSize() {
        return this.client().eventSize(this.dbName());
    }

    private Integer eventSize;

    public Integer getEventSize() {
        if (this.eventSize == null) {
            this.eventSize = this.eventSize();
        }
        return this.eventSize;
    }

    public void addEvent(MysqlEvent event) {
        this.addChild(new ShellMysqlEventTreeItem(event, this.getTreeView()));
        this.sortChild(this.isSortAsc());
    }
}
