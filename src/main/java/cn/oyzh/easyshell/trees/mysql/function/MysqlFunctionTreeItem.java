package cn.oyzh.easyshell.trees.mysql.function;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.mysql.MysqlEventUtil;
import cn.oyzh.easyshell.mysql.ShellMysqlClient;
import cn.oyzh.easyshell.mysql.function.MysqlFunction;
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

/**
 * db树函数节点
 *
 * @author oyzh
 * @since 2024/06/29
 */
public class MysqlFunctionTreeItem extends MysqlTreeItem<MysqlFunctionTreeItemValue> {

    /**
     * 当前值
     */
    private final MysqlFunction value;

    public MysqlFunctionTreeItem(MysqlFunction function, RichTreeView treeView) {
        super(treeView);
        this.value = function;
        super.setFilterable(true);
        this.setValue(new MysqlFunctionTreeItemValue(this));
    }

    @Override
    public MysqlFunctionsTreeItem parent() {
        return (MysqlFunctionsTreeItem) super.parent();
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
        // FXMenuItem open = MenuItemHelper.openFunction("12", this::onPrimaryDoubleClick);
        // items.add(open);
        FXMenuItem design = MenuItemHelper.designFunction("12", this::onPrimaryDoubleClick);
        items.add(design);
        FXMenuItem delete = MenuItemHelper.deleteFunction("12", this::delete);
        items.add(delete);
        // FXMenuItem info = MenuItemHelper.functionInfo("12", this::functionInfo);
        // items.add(info);
        return items;
    }

    // private void functionInfo() {
    // }

    @Override
    public void delete() {
        if (!MessageBox.confirm(I18nHelper.deleteFunction() + " " + this.value.getName() + "?")) {
            return;
        }
        try {
            MysqlEventUtil.dropFunction(this);
            this.dbItem().dropFunction(this.value);
            super.remove();
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    public MysqlDatabaseTreeItem dbItem() {
        return this.parent().parent();
    }

    public String dbName() {
        return this.parent().dbName();
    }

    public String infoName() {
        return this.parent().infoName();
    }

    @Override
    public void onPrimaryDoubleClick() {
        MysqlEventUtil.designFunction(this.value, this.dbItem());
    }

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
            MysqlFunction function = this.client().selectFunction(this.dbName(), this.functionName());
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

    @Override
    public void onPrimarySingleClick() {
        if (!this.isLoaded()) {
            super.onPrimarySingleClick();
        } else {
            super.onPrimarySingleClick();
        }
    }

    public MysqlFunction value() {
        return value;
    }
}
