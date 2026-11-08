package cn.oyzh.easyshell.trees.http.collection;

import cn.oyzh.easyshell.trees.http.ShellHttpTreeItem;
import cn.oyzh.easyshell.trees.http.ShellHttpTreeView;
import cn.oyzh.easyshell.domain.http.ShellHttpCollection;
import cn.oyzh.easyshell.domain.http.ShellHttpRequest;
import cn.oyzh.easyshell.store.http.ShellHttpCollectionStore;
import cn.oyzh.easyshell.store.http.ShellHttpRequestStore;
import cn.oyzh.fx.plus.drag.DragNodeItem;
import cn.oyzh.easyshell.trees.http.request.ShellHttpRequestTreeItem;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.gui.svg.glyph.AddSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.DeleteSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.RenameSVGGlyph;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * HTTP 请求集合节点。
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellHttpCollectionTreeItem extends ShellHttpTreeItem<ShellHttpCollectionTreeItemValue> {

    /**
     * 集合
     */
    private final ShellHttpCollection collection;

    /**
     * 构造集合节点
     *
     * @param collection 集合
     * @param treeView   树视图
     */
    public ShellHttpCollectionTreeItem(ShellHttpCollection collection, ShellHttpTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.collection = collection;
        this.setValue(new ShellHttpCollectionTreeItemValue(this));
        this.loadChild();
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        items.add(new FXMenuItem(new AddSVGGlyph(), ShellI18nHelper.httpAddRequest(), this::addRequest));
        items.add(new FXMenuItem(new RenameSVGGlyph(), I18nHelper.rename(), this::rename));
        items.add(new FXMenuItem(new DeleteSVGGlyph(), I18nHelper.delete(), this::delete));
        return items;
    }

    @Override
    public Object data() {
        return this.collection;
    }

    @Override
    public void loadChild() {
        List<TreeItem<?>> items = new ArrayList<>();
        for (ShellHttpRequest request : this.collection.getRequests()) {
            items.add(new ShellHttpRequestTreeItem(request, this.getTreeView()));
        }
        this.setChild(items);
        this.setLoaded(true);
    }

    @Override
    public void reloadChild() {
        this.clearChild();
        this.loadChild();
    }

    /**
     * 新增请求
     */
    public void addRequest() {
        String name = MessageBox.prompt(I18nHelper.pleaseInputName(), ShellI18nHelper.httpAddRequest(), this.getWindow());
        if (name == null) {
            return;
        }
        if (name.isBlank()) {
            MessageBox.warn(I18nHelper.pleaseInputName(), this.getWindow());
            return;
        }
        ShellHttpRequest request = new ShellHttpRequest();
        request.setIid(this.collection.getIid());
        request.setCollectionId(this.collection.getId());
        request.setName(name.trim());
        if (!ShellHttpRequestStore.INSTANCE.insert(request)) {
            MessageBox.warn(I18nHelper.operationFail(), this.getWindow());
            return;
        }
        this.collection.getRequests().add(request);
        this.reloadTree();
        this.getTreeView().selectData(request);
        this.tabPane().refreshHomeTab();
    }

    /**
     * 重命名
     */
    @Override
    public void rename() {
        String name = MessageBox.prompt(I18nHelper.pleaseInputName(), this.collection.getName(), this.getWindow());
        if (name == null || Objects.equals(name, this.collection.getName())) {
            return;
        }
        if (name.isBlank()) {
            MessageBox.warn(I18nHelper.pleaseInputName(), this.getWindow());
            return;
        }
        this.collection.setName(name.trim());
        if (!ShellHttpCollectionStore.INSTANCE.update(this.collection)) {
            MessageBox.warn(I18nHelper.operationFail(), this.getWindow());
            return;
        }
        this.getTreeView().reload();
        this.getTreeView().selectData(this.collection);
    }

    /**
     * 删除
     */
    @Override
    public void delete() {
        if (!MessageBox.confirm(ShellI18nHelper.httpConfirmDelete(this.collection.getName()), this.getWindow())) {
            return;
        }
        this.tabPane().closeCollectionTabs(this.collection);
        ShellHttpRequestStore.INSTANCE.deleteByCollectionId(this.collection.getId());
        ShellHttpCollectionStore.INSTANCE.delete(this.collection);
        this.workspace().getCollections().remove(this.collection);
        this.reloadTree();
        this.getTreeView().clearSelection();
        this.tabPane().refreshHomeTab();
    }

    @Override
    public boolean allowDrop() {
        return true;
    }

    @Override
    public boolean allowDropNode(DragNodeItem item) {
        if (!(item instanceof ShellHttpRequestTreeItem requestItem)) {
            return false;
        }
        if (!(((TreeItem<?>) requestItem.getParent()) instanceof ShellHttpCollectionTreeItem parent)) {
            return false;
        }
        return parent.collection() != this.collection;
    }

    @Override
    public void onDropNode(DragNodeItem item) {
        if (!(item instanceof ShellHttpRequestTreeItem requestItem)) {
            return;
        }
        if (!(((TreeItem<?>) requestItem.getParent()) instanceof ShellHttpCollectionTreeItem parent)) {
            return;
        }
        if (parent.collection() == this.collection) {
            return;
        }
        ShellHttpRequest request = requestItem.request();
        String oldCollectionId = request.getCollectionId();
        request.setCollectionId(this.collection.getId());
        if (!ShellHttpRequestStore.INSTANCE.update(request)) {
            request.setCollectionId(oldCollectionId);
            MessageBox.warn(I18nHelper.operationFail(), this.getWindow());
            return;
        }
        parent.collection().getRequests().remove(request);
        this.collection.getRequests().add(request);
        this.reloadTree();
        ShellHttpCollectionsTreeItem category = this.getTreeView().collectionsItem();
        if (category != null) {
            category.setExpanded(true);
            ShellHttpCollectionTreeItem collectionItem = category.collectionItem(this.collection.getId());
            if (collectionItem != null) {
                collectionItem.setExpanded(true);
            }
        }
        this.getTreeView().selectData(request);
        this.tabPane().refreshHomeTab();
    }

    /**
     * 获取集合
     *
     * @return 集合
     */
    public ShellHttpCollection collection() {
        return collection;
    }
}
