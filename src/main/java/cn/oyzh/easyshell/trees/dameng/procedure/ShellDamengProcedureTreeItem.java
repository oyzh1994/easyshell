package cn.oyzh.easyshell.trees.dameng.procedure;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.easyshell.dameng.procedure.DamengProcedure;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.dameng.ShellDamengEventUtil;
import cn.oyzh.easyshell.trees.dameng.ShellDamengTreeItem;
import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.easyshell.util.dameng.ShellDamengViewFactory;
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
 * db树视图节点
 *
 * @author oyzh
 * @since 2024/12/27
 */
public class ShellDamengProcedureTreeItem extends ShellDamengTreeItem<ShellDamengProcedureTreeItemValue> {

    /**
     * 当前值
     */
    private final DamengProcedure value;

    public DamengProcedure value() {
        return value;
    }

    public ShellDamengProcedureTreeItem(DamengProcedure procedure, RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.value = procedure;
        this.setValue(new ShellDamengProcedureTreeItemValue(this));
    }

    @Override
    public ShellDamengProceduresTreeItem parent() {
        return (ShellDamengProceduresTreeItem) super.parent();
    }

    /**
     * 获取db客户端
     *
     * @return db客户端
     */
    public ShellDamengClient client() {
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

    private void procedureInfo() {
        ShellDamengViewFactory.procedureInfo(this);
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
            DamengProcedure procedure = this.dbItem().selectProcedure(cloneProcedure);
            this.dbItem().getProcedureTypeChild().addProcedure(procedure);
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
            ShellDamengEventUtil.dropProcedure(this);
            this.parent().clearProcedureSize();
            super.remove();
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    public ShellDamengSchemaTreeItem dbItem() {
        return this.parent().parent();
    }

    public String schema() {
        return this.parent().schema();
    }

    public String infoName() {
        return this.parent().infoName();
    }

    @Override
    public void onPrimaryDoubleClick() {
        ShellDamengEventUtil.designProcedure(this.value, this.dbItem());
    }

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
            ShellDamengEventUtil.procedureRenamed(oldName, newName, this.dbItem());
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }
}
