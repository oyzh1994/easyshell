package cn.oyzh.easyshell.trees.redis.query;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.redis.ShellRedisQuery;
import cn.oyzh.easyshell.store.redis.RedisQueryStore;
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
public class ShellRedisQueryTreeView extends RichTreeView implements MenuItemAdapter {

    /**
     * 查询存储
     */
    private final RedisQueryStore queryStore = RedisQueryStore.INSTANCE;

    @Override
    protected void initTreeView() {
        this.setCellFactory((Callback<TreeView<?>, TreeCell<?>>) param -> new RichTreeCell<>());
        super.initTreeView();
    }

    @Override
    protected void initRoot() {
        super.setRoot(new ShellRedisQueryRootTreeItem(this));
        this.root().expend();
        super.initRoot();
    }

    /**
     * 新回调
     */
    private Consumer<ShellRedisQuery> addCallback;

    /**
     * 编辑回调
     */
    private Consumer<ShellRedisQuery> editCallback;

    /**
     * 删除回调
     */
    private Consumer<ShellRedisQuery> deleteCallback;

    public Consumer<ShellRedisQuery> getAddCallback() {
        return addCallback;
    }

    public void setAddCallback(Consumer<ShellRedisQuery> addCallback) {
        this.addCallback = addCallback;
    }

    public Consumer<ShellRedisQuery> getEditCallback() {
        return editCallback;
    }

    public void setEditCallback(Consumer<ShellRedisQuery> editCallback) {
        this.editCallback = editCallback;
    }

    public Consumer<ShellRedisQuery> getDeleteCallback() {
        return deleteCallback;
    }

    public void setDeleteCallback(Consumer<ShellRedisQuery> deleteCallback) {
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
            ShellRedisQuery query = new ShellRedisQuery();
            query.setName(name);
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
    public void addQuery(ShellRedisQuery query) {
        this.root().addChild(new ShellRedisQueryTreeItem(query, this));
    }

    /**
     * 编辑查询
     *
     * @param query 查询
     */
    public void editQuery(ShellRedisQuery query) {
        if (this.editCallback != null) {
            this.editCallback.accept(query);
        }
    }

    /**
     * 删除查询
     *
     * @param query 查询
     */
    public void deleteQuery(ShellRedisQuery query) {
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
}
