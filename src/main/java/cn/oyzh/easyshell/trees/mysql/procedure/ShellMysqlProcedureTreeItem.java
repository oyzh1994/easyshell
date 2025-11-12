package cn.oyzh.easyshell.trees.mysql.procedure;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.mysql.ShellMysqlEventUtil;
import cn.oyzh.easyshell.mysql.ShellMysqlClient;
import cn.oyzh.easyshell.mysql.procedure.MysqlProcedure;
import cn.oyzh.easyshell.trees.mysql.ShellMysqlTreeItem;
import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.easyshell.util.mysql.ShellMysqlUtil;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.List;

/**
 * db树视图节点
 *
 * @author oyzh
 * @since 2024/12/27
 */
public class ShellMysqlProcedureTreeItem extends ShellMysqlTreeItem<ShellMysqlProcedureTreeItemValue> {

    /**
     * 当前值
     */
    private final MysqlProcedure value;

    public MysqlProcedure value() {
        return value;
    }

    public ShellMysqlProcedureTreeItem(MysqlProcedure procedure, RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.value = procedure;
        this.setValue(new ShellMysqlProcedureTreeItemValue(this));
    }

    @Override
    public ShellMysqlProceduresTreeItem parent() {
        return (ShellMysqlProceduresTreeItem) super.parent();
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
        // FXMenuItem open = MenuItemHelper.openProcedure("12", this::onPrimaryDoubleClick);
        // items.add(open);
        FXMenuItem design = MenuItemHelper.designProcedure("12", this::onPrimaryDoubleClick);
        items.add(design);
        FXMenuItem delete = MenuItemHelper.deleteProcedure("12", this::delete);
        items.add(delete);
        // FXMenuItem info = MenuItemHelper.procedureInfo("12", this::procedureInfo);
        // items.add(info);
        items.add(MenuItemHelper.separator());
        FXMenuItem cloneProcedure = MenuItemHelper.cloneProcedure("12", this::cloneProcedure);
        items.add(cloneProcedure);
        return items;
    }

    // private void procedureInfo() {
    // }

    /**
     * 克隆过程
     */
    private void cloneProcedure() {
        StageManager.showMask(this::doCloneProcedure);
    }

    /**
     * 执行克隆过程
     */
    private void doCloneProcedure() {
        try {
            String cloneProcedure = this.procedureName() + ShellMysqlUtil.genCloneName();
            this.dbItem().cloneProcedure(this.procedureName(), cloneProcedure);
            MysqlProcedure mysqlProcedure = this.dbItem().selectProcedure(cloneProcedure);
            this.dbItem().getProcedureTypeChild().addProcedure(mysqlProcedure);
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    @Override
    public void delete() {
        if (!MessageBox.confirm(I18nHelper.deleteProcedure() + " " + this.value.getName() + "?")) {
            return;
        }
        try {
            ShellMysqlEventUtil.dropProcedure(this);
            this.dbItem().dropProcedure(this.value);
            super.remove();
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    public ShellMysqlDatabaseTreeItem dbItem() {
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
        ShellMysqlEventUtil.designProcedure(this.value, this.dbItem());
    }

    public String procedureName() {
        return this.value.getName();
    }
}
