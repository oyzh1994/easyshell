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
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import javafx.collections.ObservableList;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.List;

/**
 * mysql事件类型节点
 *
 * @author oyzh
 * @since 2024/09/09
 */
public class ShellMysqlEventsTreeItem extends ShellMysqlTreeItem<ShellMysqlEventsTreeItemValue> {

    /**
     * 构造事件类型节点
     *
     * @param treeView 树视图
     */
    public ShellMysqlEventsTreeItem(RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.setValue(new ShellMysqlEventsTreeItemValue(this));
    }

    @Override
    public ShellMysqlDatabaseTreeItem parent() {
        return (ShellMysqlDatabaseTreeItem) super.parent();
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
                        List<MysqlEvent> events = this.client().selectEventsSimple(this.dbName());
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
    public ShellMysqlClient client() {
        return this.parent().client();
    }

    /**
     * 获取mysql信息
     *
     * @return mysql信息
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
    public void addEvent(MysqlEvent event) {
        this.addChild(new ShellMysqlEventTreeItem(event, this.getTreeView()));
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
