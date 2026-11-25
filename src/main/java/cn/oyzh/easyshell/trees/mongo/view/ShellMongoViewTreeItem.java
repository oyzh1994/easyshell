package cn.oyzh.easyshell.trees.mongo.view;

import cn.oyzh.common.dto.Paging;
import cn.oyzh.easyshell.event.mongo.ShellMongoEventUtil;
import cn.oyzh.easyshell.mongo.ShellMongoClient;
import cn.oyzh.easyshell.mongo.column.MongoColumns;
import cn.oyzh.easyshell.mongo.record.MongoRecord;
import cn.oyzh.easyshell.mongo.record.MongoRecordFilter;
import cn.oyzh.easyshell.mongo.record.MongoSelectRecordParam;
import cn.oyzh.easyshell.mongo.view.MongoView;
import cn.oyzh.easyshell.trees.mongo.ShellMongoTreeItem;
import cn.oyzh.easyshell.trees.mongo.database.ShellMongoDatabaseTreeItem;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.easyshell.util.mongo.ShellMongoViewFactory;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import javafx.scene.control.MenuItem;
import java.util.ArrayList;
import java.util.List;

/**
 * MongoDB 视图节点。
 *
 * @author oyzh
 * @since 2026-11-25
 */
public class ShellMongoViewTreeItem extends ShellMongoTreeItem<ShellMongoViewTreeItemValue> {

    private final MongoView value;

    public ShellMongoViewTreeItem(MongoView view, RichTreeView treeView) {
        super(treeView);
        this.value = view;
        this.setValue(new ShellMongoViewTreeItemValue(this));
    }

    @Override
    public ShellMongoViewsTreeItem parent() {
        return (ShellMongoViewsTreeItem) super.parent();
    }

    public MongoView value() {
        return value;
    }

    public String viewName() {
        return this.value.getName();
    }

    public String dbName() {
        return this.parent().dbName();
    }

    public String connectName() {
        return this.parent().connectName();
    }

    public ShellMongoDatabaseTreeItem dbItem() {
        return this.parent() == null ? null : this.parent().parent();
    }

    public ShellMongoClient client() {
        return this.parent().client();
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        FXMenuItem open = MenuItemHelper.openView(this::onPrimaryDoubleClick);
        FXMenuItem design = MenuItemHelper.designView(this::design);
        FXMenuItem info = MenuItemHelper.viewInfo(this::viewInfo);
        FXMenuItem delete = MenuItemHelper.deleteView(this::delete);
        FXMenuItem export = MenuItemHelper.exportData(this::export);
        items.add(open);
        items.add(design);
        items.add(delete);
        items.add(MenuItemHelper.separator());
        items.add(export);
        items.add(info);
        return items;
    }

    @Override
    public void onPrimaryDoubleClick() {
        ShellMongoEventUtil.viewOpen(this, this.dbItem());
    }

    private void design() {
        ShellMongoEventUtil.designView(this.value, this.dbItem());
    }

    private void viewInfo() {
        ShellMongoViewFactory.viewInfo(this);
    }

    private void export() {
        ShellMongoViewFactory.exportData(this.client(), this.dbName(), this.viewName());
    }

    @Override
    public void delete() {
        try {
            if (MessageBox.confirm(ShellI18nHelper.confirmDeleteView(this.viewName()), this.getWindow())) {
                this.client().dropView(this.dbName(), this.viewName());
                ShellMongoEventUtil.viewDropped(this, this.dbItem());
                this.parent().clearViewSize();
                this.remove();
            }
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    public Paging<MongoRecord> recordPage(long pageNo, long limit,
                                          List<MongoRecordFilter> filters, MongoColumns columns) throws Exception {
        MongoSelectRecordParam param = new MongoSelectRecordParam();
        param.setLimit(limit);
        param.setFilters(filters);
        param.setColumns(columns);
        param.setDbName(this.dbName());
        param.setStart(pageNo * limit);
        param.setCollectionName(this.viewName());
        List<MongoRecord> rows = this.client().selectCollectionRecords(param);
        long count = this.client().selectCollectionRecordCount(param);
        Paging<MongoRecord> paging = new Paging<>(rows, limit, count);
        paging.currentPage(pageNo);
        return paging;
    }
}
