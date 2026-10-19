package cn.oyzh.easyshell.trees.mariadb.event;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.mariadb.ShellMariadbEventUtil;
import cn.oyzh.easyshell.mariadb.ShellMariadbClient;
import cn.oyzh.easyshell.mariadb.event.MariadbEvent;
import cn.oyzh.easyshell.trees.mariadb.ShellMariadbTreeItem;
import cn.oyzh.easyshell.trees.mariadb.database.ShellMariadbDatabaseTreeItem;
import cn.oyzh.easyshell.util.mariadb.ShellMariadbViewFactory;
import cn.oyzh.fx.db.util.DBUtil;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * MariaDB事件节点
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbEventTreeItem extends ShellMariadbTreeItem<ShellMariadbEventTreeItemValue> {

    /**
     * 当前值
     */
    private final MariadbEvent value;

    /**
     * 获取事件对象
     *
     * @return 事件对象
     */
    public MariadbEvent value() {
        return value;
    }

    /**
     * 构造事件节点
     *
     * @param event    事件对象
     * @param treeView 树视图
     */
    public ShellMariadbEventTreeItem(MariadbEvent event, RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.value = event;
        this.setValue(new ShellMariadbEventTreeItemValue(this));
        // // 监听展开
        // super.addEventHandler(branchExpandedEvent(), (EventHandler<TreeModificationEvent<TreeItem<?>>>) e -> this.flushLocal());
    }

    @Override
    public ShellMariadbEventsTreeItem parent() {
        return (ShellMariadbEventsTreeItem) super.parent();
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

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        // FXMenuItem open = MenuItemHelper.openEvent( this::onPrimaryDoubleClick);
        // items.add(open);
        FXMenuItem design = MenuItemHelper.designEvent(this::onPrimaryDoubleClick);
        items.add(design);
        FXMenuItem renameEvent = MenuItemHelper.renameEvent(this::rename);
        items.add(renameEvent);
        FXMenuItem delete = MenuItemHelper.deleteEvent(this::delete);
        items.add(delete);
        items.add(MenuItemHelper.separator());
        FXMenuItem cloneEvent = MenuItemHelper.cloneEvent(this::cloneEvent);
        items.add(cloneEvent);
        FXMenuItem info = MenuItemHelper.eventInfo(this::eventInfo);
        items.add(info);
        return items;
    }

    /**
     * 查看事件信息
     */
    private void eventInfo() {
        ShellMariadbViewFactory.eventInfo(this);
    }

    /**
     * 克隆事件
     */
    private void cloneEvent() {
        StageManager.showMask(this::doCloneEvent);
    }

    /**
     * 执行克隆事件
     */
    private void doCloneEvent() {
        try {
            String cloneEvent = this.eventName() + DBUtil.genCloneName();
            this.dbItem().cloneEvent(this.eventName(), cloneEvent);
            MariadbEvent mariadbEvent = this.dbItem().selectEvent(cloneEvent);
            this.dbItem().getEventTypeChild().addEvent(mariadbEvent);
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }


    @Override
    public void delete() {
        if (!MessageBox.confirm(I18nHelper.deleteEvent() + " " + this.value.getName() + "?")) {
            return;
        }
        try {
            this.dbItem().dropEvent(this.value);
            ShellMariadbEventUtil.dropEvent(this);
            this.parent().clearEventSize();
            super.remove();
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    /**
     * 获取所属数据库节点
     *
     * @return 数据库节点
     */
    public ShellMariadbDatabaseTreeItem dbItem() {
        return this.parent().parent();
    }

    /**
     * 获取数据库名称
     *
     * @return 数据库名称
     */
    public String dbName() {
        return parent().dbName();
    }

    /**
     * 获取连接名称
     *
     * @return 连接名称
     */
    public String infoName() {
        return parent().infoName();
    }

    @Override
    public void onPrimaryDoubleClick() {
        ShellMariadbEventUtil.designEvent(this.value, this.dbItem());
    }

    /**
     * 获取事件名称
     *
     * @return 事件名称
     */
    public String eventName() {
        return this.value.getName();
    }

    @Override
    public void rename() {
        try {
            String newName = MessageBox.prompt(I18nHelper.pleaseInputName(), this.eventName());
            // 名称为null或者跟当前名称相同，则忽略
            if (newName == null || Objects.equals(newName, this.eventName())) {
                return;
            }
            // 检查名称
            if (StringUtil.isBlank(newName)) {
                MessageBox.warn(I18nHelper.pleaseInputContent());
                return;
            }
            String oldName = this.eventName();
            // 修改名称
            this.dbItem().renameEvent(oldName, newName);
            ShellMariadbEventUtil.eventRenamed(oldName, newName, this.dbItem());
            this.value.setName(newName);
            this.refresh();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }
}
