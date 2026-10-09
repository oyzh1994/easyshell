package cn.oyzh.easyshell.trees.mariadb.procedure;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.mariadb.ShellMariadbEventUtil;
import cn.oyzh.easyshell.mariadb.ShellMariadbClient;
import cn.oyzh.easyshell.mariadb.procedure.MariadbProcedure;
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
 * MariaDB过程节点
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbProcedureTreeItem extends ShellMariadbTreeItem<ShellMariadbProcedureTreeItemValue> {

    /**
     * 当前值
     */
    private final MariadbProcedure value;

    /**
     * 获取过程对象
     *
     * @return 过程对象
     */
    public MariadbProcedure value() {
        return value;
    }

    /**
     * 构造过程节点
     *
     * @param procedure 过程对象
     * @param treeView  树视图
     */
    public ShellMariadbProcedureTreeItem(MariadbProcedure procedure, RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.value = procedure;
        this.setValue(new ShellMariadbProcedureTreeItemValue(this));
    }

    @Override
    public ShellMariadbProceduresTreeItem parent() {
        return (ShellMariadbProceduresTreeItem) super.parent();
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
        FXMenuItem design = MenuItemHelper.designProcedure(this::onPrimaryDoubleClick);
        items.add(design);
        FXMenuItem renameProcedure = MenuItemHelper.renameProcedure(this::rename);
        items.add(renameProcedure);
        FXMenuItem delete = MenuItemHelper.deleteProcedure(this::delete);
        items.add(delete);
        items.add(MenuItemHelper.separator());
        FXMenuItem cloneProcedure = MenuItemHelper.cloneProcedure(this::cloneProcedure);
        items.add(cloneProcedure);
        FXMenuItem info = MenuItemHelper.procedureInfo(this::procedureInfo);
        items.add(info);
        return items;
    }

    /**
     * 查看过程信息
     */
    private void procedureInfo() {
        ShellMariadbViewFactory.procedureInfo(this);
    }

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
            String cloneProcedure = this.procedureName() + DBUtil.genCloneName();
            this.dbItem().cloneProcedure(this.procedureName(), cloneProcedure);
            MariadbProcedure mariadbProcedure = this.dbItem().selectProcedure(cloneProcedure);
            this.dbItem().getProcedureTypeChild().addProcedure(mariadbProcedure);
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
            this.dbItem().dropProcedure(this.value);
            ShellMariadbEventUtil.dropProcedure(this);
            this.parent().clearProcedureSize();
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
        ShellMariadbEventUtil.designProcedure(this.value, this.dbItem());
    }

    /**
     * 获取过程名称
     *
     * @return 过程名称
     */
    public String procedureName() {
        return this.value.getName();
    }

    @Override
    public void rename() {
        try {
            String newName = MessageBox.prompt(I18nHelper.pleaseInputName(), this.procedureName());
            // 名称为null或者跟当前名称相同，则忽略
            if (newName == null || Objects.equals(newName, this.procedureName())) {
                return;
            }
            // 检查名称
            if (StringUtil.isBlank(newName)) {
                MessageBox.warn(I18nHelper.pleaseInputContent());
                return;
            }
            String oldName = this.procedureName();
            // 修改名称
            this.value.setName(newName);
            this.dbItem().renameProcedure(oldName, newName);
            this.refresh();
            ShellMariadbEventUtil.procedureRenamed(oldName, newName, this.dbItem());
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }
}
