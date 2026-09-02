package cn.oyzh.easyshell.trees.dameng.view;

import cn.oyzh.common.dto.Paging;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.easyshell.dameng.column.DamengColumn;
import cn.oyzh.easyshell.dameng.column.DamengColumns;
import cn.oyzh.easyshell.dameng.record.DamengDeleteRecordParam;
import cn.oyzh.easyshell.dameng.record.DamengInsertRecordParam;
import cn.oyzh.easyshell.dameng.record.DamengRecord;
import cn.oyzh.easyshell.dameng.record.DamengRecordData;
import cn.oyzh.easyshell.dameng.record.DamengRecordFilter;
import cn.oyzh.easyshell.dameng.record.DamengRecordPrimaryKey;
import cn.oyzh.easyshell.dameng.record.DamengSelectRecordParam;
import cn.oyzh.easyshell.dameng.record.DamengUpdateRecordParam;
import cn.oyzh.easyshell.dameng.view.DamengView;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.dameng.ShellDamengEventUtil;
import cn.oyzh.easyshell.trees.dameng.DBTreeItem;
import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.easyshell.util.dameng.ShellDamengViewFactory;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
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
public class ShellDamengViewTreeItem extends DBTreeItem<ShellDamengViewTreeItemValue> {

    /**
     * 当前值
     */
    private final DamengView value;

    public DamengView value() {
        return value;
    }

    public ShellDamengViewTreeItem(DamengView view, RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.value = view;
        this.setValue(new ShellDamengViewTreeItemValue(this));
    }

    @Override
    public ShellDamengViewsTreeItem parent() {
        return (ShellDamengViewsTreeItem) super.parent();
    }


    public ShellDamengClient client() {
        return this.parent().client();
    }

    public String schema() {
        return this.parent().schema();
    }

    /**
     * 获取redis信息
     *
     * @return redis信息
     */
    public ShellConnect info() {
        return this.parent().info();
    }

    public DamengColumns viewColumns() {
        this.value.setColumns(new DamengColumns(this.columns()));
        return this.value.getColumns();
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        FXMenuItem open = MenuItemHelper.openView(this::onPrimaryDoubleClick);
        items.add(open);
        FXMenuItem design = MenuItemHelper.designView(this::designView);
        items.add(design);
        FXMenuItem renameView = MenuItemHelper.renameView(this::rename);
        items.add(renameView);
        items.add(MenuItemHelper.separator());
        FXMenuItem delete = MenuItemHelper.deleteView(this::delete);
        items.add(delete);
        FXMenuItem info = MenuItemHelper.viewInfo(this::viewInfo);
        items.add(info);
        return items;
    }

    private void viewInfo() {
        //         StageAdapter fxView = StageManager.parseStage(DamengViewInfoController.class, this.window());
        //         fxView.setProp("item", this);
        //         fxView.display();
        ShellDamengViewFactory.viewInfo(this);
    }

    private void designView() {
        ShellDamengEventUtil.designView(this.value, this.dbItem());
    }

