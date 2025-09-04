package cn.oyzh.easyshell.trees.query;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.store.ShellQueryStore;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeCell;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.menu.MenuItemAdapter;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 代码查询树
 *
 * @author oyzh
 * @since 2025-06-11
 */
public class ShellQueryTreeView extends RichTreeView implements MenuItemAdapter {

    /**
     * 连接id
     */
    private String iid;

    /**
     * 查询存储
     */
    private final ShellQueryStore queryStore = ShellQueryStore.INSTANCE;

    @Override
    protected void initTreeView() {
        this.setCellFactory((Callback<TreeView<?>, TreeCell<?>>) param -> new RichTreeCell<>());
        super.initTreeView();
    }

    @Override
    protected void initRoot() {
        super.setRoot(new ShellQueryRootTreeItem(this));
        // this.root().expend();
        // super.initRoot();
    }

    /**
     * 新回调
     */
    private Consumer<ShellQuery> addCallback;

    /**
     * 编辑回调
     */
    private Consumer<ShellQuery> editCallback;

    /**
     * 删除回调
     */
    private Consumer<ShellQuery> deleteCallback;

    public Consumer<ShellQuery> getAddCallback() {
        return addCallback;
    }

    public void setAddCallback(Consumer<ShellQuery> addCallback) {
        this.addCallback = addCallback;
    }

    public Consumer<ShellQuery> getEditCallback() {
        return editCallback;
    }

    public void setEditCallback(Consumer<ShellQuery> editCallback) {
        this.editCallback = editCallback;
    }

    public Consumer<ShellQuery> getDeleteCallback() {
        return deleteCallback;
    }

    public void setDeleteCallback(Consumer<ShellQuery> deleteCallback) {
        this.deleteCallback = deleteCallback;
    }

    /**
     * 新增查询
     */
    public void addQuery() {
        if (this.addCallback != null) {
            String name = MessageBox.prompt(I18nHelper.pleaseInputName());
            if (StringUtil.isBlank(name)) {
                return;
            }
            ShellQuery query = new ShellQuery();
            query.setName(name);
            query.setIid(this.iid);
            this.addQuery(query);
            this.queryStore.insert(query);
            this.addCallback.accept(query);
        }
    }

    /**
     * 新增查询
     *
     * @param query 查询
     */
    public void addQuery(ShellQuery query) {
        this.root().addChild(new ShellQueryTreeItem(query, this));
    }

    /**
     * 编辑查询
     *
     * @param query 查询
     */
    public void editQuery(ShellQuery query) {
        if (this.editCallback != null) {
            this.editCallback.accept(query);
        }
    }

    /**
     * 删除查询
     *
     * @param query 查询
     */
    public void deleteQuery(ShellQuery query) {
        if (this.deleteCallback != null) {
            this.deleteCallback.accept(query);
        }
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>(12);
        FXMenuItem addQuery = MenuItemHelper.addQuery("12", this::addQuery);
        items.add(addQuery);
        return items;
    }

    public void setIid(String iid) {
        this.iid = iid;
        this.root().loadChild();
        this.root().expend();
    }

    public String getIid() {
        return iid;
    }
}
