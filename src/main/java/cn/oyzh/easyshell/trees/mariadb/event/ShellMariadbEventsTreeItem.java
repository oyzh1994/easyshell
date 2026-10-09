package cn.oyzh.easyshell.trees.mariadb.event;

import cn.oyzh.common.thread.Task;
import cn.oyzh.common.thread.TaskBuilder;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.mariadb.ShellMariadbEventUtil;
import cn.oyzh.easyshell.mariadb.ShellMariadbClient;
import cn.oyzh.easyshell.mariadb.event.MariadbEvent;
import cn.oyzh.easyshell.trees.mariadb.ShellMariadbTreeItem;
import cn.oyzh.easyshell.trees.mariadb.database.ShellMariadbDatabaseTreeItem;
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
 * MariaDB事件类型节点
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbEventsTreeItem extends ShellMariadbTreeItem<ShellMariadbEventsTreeItemValue> {

    /**
     * 构造事件类型节点
     *
     * @param treeView 树视图
     */
    public ShellMariadbEventsTreeItem(RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.setValue(new ShellMariadbEventsTreeItemValue(this));
    }

    @Override
    public ShellMariadbDatabaseTreeItem parent() {
        return (ShellMariadbDatabaseTreeItem) super.parent();
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        FXMenuItem add = MenuItemHelper.addEvent(this::add);
        items.add(add);
        FXMenuItem reload = MenuItemHelper.refreshData(this::reloadChild);
        items.add(reload);
        return items;
    }

    /**
     * 新增事件
     */
    private void add() {
        MariadbEvent event = new MariadbEvent();
        event.setDbName(this.dbName());
        ShellMariadbEventUtil.designEvent(event, this.parent());
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
                        List<MariadbEvent> events = this.client().selectEventsSimple(this.dbName());
                        // 无数据直接更新列表
                        if (this.isChildEmpty()) {
                            List<TreeItem<?>> list = new ArrayList<>();
                            for (MariadbEvent event : events) {
                                list.add(new ShellMariadbEventTreeItem(event, this.getTreeView()));
                            }
                            this.setChild(list);
                        } else {// 有数据则执行删除、新增、更新操作
                            ObservableList<ShellMariadbEventTreeItem> list = (ObservableList) this.richChildren();
                            List<ShellMariadbEventTreeItem> delList = new ArrayList<>();
                            List<ShellMariadbEventTreeItem> addList = new ArrayList<>();
                            // 删除
                            for (ShellMariadbEventTreeItem item : list) {
                                if (events.parallelStream().noneMatch(f -> f.compare(item.value()))) {
                                    delList.add(item);
                                }
                            }
                            // 新增
                            for (MariadbEvent f : events) {
                                if (list.parallelStream().noneMatch(item -> f.compare(item.value()))) {
                                    addList.add(new ShellMariadbEventTreeItem(f, this.getTreeView()));
                                }
                            }
                            // 更新
                            for (ShellMariadbEventTreeItem item : list) {
                                if (!addList.contains(item) && !delList.contains(item)) {
                                    events.parallelStream().filter(f -> f.compare(item.value())).findFirst().ifPresent(f -> item.value().copy(f));
                                }
                            }
                            list.removeAll(delList);
                            list.addAll(addList);
                        }
                    })
                    .onSuccess(this::expend)
                    .onError(ex -> {
                        this.setLoaded(false);
                        MessageBox.exception(ex);
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
        this.clearEventSize();;
        this.clearChild();
        this.setLoaded(false);
        this.loadChild();
    }

    /**
     * 获取数据库名称
     *
     * @return 数据库名称
     */
    public String dbName() {
        return this.parent().dbName();
    }

    /**
     * 获取db客户端
     *
     * @return db客户端
     */
    public ShellMariadbClient client() {
        return this.parent().client();
    }

    /**
     * 获取MariaDB信息
     *
     * @return MariaDB信息
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
     * 获取事件数量
     *
     * @return 事件数量
     */
    public int eventSize() {
        try {
            return this.client().eventSize(this.dbName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    /**
     * 事件数量缓存
     */
    private Integer eventSize;

    /**
     * 获取事件数量（带缓存）
     *
     * @return 事件数量
     */
    public Integer getEventSize() {
        if (this.eventSize == null) {
            this.eventSize = this.eventSize();
        }
        return this.eventSize;
    }

    /**
     * 新增事件并刷新排序
     *
     * @param event 事件对象
     */
    public void addEvent(MariadbEvent event) {
        this.addChild(new ShellMariadbEventTreeItem(event, this.getTreeView()));
        this.sortChild(this.isSortAsc());
        this.clearEventSize();
    }

    /**
     * 清空事件数量缓存
     */
    public void clearEventSize() {
        this.eventSize = null;
    }
}
