package cn.oyzh.easyshell.trees.connect;

import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.event.connect.ShellConnectAddedEvent;
import cn.oyzh.easyshell.event.connect.ShellConnectUpdatedEvent;
import cn.oyzh.easyshell.event.data.ShellDataImportedEvent;
import cn.oyzh.easyshell.event.group.ShellAddGroupEvent;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.event.EventSubscribe;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.svg.glyph.database.ViewSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeCell;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.event.FXEventListener;
import cn.oyzh.fx.plus.menu.MenuItemAdapter;
import cn.oyzh.fx.plus.menu.MenuItemManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.List;

/**
 * shell连接树
 *
 * @author oyzh
 * @since 2025/03/10
 */
public class ShellConnectTreeView extends RichTreeView implements MenuItemAdapter, FXEventListener {

    // /**
    //  * shell分组储存
    //  */
    // private final ShellGroupStore groupStore = ShellGroupStore.INSTANCE;

    /**
     * shell设置
     */
    private final ShellSetting setting = ShellSettingStore.SETTING;

    /**
     * shell设置储存
     */
    private final ShellSettingStore settingStore = ShellSettingStore.INSTANCE;

    @Override
    protected void initTreeView() {
        this.dragContent = "shell_connect_tree_drag";
        this.setCellFactory((Callback<TreeView<?>, TreeCell<?>>) param -> new RichTreeCell<>());
        super.initTreeView();
    }

    @Override
    protected void initRoot() {
        super.setRoot(new ShellConnectRootTreeItem(this));
        this.root().expend();
        super.initRoot();
    }

    @Override
    protected void initEvenListener() {
        super.initEvenListener();
        // // 暂停按键处理
        // KeyListener.listenReleased(this, KeyCode.PAUSE, event -> {
        //     TreeItem<?> item = this.getSelectedItem();
        //     if (item instanceof ShellConnectTreeItem treeItem) {
        //         treeItem.closeConnect();
        //     }
        // });
    }

    @Override
    public ShellConnectRootTreeItem root() {
        return (ShellConnectRootTreeItem) super.root();
    }

    // /**
    //  * 关闭连接
    //  */
    // public void closeConnects() {
    //     for (ShellConnectTreeItem treeItem : this.root().getConnectedItems()) {
    //         ThreadUtil.startVirtual(() -> treeItem.closeConnect(false));
    //     }
    // }

//    @Override
//    public void expand() {
//        TreeItem<?> item = this.getSelectedItem();
//        if (item instanceof ShellConnectTreeItem treeItem) {
//            treeItem.expend();
//        } else if (item instanceof RichTreeItem<?> treeItem) {
//            treeItem.expend();
//        }
//        if (item != null) {
//            this.select(item);
//        }
//    }
//
//    @Override
//    public void collapse() {
//        TreeItem<?> item = this.getSelectedItem();
//        if (item instanceof ShellConnectTreeItem treeItem) {
//            treeItem.collapse();
//        } else if (item instanceof RichTreeItem<?> treeItem) {
//            treeItem.collapse();
//        }
//        if (item != null) {
//            this.select(item);
//        }
//    }

    /**
     * 添加分组
     *
     * @param event 事件
     */
    @EventSubscribe
    public void addGroup(ShellAddGroupEvent event) {
        // this.addGroup();
        this.root().addGroup();
    }

    /**
     * 连接新增事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void connectAdded(ShellConnectAddedEvent event) {
        this.root().connectAdded(event.data());
    }

    /**
     * 连接变更事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void connectUpdated(ShellConnectUpdatedEvent event) {
        this.root().connectUpdated(event.data());
    }

    /**
     * 连接已导入事件
     */
    @EventSubscribe
    private void connectImported(ShellDataImportedEvent event) {
        this.root().reloadChild();
    }

    @Override
    public ShellConnectTreeItemFilter getItemFilter() {
        // 初始化过滤器
        if (this.itemFilter == null) {
            this.itemFilter = new ShellConnectTreeItemFilter();
        }
        return (ShellConnectTreeItemFilter) this.itemFilter;
    }

    @Override
    public void setHighlightText(String highlightText) {
        super.setHighlightText(highlightText);
        this.getItemFilter().setKw(highlightText);
    }