    @Override
    public void delete() {
        if (!MessageBox.confirm(I18nHelper.deleteView() + " " + this.value.getName() + "?")) {
            return;
        }
        try {
            this.dbItem().dropView(this.value);
            super.remove();
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    public ShellDamengSchemaTreeItem dbItem() {
        return this.parent().parent();
    }

    public Paging<DamengRecord> recordPage(long pageNo, long limit, List<DamengRecordFilter> filters, List<DamengColumn> columns) {
        DamengSelectRecordParam param = new DamengSelectRecordParam();
        param.setLimit(limit);
        param.setFilters(filters);
        param.setColumns(columns);
        param.setStart(pageNo * limit);
        param.setSchema(this.schema());
        param.setTableName(this.viewName());
        List<DamengRecord> records = this.client().viewRecords(this.schema(), this.viewName(), pageNo * limit, limit, filters);
        long count = this.client().selectRecordCount(param);
        Paging<DamengRecord> paging = new Paging<>(records, limit, count);
        paging.currentPage(pageNo);
        return paging;
    }

    public String infoName() {
        return this.parent().infoName();
    }

    public DamengColumns columns() {
        return new DamengColumns(this.client().viewColumns(this.schema(), this.viewName()));
    }

    @Override
    public void onPrimaryDoubleClick() {
        ShellDamengEventUtil.viewOpen(this, this.dbItem());
    }

    /**
     * 获取主键列，优先返回自动递增列
     *
     * @return 主键列
     */
    public DamengColumn getPrimaryKey() {
        if (this.value.getColumns() == null) {
            this.viewColumns();
        }
        DamengColumn dbColumn = null;
        if (this.value.columns() != null) {
            for (DamengColumn column : this.value.columns()) {
                if (column.isAutoIncrement()) {
                    dbColumn = column;
                    break;
                }
            }
        }
        return dbColumn;
    }

    public boolean isUpdatable() {
        return this.value.isUpdatable();
    }

    public String viewName() {
        return this.value.getName();
    }

    public int insertRecord(DamengRecordData recordData) {
        return this.insertRecord(recordData, null);
    }

    public int insertRecord(DamengRecordData recordData, DamengRecordPrimaryKey primaryKey) {
        DamengInsertRecordParam param = new DamengInsertRecordParam();
        param.setRecord(recordData);
        param.setSchema(this.schema());
        param.setPrimaryKey(primaryKey);
        param.setTableName(this.viewName());
        return this.client().insertRecord(param);
    }

    public int deleteRecord(DamengRecordData recordData) {
        DamengDeleteRecordParam param = new DamengDeleteRecordParam();
        param.setRecord(recordData);
        param.setSchema(this.schema());
        param.setTableName(this.viewName());
        return this.client().deleteRecord(param);
    }

    public int deleteRecord(DamengRecordPrimaryKey primaryKey) {
        DamengDeleteRecordParam param = new DamengDeleteRecordParam();
        param.setSchema(this.schema());
        param.setTableName(this.viewName());
        param.setPrimaryKey(primaryKey);
        return this.client().deleteRecord(param);
    }

    public DamengRecord selectRecord(DamengRecordPrimaryKey primaryKey) {
        DamengSelectRecordParam param = new DamengSelectRecordParam();
        param.setSchema(this.schema());
        param.setTableName(this.viewName());
        param.setPrimaryKey(primaryKey);
        return this.client().selectRecord(param);
    }

    public int updateRecord(DamengRecordData recordData, DamengRecordPrimaryKey primaryKey) {
        DamengUpdateRecordParam param = new DamengUpdateRecordParam();
        param.setSchema(this.schema());
        param.setTableName(this.viewName());
        param.setPrimaryKey(primaryKey);
        param.setUpdateRecord(recordData);
        return this.client().updateRecord(param);
    }

    public int updateRecord(DamengRecordData recordData, DamengRecordData originalRecordData) {
        DamengUpdateRecordParam param = new DamengUpdateRecordParam();
        param.setSchema(this.schema());
        param.setTableName(this.viewName());
        param.setUpdateRecord(recordData);
        param.setRecord(originalRecordData);
        return this.client().updateRecord(param);
    }

    @Override
    public void rename() {
        try {
            String viewName = MessageBox.prompt(I18nHelper.pleaseInputName(), this.viewName());
            // 名称为null或者跟当前名称相同，则忽略
            if (viewName == null || Objects.equals(viewName, this.viewName())) {
                return;
            }
            // 检查名称
            if (StringUtil.isBlank(viewName)) {
                MessageBox.warn(I18nHelper.pleaseInputContent());
                return;
            }
            String oldName = this.viewName();
            // 修改名称
            this.dbItem().renameTable(oldName, viewName);
            this.value.setName(viewName);
            this.refresh();
            ShellDamengEventUtil.viewRenamed(oldName, viewName, this.dbItem());
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }
}
