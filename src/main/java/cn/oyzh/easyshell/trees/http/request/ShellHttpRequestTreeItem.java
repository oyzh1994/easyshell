package cn.oyzh.easyshell.trees.http.request;

import cn.oyzh.easyshell.trees.http.ShellHttpTreeItem;
import cn.oyzh.easyshell.trees.http.ShellHttpTreeView;
import cn.oyzh.easyshell.domain.http.ShellHttpRequest;
import cn.oyzh.easyshell.store.http.ShellHttpRequestStore;
import cn.oyzh.easyshell.trees.http.collection.ShellHttpCollectionTreeItem;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.gui.svg.glyph.CopySVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.DeleteSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.OpenSVGGlyph;
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
 * HTTP 请求节点。
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellHttpRequestTreeItem extends ShellHttpTreeItem<ShellHttpRequestTreeItemValue> {

    /**
     * 请求
     */
    private final ShellHttpRequest request;

    /**
     * 构造请求节点
     *
     * @param request  请求
     * @param treeView 树视图
     */
    public ShellHttpRequestTreeItem(ShellHttpRequest request, ShellHttpTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.request = request;
        this.setValue(new ShellHttpRequestTreeItemValue(this));
    }

    @Override
    public Object data() {
        return this.request;
    }

    @Override
    public boolean allowDrag() {
        return true;
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        items.add(new FXMenuItem(new OpenSVGGlyph(), I18nHelper.open(), this::open));
        items.add(new FXMenuItem(new CopySVGGlyph(), I18nHelper.clone1(), this::duplicate));
        items.add(new FXMenuItem(new RenameSVGGlyph(), I18nHelper.rename(), this::rename));
        items.add(new FXMenuItem(new DeleteSVGGlyph(), I18nHelper.delete(), this::delete));
        return items;
    }

    /**
     * 打开请求
     */
    public void open() {
        this.tabPane().openRequest(this);
    }

    /**
     * 复制请求
     */
    public void duplicate() {
        ShellHttpRequest copy = this.request.copy();
        copy.setName(this.request.getName() + " " + ShellI18nHelper.httpCopySuffix());
        copy.setCollectionId(this.request.getCollectionId());
        if (!ShellHttpRequestStore.INSTANCE.insert(copy)) {
            MessageBox.warn(I18nHelper.operationFail(), this.getWindow());
            return;
        }
        List<ShellHttpRequest> requests = this.requestList();
        int index = requests.indexOf(this.request);
        if (index >= 0) {
            requests.add(index + 1, copy);
        } else {
            requests.add(copy);
        }
        this.reloadTree();
        this.getTreeView().selectData(copy);
        this.tabPane().refreshHomeTab();
    }

    /**
     * 重命名
     */
    @Override
    public void rename() {
        String name = MessageBox.prompt(I18nHelper.pleaseInputName(), this.request.getName(), this.getWindow());
        if (name == null || Objects.equals(name, this.request.getName())) {
            return;
        }
        if (name.isBlank()) {
            MessageBox.warn(I18nHelper.pleaseInputName(), this.getWindow());
            return;
        }
        this.request.setName(name.trim());
        if (!ShellHttpRequestStore.INSTANCE.update(this.request)) {
            MessageBox.warn(I18nHelper.operationFail(), this.getWindow());
            return;
        }
        this.refreshRequest();
        this.tabPane().renameRequestTab(this.request.getId(), this.request.getName());
    }

    /**
     * 删除
     */
    @Override
    public void delete() {
        if (!MessageBox.confirm(ShellI18nHelper.httpConfirmDelete(this.request.getName()), this.getWindow())) {
            return;
        }
        this.tabPane().closeRequestTab(this.request.getId());
        ShellHttpRequestStore.INSTANCE.delete(this.request);
        this.requestList().remove(this.request);
        this.reloadTree();
        this.getTreeView().clearSelection();
        this.tabPane().refreshHomeTab();
    }

    /**
     * 获取请求列表
     *
     * @return 请求列表
     */
    private List<ShellHttpRequest> requestList() {
        if (((TreeItem<?>) this.getParent()) instanceof ShellHttpCollectionTreeItem collectionItem) {
            return collectionItem.collection().getRequests();
        }
        return new ArrayList<>();
    }

    /**
     * 刷新请求节点
     */
    public void refreshRequest() {
        this.setValue(new ShellHttpRequestTreeItemValue(this));
    }

    /**
     * 获取请求
     *
     * @return 请求
     */
    public ShellHttpRequest request() {
        return request;
    }

    @Override
    public void onPrimaryDoubleClick() {
        this.open();
    }

    @Override
    public void onPrimarySingleClick() {
        this.open();
    }
}