    @Override
    public List<MenuItem> getMenuItems() {
        // TreeItem<?> item = this.getSelectedItem();
        List<MenuItem> items = new ArrayList<>(4);
        // // 添加
        // Menu add = MenuItemHelper.menu(I18nHelper.add(), new AddSVGGlyph("12"));
        // MenuItem addConnect = MenuItemHelper.menuItem(I18nHelper.connection(), this::addConnect);
        // MenuItem addGroup = MenuItemHelper.menuItem(I18nHelper.group(), this::addGroup);
        // add.getItems().add(addConnect);
        // add.getItems().add(addGroup);
        //
        // // 连接
        // Menu connection = MenuItemHelper.menu(I18nHelper.connection(), new ConnectionSVGGlyph("12"));
        // MenuItem exportConnect = MenuItemHelper.menuItem(I18nHelper.export(), this::exportConnect);
        // MenuItem importConnect = MenuItemHelper.menuItem(I18nHelper._import(), this::importConnect);
        // connection.getItems().add(exportConnect);
        // connection.getItems().add(importConnect);

        // 查看
        Menu view = MenuItemHelper.menu(I18nHelper.view1(), new ViewSVGGlyph("12"));
        MenuItem showType = MenuItemManager.getCheckMenuItem(I18nHelper.type(), this::showType, this.setting.isConnectShowType());
        MenuItem showMoreInfo = MenuItemManager.getCheckMenuItem(I18nHelper.moreInfo(), this::showMoreInfo, this.setting.isConnectShowMoreInfo());
//        CheckMenuItem showType = new CheckMenuItem(I18nHelper.type());
//        showType.setSelected(this.setting.isConnectShowType());
//        showType.setOnAction(event -> this.showType());
//        CheckMenuItem showMoreInfo = new CheckMenuItem(I18nHelper.moreInfo());
//        showMoreInfo.setSelected(this.setting.isConnectShowMoreInfo());
//        showMoreInfo.setOnAction(event -> this.showMoreInfo());
        view.getItems().add(showType);
        view.getItems().add(showMoreInfo);

        // items.add(add);
        // items.add(connection);
        items.add(view);
        //
        // if (item instanceof ShellConnectTreeItem) {
        //     add.setDisable(true);
        //     connection.setDisable(true);
        // } else if (item instanceof ShellConnectGroupTreeItem) {
        //     addGroup.setDisable(true);
        //     connection.setDisable(true);
        // }
        return items;
    }

    // /**
    //  * 初始化选择框
    //  *
    //  * @return 选择框
    //  */
    // private FXCheckBox initCheckBox() {
    //     FXCheckBox checkBox = new FXCheckBox(true);
    //     checkBox.setPrefHeight(10);
    //     checkBox.setFontSize(8);
    //     return checkBox;
    // }

    // /**
    //  * 添加连接
    //  */
    // private void addConnect() {
    //     TreeItem<?> item = this.getSelectedItem();
    //     if (item instanceof ShellConnectGroupTreeItem groupTreeItem) {
    //         ShellViewFactory.addGuid(groupTreeItem.value());
    //     } else {
    //         ShellViewFactory.addGuid(null);
    //     }
    // }

    // /**
    //  * 添加分组
    //  */
    // public void addGroup() {
    //     String groupName = MessageBox.prompt(I18nHelper.pleaseInputGroupName());
    //     // 名称为null，则忽略
    //     if (groupName == null) {
    //         return;
    //     }
    //     // 不能为空
    //     if (StringUtil.isBlank(groupName)) {
    //         MessageBox.warn(I18nHelper.nameCanNotEmpty());
    //         return;
    //     }
    //     // 检查是否存在
    //     if (this.groupStore.exist(groupName)) {
    //         MessageBox.warn(I18nHelper.contentAlreadyExists());
    //         return;
    //     }
    //     ShellGroup group = new ShellGroup();
    //     group.setName(groupName);
    //     if (this.groupStore.replace(group)) {
    //         this.root().addChild(new ShellConnectGroupTreeItem(group, this));
    //         ShellEventUtil.groupAdded(groupName);
    //     } else {
    //         MessageBox.warn(I18nHelper.operationFail());
    //     }
    // }
    //
    // /**
    //  * 导入连接
    //  */
    // private void importConnect() {
    //     ShellViewFactory.importConnect(null);
    // }

    /**
     * 显示类型
     */
    private void showType() {
        this.setting.setConnectShowType(!this.setting.isConnectShowType());
        this.settingStore.update(this.setting);
        this.refresh();
    }

    /**
     * 显示更多信息
     */
    private void showMoreInfo() {
        this.setting.setConnectShowMoreInfo(!this.setting.isConnectShowMoreInfo());
        this.settingStore.update(this.setting);
        this.refresh();
    }

    // /**
    //  * 导出连接
    //  */
    // private void exportConnect() {
    //     ShellViewFactory.exportConnect();
    // }

    public List<ShellConnectGroupTreeItem> getGroupItems() {
        return this.root().getGroupItems();
    }
}
