package cn.oyzh.easyshell.trees.dameng.procedure;

import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.easyshell.dameng.procedure.DamengProcedure;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.dameng.DamengEventUtil;
import cn.oyzh.easyshell.trees.dameng.DBTreeItem;
import cn.oyzh.easyshell.trees.dameng.schema.DamengSchemaTreeItem;
import cn.oyzh.easyshell.util.dameng.ShellDamengViewFactory;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
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
public class DamengProcedureTreeItem extends DBTreeItem<DamengProcedureTreeItemValue> {

    /**
     * 当前值
     */
    private final DamengProcedure value;

    public DamengProcedure value() {
        return value;
    }

    public DamengProcedureTreeItem(DamengProcedure procedure, RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.value = procedure;
        this.setValue(new DamengProcedureTreeItemValue(this));
    }

    @Override
    public DamengProceduresTreeItem parent() {
        return (DamengProceduresTreeItem) super.parent();
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
        // FXMenuItem open = MenuItemHelper.openProcedure( this::onPrimaryDoubleClick);
        // items.add(open);
        FXMenuItem design = MenuItemHelper.designProcedure(this::onPrimaryDoubleClick);
        items.add(design);
        FXMenuItem delete = MenuItemHelper.deleteProcedure(this::delete);
        items.add(delete);
        FXMenuItem info = MenuItemHelper.procedureInfo(this::procedureInfo);
        items.add(info);
        return items;
    }

    private void procedureInfo() {
        ShellDamengViewFactory.procedureInfo(this);
    }

    @Override
    public void delete() {
        if (!MessageBox.confirm(I18nHelper.deleteProcedure() + " " + this.value.getName() + "?")) {
            return;
        }
        try {
            this.dbItem().dropProcedure(this.value);
            super.remove();
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    public DamengSchemaTreeItem dbItem() {
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
        DamengEventUtil.designProcedure(this.value, this.dbItem());
    }

    public String procedureName() {
        return this.value.getName();
    }
}
