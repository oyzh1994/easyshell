package cn.oyzh.easyshell.trees.postgresql.procedure;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.postgresql.ShellPostgresqlClient;
import cn.oyzh.easyshell.postgresql.procedure.PostgresqlProcedure;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.postgresql.ShellPostgresqlEventUtil;
import cn.oyzh.easyshell.trees.postgresql.ShellPostgresqlTreeItem;
import cn.oyzh.easyshell.trees.postgresql.schema.ShellPostgresqlSchemaTreeItem;
import cn.oyzh.easyshell.util.postgresql.ShellPostgresqlViewFactory;
import cn.oyzh.fx.db.util.DBUtil;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * PostgreSQL数据库树过程节点
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellPostgresqlProcedureTreeItem extends ShellPostgresqlTreeItem<ShellPostgresqlProcedureTreeItemValue> {

    /**
     * 当前值
     */
    private final PostgresqlProcedure value;

    /**
     * 获取过程值
     *
     * @return 过程值
     */
    public PostgresqlProcedure value() {
        return value;
    }

    /**
     * 构造PostgreSQL数据库树过程节点
     *
     * @param procedure 过程
     * @param treeView  树视图
     */
    public ShellPostgresqlProcedureTreeItem(PostgresqlProcedure procedure, RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.value = procedure;
        this.setValue(new ShellPostgresqlProcedureTreeItemValue(this));
    }

    @Override
    public ShellPostgresqlProceduresTreeItem parent() {
        return (ShellPostgresqlProceduresTreeItem) super.parent();
    }

    /**
     * 获取db客户端
     *
     * @return db客户端
     */
    public ShellPostgresqlClient client() {
        return this.parent().client();
    }

    /**
     * 获取连接信息
     *
     * @return 连接信息
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
     * 过程信息
     */
    private void procedureInfo() {
        ShellPostgresqlViewFactory.procedureInfo(this);
    }

    /**
     * 克隆过程
     */
    private void cloneProcedure() {
        StageManager.showMask(this.getWindow(), this::doCloneProcedure);
    }

    /**
     * 执行克隆过程
     */
    private void doCloneProcedure() {
        try {
            String cloneProcedure = this.procedureName() + DBUtil.genCloneName();
            this.dbItem().cloneProcedure(this.procedureName(), cloneProcedure);
            PostgresqlProcedure procedure = this.dbItem().selectProcedure(cloneProcedure);
            this.dbItem().getProcedureTypeChild().addProcedure(procedure);
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    @Override
    public void delete() {
        if (!MessageBox.confirm(ShellI18nHelper.confirm("deleteProcedure", this.value.getName()), this.getWindow())) {
            return;
        }
        try {
            this.dbItem().dropProcedure(this.value);
            ShellPostgresqlEventUtil.dropProcedure(this);
            this.parent().clearProcedureSize();
            super.remove();
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    /**
     * 获取所属模式节点
     *
     * @return 模式节点
     */
    public ShellPostgresqlSchemaTreeItem dbItem() {
        return this.parent().parent();
    }

    /**
     * 获取模式名称
     *
     * @return 模式名称
     */
    public String schema() {
        return this.parent().schema();
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
        ShellPostgresqlEventUtil.designProcedure(this.value, this.dbItem());
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
            String newName = MessageBox.prompt(I18nHelper.pleaseInputName(), this.procedureName(), this.getWindow());
            // 名称为null或者跟当前名称相同，则忽略
            if (newName == null || Objects.equals(newName, this.procedureName())) {
                return;
            }
            // 检查名称
            if (StringUtil.isBlank(newName)) {
                MessageBox.warn(I18nHelper.pleaseInputContent(), this.getWindow());
                return;
            }
            String oldName = this.procedureName();
            // 修改名称
            this.value.setName(newName);
            this.dbItem().renameProcedure(oldName, newName);
            this.refresh();
            ShellPostgresqlEventUtil.procedureRenamed(oldName, newName, this.dbItem());
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, this.getWindow());
        }
    }
}
