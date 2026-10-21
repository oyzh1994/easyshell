package cn.oyzh.easyshell.trees.dameng.sequence;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.easyshell.dameng.sequence.DamengSequence;
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
 * 达梦数据库树序列节点
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellDamengSequenceTreeItem extends ShellDamengTreeItem<ShellDamengSequenceTreeItemValue> {

    /**
     * 当前值
     */
    private final DamengSequence value;

    /**
     * 获取序列值
     *
     * @return 序列值
     */
    public DamengSequence value() {
        return value;
    }

    /**
     * 构造达梦数据库树序列节点
     *
     * @param Sequence 序列
     * @param treeView  树视图
     */
    public ShellDamengSequenceTreeItem(DamengSequence Sequence, RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.value = Sequence;
        this.setValue(new ShellDamengSequenceTreeItemValue(this));
    }

    @Override
    public ShellDamengSequencesTreeItem parent() {
        return (ShellDamengSequencesTreeItem) super.parent();
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
        FXMenuItem design = MenuItemHelper.designSequence(this::onPrimaryDoubleClick);
        items.add(design);
        FXMenuItem renameSequence = MenuItemHelper.renameSequence(this::rename);
        items.add(renameSequence);
        FXMenuItem delete = MenuItemHelper.deleteSequence(this::delete);
        items.add(delete);
        items.add(MenuItemHelper.separator());
        FXMenuItem cloneSequence = MenuItemHelper.cloneSequence(this::cloneSequence);
        items.add(cloneSequence);
        FXMenuItem info = MenuItemHelper.sequenceInfo(this::SequenceInfo);
        items.add(info);
        return items;
    }

    /**
     * 序列信息
     */
    private void SequenceInfo() {
        ShellDamengViewFactory.sequenceInfo(this);
    }

    /**
     * 克隆序列
     */
    private void cloneSequence() {
        StageManager.showMask(this.getWindow(), this::doCloneSequence);
    }

    /**
     * 执行克隆序列
     */
    private void doCloneSequence() {
        try {
            String cloneSequence = this.sequenceName() + DBUtil.genCloneName();
            this.dbItem().cloneSequence(this.sequenceName(), cloneSequence);
            DamengSequence Sequence = this.dbItem().selectSequence(cloneSequence);
            this.dbItem().getSequenceTypeChild().addSequence(Sequence);
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    @Override
    public void delete() {
        if (!MessageBox.confirm(I18nHelper.deleteSequence() + " " + this.value.getName() + "?", this.getWindow())) {
            return;
        }
        try {
            this.dbItem().dropSequence(this.value);
            ShellDamengEventUtil.dropSequence(this);
            this.parent().clearSequenceSize();
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
    public ShellDamengSchemaTreeItem dbItem() {
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
        ShellDamengEventUtil.designSequence(this.value, this.dbItem());
    }

    /**
     * 获取序列名称
     *
     * @return 序列名称
     */
    public String sequenceName() {
        return this.value.getName();
    }

    @Override
    public void rename() {
        try {
            String newName = MessageBox.prompt(I18nHelper.pleaseInputName(), this.sequenceName(), this.getWindow());
            // 名称为null或者跟当前名称相同，则忽略
            if (newName == null || Objects.equals(newName, this.sequenceName())) {
                return;
            }
            // 检查名称
            if (StringUtil.isBlank(newName)) {
                MessageBox.warn(I18nHelper.pleaseInputContent(), this.getWindow());
                return;
            }
            String oldName = this.sequenceName();
            // 修改名称
            this.value.setName(newName);
            this.dbItem().renameSequence(oldName, newName);
            this.refresh();
            ShellDamengEventUtil.sequenceRenamed(oldName, newName, this.dbItem());
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, this.getWindow());
        }
    }
}
