package cn.oyzh.easyshell.trees.postgresql.function;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.postgresql.ShellPostgresqlClient;
import cn.oyzh.easyshell.postgresql.function.PostgresqlFunction;
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
 * PostgreSQL数据库树函数节点
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellPostgresqlFunctionTreeItem extends ShellPostgresqlTreeItem<ShellPostgresqlFunctionTreeItemValue> {

    /**
     * 当前值
     */
    private final PostgresqlFunction value;

    /**
     * 构造PostgreSQL数据库树函数节点
     *
     * @param function 函数
     * @param treeView 树视图
     */
    public ShellPostgresqlFunctionTreeItem(PostgresqlFunction function, RichTreeView treeView) {
        super(treeView);
        this.value = function;
        super.setFilterable(true);
        this.setValue(new ShellPostgresqlFunctionTreeItemValue(this));
    }

    @Override
    public ShellPostgresqlFunctionsTreeItem parent() {
        return (ShellPostgresqlFunctionsTreeItem) super.parent();
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
     * 函数信息
     */
    private void functionInfo() {
        ShellPostgresqlViewFactory.functionInfo(this);
    }

    /**
     * 克隆函数
     */
    private void cloneFunction() {
        StageManager.showMask(this.getWindow(), this::doCloneFunction);
    }

    /**
     * 执行克隆函数
     */
    private void doCloneFunction() {
        try {
            String cloneFunction = this.functionName() + DBUtil.genCloneName();
            this.dbItem().cloneFunction(this.functionName(), cloneFunction);
            PostgresqlFunction function = this.dbItem().selectFunction(cloneFunction);
            this.dbItem().getFunctionTypeChild().addFunction(function);
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    @Override
    public void delete() {
        if (!MessageBox.confirm(ShellI18nHelper.confirm("deleteFunction", this.value.getName()), this.getWindow())) {
            return;
        }
        try {
            this.dbItem().dropFunction(this.value);
            ShellPostgresqlEventUtil.dropFunction(this);
            this.parent().clearFunctionSize();
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
        ShellPostgresqlEventUtil.designFunction(this.value, this.dbItem());
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
            PostgresqlFunction function = this.client().selectFunction(this.schema(), this.functionName());
            if (function != null) {
                this.value.copy(function);
            }
        } catch (Exception ex) {
            this.setLoaded(false);
            ex.printStackTrace();
            MessageBox.exception(ex, this.getWindow());
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

    /**
     * 获取函数值
     *
     * @return 函数值
     */
    public PostgresqlFunction value() {
        return value;
    }

    @Override
    public void rename() {
        try {
            String newName = MessageBox.prompt(I18nHelper.pleaseInputName(), this.functionName(), this.getWindow());
            // 名称为null或者跟当前名称相同，则忽略
            if (newName == null || Objects.equals(newName, this.functionName())) {
                return;
            }
            // 检查名称
            if (StringUtil.isBlank(newName)) {
                MessageBox.warn(I18nHelper.pleaseInputContent(), this.getWindow());
                return;
            }
            String oldName = this.functionName();
            // 修改名称
            this.dbItem().renameFunction(oldName, newName);
            this.value.setName(newName);
            this.refresh();
            ShellPostgresqlEventUtil.functionRenamed(oldName, newName, this.dbItem());
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, this.getWindow());
        }
    }
}
