package cn.oyzh.easyshell.trees.mariadb.function;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.mariadb.ShellMariadbEventUtil;
import cn.oyzh.easyshell.mariadb.ShellMariadbClient;
import cn.oyzh.easyshell.mariadb.function.MariadbFunction;
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
 * MariaDB函数节点
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbFunctionTreeItem extends ShellMariadbTreeItem<ShellMariadbFunctionTreeItemValue> {

    /**
     * 当前值
     */
    private final MariadbFunction value;

    /**
     * 构造函数节点
     *
     * @param function 函数对象
     * @param treeView 树视图
     */
    public ShellMariadbFunctionTreeItem(MariadbFunction function, RichTreeView treeView) {
        super(treeView);
        this.value = function;
        super.setFilterable(true);
        this.setValue(new ShellMariadbFunctionTreeItemValue(this));
    }

    @Override
    public ShellMariadbFunctionsTreeItem parent() {
        return (ShellMariadbFunctionsTreeItem) super.parent();
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
        FXMenuItem design = MenuItemHelper.designFunction(this::onPrimaryDoubleClick);
        items.add(design);
        FXMenuItem renameFunction = MenuItemHelper.renameFunction(this::rename);
        items.add(renameFunction);
        FXMenuItem delete = MenuItemHelper.deleteFunction(this::delete);
        items.add(delete);
        items.add(MenuItemHelper.separator());
        FXMenuItem cloneFunction = MenuItemHelper.cloneFunction(this::cloneFunction);
        items.add(cloneFunction);
        FXMenuItem info = MenuItemHelper.functionInfo(this::functionInfo);
        items.add(info);
        return items;
    }

    /**
     * 查看函数信息
     */
    private void functionInfo() {
        ShellMariadbViewFactory.functionInfo(this);
    }

    /**
     * 克隆函数
     */
    private void cloneFunction() {
        StageManager.showMask(this::doCloneFunction);
    }

    /**
     * 执行克隆函数
     */
    private void doCloneFunction() {
        try {
            String cloneFunction = this.functionName() + DBUtil.genCloneName();
            this.dbItem().cloneFunction(this.functionName(), cloneFunction);
            MariadbFunction mariadbFunction = this.dbItem().selectFunction(cloneFunction);
            this.dbItem().getFunctionTypeChild().addFunction(mariadbFunction);
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    @Override
    public void delete() {
        if (!MessageBox.confirm(I18nHelper.deleteFunction() + " " + this.value.getName() + "?")) {
            return;
        }
        try {
            this.dbItem().dropFunction(this.value);
            ShellMariadbEventUtil.dropFunction(this);
            this.parent().clearFunctionSize();
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
        return this.parent().dbName();
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
        ShellMariadbEventUtil.designFunction(this.value, this.dbItem());
    }

    /**
     * 获取函数名称
     *
     * @return 函数名称
     */
    public String functionName() {
        return this.value.getName();
    }

    @Override
    public void reloadChild() {
        this.clearChild();
        this.setLoaded(false);
        this.loadChild();
    }

    @Override
    public void loadChild() {
        try {
            this.setLoaded(true);
            this.setLoading(true);
            MariadbFunction function = this.client().selectFunction(this.dbName(), this.functionName());
            if (function != null) {
                this.value.copy(function);
            }
        } catch (Exception ex) {
            this.setLoaded(false);
            ex.printStackTrace();
            MessageBox.exception(ex);
        } finally {
            this.setLoading(false);
        }
    }

//    @Override
//    public void onPrimarySingleClick() {
//        if (!this.isLoaded()) {
//            super.onPrimarySingleClick();
//        } else {
//            super.onPrimarySingleClick();
//        }
//    }

    /**
     * 获取函数对象
     *
     * @return 函数对象
     */
    public MariadbFunction value() {
        return value;
    }

    @Override
    public void rename() {
        try {
            String newName = MessageBox.prompt(I18nHelper.pleaseInputName(), this.functionName());
            // 名称为null或者跟当前名称相同，则忽略
            if (newName == null || Objects.equals(newName, this.functionName())) {
                return;
            }
            // 检查名称
            if (StringUtil.isBlank(newName)) {
                MessageBox.warn(I18nHelper.pleaseInputContent());
                return;
            }
            String oldName = this.functionName();
            // 修改名称
            this.dbItem().renameFunction(oldName, newName);
            this.value.setName(newName);
            this.refresh();
            ShellMariadbEventUtil.functionRenamed(oldName, newName, this.dbItem());
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }
}
