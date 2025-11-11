package cn.oyzh.easyshell.trees.mysql.event;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.mysql.ShellMysqlEventUtil;
import cn.oyzh.easyshell.mysql.ShellMysqlClient;
import cn.oyzh.easyshell.mysql.event.MysqlEvent;
import cn.oyzh.easyshell.trees.mysql.MysqlTreeItem;
import cn.oyzh.easyshell.trees.mysql.database.MysqlDatabaseTreeItem;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * db树事件节点
 *
 * @author oyzh
 * @since 2024/09/09
 */
public class MysqlEventTreeItem extends MysqlTreeItem<MysqlEventTreeItemValue> {

    /**
     * 当前值
     */
    private final MysqlEvent value;

    public MysqlEvent value() {
        return value;
    }

    public MysqlEventTreeItem(MysqlEvent event, RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.value = event;
        this.setValue(new MysqlEventTreeItemValue(this));
        // // 监听展开
        // super.addEventHandler(branchExpandedEvent(), (EventHandler<TreeModificationEvent<TreeItem<?>>>) e -> this.flushLocal());
    }

    @Override
    public MysqlEventsTreeItem parent(){
        return (MysqlEventsTreeItem) super.parent();
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
     * 获取redis信息
     *
     * @return redis信息
     */
    public ShellConnect info() {
        return this.parent().info();
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        // FXMenuItem open = MenuItemHelper.openEvent("12", this::onPrimaryDoubleClick);
        // items.add(open);
        FXMenuItem renameEvent = MenuItemHelper.renameEvent("12", this::rename);
        items.add(renameEvent);
        FXMenuItem design = MenuItemHelper.designEvent("12", this::onPrimaryDoubleClick);
        items.add(design);
        FXMenuItem delete = MenuItemHelper.deleteEvent("12", this::delete);
        items.add(delete);
        // FXMenuItem info = MenuItemHelper.eventInfo("12", this::eventInfo);
        // items.add(info);
        return items;
    }

    // private void eventInfo() {
    // }

    @Override
    public void delete() {
        if (!MessageBox.confirm(I18nHelper.deleteEvent() + " " + this.value.getName() + "?")) {
            return;
        }
        try {
            ShellMysqlEventUtil.dropEvent(this);
            this.dbItem().dropEvent(this.value);
            super.remove();
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    public MysqlDatabaseTreeItem dbItem() {
        return this.parent().parent();
    }

    public String dbName() {
        return parent().dbName();
    }

    public String infoName() {
        return parent().infoName();
    }

    @Override
    public void onPrimaryDoubleClick() {
        ShellMysqlEventUtil.designEvent(this.value, this.dbItem());
    }

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
            this.value.setName(newName);
            this.refresh();
            ShellMysqlEventUtil.eventRenamed(this, this.dbItem());
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }
}
