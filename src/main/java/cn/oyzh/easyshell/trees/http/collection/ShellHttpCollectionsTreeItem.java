package cn.oyzh.easyshell.trees.http.collection;

import cn.oyzh.easyshell.trees.http.ShellHttpTreeItem;
import cn.oyzh.easyshell.trees.http.ShellHttpTreeView;
import cn.oyzh.easyshell.domain.http.ShellHttpCollection;
import cn.oyzh.easyshell.store.http.ShellHttpCollectionStore;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.gui.svg.glyph.AddSVGGlyph;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.List;

/**
 * HTTP 请求集合分类节点。
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellHttpCollectionsTreeItem extends ShellHttpTreeItem<ShellHttpCollectionsTreeItemValue> {

    /**
     * 构造集合分类节点
     *
     * @param treeView 树视图
     */
    public ShellHttpCollectionsTreeItem(ShellHttpTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        super.setSortable(false);
        this.setValue(new ShellHttpCollectionsTreeItemValue(this));
    }

    @Override
    public Object data() {
        return null;
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        items.add(new FXMenuItem(new AddSVGGlyph(), ShellI18nHelper.httpAddCollection(), this::addCollection));
        return items;
    }

    @Override
    public void loadChild() {
        List<TreeItem<?>> items = new ArrayList<>();
        for (ShellHttpCollection collection : this.workspace().getCollections()) {
            items.add(new ShellHttpCollectionTreeItem(collection, this.getTreeView()));
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
     * 获取集合节点
     *
     * @param collectionId 集合id
     * @return 集合节点
     */
    public ShellHttpCollectionTreeItem collectionItem(String collectionId) {
        for (TreeItem<?> child : this.unfilteredChildren()) {
            if (child instanceof ShellHttpCollectionTreeItem item
                    && collectionId != null
                    && collectionId.equals(item.collection().getId())) {
                return item;
            }
        }
        return null;
    }

    /**
     * 新增集合
     */
    public void addCollection() {
        String name = MessageBox.prompt(I18nHelper.pleaseInputName(), I18nHelper.addCollection(), this.getWindow());
        if (name == null) {
            return;
        }
        if (name.isBlank()) {
            MessageBox.warn(I18nHelper.pleaseInputName(), this.getWindow());
            return;
        }
        ShellHttpCollection collection = new ShellHttpCollection();
        collection.setIid(this.workspace().getIid());
        collection.setName(name.trim());
        if (!ShellHttpCollectionStore.INSTANCE.insert(collection)) {
            MessageBox.warn(I18nHelper.operationFail(), this.getWindow());
            return;
        }
        this.workspace().getCollections().add(collection);
        this.reloadTree();
        this.getTreeView().selectData(collection);
        this.tabPane().refreshHomeTab();
    }
}
